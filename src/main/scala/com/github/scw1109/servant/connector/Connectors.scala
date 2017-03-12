package com.github.scw1109.servant.connector

import com.github.scw1109.servant.connector.slack.{Slack, SlackEvent, TextMessage}
import com.github.scw1109.servant.message.{EventSource, IncomingMessage, OutgoingMessage}
import com.typesafe.config.Config
import org.slf4j.{Logger, LoggerFactory}

/**
  * @author scw1109
  */
object Connectors {

  private val logger: Logger = LoggerFactory.getLogger(getClass)

  val connectors: List[Connector] = List[Connector](
    Slack
  )

  def init(config: Config): Unit = {
    connectors foreach {
      _.init(config)
    }
  }

  def sendResponse(outgoingMessage: OutgoingMessage,
                   incomingMessage: IncomingMessage): Unit = {

    incomingMessage.source match {
      case SlackEvent(_, _, _, _) =>
        logger.trace("Using Slack to send response")
        Slack.sendResponse(outgoingMessage, incomingMessage.source)
    }
  }
}
