package db.dao

import java.util.UUID

import api.InputValidator
import api.InputValidator.{VNel, auctionDataErrorMsg, auctionNotFoundErrorMsg}
import entities.{Auction, AuctionData}
import scalaz._
import Scalaz._

import scala.slick.lifted.Tag
import scala.slick.driver.PostgresDriver.simple._
import scala.util.Try

class AuctionsTable(tag: Tag) extends Table[Auction](tag, "auctions"){
  def id = column[UUID]("id", O.PrimaryKey)
  def data = column[String]("data", O.NotNull)
  def * = (id, data) <> ((Auction.apply _).tupled, Auction.unapply)
}

trait AuctionsDao extends BaseDao with InputValidator {

  def getAuction(id: UUID): VNel[Auction] =
    auctionsTable
      .filter( _.id === id).take(1)
      .run
      .headOption
      .toSuccessNel(auctionNotFoundErrorMsg)

  def createAuction(data: AuctionData): VNel[UUID] = newAuction(data) andThen insertAuction

  def newAuction(data: AuctionData): VNel[Auction] = {
    Apply[VNel].apply(
      validData(data, auctionDataErrorMsg)
    ) {
      _ => Auction(newUUID, data)
    }
  }

  def insertAuction(auction: Auction): VNel[UUID] =
    Try(auctionsTable insert auction).toOption
      .map( _ => auction.id)
      .toSuccessNel(s"Unable to insert auction, id: ${auction.id}")
}
