package extensions

import java.util.UUID

object StringExtensions {
  implicit class StringExtensions(val s: String) {
    def uuid = UUID.fromString(s) //TODO: make safe!
  }
}
