package com.github

import cats.{FlatMap, Functor, Id}
import cats.syntax.all._

object Main {

  def program[F[_]: FlatMap: Functor](
      mq: MessageQueue[F],
      logic: BusinessLogic[F],
      sink: Sink[F]
  ): F[Unit] =
    for {
      message <- mq.nextMessage()
      result  <- logic.react(message)
      _       <- sink.write(result)
    } yield ()

  def main(args: Array[String]): Unit = {
    val mq    = MessageQueue[Id](() => Right(Left(Message2(3))))
    val logic = BusinessLogic[Id]()
    val sink  = Sink[Id]()

    program(mq, logic, sink)

  }

}
