package api

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import scala.concurrent.duration._

import scala.util.{Failure, Success}

trait TestUploadApi extends FileHelper {

  import api.ActorSystemImplicits._

  val testUploadApi: Route = pathPrefix("uploadtest") {
    path("tmpfile") {
      withRequestTimeout(120.seconds) {
        storeUploadedFile("file", tmpFile) {
          case (_, file) => {

            val uploadFuture = S3Uploader.upload(file, file.toPath.getFileName.toString)

            onComplete(uploadFuture) {
              case Success(_) => complete(StatusCodes.OK)
              case Failure(_) => complete(StatusCodes.FailedDependency)

            }
          }
        }
      }
    } ~
      path("alpakka") {
        withRequestTimeout(500.seconds) {
          fileUpload("file") {

            case (metadata, byteSource) =>
              val uploadFuture = byteSource.runWith(S3Uploader.sink(metadata))

              onComplete(uploadFuture) {
                case Success(_) => complete(StatusCodes.OK)
                case Failure(e) => {
                  println(e); complete(StatusCodes.FailedDependency)
                }
              }
          }
        }
      }
  }
}
