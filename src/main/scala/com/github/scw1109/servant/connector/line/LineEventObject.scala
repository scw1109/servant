package com.github.scw1109.servant.connector.line

import com.github.scw1109.servant.connector.line.event.MessageEvent
import com.github.scw1109.servant.connector.{EventObject, Line}

/**
  * @author scw1109
  */
case class LineEventObject(source: Line,
                           rawEvent: MessageEvent) extends EventObject

