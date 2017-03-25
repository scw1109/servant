package com.github.scw1109.servant.connector.line

import java.nio.charset.StandardCharsets

import com.github.scw1109.servant.connector.Line
import com.github.scw1109.servant.connector.line.model.MessageEvent
import com.github.scw1109.servant.core.session.TextMessage
import com.github.scw1109.servant.util.Resources
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods.{compact, render}
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

/**
  * @author scw1109
  */
class LineMessageSender(lineConfig: Line) {

  private val logger: Logger = LoggerFactory.getLogger(getClass)

  def sendMessage(textMessage: TextMessage[MessageEvent]): Unit = {
    logger.trace(s"Sending message: $textMessage")

    val body = compact(render(
      ("to" -> textMessage.rawEvent.source.id) ~
        ("messages" ->
          List(
            ("type", "text") ~
              ("text", textMessage.text)
          ))
    ))

    Resources.executeAsyncHttpClient {
      _.preparePost(s"${lineConfig.apiUrl}/bot/message/push")
        .setHeader("Content-type", "application/json")
        .setHeader("Authorization", s"Bearer ${lineConfig.channelAccessToken}")
        .setBody(body.getBytes(StandardCharsets.UTF_8))
    } successWhen {
      _.getStatusCode == 200
    } onComplete {
      case Success(_) =>
      case Failure(t) =>
        logger.error(s"Failed to send message: ${t.getMessage}")
    }
  }
}
