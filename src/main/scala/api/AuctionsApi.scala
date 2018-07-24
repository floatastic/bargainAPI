package api

import java.util.UUID

import akka.http.scaladsl.marshalling.{Marshaller, ToResponseMarshaller}
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import api.AuctionsApi.{GetInput, PostInput}
import api.InputValidator.VNel
import entities.{AuctionData, AuctionId}
import mappings.JsonMappings
import scalaz._
import Scalaz._

import scala.util.Try

object AuctionsApi {
  case class GetInput(id: AuctionId) {
    require(Try(UUID.fromString(id).toString).isSuccess, "Invalid auction Id. Auction Id must have a UUID format.")
  }
  case class PostInput(data: AuctionData)
}

trait AuctionsApi extends JsonMappings with ServiceHolder with BaseApi with InputValidator {

  implicit def vnelAuctionIdMarshaller: ToResponseMarshaller[VNel[AuctionId]] = Marshaller.opaque { result =>
    result match {
      case Success(value) =>
        HttpResponse(entity = value)
      case Failure(errors) => {
        ErrorResponse.validationFailed(errors.toList)
      }
    }
  }

  val auctionsApi: Route = pathPrefix("auctions") {
    pathEnd {
      post {
        entity(as[PostInput]) { (input: PostInput) =>
            complete(service.createAuction(input.data))
        }
      }
    } ~
      path(Segment).as(GetInput) { input =>
        findAuctionOrNotFound(input.id) { auction =>
          complete(auction)
        }
      }
  }
}
