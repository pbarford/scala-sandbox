package org.pjb.issue

import akka.NotUsed
import akka.actor.ActorSystem
import akka.kafka.ConsumerMessage.CommittableMessage
import akka.kafka.scaladsl.Consumer.DrainingControl
import akka.kafka.scaladsl.{Committer, Consumer}
import akka.kafka.{CommitterSettings, ConsumerSettings, Subscriptions}
import akka.stream.scaladsl.{Flow, GraphDSL, Keep, Merge, Sink}
import akka.stream.{ActorMaterializer, FlowShape, Graph, UniformFanInShape}
import com.rabbitmq.client.ConnectionFactory
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, StringDeserializer}
import org.pjb.issue.rmq.Publisher
import org.pjb.issue.rmq.Publisher.{NotPublished, PublishStatus, Published}
import org.pjb.issue.streams.EitherFanOut

object Test extends App {

  /*
                                    invalid Flow
                                   /            \
                                  / (L)          \
  source -> translate -> either ->                -> merge -> commit-sink
                                  \ (R)          /
                                   \            /
                                    publish Flow

   */

  implicit val system = ActorSystem.create("kafkaConsumer")
  implicit val executionContext = system.dispatcher
  implicit val materializer = ActorMaterializer()
  val kafkaServers = "127.0.0.1:9092"

  val consumerConfig = system.settings.config.getConfig("akka.kafka.consumer")
  val committerSettings = CommitterSettings.create(consumerConfig).withMaxBatch(1)

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
  implicit val channel = connection.createChannel()
  val publisher: Publisher = new Publisher()

  val consumerSettings = ConsumerSettings(consumerConfig, new StringDeserializer, new ByteArrayDeserializer)
    .withBootstrapServers(kafkaServers)
    .withGroupId("kafkaConsumer")
    .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
    .withProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false")
    .withProperty(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "5000")
    .withProperty(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG, "org.apache.kafka.clients.consumer.RoundRobinAssignor")

  val stream = Consumer
    .committablePartitionedSource(consumerSettings, Subscriptions.topics("Foo"))
    .map {
      case (_, source) =>
        source
          .via(theFlow)
          .map(_.commitable.committableOffset)
          .runWith(Committer.sink(committerSettings))
    }
    .toMat(Sink.ignore)(Keep.both)
    .mapMaterializedValue(DrainingControl.apply)
    .run()

  def theFlow: Graph[FlowShape[CommittableMessage[String, Array[Byte]], Message], NotUsed] = {
    val partial = GraphDSL.create() { implicit builder =>
      val invalidF: Flow[Message, Message, NotUsed] = Flow[Message].map { m =>
        println(s"Thread[${Thread.currentThread().getName}] invalid [${m.value}] committing partition[${m.commitable.record.partition()}] offset[${m.commitable.record.offset()}]")
        m
      }
      val eitherF = builder.add(new EitherFanOut[Message, Message])
      val mergeF: UniformFanInShape[Message, Message]  = builder.add(Merge[Message](2))
      val pubF: Flow[Message, Message, NotUsed] = Flow[Message].mapAsync(1) {
        msg => publisher.publishConfirm(msg).map {
          ps =>
            printPublishStatus(ps)
            msg
        }
      }

      import GraphDSL.Implicits._
      eitherF.out0 ~> invalidF ~> mergeF.in(0)
      eitherF.out1 ~> pubF ~> mergeF.in(1)
      FlowShape(eitherF.in, mergeF.out)
    }.named("partial-flow")

    GraphDSL.create() { implicit builder =>
      val transF = Flow[CommittableMessage[String,Array[Byte]]].map(translate)
      builder.add(transF.via(partial))
    }
  }

  def printPublishStatus : PublishStatus => Unit = {
    case Published(part, off, _) =>
      println(s"Thread[${Thread.currentThread().getName}] PUBLISHED committing partition [$part] offset[$off]")
    case NotPublished(part, off, _) =>
      println(s"Thread[${Thread.currentThread().getName}] NOT PUBLISHED committing partition[$part] offset[$off]")
  }

  def translate: CommittableMessage[String, Array[Byte]] => Either[Message, Message] = {
    msg =>
      val s = new String(msg.record.value())
      if(Integer.valueOf(s) % 2 == 0)
        Right(Message(s, msg))
      else
        Left(Message(s, msg))
  }
}
