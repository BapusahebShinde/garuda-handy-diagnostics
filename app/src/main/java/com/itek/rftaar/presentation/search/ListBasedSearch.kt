package com.itek.rftaar.presentation.search

import android.content.Context
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import com.itek.rftaar.R
import com.itek.rftaar.api.constants.ParameterConstants
import com.itek.rftaar.api.constants.UrlConstants
import com.itek.rftaar.core.common.constants.LoginConstants
import com.itek.rftaar.core.common.utils.LogUtils
import com.itek.rftaar.core.common.utils.ParseUtils
import com.itek.rftaar.core.common.utils.ParseUtils.extractBoolean
import com.itek.rftaar.core.common.utils.ParseUtils.extractInt
import com.itek.rftaar.core.common.utils.ParseUtils.extractJSONArray
import com.itek.rftaar.core.common.utils.ParseUtils.extractJSONObject
import com.itek.rftaar.core.common.utils.ParseUtils.extractString
import com.itek.rftaar.core.database.AppDatabase
import com.itek.rftaar.core.database.DataStoreManager
import com.itek.rftaar.data.entity.ProductZoneDataEntity
import com.itek.rftaar.data.model.LabelName
import com.itek.rftaar.data.model.ListTypeInfo
import com.itek.rftaar.data.model.ProductZoneFoundQty
import com.itek.rftaar.domain.model.LocationModel
import com.itek.rftaar.mqtt.constants.SearchListTypeConstant
import com.itek.rftaar.mqtt.constants.TopicConstants
import com.itek.rftaar.presentation.commonComp.AppSnackBar
import com.itek.rftaar.presentation.commonComp.BottomSheetType
import com.itek.rftaar.presentation.commonComp.CommonTextField
import com.itek.rftaar.presentation.commonComp.CounterText
import com.itek.rftaar.presentation.commonComp.ErrorAppSnackBarData
import com.itek.rftaar.presentation.commonComp.FilterPicker
import com.itek.rftaar.presentation.commonComp.GernericBasicTextField
import com.itek.rftaar.presentation.commonComp.ImageViewFullScreen
import com.itek.rftaar.presentation.commonComp.MultiFilterPicker
import com.itek.rftaar.presentation.commonComp.SnackbarController
import com.itek.rftaar.presentation.commonComp.TextFieldConfig
import com.itek.rftaar.presentation.commonComp.TopBarContent
import com.itek.rftaar.presentation.commonComp.openSheet
import com.itek.rftaar.presentation.decoding.parseDecodeTypes
import com.itek.rftaar.presentation.decoding.saveProductHeaders
import com.itek.rftaar.presentation.encoding.DeviceSettings
import com.itek.rftaar.presentation.encoding.SessionContent
import com.itek.rftaar.presentation.inventory.InventoryUploadedSuccess
import com.itek.rftaar.presentation.inventory.SwipeToUploadButton
import com.itek.rftaar.presentation.inventory.UploadDataSheet
import com.itek.rftaar.presentation.inventory.parseDisplayData
import com.itek.rftaar.presentation.inventory.parseDisplayValues
import com.itek.rftaar.presentation.movement.clearSavedSessionValues
import com.itek.rftaar.presentation.navigation.Screen
import com.itek.rftaar.presentation.viewmodel.ApiViewModel
import com.itek.rftaar.reader.constants.StatusConstants
import com.itek.rftaar.ui.theme.BackGround
import com.itek.rftaar.ui.theme.BlackColor
import com.itek.rftaar.ui.theme.CardGreen
import com.itek.rftaar.ui.theme.CommonTypography
import com.itek.rftaar.ui.theme.ErrorBgColor
import com.itek.rftaar.ui.theme.Green
import com.itek.rftaar.ui.theme.OutlineDefault
import com.itek.rftaar.ui.theme.RedColor
import com.itek.rftaar.ui.theme.TabColor
import com.itek.rftaar.ui.theme.TextSubtext
import com.itek.rftaar.ui.theme.WhiteColor
import com.itek.rftaar.utils.CommonUtils.chkNull
import com.itek.rftaar.utils.CommonUtils.chkTrue
import com.itek.rftaar.utils.NetworkUtils.isInternetConnected
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ListBasedSearch(
    modifier: Modifier,
    navController: NavHostController,
    label: String,
    menuCode: String,
    apiViewModel: ApiViewModel = hiltViewModel(),
    searchParams: Map<String, Any>
) {

    LogUtils.showLog("label0","--"+label)
    LogUtils.showLog("menuCode0","--"+menuCode)
    val userId = DataStoreManager.readFromPreferences(ParameterConstants.USER_ID,"")

    val context = LocalContext.current
    val listObj = ParseUtils.extractObject<ListTypeInfo>(searchParams,cls= ListTypeInfo::class)
    LogUtils.showLog("listObj", listObj.toString())
    val topic = if(listObj!=null) TopicConstants.SEARCH_LIST_UPDATE else TopicConstants.SEARCH
    val preHeader = topic + "_" + menuCode + "_"
    val sessionId = if(listObj!=null) listObj.sessionId else extractString(searchParams, ParameterConstants.SESSION_ID,DataStoreManager.readFromPreferences(preHeader + ParameterConstants.DEVICE_SESSION_ID, ""))
    val searchTypeId = if(listObj!=null) listObj.searchTypeId else extractString(searchParams, ParameterConstants.SEARCH_TYPE_ID,"")
    val searchTypeName = if(listObj!=null) listObj.searchTypeName else extractString(searchParams, ParameterConstants.SEARCH_TYPE_NAME,"")
    val transactionType = if(listObj!=null) listObj.childSearchTypeName else DataStoreManager.readFromPreferences(preHeader + ParameterConstants.TRANSACTION_TYPE, DataStoreManager.readFromPreferences("replenishmentType",""))
    val referenceNumber = if(listObj!=null) listObj.referenceNumber else DataStoreManager.readFromPreferences(preHeader + ParameterConstants.REFERENCE_NUMBER, "")

    val isShowUpload  = extractBoolean(searchParams,"isShowUpload",true)
    val isDataUpload = remember { mutableStateOf(false) }
    val isOnDataUploaded = remember { mutableStateOf(false) }
    val isUploadData = remember { mutableStateOf(false) }

    /*if(listObj!=null){
        try {
            val sessionData = JSONObject()
            sessionData.put(ParameterConstants.SESSION_ID, sessionId)
            sessionData.put(ParameterConstants.ID, "")
            sessionData.put(ParameterConstants.TRANSACTION_TYPE, transactionType)
            sessionData.put(ParameterConstants.SEARCH_TYPE_ID, searchTypeId)
            sessionData.put(ParameterConstants.SEARCH_TYPE_NAME, searchTypeName)
            sessionData.put(ParameterConstants.REFERENCE_NUMBER, referenceNumber)
            DataStoreManager.saveToPreferences(preHeader + ParameterConstants.DEVICE_SESSION_ID, sessionId)
            DataStoreManager.saveToPreferences(preHeader + "sessionData", sessionData.toString())
        }catch (e: Exception) {e.printStackTrace()}
    }*/

    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { sheetValue ->
            sheetValue != SheetValue.Hidden
        }
    )
    val showSheet = remember { mutableStateOf(false) }
    val currentSheet = remember { mutableStateOf(BottomSheetType.NONE) }

    val isApiLoading = apiViewModel.isLoading.observeAsState(initial = false)

    val selFilterKeys = remember { mutableStateListOf<String>()}
    if(selFilterKeys.isEmpty()) selFilterKeys.addAll(DataStoreManager.getListStr(userId + "_" + menuCode + "_filters"))
    LogUtils.showLog("selFilterKeys",""+selFilterKeys.size)

    val dynamicFilterPicker = remember { mutableStateOf(false) }
    val dynamicFilters = remember { mutableStateMapOf<String, List<Any>>() }
    val selectedFilters = remember { mutableStateMapOf<String, List<Any>>() }
    val selectedDynamicFilterName = remember { mutableStateOf("") }


    val enablePick = remember { mutableStateOf(false) }
    val enableDecode = remember { mutableStateOf(false) }
    val searchValue = remember { mutableStateOf("") }
    val decodeTypeList = remember { mutableStateListOf<LabelName>() }

    val filterZoneKey = "Zone"

    val db = AppDatabase.getDbInstance(context)
    val hasData = db.productZoneDataDao().hasData(topic = topic, menuCode, transactionType).observeAsState(false)
    val foundQty = db.productZoneDataDao().getFoundQty(topic = topic, menuCode, transactionType).collectAsState(0)
    val decodeQty = db.productZoneDataDao().getDecodeQty(topic = topic, menuCode, transactionType).collectAsState(0)
    val foundCount =  remember { mutableStateOf( if(hasData.value) chkNull(foundQty.value,0) else 0)}//db.productZoneDataDao().getFoundQty(topic = topic, menuCode, transactionType).collectAsState(0)
    val decodeCount =  remember { mutableStateOf( if(hasData.value) chkNull(decodeQty.value,0) else 0)}//db.productZoneDataDao().getFoundQty(topic = topic, menuCode, transactionType).collectAsState(0)
    val totalCount = remember { mutableStateOf(if(listObj!=null) listObj.qty else 0) }


    LogUtils.showLog("DecodeQty",""+decodeQty.value)
    LogUtils.showLog("hasData",""+hasData.value)

    LaunchedEffect(Unit) {
        delay(20)
        LogUtils.showLog("hasData1",""+hasData.value)
        if (apiViewModel !=null  /*&& !hasData.value*/) {

            CoroutineScope(Dispatchers.IO).launch {
                val map = hashMapOf(
                    ParameterConstants.CUSTOMER_ID to DataStoreManager.readFromPreferences(ParameterConstants.CUSTOMER_ID, ""),
                    ParameterConstants.BUSINESS_LINE_ID to DataStoreManager.readFromPreferences(ParameterConstants.BUSINESS_LINE_ID, ""),
                    ParameterConstants.LOCATION_ID to DataStoreManager.readFromPreferences(LoginConstants.DEVICE_LOCATION_ID, ""),
                    ParameterConstants.DEVICE_ID to DataStoreManager.readFromPreferences(ParameterConstants.DEVICE_ID, ""),
                    ParameterConstants.SEARCH_TYPE_ID to searchTypeId,
                    ParameterConstants.SESSION_ID to sessionId,
                )
                apiViewModel.callApi(
                    UrlConstants.PRODUCT_SEARCH_LIST_CONFIG,
                    queryMap = map
                )
            }

            if(!hasData.value && false) {
                delay(30)

            CoroutineScope(Dispatchers.IO).launch {
                    val map = hashMapOf(
                        ParameterConstants.CUSTOMER_ID to DataStoreManager.readFromPreferences(
                            ParameterConstants.CUSTOMER_ID,
                            ""
                        ),
                        ParameterConstants.BUSINESS_LINE_ID to DataStoreManager.readFromPreferences(
                            ParameterConstants.BUSINESS_LINE_ID,
                            ""
                        ),
                        ParameterConstants.LOCATION_ID to DataStoreManager.readFromPreferences(
                            LoginConstants.DEVICE_LOCATION_ID,
                            ""
                        ),
                        ParameterConstants.DEVICE_ID to DataStoreManager.readFromPreferences(
                            ParameterConstants.DEVICE_ID,
                            ""
                        ),
                        ParameterConstants.SEARCH_TYPE_ID to searchTypeId,
                        ParameterConstants.SESSION_ID to sessionId
                    )

                    apiViewModel.callApi(
                        UrlConstants.PRODUCT_SEARCH_LIST_DETAILS,
                        queryMap = map
                    )
                }
            }
        }
    }

    LaunchedEffect(isDataUpload.value) {
        if (isDataUpload.value == true) {
            if(!isInternetConnected(context, isShowErrToast = true)) {isDataUpload.value= false; return@LaunchedEffect}
            try {
                val jsonRequest = JSONObject().apply {
                    put(ParameterConstants.DEVICE_ID, DataStoreManager.readFromPreferences(ParameterConstants.DEVICE_ID, ""))
                    put(ParameterConstants.CUSTOMER_ID, DataStoreManager.readFromPreferences(ParameterConstants.CUSTOMER_ID, ""))
                    put(ParameterConstants.USER_ID, DataStoreManager.readFromPreferences(ParameterConstants.USER_ID, ""))
                    put(ParameterConstants.BUSINESS_LINE_ID, DataStoreManager.readFromPreferences(ParameterConstants.BUSINESS_LINE_ID, ""))
                    put(ParameterConstants.LOCATION_ID, DataStoreManager.readFromPreferences(LoginConstants.DEVICE_LOCATION_ID, ""))
                    put(ParameterConstants.SEARCH_TYPE_ID, searchTypeId)
                    put(ParameterConstants.SESSION_ID, sessionId)
                    put(ParameterConstants.STATUS, StatusConstants.COMPLETED)
                }
                apiViewModel.callApi(UrlConstants.PRODUCT_UPDATE, jsonRequest = jsonRequest)
                if(!isInternetConnected(context)) isDataUpload.value=false
            }catch (e: Exception) {
                e.printStackTrace()
                isDataUpload.value=false;
            }
        }
    }

    val listBasedSearchArgs = ListBasedSearchArgs(
        context = context,
        label = label,
        topic = topic,
        menuCode = menuCode,
        transactionType = transactionType,
        preHeader = preHeader,
        sessionId = sessionId,
        searchTypeId = searchTypeId,
        searchTypeName = searchTypeName,
        referenceNumber = referenceNumber,
        isApiLoading = isApiLoading,
        scope = scope,
        showSheet = showSheet,
        sheetState = sheetState,
        currentSheet = currentSheet,
        enablePick = enablePick,
        enableDecode = enableDecode,
        searchValue = searchValue,
        foundCount = foundCount,
        decodeCount = decodeCount,
        totalCount = totalCount,
        decodeTypeList = decodeTypeList
    )

    BackHandler(enabled = true) {
        if(chkTrue(isApiLoading.value)) return@BackHandler
        scope.launch {
            if(sessionId.isNotEmpty() && listBasedSearchArgs.foundCount.value>0){
                openSheet(
                    scope = scope,
                    sheetState = sheetState,
                    showSheet = showSheet,
                    currentSheet = currentSheet,
                    sheet = BottomSheetType.SESSION
                )
            }
            else {
                CoroutineScope(Dispatchers.IO).launch {
                    clearSavedSessionValues(context,menuCode,transactionType,false,topic)
                    db.productZoneDataDao().deleteAll(topic,menuCode,transactionType)
                }
                if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                    navController.popBackStack()
                }
            }
        }
    }

    Box() {

        Scaffold(bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BackGround),
                contentAlignment = Alignment.BottomCenter
            ) {
                ListBasedSearchBottomBar(navController,listBasedSearchArgs,isShowUpload,isOnDataUploaded)
            }
        },
            modifier = modifier.background(BackGround)){ innerPadding ->

            Box(
                modifier = modifier
                    .background(WhiteColor)
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                ListBasedSearchContent(navController,apiViewModel,listBasedSearchArgs,dynamicFilterPicker,dynamicFilters,selectedFilters,selectedDynamicFilterName,selFilterKeys,filterZoneKey,listObj,isShowUpload,isDataUpload,isOnDataUploaded)
            }
        }

        if (dynamicFilterPicker.value) {
            LogUtils.showLog("D_Filters",dynamicFilters.toString())
            MultiFilterPicker(
                menuCode = menuCode,
                filters = dynamicFilters,
                selectedMap = selectedFilters,
                onDismiss = {
                    dynamicFilterPicker.value = false
                },
                onApply = { selectedMap ->
                    LogUtils.showLog("selectedMap",selectedMap.toString())
                    val selectedValues = selectedMap.values.flatten().filter { it.toString().isNotEmpty() }.distinct()
                    LogUtils.showLog("Selected_",selectedValues.toString())
                    selectedDynamicFilterName.value = selectedValues.joinToString(", ")
                    selectedFilters.clear()
                    selectedFilters.putAll(selectedMap)
                    dynamicFilterPicker.value = false
                }
            )
        }
    }
}

