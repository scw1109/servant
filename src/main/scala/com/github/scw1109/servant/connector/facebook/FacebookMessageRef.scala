package com.github.scw1109.servant.connector.facebook

import com.github.scw1109.servant.connector.facebook.model.Messaging
import com.github.scw1109.servant.core.TextMessage

/**
  * @author scw1109
  */
object FacebookMessageRef {

  def unapply[T](textMessage: TextMessage[T]): Option[TextMessage[Messaging]] = {
    if (textMessage.rawEvent.isInstanceOf[Messaging]) {
      Some(textMessage.asInstanceOf[TextMessage[Messaging]])
    } else {
      None
    }
  }
}
