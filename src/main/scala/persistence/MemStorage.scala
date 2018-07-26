package persistence

import entities._
import java.util.UUID

import scalaz._
import Scalaz._
import api.InputValidator
import api.InputValidator.{VNel, _}
import extensions.StringExtensions._

class MemStorage extends AuctionService with InputValidator {
  private var auctions = Seq(
    Auction("4ac772c5-bc52-4d3c-ba9e-4010f511e175".uuid, "First"),
    Auction("0ae024c0-0ddb-4e59-9e2c-721a27c386f6".uuid, "Second"),
    Auction("b9549a8e-be47-4b8b-a38c-226c2647073d".uuid, "Third")
  )

  private var lots = Seq(
    Lot("6b2e9336-7911-4381-a0e7-382a4e510291".uuid, "4ac772c5-bc52-4d3c-ba9e-4010f511e175".uuid, "Lot 1 for auction 1"),
    Lot("826a8ebb-8c9c-45b7-b08e-c784c442f55b".uuid, "4ac772c5-bc52-4d3c-ba9e-4010f511e175".uuid, "Lot 2 for auction 1"),
    Lot("2e5faabf-47eb-40c1-a961-b1ca7e928b49".uuid, "4ac772c5-bc52-4d3c-ba9e-4010f511e175".uuid, "Lot 3 for auction 1")
  )

  override def addLot(auctionId: UUID, data: LotData): VNel[UUID] = {
    newLot(auctionId, data) map { lot =>
      lots :+= lot
      lot.id
    }
  }

  override def getLots(auctionId: UUID, maybeLimit: Option[Int], maybeOffset: Option[Int]): LimitedResult[Lot] = {
    val limit = maybeLimit.getOrElse(10)
    val offset = maybeOffset.getOrElse(0)

    val auctionLots = lots.filter( _.auctionId == auctionId)
    val auctionLotsSlice = auctionLots.slice(offset, offset + limit)
    LimitedResult(auctionLotsSlice, limit, offset, auctionLots.length)
  }

  def getAuction(id: UUID): VNel[Auction] = auctions.find( _.id.equals(id) ).toSuccessNel(auctionNotFoundErrorMsg)

  private def newUUID: UUID = UUID.randomUUID

  private def newLot(auctionId: UUID, data: LotData): VNel[Lot] =
    (getAuction(auctionId) |@| validData(data, lotDataErrorMsg)) { (_, _) =>
      Lot(newUUID, auctionId, data)
    }
}

object MemStorage {

  private val storage = new MemStorage

  def shared = storage
}
