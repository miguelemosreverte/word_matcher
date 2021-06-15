package leapfin.infrastructure.stream.utils.search.logger.domain

import leapfin.lemos.word_matcher.algebra.WordMatcher.MatchResult

case class MatchResultByThread(matchResult: MatchResult, threadId: ThreadId)
