package com.github.scw1109.servant.connector.slack

import com.github.scw1109.servant.connector.{Sender, Slack}
import com.github.scw1109.servant.core.session.Reply
import com.github.scw1109.servant.util.{Helpers, Resources}

import scala.util.{Failure, Success}

/**
  * @author scw1109
  */
class SlackSender(slack: Slack) extends Sender[Slack](slack) {

  override def sendReply(reply: Reply): Unit = {
    reply.eventObject match {
      case SlackEventObject(_, event) =>
        logger.trace(s"Sending message: $reply")

        val body = s"token=${slack.botOauthToken}" +
          s"&channel=${event.channel}" +
          s"&text=${Helpers.urlEncode(reply.text)}"

        //          case RichMessage(channel, text, attachments) =>
        //            s"token=$botOauthToken" +
        //              s"&channel=$channel" +
        //              s"&text=${Helper.urlEncode(text)}" +
        //              s"&attachments=${Helper.urlEncode(attachments)}"

        Resources.executeAsyncHttpClient {
          _.preparePost(s"${slack.apiUrl}/chat.postMessage")
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
}
