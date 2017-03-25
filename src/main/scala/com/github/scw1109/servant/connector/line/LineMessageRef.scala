package com.github.scw1109.servant.connector.line

import com.github.scw1109.servant.connector.line.model.MessageEvent
import com.github.scw1109.servant.core.TextMessage

/**
  * @author scw1109
  */
object LineMessageRef {

  def unapply[T](textMessage: TextMessage[T]): Option[TextMessage[MessageEvent]] = {
    if (textMessage.rawEvent.isInstanceOf[MessageEvent]) {
      Some(textMessage.asInstanceOf[TextMessage[MessageEvent]])
    } else {
      None
    }
  }
}
