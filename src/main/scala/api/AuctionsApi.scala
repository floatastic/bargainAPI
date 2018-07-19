package api

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Route, StandardRoute}
import api.AuctionsApi.AuctionPostData
import api.InputValidator.ErrorMsgs
import entities.{Auction, AuctionData, AuctionId}
import mappings.JsonMappings
import scalaz._
import Scalaz._

object AuctionsApi {
  case class AuctionPostData(data: AuctionData)
}

trait AuctionsApi extends JsonMappings with ServiceHolder with BaseApi with InputValidator {

  val auctionsApi: Route = pathPrefix("auctions") {
    pathEnd {
      post {
        entity(as[AuctionPostData]) { (auctionData: AuctionPostData) =>

          validatePostAuctionInput(auctionData) match {
            case Success(auctionData) => {
              val auctionId = service.createAuction(auctionData.data)
              complete(auctionId)
            }
            case Failure(errors) => {
              complete(StatusCodes.BadRequest -> ErrorMsgs(errors.toList))
            }
          }

        }
      }
    } ~
      path(Segment) { id =>
        akka.http.scaladsl.server.Directives.get {
          findAuctionOrNotFound(id) { auction =>
            complete(auction)
          }
        }
      }
  }
}
