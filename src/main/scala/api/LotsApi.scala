package api

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import entities.Lot
import mappings.JsonMappings

trait LotsApi extends JsonMappings {

  val lotsApi: Route = {
    path("lot") {
      get {
        parameter() {
          val mockLots = Seq(
            Lot("First"),
            Lot("Second"),
            Lot("Third")
          )
          complete(mockLots)
        }
      } ~
        post {
          entity(as[Lot]) { (lot: Lot) =>
            // TODO: create Lot
            complete(StatusCodes.Created)
          }
        }
    }
  }
}
