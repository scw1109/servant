package com.github.scw1109.servant.command.echo

import com.github.scw1109.servant.command.{Command, CommandFunction, CommandRequest, CommandResponse}
import com.github.scw1109.servant.core.session.ReplyRef
import com.github.scw1109.servant.util.Helpers

import scala.concurrent.Future

/**
  * @author scw1109
  */
class Echo extends Command {

  override protected def commandFunction: CommandFunction = new CommandFunction {
    override def isDefinedAt(request: CommandRequest): Boolean = {
      request.text.trim.startsWith("echo")
    }

    override def apply(request: CommandRequest): Future[CommandResponse] = {
      val responseText = request.text.trim.substring("echo".length).trim
      Helpers.toFuture(ReplyRef(responseText, request))
    }
  }
}
