package com.github.scw1109.servant.connector.hipchat.model

/**
  * @author scw1109
  */
case class Message(date: String, id: String, `type`: String,
                   message: String, from: Option[From])
