package leapfin.lemos.word_matcher.status

import leapfin.lemos.word_matcher.Status
import leapfin.lemos.word_matcher.Status._

object Writeable {

  def writeStatuses =
    (statuses: Iterable[Status]) => {
      val format = "%-40s%-40s%-40s %n"
      System.out.printf(format, Writeable[Title].write(Title()): _*)
      statuses.map { status =>
        System.out.printf(format, Writeable[Status].write(status): _*)
      }
    }

  trait Writeable[T] {
    def write(x: T): Seq[String]
  }

  implicit def apply[A](implicit instance: Writeable[A]): Writeable[A] =
    instance

  implicit def toWriteable[T](p: T => Seq[String]): Writeable[T] =
    (x: T) => p(x)

  case class Title()
  implicit val TitlePrinter: Writeable[Title] = toWriteable[Title] {
    case Title() =>
      Seq(s"[elapsed [ms]]", "[byte_cnt]", "[status]")
  }
  implicit val SuccessPrinter: Writeable[Success] = toWriteable[Success] {
    case Success(elapsedTime, byteCount) =>
      Seq(s"[${elapsedTime.toMillis}]", s"[${byteCount}]", s"[SUCCESS]")
  }
  implicit val NotFoundPrinter: Writeable[NotFound] = toWriteable[NotFound] {
    case NotFound(byteCount) =>
      Seq(s"[]", s"[${byteCount}]", s"[TIMEOUT]")
  }
  implicit val StatusPrinter: Writeable[Status] = toWriteable[Status] {
    case notFound: NotFound => Writeable[NotFound].write(notFound)
    case success: Success   => Writeable[Success].write(success)
  }

}
