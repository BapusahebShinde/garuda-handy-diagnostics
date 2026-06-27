package com.itek.rftaar.core.common.utils

import android.os.Bundle
import com.google.gson.Gson
import com.itek.rftaar.api.constants.ParameterConstants
import com.itek.rftaar.utils.CommonUtils
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.Locale
import kotlin.reflect.KClass

object ParseUtils {

  fun extractErrMsg(jsonObject: JSONObject?):String{
    var keys = checkKeys(jsonObject,arrayOf(
        ParameterConstants.ERROR,
        ParameterConstants.ERROR_DESCRIPTION,
        ParameterConstants.ERR_MSG,
        ParameterConstants.MESSAGE));
    return extractString(jsonObject,keys)
  }

  fun hasKeys(jsonObject: JSONObject?, keys: Array<String>):Boolean{
      if (jsonObject != null && keys!=null && keys.isNotEmpty()) {
        return keys.filter { key-> jsonObject.has(key) }.size == keys.size
      }
      return false
  }

  private fun checkKeys(jsonObject: JSONObject?, keys: Array<String>): String? {
    if (jsonObject != null && keys!=null && keys.isNotEmpty()) {
      for(key in keys) {
        if (jsonObject.has(key)) return key
      }
    }
    return null
  }

  fun extractString(jsonObject: JSONObject?, key1: String?, def: String=""): String {
    var key = key1
    try {
      //key = checkKey(jsonObject, key)
      if (jsonObject != null && CommonUtils.isNonEmpty(key) && jsonObject.has(key) && jsonObject[key] != null) {
        return if (jsonObject[key] is String) CommonUtils.chkNull(jsonObject.getString(key), def)
        else CommonUtils.chkNull(jsonObject[key].toString(), def)
      }
    } catch (e: JSONException) {
      e.printStackTrace()
    }
    return def
  }

