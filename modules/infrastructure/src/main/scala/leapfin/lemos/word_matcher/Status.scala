package leapfin.lemos.word_matcher

import leapfin.lemos.word_matcher.Status.{PrintableStatus}

import scala.concurrent.duration.FiniteDuration

sealed trait Status extends PrintableStatus {
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

  sealed trait PrintableStatus
  case class Title() extends PrintableStatus
  val statusPrinter: PrintableStatus => Seq[String] = {
    case t: Title =>
      Seq(s"[elapsed [ms]]", "[byte_cnt]", "[status]")
    case Success(elapsedTime, byteCount) =>
      Seq(s"[${elapsedTime.toMillis}]", s"[${byteCount}]", s"[SUCCESS]")
    case NotFound(byteCount) =>
      Seq(s"[]", s"[${byteCount}]", s"[TIMEOUT]")
  }

}
