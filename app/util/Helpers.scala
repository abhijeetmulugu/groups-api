package util

import java.nio.charset.StandardCharsets
import java.util.UUID

object Helpers {

  def createUUID(key: String): UUID = {
    java.util.UUID.nameUUIDFromBytes(key.getBytes(StandardCharsets.UTF_8))
  }
}