data class ListBasedSearchArgs @OptIn(ExperimentalMaterial3Api::class) constructor(
    val context: Context,
    var label: String,
    val topic: String,
    val menuCode: String,
    val transactionType: String,
    val preHeader: String,
    val sessionId: String,
    val searchTypeId: String,
    val isApiLoading: State<Boolean>,
    val scope: CoroutineScope,
    val showSheet: MutableState<Boolean>,
    val sheetState: SheetState,
    val currentSheet: MutableState<BottomSheetType>,
    val enablePick: MutableState<Boolean>,
    val enableDecode: MutableState<Boolean>,
    val searchValue: MutableState<String>,
    val searchTypeName: String,
    val referenceNumber: String,
    val foundCount: MutableState<Int>,
    val decodeCount: MutableState<Int>,
    val totalCount: MutableState<Int>,
    val decodeTypeList: SnapshotStateList<LabelName>,
)

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ListBasedSearchContent(
    navController: NavHostController,
    apiViewModel: ApiViewModel,
    args: ListBasedSearchArgs,
    dynamicFilterPicker: MutableState<Boolean>,
    dynamicFilters: SnapshotStateMap<String, List<Any>>,
    selectedFilters: SnapshotStateMap<String, List<Any>>,
    selectedDynamicFilterName: MutableState<String>,
    selFilterKeys: SnapshotStateList<String>,
    filterZoneKey: String,
    listObj: ListTypeInfo?,
    isShowUpload: Boolean,
    isDataUpload: MutableState<Boolean>,
    isOnDataUploaded: MutableState<Boolean>,
) {

    val searchQuery = remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    val db = AppDatabase.getDbInstance(args.context)
    //TODO getALL against selected sessionId?
    val getAll = db.productZoneDataDao().getAll1(topic = args.topic, args.menuCode, args.transactionType).collectAsState(initial = emptyList())
    val foundCount = db.productZoneDataDao().getFoundQty(topic = args.topic, args.menuCode, args.transactionType).collectAsState(0)
    val decodeCount = db.productZoneDataDao().getDecodeQty(topic = args.topic, args.menuCode, args.transactionType).collectAsState(0)

    val showImagePreview = remember { mutableStateOf(false) }
    val selectedPainter = remember { mutableStateOf<Painter?>(null) }
    val snackbarController = remember { SnackbarController() }

    if(foundCount!=null) args.foundCount.value = chkNull(foundCount.value,0)
    if(decodeCount!=null) args.decodeCount.value = chkNull(decodeCount.value,0)


    val response = apiViewModel.apiResult.collectAsState(initial = null)
    LaunchedEffect(response.value) {
        val result = response.value ?: return@LaunchedEffect
        if (result?.isSuccess != true) {
            val message = result?.errMsg
            if (!message.isNullOrBlank()) {
                snackbarController.show(ErrorAppSnackBarData(result.errMsg.toString()))
                if (result.url == UrlConstants.PRODUCT_SEARCH_LIST_DETAILS) {
                    CoroutineScope(Dispatchers.IO).launch {
                        db.productZoneDataDao().deleteAll(args.topic, args.menuCode, args.transactionType)
                    }
                }
            }
        }
        else if (result.response != null) {
            val jsonResponse = result.response
            LogUtils.showLog("D_Response_"+result.url,"Response_Success_"+jsonResponse)
            when (result.url) {
                UrlConstants.PRODUCT_SEARCH_LIST_CONFIG -> {
                    //save configurations
                    val response = extractJSONObject(jsonResponse, ParameterConstants.DATA, jsonResponse)
                    val enablePick = extractBoolean(response,"enablePick")
                    val enableDecode = enablePick && extractBoolean(response,"enableDecode")
                    val hasReferenceNumber = extractBoolean(response,"hasReferenceNumber")
                    val referenceNumberLabel = extractString(response,"referenceNumberLabel")
                    val name= extractString(response,"name")
                    //confirm is Search Value is Barcode
                    val searchValue = extractString(response,"searchvalue")
                    LogUtils.showLog("searchValue11",""+searchValue)
                    //if(hasReferenceNumber && referenceNumberLabel.isNotEmpty() && args.label.contains("(")) args.label.value += " ("+referenceNumberLabel+")"
                    //if(hasReferenceNumber) chkNull(referenceNumberLabel,name) else name
                    args.enablePick.value=enablePick
                    args.enableDecode.value=enableDecode
                    args.searchValue.value=searchValue
                    LogUtils.showLog("enablePick11",""+enablePick)
                    LogUtils.showLog("enableDecode11",""+enableDecode)
                    if(args.enableDecode.value){
                        val map = HashMap<String, String>()
                        map.put(ParameterConstants.CUSTOMER_ID,DataStoreManager.readFromPreferences(ParameterConstants.CUSTOMER_ID, ""))
                        map.put(ParameterConstants.BUSINESS_LINE_ID,DataStoreManager.readFromPreferences(ParameterConstants.BUSINESS_LINE_ID, ""))
                        apiViewModel.callApi(UrlConstants.GET_DECODE_TYPES,queryMap=map)
                    }

                    //call details api
                    val map = hashMapOf(
                        ParameterConstants.CUSTOMER_ID to DataStoreManager.readFromPreferences(
                            ParameterConstants.CUSTOMER_ID,
                            ""
                        ),
                        ParameterConstants.BUSINESS_LINE_ID to DataStoreManager.readFromPreferences(
                            ParameterConstants.BUSINESS_LINE_ID,
                            ""
                        ),
                        ParameterConstants.LOCATION_ID to DataStoreManager.readFromPreferences(
                            LoginConstants.DEVICE_LOCATION_ID,
                            ""
                        ),
                        ParameterConstants.DEVICE_ID to DataStoreManager.readFromPreferences(
                            ParameterConstants.DEVICE_ID,
                            ""
                        ),
                        ParameterConstants.SEARCH_TYPE_ID to args.searchTypeId,
                        ParameterConstants.SESSION_ID to args.sessionId
                    )
                    apiViewModel.callApi(
                        UrlConstants.PRODUCT_SEARCH_LIST_DETAILS,
                        queryMap = map
                    )
                }
                UrlConstants.PRODUCT_SEARCH_LIST_DETAILS -> {
                    //temp code for using static response
                    //val response = JsonUtils.getSampleJSON(result.url)
                    val response = extractJSONObject(jsonResponse, ParameterConstants.DATA, jsonResponse)
                    val responseArray = extractJSONArray(response, ParameterConstants.DATA, JSONArray())
                    val dataList = mutableListOf<ProductZoneDataEntity>()
                    val filterListMap = HashMap<String, ArrayList<Any>>()
                    val filterSelectionList = DataStoreManager.getFilterList().toMutableList()
                    filterSelectionList.add(0,filterZoneKey)
                    val filterHeaderList = filterSelectionList
                    var totalOrderCount = 0
                    if (responseArray != null && responseArray.length() > 0) {
                        val headers  = saveProductHeaders(jsonResponse)
                        //TODO save for filters
                        for (i in 0 until responseArray.length()) {
                            val obj = responseArray.getJSONObject(i)
                            val barcode = extractString(obj, ParameterConstants.BARCODE,"")
                            val article = extractString(obj, ParameterConstants.ARTICLE,"")
                            val productInfo = extractJSONObject(obj,"productInfo",obj)
                            val qtyObj = extractJSONObject(obj, ParameterConstants.QTY,obj)
                            val zonePath= extractString(obj, ParameterConstants.ASSET_LOCATION_PATH, extractString(obj, ParameterConstants.DEST_LOCATION_PATH,extractString(obj, ParameterConstants.MOVE_AT_ASSET_LOCATION_PATH, "")))
                            val zoneName= extractString(obj, ParameterConstants.ASSET_LOCATION_NAME, extractString(obj, ParameterConstants.DEST_LOCATION_NAME, extractString(obj, ParameterConstants.MOVE_AT_ASSET_LOCATION_NAME, "")))
                            val loc = LocationModel(name=zoneName, path = zonePath)
                            if(filterHeaderList.contains(filterZoneKey) && chkNull(loc.name,"").isNotEmpty() && chkNull(loc.path,"").isNotEmpty()){
                                val listFilters = if(filterListMap.containsKey(filterZoneKey)) ArrayList<Any>(filterListMap.get(filterZoneKey))  else ArrayList<Any>()
                                if (!listFilters.contains(loc))listFilters.add(loc)
                                filterListMap.set(filterZoneKey,listFilters)
                            }
                            val listProductValues = ArrayList<Pair<String, String>>()
                            val productZoneDataModel = ProductZoneDataEntity(
                                topic = args.topic,
                                sessionType = args.menuCode,
                                transactionType = args.transactionType,
                                sessionId = args.sessionId,
                                barcode = barcode,
                                customField = article
                            )
                            if(productInfo!=null) {
                                //productInfo.put("Image", JSONArray("[\"https://retailtest.itekrfid.com/zudio/Stores/Storesproductimage/8903135516670.jpeg\"]"))
                                val keys = productInfo.keys()
                                while (keys.hasNext()) {
                                    val key = keys.next()
                                    if (key.isNullOrEmpty()) continue
                                    if (!headers.contains(key)) continue
                                    val value = chkNull(extractString(productInfo, key, ""), "")
                                    //if (value.isNullOrEmpty() || value == "null") continue
                                    listProductValues.add(key to value)
                                    if (filterHeaderList.contains(key) && chkNull(
                                            value,
                                            ""
                                        ).isNotEmpty()
                                    ) {
                                        val listFilters =
                                            if (filterListMap.containsKey(key)) ArrayList<Any>(
                                                filterListMap.get(key)
                                            ) else ArrayList<Any>()
                                        if (!listFilters.contains(value)) listFilters.add(value.toString())
                                        filterListMap.set(key, listFilters)
                                    }
                                }
                            }
                            if(listProductValues.isNotEmpty()) {
                                LogUtils.showLog("listProductValues", listProductValues.toString())
                                productZoneDataModel.displayData = listProductValues.toString()
                            }
                            if(qtyObj!=null) {
                                productZoneDataModel.quantity = extractInt(qtyObj,"requestedQty",0)
                                if(productZoneDataModel.quantity>0) totalOrderCount+=productZoneDataModel.quantity
                                productZoneDataModel.totalAvailableStock = extractInt(qtyObj,"availableQty",0)
                                //temp code (for checking found qty)
                                //productZoneDataModel.foundQty = if(productZoneDataModel.totalAvailableStock>0 && productZoneDataModel.quantity>1) 1 else extractInt(qtyObj,"pickedQty",extractInt(qtyObj,"searchQty",0))
                                productZoneDataModel.foundQty = if(!args.enablePick.value) extractInt(qtyObj,"searchQty",0) else extractInt(qtyObj,"pickedQty",extractInt(qtyObj,"foundQty",0))
                                productZoneDataModel.decodeQty = extractInt(qtyObj,"decodedQty",0)
                                productZoneDataModel.errMsg=extractString(qtyObj, ParameterConstants.MESSAGE,"")
                                val locArray = extractJSONArray(qtyObj, "locationQty", JSONArray())
                                if (locArray != null && locArray.length() > 0) {
                                    var totalStockQty=0
                                    for (i in 0 until locArray.length()) {
                                        val locObj = locArray.getJSONObject(i)
                                        if (locObj != null) {
                                            val zonePath = extractString(
                                                locObj,
                                                ParameterConstants.ASSET_LOCATION_PATH,
                                                extractString(locObj, ParameterConstants.DEST_LOCATION_PATH, extractString(locObj, ParameterConstants.MOVE_AT_ASSET_LOCATION_PATH,extractString(locObj, "locationId", ""))))
                                            val zoneName = extractString(
                                                locObj,
                                                ParameterConstants.ASSET_LOCATION_NAME,
                                                extractString(locObj, ParameterConstants.DEST_LOCATION_NAME, extractString(locObj, ParameterConstants.MOVE_AT_ASSET_LOCATION_NAME,extractString(locObj, "locationName", ""))))
                                            val zoneQty=extractInt(locObj, ParameterConstants.QTY,0)
                                            totalStockQty+=zoneQty
                                            val loc = LocationModel(name=zoneName, path = zonePath)
                                            if(filterHeaderList.contains(filterZoneKey) && chkNull(loc.name,"").isNotEmpty() && chkNull(loc.path,"").isNotEmpty()){
                                                val listFilters = if(filterListMap.containsKey(filterZoneKey)) ArrayList<Any>(filterListMap.get(filterZoneKey))  else ArrayList<Any>()
                                                if (!listFilters.contains(loc)) listFilters.add(loc)
                                                filterListMap.set(filterZoneKey,listFilters)
                                            }
                                            if (i == 0) {
                                                productZoneDataModel.assetLocationPath = zonePath
                                                productZoneDataModel.assetLocationName = zoneName
                                                productZoneDataModel.stock = zoneQty
                                                dataList.add(productZoneDataModel)
                                            }
                                            else {
                                                val copiedProductZoneDataModel = productZoneDataModel.copy()
                                                copiedProductZoneDataModel.assetLocationPath = zonePath
                                                copiedProductZoneDataModel.assetLocationName = zoneName
                                                copiedProductZoneDataModel.stock = zoneQty
                                                copiedProductZoneDataModel.foundQty = 0
                                                copiedProductZoneDataModel.decodeQty = 0
                                                dataList.add(copiedProductZoneDataModel)
                                            }
                                        }
                                    }
                                    if(totalStockQty>0 && productZoneDataModel.totalAvailableStock<=0)
                                        productZoneDataModel.totalAvailableStock = totalStockQty
                                }
                                else {
                                    productZoneDataModel.errMsg = extractString(
                                        qtyObj,
                                        ParameterConstants.MESSAGE,
                                        "Product not available in stock."
                                    )
                                    productZoneDataModel.assetLocationName = "-"
                                    productZoneDataModel.assetLocationPath = "-"
                                    dataList.add(productZoneDataModel)
                                }
                            }
                           /* val epcArray = extractJSONArray(obj, ParameterConstants.EPC, JSONArray())
                            if (epcArray != null && epcArray.length() > 0) {
                                for (i in 0 until epcArray.length()) {
                                    val epc = epcArray.getString(i)
                                    if (epc.isNotEmpty()) {
                                        if (i == 0) {
                                            productZoneDataModel.epc = epc
                                            dataList.add(productZoneDataModel)
                                        } else {
                                            val copiedProductZoneDataModel =
                                                productZoneDataModel.copy()
                                            copiedProductZoneDataModel.epc = epc
                                            dataList.add(copiedProductZoneDataModel)
                                        }
                                    }
                                }
                            }*/
                            //else dataList.add(productZoneDataModel)
                        }
                    }
                    if (dataList.isNotEmpty()) {
                        LogUtils.showLog("FilterList",filterListMap.toString())
                        dynamicFilters.clear()
                        dynamicFilters.putAll(filterListMap)
                        LogUtils.showLog("D_FilterList",dynamicFilters.toString())
                        if(totalOrderCount>0 && args.totalCount.value<=0) args.totalCount.value=totalOrderCount

                        CoroutineScope(Dispatchers.IO).launch {
                            db.productZoneDataDao().deleteAll(args.topic, args.menuCode, args.transactionType)
                            db.productZoneDataDao().insertAll(dataList)
                        }
                        /*locationList.clear()
                        locationList.addAll(dataList)*/
                        //LogUtils.showLog("DataHolder.locationList", "StartInventoryContent:$dataList")
                    }
                    else {
                        CoroutineScope(Dispatchers.IO).launch {
                            db.productZoneDataDao().deleteAll(args.topic, args.menuCode, args.transactionType)
                        }
                    }
                }
                UrlConstants.PRODUCT_UPDATE -> {
                    if(isDataUpload.value) {
                        isOnDataUploaded.value=true
                        args.sheetState.hide()
                        args.showSheet.value = false
                        args.currentSheet.value = BottomSheetType.NONE
                        delay(500)
                    }
                    CoroutineScope(Dispatchers.IO).launch {
                        clearSavedSessionValues(args.context,args.menuCode,args.transactionType,false,args.topic)
                        db.productZoneDataDao().deleteAll(args.topic, args.menuCode, args.transactionType)
                    }
                    if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED)
                        navController.popBackStack()
                }
                UrlConstants.GET_DECODE_TYPES ->{
                    args.decodeTypeList.clear()
                    args.decodeTypeList.addAll(parseDecodeTypes(result))
                    LogUtils.showLog("decodeTypeList",""+args.decodeTypeList)
                }
            }
        }
    }

    val filteredList = remember(getAll.value, searchQuery.value,selectedFilters.toMap()) {
        val query = searchQuery.value.trim()

        val sourceList = getAll.value.filter { item ->
            //Search filter
            val matchesSearch = query.isBlank() || item.barcode.contains(query, ignoreCase = true)

            LogUtils.showLog("filter_item",item.displayData.toString())
            //Dynamic filters
            val matchesFilters = selectedFilters.isNullOrEmpty() || selectedFilters.all { (key, values) ->
                //apply loop for List
                values.any { value ->
                    LogUtils.showLog("filter_value","(" + key + ", " + value + ")")
                    if(value is LocationModel){
                        item.name.contains(value.name) && item.path.contains(value.path)
                    }
                    else item.displayData.contains("(" + key + ", " + value + ")", ignoreCase = true)
                }
            }

            matchesSearch && matchesFilters
        }

        // Grouping items by barcode
        sourceList.groupBy { it.barcode }
    }


    if (isOnDataUploaded.value) {
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(dimensionResource(R.dimen.dp_16)),
            contentAlignment = Alignment.Center) {
            InventoryUploadedSuccess(count = args.foundCount.value)
        }
    }
    else {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(modifier = Modifier, verticalArrangement = Arrangement.Top) {
                    TopBarContent(
                        label = args.label,
                        onBackClickL = {
                            if(chkTrue(args.isApiLoading.value)) return@TopBarContent
                            args.scope.launch {
                                if(args.sessionId.isNotEmpty() && args.foundCount.value>0){
                                    openSheet(
                                        scope = args.scope,
                                        sheetState = args.sheetState,
                                        showSheet = args.showSheet,
                                        currentSheet = args.currentSheet,
                                        sheet = BottomSheetType.SESSION
                                    )
                                }
                                else {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        clearSavedSessionValues(args.context,args.menuCode,args.transactionType,false,args.topic)
                                        db.productZoneDataDao().deleteAll(args.topic,args.menuCode,args.transactionType)
                                    }
                                    if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                                        navController.popBackStack()
                                    }
                                }
                            }
                        },
                        onSettingClick = {
                            if (apiViewModel.isLoading.value == true) return@TopBarContent
                            openSheet(
                                scope = args.scope,
                                sheetState = args.sheetState,
                                showSheet = args.showSheet,
                                currentSheet = args.currentSheet,
                                sheet = BottomSheetType.SETTINGS
                            )
                        },
                        isSetting = true
                    )
                }
                HorizontalDivider(
                    thickness = dimensionResource(R.dimen.dp_1),
                    color = OutlineDefault
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(dimensionResource(R.dimen.dp_16)),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    CounterText(
                        current = args.totalCount.value,
                        total = args.totalCount.value.toString(),
                        isLimitShow = false//args.foundCount.value>0
                    )


                    Text(
                        text = "Total Quantity",
                        style = CommonTypography.current.noteText.copy(color = BlackColor)
                    )

                    Spacer(modifier = Modifier.size(10.dp))

                    Row() {
                        if (selFilterKeys.isNotEmpty() && dynamicFilters.isNotEmpty()) {
                            if (selectedDynamicFilterName.value.isNullOrEmpty()) {
                                Card(
                                    modifier = Modifier
                                        .width(dimensionResource(R.dimen.dp_160))
                                        .height(dimensionResource(R.dimen.dp_44))
                                        .clickable(
                                            onClick = {
                                                /*scope.launch {
                                            optionPicker.value = true
                                        }*/
                                                dynamicFilterPicker.value = true
                                                //optionPicker.value = true
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
                                        Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_10)))
                                        Image(
                                            painter = painterResource(R.drawable.property_slidershorizontal),
                                            contentDescription = null,
                                            modifier = Modifier.size(dimensionResource(R.dimen.dp_16))

                                        )

                                        Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_8)))

                                        Text(
                                            text = stringResource(R.string.set_filters),
                                            style = CommonTypography.current.noteText,
                                            color = BlackColor
                                        )
                                        Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_10)))
                                    }
                                }
                            } else {
                                GernericBasicTextField(
                                    sourceZone = selectedDynamicFilterName.value,
                                    destZone = "",
                                    menuCode = args.menuCode,
                                    value = selectedDynamicFilterName.value,
                                    labelRowAction = {
                                        args.scope.launch {
                                            apiViewModel.callApi(
                                                url = UrlConstants.LOCATION_SUB_ZONES,
                                                appendData = DataStoreManager.readFromPreferences(
                                                    LoginConstants.DEVICE_LOCATION_ID, ""
                                                )
                                            )
                                            dynamicFilterPicker.value = true
                                        }
                                    },
                                    destAction = {

                                    },
                                    clearAction = {
                                        args.scope.launch {
                                            selectedDynamicFilterName.value = ""
                                            selectedFilters.clear()
                                            dynamicFilterPicker.value = false
                                        }
                                    },
                                    isDestZone = false,
                                )
                                /** BasicTextField(
                                value = selectedDynamicFilterName.value,
                                onValueChange = {},
                                readOnly = true,
                                singleLine = true,
                                textStyle = CommonTypography.current.noteText.copy(
                                color = BlackColor,
                                textAlign = TextAlign.Start
                                ),
                                modifier = Modifier
                                //.wrapContentWidth()
                                .fillMaxWidth()
                                .shadow(
                                12.dp,
                                RoundedCornerShape(dimensionResource(R.dimen.dp_24))
                                )
                                .height(dimensionResource(R.dimen.dp_44))
                                .border(
                                width = 1.dp,
                                color = Yellow,
                                shape = RoundedCornerShape(dimensionResource(R.dimen.dp_24))
                                )
                                .background(
                                WhiteColor,
                                RoundedCornerShape(dimensionResource(R.dimen.dp_24))
                                )
                                .padding(horizontal = 12.dp), // mimic TextField padding
                                decorationBox = { innerTextField ->

                                Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxHeight()
                                ) {

                                Icon(
                                painter = painterResource(id = R.drawable.icon_location),
                                contentDescription = null,
                                modifier = Modifier.size(dimensionResource(R.dimen.dp_18)),
                                tint = Yellow
                                )

                                Spacer(Modifier.width(6.dp))

                                Box(
                                modifier = Modifier
                                .weight(1f)
                                .padding(end = 6.dp),
                                contentAlignment = Alignment.CenterStart
                                ) {
                                androidx.compose.material.Text(
                                text = selectedDynamicFilterName.value,
                                maxLines = 1,
                                overflow = TextOverflow.Clip,
                                style = CommonTypography.current.noteText.copy(color = BlackColor),
                                modifier = Modifier
                                .fillMaxWidth()
                                .basicMarquee()
                                )
                                }

                                Spacer(Modifier.width(6.dp))
                                if (true) {
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
                                selectedFilters.clear()
                                selectedDynamicFilterName.value = ""
                                }
                                }
                                )

                                } else {
                                Icon(
                                painter = painterResource(id = R.drawable.property_check_selected),
                                contentDescription = null,
                                tint = LightGray
                                )
                                }
                                }
                                }
                                )*/
                            }
                        }
                    }

                    CommonTextField(
                        config = TextFieldConfig(
                            value = searchQuery.value,
                            onValueChange = { searchQuery.value = it },
                            label = stringResource(id = R.string.search_for_ean),
                            isSearch = true,
                            imeAction = ImeAction.Done,
                            onImeAction = {
                                focusManager.clearFocus()
                            }),
                        modifier = Modifier.padding(vertical = dimensionResource(R.dimen.dp_24))
                    )

                    if (filteredList.isEmpty()) {

                        Text(
                            text = stringResource(R.string.err_no_data),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                            style = CommonTypography.current.noteText,
                            color = RedColor
                        )

                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f) // IMPORTANT
                        ) {

                            filteredList.forEach { (barcode, itemsInGroup) ->

                                item(key = barcode) {
                                    // Use the first item in the group to extract common product details
                                    val commonProduct = itemsInGroup
                                        .filter { it.foundQty > 0 || it.decodeQty > 0 }
                                        .firstOrNull() ?: itemsInGroup.first()

                                    val displayMap = remember(commonProduct.displayData) {
                                        parseDisplayData(commonProduct.displayData)
                                    }
                                    val values = remember(commonProduct.displayData) {
                                        parseDisplayValues(commonProduct.displayData)
                                    }

                                    // Image Logic
                                    val imagesValue = displayMap[DataStoreManager.getImageLabel()]
                                    val imageUrls = try {
                                        if (!imagesValue.isNullOrBlank() && imagesValue.startsWith("[")) {
                                            JSONArray(imagesValue).let { array ->
                                                List(array.length()) { index ->
                                                    array.getString(
                                                        index
                                                    )
                                                }
                                            }
                                        } else emptyList()
                                    } catch (e: Exception) {
                                        emptyList()
                                    }

                                    val imagePainters = if (imageUrls.isEmpty()) {
                                        listOf(rememberAsyncImagePainter(R.drawable.image))
                                    } else {
                                        imageUrls.map { rememberAsyncImagePainter(it) }
                                    }

                                    val title = values.drop(1).take(2).filter { it.isNotBlank() }
                                        .joinToString(",")
                                    val sku = values.drop(3).take(3).filter { it.isNotBlank() }
                                        .joinToString(".")

                                    // --- UI Rendering for the Group ---
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(dimensionResource(R.dimen.dp_8))
                                    ) {
                                        // Header: Image + Product Details (Shown once)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            ProductImage(
                                                imagePainters = imagePainters,
                                                modifier = Modifier,
                                                imageUrls = imageUrls,
                                                selectedPainter = selectedPainter,
                                                showImagePreview = showImagePreview,
                                            )

                                            Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_8)))

                                            ListBasedProductDetails(
                                                label = barcode,
                                                info = title,
                                                data = sku,
                                                isShowExpectedAndAvailableQty = true,
                                                commonProduct = commonProduct,
                                                badgeText = "Found: " + commonProduct.foundQty,//(if (locationItem.foundQty > 0)"Found: " else "Pending:")+"${locationItem.totalQty}",
                                                badgeText2 = if(args.enableDecode.value) "Decode: " + commonProduct.decodeQty else "",
                                            )
                                        }

                                        HorizontalDivider(
                                            thickness = dimensionResource(R.dimen.dp_1),
                                            color = OutlineDefault,
                                            modifier = Modifier.padding(
                                                vertical = dimensionResource(
                                                    R.dimen.dp_10
                                                )
                                            )
                                        )

                                        // Sub-list: Locations (Shown for every item with this barcode)
                                        itemsInGroup.forEach { locationItem ->

                                            // Apply your location name logic (removing brackets if needed)
                                            val cleanLocationName = locationItem.name
                                                .replace(Regex("\\(.*\\)"), "")
                                                .trim()

                                            val displayLocation =
                                                if (cleanLocationName.length > 25) {
                                                    cleanLocationName.take(25) + "..."
                                                } else cleanLocationName
                                            val lblSrcZone = stringResource(R.string.source_zone)
                                            val selErr = stringResource(R.string.err_select_)

                                            ListBasedInfoRow(
                                                location = displayLocation,
                                                quantity = "Qty:${locationItem.stockQty}",
                                                isQtyEnable = true,
                                                onItemClick = {
                                                    if (locationItem.errMsg.isNotEmpty()) {
                                                        snackbarController.show(
                                                            ErrorAppSnackBarData(
                                                                locationItem.errMsg
                                                            )
                                                        )
                                                        return@ListBasedInfoRow
                                                    }
                                                    LogUtils.showLog(
                                                        "enablePick12",
                                                        "" + args.enablePick.value
                                                    )
                                                    LogUtils.showLog(
                                                        "enableDecode12",
                                                        "" + args.enableDecode.value
                                                    )
                                                    clearSavedSessionValues(args.context, args.menuCode, args.transactionType,false)
                                                    if (locationItem.name.isNotEmpty() && locationItem.path.isNotEmpty()) {
                                                        DataStoreManager.saveToPreferences(
                                                            args.preHeader + ParameterConstants.ASSET_LOCATION_NAME,
                                                            locationItem.name
                                                        )
                                                        DataStoreManager.saveToPreferences(
                                                            args.preHeader + ParameterConstants.ASSET_LOCATION_PATH,
                                                            locationItem.path
                                                        )
                                                    }
                                                    val obj = listObj!!

                                                    LogUtils.showLog("decTypeList0",args.decodeTypeList.toString())
                                                    val decTypeList = if (args.enablePick.value && args.enableDecode.value && args.transactionType.isNotEmpty()) args.decodeTypeList.filter { decType-> if(args.searchTypeName.equals(SearchListTypeConstant.OMNICHANNEL)) "OMNI".contains(decType.name,true) else args.transactionType.contains(decType.name,false) ||  args.transactionType.filter { it.isUpperCase() }.equals(decType.name,false) } else emptyList()
                                                    val decoTypeList = if(decTypeList.isNotEmpty()) decTypeList else args.decodeTypeList
                                                    LogUtils.showLog("decTypeList",decTypeList.toString())
                                                    val searchBundle = mapOf(
                                                        "topic" to args.topic,
                                                        "sessionType" to args.menuCode,
                                                        "transactionType" to args.transactionType,
                                                        "searchTypeId" to args.searchTypeId,
                                                        "searchTypeName" to args.searchTypeName,
                                                        "sessionId" to args.sessionId,
                                                        "searchValue" to args.searchValue.value,
                                                        "referenceNumber" to args.referenceNumber,
                                                        locationItem::class.java.simpleName to locationItem,
                                                        "mode" to if (args.enablePick.value && args.enableDecode.value) SearchDetailsActionMode.SEARCH_AND_PICK_AND_DECODE.toString() else if (args.enablePick.value && !args.enableDecode.value) SearchDetailsActionMode.SEARCH_AND_PICK.toString() else SearchDetailsActionMode.SEARCH_FOUND.toString(),
                                                        "isShowUpload" to (args.enablePick.value && !args.enableDecode.value),
                                                        "decodeTypesList" to decoTypeList
                                                    )
                                                    LogUtils.showLog(
                                                        "decTypeList01",
                                                        searchBundle.get("decodeTypesList").toString()
                                                    )
                                                    val route = Screen.ProductSearch.createRoute(
                                                        code = args.menuCode,
                                                        label = args.label,
                                                        params = searchBundle
                                                    )
                                                    navController.navigate(route)
                                                }
                                            )

                                            HorizontalDivider(
                                                thickness = 0.5.dp,
                                                color = OutlineDefault.copy(alpha = 0.4f),
                                                modifier = Modifier.padding(
                                                    vertical = dimensionResource(
                                                        R.dimen.dp_4
                                                    )
                                                )
                                            )
                                        }

                                        // Space between different product groups
                                        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.dp_12)))
                                    }
                                }
                            }
                        }
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

                        BottomSheetType.SESSION -> Box(
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
                                .fillMaxWidth()
                                .wrapContentHeight()
                        ) {
                            val formatedText = buildAnnotatedString {
                                append(stringResource(R.string.txt_found))
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold,color = BlackColor)) {
                                    append(": ${args.foundCount.value}/${args.totalCount.value}")
                                }
                                if(args.enableDecode.value) {
                                    append("\t | \t")
                                    append(stringResource(R.string.decode))
                                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold,color = BlackColor)) {
                                        append(": ${args.decodeCount.value}/${args.totalCount.value}")
                                    }
                                }
                                append("\n")
                                append(stringResource(R.string.data_lost))
                                append("\n")
                                append(stringResource(R.string.please_confirm_))
                            }

                            SessionContent(
                                showSheet = args.showSheet,
                                sheetState = args.sheetState,
                                scope = args.scope,
                                navController = navController,
                                tagCount = args.foundCount.value,
                                subHeading = formatedText,
                                onStopSession = {
                                    if (chkTrue(args.isApiLoading.value)) return@SessionContent
                                    args.scope.launch {
                                        CoroutineScope(Dispatchers.IO).launch {
                                            clearSavedSessionValues(args.context,args.menuCode,args.transactionType,false,args.topic)
                                            db.productZoneDataDao().deleteAll(args.topic,args.menuCode,args.transactionType)
                                        }
                                        if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                                            //clearSavedSessionValues(args.context, args.menuCode, args.transactionType, false, args.topic)
                                            //readerViewModel.clearSessionAndTransactionType()
                                            navController.popBackStack()
                                        }
                                    }
                                },
                                readerViewModel = null,
                                limit = args.totalCount.value,
                                isAllowContinue = false
                            )
                        }

                        BottomSheetType.SETTINGS -> Box(
                            modifier = Modifier
                                .background(WhiteColor)
                                .fillMaxWidth()
                                .wrapContentHeight()
                        ) {
                            DeviceSettings(
                                showSheet = args.showSheet,
                                sheetState = args.sheetState,
                                scope = args.scope,
                                currentSheet = args.currentSheet,
                                isPowerSet = false,
                                isFilterEnable = true
                            )
                        }

                        BottomSheetType.FILTER -> Box(
                            modifier = Modifier
                                .background(WhiteColor)
                                .fillMaxWidth()
                                .wrapContentHeight()
                        ) {

                            val filterSelectionList =
                                DataStoreManager.getFilterList().toMutableList()
                            filterSelectionList.add(0, filterZoneKey)
                            FilterPicker(
                                filters = filterSelectionList,
                                selFilterKeys = selFilterKeys,
                                onDismiss = {
                                    args.scope.launch {
                                        args.sheetState.hide()
                                        args.showSheet.value = false
                                        args.currentSheet.value = BottomSheetType.NONE
                                    }
                                },
                                onApply = { selectedMap ->
                                    LogUtils.showLog("D_selectedMAp", selectedMap.toString())
                                    val selectedValues =
                                        selectedMap.filter { it.isNotBlank() }.distinct()
                                    LogUtils.showLog("D_selectedMAp1", selectedValues.toString())

                                    selFilterKeys.clear()
                                    selFilterKeys.addAll(selectedValues)

                                    val userId = DataStoreManager.readFromPreferences(
                                        ParameterConstants.USER_ID,
                                        ""
                                    )
                                    DataStoreManager.saveListStr(
                                        userId + "_" + args.menuCode + "_filters",
                                        selectedValues
                                    )

                                    /*selectedDynamicFilterName.value =
                                    selectedValues.joinToString(", ")*/

                                    args.scope.launch {
                                        args.sheetState.hide()
                                        args.showSheet.value = false
                                        args.currentSheet.value = BottomSheetType.NONE
                                    }
                                }
                            )
                        }

                        BottomSheetType.UPLOAD ->
                            Box(
                                modifier = Modifier
                                    .background(WhiteColor)
                                    .fillMaxWidth()
                                    .wrapContentHeight()
                            ) {
                                val formatedText = buildAnnotatedString {
                                    append(stringResource(R.string.txt_found))
                                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold,color = BlackColor)) {
                                        append(": ${args.foundCount.value}/${args.totalCount.value}")
                                    }
                                    if(args.enableDecode.value) {
                                        append("\t | \t")
                                        append(stringResource(R.string.decode))
                                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold,color = BlackColor)) {
                                            append(": ${args.decodeCount.value}/${args.totalCount.value}")
                                        }
                                    }
                                }
                                UploadDataSheet(
                                    title = args.label,//stringResource(R.string.confirm_and_complete),
                                    heading = stringResource(R.string.complete_, args.label),
                                    subHeading = formatedText,
                                    primaryButtonText = stringResource(R.string.confirm_and_complete),
                                    showSheet = args.showSheet,
                                    sheetState = args.sheetState,
                                    scope = args.scope,
                                    tagCount = args.foundCount.value,
                                    isDataUploaded = isDataUpload
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

            Box(modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center){
                if (showImagePreview.value) {
                    ImageViewFullScreen(showImagePreview , selectedPainter)
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ListBasedSearchBottomBar(
    navController: NavHostController,
    args: ListBasedSearchArgs,
    isShowUpload: Boolean,
    isOnDataUploaded: MutableState<Boolean>,
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(WhiteColor)
    ) {
        val hasScannedItems = args.foundCount.value>0
        if (!chkTrue(args.isApiLoading.value) && hasScannedItems && isShowUpload && !isOnDataUploaded.value) {
            SwipeToUploadButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimensionResource(R.dimen.dp_16)),
                text = stringResource(R.string.swipe_to_complete)
            ) {
                openSheet(
                    scope = args.scope,
                    sheetState = args.sheetState,
                    showSheet = args.showSheet,
                    currentSheet = args.currentSheet,
                    sheet = BottomSheetType.UPLOAD
                )
                    CoroutineScope(Dispatchers.IO).launch {
                        delay(100)
                    }
                }
            }
    }
}



