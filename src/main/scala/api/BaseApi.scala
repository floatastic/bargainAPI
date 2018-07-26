package api

import akka.http.scaladsl.marshalling.{Marshaller, Marshalling, ToEntityMarshaller, ToResponseMarshaller}
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.StandardRoute
import api.InputValidator.{ErrorMsg, VNel}
import entities.{Auction, AuctionId, LotId}
import mappings.JsonMappings
import scala.concurrent.Future
import scalaz._
import Scalaz._

trait BaseApi extends ServiceHolder with JsonMappings {

  def errorsListMarshaller: ToResponseMarshaller[NonEmptyList[ErrorMsg]] =
    Marshaller { _ =>
      errors => Future.successful { List(Marshalling.Opaque(() => ErrorResponse.validationFailed(errors.toList))) }
    }

  implicit def vnelResponseMarshaller[A](implicit m: ToEntityMarshaller[A]): ToResponseMarshaller[VNel[A]] = 
    Marshaller { implicit ec =>
      {
        case Success(value) => 
          m(value) map { marshallings =>
            marshallings map (_ map (HttpResponse(OK, Nil, _)))
          }
        case Failure(errors) => errorsListMarshaller(errors)
      }
    }

  def findAuctionOrNotFound(id: AuctionId)(auctionTransformer: Auction => StandardRoute): StandardRoute = {
    service.getAuction(id) match {
      case Success(auction) => auctionTransformer(auction)
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
