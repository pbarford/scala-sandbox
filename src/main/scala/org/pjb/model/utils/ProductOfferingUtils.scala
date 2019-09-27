package org.pjb.model.utils

import org.pjb.model.inbound.{Market => InboundMarket}
import org.pjb.model.state.{Market, ProductOffering}
import org.pjb.model.utils.MarketUtils._
import org.pjb.model.version.Versioned.Ver

object ProductOfferingUtils {

  implicit class ProductOfferingOps(po: ProductOffering) {

    def mergeInboundMarkets(ver:Ver, ims: List[InboundMarket]): ProductOffering = {
      val updates: Seq[(Int, Market)] = ims.map(im => po.markets.get(im.id) match {
        case None => im.id -> Market(im.id).mergeInput(ver, im)
        case Some(m) => im.id -> m.mergeInput(ver, im)
      })
      ProductOffering(po.markets ++ updates.toMap)
    }

    def diffSince(ver:Ver):ProductOffering = {
      ProductOffering(po.markets.map(m => m._1 -> m._2.diffSince(ver)).dropWhile(p => p._2.tradingInfo.isEmpty))
    }
  }

}
