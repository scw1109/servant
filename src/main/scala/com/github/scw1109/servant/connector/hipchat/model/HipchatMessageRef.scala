package com.github.scw1109.servant.connector.hipchat.model

import com.github.scw1109.servant.core.session.TextMessage

/**
  * @author scw1109
  */
object HipchatMessageRef {

  def unapply[T](textMessage: TextMessage[T]): Option[TextMessage[WebHookData]] = {
    if (textMessage.rawEvent.isInstanceOf[WebHookData]) {
      Some(textMessage.asInstanceOf[TextMessage[WebHookData]])
    } else {
      None
    }
  }
}
