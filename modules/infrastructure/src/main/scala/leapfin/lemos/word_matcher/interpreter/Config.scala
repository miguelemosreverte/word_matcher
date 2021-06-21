package leapfin.lemos.word_matcher.interpreter

case class Config(
    timeout: Int = 10,
    workers: Int = 10,
    searchWord: String = "Leapfn",
    padding: Int = 15,
    verbose: Boolean = true
)
