package com.synerise.steam.common

import spray.json.DefaultJsonProtocol._
import spray.json._

import scala.util.Try

case class ClientData(id: Long,
                      firstname: Option[String],
                      lastname: Option[String],
                      email: Option[String],
                      avatarUrl: Option[String],
                      uuid: Option[String]) {

  def toMap =
    Map(
      "firstname" -> firstname.getOrElse("unknown"),
      "lastname" -> lastname.getOrElse("unknown"),
      "email" -> email.getOrElse("unknown"),
      "avatarUrl" -> avatarUrl.getOrElse("unknown"),
      "uuid" -> uuid.getOrElse("unknown")
    )
}

object ClientData {

  implicit val clientDataFormatter = new RootJsonFormat[ClientData] {
    override def write(clientData: ClientData): JsValue = {
      JsObject(
        Map(
          "id" -> JsNumber(clientData.id),
          "firstname" -> clientData.firstname.map(JsString(_)).getOrElse(JsNull),
          "lastname" -> clientData.lastname.map(JsString(_)).getOrElse(JsNull),
          "email" -> clientData.email.map(JsString(_)).getOrElse(JsNull),
          "avatarUrl" -> clientData.avatarUrl.map(JsString(_)).getOrElse(JsNull),
          "uuid" -> clientData.uuid.map(JsString(_)).getOrElse(JsNull),
        )
      )
    }

    override def read(json: JsValue): ClientData = {
      val fields = json.asJsObject.fields
      ClientData(
        id = fields("id").convertTo[Long],
        firstname = Try(Some(fields("firstname").convertTo[String])).getOrElse(None),
        lastname = Try(Some(fields("lastname").convertTo[String])).getOrElse(None),
        email = Try(Some(fields("email").convertTo[String])).getOrElse(None),
        avatarUrl = Try(Some(fields("avatarUrl").convertTo[String])).getOrElse(None),
        uuid = Try(Some(fields("uuid").convertTo[String])).getOrElse(None)
      )
    }
  }
}

case class UserAgent(system: String, browser: String)

object UserAgent {

  implicit val formatter: RootJsonFormat[UserAgent] = jsonFormat2(UserAgent.apply)
}

case class Action(name: String, label: String)

object Action {

  import spray.json.DefaultJsonProtocol._


  implicit val formatter = jsonFormat2(Action.apply)
}

case class Activity(
                     action: Action,
                     description: String,
                     eventUUID: String,
                     icon: String,
                     time: Long,
                     label: Option[String],
                     client: ClientData,
                     userAgent: UserAgent)

object Activity {

  import spray.json.DefaultJsonProtocol._

  implicit val activityFormatter: RootJsonFormat[Activity] = new RootJsonFormat[Activity] {
    override def write(activity: Activity): JsValue = {
      JsObject(
        Map(
          "action" -> activity.action.toJson,
          "description" -> JsString(activity.description),
          "eventUUID" -> JsString(activity.eventUUID),
          "icon" -> JsString(activity.icon),
          "time" -> JsNumber(activity.time),
          "label" -> activity.label.map(JsString(_)).getOrElse(JsNull),
          "client" -> activity.client.toJson,
          "userAgent" -> activity.userAgent.toJson
        )
      )
    }

    override def read(json: JsValue): Activity = {
      val fields: Map[String, JsValue] = json.asJsObject.fields
      Activity(
        action = fields("action").convertTo[Action],
        description = fields("description").convertTo[String],
        eventUUID = fields("eventUUID").convertTo[String],
        icon = fields("icon").convertTo[String],
        time = fields("time").convertTo[Long],
        label = Try(Some(fields("label").convertTo[String])).getOrElse(None),
        client = fields("client").convertTo[ClientData],
        userAgent = fields("userAgent").convertTo[UserAgent],
      )
    }
  }
}
