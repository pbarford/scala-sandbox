package org.pjb.actors

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors

object TypedActor {

  sealed trait Protocol
  case class UpdateData(data:String) extends Protocol
  case class GetState(replyTo: ActorRef[State]) extends Protocol

  case class State(version:Int, data:String) {
    def apply : UpdateData => State = {
      u => copy(data = u.data, version = this.version + 1)
    }
  }

  def handler(state:State): Behavior[Protocol] = Behaviors.receive { (context, message) =>
      message match {
        case u:UpdateData =>
          val s = state.apply(u)
          context.log.info(s"$s")
          handler(s)
        case GetState(replyTo) =>
          replyTo ! state
          Behaviors.same

      }
  }

  def behavior: Behavior[Protocol] = {
    Behaviors.setup { context =>
      val actor = context.spawn(TypedActor.handler(State(0, "")), "typed-actor")

      Behaviors.receiveMessage { command =>
        actor ! command
        Behaviors.same
      }

    }
  }

}
