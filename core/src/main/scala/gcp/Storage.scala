package gcp

import java.io.InputStream
import java.nio.ByteBuffer

import cats.MonadError
import com.google.cloud.ReadChannel
import com.google.cloud.storage.{BlobId, Storage => GStorage, StorageOptions}

trait Storage {
  def fetch[F[_]](bucket: String, name: String, chunkSize: Int, options: GStorage.BlobSourceOption*)(
      implicit F: MonadError[F, Throwable]): F[InputStream]
}

object Storage {

  def apply()(implicit storageService: GStorage): Storage = new Storage {

    def fetch[F[_]](bucket: String, name: String, chunkSize: Int, options: GStorage.BlobSourceOption*)(
        implicit F: MonadError[F, Throwable]): F[InputStream] =
      F.catchNonFatal(transform(storageService.reader(BlobId.of(bucket, name), options: _*), chunkSize))
  }

  def transform(ch: ReadChannel, chunkSize: Int): InputStream =
    new InputStream {

      private[this] val buffer: ByteBuffer =
        ByteBuffer.allocateDirect(chunkSize)

      private[this] lazy val initial: Int = {
        val x = ch.read(buffer)
        buffer.flip()
        x
      }

      private def more: Int = {
        buffer.clear()
        ch.read(buffer)
      }

      private def first: Int = {
        buffer.flip()
        buffer.get & 0xff
      }

      override def available(): Int =
        buffer.remaining()

      override def read(): Int =
        if (initial <= 0) -1
        else if (buffer.hasRemaining) buffer.get & 0xff
        else if (more <= 0) -1
        else first

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
