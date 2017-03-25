package com.github.scw1109.servant.core.session

import com.github.scw1109.servant.command.CommandRequest


case class ReceivedMessage[T](sessionKey: String, messageId: String,
                           text: String, rawEvent: T) extends CommandRequest

object ReceivedMessageRef {

  def unapply[T](receivedMessage: ReceivedMessage[T]): Option[ReceivedMessage[T]] = {
    Some(receivedMessage)
  }
}
