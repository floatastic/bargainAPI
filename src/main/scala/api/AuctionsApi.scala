package api

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import entities.AuctionData
import mappings.{AuctionPostData, JsonMappings}

trait AuctionsApi extends JsonMappings with ServiceHolder {

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
          service.getAuction(id) match {
            case Some(auction) => complete(auction)
            case _ => complete(StatusCodes.NotFound)
          }
        }
      }
  }
}
