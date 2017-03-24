package com.github.scw1109.servant.connector_new.line.model

/**
  * @author scw1109
  */
trait Source {

  def `type`: String

  def id: String
}

case class User(`type`: String = "user", userId: String) extends Source {

  override def id: String = userId
}

case class Group(`type`: String = "group", groupId: String) extends Source {

  override def id: String = groupId
}

case class Room(`type`: String = "room", roomId: String) extends Source {

  override def id: String = roomId
}
