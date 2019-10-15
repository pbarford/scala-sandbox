package org.pjb.actors

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, Entity, EntityTypeKey}
import akka.util.Timeout
import org.pjb.actors.TypedShardActor.Command

import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.concurrent.duration._

object TypedShardActorTest extends App {

  import akka.actor.typed.scaladsl.AskPattern._

  val system = ActorSystem[TypedShardActor.Command](TypedShardActor.behavior("id"), "actorSystem")
  val TypeKey = EntityTypeKey[Command]("Command")
  val sharding = ClusterSharding(system)
  val shardRegion: ActorRef[ShardingEnvelope[Command]] =
    sharding.init(Entity(typeKey = TypeKey, createBehavior = ctx => TypedShardActor.behavior(ctx.entityId)))

  shardRegion ! ShardingEnvelope("1", TypedShardActor.UpdateData("TESTING"))
  shardRegion ! ShardingEnvelope("2", TypedShardActor.UpdateData("TESTING"))
  shardRegion ! ShardingEnvelope("1", TypedShardActor.UpdateData("TESTING"))

  implicit val timeout: Timeout = 3.seconds
  implicit val scheduler = system.scheduler
  implicit val ec = system.executionContext
  val result: Future[TypedShardActor.State] = shardRegion.ask(ref => ShardingEnvelope("1", TypedShardActor.GetState(ref)))
  result.onComplete {
    case Success(r) => println(r)
    case Failure(ex) => println(ex.getMessage)
  }
}
