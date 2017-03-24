package com.github.scw1109.servant.connector_new.hipchat

import com.github.scw1109.servant.connector_new.hipchat.model.{HipchatMessageRef, WebHookDataRef}
import com.github.scw1109.servant.connector_new.{ConnectionActor, HipchatConfig}
import com.github.scw1109.servant.session.ReceivedMessage
import org.json4s.DefaultFormats
import org.slf4j.{Logger, LoggerFactory}

/**
  * @author scw1109
  */
class HipchatActor(hipchatConfig: HipchatConfig) extends ConnectionActor {

  private implicit lazy val formats = DefaultFormats

  private val logger: Logger = LoggerFactory.getLogger(getClass)

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
