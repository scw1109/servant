package com.github.scw1109.servant.connector

import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

import akka.actor.{ActorRef, Cancellable, Props}
import com.github.scw1109.servant.core.actor.ActorBase
import com.github.scw1109.servant.core.session.{Reply, Session, SessionActor, SessionEvent}

import scala.collection.mutable
import scala.concurrent.duration.Duration

/**
  * @author scw1109
  */
abstract class ServiceActor
[C <: Connector, R <: Receiver[C], S <: Sender[C]]
(connector: C) extends ActorBase {

  import ServiceActor._

  protected val sessions: mutable.Map[String, Session] = mutable.Map()

  protected val receiveActor: ActorRef = createReceiveActor(connector)
  protected val sendActor: ActorRef = createSendActor(connector)

  private var sessionCleanScheduler = None: Option[Cancellable]

  override def preStart(): Unit = {
    super.preStart()

    sessionCleanScheduler = Option(
      context.system.scheduler.schedule(
        Duration(30, TimeUnit.MINUTES),
        Duration(30, TimeUnit.MINUTES),
        self, SessionClean
      )
    )
  }

  override def postStop(): Unit = {
    sessionCleanScheduler.foreach(_.cancel())

    super.postStop()
  }

  final override def receive: Receive = receiveMessage orElse receiveExtension

  final private val receiveExtension: Receive = {
    case reply: Reply =>
      sendActor.tell(reply, self)

    case SessionClean =>
      sessions filter {
        (entry) =>
          Instant.ofEpochMilli(entry._2.lastMessageTime)
            .plus(30, ChronoUnit.MINUTES)
            .isBefore(Instant.now())
      } foreach {
        (entry) =>
          context.stop(entry._2.sessionActor)
          sessions.remove(entry._1)
      }
  }

  protected def dispatchToSession(sessionEvent: SessionEvent): Unit = {
    val sessionKey = sessionEvent.sessionKey
    if (sessions.contains(sessionKey)) {
      sessions.get(sessionKey).foreach {
        session =>
          session.sessionActor.tell(sessionEvent, self)
          session.lastMessageTime = System.currentTimeMillis()
      }
    } else {
      val sessionActor = context.actorOf(
        Props(classOf[SessionActor], sessionKey, self, connector))
      sessionActor.tell(sessionEvent, self)
      val session = Session(sessionKey, sessionActor)
      session.lastMessageTime = System.currentTimeMillis()
      sessions += (sessionKey -> session)
    }
  }

  protected def createReceiveActor(connector: C): ActorRef

  protected def createSendActor(connector: C): ActorRef

  def receiveMessage: Receive
}

object ServiceActor {

  case class SessionClean()

}