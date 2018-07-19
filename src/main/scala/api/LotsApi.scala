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

trait LotsApi extends BaseApi with JsonMappings with InputValidator {

  val lotsApi: Route = pathPrefix("lots") {
    pathEnd {
      post {
        entity(as[LotPostData]) { (postData: LotPostData) =>

          validatePostLotsInput(postData) match {
            case Success(postData) => {
              val lotId = service.addLot(postData.auctionId, postData.lotData)
              complete(lotId)
            }
            case Failure(errors) => {
              complete(StatusCodes.BadRequest -> ErrorMsgs(errors.toList))
            }
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

