package com.github.scw1109.servant.command.dictionary

import java.nio.charset.StandardCharsets

import com.github.scw1109.servant.command.{CommandRequest, CommandResponse}
import com.github.scw1109.servant.core.TextMessageRef
import com.github.scw1109.servant.util.{Helper, Resources}
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

/**
  * @author scw1109
  */
class DictionaryDotCom extends Dictionary {

  private val dictDotComBaseUrl = "http://www.dictionary.com"

  override def searchDictionary(word: String,
                                request: CommandRequest): Future[CommandResponse] = {

    val dictDotComUrl = s"$dictDotComBaseUrl/browse/${Helper.urlEncode(word)}"

    Resources.executeAsyncHttpClient {
      _.prepareGet(dictDotComUrl)
    } successWhen {
      _.getStatusCode == 200
    } transform {
      case Success(response) =>
        val body = response.getResponseBody(StandardCharsets.UTF_8)
        handleResponseBody(request, body)
      case Failure(t) => Failure(t)
    }
  }

  private def handleResponseBody(request: CommandRequest,
                                 body: String): Try[CommandResponse] = {

    val doc = Jsoup.parse(body)
    val explains = doc.select("div.def-content")
      .toArray(Array[Element]())
      .take(3)
    val meaning = explains.zipWithIndex.map {
      case (e, i) =>
        s"${i + 1}. ${e.text()}"
    }.mkString("\n")

    Success(TextMessageRef(s"= Dictionary_com =\n$meaning", request))

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
