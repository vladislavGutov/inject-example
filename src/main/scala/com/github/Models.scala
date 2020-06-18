package com.github

import io.circe.generic.semiauto._
import io.circe.{Codec, Decoder}

case class Message1(value: String)
case class Message2(value: Int)
case class ControlMessage(command: String)

object Message1 extends CirceConfig {

  implicit val decoder: Decoder[Message1] =
    validate(deriveDecoder[Message1], "Message1")
}

object Message2 extends CirceConfig {
  implicit val decoder: Decoder[Message2] =
    validate(deriveDecoder[Message2], "Message2")
}

object ControlMessage extends CirceConfig {
  implicit val decoder: Decoder[ControlMessage] =
    validate(deriveDecoder[ControlMessage], "ControlMessage")

}

trait CirceConfig {
  val discriminator = "@type"

  def validate[A](decoder: Decoder[A], tpe: String): Decoder[A] =
    decoder.validate(
      _.get[String](discriminator).contains(tpe),
      s"Not a $tpe"
    )
}
