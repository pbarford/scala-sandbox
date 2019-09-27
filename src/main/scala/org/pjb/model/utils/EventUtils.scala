package org.pjb.model.utils

import org.pjb.model.inbound.{Event => InboundEvent}
import org.pjb.model.state.{Event, ProductOffering}
import org.pjb.model.utils.ProductOfferingUtils._
import org.pjb.model.utils.VersionedUtils._
import org.pjb.model.version.Versioned.Ver

object EventUtils {
  implicit class EventOps(ev: Event) {
    def mergeInput(ver: Ver, input: InboundEvent): Event = {
      val po = ev.productOffering match {
        case Some(po) => Some(po.mergeInboundMarkets(ver, input.markets))
          case None => Some(ProductOffering().mergeInboundMarkets(ver, input.markets))
      }
      Event(ev.id, ev.name.merge(input.name.toVersioned(ver)), po)
    }

    def diffSince(ver:Ver) : Event = {
      Event(ev.id, ev.name.diffSince(ver), ev.productOffering.map(_.diffSince(ver)))
    }

    def map[B](f: Event => B): B = f(ev)
    def flatMap[B](f: Event => B):B = map(f)
  }
}
