package com.github.scw1109.servant.connector_new.slack

import java.util.concurrent.TimeUnit

import com.github.scw1109.servant.connector_new.slack.model._
import com.github.scw1109.servant.connector_new.{ConnectionActor, SlackConfig}
import com.github.scw1109.servant.session.{ReceivedMessage, TextMessage}
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

/**
  * @author scw1109
  */
class SlackActor(slackConfig: SlackConfig) extends ConnectionActor {

  private val logger: Logger = LoggerFactory.getLogger(getClass)

  private var connector: SlackConnector = _
  private var infoLoader: SlackInfoLoader = _

  override def preStart(): Unit = {
    infoLoader = new SlackInfoLoader(slackConfig)
    connector = new SlackConnector(slackConfig, self)

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
            val sessionKey = s"${slackConfig.id}_${message.channel}_${message.user}"
            val receivedMessage = ReceivedMessage[Message](sessionKey,
              callback.event_id, text, message)
            dispatchSessionMessage(receivedMessage)
          }
      }
    case SlackMessageRef(textMessage) =>
      connector.sendMessage(textMessage)
  }

  private def shouldHandleMessage(message: Message): Boolean =
    isIm(message.channel) || isMentioned(message.text)

  private def isIm(id: String): Boolean = infoLoader.ims.contains(id)

  private def isMentioned(text: String) = {
    text.contains(s"<@${infoLoader.botSelfInfo.user_id}>")
  }

  private def removeMentioned(text: String) = {
    text.replaceAll(s"<@${infoLoader.botSelfInfo.user_id}>", "")
  }

  private object SlackMessageRef {

    def unapply[T](textMessage: TextMessage[T]): Option[TextMessage[Message]] = {
      if (textMessage.rawEvent.isInstanceOf[Message]) {
        Some(textMessage.asInstanceOf[TextMessage[Message]])
      } else {
        None
      }
    }
  }

}
