package com.github.scw1109.servant.connector.slack.event

/**
  * @author scw1109
  */
case class Message(`type`: String = "message",
                   user: String, ts: String,
                   text: String, channel: String)