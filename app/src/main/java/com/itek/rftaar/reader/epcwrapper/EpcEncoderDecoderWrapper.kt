package com.itek.rftaar.reader.epcwrapper

import com.itek.rftaar.api.constants.ParameterConstants
import com.itek.rftaar.core.common.utils.LogUtils
import com.itek.rftaar.core.database.DataStoreManager
import com.itek.rftaar.reader.epcwrapper.constants.BarcodeConstants
import com.itek.rftaar.reader.epcwrapper.constants.EncodeAlgorithmNonStd
import com.itek.rftaar.reader.epcwrapper.constants.EncodeAlgorithmStd
import com.itek.rftaar.reader.epcwrapper.sgtin.SGTIN128Helper
import com.itek.rftaar.reader.epcwrapper.sgtin.SGTIN96
import com.itek.rftaar.reader.epcwrapper.sgtin.iTEKGID96
import com.itek.rftaar.reader.epcwrapper.sgtin.iTEKNonStd
import com.itek.rftaar.utils.CommonUtils.chkNull
import com.itek.rftaar.utils.CommonUtils.isNullOrEmpty
import java.util.Locale

object EpcEncoderDecoderWrapper {
  private var encodeAlgorithmStd: EncodeAlgorithmStd = EncodeAlgorithmStd.OTHER
  private var encodeAlgorithmNonStd: EncodeAlgorithmNonStd = EncodeAlgorithmNonStd.OTHER

  fun isGID():Boolean{
    return encodeAlgorithmStd == EncodeAlgorithmStd.OTHER || encodeAlgorithmStd==EncodeAlgorithmStd.GID_35
  }

  fun isSGTIN():Boolean{
    return encodeAlgorithmStd==EncodeAlgorithmStd.OTHER || encodeAlgorithmStd==EncodeAlgorithmStd.SGTIN_30
  }

  fun isBC_ALPHA_NUM():Boolean{
    return encodeAlgorithmNonStd == EncodeAlgorithmNonStd.OTHER || encodeAlgorithmNonStd==EncodeAlgorithmNonStd.BC_ALPHA_NUM
  }

  fun isBB_BD_ITEK_NONSTD():Boolean{
    return encodeAlgorithmNonStd==EncodeAlgorithmNonStd.OTHER || encodeAlgorithmNonStd==EncodeAlgorithmNonStd.BB_BD_ITEK_NONSTD
  }

  private fun getZeroAppended(barcode: String, length: Int): String {
    var barcode = barcode
    if (isNullOrEmpty(barcode)) return barcode
    val len = length - barcode!!.length
    if (len > 0) for (i in 0..<len) barcode = "0" + barcode
    return barcode
  }

  fun getValidatedSerialNumber(epc: String):String{
    if (chkNull(epc, "").length < 8) return ""
    val serial = getSerialFromEpc(epc)
    if(serial.equals(BarcodeConstants.UNKNOWN,true)) return serial
    if (serial != null) {
      if (isSGTIN() && epc.matches("(?i)(^(00|30).*$)".toRegex()) && serial.length < 12) return getZeroAppended(serial,12)
      if (isGID() && epc.matches("(?i)(^(05|35).*$)".toRegex()) && serial.length < 11) return getZeroAppended(serial,11)
      if (isBC_ALPHA_NUM() && epc.matches("(?i)(^(0B|BB|0D|BD).*$)".toRegex()) && serial.length < 8) return getZeroAppended(serial,8)
      if (isBB_BD_ITEK_NONSTD() && epc.matches("(?i)(^(0C|BC).*$)".toRegex()) && (serial.length < 7)) return getZeroAppended(serial,7)
    }
    return serial
  }


  fun getSerialFromEpc(epc: String):String{
    if (chkNull(epc, "").length < 8) return ""
    val epcUpper = epc.uppercase(Locale.getDefault()).trim { it <= ' ' }
    val m: Map<String, Any>? = getBarcodeAndSerialFromEpc(epcUpper)
    LogUtils.showLog("m", m?.toString() ?: "null")
    return if ((m != null)) m["serial"].toString().trim() else BarcodeConstants.UNKNOWN
  }

