package com.github.scw1109.servant.connector.websocket.model

import org.eclipse.jetty.websocket.api.Session

/**
  * @author scw1109
  */
case class WebSocketMessage(text: String, session: Session)
