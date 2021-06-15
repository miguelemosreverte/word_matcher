package leapfin.lemos.word_matcher.algebra

import leapfin.lemos.word_matcher.Status.{NotFound, Success}
import leapfin.lemos.word_matcher.algebra.WordMatcher.MatchResult

import scala.concurrent.duration.FiniteDuration

object WordMatcher {
  type MatchResult = Either[NotFound, Success]
}
trait WordMatcher {
  def matchWord(
      stream: LazyList[Char],
      word: String,
      timeout: FiniteDuration
  ): MatchResult
}
