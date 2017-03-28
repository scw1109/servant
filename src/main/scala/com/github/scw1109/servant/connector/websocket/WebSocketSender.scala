package com.github.scw1109.servant.connector.websocket

import com.github.scw1109.servant.connector.{Sender, WebSocket}
import com.github.scw1109.servant.core.session.Reply

/**
  * @author scw1109
  */
class WebSocketSender(webSocket: WebSocket) extends Sender[WebSocket](webSocket) {

  override def sendReply(reply: Reply): Unit = {
    reply.eventObject match {
      case WebSocketEventObject(_, rawEvent) =>
        rawEvent.webSocketSession.getRemote.sendString(reply.text)
    }
  }
}
