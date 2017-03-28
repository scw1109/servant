package com.github.scw1109.servant.command

import com.github.scw1109.servant.core.actor.ActorBase
import com.github.scw1109.servant.core.session.SessionEvent
import com.typesafe.config.Config

import scala.util.{Failure, Success}

/**
  * @author scw1109
  */
abstract class CommandActor(config: Config) extends ActorBase {

  protected def command: Command

  override def receive: Receive = {
    case sessionEvent: SessionEvent =>
      val sessionActor = sender()
      if (command.isDefinedAt(sessionEvent)) {
        command.apply(sessionEvent)
          .onComplete({
            case Success(option) =>
              option match {
                case Some(reply) =>
                  sessionActor.tell(reply, self)
                case None =>
              }
            case Failure(t) =>
              logger.trace(s"Failure when execute command: ${t.getMessage}")
          })
      }
  }
}


