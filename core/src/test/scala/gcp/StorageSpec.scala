package gcp

import java.io._
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets._

import com.google.cloud.{ReadChannel, RestorableState}
import org.scalatest.{FunSpec, Matchers}

class StorageSpec extends FunSpec with Matchers {

  class ByteBufferReadChannel(src: ByteBuffer) extends ReadChannel {
    var eof = false
    var open = true
    override def setChunkSize(chunkSize: Int): Unit = ???
    override def capture(): RestorableState[ReadChannel] = ???
    override def seek(position: Long): Unit = ()
    override def close(): Unit = open = false
    override def isOpen: Boolean = open
    override def read(dst: ByteBuffer): Int = src.synchronized {
      if (eof) throw new EOFException()
      else if (src.remaining() == 0) {
        eof = true
        -1
      } else {
        val toWrite = Math.min(src.remaining(), dst.remaining())
        val arr = Array.ofDim[Byte](toWrite)
        src.get(arr)
        dst.put(arr)
        toWrite
      }
    }
  }

  describe("transform") {

    it("should transform to valid InputStream") {
      val str =
        """
          |abc
          |def😅
          |
          |g
          |hijk
          |lmhopqr
          |""".stripMargin
      val bb = ByteBuffer.wrap(Array.concat(str.getBytes(UTF_8), Array()))
      val in = Storage.transform(new ByteBufferReadChannel(bb), 5)

      val reader = new BufferedReader(new InputStreamReader(in))

      val sb = new StringBuilder()
      Iterator
        .continually(reader.readLine())
        .take(7)
        .map(s => s"$s\n")
        .foreach(sb.append)
      reader.close()
      assert(sb.toString() === str)
    }
  }
}
