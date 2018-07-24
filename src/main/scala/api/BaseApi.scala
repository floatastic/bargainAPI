package api

import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.StandardRoute
import api.InputValidator.VNel
import entities.{Auction, AuctionId, LotId}
import scalaz._
import Scalaz._
import akka.http.scaladsl.marshalling.{Marshaller, ToResponseMarshaller}
import mappings.JsonMappings

trait BaseApi extends ServiceHolder with JsonMappings {

  implicit def vnelAuctionIdMarshaller: ToResponseMarshaller[VNel[String]] = Marshaller.opaque { result =>
    result match {
      case Success(value) =>
        HttpResponse(entity = value)
      case Failure(errors) => {
        ErrorResponse.validationFailed(errors.toList)
      }
    }
  }

  def findAuctionOrNotFound(id: AuctionId)(auctionTransformer: Auction => StandardRoute): StandardRoute = {
    service.getAuction(id) match {
      case Some(auction) => auctionTransformer(auction)
      case _ => complete(StatusCodes.NotFound)
    }
  }

  def validDataOrErrorResponse[T](data: T)(validator: T => VNel[T])(successCompletion: T => StandardRoute): StandardRoute = {
   validator(data) match {
     case Success(validInput) =>
       successCompletion(validInput)
     case Failure(errors) => {
       complete(ErrorResponse.validationFailed(errors.toList))
     }
   }
  }
}
