package mappings

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import entities.Auction
import spray.json.DefaultJsonProtocol

trait JsonMappings extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val auctionFormat = jsonFormat2(Auction)

}
