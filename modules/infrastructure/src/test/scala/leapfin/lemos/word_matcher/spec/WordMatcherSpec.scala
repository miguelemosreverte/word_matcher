package leapfin.lemos.word_matcher.spec

import akka.actor.ActorSystem
import leapfin.infrastructure.stream.utils.search.SlidingWindowSearch.successLogger
import leapfin.lemos.word_matcher
import leapfin.lemos.word_matcher.algebra
import leapfin.lemos.word_matcher.interpreter.{Config, WordMatcher}

object WordMatcherSpec {
  lazy val system = ActorSystem("WordMatcherSpec")
  lazy val executionContext = system.dispatcher
}
class WordMatcherSpec
    extends algebra.WordMatcherSpec(
      new WordMatcher(
        config = Config(),
        logger = successLogger
      )(
        WordMatcherSpec.system,
        WordMatcherSpec.executionContext
      )
    )
