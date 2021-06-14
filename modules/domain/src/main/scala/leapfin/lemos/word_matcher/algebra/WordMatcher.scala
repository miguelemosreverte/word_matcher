package leapfin.lemos.word_matcher.algebra

import leapfin.lemos.word_matcher.algebra.Status.{Failure, Success}

import scala.concurrent.duration.FiniteDuration

trait WordMatcher {
  def matchWord(
      stream: LazyList[Char],
      word: String,
      timeout: FiniteDuration
  ): Either[Failure, Success]
}
