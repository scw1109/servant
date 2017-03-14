package com.github.scw1109.servant

import com.github.scw1109.servant.command.Commands
import com.github.scw1109.servant.connector.Connectors
import com.github.scw1109.servant.message.{IncomingMessage, OutgoingMessage}
import com.typesafe.config.Config
import spark.Spark.port

/**
  * @author scw1109
  */
object Servant {

  private var config: Config = _

  def getConfig: Config = config

  def start(config: Config, portNumber: Int): Unit = {
    port(portNumber)

    Commands.init(config)
    Connectors.init(config)
  }

  def process(incomingMessage: IncomingMessage): Unit = {
    Commands.execute(incomingMessage)
  }

  def sendResponse(outgoingMessage: OutgoingMessage,
                   incomingMessage: IncomingMessage): Unit = {

    Connectors.sendResponse(outgoingMessage, incomingMessage)
  }
}




