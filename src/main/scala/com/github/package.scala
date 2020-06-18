package com

import cats.Applicative
import cats.data.{EitherT, NonEmptyList}
import cats.syntax.applicative._
import cats.syntax.semigroupk._
import cats.syntax.reducible._
import com.github.inject._
import io.circe.{Decoder, Json}

package object github {

  type :+:[A, B] = Either[A, B]

  type Messages = Message1 :+: Message2 :+: ControlMessage

  implicit val messagesDecoder: Decoder[Messages] =
    NonEmptyList.of(
      Decoder[Message1].inj[Messages],
      Decoder[Message2].inj[Messages],
      Decoder[ControlMessage].inj[Messages]
    ).reduceK


  trait MessageQueue[F[_]] {
    def nextMessage(): F[Messages]
  }

  trait BusinessLogic[F[_]] {
    def react(message: Messages): F[Int]
  }

  trait Sink[F[_]] {
    def write(value: Int): F[Unit]
  }

  object MessageQueue {
    def apply[F[_]: Applicative](
        m: () => Either[Throwable, Json]
    ): MessageQueue[EitherT[F, Throwable, *]] =
      () => {
        EitherT.fromEither[F](
          for {
            json    <- m()
            message <- json.as[Messages]
          } yield message
        )
      }
  }

  object BusinessLogic {
    def apply[F[_]: Applicative](): BusinessLogic[F] = {
      case Left(v)         => v.value.length.pure[F]
      case Right(Left(v))  => v.value.pure[F]
      case Right(Right(_)) => (-1).pure[F]
    }
  }

  object Sink {
    //unsafe
    def apply[F[_]: Applicative](): Sink[F] =
      v => println(s"Value is $v").pure[F]
  }

}
