package com.github.scw1109.servant.connector.slack

import akka.actor.{ActorRef, Props}
import com.github.scw1109.servant.connector.SlackRtm
import com.github.scw1109.servant.core.session.SessionEvent

/**
  * @author scw1109
  */
class SlackRtmActor(slackRtm: SlackRtm)
  extends SlackActor[SlackRtm, SlackRtmReceiver, SlackSender](slackRtm) {

  override def receiveMessage: Receive = {
    case eventObject: SlackEventObject =>
      val message = eventObject.rawEvent
      val messageId = s"${message.channel}_${message.user}_${message.ts}"
      if (shouldHandleMessage(message)) {
        dispatchToSession(
          SessionEvent(
            s"${message.channel}_${message.user}",
            messageId,
            removeMentioned(message.text),
            eventObject
          )
        )
      } else {
        logger.trace(s"Skip message, message id: $messageId")
      }
  }

  override protected def createReceiveActor(slackRtm: SlackRtm): ActorRef = {
    context.actorOf(Props(classOf[SlackRtmReceiver], slackRtm), "receiver")
  }

  override protected def createSendActor(slackRtm: SlackRtm): ActorRef = {
    context.actorOf(Props(classOf[SlackSender], slackRtm), "sender")
  }
}
