package com.github.scw1109.servant.core.session

import akka.actor.ActorRef

/**
  * @author scw1109
  */
case class Session(sessionKey: String, sessionActor: ActorRef) {

  private var _lastMessageTime: Long = 0

  def lastMessageTime: Long = _lastMessageTime

  def lastMessageTime_=(time: Long): Unit = _lastMessageTime = time
}
