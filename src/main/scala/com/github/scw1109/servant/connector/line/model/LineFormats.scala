package com.github.scw1109.servant.connector.line.model

import org.json4s.{DefaultFormats, Formats}

/**
  * @author scw1109
  */
object LineFormats {

  val format: Formats = DefaultFormats.withHints(LineTypeHints)
    .withTypeHintFieldName("type")
}
