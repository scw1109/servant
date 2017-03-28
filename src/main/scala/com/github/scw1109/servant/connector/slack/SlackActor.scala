package com.github.scw1109.servant.connector.slack

import java.util.concurrent.TimeUnit

import akka.actor.Cancellable
import com.github.scw1109.servant.connector._
import com.github.scw1109.servant.connector.slack.event.Message

import scala.concurrent.duration.Duration

/**
  * @author scw1109
  */
abstract class SlackActor[C <: Slack, R <: Receiver[C], S <: Sender[C]]
(slack: C) extends ServiceActor[C, R, S](slack) {

  protected var infoLoader: SlackInfoLoader = _

  private var infoLoadScheduler = None: Option[Cancellable]

  override def preStart(): Unit = {
    super.preStart()
    infoLoader = new SlackInfoLoader(slack)

    infoLoadScheduler = Option(
      context.system.scheduler.schedule(
        Duration.Zero,
        Duration(5, TimeUnit.MINUTES),
        () => {
          infoLoader.loadAll()
        }
      )
    )
  }

  override def postStop(): Unit = {
    infoLoadScheduler.foreach(_.cancel())

    super.postStop()
  }

  protected def shouldHandleMessage(message: Message): Boolean =
    isIm(message.channel) || isMentioned(message.text)

  protected def isIm(id: String): Boolean = infoLoader.ims.contains(id)

  protected def isMentioned(text: String): Boolean = {
    text.contains(s"<@${infoLoader.botSelfInfo.user_id}>")
  }

  protected def removeMentioned(text: String): String = {
    text.replaceAll(s"<@${infoLoader.botSelfInfo.user_id}>", "")
  }
}
