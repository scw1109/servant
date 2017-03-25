package com.github.scw1109.servant.connector.websocket

import com.github.scw1109.servant.connector.websocket.model.WebSocketMessage
import com.github.scw1109.servant.core.session.TextMessage

/**
  * @author scw1109
  */
object WebSocketMessageRef {

  def unapply[T](textMessage: TextMessage[T]): Option[TextMessage[WebSocketMessage]] = {
    if (textMessage.rawEvent.isInstanceOf[WebSocketMessage]) {
      Some(textMessage.asInstanceOf[TextMessage[WebSocketMessage]])
    } else {
      None
    }
  }
}
