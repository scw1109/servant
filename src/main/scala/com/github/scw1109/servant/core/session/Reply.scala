package com.github.scw1109.servant.core.session

import com.github.scw1109.servant.command.{CommandRequest, CommandResponse}
import com.github.scw1109.servant.connector.EventObject

/**
  * @author scw1109
  */
case class Reply(text: String, eventObject: EventObject,
                 sessionKey: Option[String] = None,
                 respondTo: Option[String] = None) extends CommandResponse

object ReplyRef {

  def apply(text: String, eventObject: EventObject,
            sessionKey: String,
            respondTo: String): Reply =
    Reply(text, eventObject, Some(sessionKey), Some(respondTo))

  def apply(text: String, commandRequest: CommandRequest): Reply = {
    commandRequest match {
      case sessionEvent: SessionEvent =>
        Reply(text, sessionEvent.event, Some(sessionEvent.sessionKey),
          Some(sessionEvent.eventId))
      case _ =>
        throw new IllegalArgumentException(s"Cannot create Reply from $commandRequest")
    }
  }
}
