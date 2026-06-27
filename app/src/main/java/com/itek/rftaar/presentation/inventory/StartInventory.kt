package com.itek.rftaar.presentation.inventory

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController
import com.itek.rftaar.DataHolder
import com.itek.rftaar.R
import com.itek.rftaar.ReaderActivity
import com.itek.rftaar.api.constants.ParameterConstants
import com.itek.rftaar.api.constants.UrlConstants
import com.itek.rftaar.core.common.constants.LoginConstants
import com.itek.rftaar.core.common.utils.BaseUtils
import com.itek.rftaar.core.common.utils.LogUtils
import com.itek.rftaar.core.common.utils.ParseUtils
import com.itek.rftaar.core.common.utils.ParseUtils.extractString
import com.itek.rftaar.core.database.AppDatabase
import com.itek.rftaar.core.database.DataStoreManager
import com.itek.rftaar.domain.model.LocationModel
import com.itek.rftaar.mqtt.constants.InventoryConstants
import com.itek.rftaar.mqtt.constants.TopicConstants
import com.itek.rftaar.presentation.commonComp.AppSnackBar
import com.itek.rftaar.presentation.commonComp.AppSnackBarData
import com.itek.rftaar.presentation.commonComp.BottomSheetType
import com.itek.rftaar.presentation.commonComp.CommonButton
import com.itek.rftaar.presentation.commonComp.ErrorAppSnackBarData
import com.itek.rftaar.presentation.commonComp.GernericBasicTextField
import com.itek.rftaar.presentation.commonComp.InventoryPulseCircle
import com.itek.rftaar.presentation.commonComp.SetValueActions
import com.itek.rftaar.presentation.commonComp.SetValueContent
import com.itek.rftaar.presentation.commonComp.SetValueUiConfig
import com.itek.rftaar.presentation.commonComp.SnackbarController
import com.itek.rftaar.presentation.commonComp.TopBarContent
import com.itek.rftaar.presentation.commonComp.UploadStatusChip
import com.itek.rftaar.presentation.commonComp.openSheet
import com.itek.rftaar.presentation.encoding.DeviceSettings
import com.itek.rftaar.presentation.encoding.SessionContent
import com.itek.rftaar.presentation.viewmodel.ApiViewModel
import com.itek.rftaar.presentation.viewmodel.ReaderViewModel
import com.itek.rftaar.reader.constants.ActionConstants
import com.itek.rftaar.ui.theme.BackGround
import com.itek.rftaar.ui.theme.BlackColor
import com.itek.rftaar.ui.theme.ButtonGray
import com.itek.rftaar.ui.theme.CommonTypography
import com.itek.rftaar.ui.theme.LightGray
import com.itek.rftaar.ui.theme.OutlineDefault
import com.itek.rftaar.ui.theme.RedColor
import com.itek.rftaar.ui.theme.ShadowColor
import com.itek.rftaar.ui.theme.TabColor
import com.itek.rftaar.ui.theme.TextSubtext
import com.itek.rftaar.ui.theme.WhiteColor
import com.itek.rftaar.ui.theme.Yellow
import com.itek.rftaar.utils.CommonUtils.chkNull
import com.itek.rftaar.utils.CommonUtils.chkTrue
import com.itek.rftaar.utils.NetworkUtils.isInternetConnected
import com.itek.rftaar.utils.ZoneUtils.processChildren
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartInventory(
    modifier: Modifier,
    navController: NavHostController,
    label: String,
    menuCode: String,
    //readerViewModel: ReaderViewModel = hiltViewModel(),
    apiViewModel: ApiViewModel = hiltViewModel(),
    searchParams: Map<String, Any> = emptyMap()
) {
    val context = LocalContext.current
    val activity = context as? ReaderActivity
    //val powerSheet = remember { mutableStateOf(false) }
    val readerViewModel = if (activity != null) activity.findReaderViewModel() else hiltViewModel()
    val scope = rememberCoroutineScope()
    //val sessionSheet = remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { sheetValue ->
            sheetValue != SheetValue.Hidden
        }
    )
    val showSheet = remember { mutableStateOf(false) }
    val preHeader = TopicConstants.INVENTORY + "_" + menuCode + "_"
    val id = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.ID, "")
    val deviceSessionId = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.DEVICE_SESSION_ID, "")
    val transactionType = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.TRANSACTION_TYPE, if (menuCode.contains("add", true)) InventoryConstants.INVENTORY_ADD else InventoryConstants.INVENTORY)
    val assetLocationName = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.ASSET_LOCATION_NAME, "")
    val assetLocationPath = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.ASSET_LOCATION_PATH, "")
    val readerPower = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.READER_POWER, 30)
    val setPower = rememberSaveable(deviceSessionId) { mutableStateOf(readerPower) }
    LaunchedEffect(setPower.value) {
        readerViewModel.setPower(setPower.value)
    }
    LogUtils.showLog("assetLocationPath", "StartInventory:$assetLocationName \n $assetLocationPath ")
    val locationList = remember { mutableStateListOf<LocationModel>() }
    val selectedLocationId = remember { mutableStateOf<LocationModel?>(null) }
    val selectedLocation = remember { mutableStateOf(assetLocationName) }
    val lastTagCount = remember { mutableStateOf(0) }

    if (activity != null) {
        if (deviceSessionId.isNullOrEmpty() || id.isNullOrEmpty()) readerViewModel.onCreate()
        readerViewModel.setSessionAndTransactionType(menuCode, transactionType, topic=TopicConstants.INVENTORY)
        if (deviceSessionId.isNullOrEmpty() || id.isNullOrEmpty()) {
            apiViewModel.callApi(
                UrlConstants.LOCATION_SUB_ZONES, appendData = DataStoreManager.readFromPreferences(
                    LoginConstants.DEVICE_LOCATION_ID,
                    ""
                )
            )
        }
        else {
            if(deviceSessionId.isNotEmpty() && id.isNotEmpty()) {
                val sessionData = JSONObject()
                sessionData.put(ParameterConstants.SESSION_ID, deviceSessionId)
                sessionData.put(ParameterConstants.ID, id)
                sessionData.put(ParameterConstants.TRANSACTION_TYPE, transactionType)
                sessionData.put(ParameterConstants.ASSET_LOCATION_PATH, assetLocationPath)
                readerViewModel.setSessionId(deviceSessionId, sessionData.toString())
            }
        }
    }
    val snackbarController = remember { SnackbarController() }
    val hasActiveSession = !deviceSessionId.isNullOrEmpty() && !id.isNullOrEmpty()
    val showLocationPicker = remember { mutableStateOf(false) }
    val isInvOn = readerViewModel.isInventoryOn().observeAsState()
    val isApiLoading = apiViewModel.isLoading.observeAsState()
    val isOnDataUploaded = remember { mutableStateOf(false) }
    //val sessionTagCount = remember { mutableStateOf(0) }
    LogUtils.showLog("isInvOn", "StartInventory: ${isInvOn.value}")
    val currentSheet = remember { mutableStateOf(BottomSheetType.NONE) }
    val db = AppDatabase.getDbInstance(context)

    val tagCount = db.tagInfoDao().getTotalCount(menuCode, transactionType).observeAsState(initial = 0)
    val uploadedTagCount = db.tagInfoDao().getUploadedCount(menuCode, transactionType).observeAsState(initial = 0)
    val validCount = db.tagInfoDao().getValidCount(menuCode, transactionType).observeAsState(initial = 0)
    val invalidCount = db.tagInfoDao().getInvalidCount(menuCode, transactionType).observeAsState(initial = 0)
    val unEncodedCount = db.tagInfoDao().getUnencodedCount(menuCode,transactionType).observeAsState(0)
    val alienCount = db.tagInfoDao().getAlienCount(menuCode,transactionType).observeAsState(initial = 0)
    val isLoader = remember { mutableStateOf(false) }
    LogUtils.showLog("tagCount", "StartInventory: ${isInvOn.value}\n${tagCount.value} \n ${validCount.value} \n${invalidCount.value}")

    BackHandler(enabled = true) {
        if (chkTrue(isInvOn.value) || chkTrue(isApiLoading.value)) return@BackHandler
        scope.launch {
            val preHeader = TopicConstants.INVENTORY + "_" + menuCode + "_"
            val deviceSessionId = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.DEVICE_SESSION_ID, "")
            val id = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.ID, "")
            if (deviceSessionId.isNullOrEmpty() || id.isNullOrEmpty()) {
                DataStoreManager.saveToPreferences(preHeader + ParameterConstants.READER_POWER, 30)
                if(navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                    readerViewModel.clearSessionAndTransactionType()
                    navController.popBackStack()
                }
            } else {
                openSheet(
                    scope = scope,
                    sheetState = sheetState,
                    showSheet = showSheet,
                    currentSheet = currentSheet,
                    sheet = BottomSheetType.SESSION
                )
            }
        }
    }

    Box() {
        Scaffold(
            bottomBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(WhiteColor),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    StartInventoryBottomBar(
                        readerViewModel,
                        selectedLocation,
                        showLocationPicker.value,
                        tagCount,
                        uploadedTagCount,
                        isOnDataUploaded,
                        isInvOn,
                        isLoader
                    )
                }
            },
            modifier = modifier.background(BackGround)
        ) { innerPadding ->

            Box(
                modifier = Modifier
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(
                                Color(0xFFF3F3F3),
                                Color(0xFFFFFFFF),
                                Color(0xFFFFFFFF),
                                Color(0xFFFFFFFF)
                            )
                        )
                    )
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                val inventoryArgs = InventoryScreenArgs(
                    context = context,
                    label = label,
                    menuCode = menuCode,
                    transactionType = transactionType,
                    scope = scope,
                    sheetState = sheetState,
                    showSheet = showSheet,
                    currentSheet = currentSheet,
                    readerPower = readerPower,
                    setPower = setPower,
                    isInvOn = isInvOn
                )
                StartInventoryContent(
                    apiViewModel,
                    readerViewModel,
                    navController,
                    selectedLocation,
                    locationList,
                    selectedLocationId,
                    showLocationPicker,
                    args = inventoryArgs,
                    validCount,
                    invalidCount,
                    tagCount,
                    uploadedTagCount,
                    unEncodedCount,
                    alienCount,
                    isOnDataUploaded,
                    lastTagCount,
                    isLoader
                )
            }
        }
        if (showLocationPicker.value) {
            LocationPicker(
                locationList,
                onDismiss = { showLocationPicker.value = false },
                onLocationSelected = { location ->
                    val isEmptyList = DataHolder.zoneWiseStockQtyList.isNullOrEmpty()
                    val zoneStockQty = if(isEmptyList) null else DataHolder.zoneWiseStockQtyList.filter { zs-> zs.path.equals(location.path) && zs.name.equals(location.name)}.firstOrNull()
                    LogUtils.showLog("zoneStockQty",""+if(zoneStockQty!=null) zoneStockQty.stockCount else "null")
                    selectedLocationId.value = location
                    selectedLocation.value = location.name
                    if(zoneStockQty!=null && zoneStockQty.stockCount>0) lastTagCount.value=zoneStockQty.stockCount
                    showLocationPicker.value = false
                }
            )
        }

    }
}

