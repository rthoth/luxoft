package luxoft

import java.util.concurrent.ThreadLocalRandom

def generateRandomString(bytes: Int = 2): String =
  val random    = ThreadLocalRandom.current()
  val generated = Array[Byte](1)
  val builder   = StringBuilder()

  for _ <- 0 until bytes do
    random.nextBytes(generated)
    if (generated.head < 16)
      builder
        .addAll("0")

    builder.addAll(generated.head.toString())

  builder.result()