  fun getReturnEpc(epc: String):String{
      if (chkNull(epc, "").length < 8) return ""
      val epcUpper = epc.uppercase(Locale.getDefault()).trim { it <= ' ' }
      val header = epcUpper.substring(0, 2).trim { it <= ' ' }
      if(!header.startsWith(DataStoreManager.readFromPreferences("decodeBits","0"),true)) return ""
      try {
          when (header) {
              DataStoreManager.readFromPreferences("decodeBits","0")+"B" -> {
                  return epcUpper.replaceFirst(header.toRegex(), "BB")
              }
              DataStoreManager.readFromPreferences("decodeBits","0")+"D" -> {
                  return epcUpper.replaceFirst(header.toRegex(), "BD")
              }
              DataStoreManager.readFromPreferences("decodeBits","0")+"C" -> {
                  return epcUpper.replaceFirst(header.toRegex(), "BC")
              }
              DataStoreManager.readFromPreferences("decodeBits","0")+"5" -> {
                  return if (isGID()) epcUpper.replaceFirst(header.toRegex(), "35") else ""
              }
              DataStoreManager.readFromPreferences("decodeBits","0")+"0" -> {
                  return (if (epcUpper.length >= 32) epcUpper.replaceFirst(header.toRegex(), if(isBB_BD_ITEK_NONSTD()) "BD" else if(isBC_ALPHA_NUM()) "BC" else "BD")
                  else epcUpper.replaceFirst(header.toRegex(), (if (isSGTIN()) "30" else if (isGID()) "35" else if(isBB_BD_ITEK_NONSTD()) "BB" else "30")))
              }
              else -> return ""
          }
      }catch (e: Exception) {e.printStackTrace()}
      return ""
  }

  fun getBarcodeFromEpc(epc: String,isCheckDecoded:Boolean=false):String {
    //check here if decode should be ignored
    if (chkNull(epc, "").length < 8) return ""
    val epcUpper = epc.uppercase(Locale.getDefault()).trim { it <= ' ' }
    val header = epcUpper.substring(0, 2).trim { it <= ' ' } //.toUpperCase().trim();
    //showLog("header", header)
    try {
      when (header) {
        DataStoreManager.readFromPreferences("decodeBits","0")+"B" -> {
          return if (isCheckDecoded && isBB_BD_ITEK_NONSTD()) getBarcodeFromEpc(epcUpper.replaceFirst(header.toRegex(), "BB"))
          else BarcodeConstants.NON_ENCODED
        }
        DataStoreManager.readFromPreferences("decodeBits","0")+"D" -> {
          return if (isCheckDecoded && isBB_BD_ITEK_NONSTD()) getBarcodeFromEpc(epcUpper.replaceFirst(header.toRegex(), "BD"))
          else BarcodeConstants.NON_ENCODED
        }
        DataStoreManager.readFromPreferences("decodeBits","0")+"C" -> {
          return if (isCheckDecoded && isBC_ALPHA_NUM()) getBarcodeFromEpc(epcUpper.replaceFirst(header.toRegex(), "BC"))
          else BarcodeConstants.NON_ENCODED
        }
        DataStoreManager.readFromPreferences("decodeBits","0")+"5" -> {
          return if (isCheckDecoded && isGID())
            getBarcodeFromEpc(epcUpper.replaceFirst(header.toRegex(), "35")) else BarcodeConstants.NON_ENCODED
        }
        DataStoreManager.readFromPreferences("decodeBits","0")+"0" -> {
          return if (isCheckDecoded) getBarcodeFromEpc(if (epcUpper.length >= 32) epcUpper.replaceFirst(header.toRegex(), if(isBB_BD_ITEK_NONSTD()) "BD" else if(isBC_ALPHA_NUM()) "BC" else "BD")
          else epcUpper.replaceFirst(header.toRegex(), (if (isSGTIN()) "30" else if (isGID()) "35" else if(isBB_BD_ITEK_NONSTD()) "BB" else "30")))
          else BarcodeConstants.NON_ENCODED
        }
        else -> {
          val m: Map<String, Any>? = getBarcodeAndSerialFromEpc(epcUpper)
          //LogUtils.showLog("m", m?.toString() ?: "null")
          return if ((m != null)) getLeftZeroReplacedString((m["ean"] as String?)!!.trim { it <= ' ' }) else BarcodeConstants.NON_ENCODED
        }
      }
    } catch (e: Exception) {
      return BarcodeConstants.NON_ENCODED
    }
  }

