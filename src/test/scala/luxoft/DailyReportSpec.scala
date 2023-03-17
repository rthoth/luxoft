package luxoft

import luxoft.SensorStatus.Data
import zio.Scope
import zio.ZIO
import zio.ZLayer
import zio.test.TestEnvironment
import zio.test.assertTrue

object DailyReportSpec extends Spec:

  override def spec: zio.test.Spec[TestEnvironment & Scope, Any] =
    suite("A DailyReport")(
      test("It should count correctly data collected by some sensors.") {
        for
          context     <- TestResource.readLines("csv/dailyReport.csv")
          reporter    <- ZIO.service[DailyReporter]
          (_, content) = context
          report      <- reporter.report(content)
        yield assertTrue(
          report == DailyReport(
            processed = 7,
            failed = 5,
            statusMap = Map(
              "d1" -> SensorStatus(
                sensorId = "d1",
                processed = 3,
                failed = 0,
                data = Some(Data(10, 99, 129))
              ),
              "d2" -> SensorStatus(
                sensorId = "d2",
                processed = 2,
                failed = 1,
                data = Some(Data(40, 40, 80))
              ),
              "d3" -> SensorStatus(
                sensorId = "d3",
                processed = 2,
                failed = 1,
                data = Some(Data(20, 30, 50))
              ),
              "d4" -> SensorStatus(
                sensorId = "d4",
                processed = 0,
                failed = 3,
                data = None
              )
            )
          )
        )
      }
    ).provideSome(
      ZLayer.fromZIO(DailyReporter(generateRandomString(4)))
    )
