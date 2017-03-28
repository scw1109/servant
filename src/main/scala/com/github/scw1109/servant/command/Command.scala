package com.github.scw1109.servant.command

import com.github.scw1109.servant.core.session.{Reply, SessionEvent}

import scala.concurrent.Future

/**
  * @author scw1109
  */
trait Command extends PartialFunction[SessionEvent, Future[Option[Reply]]] {

  override def isDefinedAt(sessionEvent: SessionEvent): Boolean

  override def apply(sessionEvent: SessionEvent): Future[Option[Reply]]
}

trait PrefixCommand extends Command {

  def prefixes: Seq[String]

  def apply(sessionEvent: SessionEvent, message: String): Future[Option[Reply]]

  override def isDefinedAt(sessionEvent: SessionEvent): Boolean = {
    sessionEvent.hasPrefix(prefixes: _*)
  }

  override def apply(sessionEvent: SessionEvent): Future[Option[Reply]] = {
    sessionEvent.extractContent(prefixes: _*) match {
      case Some(message) =>
        apply(sessionEvent, message)
      case None => Future.successful(None)
    }
  }
}
