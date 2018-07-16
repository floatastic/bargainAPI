import java.util.UUID

import akka.http.scaladsl.model.{ContentTypes, HttpEntity, MediaTypes, StatusCodes}
import akka.http.scaladsl.server.UnsupportedRequestContentTypeRejection
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{Matchers, WordSpec}

import scala.util.Try

class LotsApiSpec extends WordSpec with Matchers with ScalatestRouteTest with Routes {
  "Lots Api" should {

    "return a created object id given valid POST data" in {

      val entity = HttpEntity(MediaTypes.`application/json`,
        "{\"auctionId\": \"0ae024c0-0ddb-4e59-9e2c-721a27c386f6\", \"lotData\": \"Test lot\"}")

      Post("/lots").withEntity(entity) ~> lotsApi ~> check {
        status shouldEqual StatusCodes.OK
        Try(UUID.fromString(responseAs[String])) shouldBe 'success
      }
    }

    "reject request given invalid content" in {
      val entity = HttpEntity(MediaTypes.`application/json`,
        "{\"auctionId\": \"0ae024c0-0ddb-4e59-9e2c-721a27c386f6\", \"wrongLotData\": \"Test lot\"}")

      Post("/lots").withEntity(entity) ~> lotsApi ~> check {
        rejections.length shouldEqual 2
      }
    }

    "return 400 bad request given unexisting auction id" in {
      val entity = HttpEntity(MediaTypes.`application/json`,
        "{\"auctionId\": \"0ae024c0-0ddb-4e59-5555-721a27c386f6\", \"lotData\": \"Test lot\"}")

      Post("/lots").withEntity(entity) ~> lotsApi ~> check {
        status shouldEqual StatusCodes.BadRequest
      }
    }
  }
}
