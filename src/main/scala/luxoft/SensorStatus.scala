package luxoft

import luxoft.SensorStatus.Data

object SensorStatus:

  given Ordering[SensorStatus] = Ordering.by[SensorStatus, Option[Int]](_.average).reverse

  case class Data(min: Long, max: Long, sum: Long)

  def apply(measurement: Measurement): SensorStatus =
    measurement.value match
      case Some(value) => SensorStatus(measurement.sensorId, 1, 0, Some(Data(value, value, value)))
      case None        => SensorStatus(measurement.sensorId, 0, 1, None)

final case class SensorStatus(sensorId: String, processed: Long, failed: Long, data: Option[Data]):

  def maxString: String = data.map(_.max.toString()).getOrElse("NaN")

  def minString: String = data.map(_.min.toString()).getOrElse("NaN")

  def aggregate(measurement: Measurement): SensorStatus =
    measurement.value match
      case Some(value) => copy(processed = processed + 1, data = computeData(value))
      case None        => copy(failed = failed + 1)

  def merge(other: SensorStatus): SensorStatus =
    copy(
      processed = processed + other.processed,
      failed = failed + other.failed,
      data = mergeData(other.data)
    )

  def average: Option[Int] =
    for v <- data if processed > 0 yield (v.sum / processed).toInt

  private def computeData(value: Int): Option[Data] =
    data match
      case Some(Data(min, max, sum)) => Some(Data(math.min(min, value), math.max(max, value), sum + value))
      case None                      => Some(Data(value, value, value))

  private def mergeData(other: Option[Data]): Option[Data] =
    (data, other) match
      case (Some(Data(minA, maxA, sumA)), Some(Data(minB, maxB, sumB))) =>
        Some(Data(math.min(minA, minB), math.max(maxA, maxB), sumA + sumB))

      case _ => data.orElse(other)
