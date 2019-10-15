package org.pjb.actors

import akka.actor.typed.ActorSystem
import akka.util.Timeout

import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.concurrent.duration._

object PTypedActorTest extends App {

  import akka.actor.typed.scaladsl.AskPattern._
  val system = ActorSystem[PTypedActor.Command](PTypedActor.behavior("id"), "actorSystem")

  system ! PTypedActor.UpdateData("TESTING")
  system ! PTypedActor.UpdateData("TESTING")
  system ! PTypedActor.UpdateData("TESTING")

  implicit val timeout: Timeout = 3.seconds
  implicit val scheduler = system.scheduler
  implicit val ec = system.executionContext
  val result: Future[PTypedActor.State] = system.ask(ref => PTypedActor.GetState(ref))
  result.onComplete {
    case Success(r) => println(r)
    case Failure(ex) => println(ex.getMessage)
  }
}
