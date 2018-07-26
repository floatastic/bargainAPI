package entities

import java.util.UUID

case class Auction(id: UUID, data: AuctionData)
case class Lot(id: UUID, auctionId: UUID, data: LotData)
