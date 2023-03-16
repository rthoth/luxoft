package luxoft

import zio.Console
import zio.Task
import zio.stream.ZStream

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

object Reporter:

  def apply(files: TaskStream[Path]): Task[Report] =
    val context =
      for path <- files
      yield (path.toString(), ZStream.fromJavaStream(Files.lines(path, StandardCharsets.UTF_8)))

    new Reporter(DailyReporter).report(context)

class Reporter(factory: DailyReporterFactory):

  def report(context: TaskStream[(String, TaskStream[String])]): Task[Report] =
    context
      .mapZIO(computeDailyReport)
      .runFold(Report.empty)(aggregateDailyReport)

  private def computeDailyReport(name: String, content: TaskStream[String]): Task[DailyReport] =
    for
      dailyReporter <- factory(name)
      report        <- dailyReporter.report(content)
    yield report

  private def aggregateDailyReport(report: Report, dailyReport: DailyReport): Report =
    report.aggregate(dailyReport)
