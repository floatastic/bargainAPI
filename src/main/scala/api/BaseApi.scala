package api

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.StandardRoute
import api.InputValidator.{ErrorMsgs, VNel}
import entities.{Auction, AuctionId}
import scalaz._
import Scalaz._
import mappings.JsonMappings

trait BaseApi extends ServiceHolder with JsonMappings {

  def findAuctionOrNotFound(id: AuctionId)(auctionTransformer: Auction => StandardRoute): StandardRoute = {
    service.getAuction(id) match {
      case Some(auction) => auctionTransformer(auction)
      case _ => complete(StatusCodes.NotFound)
    }
  }

  def validDataOrBadRequest[T](data: T)(validator: T => VNel[T])(successCompletion: T => StandardRoute): StandardRoute = {
   validator(data) match {
     case Success(validInput) =>
       successCompletion(validInput)
     case Failure(errors) => {
       complete(StatusCodes.BadRequest -> ErrorMsgs(errors.toList))
     }
   }
  }
}
