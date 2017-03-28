package com.github.scw1109.servant.command.dictionary

import java.nio.charset.StandardCharsets

import com.github.scw1109.servant.core.session.{Reply, SessionEvent}
import com.github.scw1109.servant.util.{Helpers, Resources}
import com.typesafe.config.Config
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  * @author scw1109
  */
class DictionaryDotCom(config: Config) extends Dictionary(config) {

  private val dictDotComBaseUrl = "http://www.dictionary.com"

  override def search(word: String, sessionEvent: SessionEvent): Future[Option[Reply]] = {

    val dictDotComUrl = s"$dictDotComBaseUrl/browse/${Helpers.urlEncode(word)}"

    Resources.executeAsyncHttpClient {
      _.prepareGet(dictDotComUrl)
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
    val explains = doc.select("div.def-content")
      .toArray(Array[Element]())
      .take(3)
    val meaning = explains.zipWithIndex.map {
      case (e, i) =>
        s"${i + 1}. ${e.text()}"
    }.mkString("\n")

    Option(Reply(s"= Dictionary_com =\n$meaning", sessionEvent.event))

    //            incomingMessage.source.getType match {
    //              case SlackType() =>
    //                val attachments = buildAttachments(word, dictDotComUrl,
    //                  "Dictionary.com", s"$dictDotComBaseUrl/favicon.ico",
    //                  "#307dbc")
    //
    //                Servant.sendResponse(
    //                  RichOutgoingMessage(meaning, attachments),
    //                  incomingMessage)
  }
}
