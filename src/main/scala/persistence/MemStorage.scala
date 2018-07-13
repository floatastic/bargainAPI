package persistence

import entities._
import java.util.UUID

class MemStorage extends AuctionService {
  private var auctions = Seq(
    Auction("4ac772c5-bc52-4d3c-ba9e-4010f511e175", "First"),
    Auction("0ae024c0-0ddb-4e59-9e2c-721a27c386f6", "Second"),
    Auction("b9549a8e-be47-4b8b-a38c-226c2647073d", "Third")
  )

  private var lots = Seq(
    Lot("6b2e9336-7911-4381-a0e7-382a4e510291", "4ac772c5-bc52-4d3c-ba9e-4010f511e175", "Lot for auction 1")
  )

  override def createAuction(data: AuctionData): AuctionId = {
    val newId = UUID.randomUUID.toString
    val auction = Auction(newId, data)
    auctions :+= auction
    newId
  }

  override def getAuction(id: AuctionId): Option[Auction] = auctions.find( _.id == id)

  override def addLot(auctionId: AuctionId, data: LotData): Option[LotId] = {
    getAuction(auctionId) match {
      case Some(auction) => {
        val newId = UUID.randomUUID.toString
        val lot = Lot(newId, auction.id, data)
        lots :+= lot
        Option(newId)
      }
      case _ => None
    }
  }

  override def getLots(auctionId: AuctionId, limit: Int, offset: Int): LimitedResult[Lot] = ???
}

object MemStorage {

  private val storage = new MemStorage

  def shared = storage
}