  /**
   * Extract int integer.
   *
   * @param jsonObject the json object
   * @param key        the key
   * @param def        the def
   * @return the integer
   */
  fun extractInt(jsonObject: JSONObject?, key1: String?, def: Int=0): Int {
    var key = key1
    try {
      if (jsonObject != null && CommonUtils.isNonEmpty(key) && jsonObject.has(
          key
        ) && jsonObject[key] != null
      ) {
        return if (jsonObject[key] is Int) CommonUtils.chkNull(
            jsonObject.getInt(key),
            def
        )
        else if (jsonObject[key] is Long) CommonUtils.chkNull(
            jsonObject.getLong(
                key
            ).toInt(), def
        )
        else if (jsonObject[key] is Double) CommonUtils.chkNull(
            jsonObject.getDouble(key).toInt(),
            def
        )
        else CommonUtils.chkNull(
            jsonObject[key].toString().toInt(),
            def
        )
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }
    return def
  }

  /**
   * Extract float float.
   *
   * @param jsonObject the json object
   * @param key        the key
   * @param def        the def
   * @return the float
   */
  fun extractFloat(jsonObject: JSONObject?, key1: String?, def: Float): Float {
    var key = key1
    try {
      if (jsonObject != null && CommonUtils.isNonEmpty(key) && jsonObject.has(
          key
        ) && jsonObject[key] != null
      ) {
        return if (jsonObject[key] is Float) CommonUtils.chkNull(
            jsonObject[key] as Float, def
        )
        else if (jsonObject[key] is Double) CommonUtils.chkNull(
            jsonObject.getDouble(key).toFloat(),
            def
        )
        else if (jsonObject[key] is Long) CommonUtils.chkNull(
            jsonObject.getLong(
                key
            ).toFloat(), def
        )
        else if (jsonObject[key] is Int) CommonUtils.chkNull(
            jsonObject.getInt(
                key
            ).toFloat(), def
        )
        else CommonUtils.chkNull(
            jsonObject[key].toString().toFloat(),
            def
        )
      }
    } catch (e: java.lang.Exception) {
      e.printStackTrace()
    }
    return def
  }

  /**
   * Extract long long.
   *
   * @param jsonObject the json object
   * @param key        the key
   * @param def        the def
   * @return the long
   */
  fun extractLong(jsonObject: JSONObject?, key1: String?, def: Long=0L): Long {
    var key = key1
    try {
      if (jsonObject != null && CommonUtils.isNonEmpty(key) && jsonObject.has(
          key
        ) && jsonObject[key] != null
      ) {
        return if (jsonObject[key] is Long) CommonUtils.chkNull(
            jsonObject.getLong(key),
            def
        )
        else if (jsonObject[key] is Int) CommonUtils.chkNull(
            jsonObject.getInt(
                key
            ).toLong(), def
        )
        else if (jsonObject[key] is Double) CommonUtils.chkNull(
            jsonObject.getDouble(key).toLong(),
            def
        )
        else CommonUtils.chkNull(
            jsonObject[key].toString().toLong(),
            def
        )
      }
    } catch (e: java.lang.Exception) {
      e.printStackTrace()
    }
    return def
  }

  /**
   * Extract double double.
   *
   * @param jsonObject the json object
   * @param key        the key
   * @param def        the def
   * @return the double
   */
  fun extractDouble(jsonObject: JSONObject?, key1: String?, def: Double=0.00): Double {
    var key = key1
    try {
      if (jsonObject != null && CommonUtils.isNonEmpty(key) && jsonObject.has(
          key
        ) && jsonObject[key] != null
      ) {
        return if (jsonObject[key] is Double) CommonUtils.chkNull(
            jsonObject.getDouble(key),
            def
        )
        else if (jsonObject[key] is Long) CommonUtils.chkNull(
            jsonObject.getLong(
                key
            ).toDouble(), def
        )
        else if (jsonObject[key] is Int) CommonUtils.chkNull(
            jsonObject.getInt(
                key
            ).toDouble(), def
        )
        else CommonUtils.chkNull(
            jsonObject[key].toString().toDouble(),
            def
        )
      }
    } catch (e: java.lang.Exception) {
      e.printStackTrace()
    }
    return def
  }

  /**
   * Extract boolean boolean.
   *
   * @param jsonObject the json object
   * @param key        the key
   * @param def        the def
   * @return the boolean
   */
  fun extractBoolean(jsonObject: JSONObject?, key1: String?, def: Boolean=false): Boolean {
    var withoutIsKey:String=
        CommonUtils.chkNull(key1?.lowercase(Locale.getDefault())?.replace("is", ""), "")
    var withIsKey="is"+withoutIsKey[0].uppercase(Locale.getDefault())+withoutIsKey.substring(1).lowercase(
        Locale.getDefault())
    var key = checkKeys(jsonObject, if(!key1.isNullOrEmpty()) arrayOf(CommonUtils.chkNull(key1, ""),withoutIsKey,withIsKey) else arrayOf(withoutIsKey,withIsKey))
    try {
      if (jsonObject != null && CommonUtils.isNonEmpty(key) &&
        jsonObject.has(key) && jsonObject[key] != null
      ) {
        return if (jsonObject[key] is Boolean) CommonUtils.chkNull(
            jsonObject.getBoolean(key),
            def
        )
        else CommonUtils.chkNull(
            jsonObject[key].toString().toBoolean(),
            def
        )
      }
    } catch (e: java.lang.Exception) {
      e.printStackTrace()
    }
    return def
  }

  fun extractJSONObject(jsonObject: JSONObject?, key1: String?, def: JSONObject): JSONObject {
    var key = key1
    try {
      if (jsonObject != null && CommonUtils.isNonEmpty(key) && jsonObject.has(
          key
        ) && jsonObject[key] != null && jsonObject[key] is JSONObject
      ) return CommonUtils.chkNull(jsonObject.getJSONObject(key), def)
      else if (jsonObject != null && CommonUtils.isNonEmpty(key) && jsonObject.has(
          key
        ) && jsonObject[key] != null && jsonObject[key] is String
      ) {
        try {
          return CommonUtils.chkNull(
              JSONObject(
                  jsonObject.getString(key).replace("\\\"".toRegex(), "\"")
              ), def
          )
        } catch (e: java.lang.Exception) {
          e.printStackTrace()
        }
      }
    } catch (e: JSONException) {
      e.printStackTrace()
    }
    return def
  }

  //Extract from JSONObject
  fun extractJSONArray(jsonObject: JSONObject?, key1: String?, def: JSONArray): JSONArray {
    var key = key1
    try {
      if (jsonObject != null && CommonUtils.isNonEmpty(key) && jsonObject.has(
          key
        ) && jsonObject[key] != null && jsonObject[key] is JSONArray
      ) return CommonUtils.chkNull(jsonObject.getJSONArray(key), def)
    } catch (e: JSONException) {
      e.printStackTrace()
    }
    return def
  }

  /**
   * Extract long long.
   *
   * @param args the args
   * @param key  the key
   * @param def  the def
   * @return the long
   */
  fun extractLong(args: Bundle?, key: String?, def: Long): Long {
    try {
      if (args != null && args.containsKey(key) && args[key] != null) {
        return if (args[key] is Long) CommonUtils.chkNull(
            args.getLong(
                key
            ), def
        )
        else if (args[key] is Int) CommonUtils.chkNull(
            args.getInt(key).toLong(), def
        )
        else if (args[key] is Double) CommonUtils.chkNull(
            args.getDouble(
                key
            ).toLong(), def
        )
        else CommonUtils.chkNull(args[key].toString().toLong(), def)
      }
    } catch (e: java.lang.Exception) {
      e.printStackTrace()
    }
    return def
  }

  /**
   * Extract int integer.
   *
   * @param args the args
   * @param key  the key
   * @param def  the def
   * @return the integer
   */
  fun extractInt(args: Bundle?, key: String?, def: Int): Int {
    try {
      if (args != null && args.containsKey(key) && args[key] != null) {
        return if (args[key] is Int) CommonUtils.chkNull(
            args.getInt(key),
            def
        )
        else if (args[key] is Long) CommonUtils.chkNull(
            args.getLong(key).toInt(), def
        )
        else if (args[key] is Double) CommonUtils.chkNull(
            args.getDouble(
                key
            ).toInt(), def
        )
        else CommonUtils.chkNull(args[key].toString().toInt(), def)
      }
    } catch (e: java.lang.Exception) {
      e.printStackTrace()
    }
    return def
  }


  //Map Parsing
  fun isValidParamsKey(params:Map<String, Any>?, key: String): Boolean{
      return params!!.isNotEmpty() && key.isNotEmpty() && params!!.containsKey(key) && params!!.get(key)!=null
  }

  fun extractString(params:Map<String, Any>?, key: String, def:String=""):String{
    return if(isValidParamsKey(params,key) && params!!.get(key) is String) CommonUtils.chkNull(
        params[key] as String,
        def
    ) else def
  }

  fun isListString(obj:Any?): Boolean{
    return obj!=null && obj is List<*> && obj.all { it is String }
  }

  fun extractStringList(params:Map<String, Any>?, key: String, def:List<String>):List<String>{
    return if(isValidParamsKey(params,key) && isListString(params!!.get(key))) params[key] as List<String> else def
  }

  /*fun extractObject(params:Map<String, Any>?, cls: KClass<Any>){
    val key =chkNull(cls.simpleName,"")
    return if(isValidParamsKey(params,key) && cls.isInstance(params!!.get(key)))
    else null
  }*/

  fun reconvertToPairList(stringFields:String): List<Pair<String, String>> {
      LogUtils.showLog("stringFields",stringFields)
      // 1. Remove the outer brackets
      val content = stringFields.trim().removeSurrounding("[", "]")
      if (content.isEmpty()) return emptyList()

      // 2. Split by the boundary between pairs: "), ("
      // This is safer than splitting by every comma.
      return content.split("), (").map { entry ->
          // Clean up the remaining parentheses from the first and last items
          val cleanEntry = entry.removePrefix("(").removeSuffix(")")

          // 3. Split by the first comma to separate Key and Value
          // limit = 2 ensures we don't break if the value itself contains a comma
          val parts = cleanEntry.split(",", limit = 2)

          val key = parts.getOrNull(0)?.trim() ?: ""
          val value = parts.getOrNull(1)?.trim() ?: ""

          Pair(key, value)
      }
  }


  inline fun <reified T> extractTypedList(params: Map<String, Any>?, key: String): List<T> {
      val list = params?.get(key) as? List<*> ?: return emptyList()
      // filterIsInstance removes any item that isn't of type T
      LogUtils.showLog("1List",""+list)
      list?.firstOrNull()?.let {
       LogUtils.showLog("TypeCheck", "Item is: ${it::class.java.simpleName}, Target is: ${T::class.java.simpleName}")
      }

      val gson = Gson()
      return list.map { item ->
          when (item) {
              is T -> item // Already the correct type
              is Map<*, *> -> {
                  // If it's a LinkedTreeMap, convert it to the target type T
                  val json = gson.toJson(item)
                  gson.fromJson(json, T::class.java)
              }
              else -> null
          }
      }.filterNotNull()
  }

  inline fun <reified T : Any> extractObject(params:Map<String, Any>?, key1:String="", cls: KClass<T>): T? {
    // 1. Get the key from the class
    val key = if(key1.isNotEmpty()) key1 else cls.simpleName ?: return null
    LogUtils.showLog("1key", key+"_"+params?.containsKey(key))
    // 2. Safely get the value from the map
    val value = params?.get(key)
    LogUtils.showLog("1value",""+value)
    // 3. Use 'is' to check the type and return it (Kotlin smart-casts it to T)
    //return if (value is T) value else null
    if(value is T) return value

    return try {
          val gson = Gson()
          val json = gson.toJson(value) // Converts the Map/Value to JSON
          gson.fromJson(json, T::class.java) // Converts JSON to your Class
      } catch (e: Exception) {
          e.printStackTrace()
          null
    }
  }

  fun extractBoolean(params:Map<String, Any>?, key: String, def:Boolean=false): Boolean{
    return if(isValidParamsKey(params,key) && params!!.get(key) is Boolean) CommonUtils.chkNull(
        params[key] as Boolean,
        def
    ) else def
  }

}