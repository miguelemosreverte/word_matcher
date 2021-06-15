package leapfin.lemos.word_matcher.algebra

import leapfin.lemos.word_matcher.Dataset._
import leapfin.lemos.word_matcher.Status.{NotFound}
import org.junit.Assert._
import org.junit.Test

import scala.concurrent.duration._
import scala.language.postfixOps

abstract class WordMatcherSpec(matcher: WordMatcher) {

  @Test def ShouldFindMatch(): Unit = {
    val timeout = 1 second

    val result = matcher
      .matchWord(
        exampleStreamWithNEmptyChars(N = 1000),
        exampleMatchWord,
        timeout
      )

    result.map { r =>
      assertEquals(
        BigInt(1000),
        r.byteCount
      )
    }

  }

  @Test def ShouldFailByTimeout(): Unit = {
    val timeout = 0 seconds

    val result = matcher
      .matchWord(
        exampleStreamWithNEmptyChars(N = 1000),
        exampleMatchWord,
        timeout
      )

    assertEquals(
      Left(
        NotFound(
          0
        )
      ),
      result
    )

  }

}