data class InventoryScreenArgs @OptIn(ExperimentalMaterial3Api::class) constructor(
    val context: Context,
    val label: String,
    val menuCode: String,
    val transactionType: String,
    val scope: CoroutineScope,
    val sheetState: SheetState,
    val showSheet: MutableState<Boolean>,
    val currentSheet: MutableState<BottomSheetType>,
    val readerPower: Int,
    val setPower: MutableState<Int>,
    val isInvOn: State<Boolean?>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartInventoryContent(
    apiViewModel: ApiViewModel,
    readerViewModel: ReaderViewModel,
    navController: NavHostController,
    selectedLocation: MutableState<String>,
    locationList: SnapshotStateList<LocationModel>,
    selectedLocationId: MutableState<LocationModel?>,
    showLocationPicker: MutableState<Boolean>,
    args: InventoryScreenArgs,
    validCount: State<Int>,
    invalidCount: State<Int>,
    tagCount: State<Int>,
    uploadedTagCount: State<Int>,
    unEncodedCount: State<Int>,
    alienCount: State<Int>,
    isOnDataUploaded: MutableState<Boolean>,
    lastTagCount: MutableState<Int>,
    isLoader: MutableState<Boolean>,
    //sessionTagCount: MutableState<Int>
) {
    val db = AppDatabase.getDbInstance(args.context)
    val strokeWidth = dimensionResource(R.dimen.dp_2)
    val isApiLoading = apiViewModel.isLoading.observeAsState()
    val triggerPressed = readerViewModel.isTriggerPressed().observeAsState(false)

    val snackbarController = remember { SnackbarController() }
    val error = readerViewModel.error().observeAsState()
    val locationMessage = stringResource(R.string.select_location_)
    val preHeader = TopicConstants.INVENTORY + "_" + args.menuCode + "_"
    val id = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.ID, "")
    val deviceSessionId = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.DEVICE_SESSION_ID, "")
    val hasActiveSession = deviceSessionId.isNotEmpty() && id.isNotEmpty()
    val isProcessOn = readerViewModel.isProcessOn().observeAsState(false)
    //val lastTagCount = remember { mutableStateOf(0) }
    val isDataUploaded = remember { mutableStateOf(false) }

    val animatedTagCountCount = animateIntAsState(
        targetValue = tagCount.value,
        label = "count"
    )
    val animatedValidCountCount = animateIntAsState(
        targetValue = validCount.value,
        label = "count"
    )

    /**LaunchedEffect(Unit) {
        !powerSheet.value
        !sessionSheet.value

    }*/
    LaunchedEffect(error.value) {
        val message = error.value
        if (!message.isNullOrBlank()) {
            LogUtils.showLog("isTagNotFound", "SingleEncodingContent: $message")
            snackbarController.show(
                ErrorAppSnackBarData(message)
            )
        }
    }

    LaunchedEffect(isDataUploaded.value) {
        if (isDataUploaded.value==true) {
            try {
                val jsonRequest = JSONObject().apply {
                    put(ParameterConstants.ID, id)
                    put(ParameterConstants.VALID_SCAN_TAG_COUNT, validCount.value + alienCount.value)
                    put(ParameterConstants.INVALID_SCAN_TAG_COUNT, invalidCount.value)
                    put(ParameterConstants.ACTION,ActionConstants.UPLOAD)
                }
                apiViewModel.callApi(UrlConstants.STOP_DEVICE_SESSION_INVENTORY, jsonRequest = jsonRequest)
                if(!isInternetConnected(args.context)) isDataUploaded.value=false
            }catch (e: Exception) {
                e.printStackTrace()
                isDataUploaded.value=false;
            }
        }
    }

    /**LaunchedEffect(isOnDataUploaded.value) {
        args.sheetState.hide()
        args.showSheet.value = false
        args.currentSheet.value = BottomSheetType.NONE
    }*/


    LaunchedEffect(triggerPressed.value) {
        if (triggerPressed.value == true) {
            readerViewModel.setTriggerValue(false)
            val preHeader = TopicConstants.INVENTORY + "_" + args.menuCode + "_"
            val id = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.ID, "")
            val deviceSessionId = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.DEVICE_SESSION_ID, "")
            if (isLoader.value == true) return@LaunchedEffect
            LogUtils.showLog("TriggerPressed", "Trigger is pressed")
            if(chkTrue(isApiLoading.value)) return@LaunchedEffect
            if(navController.currentBackStackEntry?.lifecycle?.currentState != Lifecycle.State.RESUMED) return@LaunchedEffect
            if(isDataUploaded.value || isOnDataUploaded.value) return@LaunchedEffect
            if(args.showSheet.value!=false && args.currentSheet.value != BottomSheetType.NONE) return@LaunchedEffect
            if (selectedLocation.value.isNullOrEmpty()) {
                snackbarController.show(
                    AppSnackBarData(
                        icon = R.drawable.error,
                        message = locationMessage,
                        showCancel = false
                    )
                )
            }
            else if(deviceSessionId.isNullOrEmpty() || id.isNullOrEmpty()) {
                try {
                    val jsonRequest = JSONObject()
                    jsonRequest.put(
                        ParameterConstants.CUSTOMER_ID,
                        DataStoreManager.readFromPreferences(ParameterConstants.CUSTOMER_ID, "")
                    )
                    jsonRequest.put(
                        ParameterConstants.OPERATION_LOCATION_ID,
                        DataStoreManager.readFromPreferences(LoginConstants.DEVICE_LOCATION_ID, "")
                    )
                    jsonRequest.put(
                        ParameterConstants.USER_ID,
                        DataStoreManager.readFromPreferences(ParameterConstants.USER_ID, "")
                    )
                    jsonRequest.put(
                        ParameterConstants.DEVICE_ID,
                        DataStoreManager.readFromPreferences(ParameterConstants.DEVICE_ID, "")
                    )
                    jsonRequest.put(ParameterConstants.TRANSACTION_TYPES, args.transactionType)
                    jsonRequest.put(
                        ParameterConstants.ASSET_LOCATION_PATH,
                        selectedLocationId.value!!.path
                    )
                    jsonRequest.put(ParameterConstants.DEVICE_SESSION_ID, deviceSessionId)
                    jsonRequest.put(ParameterConstants.VALID_SCAN_TAG_COUNT, validCount.value)
                    jsonRequest.put(ParameterConstants.INVALID_SCAN_TAG_COUNT, invalidCount.value)
                    apiViewModel.callApi(
                        UrlConstants.START_DEVICE_SESSION_INVENTORY,
                        jsonRequest = jsonRequest
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            else readerViewModel.toggleInventory(args.setPower.value)
        } else {
            LogUtils.showLog("TriggerPressed", "Trigger released")
        }
    }

    val response = apiViewModel.apiResult.collectAsState(initial = null)
    LaunchedEffect(response.value) {
        if(response.value==null) return@LaunchedEffect
        val result = response.value
        if (result?.isSuccess != true) {
            isDataUploaded.value=false
            val message = result?.errMsg
            if (!message.isNullOrBlank()) {
                //temp condition (to be deleted // )
                /**if(result.url.equals(UrlConstants.START_DEVICE_SESSION_INVENTORY,true)){
                    val preHeader = TopicConstants.INVENTORY + "_" + args.menuCode + "_"
                    DataStoreManager.saveToPreferences(preHeader + ParameterConstants.ID, "123")
                    DataStoreManager.saveToPreferences(preHeader + ParameterConstants.DEVICE_SESSION_ID, "kk123")
                    DataStoreManager.saveToPreferences(preHeader + ParameterConstants.ASSET_LOCATION_NAME, selectedLocationId.value!!.name)
                    DataStoreManager.saveToPreferences(preHeader + ParameterConstants.ASSET_LOCATION_PATH, selectedLocationId.value!!.path)
                }*/
                snackbarController.show(ErrorAppSnackBarData(result.errMsg.toString()))
            }
        } else if (result.response != null) {
            val jsonResponse = result.response

            when (result.url) {
                UrlConstants.LOCATION_ZONES -> {
                    val responseArray = ParseUtils.extractJSONArray(jsonResponse, ParameterConstants.DATA, JSONArray())
                    val dataList = mutableListOf<LocationModel>()
                    if (responseArray != null && responseArray.length() > 0) {
                        processChildren(responseArray,dataList);
                        LogUtils.showLog("dataList1",""+dataList.size)
                        /*for (i in 0 until responseArray.length()) {
                            val obj = responseArray.getJSONObject(i)

                            val location = LocationModel(
                                id = obj.optString(ParameterConstants.ID),
                                name = obj.optString(ParameterConstants.NAME),
                                code = obj.optString(ParameterConstants.CODE),
                                path = obj.optString(ParameterConstants.PATH)
                            )
                            dataList.add(location)
                        }*/
                    }
                    if (dataList.isNotEmpty()) {
                        locationList.clear()
                        locationList.addAll(dataList)
                        LogUtils.showLog("DataHolder.locationList", "StartInventoryContent:$dataList")
                    } else {
                        //TODO give custom error if responseArray is empty
                    }
                }

                UrlConstants.LOCATION_SUB_ZONES -> {
                    val dataObj = ParseUtils.extractJSONObject(jsonResponse, ParameterConstants.DATA, jsonResponse)
                    val responseArray = ParseUtils.extractJSONArray(dataObj, ParameterConstants.CHILDREN, JSONArray())
                    val dataList = mutableListOf<LocationModel>()
                    if (responseArray != null && responseArray.length() > 0) {
                        processChildren(responseArray,dataList);
                        LogUtils.showLog("responseArray1",""+responseArray)
                        LogUtils.showLog("dataList1",""+dataList.size)
                    }
                    if (dataList.isNotEmpty()) {
                        locationList.clear()
                        locationList.addAll(dataList)
                        LogUtils.showLog("DataHolder.locationList", "StartInventoryContent:$dataList")
                    }
                    else {
                        //TODO give custom error if responseArray is empty
                    }
                }

                UrlConstants.START_DEVICE_SESSION_INVENTORY -> {
                    val id = extractString(jsonResponse, ParameterConstants.ID, "")
                    val deviceSessionId = extractString(jsonResponse, ParameterConstants.DEVICE_SESSION_ID, "")
                    val transactionType = extractString(jsonResponse, ParameterConstants.TRANSACTION_TYPES, extractString(jsonResponse, ParameterConstants.TRANSACTION_TYPE, ""))
                    val assetLocationPath = extractString(jsonResponse, ParameterConstants.ASSET_LOCATION_PATH, "")
                    val stockCount = ParseUtils.extractInt(jsonResponse, ParameterConstants.STOCK_COUNT, 0)

                    val sessionData = JSONObject()
                    sessionData.put(ParameterConstants.SESSION_ID,deviceSessionId)
                    sessionData.put(ParameterConstants.ID,id)
                    sessionData.put(ParameterConstants.TRANSACTION_TYPE,transactionType)
                    sessionData.put(ParameterConstants.ASSET_LOCATION_PATH,assetLocationPath)
                    readerViewModel.setSessionId(deviceSessionId,sessionData.toString())
                    if(stockCount>0) lastTagCount.value=stockCount
                    val preHeader = TopicConstants.INVENTORY + "_" + args.menuCode + "_"
                    DataStoreManager.saveToPreferences(preHeader + ParameterConstants.ID, id)
                    DataStoreManager.saveToPreferences(preHeader + ParameterConstants.DEVICE_SESSION_ID, deviceSessionId)
                    LogUtils.showLog("API_INV_DeviceSessionID",chkNull(DataStoreManager.readFromPreferences(preHeader+ParameterConstants.DEVICE_SESSION_ID, ""),"empty"))
                    DataStoreManager.saveToPreferences(preHeader + ParameterConstants.TRANSACTION_TYPE, transactionType)
                    DataStoreManager.saveToPreferences(preHeader + ParameterConstants.ASSET_LOCATION_NAME, selectedLocationId.value!!.name)
                    DataStoreManager.saveToPreferences(preHeader + ParameterConstants.ASSET_LOCATION_PATH, assetLocationPath)
                    readerViewModel.toggleInventory(args.setPower.value)
                }

                UrlConstants.STOP_DEVICE_SESSION_INVENTORY,UrlConstants.CANCEL_DEVICE_SESSION_INVENTORY -> {
                    //sessionTagCount.value=tagCount.value
                    if(isDataUploaded.value) {
                        isOnDataUploaded.value=true
                        args.sheetState.hide()
                        args.showSheet.value = false
                        args.currentSheet.value = BottomSheetType.NONE
                        delay(500)
                    }
                    args.scope.launch(Dispatchers.IO) {
                      try{
                        if (tagCount.value > 0) {
                            val result = db.tagInfoDao().deleteBySessionTypeAndTransactionType(sessionType = args.menuCode, args.transactionType)
                            LogUtils.showLog("resultDeleteSession", "" + result)
                            if (result <= 0) return@launch
                        }
                        val preHeader = TopicConstants.INVENTORY + "_" + args.menuCode + "_"
                        DataStoreManager.saveToPreferences(preHeader + ParameterConstants.ID, "")
                        DataStoreManager.saveToPreferences(preHeader + ParameterConstants.DEVICE_SESSION_ID, "")
                        DataStoreManager.saveToPreferences(preHeader + ParameterConstants.TRANSACTION_TYPE, "")
                        DataStoreManager.saveToPreferences(preHeader + ParameterConstants.ASSET_LOCATION_NAME, "")
                        DataStoreManager.saveToPreferences(preHeader + ParameterConstants.ASSET_LOCATION_PATH, "")
                        DataStoreManager.saveToPreferences(preHeader + ParameterConstants.READER_POWER, 30)
                        args.scope.launch {
                            readerViewModel.clearSessionAndTransactionType()
                            navController.popBackStack()
                        }
                      }catch(e: Exception){e.printStackTrace()}
                    }
                    //readerViewModel.clearSessionAndTransactionType()
                    //readerViewModel.onDestroy()
                    //navController.popBackStack()
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
        )
        {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.Top) {
                TopBarContent(
                    args.label,
                    onBackClickL = {
                        args.scope.launch {
                            val preHeader = TopicConstants.INVENTORY + "_" + args.menuCode + "_"
                            val id = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.ID, "")
                            val deviceSessionId = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.DEVICE_SESSION_ID, "")

                            if (chkTrue(isProcessOn.value)) return@launch
                            if (deviceSessionId.isNullOrEmpty() || id.isNullOrEmpty()) {
                                DataStoreManager.saveToPreferences(preHeader + ParameterConstants.READER_POWER, 30)
                                if(navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                                 readerViewModel.clearSessionAndTransactionType()
                                 navController.popBackStack()
                                }
                            } else {
                                openSheet(
                                    scope = args.scope,
                                    sheetState = args.sheetState,
                                    showSheet = args.showSheet,
                                    currentSheet = args.currentSheet,
                                    sheet = BottomSheetType.SESSION
                                )
                            }
                        }
                    }, onSettingClick = {
                        //args.currentSheet.value = BottomSheetType.SETTINGS
                        if (readerViewModel.isProcessOn().value == true || apiViewModel.isLoading.value == true) return@TopBarContent
                        openSheet(
                            scope = args.scope,
                            sheetState = args.sheetState,
                            showSheet = args.showSheet,
                            currentSheet = args.currentSheet,
                            sheet = BottomSheetType.SETTINGS
                        )
                    }
                )
            }
            HorizontalDivider(thickness = dimensionResource(R.dimen.dp_1), color = OutlineDefault)

            Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_16)))
            //Temp condition for showing uploaded count
            if (BaseUtils.isDebuggable() && (tagCount.value > 0 || uploadedTagCount.value > 0)){
              UploadStatusChip(uploadedTagCount.value,tagCount.value)
            }

            Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_24)))

            if (!hasActiveSession && selectedLocation.value.isNullOrEmpty()) {
                Card(
                    modifier = Modifier
                        .width(dimensionResource(R.dimen.dp_116))
                        .height(dimensionResource(R.dimen.dp_44))
                        .clickable(
                            onClick = {
                                if (locationList.isNullOrEmpty()) {
                                    apiViewModel.callApi(
                                        url = UrlConstants.LOCATION_SUB_ZONES,
                                        appendData = DataStoreManager.readFromPreferences(
                                            LoginConstants.DEVICE_LOCATION_ID,
                                            ""
                                        )
                                    )
                                }
                                args.scope.launch {
                                    showLocationPicker.value = true
                                }
                            },
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ),
                    shape = RoundedCornerShape(dimensionResource(R.dimen.dp_24)),
                    colors = CardDefaults.cardColors(containerColor = WhiteColor),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 8.dp
                    ),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(
                                width = dimensionResource(R.dimen.dp_1),
                                color = TabColor,
                                shape = RoundedCornerShape(dimensionResource(R.dimen.dp_16))
                            )
                            .shadow(
                                elevation = dimensionResource(R.dimen.dp_4),
                                ambientColor = TabColor,
                                spotColor = WhiteColor
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = painterResource(R.drawable.property_slidershorizontal),
                            contentDescription = null,
                            modifier = Modifier.size(dimensionResource(R.dimen.dp_16))

                        )

                        Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_8)))

                        Text(
                            text = stringResource(R.string.select_location),
                            style = CommonTypography.current.noteText,
                            color = BlackColor
                        )
                    }
                }
            }
            else {
                GernericBasicTextField(
                    sourceZone = selectedLocation.value,
                    destZone = "",
                    menuCode = args.menuCode,
                    value = selectedLocation.value,
                    isOnDataUploaded = isOnDataUploaded.value,
                    labelRowAction = {
                        if(deviceSessionId.isNotEmpty()) return@GernericBasicTextField
                        args.scope.launch {
                            apiViewModel.callApi(
                                url = UrlConstants.LOCATION_SUB_ZONES,
                                appendData = DataStoreManager.readFromPreferences(
                                    LoginConstants.DEVICE_LOCATION_ID, ""
                                )
                            )
                            showLocationPicker.value = true
                        }
                    },
                    destAction = {

                    },
                    clearAction = {
                        args.scope.launch {
                            selectedLocation.value = ""
                            selectedLocationId.value = null
                            lastTagCount.value = 0
                            showLocationPicker.value = false
                        }
                    },
                    isDestZone = false,
                    isSet = !(deviceSessionId.isNullOrEmpty() || id.isNullOrEmpty())
                )
                /**BasicTextField(
                    value = selectedLocation.value,
                    onValueChange = {},
                    readOnly = true,
                    singleLine = true,
                    textStyle = CommonTypography.current.noteText.copy(
                        color = BlackColor,
                        textAlign = TextAlign.Start
                    ),
                    modifier = Modifier
                        .height(dimensionResource(R.dimen.dp_44))
                        .shadow(12.dp, RoundedCornerShape(dimensionResource(R.dimen.dp_24)))
                        .wrapContentWidth()   // 🔥 Important
                        .border(
                            1.dp,
                            if (isOnDataUploaded.value) GreenBorder else Yellow,
                            RoundedCornerShape(24.dp)
                        )
                        .background(WhiteColor, RoundedCornerShape(24.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    decorationBox = { innerTextField ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            // Leading icon
                            Icon(
                                painter = painterResource(id = R.drawable.icon_location),
                                contentDescription = null,
                                modifier = Modifier.size(dimensionResource(R.dimen.dp_18)),
                                tint = if (isOnDataUploaded.value) Green else Yellow
                            )

                            Spacer(Modifier.width(6.dp))

                            // Text
                            Row(modifier = Modifier.clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                args.scope.launch {
                                   showLocationPicker.value = true
                                }
                            }) {
                                innerTextField()
                            }

                            Spacer(Modifier.width(6.dp))

                            // Trailing icon
                            val preHeader = TopicConstants.INVENTORY + "_" + args.menuCode + "_"
                            val deviceSessionId = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.DEVICE_SESSION_ID, "")
                            val id = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.ID, "")

                            if (deviceSessionId.isNullOrEmpty() || id.isNullOrEmpty()) {
                                Image(
                                    painter = painterResource(id = R.drawable.clear),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(dimensionResource(R.dimen.dp_16))
                                        .clickable(
                                            indication = null,
                                            interactionSource = remember { MutableInteractionSource() }
                                        ) {
                                            args.scope.launch {
                                                selectedLocation.value = ""
                                                selectedLocationId.value = null
                                                lastTagCount.value = 0
                                            }
                                        }
                                )
                            } else {
                                Icon(
                                    painter = painterResource(id = R.drawable.property_check_selected),
                                    contentDescription = null,
                                    tint = if (isOnDataUploaded.value) Green else LightGray
                                )
                            }
                        }
                    }
                )*/

            }

            if (unEncodedCount.value > 0 || alienCount.value > 0){
                Row(
                    modifier = Modifier
                        .padding(top = dimensionResource(R.dimen.dp_24))
                        .wrapContentWidth()
                        .height(dimensionResource(R.dimen.dp_36))
                        .background(
                            color = ShadowColor,
                            shape = RoundedCornerShape(dimensionResource(R.dimen.dp_20))
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {

                    if(DataStoreManager.readFromPreferences("enableAlienTags",false) && DataStoreManager.readFromPreferences("enableVendorSerialCode",false) && DataStoreManager.readFromPreferences("vendorSerialCode","").isNotEmpty()) {
                        Text(
                            stringResource(
                                R.string.alien_tags, alienCount.value.toString()//(invalidCount.value - unEncodedCount.value).toString()
                            ),
                            style = CommonTypography.current.noteText,
                            color = TextSubtext,
                            textAlign = TextAlign.Left,
                            modifier = Modifier.padding(start = dimensionResource(R.dimen.dp_10))
                        )

                        VerticalDivider(
                            color = TabColor,
                            thickness = dimensionResource(R.dimen.dp_1),
                            modifier = Modifier.padding(
                                vertical = dimensionResource(R.dimen.dp_10),
                                horizontal = dimensionResource(R.dimen.dp_8)
                            )
                        )
                    }
                    Text(
                        stringResource(R.string.unencoded_tags, unEncodedCount.value.toString()),
                        style = CommonTypography.current.noteText,
                        color = RedColor,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.padding(dimensionResource(R.dimen.dp_10))
                    )

                }
            }

            if (isOnDataUploaded.value) {
                InventoryUploadedSuccess(count = tagCount.value)//sessionTagCount.value)
            }
            else {
                InventoryPulseCircle(
                    totalCount = tagCount.value,//animatedTagCountCount.value,//tagCount.value,
                    tagCount = validCount.value.toString(),//animatedValidCountCount.value.toString(),//validCount.value.toString(),
                    isInvOn = chkTrue(args.isInvOn.value),
                    lastTagCount = if(lastTagCount.value>0) lastTagCount.value.toString() else "",
                    onClick = {
                        openSheet(
                            scope = args.scope,
                            sheetState = args.sheetState,
                            showSheet = args.showSheet,
                            currentSheet = args.currentSheet,
                            sheet = BottomSheetType.UPLOAD
                        )
                    }
                )

                Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_16)))

                if (validCount.value > 0){
                    Text(text = stringResource(R.string.swipe_to_upload_items),
                        style = CommonTypography.current.noteText,
                        color = TextSubtext)
                }
            }
        }

        if (args.showSheet.value) {
            ModalBottomSheet(
                sheetState = args.sheetState,
                onDismissRequest = {

                },
                dragHandle = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent)
            ) {
                when (args.currentSheet.value) {
                    BottomSheetType.SETTINGS -> Box(
                        modifier = Modifier
                            .background(WhiteColor)
                            .fillMaxWidth()
                            .fillMaxHeight(0.26f)
                    ) {
                        DeviceSettings(
                            showSheet = args.showSheet,
                            sheetState = args.sheetState,
                            scope = args.scope,
                            currentSheet = args.currentSheet
                        )
                    }

                    BottomSheetType.POWER -> Box(
                        modifier = Modifier
                            .background(WhiteColor)
                            .fillMaxWidth()
                            .fillMaxHeight(0.47f)
                    ) {
                        SetInvDevicePower(
                            showSheet = args.showSheet,
                            sheetState = args.sheetState,
                            scope = args.scope,
                            currentPower = args.setPower.value,
                            onPowerSet = {
                                args.setPower.value = it
                                DataStoreManager.saveToPreferences(preHeader + ParameterConstants.READER_POWER,args.setPower.value)
                                readerViewModel.setPower(args.setPower.value)
                            },
                        )
                    }

                    BottomSheetType.SESSION -> Box(
                        modifier = Modifier
                            .background(WhiteColor)
                            .fillMaxWidth()
                            .fillMaxHeight(if (args.currentSheet.value == BottomSheetType.SETTINGS) 0f else 0.38f)
                    ) {
                        SessionContent(
                            showSheet = args.showSheet,
                            sheetState = args.sheetState,
                            scope = args.scope,
                            navController = navController,
                            tagCount = tagCount.value,
                            onStopSession = {
                                args.scope.launch {
                                    if (chkTrue(isProcessOn.value)) return@launch
                                    val jsonRequest = JSONObject().apply {
                                        put(ParameterConstants.ID, id)
                                        put(ParameterConstants.VALID_SCAN_TAG_COUNT, validCount.value + alienCount.value)
                                        put(ParameterConstants.INVALID_SCAN_TAG_COUNT, invalidCount.value)
                                        put(ParameterConstants.ACTION,ActionConstants.DISCARD)
                                    }

                                   // if (NetworkMonitor.isNetworkConnected) {
                                        apiViewModel.callApi(
                                            UrlConstants.CANCEL_DEVICE_SESSION_INVENTORY,
                                            jsonRequest = jsonRequest
                                        )
                                    /*} else{
                                        readerViewModel.clearSessionAndTransactionType()
                                        navController.popBackStack()
                                    }*/
                                }
                            },
                            readerViewModel = readerViewModel,
                        )
                    }

                    BottomSheetType.MESSAGE ->
                        Box(
                            modifier = Modifier
                                .background(WhiteColor)
                                .fillMaxWidth()
                                .fillMaxHeight(0.35f)
                        ) {
                            SessionAlertBottomSheet(
                                showSheet = args.showSheet,
                                sheetState = args.sheetState,
                                scope = args.scope,
                            )
                        }

                    BottomSheetType.UPLOAD ->
                        Box(
                            modifier = Modifier
                                .background(WhiteColor)
                                .fillMaxWidth()
                                .fillMaxHeight(0.44f)
                        ) {
                            val scannedText = if (lastTagCount.value <= 0) {
                                tagCount.value.toString()
                            } else {
                                "${tagCount.value}/${lastTagCount.value}"
                            }

                            val formatedText = buildAnnotatedString {
                                append(stringResource(R.string.scanned_stock_))
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append(scannedText)
                                }
                            }

                            UploadDataSheet(
                                title = args.label,//stringResource(R.string.upload_stock),
                                //heading = stringResource(R.string.confirm_stock_upload),
                                subHeading = formatedText,
                                //primaryButtonText = stringResource(R.string.confirm_and_upload),
                                showSheet = args.showSheet,
                                sheetState = args.sheetState,
                                scope = args.scope,
                                tagCount = tagCount.value,
                                isDataUploaded = isDataUploaded
                            )
                        }


                    else -> {

                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            AppSnackBar(snackbarController)
        }
    }
}

