package com.github.scw1109.servant.connector.hipchat

import com.github.scw1109.servant.connector.hipchat.model.{HipchatMessageRef, WebHookDataRef}
import com.github.scw1109.servant.connector.{ConnectionActor, Hipchat}
import com.github.scw1109.servant.core.session.ReceivedMessage
import org.json4s.DefaultFormats

/**
  * @author scw1109
  */
class HipchatActor(hipchatConfig: Hipchat) extends ConnectionActor {

  private implicit lazy val formats = DefaultFormats

  private var messageSender: HipchatMessageSender = _
  private var connector: HipchatConnector = _

  override def preStart(): Unit = {
    super.preStart()

    messageSender = new HipchatMessageSender(hipchatConfig)
    connector = new HipchatConnector(hipchatConfig, self)
  }

  override def receive: Receive = {
    case WebHookDataRef(webHookData) =>
      val msg = webHookData.item.message.message.trim
      val text = if (msg.startsWith(hipchatConfig.slashCommand)) {
        msg.substring(hipchatConfig.slashCommand.length)
      } else {
        msg
      }

      val fromId = webHookData.item.message.from match {
        case Some(from) => from.id
        case None => ""
      }
      dispatchMessage(ReceivedMessage(
        s"${webHookData.item.room.id}_$fromId",
        webHookData.item.message.id,
        text,
        webHookData
      ))
    case HipchatMessageRef(textMessage) =>
      messageSender.sendMessage(textMessage)
  }
}
