package leapfin.lemos.word_matcher

import akka.actor.{ActorSystem, Terminated}
import leapfin.infrastructure.stream.utils.search.SlidingWindowSearch
import leapfin.infrastructure.stream.utils.search.logger.domain.MatchResultByThread
import leapfin.infrastructure.stream.utils.search.logger.interpreter.AsyncLogger
import leapfin.lemos.word_matcher.Status.{NotFound, Success}
import leapfin.lemos.word_matcher.algebra.WordMatcher.MatchResult
import leapfin.lemos.word_matcher.interpreter.{Config, WordMatcher}
import zio.{Chunk, Clock, Exit, ExitCode, Has, IO, Task, URIO, ZEnv, ZIO}

import java.util.concurrent.Executors
import scala.collection.immutable.LazyList.continually
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration.{Duration, DurationInt}
import scala.language.postfixOps
import scala.util.Random
import zio.Console._
import zio.stream.experimental.ZStream

object Main extends zio.App {

  implicit val system = ActorSystem("word_matcher")

  val logger = AsyncLogger.start

  def run(args: List[String]) = {
    val done = for {
      _ <- putStrLn(s"Welcome to ZIO.")
      done = word_matcher(Config())
    } yield {
      println("FINISHED?")
      done
    }
    done.exitCode
  }

  def word_matcher(config: Config) = {

    type Stream = LazyList[Char]
    def pseudorandomStreamOfCharacters(N: Int): Stream =
      LazyList
        .fill(N)('-')
        .concat(LazyList.from(config.searchWord.iterator))
        .concat(continually('-'))

    println(config.padding - (config.padding % config.searchWord.length))

    val streams: Seq[Stream] = (1 to config.workers) map { _ =>
      pseudorandomStreamOfCharacters(
        config.padding - (config.padding % config.searchWord.length)
      )
    }

    def startProcessing =
      (stream: Stream, streamIndex: Int) => {
        println("Starting search")
        println(
          s"Going to stop after ${zio.Duration.fromScala(config.timeout seconds).toSeconds} seconds"
        )
        new WordMatcher(
          config,
          logger =
            result => logger.feed(MatchResultByThread(result, streamIndex))
        ).matchWord(stream, config.searchWord, config.timeout seconds)
      }

    val c: Exit[Throwable, Seq[MatchResult]] = unsafeRunSync(
      ZIO.collectAllPar(streams.zipWithIndex map {
        case (stream, streamIndex) =>
          startProcessing(stream, streamIndex).map { option =>
            option.getOrElse(Left(NotFound(0)))
          }
      })
    )

    logger.printSummarization
    println(c)

    c

  }

}
