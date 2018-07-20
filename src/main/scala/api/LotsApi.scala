package api

import java.util.UUID

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import api.LotsApi.{GetInput, PostInput}
import entities.{AuctionId, LotData}
import mappings.JsonMappings

import scala.util.Try

object LotsApi {
  case class PostInput(auctionId: AuctionId, lotData: LotData)

  case class GetInput(auctionId: AuctionId, limit: Option[Int], offset: Option[Int]) {
    require(
      Try(UUID.fromString(auctionId).toString).isSuccess,
      "Invalid auction Id. Auction Id must have a UUID format."
    )
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

          validDataOrBadRequest(input)(validatePostLotsInput) { postData =>
            complete(service.addLot(postData.auctionId, postData.lotData))
          }

        }
      } ~
      parameters('auctionId, 'limit.as[Int].?, 'offset.as[Int].?).as(GetInput) { input =>

        validDataOrBadRequest(input)(validateGetLotsInput) { input =>
          findAuctionOrNotFound(input.auctionId) { auction =>
            complete(service.getLots(auction.id, input.limit, input.offset))
          }
        }

      }
    }
  }
}

