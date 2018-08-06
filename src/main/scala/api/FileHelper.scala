package api

import java.io.File
import java.util.UUID

import akka.http.scaladsl.server.directives.FileInfo

trait FileHelper {

  def tmpFile(fileInfo: FileInfo): File = {
    val tmpName = tmpFileName(fileInfo)
    File.createTempFile(tmpName._1, tmpName._2)
  }

  def tmpFileName(fileInfo: FileInfo): (String, String) = (UUID.randomUUID().toString, "." + extension(fileInfo.fileName))

  private def extension(fileName: String): String =  fileName.lastIndexOf('.') match {
    case 0 => ""
    case i => fileName.substring(i + 1)
  }
}
