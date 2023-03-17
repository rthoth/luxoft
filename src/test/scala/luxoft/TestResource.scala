package luxoft

import zio.Task
import zio.ZIO
import zio.stream.ZStream

import java.io.BufferedReader
import java.io.InputStreamReader

object TestResource:

  def readLines(path: String): Task[(String, TaskStream[String])] = ZIO.attemptBlocking {
    val url = Thread.currentThread().getContextClassLoader().getResource(path)
    url.getFile() -> ZStream
      .fromJavaStream(BufferedReader(InputStreamReader(url.openStream())).lines())
  }
