package api

import java.io.File
import java.nio.file.{Files, Paths}
import java.util.UUID

import com.amazonaws.event.ProgressEventType
import akka.http.javadsl.unmarshalling.StringUnmarshaller
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.unmarshalling.Unmarshaller
import akka.stream.scaladsl.{FileIO, Framing}
import akka.util.ByteString
import api.LotsApi.{LimitedResultRequest, PostInput}
import config.Config
import db.dao.LotsDao
import entities.LotData
import mappings.JsonMappings
import com.amazonaws.auth.{AWSCredentials, BasicAWSCredentials}
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.PutObjectRequest
import com.amazonaws.ClientConfiguration
import com.amazonaws.event.{ProgressEvent, ProgressListener}
import com.amazonaws.regions.{Region, Regions}

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

object LotsApi {
  case class PostInput(auctionId: UUID, lotData: LotData)

  case class LimitedResultRequest[T](resourceId: T, limit: Option[Int], offset: Option[Int]) {
    require(
      limit match {
        case Some(limit) => limit > 0 && limit <= 100
        case None => true
      }, "Limit must be a value between 0 and 100 (right inclusive)."
    )
    require(
      offset match {
        case Some(offset) => offset >= 0
        case None => true
      }, "Offset must be a value greater than or equal 0."
    )
  }
}

trait LotsApi extends BaseApi with JsonMappings with InputValidator with LotsDao {

  val lotsApi: Route = pathPrefix("lots") {
    path("thumbnailtmpinmem") {
      post {
        extractRequestContext { ctx =>
          implicit val materializer = ctx.materializer
          implicit val ec = ctx.executionContext

          fileUpload("file") {
            case (metadata, byteSource) =>

              val sumF = byteSource.runFold(ByteString.empty) { case (acc, i) => acc ++ i }

              onSuccess(sumF) { sum =>

                def extension(fileName: String): String =  fileName.lastIndexOf('.') match {
                  case 0 => ""
                  case i => fileName.substring(i + 1)
                }

                val tmpFile = File.createTempFile(UUID.randomUUID().toString, "." + extension(metadata.fileName))
                Files.write(Paths.get(tmpFile.getAbsolutePath), sum.toArray)

                val tmpFilePath = tmpFile.toPath

                val uploadFuture = S3Uploader.upload(tmpFile, tmpFilePath.getFileName.toString)

                onComplete(uploadFuture) {
                  case Success(_) => complete(StatusCodes.OK)
                  case Failure(_)    => complete(StatusCodes.FailedDependency)

                }
              }
          }
        }
      }
    } ~
      path("thumbnailtmpstream") {
        post {
          extractRequestContext { ctx =>
            implicit val materializer = ctx.materializer
            implicit val ec = ctx.executionContext

            fileUpload("file") {
              case (metadata, byteSource) =>

                def extension(fileName: String): String =  fileName.lastIndexOf('.') match {
                  case 0 => ""
                  case i => fileName.substring(i + 1)
                }

                val tmpFile = File.createTempFile(UUID.randomUUID().toString, "." + extension(metadata.fileName))
                val tmpFilePath = tmpFile.toPath

                val action = byteSource.runWith(FileIO.toPath(tmpFilePath))(materializer).map {
                  case ior if ior.wasSuccessful => {

                    val uploadFuture = S3Uploader.upload(tmpFile, tmpFilePath.getFileName.toString)

                    onComplete(uploadFuture) {
                      case Success(_) => complete(StatusCodes.OK)
                      case Failure(_) => complete(StatusCodes.FailedDependency)
                    }
                  }
                  case ior => complete(StatusCodes.EnhanceYourCalm, ior.getError.toString)
                }

                onSuccess(action) { extraction =>
                  complete(StatusCodes.OK)
                }
            }
          }
        }
      } ~
    pathEnd {
      post {
        entity(as[PostInput]) { (input: PostInput) =>
          complete(addLot(input.auctionId, input.lotData))
        }
      } ~
      parameters('auctionId.as[UUID], 'limit.as[Int].?, 'offset.as[Int].?).as(LimitedResultRequest[UUID]) { input =>

        findAuctionOrNotFound(input.resourceId) { auction =>
          complete(getLots(auction.id, input.limit, input.offset))
        }

      }
    }
  }
}

