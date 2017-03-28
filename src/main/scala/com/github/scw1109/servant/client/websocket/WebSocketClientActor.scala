package com.github.scw1109.servant.client.websocket

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

import akka.actor.Cancellable
import com.github.scw1109.servant.client.{ClientActor, WebSocketClient}
import com.github.scw1109.servant.util.Resources
import org.asynchttpclient.ws.{WebSocket, WebSocketTextListener, WebSocketUpgradeHandler}

import scala.concurrent.duration.Duration
import scala.io.StdIn
import scala.util.Try

/**
  * @author scw1109
  */
class WebSocketClientActor(webSocketClient: WebSocketClient) extends ClientActor {

  import WebSocketClientActor._

  private var webSocketRef = None: Option[WebSocket]

  private var pingScheduler = None: Option[Cancellable]

  private val pingMessageId = new AtomicInteger(0)

  override def preStart(): Unit = {
    super.preStart()

    connectWebSocket
    listenStdIn

    pingScheduler = Option(
      context.system.scheduler.schedule(
        Duration.Zero,
        Duration(30, TimeUnit.SECONDS),
        self, Ping
      )
    )
  }

  override def postStop(): Unit = {
    pingScheduler.foreach(_.cancel())
    webSocketRef.foreach(_.close())

    super.postStop()
  }

  private def listenStdIn = {
    Try {
      while (true) {
        val line = StdIn.readLine()
        webSocketRef match {
          case Some(webSocket) =>
            webSocket.sendMessage(line)
          case None =>
            System.out.println("Websocket not ready.")
        }
      }
    }
  }

  private def connectWebSocket = {
    Resources.asyncHttpClient.prepareGet(webSocketClient.wsUrl)
      .execute(new WebSocketUpgradeHandler.Builder().addWebSocketListener(
        new WebSocketTextListener {
          override def onMessage(message: String): Unit = {
            System.out.println(
              "-----\n" +
                s"Servant:\n$message\n" +
                "-----\n")
          }

          override def onOpen(websocket: WebSocket): Unit = {
            webSocketRef = Some(websocket)
          }

          override def onError(t: Throwable): Unit = {
            System.out.println(s"WebSocketClient connection error: ${t.getMessage}")
          }

          override def onClose(websocket: WebSocket): Unit = {
            System.out.println("WebSocketClient connection closed.")
          }
        }
      ).build())
  }

  override def receive: Receive = {
    case Ping =>
      webSocketRef match {
        case Some(webSocket) =>
          val payload = pingMessageId.getAndIncrement().toString
          webSocket.sendPing(payload.getBytes)
        case None =>
      }
  }
}

object WebSocketClientActor {

  case class Ping()

}
