package org.pjb.issue

import com.rabbitmq.client.{AMQP, ConnectionFactory, Consumer, Envelope, ShutdownSignalException}

object Checker extends App {

  val connectionFactory: ConnectionFactory = {
    val cf = new ConnectionFactory()
    cf.setUsername("guest")
    cf.setPassword("guest")
    cf.setRequestedHeartbeat(30)
    cf.setConnectionTimeout(60)
    cf.setVirtualHost("/")
    cf.setHost("127.0.0.1")
    cf.setPort(5672)
    cf
  }

  val connection = connectionFactory.newConnection()
  val channel = connection.createChannel()

  channel.basicConsume("foo", new Consumer {

    var check : Map[Int, Int] = Map.empty

    override def handleConsumeOk(consumerTag: String): Unit = {}

    override def handleCancelOk(consumerTag: String): Unit = {}

    override def handleCancel(consumerTag: String): Unit = {}

    override def handleShutdownSignal(consumerTag: String, sig: ShutdownSignalException): Unit = {}

    override def handleRecoverOk(consumerTag: String): Unit = {}

    override def handleDelivery(consumerTag: String, envelope: Envelope, properties: AMQP.BasicProperties, body: Array[Byte]): Unit = {
      println(s"checking ... deliveryTag [${envelope.getDeliveryTag}]")
      val v = Integer.valueOf(new String(body))
      val p = Integer.valueOf(properties.getHeaders.get("partition").toString)

      check.find(el => el._1 == p) match {
        case Some((_, ov)) =>
          if (v > ov) check + (Int.unbox(p) -> Int.unbox(v))
          else {
            println(s"out of order detected - partition[$p] - $v < $ov")
            check + (Int.unbox(p) -> Int.unbox(v))
          }
        case _ =>
          check + (Int.unbox(p) -> Int.unbox(v))
      }

      check = check + (Int.unbox(p) -> Int.unbox(v))
    }
  })
}
