package com.github.scw1109.servant.connector.slack

import java.util.concurrent.TimeUnit

import com.github.scw1109.servant.connector.SlackEvent
import com.github.scw1109.servant.connector.slack.model._
import com.github.scw1109.servant.core.session.ReceivedMessage
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

/**
  * @author scw1109
  */
class SlackEventActor(slackEventConfig: SlackEvent) extends SlackActor(slackEventConfig) {

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
            dispatchMessage(ReceivedMessage(
              s"${message.channel}_${message.user}",
              callback.event_id,
              removeMentioned(message.text),
              message
            ))
          } else {
            logger.trace(s"Skip message, event id: ${callback.event_id}")
          }
      }
    case SlackMessageRef(textMessage) =>
      messageSender.sendMessage(textMessage)
  }
}
