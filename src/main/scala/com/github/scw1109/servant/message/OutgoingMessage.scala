package com.github.scw1109.servant.message

/**
  * @author scw1109
  */
sealed abstract class OutgoingMessage(val text: String)

case class TextOutgoingMessage(override val text: String) extends OutgoingMessage(text)

case class RichOutgoingMessage(override val text: String, richContent: String) extends OutgoingMessage(text)
