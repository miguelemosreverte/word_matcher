package leapfin.infrastructure.stream.utils.search

import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl._
import leapfin.lemos.word_matcher.Status.{NotFound, Success}
import leapfin.lemos.word_matcher.algebra.WordMatcher.MatchResult

import scala.concurrent._
import scala.concurrent.duration._
import scala.language.postfixOps

class SlidingWindowSearch[A](
    verbose: Boolean = true,
    logger: MatchResult => Unit
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
            this logger result
          result
        }
        .takeWhile(
          substream => substream.isLeft && deadline.hasTimeLeft,
          inclusive = true
        )
        .toMat(Sink.last)(Keep.both)
        .run()

    status foreach { _ =>
      killswitch.shutdown()
    }

    Await.result(status, Duration.Inf)
  }

}
