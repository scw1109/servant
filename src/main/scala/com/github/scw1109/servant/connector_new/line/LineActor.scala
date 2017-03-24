package com.github.scw1109.servant.connector_new.line

import com.github.scw1109.servant.connector_new.line.model.{LineFormats, MessageEvent, TextMessage}
import com.github.scw1109.servant.connector_new.{ConnectionActor, LineConfig}
import com.github.scw1109.servant.session.ReceivedMessage
import org.json4s.Formats
import org.slf4j.{Logger, LoggerFactory}

/**
  * @author scw1109
  */
class LineActor(lineConfig: LineConfig) extends ConnectionActor {

  implicit lazy val formats: Formats = LineFormats.format

  private val logger: Logger = LoggerFactory.getLogger(getClass)

  private var messageSender: LineMessageSender = _
  private var connector: LineConnector = _

  override def preStart(): Unit = {
    super.preStart()

    messageSender = new LineMessageSender(lineConfig)
    connector = new LineConnector(lineConfig, self)
  }

  override def receive: Receive = {
    case event if event.isInstanceOf[MessageEvent] =>
      val messageEvent = event.asInstanceOf[MessageEvent]
      messageEvent.message match {
        case TextMessage(_, id, text) =>
          dispatchMessage(ReceivedMessage(
            s"${messageEvent.`type`}_${messageEvent.source.id}",
            id,
            text,
            messageEvent
          ))
      }
    case LineMessageRef(textMessage) =>
      messageSender.sendMessage(textMessage)
  }
}