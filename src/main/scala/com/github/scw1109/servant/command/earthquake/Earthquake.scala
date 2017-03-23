package com.github.scw1109.servant.command.earthquake

import java.nio.charset.StandardCharsets

import com.github.scw1109.servant.Servant
import com.github.scw1109.servant.command.Command
import com.github.scw1109.servant.message.{IncomingMessage, RichOutgoingMessage, SlackType, TextOutgoingMessage}
import com.typesafe.config.Config
import org.asynchttpclient._
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods.{compact, render}
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

import scala.language.postfixOps

/**
  * @author scw1109
  */
object Earthquake extends Command {

  private val asyncHttpClient: AsyncHttpClient =
    new DefaultAsyncHttpClient(
      new DefaultAsyncHttpClientConfig.Builder()
        .setFollowRedirect(true)
        .build())


  private case class Event(time: String, measure: String,
                           deep: String, location: String, link: String)

  override def init(config: Config): Unit = {}

  override def accept(incomingMessage: IncomingMessage): Boolean = {
    val text = incomingMessage.text.trim
    text == "earthquake" || text == "地震"
  }

  override def execute(incomingMessage: IncomingMessage): Unit = {
    val cwbBaseUrl = "http://www.cwb.gov.tw"
    val cwbEarthquakeUrl =
      s"$cwbBaseUrl/V7/modules/MOD_EC_Home.htm?_=${System.currentTimeMillis()}"

    asyncHttpClient
      .prepareGet(cwbEarthquakeUrl)
      .execute(new AsyncCompletionHandler[Unit] {
        override def onCompleted(response: Response): Unit = {
          if (response.getStatusCode == 200) {
            val body = response.getResponseBody(StandardCharsets.UTF_8)
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

            incomingMessage.source.getType match {
              case SlackType() =>
                val attachments = compact(render(
                  events take 1 map {
                    e =>
                      val imageLink = e.link.replaceAll("\\.htm", ".gif")
                      ("color" -> "#0054a6") ~
                        ("title" -> s"${e.time} 規模:${e.measure} 深度:${e.deep}公里") ~
                        ("title_link" -> s"$cwbBaseUrl/V7/earthquake/Data/local/${e.link}") ~
                        ("text" -> e.location) ~
                        ("image_url" -> s"$cwbBaseUrl/V7/earthquake/Data/local/$imageLink") ~
                        ("footer" -> "中央氣象局") ~
                        ("footer_icon" -> s"$cwbBaseUrl/favicon.ico")
                  } toList))

                Servant.sendResponse(
                  RichOutgoingMessage("中央氣象局最近地震", attachments),
                  incomingMessage)

              case _ =>
                val message = events map {
                  e =>
                    s"${e.time} 規模:${e.measure} 深度:${e.deep}公里 位置:${e.location} " +
                      s"[$cwbBaseUrl/V7/earthquake/Data/local/${e.link}]"
                } mkString "\n"

                Servant.sendResponse(
                  TextOutgoingMessage(message),
                  incomingMessage)
            }
          }
        }
      })
  }
}
