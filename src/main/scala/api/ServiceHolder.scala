package api

import persistence.{AuctionService, MemStorage}

trait ServiceHolder {
  val service: AuctionService = MemStorage.shared
}
