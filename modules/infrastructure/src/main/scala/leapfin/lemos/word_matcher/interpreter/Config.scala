package leapfin.lemos.word_matcher.interpreter

case class Config(
    timeout: Int = 5,
    workers: Int = 4,
    searchWord: String = "Leapfn",
    padding: Int = 100000,
    verbose: Boolean = true
)
