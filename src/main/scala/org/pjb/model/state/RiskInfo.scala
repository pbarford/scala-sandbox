package org.pjb.model.state

import org.pjb.model.version.{Empty, Versioned}

case class RiskInfo(layToLose:Versioned[Int] = Empty)
