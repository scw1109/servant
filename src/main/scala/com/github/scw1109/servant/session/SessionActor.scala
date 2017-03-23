package com.github.scw1109.servant.session

import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorRef}
import com.github.scw1109.servant.command_new.CommandSets
import com.github.scw1109.servant.connector_new.SessionClean
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

/**
  * @author scw1109
  */
class SessionActor(sessionKey: String,
                   connectionActor: ActorRef) extends Actor {

  private val logger: Logger = LoggerFactory.getLogger(getClass)

  private val maxHistorySize = 100

  private val historyMessage: mutable.Queue[String] = mutable.Queue()
  private var lastMessageTime: Long = _

  private case class IdleCheck()

  override def preStart(): Unit = {
    context.system.scheduler.schedule(
      Duration(10, TimeUnit.MINUTES),
      Duration(10, TimeUnit.MINUTES),
      () => {
        self ! IdleCheck
      }
    )

    CommandSets.default.foreach(
      props => context.actorOf(props,
        s"commands-${props.actorClass().getSimpleName}")
    )
  }

  override def receive: Receive = {
    case ReceivedMessageRef(receivedMessage) =>
      if (!historyMessage.contains(receivedMessage.messageId)) {
        context.children.foreach(
          command => command.tell(receivedMessage, self)
        )
        recordMessage(receivedMessage.messageId)
      } else {
        logger.trace(s"Skip repeated message: ${receivedMessage.messageId}")
      }
    case TextMessageRef(textMessage) =>
      connectionActor.tell(textMessage, self)
    case IdleCheck =>
      if (Instant.now().minus(6, ChronoUnit.HOURS)
        .toEpochMilli >= lastMessageTime) {

        connectionActor.tell(SessionClean(sessionKey), self)
        context.stop(self)
      }
  }

  private def recordMessage(messageId: String) = {
    if (historyMessage.size >= maxHistorySize) {
      historyMessage.dequeue()
    }
    historyMessage.enqueue(messageId)
    lastMessageTime = System.currentTimeMillis()
  }
}
