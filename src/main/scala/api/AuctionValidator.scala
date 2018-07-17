package api

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.StandardRoute
import entities.{Auction, AuctionId}
import persistence.AuctionService

trait AuctionValidator {

  implicit val service: AuctionService

  def findAuctionOrNotFound(id: AuctionId)(auctionTransformer: Auction => StandardRoute): StandardRoute = {
    service.getAuction(id) match {
      case Some(auction) => auctionTransformer(auction)
      case _ => complete(StatusCodes.NotFound)
    }
  }
}
