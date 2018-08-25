package api

import java.io.File

import akka.Done
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.FileInfo
import akka.stream.alpakka.s3.scaladsl.MultipartUploadResult
import akka.stream.scaladsl.{Flow, GraphDSL, Keep, RunnableGraph, Sink, Source}
import akka.util.ByteString
import akka.stream.{ClosedShape, OverflowStrategy, QueueOfferResult}

import scala.concurrent.{Future, Promise}
import scala.concurrent.duration._
import scala.util.{Failure, Success}

trait TestUploadApi extends FileHelper {

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

              val f = UploadPool.queueFileUpload((metadata, byteSource))

              onComplete(f) {
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

object UploadPool {
  import api.ActorSystemImplicits._

  type FileUploadTuple = (FileInfo, Source[ByteString, Any])

  val queueSize = 16

  val flow = Flow[(FileUploadTuple, Promise[MultipartUploadResult])].map { x =>
    val f = x._1._2.runWith(S3Uploader.sink(x._1._1))
    (f, x._2)
  }

  val queue =
    Source.queue[(FileUploadTuple, Promise[MultipartUploadResult])](queueSize, OverflowStrategy.backpressure)
      .via(flow).
      to(
        Sink.foreach { t =>
          t._1.onComplete {
            case Success(value) => t._2.success(value)
            case Failure(e) => t._2.failure(e)
          }
        }
      )
      .run()

  val poolClientFlow = Http().cachedHostConnectionPool[Promise[MultipartUploadResult]]("akka.io")

  def queueFileUpload(t: FileUploadTuple): Future[MultipartUploadResult] = {
    val p = Promise[MultipartUploadResult]()
    queue.offer(t -> p).flatMap {
      case QueueOfferResult.Enqueued    => p.future
      case QueueOfferResult.Dropped     => Future.failed(new RuntimeException("Queue overflowed. Try again later."))
      case QueueOfferResult.Failure(ex) => Future.failed(ex)
      case QueueOfferResult.QueueClosed => Future.failed(new RuntimeException("Queue was closed (pool shut down) while running the request. Try again later."))
    }
  }
}

//object UploadPool {
//  val QueueSize = 32
//
//  val poolClientFlow = Http().cachedHostConnectionPool[Promise[HttpResponse]]("akka.io")
//
//  val queue =
//    Source.queue[(HttpRequest, Promise[HttpResponse])](QueueSize, OverflowStrategy.backpressure)
//      .via(poolClientFlow)
//      .toMat(Sink.foreach({
//        case ((Success(resp), p)) => p.success(resp)
//        case ((Failure(e), p))    => p.failure(e)
//      }))(Keep.left)
//      .run()
//
//  def queueRequest(request: HttpRequest): Future[HttpResponse] = {
//    val responsePromise = Promise[HttpResponse]()
//    queue.offer(request -> responsePromise).flatMap {
//      case QueueOfferResult.Enqueued    => responsePromise.future
//      case QueueOfferResult.Dropped     => Future.failed(new RuntimeException("Queue overflowed. Try again later."))
//      case QueueOfferResult.Failure(ex) => Future.failed(ex)
//      case QueueOfferResult.QueueClosed => Future.failed(new RuntimeException("Queue was closed (pool shut down) while running the request. Try again later."))
//    }
//  }
//}

