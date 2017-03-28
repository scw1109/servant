package com.github.scw1109.servant.command.dictionary

import java.nio.charset.StandardCharsets

import com.github.scw1109.servant.core.session.{Reply, SessionEvent}
import com.github.scw1109.servant.util.{Helpers, Resources}
import com.typesafe.config.Config
import org.jsoup.Jsoup

import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  * @author scw1109
  */
class UrbanDictionary(config: Config) extends Dictionary(config) {

  private val urbanBaseUrl = "http://www.urbandictionary.com"

  override def search(word: String, sessionEvent: SessionEvent): Future[Option[Reply]] = {

    val urbanUrl = s"$urbanBaseUrl/define.php?term=${Helpers.urlEncode(word)}"

    Resources.executeAsyncHttpClient {
      _.prepareGet(urbanUrl)
    } successWhen {
      _.getStatusCode == 200
    } transform {
      case Success(response) =>
        val body = response.getResponseBody(StandardCharsets.UTF_8)
        Success(handleResult(sessionEvent, body))
      case Failure(t) => Failure(t)
    }
  }

  private def handleResult(sessionEvent: SessionEvent, body: String): Option[Reply] = {
    val doc = Jsoup.parse(body)
    val meaning = doc.select("div.def-panel div.meaning").first().text()

    Option(Reply(s"= Urban dictionary =\n$meaning", sessionEvent.event))

    //            incomingMessage.source.getType match {
    //              case SlackType() =>
    //                val attachments = buildAttachments(word, urbanUrl,
    //                  "Urban dictionary", s"$urbanBaseUrl/favicon.ico",
    //                  "#ff8800")
    //
    //                Servant.sendResponse(
    //                  RichOutgoingMessage(meaning, attachments),
    //                  incomingMessage)
  }
}
