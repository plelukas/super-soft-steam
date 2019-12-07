package com.synerise.steam.master

import java.util.UUID

import akka.actor.ActorSelection
import akka.cluster.Member
import akka.http.scaladsl.model.ws.TextMessage
import akka.stream.scaladsl.SourceQueueWithComplete
import com.synerise.steam.common.Filter

final case class MasterState(websockets: Map[UUID, WebsocketOpenState], workers: Map[Member, ActorSelection])

case class Websocket(id: UUID, queue: SourceQueueWithComplete[TextMessage])

case class WebsocketOpen(id: UUID, queue: SourceQueueWithComplete[TextMessage], filter: Filter)

case class WebsocketOpenState(id: UUID, filter: Filter)

case class WebsocketClose(id: UUID)

case class WorkerJoined(member: Member, actorSelection: ActorSelection)

case class WorkerRemoved(member: Member)
