package com.github.scw1109.servant.connector_new.websocket

import com.github.scw1109.servant.connector_new.websocket.model.WebSocketMessage
import com.github.scw1109.servant.connector_new.{ConnectionActor, WebSocketConfig}
import com.github.scw1109.servant.session.ReceivedMessage

/**
  * @author scw1109
  */
class WebSocketActor(webSocketConfig: WebSocketConfig) extends ConnectionActor {

  private var messageSender: WebSocketMessageSender = _
  private var connector: WebSocketConnector = _

  override def preStart(): Unit = {
    super.preStart()

    messageSender = new WebSocketMessageSender(webSocketConfig)
    connector = new WebSocketConnector(webSocketConfig, self)
  }

  override def receive: Receive = {
    case w if w.isInstanceOf[WebSocketMessage] =>
      val webSocketMessage = w.asInstanceOf[WebSocketMessage]
      dispatchMessage(ReceivedMessage(
        s"${webSocketMessage.session.hashCode()}",
        s"${System.currentTimeMillis()}",
        webSocketMessage.text,
        webSocketMessage
      ))
    case WebSocketMessageRef(textMessage) =>
      messageSender.sendMessage(textMessage)
  }
}
