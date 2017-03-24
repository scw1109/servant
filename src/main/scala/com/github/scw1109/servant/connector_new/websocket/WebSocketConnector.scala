package com.github.scw1109.servant.connector_new.websocket

import akka.actor.ActorRef
import com.github.scw1109.servant.connector_new.WebSocketConfig
import com.github.scw1109.servant.connector_new.websocket.model.WebSocketMessage
import org.eclipse.jetty.websocket.api.WebSocketAdapter
import spark.Spark.webSocket

/**
  * @author scw1109
  */
class WebSocketConnector(webSocketConfig: WebSocketConfig,
                         webSocketActor: ActorRef) {

  webSocket(s"/${webSocketConfig.id}", new WebSocketAdapter() {

    override def onWebSocketText(message: String): Unit = {

      webSocketActor ! WebSocketMessage(message, getSession)
    }
  })
}
