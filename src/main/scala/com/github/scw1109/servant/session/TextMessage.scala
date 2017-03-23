package com.github.scw1109.servant.session

import com.github.scw1109.servant.command_new.{CommandRequest, CommandResponse}


case class TextMessage[T](text: String, rawEvent: T,
                          sessionKey: Option[String] = None,
                          respondTo: Option[String] = None) extends CommandResponse

object TextMessageRef {

  def apply[T](text: String, rawEvent: T,
               sessionKey: String,
               respondTo: String): TextMessage[T] =
    TextMessage[T](text, rawEvent, Some(sessionKey), Some(respondTo))

  def apply[T](text: String, commandRequest: CommandRequest): TextMessage[T] = {
    commandRequest match {
      case ReceivedMessageRef(receivedMessage) =>
        val rawEvent = receivedMessage.rawEvent.asInstanceOf[T]
        TextMessage[T](text, rawEvent, Some(receivedMessage.sessionKey),
          Some(receivedMessage.messageId))
      case _ =>
        throw new IllegalArgumentException("Unknown message type")
    }
  }

  def unapply[T](textMessage: TextMessage[T]): Option[TextMessage[T]] = {
    Some(textMessage)
  }
}
