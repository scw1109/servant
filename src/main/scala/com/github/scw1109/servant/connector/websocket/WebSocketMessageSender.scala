package com.github.scw1109.servant.connector.websocket

import com.github.scw1109.servant.connector.WebSocket
import com.github.scw1109.servant.connector.websocket.model.WebSocketMessage
import com.github.scw1109.servant.core.session.TextMessage
import org.slf4j.{Logger, LoggerFactory}

/**
  * @author scw1109
  */
class WebSocketMessageSender(webSocketConfig: WebSocket) {

  private val logger: Logger = LoggerFactory.getLogger(getClass)

  def sendMessage(textMessage: TextMessage[WebSocketMessage]): Unit = {
    logger.trace(s"Sending message: $textMessage")

    textMessage.rawEvent.session.getRemote.sendString(textMessage.text)
  }
}
