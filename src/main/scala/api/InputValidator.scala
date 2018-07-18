package api

import java.util.UUID

import entities.AuctionId
import scalaz._
import Scalaz._
import api.InputValidator.{GetLotsInput, VNel}

import scala.util.Try

object InputValidator {
  type ErrorMsg = String
  type VNel[A] = ValidationNel[ErrorMsg, A]
  type GetLotsInput = (AuctionId, Option[Int], Option[Int])

  case class ErrorMsgs(errors: List[ErrorMsg])
}

trait InputValidator {

  def validUUIDString(uuid: String): Option[String] = Try(UUID.fromString(uuid).toString).toOption

  def validLimit(limit: Option[Int]): Option[Option[Int]] = limit match {
    case None => Some(None)
    case Some(limit) => if (limit > 0) Some(Some(limit)) else None
  }

  def validOffset(offset: Option[Int]) = offset match {
    case None => Some(None)
    case Some(offset) => if (offset >= 0) Some(Some(offset)) else None
  }

  def validateGetLotsInput(auctionId: AuctionId, limit: Option[Int], offset: Option[Int]): VNel[GetLotsInput] = {
    val aId = validUUIDString(auctionId).toSuccessNel("Invalid auction Id")
    val lim = validLimit(limit).toSuccessNel("Invalid limit")
    val off = validOffset(offset).toSuccessNel("Invalid offset")

    Apply[VNel].apply3(aId, lim, off){(aId, lim, off) => (aId, lim, off)}
  }
}
