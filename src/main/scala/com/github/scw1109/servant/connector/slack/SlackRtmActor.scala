package com.github.scw1109.servant.connector.slack

import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

import akka.actor.Cancellable
import com.github.scw1109.servant.connector.SlackRtm
import com.github.scw1109.servant.connector.slack.model.{Message, MessageRef, RtmStartResponse, SlackMessageRef}
import com.github.scw1109.servant.core.session.ReceivedMessage
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
object SlackRtmActor {

  case class WebSocketPing()

}

class SlackRtmActor(slackRtmConfig: SlackRtm) extends SlackActor(slackRtmConfig) {

  import SlackRtmActor._

  implicit lazy val formats = DefaultFormats

  private var webSocket: Option[WebSocket] = None: Option[WebSocket]
  private val pingMessageId = new AtomicInteger(0)

  private var infoLoadScheduler = None: Option[Cancellable]
  private var pingScheduler = None: Option[Cancellable]

  override def preStart(): Unit = {
    super.preStart()

    import context.dispatcher

    infoLoadScheduler = Option(
      context.system.scheduler.schedule(
        Duration.Zero,
        Duration(5, TimeUnit.MINUTES),
        () => infoLoader.loadAll()
      )
    )

    pingScheduler = Option(
      context.system.scheduler.schedule(
        Duration.Zero,
        Duration(20, TimeUnit.SECONDS),
        () => self ! WebSocketPing
      )
    )

    rtmStart()
  }

  override def postStop(): Unit = {
    infoLoadScheduler.foreach(_.cancel())
    pingScheduler.foreach(_.cancel())

    webSocket.foreach(_.close())

    super.postStop()
  }

  override def receive: Receive = {
    case MessageRef(message) =>
      if (shouldHandleMessage(message)) {
        dispatchMessage(ReceivedMessage(
          s"${message.channel}_${message.user}",
          s"${s"${message.channel}_${message.user}"}_${message.ts}",
          removeMentioned(message.text),
          message
        ))
      } else {
        logger.trace(
          s"Skip message, event id: ${message.channel}_${message.user}_${message.ts}")
      }
    case SlackMessageRef(textMessage) =>
      messageSender.sendMessage(textMessage)
    case WebSocketPing =>
      sendPing()
  }

  def rtmStart(): Unit = {
    val api = "rtm.start"

    Resources.executeAsyncHttpClient {
      _.preparePost(s"${slackRtmConfig.apiUrl}/$api")
        .setBody(s"token=${slackRtmConfig.botOauthToken}")
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
        if (ws.isOpen) {
          val payload = compact(render(
            ("id" -> pingMessageId.getAndIncrement()) ~
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
      logger.trace(s"Received rtm message $message")

      val event = parse(message)
      if ((event \ "type").extract[String] == "message" &&
        (event \ "subtype") == JNothing) {
        val eventMessage = event.extract[Message]
        self ! eventMessage
      }
    }

    override def onError(t: Throwable): Unit = {
      logger.debug(s"WebSocket error of Slack rtm: $t")
    }
  }
}
