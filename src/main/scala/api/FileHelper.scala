package api

import java.util.UUID

import akka.http.scaladsl.server.directives.FileInfo

trait FileHelper {

  def tmpFileName(fileInfo: FileInfo): (String, String) = (UUID.randomUUID().toString, "." + extension(fileInfo.fileName))

  private def extension(fileName: String): String =  fileName.lastIndexOf('.') match {
    case 0 => ""
    case i => fileName.substring(i + 1)
  }
}
