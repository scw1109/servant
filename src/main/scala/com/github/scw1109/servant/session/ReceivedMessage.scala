package com.github.scw1109.servant.session

import com.github.scw1109.servant.command_new.CommandRequest


case class ReceivedMessage[T](sessionKey: String, messageId: String,
                           text: String, rawEvent: T) extends CommandRequest

object ReceivedMessageRef {

  def unapply[T](receivedMessage: ReceivedMessage[T]): Option[ReceivedMessage[T]] = {
    Some(receivedMessage)
  }
}
