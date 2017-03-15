package com.github.scw1109.servant.message

/**
  * @author scw1109
  */
trait EventSource {

  def getType: EventSourceType
}

sealed trait EventSourceType

case class SlackType() extends EventSourceType

case class WebSocketType() extends EventSourceType

case class LineType() extends EventSourceType

case class FacebookType() extends EventSourceType

case class HipchatType() extends EventSourceType