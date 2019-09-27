package org.pjb.model.state

import org.pjb.model.version.{Empty, Versioned}

case class TradingInfo(bettingStatus:Versioned[String] = Empty)

