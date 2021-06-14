package leapfin.lemos.word_matcher

import leapfin.lemos.word_matcher.algebra.Status._
import leapfin.lemos.word_matcher.algebra.{WordMatcher, _}

import scala.concurrent.duration.FiniteDuration
import scala.language.postfixOps

case object Mock extends WordMatcher {

  override def matchWord(
      stream: LazyList[Char],
      word: String,
      timeout: FiniteDuration
  ): Either[Status.Failure, Status.Success] = {
    val deadline = timeout fromNow

    stream.zipWithIndex
      .map {
        case (character, byteIndex) =>
          val `found the word` =
            character == word.charAt(0) && stream
              .slice(byteIndex, byteIndex + word.length)
              .mkString == word

          if (deadline.isOverdue || !`found the word`)
            Left(Status.Timeout(byteIndex))
          else if (!`found the word`) Left(Status.NotFound(byteIndex))
          else
            Right(
              Status.Success(
                elapsedTime = deadline.time,
                byteCount = byteIndex
              )
            )
      }
      .reduceLeft[Either[Status.Failure, Status.Success]] {
        case (Left(a), Left(b)) =>
          (a, b) match {
            case (a: Timeout, b: Timeout) =>
              //println(a, b)
              if (a.byteCount > b.byteCount) Left(a) else Left(b)
            case (a: Timeout, b: Status.NotFound) => Left(a)
            case (a: Status.NotFound, b: Timeout) => Left(b)
            case (a: Status.NotFound, b: Status.NotFound) =>
              if (a.byteCount > b.byteCount) Left(a) else Left(b)
          }
        case (r @ Right(a), Left(b))        => r
        case (Left(a), r @ Right(b))        => r
        case (r1 @ Right(a), r2 @ Right(b)) => r1
      }
  }

}
