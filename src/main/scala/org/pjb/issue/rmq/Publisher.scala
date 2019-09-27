package org.pjb.issue.rmq

import akka.kafka.ConsumerMessage.CommittableOffset
import com.rabbitmq.client.{AMQP, Channel, ConfirmListener}
import org.pjb.issue.Message
import org.pjb.issue.rmq.Publisher.{NotPublished, PublishStatus, Published}

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success}

object Publisher {
  trait PublishStatus {
    val offset:Long
    val committableOffset:CommittableOffset
  }
  case class Published(partition:Int, offset:Long, committableOffset:CommittableOffset) extends PublishStatus
  case class NotPublished(partition:Int, offset:Long, committableOffset:CommittableOffset) extends PublishStatus
}

class Publisher(implicit executionContext: ExecutionContext, channel: Channel) {


  def publish(msg:Message) = {
    publishF(msg).map(_ => Published(msg.commitable.record.partition(), msg.commitable.record.offset(), msg.commitable.committableOffset))
  }

  def publishConfirm(msg:Message) = {

    futureErrorWrapper {
      channel.confirmSelect()
    }.flatMap { _ =>
      addConfirmListener(msg).flatMap { comittablePromise =>
        publishF(msg)
          .flatMap(_ => comittablePromise.future)
      }
    }
  }

  private def futureErrorWrapper[T](body: => T): Future[T] = Future(body).andThen {
    case success@Success(_) => success
    case Failure(exception) =>
      Failure(exception)
  }

  private def addConfirmListener(message:Message): Future[Promise[PublishStatus]] = {

    val callback = Promise[PublishStatus]()

    val confirmListener = new ConfirmListener {
      override def handleAck(deliveryTag: Long, multiple: Boolean): Unit = {
        callback.trySuccess(Published(message.commitable.record.partition(), message.commitable.record.offset(), message.commitable.committableOffset))
        println(s"Thread[${Thread.currentThread().getName}-${Thread.currentThread().getId}] deliveryTag[$deliveryTag] multiple[$multiple] removeConfirmListener")
        channel.removeConfirmListener(this)
      }

      override def handleNack(deliveryTag: Long, multiple: Boolean): Unit = {
        callback.trySuccess(NotPublished(message.commitable.record.partition(), message.commitable.record.offset(), message.commitable.committableOffset))
        println(s"Thread[${Thread.currentThread().getName}-${Thread.currentThread().getId}] deliveryTag[$deliveryTag] removeConfirmListener")
        channel.removeConfirmListener(this)
      }
    }

    futureErrorWrapper(channel.addConfirmListener(confirmListener)).map(_ => callback)
  }

  private def publishF(message: Message) = {
    futureErrorWrapper {
      val exchange = "exchange.foo"
      val routingKey = "foo"

      import scala.collection.JavaConverters._
      val headers = Map[String,AnyRef]("partition" -> Integer.valueOf(message.commitable.record.partition()),
                                        "offset" -> java.lang.Long.valueOf(message.commitable.record.offset()))
      val props =
        new AMQP.BasicProperties.Builder().headers(headers.asJava).build()

      channel.basicPublish(
        exchange,
        routingKey,
        props,
        message.value.getBytes
      )
    }
  }
}