  private fun setAlgorithms(){
    val stdAlgo = DataStoreManager.readFromPreferences(ParameterConstants.STANDARD_ALGORITHM,"").replace("tatagid","GID_35").replace("retailgtin","SGTIN_30")
    val nonStdAlgo = DataStoreManager.readFromPreferences(ParameterConstants.NON_STANDARD_ALGORITHM,"").replace("iteknonstandard","BB_BD_ITEK_NONSTD")
    if(stdAlgo.isNotEmpty() && !encodeAlgorithmStd.toString().equals(stdAlgo,true)){
      encodeAlgorithmStd = if(stdAlgo.equals("GID_35",true)) EncodeAlgorithmStd.GID_35 else if(stdAlgo.equals("SGTIN_30",true)) EncodeAlgorithmStd.SGTIN_30 else EncodeAlgorithmStd.OTHER
    }
    if(nonStdAlgo.isNotEmpty() && !encodeAlgorithmNonStd.toString().equals(nonStdAlgo,true)){
      encodeAlgorithmNonStd  = if(nonStdAlgo.equals("BB_BD_ITEK_NONSTD",true)) EncodeAlgorithmNonStd.BB_BD_ITEK_NONSTD else EncodeAlgorithmNonStd.OTHER
    }
  }

  fun getEpcFromBarcode(barcode:String,isFixedSerial:Boolean=false, serial:String="10000"):String{
    setAlgorithms()
    if(isValidStdBarcode(barcode) && encodeAlgorithmStd != EncodeAlgorithmStd.OTHER){
      if(isSGTIN()) {
        val epc = SGTIN96.convertToSGTIN96(barcode, 7, serial.toLong(), 1)
        if(epc.isNotEmpty()) return if (isFixedSerial && epc.length > 14) epc.substring(0, 14) else epc
      }
      else if(isGID()) {
        val epc = iTEKGID96.GetEpc(barcode,serial.toLong())
        if(epc.isNotEmpty()) return if(isFixedSerial && epc.length > 14) epc.substring(0, 14) else epc
      }
    }
    if(isBB_BD_ITEK_NONSTD()) {
     val epc = iTEKNonStd.encode(barcode,serial)
     if(epc.isNotEmpty()) {
       return if(!isFixedSerial) epc
       else if(isFixedSerial && epc.length <= 24)
         epc.substring(0,18)//17.5 //18 //17
       else if(isFixedSerial && epc.length > 24)
         epc.substring(0,26) //25.5 //26 //25
       else ""
     }
    }
    else if(isBC_ALPHA_NUM()) {
     val epc = SGTIN128Helper.getSgtinFromBarcodeAndSrno(barcode,serial)
     if(epc.isNotEmpty()) return if(isFixedSerial && epc.length > 9) epc.substring(9) else epc
    }
    return ""
  }

  private fun getBarcodeAndSerialFromEpc(epc: String): Map<String, Any>? {
    try {
      if (chkNull(epc, "").length < 8) return null
      val header = epc.substring(0, 2).uppercase(Locale.getDefault())
      return when (header) {
        "30" ->
          if (isSGTIN()) SGTIN96.getBarcodeAndSerialFromEpc(
            (if (epc.length > 24) epc.substring(0, 24) else epc).uppercase(Locale.getDefault())
          )
          else null
        "35" -> if (isGID()) iTEKGID96.getBarcodeAndSerialFromEpc(
          (if (epc.length > 24) epc.substring(0, 24) else epc).uppercase(Locale.getDefault())
        ) else null
        "BC" -> if (isBC_ALPHA_NUM()) SGTIN128Helper.getBarcodeAndSerialFromEpc(epc.uppercase(Locale.getDefault()))
          else null
        "BB","BD" -> if (isBB_BD_ITEK_NONSTD()) iTEKNonStd.decode(epc.uppercase(Locale.getDefault()))
          else null
        else -> null
      }
    } catch (e: java.lang.Exception) {
      e.printStackTrace()
      return null
    }
  }

  internal fun isValidStdBarcode(barcode: String?): Boolean {
    return (isSGTIN() && SGTIN96.IsValidGtin(barcode)) || (isGID() && iTEKGID96.isValidGID(barcode))
  }

  private fun getLeftZeroReplacedString(barcode: String): String {
    val isStdBarcode: Boolean = isValidStdBarcode(barcode.trim())
    return if (!isStdBarcode)  chkNull(barcode, "").trim()
    else chkNull(barcode, "").replaceFirst(Regex("^0+(?!$)"), "").trim()
  }
}
