package api

import java.util.UUID

import scalaz._
import Scalaz._
import api.AuctionsApi.AuctionPostData
import api.LotsApi.{GetInput, PostInput}
import persistence.AuctionService

import scala.util.Try

object InputValidator {
  type ErrorMsg = String
  type VNel[A] = ValidationNel[ErrorMsg, A]

  case class ErrorMsgs(errors: List[ErrorMsg])

  val auctionIdErrorMsg = "Invalid auction Id. Please provide a valid UUID."
  val auctionNotFoundErrorMsg = "Invalid auction Id. Auction does not exist."
  val auctionDataErrorMsg = "Auction data cannot be empty."
  val lotDataErrorMsg = "Lot data cannot be empty."
  val limitErrorMsg = "Limit should be a value between 0 and 100 (right inclusive)."
  val offsetErrorMsg = "Offset should be a value greater than 0."
}

trait InputValidator {

  import InputValidator._

  implicit val service: AuctionService

  def validUUIDString(uuid: String): Option[String] = Try(UUID.fromString(uuid).toString).toOption

  def validLimit(limit: Option[Int]): Option[Option[Int]] = limit match {
    case None => Some(None)
    case Some(limit) => if (limit > 0 && limit <= 100) Some(Some(limit)) else None
  }

  def validOffset(offset: Option[Int]) = offset match {
    case None => Some(None)
    case Some(offset) => if (offset >= 0) Some(Some(offset)) else None
  }

  def validData(data: String): Option[String] = if (data.trim.length > 0) Some(data) else None

  def validateGetLotsInput(input: LotsApi.GetInput): VNel[LotsApi.GetInput] = {
    (
      validUUIDString(input.auctionId).toSuccessNel(auctionIdErrorMsg) |@|
      validLimit(input.limit).toSuccessNel(limitErrorMsg) |@|
      validOffset(input.offset).toSuccessNel(offsetErrorMsg)
    ) {
      (aId, lim, off) => GetInput(aId, lim, off)
    }
  }

  def validatePostLotsInput(lotPostData: LotsApi.PostInput): VNel[LotsApi.PostInput] = {
    (
      validUUIDString(lotPostData.auctionId).toSuccessNel(auctionIdErrorMsg) |@|
      service.getAuction(lotPostData.auctionId).toSuccessNel(auctionNotFoundErrorMsg) |@|
      validData(lotPostData.lotData).toSuccessNel(lotDataErrorMsg)
    ) {
      (auctionId, _, lotData) => PostInput(auctionId, lotData)
    }
  }

  def validatePostAuctionInput(auctionPostData: AuctionPostData): VNel[AuctionPostData] = {
    Apply[VNel].apply(
      validData(auctionPostData.data).toSuccessNel(auctionDataErrorMsg)
    ){
      auctionData => AuctionPostData(auctionData)
    }
  }
}
