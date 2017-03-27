package com.github.scw1109.servant.connector.slack

import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

import akka.actor.Cancellable
import com.github.scw1109.servant.connector.slack.event.{Message, RtmStartResponse}
import com.github.scw1109.servant.connector.{Receiver, SlackRtm}
import com.github.scw1109.servant.util.Resources
import org.asynchttpclient.ws.{WebSocket, WebSocketTextListener, WebSocketUpgradeHandler}
import org.json4s.DefaultFormats
import org.json4s.JsonAST.JNothing
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods.{compact, parse, render}

import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}

/**
  * @author scw1109
  */
class SlackRtmReceiver(slackRtm: SlackRtm) extends Receiver[SlackRtm](slackRtm) {

  import SlackRtmReceiver._

  private implicit lazy val formats = DefaultFormats

  private var webSocket: Option[WebSocket] = None: Option[WebSocket]
  private val pingMessageId = new AtomicInteger(0)

  private var pingScheduler = None: Option[Cancellable]

  override def preStart(): Unit = {
    super.preStart()

    rtmStart()

    import context.dispatcher
    pingScheduler = Option(
      context.system.scheduler.schedule(
        Duration.Zero,
        Duration(20, TimeUnit.SECONDS),
        () => self ! WebSocketPing
      )
    )
  }

  override def postStop(): Unit = {
    pingScheduler.foreach(_.cancel())

    webSocket.foreach(_.close())

    super.postStop()
  }

  override def receiveMessage: Receive = {
    case WebSocketPing =>
      sendPing()
  }

  def rtmStart(): Unit = {
    val api = "rtm.start"

    import context.dispatcher

    Resources.executeAsyncHttpClient {
      _.preparePost(s"${slackRtm.apiUrl}/$api")
        .setBody(s"token=${slackRtm.botOauthToken}")
        .setHeader("Content-type", "application/x-www-form-urlencoded")
    } successWhen {
      _.getStatusCode == 200
    } onComplete {
      case Success(response) =>
        val body = response.getResponseBody(StandardCharsets.UTF_8)
        logger.info(s"Successfully get info from $api")
        logger.trace(s"rtm response: $body")
        val rtmResponse = parse(body).extract[RtmStartResponse]
        startWebSocket(rtmResponse.url)
      case Failure(t) =>
        logger.error(s"Failed to start rtm: ${t.getMessage}")
    }
  }

  def startWebSocket(url: String): Unit = {
    logger.trace(s"Connecting to websocket: $url")
    Resources.asyncHttpClient.prepareGet(url)
      .execute(new WebSocketUpgradeHandler.Builder()
        .addWebSocketListener(webSocketTextListener)
        .build())
  }

  def sendPing(): Unit = {
    webSocket match {
      case Some(ws) =>
        val sequenceId = pingMessageId.getAndIncrement
        logger.trace(s"Sending ping ... $sequenceId")
        if (ws.isOpen) {
          val payload = compact(render(
            ("id" -> sequenceId) ~
              ("type" -> "ping") ~
              ("time" -> System.currentTimeMillis())
          ))

          ws.sendMessage(payload)
        }
      case None =>
    }
  }

  val webSocketTextListener = new WebSocketTextListener {

    override def onOpen(ws: WebSocket): Unit = {
      webSocket = Some(ws)
    }

    override def onClose(ws: WebSocket): Unit = {
      webSocket = None: Option[WebSocket]
      rtmStart()
    }

    override def onMessage(message: String): Unit = {
      logger.trace(s"Received message: $message")

      val event = parse(message)
      if ((event \ "type").extract[String] == "message" &&
        (event \ "subtype") == JNothing) {
        val eventMessage = event.extract[Message]
        propagateEvent(SlackEventObject(
          slackRtm,
          eventMessage
        ))
      }
    }

    override def onError(t: Throwable): Unit = {
      logger.debug(s"WebSocket error of Slack rtm: $t")
    }
  }
}

object SlackRtmReceiver {

  case class WebSocketPing()

}
