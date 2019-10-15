package org.pjb.actors

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior, RetentionCriteria}
import akka.persistence.typed.scaladsl.EventSourcedBehavior.CommandHandler

object PTypedActor {

  sealed trait Command
  case class UpdateData(data:String) extends PTypedActor.Command
  case class GetState(replyTo: ActorRef[State]) extends PTypedActor.Command

  sealed trait Event
  case class DataPersisted(data:String) extends PTypedActor.Event

  case class State(version:Int, data:String) {
    def apply : (ActorContext[Command], PTypedActor.Event) => PTypedActor.State = {
      case (c, p:PTypedActor.DataPersisted) =>
        val u = copy(data = p.data, version = this.version + 1)
        c.log.info(s"APPLY [${c.self.path.name}] : $u")
        u

    }
  }

  private def commandHandler(context:ActorContext[PTypedActor.Command]): CommandHandler[Command, Event, State] = { (state, cmd) =>
    cmd match {
      case u:UpdateData =>
        context.log.info(s"$u")
        Effect.persist(DataPersisted(u.data))
      case GetState(replyTo) =>
        replyTo ! state
        Effect.none
    }
  }

  def behavior(id: String): Behavior[Command] = {
    Behaviors.setup { context =>
      EventSourcedBehavior[Command, Event, State](
        persistenceId = PersistenceId(id),
        emptyState = State(0, ""),
        commandHandler(context),
        eventHandler = (state, event) => state.apply(context, event))
        .snapshotWhen {
          case (State(_,_), DataPersisted(_), _) => true
        }
        .withRetention(RetentionCriteria.snapshotEvery(numberOfEvents = 2, keepNSnapshots = 1))
    }

  }

}