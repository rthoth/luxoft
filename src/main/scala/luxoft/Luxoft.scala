package luxoft

import zio.Console
import zio.Scope
import zio.Task
import zio.UIO
import zio.ZIO
import zio.ZIOAppArgs
import zio.ZIOAppDefault
import zio.stream.ZStream

import java.nio.file.Files
import java.nio.file.Path

object Luxoft extends ZIOAppDefault:

  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    for
      args              <- ZIOAppArgs.getArgs
      tuple             <- openDirectory(args)
      (directory, files) = tuple
      result            <- Reporter(files)
      _                 <- print(result)
    yield ()

  private def openDirectory(args: Seq[String]): Task[(Path, TaskStream[Path])] =
    args.headOption match
      case Some(directory) =>
        ZIO.attempt {
          val path = Path.of(directory)
          path -> ZStream
            .fromJavaStream(Files.list(path))
            .filter(_.toString().endsWith(".csv"))
        }

      case None =>
        ZIO.fail(IllegalArgumentException("It was not informed a directory to read."))

  private def print(report: Report): Task[Unit] =
    for
      _      <- Console.printLine(s"Num of processed files: ${report.total}")
      _      <- Console.printLine(s"Num of processed measurements: ${report.processed}")
      _      <- Console.printLine(s"Num of failed measurements: ${report.failed}")
      ordered = Vector.from(report.statusMap.valuesIterator).sorted
      _      <- Console.printLine("\nSensors with highest avg humidity:\n")
      _      <- ZIO.foreach(ordered)(print)
    yield ()

  private def print(status: SensorStatus): Task[Unit] =
    status.average match
      case Some(avg) => Console.printLine(s"${status.sensorId},${status.minString},${avg},${status.maxString}")
      case None      => Console.printLine(s"${status.sensorId},NaN,NaN")
