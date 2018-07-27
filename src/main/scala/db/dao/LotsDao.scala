package db.dao

import java.util.UUID

import api.InputValidator
import api.InputValidator.{VNel, lotDataErrorMsg}
import entities.{Lot, LotData}
import scalaz._
import Scalaz._

import scala.slick.driver.PostgresDriver.simple._
import scala.slick.lifted.Tag
import scala.util.Try

class LotsTable(tag: Tag) extends Table[Lot](tag, "lots"){
  def id = column[UUID]("id", O.PrimaryKey)
  def auctionId = column[UUID]("auction_id", O.NotNull)
  def data = column[String]("data", O.NotNull)
  def * = (id, auctionId, data) <> ((Lot.apply _).tupled, Lot.unapply)

  def auction = foreignKey("auction_fk", id, TableQuery[AuctionsTable])(_.id, onDelete=ForeignKeyAction.Cascade)
}

trait LotsDao extends BaseDao with InputValidator {

  def addLot(auctionId: UUID, data: LotData): VNel[UUID] = newLot(auctionId, data) andThen insertLot

  private def newLot(auctionId: UUID, data: LotData): VNel[Lot] =
    Apply[VNel].apply(
      validData(data, lotDataErrorMsg)
    ) {
        (_) => Lot(newUUID, auctionId, data)
    }

  //TODO: Catch and forward db error? Should we use the same pattern for inserting auction?
  private def insertLot(lot: Lot): VNel[UUID] = Try(lotsTable insert lot).toOption.map( _ => lot.id).toSuccessNel(s"Unable to insert lot, id: ${lot.id}")
}
