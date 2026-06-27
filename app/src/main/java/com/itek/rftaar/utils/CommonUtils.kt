package com.itek.rftaar.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ApplicationInfo
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.itek.rftaar.R
import com.itek.rftaar.core.common.utils.LogUtils
import org.json.JSONArray
import org.json.JSONObject
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CommonUtils {

  fun getFormattedDate(): String {
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return dateFormat.format(Date())
  }

  fun getFormattedTime(): String {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    return timeFormat.format(Date())
  }


  @Composable
  fun DialogIconRow(
    modifier: Modifier = Modifier,
    centerIcon: Painter? = null,
    rightIcon: Painter? = null,
    onRightClick: (() -> Unit)? = null
  ) {
    Row(
      modifier = modifier
        .fillMaxWidth()
        .padding(vertical = 12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {

      if (centerIcon != null) {
        Icon(
          painter = centerIcon,
          contentDescription = null,
          modifier = Modifier
            .size(56.dp),
          tint = Color.Unspecified
        )
      }

      if (rightIcon != null) {
        Icon(
          painter = rightIcon,
          contentDescription = null,
          modifier = Modifier
            .wrapContentWidth()
            .wrapContentHeight()
            .clickable(
              onClick = { onRightClick?.invoke() },
              indication = null,
              interactionSource = remember { MutableInteractionSource() }
            ),
          tint = Color.Unspecified
        )
      }
    }
  }


  @SuppressLint("HardwareIds")
  fun getDeviceId(context: Context): String {
    return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
  }

  fun isNonEmpty(str: String?): Boolean {
    return str?.isNotEmpty() == true && !str.trim { it <= ' ' }
      .equals("null", ignoreCase = true)
  }

  fun isNullOrEmpty(str: String?): Boolean { return !isNonEmpty(str) }

  fun isNonEmpty(collection: Collection<Any>?): Boolean { return !collection.isNullOrEmpty() }

  fun isNullOrEmpty(collection: Collection<Any>?): Boolean { return !isNonEmpty(collection) }

  fun chkNull(str: String?, def: String): String {
    return if (isNonEmpty(str)) str!!.trim { it <= ' ' } else def
  }

  fun getMaskedString(str: String,mask:String="*",isInverseMode:Boolean=true,maskEndLength:Int=4,maskStarLength:Int=0): String{
    LogUtils.showLog("str",str)
    if(chkNull(str,"").isEmpty()) return chkNull(str,"")
    val preString =  if(maskStarLength<=0) "" else if(isInverseMode) str.substring(0,maskStarLength) else String.format("%" + maskStarLength + "s", "").replace(" ", mask)
    val endString =  if(maskEndLength<=0) "" else if(isInverseMode) str.substring(str.length-maskEndLength) else String.format("%" + maskEndLength + "s", "").replace(" ", mask)
    val middleString = str.substring(maskStarLength,str.length-maskEndLength)
    val maskedStr = preString + (if(isInverseMode) String.format("%" + middleString.length + "s", "").replace(" ", mask) else middleString) + endString
    LogUtils.showLog("maskedStr",maskedStr)
    return maskedStr
  }

  fun getMostRepeatedValue(list: ArrayList<String>): String? {
    // 1. Convert the list to a sequence (Kotlin's lazy stream equivalent)
    return list.asSequence()
      // 2. Group the elements by their value and count occurrences
      .groupBy { it }
      .mapValues { it.value.count() }
      // 3. Convert the map entries to a sequence for sorting
      .asSequence()
      // 4. Sort the entries by count (value) in descending order
      .sortedByDescending { it.value }
      // 5. Take the first entry (which has the highest count)
      .firstOrNull()
      // 6. Get the key (the string value)
      ?.key
  }

  /**
   * Chk null integer.
   *
   * @param i   the
   * @param def the def
   * @return the integer
   */
  fun chkNull(i: Int?, def: Int): Int {
    return i ?: def
  }

  /**
   * Chk null long.
   *
   * @param l   the l
   * @param def the def
   * @return the long
   */
  fun chkNull(l: Long?, def: Long): Long {
    return l ?: def
  }

  /**
   * Chk null float.
   *
   * @param f   the f
   * @param def the def
   * @return the float
   */
  fun chkNull(f: Float?, def: Float): Float {
    return f ?: def
  }

  /**
   * Chk null double.
   *
   * @param d   the d
   * @param def the def
   * @return the double
   */
  fun chkNull(d: Double?, def: Double): Double {
    return d ?: def
  }

  /**
   * Chk null boolean.
   *
   * @param bool the bool
   * @param def  the def
   * @return the boolean
   */
  fun chkNull(bool: Boolean?, def: Boolean): Boolean {
    return bool ?: def
  }

  /**
   * Chk true boolean.
   *
   * @param bool the bool
   * @return the boolean
   */
  fun chkTrue(bool: Boolean?): Boolean {
    return bool != null && bool
  }

  /**
   * Chk false boolean.
   *
   * @param bool the bool
   * @return the boolean
   */
  fun chkFalse(bool: Boolean?): Boolean {
    return bool != null && !bool
  }

  fun chkNull(o: Any?, def: Any?): Any? {
    return o ?: def
  }

  /**
   * Chk null json object.
   *
   * @param jObj the j obj
   * @param def  the def
   * @return the json object
   */
  fun chkNull(jObj: JSONObject?, def: JSONObject?): JSONObject {
    return jObj ?: def!!
  }

  fun getRequestJson(jsonRequest: JSONObject?): JsonObject? {
    if(jsonRequest==null) return null
    return getGSON().fromJson(jsonRequest.toString(), JsonObject::class.java)
  }

  @Throws(JsonParseException::class)
  fun getGSON(): Gson {
    return GsonBuilder().registerTypeAdapter(
      String::class.java,
      StringTrimJsonDeserializer()
    ).create()
  }

  class StringTrimJsonDeserializer : JsonDeserializer<String?> {
    @Throws(JsonParseException::class)
    override fun deserialize(
      json: JsonElement,
      typeOfT: Type?,
      context: JsonDeserializationContext?
    ): String? {
      val value = json.asString
      return value?.trim { it <= ' ' }
    }
  }

  fun toUnderScoreCase(str: String?): String? {
    if (str.isNullOrEmpty()) return str ?: ""
    val hasUppercase = str?.replace(Regex("([A-Z])"), " $1")?.trim()?.contains("\\s".toRegex())
    return if (!hasUppercase!!) {
      str?.lowercase()
    } else {
      str?.replace(Regex("([A-Z])"), " $1")
        ?.trim()
        ?.replace(" ", "_")
        ?.lowercase()
    }
  }

  fun toCamelCase(str: String?): String? {
    if (str.isNullOrEmpty()) return str ?: ""
    val hasUnderscore = str?.replace("_", " ")?.contains("\\s".toRegex())
    if (!hasUnderscore!!) return str?.lowercase()

    val words = str?.replace("_", " ")?.split(" ")
    return buildString {
      words?.forEachIndexed { index, word ->
        append(
          if (index == 0) word.lowercase()
          else word.replaceFirstChar { it.uppercase() }.lowercase()
            .replaceFirstChar { it.uppercase() }
        )
      }
    }.trim()
  }

  fun toPascalCase(str: String?): String {
    if (str.isNullOrEmpty()) return str ?: ""
    val hasUnderscore = str?.replace("_", " ")?.contains("\\s".toRegex())
    val words = if (hasUnderscore == true) str?.replace("_", " ")?.split(" ")
    else listOf(str)

    return buildString {
      if (words != null) {
        for (word in words) {
          append(
            word?.lowercase()?.replaceFirstChar { it.uppercase() }
          )
        }
      }
    }.trim()
  }

  fun chkNull(jArray: JSONArray?, def: JSONArray): JSONArray {
    return jArray ?: def
  }

  /*
fun checkKey(jsonObject: JSONObject?, key: String): String {
    if (jsonObject == null) return key

    return when {
        jsonObject.has(key) -> key
        jsonObject.has(toPascalCase(key)) -> toPascalCase(key)
        jsonObject.has(toCamelCase(key)) -> toCamelCase(key)
        jsonObject.has(toUnderScoreCase(key)) -> toUnderScoreCase(key)
        else -> key
    }
}*/

  /*
fun extractJSONArray(jsonObject: JSONObject?, key: String, default: JSONArray? = null): JSONArray? {
    if (jsonObject == null) return default

    return try {
        val validKey = checkKey(jsonObject, key)
        if (validKey.isNotEmpty() && jsonObject.has(validKey)) {
            val value = jsonObject.opt(validKey)
            if (value is JSONArray) {
                chkNull(value, default)
            } else {
                default
            }
        } else {
            default
        }
    } catch (e: JSONException) {
        e.printStackTrace()
        default
    } as JSONArray?
}


fun extractString(jsonObject: JSONObject?, key: String, def: String = ""): String? {
    if (jsonObject == null) return def

    return try {
        val validKey = checkKey(jsonObject, key)
        if (validKey.isNotEmpty() && jsonObject.has(validKey)) {
            val value = jsonObject.opt(validKey)
            when (value) {
                is String -> chkNull(value, def)
                null -> def
                else -> chkNull(value.toString(), def)
            }
        } else {
            def
        }
    } catch (e: JSONException) {
        e.printStackTrace()
        def
    }
}
*/

    fun getMenuIconByCode(menuCode: String): Int {
        return when (menuCode.replaceFirst(Regex("(ROOT_|CHILD_)"),"")) {
            "MENU_ASSOCIATE_BARCODE" -> R.drawable.ic_map_barcode
            "MENU_RETURN" -> R.drawable.ic_return
            "MENU_INVENTORY" -> R.drawable.ic_inv
            "MENU_ENCODE" -> R.drawable.ic_enc
            "MENU_SEARCH" -> R.drawable.product_search
            "MENU_INWARD" -> R.drawable.ic_inw
            "MENU_MOVEMENT" -> R.drawable.ic_mov
            "MENU_OUTWARD" -> R.drawable.ic_otw
            "MENU_DECODE" -> R.drawable.decoding
            "MENU_NORMAL_ENCODE","MENU_SINGLE_ENCODE" -> R.drawable.start_encoding
            "MENU_BULK_ENCODE","MENU_MULTI_ENCODE" -> R.drawable.multi_encoding
            "MENU_VERIFY_ENCODE" -> R.drawable.verify_encoding
            "MENU_SCAN_SCAN_ENCODE" -> R.drawable.scan_encoding
            "MENU_TAKE_INVENTORY" -> R.drawable.property_available
            "MENU_CYCLE_COUNT_INVENTORY" -> R.drawable.strat_inventory
            "MENU_ADD_INVENTORY" -> R.drawable.property_1_add_circle
            "MENU_CUSTOM_INVENTORY" -> R.drawable.brand_inventory
            "MENU_CUSTOM_TAKE_INVENTORY" -> R.drawable.property_available
            "MENU_CUSTOM_ADD_INVENTORY" -> R.drawable.property_1_add_circle
            "MENU_STOCK_CORRECTION" -> R.drawable.stock_correction
            "MENU_MOVE_STOCK" -> R.drawable.property_zonal_movement
            "MENU_REPLENISH_STOCK" -> R.drawable.property_replenishment
            "MENU_NORMAL_SEARCH","MENU_PRODUCT_SEARCH" -> R.drawable.strat_inventory
            "MENU_OMNICHANNEL_SEARCH" -> R.drawable.omnichannel
            "MENU_LIST_SEARCH" -> R.drawable.list_based
            "MENU_UNENCODED_SEARCH" -> R.drawable.unencoded
            else -> R.drawable.property_info
        }
    }


    @SuppressLint("DiscouragedApi")
    fun getMenuIconId(context: Context, menuCode: String?): Int {
    val menuIconName = "ic_" + menuCode?.lowercase()?.replaceFirst(Regex("(?i)^.*MENU_"),"")
    val resId = context.resources.getIdentifier(
        menuIconName,
        "drawable",
        context.packageName
    )
    return if (resId != 0) resId else R.drawable.property_info
}

  fun splitDateTime(dateTime: String): Pair<String, String> {
    val parts = dateTime.split(" ")
    val date = parts.getOrNull(0) ?: ""
    val time = parts.getOrNull(1) ?: ""
    return Pair(date, time)
  }

  var LocalBackNavigationHandler = staticCompositionLocalOf<(() -> Unit)?> { null }

  /*fun generateDeviceId(uniqueId: String): String {
    val prefixed = "itek-${uniqueId.toLowerCase()}"
    return UUIDV5.generateUuidV5(name = prefixed);
  }*/

  private fun isDebuggable(context: Context): Boolean {
    return 0 != (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE)
  }
}






