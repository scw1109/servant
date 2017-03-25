package com.github.scw1109.servant.command.dictionary

import java.nio.charset.StandardCharsets

import com.github.scw1109.servant.command.{CommandRequest, CommandResponse}
import com.github.scw1109.servant.core.session.TextMessageRef
import com.github.scw1109.servant.util.{Helper, Resources}
import org.jsoup.Jsoup

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

/**
  * @author scw1109
  */
class UrbanDictionary extends Dictionary {

  private val urbanBaseUrl = "http://www.urbandictionary.com"

  override def searchDictionary(word: String,
                                request: CommandRequest): Future[CommandResponse] = {

    val urbanUrl = s"$urbanBaseUrl/define.php?term=${Helper.urlEncode(word)}"

    Resources.executeAsyncHttpClient {
      _.prepareGet(urbanUrl)
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
    val meaning = doc.select("div.def-panel div.meaning").first().text()

    Success(TextMessageRef(s"= Urban dictionary =\n$meaning", request))

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
