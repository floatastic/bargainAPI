package api

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import scala.concurrent.duration._

import scala.util.{Failure, Success}

trait TestUploadApi extends FileHelper {
  val testUploadApi: Route = pathPrefix("uploadtest") {
    path("tmpfile") {
      withRequestTimeout(120.seconds) {
        extractRequestContext { ctx =>
          implicit val materializer = ctx.materializer
          implicit val ec = ctx.executionContext

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
      }
    } ~
      path("alpakka") {
        withRequestTimeout(120.seconds) {
          extractRequestContext { ctx =>
            implicit val materializer = ctx.materializer

            extractActorSystem { actorSystem =>

              fileUpload("file") {

                case (metadata, byteSource) =>

                  val uploadFuture = byteSource.runWith(S3Uploader.sink(metadata)(actorSystem, materializer))

                  onComplete(uploadFuture) {
                    case Success(_) => complete(StatusCodes.OK)
                    case Failure(_) => complete(StatusCodes.FailedDependency)
                  }
              }
            }
          }
        }
      }
  }
}
