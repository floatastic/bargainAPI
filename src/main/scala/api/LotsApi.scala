package api

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import entities.{AuctionData, AuctionId, LotData}
import mappings.{JsonMappings, LotPostData}

trait LotsApi extends JsonMappings with ServiceHolder with AuctionValidator {

  val lotsApi: Route = pathPrefix("lots") {
    pathEnd {
      post {
        entity(as[LotPostData]) { (postData: LotPostData) =>
          service.getAuction(postData.auctionId) match {
            case Some(auction) => {
              val lotId = service.addLot(auction.id, postData.lotData)
              complete(lotId)
            }
            case _ => complete(StatusCodes.custom(StatusCodes.BadRequest.intValue, s"Auction not found (id: ${postData.auctionId})"))
          }
        }
      } ~
      parameters('auctionId, 'limit.as[Int].?, 'offset.as[Int].?) { (auctionId: AuctionId, limit, offset) =>
        findAuctionOrNotFound(auctionId) { auction =>
          complete(service.getLots(auction.id, limit, offset))
        }
      }
    }
  }
}

