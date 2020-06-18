package com

package object github {

  trait MessageQueue {
    def nextMessage(): Either[Message1, Message2]
  }

  trait BusinessLogic {
    def react(message: Either[Message1, Message2]): Int
  }

  trait Sink {
    def write(value: Int): Unit
  }

  object MessageQueue {
    def apply(m: () => Either[Message1, Message2]): MessageQueue = () => m()
  }

  object BusinessLogic {
    def apply(): BusinessLogic = {
      case Left(v)  => v.value.length
      case Right(v) => v.value
    }
  }

  object Sink {
    def apply(): Sink = v => println(s"Value is $v")
  }


}
