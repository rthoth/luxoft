package luxoft

import zio.stream.ZStream


type TaskStream[+A] = ZStream[Any, Throwable, A]
