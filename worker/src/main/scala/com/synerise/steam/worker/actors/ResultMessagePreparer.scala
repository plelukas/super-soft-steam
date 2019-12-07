package com.synerise.steam.worker.actors

import akka.actor.{Actor, ActorLogging, Props}
import com.synerise.steam.common._
import com.synerise.steam.worker.FilteredMessage
import spray.json.DefaultJsonProtocol._

class ResultMessagePreparer extends Actor with ActorLogging {
  override def receive: Receive = {
    case FilteredMessage(master, websocketIds, event) => {

      val eventFields = event.asJsObject.fields
      val params = eventFields("params").asJsObject.fields
      val action = eventFields("action").convertTo[String]

      val maybeName = params.get("$name").map(_.convertTo[String])

      // TODO: clean
      log.info("Sending event with id {} for sockets {}", params("eventUUID"), websocketIds)

      val activity =
        Activity(
          Action(
            action,
            action
          ),
          action,
          eventFields.getOrElse("uuid", params("uuid")).convertTo[String],
          "https://api-portal.rc.snrstage.com/activities-api/resources/icon/client-updateData-.svg",
          eventFields.getOrElse("time", params("time")).convertTo[Long],
          eventFields.get("label").map(_.convertTo[String]),
          ClientData(
            eventFields("clientId").convertTo[Long],
            maybeName,
            maybeName,
            params.get("email").map(_.convertTo[String]),
            Some("avatarUrl-test"),
            Some(eventFields.getOrElse("uuid", params("uuid")).convertTo[String])
          ),
          UserAgent(
            "system-test",
            "browser-test"
          )
        )

      val activityString = Activity.activityFormatter.write(activity).compactPrint
      master ! WorkerResponse(activityString, websocketIds)
    }
  }
}

object ResultMessagePreparer {
  def props: Props = Props(new ResultMessagePreparer())
}
