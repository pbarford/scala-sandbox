package org.pjb.model.utils

import org.pjb.model.inbound.{Market => InboundMarket}
import org.pjb.model.state.{Market, RiskInfo, TradingInfo}
import org.pjb.model.utils.RiskInfoUtils._
import org.pjb.model.utils.TradingInfoUtils._
import org.pjb.model.utils.VersionedUtils._
import org.pjb.model.version.Versioned.Ver

object MarketUtils {
  implicit class MarketOps(m:Market) {
    def mergeInput(ver: Ver, input: InboundMarket): Market = {
      val ti: TradingInfo = m.tradingInfo match {
        case None => TradingInfo().mergeInput(ver, input)
        case Some(info) => info.mergeInput(ver, input)
      }
      val ri:RiskInfo = m.riskInfo match {
        case None => RiskInfo().mergeInput(ver, input)
        case Some(info) => info.mergeInput(ver, input)
      }
      Market(m.id,
        m.name.merge(input.name.toVersioned(ver)),
        Some(ti),
        Some(ri))
    }

    def diffSince(ver:Ver):Market = {
      Market(m.id,
             m.name.diffSince(ver),
             m.tradingInfo.flatMap(ti => ti.diffSince(ver)),
             m.riskInfo.flatMap(ri => ri.diffSince(ver)))
    }
  }
}
