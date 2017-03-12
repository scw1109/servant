package com.github.scw1109.servant.connector.slack

import com.typesafe.config.Config
import org.json4s.DefaultFormats
import org.json4s.native.JsonMethods.parse
import spark.{Request, Response}

import scala.util.{Failure, Success, Try}

/**
  * @author scw1109
  */
object Handlers {

  implicit lazy val formats = DefaultFormats

  def apply(config: Config, request: Request, response: Response): Handler = {
    Try {
      val body = parse(request.body())
      (body \ "type").extract[String] match {
        case "url_verification" => new UrlVerification(config, body, response)
        case "event_callback" => new EventCallback(config, body, response)
        case _ => new Unknown
      }
    }
  } match {
    case Success(s) => s
    case Failure(_) => new Unknown
  }
}
