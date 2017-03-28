package com.github.scw1109.servant.core.session

import com.github.scw1109.servant.command.CommandSets
import com.github.scw1109.servant.connector.Connector
import com.github.scw1109.servant.core.actor.ActorBase

import scala.collection.mutable

/**
  * @author scw1109
  */
class SessionActor(sessionKey: String, connector: Connector) extends ActorBase {

  private val maxHistorySize = 100

  private val messageHistory: mutable.Queue[String] = mutable.Queue()

  override def preStart(): Unit = {
    super.preStart()

    val commandSet = connector.commandSet.getOrElse(CommandSets.defaultSetKey)
    CommandSets.get(commandSet).foreach(
      props => context.actorOf(props, s"command-${props.clazz.getSimpleName}")
    )
  }

  override def receive: Receive = {
    case sessionEvent: SessionEvent =>
      if (!messageHistory.contains(sessionEvent.eventId)) {
        context.children.foreach(
          command => command.tell(sessionEvent, self)
        )
        recordMessage(sessionEvent.eventId)
      } else {
        logger.trace(s"Skip repeated message: ${sessionEvent.eventId}")
      }
    case reply: Reply =>
      context.parent.tell(reply, self)
  }

  private def recordMessage(messageId: String) = {
    if (messageHistory.size >= maxHistorySize) {
      messageHistory.dequeue()
    }
    messageHistory.enqueue(messageId)
  }
}
