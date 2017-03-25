package com.github.scw1109.servant.connector.facebook.model

/**
  * @author scw1109
  */
case class Messaging(sender: Sender, recipient: Recipient,
                     timestamp: Long, message: Option[Message],
                     postback: Option[Postback])