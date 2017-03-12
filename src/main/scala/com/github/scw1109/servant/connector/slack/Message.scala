package com.github.scw1109.servant.connector.slack

/**
  * @author scw1109
  */
sealed trait Message

case class TextMessage(channel: String, text: String) extends Message