@Composable
fun ListBasedProductDetails(
    label: String,
    info: String,
    data: String,
    isShowExpectedAndAvailableQty: Boolean = false,
    commonProduct: ProductZoneFoundQty,
    badgeText: String = "",
    badgeText2: String = "",
){
    Column(modifier = Modifier, verticalArrangement = Arrangement.Center) {
        Text(
            text = label,
            style = CommonTypography.current.textSemiBold,
        )
        Spacer(modifier = Modifier.size(2.dp))
        Text(
            text = info,
            style = CommonTypography.current.smallTxt,
            color = TextSubtext,
            modifier = Modifier.basicMarquee()
        )
        Spacer(modifier = Modifier.size(2.dp))
        Row {
            Text(
                text = data,
                style = CommonTypography.current.smallTxt,
                color = TextSubtext,
                modifier = Modifier.basicMarquee()
            )
        }
        Spacer(modifier = Modifier.size(2.dp))
        if (isShowExpectedAndAvailableQty) {
            Row {
                Text(
                    text = stringResource(R.string.txt_search_qty,commonProduct.totalQty),
                    style = CommonTypography.current.smallTxt
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.txt_available_qty,commonProduct.totalAvailableStockQty),
                    style = CommonTypography.current.smallTxt
                )
            }
        }

        Row(modifier= Modifier,verticalAlignment = Alignment.CenterVertically) {

            val isFound = commonProduct.foundQty == commonProduct.totalQty
            val isDecodeFound = commonProduct.decodeQty == commonProduct.totalQty
            InventoryBadge(
                text = badgeText,
                backGroundColor = if(isFound) CardGreen else ErrorBgColor,
                textColor = if(isFound) Green else RedColor,
                iconColor = if(isFound) Green else RedColor,
                textStyle = CommonTypography.current.smallTxt,
                padding = 0.dp
            )

            if (badgeText2.isNotEmpty()) {
                Spacer(modifier = Modifier.width(dimensionResource(R.dimen.dp_4)))
                InventoryBadge(
                    text = badgeText2,
                    backGroundColor = if(isDecodeFound) CardGreen else BackGround,
                    textColor = if(isDecodeFound) Green else TextSubtext,
                    iconColor = if(isDecodeFound) Green else BlackColor,
                    textStyle = CommonTypography.current.smallTxt,
                    padding = 0.dp
                )
            }
        }
    }
}

