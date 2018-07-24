package api

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import api.LotsApi.{LimitedResultRequest, PostInput}
import entities.{AuctionId, LotData}
import mappings.JsonMappings

object LotsApi {
  case class PostInput(auctionId: AuctionId, lotData: LotData)

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

trait LotsApi extends BaseApi with JsonMappings with InputValidator {

  val lotsApi: Route = pathPrefix("lots") {
    pathEnd {
      post {
        entity(as[PostInput]) { (input: PostInput) =>
          complete(service.addLot(input.auctionId, input.lotData))
        }
      } ~
      parameters('auctionId, 'limit.as[Int].?, 'offset.as[Int].?).as(LimitedResultRequest[AuctionId]) { input =>

        validDataOrErrorResponse(input)(validateGetLotsInput) { input =>
          findAuctionOrNotFound(input.resourceId) { auction =>
            complete(service.getLots(auction.id, input.limit, input.offset))
          }
        }

      }
    }
  }
}

