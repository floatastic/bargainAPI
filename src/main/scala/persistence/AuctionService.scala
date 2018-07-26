package persistence

import java.util.UUID

import api.InputValidator.VNel
import entities._

case class LimitedResult[T](items: Seq[T], limit: Int, offset: Int, total: Int)

trait AuctionService {
  def createAuction(data: AuctionData): VNel[UUID]

  def getAuction(id: UUID): VNel[Auction]

  def addLot(auctionId: UUID, data: LotData): VNel[UUID]

  def getLots(auctionId: UUID, limit: Option[Int], offset: Option[Int]): LimitedResult[Lot]
}
