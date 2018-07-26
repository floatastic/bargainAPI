package api

import java.util.UUID

import akka.http.javadsl.unmarshalling.StringUnmarshaller
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.unmarshalling.Unmarshaller
import api.LotsApi.{LimitedResultRequest, PostInput}
import entities.LotData
import mappings.JsonMappings

import scala.util.{Success, Try}

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

trait LotsApi extends BaseApi with JsonMappings with InputValidator with ServiceHolder {

  val lotsApi: Route = pathPrefix("lots") {
    pathEnd {
      post {
        entity(as[PostInput]) { (input: PostInput) =>
          complete(service.addLot(input.auctionId, input.lotData))
        }
      } ~
      parameters('auctionId.as[UUID], 'limit.as[Int].?, 'offset.as[Int].?).as(LimitedResultRequest[UUID]) { input =>

        findAuctionOrNotFound(input.resourceId) { auction =>
          complete(service.getLots(auction.id, input.limit, input.offset))
        }

      }
    }
  }
}

