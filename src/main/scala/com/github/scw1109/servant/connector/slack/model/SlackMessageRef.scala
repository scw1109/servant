package com.github.scw1109.servant.connector.slack.model

import com.github.scw1109.servant.core.session.TextMessage

/**
  * @author scw1109
  */
object SlackMessageRef {

  def unapply[T](textMessage: TextMessage[T]): Option[TextMessage[Message]] = {
    if (textMessage.rawEvent.isInstanceOf[Message]) {
      Some(textMessage.asInstanceOf[TextMessage[Message]])
    } else {
      None
    }
  }
}
