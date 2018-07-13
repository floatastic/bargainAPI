package mappings

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import entities.Lot
import spray.json.DefaultJsonProtocol

trait JsonMappings extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val lotFormat = jsonFormat1(Lot)

}
