package com.github.scw1109.servant.connector.slack.model

/**
  * @author scw1109
  */
case class User(id: String, name: String, real_name: String,
                is_bot: Boolean, is_admin: Boolean, is_owner: Boolean)
