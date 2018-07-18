package api

import scalaz._
import Scalaz._
import akka.http.scaladsl.model.{ContentTypes, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Route, StandardRoute}
import api.InputValidator.ErrorMsgs
import api.LotsApi.LotPostData
import entities.{AuctionData, AuctionId, LotData}
import mappings.JsonMappings

object LotsApi {
  case class LotPostData(auctionId: AuctionId, lotData: LotData)
}

trait LotsApi extends JsonMappings with ServiceHolder with AuctionValidator with InputValidator {

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

        validateGetLotsInput(auctionId, limit, offset) match {
          case Success((auctionId, limit, offset)) =>
            findAuctionOrNotFound(auctionId) { auction =>
              complete(service.getLots(auction.id, limit, offset))
            }
          case Failure(errors) => {
            complete(StatusCodes.BadRequest -> ErrorMsgs(errors.toList))
          }
        }

      }
    }
  }
}

