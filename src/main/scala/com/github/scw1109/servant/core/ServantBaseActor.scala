package com.github.scw1109.servant.core

import akka.actor.Actor
import org.slf4j.{Logger, LoggerFactory}

/**
  * @author scw1109
  */
trait ServantBaseActor extends Actor {

  protected val logger: Logger = LoggerFactory.getLogger(getClass)

  override def aroundPreStart(): Unit = {
    logger.trace(s"Starting actor ${getClass.getName}")
    super.aroundPreStart()
  }

  override def aroundPostStop(): Unit = {
    logger.trace(s"Stopping actor ${getClass.getName}")
    super.aroundPostStop()
  }
}
