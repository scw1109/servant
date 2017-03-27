package com.github.scw1109.servant.connector.hipchat.event

/**
  * @author scw1109
  */
case class Links(members: Option[String], participants: String,
                 self: String, webhooks: String)
