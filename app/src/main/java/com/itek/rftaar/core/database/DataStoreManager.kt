package com.itek.rftaar.core.database

import android.annotation.SuppressLint
import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.itek.rftaar.api.constants.ParameterConstants
import com.itek.rftaar.core.common.constants.DateFormatUtils
import com.itek.rftaar.core.common.constants.LoginConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar

object DataStoreManager {

    private const val PREFS_NAME = "my_prefs"
    private lateinit var appContext: Context
    private val cache = mutableMapOf<String, Any>()
    private val appScope = CoroutineScope(Dispatchers.IO)
    private val Context.dataStore by preferencesDataStore(name = PREFS_NAME)

    fun init(context: Context) {
        appContext = context.applicationContext

        appScope.launch {
            appContext.dataStore.data.collect { prefs ->
                prefs.asMap().forEach { (key, value) ->
                    cache[key.name] = value
                }
            }
        }
    }

    // --- Generic save ---
    fun <T> saveToPreferences(key: String, value: T) {
        cache[key] = value as Any
        val prefKey = when (value) {
            is String -> stringPreferencesKey(key)
            is Int -> intPreferencesKey(key)
            is Boolean -> booleanPreferencesKey(key)
            is Float -> floatPreferencesKey(key)
            is Long -> longPreferencesKey(key)
            is Double -> floatPreferencesKey(key)
            else -> throw kotlin.IllegalArgumentException("Unsupported type")
        }

        appScope.launch {
            appContext.dataStore.edit { prefs ->
                when (value) {
                    is String -> prefs[prefKey as Preferences.Key<String>] = value
                    is Int -> prefs[prefKey as Preferences.Key<Int>] = value
                    is Boolean -> prefs[prefKey as Preferences.Key<Boolean>] = value
                    is Float -> prefs[prefKey as Preferences.Key<Float>] = value
                    is Long -> prefs[prefKey as Preferences.Key<Long>] = value
                    is Double -> prefs[prefKey as Preferences.Key<Float>] = value.toFloat()
                }
            }
        }
    }

    // --- Generic  read  ---
    @Suppress("UNCHECKED_CAST")
    fun <T> readFromPreferences(key: String, defaultValue: T): T {

        val cachedValue = cache[key]

        return when (defaultValue) {
            is String -> {
                val value = cachedValue as? String
                if (!value.isNullOrEmpty()) value as T else defaultValue
            }
            else -> cachedValue as? T ?: defaultValue
        }
    }
    /**fun <T> readFromPreferences(key: String, defaultValue: T): T {
        val prefKey = when (defaultValue) {
            is String -> stringPreferencesKey(key)
            is Int -> intPreferencesKey(key)
            is Boolean -> booleanPreferencesKey(key)
            is Float -> floatPreferencesKey(key)
            is Long -> longPreferencesKey(key)
            is Double -> floatPreferencesKey(key)
            else -> throw kotlin.IllegalArgumentException("Unsupported type")
        }

        return runBlocking {
            try {
                val flow = appContext.dataStore.data.map { prefs ->
                    when (defaultValue) {
                        is String -> {
                            val readValue = chkNull(prefs[prefKey as Preferences.Key<String>],"")
                            if(readValue.isNotEmpty()) readValue else defaultValue
                        }
                        is Int -> prefs[prefKey as Preferences.Key<Int>] ?: defaultValue
                        is Boolean -> prefs[prefKey as Preferences.Key<Boolean>] ?: defaultValue
                        is Float -> prefs[prefKey as Preferences.Key<Float>] ?: defaultValue
                        is Long -> prefs[prefKey as Preferences.Key<Long>] ?: defaultValue
                        is Double -> prefs[prefKey as Preferences.Key<Float>]?.toDouble() ?: defaultValue
                        else -> throw kotlin.IllegalArgumentException("Unsupported type")
                    }
                }
                flow.first()
            } catch (e: ClassCastException) {
                // Wrong type stored, clear and return default
                appContext.dataStore.edit { it.remove(prefKey) }
                defaultValue
            }
        } as T
    }*/

