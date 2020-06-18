package com

import cats.{Applicative, Monad}
import cats.data.{EitherT, Kleisli, NonEmptyList}
import cats.syntax.applicative._
import cats.syntax.semigroupk._
import cats.syntax.reducible._
import com.github.inject._
import io.circe.{Decoder, Json}

package object github {

  type :+:[A, B] = Either[A, B]

  type Messages = Message1 :+: Message2 :+: ControlMessage

  type MetadataMessages = MessageWithMetadata[Messages]

  trait MessageQueue[F[_]] {
    def nextMessage(): F[MessageWithMetadata[Messages]]
  }

  trait BusinessLogic[F[_]] {
    def react(message: MetadataMessages): F[Int]
  }

  trait Sink[F[_]] {
    def write(value: Int): F[Unit]
  }

  object MessageQueue {

    implicit val messagesDecoder: Decoder[MetadataMessages] =
      NonEmptyList
        .of(
          Decoder[MessageWithMetadata[Message1]].inj[MetadataMessages],
          Decoder[MessageWithMetadata[Message2]].inj[MetadataMessages],
          Decoder[MessageWithMetadata[ControlMessage]].inj[MetadataMessages]
        )
        .reduceK

    def apply[F[_]: Applicative](
        m: () => Either[Throwable, Json]
    ): MessageQueue[EitherT[F, Throwable, *]] =
      () => {
        EitherT.fromEither[F](
          for {
            json    <- m()
            message <- json.as[MetadataMessages]
          } yield message
        )
      }
  }

  object BusinessLogic {

    class BusinessLogicImpl[F[_]](implicit F: Monad[F])
        extends BusinessLogic[F] {

      override def react(message: MetadataMessages): F[Int] =
        NonEmptyList
          .of(
            processMessage1.prj[MetadataMessages],
            processMessage2.prj[MetadataMessages],
            processControl.prj[MetadataMessages]
          )
          .reduceK
          .run(message)
          .getOrElseF(F.pure(Int.MinValue))

      private val processMessage1: Kleisli[F, MessageWithMetadata[Message1], Int] =
        Kleisli { _.message.value.length.pure[F] }

      private val processMessage2: Kleisli[F, MessageWithMetadata[Message2], Int] =
        Kleisli { _.message.value.pure[F] }

      private val processControl: Kleisli[F, MessageWithMetadata[ControlMessage], Int] =
        Kleisli { _ => (-1).pure[F] }

    }

    def apply[F[_]: Monad](): BusinessLogic[F] = new BusinessLogicImpl[F]
  }

  object Sink {
    //unsafe
    def apply[F[_]: Applicative](): Sink[F] =
      v => println(s"Value is $v").pure[F]
  }

}
