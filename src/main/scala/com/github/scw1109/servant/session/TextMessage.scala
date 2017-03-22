package com.github.scw1109.servant.session

/**
  * @author scw1109
  */
case class TextMessage[T](text: String, rawEvent: T,
                       sessionKey: Option[String] = None,
                       respondTo: Option[String] = None)

object TextMessageRef {

  def apply[T](text: String, rawEvent: T,
            sessionKey: String,
            respondTo: String): TextMessage[T] =
    TextMessage[T](text, rawEvent, Some(sessionKey), Some(respondTo))

  def unapply[T](textMessage: TextMessage[T]): Option[TextMessage[T]] = {
    Some(textMessage)
  }
}
