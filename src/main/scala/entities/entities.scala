package entities

case class Auction(id: AuctionId, data: AuctionData)
case class Lot(id: LotId, auctionId: AuctionId, data: LotData)