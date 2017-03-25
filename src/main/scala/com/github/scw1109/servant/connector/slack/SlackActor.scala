package com.github.scw1109.servant.connector.slack

import com.github.scw1109.servant.connector.slack.model.Message
import com.github.scw1109.servant.connector.{ConnectionActor, Slack}

/**
  * @author scw1109
  */
abstract class SlackActor(slackConfig: Slack) extends ConnectionActor {

  protected var infoLoader: SlackInfoLoader = _
  protected var messageSender: SlackMessageSender = _

  override def preStart(): Unit = {
    super.preStart()
    infoLoader = new SlackInfoLoader(slackConfig)
    messageSender = new SlackMessageSender(slackConfig)
  }

  protected def shouldHandleMessage(message: Message): Boolean =
    isIm(message.channel) || isMentioned(message.text)

  protected def isIm(id: String): Boolean = infoLoader.ims.contains(id)

  protected def isMentioned(text: String): Boolean = {
    text.contains(s"<@${infoLoader.botSelfInfo.user_id}>")
  }

  protected def removeMentioned(text: String): String = {
    text.replaceAll(s"<@${infoLoader.botSelfInfo.user_id}>", "")
  }
}