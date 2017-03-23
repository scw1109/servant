package com.github.scw1109.servant.connector_new.slack

import akka.actor.ActorRef
import com.github.scw1109.servant.connector_new.SlackEventConfig
import com.github.scw1109.servant.connector_new.slack.model.{Callback, MessageRef}
import org.json4s.JsonAST.JNothing
import org.json4s.native.JsonMethods.parse
import org.json4s.{DefaultFormats, JValue}
import org.slf4j.{Logger, LoggerFactory}
import spark.Spark.post
import spark.{Request, Response}

import scala.util.{Failure, Success, Try}

/**
  * @author scw1109
  */
class SlackConnector(slackEventConfig: SlackEventConfig,
                     slackActor: ActorRef) {

  post(s"/${slackEventConfig.id}", (request, response) =>
    handleEvent(request, response))

  private implicit lazy val formats = DefaultFormats

  private val logger: Logger = LoggerFactory.getLogger(getClass)

  private def handleEvent(request: Request, response: Response) = {
    logger.trace(s"Received message: ${request.body()}")
    val body = parse(request.body())

    if (verifyToken((body \ "token").extract[String])) {
      (body \ "type").extract[String] match {
        case "url_verification" =>
          ackUrlVerification(response, body)
        case "event_callback" =>
          handleEventCallback(body)
        case _ => ""
      }
    } else {
      ""
    }
  }

  private def verifyToken(token: String): Boolean = {
    token match {
      case slackEventConfig.verificationToken => true
      case _ => false
    }
  }

  private def ackUrlVerification(response: Response, body: JValue) = {
    val challenge = (body \ "challenge").extract[String]
    response.header("Content-type", "application/x-www-form-urlencoded")
    s"challenge=$challenge"
  }

  private def handleEventCallback(body: JValue) = {
    Try {
      val event = body \ "event"
      if ((event \ "type").extract[String] == "message" &&
        (event \ "subtype") == JNothing) {
        val callback = body.extract[Callback]
        slackActor ! callback
      }
    } match {
      case Success(_) =>
      case Failure(t) =>
        logger.error(s"Exception when parse event: ${t.getMessage}")
    }
    ""
  }
}
