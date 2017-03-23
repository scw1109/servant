package com.github.scw1109.servant.connector_new.slack.model

/**
  * @author scw1109
  */
case class Message(`type`: String = "message",
                   user: String, ts: String,
                   text: String, channel: String)

object MessageRef {
  def unapply(message: Message): Option[Message] = {
    Some(message)
  }
}