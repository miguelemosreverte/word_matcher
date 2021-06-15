package leapfin.infrastructure.stream.utils.search

import akka.actor.{Actor, ActorSystem, Props}
import leapfin.infrastructure.stream.utils.search.Logger.{PrintToConsole}
import leapfin.infrastructure.stream.utils.search.SlidingWindowSearch.{
  MatchResultByThread,
  ThreadId,
  Logger => SlidingWindowSearchLogger
}
import leapfin.lemos.word_matcher.Status

import collection.mutable.{Map => MutableMap}
import scala.concurrent.duration.{DurationDouble, DurationInt}
import scala.language.postfixOps
object Logger {

  def asyncLogger(implicit
      system: ActorSystem
  ) = {
    val logger = system.actorOf(Props(new Logger))
    class AsyncLogger extends SlidingWindowSearchLogger {
      def feed = logger ! _
      def printSummarization = logger ! PrintToConsole
    }
    new AsyncLogger
  }

  case object PrintToConsole

}

class Logger extends Actor {
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
