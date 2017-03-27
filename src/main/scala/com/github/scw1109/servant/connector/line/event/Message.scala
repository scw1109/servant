package com.github.scw1109.servant.connector.line.event

/**
  * @author scw1109
  */
trait Message {

  def id: String

  def `type`: String
}

case class TextMessage(`type`: String = "text", id: String,
                       text: String) extends Message

case class ImageMessage(`type`: String = "image", id: String) extends Message

case class VideoMessage(`type`: String = "video", id: String) extends Message

case class AudioMessage(`type`: String = "audio", id: String) extends Message

case class LocationMessage(`type`: String = "location", id: String,
                           title: String, address: String,
                           latitude: Double, longitude: Double) extends Message

case class StickerMessage(`type`: String = "sticker", id: String,
                          packageId: String, stickerId: String) extends Message
