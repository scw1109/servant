package com.github.scw1109.servant.connector_new.websocket

import com.github.scw1109.servant.connector_new.WebSocketConfig
import com.github.scw1109.servant.connector_new.websocket.model.WebSocketMessage
import com.github.scw1109.servant.session.TextMessage
import org.slf4j.{Logger, LoggerFactory}

/**
  * @author scw1109
  */
class WebSocketMessageSender(webSocketConfig: WebSocketConfig) {

  private val logger: Logger = LoggerFactory.getLogger(getClass)

  def sendMessage(textMessage: TextMessage[WebSocketMessage]): Unit = {
    logger.trace(s"Sending message: $textMessage")

    textMessage.rawEvent.session.getRemote.sendString(textMessage.text)
  }
}
