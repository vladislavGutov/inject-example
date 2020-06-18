package com

package object github {

  trait MessageQueue {
    def nextMessage(): Either[Message1, Either[Message2, ControlMessage]]
  }

  trait BusinessLogic {
    def react(message: Either[Message1, Either[Message2, ControlMessage]]): Int
  }

  trait Sink {
    def write(value: Int): Unit
  }

  object MessageQueue {
    def apply(m: () => Either[Message1, Either[Message2, ControlMessage]]): MessageQueue = () => m()
  }

  object BusinessLogic {
    def apply(): BusinessLogic = {
      case Left(v)  => v.value.length
      case Right(Left(v)) => v.value
      case Right(Right(_)) => -1
    }
  }

  object Sink {
    def apply(): Sink = v => println(s"Value is $v")
  }


}
