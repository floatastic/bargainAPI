package api

import scalaz._
import Scalaz._

object InputValidator {
  type ErrorMsg = String
  type VNel[A] = ValidationNel[ErrorMsg, A]

  val auctionIdErrorMsg = "Invalid auction Id. Please provide a valid UUID."
  val auctionNotFoundErrorMsg = "Invalid auction Id. Auction does not exist."
  val auctionDataErrorMsg = "Auction data cannot be empty."
  val lotNotFoundErrorMsg = "Invalid lot Id. Lot does not exist."
  val lotDataErrorMsg = "Lot data cannot be empty."
  val limitErrorMsg = "Limit should be a value between 0 and 100 (right inclusive)."
  val offsetErrorMsg = "Offset should be a value greater than 0."

  val defaultUUIDErrorMsg = "Invalid UUID string."
  val defaultDataErrorMsg = "Data cannot be empty."
}

trait InputValidator {

  import InputValidator._

  def validData(data: String, errorMessage: String = defaultDataErrorMsg): VNel[String] = {
    val maybeData = if (data.trim.length > 0) Some(data) else None
    maybeData.toSuccessNel(errorMessage)
  }
}
