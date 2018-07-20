package api

import akka.http.scaladsl.unmarshalling.Unmarshaller
import mappings.JsonMappings
import spray.json._

trait ResponseUnmarshaller extends JsonMappings {

  import api.ContentTypes.`application/vnd.api+json`

  def halJsonResponseUnmarshaller[T: JsonReader] = Unmarshaller.stringUnmarshaller.forContentTypes(`application/vnd.api+json`).map(_.parseJson.convertTo[T])
  implicit def errorResponseMessageUnmarshaller = halJsonResponseUnmarshaller[ApiRejectionHandler.ErrorResponseMessage]
}
