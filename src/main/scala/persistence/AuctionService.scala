package persistence

import entities._

case class LimitedResult[T](items: Seq[T], limit: Int, offset: Int, total: Int)

trait AuctionService {
  def createAuction(data: AuctionData): AuctionId

  def getAuction(id: AuctionId): Option[Auction]

  def addLot(auctionId: AuctionId, data: LotData): Option[LotId]

  def getLots(auctionId: AuctionId, limit: Int, offset: Int): LimitedResult[Lot]
}
