package com.itek.rftaar.core.common.utils

import android.annotation.SuppressLint
import android.content.Context
import android.icu.util.Calendar
import com.google.gson.Gson
import com.itek.rftaar.api.constants.ParameterConstants
import com.itek.rftaar.core.common.constants.DateFormatUtils
import com.itek.rftaar.core.common.constants.DateFormatUtils.API_DATE_TIME_FORMAT
import com.itek.rftaar.core.common.constants.DateFormatUtils.DISPLAY_DATE_TIME_FORMAT
import com.itek.rftaar.core.common.constants.LoginConstants
import com.itek.rftaar.core.common.constants.MenuConstants
import com.itek.rftaar.core.common.utils.ParseUtils.extractBoolean
import com.itek.rftaar.core.common.utils.ParseUtils.extractJSONArray
import com.itek.rftaar.core.common.utils.ParseUtils.extractJSONObject
import com.itek.rftaar.core.common.utils.ParseUtils.extractString
import com.itek.rftaar.core.database.AppDatabase
import com.itek.rftaar.core.database.DataStoreManager
import com.itek.rftaar.data.entity.InOutConfigEntity
import com.itek.rftaar.data.entity.MenuEntity
import com.itek.rftaar.data.entity.MenuNotificationEntity
import com.itek.rftaar.mqtt.MqttManager.showLog
import com.itek.rftaar.mqtt.constants.TopicConstants
import com.itek.rftaar.utils.CommonUtils.chkNull
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat

@SuppressLint("StaticFieldLeak")
object JsonUtils {

    private lateinit var context: Context
    private const val TAG = "ParseConfig"

