package luxoft

object SensorStatus:

  given Ordering[SensorStatus] = Ordering.by[SensorStatus, Option[Int]](_.average).reverse

  def apply(measurement: Measurement): SensorStatus =
    measurement.value match
      case Some(value) => SensorStatus(measurement.sensorId, 1, 0, value, value, value)
      case None        => SensorStatus(measurement.sensorId, 0, 1, 0, 0, 0)

final case class SensorStatus(sensorId: String, processed: Long, failed: Long, sum: Long, min: Long, max: Long):

  def aggregate(measurement: Measurement): SensorStatus =
    measurement.value match
      case Some(value) => copy(processed = processed + 1, sum = sum + value, math.min(min, value), math.max(max, value))
      case None        => copy(failed = failed + 1)

  def merge(other: SensorStatus): SensorStatus =
    copy(
      processed = processed + other.processed,
      failed = failed + other.failed,
      sum = sum + other.sum,
      min = math.min(min, other.min),
      max = math.max(max, other.max)
    )

  def average: Option[Int] = Option.when(processed > 0)((sum / processed).toInt)
