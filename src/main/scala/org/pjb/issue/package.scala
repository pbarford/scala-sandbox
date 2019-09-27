package org.pjb

import akka.kafka.ConsumerMessage.CommittableMessage

package object issue {
  case class Message(value:String, commitable: CommittableMessage[String, Array[Byte]])
}
