import api.{ApiErrorHandler, LotsApi}
import akka.http.scaladsl.server.Directives._

trait Routes extends ApiErrorHandler with LotsApi {
  val routes =
    pathPrefix("v1") {
      lotsApi
    }
}
