package com.github.scw1109.servant.connector.line

import akka.actor.{ActorRef, Props}
import com.github.scw1109.servant.connector.line.event.TextMessage
import com.github.scw1109.servant.connector.{Line, ServiceActor}
import com.github.scw1109.servant.core.session.SessionEvent

/**
  * @author scw1109
  */
class LineActor(line: Line)
  extends ServiceActor[Line, LineReceiver, LineSender](line) {

  override def receiveMessage: Receive = {
    case eventObject: LineEventObject =>
      val messageEvent = eventObject.rawEvent
      messageEvent.message match {
        case t: TextMessage =>
          dispatchToSession(SessionEvent(
            s"${messageEvent.`type`}_${messageEvent.source.id}",
            t.id,
            t.text,
            eventObject
          ))
        case _ =>
      }
  }

  override protected def createReceiveActor(line: Line): ActorRef = {
    context.actorOf(Props(classOf[LineReceiver], line), "receiver")
  }

  override protected def createSendActor(line: Line): ActorRef = {
    context.actorOf(Props(classOf[LineSender], line), "sender")
  }
}
