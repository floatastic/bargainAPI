package mappings

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import entities._
import persistence.LimitedResult
import spray.json.DefaultJsonProtocol
import api.AuctionsApi.AuctionPostData
import api.InputValidator.ErrorMsgs
import api.LotsApi.PostInput

trait JsonMappings extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val auctionPostDataFormat = jsonFormat1(AuctionPostData)
  implicit val auctionFormat = jsonFormat2(Auction)
  implicit val lotPostDataFormat = jsonFormat2(PostInput)
  implicit val lotFormat = jsonFormat3(Lot)
  implicit val limitedResultFormat = jsonFormat4(LimitedResult[Lot])
  implicit val errorsFormat = jsonFormat1(ErrorMsgs)
}
