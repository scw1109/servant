package com.github.scw1109.servant.core.actor

import java.util.concurrent.ExecutorService

import akka.actor.Actor
import com.github.scw1109.servant.util.Resources
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.ExecutionContext

/**
  * @author scw1109
  */
abstract class ActorBase extends Actor {

  protected implicit val executionContext: ExecutionContext = context.dispatcher

  protected implicit val executorService: ExecutorService = Resources.executorService

  protected val logger: Logger = LoggerFactory.getLogger(getClass)

  override def preStart(): Unit = {
    super.preStart()
    logger.trace(s"Starting actor => ${getClass.getName}")
  }

  override def postStop(): Unit = {
    logger.trace(s"Stopping actor => ${getClass.getName}")
    super.postStop()
  }
}
