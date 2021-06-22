package leapfin.lemos.word_matcher.interpreter

import akka.actor.ActorSystem
import leapfin.infrastructure.stream.utils.search.SlidingWindowSearch
import leapfin.infrastructure.stream.utils.search.logger.algebra.Logger
import leapfin.infrastructure.stream.utils.search.logger.interpreter.SuccessLoger
import leapfin.lemos.word_matcher.Status
import leapfin.lemos.word_matcher.algebra.WordMatcher.MatchResult
import leapfin.lemos.word_matcher.algebra.{WordMatcher => WordMatcherContract}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration
import scala.language.postfixOps

class WordMatcher(
    config: Config = Config(),
    logger: MatchResult => Unit = _ => ()
) extends WordMatcherContract {

  override def matchWord(
      stream: LazyList[Char],
      searchWord: String,
      timeout: FiniteDuration
  ) = {
    new SlidingWindowSearch[Char](
      config.verbose,
      logger
    ).search(
      windowSize = searchWord.length,
      windowStep = 1,
      parallelism = config.workers,
      predicate = _.map(_._1).mkString == searchWord,
      stream,
      timeout
    )
  }

}
