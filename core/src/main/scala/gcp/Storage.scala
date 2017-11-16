package gcp

import java.io.InputStream
import java.nio.ByteBuffer

import cats.MonadError
import com.google.cloud.ReadChannel
import com.google.cloud.storage.{BlobId, StorageOptions, Storage => GStorage}

trait Storage {
  def fetch[F[_]](
      bucket: String,
      name: String,
      chunkSize: Int,
      options: GStorage.BlobSourceOption*)(implicit F: MonadError[F, Throwable]): F[InputStream]
}

object Storage {

  def apply()(implicit storageService: GStorage): Storage = new Storage {

    def fetch[F[_]](
        bucket: String,
        name: String,
        chunkSize: Int,
        options: GStorage.BlobSourceOption*)(implicit F: MonadError[F, Throwable]): F[InputStream] =
      F.catchNonFatal(
        transform(storageService.reader(BlobId.of(bucket, name), options: _*), chunkSize))
  }

  def transform(ch: ReadChannel, chunkSize: Int): InputStream =
    new InputStream {

      private[this] var isEof = false

      private[this] val buffer: ByteBuffer =
        ByteBuffer.allocateDirect(chunkSize)

      ch.read(buffer)
      buffer.flip()

      private def more: Int = {
        if (isEof) -1
        else {
          buffer.clear()
          val a = ch.read(buffer)
          buffer.flip()
          a
        }
      }

      override def available(): Int =
        if (isEof) 0
        else buffer.remaining()

      override def read(): Int = {
        if (isEof) -1
        else if (buffer.hasRemaining || more >= 0)
          buffer.get & 0xff
        else {
          isEof = true
          -1
        }
      }

      override def close(): Unit = {
        super.close()
        ch.close()
        buffer.clear()
      }
    }

  object Service {
    implicit lazy val default: GStorage =
      StorageOptions.getDefaultInstance.getService
  }
}
