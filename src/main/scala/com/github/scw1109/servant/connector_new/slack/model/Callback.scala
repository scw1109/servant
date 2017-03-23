package com.github.scw1109.servant.connector_new.slack.model

/**
  * @author scw1109
  */
case class Callback(event_id: String, event_time: Long,
                    team_id: String, api_app_id: String,
                    event: Message, `type`: String,
                    authed_users: Seq[String])

object CallbackRef {
  def unapply(callback: Callback): Option[Callback] = {
    Some(callback)
  }
}