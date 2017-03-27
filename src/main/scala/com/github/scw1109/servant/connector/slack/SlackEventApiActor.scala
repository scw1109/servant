package com.github.scw1109.servant.connector.slack

import akka.actor.{ActorRef, Props}
import com.github.scw1109.servant.connector.SlackEventApi
import com.github.scw1109.servant.core.session.SessionEvent

/**
  * @author scw1109
  */
class SlackEventApiActor(slackEventApi: SlackEventApi)
  extends SlackActor[SlackEventApi, SlackEventReceiver, SlackSender](slackEventApi) {

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

  override protected def createReceiveActor(slackEventApi: SlackEventApi): ActorRef = {
    context.actorOf(Props(classOf[SlackEventReceiver], slackEventApi), "receiver")
  }

  override protected def createSendActor(slackEventApi: SlackEventApi): ActorRef = {
    context.actorOf(Props(classOf[SlackSender], slackEventApi), "sender")
  }
}
