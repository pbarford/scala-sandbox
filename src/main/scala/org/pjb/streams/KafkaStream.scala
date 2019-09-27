package org.pjb.streams

import akka.actor.ActorSystem
import akka.kafka.ConsumerMessage.CommittableMessage
import akka.kafka.{CommitterSettings, ConsumerMessage, ConsumerSettings, ProducerMessage, ProducerSettings, Subscriptions}
import akka.kafka.scaladsl.{Committer, Consumer, Producer}
import akka.stream.ActorMaterializer
import akka.stream.contrib.{SwitchMode, Valve, ValveSwitch}
import akka.stream.scaladsl.Keep
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, IntegerSerializer, StringDeserializer, StringSerializer}

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.Success
import scala.concurrent.duration._

object KafkaStream extends App {

  implicit val system = ActorSystem.create("kafkaStream")
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()
  //val kafkaServers = "http://localhost:9092"
  //val kafkaServers = "http://b-1.mskplayerpropsdev.ctbea7.c2.kafka.eu-west-1.amazonaws.com:9092,http://b-2.mskplayerpropsdev.ctbea7.c2.kafka.eu-west-1.amazonaws.com:9092,http://b-3.mskplayerpropsdev.ctbea7.c2.kafka.eu-west-1.amazonaws.com:9092"
  val kafkaServers = "b-2.mskgamestatedev.eo6mzi.c2.kafka.eu-west-1.amazonaws.com:9092,b-3.mskgamestatedev.eo6mzi.c2.kafka.eu-west-1.amazonaws.com:9092,b-1.mskgamestatedev.eo6mzi.c2.kafka.eu-west-1.amazonaws.com:9092"

  val consumerConfig = system.settings.config.getConfig("akka.kafka.consumer")
  val producerConfig = system.settings.config.getConfig("akka.kafka.producer")

  val consumerSettings = ConsumerSettings(consumerConfig, new StringDeserializer, new ByteArrayDeserializer)
      .withBootstrapServers(kafkaServers)
      .withGroupId("kafkaStream")
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
      .withProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false")
      .withProperty(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "5000")
    .withProperty(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG, "org.apache.kafka.clients.consumer.RoundRobinAssignor")

  val producerSettings:ProducerSettings[Integer, String] =
    ProducerSettings(producerConfig, new IntegerSerializer, new StringSerializer)
      .withBootstrapServers(kafkaServers)

  val committerSettings = CommitterSettings.create(consumerConfig)

  /*
  val control =
    Consumer.committableSource(consumerSettings, Subscriptions.topics("Foo"))
      .mapAsync(10)( msg => print(msg))
      .via(Producer.flexiFlow(producerSettings))
      .map(_.passThrough)
      .toMat(Committer.sink(committerSettings))(Keep.both)
      .mapMaterializedValue(DrainingControl.apply)
  control.run()
*/

  val switchFut:Future[ValveSwitch] =
    Consumer.committableSource(consumerSettings, Subscriptions.topics("PlayerPropsTopic"))
      .viaMat(new Valve(SwitchMode.Close))(Keep.right)
      .mapAsync(10)( msg => print(msg))
      .via(Producer.flexiFlow(producerSettings))
      .map(_.passThrough)
      .toMat(Committer.sink(committerSettings))(Keep.left)
      .run()

  switchFut.onComplete {
    case Success(switch: ValveSwitch) => delayedFuture(20 seconds)(flip(switch, SwitchMode.Open))
    case _ => println("the valve failed")
  }


  def flip(s:ValveSwitch, mode:SwitchMode) :Unit = {
    println(s"flipping $mode")
    s.flip(mode)
    mode match {
      case SwitchMode.Open =>
        delayedFuture(20 seconds)(flip(s ,SwitchMode.Close))
      case SwitchMode.Close =>
        delayedFuture(20 seconds)(flip(s, SwitchMode.Open))
    }
  }

  def delayedFuture[T](delay: FiniteDuration)(block: => T)(implicit executor : ExecutionContext): Future[T] = {
    val promise = Promise[T]
    system.scheduler.scheduleOnce(delay) {
      try {
        val result = block

        promise.complete(Success(result))
      } catch {
        case t: Throwable => promise.failure(t)
      }
    }
    promise.future
  }

  def print(msg:CommittableMessage[String, Array[Byte]]): Future[ProducerMessage.Envelope[Integer, String, ConsumerMessage.CommittableOffset]] = {
    val s = new String(msg.record.value())
    println(s"partition=${msg.record.partition()}, value =$s")
    val res = ProducerMessage.single(
      new ProducerRecord("PlayerPropsTopic2", Integer.valueOf(s.hashCode), s.toUpperCase),
      passThrough = msg.committableOffset
    )
   Future(res)
  }
}
