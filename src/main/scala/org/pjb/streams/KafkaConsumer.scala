package org.pjb.streams

import akka.actor.ActorSystem
import akka.kafka.ConsumerMessage.CommittableMessage
import akka.kafka.scaladsl.{Committer, Consumer}
import akka.kafka.{CommitterSettings, ConsumerMessage, ConsumerSettings, ProducerMessage, Subscriptions}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Keep
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, StringDeserializer}

import scala.concurrent.Future

object KafkaConsumer extends App {

  implicit val system = ActorSystem.create("kafkaConsumer")
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()
  //val kafkaServers = "http://b-1.mskplayerpropsdev.ctbea7.c2.kafka.eu-west-1.amazonaws.com:9092,http://b-2.mskplayerpropsdev.ctbea7.c2.kafka.eu-west-1.amazonaws.com:9092,http://b-3.mskplayerpropsdev.ctbea7.c2.kafka.eu-west-1.amazonaws.com:9092"
  val kafkaServers = "b-2.mskgamestatedev.eo6mzi.c2.kafka.eu-west-1.amazonaws.com:9092,b-3.mskgamestatedev.eo6mzi.c2.kafka.eu-west-1.amazonaws.com:9092,b-1.mskgamestatedev.eo6mzi.c2.kafka.eu-west-1.amazonaws.com:9092"

  val consumerConfig = system.settings.config.getConfig("akka.kafka.consumer")
  val committerSettings = CommitterSettings.create(consumerConfig)

  val consumerSettings = ConsumerSettings(consumerConfig, new StringDeserializer, new ByteArrayDeserializer)
    .withBootstrapServers(kafkaServers)
    .withGroupId("kafkaConsumer")
    .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
    .withProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false")
    .withProperty(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "5000")
    .withProperty(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG, "org.apache.kafka.clients.consumer.RoundRobinAssignor")

  Consumer.committableSource(consumerSettings, Subscriptions.topics("PlayerPropsTopic2"))
    .mapAsync(10) { m =>
      print(m)
    }
    .map(_.passThrough)
    .toMat(Committer.sink(committerSettings))(Keep.right)
    .run()


  def print(msg:CommittableMessage[String, Array[Byte]]): Future[ProducerMessage.Envelope[Integer, String, ConsumerMessage.CommittableOffset]] = {
    val s = new String(msg.record.value())
    println(s"partition=${msg.record.partition()}, value =$s")
    val res = ProducerMessage.single(
      new ProducerRecord("PlayerPropsTopic2", Integer.valueOf(s.hashCode), s),
      passThrough = msg.committableOffset
    )
    Future(res)
  }
}
