package org.pjb.model

import cats.effect.IO
import org.pjb.model.inbound.{Event => InboundEvent, Market => InboundMarket}
import org.pjb.model.state.Event
import org.pjb.model.utils.EventUtils._
import org.pjb.model.version.Versioned.Ver

object Sample extends App {

  case class IncomingMessage(ver:Ver, inboundEvent:InboundEvent)

  type State = Event
  type Publish = Event

  val msg1 = IncomingMessage(1, InboundEvent(id = 3223, name = Some("p1 v p2"), List(InboundMarket(id = 4232, Some("WDW"), Some("SUSPENDED"), Some(20)))))
  val msg2 = IncomingMessage(2, InboundEvent(id = 3223, name = Some("p1 v p2"), List(InboundMarket(id = 4232, None, Some("ACTIVE"), None), InboundMarket(id = 7872, Some("FirstScore"), Some("SUSPENDED"), Some(20)))))
  val msg3 = IncomingMessage(3, InboundEvent(id = 3223, name = Some("Man Utd v Arsenal"), List.empty))

  val state = Event(3223)

  def merge: (IncomingMessage, Event) => Event = (incomingMessage, state) => state.mergeInput(incomingMessage.ver, incomingMessage.inboundEvent)
  def applyBusinessLogic(ev:Event)( implicit logic :Event => Event) : Event = logic(ev)
  def determinePublish: (Ver, Event) => Event =  (ver, ev) => ev.diffSince(ver)

  def process(incomingMessage: IncomingMessage, ev:Event)(implicit logic: Event => Event):IO[(Publish, State)] = {
    for {
      merged <- merge(incomingMessage, ev)
      state <- applyBusinessLogic(merged)(logic)
      publish <- determinePublish(incomingMessage.ver, state)
    } yield IO(publish, state)
  }

  implicit val someBusinessLogic : Event => Event = {
    e =>
      println("applying some logic")
      e
  }

  println(s"msg1=$msg1")
  val (publish1, state1) = process(msg1, state)(someBusinessLogic).unsafeRunSync
  println(s"publish1=$publish1")
  println(s"state1=$state1\n\n")

  println(s"msg2=$msg2")
  val (publish2, state2) = process(msg2, state1)(someBusinessLogic).unsafeRunSync
  println(s"publish2=$publish2")
  println(s"state2=$state2\n\n")

  println(s"msg3=$msg3")
  val (publish3, state3) = process(msg3, state2)(someBusinessLogic).unsafeRunSync
  println(s"publish3=$publish3")
  println(s"state3=$state3\n\n")

  println(state3.diffSince(2))

}
