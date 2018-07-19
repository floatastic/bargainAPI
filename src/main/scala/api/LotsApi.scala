package api

import scalaz._
import Scalaz._
import akka.http.scaladsl.model.{ContentTypes, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Route, StandardRoute}
import api.InputValidator.ErrorMsgs
import api.LotsApi.{GetInput, PostInput}
import entities.{AuctionData, AuctionId, LotData}
import mappings.JsonMappings

object LotsApi {
  case class PostInput(auctionId: AuctionId, lotData: LotData)
  case class GetInput(auctionId: AuctionId, limit: Option[Int], offset: Option[Int])
}

trait LotsApi extends BaseApi with JsonMappings with InputValidator {

  val lotsApi: Route = pathPrefix("lots") {
    pathEnd {
      post {
        entity(as[PostInput]) { (input: PostInput) =>

          validDataOrBadRequest(input)(validatePostLotsInput) { postData =>
            complete(service.addLot(postData.auctionId, postData.lotData))
          }

        }
      } ~
      parameters('auctionId, 'limit.as[Int].?, 'offset.as[Int].?) { (auctionId: AuctionId, limit, offset) =>

        validDataOrBadRequest(GetInput(auctionId, limit, offset))(validateGetLotsInput) { input =>
          findAuctionOrNotFound(input.auctionId) { auction =>
            complete(service.getLots(auction.id, input.limit, input.offset))
          }
        }

      }
    }
  }
}

