package org.pjb.actors

import akka.actor.typed.ActorSystem
import akka.util.Timeout

import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.concurrent.duration._

object PTypedActorTest extends App {

  import akka.actor.typed.scaladsl.AskPattern._
  val s = ActorSystem[PTypedActor.Command](PTypedActor.behavior("id"), "ptyped-actor")

  s ! PTypedActor.UpdateData("TESTING")
  s ! PTypedActor.UpdateData("TESTING")
  s ! PTypedActor.UpdateData("TESTING")

  implicit val timeout: Timeout = 3.seconds
  implicit val scheduler = s.scheduler
  implicit val ec = s.executionContext
  val result: Future[PTypedActor.State] = s.ask(ref => PTypedActor.GetState(ref))
  result.onComplete {
    case Success(r) => println(r)
    case Failure(ex) => println(ex.getMessage)
  }
}
