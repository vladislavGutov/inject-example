package com.github

import cats.data.EitherT
import cats.{FlatMap, Functor, Id, Monad}
import cats.syntax.all._
import io.circe.syntax._
import io.circe.parser

object Main {


  def program[F[_]: Monad](
      mq: MessageQueue[EitherT[F, Throwable, *]],
      logic: BusinessLogic[EitherT[F, Throwable, *]],
      sink: Sink[EitherT[F, Throwable, *]]
  ): EitherT[F, Throwable, Unit] =
    for {
      message <- mq.nextMessage()
      result  <- logic.react(message)
      _       <- sink.write(result)
    } yield ()

  def main(args: Array[String]): Unit = {
    val message =
      """
        |{
        |  "@type": "Message1",
        |  "value": "hello"
        |}
        |""".stripMargin

    val mq    = MessageQueue[Id](() => parser.parse(message))
    val logic = BusinessLogic[EitherT[Id, Throwable, *]]()
    val sink  = Sink[EitherT[Id, Throwable, *]]()

    program(mq, logic, sink)




  }

}
