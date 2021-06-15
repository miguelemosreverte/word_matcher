package leapfin.lemos.word_matcher

object Dataset {

  val exampleMatchWord = "leapfn"
  val exampleStream: LazyList[Char] = s"---$exampleMatchWord---".to(LazyList)

  def exampleStreamWithNEmptyChars(N: Int): LazyList[Char] =
    ("-" * N + exampleMatchWord + "-" * N).to(LazyList)

}
