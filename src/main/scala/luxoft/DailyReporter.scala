package luxoft

import zio.Console
import zio.Task
import zio.ZIO

import scala.collection.immutable.HashMap
import scala.util.Failure
import scala.util.Success
import scala.util.Try

trait DailyReporter:
  def report(content: TaskStream[String]): Task[DailyReport]

object DailyReporter extends DailyReporterFactory:

  private val CSVRegex      = """\s*,\s*"""
  private val SensorIDField = "sensor-id"
  private val HumidityField = "humidity"
  private val NoHumidyValue = "NaN"

  override def apply(name: String): Task[DailyReporter] =
    ZIO.succeed(Impl(name))

  private class Header(index: Map[String, Int]):

    def get(fieldName: String, fields: Array[String]): Option[String] =
      index.get(fieldName) match
        case Some(idx) => get(idx, fields)
        case None      => None

    def get(idx: Int, fields: Array[String]): Option[String] =
      Option.when(idx >= 0 && idx < fields.length)(fields(idx))

  private class Impl(name: String) extends DailyReporter:

    override def report(content: TaskStream[String]): Task[DailyReport] =
      content
        .mapAccumZIO(Option.empty[Header])(readHeader)
        .zipWithIndex
        .collectZIO { case (Some((header, line)), lineNum) =>
          readLine(header, line, lineNum + 1)
        }
        .runFold(DailyReport.empty)(aggregate)

    private def aggregate(report: DailyReport, measurement: Measurement): DailyReport =
      report.aggregate(measurement)

    private def readHeader(opt: Option[Header], line: String): Task[(Option[Header], Option[(Header, String)])] =
      opt match
        case Some(header) =>
          ZIO.succeed(opt -> Some(header -> line))

        case None =>
          for header <- readHeader(line) yield (Some(header), None)

    private def readHeader(line: String): Task[Header] = ZIO.attempt {
      val map = line.split(CSVRegex).zipWithIndex.to(HashMap)
      if (map.contains(SensorIDField) && map.contains(HumidityField))
        Header(map)
      else
        throw CSVException.InvalidHeader(s"Invalid CSV, it is necessary: $SensorIDField, $HumidityField")
    }

    private def readLine(header: Header, line: String, lineNum: Long): Task[Measurement] = ZIO.attempt {
      val fields = line.split(CSVRegex)

      (header.get(SensorIDField, fields), header.get(HumidityField, fields)) match
        case (Some(sensorId), Some(humidity)) => readRecord(sensorId, humidity, lineNum)
        case _                                => throw CSVException.InvalidRecord("")
    }

    private def readRecord(sensorId: String, humidity: String, lineNum: Long): Measurement =
      Try(humidity.toInt) match
        case Success(value) =>
          if value >= 0 && value <= 100 then Measurement(sensorId, Some(value))
          else throw CSVException.InvalidRecord(s"Invalid humidity value at $name:$lineNum: $value!")

        case Failure(cause) =>
          if humidity == NoHumidyValue then Measurement(sensorId, None)
          else throw CSVException.InvalidRecord(s"Unexpected humidity value at $name:$lineNum: $humidity.", cause)
