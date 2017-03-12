package com.github.scw1109.servant.command

import com.github.scw1109.servant.command.echo.Echo
import com.github.scw1109.servant.message.IncomingMessage
import com.typesafe.config.Config
import org.slf4j.{Logger, LoggerFactory}

/**
  * @author scw1109
  */
object Commands {

  private val logger: Logger = LoggerFactory.getLogger(getClass)

  val commandChain: List[Command] = List[Command](
    Echo
  )

  def init(config: Config): Unit = {
    commandChain foreach {
      _.init(config)
    }
  }

  def execute(incomingMessage: IncomingMessage): Unit = {
    commandChain collectFirst {
      case c if c.accept(incomingMessage) => c
    } foreach {
      command =>
        logger.trace(s"Execute command ${command.getClass.getName}")
        command.execute(incomingMessage)
    }
  }
}
