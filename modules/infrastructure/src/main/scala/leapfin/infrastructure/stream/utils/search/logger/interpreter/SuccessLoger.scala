package leapfin.infrastructure.stream.utils.search.logger.interpreter

import leapfin.infrastructure.stream.utils.search.logger.algebra.Logger
import leapfin.infrastructure.stream.utils.search.logger.domain.MatchResultByThread

class SuccessLoger extends Logger {
  def printSummarization: Unit = println("Done!")
  def feed = {
    case MatchResultByThread(Left(notFound), threadId) => ()
    case MatchResultByThread(Right(success), threadId) =>
      println(s"Found it! -- $success")
  }
}
