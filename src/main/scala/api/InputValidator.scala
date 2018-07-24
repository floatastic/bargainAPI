package api

import java.util.UUID

import scalaz._
import Scalaz._
import api.LotsApi.{LimitedResultRequest}
import entities.AuctionId

import scala.util.Try

object InputValidator {
  type ErrorMsg = String
  type VNel[A] = ValidationNel[ErrorMsg, A]

  val auctionIdErrorMsg = "Invalid auction Id. Please provide a valid UUID."
  val auctionNotFoundErrorMsg = "Invalid auction Id. Auction does not exist."
  val auctionDataErrorMsg = "Auction data cannot be empty."
  val lotDataErrorMsg = "Lot data cannot be empty."
  val limitErrorMsg = "Limit should be a value between 0 and 100 (right inclusive)."
  val offsetErrorMsg = "Offset should be a value greater than 0."

  val defaultUUIDErrorMsg = "Invalid UUID string."
  val defaultDataErrorMsg = "Data cannot be empty."
}

trait InputValidator {

  import InputValidator._

  def uuid(stringUUID: String, errorMessage: String = defaultUUIDErrorMsg): VNel[UUID] = Try(UUID.fromString(stringUUID)).toOption.toSuccessNel(errorMessage)

  def validData(data: String, errorMessage: String = defaultDataErrorMsg): VNel[String] = {
    val maybeData = if (data.trim.length > 0) Some(data) else None
    maybeData.toSuccessNel(errorMessage)
  }

  def validateGetLotsInput(input: LotsApi.LimitedResultRequest[AuctionId]): VNel[LimitedResultRequest[AuctionId]] = {
    Apply[VNel].apply(
      uuid(input.resourceId, auctionIdErrorMsg)
    ) {
      _ => input
    }
  }
}
