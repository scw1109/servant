package com.github.scw1109.servant.connector

import com.github.scw1109.servant.message.{EventSource, OutgoingMessage}
import com.typesafe.config.Config

/**
  * @author scw1109
  */
trait Connector {

  def init(config: Config): Unit

  def sendResponse(outgoingMessage: OutgoingMessage,
                   eventSource: EventSource): Unit
}
