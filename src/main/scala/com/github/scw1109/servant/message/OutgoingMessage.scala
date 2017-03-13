package com.github.scw1109.servant.message

/**
  * @author scw1109
  */
trait OutgoingMessage

case class TextOutgoingMessage(text: String) extends OutgoingMessage

case class RichOutgoingMessage(text: String, richContent: String) extends OutgoingMessage
