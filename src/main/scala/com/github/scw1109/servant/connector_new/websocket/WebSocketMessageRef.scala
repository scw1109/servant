package com.github.scw1109.servant.connector_new.websocket

import com.github.scw1109.servant.connector_new.websocket.model.WebSocketMessage
import com.github.scw1109.servant.session.TextMessage

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
