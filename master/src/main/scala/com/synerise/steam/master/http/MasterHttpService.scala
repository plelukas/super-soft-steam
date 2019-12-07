package com.synerise.steam.master.http

import java.util.UUID

import akka.actor.{ActorRef, ActorSystem}
import akka.event.Logging
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.ws.TextMessage
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.{Materializer, OverflowStrategy}
import com.synerise.steam.common.{Filter, TestCommunication}
import com.synerise.steam.master.AppConfig.Websocket
import com.synerise.steam.master.{WebsocketClose, WebsocketOpen}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.{Failure, Success}

class MasterHttpService(websocketManager: ActorRef)(implicit materializer: Materializer, ec: ExecutionContext, system: ActorSystem) {

  private val log = Logging.getLogger(system, this)

  def routes: Route = pathPrefix("activities") {
    (get & pathEndOrSingleSlash) {
      QueryParameters.fromParameters { queryParameters =>
        websocketHandler(queryParameters)
      }
    } ~
      // test
      pathPrefix(IntNumber) { testId =>
        (get & pathEndOrSingleSlash) {
          websocketManager ! TestCommunication(testId)
          complete(StatusCodes.OK)
        }
      }
  }

  private def websocketHandler(queryParameters: QueryParameters): Route = {
    val filter = Filter(queryParameters.actions, queryParameters.businessProfileId)
    log.info("Opening new websocket with filter {}", filter)
    val (queue, source) = Source.queue[TextMessage](100, OverflowStrategy.dropHead)
      .throttle(Websocket.ThrottleElements, Websocket.ThrottleSeconds.seconds)
      .preMaterialize()
    val websocketId = UUID.randomUUID()
    websocketManager ! WebsocketOpen(websocketId, queue, filter)
    val (done, sink) = Sink.ignore.preMaterialize()
    done.onComplete {
      case Failure(exception) =>
        log.error(exception, "Websocket with filter {} closed with error", filter)
        websocketManager ! WebsocketClose(websocketId)
      case Success(_) =>
        log.info("Websocket with filter {} successfully finished", filter)
        websocketManager ! WebsocketClose(websocketId)
    }
    val flow = Flow.fromSinkAndSourceCoupled(sink, source)
    handleWebSocketMessages(flow)
  }

}
