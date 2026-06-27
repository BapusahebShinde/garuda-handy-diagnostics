package com.itek.rftaar.mqtt

import android.annotation.SuppressLint
import android.content.Context
import com.itek.rftaar.DataHolder
import com.itek.rftaar.api.constants.ParameterConstants
import com.itek.rftaar.api.constants.UrlConstants
import com.itek.rftaar.core.common.constants.DateFormatUtils
import com.itek.rftaar.core.common.constants.DateFormatUtils.formatToUTCTime
import com.itek.rftaar.core.common.constants.LoginConstants
import com.itek.rftaar.core.common.utils.FileUtils
import com.itek.rftaar.core.common.utils.JsonUtils
import com.itek.rftaar.core.common.utils.LogUtils
import com.itek.rftaar.core.common.utils.ParseUtils.extractBoolean
import com.itek.rftaar.core.common.utils.ParseUtils.extractInt
import com.itek.rftaar.core.common.utils.ParseUtils.extractJSONArray
import com.itek.rftaar.core.common.utils.ParseUtils.extractString
import com.itek.rftaar.core.common.utils.ParseUtils.hasKeys
import com.itek.rftaar.core.database.AppDatabase
import com.itek.rftaar.core.database.DataStoreManager
import com.itek.rftaar.data.dao.TagInfoDao
import com.itek.rftaar.data.entity.SearchLogEntity
import com.itek.rftaar.data.entity.TagInfoEntity
import com.itek.rftaar.diagnostics.MqttDiagnosticTracker
import com.itek.rftaar.domain.APIBackGroundCall
import com.itek.rftaar.mqtt.constants.TopicConstants
import com.itek.rftaar.presentation.navigation.Screen
import com.itek.rftaar.reader.constants.StatusConstants
import com.itek.rftaar.reader.epcwrapper.constants.BarcodeConstants
import com.itek.rftaar.utils.CommonUtils.chkNull
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.json.JSONArray
import org.json.JSONObject

object MqttManager {

  private lateinit var mqttClient: MqttAsyncClient
  private lateinit var appContext: Context
  private var isConnecting = false

  private val _isConnected = MutableStateFlow(false)

  private val _connectionStatus = MutableStateFlow("Disconnected")
  val connectionStatus: StateFlow<String> = _connectionStatus

  val topicItems = ArrayList<String>(emptyList())
  val subscribedTopics = ArrayList<String>(DataStoreManager.getListStr("subscribedTopics"))
  var receiverRegistered = false
  //var serverUrl = ""

 // private var context: Context? = null

  fun isInitialized(): Boolean = this::mqttClient.isInitialized

  fun initialize(context: Context, serverUrl: String, isSettingServerUrl: Boolean=false) {
    //MqttManager.context = context
    if(serverUrl.trim().isNullOrEmpty()) return
    appContext = context
    val isSameBroker = serverUrl.trim().length>0 && serverUrl.trim().equals(DataStoreManager.readFromPreferences(ParameterConstants.BROKER_URL,""))
    val isInit = isInitialized()
    showToast("isInitialized:"+isInit)
    if(!isSettingServerUrl && isInit && isSameBroker) return
    /*if(serverUrl.isNotEmpty() && !serverUrl.equals(DataStoreManager.readFromPreferences(ParameterConstants.BROKER_URL,""),true))
      this.serverUrl= serverUrl*/
    //val pingSender = SafeAlarmPingSender(appContext)
    val clientId = MqttAsyncClient.generateClientId()
    try {
      //mqttClient = MqttAsyncClient(serverUrl.trim(), clientId, MemoryPersistence(), pingSender)
      mqttClient = MqttAsyncClient(serverUrl.trim(), clientId, MemoryPersistence())
      MqttDiagnosticTracker.brokerHost = serverUrl.trim()
      MqttDiagnosticTracker.clientId = clientId
    }
    catch (e: Exception) {
      e.printStackTrace()
      _connectionStatus.value = chkNull(e.message.toString(),"Failed to init MQTT Broker")
      return
    }

    setCallback()
    setupTopics() // Setup topics
    connect()
  }

  private fun setupTopics() {
     try {
         topicItems.clear()
         topicItems.add(TopicConstants.ENCODE)
         topicItems.add(TopicConstants.DECODE)
         topicItems.add(TopicConstants.INVENTORY)
         topicItems.add(TopicConstants.SEARCH)
         topicItems.add(TopicConstants.MOVEMENT)
         topicItems.add(TopicConstants.REPLENISHMENT)
         topicItems.add(TopicConstants.SEARCH_LIST_UPDATE)
         if (DataStoreManager.readFromPreferences(LoginConstants.IS_LOGGED_IN, false)) addAfterLoginTopics()
     }catch (e: Exception) {e.printStackTrace()}
  }

  fun subscribeAfterLogin(): Boolean{
      //val afterLoginTopics= addAfterLoginTopics()
      //for(topic in afterLoginTopics) subscribe(topic)
      subscribe(addAfterLoginTopics())
      showLog("topicItems",topicItems.toString())
      showLog("subscribedTopics",subscribedTopics.toString())
      if(topicItems.size == subscribedTopics.size) {
       DataStoreManager.saveListStr("subscribedTopics",subscribedTopics)
       return true;
      }
      return false;
  }

  fun unsubscribeAfterLogout(){
      val afterLoginTopics= addAfterLoginTopics()
      //for(topic in afterLoginTopics) unsubscribe(topic)
      unsubscribe(afterLoginTopics)
      subscribedTopics.removeAll(afterLoginTopics)
      DataStoreManager.saveListStr("subscribedTopics",subscribedTopics)
  }

  private fun addAfterLoginTopics():List<String>{
    val listAfterLoginTopics =  ArrayList<String>()
    //val devicePkId = DataStoreManager.readFromPreferences(LoginConstants.DEVICE_PK_ID,"")
    val userId = DataStoreManager.readFromPreferences(ParameterConstants.USER_ID,"")
    val deviceId = DataStoreManager.readFromPreferences(ParameterConstants.DEVICE_ID,"")
    val deviceLocationId = DataStoreManager.readFromPreferences(LoginConstants.DEVICE_LOCATION_ID,"")
    val businessLineId = DataStoreManager.readFromPreferences(ParameterConstants.BUSINESS_LINE_ID,"")

    showLog("userUpdateTopic",TopicConstants.USER_UPDATE+userId)
    showLog("deviceProvisionConfigTopic",TopicConstants.DEVICE_PROVISION+deviceId)
    showLog("locationConfigTopic",TopicConstants.UPDATE_LOCATION_CONFIG+deviceLocationId)
    showLog("notifyTopic",TopicConstants.NOTIFY+deviceLocationId)
    showLog("deviceMenuTopic",TopicConstants.DEVICE_MENU+businessLineId)
    /*if(devicePkId.isNotEmpty())  {
        topicItems.add(TopicConstants.DEVICE_PROVISION+devicePkId)
        listAfterLoginTopics.add(TopicConstants.DEVICE_PROVISION+devicePkId)
    }*/
    if(userId.isNotEmpty())  {
        topicItems.add(TopicConstants.USER_UPDATE+userId)
        listAfterLoginTopics.add(TopicConstants.USER_UPDATE+userId)
    }
    if(deviceId.isNotEmpty())  {
        topicItems.add(TopicConstants.DEVICE_PROVISION+deviceId)
        listAfterLoginTopics.add(TopicConstants.DEVICE_PROVISION+deviceId)
    }
    if(deviceLocationId.isNotEmpty()) {
        topicItems.add(TopicConstants.UPDATE_LOCATION_CONFIG+deviceLocationId)
        topicItems.add(TopicConstants.NOTIFY+deviceLocationId)
        listAfterLoginTopics.add(TopicConstants.UPDATE_LOCATION_CONFIG+deviceLocationId)
        listAfterLoginTopics.add(TopicConstants.NOTIFY+deviceLocationId)
    }
    if(businessLineId.isNotEmpty()) {
        topicItems.add(TopicConstants.DEVICE_MENU+businessLineId)
        listAfterLoginTopics.add(TopicConstants.DEVICE_MENU+businessLineId)
    }
    return listAfterLoginTopics
  }

  fun addTopic(topic:String) {
      if(topicItems.isEmpty()){
          topicItems.add(TopicConstants.ENCODE)
          topicItems.add(TopicConstants.DECODE)
          topicItems.add(TopicConstants.INVENTORY)
          topicItems.add(TopicConstants.SEARCH)
          topicItems.add(TopicConstants.MOVEMENT)
          topicItems.add(TopicConstants.REPLENISHMENT)
          topicItems.add(TopicConstants.SEARCH_LIST_UPDATE)
          if(DataStoreManager.readFromPreferences(LoginConstants.IS_LOGGED_IN,false)) addAfterLoginTopics()
      }
      if(!topicItems.contains(topic))
       topicItems.add(topic)
  }

  fun getTopicForPublish(topic:String):String  {
   return if(topicItems.isNotEmpty() && topicItems.contains(topic)) topic else ""
  }

  fun getEncodeTopic(): String {
   return getTopicForPublish(TopicConstants.ENCODE)
    //return topicItems.stream().filter { it -> it.matches(Regex(".*" + TopicConstants.ENCODE)) }.findFirst().get()
  }

  fun getDecodeTopic(): String {
    return getTopicForPublish(TopicConstants.DECODE)
    //return topicItems.stream().filter { it -> it.matches(Regex(".*" + TopicConstants.DECODE)) }.findFirst().get()
  }

  fun getInventoryTopic(): String {
    return getTopicForPublish(TopicConstants.INVENTORY)
    //return topicItems.stream().filter { it -> it.matches(Regex(".*" + TopicConstants.INVENTORY)) }.findFirst().get()
  }



  fun getSearchTopic(): String {
    return getTopicForPublish(TopicConstants.SEARCH)
    //return topicItems.stream().filter { it -> it.matches(Regex(".*" + TopicConstants.SEARCH)) }.findFirst().get()
  }

  fun getMovementTopic(): String {
    return getTopicForPublish(TopicConstants.MOVEMENT)
    //return topicItems.stream().filter { it -> it.matches(Regex(".*" + TopicConstants.MOVEMENT)) }.findFirst().get()
  }

  fun getReplenishmentTopic(): String {
    return getTopicForPublish(TopicConstants.REPLENISHMENT)
    //return topicItems.stream().filter { it -> it.matches(Regex(".*" + TopicConstants.REPLENISHMENT)) }.findFirst().get()
  }

  fun getSearchListUpdateTopic(): String {
    return getTopicForPublish(TopicConstants.SEARCH_LIST_UPDATE)
    //return topicItems.stream().filter { it -> it.matches(Regex(".*" + TopicConstants.SEARCH_LIST_UPDATE)) }.findFirst().get()
  }


/*  fun isConnected(): Boolean {
    return _isConnected.value
  }*/

  fun isConnected(): Boolean {
    return if (this::mqttClient.isInitialized) mqttClient.isConnected else false
  }


