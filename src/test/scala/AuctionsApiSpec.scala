import java.util.UUID

import akka.http.scaladsl.model.{ContentTypes, HttpEntity, MediaTypes, StatusCodes}
import akka.http.scaladsl.server.UnsupportedRequestContentTypeRejection
import akka.http.scaladsl.testkit.ScalatestRouteTest
import api.InputValidator.ErrorMsgs
import org.scalatest.{Matchers, WordSpec}
import entities.Auction

import scala.util.Try


class AuctionsApiSpec extends WordSpec with Matchers with ScalatestRouteTest with Routes {
  "Auctions Api" should {

    "return an auction given a valid auction id" in {
      Get("/auctions/4ac772c5-bc52-4d3c-ba9e-4010f511e175") ~> auctionsApi ~> check {
        status shouldEqual StatusCodes.OK
        contentType shouldEqual ContentTypes.`application/json`
        responseAs[Auction] shouldEqual Auction("4ac772c5-bc52-4d3c-ba9e-4010f511e175", "First")
      }
    }

    "return 404 not found given unexisting auction id" in {
      Get("/auctions/55555555-bc52-4d3c-ba9e-4010f511e175") ~> auctionsApi ~> check {
        status shouldEqual StatusCodes.NotFound
      }
    }

    "return 400 with error given wrong format of ID" in {
      Get("/auctions/55555555-bc52") ~> auctionsApi ~> check {
        status shouldEqual StatusCodes.BadRequest

        val errors = responseAs[ErrorMsgs].errors
        errors.length shouldBe 1

        errors(0) shouldEqual "Invalid auction Id. Please provide a valid UUID."
      }
    }

    "return a created object id given valid POST data" in {
      val entity = HttpEntity(MediaTypes.`application/json`,
        "{\"data\": \"Test auction\"}")

      Post("/auctions").withEntity(entity) ~> auctionsApi ~> check {
        status shouldEqual StatusCodes.OK
        Try(UUID.fromString(responseAs[String])) shouldBe 'success
      }
    }

    "reject request given invalid content" in {
      val entity = HttpEntity(MediaTypes.`application/json`,
        "{\"wrongField\": \"Test auction\"}")

      Post("/auctions").withEntity(entity) ~> auctionsApi ~> check {
        rejections.length shouldEqual 1
      }
    }

    "return 400 with error given empty data" in {
      val entity = HttpEntity(MediaTypes.`application/json`,
        "{\"data\": \"   \"}")

      Post("/auctions").withEntity(entity) ~> auctionsApi ~> check {
        status shouldEqual StatusCodes.BadRequest

        val errors = responseAs[ErrorMsgs].errors
        errors.length shouldBe 1

        errors(0) shouldEqual "Auction data cannot be empty."
      }
    }
  }
}
