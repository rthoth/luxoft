package luxoft

object CSVException:

  class InvalidHeader(message: String) extends CSVException(message)

  class InvalidRecord(message: String, cause: Throwable = null) extends CSVException(message)

abstract class CSVException(message: String, cause: Throwable = null)
    extends RuntimeException(message, cause, true, false)
