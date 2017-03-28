package com.github.scw1109.servant.core.session

import com.github.scw1109.servant.connector.EventObject

/**
  * @author scw1109
  */
case class SessionEvent(sessionKey: String, eventId: String,
                        text: String, event: EventObject) {

  def hasPrefix(prefixes: String*): Boolean = {
    prefixes.exists(text.trim.startsWith(_))
  }

  def extractContent(prefixes: String*): Option[String] = {
    prefixes.filter(text.trim.startsWith(_))
      .map(p => text.substring(p.length).trim)
      .collectFirst({ case s: String => s })
  }
}
