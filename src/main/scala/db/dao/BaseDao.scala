package db.dao

import java.util.UUID
import scala.slick.driver.PostgresDriver.simple._

case class LimitedResult[T](items: Seq[T], limit: Int, offset: Int, total: Int)

trait BaseDao extends db.Database {
  val auctionsTable = TableQuery[AuctionsTable]
  val lotsTable = TableQuery[LotsTable]

  def newUUID = UUID.randomUUID
}
