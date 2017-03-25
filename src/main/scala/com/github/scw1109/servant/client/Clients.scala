package com.github.scw1109.servant.client

import com.github.scw1109.servant.client.websocket.WebSocketClientActor

/**
  * @author scw1109
  */
sealed trait Client {

  def id: String

  def actorType: Class[_ <: ClientActor]
}

case class WebSocketClient(id: String, wsUrl: String) extends Client {

  override def actorType: Class[_ <: ClientActor] = classOf[WebSocketClientActor]
}
