::#!
@echo off
call "C:\Program Files\scala-2.8.0.RC7\bin\scala" %0 %*
goto :eof
::!#

import java.io._

val to = "C:\\Program Files\\celloscala\\celloscala-0.0.1-SNAPSHOT-jar-with-dependencies.jar"
println("Copying to " + to)
copy("target\\celloscala-0.0.1-SNAPSHOT-jar-with-dependencies.jar",to)
println("Done")

def use[T <: { def close(): Unit }](closable: T)(block: T => Unit) {
  try {
    block(closable)
  }
  finally {
    closable.close()
  }
}

@throws(classOf[IOException])
def copy(from: String, to: String) {
  use(new FileInputStream(from)) { in =>
    use(new FileOutputStream(to)) { out =>
      val buffer = new Array[Byte](1024)
      Iterator.continually(in.read(buffer))
          .takeWhile(_ != -1)
          .foreach { out.write(buffer, 0 , _) }
    }
  }
}
