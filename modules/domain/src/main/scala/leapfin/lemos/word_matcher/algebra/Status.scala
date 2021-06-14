package leapfin.lemos.word_matcher.algebra

import scala.concurrent.duration.FiniteDuration

sealed trait Status {
  val byteCount: BigInt
}

object Status {

  case class Success(
      elapsedTime: FiniteDuration,
      byteCount: BigInt
  ) extends Status

  sealed trait Failure extends Status
  case class Timeout(
      byteCount: BigInt
  ) extends Failure

  case class NotFound(
      byteCount: BigInt
  ) extends Failure

}
