package com.github.scw1109.servant.connector_new.slack

import akka.actor.ActorRef
import com.github.scw1109.servant.connector_new.SlackConfig
import com.github.scw1109.servant.connector_new.slack.model.{Callback, Message, MessageRef}
import com.github.scw1109.servant.session.TextMessage
import com.github.scw1109.servant.util.{Helper, Resources}
import org.json4s.JsonAST.JNothing
import org.json4s.native.JsonMethods.parse
import org.json4s.{DefaultFormats, JValue}
import org.slf4j.{Logger, LoggerFactory}
import spark.Spark.post
import spark.{Request, Response}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}

/**
  * @author scw1109
  */
class SlackConnector(slackConfig: SlackConfig,
                     slackActor: ActorRef) {

  post(s"/${slackConfig.id}", (request, response) =>
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
      case slackConfig.verificationToken => true
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
        callback.event match {
          case MessageRef(message) =>
            slackActor ! callback
        }
      }
    } match {
      case Success(_) =>
      case Failure(t) =>
        logger.error(s"Exception when parse event: ${t.getMessage}")
    }
    ""
  }

  def sendMessage(textMessage: TextMessage[Message]): Unit = {
    logger.trace(s"Sending message:\n $textMessage")

    val message: Message = textMessage.rawEvent
    val body = s"token=${slackConfig.botOauthToken}" +
      s"&channel=${message.channel}" +
      s"&text=${Helper.urlEncode(textMessage.text)}"
    //          case RichMessage(channel, text, attachments) =>
    //            s"token=$botOauthToken" +
    //              s"&channel=$channel" +
    //              s"&text=${Helper.urlEncode(text)}" +
    //              s"&attachments=${Helper.urlEncode(attachments)}"

    Helper.toFuture {
      Resources.asyncHttpClient
        .preparePost(s"${slackConfig.apiUrl}/chat.postMessage")
        .setBody(body)
        .setHeader("Content-type", "application/x-www-form-urlencoded")
        .execute()
    } onComplete {
      case Success(response) =>
        if (response.getStatusCode != 200) {
          logger.error(s"Failed to send message.\n" +
            s"response status is ${response.getStatusCode} ${response.getStatusText}")
        }
      case Failure(t) =>
        logger.error(s"Failed to send message.\n ${t.getMessage}")
    }
  }
}
