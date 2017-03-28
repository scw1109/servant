package com.github.scw1109.servant.command.dictionary

import com.github.scw1109.servant.command._
import com.github.scw1109.servant.core.session.{Reply, SessionEvent}
import com.typesafe.config.Config

import scala.concurrent.Future

/**
  * @author scw1109
  */
abstract class Dictionary(config: Config) extends CommandActor(config) {

  override protected def command: Command = new PrefixCommand {

    override def prefixes: Seq[String] = Seq("dict ")

    override def apply(sessionEvent: SessionEvent, message: String): Future[Option[Reply]] = {
      search(message, sessionEvent)
    }
  }

  def search(word: String, sessionEvent: SessionEvent): Future[Option[Reply]]
}