package com.itek.rftaar.utils

import java.nio.ByteBuffer
import java.security.MessageDigest
import java.util.Locale
import java.util.UUID


object UUIDV5 {
  private val DNS_NAMESPACE: UUID = UUID.fromString("6ba7b810-9dad-11d1-80b4-00c04fd430c8")

  fun generateDeviceId(uniqueId: String): UUID {
    val name = "itek-" + uniqueId.lowercase(Locale.getDefault())
    return generateUUIDv5(DNS_NAMESPACE, name)
  }

  fun generateUUID(uniqueId: String): UUID {
    val name = uniqueId.lowercase(Locale.getDefault())
    return generateUUIDv5(DNS_NAMESPACE, name)
  }

  fun generateUUIDv5(namespace: UUID, name: String): UUID {
    try {
      val sha1 = MessageDigest.getInstance("SHA-1")


      // Convert namespace UUID to bytes
      sha1.update(toBytes(namespace))


      // Add name bytes
      sha1.update(name.toByteArray(charset("UTF-8")))

      val hash = sha1.digest()


      // Use first 16 bytes for UUID
      hash[6] = (hash[6].toInt() and 0x0f).toByte()
      hash[6] = (hash[6].toInt() or 0x50).toByte() // version 5

      hash[8] = (hash[8].toInt() and 0x3f).toByte()
      hash[8] = (hash[8].toInt() or 0x80).toByte() // IETF variant

      return fromBytes(hash)
    } catch (e: Exception) {
      throw RuntimeException("Failed to generate UUIDv5", e)
    }
  }

  private fun toBytes(uuid: UUID): ByteArray {
    val buffer = ByteBuffer.wrap(ByteArray(16))
    buffer.putLong(uuid.mostSignificantBits)
    buffer.putLong(uuid.leastSignificantBits)
    return buffer.array()
  }

  private fun fromBytes(data: ByteArray): UUID {
    val buffer = ByteBuffer.wrap(data)
    val mostSigBits = buffer.getLong()
    val leastSigBits = buffer.getLong()
    return UUID(mostSigBits, leastSigBits)
  }


}