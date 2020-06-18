package com

import cats.Applicative
import cats.syntax.applicative._

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
    def apply[F[_]: Applicative](m: () => Messages): MessageQueue[F] =
      () => m().pure[F]
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
