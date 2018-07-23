package api

import akka.http.scaladsl.model._
import akka.http.scaladsl.server._
import Directives._
import mappings.JsonMappings

trait ApiRejectionHandler extends JsonMappings {

  implicit def rejectionHandler = RejectionHandler.newBuilder()
    .handle { case ValidationRejection(msg, _) =>
      complete {
        ErrorResponse.validationFailed(List(msg))
      }
    }
    .result()

}
