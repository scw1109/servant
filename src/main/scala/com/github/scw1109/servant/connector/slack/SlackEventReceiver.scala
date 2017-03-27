package com.github.scw1109.servant.connector.slack

import com.github.scw1109.servant.connector.slack.event.Callback
import com.github.scw1109.servant.connector.{Receiver, SlackEventApi}
import org.json4s.JsonAST.JNothing
import org.json4s.native.JsonMethods.parse
import org.json4s.{DefaultFormats, JValue}
import spark.Spark.post
import spark.{Request, Response}

import scala.util.{Failure, Success, Try}

/**
  * @author scw1109
  */
class SlackEventReceiver(slackEventApi: SlackEventApi)
  extends Receiver[SlackEventApi](slackEventApi) {

  post(s"/${slackEventApi.id}", (request, response) => {
    logger.trace(s"Received message: ${request.body()}")
    handleEvent(request, response)
  })

  private implicit lazy val formats = DefaultFormats

  private def handleEvent(request: Request, response: Response) = {
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
      case slackEventApi.verificationToken => true
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
        propagateEvent(SlackEventObject(
          slackEventApi,
          callback.event
        ))
      }
    } match {
      case Success(_) =>
      case Failure(t) =>
        logger.error(s"Exception when parse event: ${t.getMessage}")
    }
    ""
  }
}
