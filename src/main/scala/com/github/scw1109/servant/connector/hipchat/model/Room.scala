package com.github.scw1109.servant.connector.hipchat.model

/**
  * @author scw1109
  */
case class Room(id: Long, is_archived: Boolean, links: Links,
                name: String, privacy: String, version: String)
