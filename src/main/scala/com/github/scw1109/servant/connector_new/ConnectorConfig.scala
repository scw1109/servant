package com.github.scw1109.servant.connector_new

/**
  * @author scw1109
  */
sealed trait ConnectorConfig {

  def id: String
}

case class SlackConfig(id: String,
                 verificationToken: String,
                 botOauthToken: String) extends ConnectorConfig {

  def apiUrl = "https://slack.com/api"
}

case class SlackRtmConfig(id: String,
                    botOauthToken: String) extends ConnectorConfig

case class LineConfig(id: String,
                channelSecret: String,
                channelAccessToken: String,
                botUserId: String) extends ConnectorConfig

case class FacebookConfig(id: String,
                    appSecret: String,
                    pageAccessToken: String) extends ConnectorConfig

case class HipchatConfig(id: String,
                   domain: String,
                   authToken: String,
                   slashCommand: String) extends ConnectorConfig
