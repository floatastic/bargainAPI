package api

import java.util.UUID

import akka.http.scaladsl.marshalling.{Marshaller, Marshalling, ToEntityMarshaller, ToResponseMarshaller}
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.StandardRoute
import api.InputValidator.{ErrorMsg, VNel}
import entities.Auction
import mappings.JsonMappings

import scala.concurrent.Future
import scalaz._
import Scalaz._
import akka.http.scaladsl.unmarshalling.Unmarshaller
import spray.json.{DeserializationException}

import scala.util.Try
import db.dao.AuctionsDao

trait BaseApi extends JsonMappings with AuctionsDao {

  implicit def uuidToEntityMarshaller: ToEntityMarshaller[UUID] = Marshaller.stringMarshaller(MediaTypes.`application/json`).compose( _.toString )

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

  implicit val uuidFromStringUnmarshaller: Unmarshaller[String, UUID] =
    Unmarshaller.strict[String, UUID](stringFromUUID)

  private def stringFromUUID(s: String): UUID = Try(UUID.fromString(s)) match {
    case scala.util.Success(uuid) => uuid
    case _ => throw new DeserializationException(s"'$s' is not a valid UUID")
  }

  def findAuctionOrNotFound(id: UUID)(auctionTransformer: Auction => StandardRoute): StandardRoute = {
    getAuction(id) match {
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
