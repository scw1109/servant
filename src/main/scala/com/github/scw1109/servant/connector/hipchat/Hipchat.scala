package com.github.scw1109.servant.connector.hipchat

import java.nio.charset.StandardCharsets

import com.github.scw1109.servant.Servant
import com.github.scw1109.servant.connector.Connector
import com.github.scw1109.servant.message.{EventSource, IncomingMessage, OutgoingMessage}
import com.typesafe.config.Config
import org.asynchttpclient.{AsyncCompletionHandler, DefaultAsyncHttpClient, Response}
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods.{compact, parse, render}
import org.json4s.{DefaultFormats, Formats}
import org.slf4j.{Logger, LoggerFactory}
import spark.Spark.post

/**
  * @author scw1109
  */
object Hipchat extends Connector {

  implicit lazy val formats: Formats = DefaultFormats

  private val logger: Logger = LoggerFactory.getLogger(getClass)
  private var hipchatUrl: String = _

  private val asyncHttpClient = new DefaultAsyncHttpClient()

  private var authToken: String = _
  private var slashCommand: String = _

  override def init(config: Config): Unit = {
    authToken = config.getString("servant.hipchat.auth-token")
    slashCommand = config.getString("servant.hipchat.slash-command")

    val domain = config.getString("servant.hipchat.domain")
    hipchatUrl = s"https://$domain.hipchat.com/v2/"

    post("/hipchat", (request, response) => {
      process(request.body())
      ""
    })
  }

  def process(body: String): Unit = {
    val event = parse(body).extract[HipchatEvent]
    val msg = event.item.message.message.trim
    val text = if (msg.startsWith(slashCommand)) {
      msg.substring(slashCommand.length)
    } else {
      msg
    }

    Servant.process(IncomingMessage(text, event))
  }

  override def sendResponse(outgoingMessage: OutgoingMessage, eventSource: EventSource): Unit = {
    val event = eventSource.asInstanceOf[HipchatEvent]

    val body = compact(render(
      ("message" -> outgoingMessage.text) ~
        ("message_format" -> "text") ~
        ("color" -> "green")
    ))

    postToApi(s"room/${event.item.room.id}/notification", body)
  }

  private def postToApi(api: String, body: String): Unit = {
    asyncHttpClient
      .preparePost(s"$hipchatUrl$api?auth_token=$authToken")
      .setHeader("Content-type", "application/json")
      .setBody(body.getBytes(StandardCharsets.UTF_8))
      .execute(new AsyncCompletionHandler[Unit] {
        override def onCompleted(response: Response): Unit = {
          if (response.getStatusCode != 200) {
            logger.warn(s"Failed to send message to $api.\n" +
              s"response status is ${response.getStatusCode} ${response.getStatusText}")
          }
        }
      })
  }
}
