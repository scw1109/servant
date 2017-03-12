package com.github.scw1109.servant.connector.slack

import com.github.scw1109.servant.message.EventSource

/**
  * @author scw1109
  */
case class SlackEvent(event_id: String, event_time: Long,
                      `type`:String, event: EventContent) extends EventSource

case class EventContent(`type`: String, text: String,
                        channel: String, user: String,
                        subtype: Option[String])
