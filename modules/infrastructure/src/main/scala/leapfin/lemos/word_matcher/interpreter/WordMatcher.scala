package leapfin.lemos.word_matcher.interpreter

import akka.actor.ActorSystem
import leapfin.infrastructure.stream.utils.search.SlidingWindowSearch
import leapfin.infrastructure.stream.utils.search.SlidingWindowSearch._
import leapfin.lemos.word_matcher.Status
import leapfin.lemos.word_matcher.algebra.WordMatcher.MatchResult
import leapfin.lemos.word_matcher.algebra.{WordMatcher => WordMatcherContract}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration
import scala.language.postfixOps

class WordMatcher(
    config: Config,
    logger: Logger = successLogger
)(implicit system: ActorSystem, ec: ExecutionContext)
    extends WordMatcherContract {

  override def matchWord(
      stream: LazyList[Char],
      searchWord: String,
      timeout: FiniteDuration
  ): MatchResult = {
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
    ) match {
      case Left(value) =>
        Left(Status.NotFound(value.byteCount))
      case Right(value) =>
        Right(Status.Success(value.elapsedTime, value.byteCount))
    }
  }

}
