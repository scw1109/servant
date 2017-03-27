package com.github.scw1109.servant.connector.slack

import com.github.scw1109.servant.connector.slack.event.Message
import com.github.scw1109.servant.connector.{EventObject, Slack}

/**
  * @author scw1109
  */
case class SlackEventObject(source: Slack, rawEvent: Message) extends EventObject
