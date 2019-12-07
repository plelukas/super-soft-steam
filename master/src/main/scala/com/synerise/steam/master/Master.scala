package com.synerise.steam.master

import akka.actor.ActorSystem
import akka.cluster.Cluster
import akka.http.scaladsl.Http
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.management.scaladsl.AkkaManagement
import akka.stream.ActorMaterializer
import com.synerise.steam.master.actors.{MasterClusterListener, MasterStateManager, WebsocketManager}
import com.synerise.steam.master.http.MasterHttpService
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

object Master extends App {

  lazy val config = ConfigFactory.load()
  implicit val system = ActorSystem("super-soft-steam-cluster")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher
  implicit val cluster = Cluster(system)

  val masterStateManager = system.actorOf(MasterStateManager.props(), "master-state-manager")
  val masterClusterListener = system.actorOf(MasterClusterListener.props(cluster, masterStateManager), "master-cluster-listener")
  val websocketManager = system.actorOf(WebsocketManager.props(masterStateManager), "master-websocket-manager")

  val masterHttpService = new MasterHttpService(websocketManager)

  AkkaManagement(system).start()
  ClusterBootstrap(system).start()

  val server: Future[Http.ServerBinding] = Http()(system).bindAndHandle(
    masterHttpService.routes,
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
