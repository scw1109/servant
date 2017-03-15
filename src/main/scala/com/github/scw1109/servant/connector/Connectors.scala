package com.github.scw1109.servant.connector

import com.github.scw1109.servant.connector.facebook.Facebook
import com.github.scw1109.servant.connector.hipchat.Hipchat
import com.github.scw1109.servant.connector.line.Line
import com.github.scw1109.servant.connector.slack.Slack
import com.github.scw1109.servant.connector.websocket.WebSocket
import com.github.scw1109.servant.message._
import com.typesafe.config.Config
import org.slf4j.{Logger, LoggerFactory}

/**
  * @author scw1109
  */
object Connectors {

  private val logger: Logger = LoggerFactory.getLogger(getClass)

  val connectors: List[Connector] = List[Connector](
    // WebSocket must be the first to setup
    WebSocket,
    Slack,
    Line,
    Facebook,
    Hipchat
  )

  def init(config: Config): Unit = {
    connectors foreach {
      _.init(config)
    }
  }

  def sendResponse(outgoingMessage: OutgoingMessage,
                   incomingMessage: IncomingMessage): Unit = {

    incomingMessage.source.getType match {
      case SlackType() =>
        logger.trace("Using Slack to send response")
        Slack.sendResponse(outgoingMessage, incomingMessage.source)
      case LineType() =>
        logger.trace("Using Line to send response")
        Line.sendResponse(outgoingMessage, incomingMessage.source)
      case FacebookType() =>
        logger.trace("Using Facebook to send response")
        Facebook.sendResponse(outgoingMessage, incomingMessage.source)
      case HipchatType() =>
        logger.trace("Using Hipchat to send response")
        Hipchat.sendResponse(outgoingMessage, incomingMessage.source)
      case WebSocketType() =>
        logger.trace("Using WebSocket to send response")
        WebSocket.sendResponse(outgoingMessage, incomingMessage.source)
      case _ =>
    }
  }
}
