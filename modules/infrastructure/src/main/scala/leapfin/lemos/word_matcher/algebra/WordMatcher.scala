package leapfin.lemos.word_matcher.algebra

import leapfin.lemos.word_matcher.Status.{NotFound, Success}
import leapfin.lemos.word_matcher.algebra.WordMatcher.MatchResult
import zio.{Clock, Has, ZIO}

import scala.concurrent.duration.FiniteDuration

object WordMatcher {
  type MatchResult = Either[NotFound, Success]
}
trait WordMatcher {
  def matchWord(
      stream: LazyList[Char],
      word: String,
      timeout: FiniteDuration
  ): ZIO[Any with Has[Clock], Throwable, Option[MatchResult]]
}
