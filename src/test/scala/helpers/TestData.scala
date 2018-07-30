package helpers

import db.dao.{AuctionsDao, LotsDao}
import entities.{Auction, Lot}
import extensions.Int2UUIDExtension._
import scala.slick.driver.PostgresDriver.simple._

trait TestData extends LotsDao with AuctionsDao {
  private var auctions = Seq(
    Auction(1.asUUID, "First"),
    Auction(2.asUUID, "Second"),
    Auction(3.asUUID, "Third")
  )

  private var lots = Seq(
    Lot(1.asUUID, 1.asUUID, "Lot 1 for auction 1"),
    Lot(2.asUUID, 1.asUUID, "Lot 2 for auction 1"),
    Lot(3.asUUID, 1.asUUID, "Lot 3 for auction 1")
  )

  def insertTestData: Unit = {
    (auctionsTable ++= auctions).run
    (lotsTable ++= lots).run
  }

  def dropAllData: Unit = {
    lotsTable.delete.run
    auctionsTable.delete.run
  }

  def ensureTransaction(isSet: Boolean): Unit = {
    session.conn.setAutoCommit(!isSet)
  }

  def rollback: Unit = {
    session.conn.rollback()
  }
}
