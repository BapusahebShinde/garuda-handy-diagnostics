package com.itek.rftaar.reader.epcwrapper.constants

enum class EncodeAlgorithmNonStd
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
  OTHER(0), BC_ALPHA_NUM(1), BB_BD_ITEK_NONSTD(2);

  companion object {
    /**
     * Get enc non-std algorithm.
     *
     * @param value the value
     * @return the device type
     */
    fun get(value: Int): EncodeAlgorithmNonStd {
      return EncodeAlgorithmNonStd.entries[value]
    }
  }
}