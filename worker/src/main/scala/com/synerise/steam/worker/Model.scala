package com.synerise.steam.worker

import java.util.UUID

import akka.actor.ActorRef
import spray.json.JsValue


final case class KafkaMessage(event: JsValue)
final case class FilteredMessage(master: ActorRef, websocketIds: Seq[UUID], event: JsValue)

