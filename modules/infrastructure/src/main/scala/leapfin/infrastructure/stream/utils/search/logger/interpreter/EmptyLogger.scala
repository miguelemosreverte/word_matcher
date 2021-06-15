package leapfin.infrastructure.stream.utils.search.logger.interpreter

import leapfin.infrastructure.stream.utils.search.logger.algebra.Logger

class EmptyLogger extends Logger {
  def printSummarization: Unit = ()

  def feed = _ => ()
}
