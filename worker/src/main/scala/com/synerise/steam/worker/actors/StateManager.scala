package com.synerise.steam.worker.actors

import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.synerise.steam.common.{Filter, TestCommunication, WebsocketCreate, WebsocketRemove}
import com.synerise.steam.worker.actors.StateManager.StateRecord
import com.synerise.steam.worker.{FilteredMessage, KafkaMessage}
import spray.json.DefaultJsonProtocol._

class StateManager(resultMessagePreparer: ActorRef) extends Actor with ActorLogging{

  override def receive: Receive = state(Map.empty)

  private def state(websockets: Map[UUID, StateRecord]): Receive = {
    case TestCommunication(id) =>
      log.info(s"RECEIVED TEST MESSAGE $id")

    case WebsocketCreate(websocketId, filter) =>
      log.info(s"Created websocket $websocketId state in worker")
      context.become(state(websockets + (websocketId -> StateRecord(sender(), websocketId, filter))))

    case WebsocketRemove(websocketId) =>
      log.info(s"Removed websocket $websocketId state")
      context.become(state(websockets - websocketId))

    case KafkaMessage(event) =>
      val fields = event.asJsObject.fields
      val action = fields("action").convertTo[String]
      val bpId = fields("businessProfileId").convertTo[Int]
      val seq = websockets.values
        .filter(e => eventMatches(action, bpId, e))
        .groupBy(stateRecord => stateRecord.master)
        .mapValues(stateRecords => stateRecords.map(_.websocketId))
        .toSeq
      seq.foreach {
        case (master, uuids) =>
          resultMessagePreparer ! FilteredMessage(master, uuids.toSeq, event)
      }
  }

  private def eventMatches(eventAction: String, eventBpId: Int, stateRecord: StateRecord): Boolean = {
    val filterActions = stateRecord.filter.actions
    stateRecord.filter.businessProfileId == eventBpId && (filterActions.isEmpty || filterActions.contains(eventAction))
  }
}

object StateManager {

  def props(resultMessagePreparer: ActorRef): Props = Props(new StateManager(resultMessagePreparer))

  final case class StateRecord(master: ActorRef, websocketId: UUID, filter: Filter)
}