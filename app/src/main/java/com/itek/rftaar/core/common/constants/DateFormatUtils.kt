package com.itek.rftaar.core.common.constants

import com.itek.rftaar.api.constants.ParameterConstants
import com.itek.rftaar.core.database.DataStoreManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

object DateFormatUtils {
  const val API_DATE_TIME_FORMAT:String="yyyy-MM-dd HH:mm:ss"
  const val SERVER_DATE_TIME_FORMAT:String="yyyy-MM-dd HH:mm:ss.SSS"
  const val DISPLAY_DATE_TIME_FORMAT:String="dd-MMM-yyyy HH:mm:ss"

  const val UTC_DATE_TIME_FORMAT:String="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"

    fun formatToUTCTime(dbTime: String):String{
        return formatToDisplayTime(dbTime,SERVER_DATE_TIME_FORMAT,UTC_DATE_TIME_FORMAT)
    }

  fun formatToDisplayTime(dbTime: String,inputFormat:String=SERVER_DATE_TIME_FORMAT,outputFormat:String=DISPLAY_DATE_TIME_FORMAT): String {
     try {
       val inFormat =  getDateFormater(/*if(dbTime.contains("T")) UTC_DATE_TIME_FORMAT else*/ inputFormat)
       val outFormat =  getDateFormater(outputFormat)
       return outFormat.format(inFormat.parse(dbTime))
    } catch (e: Exception) {
      e.printStackTrace()
      return ""
    }
  }

  fun formatToDisplayTime(time:Date,outputFormat:String=DISPLAY_DATE_TIME_FORMAT) :String {
       try{
         return getDateFormater(outputFormat).format(time)
       }
       catch (e: Exception) {e.printStackTrace()}
       return ""
   }

  fun getCurrentTime():String{
    return getDateFormater(SERVER_DATE_TIME_FORMAT).format(Date(System.currentTimeMillis()))
  }

  fun getCurrentUTCTime():String{
    return getDateFormater(UTC_DATE_TIME_FORMAT).format(Date(System.currentTimeMillis()))
  }

  fun getCurrentAPITime():String{
    return getDateFormater(API_DATE_TIME_FORMAT).format(Date(System.currentTimeMillis()))
  }

  fun getCurrentDisplayTime():String{
    return getDateFormater(DISPLAY_DATE_TIME_FORMAT).format(Date(System.currentTimeMillis()))
  }

  private fun getDateFormater(dateFormat: String):SimpleDateFormat{
      val dateFormater = SimpleDateFormat(dateFormat)
      val timeZone = DataStoreManager.readFromPreferences(ParameterConstants.TIME_ZONE, "")
      try{
       if(timeZone.isNotEmpty() || dateFormat.contains("Z"))
        dateFormater.timeZone= TimeZone.getTimeZone(if(dateFormat.contains("Z")) "UTC" else timeZone)
      }catch (e: Exception) {e.printStackTrace()}
      return dateFormater
  }

}