package com.github.scw1109.servant.connector.slack

import java.time.Instant
import java.time.temporal.ChronoUnit

import com.github.scw1109.servant.Servant
import com.github.scw1109.servant.message.IncomingMessage
import com.typesafe.config.Config
import org.json4s.{DefaultFormats, JValue}
import org.slf4j.{Logger, LoggerFactory}
import spark.Response

import scala.util.{Failure, Success, Try}

sealed abstract class Handler(config: Config, body: JValue, response: Response) {

  implicit lazy val formats = DefaultFormats

  val logger: Logger = LoggerFactory.getLogger(getClass)

  def handleEvent(): String = {
    Try {
      if (tokenValid())
        handle()
      else ""
    } match {
      case Success(s) => s
      case Failure(_) => ""
    }
  }

  private def tokenValid(): Boolean = {
    val configToken = config.getString("servant.slack.verification-token")
    (body \ "token").extract[String] match {
      case `configToken` => true
      case _ => false
    }
  }

  def handle(): String
}

private class UrlVerification(config: Config, body: JValue, response: Response)
  extends Handler(config, body, response) {

  override def handle(): String = {
    val challenge = (body \ "challenge").extract[String]
    response.header("Content-type", "application/x-www-form-urlencoded")
    s"challenge=$challenge"
  }
}

private class EventCallback(config: Config, body: JValue, response: Response)
  extends Handler(config, body, response) {

  override def handle(): String = {
    val eventWrapper = body.extract[SlackEvent]
    logger.trace(s"Got event:\n $eventWrapper")

    if (shouldHandle(eventWrapper)) {
      logger.trace(s"Decided to process event: ${eventWrapper.event_id}")
      processEvent(eventWrapper)
    } else {
      logger.trace(s"Skip event: ${eventWrapper.event_id}")
    }
    ""
  }

  private def processEvent(eventWrapper: SlackEvent) = {
    val text = if (isMentionedInMessage(eventWrapper.event)) {
      removeMentioned(eventWrapper.event.text)
    } else {
      eventWrapper.event.text
    }

    Servant.process(IncomingMessage(text, eventWrapper))
  }

  private def shouldHandle(eventWrapper: SlackEvent): Boolean = {
    val event = eventWrapper.event
    event.`type` == "message" &&
      event.subtype.isEmpty &&
      !Slack.isBotUser(event.user) &&
      (Slack.isDirectMessageChannel(event.channel) ||
        isMentionedInMessage(event)) &&
      isRecentMessage(eventWrapper.event_time)
  }

  private def isMentionedInMessage(event: EventContent) = {
    event.text.contains(s"<@${Slack.getBotSelfInfo.user_id}>")
  }

  private def removeMentioned(text: String) = {
    text.replaceAll(s"<@${Slack.getBotSelfInfo.user_id}>", "")
  }

  private def isRecentMessage(tsInSec: Long): Boolean = {
    Instant.ofEpochSecond(tsInSec)
      .plus(1, ChronoUnit.MINUTES)
      .isAfter(Instant.now())
  }
}

private class Unknown() extends Handler(null, null, null) {

  override def handle(): String = ""
}
