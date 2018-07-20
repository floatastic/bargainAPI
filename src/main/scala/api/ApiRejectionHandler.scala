package api

import akka.http.scaladsl.model._
import akka.http.scaladsl.server._
import StatusCodes._
import Directives._
import api.ApiRejectionHandler.{ErrorMessage, ErrorResponseMessage}
import mappings.JsonMappings

object ApiRejectionHandler {
  case class ErrorMessage(message: String)
  case class ErrorResponseMessage(message: String, _embedded: List[ErrorMessage])
}

trait ApiRejectionHandler extends JsonMappings {

  implicit def rejectionHandler = RejectionHandler.newBuilder()
    .handle { case ValidationRejection(msg, _) =>
      complete {
        val message = ErrorResponseMessage("Validation failed", List(ErrorMessage(msg)))
        val entity = HttpEntity(ContentTypes.`application/vnd.api+json` , errorResponseMessageFormat.write(message).toString)
        HttpResponse(BadRequest, headers = Nil, entity)
      }
    }
    .result()

}
