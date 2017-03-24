package com.github.scw1109.servant.connector_new.line

import com.github.scw1109.servant.connector_new.line.model.MessageEvent
import com.github.scw1109.servant.session.TextMessage

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
