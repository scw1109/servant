package com.github.scw1109.servant.connector.slack

import com.github.scw1109.servant.connector.Slack
import com.github.scw1109.servant.connector.slack.model.Message
import com.github.scw1109.servant.core.session.TextMessage
import com.github.scw1109.servant.util.{Helper, Resources}
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

/**
  * @author scw1109
  */
class SlackMessageSender(slackConfig: Slack) {

  private val logger: Logger = LoggerFactory.getLogger(getClass)

  def sendMessage(textMessage: TextMessage[Message]): Unit = {
    logger.trace(s"Sending message: $textMessage")

    val message: Message = textMessage.rawEvent
    val body = s"token=${slackConfig.botOauthToken}" +
      s"&channel=${message.channel}" +
      s"&text=${Helper.urlEncode(textMessage.text)}"

    //          case RichMessage(channel, text, attachments) =>
    //            s"token=$botOauthToken" +
    //              s"&channel=$channel" +
    //              s"&text=${Helper.urlEncode(text)}" +
    //              s"&attachments=${Helper.urlEncode(attachments)}"

    Resources.executeAsyncHttpClient {
      _.preparePost(s"${slackConfig.apiUrl}/chat.postMessage")
        .setBody(body)
        .setHeader("Content-type", "application/x-www-form-urlencoded")
    } successWhen {
      _.getStatusCode == 200
    } onComplete {
      case Success(_) =>
      case Failure(t) =>
        logger.error(s"Failed to send message: ${t.getMessage}")
    }
  }
}
