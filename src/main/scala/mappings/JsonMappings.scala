package mappings

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import api.ApiRejectionHandler.{ErrorMessage, ErrorResponseMessage}
import entities._
import persistence.LimitedResult
import spray.json.DefaultJsonProtocol
import api.AuctionsApi
import api.InputValidator.ErrorMsgs
import api.LotsApi

trait JsonMappings extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val auctionPostDataFormat = jsonFormat1(AuctionsApi.PostInput)
  implicit val auctionFormat = jsonFormat2(Auction)
  implicit val lotPostDataFormat = jsonFormat2(LotsApi.PostInput)
  implicit val lotFormat = jsonFormat3(Lot)
  implicit val limitedResultFormat = jsonFormat4(LimitedResult[Lot])
  implicit val errorsFormat = jsonFormat1(ErrorMsgs)
  implicit val errorMessageFormat = jsonFormat1(ErrorMessage)
  implicit val errorResponseMessageFormat = jsonFormat2(ErrorResponseMessage)
}
