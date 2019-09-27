package org.pjb.model.inbound

case class Market(id:Int,
                  name:Option[String],
                  bettingStatus:Option[String],
                  layToLose:Option[Int])
