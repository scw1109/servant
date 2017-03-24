package com.github.scw1109.servant.connector_new.facebook

import com.github.scw1109.servant.connector_new.facebook.model.Messaging
import com.github.scw1109.servant.connector_new.{ConnectionActor, FacebookConfig}
import com.github.scw1109.servant.session.ReceivedMessage
import org.slf4j.{Logger, LoggerFactory}

/**
  * @author scw1109
  */
class FacebookActor(facebookConfig: FacebookConfig) extends ConnectionActor {

  private val logger: Logger = LoggerFactory.getLogger(getClass)

  private var messageSender: FacebookMessageSender = _
  private var connector: FacebookConnector = _

  override def preStart(): Unit = {
    super.preStart()

    messageSender = new FacebookMessageSender(facebookConfig)
    connector = new FacebookConnector(facebookConfig, self)
  }

  override def receive: Receive = {
    case m if m.isInstanceOf[Messaging] =>
      val messaging = m.asInstanceOf[Messaging]
      messaging.message match {
        case Some(message) =>
          dispatchMessage(ReceivedMessage(
            s"${messaging.sender}_${messaging.recipient}",
            message.mid,
            message.text,
            messaging
          ))
        case None =>
      }
    case FacebookMessageRef(textMessage) =>
      messageSender.sendMessage(textMessage)
  }
}