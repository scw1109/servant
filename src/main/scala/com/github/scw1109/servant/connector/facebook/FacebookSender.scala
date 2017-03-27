package com.github.scw1109.servant.connector.facebook

import java.nio.charset.StandardCharsets

import com.github.scw1109.servant.connector.{Facebook, Sender}
import com.github.scw1109.servant.core.session.Reply
import com.github.scw1109.servant.util.Resources
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods.{compact, render}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

/**
  * @author scw1109
  */
class FacebookSender(facebook: Facebook) extends Sender[Facebook](facebook) {

  override def sendReply(textReply: Reply): Unit = {
    textReply.eventObject match {
      case FacebookEventObject(_, event) =>
        logger.trace(s"Sending message: $textReply")

        val body = compact(render(
          ("recipient" ->
            ("id" -> event.sender.id)) ~
            ("message" ->
              ("text" -> textReply.text))
        ))

        Resources.executeAsyncHttpClient {
          val url = facebook.apiUrl
          _.preparePost(s"$url/me/messages?access_token=${facebook.pageAccessToken}")
            .setHeader("Content-type", "application/json")
            .setBody(body.getBytes(StandardCharsets.UTF_8))
        } successWhen {
          _.getStatusCode == 200
        } onComplete {
          case Success(_) =>
          case Failure(t) =>
            logger.error(s"Failed to send message: ${t.getMessage}")
        }
      case _ =>
    }
  }
}
