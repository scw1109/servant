package com.github.scw1109.servant.connector

import com.github.scw1109.servant.core.actor.ActorBase

/**
  * @author scw1109
  */
abstract class Receiver[-C <: Connector](connector: C) extends ActorBase {

  final private val selfActorSelection = context.actorSelection(self.path)

  final override def receive: Receive = receiveMessage orElse receiveExtension

  final private val receiveExtension: Receive = {
    case event: EventObject =>
      logger.trace(s"Received event: ${event.rawEvent}")
      context.parent.tell(event, self)
  }

  def receiveMessage: Receive = PartialFunction.empty

  def propagateEvent(event: EventObject): Unit = {
    selfActorSelection ! event
  }
}
