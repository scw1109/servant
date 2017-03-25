package com.github.scw1109.servant.command

import akka.actor.Actor
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

/**
  * @author scw1109
  */
abstract class Command extends Actor {

  protected implicit val executionContext: ExecutionContext =
    ExecutionContext.Implicits.global

  private val logger: Logger = LoggerFactory.getLogger(getClass)

  protected def commandFunction: CommandFunction

  override def receive: Receive = {
    case request: CommandRequest =>
      val sessionActorRef = sender()
      if (commandFunction.isDefinedAt(request)) {
        commandFunction.apply(request)
          .onComplete({
            case Success(commandResponse) =>
              sessionActorRef ! commandResponse
            case Failure(t) =>
              logger.trace(s"Failure when execute command: ${t.getMessage}")
          })
      }
  }
}

trait CommandFunction extends PartialFunction[CommandRequest, Future[CommandResponse]]

trait CommandRequest {

  def text: String
}

trait CommandResponse {

  def text: String
}

