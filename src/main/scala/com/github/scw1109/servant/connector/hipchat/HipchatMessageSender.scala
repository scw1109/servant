package com.github.scw1109.servant.connector.hipchat

import java.nio.charset.StandardCharsets

import com.github.scw1109.servant.connector.Hipchat
import com.github.scw1109.servant.connector.hipchat.model.WebHookData
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
class HipchatMessageSender(hipchatConfig: Hipchat) {

  private val logger: Logger = LoggerFactory.getLogger(getClass)

  def sendMessage(textMessage: TextMessage[WebHookData]): Unit = {
    logger.trace(s"Sending message: $textMessage")

    val body = compact(render(
      ("message" -> textMessage.text) ~
        ("message_format" -> "text") ~
        ("color" -> "green")
    ))

    val apiUrl = hipchatConfig.apiUrl
    val roomId = textMessage.rawEvent.item.room.id
    val authToken = hipchatConfig.authToken
    val postUrl = s"$apiUrl/room/$roomId/notification?auth_token=$authToken"

    Resources.executeAsyncHttpClient {
      _.preparePost(postUrl)
        .setHeader("Content-type", "application/json")
        .setBody(body.getBytes(StandardCharsets.UTF_8))
    } successWhen {
      response =>
        response.getStatusCode >= 200 &&
          response.getStatusCode < 300
    } onComplete {
      case Success(_) =>
      case Failure(t) =>
        logger.error(s"Failed to send message: ${t.getMessage}")
    }
  }
}
