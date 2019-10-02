package org.pjb.issue

import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{IntegerSerializer, StringSerializer}

object FooProducer extends App {

  implicit val system = ActorSystem.create("kafkaProducer")
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()
  val kafkaServers = "127.0.0.1:9092"

  val producerConfig = system.settings.config.getConfig("akka.kafka.producer")
  val producerSettings:ProducerSettings[Integer, String] =
    ProducerSettings(producerConfig, new IntegerSerializer, new StringSerializer)
      .withProperty("partitioner.class", "org.pjb.streams.MyPartitioner")
      .withBootstrapServers(kafkaServers)

  Source(1 to 20000)
    .map(i => s"$i")
    .map(value => new ProducerRecord[Integer, String]("Foo", Integer.valueOf(value.hashCode), value))
    .map{m => println(m); m}
    .runWith(Producer.plainSink(producerSettings))
}