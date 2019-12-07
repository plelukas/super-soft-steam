package com.synerise.steam.master.actors

import akka.actor.{Actor, ActorLogging, Props}
import com.synerise.steam.common.{TestCommunication, WebsocketCreate, WebsocketRemove}
import com.synerise.steam.master._

class MasterStateManager extends Actor with ActorLogging {
  override def receive: Receive = state(MasterState(Map.empty, Map.empty))

  private def state(masterState: MasterState): Receive = {
    case e: TestCommunication =>
      log.info(s"Sending test communication message ${e.id} to ${masterState.workers.size} workers")
      masterState.workers.values.foreach(worker => worker.tell(e, sender()))

    case e@WebsocketOpenState(id, filter) =>
      masterState.workers.values.foreach(worker => worker.tell(WebsocketCreate(id, filter), sender()))
      context.become(state(masterState.copy(websockets = masterState.websockets + (id -> e))))

    case WebsocketClose(id) =>
      masterState.workers.values.foreach(worker => worker ! WebsocketRemove(id))
      context.become(state(masterState.copy(websockets = masterState.websockets - id)))

    case WorkerJoined(member, actorSelection) =>
      masterState.websockets.values.foreach(websocket => actorSelection.tell(WebsocketCreate(websocket.id, websocket.filter), sender()))
      context.become(state(masterState.copy(workers = masterState.workers + (member -> actorSelection))))

    case WorkerRemoved(member) =>
      // do nothing
      context.become(state(masterState.copy(workers = masterState.workers - member)))
  }
}

object MasterStateManager {

  def props(): Props = Props(new MasterStateManager())

}
