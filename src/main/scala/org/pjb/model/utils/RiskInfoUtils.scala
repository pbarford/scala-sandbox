package org.pjb.model.utils

import org.pjb.model.inbound.{Market => InboundMarket}
import org.pjb.model.state.RiskInfo
import org.pjb.model.utils.VersionedUtils._
import org.pjb.model.version.{Empty, Populated}
import org.pjb.model.version.Versioned.Ver

object RiskInfoUtils {
  implicit class RiskInfoOps(ri: RiskInfo) {
    def mergeInput(ver:Long, im:InboundMarket):RiskInfo = {
      RiskInfo(ri.layToLose.merge(im.layToLose.toVersioned(ver)))
    }

    def diffSince(ver:Ver):Option[RiskInfo] = {
      ri.layToLose.diffSince(ver) match {
        case p@Populated(_, _) => Some(RiskInfo(p))
        case Empty => None
      }
    }
  }
}
