package api

import persistence.{AuctionService, MemStorage}

trait ServiceHolder {
  implicit val service: AuctionService = MemStorage.shared
}