@Composable
fun ProductDetailsShort(
    label: String,
    info: String,
    data: String,
    isShowExpectedAndAvailableQty: Boolean = false,
    commonProduct: ProductZoneFoundQty
){
    Column(modifier = Modifier, verticalArrangement = Arrangement.Center) {
        Text(
            text = label,
            style = CommonTypography.current.textSemiBold,
        )
        Text(
            text = info,
            style = CommonTypography.current.noteText,
            color = TextSubtext
        )

        Row() {
            Text(
                text = data,
                style = CommonTypography.current.smallTxt,
            )
        }

        if (isShowExpectedAndAvailableQty) {
            Row {
                Text(
                    text = stringResource(R.string.txt_search_qty,commonProduct.totalQty),
                    style = CommonTypography.current.smallTxt
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.txt_available_qty,commonProduct.totalAvailableStockQty),
                    style = CommonTypography.current.smallTxt
                )
            }
        }
    }
}

data class ProductStockUiModel(
    val location: String,
    val quantity: String,
    val badgeText: String
)
/*data class ProductItemUiModel(
    val imageRes: Int,
    val heading: String,
    val info: String,
    val data: String,
    val stocks: List<ProductStockUiModel> // 👈 nested list
)*/


/*
@Composable
fun ProductListItem(
    item: ProductZoneFoundQty,
    modifier: Modifier = Modifier,
    onItemClick: (ProductZoneFoundQty) -> Unit,
    heading: String,
    info: String,
    data: String,
    location: String,
    quantity: String,
    badgeText: String,
    imagePainters: List<AsyncImagePainter>,
    showImagePreview: MutableState<Boolean>,
    selectedPainter: MutableState<Painter?>,
    imageUrls: List<String>
) {

    Column(
        modifier = modifier
        */
