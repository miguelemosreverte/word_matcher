package leapfin.infrastructure.stream.utils.search.logger.interpreter

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import leapfin.infrastructure.stream.utils.search.logger.algebra.Logger
import leapfin.infrastructure.stream.utils.search.logger.domain.{MatchResultByThread, ThreadId}
import leapfin.infrastructure.stream.utils.search.logger.interpreter.AsyncLogger.PrintToConsole
import leapfin.lemos.word_matcher.Status
import leapfin.lemos.word_matcher.status.Writeable.writeStatuses

import scala.collection.mutable.{Map => MutableMap}
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

class AsyncLogger(loggerActor: ActorRef) extends Logger {
  def feed = loggerActor ! _
  implicit val timeout = Timeout(2 seconds)
  def printSummarization = Await.result((loggerActor ? PrintToConsole).mapTo[akka.Done], 3 seconds)
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
        writeStatuses(
          statuses.values.toSeq
            .sortBy[Long] {
              case Status.Success(elapsedTime, byteCount) =>
                elapsedTime.toMillis
              case Status.NotFound(byteCount) =>
                0L
            }
            .reverse
        )

        println(
          s"${statuses.values.collect {
            case Status.Success(elapsedTime, byteCount) =>
              (byteCount.toDouble / 1000) / elapsedTime.toSeconds
          }.sum / statuses.values.toSeq.length} MB/s was the average speed per worker"
        )
        sender() ! akka.Done

    }
  }

}
