package com.github.scw1109.servant.connector

/**
  * @author scw1109
  */
trait EventObject {

  def source: Connector

  def rawEvent: Any
}
