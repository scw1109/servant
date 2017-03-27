package com.github.scw1109.servant.connector.websocket

import akka.actor.{ActorRef, Props}
import com.github.scw1109.servant.connector.{ServiceActor, WebSocket}
import com.github.scw1109.servant.core.session.SessionEvent

/**
  * @author scw1109
  */
class WebSocketActor(webSocket: WebSocket)
  extends ServiceActor[WebSocket, WebSocketReceiver, WebSocketSender](webSocket) {

  override def receiveMessage: Receive = {
    case eventObject: WebSocketEventObject =>
      dispatchToSession(
        SessionEvent(
          s"${eventObject.rawEvent.webSocketSession.hashCode()}",
          s"${System.currentTimeMillis()}",
          eventObject.rawEvent.text,
          eventObject
        )
      )
  }

  override protected def createReceiveActor(webSocket: WebSocket): ActorRef = {
    context.actorOf(Props(classOf[WebSocketReceiver], webSocket), "receiver")
  }

  override protected def createSendActor(webSocket: WebSocket): ActorRef = {
    context.actorOf(Props(classOf[WebSocketSender], webSocket), "sender")
  }
}
