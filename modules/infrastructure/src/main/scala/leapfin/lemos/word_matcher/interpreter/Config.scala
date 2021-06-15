package leapfin.lemos.word_matcher.interpreter

case class Config(
    timeout: Int = 10,
    workers: Int = 100,
    searchWord: String = "Leapfn",
    padding: Int = 150000,
    verbose: Boolean = true
)
