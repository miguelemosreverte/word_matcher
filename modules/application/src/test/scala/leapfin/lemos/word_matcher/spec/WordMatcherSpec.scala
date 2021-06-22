package leapfin.lemos.word_matcher.spec

import akka.actor.ActorSystem
import leapfin.infrastructure.stream.utils.search.logger.domain.MatchResultByThread
import leapfin.lemos.word_matcher.algebra
import leapfin.lemos.word_matcher.interpreter.{Config, WordMatcher}

object WordMatcherSpec {
  lazy val system = ActorSystem("WordMatcherSpec")
  lazy val executionContext = system.dispatcher
}
class WordMatcherSpec
    extends algebra.WordMatcherSpec(
      new WordMatcher(config = Config(), logger = _ => ())(
        //WordMatcherSpec.system,
        WordMatcherSpec.executionContext
      )
    )
