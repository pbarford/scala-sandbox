package org.pjb.actors

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, Entity, EntityTypeKey}
import akka.util.Timeout

import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.concurrent.duration._

object PTypedActorShardTest extends App {

  import akka.actor.typed.scaladsl.AskPattern._

  val system = ActorSystem[PTypedActor.Command](PTypedActor.behavior("id"), "actorSystem")
  val TypeKey = EntityTypeKey[PTypedActor.Command]("Command")
  val sharding = ClusterSharding(system)
  val shardRegion: ActorRef[ShardingEnvelope[PTypedActor.Command]] =
    sharding.init(Entity(typeKey = TypeKey, createBehavior = ctx => PTypedActor.behavior(ctx.entityId)))

  shardRegion ! ShardingEnvelope("1", PTypedActor.UpdateData("TESTING"))
  shardRegion ! ShardingEnvelope("2", PTypedActor.UpdateData("TESTING"))
  shardRegion ! ShardingEnvelope("1", PTypedActor.UpdateData("TESTING"))

  implicit val timeout: Timeout = 3.seconds
  implicit val scheduler = system.scheduler
  implicit val ec = system.executionContext
  val result: Future[PTypedActor.State] = shardRegion.ask(ref => ShardingEnvelope("1", PTypedActor.GetState(ref)))
  result.onComplete {
    case Success(r) => println(r)
    case Failure(ex) => println(ex.getMessage)
  }
}
