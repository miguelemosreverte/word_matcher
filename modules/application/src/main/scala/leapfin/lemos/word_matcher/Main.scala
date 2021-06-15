package leapfin.lemos.word_matcher

import akka.actor.ActorSystem
import leapfin.infrastructure.stream.utils.search.logger.domain.MatchResultByThread
import leapfin.infrastructure.stream.utils.search.logger.interpreter.AsyncLogger
import leapfin.lemos.word_matcher.interpreter.{Config, WordMatcher}

import java.util.concurrent.Executors
import scala.collection.immutable.LazyList.continually
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration.{Duration, DurationInt}
import scala.language.postfixOps
import scala.util.Random

object Main extends App {

  def showMenu: Option[Config] = {
    import scopt.OParser
    val builder = OParser.builder[Config]
    val menu = {
      import builder._
      OParser.sequence(
        programName("word_matcher"),
        head("word_matcher"),
        opt[Int]('w', "workers")
          .valueName("<0 to 64>")
          .action((workers, config) => config.copy(workers = workers))
          .validate(workers =>
            if (workers > 0 && workers <= 64) success
            else failure("Value <workers> must be between 0 and 64")
          )
          .text(
            "how many workers will perform the same task in parallel"
          ),
        opt[Int]("padding")
          .valueName("<padding>")
          .action((padding, config) => config.copy(padding = padding))
          .validate(padding =>
            if (padding > 0) success
            else failure("Value <padding> must be >0")
          )
          .text(
            "how many pseudorandom stream elements to be expected before the searched element is found"
          ),
        opt[String]("search_word")
          .valueName("<search_word>")
          .action((searchWord, config) => config.copy(searchWord = searchWord))
          .validate(searchWord =>
            if (searchWord.nonEmpty) success
            else failure("Value <searchWord> must be not empty")
          )
          .text(
            "what is the searched element to be found"
          ),
        opt[Int]('t', "timeout")
          .valueName("<seconds>")
          .action((timeout, config) => config.copy(timeout = timeout))
          .validate(timeout =>
            if (timeout > 0) success
            else failure("Value <timeout> must be >0")
          )
          .text(
            "maximum time allowed for the program to run"
          ),
        opt[Unit]("verbose")
          .action((_, c) => c.copy(verbose = true)),
        help("help"),
        note(
          """
            |WordMatcher:
            |    Generates pseudo-random streams of characters 
            |    and distributes the task of finding a string 
            |    to a set of workers with a timeout
            |    """.stripMargin
        )
      )
    }
    OParser.parse(menu, args, Config())
  }

  def word_matcher(config: Config) = {
    implicit val system = ActorSystem("WordMatcher")
    implicit val fixedThreadPool = Executors.newFixedThreadPool(config.workers)
    implicit val ec =
      system.dispatcher // ExecutionContext fromExecutor fixedThreadPool
    // we dont want to pollute the actor system execution context with the tasks performed by the workers
    // ie.: system.dispatcher

    type Stream = LazyList[Char]
    def pseudorandomStreamOfCharacters(N: Int): Stream =
      LazyList
        .fill(N)('-')
        .concat(LazyList.from(config.searchWord.iterator))
        .concat(continually('-'))

    val logger = AsyncLogger.start

    val bound = (config.padding / 2)
    def random = Random.between(-bound, bound)

    val streams: Seq[Stream] =
      (1 to config.workers) map (_ =>
        pseudorandomStreamOfCharacters(
          config.padding + random
        )
      )

    def startProcessing =
      (stream: Stream, streamIndex: Int) =>
        new WordMatcher(
          config.copy(workers = 1),
          logger = { matchResult =>
            logger.feed(
              MatchResultByThread(matchResult, streamIndex)
            )
          }
        ) matchWord (
          stream,
          config.searchWord,
          config.timeout seconds
        )

    val processAllStreams = Future
      .sequence(
        streams.zipWithIndex
          .map {
            case (stream, index) => Future { startProcessing(stream, index) }
          }
      )

    Await.result(
      for {
        done <- processAllStreams
      } yield {
        logger.printSummarization
      },
      Duration.Inf
    )

    fixedThreadPool.shutdown()
    system.terminate()
  }

  showMenu map word_matcher

}
