package mappings

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import entities._
import persistence.LimitedResult
import spray.json.DefaultJsonProtocol

case class AuctionPostData(data: AuctionData)
case class LotPostData(auctionId: AuctionId, lotData: LotData)

trait JsonMappings extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val auctionPostDataFormat = jsonFormat1(AuctionPostData)
  implicit val auctionFormat = jsonFormat2(Auction)
  implicit val lotPostDataFormat = jsonFormat2(LotPostData)
  implicit val lotFormat = jsonFormat3(Lot)
  implicit val limitedResult = jsonFormat4(LimitedResult[Lot])
}
