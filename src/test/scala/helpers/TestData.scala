package helpers

import db.dao.{AuctionsDao, LotsDao}
import entities.{Auction, Lot}
import extensions.StringExtensions._
import scala.slick.driver.PostgresDriver.simple._

trait TestData extends LotsDao with AuctionsDao {
  private var auctions = Seq(
    Auction("4ac772c5-bc52-4d3c-ba9e-4010f511e175".asUUID, "First"),
    Auction("0ae024c0-0ddb-4e59-9e2c-721a27c386f6".asUUID, "Second"),
    Auction("b9549a8e-be47-4b8b-a38c-226c2647073d".asUUID, "Third")
  )

  private var lots = Seq(
    Lot("6b2e9336-7911-4381-a0e7-382a4e510291".asUUID, "4ac772c5-bc52-4d3c-ba9e-4010f511e175".asUUID, "Lot 1 for auction 1"),
    Lot("826a8ebb-8c9c-45b7-b08e-c784c442f55b".asUUID, "4ac772c5-bc52-4d3c-ba9e-4010f511e175".asUUID, "Lot 2 for auction 1"),
    Lot("2e5faabf-47eb-40c1-a961-b1ca7e928b49".asUUID, "4ac772c5-bc52-4d3c-ba9e-4010f511e175".asUUID, "Lot 3 for auction 1")
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
