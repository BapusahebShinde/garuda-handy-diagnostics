package com.itek.rftaar.reader.epcwrapper.constants

enum class EncodeAlgorithmStd
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
  OTHER(0), SGTIN_30(1), GID_35(2);
  companion object {
    /**
     * Get enc std algorithm.
     *
     * @param value the value
     * @return the device type
     */
    fun get(value: Int): EncodeAlgorithmStd {
      return EncodeAlgorithmStd.entries[value]
    }
  }
}