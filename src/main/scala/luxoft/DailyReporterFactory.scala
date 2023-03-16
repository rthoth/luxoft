package luxoft

import zio.Task

trait DailyReporterFactory:
  def apply(name: String): Task[DailyReporter]
