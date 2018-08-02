package api

import java.io.File
import java.nio.file.{Files, Paths}
import java.util.UUID

import akka.http.scaladsl.server.directives.FileInfo
import akka.stream.Materializer
import akka.stream.scaladsl.{FileIO, Source}
import akka.util.ByteString

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

trait LocalUploading {

  def uploadViaInMem(fileInfo: FileInfo, source: Source[ByteString, Any])(implicit ec: ExecutionContext, m: Materializer): Future[File] =
    source.runFold(ByteString.empty) { case (acc, i) => acc ++ i }
    .map { byteString =>
      val file = tmpFile(fileInfo)
      Files.write(Paths.get(file.getAbsolutePath), byteString.toArray)
      file
    }

  def uploadViaStream(fileInfo: FileInfo, source: Source[ByteString, Any])(implicit ec: ExecutionContext, m: Materializer): Future[File] = {
    val file = tmpFile(fileInfo)
    source.runWith(FileIO.toPath(file.toPath))(m).map(_ => file)
  }

  private def tmpFile(fileInfo: FileInfo): File =
    File.createTempFile(UUID.randomUUID().toString, "." + extension(fileInfo.fileName))

  private def extension(fileName: String): String =  fileName.lastIndexOf('.') match {
    case 0 => ""
    case i => fileName.substring(i + 1)
  }
}
