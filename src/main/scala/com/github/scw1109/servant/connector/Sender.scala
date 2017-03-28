package com.github.scw1109.servant.connector

import com.github.scw1109.servant.core.actor.ActorBase
import com.github.scw1109.servant.core.session.Reply

/**
  * @author scw1109
  */
abstract class Sender[-C <: Connector](connector: C) extends ActorBase {

  final override def receive: Receive = receiveMessage orElse receiveExtension

  final private val receiveExtension: Receive = {
    case reply: Reply =>
      logger.trace(s"Sending reply: ${reply.text}")
      sendReply(reply)
  }

  def receiveMessage: Receive = PartialFunction.empty

  def sendReply(reply: Reply): Unit
}
