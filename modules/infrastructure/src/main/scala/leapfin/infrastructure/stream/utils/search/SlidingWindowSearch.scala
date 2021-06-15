package leapfin.infrastructure.stream.utils.search

import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl._
import leapfin.infrastructure.stream.utils.search.SlidingWindowSearch.{
  Logger,
  MatchResultByThread,
  successLogger
}
import leapfin.lemos.word_matcher.Status.{NotFound, Success}
import leapfin.lemos.word_matcher.algebra.WordMatcher.MatchResult

import scala.concurrent._
import scala.concurrent.duration._
import scala.language.postfixOps

class SlidingWindowSearch[A](
    verbose: Boolean = true,
    logger: Logger = successLogger
)(implicit system: ActorSystem, ec: ExecutionContext) {

  def search(
      windowSize: Int,
      windowStep: Int,
      parallelism: Int,
      predicate: Seq[(A, Long)] => Boolean,
      stream: LazyList[A],
      timeout: FiniteDuration
  ): MatchResult = {
    val deadline = timeout fromNow

    def `get first index of window` =
      (window: Seq[(A, Long)]) =>
        window.headOption match {
          case Some((element, index)) => index
          case None                   => 0
        }
    def `get the time it took to finish` = deadline.time

    val (
      killswitch: UniqueKillSwitch,
      status: Future[MatchResult]
    ) =
      Source
        .fromIterator(() => stream.iterator)
        .zipWithIndex
        .viaMat(KillSwitches.single)(Keep.right)
        .sliding(windowSize, windowStep)
        .mapAsyncUnordered(parallelism) { window =>
          Future {
            if (predicate(window)) {
              Right(
                Success(
                  elapsedTime = `get the time it took to finish`,
                  byteCount = `get first index of window`(window)
                )
              )
            } else {
              Left(
                NotFound(
                  byteCount = `get first index of window`(window)
                )
              )
            }
          }
        }
        .map { result =>
          if (verbose)
            this.logger feed MatchResultByThread(
              result,
              Thread.currentThread().getId
            )
          result
        }
        .takeWhile(
          substream => substream.isLeft && deadline.hasTimeLeft,
          inclusive = true
        )
        .toMat(Sink.last)(Keep.both)
        .run()

    status foreach { _ =>
      this.logger.printSummarization
      killswitch.shutdown()
    }

    Await.result(status, Duration.Inf)
  }

}

object SlidingWindowSearch {

  type ThreadId = Long
  case class MatchResultByThread(matchResult: MatchResult, threadId: ThreadId)
  trait Logger {
    def feed: MatchResultByThread => Unit
    def printSummarization: Unit
  }
  val emptyLogger: Logger = {
    class EmptyLogger extends Logger {
      def printSummarization: Unit = ()

      def feed = _ => ()
    }
    new EmptyLogger
  }
  val successLogger: Logger = {
    class SuccessLoger extends Logger {
      def printSummarization: Unit = println("Done!")
      def feed = {
        case MatchResultByThread(Left(notFound), threadId) => ()
        case MatchResultByThread(Right(success), threadId) =>
          println(s"Found it! -- $success")
      }
    }
    new SuccessLoger
  }
}
