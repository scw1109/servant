package com.github.scw1109.servant.connector.hipchat

import java.nio.charset.StandardCharsets

import com.github.scw1109.servant.connector.{Hipchat, Sender}
import com.github.scw1109.servant.core.session.Reply
import com.github.scw1109.servant.util.Resources
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods.{compact, render}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

/**
  * @author scw1109
  */
class HipchatSender(hipchat: Hipchat) extends Sender[Hipchat](hipchat) {

  def sendReply(textReply: Reply): Unit = {
    textReply.eventObject match {
      case HipchatEventObject(_, event) =>
        logger.trace(s"Sending message: $textReply")

        val body = compact(render(
          ("message" -> textReply.text) ~
            ("message_format" -> "text") ~
            ("color" -> "green")
        ))

        val apiUrl = hipchat.apiUrl
        val roomId = event.item.room.id
        val authToken = hipchat.authToken
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
}
