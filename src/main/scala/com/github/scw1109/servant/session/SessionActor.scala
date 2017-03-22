package com.github.scw1109.servant.session

import akka.actor.{Actor, ActorRef}

/**
  * @author scw1109
  */
class SessionActor(sessionKey: String, connectionActor: ActorRef) extends Actor {

  override def receive: Receive = {
    case ReceivedMessageRef(sessionMessage) =>
      self ! TextMessageRef(sessionMessage.text, sessionMessage.rawEvent,
        sessionMessage.sessionKey, sessionMessage.messageId)
    case TextMessageRef(textMessage) =>
      connectionActor.tell(
        textMessage,
        self)
  }
}
