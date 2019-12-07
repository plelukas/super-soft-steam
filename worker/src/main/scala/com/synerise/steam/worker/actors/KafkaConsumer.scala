package com.synerise.steam.worker.actors

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.dispatch.MessageDispatcher
import akka.kafka.scaladsl.Consumer
import akka.kafka.scaladsl.Consumer.{Control, DrainingControl}
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.scaladsl.{Keep, Sink}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import com.synerise.steam.worker.AppConfig.Kafka
import com.synerise.steam.worker.KafkaMessage
import org.apache.kafka.common.serialization.StringDeserializer
import spray.json._

import scala.concurrent.duration._

case object Start

class KafkaConsumer(stateManager: ActorRef) extends Actor with ActorLogging {

  implicit val executionContext: MessageDispatcher = context.system.dispatchers.lookup("worker-dispatcher")

  implicit val materializer: ActorMaterializer = {
    val settings =
      ActorMaterializerSettings(context.system)
        .withSupervisionStrategy { ex =>
          log.error(ex, "An error occurred, stopping trigger consumer")
          Supervision.Stop
        }
    ActorMaterializer(settings)
  }

  val consumerSettings: ConsumerSettings[String, String] = ConsumerSettings(Kafka.ConsumerConfig, new StringDeserializer, new StringDeserializer)
    .withBootstrapServers(Kafka.BootstrapServers)
    .withGroupId(Kafka.GroupId)

  private var controller: Control = _

  override def preStart(): Unit = {
    super.preStart()
    self ! Start
  }

  override def receive: Receive = {
    case Start =>
      val (control, streamCompletion) = Consumer
        .committableSource(consumerSettings, Subscriptions.topics(Kafka.Topic))
        .map(message => {
          stateManager ! KafkaMessage(message.record.value().parseJson)
        })
        .toMat(Sink.ignore)(Keep.both)
        .run()

      controller = DrainingControl((control, streamCompletion))
      streamCompletion.onComplete {
        result =>
          log.info(s"Automation trigger stream completed with $result, restarting in 10s")
          context.system.scheduler.scheduleOnce(10.seconds, () => self ! Start)
      }
      log.info("Kafka message streaming started.")
  }
}

object KafkaConsumer {
  def props(stateManager: ActorRef): Props = Props(new KafkaConsumer(stateManager))
}