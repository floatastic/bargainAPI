package api

import java.util.UUID

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{PathMatchers, Route}
import api.LotsApi.{LimitedResultRequest, PostInput}
import db.dao.LotsDao
import entities.LotData
import mappings.JsonMappings
import InputValidator._

import scala.util.{Failure, Success}
import scala.concurrent.duration._

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

trait LotsApi extends BaseApi with JsonMappings with InputValidator with LotsDao with FileHelper {

  import api.ActorSystemImplicits._

  val lotsApi: Route = pathPrefix("lots") {
      path( PathMatchers.JavaUUID / "image" ) { uuid =>
        withRequestTimeout(20.seconds) {
          validate(exists(uuid), lotNotFoundErrorMsg) {
            fileUpload("file") {

              case (metadata, byteSource) =>

                val uploadFuture = byteSource.runWith(S3Uploader.sink(metadata))

                onComplete(uploadFuture) {
                  case Success(uploadResult) => {
                    complete(StatusCodes.OK)
                  }
                  case Failure(_) => complete(StatusCodes.FailedDependency)
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

