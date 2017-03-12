package com.github.scw1109.servant.command

import com.github.scw1109.servant.message.IncomingMessage
import com.typesafe.config.Config

/**
  * @author scw1109
  */
trait Command {

  def init(config: Config): Unit

  def accept(incomingMessage: IncomingMessage): Boolean

  def execute(incomingMessage: IncomingMessage): Unit
}