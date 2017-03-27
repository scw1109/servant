package com.github.scw1109.servant.connector.hipchat

import akka.actor.{ActorRef, Props}
import com.github.scw1109.servant.connector.{Hipchat, ServiceActor}
import com.github.scw1109.servant.core.session.SessionEvent

/**
  * @author scw1109
  */
class HipchatActor(hipchat: Hipchat)
  extends ServiceActor[Hipchat, HipchatReceiver, HipchatSender](hipchat) {

  override def receiveMessage: Receive = {
    case eventObject: HipchatEventObject =>
      val webHookData = eventObject.rawEvent
      val msg = webHookData.item.message.message.trim
      val text = if (msg.startsWith(hipchat.slashCommand)) {
        msg.substring(hipchat.slashCommand.length)
      } else {
        msg
      }

      val fromId = webHookData.item.message.from match {
        case Some(from) => from.id
        case None => ""
      }

      dispatchToSession(SessionEvent(
        s"${webHookData.item.room.id}_$fromId",
        webHookData.item.message.id,
        text,
        eventObject
      ))
  }

  override protected def createReceiveActor(hipchat: Hipchat): ActorRef = {
    context.actorOf(Props(classOf[HipchatReceiver], hipchat), "receiver")
  }

  override protected def createSendActor(hipchat: Hipchat): ActorRef = {
    context.actorOf(Props(classOf[HipchatSender], hipchat), "sender")
  }
}
