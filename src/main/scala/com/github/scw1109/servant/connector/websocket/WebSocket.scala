package com.github.scw1109.servant.connector.websocket

import com.github.scw1109.servant.Servant
import com.github.scw1109.servant.connector.Connector
import com.github.scw1109.servant.message._
import com.typesafe.config.Config
import org.eclipse.jetty.websocket.api.{Session, WebSocketAdapter}
import spark.Spark
import spark.Spark.webSocket

/**
  * @author scw1109
  */
object WebSocket extends Connector {

  override def init(config: Config): Unit = {
    webSocket("/websocket", new WebSocketAdapter() {
      override def onWebSocketText(message: String): Unit = {
        Servant.process(IncomingMessage(message, WebSocketSource(getSession)))
      }
    })
  }

  override def sendResponse(outgoingMessage: OutgoingMessage, eventSource: EventSource): Unit = {
    eventSource match {
      case WebSocketSource(session) =>
        session.getRemote.sendString(outgoingMessage.text)
    }
  }

  case class WebSocketSource(session: Session) extends EventSource {

    override def getType: EventSourceType = WebSocketType()
  }
}
