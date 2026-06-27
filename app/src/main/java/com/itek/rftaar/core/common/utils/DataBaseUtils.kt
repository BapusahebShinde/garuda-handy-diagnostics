package com.itek.rftaar.core.common.utils

import com.google.gson.Gson
import org.json.JSONArray

object DataBaseUtils {
    suspend inline fun <reified T> saveArrayToDb(
        array: JSONArray?,
        crossinline insertAll: suspend (List<T>) -> Unit
    ) {
        if (array == null || array.length() == 0) return

        val list = mutableListOf<T>()
        val gson = Gson()
        for (i in 0 until array.length()) {
            val obj = array.optJSONObject(i) ?: continue
            val item = gson.fromJson(obj.toString(), T::class.java)
            list.add(item)
        }

        if (list.isNotEmpty()) {
            insertAll(list)
        }
    }
}