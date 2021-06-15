package leapfin.infrastructure.stream.utils.search.logger.algebra

import leapfin.infrastructure.stream.utils.search.logger.domain.MatchResultByThread

trait Logger {
  def feed: MatchResultByThread => Unit

  def printSummarization: Unit
}
