package com.github.scw1109.servant.connector_new

import akka.actor.Actor
import com.github.scw1109.servant.connector_new.slack.{SlackEventActor, SlackRtmActor}

/**
  * @author scw1109
  */
sealed trait ConnectorConfig {

  def id: String

  def actorType: Class[_ <: Actor]
}

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

  override def actorType: Class[_ <: Actor] = classOf[SlackEventActor]
}

case class FacebookConfig(id: String,
                          appSecret: String,
                          pageAccessToken: String) extends ConnectorConfig {

  override def actorType: Class[_ <: Actor] = classOf[SlackEventActor]
}

case class HipchatConfig(id: String,
                         domain: String,
                         authToken: String,
                         slashCommand: String) extends ConnectorConfig {

  override def actorType: Class[_ <: Actor] = classOf[SlackEventActor]
}
