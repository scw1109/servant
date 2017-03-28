package com.github.scw1109.servant.command.echo

import com.github.scw1109.servant.command.{Command, CommandActor, PrefixCommand}
import com.github.scw1109.servant.core.session.{Reply, SessionEvent}
import com.typesafe.config.Config

import scala.concurrent.Future
import scala.util.Try

/**
  * @author scw1109
  */
class Echo(config: Config) extends CommandActor(config) {

  private val repeat = Try(config.getInt("repeat")).getOrElse(1)

  override protected def command: Command = new PrefixCommand {

    override def prefixes: Seq[String] = Seq("echo ")

    override def apply(sessionEvent: SessionEvent, message: String): Future[Option[Reply]] = {
      Future.successful(
        Option(Reply(message * repeat, sessionEvent.event))
      )
    }
  }
}
