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
    NonEmptyList
      .of(
        Decoder[Message1].inj[Messages],
        Decoder[Message2].inj[Messages],
        Decoder[ControlMessage].inj[Messages]
      )
      .reduceK

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

    class BusinessLogicImpl[F[_]](implicit F: Applicative[F])
        extends BusinessLogic[F] {

      override def react(message: Messages): F[Int] =
        message match {
          case Left(v)         => processMessage1(v)
          case Right(Left(v))  => processMessage2(v)
          case Right(Right(v)) => processControl(v)
        }

      private def processMessage1(message1: Message1): F[Int] =
        message1.value.length.pure[F]

      private def processMessage2(message2: Message2): F[Int] =
        message2.value.pure[F]

      private def processControl(control: ControlMessage): F[Int] =
        (-1).pure[F]

    }

    def apply[F[_]: Applicative](): BusinessLogic[F] = new BusinessLogicImpl[F]
  }

  object Sink {
    //unsafe
    def apply[F[_]: Applicative](): Sink[F] =
      v => println(s"Value is $v").pure[F]
  }

}
