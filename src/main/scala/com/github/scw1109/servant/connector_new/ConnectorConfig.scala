package com.github.scw1109.servant.connector_new

import akka.actor.Actor
import com.github.scw1109.servant.connector_new.facebook.FacebookActor
import com.github.scw1109.servant.connector_new.hipchat.HipchatActor
import com.github.scw1109.servant.connector_new.line.LineActor
import com.github.scw1109.servant.connector_new.slack.{SlackEventActor, SlackRtmActor}
import com.github.scw1109.servant.connector_new.websocket.WebSocketActor

/**
  * @author scw1109
  */
sealed trait ConnectorConfig {

  def id: String

  def actorType: Class[_ <: Actor]
}

sealed trait WebSocketEnabled

sealed trait SlackConfig {

  def apiUrl = "https://slack.com/api"

  def id: String

  def botOauthToken: String
}

case class SlackEventConfig(id: String,
                            verificationToken: String,
                            botOauthToken: String)
  extends ConnectorConfig with SlackConfig {

  override def actorType: Class[_ <: Actor] = classOf[SlackEventActor]
}

case class SlackRtmConfig(id: String,
                          botOauthToken: String)
  extends ConnectorConfig with SlackConfig {

  override def actorType: Class[_ <: Actor] = classOf[SlackRtmActor]
}

case class LineConfig(id: String,
                      channelSecret: String,
                      channelAccessToken: String,
                      botUserId: String) extends ConnectorConfig {

  def apiUrl: String = "https://api.line.me/v2"

  override def actorType: Class[_ <: Actor] = classOf[LineActor]
}

case class FacebookConfig(id: String,
                          appSecret: String,
                          pageAccessToken: String,
                          verifyToken: String) extends ConnectorConfig {

  def apiUrl: String = "https://graph.facebook.com/v2.8"

  override def actorType: Class[_ <: Actor] = classOf[FacebookActor]
}

case class HipchatConfig(id: String,
                         domain: String,
                         authToken: String,
                         slashCommand: String) extends ConnectorConfig {

  def apiUrl = s"https://$domain.hipchat.com/v2"

  override def actorType: Class[_ <: Actor] = classOf[HipchatActor]
}

case class WebSocketConfig(id: String) extends ConnectorConfig with WebSocketEnabled {

  override def actorType: Class[_ <: Actor] = classOf[WebSocketActor]
}
