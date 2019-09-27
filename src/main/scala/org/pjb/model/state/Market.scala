package org.pjb.model.state

import org.pjb.model.version.{Empty, Versioned}

case class Market(id:Int,
                  name:Versioned[String] = Empty,
                  tradingInfo:Option[TradingInfo] = None,
                  riskInfo: Option[RiskInfo] = None)


