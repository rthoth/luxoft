package luxoft

import luxoft.SensorStatus.Data
import org.mockito.Mockito // We don't have ScalaMock for Scala3 yet :-(
import zio.Scope
import zio.Task
import zio.ZIO
import zio.ZLayer
import zio.stream.ZStream
import zio.test.TestEnvironment
import zio.test.assertTrue

object ReporterSpec extends Spec:

  override def spec: zio.test.Spec[TestEnvironment & Scope, Any] =
    suite("A Reporter")(
      test("It should compute merge two dailyreports correctly") {

        val s1 = SensorStatus(
          sensorId = "s1",
          processed = 2,
          failed = 2,
          data = Some(Data(0, 10, 10))
        )

        val s2 = SensorStatus(
          sensorId = "s2",
          processed = 0,
          failed = 10,
          data = None
        )

        val report1 = DailyReport(
          processed = 1, // The reporter trusts these numbers.
          failed = 2,
          statusMap = Map(
            "s1" -> s1
          )
        )

        val report2 = DailyReport(
          processed = 3,
          failed = 4,
          statusMap = Map(
            "s1" -> s1,
            "s2" -> s2
          )
        )

        val c1 = ZStream("c1")
        val c2 = ZStream("c2")

        val context = ZStream(
          "c1" -> c1,
          "c2" -> c2
        )

        for
          dailyReporter <- ZIO.service[DailyReporter]
          reporter      <- ZIO.service[Reporter]

          _ = Mockito
                .when(dailyReporter.report(c1))
                .thenReturn(ZIO.succeed(report1))
          _ = Mockito
                .when(dailyReporter.report(c2))
                .thenReturn(ZIO.succeed(report2))

          report <- reporter.report(context)
        yield assertTrue(
          report == Report(
            total = 2,
            processed = 4,
            failed = 6,
            statusMap = Map(
              "s1" -> SensorStatus(
                sensorId = "s1",
                processed = 4,
                failed = 4,
                data = Some(Data(0, 10, 20))
              ),
              "s2" -> SensorStatus(
                sensorId = "s2",
                processed = 0,
                failed = 10,
                data = None
              )
            )
          )
        )
      }
    ).provideSome(
      ZLayer.succeed(Mockito.mock(classOf[DailyReporter])),
      ZLayer {
        for reporter <- ZIO.service[DailyReporter]
        yield new DailyReporterFactory:
          override def apply(name: String): Task[DailyReporter] = ZIO.succeed(reporter)
      },
      ZLayer(ZIO.serviceWith[DailyReporterFactory](new Reporter(_)))
    )
