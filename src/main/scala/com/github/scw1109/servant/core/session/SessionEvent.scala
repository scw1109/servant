package com.github.scw1109.servant.core.session

import com.github.scw1109.servant.command.CommandRequest
import com.github.scw1109.servant.connector.EventObject

/**
  * @author scw1109
  */
case class SessionEvent(sessionKey: String, eventId: String,
                        text: String, event: EventObject) extends CommandRequest
