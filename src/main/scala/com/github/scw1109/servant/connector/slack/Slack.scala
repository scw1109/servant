package com.github.scw1109.servant.connector.slack

import java.nio.charset.StandardCharsets
import java.util.concurrent.{Executors, ScheduledExecutorService, TimeUnit}

import com.github.scw1109.servant.connector.Connector
import com.github.scw1109.servant.message.{EventSource, OutgoingMessage, RichOutgoingMessage, TextOutgoingMessage}
import com.github.scw1109.servant.util.Helper
import com.typesafe.config.Config
import org.asynchttpclient.{AsyncCompletionHandler, AsyncHttpClient, DefaultAsyncHttpClient, Response}
import org.json4s.DefaultFormats
import org.json4s.native.JsonMethods.parse
import org.slf4j.{Logger, LoggerFactory}
import spark.Spark.post

import scala.language.postfixOps

/**
  * @author scw1109
  */
object Slack extends Connector {

  implicit lazy val formats = DefaultFormats

  private val logger: Logger = LoggerFactory.getLogger(getClass)
  private val slackUrl: String = "https://slack.com/api/"

  private val asyncHttpClient: AsyncHttpClient = new DefaultAsyncHttpClient()
  private val scheduledExecutors: ScheduledExecutorService = Executors.newScheduledThreadPool(5)

  private var botOauthToken: String = _
  private var botSelfInfo: BotSelfInfo = _

  private var publicChannels: Map[String, PublicChannel] = Map()
  private var privateChannels: Map[String, PrivateChannel] = Map()
  private var directMessageChannels: Map[String, DirectMessageChannel] = Map()
  private var members: Map[String, Member] = Map()

  override def init(config: Config): Unit = {
    botOauthToken = config.getString("servant.slack.bot-oauth-token")

    scheduledExecutors.scheduleAtFixedRate(
      () => loadPublicChannels, 0, 5, TimeUnit.MINUTES
    )
    scheduledExecutors.scheduleAtFixedRate(
      () => loadPrivateChannels, 0, 5, TimeUnit.MINUTES
    )
    scheduledExecutors.scheduleAtFixedRate(
      () => loadDirectMessageChannels, 0, 5, TimeUnit.MINUTES
    )
    scheduledExecutors.scheduleAtFixedRate(
      () => loadMembers, 0, 5, TimeUnit.MINUTES
    )
    scheduledExecutors.scheduleAtFixedRate(
      () => loadBotInfo, 0, 5, TimeUnit.MINUTES
    )

    post("/slack", (request, response) =>
      Handlers(config, request, response).handleEvent())
  }

  def isDirectMessageChannel(id: String): Boolean = {
    directMessageChannels.contains(id)
  }

  def getBotSelfInfo: BotSelfInfo = botSelfInfo

  def isBotUser(id: String): Boolean = members(id).is_bot

  override def sendResponse(outgoingMessage: OutgoingMessage,
                            eventSource: EventSource): Unit = {
    eventSource match {
      case SlackEvent(event_id, _, _, event) =>
        outgoingMessage match {
          case TextOutgoingMessage(text) =>
            val message = TextMessage(event.channel, text)
            logger.trace(s"Sending response to event $event_id:\n $message")
            Slack.sendMessage(message)
          case RichOutgoingMessage(text, attachments) =>
            val message = RichMessage(event.channel, text, attachments)
            logger.trace(s"Sending response to event $event_id:\n $message")
            Slack.sendMessage(message)
        }
    }
  }

  def sendMessage(message: Message): Unit = {
    logger.trace(s"Sending message: $message")

    val body = message match {
      case TextMessage(channel, text) =>
        s"token=$botOauthToken" +
          s"&channel=$channel" +
          s"&text=${Helper.urlEncode(text)}"
      case RichMessage(channel, text, attachments) =>
        s"token=$botOauthToken" +
          s"&channel=$channel" +
          s"&text=${Helper.urlEncode(text)}" +
          s"&attachments=${Helper.urlEncode(attachments)}"
    }

    asyncHttpClient
      .preparePost(slackUrl + "chat.postMessage")
      .setBody(body)
      .setHeader("Content-type", "application/x-www-form-urlencoded")
      .execute(new AsyncCompletionHandler[Unit] {
        override def onCompleted(response: Response): Unit = {
          if (response.getStatusCode != 200) {
            logger.warn(s"Failed to send message.\n" +
              s"response status is ${response.getStatusCode} ${response.getStatusText}")
          }
        }
      })
  }

  private def loadPublicChannels = {
    loadSlackInfo("channels.list", response => {
      val body = response.getResponseBody(StandardCharsets.UTF_8)
      publicChannels = (parse(body) \ "channels")
        .children filter {
        channel => !(channel \ "is_archived").extract[Boolean]
      } map {
        channel =>
          val publicChannel = channel.extract[PublicChannel]
          publicChannel.id -> publicChannel
      } toMap

      logger.info(s"Got ${publicChannels.size} public channels.")
    })
  }

  private def loadPrivateChannels = {
    loadSlackInfo("groups.list", response => {
      val body = response.getResponseBody(StandardCharsets.UTF_8)
      privateChannels = (parse(body) \ "groups")
        .children filter {
        group => !(group \ "is_archived").extract[Boolean]
      } map {
        group =>
          val privateChannel = group.extract[PrivateChannel]
          privateChannel.id -> privateChannel
      } toMap

      logger.info(s"Got ${privateChannels.size} private channels.")
    })
  }

  private def loadDirectMessageChannels = {
    loadSlackInfo("im.list", response => {
      val body = response.getResponseBody(StandardCharsets.UTF_8)
      directMessageChannels = (parse(body) \ "ims")
        .children filter {
        im => !(im \ "is_user_deleted").extract[Boolean]
      } map {
        im =>
          val directMessageChannel = im.extract[DirectMessageChannel]
          directMessageChannel.id -> directMessageChannel
      } toMap

      logger.info(s"Got ${directMessageChannels.size} direct message channels.")
    })
  }

  private def loadMembers = {
    loadSlackInfo("users.list", response => {
      val body = response.getResponseBody(StandardCharsets.UTF_8)
      members = (parse(body) \ "members")
        .children filter {
        m => !(m \ "deleted").extract[Boolean]
      } map {
        m =>
          val member = m.extract[Member]
          member.id -> member
      } toMap

      logger.info(s"Got ${members.size} members.")
    })
  }

  private def loadBotInfo = {
    loadSlackInfo("auth.test", response => {
      val body = response.getResponseBody(StandardCharsets.UTF_8)
      botSelfInfo = parse(body).extract[BotSelfInfo]

      logger.info(s"Got bot info, my name is ${botSelfInfo.user}")
    })
  }

  private def loadSlackInfo(api: String, responseHandler: Response => Unit) = {
    logger.info(s"Start to get slack info from $api")
    asyncHttpClient
      .preparePost(slackUrl + api)
      .setBody(s"token=$botOauthToken")
      .setHeader("Content-type", "application/x-www-form-urlencoded")
      .execute(new AsyncCompletionHandler[Unit] {
        override def onCompleted(response: Response): Unit = {
          if (response.getStatusCode == 200) {
            logger.info(s"Successfully get info from $api")
            responseHandler(response)
          } else {
            logger.warn(s"Failed to get slack info from $api\n" +
              s"response status is ${response.getStatusCode} ${response.getStatusText}")
          }
        }
      })
  }
}