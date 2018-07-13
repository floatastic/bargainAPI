import api.{ApiErrorHandler, AuctionsApi}
import akka.http.scaladsl.server.Directives._

trait Routes extends ApiErrorHandler with AuctionsApi {
  val routes =
    pathPrefix("v1") {
      auctionsApi
    }
}
