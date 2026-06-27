package com.itek.rftaar.reader.epcwrapper.sgtin

import java.lang.Integer.parseInt
import java.math.BigInteger

object iTEKNonStd {

  //i-TEK NS CONSTANTS
  val BASE75_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz!@#$%^&*=?-_+";
  val BASE62_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
  val ITEK_NON_STD_SERIAL_NUMBER_LIMIT = 67108863;
  val ITEK_NON_STD_96_HEADER = "10111011";
  val ITEK_NON_STD_128_HEADER = "10111101";

  fun decode(encoded: String): Map<String, Any>?{
    var errorMessage = ""
    if (encoded.length < 24) {
      errorMessage="INVALID_EPC_MESSAGE";// errorDecodeResult(INVALID_EPC_MESSAGE);
      return null
    }

    val encodedBytes = encoded.decodeHex()//hexToBytes(encoded);
    // Convert EPC bytes to a padded binary string
    val binary = BigInteger(encoded, 16).toString(2)//bytesToBinary(encodedBytes);
    val expectedBits = if(encodedBytes.size === 12) 96 else 128;
    val paddedBinary = padBinary(binary, expectedBits);

    // Extract header and mode bits
    val headerBits = paddedBinary.substring(0, 8);
    val modeBits = paddedBinary.substring(8, 10);
    val modeCode = parseInt(modeBits, 2);

    val mode = Mode.get(modeCode)
    if (mode==null) {
      errorMessage= "INVALID_MODE_MESSAGE";// errorDecodeResult(INVALID_MODE_MESSAGE);
      return null
    }

    var dataBits: String="";
    var serialBits: String="";

    // Case 1: Short data (header 0xBB)
    if (headerBits.equals(ITEK_NON_STD_96_HEADER,true)) {
      dataBits = paddedBinary.substring(10, 70);
      serialBits = paddedBinary.substring(70, 96);
    }
    // Case 2: Long data (header 0xBD)
    else if (headerBits.equals(ITEK_NON_STD_128_HEADER,true)) {
      dataBits = paddedBinary.substring(10, 102);
      serialBits = paddedBinary.substring(102, 128);
    }
    // Case 3: Unrecognized header
    else {
      errorMessage= "UNKNOWN_HEADER_MESSAGE";// errorDecodeResult(UNKNOWN_HEADER_MESSAGE);
      return null
    }

    // Convert binary strings back to numeric values
    val data = BigInteger( dataBits,2);
    val serial = BigInteger( serialBits,2);

    // Convert data back to string based on mode
    var barcode: String=""
    var serialNo: String=""
    if (mode === Mode.NUMERIC) {
      barcode = data.toString();
    } else if (mode === Mode.BASE62) {
      barcode = bigIntToBase62(data);
    } else {
      barcode = bigIntToBase75(data);
    }

    serialNo = serial.toString()
    if (serialNo.length < 8) serialNo=getZeroAppendedBarcode(serialNo,8)

    val m = HashMap<String, Any>();
    m.put("ean", barcode);
    m.put("serial", serialNo);
    m.put("mode", mode);
    return m
  }

  private fun chkNull(str: String?, def: String?): String? {
    return if (isNonEmpty(str)) str!!.trim { it <= ' ' } else def
  }

  private fun isNonEmpty(str: String?): Boolean {
    return str != null && str.trim { it <= ' ' }.length > 0 && !str.trim { it <= ' ' }
      .equals("null", ignoreCase = true)
  }

  private fun isNullOrEmpty(str: String?): Boolean {
    return !isNonEmpty(str)
  }

  private fun getZeroAppendedBarcode(barcode: String, length: Int): String {
    var barcode = barcode
    if (isNullOrEmpty(barcode)) return barcode
    val len = length - barcode.length
    if (len > 0) for (i in 0..<len) barcode = "0" + barcode
    return barcode
  }

  fun encode(barcode: String, serialNumber: String):String{
    var errorMessage = ""
    val serialBigInt = BigInteger(serialNumber);
    // Reject serial numbers exceeding allowed limit
    if (serialBigInt > BigInteger(ITEK_NON_STD_SERIAL_NUMBER_LIMIT.toString())) {
      errorMessage = "SERIAL_NUMBER_LIMIT_MESSAGE"
      return "";
    }

    // Detect encoding mode based on input characters
    val mode = detectMode(barcode)
    // Convert input to bigint depending on mode
    val data = when (mode) {
      Mode.NUMERIC -> BigInteger(barcode);
      Mode.BASE62 -> base62ToBigInt(barcode);
      Mode.BASE75 -> base75ToBigInt(barcode);
      else -> null
    }
    if (data == null) {
      errorMessage="INVALID_INPUT_MESSAGE"
      return ""
    }


    // Calculate bit lengths of data and serial number
    val dataBitLength = getBitLength(data);
    // Case 1: Small payload (≤ 60 bits of data) → 96-bit EPC
    var result=
    if (dataBitLength <= 60) {
      val binary =
      ITEK_NON_STD_96_HEADER + // Header: short data
        padBinary(mode.value.toString(2), 2) +
        padBinary(data.toString(2), 60) +
        padBinary(serialBigInt.toString(2), 26);
      BigInteger(binary, 2).toString(16)
      /*result = binaryToBytes(binary);
      result = padToLength(result, 12);*/ // Pad to 12 bytes (96 bits)
    }
    // Case 2: Larger payload (≤ 92 bits of data) → 128-bit EPC
    else if (dataBitLength <= 92) {
     val binary =
      ITEK_NON_STD_128_HEADER + // Header: long data
        padBinary(mode.value.toString(2), 2) +
        padBinary(data.toString(2), 92) +
        padBinary(serialBigInt.toString(2), 26);
      BigInteger(binary, 2).toString(16)

      /*result = binaryToBytes(binary);
      result = padToLength(result, 16); // Pad to 16 bytes (128 bits)*/
    }
    // Case 3: Too large → reject
    else {
      ""
    }

    if(result.isNullOrEmpty()){
      errorMessage="LARGE_INPUT_DATA_MESSAGE"
      return ""
    }
    else {
      return result.uppercase()
    }
    // Convert final byte array to hex string
  }

