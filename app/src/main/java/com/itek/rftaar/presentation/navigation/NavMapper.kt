package com.itek.rftaar.presentation.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.Base64

object NavMapper {
    private val gson = Gson()

    // Encodes Map -> JSON -> Base64 (to prevent URL breaking)
    @RequiresApi(Build.VERSION_CODES.O)
    fun encodeMap(params: Map<String, Any>): String {
        val json = gson.toJson(params)
        val encoded = Base64.getEncoder().encodeToString(json.toByteArray())
        return URLEncoder.encode(encoded, "UTF-8")
    }

    // Decodes Base64 -> JSON -> Map
    @RequiresApi(Build.VERSION_CODES.O)
    fun decodeToMap(encoded: String?): Map<String, Any> {
        return try {
            val decoded = URLDecoder.decode(encoded, "UTF-8")
            val json = String(Base64.getDecoder().decode(decoded))
            gson.fromJson(json, Map::class.java) as Map<String, Any>
        } catch (e: Exception) {
            emptyMap()
        }
    }
}