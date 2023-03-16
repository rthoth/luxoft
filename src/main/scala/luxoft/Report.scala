package luxoft

import scala.collection.immutable.HashMap

object Report:
  val empty: Report = Report(HashMap.empty, 0L, 0L, 0L)

final case class Report(statusMap: Map[String, SensorStatus], total: Long, processed: Long, failed: Long):

  def aggregate(dailyReport: DailyReport): Report =

    val newStatusMap = dailyReport.statusMap.foldLeft(statusMap) { case (result, (sensorId, dailyStatus)) =>
      result.get(sensorId) match
        case Some(status) => result + (sensorId -> status.merge(dailyStatus))
        case None         => result + (sensorId -> dailyStatus)
    }

    copy(
      total = total + 1,
      statusMap = newStatusMap,
      processed = processed + dailyReport.processed,
      failed = failed + dailyReport.failed
    )