    @SuppressLint("SimpleDateFormat")
    fun getAccessTokenTime(): Long {
        return try {
            val cc = Calendar.getInstance()
            val date: String =
                readFromPreferences(
                    LoginConstants.TOKEN_TIME,
                    ""
                )
            if (date.isEmpty()) return 0L
            val tokenValidDate = SimpleDateFormat(DateFormatUtils.API_DATE_TIME_FORMAT).parse(date)
            if (!cc.time.before(tokenValidDate)) 0L
            else (tokenValidDate.time - cc.timeInMillis) / 1000
        } catch (e: ParseException) {
            e.printStackTrace()
            0L
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun setAccessTokenTime(accessTokenTime: Long?) {
        if (accessTokenTime != null && accessTokenTime > 0) {
            val cc = Calendar.getInstance()
            cc.add(Calendar.SECOND, accessTokenTime.toInt())
            val formatted = SimpleDateFormat(DateFormatUtils.API_DATE_TIME_FORMAT).format(cc.time)
            saveToPreferences(
                LoginConstants.TOKEN_TIME,
                formatted
            )
        }
    }

    /**
     * Get device type value integer.
     *
     * @return the integer
     */
    fun getSensorTypeValue(): Int {
        return readFromPreferences("SENSOR_TYPE",0)
    }

    fun setSensorTypeValue(sensorType: Int) {
        saveToPreferences("SENSOR_TYPE",sensorType)
    }

    /**
     * Get is sensor available boolean.
     *
     * @return the boolean
     */
    fun getIsSensorAvailable(): Boolean? {
        return readFromPreferences("IS_SENSOR_AVAILABLE",false)
    }

    /**
     * Set is sensor available.
     *
     * @param isSensorAvailable the is show crash log
     */
    fun setIsSensorAvailable(isSensorAvailable: Boolean?) {
        saveToPreferences("IS_SENSOR_AVAILABLE", isSensorAvailable)
    }

    /**
     * Get is chk rationale boolean.
     *
     * @return the boolean
     */
    fun getIsChkRationale(): Boolean {
        return readFromPreferences("IS_CHK_RATIONALE",false)
    }

    /**
     * Set is chk rationale.
     *
     * @param isChkRationale the is chk rationale
     */
    fun setIsChkRationale(isChkRationale: Boolean) {
      saveToPreferences("IS_CHK_RATIONALE",isChkRationale)
    }

    /**
     * Get is zebra barcode profile set boolean.
     *
     * @return the boolean
     */
    fun getIsZebraBarcodeProfileSet(): Boolean {
        return readFromPreferences("IS_ZEBRA_BARCODE_PROFILE_SET",false)
    }

    /**
     * Set is zebra barcode profile set.
     *
     * @param zebraBarcodeProfileSet the zebra barcode profile set
     */
    fun setIsZebraBarcodeProfileSet(zebraBarcodeProfileSet: Boolean) {
      saveToPreferences("IS_ZEBRA_BARCODE_PROFILE_SET",zebraBarcodeProfileSet)
    }

    fun savePasswords(currentPassword:String,oldPasswords: List<String>){
        if(currentPassword.isNotEmpty()) setCurrentPassword(currentPassword)
        if(oldPasswords.isNotEmpty()) saveOldPasswordList(oldPasswords)
    }

    fun getCurrentPassword():String{
     return readFromPreferences(ParameterConstants.CURRENT_PASSWORD,"")
    }

    fun setCurrentPassword(currentPassword:String){
      saveToPreferences(ParameterConstants.CURRENT_PASSWORD,currentPassword)
    }

    fun saveOldPasswordList(passwords: List<String>) {
        saveListStr(ParameterConstants.OLD_PASSWORDS,passwords)
    }

    fun saveFilterList(passwords: List<String>) {
        saveListStr(ParameterConstants.FILTERS,passwords)
    }

    fun saveListStr(key:String,list:List<String>) {
        try {
            val jsonArray = org.json.JSONArray(list)
            saveToPreferences(key, jsonArray.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getListStr(key:String):List<String>{
        val jsonString = readFromPreferences(key, "[]")
        val list = mutableListOf<String>()
        try {
            val jsonArray = org.json.JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                list.add(jsonArray.optString(i))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun getFilterList(): List<String> {
        return getListStr(ParameterConstants.FILTERS)
    }

    fun getOldPasswordList(): List<String> {
        return getListStr(ParameterConstants.OLD_PASSWORDS)
        /*val jsonString = readFromPreferences(ParameterConstants.OLD_PASSWORDS, "[]")
        val list = mutableListOf<String>()

        try {
            val jsonArray = org.json.JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                list.add(jsonArray.optString(i))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return list*/
    }

    fun getBarcodeLabel():String{
        return readFromPreferences(ParameterConstants.LABEL_BARCODE,"Barcode")
    }

    fun getArticleLabel():String{
        return readFromPreferences(ParameterConstants.LABEL_ARTICLE,"Article")
    }

    fun getImageLabel():String{
        return readFromPreferences(ParameterConstants.LABEL_IMAGE,"Images")
    }
}