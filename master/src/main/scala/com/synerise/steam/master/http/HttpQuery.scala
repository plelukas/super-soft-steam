package com.synerise.steam.master.http

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.unmarshalling.{FromStringUnmarshaller, Unmarshaller}
import com.synerise.steam.common.Filter
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

import scala.concurrent.Future

case class QueryParameters(actions: Seq[String], businessProfileId: Int)

object QueryParameters {

  private val default: QueryParameters = QueryParameters(Seq.empty, 0)

  implicit def seqParamMarshaller: FromStringUnmarshaller[Seq[String]] =
    Unmarshaller(ex ⇒ s ⇒ Future.successful(s.split(",")))

  val fromParameters: Directive1[QueryParameters] = {
    import akka.http.scaladsl.server.Directives._

    parameters("actions".as[Seq[String]].?(default.actions), "bpId".as[String] ? default.businessProfileId).as(QueryParameters.apply)
  }
}

object JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {

  implicit val filterFormat: RootJsonFormat[Filter] = jsonFormat2(Filter)

}
