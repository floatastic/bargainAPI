package extensions

import java.util.UUID

object Int2UUIDExtension {
  implicit class Int2UUIDExtension(val i: Int) {
    def asUUID = new UUID(0, i.toLong)
  }
}
