package com.github

object Main {

  def main(args: Array[String]): Unit = {
    val mq    = MessageQueue(() => Right(Message2(3)))
    val logic = BusinessLogic()
    val sink  = Sink()

    sink.write(logic.react(mq.nextMessage()))
  }

}
