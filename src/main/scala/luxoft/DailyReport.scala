package luxoft

import scala.collection.immutable.HashMap

object DailyReport:

  val empty: DailyReport = DailyReport(HashMap.empty, 0L, 0L)

final case class DailyReport(statusMap: Map[String, SensorStatus], processed: Long, failed: Long):

  def aggregate(measurement: Measurement): DailyReport =
    val newStatusMap = statusMap.get(measurement.sensorId) match
      case Some(status) => statusMap + (status.sensorId      -> status.aggregate(measurement))
      case None         => statusMap + (measurement.sensorId -> SensorStatus(measurement))

    if measurement.value.isDefined then copy(processed = processed + 1, statusMap = newStatusMap)
    else copy(failed = failed + 1, statusMap = newStatusMap)