  fun getBitLength(value: BigInteger): Int {
    return if(value === BigInteger.ZERO) 1 else value.toString(2).length;
  }

  fun detectMode(barcode: String): Mode {
    if (barcode.matches(Regex("^[0-9]+$"))) return Mode.NUMERIC;
    if (barcode.matches(Regex("^[0-9A-Za-z]+$"))) return Mode.BASE62;
    return Mode.BASE75;
  }

  fun String.decodeHex(): ByteArray {
    check(length % 2 == 0) { "Must have an even length" }

    return chunked(2)
      .map { it.toInt(16).toByte() }
      .toByteArray()
  }

  /** Converts a BASE62 string into a bigint. */
  fun base62ToBigInt(input: String): BigInteger? {
    var result: BigInteger = BigInteger.ZERO;
    for (c in input) {
      val index = BASE62_CHARS.indexOf(c);
      if (index === -1) return null; // invalid input
      result = result * BigInteger.valueOf(62) + BigInteger.valueOf(index.toLong());
    }
    return result;
  }

  /** Converts a bigint into a BASE62 string. */
  fun bigIntToBase62(value1: BigInteger): String {
    if (value1 === BigInteger.ZERO) return BASE62_CHARS.get(0).toString();
    var value=value1
    var result = "";
    while (value > BigInteger("0")) {
      val remainder = value % BigInteger.valueOf(62);
      result += BASE62_CHARS.get(remainder.toInt());
      value /= BigInteger.valueOf(62);
    }
    return result.reversed();
  }

  /** Converts a BASE75 string into a bigint. */
  fun base75ToBigInt(input: String): BigInteger?{
    var result: BigInteger = BigInteger.ZERO;
    for (c in input) {
      val index = BASE75_CHARS.indexOf(c);
      if (index === -1) return null; // invalid input
      result = result * BigInteger.valueOf(75) + BigInteger.valueOf(index.toLong());
    }
    return result;
  }

  /** Converts a bigint into a BASE75 string. */
  fun bigIntToBase75(value1: BigInteger): String {
    if (value1 === BigInteger.ZERO) return BASE75_CHARS.get(0).toString();
    var value = value1;
    var result = "";
    while (value > BigInteger.ZERO) {
      val remainder = value % BigInteger.valueOf(75);
      result = BASE75_CHARS.get(remainder.toInt()) + result;
      value /= BigInteger.valueOf(75);
    }
    return result;
  }

  fun hexToBytes(s: String): ByteArray {
    val len = s.length
    val data = ByteArray(len / 2)
    var i = 0
    while (i < len) {
      data[i / 2] = ((s.get(i).digitToIntOrNull(16) ?: -1 shl 4)
      + s.get(i + 1).digitToIntOrNull(16)!! ?: -1).toByte()
      i += 2
    }
    return data
  }


  fun bytesToHex(bytes: ByteArray): String {
    val sb = StringBuilder()
    for (b in bytes) sb.append(String.format("%02X", b))
    return sb.toString()
  }

  fun padBinary(binary: String, length: Int): String {
    return binary.padStart(length, '0');
  }

  /*fun hexStringToBytes(hex: String): UIntArray {
    var bytes = UIntArray(hex.length / 2)
    for (i in 0 until hex.length step 2) {
      bytes[i / 2] = parseInt(hex.slice(i, i + 2), 16).toUInt();
    }
    return bytes;
  }

  fun bytesToHex(bytes: UIntArray): String {
    return bytes.stream().map(byte -> byte.toString(16).padStart(2, "0").toUpperCase()).join("")
  }*/

  fun bytesToBinary(bytes: ByteArray): String {
    return bytes.joinToString("") { b -> b.toInt().and(0xFF).toString(2).padStart(8, '0')}
    //return bytes.map{b-> b.toString(2).padStart(8, '0')}.toString()
  }

}

enum class Mode(val value: Int){
  NUMERIC(0b00), BASE62(0b01),BASE75(0b10);
  companion object {
    /**
     * Get epc non-std mode.
     *
     * @param value the value
     * @return the mode
     */
    fun get(value: Int): Mode? {
      return Mode.entries[value]
    }
  }
}