@Composable
fun StartInventoryBottomBar(
    readerViewModel: ReaderViewModel,
    selectedLocation: MutableState<String>,
    showLocationPicker: Boolean,
    tagCount: State<Int>,
    uploadedTagCount: State<Int>,
    isOnDataUploaded: MutableState<Boolean>,
    isInvOn: State<Boolean?>,
    isLoader: MutableState<Boolean>,
) {

    val previousInvState = remember { mutableStateOf(isInvOn.value) }

    LaunchedEffect(isInvOn.value) {

        // show loader only when TRUE -> FALSE
        if (previousInvState.value == true && isInvOn.value == false) {

            isLoader.value = true

            delay(500)

            isLoader.value = false
        }

        previousInvState.value = isInvOn.value
    }
    LogUtils.showLog("isLoader", "StartInventory: ${isInvOn.value}\n${isLoader.value}")
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(WhiteColor)
    ) {

        HorizontalDivider(color = OutlineDefault)
        if (!showLocationPicker && !isOnDataUploaded.value){
            Row(modifier = Modifier.padding(dimensionResource(R.dimen.dp_16))) {

                CommonButton(
                    text = if (isLoader.value) {
                        "Processing..."
                    } else {
                        stringResource(
                            id = if (isInvOn.value == true) {
                                R.string.stop_scanning
                            } else if (tagCount.value > 0) {
                                R.string.continue_scanning
                            } else {
                                R.string.start_scanning_inventory
                            }
                        )
                    },
                    onClick = {
                        readerViewModel.setTriggerValue(true)
                    },
                    icon = if (isInvOn.value == true) painterResource(R.drawable.stopicon) else null,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = selectedLocation.value.isNotEmpty() || isLoader.value,
                    contentColor = if (isInvOn.value == true) BlackColor else WhiteColor,
                    gradientBrush = if (isInvOn.value == true)SolidColor(WhiteColor) else Brush.horizontalGradient(
                        colors = listOf(
                            BlackColor,
                            ButtonGray
                        )
                    ),
                    isLoading = isLoader.value
                )


            }
        }
    }
}


