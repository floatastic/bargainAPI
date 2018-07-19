package api

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import api.AuctionsApi.{GetInput, PostInput}
import entities.{AuctionData, AuctionId}
import mappings.JsonMappings

object AuctionsApi {
  case class GetInput(id: AuctionId)
  case class PostInput(data: AuctionData)
}

trait AuctionsApi extends JsonMappings with ServiceHolder with BaseApi with InputValidator {

  val auctionsApi: Route = pathPrefix("auctions") {
    pathEnd {
      post {
        entity(as[PostInput]) { (input: PostInput) =>

          validDataOrBadRequest(input)(validatePostAuctionInput) { input =>
            complete(service.createAuction(input.data))
          }

        }
      }
    } ~
      path(Segment) { id: AuctionId =>

        validDataOrBadRequest(GetInput(id))(validateGetAuctionInput) { input =>
          findAuctionOrNotFound(input.id) { auction =>
            complete(auction)
          }
        }
      }
  }
}
