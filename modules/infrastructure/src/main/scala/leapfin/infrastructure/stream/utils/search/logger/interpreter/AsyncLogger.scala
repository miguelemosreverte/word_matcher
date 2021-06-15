package leapfin.infrastructure.stream.utils.search.logger.interpreter

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import leapfin.infrastructure.stream.utils.search.logger.algebra.Logger
import leapfin.infrastructure.stream.utils.search.logger.domain.{
  MatchResultByThread,
  ThreadId
}
import leapfin.infrastructure.stream.utils.search.logger.interpreter.AsyncLogger.PrintToConsole
import leapfin.lemos.word_matcher.Status

import scala.collection.mutable.{Map => MutableMap}
import scala.language.postfixOps

class AsyncLogger(loggerActor: ActorRef) extends Logger {
  def feed = loggerActor ! _
  def printSummarization = loggerActor ! PrintToConsole
}

object AsyncLogger {

  def start(implicit system: ActorSystem) =
    new AsyncLogger(system.actorOf(Props(new AsyncLoggerActor)))

  case object PrintToConsole

  class AsyncLoggerActor extends Actor {
    var statuses: MutableMap[ThreadId, Status] = MutableMap.empty

    override def receive: Receive = {
      case MatchResultByThread(status, threadId) =>
        statuses.get(threadId) match {
          case Some(_: Status.Success) =>
          case _ =>
            statuses.addOne(
              (
                threadId,
                status match {
                  case Left(value)  => value
                  case Right(value) => value
                }
              )
            )
        }

      case PrintToConsole =>
        println(statuses.mkString("\n"))
    }
  }

}
