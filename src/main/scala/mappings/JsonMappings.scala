package mappings

import java.util.UUID

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import entities._
import persistence.LimitedResult
import spray.json.{DefaultJsonProtocol, JsString, DeserializationException, JsValue, JsonFormat}
import api.AuctionsApi
import api.ErrorResponse.{ErrorMessage, ErrorResponseMessage}
import api.LotsApi

import scala.util.Try

trait JsonMappings extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val uuidFormat = new JsonFormat[UUID] {
    override def write(obj: UUID): JsValue = JsString(obj.toString)

    override def read(json: JsValue): UUID = json match {
      case JsString(uuid) => Try(UUID.fromString(uuid)).getOrElse(throw new DeserializationException("Expected UUID format."))
      case _              => throw new DeserializationException("Expected UUID format.")
    }
  }

  implicit val auctionPostDataFormat = jsonFormat1(AuctionsApi.PostInput)
  implicit val auctionFormat = jsonFormat2(Auction)
  implicit val lotPostDataFormat = jsonFormat2(LotsApi.PostInput)
  implicit val lotFormat = jsonFormat3(Lot)
  implicit val limitedResultFormat = jsonFormat4(LimitedResult[Lot])
  implicit val errorMessageFormat = jsonFormat1(ErrorMessage)
  implicit val errorResponseMessageFormat = jsonFormat2(ErrorResponseMessage)
}
