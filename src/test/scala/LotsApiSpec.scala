import java.util.UUID

import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import api.InputValidator.ErrorMsgs
import entities.Lot
import org.scalatest.{Matchers, WordSpec}
import persistence.LimitedResult

import scala.util.Try

class LotsApiSpec extends WordSpec with Matchers with ScalatestRouteTest with Routes {

  implicit val limResLotUm = jsonFormat4(LimitedResult[Lot])

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

    "return a result with default limit and offset given a valid auction id" in {
      Get("/lots?auctionId=4ac772c5-bc52-4d3c-ba9e-4010f511e175") ~> lotsApi ~> check {
        status shouldEqual StatusCodes.OK
        contentType shouldEqual ContentTypes.`application/json`

        val limitedResult = entityAs[LimitedResult[Lot]]

        limitedResult.limit shouldEqual 10
        limitedResult.offset shouldEqual 0

        limitedResult.total shouldEqual 3
        limitedResult.items.length shouldEqual 3
      }
    }

    "return a result with limit and offset given in a request" in {
      Get("/lots?auctionId=4ac772c5-bc52-4d3c-ba9e-4010f511e175&limit=1&offset=2") ~> lotsApi ~> check {
        status shouldEqual StatusCodes.OK
        contentType shouldEqual ContentTypes.`application/json`

        val limitedResult = entityAs[LimitedResult[Lot]]

        limitedResult.limit shouldEqual 1
        limitedResult.offset shouldEqual 2

        limitedResult.total shouldEqual 3
        limitedResult.items.length shouldEqual 1

        limitedResult.items.head.id shouldEqual "2e5faabf-47eb-40c1-a961-b1ca7e928b49"
      }
    }

    "return 404 not found given unexisting auction id" in {
      Get("/lots?auctionId=4ac772c5-bc52-5555-ba9e-4010f511e175") ~> lotsApi ~> check {
        status shouldEqual StatusCodes.NotFound
      }
    }

    "return a 400 with errors given bad format of UUID or wrong limit or offset values" in {
      Get("/lots?auctionId=4ac772c5-bc52-4d3c&limit=200&offset=-1") ~> lotsApi ~> check {
        status shouldEqual StatusCodes.BadRequest

        val errors = responseAs[ErrorMsgs].errors
        errors.length shouldBe 3

        errors(0) shouldEqual "Invalid auction Id. Please provide a valid UUID."
        errors(1) shouldEqual "Limit should be a value between 0 and 100 (right inclusive)."
        errors(2) shouldEqual "Offset should be a value greater than 0."
      }
    }

    "return 400 with errors given bad format of UUID and empty data" in {
      val entity = HttpEntity(MediaTypes.`application/json`,
        "{\"auctionId\": \"721a27c386f6\", \"lotData\": \"    \"}")

      Post("/lots").withEntity(entity) ~> lotsApi ~> check {
        status shouldEqual StatusCodes.BadRequest

        val errors = responseAs[ErrorMsgs].errors
        errors.length shouldBe 3

        errors(0) shouldEqual "Invalid auction Id. Please provide a valid UUID."
        errors(1) shouldEqual "Invalid auction Id. Auction does not exist."
        errors(2) shouldEqual "Lot data cannot be empty."
      }
    }
  }
}