/*.then(
        if (onItemClick != null) {
            Modifier.clickable { onItemClick() }
        } else {
            Modifier
        }
    )*//*

    ) {

        // ---------- Image + Details ----------
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {

            ProductImage(imagePainters = imagePainters,
                selectedPainter = selectedPainter,
                showImagePreview = showImagePreview,
                imageUrls = imageUrls,
                modifier = Modifier)

            Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_8)))

            ProductDetailsShort(
                label = heading,
                info = info,
                data = data,
                commonProduct = commonProduct
            )
        }

        HorizontalDivider(
            thickness = dimensionResource(R.dimen.dp_1),
            color = OutlineDefault,
            modifier = Modifier.padding(vertical = dimensionResource(R.dimen.dp_10))
        )


        InventoryInfoRow(
            location = location,
            quantity = quantity,
            badgeText = badgeText,
            onItemClick = { onItemClick(item) }
        )
        HorizontalDivider(
            thickness = dimensionResource(R.dimen.dp_1),
            color = OutlineDefault,
            modifier = Modifier.padding(vertical = dimensionResource(R.dimen.dp_8))
        )

    }
}
*/

@Composable
fun ProductImage(
    imagePainters: List<AsyncImagePainter>,
    modifier: Modifier = Modifier,
    imageUrls: List<String>,
    showImagePreview: MutableState<Boolean>,
    selectedPainter: MutableState<Painter?>,
) {
    val listState = rememberLazyListState()
    val currentIndex = remember { derivedStateOf { listState.firstVisibleItemIndex } }
    Column(modifier = Modifier,
        verticalArrangement = Arrangement.Center) {
        // Image Box
        Box(
            modifier = modifier
                .width(dimensionResource(R.dimen.dp_105))
                .height(dimensionResource(R.dimen.dp_105)),
            contentAlignment = Alignment.Center
        ) {
            if (imagePainters.size == 1) {

                Image(
                    painter = imagePainters.first(),
                    contentDescription = null,
                    modifier = Modifier
                        .matchParentSize()
                        .fillMaxSize()
                        .clip(RoundedCornerShape(dimensionResource(R.dimen.dp_8)))
                        .background(WhiteColor)
                        .border(
                            dimensionResource(R.dimen.dp_1),
                            OutlineDefault,
                            RoundedCornerShape(dimensionResource(R.dimen.dp_8))
                        )
                        .clickable(enabled = imageUrls.isNotEmpty()) {
                            selectedPainter.value = imagePainters.first()
                            showImagePreview.value = true
                        },
                    contentScale = ContentScale.FillBounds
                )

            } else {

                LazyRow(
                    state = listState,
                    userScrollEnabled = imagePainters.size > 1,
                    modifier = Modifier.fillMaxSize()
                ) {

                    itemsIndexed(imagePainters) { index, painter ->

                        Image(
                            painter = painter,
                            contentDescription = null,
                            modifier = Modifier
                                .matchParentSize()
                                .fillMaxHeight()
                                .aspectRatio(1f)
                                .padding(end = 4.dp)
                                .clip(RoundedCornerShape(dimensionResource(R.dimen.dp_8)))
                                .background(WhiteColor)
                                .border(
                                    dimensionResource(R.dimen.dp_1),
                                    OutlineDefault,
                                    RoundedCornerShape(dimensionResource(R.dimen.dp_8))
                                )
                                .clickable(enabled = imageUrls.isNotEmpty()) {
                                    selectedPainter.value = painter
                                    showImagePreview.value = true
                                },
                            contentScale = ContentScale.FillBounds
                        )
                    }
                }

                Text(
                    text = "${currentIndex.value + 1}/${imagePainters.size}",
                    style = CommonTypography.current.noteText.copy(WhiteColor),
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                        .background(
                            Color.Black.copy(alpha = 0.6f), RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

        }
    }
}

@Composable
fun ListBasedInfoRow(
    location: String,
    quantity: String,
    isQtyEnable: Boolean = true,
    modifier: Modifier = Modifier,
    onItemClick: (() -> Unit)?
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        // Location
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(R.drawable.icon_location),
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(dimensionResource(R.dimen.dp_5)))
            Text(
                text = location,
                style = CommonTypography.current.smallTxt,
                modifier = Modifier.basicMarquee()
            )
        }

        // Quantity
        if (isQtyEnable) {
            Text(
                text = quantity,
                style = CommonTypography.current.smallTxt
            )
        }

        // Badge + Arrow
        Row(modifier= Modifier.clickable{ onItemClick?.invoke() },verticalAlignment = Alignment.CenterVertically) {

            Spacer(modifier = Modifier.width(dimensionResource(R.dimen.dp_4)))

            Image(
                painter = painterResource(R.drawable.icon__next),
                contentDescription = null
            )
        }
    }
}

