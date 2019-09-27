package org.pjb.streams

import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{IntegerSerializer, StringSerializer}

object KafkaProducer extends App {

  implicit val system = ActorSystem.create("kafkaProducer")
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()
  //val kafkaServers = "http://b-1.mskplayerpropsdev.ctbea7.c2.kafka.eu-west-1.amazonaws.com:9092,http://b-2.mskplayerpropsdev.ctbea7.c2.kafka.eu-west-1.amazonaws.com:9092,http://b-3.mskplayerpropsdev.ctbea7.c2.kafka.eu-west-1.amazonaws.com:9092"
  val kafkaServers = "b-2.mskgamestatedev.eo6mzi.c2.kafka.eu-west-1.amazonaws.com:9092,b-3.mskgamestatedev.eo6mzi.c2.kafka.eu-west-1.amazonaws.com:9092,b-1.mskgamestatedev.eo6mzi.c2.kafka.eu-west-1.amazonaws.com:9092"

  val producerConfig = system.settings.config.getConfig("akka.kafka.producer")
  val producerSettings:ProducerSettings[Integer, String] =
    ProducerSettings(producerConfig, new IntegerSerializer, new StringSerializer)
    .withProperty("partitioner.class", "org.pjb.streams.MyPartitioner")
      .withBootstrapServers(kafkaServers)

  Source(1 to 100)
    .map(i => s"message iter-$i-${System.currentTimeMillis()}")
    .map(value => new ProducerRecord[Integer, String]("PlayerPropsTopic", Integer.valueOf(value.hashCode), value))
    .map{m => println(m); m}
    .runWith(Producer.plainSink(producerSettings))
}
