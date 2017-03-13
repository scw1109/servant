package com.github.scw1109.servant.command.echo

import com.github.scw1109.servant.command.Command
import com.github.scw1109.servant.connector.Connectors
import com.github.scw1109.servant.message.{IncomingMessage, TextOutgoingMessage}
import com.typesafe.config.Config

/**
  * @author scw1109
  */
object Echo extends Command {

  override def init(config: Config): Unit = {
    // No init needed
  }

  override def accept(incomingMessage: IncomingMessage): Boolean = {
    incomingMessage.text.trim.startsWith("echo")
  }

  override def execute(incomingMessage: IncomingMessage): Unit = {
    Connectors.sendResponse(TextOutgoingMessage(
      incomingMessage.text.trim.substring("echo".length).trim),
      incomingMessage)
  }
}