@Composable
fun InventoryInfoRow(
    location: String,
    quantity: String,
    isQtyEnable: Boolean = true,
    badgeText: String="",
    badgeText2: String="",
    modifier: Modifier = Modifier,
    onItemClick: (() -> Unit)?
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        // Location
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(R.drawable.icon_location),
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(dimensionResource(R.dimen.dp_5)))
            Text(
                text = location,
                style = CommonTypography.current.smallTxt,
                modifier = Modifier.basicMarquee()
            )
        }

        // Quantity
        if (isQtyEnable) {
            Text(
                text = quantity,
                style = CommonTypography.current.smallTxt
            )
        }

        // Badge + Arrow
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {

            InventoryBadge(text = badgeText , backGroundColor = ErrorBgColor,textColor = RedColor, iconColor = RedColor)

            if(badgeText2.isNotEmpty()) {
                Spacer(modifier = Modifier.width(dimensionResource(R.dimen.dp_4)))
                InventoryBadge(text = badgeText2, backGroundColor = BackGround, textColor = TextSubtext , iconColor = BlackColor)
            }
        }

        Row(modifier= Modifier.clickable{ onItemClick?.invoke() },verticalAlignment = Alignment.CenterVertically) {
            Spacer(modifier = Modifier.width(dimensionResource(R.dimen.dp_4)))

            Image(
                painter = painterResource(R.drawable.icon__next),
                contentDescription = null
            )
        }
    }
}



@Composable
fun InventoryBadge(
    text: String,
    modifier: Modifier = Modifier,
    backGroundColor: Color,
    textColor: Color,
    iconColor: Color,
    textStyle: TextStyle = CommonTypography.current.noteText,
    padding: Dp = 5.dp
) {
    Row(
        modifier = modifier
            .background(
                backGroundColor,
                RoundedCornerShape(dimensionResource(R.dimen.dp_24))
            )
            .padding(
                horizontal = dimensionResource(R.dimen.dp_5),
                vertical = padding
            )
            .wrapContentHeight(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Icon(
            painter = painterResource(R.drawable.property_sort),
            contentDescription = null,
            tint = iconColor
        )

        Spacer(modifier = Modifier.width(dimensionResource(R.dimen.dp_2)))

        Text(
            text = text,
            style = CommonTypography.current.noteText,
            color = textColor
        )
    }
}


/*@Composable
fun ProductListView(
    onItemClick: (ProductItemUiModel) -> Unit
) {
            ProductListItem(
                onItemClick = { onItemClick(item) }
            )


}*/

