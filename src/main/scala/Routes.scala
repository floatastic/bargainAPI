import api.{ApiRejectionHandler, AuctionsApi, LotsApi}
import akka.http.scaladsl.server.Directives._

trait Routes extends ApiRejectionHandler with AuctionsApi with LotsApi {
  val routes =
    pathPrefix("v1") {
      auctionsApi ~ lotsApi
    }
}
