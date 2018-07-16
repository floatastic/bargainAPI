package api

import persistence.MemStorage

trait PersistanceHolder {
  val persistence = MemStorage.shared
}
