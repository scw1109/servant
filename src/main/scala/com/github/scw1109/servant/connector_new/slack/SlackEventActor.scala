package com.github.scw1109.servant.connector_new.slack

import java.util.concurrent.TimeUnit

import com.github.scw1109.servant.connector_new.SlackEventConfig
import com.github.scw1109.servant.connector_new.slack.model._
import com.github.scw1109.servant.session.ReceivedMessage
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

/**
  * @author scw1109
  */
class SlackEventActor(slackEventConfig: SlackEventConfig) extends SlackActor(slackEventConfig) {

  private val logger: Logger = LoggerFactory.getLogger(getClass)

  private var connector: SlackConnector = _

  override def preStart(): Unit = {
    super.preStart()
    connector = new SlackConnector(slackEventConfig, self)

    context.system.scheduler.schedule(
      Duration(0, TimeUnit.SECONDS),
      Duration(5, TimeUnit.MINUTES),
      () => {
        infoLoader.loadAll()
      }
    )
  }

  override def receive: Receive = {
    case CallbackRef(callback) =>
      callback.event match {
        case MessageRef(message) =>
          if (shouldHandleMessage(message)) {
            val text = removeMentioned(message.text)
            val sessionKey = s"${slackEventConfig.id}_${message.channel}_${message.user}"
            val receivedMessage = ReceivedMessage[Message](sessionKey,
              callback.event_id, text, message)
            dispatchMessage(receivedMessage)
          } else {
            logger.trace(s"Skip message, event id: ${callback.event_id}")
          }
      }
    case SlackMessageRef(textMessage) =>
      messageSender.sendMessage(textMessage)
  }
}
