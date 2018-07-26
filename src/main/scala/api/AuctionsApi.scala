package api

import java.util.UUID

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import api.AuctionsApi.{PostInput}
import entities.{AuctionData}
import db.dao.AuctionsDao
import mappings.JsonMappings

object AuctionsApi {
  case class GetInput(id: UUID)
  case class PostInput(data: AuctionData)
}

trait AuctionsApi extends JsonMappings with ServiceHolder with BaseApi with InputValidator {

  val auctionsApi: Route = pathPrefix("auctions") {
    pathEnd {
      post {
        entity(as[PostInput]) { (input: PostInput) =>
            complete(service.createAuction(input.data))
        }
      }
    } ~
      path(JavaUUID) { id =>
        findAuctionOrNotFound(id) { auction =>
          complete(auction)
        }
      }
  }
}
