package com.synerise.steam.common

import java.util.UUID

trait JacksonSerializable

final case class WebsocketCreate(id: UUID, filter: Filter) extends JacksonSerializable

final case class WebsocketRemove(id: UUID) extends JacksonSerializable

final case class TestCommunication(id: Int) extends JacksonSerializable

final case class Filter(actions: Seq[String], businessProfileId: Int) extends JacksonSerializable

final case class WorkerResponse(activity: String, websockets: Seq[UUID]) extends JacksonSerializable
