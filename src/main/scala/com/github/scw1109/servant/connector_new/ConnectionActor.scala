package com.github.scw1109.servant.connector_new

import akka.actor.{Actor, ActorPath, Props}
import com.github.scw1109.servant.session.{SessionActor, ReceivedMessage}

import scala.collection.mutable

/**
  * @author scw1109
  */
abstract class ConnectionActor extends Actor {

  protected val sessions: mutable.Map[String, ActorPath] = mutable.Map()

  protected def dispatchSessionMessage[T](receivedMessage: ReceivedMessage[T]): Unit = {
    val sessionKey = receivedMessage.sessionKey
    if (sessions.contains(sessionKey)) {
      sessions.get(sessionKey) match {
        case Some(path) =>
          context.actorSelection(path)
            .tell(receivedMessage, self)
        case None =>
      }
    } else {
      val sessionActor = context.actorOf(
        Props(classOf[SessionActor], sessionKey, self),
        sessionKey)
      sessionActor.tell(receivedMessage, self)
      sessions += (sessionKey -> sessionActor.path)
    }
  }
}
