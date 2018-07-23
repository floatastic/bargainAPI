package api

import akka.http.scaladsl.model.{HttpEntity, HttpResponse}
import akka.http.scaladsl.model.StatusCodes.BadRequest
import mappings.JsonMappings

object ErrorResponse extends JsonMappings {

  case class ErrorMessage(message: String)
  case class ErrorResponseMessage(message: String, _embedded: List[ErrorMessage])

  def validationFailed(messages: List[String]): HttpResponse = {
    val responseMessage = ErrorResponseMessage("Validation failed", messages.map(ErrorMessage))
    val entity = HttpEntity(ContentTypes.`application/vnd.error+json` , errorResponseMessageFormat.write(responseMessage).toString)
    HttpResponse(BadRequest, headers = Nil, entity)
  }
}
