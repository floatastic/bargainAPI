package api

import java.util.UUID

import entities.AuctionId
import scalaz._
import Scalaz._
import api.InputValidator.{ErrorMsg, GetLotsInput, VNel}
import api.LotsApi.LotPostData
import persistence.AuctionService

import scala.util.Try

object InputValidator {
  type ErrorMsg = String
  type VNel[A] = ValidationNel[ErrorMsg, A]
  type GetLotsInput = (AuctionId, Option[Int], Option[Int])

  case class ErrorMsgs(errors: List[ErrorMsg])

  val auctionIdErrorMsg = "Invalid auction Id. Please provide a valid UUID."
  val auctionNotFoundErrorMsg = "Invalid auction Id. Auction does not exist."
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

  def validateGetLotsInput(auctionId: AuctionId, limit: Option[Int], offset: Option[Int]): VNel[GetLotsInput] = {
    val aId = validUUIDString(auctionId).toSuccessNel(auctionIdErrorMsg)
    val lim = validLimit(limit).toSuccessNel(limitErrorMsg)
    val off = validOffset(offset).toSuccessNel(offsetErrorMsg)

    Apply[VNel].apply3(aId, lim, off){(aId, lim, off) => (aId, lim, off)}
  }

  def validatePostLotsInput(lotPostData: LotPostData): VNel[LotPostData] = {
    val auctionId = validUUIDString(lotPostData.auctionId).toSuccessNel(auctionIdErrorMsg)
    val auction = service.getAuction(lotPostData.auctionId).toSuccessNel(auctionNotFoundErrorMsg)
    val lotData = validData(lotPostData.lotData).toSuccessNel(lotDataErrorMsg)

    Apply[VNel].apply3(auctionId, auction, lotData){(auctionId, _, lotData) => LotPostData(auctionId, lotData)}
  }
}
