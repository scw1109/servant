package com.github.scw1109.servant.connector.slack

/**
  * @author scw1109
  */
sealed trait Channel

case class PublicChannel(id: String, name: String, is_general: Boolean) extends Channel

case class PrivateChannel(id: String, name: String) extends Channel

case class DirectMessageChannel(id: String, user: String) extends Channel
