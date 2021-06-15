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
  def summarizationLog(implicit
      system: ActorSystem
  ): () => SlidingWindowSearchLogger =
    () => {
      val logger = system.actorOf(Props(new Logger))
      (matchResult: MatchResultByThread) => logger ! matchResult
    }
  case object PrintToConsole

}

class Logger extends Actor {
  var statuses: MutableMap[ThreadId, Status] = MutableMap.empty
  this.context.system.getScheduler.scheduleAtFixedRate(
    initialDelay = 0 second,
    interval = 0.5 seconds,
    receiver = self,
    message = PrintToConsole
  )(this.context.dispatcher)

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
      print("\u001b[2J") // clear console
      println(statuses.mkString("\n"))
  }
}
