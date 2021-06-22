package leapfin.infrastructure.stream.utils.search

import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl._
import leapfin.lemos.word_matcher.Status.{NotFound, Success}
import leapfin.lemos.word_matcher.algebra.WordMatcher.MatchResult
import zio.{Chunk, Clock, Exit, ExitCode, Has, URIO, ZIO}
import zio.ZIO.never
import zio.stream.experimental.ZStream

import scala.concurrent._
import scala.concurrent.duration._
import scala.language.postfixOps

class SlidingWindowSearch[A](
    verbose: Boolean = true,
    logger: MatchResult => Unit = _ => ()
) {

  def search(
      windowSize: Int,
      windowStep: Int,
      parallelism: Int,
      predicate: Seq[(A, Long)] => Boolean,
      stream: LazyList[A],
      timeout: FiniteDuration
  ): ZIO[Any with Has[Clock], Throwable, Option[MatchResult]] = {
    val deadline = timeout fromNow

    def `get first index of window` =
      (window: Seq[(A, Long)]) =>
        window.headOption match {
          case Some((element, index)) => index
          case None                   => 0
        }
    def `get the time it took to finish` = deadline.time

    ZStream
      .fromIterator(stream.iterator)
      .zipWithIndex
      .grouped("Leapfn".length)
      .map{ window =>
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
      }}
      .map{ result =>
        logger(result)
        result
      }
      .find {
        case Left(value) =>  false
        case Right(value) => true
      }
      .interruptAfter(zio.Duration.fromScala(timeout))
      .runHead


  }

}
