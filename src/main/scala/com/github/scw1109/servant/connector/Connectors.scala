package com.github.scw1109.servant.connector

import com.github.scw1109.servant.connector.facebook.FacebookActor
import com.github.scw1109.servant.connector.hipchat.HipchatActor
import com.github.scw1109.servant.connector.line.LineActor
import com.github.scw1109.servant.connector.slack.{SlackEventApiActor, SlackRtmActor}
import com.github.scw1109.servant.connector.websocket.WebSocketActor

/**
  * @author scw1109
  */
sealed trait Connector {

  def id: String

  def actorType: Class[_ <: ServiceActor[_, _, _]]

  def commandSet: Option[String] = None: Option[String]
}

sealed trait WebSocketEnabled

sealed trait Slack extends Connector {

  def apiUrl = "https://slack.com/api"

  def botOauthToken: String
}

case class SlackEventApi(id: String,
                         verificationToken: String,
                         botOauthToken: String,
                         override val commandSet: Option[String]) extends Slack {

  override def actorType: Class[_ <: ServiceActor[_, _, _]] =
    classOf[SlackEventApiActor]
}

case class SlackRtm(id: String,
                    botOauthToken: String,
                    override val commandSet: Option[String]) extends Slack {

  override def actorType: Class[_ <: ServiceActor[_, _, _]] =
    classOf[SlackRtmActor]
}

case class Line(id: String,
                channelSecret: String,
                channelAccessToken: String,
                botUserId: String,
                override val commandSet: Option[String]) extends Connector {

  def apiUrl: String = "https://api.line.me/v2"

  override def actorType: Class[_ <: ServiceActor[_, _, _]] =
    classOf[LineActor]
}

case class Facebook(id: String,
                    appSecret: String,
                    pageAccessToken: String,
                    verifyToken: String,
                    override val commandSet: Option[String]) extends Connector {

  def apiUrl: String = "https://graph.facebook.com/v2.8"

  override def actorType: Class[_ <: ServiceActor[_, _, _]] =
    classOf[FacebookActor]
}

case class Hipchat(id: String,
                   domain: String,
                   authToken: String,
                   slashCommand: String,
                   override val commandSet: Option[String]) extends Connector {

  def apiUrl = s"https://$domain.hipchat.com/v2"

  override def actorType: Class[_ <: ServiceActor[_, _, _]] =
    classOf[HipchatActor]
}

case class WebSocket(id: String,
                     override val commandSet: Option[String])
  extends Connector with WebSocketEnabled {

  override def actorType: Class[_ <: ServiceActor[_, _, _]] =
    classOf[WebSocketActor]
}
