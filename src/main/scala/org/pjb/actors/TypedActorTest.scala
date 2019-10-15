package org.pjb.actors

import akka.actor.typed.ActorSystem
import akka.util.Timeout
import org.pjb.actors.TypedActor.GetState

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}

object TypedActorTest extends App {

  import akka.actor.typed.scaladsl.AskPattern._
  val s = ActorSystem[TypedActor.Protocol](TypedActor.behavior, "typed-actor")

  s ! TypedActor.UpdateData("TESTING")
  s ! TypedActor.UpdateData("TESTING")
  s ! TypedActor.UpdateData("TESTING")

  implicit val timeout: Timeout = 3.seconds
  implicit val scheduler = s.scheduler
  implicit val ec = s.executionContext
  val result: Future[TypedActor.State] = s.ask(ref => GetState(ref))
  result.onComplete {
    case Success(r) => println(r)
    case Failure(ex) => println(ex.getMessage)
  }

}
