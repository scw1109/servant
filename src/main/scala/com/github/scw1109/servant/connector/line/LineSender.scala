package com.github.scw1109.servant.connector.line

import java.nio.charset.StandardCharsets

import com.github.scw1109.servant.connector.{Line, Sender}
import com.github.scw1109.servant.core.session.Reply
import com.github.scw1109.servant.util.Resources
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods.{compact, render}

import scala.util.{Failure, Success}

/**
  * @author scw1109
  */
class LineSender(line: Line) extends Sender[Line](line) {

  override def sendReply(reply: Reply): Unit = {
    reply.eventObject match {
      case LineEventObject(_, event) =>
        logger.trace(s"Sending message: $reply")

        val body = compact(render(
          ("to" -> event.source.id) ~
            ("messages" ->
              List(
                ("type", "text") ~
                  ("text", reply.text)
              ))
        ))

        Resources.executeAsyncHttpClient {
          _.preparePost(s"${line.apiUrl}/bot/message/push")
            .setHeader("Content-type", "application/json")
            .setHeader("Authorization", s"Bearer ${line.channelAccessToken}")
            .setBody(body.getBytes(StandardCharsets.UTF_8))
        } successWhen {
          _.getStatusCode == 200
        } onComplete {
          case Success(_) =>
          case Failure(t) => {
            logger.error(s"Failed to send message: ${t.getMessage}")
          }
        }
    }
  }
}
