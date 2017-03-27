package com.github.scw1109.servant.connector.websocket

import com.github.scw1109.servant.connector.websocket.event.WebSocketMessage
import com.github.scw1109.servant.connector.{Receiver, WebSocket}
import org.eclipse.jetty.websocket.api.{Session, WebSocketAdapter}
import spark.Spark

/**
  * @author scw1109
  */
class WebSocketReceiver(webSocket: WebSocket) extends Receiver[WebSocket](webSocket) {

  Spark.webSocket(s"/${webSocket.id}", new WebSocketAdapter() {

    override def onWebSocketText(message: String): Unit = {
      logger.trace(s"Received message: $message")

      propagateEvent(WebSocketEventObject(
        webSocket,
        WebSocketMessage(message, getSession)
      ))
    }

    override def onWebSocketConnect(session: Session): Unit = {
      super.onWebSocketConnect(session)
      logger.trace(s"Websocket connected. remote address: ${session.getRemoteAddress}")
    }

    override def onWebSocketClose(statusCode: Int, reason: String): Unit = {
      logger.trace(s"Websocket closed. status code: $statusCode reason: $reason")
      super.onWebSocketClose(statusCode, reason)
    }
  })
}
