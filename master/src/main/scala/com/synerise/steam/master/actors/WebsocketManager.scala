package com.synerise.steam.master.actors

import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.http.scaladsl.model.ws.TextMessage
import com.synerise.steam.common.{TestCommunication, WorkerResponse}
import com.synerise.steam.master.{Websocket, WebsocketClose, WebsocketOpen, WebsocketOpenState}

class WebsocketManager(masterStateManager: ActorRef) extends Actor with ActorLogging {

  override def receive: Receive = state(Map.empty)

  private def state(stateWebsockets: Map[UUID, Websocket]): Receive = {
    case e: TestCommunication => masterStateManager ! e
    case WebsocketOpen(id, queue, filter) =>
      masterStateManager ! WebsocketOpenState(id, filter)
      context.become(state(stateWebsockets + (id -> Websocket(id, queue))))
    case e@WebsocketClose(id) =>
      masterStateManager ! e
      context.become(state(stateWebsockets - id))
    case WorkerResponse(activity, websockets) =>
      websockets.flatMap(websocket => stateWebsockets.get(websocket)
        .orElse {
          log.warning(s"Could not find websocket $websocket in state $stateWebsockets")
          None
        })
        .map(_.queue)
        .foreach(queue => queue.offer(TextMessage(activity)))
  }

}

object WebsocketManager {

  def props(masterStateManager: ActorRef) = Props(new WebsocketManager(masterStateManager))

}
