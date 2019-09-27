package org.pjb.model.inbound

case class Event(id:Int,
                 name:Option[String],
                 markets:List[Market])