  fun connect(isSameBroker: Boolean = true) {
    //if (!MqttManager::mqttClient.isInitialized) return
    //if (!this::mqttClient.isInitialized) return
    if(!isInitialized()) { initialize(appContext, DataStoreManager.readFromPreferences(ParameterConstants.BROKER_URL,"")); return}
    GlobalScope.launch(Dispatchers.IO) {
          if (mqttClient.isConnected) return@launch
          if (isConnecting) return@launch   //prevents "connect already in progress"

          isConnecting = true

          val options = MqttConnectOptions().apply {
              isCleanSession = true
              keepAliveInterval = 60
              connectionTimeout = 30
              isAutomaticReconnect = true
              maxInflight = 10
              //socketFactory = SSLSocketFactory.getDefault()
              //socketFactory = SocketFactory.getDefault()
          }
          mqttClient.connect(options, null, object : IMqttActionListener {
              override fun onSuccess(asyncActionToken: IMqttToken?) {
                  isConnecting = false
                  _isConnected.value = true
                  _connectionStatus.value = "Connected"
                  MqttDiagnosticTracker.onConnected(_connectionStatus.value, mqttClient.serverURI ?: "")
                  /*if(serverUrl.isNotEmpty()) {
          DataStoreManager.saveToPreferences(ParameterConstants.BROKER_URL,serverUrl)
          //showSuccess
        }*/
                  FileUtils.writeMqttLog(
                      serverUrl = mqttClient.serverURI,
                      topic = "Connect",
                      message = "Connected successfully:-${_connectionStatus.value}"
                  )
                  showToast("Connected")
                  //setCallback()
                  /*if(!isSameBroker) */subscribeAll()
              }

              override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                  isConnecting = false
                  _isConnected.value = false
                  _connectionStatus.value = "Failed to connect"
                  MqttDiagnosticTracker.onDisconnected(_connectionStatus.value, exception?.message)
                  /*if(serverUrl.isNotEmpty()) {
          //showFail
        }*/
                  exception?.printStackTrace()
                  FileUtils.writeMqttLog(
                      serverUrl = mqttClient.serverURI,
                      topic = "Connect",
                      message = "Connection failed: ${exception?.message}"
                  )
                  showLog("MQTT", "Connection failed: ${exception?.message}")
              }
          })
      }
  }

  private fun setCallback() {
    mqttClient.setCallback(object : MqttCallback {
      override fun connectionLost(cause: Throwable?) {
        _isConnected.value = false
        _connectionStatus.value = "Connection lost"
        MqttDiagnosticTracker.onDisconnected(_connectionStatus.value, cause?.message)
        showToast("Connection lost")
        connect()
      }

      override fun messageArrived(topic1: String?, message: MqttMessage?) {
        MqttDiagnosticTracker.onMessageArrived()
        processMessageUpdate(topic1,message?.toString())
        /*showLog("MQTT_Published", "Message arrived on topic: $topic1")
        if(topic1.isNullOrEmpty()) return

        val payload = message?.toString() ?: return
        showLog("MQTT_Published", "Payload received: $payload")
        FileUtils.writeMqttLog(
          serverUrl = mqttClient.serverURI,
          topic = topic1+"_Arrived",
          message = payload
        )

        val topic = chkNull(topic1,"");
        LogUtils.showLog("topic",""+topic)
        CoroutineScope(Dispatchers.IO).launch {
            try {
              val tagInfoDao = AppDatabase.getDbInstance(appContext).tagInfoDao()
              val searchLogDao = AppDatabase.getDbInstance(appContext).searchLogDao()
              val jsonArray = if(payload.trim().startsWith("[") and payload.trim().endsWith("]")) JSONArray(payload) else null
              val jsonResponse = if(jsonArray!=null && jsonArray.length()>0 && jsonArray.get(0)!=null && jsonArray.get(0) is JSONObject) jsonArray.getJSONObject(0) else JSONObject(payload.trim().removeSurrounding("[", "]"))
              when(topic) {
                TopicConstants.USER_UPDATE+DataStoreManager.readFromPreferences(ParameterConstants.USER_ID,"") -> {
                    showLog(topic+"_Json",jsonResponse.toString())
                    // force logout if user is deactived or the user location is changed
                    val preUserCustomerId = DataStoreManager.readFromPreferences(LoginConstants.USER_CUSTOMER_ID, "")
                    val preUserRootLocationId = DataStoreManager.readFromPreferences(LoginConstants.USER_ROOT_LOCATION_ID, "")
                    val deviceCustomerId = DataStoreManager.readFromPreferences(LoginConstants.DEVICE_CUSTOMER_ID, "")
                    val deviceLocationId = DataStoreManager.readFromPreferences(LoginConstants.DEVICE_LOCATION_ID, "")
                    val deviceLocationPath = DataStoreManager.readFromPreferences(LoginConstants.DEVICE_LOCATION_PATH, "")
                    val isActive = extractBoolean(jsonResponse, ParameterConstants.IS_ACTIVE,false)
                    val userId = extractString(jsonResponse, ParameterConstants.USER_ID,extractString(jsonResponse, ParameterConstants.ID,""))
                    val userName = extractString(jsonResponse, ParameterConstants.USERNAME)
                    val userFirstName = extractString(jsonResponse, ParameterConstants.USER_FIRST_NAME, "")
                    val userLastName = extractString(jsonResponse, ParameterConstants.USER_LAST_NAME, "")
                    val userCustomerId = extractString(jsonResponse, ParameterConstants.CUSTOMER_ID)
                    val userRootLocationId = extractString(jsonResponse, ParameterConstants.LOCATION_ID)
                    val errMessage = extractString(jsonResponse, ParameterConstants.ERROR,extractString(jsonResponse, ParameterConstants.ERR_MSG, extractString(jsonResponse, ParameterConstants.MESSAGE,extractString(jsonResponse, ParameterConstants.ACTION, ""))))
                    DataStoreManager.saveToPreferences(ParameterConstants.USER_ID, userId)
                    DataStoreManager.saveToPreferences(ParameterConstants.USERNAME, userName)
                    DataStoreManager.saveToPreferences(ParameterConstants.USER_FIRST_NAME, userFirstName)
                    DataStoreManager.saveToPreferences(ParameterConstants.USER_LAST_NAME, userLastName)
                    DataStoreManager.saveToPreferences(LoginConstants.USER_CUSTOMER_ID, userCustomerId)
                    DataStoreManager.saveToPreferences(ParameterConstants.CUSTOMER_ID, userCustomerId)
                    DataStoreManager.saveToPreferences(LoginConstants.USER_ROOT_LOCATION_ID, userRootLocationId)
                    val isLoggedIn = DataStoreManager.readFromPreferences(LoginConstants.IS_LOGGED_IN,false)
                    if(isLoggedIn && (!isActive || userCustomerId.isNullOrEmpty() || userRootLocationId.isNullOrEmpty() || !deviceCustomerId.equals(userCustomerId, true) || !deviceLocationPath.contains(userRootLocationId) || !userCustomerId.equals(preUserCustomerId, true) || !userRootLocationId.equals(preUserRootLocationId,true))){
                        //force logout
                        val forceLogoutErrorMsg = chkNull(errMessage,"")
                        DataStoreManager.saveToPreferences("forceLogoutErrorMsg",forceLogoutErrorMsg)
                        DataHolder.isForceLogOut.value=true
                    }
                }
                TopicConstants.DEVICE_PROVISION+DataStoreManager.readFromPreferences(ParameterConstants.DEVICE_ID,"") ->{
                    showLog(topic+"_Json",jsonResponse.toString())
                    val preDeviceCustomerId = DataStoreManager.readFromPreferences(LoginConstants.DEVICE_CUSTOMER_ID, "")
                    val preDeviceLocationId = DataStoreManager.readFromPreferences(LoginConstants.DEVICE_LOCATION_ID, "")
                    val isUnProvisioned = extractBoolean(jsonResponse, ParameterConstants.IS_UN_PROVISIONED,false)
                    val isActive = extractBoolean(jsonResponse, ParameterConstants.IS_ACTIVE,false)
                    val deviceCustomerId = extractString(jsonResponse, ParameterConstants.CUSTOMER_ID, "")
                    val deviceLocationId = extractString(jsonResponse, ParameterConstants.LOCATION_ID, "")
                    val deviceLocationPath = extractString(jsonResponse, ParameterConstants.LOCATION_PATH, "")
                    val deviceLocationCode = extractString(jsonResponse, ParameterConstants.LOCATION_CODE, "")
                    val deviceLocationName = extractString(jsonResponse, ParameterConstants.LOCATION_NAME, "")
                    val errMessage = extractString(jsonResponse, ParameterConstants.ERROR,extractString(jsonResponse, ParameterConstants.ERR_MSG, extractString(jsonResponse, ParameterConstants.MESSAGE, "")))
                    DataStoreManager.saveToPreferences(LoginConstants.DEVICE_CUSTOMER_ID,if(isUnProvisioned)"" else deviceCustomerId)
                    DataStoreManager.saveToPreferences(LoginConstants.DEVICE_LOCATION_ID,if(isUnProvisioned)"" else deviceLocationId)
                    DataStoreManager.saveToPreferences(LoginConstants.DEVICE_LOCATION_PATH,if(isUnProvisioned)"" else deviceLocationPath)
                    DataStoreManager.saveToPreferences(LoginConstants.DEVICE_LOCATION_CODE, if(isUnProvisioned)"" else deviceLocationCode)
                    DataStoreManager.saveToPreferences(LoginConstants.DEVICE_LOCATION_NAME, if(isUnProvisioned)"" else deviceLocationName)

                    val isLoggedIn = DataStoreManager.readFromPreferences(LoginConstants.IS_LOGGED_IN,false)
                    val userCustomerId = DataStoreManager.readFromPreferences(LoginConstants.USER_CUSTOMER_ID,"")
                    val userRootLocationId =  DataStoreManager.readFromPreferences(LoginConstants.USER_ROOT_LOCATION_ID,"")
                    showLog("isLoggedIn",""+isLoggedIn)
                    if(isLoggedIn && (isUnProvisioned || !isActive || deviceCustomerId.isNullOrEmpty() || deviceLocationPath.isNullOrEmpty() || !deviceCustomerId.equals(userCustomerId, true) || !deviceLocationPath.contains(userRootLocationId) || !deviceCustomerId.equals(preDeviceCustomerId, true) || !deviceLocationId.equals(preDeviceLocationId,true))){
                        //force logout
                        val forceLogoutErrorMsg = chkNull(errMessage,"")
                        DataStoreManager.saveToPreferences("forceLogoutErrorMsg",forceLogoutErrorMsg)
                        DataHolder.isForceLogOut.value=true
                    }
                }
                TopicConstants.NOTIFY+DataStoreManager.readFromPreferences(LoginConstants.DEVICE_LOCATION_ID,"") -> {
                  showLog(topic+"_Json",jsonResponse.toString())
                  //Always parse & save notifications in a db table
                  JsonUtils.parseNotification(jsonResponse)
                  //Only Show notification if already logged In
                  if(DataStoreManager.readFromPreferences(LoginConstants.IS_LOGGED_IN,false)) {

                      val currentScreen = DataHolder.currentScreen
                      val isForeground = DataHolder.isAppInForeground

                      LogUtils.showLog("STATE", "Screen=$currentScreen Foreground=$isForeground")

                      val isRestrictedScreen =
                          currentScreen.contains(Screen.NotificationScreen.route) ||
                                  currentScreen.contains(Screen.NotificationDetailScreen.route) ||
                                  currentScreen.contains(Screen.UserDetails.route)


                      //Show Notification If App in background or Non Restricted Screen
                      if (!isForeground || !isRestrictedScreen)
                          NotificationHelper.showNotification(appContext, jsonResponse)
                  }
                }
                TopicConstants.UPDATE_LOCATION_CONFIG+DataStoreManager.readFromPreferences(LoginConstants.DEVICE_LOCATION_ID,"") -> {
                  showLog(topic+"_Json",jsonResponse.toString())
                  JsonUtils.parseLocationConfig(jsonResponse)
                  //call device menu api after every config change
                  callDeviceMenuAPI()
                }
                TopicConstants.DEVICE_MENU+DataStoreManager.readFromPreferences(ParameterConstants.BUSINESS_LINE_ID,"") -> {
                  showLog(topic+"_Json",jsonResponse.toString())
                  //call device menu api after menu change
                  callDeviceMenuAPI()
                }
                TopicConstants.INVENTORY -> {
                  val deviceId = extractString(jsonResponse, ParameterConstants.DEVICE_ID,"")
                  val userId = extractString(jsonResponse, ParameterConstants.USER_ID,"")
                  val userFullName = extractString(jsonResponse, ParameterConstants.USER_FIRST_NAME,"")+"_"+extractString(jsonResponse, ParameterConstants.USER_LAST_NAME,"")
                  //showLog("Inv_Upload_Json",jsonResponse.toString())
                  val sessionId = extractString(jsonResponse, ParameterConstants.SESSION_ID,extractString(jsonResponse, ParameterConstants.DEVICE_SESSION_ID, ""))
                  val transactionType = extractString(jsonResponse, ParameterConstants.TRANSACTION_TYPE, "")
                  val invMaxId = extractInt(jsonResponse, ParameterConstants.INV_MAX_ID, 0)
                  val encodedItemsArray = extractJSONArray(jsonResponse, ParameterConstants.ITEMS, JSONArray())
                  val unEncodedItemsArray = extractJSONArray(jsonResponse, ParameterConstants.NON_ENCODED_ITEMS, JSONArray())
                  val size = encodedItemsArray.length()+unEncodedItemsArray.length()
                  showLog("invMaxId",invMaxId.toString())
                  *//*val epcs = ArrayList<String>(0)
                  for (i in 0 until encodedItemsArray.length()) {
                    val item = encodedItemsArray.getJSONObject(i)
                    val epc = extractString(item, ParameterConstants.EPC, "")
                    val tid = extractString(item, ParameterConstants.TID, "")
                    if(epc.isNotEmpty())epcs.add(epc)
                    //topic,transactionType, epc, tid (get these from payload)
                    //showLog("Inv_Upload",topic+"_"+transactionType+"_"+sessionId+"_"+epc+"_"+tid)
                    //val result =tagInfoDao.updateUploadedInvForBackgroundUpload(topic, transactionType, epc, tid,sessionId)
                    //showLog("Inv_Upload_Result",""+result)
                  }
                  for (i in 0 until unEncodedItemsArray.length()) {
                    val item = unEncodedItemsArray.getJSONObject(i)
                    val epc = extractString(item, ParameterConstants.EPC, "")
                    val tid = extractString(item, ParameterConstants.TID, "")
                    if(epc.isNotEmpty())epcs.add(epc)
                    //topic,transactionType, epc, tid (get these from payload)
                    //showLog("Inv_Upload",topic+"_"+transactionType+"_"+sessionId+"_"+epc+"_"+tid)
                    //val result = tagInfoDao.updateUploadedInvForBackgroundUpload(topic,transactionType, epc, tid,sessionId)
                    //showLog("Inv_Upload_Result",""+result)
                  }
                    showLog("Inv_Upload_Result",""+epcs.size)
                    epcs.distinct().chunked(900).forEach { chunk ->
                    val result = tagInfoDao.updateUploadedInvForBackgroundUpload(topic,transactionType, chunk, sessionId)
                    showLog("Inv_Upload_Result",transactionType+"_"+sessionId+"_"+chunk.size+"->"+result)
                    }*//*
                  if(!deviceId.equals(DataStoreManager.readFromPreferences(ParameterConstants.DEVICE_ID,""),true)) {
                      //TODO check if sessionId is active
                     //TODO save to show group inventory
                  }
                  else{
                      showLog("Inv_Upload_Result",transactionType+"_"+sessionId+"->"+size)
                      val result = tagInfoDao.updateUploadedInvForBackgroundUpload(topic,transactionType,sessionId,invMaxId)
                      showLog("Inv_Upload_Result",transactionType+"_"+sessionId+"_"+size+"->"+result)
                  }
                }
                TopicConstants.MOVEMENT,TopicConstants.REPLENISHMENT -> {
                  val deviceId = extractString(jsonResponse, ParameterConstants.DEVICE_ID,"")
                  if(!deviceId.equals(DataStoreManager.readFromPreferences(ParameterConstants.DEVICE_ID,""),true)) return@launch
                  val sessionId = extractString(jsonResponse, ParameterConstants.SESSION_ID,extractString(jsonResponse, ParameterConstants.DEVICE_SESSION_ID, ""))
                  val transactionType = extractString(jsonResponse, ParameterConstants.TRANSACTION_TYPE, "")
                  val invMaxId = extractInt(jsonResponse, ParameterConstants.INV_MAX_ID, 0)
                  val encodedItemsArray = extractJSONArray(jsonResponse, ParameterConstants.ITEMS, JSONArray())
                  val unEncodedItemsArray = extractJSONArray(jsonResponse, ParameterConstants.NON_ENCODED_ITEMS, JSONArray())
                  val size = encodedItemsArray.length()+unEncodedItemsArray.length()
                  *//*for (i in 0 until encodedItemsArray.length()) {
                    val item = encodedItemsArray.getJSONObject(i)
                    val epc = extractString(item, ParameterConstants.EPC, "")
                    val tid = extractString(item, ParameterConstants.TID, "")
                    //topic,transactionType, epc, tid (get these from payload)
                    showLog("Mov_Upload",topic+"_"+transactionType+"_"+sessionId+"_"+epc+"_"+tid)
                    tagInfoDao.updateUploadedInvForBackgroundUpload(topic, transactionType, epc, tid,sessionId)
                  }
                  for (i in 0 until unEncodedItemsArray.length()) {
                    val item = unEncodedItemsArray.getJSONObject(i)
                    val epc = extractString(item, ParameterConstants.EPC, "")
                    val tid = extractString(item, ParameterConstants.TID, "")
                    //topic,transactionType, epc, tid (get these from payload)
                    showLog("Mov_Upload",topic+"_"+transactionType+"_"+sessionId+"_"+epc+"_"+tid)
                    tagInfoDao.updateUploadedInvForBackgroundUpload(topic,transactionType, epc, tid,sessionId)
                  }*//*
                  showLog(topic+"_Upload_Result",transactionType+"_"+sessionId+"->"+size)
                  val result = tagInfoDao.updateUploadedInvForBackgroundUpload(topic,transactionType,sessionId,invMaxId)
                  showLog(topic+"_Upload_Result",transactionType+"_"+sessionId+"_"+size+"->"+result)
                }
                  TopicConstants.ENCODE, TopicConstants.DECODE -> {
                      val deviceId = extractString(jsonResponse, ParameterConstants.DEVICE_ID, "")
                      if (!deviceId.equals(
                              DataStoreManager.readFromPreferences(
                                  ParameterConstants.DEVICE_ID,
                                  ""
                              ), true
                          )
                      ) return@launch
                      val sessionId = extractString(
                          jsonResponse,
                          ParameterConstants.SESSION_ID,
                          extractString(jsonResponse, ParameterConstants.DEVICE_SESSION_ID, "")
                      )
                      //val transactionType = extractString(jsonResponse, ParameterConstants.TRANSACTION_TYPE, "")
                      val detailsArray =
                          extractJSONArray(jsonResponse, ParameterConstants.DETAILS, JSONArray())
                      for (i in 0 until detailsArray.length()) {
                          val item = detailsArray.getJSONObject(i)
                          val id = extractString(item, ParameterConstants.ID, "")
                          val oldEpc = extractString(item, ParameterConstants.OLD_EPC, "")
                          val newEpc = extractString(item, ParameterConstants.NEW_EPC, "")
                          val tid = extractString(item, ParameterConstants.TID, "")
                          //id, epc, newEpc, tid (get these from payload)
                          tagInfoDao.updateUploadedTagWriteForBackgroundUpload(
                              topic,
                              id,
                              oldEpc,
                              newEpc,
                              tid,
                              sessionId
                          )
                      }
                  }
                TopicConstants.SEARCH-> {
                  val deviceId = extractString(jsonResponse, ParameterConstants.DEVICE_ID,"")
                  if(!deviceId.equals(DataStoreManager.readFromPreferences(ParameterConstants.DEVICE_ID,""),true)) return@launch
                  //val sessionId = extractString(jsonResponse, ParameterConstants.SESSION_ID,extractString(jsonResponse, ParameterConstants.DEVICE_SESSION_ID, ""))
                  //val searchTypeName = extractString(jsonResponse, ParameterConstants.SEARCH_TYPE_NAME,"")
                  //val sessionType = extractString(jsonResponse, ParameterConstants.SESSION_TYPE,"")
                  //val transactionType = extractString(jsonResponse, ParameterConstants.TRANSACTION_TYPE,extractString(jsonResponse, ParameterConstants.TRANSACTION_TYPES, ""))
                  val id = extractInt(jsonResponse, ParameterConstants.ID, 0)
                  val searchType = extractString(jsonResponse, ParameterConstants.SEARCH_VALUE_TYPE, "")
                  val searchValue = extractString(jsonResponse, ParameterConstants.SEARCH_VALUE,"")
                  val startTime = extractString(jsonResponse, ParameterConstants.START_DATE_TIME,"")
                  val endTime = extractString(jsonResponse, ParameterConstants.END_DATE_TIME,"")
                  showLog("Search11_id",""+id)
                  showLog("Search11",topic+"_"+searchType+"_"+searchValue+"_"+startTime+"_"+endTime)
                  if(id>0) searchLogDao.updateUploadedForBackgroundUpload(topic, searchType, searchValue,id)
                  else searchLogDao.updateUploadedForBackgroundUpload(topic, searchType, searchValue,startTime,endTime)
                }
                TopicConstants.SEARCH_LIST_UPDATE -> {
                    val deviceId = extractString(jsonResponse, ParameterConstants.DEVICE_ID,"")
                    val id = extractString(jsonResponse, ParameterConstants.ID,"")
                    val barcode = extractString(jsonResponse, ParameterConstants.BARCODE,"")
                    val oldEpc = extractString(jsonResponse, ParameterConstants.OLD_EPC,"")
                    val epc = extractString(jsonResponse, ParameterConstants.EPC,"")
                    val newEpc = if(oldEpc.isNotEmpty() && epc.isNotEmpty()) epc else ""
                    val tid = extractString(jsonResponse, ParameterConstants.TID,"")
                    val sessionId = extractString(jsonResponse, ParameterConstants.SESSION_ID,extractString(jsonResponse, ParameterConstants.DEVICE_SESSION_ID, ""))
                    val sessionType = extractString(jsonResponse, ParameterConstants.SESSION_TYPE, "")
                    val transactionType = extractString(jsonResponse, ParameterConstants.TRANSACTION_TYPE, "")
                    val searchTypeId = extractString(jsonResponse, ParameterConstants.SEARCH_TYPE_ID, "")
                    val searchTypeName= extractString(jsonResponse, ParameterConstants.SEARCH_TYPE_NAME, "")
                    val searchValue = extractString(jsonResponse, ParameterConstants.SEARCH_VALUE,"")
                    val referenceNumber = extractString(jsonResponse, ParameterConstants.REFERENCE_NUMBER,"")
                    val locationName = extractString(jsonResponse, ParameterConstants.ASSET_LOCATION_NAME,"")
                    val locationPath = extractString(jsonResponse, ParameterConstants.ASSET_LOCATION_PATH,"")
                    val pickedAt = extractString(jsonResponse, "pickedAt","")
                    val decodedAt = extractString(jsonResponse, "decodedAt","")
                    val remark = extractString(jsonResponse, ParameterConstants.REMARK,"")
                    val operation = extractString(jsonResponse, ParameterConstants.OPERATION,"")
                    if(!deviceId.equals(DataStoreManager.readFromPreferences(ParameterConstants.DEVICE_ID,""),true)) {
                        *//*if(newEpc.isNotEmpty() && !tagInfoDao.hasTid(topic,transactionType,barcode,tid)) {
                            //TODO add tagInfo to tagInfoDao
                            val tagInfo = TagInfoEntity(sessionType,transactionType,barcode,chkNull(oldEpc,epc),tid)
                            tagInfo.newEpc = newEpc
                            tagInfo.topic = topic
                            tagInfo.sessionId = sessionId
                            tagInfo.isTagWriteDone = newEpc.isNotEmpty()
                            tagInfo.isFound = true
                            tagInfo.isUploaded = true
                            tagInfo.isDeleted = false
                            tagInfo.insertTime = pickedAt
                            if(remark.isNotEmpty()){
                             if(newEpc.isNotEmpty()) tagInfo.writeFailReason=remark
                             else tagInfo.remark=remark
                            }
                            tagInfoDao.insert(tagInfo)
                        }*//*
                        val productZoneDataDao = AppDatabase.getDbInstance(appContext).productZoneDataDao()
                        if(newEpc.isNullOrEmpty()) productZoneDataDao.updateFoundBarcodeZone(topic,sessionType,transactionType,sessionId,barcode,locationName,locationPath)
                        else productZoneDataDao.updateFoundAndDecodeBarcodeZone(topic,sessionType,transactionType,sessionId,barcode,locationName,locationPath)
                    }
                    else{
                        LogUtils.showLog("barcode_newEpc_epc_tid",barcode+"_"+newEpc+"_"+epc+"_"+tid)
                        LogUtils.showLog("sessionId_sessionType_transactionType",sessionId+"_"+sessionType+"_"+transactionType)
                        LogUtils.showLog("searchTypeId_searchTypeName_searchValue_referenceNumber",searchTypeId+"_"+searchTypeName+"_"+searchValue+"_"+referenceNumber)
                        LogUtils.showLog("locationName_locationPath",locationName+"_"+locationPath)
                        if(newEpc.isNullOrEmpty()) tagInfoDao.updateUploadedInvForBackgroundUpload(topic,transactionType, epc, tid,sessionId)
                        else tagInfoDao.updateUploadedTagWriteForBackgroundUpload(topic, id, oldEpc, newEpc, tid, sessionId)
                    }
                    *//*val productZoneDataDao = AppDatabase.getDbInstance(appContext).productZoneDataDao()
                    if(newEpc.isNullOrEmpty()) productZoneDataDao.updateFoundBarcodeZone(topic,sessionType,transactionType,barcode,locationName,locationPath)
                    else productZoneDataDao.updateFoundAndDecodeBarcodeZone(topic,sessionType,transactionType,barcode,locationName,locationPath)*//*
                }
              }
              tagInfoDao.removeUploadedAndDeletedFromBackground()
              searchLogDao.removeUploadedAndDeletedFromBackground()
            } catch (e: Exception) {
              e.printStackTrace()
            }
        }*/
      }

      override fun deliveryComplete(token: IMqttDeliveryToken?) {
        showLog("MQTT", "deliveryComplete: $token")
      }

      /*private fun callDeviceMenuAPI(){
        //call device menu api after every config change (fully background)
        try {
                val map = hashMapOf(
                    ParameterConstants.CUSTOMER_ID to DataStoreManager.readFromPreferences(ParameterConstants.CUSTOMER_ID, ""),
                    ParameterConstants.BUSINESS_LINE_ID to DataStoreManager.readFromPreferences(ParameterConstants.BUSINESS_LINE_ID, ""),
                    ParameterConstants.LOCATION_ID to DataStoreManager.readFromPreferences(LoginConstants.DEVICE_LOCATION_ID, ""),
                    ParameterConstants.DEVICE_ID to DataStoreManager.readFromPreferences(ParameterConstants.DEVICE_ID, ""),
                    ParameterConstants.DEVICE_TYPE to ParameterConstants.DEVICE_TYPE_VAL)
                APIBackGroundCall.callApi(appContext, UrlConstants.DEVICE_MENUS, queryMap = map)
            } catch (e: Exception) { e.printStackTrace() }
      }*/
    })
  }
    private fun callDeviceMenuAPI(){
        //call device menu api after every config change (fully background)
        try {
            val map = hashMapOf(
                ParameterConstants.CUSTOMER_ID to DataStoreManager.readFromPreferences(ParameterConstants.CUSTOMER_ID, ""),
                ParameterConstants.BUSINESS_LINE_ID to DataStoreManager.readFromPreferences(ParameterConstants.BUSINESS_LINE_ID, ""),
                ParameterConstants.LOCATION_ID to DataStoreManager.readFromPreferences(LoginConstants.DEVICE_LOCATION_ID, ""),
                ParameterConstants.DEVICE_ID to DataStoreManager.readFromPreferences(ParameterConstants.DEVICE_ID, ""),
                ParameterConstants.DEVICE_TYPE to ParameterConstants.DEVICE_TYPE_VAL)
            APIBackGroundCall.callApi(appContext, UrlConstants.DEVICE_MENUS, queryMap = map)
        } catch (e: Exception) { e.printStackTrace() }
    }
    private fun processMessageUpdate(topic1: String?, message: String?,isPublished: Boolean=false){
        showLog("MQTT_Published", "Message arrived on topic: $topic1 isPublished:$isPublished")
        if(topic1.isNullOrEmpty()) return
        //if(isPublished == topic1.contains("/")) return
        if(isPublished && topic1.contains("/")) return
        if(!isPublished && !topic1.contains("/")) return

        val payload = message?.toString() ?: return
        showLog("MQTT_Published", "Payload received: $payload")
        FileUtils.writeMqttLog(
            serverUrl = mqttClient.serverURI,
            topic = topic1+"_Arrived",
            message = payload
        )

        val topic = chkNull(topic1,"");
        LogUtils.showLog("topic",""+topic)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val tagInfoDao = AppDatabase.getDbInstance(appContext).tagInfoDao()
                val searchLogDao = AppDatabase.getDbInstance(appContext).searchLogDao()
                val jsonArray = if(payload.trim().startsWith("[") and payload.trim().endsWith("]")) JSONArray(payload) else null
                val jsonResponse = if(jsonArray!=null && jsonArray.length()>0 && jsonArray.get(0)!=null && jsonArray.get(0) is JSONObject) jsonArray.getJSONObject(0) else JSONObject(payload.trim().removeSurrounding("[", "]"))
                when(topic) {
                    TopicConstants.USER_UPDATE+DataStoreManager.readFromPreferences(ParameterConstants.USER_ID,"") -> {
                        showLog(topic+"_Json",jsonResponse.toString())
                        // force logout if user is deactived or the user location is changed
                        val preUserCustomerId = DataStoreManager.readFromPreferences(LoginConstants.USER_CUSTOMER_ID, "")
                        val preUserRootLocationId = DataStoreManager.readFromPreferences(LoginConstants.USER_ROOT_LOCATION_ID, "")
                        val deviceCustomerId = DataStoreManager.readFromPreferences(LoginConstants.DEVICE_CUSTOMER_ID, "")
                        val deviceLocationId = DataStoreManager.readFromPreferences(LoginConstants.DEVICE_LOCATION_ID, "")
                        val deviceLocationPath = DataStoreManager.readFromPreferences(LoginConstants.DEVICE_LOCATION_PATH, "")
                        val isActive = extractBoolean(jsonResponse, ParameterConstants.IS_ACTIVE,false)
                        val userId = extractString(jsonResponse, ParameterConstants.USER_ID,extractString(jsonResponse, ParameterConstants.ID,""))
                        val userName = extractString(jsonResponse, ParameterConstants.USERNAME)
                        val userFirstName = extractString(jsonResponse, ParameterConstants.USER_FIRST_NAME, "")
                        val userLastName = extractString(jsonResponse, ParameterConstants.USER_LAST_NAME, "")
                        val userCustomerId = extractString(jsonResponse, ParameterConstants.CUSTOMER_ID)
                        val userRootLocationId = extractString(jsonResponse, ParameterConstants.LOCATION_ID)
                        val errMessage = extractString(jsonResponse, ParameterConstants.ERROR,extractString(jsonResponse, ParameterConstants.ERR_MSG, extractString(jsonResponse, ParameterConstants.MESSAGE,extractString(jsonResponse, ParameterConstants.ACTION, ""))))
                        DataStoreManager.saveToPreferences(ParameterConstants.USER_ID, userId)
                        DataStoreManager.saveToPreferences(ParameterConstants.USERNAME, userName)
                        DataStoreManager.saveToPreferences(ParameterConstants.USER_FIRST_NAME, userFirstName)
                        DataStoreManager.saveToPreferences(ParameterConstants.USER_LAST_NAME, userLastName)
                        DataStoreManager.saveToPreferences(LoginConstants.USER_CUSTOMER_ID, userCustomerId)
                        DataStoreManager.saveToPreferences(ParameterConstants.CUSTOMER_ID, userCustomerId)
                        DataStoreManager.saveToPreferences(LoginConstants.USER_ROOT_LOCATION_ID, userRootLocationId)
                        val isLoggedIn = DataStoreManager.readFromPreferences(LoginConstants.IS_LOGGED_IN,false)
                        if(isLoggedIn && (!isActive || userCustomerId.isNullOrEmpty() || userRootLocationId.isNullOrEmpty() || !deviceCustomerId.equals(userCustomerId, true) || !deviceLocationPath.contains(userRootLocationId) || !userCustomerId.equals(preUserCustomerId, true) || !userRootLocationId.equals(preUserRootLocationId,true))){
                            //force logout
                            val forceLogoutErrorMsg = chkNull(errMessage,"")
                            DataStoreManager.saveToPreferences("forceLogoutErrorMsg",forceLogoutErrorMsg)
                            DataHolder.isForceLogOut.value=true
                        }
                    }
                    TopicConstants.DEVICE_PROVISION+DataStoreManager.readFromPreferences(ParameterConstants.DEVICE_ID,"") ->{
                        showLog(topic+"_Json",jsonResponse.toString())
                        val preDeviceCustomerId = DataStoreManager.readFromPreferences(LoginConstants.DEVICE_CUSTOMER_ID, "")
                        val preDeviceLocationId = DataStoreManager.readFromPreferences(LoginConstants.DEVICE_LOCATION_ID, "")
                        val isUnProvisioned = extractBoolean(jsonResponse, ParameterConstants.IS_UN_PROVISIONED,false)
                        val isActive = extractBoolean(jsonResponse, ParameterConstants.IS_ACTIVE,false)
                        val deviceCustomerId = extractString(jsonResponse, ParameterConstants.CUSTOMER_ID, "")
                        val deviceLocationId = extractString(jsonResponse, ParameterConstants.LOCATION_ID, "")
                        val deviceLocationPath = extractString(jsonResponse, ParameterConstants.LOCATION_PATH, "")
                        val deviceLocationCode = extractString(jsonResponse, ParameterConstants.LOCATION_CODE, "")
                        val deviceLocationName = extractString(jsonResponse, ParameterConstants.LOCATION_NAME, "")
                        val errMessage = extractString(jsonResponse, ParameterConstants.ERROR,extractString(jsonResponse, ParameterConstants.ERR_MSG, extractString(jsonResponse, ParameterConstants.MESSAGE, "")))
                        DataStoreManager.saveToPreferences(LoginConstants.DEVICE_CUSTOMER_ID,if(isUnProvisioned)"" else deviceCustomerId)
                        DataStoreManager.saveToPreferences(LoginConstants.DEVICE_LOCATION_ID,if(isUnProvisioned)"" else deviceLocationId)
                        DataStoreManager.saveToPreferences(LoginConstants.DEVICE_LOCATION_PATH,if(isUnProvisioned)"" else deviceLocationPath)
                        DataStoreManager.saveToPreferences(LoginConstants.DEVICE_LOCATION_CODE, if(isUnProvisioned)"" else deviceLocationCode)
                        DataStoreManager.saveToPreferences(LoginConstants.DEVICE_LOCATION_NAME, if(isUnProvisioned)"" else deviceLocationName)

                        val isLoggedIn = DataStoreManager.readFromPreferences(LoginConstants.IS_LOGGED_IN,false)
                        val userCustomerId = DataStoreManager.readFromPreferences(LoginConstants.USER_CUSTOMER_ID,"")
                        val userRootLocationId =  DataStoreManager.readFromPreferences(LoginConstants.USER_ROOT_LOCATION_ID,"")
                        showLog("isLoggedIn",""+isLoggedIn)
                        if(isLoggedIn && (isUnProvisioned || !isActive || deviceCustomerId.isNullOrEmpty() || deviceLocationPath.isNullOrEmpty() || !deviceCustomerId.equals(userCustomerId, true) || !deviceLocationPath.contains(userRootLocationId) || !deviceCustomerId.equals(preDeviceCustomerId, true) || !deviceLocationId.equals(preDeviceLocationId,true))){
                            //force logout
                            val forceLogoutErrorMsg = chkNull(errMessage,"")
                            DataStoreManager.saveToPreferences("forceLogoutErrorMsg",forceLogoutErrorMsg)
                            DataHolder.isForceLogOut.value=true
                        }
                    }
                    TopicConstants.NOTIFY+DataStoreManager.readFromPreferences(LoginConstants.DEVICE_LOCATION_ID,"") -> {
                        showLog(topic+"_Json",jsonResponse.toString())
                        //Always parse & save notifications in a db table
                        JsonUtils.parseNotification(jsonResponse)
                        //Only Show notification if already logged In
                        if(DataStoreManager.readFromPreferences(LoginConstants.IS_LOGGED_IN,false)) {

                            val currentScreen = DataHolder.currentScreen
                            val isForeground = DataHolder.isAppInForeground

                            LogUtils.showLog("STATE", "Screen=$currentScreen Foreground=$isForeground")

                            val isRestrictedScreen =
                                currentScreen.contains(Screen.NotificationScreen.route) ||
                                        currentScreen.contains(Screen.NotificationDetailScreen.route) ||
                                        currentScreen.contains(Screen.UserDetails.route)


                            //Show Notification If App in background or Non Restricted Screen
                            if (!isForeground || !isRestrictedScreen)
                                NotificationHelper.showNotification(appContext, jsonResponse)
                        }
                    }
                    TopicConstants.UPDATE_LOCATION_CONFIG+DataStoreManager.readFromPreferences(LoginConstants.DEVICE_LOCATION_ID,"") -> {
                        showLog(topic+"_Json",jsonResponse.toString())
                        JsonUtils.parseLocationConfig(jsonResponse)
                        //call device menu api after every config change
                        callDeviceMenuAPI()
                    }
                    TopicConstants.DEVICE_MENU+DataStoreManager.readFromPreferences(ParameterConstants.BUSINESS_LINE_ID,"") -> {
                        showLog(topic+"_Json",jsonResponse.toString())
                        //call device menu api after menu change
                        callDeviceMenuAPI()
                    }
                    TopicConstants.INVENTORY -> {
                        val deviceId = extractString(jsonResponse, ParameterConstants.DEVICE_ID,"")
                        val userId = extractString(jsonResponse, ParameterConstants.USER_ID,"")
                        val userFullName = extractString(jsonResponse, ParameterConstants.USER_FIRST_NAME,"")+"_"+extractString(jsonResponse, ParameterConstants.USER_LAST_NAME,"")
                        //showLog("Inv_Upload_Json",jsonResponse.toString())
                        val sessionId = extractString(jsonResponse, ParameterConstants.SESSION_ID,extractString(jsonResponse, ParameterConstants.DEVICE_SESSION_ID, ""))
                        val transactionType = extractString(jsonResponse, ParameterConstants.TRANSACTION_TYPE, "")
                        val invMaxId = extractInt(jsonResponse, ParameterConstants.INV_MAX_ID, 0)
                        val encodedItemsArray = extractJSONArray(jsonResponse, ParameterConstants.ITEMS, JSONArray())
                        val unEncodedItemsArray = extractJSONArray(jsonResponse, ParameterConstants.NON_ENCODED_ITEMS, JSONArray())
                        val size = encodedItemsArray.length()+unEncodedItemsArray.length()
                        showLog("invMaxId",invMaxId.toString())
                        /*val epcs = ArrayList<String>(0)
                        for (i in 0 until encodedItemsArray.length()) {
                          val item = encodedItemsArray.getJSONObject(i)
                          val epc = extractString(item, ParameterConstants.EPC, "")
                          val tid = extractString(item, ParameterConstants.TID, "")
                          if(epc.isNotEmpty())epcs.add(epc)
                          //topic,transactionType, epc, tid (get these from payload)
                          //showLog("Inv_Upload",topic+"_"+transactionType+"_"+sessionId+"_"+epc+"_"+tid)
                          //val result =tagInfoDao.updateUploadedInvForBackgroundUpload(topic, transactionType, epc, tid,sessionId)
                          //showLog("Inv_Upload_Result",""+result)
                        }
                        for (i in 0 until unEncodedItemsArray.length()) {
                          val item = unEncodedItemsArray.getJSONObject(i)
                          val epc = extractString(item, ParameterConstants.EPC, "")
                          val tid = extractString(item, ParameterConstants.TID, "")
                          if(epc.isNotEmpty())epcs.add(epc)
                          //topic,transactionType, epc, tid (get these from payload)
                          //showLog("Inv_Upload",topic+"_"+transactionType+"_"+sessionId+"_"+epc+"_"+tid)
                          //val result = tagInfoDao.updateUploadedInvForBackgroundUpload(topic,transactionType, epc, tid,sessionId)
                          //showLog("Inv_Upload_Result",""+result)
                        }
                          showLog("Inv_Upload_Result",""+epcs.size)
                          epcs.distinct().chunked(900).forEach { chunk ->
                          val result = tagInfoDao.updateUploadedInvForBackgroundUpload(topic,transactionType, chunk, sessionId)
                          showLog("Inv_Upload_Result",transactionType+"_"+sessionId+"_"+chunk.size+"->"+result)
                          }*/
                        if(!deviceId.equals(DataStoreManager.readFromPreferences(ParameterConstants.DEVICE_ID,""),true)) {
                            //TODO check if sessionId is active
                            //TODO save to show group inventory
                        }
                        else{
                            showLog("Inv_Upload_Result",transactionType+"_"+sessionId+"->"+size)
                            val result = tagInfoDao.updateUploadedInvForBackgroundUpload(topic,transactionType,sessionId,invMaxId)
                            showLog("Inv_Upload_Result",transactionType+"_"+sessionId+"_"+size+"->"+result)
                        }
                    }
                    TopicConstants.MOVEMENT,TopicConstants.REPLENISHMENT -> {
                        val deviceId = extractString(jsonResponse, ParameterConstants.DEVICE_ID,"")
                        if(!deviceId.equals(DataStoreManager.readFromPreferences(ParameterConstants.DEVICE_ID,""),true)) return@launch
                        val sessionId = extractString(jsonResponse, ParameterConstants.SESSION_ID,extractString(jsonResponse, ParameterConstants.DEVICE_SESSION_ID, ""))
                        val transactionType = extractString(jsonResponse, ParameterConstants.TRANSACTION_TYPE, "")
                        val invMaxId = extractInt(jsonResponse, ParameterConstants.INV_MAX_ID, 0)
                        val encodedItemsArray = extractJSONArray(jsonResponse, ParameterConstants.ITEMS, JSONArray())
                        val unEncodedItemsArray = extractJSONArray(jsonResponse, ParameterConstants.NON_ENCODED_ITEMS, JSONArray())
                        val size = encodedItemsArray.length()+unEncodedItemsArray.length()
                        /*for (i in 0 until encodedItemsArray.length()) {
                          val item = encodedItemsArray.getJSONObject(i)
                          val epc = extractString(item, ParameterConstants.EPC, "")
                          val tid = extractString(item, ParameterConstants.TID, "")
                          //topic,transactionType, epc, tid (get these from payload)
                          showLog("Mov_Upload",topic+"_"+transactionType+"_"+sessionId+"_"+epc+"_"+tid)
                          tagInfoDao.updateUploadedInvForBackgroundUpload(topic, transactionType, epc, tid,sessionId)
                        }
                        for (i in 0 until unEncodedItemsArray.length()) {
                          val item = unEncodedItemsArray.getJSONObject(i)
                          val epc = extractString(item, ParameterConstants.EPC, "")
                          val tid = extractString(item, ParameterConstants.TID, "")
                          //topic,transactionType, epc, tid (get these from payload)
                          showLog("Mov_Upload",topic+"_"+transactionType+"_"+sessionId+"_"+epc+"_"+tid)
                          tagInfoDao.updateUploadedInvForBackgroundUpload(topic,transactionType, epc, tid,sessionId)
                        }*/
                        showLog(topic+"_Upload_Result",transactionType+"_"+sessionId+"->"+size)
                        val result = tagInfoDao.updateUploadedInvForBackgroundUpload(topic,transactionType,sessionId,invMaxId)
                        showLog(topic+"_Upload_Result",transactionType+"_"+sessionId+"_"+size+"->"+result)
                    }
                    TopicConstants.ENCODE, TopicConstants.DECODE -> {
                        val deviceId = extractString(jsonResponse, ParameterConstants.DEVICE_ID, "")
                        if (!deviceId.equals(
                                DataStoreManager.readFromPreferences(
                                    ParameterConstants.DEVICE_ID,
                                    ""
                                ), true
                            )
                        ) return@launch
                        val sessionId = extractString(
                            jsonResponse,
                            ParameterConstants.SESSION_ID,
                            extractString(jsonResponse, ParameterConstants.DEVICE_SESSION_ID, "")
                        )
                        //val transactionType = extractString(jsonResponse, ParameterConstants.TRANSACTION_TYPE, "")
                        val detailsArray =
                            extractJSONArray(jsonResponse, ParameterConstants.DETAILS, JSONArray())
                        for (i in 0 until detailsArray.length()) {
                            val item = detailsArray.getJSONObject(i)
                            val id = extractString(item, ParameterConstants.ID, "")
                            val oldEpc = extractString(item, ParameterConstants.OLD_EPC, "")
                            val newEpc = extractString(item, ParameterConstants.NEW_EPC, "")
                            val tid = extractString(item, ParameterConstants.TID, "")
                            //id, epc, newEpc, tid (get these from payload)
                            tagInfoDao.updateUploadedTagWriteForBackgroundUpload(
                                topic,
                                id,
                                oldEpc,
                                newEpc,
                                tid,
                                sessionId
                            )
                        }
                    }
                    TopicConstants.SEARCH-> {
                        val deviceId = extractString(jsonResponse, ParameterConstants.DEVICE_ID,"")
                        if(!deviceId.equals(DataStoreManager.readFromPreferences(ParameterConstants.DEVICE_ID,""),true)) return@launch
                        //val sessionId = extractString(jsonResponse, ParameterConstants.SESSION_ID,extractString(jsonResponse, ParameterConstants.DEVICE_SESSION_ID, ""))
                        //val searchTypeName = extractString(jsonResponse, ParameterConstants.SEARCH_TYPE_NAME,"")
                        //val sessionType = extractString(jsonResponse, ParameterConstants.SESSION_TYPE,"")
                        //val transactionType = extractString(jsonResponse, ParameterConstants.TRANSACTION_TYPE,extractString(jsonResponse, ParameterConstants.TRANSACTION_TYPES, ""))
                        val id = extractInt(jsonResponse, ParameterConstants.ID, 0)
                        val searchType = extractString(jsonResponse, ParameterConstants.SEARCH_VALUE_TYPE, "")
                        val searchValue = extractString(jsonResponse, ParameterConstants.SEARCH_VALUE,"")
                        val startTime = extractString(jsonResponse, ParameterConstants.START_DATE_TIME,"")
                        val endTime = extractString(jsonResponse, ParameterConstants.END_DATE_TIME,"")
                        showLog("Search11_id",""+id)
                        showLog("Search11",topic+"_"+searchType+"_"+searchValue+"_"+startTime+"_"+endTime)
                        if(id>0) searchLogDao.updateUploadedForBackgroundUpload(topic, searchType, searchValue,id)
                        else searchLogDao.updateUploadedForBackgroundUpload(topic, searchType, searchValue,startTime,endTime)
                    }
                    TopicConstants.SEARCH_LIST_UPDATE -> {
                        val deviceId = extractString(jsonResponse, ParameterConstants.DEVICE_ID,"")
                        val id = extractString(jsonResponse, ParameterConstants.ID,"")
                        val barcode = extractString(jsonResponse, ParameterConstants.BARCODE,"")
                        val oldEpc = extractString(jsonResponse, ParameterConstants.OLD_EPC,"")
                        val epc = extractString(jsonResponse, ParameterConstants.EPC,"")
                        val newEpc = if(oldEpc.isNotEmpty() && epc.isNotEmpty()) epc else ""
                        val tid = extractString(jsonResponse, ParameterConstants.TID,"")
                        val sessionId = extractString(jsonResponse, ParameterConstants.SESSION_ID,extractString(jsonResponse, ParameterConstants.DEVICE_SESSION_ID, ""))
                        val sessionType = extractString(jsonResponse, ParameterConstants.SESSION_TYPE, "")
                        val transactionType = extractString(jsonResponse, ParameterConstants.TRANSACTION_TYPE, "")
                        val searchTypeId = extractString(jsonResponse, ParameterConstants.SEARCH_TYPE_ID, "")
                        val searchTypeName= extractString(jsonResponse, ParameterConstants.SEARCH_TYPE_NAME, "")
                        val searchValue = extractString(jsonResponse, ParameterConstants.SEARCH_VALUE,"")
                        val referenceNumber = extractString(jsonResponse, ParameterConstants.REFERENCE_NUMBER,"")
                        val locationName = extractString(jsonResponse, ParameterConstants.ASSET_LOCATION_NAME,"")
                        val locationPath = extractString(jsonResponse, ParameterConstants.ASSET_LOCATION_PATH,"")
                        val pickedAt = extractString(jsonResponse, "pickedAt","")
                        val decodedAt = extractString(jsonResponse, "decodedAt","")
                        val remark = extractString(jsonResponse, ParameterConstants.REMARK,"")
                        val operation = extractString(jsonResponse, ParameterConstants.OPERATION,"")
                        if(!deviceId.equals(DataStoreManager.readFromPreferences(ParameterConstants.DEVICE_ID,""),true)) {
                            /*if(newEpc.isNotEmpty() && !tagInfoDao.hasTid(topic,transactionType,barcode,tid)) {
                                //TODO add tagInfo to tagInfoDao
                                val tagInfo = TagInfoEntity(sessionType,transactionType,barcode,chkNull(oldEpc,epc),tid)
                                tagInfo.newEpc = newEpc
                                tagInfo.topic = topic
                                tagInfo.sessionId = sessionId
                                tagInfo.isTagWriteDone = newEpc.isNotEmpty()
                                tagInfo.isFound = true
                                tagInfo.isUploaded = true
                                tagInfo.isDeleted = false
                                tagInfo.insertTime = pickedAt
                                if(remark.isNotEmpty()){
                                 if(newEpc.isNotEmpty()) tagInfo.writeFailReason=remark
                                 else tagInfo.remark=remark
                                }
                                tagInfoDao.insert(tagInfo)
                            }*/
                            val productZoneDataDao = AppDatabase.getDbInstance(appContext).productZoneDataDao()
                            if(newEpc.isNullOrEmpty()) productZoneDataDao.updateFoundBarcodeZone(topic,sessionType,transactionType,sessionId,barcode,locationName,locationPath)
                            else productZoneDataDao.updateFoundAndDecodeBarcodeZone(topic,sessionType,transactionType,sessionId,barcode,locationName,locationPath)
                        }
                        else{
                            LogUtils.showLog("barcode_newEpc_epc_tid",barcode+"_"+newEpc+"_"+epc+"_"+tid)
                            LogUtils.showLog("sessionId_sessionType_transactionType",sessionId+"_"+sessionType+"_"+transactionType)
                            LogUtils.showLog("searchTypeId_searchTypeName_searchValue_referenceNumber",searchTypeId+"_"+searchTypeName+"_"+searchValue+"_"+referenceNumber)
                            LogUtils.showLog("locationName_locationPath",locationName+"_"+locationPath)
                            if(newEpc.isNullOrEmpty()) tagInfoDao.updateUploadedInvForBackgroundUpload(topic,transactionType, epc, tid,sessionId)
                            else tagInfoDao.updateUploadedTagWriteForBackgroundUpload(topic, id, oldEpc, newEpc, tid, sessionId)
                        }
                        /*val productZoneDataDao = AppDatabase.getDbInstance(appContext).productZoneDataDao()
                        if(newEpc.isNullOrEmpty()) productZoneDataDao.updateFoundBarcodeZone(topic,sessionType,transactionType,barcode,locationName,locationPath)
                        else productZoneDataDao.updateFoundAndDecodeBarcodeZone(topic,sessionType,transactionType,barcode,locationName,locationPath)*/
                    }
                }
                tagInfoDao.removeUploadedAndDeletedFromBackground()
                searchLogDao.removeUploadedAndDeletedFromBackground()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

  fun disconnect() {
    if (isConnected()) {
      mqttClient.disconnect(null, object : IMqttActionListener {
        override fun onSuccess(asyncActionToken: IMqttToken?) {
          _isConnected.value = false
          _connectionStatus.value = "Disconnected"
          MqttDiagnosticTracker.onDisconnected(_connectionStatus.value)
          showToast("Disconnected")
          FileUtils.writeMqttLog(
           serverUrl = mqttClient.serverURI,
           topic = "disconnect",
           message = "Disconnected successfully:-${_connectionStatus.value}"
          )
          unsubscribeAll()
        }

        override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
          showToast("Disconnect failed")
            FileUtils.writeMqttLog(
                serverUrl = mqttClient.serverURI,
                topic = "disconnect",
                message = "Disconnected Failed:-${_connectionStatus.value}"
            )
        }
      })
    }
  }

  fun showToast(message: String) {
    showLog("MQTT_Message",message)
    /* if(context!=null && context is MainActivity){
       (context as MainActivity).showToast(message);
     }*/
  }

  fun subscribeAll() {
    //for (topic in topicItems) subscribe(topic)
    subscribedTopics.clear()
    subscribe(topicItems)
  }

  fun subscribe(topic: String) {
    if (isConnected() && topic.isNotEmpty()) {
      //publishDeviceStatus(true)
      mqttClient.subscribe(topic, 1, null, object : IMqttActionListener {
        override fun onSuccess(asyncActionToken: IMqttToken?) {
          showToast("Subscribed to $topic")
          if(!subscribedTopics.contains(topic)) subscribedTopics.add(topic)
          //Toast.makeText(appContext, "Subscribed to $topic", Toast.LENGTH_SHORT).show()
          //CoroutineScope(Dispatchers.IO).launch {
            val existing = topicItems.contains(topic)
            if (!existing) {
              topicItems.add(topic)
              showLog("Insert", topic)
              //  topicDao.insertAll(listOf(TopicData(subscribedName = topic, status = true)))
            } else {
              showLog("Subscribed", topic)
            }
          //}
        }

        override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
          showToast("Subscribe failed "+topic)
          //Toast.makeText(appContext, "Subscribe failed", Toast.LENGTH_SHORT).show()
        }
      })
    } else {
      if (topic.isEmpty()) {
        showToast("Topic cannot be empty")
        //Toast.makeText(appContext, "Topic cannot be empty", Toast.LENGTH_SHORT).show()
      } else if (!isConnected()) {
        showToast("Not connected to MQTT broker")
        //Toast.makeText(appContext, "Not connected to MQTT broker", Toast.LENGTH_SHORT).show()
      }
    }
  }

  fun subscribe(topics: List<String>) {
    if(topics.isEmpty()) { showToast("Topics cannot be empty"); return}
    if(!isConnected())  { showToast("Not connected to MQTT broker"); return}
    //if (isConnected() && topics.isNotEmpty()) {
      //publishDeviceStatus(true)
    //var subscribedCount = 0
    //if (isConnected() && topics.isNotEmpty()) {
    var topicCount = 0
      topics.forEach { topic ->
          if(topic.isNotEmpty())
          mqttClient.subscribe(topic, 1, null, object : IMqttActionListener {
              override fun onSuccess(asyncActionToken: IMqttToken?) {
                  topicCount++
                  showToast("Subscribed to $topic")
                  if (!subscribedTopics.contains(topic)) subscribedTopics.add(topic)
                  //Toast.makeText(appContext, "Subscribed to $topic", Toast.LENGTH_SHORT).show()
                  //CoroutineScope(Dispatchers.IO).launch {
                  val existing = topicItems.contains(topic)
                  if (!existing) {
                      topicItems.add(topic)
                      showLog("Insert", topic)
                      //  topicDao.insertAll(listOf(TopicData(subscribedName = topic, status = true)))
                  } else {
                      showLog("Subscribed", topic)
                  }
                  //}
              }

              override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                  topicCount++
                  showLog("Subscribe failed",topic)
                  //Toast.makeText(appContext, "Subscribe failed", Toast.LENGTH_SHORT).show()
              }
          })
     // }
    }
      showLog("topicCount",""+topicCount)
      while (topicCount<topics.size){}
      showLog("topicCount1",""+topicCount)
      //return subscribedCount
  }

  fun unsubscribe(topics: List<String>) {
    if(topics.isEmpty()) { showToast("Topics cannot be empty"); return}
    if(!isConnected())  { showToast("Not connected to MQTT broker"); return}
    if(isConnected() && topics.isNotEmpty()) {
        topics.forEach { topic ->
            if (topic.isNotEmpty())
                mqttClient.unsubscribe(topic, null, object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        showLog("Unsubscribed", topic)
                        //Toast.makeText(appContext, "Unsubscribed from $topic", Toast.LENGTH_SHORT).show()
                        //CoroutineScope(Dispatchers.IO).launch {
                            if (subscribedTopics.contains(topic)) {
                                subscribedTopics.remove(topic)
                            }
                            if (topicItems.contains(topic)) {
                                topicItems.remove(topic)
                            }
                        //}
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        showLog("Unsubscribe failed",topic)
                        //Toast.makeText(appContext, "Unsubscribe failed $topic", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }
  }

  fun unsubscribe(topic: String) {
    if (isConnected() && topic.isNotEmpty()) {
      mqttClient.unsubscribe(topic, null, object : IMqttActionListener {
        override fun onSuccess(asyncActionToken: IMqttToken?) {
          showToast("Unsubscribed from $topic")
          //Toast.makeText(appContext, "Unsubscribed from $topic", Toast.LENGTH_SHORT).show()
          //CoroutineScope(Dispatchers.IO).launch {
            val existingTopic = topicItems.contains(topic)
            showLog("Existing_Topic", existingTopic.toString())
            if (existingTopic) topicItems.remove(topic)
            if (subscribedTopics.contains(topic)) {
                subscribedTopics.remove(topic)
            }
            showLog("Existing_Topic", existingTopic.toString())
          //}
        }

        override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
          showToast("Unsubscribe failed $topic")
          //Toast.makeText(appContext, "Unsubscribe failed $topic", Toast.LENGTH_SHORT).show()
        }
      })
    } else {
      if (topic.isEmpty()) {
        showToast("Topic cannot be empty")
        //Toast.makeText(appContext, "Topic cannot be empty", Toast.LENGTH_SHORT).show()
      } else if (!isConnected()) {
        showToast("Not connected to MQTT broker")
        //Toast.makeText(appContext, "Not connected to MQTT broker", Toast.LENGTH_SHORT).show()
      }
    }
  }

  fun unsubscribeAll() {
    //for (topic in topicItems) unsubscribe(topic)
    unsubscribe(topicItems)
  }

  fun removeUploadedAndDeleted(){
    /*CoroutineScope(Dispatchers.IO).launch {

    }*/
  }

  fun publishPending(context: Context?,brokerUrl:String){
    showLog("MQTT_method","publishPending")
    if(brokerUrl.trim().isNullOrEmpty()) return
    if(context!=null && !isInitialized()) {initialize(context,brokerUrl.trim()); return}
    showLog("MQTT_isConnected","isConnected")
    if(!isConnected()) { connect(); return}
    CoroutineScope(Dispatchers.IO).launch {
      try {
            val tagInfoDao = AppDatabase.getDbInstance(appContext).tagInfoDao()
            val searchLogDao = AppDatabase.getDbInstance(appContext).searchLogDao()
            val menuNotificationDao = AppDatabase.getDbInstance(appContext).menuNotificationDao()
            tagInfoDao.removeUploadedAndDeletedFromBackground()
            searchLogDao.removeUploadedAndDeletedFromBackground()
            showLog("getCurrentAPITime",DateFormatUtils.getCurrentAPITime())
            menuNotificationDao.deleteExpired(DateFormatUtils.getCurrentAPITime())
      }catch (e: Exception) {e.printStackTrace()}
      try {
        val tagInfoDao = AppDatabase.getDbInstance(appContext).tagInfoDao()
        val listPendingTopics:List<String> = tagInfoDao.getNonUploadedTopicsForBackgroundUpload()
        showLog("listPendingTopics",""+listPendingTopics.size+"_"+listPendingTopics.toString())
        if(listPendingTopics.isNotEmpty()) {
          for(topic in listPendingTopics){
            val listPendingSessionTypes:List<String> = tagInfoDao.getNonUploadedSessionTypesForBackgroundUpload(topic)
            showLog("listPendingSessionTypes",""+listPendingSessionTypes.size+"_"+listPendingSessionTypes.toString());
            if(listPendingSessionTypes.isNotEmpty()) {
              for (sessionType in listPendingSessionTypes) {
                val listPendingTransactionTypes:List<String> = tagInfoDao.getNonUploadedTransactionTypesForBackgroundUpload(topic,sessionType)
                showLog("listPendingTransactionTypes",""+listPendingTransactionTypes.size+"_"+listPendingTransactionTypes.toString())
                if(listPendingTransactionTypes.isNotEmpty()) {
                  for (transactionType in listPendingTransactionTypes) {
                    val listPendingSessionIds:List<String> = tagInfoDao.getNonUploadedSessionIdsForBackgroundUpload(topic,sessionType,transactionType)
                    showLog("listPendingSessionIds",""+listPendingSessionIds.size+"_"+listPendingSessionIds.toString())
                    if(listPendingSessionIds.isNotEmpty()) {
                    for(sessionId in listPendingSessionIds) {
                      when (topic) {
                        //Note: As Discussed, Encoding & Decoding has to be uploaded 1 by 1
                        TopicConstants.ENCODE ->
                          for (tagInfo: TagInfoEntity in tagInfoDao.getNonUploadedForBackgroundUpload(topic, sessionType, transactionType,sessionId)) {
                            publishEncode(tagInfo, sessionType, transactionType,sessionId,tagInfo.sessionData)
                          }

                        TopicConstants.DECODE ->
                          for (tagInfo: TagInfoEntity in tagInfoDao.getNonUploadedForBackgroundUpload(topic, sessionType, transactionType, sessionId)) {
                          publishDecode(tagInfo, sessionType,tagInfo.decodeType, transactionType,sessionId)
                        }

                        TopicConstants.SEARCH_LIST_UPDATE -> {
                            //publishListUpdate(tagInfoDao.getNonUploadedForBackgroundUpload(topic, sessionType, transactionType, sessionId), sessionType, transactionType, sessionId)
                            var offset=0
                            while(true){
                                val chunk = tagInfoDao.getNonUploadedForBackgroundUpload(topic, sessionType, transactionType, sessionId, offset)
                                if(chunk.isNotEmpty()){
                                    publishListUpdate(tagInfoDao.getNonUploadedForBackgroundUpload(topic, sessionType, transactionType, sessionId), sessionType, transactionType, sessionId)
                                    offset+=chunk.size
                                    if(offset<1000) break
                                }
                                else break
                            }
                        }

                        TopicConstants.MOVEMENT ->{
                          //publishMovement(tagInfoDao.getNonUploadedForBackgroundUpload(topic, sessionType, transactionType,sessionId), sessionType, transactionType,sessionId)
                          var offset=0
                          while(true){
                           val chunk = tagInfoDao.getNonUploadedForBackgroundUpload(topic, sessionType, transactionType, sessionId, offset)
                           if(chunk.isNotEmpty()){
                            publishMovement(tagInfoDao.getNonUploadedForBackgroundUpload(topic, sessionType, transactionType, sessionId), sessionType, transactionType, sessionId)
                             offset+=chunk.size
                             if(offset<1000) break
                            }
                            else break
                           }
                        }

                        TopicConstants.REPLENISHMENT -> {
                          //publishReplenishment(tagInfoDao.getNonUploadedForBackgroundUpload(topic, sessionType, transactionType,sessionId), sessionType, transactionType,sessionId)
                          var offset=0
                          while(true){
                              val chunk = tagInfoDao.getNonUploadedForBackgroundUpload(topic, sessionType, transactionType, sessionId, offset)
                              if(chunk.isNotEmpty()){
                                  publishReplenishment(tagInfoDao.getNonUploadedForBackgroundUpload(topic, sessionType, transactionType, sessionId), sessionType, transactionType, sessionId)
                                  offset+=chunk.size
                                  if(offset<1000) break
                              }
                              else break
                          }
                        }

                        TopicConstants.INVENTORY -> {
                          //publishInventory(tagInfoDao.getNonUploadedForBackgroundUpload(topic, sessionType, transactionType, sessionId), sessionType, transactionType, sessionId)
                          var offset=0
                          while(true){
                           val chunk = tagInfoDao.getNonUploadedForBackgroundUpload(topic, sessionType, transactionType, sessionId, offset)
                           if(chunk.isNotEmpty()){
                             publishInventory(tagInfoDao.getNonUploadedForBackgroundUpload(topic, sessionType, transactionType, sessionId), sessionType, transactionType, sessionId)
                             offset+=chunk.size
                             if(offset<1000) break
                           }
                           else break
                          }
                        }
                      }
                    }
                    }
                  }
                }
              }
            }
          }
        }
      }catch (e: Exception) {e.printStackTrace()}
      try {
            val searchLogDao = AppDatabase.getDbInstance(appContext).searchLogDao()
            val listPendingSearchLogs:List<SearchLogEntity> = searchLogDao.getNonUploadedForBackgroundUpload()
            showLog("listPendingSearchLogs",""+listPendingSearchLogs.size)
            for(searchLog in listPendingSearchLogs)
                publishSearch(searchLog)
        }catch (e: Exception) {e.printStackTrace()}
    }
  }

  /*fun publishToTopic(tagInfoEntity: TagInfoEntity,topic: String,sessionType:String,transactionType: String,sessionId: String,sessionData: String) {
    when (topic){
      TopicConstants.ENCODE -> publishEncode(tagInfoEntity,sessionType,transactionType,sessionId,sessionData)
      TopicConstants.DECODE -> publishDecode(tagInfoEntity,sessionType,transactionType,sessionId)
      TopicConstants.INVENTORY -> publishInventory(tagInfoEntity,sessionType,transactionType,sessionId,sessionData)
      TopicConstants.MOVEMENT -> publishMovement(tagInfoEntity,sessionType,transactionType,sessionId,sessionData)
    }
  }*/

  /*fun publishToTopic(tagInfoEntities: List<TagInfoEntity>,topic: String,sessionType:String,transactionType: String,sessionId: String,sessionData: String) {
    if(tagInfoEntities.isNullOrEmpty()) return
    when (topic){
      TopicConstants.ENCODE -> publishEncode(tagInfoEntities,sessionType,transactionType,sessionId,sessionData)
      TopicConstants.DECODE -> publishDecode(tagInfoEntities,sessionType,transactionType,sessionId,sessionData)
      TopicConstants.INVENTORY -> publishInventory(tagInfoEntities,sessionType,transactionType,sessionId,sessionData)
      TopicConstants.MOVEMENT -> publishMovement(tagInfoEntities,sessionType,transactionType,sessionId,sessionData)
    }
  }*/

  fun publishInventory(tagInfoEntity: TagInfoEntity,sessionType: String,transactionType: String,sessionId: String, sessionData: String) {
    val tagInfoEntities = ArrayList<TagInfoEntity>(0)
    tagInfoEntities.add(tagInfoEntity)
    publishInventory(tagInfoEntities,sessionType,transactionType,sessionId,sessionData)
    showLog("Publish Inventory Data",tagInfoEntities.toString())
  }

  fun publishInventory(tagInfoEntities: List<TagInfoEntity>, sessionType: String, transactionType: String, sessionId: String) {
    if(tagInfoEntities.isNullOrEmpty()) return
    showLog("MQTT_method","publishInventory_"+tagInfoEntities.size)
    val tagInfoEntity = tagInfoEntities.get(0)
    publishInventory(tagInfoEntities,sessionType,transactionType,sessionId,tagInfoEntity.sessionData)
  }
  fun publishInventory(tagInfoEntities: List<TagInfoEntity>,sessionType: String,transactionType: String,sessionId: String, sessionData:String) {
    if(tagInfoEntities.isNullOrEmpty()) return
    try {
      val jo = if(sessionData.isNotEmpty()) JSONObject(sessionData) else JSONObject()
      if(!jo.has(ParameterConstants.CUSTOMER_ID) || jo.getString(ParameterConstants.CUSTOMER_ID).isNullOrEmpty()) jo.put(ParameterConstants.CUSTOMER_ID, DataStoreManager.readFromPreferences(ParameterConstants.CUSTOMER_ID, ""))
      if(!jo.has(ParameterConstants.OPERATION_LOCATION_ID) || jo.getString(ParameterConstants.OPERATION_LOCATION_ID).isNullOrEmpty()) jo.put(ParameterConstants.OPERATION_LOCATION_ID, DataStoreManager.readFromPreferences(ParameterConstants.DEVICE_LOCATION_ID, ""))
      if(!jo.has(ParameterConstants.USER_ID) || jo.getString(ParameterConstants.USER_ID).isNullOrEmpty()) jo.put(ParameterConstants.USER_ID, DataStoreManager.readFromPreferences(ParameterConstants.USER_ID, ""))
      if(!jo.has(ParameterConstants.DEVICE_ID) || jo.getString(ParameterConstants.DEVICE_ID).isNullOrEmpty()) jo.put(ParameterConstants.DEVICE_ID, DataStoreManager.readFromPreferences(ParameterConstants.DEVICE_ID, ""))
      //showLog("MQTT_INV_DeviceID",DataStoreManager.readFromPreferences(ParameterConstants.DEVICE_ID, ""))
      if(!jo.has(ParameterConstants.LAT_LNG) || jo.getString(ParameterConstants.LAT_LNG).isNullOrEmpty()) jo.put(ParameterConstants.LAT_LNG, DataStoreManager.readFromPreferences(ParameterConstants.LAT_LNG, ""))
      if(!jo.has(ParameterConstants.DEVICE_DATE_TIME) || jo.getString(ParameterConstants.DEVICE_DATE_TIME).isNullOrEmpty()) jo.put(ParameterConstants.DEVICE_DATE_TIME, DateFormatUtils.getCurrentUTCTime())
      val preHeader = TopicConstants.INVENTORY+"_"+sessionType+"_"
      if(!jo.has(ParameterConstants.ID) || jo.getString(ParameterConstants.ID).isNullOrEmpty()) jo.put(ParameterConstants.ID, DataStoreManager.readFromPreferences(preHeader+ParameterConstants.ID, ""))
      if(!jo.has(ParameterConstants.SESSION_ID) || jo.getString(ParameterConstants.SESSION_ID).isNullOrEmpty()) jo.put(ParameterConstants.SESSION_ID, chkNull(sessionId,DataStoreManager.readFromPreferences(preHeader+ParameterConstants.DEVICE_SESSION_ID, sessionId)))
      //showLog("MQTT_INV_DeviceSessionID",chkNull(DataStoreManager.readFromPreferences(preHeader+ParameterConstants.DEVICE_SESSION_ID, ""),"empty"))
      if(!jo.has(ParameterConstants.TRANSACTION_TYPE) || jo.getString(ParameterConstants.TRANSACTION_TYPE).isNullOrEmpty()) jo.put(ParameterConstants.TRANSACTION_TYPE, chkNull(transactionType,DataStoreManager.readFromPreferences(preHeader+ParameterConstants.TRANSACTION_TYPE, transactionType)))
      if(!jo.has(ParameterConstants.ASSET_LOCATION_PATH) || jo.getString(ParameterConstants.ASSET_LOCATION_PATH).isNullOrEmpty()) jo.put(ParameterConstants.ASSET_LOCATION_PATH, DataStoreManager.readFromPreferences(preHeader+ParameterConstants.ASSET_LOCATION_PATH, ""))

      //Encoded Items
      val encodedArray = JSONArray()
      //Non-Encoded Items
      val nonEncodedArray = JSONArray()
      var maxPkId = 0
      tagInfoEntities.forEach { tag ->
        if (tag.pkId > maxPkId) maxPkId = tag.pkId
        val item = JSONObject()
        item.put(ParameterConstants.EPC, tag.epc)
        item.put(ParameterConstants.TID, tag.tid)
        item.put(ParameterConstants.BARCODE, tag.barcode)
        if(tag.barcode.isEmpty() || tag.barcode.equals(BarcodeConstants.NON_ENCODED,true) || tag.barcode.equals(BarcodeConstants.UNKNOWN,true) || tag.barcode.equals(BarcodeConstants.ALIEN,true))
          nonEncodedArray.put(item)
        else
          encodedArray.put(item)
      }
      jo.put(ParameterConstants.INV_MAX_ID, maxPkId)
      jo.put(ParameterConstants.ITEMS, encodedArray)
      jo.put(ParameterConstants.NON_ENCODED_ITEMS, nonEncodedArray)
      jo.put(ParameterConstants.IP_ADDRESS, DataStoreManager.readFromPreferences(ParameterConstants.IP_ADDRESS, ""))
      showLog("Publish Inventory Data", jo.toString())
      publish(getInventoryTopic(), jo.toString())
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  fun publishMovement(tagInfoEntity: TagInfoEntity,sessionType: String,transactionType: String,sessionId: String,sessionData: String) {
    val tagInfoEntities = ArrayList<TagInfoEntity>(0)
    tagInfoEntities.add(tagInfoEntity)
    publishMovement(tagInfoEntities,sessionType,transactionType,sessionId,chkNull(sessionData,tagInfoEntity.sessionData))
    showLog("Publish Movement Data",tagInfoEntities.toString())
  }

  fun publishMovement(tagInfoEntities: List<TagInfoEntity>,sessionType: String,transactionType: String,sessionId: String) {
      if(tagInfoEntities.isNullOrEmpty()) return
      showLog("MQTT_method","publishInventory_"+tagInfoEntities.size)
      val tagInfoEntity = tagInfoEntities.get(0)
      publishMovement(tagInfoEntities,sessionType,transactionType,sessionId,tagInfoEntity.sessionData)
  }

  fun publishMovement(tagInfoEntities: List<TagInfoEntity>,sessionType: String,transactionType: String,sessionId: String,sessionData: String) {
    if(tagInfoEntities.isNullOrEmpty()) return
    try {
      val jo = if(sessionData.isNotEmpty()) JSONObject(sessionData) else JSONObject()
      if(!jo.has(ParameterConstants.CUSTOMER_ID) || jo.getString(ParameterConstants.CUSTOMER_ID).isNullOrEmpty()) jo.put(ParameterConstants.CUSTOMER_ID, DataStoreManager.readFromPreferences(ParameterConstants.CUSTOMER_ID, ""))
      if(!jo.has(ParameterConstants.OPERATION_LOCATION_ID) || jo.getString(ParameterConstants.OPERATION_LOCATION_ID).isNullOrEmpty()) jo.put(ParameterConstants.OPERATION_LOCATION_ID, DataStoreManager.readFromPreferences(ParameterConstants.DEVICE_LOCATION_ID, ""))
      if(!jo.has(ParameterConstants.USER_ID) || jo.getString(ParameterConstants.USER_ID).isNullOrEmpty()) jo.put(ParameterConstants.USER_ID, DataStoreManager.readFromPreferences(ParameterConstants.USER_ID, ""))
      if(!jo.has(ParameterConstants.DEVICE_ID) || jo.getString(ParameterConstants.DEVICE_ID).isNullOrEmpty()) jo.put(ParameterConstants.DEVICE_ID, DataStoreManager.readFromPreferences(ParameterConstants.DEVICE_ID, ""))
      if(!jo.has(ParameterConstants.LAT_LNG) || jo.getString(ParameterConstants.LAT_LNG).isNullOrEmpty()) jo.put(ParameterConstants.LAT_LNG, DataStoreManager.readFromPreferences(ParameterConstants.LAT_LNG, ""))
      if(!jo.has(ParameterConstants.DEVICE_DATE_TIME) || jo.getString(ParameterConstants.DEVICE_DATE_TIME).isNullOrEmpty()) jo.put(ParameterConstants.DEVICE_DATE_TIME, DateFormatUtils.getCurrentUTCTime())
      val preHeader = TopicConstants.MOVEMENT+"_"+sessionType+"_"
      if(!jo.has(ParameterConstants.ID) || jo.getString(ParameterConstants.ID).isNullOrEmpty()) jo.put(ParameterConstants.ID, DataStoreManager.readFromPreferences(preHeader+ParameterConstants.ID, ""))
      if(!jo.has(ParameterConstants.SESSION_ID) || jo.getString(ParameterConstants.SESSION_ID).isNullOrEmpty()) jo.put(ParameterConstants.SESSION_ID, chkNull(sessionId,DataStoreManager.readFromPreferences(preHeader+ParameterConstants.DEVICE_SESSION_ID, sessionId)))
      if(!jo.has(ParameterConstants.TRANSACTION_TYPE) || jo.getString(ParameterConstants.TRANSACTION_TYPE).isNullOrEmpty()) jo.put(ParameterConstants.TRANSACTION_TYPE, chkNull(transactionType,DataStoreManager.readFromPreferences(preHeader+ParameterConstants.TRANSACTION_TYPE, transactionType)))
      if(!jo.has(ParameterConstants.ASSET_LOCATION_PATH) || jo.getString(ParameterConstants.ASSET_LOCATION_PATH).isNullOrEmpty()) jo.put(ParameterConstants.ASSET_LOCATION_PATH, DataStoreManager.readFromPreferences(preHeader+ParameterConstants.ASSET_LOCATION_PATH, ""))
      if(!jo.has(ParameterConstants.MOVE_AT_ASSET_LOCATION_PATH) || jo.getString(ParameterConstants.MOVE_AT_ASSET_LOCATION_PATH).isNullOrEmpty()) jo.put(ParameterConstants.MOVE_AT_ASSET_LOCATION_PATH, DataStoreManager.readFromPreferences(preHeader+ParameterConstants.MOVE_AT_ASSET_LOCATION_PATH, ""))

      //Encoded Items
      val encodedArray = JSONArray()
      //Non-Encoded Items
      val nonEncodedArray = JSONArray()
      var maxPkId = 0
      tagInfoEntities.forEach { tag ->
        if (tag.pkId > maxPkId) maxPkId = tag.pkId
        val item = JSONObject()
        item.put(ParameterConstants.EPC, tag.epc)
        item.put(ParameterConstants.TID, tag.tid)
        item.put(ParameterConstants.BARCODE, tag.barcode)
        if(tag.barcode.isEmpty() || tag.barcode.equals(BarcodeConstants.NON_ENCODED,true) || tag.barcode.equals(BarcodeConstants.UNKNOWN,true) || tag.barcode.equals(BarcodeConstants.ALIEN,true))
          nonEncodedArray.put(item)
        else
          encodedArray.put(item)
      }
      jo.put(ParameterConstants.INV_MAX_ID, maxPkId)
      jo.put(ParameterConstants.ITEMS, encodedArray)
      jo.put(ParameterConstants.NON_ENCODED_ITEMS, nonEncodedArray)
      jo.put(ParameterConstants.IP_ADDRESS, DataStoreManager.readFromPreferences(ParameterConstants.IP_ADDRESS, ""))
      showLog("Publish Movement Data", jo.toString())
      publish(getMovementTopic(), jo.toString())
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  fun publishReplenishment(tagInfoEntity: TagInfoEntity,sessionType: String,transactionType: String,sessionId: String,sessionData: String) {
    val tagInfoEntities = ArrayList<TagInfoEntity>(0)
    tagInfoEntities.add(tagInfoEntity)
    publishReplenishment(tagInfoEntities,sessionType,transactionType,sessionId,chkNull(sessionData,tagInfoEntity.sessionData))
    showLog("Publish Replenishment Data",tagInfoEntities.toString())
  }

  fun publishReplenishment(tagInfoEntities: List<TagInfoEntity>,sessionType: String,transactionType: String,sessionId: String) {
      if(tagInfoEntities.isNullOrEmpty()) return
      showLog("MQTT_method","publishReplenishment_"+tagInfoEntities.size)
      val tagInfoEntity = tagInfoEntities.get(0)
      publishReplenishment(tagInfoEntities,sessionType,transactionType,sessionId,tagInfoEntity.sessionData)
  }

  fun publishReplenishment(tagInfoEntities: List<TagInfoEntity>,sessionType: String,transactionType: String,sessionId: String,sessionData: String) {
    if(tagInfoEntities.isNullOrEmpty()) return
    try {
      val jo = if(sessionData.isNotEmpty()) JSONObject(sessionData) else JSONObject()
      if(!jo.has(ParameterConstants.CUSTOMER_ID) || jo.getString(ParameterConstants.CUSTOMER_ID).isNullOrEmpty()) jo.put(ParameterConstants.CUSTOMER_ID, DataStoreManager.readFromPreferences(ParameterConstants.CUSTOMER_ID, ""))
      if(!jo.has(ParameterConstants.OPERATION_LOCATION_ID) || jo.getString(ParameterConstants.OPERATION_LOCATION_ID).isNullOrEmpty()) jo.put(ParameterConstants.OPERATION_LOCATION_ID, DataStoreManager.readFromPreferences(LoginConstants.DEVICE_LOCATION_ID, ""))
      if(!jo.has(ParameterConstants.USER_ID) || jo.getString(ParameterConstants.USER_ID).isNullOrEmpty()) jo.put(ParameterConstants.USER_ID, DataStoreManager.readFromPreferences(ParameterConstants.USER_ID, ""))
      if(!jo.has(ParameterConstants.DEVICE_ID) || jo.getString(ParameterConstants.DEVICE_ID).isNullOrEmpty()) jo.put(ParameterConstants.DEVICE_ID, DataStoreManager.readFromPreferences(ParameterConstants.DEVICE_ID, ""))
      if(!jo.has(ParameterConstants.LAT_LNG) || jo.getString(ParameterConstants.LAT_LNG).isNullOrEmpty()) jo.put(ParameterConstants.LAT_LNG, DataStoreManager.readFromPreferences(ParameterConstants.LAT_LNG, ""))
      if(!jo.has(ParameterConstants.DEVICE_DATE_TIME) || jo.getString(ParameterConstants.DEVICE_DATE_TIME).isNullOrEmpty()) jo.put(ParameterConstants.DEVICE_DATE_TIME, DateFormatUtils.getCurrentUTCTime())
      val preHeader = TopicConstants.REPLENISHMENT+"_"+sessionType+"_"
      if(!jo.has(ParameterConstants.ID) || jo.getString(ParameterConstants.ID).isNullOrEmpty()) jo.put(ParameterConstants.ID, DataStoreManager.readFromPreferences(preHeader+ParameterConstants.ID, ""))
      if(!jo.has(ParameterConstants.SESSION_ID) || jo.getString(ParameterConstants.SESSION_ID).isNullOrEmpty()) jo.put(ParameterConstants.SESSION_ID, chkNull(sessionId,DataStoreManager.readFromPreferences(preHeader+ParameterConstants.DEVICE_SESSION_ID, sessionId)))
      if(!jo.has(ParameterConstants.TRANSACTION_TYPE) || jo.getString(ParameterConstants.TRANSACTION_TYPE).isNullOrEmpty()) jo.put(ParameterConstants.TRANSACTION_TYPE, chkNull(transactionType,DataStoreManager.readFromPreferences(preHeader+ParameterConstants.TRANSACTION_TYPE, transactionType)))
      if(!jo.has(ParameterConstants.ASSET_LOCATION_PATH) || jo.getString(ParameterConstants.ASSET_LOCATION_PATH).isNullOrEmpty()) jo.put(ParameterConstants.ASSET_LOCATION_PATH, DataStoreManager.readFromPreferences(preHeader+ParameterConstants.ASSET_LOCATION_PATH, ""))
      if(!jo.has(ParameterConstants.MOVE_AT_ASSET_LOCATION_PATH) || jo.getString(ParameterConstants.MOVE_AT_ASSET_LOCATION_PATH).isNullOrEmpty()) jo.put(ParameterConstants.MOVE_AT_ASSET_LOCATION_PATH, DataStoreManager.readFromPreferences(preHeader+ParameterConstants.MOVE_AT_ASSET_LOCATION_PATH, ""))

      //Encoded Items
      val encodedArray = JSONArray()
      //Non-Encoded Items
      val nonEncodedArray = JSONArray()
      var maxPkId = 0
      tagInfoEntities.forEach { tag ->
        if (tag.pkId > maxPkId) maxPkId = tag.pkId
        val item = JSONObject()
        item.put(ParameterConstants.EPC, tag.epc)
        item.put(ParameterConstants.TID, tag.tid)
        item.put(ParameterConstants.BARCODE, tag.barcode)
        if(tag.barcode.isEmpty() || tag.barcode.equals(BarcodeConstants.NON_ENCODED,true) || tag.barcode.equals(BarcodeConstants.UNKNOWN,true) || tag.barcode.equals(BarcodeConstants.ALIEN,true))
          nonEncodedArray.put(item)
        else
          encodedArray.put(item)
      }
      jo.put(ParameterConstants.INV_MAX_ID, maxPkId)
      jo.put(ParameterConstants.ITEMS, encodedArray)
      jo.put(ParameterConstants.NON_ENCODED_ITEMS, nonEncodedArray)
      jo.put(ParameterConstants.IP_ADDRESS, DataStoreManager.readFromPreferences(ParameterConstants.IP_ADDRESS, ""))
      showLog("Publish Replenishment Data", jo.toString())
      publish(getReplenishmentTopic(), jo.toString())
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  fun publishListUpdate(tagInfoEntities: List<TagInfoEntity>,sessionType: String,transactionType: String,sessionId: String){
     LogUtils.showLog("publishListUpdate",""+tagInfoEntities.size)
     tagInfoEntities.forEach { tagInfoEntity ->
         if(tagInfoEntity.sessionData!=null && hasKeys(JSONObject(tagInfoEntity.sessionData),arrayOf(ParameterConstants.SEARCH_TYPE_ID,ParameterConstants.SEARCH_TYPE_NAME,ParameterConstants.REFERENCE_NUMBER,ParameterConstants.SEARCH_VALUE)))
          publishListUpdate(tagInfoEntity,sessionType,transactionType,sessionId,tagInfoEntity.sessionData,"","","","")
     }
  }

  fun publishListUpdate(tagInfoEntities: List<TagInfoEntity>,sessionType: String,transactionType: String,sessionId: String,sessionData: String,searchTypeId:String,searchTypeName:String,referenceNumber:String,searchValue:String){
     LogUtils.showLog("publishListUpdate",""+tagInfoEntities.size)
     tagInfoEntities.forEach { tagInfoEntity -> publishListUpdate(tagInfoEntity,sessionType,transactionType,sessionId,sessionData,searchTypeId,searchTypeName,referenceNumber,searchValue) }
  }

  fun publishListUpdate(tagInfoEntity: TagInfoEntity?,sessionType: String,transactionType: String,sessionId: String,sessionData: String,searchTypeId:String,searchTypeName:String,referenceNumber:String,searchValue:String,barCode:String="",foundCount:Int=0){
        try {
            val jo = if(sessionData.isNotEmpty()) JSONObject(sessionData) else JSONObject()
            LogUtils.showLog("publishListUpdate_sessionData",""+jo.toString())
            if(!jo.has(ParameterConstants.CUSTOMER_ID) || jo.getString(ParameterConstants.CUSTOMER_ID).isNullOrEmpty()) jo.put(ParameterConstants.CUSTOMER_ID, DataStoreManager.readFromPreferences(ParameterConstants.CUSTOMER_ID, ""))
            if(!jo.has(ParameterConstants.BUSINESS_LINE_ID) || jo.getString(ParameterConstants.BUSINESS_LINE_ID).isNullOrEmpty()) jo.put(ParameterConstants.BUSINESS_LINE_ID, DataStoreManager.readFromPreferences(ParameterConstants.BUSINESS_LINE_ID, ""))
            if(!jo.has(ParameterConstants.LOCATION_ID) || jo.getString(ParameterConstants.LOCATION_ID).isNullOrEmpty()) jo.put(ParameterConstants.LOCATION_ID, DataStoreManager.readFromPreferences(LoginConstants.DEVICE_LOCATION_ID, ""))
            if(!jo.has(ParameterConstants.USER_ID) || jo.getString(ParameterConstants.USER_ID).isNullOrEmpty()) jo.put(ParameterConstants.USER_ID, DataStoreManager.readFromPreferences(ParameterConstants.USER_ID, ""))
            if(!jo.has(ParameterConstants.DEVICE_ID) || jo.getString(ParameterConstants.DEVICE_ID).isNullOrEmpty()) jo.put(ParameterConstants.DEVICE_ID, DataStoreManager.readFromPreferences(ParameterConstants.DEVICE_ID, ""))
            if(!jo.has(ParameterConstants.LAT_LNG) || jo.getString(ParameterConstants.LAT_LNG).isNullOrEmpty()) jo.put(ParameterConstants.LAT_LNG, DataStoreManager.readFromPreferences(ParameterConstants.LAT_LNG, ""))
            if(!jo.has(ParameterConstants.DEVICE_DATE_TIME) || jo.getString(ParameterConstants.DEVICE_DATE_TIME).isNullOrEmpty()) jo.put(ParameterConstants.DEVICE_DATE_TIME, DateFormatUtils.getCurrentUTCTime())
            val preHeader = TopicConstants.SEARCH_LIST_UPDATE+"_"+sessionType+"_"
            if(!jo.has(ParameterConstants.SESSION_TYPE) || jo.getString(ParameterConstants.SESSION_TYPE).isNullOrEmpty()) jo.put(ParameterConstants.SESSION_TYPE, chkNull(sessionType,DataStoreManager.readFromPreferences(preHeader+ParameterConstants.SESSION_TYPE, "")))
            if(!jo.has(ParameterConstants.ID) || jo.getString(ParameterConstants.ID).isNullOrEmpty()) jo.put(ParameterConstants.ID, DataStoreManager.readFromPreferences(preHeader+ParameterConstants.ID, ""))
            if(!jo.has(ParameterConstants.SESSION_ID) || jo.getString(ParameterConstants.SESSION_ID).isNullOrEmpty()) jo.put(ParameterConstants.SESSION_ID, chkNull(sessionId,DataStoreManager.readFromPreferences(preHeader+ParameterConstants.DEVICE_SESSION_ID, sessionId)))
            if(!jo.has(ParameterConstants.TRANSACTION_TYPE) || jo.getString(ParameterConstants.TRANSACTION_TYPE).isNullOrEmpty()) jo.put(ParameterConstants.TRANSACTION_TYPE, chkNull(transactionType,DataStoreManager.readFromPreferences(preHeader+ParameterConstants.TRANSACTION_TYPE, transactionType)))
            if(!jo.has(ParameterConstants.SEARCH_TYPE_ID) || jo.getString(ParameterConstants.SEARCH_TYPE_ID).isNullOrEmpty()) jo.put(ParameterConstants.SEARCH_TYPE_ID, chkNull(searchTypeId,DataStoreManager.readFromPreferences(preHeader+ParameterConstants.SEARCH_TYPE_ID, searchTypeId)))
            if(!jo.has(ParameterConstants.SEARCH_TYPE_NAME) || jo.getString(ParameterConstants.SEARCH_TYPE_NAME).isNullOrEmpty()) jo.put(ParameterConstants.SEARCH_TYPE_NAME, chkNull(searchTypeName,DataStoreManager.readFromPreferences(preHeader+ParameterConstants.SEARCH_TYPE_NAME, searchTypeName)))
            if(!jo.has(ParameterConstants.SEARCH_VALUE) || jo.getString(ParameterConstants.SEARCH_VALUE).isNullOrEmpty()) jo.put(ParameterConstants.SEARCH_VALUE, chkNull(searchValue,DataStoreManager.readFromPreferences(preHeader+ParameterConstants.SEARCH_VALUE, searchValue)))
            if(!jo.has(ParameterConstants.REFERENCE_NUMBER) || jo.getString(ParameterConstants.REFERENCE_NUMBER).isNullOrEmpty()) jo.put(ParameterConstants.REFERENCE_NUMBER, chkNull(referenceNumber,DataStoreManager.readFromPreferences(preHeader+ParameterConstants.REFERENCE_NUMBER, referenceNumber)))

            if(!jo.has(ParameterConstants.ASSET_LOCATION_NAME) || jo.getString(ParameterConstants.ASSET_LOCATION_NAME).isNullOrEmpty()) jo.put(ParameterConstants.ASSET_LOCATION_NAME,DataStoreManager.readFromPreferences(preHeader + ParameterConstants.ASSET_LOCATION_NAME, ""))
            if(!jo.has(ParameterConstants.ASSET_LOCATION_PATH) || jo.getString(ParameterConstants.ASSET_LOCATION_PATH).isNullOrEmpty()) jo.put(ParameterConstants.ASSET_LOCATION_PATH,DataStoreManager.readFromPreferences(preHeader + ParameterConstants.ASSET_LOCATION_PATH,""))

            if(!jo.has(ParameterConstants.START_DATE_TIME) || jo.getString(ParameterConstants.START_DATE_TIME).isNullOrEmpty()) jo.put(ParameterConstants.START_DATE_TIME, DateFormatUtils.getCurrentUTCTime())
            if(!jo.has(ParameterConstants.END_DATE_TIME) || jo.getString(ParameterConstants.END_DATE_TIME).isNullOrEmpty()) jo.put(ParameterConstants.END_DATE_TIME,DateFormatUtils.getCurrentUTCTime())

            if(tagInfoEntity!=null) {
                jo.put(ParameterConstants.BARCODE, tagInfoEntity.barcode)
                jo.put(ParameterConstants.REMARK, tagInfoEntity.remark)
                jo.put(ParameterConstants.TID, tagInfoEntity.tid)
                jo.put("pickedAt", formatToUTCTime(tagInfoEntity.insertTime))
                if(tagInfoEntity.newEpc.isNotEmpty()){
                  jo.put(ParameterConstants.OPERATION, "decode")
                  jo.put(ParameterConstants.EPC, tagInfoEntity.newEpc)
                  jo.put(ParameterConstants.OLD_EPC,tagInfoEntity.epc)
                  jo.put(ParameterConstants.REMARK, tagInfoEntity.writeFailReason)
                  jo.put("decodedAt", formatToUTCTime(tagInfoEntity.endTime))
                }
                else {
                    jo.put(ParameterConstants.EPC, tagInfoEntity.epc)
                    jo.put(ParameterConstants.OPERATION, "pick")
                }
            }
            else{
                //code to handle mark
                //jo.put("foundCount", foundCount)
                jo.put(ParameterConstants.BARCODE, barCode)
                jo.put(ParameterConstants.EPC, "-")
                jo.put(ParameterConstants.REMARK, "")
                jo.put(ParameterConstants.TID, "-")
                jo.put(ParameterConstants.OPERATION, "FOUND")
                jo.put("foundAt", formatToUTCTime(DateFormatUtils.getCurrentTime()))
            }

            showLog("Publish Search List Update Data", jo.toString())
            val jarray = JSONArray()
            jarray.put(jo)
            publish(getSearchListUpdateTopic(), jarray.toString())
        }
        catch (e: Exception) {
            e.printStackTrace()
        }
  }

  fun publishEncode(tagInfoEntity: TagInfoEntity,sessionType: String,transactionType: String,sessionId: String,sessionData: String) {
    val tagInfoEntities = ArrayList<TagInfoEntity>(0)
    tagInfoEntities.add(tagInfoEntity)
    publishEncode(tagInfoEntities,sessionType,transactionType,sessionId,sessionData)
    showLog("Publish Encode Data",tagInfoEntities.toString())
  }

  fun publishEncode(tagInfoEntities: List<TagInfoEntity>,sessionType: String,transactionType: String,sessionId: String,sessionData: String) {
    if(tagInfoEntities.isNullOrEmpty()) return
    try {
      val jo = if(sessionData.isNotEmpty()) JSONObject(sessionData) else JSONObject()
      if(!jo.has(ParameterConstants.USER_ID) || jo.getString(ParameterConstants.USER_ID).isNullOrEmpty()) jo.put(ParameterConstants.USER_ID, DataStoreManager.readFromPreferences(ParameterConstants.USER_ID, ""))
      if(!jo.has(ParameterConstants.DEVICE_ID) || jo.getString(ParameterConstants.DEVICE_ID).isNullOrEmpty()) jo.put(ParameterConstants.DEVICE_ID, DataStoreManager.readFromPreferences(ParameterConstants.DEVICE_ID, ""))
      if(!jo.has(ParameterConstants.CUSTOMER_ID) || jo.getString(ParameterConstants.CUSTOMER_ID).isNullOrEmpty()) jo.put(ParameterConstants.CUSTOMER_ID, DataStoreManager.readFromPreferences(ParameterConstants.CUSTOMER_ID, ""))
      if(!jo.has(ParameterConstants.OPERATION_LOCATION_ID) || jo.getString(ParameterConstants.OPERATION_LOCATION_ID).isNullOrEmpty()) jo.put(ParameterConstants.OPERATION_LOCATION_ID, DataStoreManager.readFromPreferences(LoginConstants.DEVICE_LOCATION_ID, ""))
      if(!jo.has(ParameterConstants.LAT_LNG) || jo.getString(ParameterConstants.LAT_LNG).isNullOrEmpty()) jo.put(ParameterConstants.LAT_LNG, DataStoreManager.readFromPreferences(ParameterConstants.LAT_LNG, ""))
      val preHeader = TopicConstants.ENCODE+"_"+sessionType+"_"
      if(!jo.has(ParameterConstants.ID) || jo.getString(ParameterConstants.ID).isNullOrEmpty()) jo.put(ParameterConstants.ID, DataStoreManager.readFromPreferences(preHeader+ParameterConstants.ID, ""))
      if(!jo.has(ParameterConstants.DEVICE_SESSION_ID) || jo.getString(ParameterConstants.DEVICE_SESSION_ID).isNullOrEmpty()) jo.put(ParameterConstants.DEVICE_SESSION_ID, chkNull(sessionId,DataStoreManager.readFromPreferences(preHeader+ParameterConstants.DEVICE_SESSION_ID, sessionId)))
      if(tagInfoEntities.size==1) jo.put(ParameterConstants.START_DATE, formatToUTCTime(tagInfoEntities.get(0).startTime))
      if(tagInfoEntities.size==1) jo.put(ParameterConstants.END_DATE, formatToUTCTime(tagInfoEntities.get(0).endTime))
      if(tagInfoEntities.size==1) jo.put(ParameterConstants.BARCODE, tagInfoEntities.get(0).barcode)
      if(!jo.has(ParameterConstants.QTY) || jo.getString(ParameterConstants.QTY).isNullOrEmpty()) jo.put(ParameterConstants.QTY, tagInfoEntities.size)
      if(!jo.has(ParameterConstants.TYPE) || jo.getString(ParameterConstants.TYPE).isNullOrEmpty()) jo.put(ParameterConstants.TYPE, "Online")
      val ja: JSONArray = JSONArray()
      var startDate=""
      var endDate=""
      var statuses = ArrayList<String>(0)
      for(tagInfoEntity:TagInfoEntity in tagInfoEntities) {
        val jao: JSONObject = JSONObject()
        if(!jo.has(ParameterConstants.BARCODE)) jo.put(ParameterConstants.BARCODE, tagInfoEntity.barcode)
        if(!jo.has(ParameterConstants.START_DATE) && startDate.isEmpty() || startDate.compareTo(tagInfoEntity.startTime)<0)
          startDate= formatToUTCTime(tagInfoEntity.startTime)
        if(!jo.has(ParameterConstants.END_DATE) && endDate.isEmpty() || endDate.compareTo(tagInfoEntity.endTime)>0)
          endDate= formatToUTCTime(tagInfoEntity.endTime)
        if(!statuses.contains(tagInfoEntity.tagWriteStatus))statuses.add(tagInfoEntity.tagWriteStatus)
        jao.put(ParameterConstants.ID, tagInfoEntity.id)
        jao.put(ParameterConstants.CUSTOMER_ID, tagInfoEntity.customerId)
        jao.put(ParameterConstants.ENCODE_LOG_ID, tagInfoEntity.encodeLogId)
        jao.put(ParameterConstants.OLD_EPC, tagInfoEntity.epc)
        jao.put(ParameterConstants.NEW_EPC, tagInfoEntity.newEpc)
        jao.put(ParameterConstants.TID, tagInfoEntity.tid)
        jao.put(ParameterConstants.COMPLETION_STATUS, tagInfoEntity.tagWriteStatus)
        jao.put(ParameterConstants.COMPLETION_REMARK, tagInfoEntity.writeFailReason)
        ja.put(jao)
      }
      if(!jo.has(ParameterConstants.START_DATE) && startDate.isNotEmpty()){
        jo.put(ParameterConstants.START_DATE,startDate)
      }
      if(!jo.has(ParameterConstants.END_DATE) && endDate.isNotEmpty()){
        jo.put(ParameterConstants.END_DATE,endDate)
      }
      if(!jo.has(ParameterConstants.COMPLETION_STATUS) && statuses.isNotEmpty()){
         jo.put(ParameterConstants.COMPLETION_STATUS,if(statuses.size>1) StatusConstants.INPROCESS else statuses.get(0))
      }
      jo.put(ParameterConstants.DETAILS, ja)
      showLog("Publish Encode Data:-",jo.toString())
      publish(getEncodeTopic(), jo.toString())
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  fun publishDecode(tagInfoEntity: TagInfoEntity,sessionType: String,decodeType: String,transactionType: String,sessionId: String) {
    val tagInfoEntities = ArrayList<TagInfoEntity>(0)
    tagInfoEntities.add(tagInfoEntity)
    publishDecode(tagInfoEntities,sessionType,decodeType,transactionType,sessionId,tagInfoEntity.remark)
    showLog("Publish Decode Data",tagInfoEntities.toString())
  }

  fun publishDecode(tagInfoEntities: List<TagInfoEntity>,sessionType: String,decodeType:String,transactionType: String,sessionId: String, userRemark:String) {
      showLog("Publish Decode Data",tagInfoEntities.toString())
    if(tagInfoEntities.isNullOrEmpty()) return
    try {
      val jo = JSONObject()
      jo.put(ParameterConstants.USER_ID, DataStoreManager.readFromPreferences(ParameterConstants.USER_ID, ""))
      jo.put(ParameterConstants.DEVICE_ID, DataStoreManager.readFromPreferences(ParameterConstants.DEVICE_ID, ""))
      jo.put(ParameterConstants.CUSTOMER_ID, DataStoreManager.readFromPreferences(ParameterConstants.CUSTOMER_ID, ""))
      jo.put(ParameterConstants.OPERATION_LOCATION_ID, DataStoreManager.readFromPreferences(LoginConstants.DEVICE_LOCATION_ID, ""))
      jo.put(ParameterConstants.LAT_LNG, DataStoreManager.readFromPreferences(ParameterConstants.LAT_LNG, ""))
      val preHeader = TopicConstants.DECODE+"_"+sessionType+"_"
      jo.put(ParameterConstants.ID, DataStoreManager.readFromPreferences(preHeader+ParameterConstants.ID, ""))
      jo.put(ParameterConstants.DEVICE_SESSION_ID, chkNull(sessionId,DataStoreManager.readFromPreferences(preHeader+ParameterConstants.DEVICE_SESSION_ID, sessionId)))
      jo.put(ParameterConstants.DECODE_TYPE,chkNull(decodeType, chkNull(transactionType,DataStoreManager.readFromPreferences(preHeader+ParameterConstants.TRANSACTION_TYPE, transactionType))))
      jo.put(ParameterConstants.REMARK, chkNull(userRemark,DataStoreManager.readFromPreferences(preHeader+ParameterConstants.REMARK, "")))

      //val date = SimpleDateFormat(DateFormatConstants.API_DATE_TIME_FORMAT).format(Calendar.getInstance().time)
      if(tagInfoEntities.size==1) jo.put(ParameterConstants.START_DATE, formatToUTCTime(tagInfoEntities.get(0).startTime))
      if(tagInfoEntities.size==1) jo.put(ParameterConstants.END_DATE, formatToUTCTime(tagInfoEntities.get(0).endTime))
      if(tagInfoEntities.size==1) jo.put(ParameterConstants.COMPLETION_STATUS, tagInfoEntities.get(0).tagWriteStatus)

      val ja: JSONArray = JSONArray()
      var startDate=""
      var endDate=""
      var statuses = ArrayList<String>(0)
      for(tagInfoEntity:TagInfoEntity in tagInfoEntities) {
        val jao: JSONObject = JSONObject()
        if(!jo.has(ParameterConstants.START_DATE) && startDate.isEmpty() || startDate.compareTo(tagInfoEntity.startTime)<0)
          startDate= formatToUTCTime(tagInfoEntity.startTime)
        if(!jo.has(ParameterConstants.END_DATE) && endDate.isEmpty() || endDate.compareTo(tagInfoEntity.endTime)>0)
          endDate= formatToUTCTime(tagInfoEntity.endTime)
        if(!jo.has(ParameterConstants.COMPLETION_STATUS)) jo.put(ParameterConstants.COMPLETION_STATUS, tagInfoEntity.tagWriteStatus)
        if(!statuses.contains(tagInfoEntity.tagWriteStatus))statuses.add(tagInfoEntity.tagWriteStatus)
        jao.put(ParameterConstants.BARCODE, tagInfoEntity.barcode)
        jao.put(ParameterConstants.OLD_EPC, tagInfoEntity.epc)
        jao.put(ParameterConstants.NEW_EPC, tagInfoEntity.newEpc)
        jao.put(ParameterConstants.TID, tagInfoEntity.tid)
        jao.put(ParameterConstants.COMPLETION_STATUS, tagInfoEntity.tagWriteStatus)
        jao.put(ParameterConstants.COMPLETION_REMARK, tagInfoEntity.writeFailReason)
        ja.put(jao)
      }
      if(!jo.has(ParameterConstants.START_DATE) && startDate.isNotEmpty()){
        jo.put(ParameterConstants.START_DATE,startDate)
      }
      if(!jo.has(ParameterConstants.END_DATE) && endDate.isNotEmpty()){
        jo.put(ParameterConstants.END_DATE,endDate)
      }
      if(!jo.has(ParameterConstants.COMPLETION_STATUS) && statuses.isNotEmpty()){
        jo.put(ParameterConstants.COMPLETION_STATUS,if(statuses.size>1) StatusConstants.INPROCESS else statuses.get(0))
      }
      jo.put(ParameterConstants.DETAILS, ja)
      showLog("Publish Decode Data:-",jo.toString())
      publish(getDecodeTopic(), jo.toString())
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  /*fun publishSearch(sessionType: String) {
    publishSearch(sessionType)
    showLog("Publish Search Data",tagInfoEntities.toString())
  }*/

  fun publishSearch(searchLog: SearchLogEntity) {publishSearch(searchLog,searchLog.sessionType,searchLog.transactionType)}
  fun publishSearch(searchLog: SearchLogEntity?=null, sessionType: String, transactionType: String) {
    try {
      val jo = if(searchLog!=null && searchLog.sessionData.isNotEmpty()) JSONObject(searchLog.sessionData) else JSONObject()
      if(!jo.has(ParameterConstants.DEVICE_ID) || jo.getString(ParameterConstants.DEVICE_ID).isNullOrEmpty()) jo.put(ParameterConstants.DEVICE_ID, DataStoreManager.readFromPreferences(ParameterConstants.DEVICE_ID, ""))
      if(!jo.has(ParameterConstants.CUSTOMER_ID) || jo.getString(ParameterConstants.CUSTOMER_ID).isNullOrEmpty()) jo.put(ParameterConstants.CUSTOMER_ID, DataStoreManager.readFromPreferences(ParameterConstants.CUSTOMER_ID, ""))
      if(!jo.has(ParameterConstants.BUSINESS_LINE_ID) || jo.getString(ParameterConstants.BUSINESS_LINE_ID).isNullOrEmpty()) jo.put(ParameterConstants.BUSINESS_LINE_ID, DataStoreManager.readFromPreferences(ParameterConstants.BUSINESS_LINE_ID, ""))
      if(!jo.has(ParameterConstants.LOCATION_ID) || jo.getString(ParameterConstants.LOCATION_ID).isNullOrEmpty()) jo.put(ParameterConstants.LOCATION_ID, DataStoreManager.readFromPreferences(LoginConstants.DEVICE_LOCATION_ID, ""))
      if(!jo.has(ParameterConstants.CREATED_BY) || jo.getString(ParameterConstants.CREATED_BY).isNullOrEmpty()) jo.put(ParameterConstants.CREATED_BY, DataStoreManager.readFromPreferences(ParameterConstants.USER_ID,""))
      val preHeader = TopicConstants.SEARCH+"_"+sessionType+"_"
      //jo.put(ParameterConstants.ID, DataStoreManager.readFromPreferences(preHeader+ParameterConstants.ID, ""))
      //jo.put(ParameterConstants.DEVICE_SESSION_ID, DataStoreManager.readFromPreferences(preHeader+ParameterConstants.DEVICE_SESSION_ID, ""))
      /*if(!jo.has(ParameterConstants.ID) || jo.getInt(ParameterConstants.ID,0)<=0)*/ jo.put(ParameterConstants.ID, if(searchLog!=null) chkNull(searchLog.pkId,0) else 0)
      /*if(!jo.has(ParameterConstants.SEARCH_TYPE_NAME) || jo.getString(ParameterConstants.SEARCH_TYPE_NAME).isNullOrEmpty())*/ jo.put(ParameterConstants.SEARCH_TYPE_NAME,if(searchLog!=null) searchLog.sessionType else chkNull(sessionType,DataStoreManager.readFromPreferences(preHeader+ParameterConstants.SEARCH_TYPE_NAME, sessionType)))
      /*if(!jo.has(ParameterConstants.SEARCH_VALUE_TYPE) || jo.getString(ParameterConstants.SEARCH_VALUE_TYPE).isNullOrEmpty())*/ jo.put(ParameterConstants.SEARCH_VALUE_TYPE, if(searchLog!=null) searchLog.searchType else DataStoreManager.readFromPreferences(preHeader+ParameterConstants.SEARCH_VALUE_TYPE, ""))
      /*if(!jo.has(ParameterConstants.SEARCH_VALUE) || jo.getString(ParameterConstants.SEARCH_VALUE).isNullOrEmpty())*/ jo.put(ParameterConstants.SEARCH_VALUE, if(searchLog!=null) searchLog.searchValue else DataStoreManager.readFromPreferences(preHeader+ParameterConstants.SEARCH_VALUE, ""))
      /*if(!jo.has(ParameterConstants.REFERENCE_NUMBER) || jo.getString(ParameterConstants.REFERENCE_NUMBER).isNullOrEmpty())*/ jo.put(ParameterConstants.REFERENCE_NUMBER, if(searchLog!=null) searchLog.referenceNumber else  DataStoreManager.readFromPreferences(preHeader+ParameterConstants.REFERENCE_NUMBER, ""))
      /*if(!jo.has(ParameterConstants.CREATED_AT) || jo.getString(ParameterConstants.CREATED_AT).isNullOrEmpty())*/ jo.put(ParameterConstants.CREATED_AT, formatToUTCTime(if(searchLog!=null) searchLog.startTime else DataStoreManager.readFromPreferences(preHeader+ ParameterConstants.START_DATE_TIME,"")))
      /*if(!jo.has(ParameterConstants.START_DATE_TIME) || jo.getString(ParameterConstants.START_DATE_TIME).isNullOrEmpty())*/ jo.put(ParameterConstants.START_DATE_TIME,formatToUTCTime(if(searchLog!=null) searchLog.startTime else  DataStoreManager.readFromPreferences(preHeader+ ParameterConstants.START_DATE_TIME,"")))
      /*if(!jo.has(ParameterConstants.END_DATE_TIME) || jo.getString(ParameterConstants.END_DATE_TIME).isNullOrEmpty())*/ jo.put(ParameterConstants.END_DATE_TIME, formatToUTCTime(if(searchLog!=null) searchLog.endTime else  DataStoreManager.readFromPreferences(preHeader+ ParameterConstants.END_DATE_TIME,"")))

      showLog("Publish Search Data:-",jo.toString())
      publish(getSearchTopic(), jo.toString())
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  fun checkConnection() : Boolean {
    if (isInitialized()) {
      showToast("MqttClient Not Initialized")
      initialize(appContext, DataStoreManager.readFromPreferences(ParameterConstants.BROKER_URL,"").trim())//BrokerConstants.BROKER_URL))
      return false;
    }
    else if(!isConnected()) {
      showToast("Not Connected")
      connect()
      return false;
    }
    return true;
  }

  @SuppressLint("StaticFieldLeak")
  fun publish(topic: String, message: String) {
    if(topic.isNullOrEmpty())  {
      MqttDiagnosticTracker.onDroppedMessage()
      return
    }
    showLog("MQTT_method","publish=>"+topic+"_"+isConnected())
    when {
      !isInitialized()-> {
          MqttDiagnosticTracker.onPublishFailure("client_not_initialized")
          initialize(appContext,DataStoreManager.readFromPreferences(ParameterConstants.BROKER_URL,"").trim());
          showToast("Not initialized to MQTT broker")
      }
      !isConnected() -> {
        MqttDiagnosticTracker.onPublishFailure("client_not_connected")
        connect()
        showToast("Not connected to MQTT broker")
        //Toast.makeText(appContext, "Not connected to MQTT broker", Toast.LENGTH_SHORT).show()
      }

      topic.isEmpty() -> {
        showToast("Topic cannot be empty")
        //Toast.makeText(appContext, "Topic cannot be empty", Toast.LENGTH_SHORT).show()
      }

      message.isEmpty() -> {
        MqttDiagnosticTracker.onPublishFailure("empty_message")
        showToast("Message cannot be empty")
      }

      else -> {
        val publishStartMs = System.currentTimeMillis()
        MqttDiagnosticTracker.onPublishAttempt()
        showToast("Publish topic: "+topic)
        showToast("Publish Message:"+message)
        val mqttMessage = MqttMessage().apply {
          payload = message.toByteArray()
          qos = 1
        }
        val publishResult: IMqttDeliveryToken = mqttClient.publish(topic, mqttMessage)
        FileUtils.writeMqttLog(serverUrl = mqttClient.serverURI, topic = topic+"_publish", message = message)
        showToast("publishResult:" + topic + publishResult.message)
        //LogUtils.showLog("MQTT_PublishResult", "publish: " + topic + publishResult.message)
        // Assign a listener to this specific token
        publishResult.setActionCallback(object : IMqttActionListener {
              override fun onSuccess(asyncActionToken: IMqttToken) {
                  // PUBACK received successfully
                  MqttDiagnosticTracker.onPublishSuccess(System.currentTimeMillis() - publishStartMs)
                  showLog("MQTT_PublishResult", "Publish Successful! Packet ID: " + asyncActionToken.getMessageId())
                  processMessageUpdate(topic,message,true)
              }

              override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable) {
                  // Broker failed to respond, or connection dropped
                  MqttDiagnosticTracker.onPublishFailure(exception.message, System.currentTimeMillis() - publishStartMs)
                  showLog("MQTT_PublishResult", "Publish Failed: " + exception.message)
              }
        })
      }
    }
  }

  public fun showLog(tag:String,message:String){
    LogUtils.showLog(tag,message)
  }
}
