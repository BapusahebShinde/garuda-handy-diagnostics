package com.itek.rftaar.reader

enum class DeviceType
/**
 * Instantiates a new Device type.
 *
 * @param newValue the new value
 */(
  /**
   * Get value int.
   *
   * @return the int
   */
  val value: Int
) {
  OTHER(0), ZEBRA(1), CHAINWAY(2), SEUIC(3), ALPS(4), HONEYWELL(5), CIPHERLAB(6);

  companion object {
    /**
     * Get device type.
     *
     * @param value the value
     * @return the device type
     */
    fun get(value: Int): DeviceType {
      return entries[value]
    }
  }
}