@Composable
fun LocationPicker(
    locationList: List<LocationModel>,
    onDismiss: () -> Unit,
    onLocationSelected: (LocationModel) -> Unit
) {
    val searchQuery = remember { mutableStateOf("") }
    val filteredLocations = if (searchQuery.value.isEmpty()) {
        locationList
    } else {
        locationList.filter {
            it.name.contains(searchQuery.value, ignoreCase = true)
        }
    }

    // 🔹 FULL SCREEN OVERLAY
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.35f))
            .padding(top = 44.dp),
        contentAlignment = Alignment.BottomCenter   // ✅ CENTER EVERYTHING
    ) {
        Column(
            horizontalAlignment = Alignment.End
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .heightIn(max = 350.dp)
                    .background(WhiteColor, RoundedCornerShape(24.dp))
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {

                    Text(text = stringResource(R.string.select_location),
                        style = CommonTypography.current.headingH1,
                        modifier = Modifier.padding(bottom = dimensionResource(R.dimen.dp_16)))

                    SearchBar(
                        searchQuery = searchQuery.value,
                        onQueryChange = { searchQuery.value = it },
                        placeholder = stringResource(id = R.string.search_by_location_code)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn {
                        items(filteredLocations) { location ->
                            LocationRow(
                                location = location.name,
                                onClick = { onLocationSelected(location) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_12)))

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .heightIn(dimensionResource(R.dimen.dp_48))
                    .background(WhiteColor, RoundedCornerShape(24.dp))
            ) {
                Button(
                    onClick = {
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = WhiteColor,
                            shape = RoundedCornerShape(dimensionResource(R.dimen.dp_16))
                        )
                        .padding(horizontal = dimensionResource(R.dimen.dp_16)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WhiteColor,
                        contentColor = RedColor
                    )
                ) {
                    Text(
                        text = stringResource(R.string.cancel),
                        style = CommonTypography.current.buttonSemiBold,
                        color = RedColor
                    )
                }
            }

            Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_14)))

        }
    }
}


