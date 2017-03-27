package com.github.scw1109.servant.connector.websocket

import com.github.scw1109.servant.connector.websocket.event.WebSocketMessage
import com.github.scw1109.servant.connector.{EventObject, WebSocket}

/**
  * @author scw1109
  */
case class WebSocketEventObject(source: WebSocket,
                                rawEvent: WebSocketMessage) extends EventObject
