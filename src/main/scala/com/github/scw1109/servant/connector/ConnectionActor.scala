package com.github.scw1109.servant.connector

import akka.actor.{ActorPath, Props}
import com.github.scw1109.servant.core.ServantActorBase
import com.github.scw1109.servant.core.session.{ReceivedMessage, SessionActor}

import scala.collection.mutable

/**
  * @author scw1109
  */
object ConnectionActor {

  case class SessionClean(sessionKey: String)
}

abstract class ConnectionActor extends ServantActorBase {

  import ConnectionActor._

  protected val sessions: mutable.Map[String, ActorPath] = mutable.Map()

  protected def dispatchMessage[T](receivedMessage: ReceivedMessage[T]): Unit = {
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
        Props(classOf[SessionActor], sessionKey, self))
      sessionActor.tell(receivedMessage, self)
      sessions += (sessionKey -> sessionActor.path)
    }
  }

  override def unhandled(message: Any): Unit = {
    message match {
      case SessionClean(sessionKey) =>
        sessions.remove(sessionKey)
    }

    super.unhandled(message)
  }
}