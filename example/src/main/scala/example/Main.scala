package example

import java.io.{BufferedReader, InputStreamReader}
import java.util.zip.GZIPInputStream

import cats.instances.try_._
import gcp.Storage

import scala.util.{Success, Try}

object Main extends App {
  implicit val storage: Storage = {
    implicit val service = Storage.Service.default
    Storage()
  }
  val Array(bucket, name) = args
  val Success(r) = storage
    .fetch[Try](bucket, name, 10)
    .flatMap(a => Try(new BufferedReader(new InputStreamReader(a))))

  try r.lines().forEach((s: String) => println(s))
  finally r.close()
}
