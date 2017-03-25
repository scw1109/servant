package com.github.scw1109.servant.connector.facebook

import java.nio.charset.StandardCharsets

import com.github.scw1109.servant.connector.Facebook
import com.github.scw1109.servant.connector.facebook.model.Messaging
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
class FacebookMessageSender(facebookConfig: Facebook) {

  private val logger: Logger = LoggerFactory.getLogger(getClass)

  def sendMessage(textMessage: TextMessage[Messaging]): Unit = {
    logger.trace(s"Sending message: $textMessage")

    val body = compact(render(
      ("recipient" ->
        ("id" -> textMessage.rawEvent.sender.id)) ~
        ("message" ->
          ("text" -> textMessage.text))
    ))

    Resources.executeAsyncHttpClient {
      val url = facebookConfig.apiUrl
      _.preparePost(s"$url/me/messages?access_token=${facebookConfig.pageAccessToken}")
        .setHeader("Content-type", "application/json")
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
