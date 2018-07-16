import api.{ApiErrorHandler, AuctionsApi, LotsApi}
import akka.http.scaladsl.server.Directives._

trait Routes extends ApiErrorHandler with AuctionsApi with LotsApi {
  val routes =
    pathPrefix("v1") {
      auctionsApi ~ lotsApi
    }
}