@Composable
fun SearchBar(searchQuery: String, onQueryChange: (String) -> Unit, placeholder: String) {

    Box(modifier = Modifier.fillMaxWidth()) {

        val interactionSource = remember { MutableInteractionSource() }
        val isFocused = interactionSource.collectIsFocusedAsState()
        val borderColor = if (isFocused.value) Yellow else LightGray
        val borderWidth = if (isFocused.value) 2.dp else 1.dp


        Box(modifier = Modifier.fillMaxWidth()) {

            TextField(
                value = searchQuery,
                onValueChange = {
                    onQueryChange(it)
                },
                placeholder = { Text(
                    text = placeholder,
                    style = CommonTypography.current.textMedium
                ) },
                trailingIcon = {
                    Image(
                        painter = painterResource(id = R.drawable.property_search),
                        contentDescription = null
                    )

                },
                singleLine = true,
                interactionSource = interactionSource,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = borderWidth,
                        color = borderColor,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .background(WhiteColor, RoundedCornerShape(16.dp)),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    cursorColor = BlackColor,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                textStyle = CommonTypography.current.textSemiBold.copy(color = BlackColor)
                    .copy(textAlign = TextAlign.Start)
            )
        }
    }
}

@Composable
fun LocationRow(
    location: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp)
    ) {

        Row(verticalAlignment = Alignment.CenterVertically) {

            Image(
                painter = painterResource(R.drawable.icon_location),
                contentDescription = null
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = location,
                style = CommonTypography.current.textSemiBold
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetInvDevicePower(
    showSheet: MutableState<Boolean>,
    sheetState: SheetState,
    scope: CoroutineScope,
    currentPower: Int,
    onPowerSet: (Int) -> Unit
) {

    val setPower = stringResource(R.string.set_power)
    val heading = stringResource(R.string.set_device_power)
    val subHeading = stringResource(R.string.adjust_the_reader)

    val config = remember(currentPower) {
        SetValueUiConfig(
            title = setPower,
            heading = heading,
            subHeading = subHeading,
            minValue = 5,
            maxValue = 30,
            initialValue = currentPower
        )
    }

    val actions = remember {
        SetValueActions(
            onConfirm =
                { value ->
                    onPowerSet(value)
                }, onClose = { })
    }

    SetValueContent(
        showSheet = showSheet,
        sheetState = sheetState,
        scope = scope,
        config = config,
        actions = actions
    )
}