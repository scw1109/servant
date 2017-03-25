package com.github.scw1109.servant.command.earthquake

import java.nio.charset.StandardCharsets

import com.github.scw1109.servant.command.{Command, CommandFunction, CommandRequest, CommandResponse}
import com.github.scw1109.servant.core.session.TextMessageRef
import com.github.scw1109.servant.util.Resources
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.Future
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

/**
  * @author scw1109
  */
class Earthquake extends Command {

  private val logger: Logger = LoggerFactory.getLogger(getClass)

  private val cwbBaseUrl = "http://www.cwb.gov.tw"

  private case class Event(time: String, measure: String,
                           deep: String, location: String, link: String)

  override protected def commandFunction: CommandFunction = new CommandFunction {
    override def isDefinedAt(request: CommandRequest): Boolean = {
      val text = request.text.trim
      text == "earthquake" || text == "地震"
    }

    override def apply(request: CommandRequest): Future[CommandResponse] = {
      val cwbEarthquakeUrl =
        s"$cwbBaseUrl/V7/modules/MOD_EC_Home.htm?_=${System.currentTimeMillis()}"

      Resources.executeAsyncHttpClient {
        _.prepareGet(cwbEarthquakeUrl)
      } successWhen {
        _.getStatusCode == 200
      } transform {
        case Success(response) =>
          val body = response.getResponseBody(StandardCharsets.UTF_8)
          handleResponseBody(request, body)
        case Failure(t) => Failure(t)
      }
    }
  }

  private def handleResponseBody(request: CommandRequest,
                                 body: String): Try[CommandResponse] = {
    val doc = Jsoup.parse(body)
    val rows = doc.select("div.earthshockinfo02 > table.BoxTable tr")
      .toArray(Array[Element]())
      .slice(1, 4)
    val events = rows map {
      r =>
        val cells = r.select("td")
        Event(
          cells.get(1).text(),
          cells.get(4).text(),
          cells.get(5).text(),
          cells.get(6).text(),
          cells.get(7).text()
        )
    }

    val responseText = events map {
      e =>
        s"${e.time} 規模:${e.measure} 深度:${e.deep}公里 位置:${e.location} " +
          s"[$cwbBaseUrl/V7/earthquake/Data/local/${e.link}]"
    } mkString "\n"

    Success(TextMessageRef(responseText, request))

    //            incomingMessage.source.getType match {
    //              case SlackType() =>
    //                val attachments = compact(render(
    //                  events take 1 map {
    //                    e =>
    //                      val imageLink = e.link.replaceAll("\\.htm", ".gif")
    //                      ("color" -> "#0054a6") ~
    //                        ("title" -> s"${e.time} 規模:${e.measure} 深度:${e.deep}公里") ~
    //                        ("title_link" -> s"$cwbBaseUrl/V7/earthquake/Data/local/${e.link}") ~
    //                        ("text" -> e.location) ~
    //                        ("image_url" -> s"$cwbBaseUrl/V7/earthquake/Data/local/$imageLink") ~
    //                        ("footer" -> "中央氣象局") ~
    //                        ("footer_icon" -> s"$cwbBaseUrl/favicon.ico")
    //                  } toList))
    //
    //                Servant.sendResponse(
    //                  RichOutgoingMessage("中央氣象局最近地震", attachments),
    //                  incomingMessage)
    //
    //              case _ =>
    //                val message = events map {
    //                  e =>
    //                    s"${e.time} 規模:${e.measure} 深度:${e.deep}公里 位置:${e.location} " +
    //                      s"[$cwbBaseUrl/V7/earthquake/Data/local/${e.link}]"
    //                } mkString "\n"
    //
    //                Servant.sendResponse(
    //                  TextOutgoingMessage(message),
    //                  incomingMessage)
    //            }
  }
}
