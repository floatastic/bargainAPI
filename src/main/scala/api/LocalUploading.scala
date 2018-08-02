package api

import java.io.File
import java.nio.file.{Files, Paths}

import akka.http.scaladsl.server.directives.FileInfo
import akka.stream.Materializer
import akka.stream.scaladsl.{FileIO, Source}
import akka.util.ByteString

import scala.concurrent.{ExecutionContext, Future}

trait LocalUploading extends FileHelper {

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

  private def tmpFile(fileInfo: FileInfo): File = {
    val tmpName = tmpFileName(fileInfo)
    File.createTempFile(tmpName._1, tmpName._2)
  }

}