    fun init(context: Context) {
        this.context = context
    }
    /**
     * Get sample json json object.
     *
     * @param context  the context
     * @param fileName the file name
     * @return the json object
     */
    fun getSampleJSON(fileName: String,context: Context= JsonUtils.context): JSONObject {
        if(context==null || fileName.isNullOrEmpty()) return JSONObject()
        try {
            LogUtils.showLog("fileNameJson",fileName.replace("/","_") + ".json")
            context.getAssets().open(fileName.replace("/","_") + ".json").use { stream ->
                val size = stream.available()
                val buffer = ByteArray(size)
                stream.read(buffer)
                stream.close()
                return JSONObject(String(buffer))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return JSONObject()
    }

    suspend fun parseNotification(response: JSONObject){
        LogUtils.showLog("parseNotification",response.toString())
        try{
            val menuNotification = Gson().fromJson(response.toString(), MenuNotificationEntity::class.java)
            menuNotification.typeId=menuNotification.typeId.uppercase().replaceFirst(Regex("(ROOT_|CHILD_)"),"")
            if(menuNotification!=null && menuNotification.date.isNotEmpty()){
                val cc = Calendar.getInstance()
                try {
                    cc.time = SimpleDateFormat(DISPLAY_DATE_TIME_FORMAT).parse(menuNotification.date)
                }catch (e: Exception){
                    //e.printStackTrace()
                    cc.time = SimpleDateFormat("dd-MMM-yyyy HH:mm").parse(menuNotification.date)
                }
                menuNotification.date=DateFormatUtils.formatToDisplayTime(cc.time,API_DATE_TIME_FORMAT)
                cc.add(Calendar.HOUR_OF_DAY,chkNull(menuNotification.validTill,24))
                menuNotification.validTillDate=DateFormatUtils.formatToDisplayTime(cc.time,API_DATE_TIME_FORMAT)
                menuNotification.userId= DataStoreManager.readFromPreferences(ParameterConstants.USER_ID,"")
                LogUtils.showLog("menuNotification",menuNotification.toString())
                val menuNotificationDao = AppDatabase.getDbInstance(context).menuNotificationDao()
                showLog("getCurrentAPITime",DateFormatUtils.getCurrentAPITime())
                menuNotificationDao.deleteExpired(DateFormatUtils.getCurrentAPITime())
                menuNotificationDao.insert(menuNotification)
            }
        }catch (e: Exception) {e.printStackTrace()}
    }

    suspend fun parseMenus(response: JSONObject){
        LogUtils.showLog("parseMenus", "Response → $response")
        val menuArray = extractJSONArray(response,ParameterConstants.DATA, JSONArray())
        LogUtils.showLog("parseMenus_menuArray", "menuArray → $menuArray")
        val listMenus = ArrayList<MenuEntity>()
        if(menuArray!=null && menuArray.length()>0) {
            for (i in 0 until menuArray.length()) {
                val menuJson = menuArray.optJSONObject(i) ?: continue
                val menu = Gson().fromJson(menuJson.toString(), MenuEntity::class.java)
                LogUtils.showLog("parseMenus_menu", "menu → $menu")
                if(!validateMenu(menu)) continue
                LogUtils.showLog("parseMenus_menu1", "validate_menu → $menu")
                //TEMP CODE CONDITIONenableCustomInventory_
                //if(listMenus.filter { m -> m.code == menu.code }.firstOrNull()!=null) continue
                if (menu.path.isNullOrEmpty()) {
                 if (menu.parentCode.isNullOrEmpty()) menu.path = menu.code
                 else {
                   val parentMenu = listMenus.filter { m -> m.code == menu.parentCode }.firstOrNull()
                   if (parentMenu != null && parentMenu.path.isNotEmpty()) menu.path = parentMenu.path + "/" + menu.code
                 }
                }
                listMenus.add(menu)
                LogUtils.showLog("parseMenus_menu2", "listMenus → ${listMenus.size}")

            }
        }
        if(listMenus.isNotEmpty()) {
            updatePaths(listMenus)
            LogUtils.showLog("parseMenus_listMenus", "listMenus → $listMenus")
            val menuDao = AppDatabase.getDbInstance(context).menuDao()
            menuDao.deleteAll()
            menuDao.insertAll(listMenus)
        }
    }

    fun validateMenu(menu: MenuEntity?): Boolean{
        if(menu==null || menu.code.isNullOrEmpty() || !menu.isActive) return false
        if(!menu.parentCode.isNullOrEmpty()) menu.parentCode = menu.parentCode.uppercase().replaceFirst(Regex("(ROOT_|CHILD_)"),"").trim()
        menu.code = menu.code.uppercase().replaceFirst(Regex("(ROOT_|CHILD_)"),"").trim()
        if(!MenuConstants.isValueInConstants(menu.code)) return false
        showLog("enableCustomInventory",""+DataStoreManager.readFromPreferences("enableCustomInventory",false))
        menu.hasDashboard = menu.code.matches(Regex("(?i)("+MenuConstants.INVENTORY+"|"+ MenuConstants.ENCODE+"|"+ MenuConstants.SEARCH+")"))
        LogUtils.showLog("enableCustomInventory_"+menu.code,""+DataStoreManager.readFromPreferences("enableCustomInventory",false))
        if(menu.code.contains("CUSTOM",true) && menu.code.contains("INVENTORY",true) && !DataStoreManager.readFromPreferences("enableCustomInventory",false)) return false
        if(menu.code.contains("REPLENISH",true) && !DataStoreManager.readFromPreferences("enableReplenishment",false)) return false
        if(menu.code.contains("ALIEN",true) && (!DataStoreManager.readFromPreferences("enableAlienTags",false) || !DataStoreManager.readFromPreferences("enableVendorSerialCode",false) || DataStoreManager.readFromPreferences("vendorSerialCode","").isNullOrEmpty())) return false
        return true
    }

    fun setMenuPath(menu: MenuEntity,listMenus: ArrayList<MenuEntity>){
        if (menu.path.isNullOrEmpty()) {
            if (menu.parentCode.isNullOrEmpty()) menu.path = menu.code
            else {
                val parentMenu = listMenus.filter { m -> m.code == menu.parentCode }.first()
                if (parentMenu != null) {
                    if (parentMenu.path.isNotEmpty()) menu.path = parentMenu.path + "/" + menu.code
                    else if (parentMenu.path.isEmpty()) menu.path = parentMenu.code + "/" + menu.code
                }
            }
        }
    }

    fun updatePaths(listMenus: ArrayList<MenuEntity>) {
        for(menu in listMenus){
            if(menu.code.isNotEmpty() && menu.isEnabled && menu.path.isNullOrEmpty())
              updatePath(listMenus,menu)
        }
        //LogUtils.showLog("updatePaths_listMenus", "listMenus → $listMenus")
    }

    fun updatePath(listMenus: ArrayList<MenuEntity>, menu: MenuEntity):String{
        //LogUtils.showLog("updatePath",menu.code)
        val parentMenu = listMenus.filter { m -> m.code == menu.parentCode }.firstOrNull()

        if (parentMenu != null) {
            //LogUtils.showLog("updatePath_parentMenu",parentMenu.code+"->"+parentMenu.path)
            if (parentMenu.path.isNotEmpty()) menu.path = parentMenu.path + "/" + menu.code
            else if (parentMenu.path.isEmpty()) menu.path = updatePath(listMenus,parentMenu) + "/" + menu.code
        }
        //LogUtils.showLog("updatedPath",menu.code+"->"+chkNull(menu.path,menu.code))
        return menu.path
    }

    suspend fun parseLocationConfig(response: JSONObject){
        val dataObj = extractJSONObject(response,ParameterConstants.DATA,response)
        val businessLineId = extractString(dataObj, ParameterConstants.BUSINESS_LINE_ID, extractString(dataObj, ParameterConstants.BUSINESS_LINE_ID1,""))
        val organizationId= extractString(dataObj, ParameterConstants.ORGANIZATION_ID,"")
        val groupId = extractString(dataObj, ParameterConstants.GROUP_ID,"")
        val deviceLocationId = extractString(dataObj, ParameterConstants.ID,"")
        val deviceLocationCode = extractString(dataObj, ParameterConstants.CODE,"")
        val deviceLocationName = extractString(dataObj, ParameterConstants.NAME,"")
        val timezone = extractString(dataObj, ParameterConstants.TIME_ZONE,"")

        val configurationObj = dataObj.optJSONObject(ParameterConstants.CONFIGURATION)
        if (configurationObj != null) {
            //DataStoreManager.saveToPreferences(ParameterConstants.CUSTOMER_ID, customerId)
            DataStoreManager.saveToPreferences(LoginConstants.DEVICE_LOCATION_ID, deviceLocationId)
            DataStoreManager.saveToPreferences(LoginConstants.DEVICE_LOCATION_CODE, deviceLocationCode)
            DataStoreManager.saveToPreferences(LoginConstants.DEVICE_LOCATION_NAME, deviceLocationName)
            DataStoreManager.saveToPreferences(ParameterConstants.BUSINESS_LINE_ID, businessLineId)
            DataStoreManager.saveToPreferences(ParameterConstants.TIME_ZONE, timezone)
            DataStoreManager.saveToPreferences(ParameterConstants.ORGANIZATION_ID, organizationId)
            DataStoreManager.saveToPreferences(ParameterConstants.GROUP_ID, groupId)
            LogUtils.showLog("insertMenu", "✔ configuration found → parsing")
            parseConfig(configurationObj)  // Pass the configuration object directly
        }
    }
    suspend fun parseConfig(configurationObj: JSONObject) {
     //CoroutineScope(Dispatchers.IO).launch {
         val listMenus = ArrayList<MenuEntity>()
         LogUtils.showLog("configurationObj",configurationObj.toString())

         var moduleIndex = 0
         val modules = configurationObj.keys()
         while (modules.hasNext()) {
             val moduleName = modules.next()
             moduleIndex++;
             val moduleObj = configurationObj.optJSONObject(moduleName) ?: continue
             if (moduleName.equals(ParameterConstants.TAG_PASSWORDS, true)) {
                 val currentPasswordObj = extractJSONObject(moduleObj, ParameterConstants.CURRENT_PASSWORD, JSONObject())
                 val currentPassword = currentPasswordObj.optString(ParameterConstants.VALUE)
                 val oldPasswordsObj = extractJSONObject(moduleObj, ParameterConstants.OLD_PASSWORDS, JSONObject())
                 val oldPasswordArray = extractJSONArray(oldPasswordsObj, ParameterConstants.VALUE, JSONArray())
                 val oldPasswords = mutableListOf<String>()
                 for (i in 0 until oldPasswordArray.length()) {
                     oldPasswords.add(oldPasswordArray.optString(i))
                 }
                 DataStoreManager.savePasswords(currentPassword, oldPasswords)
             }

             val isEnabled = extractBoolean(extractJSONObject(moduleObj, ParameterConstants.IS_ENABLED, extractJSONObject(moduleObj, ParameterConstants.ENABLED, JSONObject())), ParameterConstants.VALUE, false)// || BaseUtils.isDebuggable()
             if (!isEnabled) continue

             if (moduleName.equals("encoding", true)) {
                 LogUtils.showLog("moduleName",moduleName)
                 val enableOfflineEncoding = extractBoolean(extractJSONObject(moduleObj, "enableOfflineEncoding", JSONObject()), ParameterConstants.VALUE, false)
                 val enableReEncoding = extractBoolean(extractJSONObject(moduleObj, "enableReEncoding", JSONObject()), ParameterConstants.VALUE, false)
                 val enableVendorSerialCode = extractBoolean(extractJSONObject(moduleObj, "enableVendorSerialCode", JSONObject()), ParameterConstants.VALUE, false)
                 val vendorSerialCode = extractString(extractJSONObject(moduleObj, "vendorSerialCode", JSONObject()), ParameterConstants.VALUE, "")
                 val encodeAlgorithms = extractJSONObject(extractJSONObject(moduleObj, "encodeAlgorithms", JSONObject()),ParameterConstants.VALUE, JSONObject())

                 //save to DataStore
                 DataStoreManager.saveToPreferences("enableOfflineEncoding", enableOfflineEncoding)
                 DataStoreManager.saveToPreferences("enableReEncoding", enableReEncoding)
                 DataStoreManager.saveToPreferences("enableVendorSerialCode", enableVendorSerialCode)
                 DataStoreManager.saveToPreferences("vendorSerialCode", vendorSerialCode)
                 DataStoreManager.saveToPreferences(ParameterConstants.STANDARD_ALGORITHM, extractString(encodeAlgorithms, ParameterConstants.STANDARD_ALGORITHM, ""))
                 DataStoreManager.saveToPreferences(ParameterConstants.NON_STANDARD_ALGORITHM, extractString(encodeAlgorithms, ParameterConstants.NON_STANDARD_ALGORITHM, ""))

             }
             if (moduleName.equals("decode", true)) {
                 LogUtils.showLog("moduleName",moduleName)
                 val notificationOnDecode = extractBoolean(extractJSONObject(moduleObj, "notificationOnDecode", JSONObject()), ParameterConstants.VALUE,false)
                 val decodeBits = extractJSONObject(moduleObj, "decodeBits", JSONObject())
                 val decodeBitValue = extractString(decodeBits, ParameterConstants.VALUE, "0")

                 //save to DataStore (Only single bit can be changed)
                 DataStoreManager.saveToPreferences("decodeBits", if (decodeBitValue.length > 1) decodeBitValue.substring(0, 1) else decodeBitValue)
                 DataStoreManager.saveToPreferences("notificationOnDecode", notificationOnDecode)
             }
             if (moduleName.equals("inventory", true)) {
                 LogUtils.showLog("moduleName",moduleName)
                 val enableAlienTags = extractJSONObject(moduleObj, "enableAlienTags", JSONObject())
                 val enableCustomInventory = extractJSONObject(moduleObj, "enableCustomInventory", JSONObject())
                 val customInventoryParam = extractJSONObject(moduleObj, "customInventoryParam", JSONObject())
                 val customInventoryAdvanceFilters = extractJSONArray(extractJSONObject(moduleObj, "customInventoryAdvanceFilters", JSONObject()),ParameterConstants.VALUE, JSONArray())
                 val customInvAdvancedFilters = mutableListOf<String>()
                 for (i in 0 until customInventoryAdvanceFilters.length()) {
                     customInvAdvancedFilters.add(customInventoryAdvanceFilters.optString(i))
                 }
                 //save to DataStore
                 DataStoreManager.saveToPreferences("enableAlienTags", extractBoolean(enableAlienTags, ParameterConstants.VALUE, false))
                 DataStoreManager.saveToPreferences("enableCustomInventory", extractBoolean(enableCustomInventory, ParameterConstants.VALUE, false))
                 LogUtils.showLog("enableCustomInventory_",""+DataStoreManager.readFromPreferences("enableCustomInventory",false))
                 DataStoreManager.saveToPreferences("customInventoryParam", extractString(customInventoryParam, ParameterConstants.VALUE, ""))
                 DataStoreManager.saveListStr("customInventoryAdvanceFilters",customInvAdvancedFilters)
                 showLog("enableCustomInventory11",""+DataStoreManager.readFromPreferences("enableCustomInventory",false))
             }
             if (moduleName.equals("movement", true)) {
                     LogUtils.showLog("moduleName",moduleName)
                     val enableReplenishment = extractJSONObject(moduleObj, "enableReplenishment", JSONObject())
                     val replenishmentType = extractJSONObject(moduleObj, "replenishmentType", JSONObject())
                     //save to DataStore
                     DataStoreManager.saveToPreferences("enableReplenishment", extractBoolean(enableReplenishment, ParameterConstants.VALUE, false))
                     DataStoreManager.saveToPreferences("replenishmentType", extractString(replenishmentType, ParameterConstants.VALUE, ""))
             }
             if (moduleName.equals("inward", true) || moduleName.equals("outward", true)) {
                 LogUtils.showLog("moduleName",moduleName)
                 val topic = if (moduleName.equals("inward", true)) TopicConstants.INWARD else TopicConstants.OUTWARD
                 val hirarchies = extractJSONObject(moduleObj, "hierarchies", JSONObject())
                 val inwConfigVals = extractJSONArray(hirarchies, ParameterConstants.VALUE, JSONArray())
                 //save in table
                 val listInwConfig = ArrayList<InOutConfigEntity>(0);
                 for (i in 0 until inwConfigVals.length()) {
                     val inwConfig: JSONObject = inwConfigVals.optJSONObject(i)
                     val groupId = extractString(inwConfig, ParameterConstants.GROUP_ID, "")
                     if (!groupId.equals(DataStoreManager.readFromPreferences(ParameterConstants.GROUP_ID, ""))) continue;
                     val depth = ParseUtils.extractInt(inwConfig, "depth", 0)
                     if (depth <= 0) continue
                     val ioConfig = Gson().fromJson(inwConfig.toString(), InOutConfigEntity::class.java)
                     val id = extractString(inwConfig, ParameterConstants.GROUP_ID, "")
                     val hierarchyFor = extractString(inwConfig, "hierarchyFor", "")
                     val hierarchyName = extractString(inwConfig, "hierarchyName", "")
                     val levels = extractString(inwConfig, "levels", "")
                     val rules = extractString(inwConfig, "rules", "")
                     ioConfig.topic = topic
                     ioConfig.levelsJArray = levels
                     ioConfig.rulesJObj = rules
                     listInwConfig.add(ioConfig)
                 }
                 if (listInwConfig.isNotEmpty()) {
                     LogUtils.showLog("listInwConfig", "parseConfig result: ${listInwConfig.size} configurations")
                     if (listInwConfig.isNotEmpty())
                         AppDatabase.getDbInstance(context).inOutConfigDao().insertAll(listInwConfig)
                 }
             }

             //Commented code for Config Menus
             /*if(false && BaseUtils.isDebuggable()) {
                 val deviceMenuObj = moduleObj.optJSONObject(ParameterConstants.DEVICE_MENU) ?: continue
                 val menuArray = deviceMenuObj.optJSONArray(ParameterConstants.VALUE) ?: continue

                 for (i in 0 until menuArray.length()) {
                     val menuJson = menuArray.optJSONObject(i) ?: continue
                     val menu = Gson().fromJson(menuJson.toString(), MenuEntity::class.java)
                     if(!menu.isActive) menu.isActive = true
                     if(!menu.isEnabled) menu.isEnabled = true
                     if(!validateMenu(menu)) continue
                     if(listMenus.filter { m -> m.code == menu.code }.firstOrNull()!=null) continue
                     if (menu.path.isNullOrEmpty()) {
                      if (menu.parentCode.isNullOrEmpty()) menu.path = menu.code
                      else {
                                 val parentMenu = listMenus.filter { m -> m.code == menu.parentCode }.first()
                                 if (parentMenu != null) {
                                     if (parentMenu.path.isNotEmpty()) menu.path =
                                         parentMenu.path + "/" + menu.code
                                     else if (parentMenu.path.isEmpty()) menu.path =
                                         parentMenu.code + "/" + menu.code
                                 }
                           }
                     }
                     listMenus.add(menu)
                 }
             }*/
         }

         //Temp Static Menu  (Associated Barcode) (Commented)
         /*if (false && BaseUtils.isDebuggable()) {
             try {
                 //val mapBarcodeJson = "{\"code\": \"ROOT_MENU_ASSOCIATE_BARCODE\",\"label\": \"Map Barcode\",\"isActive\": false,\"enabled\": false,\"icon\": null,\"sequence\": 1,\"deviceTypes\": [],\"parentCode\": \"ROOT_MENU_ENCODE\" }"
                 val mapBarcodeJson = JSONObject()
                 mapBarcodeJson.put(ParameterConstants.CODE, MenuConstants.ASSOCIATE_BARCODE)
                 mapBarcodeJson.put(ParameterConstants.LABEL, "Map Barcode")
                 mapBarcodeJson.put(ParameterConstants.IS_ENABLED, true)
                 mapBarcodeJson.put(ParameterConstants.IS_ACTIVE, true)
                 mapBarcodeJson.put(ParameterConstants.SEQUENCE, moduleIndex + 1)
                 mapBarcodeJson.put(ParameterConstants.ICON, null)
                 mapBarcodeJson.put(ParameterConstants.PARENT_CODE, null)
                 val mapBarcodeMenu = Gson().fromJson(mapBarcodeJson.toString(), MenuEntity::class.java)
                 listMenus.add(mapBarcodeMenu)
             } catch (e: Exception) {
                 e.printStackTrace()
             }
         }*/

        //Temp code for adding the return menu (Commented)
        /*if (false && BaseUtils.isDebuggable()) {
             try {
                 //val mapReturnJson = "{\"code\": \"ROOT_MENU_ASSOCIATE_BARCODE\",\"label\": \"Map Barcode\",\"isActive\": false,\"enabled\": false,\"icon\": null,\"sequence\": 1,\"deviceTypes\": [],\"parentCode\": \"ROOT_MENU_ENCODE\" }"
                 val maReturnJson = JSONObject()
                 maReturnJson.put(ParameterConstants.CODE, MenuConstants.RETURN)
                 maReturnJson.put(ParameterConstants.LABEL, "Return")
                 maReturnJson.put(ParameterConstants.IS_ENABLED, true)
                 maReturnJson.put(ParameterConstants.IS_ACTIVE, true)
                 maReturnJson.put(ParameterConstants.SEQUENCE, moduleIndex + 1)
                 maReturnJson.put(ParameterConstants.ICON, null)
                 maReturnJson.put(ParameterConstants.PARENT_CODE, null)
                 val mapReturnMenu = Gson().fromJson(maReturnJson.toString(), MenuEntity::class.java)
                 listMenus.add(mapReturnMenu)
             } catch (e: Exception) {
                 e.printStackTrace()
             }
         }*/

        //Commented code for Config Menus
         //LogUtils.showLog("listMenus", "parseConfig result: ${listMenus.size} menus")
         /*if (false && listMenus.isNotEmpty()) {//Temp Code
             val menuDao = AppDatabase.getDbInstance(context).menuDao()
             menuDao.deleteAll()
             menuDao.insertAll(listMenus)
         }*/
     //}
    }
}