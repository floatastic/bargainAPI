import api.{ApiRejectionHandler, AuctionsApi, LotsApi, TestUploadApi}
import akka.http.scaladsl.server.Directives._

trait Routes extends ApiRejectionHandler with AuctionsApi with LotsApi with TestUploadApi {
  val routes =
    pathPrefix("v1") {
      auctionsApi ~ lotsApi ~ testUploadApi
    }
}
