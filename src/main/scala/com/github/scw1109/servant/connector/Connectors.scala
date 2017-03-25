package com.github.scw1109.servant.connector

import com.github.scw1109.servant.connector.facebook.FacebookActor
import com.github.scw1109.servant.connector.hipchat.HipchatActor
import com.github.scw1109.servant.connector.line.LineActor
import com.github.scw1109.servant.connector.slack.{SlackEventActor, SlackRtmActor}
import com.github.scw1109.servant.connector.websocket.WebSocketActor

/**
  * @author scw1109
  */
sealed trait Connector {

  def id: String

  def actorType: Class[_ <: ConnectionActor]
}

sealed trait WebSocketEnabled

sealed trait Slack {

  def apiUrl = "https://slack.com/api"

  def id: String

  def botOauthToken: String
}

case class SlackEvent(id: String,
                      verificationToken: String,
                      botOauthToken: String)
  extends Connector with Slack {

  override def actorType: Class[_ <: ConnectionActor] = classOf[SlackEventActor]
}

case class SlackRtm(id: String,
                    botOauthToken: String)
  extends Connector with Slack {

  override def actorType: Class[_ <: ConnectionActor] = classOf[SlackRtmActor]
}

case class Line(id: String,
                channelSecret: String,
                channelAccessToken: String,
                botUserId: String) extends Connector {

  def apiUrl: String = "https://api.line.me/v2"

  override def actorType: Class[_ <: ConnectionActor] = classOf[LineActor]
}

case class Facebook(id: String,
                    appSecret: String,
                    pageAccessToken: String,
                    verifyToken: String) extends Connector {

  def apiUrl: String = "https://graph.facebook.com/v2.8"

  override def actorType: Class[_ <: ConnectionActor] = classOf[FacebookActor]
}

case class Hipchat(id: String,
                   domain: String,
                   authToken: String,
                   slashCommand: String) extends Connector {

  def apiUrl = s"https://$domain.hipchat.com/v2"

  override def actorType: Class[_ <: ConnectionActor] = classOf[HipchatActor]
}

case class WebSocket(id: String) extends Connector with WebSocketEnabled {

  override def actorType: Class[_ <: ConnectionActor] = classOf[WebSocketActor]
}
