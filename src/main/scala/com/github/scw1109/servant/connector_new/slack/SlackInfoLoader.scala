package com.github.scw1109.servant.connector_new.slack

import java.nio.charset.StandardCharsets

import com.github.scw1109.servant.connector_new.SlackConfig
import com.github.scw1109.servant.connector_new.slack.model._
import com.github.scw1109.servant.util.{Helper, Resources}
import org.asynchttpclient.Response
import org.json4s.DefaultFormats
import org.json4s.native.JsonMethods.parse
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.Future
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * @author scw1109
  */
class SlackInfoLoader(slackConfig: SlackConfig) {

  private implicit lazy val formats = DefaultFormats

  private val logger: Logger = LoggerFactory.getLogger(getClass)

  private var _botSelfInfo: BotSelfInfo = _
  private var _channels: Map[String, Channel] = Map()
  private var _groups: Map[String, Group] = Map()
  private var _ims: Map[String, Im] = Map()
  private var _users: Map[String, User] = Map()

  def botSelfInfo: BotSelfInfo = _botSelfInfo

  def channels: Map[String, Channel] = _channels

  def groups: Map[String, Group] = _groups

  def ims: Map[String, Im] = _ims

  def users: Map[String, User] = _users

  def loadAll(): Unit = {
    loadBotInfo().onComplete(
      applyIfSuccess(b => {
        _botSelfInfo = b
      })
    )
    loadChannels().onComplete(
      applyIfSuccess(c => {
        _channels = c
      })
    )
    loadGroups().onComplete(
      applyIfSuccess(g => {
        _groups = g
      })
    )
    loadIms().onComplete(
      applyIfSuccess(i => {
        _ims = i
      })
    )
    loadUsers().onComplete(
      applyIfSuccess(u => {
        _users = u
      })
    )
  }

  private def loadChannels(): Future[Map[String, Channel]] = {
    loadSlackInfo("channels.list").transform({
      case Success(response) =>
        val body = response.getResponseBody(StandardCharsets.UTF_8)
        val channels = (parse(body) \ "channels")
          .children filter {
          c => !(c \ "is_archived").extract[Boolean]
        } map {
          c =>
            val channel = c.extract[Channel]
            channel.id -> channel
        } toMap

        logger.trace(s"Got ${channels.size} channels.")
        Success(channels)
      case Failure(t) =>
        Failure(t)
    })
  }

  private def loadGroups(): Future[Map[String, Group]] = {
    loadSlackInfo("groups.list").transform({
      case Success(response) =>
        val body = response.getResponseBody(StandardCharsets.UTF_8)
        val groups = (parse(body) \ "groups")
          .children filter {
          g => !(g \ "is_archived").extract[Boolean]
        } map {
          g =>
            val group = g.extract[Group]
            group.id -> group
        } toMap

        logger.trace(s"Got ${groups.size} groups.")
        Success(groups)
      case Failure(t) =>
        Failure(t)
    })
  }

  private def loadIms(): Future[Map[String, Im]] = {
    loadSlackInfo("im.list").transform({
      case Success(response) =>
        val body = response.getResponseBody(StandardCharsets.UTF_8)
        val ims = (parse(body) \ "ims")
          .children filter {
          i => !(i \ "is_user_deleted").extract[Boolean]
        } map {
          i =>
            val im = i.extract[Im]
            im.id -> im
        } toMap

        logger.trace(s"Got ${ims.size} ims.")
        Success(ims)
      case Failure(t) =>
        Failure(t)
    })
  }

  private def loadUsers(): Future[Map[String, User]] = {
    loadSlackInfo("users.list").transform({
      case Success(response) =>
        val body = response.getResponseBody(StandardCharsets.UTF_8)
        val users = (parse(body) \ "members")
          .children filter {
          u => !(u \ "deleted").extract[Boolean]
        } map {
          u =>
            val user = u.extract[User]
            user.id -> user
        } toMap

        logger.trace(s"Got ${users.size} users.")
        Success(users)
      case Failure(t) =>
        Failure(t)
    })
  }

  private def loadBotInfo(): Future[BotSelfInfo] = {
    loadSlackInfo("auth.test").transform({
      case Success(response) =>
        val body = response.getResponseBody(StandardCharsets.UTF_8)
        val botSelfInfo = parse(body).extract[BotSelfInfo]

        logger.trace(s"Got bot info, my name is ${botSelfInfo.user}")
        Success(botSelfInfo)
      case Failure(t) =>
        Failure(t)
    })
  }

  private def loadSlackInfo(api: String): Future[Response] = {
    logger.trace(s"Start to get slack info from $api")
    Helper.toFuture {
      Resources.asyncHttpClient
        .preparePost(s"${slackConfig.apiUrl}/$api")
        .setBody(s"token=${slackConfig.botOauthToken}")
        .setHeader("Content-type", "application/x-www-form-urlencoded")
        .execute()
    } transform {
      case Success(response) =>
        if (response.getStatusCode == 200) {
          logger.trace(s"Successfully get info from $api")
          Success(response)
        } else {
          val msg = s"response status is ${response.getStatusCode} ${response.getStatusText}"
          logger.warn(s"Failed to get slack info from $api\n $msg")
          Failure(new RuntimeException(msg))
        }
      case Failure(t) =>
        logger.error(s"Failed to get slack info from $api\n ${t.getMessage}")
        Failure(t)
    }
  }

  private def applyIfSuccess[T](f: T => Unit): Try[T] => Unit = {
    case Success(s) =>
      f.apply(s)
    case Failure(_) =>
  }
}
