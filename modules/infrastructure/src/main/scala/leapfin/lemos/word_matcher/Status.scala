package leapfin.lemos.word_matcher

import scala.concurrent.duration.FiniteDuration
import scala.language.implicitConversions

sealed trait Status {
  val byteCount: BigInt
}

object Status {

  case class Success(
      elapsedTime: FiniteDuration,
      byteCount: BigInt
  ) extends Status

  case class NotFound(
      byteCount: BigInt
  ) extends Status

}
