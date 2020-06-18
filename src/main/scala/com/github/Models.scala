package com.github

import io.circe.generic.semiauto._
import io.circe.{Codec, Decoder}
import monocle.{PIso, PLens}

case class Message1(value: String)
case class Message2(value: Int)
case class ControlMessage(command: String)

case class MessageWithMetadata[T](message: T, metadata: String)

object Message1 extends CirceConfig {

  implicit val decoder: Decoder[MessageWithMetadata[Message1]] =
    validate(deriveDecoder[Message1], "Message1")
      .map(MessageWithMetadata(_, "metadata"))
}

object Message2 extends CirceConfig {
  implicit val decoder: Decoder[MessageWithMetadata[Message2]] =
    validate(deriveDecoder[Message2], "Message2")
      .map(MessageWithMetadata(_, "metadata"))
}

object ControlMessage extends CirceConfig {
  implicit val decoder: Decoder[MessageWithMetadata[ControlMessage]] =
    validate(deriveDecoder[ControlMessage], "ControlMessage")
      .map(MessageWithMetadata(_, "metadata"))

}

object MessageWithMetadata {
  implicit def plens[A, B]
      : PLens[MessageWithMetadata[A], MessageWithMetadata[B], A, B] =
    PLens[MessageWithMetadata[A], MessageWithMetadata[B], A, B](_.message)(b =>
      _.copy(message = b)
    )
}

trait CirceConfig {
  val discriminator = "@type"

  def validate[A](decoder: Decoder[A], tpe: String): Decoder[A] =
    decoder.validate(
      _.get[String](discriminator).contains(tpe),
      s"Not a $tpe"
    )
}
