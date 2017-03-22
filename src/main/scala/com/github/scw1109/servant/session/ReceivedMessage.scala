package com.github.scw1109.servant.session

/**
  * @author scw1109
  */
case class ReceivedMessage[T](sessionKey: String, messageId: String,
                           text: String, rawEvent: T)

object ReceivedMessageRef {

  def unapply[T](receivedMessage: ReceivedMessage[T]): Option[ReceivedMessage[T]] = {
    Some(receivedMessage)
  }
}
