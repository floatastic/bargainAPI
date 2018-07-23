package api

import akka.http.scaladsl.unmarshalling.Unmarshaller
import api.ErrorResponse.ErrorResponseMessage
import mappings.JsonMappings
import spray.json._

trait ResponseUnmarshaller extends JsonMappings {

  import api.ContentTypes.`application/vnd.error+json`

  def errorJsonResponseUnmarshaller[T: JsonReader] =
    Unmarshaller.stringUnmarshaller
      .forContentTypes(`application/vnd.error+json`)
      .map(_.parseJson.convertTo[T])

  implicit def errorResponseMessageUnmarshaller = errorJsonResponseUnmarshaller[ErrorResponseMessage]
}
