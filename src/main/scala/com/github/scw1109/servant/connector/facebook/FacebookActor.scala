package com.github.scw1109.servant.connector.facebook

import akka.actor.{ActorRef, Props}
import com.github.scw1109.servant.connector.{Facebook, ServiceActor}
import com.github.scw1109.servant.core.session.SessionEvent

/**
  * @author scw1109
  */
class FacebookActor(facebook: Facebook)
  extends ServiceActor[Facebook, FacebookReceiver, FacebookSender](facebook) {

  override def receiveMessage: Receive = {
    case eventObject: FacebookEventObject =>
      val messaging = eventObject.rawEvent
      messaging.message match {
        case Some(message) =>
          dispatchToSession(SessionEvent(
            s"${messaging.sender}_${messaging.recipient}",
            message.mid,
            message.text,
            eventObject
          ))
        case None =>
      }
  }

  override protected def createReceiveActor(facebook: Facebook): ActorRef = {
    context.actorOf(Props(classOf[FacebookReceiver], facebook), "receiver")
  }

  override protected def createSendActor(facebook: Facebook): ActorRef = {
    context.actorOf(Props(classOf[FacebookSender], facebook), "sender")
  }
}
