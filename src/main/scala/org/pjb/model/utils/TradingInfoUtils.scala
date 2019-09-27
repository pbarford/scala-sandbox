package org.pjb.model.utils

import org.pjb.model.inbound.{Market => InboundMarket}
import org.pjb.model.state.TradingInfo
import org.pjb.model.utils.VersionedUtils._
import org.pjb.model.version.{Empty, Populated}
import org.pjb.model.version.Versioned.Ver

object TradingInfoUtils {
  implicit class TradingInfoOps(ti: TradingInfo) {
    def mergeInput(ver:Long, im:InboundMarket):TradingInfo = {
      TradingInfo(ti.bettingStatus.merge(im.bettingStatus.toVersioned(ver)))
    }

    def diffSince(ver:Ver):Option[TradingInfo] = {
      ti.bettingStatus.diffSince(ver) match {
        case p@Populated(_, _) => Some(TradingInfo(p))
        case Empty => None
      }
    }
  }
}
