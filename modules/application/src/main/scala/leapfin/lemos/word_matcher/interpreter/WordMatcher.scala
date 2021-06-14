package leapfin.lemos.word_matcher.interpreter

import akka.actor.ActorSystem
import leapfin.infrastructure.stream.utils.search.SlidingWindowSearch
import leapfin.lemos.word_matcher.interpreter.WordMatcher.{
  Logger,
  MatchResult,
  successLogger
}
import leapfin.lemos.word_matcher.algebra.{
  Status,
  WordMatcher => WordMatcherContract
}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration
import scala.language.postfixOps

object WordMatcher {
  type MatchResult = Either[Status.Failure, Status.Success]
  type Logger = MatchResult => Unit
  val emptyLogger: Logger = _ => ()
  val successLogger: Logger = {
    case Left(timeout)  => ()
    case Right(success) => println(s"Found the word! -- $success")
  }
}

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
    ).search(
      windowSize = searchWord.length,
      windowStep = 1,
      parallelism = config.workers,
      predicate = _.map(_._1).mkString == searchWord,
      stream,
      timeout
    ) match {
      case Left(value) =>
        Left(Status.Timeout(value.byteCount))
      case Right(value) =>
        Right(Status.Success(value.elapsedTime, value.byteCount))
    }
  }

}
