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

    implicit val messagesDecoder: Decoder[Messages] =
      NonEmptyList
        .of(
          Decoder[Message1].inj[Messages],
          Decoder[Message2].inj[Messages],
          Decoder[ControlMessage].inj[Messages]
        )
        .reduceK

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

    class BusinessLogicImpl[F[_]](implicit F: Monad[F])
        extends BusinessLogic[F] {

      override def react(message: Messages): F[Int] =
        NonEmptyList
          .of(
            processMessage1.prj[Messages],
            processMessage2.prj[Messages],
            processControl.prj[Messages]
          )
          .reduceK
          .run(message)
          .getOrElseF(F.pure(Int.MinValue))

      private val processMessage1: Kleisli[F, Message1, Int] =
        Kleisli { _.value.length.pure[F] }

      private val processMessage2: Kleisli[F, Message2, Int] =
        Kleisli { _.value.pure[F] }

      private val processControl: Kleisli[F, ControlMessage, Int] =
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
