package com.github.scw1109.servant.client

import org.asynchttpclient.ws.{WebSocket, WebSocketTextListener, WebSocketUpgradeHandler}
import org.asynchttpclient.{DefaultAsyncHttpClient, DefaultAsyncHttpClientConfig}

import scala.io.StdIn
import scala.util.Try

/**
  * @author scw1109
  */
object WebSocketClient {

  def start(port: Int, path: String): Unit = {

    var webSocketRef = None: Option[WebSocket]

    val asyncHttpClientConfig = new DefaultAsyncHttpClientConfig.Builder()
      .setMaxConnections(1)
      .build()

    val asyncHttpClient = new DefaultAsyncHttpClient(asyncHttpClientConfig)
    asyncHttpClient.prepareGet(s"ws://localhost:$port/$path")
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
}
