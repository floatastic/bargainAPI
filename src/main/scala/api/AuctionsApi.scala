package api

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Route, StandardRoute}
import api.AuctionsApi.AuctionPostData
import entities.{Auction, AuctionData, AuctionId}
import mappings.JsonMappings

object AuctionsApi {
  case class AuctionPostData(data: AuctionData)
}

trait AuctionsApi extends JsonMappings with ServiceHolder with AuctionValidator {

  val auctionsApi: Route = pathPrefix("auctions") {
    pathEnd {
      post {
        entity(as[AuctionPostData]) { (auctionData: AuctionPostData) =>
          val auctionId = service.createAuction(auctionData.data)
          complete(auctionId)
        }
      }
    } ~
      path(Segment) { id =>
        get {
          findAuctionOrNotFound(id) { auction =>
            complete(auction)
          }
        }
      }
  }
}
