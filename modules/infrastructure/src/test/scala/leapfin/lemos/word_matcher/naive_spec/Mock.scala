package leapfin.lemos.word_matcher.naive_spec

import leapfin.lemos.word_matcher.Status
import leapfin.lemos.word_matcher.Status.NotFound
import leapfin.lemos.word_matcher.algebra.WordMatcher.MatchResult
import leapfin.lemos.word_matcher.algebra.{WordMatcher, _}
import zio.{Clock, Has, ZIO}

import scala.concurrent.duration.FiniteDuration
import scala.language.postfixOps

case object Mock extends WordMatcher {

  override def matchWord(
      stream: LazyList[Char],
      word: String,
      timeout: FiniteDuration
  ): ZIO[Any with Has[Clock], Throwable, Option[MatchResult]] = {
    val deadline = timeout fromNow

    val result = stream.zipWithIndex
      .map {
        case (character, byteIndex) =>
          val `found the word` =
            character == word.charAt(0) && stream
              .slice(byteIndex, byteIndex + word.length)
              .mkString == word

          if (deadline.isOverdue || !`found the word`)
            Left(Status.NotFound(byteIndex))
          else if (!`found the word`) Left(Status.NotFound(byteIndex))
          else
            Right(
              Status.Success(
                elapsedTime = deadline.time,
                byteCount = byteIndex
              )
            )
      }
      .takeWhile {
        case Right(value) => true
        case Left(value)  => false
      }
      .foldLeft[MatchResult](
        Left(NotFound(0))
      ) {
        case (Left(a), Left(b)) =>
          (a, b) match {
            case (a: Status.NotFound, b: Status.NotFound) =>
              if (a.byteCount > b.byteCount) Left(a) else Left(b)
          }
        case (r @ Right(a), Left(b))        => r
        case (Left(a), r @ Right(b))        => r
        case (r1 @ Right(a), r2 @ Right(b)) => r1
      }

    zio.stream.ZStream
      .fromEffect(
        ZIO.succeed(result)
      )
      .runHead

  }

}
