package db.dao

import java.util.UUID

import api.InputValidator
import api.InputValidator.{VNel, auctionNotFoundErrorMsg}

import entities.{Auction}
import scalaz._
import Scalaz._

import scala.slick.lifted.Tag
import scala.slick.driver.PostgresDriver.simple._

class AuctionsTable(tag: Tag) extends Table[Auction](tag, "auctions"){
  def id = column[UUID]("id", O.PrimaryKey)//, O.DBType("UUID"))
  def data = column[String]("data")
  def * = (id, data) <> ((Auction.apply _).tupled, Auction.unapply)
}

trait AuctionsDao extends db.Database with InputValidator {
  private val auctionsTable = TableQuery[AuctionsTable]

  def getAuction(id: UUID): VNel[Auction] =
    auctionsTable
      .filter( _.id === id).take(1)
      .run
      .headOption
      .toSuccessNel(auctionNotFoundErrorMsg)

}
