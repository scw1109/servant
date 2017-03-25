package com.github.scw1109.servant.webservice

import com.github.scw1109.servant.webservice.healthcheck.HealthCheckActor

/**
  * @author scw1109
  */
sealed trait WebService {

  def id: String

  def actorType: Class[_ <: WebServiceActor]
}

case class HealthCheck(id: String) extends WebService {

  override def actorType: Class[_ <: WebServiceActor] = classOf[HealthCheckActor]
}
