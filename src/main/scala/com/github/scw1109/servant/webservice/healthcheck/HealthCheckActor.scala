package com.github.scw1109.servant.webservice.healthcheck

import com.github.scw1109.servant.util.Helper
import com.github.scw1109.servant.webservice.{HealthCheck, WebServiceActor}
import spark.Spark.get

/**
  * @author scw1109
  */
class HealthCheckActor(healthCheck: HealthCheck) extends WebServiceActor {

  get(s"/${healthCheck.id}", (_, _) => "OK")

  override def receive: Receive = Helper.emptyActorReceive
}
