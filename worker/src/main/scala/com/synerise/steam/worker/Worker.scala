package com.synerise.steam.worker


import akka.actor.{ActorRef, ActorSystem}
import akka.cluster.Cluster
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.management.scaladsl.AkkaManagement
import akka.stream.ActorMaterializer
import com.synerise.steam.worker.actors.{KafkaConsumer, ResultMessagePreparer, StateManager}
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

object Worker extends App {

  lazy val config = ConfigFactory.load()
  implicit val system = ActorSystem("super-soft-steam-cluster")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher
  implicit val cluster = Cluster(system)

  AkkaManagement(system).start()
  ClusterBootstrap(system).start()

  //Actors
  val actorResultMessagePreparer: ActorRef = system.actorOf(ResultMessagePreparer.props, "result-preparer")
  val actorStateManager: ActorRef = system.actorOf(StateManager.props(actorResultMessagePreparer), "state-manager")
  val actorKafkaConsumer: ActorRef = system.actorOf(KafkaConsumer.props(actorStateManager), "kafka-stream")

  val server: Future[Http.ServerBinding] = Http()(system).bindAndHandle(
    complete(StatusCodes.OK),
    "0.0.0.0",
    8080,
  )

  def onClose(): Unit = {
    server.foreach(s => s.terminate(10.seconds))
    system.terminate()
  }

  scala.sys.addShutdownHook {
    onClose()
    Await.result(system.whenTerminated, 10.seconds)
  }

}
