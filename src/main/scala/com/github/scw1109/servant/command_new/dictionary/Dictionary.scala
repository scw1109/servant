package com.github.scw1109.servant.command_new.dictionary

import com.github.scw1109.servant.command_new.{Command, CommandFunction, CommandRequest, CommandResponse}

import scala.concurrent.Future

/**
  * @author scw1109
  */
abstract class Dictionary extends Command {

  override protected def commandFunction: CommandFunction = new CommandFunction {
    override def isDefinedAt(request: CommandRequest): Boolean = {
      val text = request.text.trim
      text.startsWith("dict")
    }

    override def apply(request: CommandRequest): Future[CommandResponse] = {
      val word = request.text.trim.substring("dict".length).trim
      searchDictionary(word, request)
    }
  }

  def searchDictionary(word: String, request: CommandRequest): Future[CommandResponse]
}