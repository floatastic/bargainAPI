package persistence

import api.InputValidator.VNel
import entities._

case class LimitedResult[T](items: Seq[T], limit: Int, offset: Int, total: Int)

trait AuctionService {
  def createAuction(data: AuctionData): VNel[AuctionId]

  def getAuction(id: AuctionId): VNel[Auction]

  def addLot(auctionId: AuctionId, data: LotData): VNel[LotId]

  def getLots(auctionId: AuctionId, limit: Option[Int], offset: Option[Int]): LimitedResult[Lot]
}
