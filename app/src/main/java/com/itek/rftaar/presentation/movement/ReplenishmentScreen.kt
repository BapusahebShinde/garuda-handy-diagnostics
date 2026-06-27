package com.itek.rftaar.presentation.movement

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.itek.rftaar.R
import com.itek.rftaar.api.constants.ParameterConstants
import com.itek.rftaar.api.constants.UrlConstants
import com.itek.rftaar.core.common.constants.LoginConstants
import com.itek.rftaar.core.common.utils.LogUtils
import com.itek.rftaar.core.common.utils.ParseUtils
import com.itek.rftaar.core.common.utils.ParseUtils.extractInt
import com.itek.rftaar.core.common.utils.ParseUtils.extractJSONArray
import com.itek.rftaar.core.common.utils.ParseUtils.extractString
import com.itek.rftaar.core.database.AppDatabase
import com.itek.rftaar.core.database.DataStoreManager
import com.itek.rftaar.data.entity.ProductZoneDataEntity
import com.itek.rftaar.domain.model.LocationModel
import com.itek.rftaar.mqtt.constants.TopicConstants
import com.itek.rftaar.presentation.commonComp.AppSnackBar
import com.itek.rftaar.presentation.commonComp.BottomSheetType
import com.itek.rftaar.presentation.commonComp.CommonTextField
import com.itek.rftaar.presentation.commonComp.ErrorAppSnackBarData
import com.itek.rftaar.presentation.commonComp.FilterPicker
import com.itek.rftaar.presentation.commonComp.GernericBasicTextField
import com.itek.rftaar.presentation.commonComp.ImageViewFullScreen
import com.itek.rftaar.presentation.commonComp.ItekFooter
import com.itek.rftaar.presentation.commonComp.MultiFilterPicker
import com.itek.rftaar.presentation.commonComp.SnackbarController
import com.itek.rftaar.presentation.commonComp.StatItem
import com.itek.rftaar.presentation.commonComp.StockItem
import com.itek.rftaar.presentation.commonComp.TextFieldConfig
import com.itek.rftaar.presentation.commonComp.TopBarContent
import com.itek.rftaar.presentation.commonComp.openSheet
import com.itek.rftaar.presentation.decoding.saveProductHeaders
import com.itek.rftaar.presentation.encoding.DeviceSettings
import com.itek.rftaar.presentation.inventory.LocationPicker
import com.itek.rftaar.presentation.inventory.parseDisplayData
import com.itek.rftaar.presentation.inventory.parseDisplayValues
import com.itek.rftaar.presentation.navigation.Screen
import com.itek.rftaar.presentation.search.InventoryInfoRow
import com.itek.rftaar.presentation.search.ProductDetailsShort
import com.itek.rftaar.presentation.search.ProductImage
import com.itek.rftaar.presentation.search.SearchDetailsActionMode
import com.itek.rftaar.presentation.viewmodel.ApiViewModel
import com.itek.rftaar.ui.theme.BackGround
import com.itek.rftaar.ui.theme.BlackColor
import com.itek.rftaar.ui.theme.CommonTypography
import com.itek.rftaar.ui.theme.OutlineDefault
import com.itek.rftaar.ui.theme.RedColor
import com.itek.rftaar.ui.theme.TabColor
import com.itek.rftaar.ui.theme.WhiteColor
import com.itek.rftaar.ui.theme.Yellow
import com.itek.rftaar.utils.CommonUtils.chkNull
import com.itek.rftaar.utils.CommonUtils.chkTrue
import com.itek.rftaar.utils.ZoneUtils.processChildren
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReplenishmentScreen(
    modifier: Modifier,
    navController: NavHostController,
    label: String,
    menuCode: String,
    searchParams: Map<String, Any>,
    apiViewModel: ApiViewModel = hiltViewModel()) {

    val userId = DataStoreManager.readFromPreferences(ParameterConstants.USER_ID,"")
    //DataStoreManager.saveListStr(userId+"_"+menuCode+"_filters",emptyList())

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val topic = TopicConstants.REPLENISHMENT

    val snackbarController = remember { SnackbarController() }

    val isExpand = remember { mutableStateOf(false) }
    val preHeader = topic + "_" + menuCode + "_"
    val transactionType = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.TRANSACTION_TYPE, TopicConstants.REPLENISHMENT)//,DataStoreManager.readFromPreferences("replenishmentType",""))
    val stockListState = remember { mutableStateOf<List<StockItem>>(emptyList()) }

    val showLocationPicker = remember { mutableStateOf(false) }
    val srcLocationList = remember { mutableStateListOf<LocationModel>() }
    val selectedSrcLocationId = remember { mutableStateOf<LocationModel?>(null) }
    val selectedSrcLocationName = remember { mutableStateOf("") }


    val selFilterKeys = remember { mutableStateListOf<String>()}
    if(selFilterKeys.isEmpty()) selFilterKeys.addAll(DataStoreManager.getListStr(userId + "_" + menuCode + "_filters"))
    LogUtils.showLog("selFilterKeys",""+selFilterKeys.size)
        /*mutableStateListOf<String>().apply {
            addAll(DataStoreManager.getListStr(userId + "S_" + menuCode + "_filters"))
        }
    }*/

    val dynamicFilterPicker = remember { mutableStateOf(false) }
    val dynamicFilters = remember { mutableStateMapOf<String, List<Any>>() }
    val selectedFilters = remember { mutableStateMapOf<String, List<Any>>() }
    val selectedDynamicFilterName = remember { mutableStateOf("") }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { sheetValue ->
            sheetValue != SheetValue.Hidden
        }
    )
    val showSheet = remember { mutableStateOf(false) }
    val currentSheet = remember { mutableStateOf(BottomSheetType.NONE) }

    val replenishmentScreenArgs = ReplenishmentScreenArgs(
        context = context,
        label = label,
        topic = topic,
        menuCode = menuCode,
        transactionType = transactionType,
        scope = scope,
        sheetState = sheetState,
        showSheet = showSheet,
        currentSheet = currentSheet
    )

    val isApiLoading = apiViewModel.isLoading.observeAsState()
    //val getAllZones = db.productZoneDataDao().getAllZones(topic = topic, menuCode, transactionType).collectAsState(initial = emptyList())
    //val totalCount = db.productZoneDataDao().getTotalQty(topic, menuCode, transactionType).collectAsState(0)
    //val totalCountZone = db.productZoneDataDao().getTotalQty(topic, code, transactionType, selectedLocationName.value, selectedLocationPath.value).collectAsState(0)
    //val tagCount = db.tagInfoDao().getTotalCount(menuCode, transactionType).observeAsState(initial = 0)
    //val uploadedTagCount = db.tagInfoDao().getUploadedCount(menuCode, transactionType).observeAsState(initial = 0)
    val filterZoneKey = "Destination Zone"

    BackHandler(enabled = true) {
        LogUtils.showLog("Backhandler_1","Backhandler_1")
        if (chkTrue(isApiLoading.value)) return@BackHandler
        scope.launch {
            LogUtils.showLog("Backhandler_2","Backhandler_2")
            if (showSheet.value != false && currentSheet.value != BottomSheetType.NONE)
             //Code for Auto clearing
            {
                LogUtils.showLog("Backhandler_3","Backhandler_3")
                showSheet.value=false
              currentSheet.value = BottomSheetType.NONE
              return@launch
            }
            /*val preHeader = topic + "_" + menuCode + "_"
            val deviceSessionId = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.DEVICE_SESSION_ID, "")
            val id = DataStoreManager.readFromPreferences(preHeader+ParameterConstants.ID,"")
            if(deviceSessionId.isNullOrEmpty() || id.isNullOrEmpty()){*/
            if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                //DataStoreManager.saveToPreferences(preHeader + ParameterConstants.READER_POWER,30)
                //readerViewModel.clearSessionAndTransactionType()
                navController.popBackStack()
            }
            /*}
            else {
                openSheet(
                    scope = scope,
                    sheetState = sheetState,
                    showSheet = showSheet,
                    currentSheet = currentSheet,
                    sheet = BottomSheetType.SESSION
                )
            }*/
        }
    }

    LaunchedEffect(Unit) {
        LogUtils.showLog("D_LaunchedEffect","LaunchedEffect")
        if (apiViewModel != null) {
            CoroutineScope(Dispatchers.IO).launch {
                val map = hashMapOf(
                    ParameterConstants.CUSTOMER_ID to DataStoreManager.readFromPreferences(
                        ParameterConstants.CUSTOMER_ID,
                        ""
                    ),
                    ParameterConstants.OPERATION_LOCATION_ID to DataStoreManager.readFromPreferences(
                        LoginConstants.DEVICE_LOCATION_ID,
                        ""
                    )
                )
                LogUtils.showLog("D_callApi", "GET_REPLENISHMENT_LIST")
                apiViewModel.callApi(UrlConstants.GET_REPLENISHMENT_LIST, queryMap = map)
            }

            delay(30)
            //check if srcList is not empty
            //if (srcLocationList.isNullOrEmpty())
            CoroutineScope(Dispatchers.IO).launch {
                apiViewModel.callApi(
                    UrlConstants.LOCATION_SUB_ZONES,
                    appendData = DataStoreManager.readFromPreferences(
                        LoginConstants.DEVICE_LOCATION_ID,
                        ""
                    )
                )
            }

            delay(30)

            CoroutineScope(Dispatchers.IO).launch {
                val map1 = hashMapOf(
                    ParameterConstants.CUSTOMER_ID to DataStoreManager.readFromPreferences(
                        ParameterConstants.CUSTOMER_ID,
                        ""
                    ),
                    ParameterConstants.BUSINESS_LINE_ID to DataStoreManager.readFromPreferences(
                        ParameterConstants.BUSINESS_LINE_ID,
                        ""
                    ),
                )
                apiViewModel.callApi(UrlConstants.GET_REPLENISHMENT_DASHBOARD, queryMap = map1)
            }
        }
    }

    Box(modifier = Modifier.imePadding()) {
        Scaffold(
            bottomBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(WhiteColor),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    ReplenishmentBottomBar(replenishmentScreenArgs)
                }
            },
            modifier = modifier.background(BackGround)
        ) { innerPadding ->

            Box(
                modifier = Modifier
                    .background(BackGround)
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                ReplenishmentContent(
                    isExpand,
                    stockListState,
                    showLocationPicker,
                    srcLocationList,
                    selectedSrcLocationName,
                    selectedSrcLocationId,
                    dynamicFilterPicker,
                    dynamicFilters,
                    selectedFilters,
                    selFilterKeys,
                    selectedDynamicFilterName,
                    replenishmentScreenArgs,
                    snackbarController,
                    navController,
                    apiViewModel,
                    label,
                    menuCode,
                    filterZoneKey
                )
            }
        }

        if (showLocationPicker.value) {
            LocationPicker(
                srcLocationList,
                onDismiss = { showLocationPicker.value = false },
                onLocationSelected = { location ->
                    selectedSrcLocationId.value = location
                    selectedSrcLocationName.value = location.name
                    showLocationPicker.value = false
                }
            )
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

data class ReplenishmentScreenArgs @OptIn(ExperimentalMaterial3Api::class) constructor(
    val context: Context,
    val label: String,
    val topic: String,
    val menuCode: String,
    val transactionType: String,
    val scope: CoroutineScope,
    val sheetState: SheetState,
    val showSheet: MutableState<Boolean>,
    val currentSheet: MutableState<BottomSheetType>
)

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReplenishmentContent(
    isExpand: MutableState<Boolean>,
    stockListState: MutableState<List<StockItem>>,
    showLocationPicker: MutableState<Boolean>,
    srcLocationList: SnapshotStateList<LocationModel>,
    selectedSrcLocationName: MutableState<String>,
    selectedSrcLocationId: MutableState<LocationModel?>,
    dynamicFilterPicker: MutableState<Boolean>,
    dynamicFilters: SnapshotStateMap<String, List<Any>>,
    selectedFilters: SnapshotStateMap<String, List<Any>>,
    selFilterKeys: SnapshotStateList<String>,
    selectedDynamicFilterName: MutableState<String>,
    args: ReplenishmentScreenArgs,
    snackbarController: SnackbarController,
    navController: NavHostController,
    apiViewModel: ApiViewModel,
    label: String,
    menuCode: String,
    filterZoneKey: String,
) {

    val db = AppDatabase.getDbInstance(args.context)
    val getAll = db.productZoneDataDao().getAll1(topic = args.topic, args.menuCode, args.transactionType).collectAsState(initial = emptyList())
    val searchQuery = remember { mutableStateOf("") }

    val filteredList = remember(getAll.value, searchQuery.value, selectedFilters.toMap()) {

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

    val showImagePreview = remember { mutableStateOf(false) }
    val selectedPainter = remember { mutableStateOf<Painter?>(null) }

    val displayPendingCount = remember { mutableStateOf(0) }
    val displayCompleteCount = remember { mutableStateOf(0) }
    val belowRange = remember { mutableStateOf("") }
    val belowRangeCount = remember { mutableStateOf(0) }
    val withinRange = remember { mutableStateOf("") }
    val withinRangeCount = remember { mutableStateOf(0) }
    val aboveRange = remember { mutableStateOf("") }
    val aboveRangeCount = remember { mutableStateOf(0) }
    //val noDataErrMsg = stringResource(R.string.err_no_data)

    val response = apiViewModel.apiResult.collectAsState(initial = null)
    LaunchedEffect(response.value) {
        if (response.value == null) return@LaunchedEffect
        val result = response.value
        LogUtils.showLog("D_Result_"+result?.url,""+result?.isSuccess+"_"+result?.responseCode)
        if (result?.isSuccess != true) {
            LogUtils.showLog("D_Response"+result?.url,"Result_fail")
            val message = result?.errMsg
            if (!message.isNullOrBlank()) {
                snackbarController.show(ErrorAppSnackBarData(result.errMsg.toString()))
                if (result.url == UrlConstants.GET_REPLENISHMENT_LIST) {
                    CoroutineScope(Dispatchers.IO).launch {
                        db.productZoneDataDao().deleteAll(args.topic, args.menuCode, args.transactionType)
                    }
                }
            }
        }
        else if (result.response != null) {
            val jsonResponse = result.response
            LogUtils.showLog("D_Response"+result.url,"Response_Success")
            when (result.url) {
                UrlConstants.LOCATION_SUB_ZONES -> {
                    val dataObj = ParseUtils.extractJSONObject(
                        jsonResponse,
                        ParameterConstants.DATA,
                        jsonResponse
                    )
                    val responseArray = extractJSONArray(
                        dataObj,
                        ParameterConstants.CHILDREN,
                        JSONArray()
                    )
                    val dataListSrc = mutableListOf<LocationModel>()
                    if (responseArray != null && responseArray.length() > 0) {
                        processChildren(responseArray, dataListSrc,true);
                    }
                    if (dataListSrc.isNotEmpty() && dataListSrc.isNotEmpty()) {
                        srcLocationList.clear()
                        srcLocationList.addAll(dataListSrc)
                        LogUtils.showLog("DataHolder.locationList", "Replenishment:$dataListSrc")
                    }
                    else {
                        //Give custom error if responseArray is empty
                        snackbarController.show(ErrorAppSnackBarData("No Replenishment Source(s) are Configured.\nPlease Contact Admin."))
                    }
                }
                UrlConstants.GET_REPLENISHMENT_LIST -> {
                    LogUtils.showLog("D_API_RES","GET_REPLENISHMENT_LIST")
                    //temp code for using static response
                    //val response = JsonUtils.getSampleJSON(UrlConstants.GET_REPLENISHMENT_LIST)
                    val response = ParseUtils.extractJSONObject(jsonResponse, ParameterConstants.DATA, jsonResponse)
                    val responseArray = extractJSONArray(response, ParameterConstants.DATA, extractJSONArray(response, ParameterConstants.LIST, JSONArray()))
                    val dataList = mutableListOf<ProductZoneDataEntity>()
                    val filterListMap = HashMap<String, ArrayList<Any>>()
                    val headers  = saveProductHeaders(response)
                    //LogUtils.showLog("headers",""+headers.size)
                    val filterSelectionList = DataStoreManager.getFilterList().toMutableList()
                    filterSelectionList.add(0,filterZoneKey)
                    val filterHeaderList = filterSelectionList

                    //LogUtils.showLog("filterHeaderList",filterHeaderList.toString())
                    //set Filter Maps (i.e. Map<String,List<String>>) for filteration
                    LogUtils.showLog("D_API_RES_ARR",""+(if(responseArray != null) responseArray.length() else "null"))
                    if (responseArray != null && responseArray.length() > 0) {
                        //Note: Temp code to be changed later
                        for (i in 0 until responseArray.length()) {
                            val obj = responseArray.getJSONObject(i)
                            val zonePath= extractString(obj, ParameterConstants.ASSET_LOCATION_PATH, extractString(obj, ParameterConstants.DEST_LOCATION_PATH,extractString(obj, ParameterConstants.MOVE_AT_ASSET_LOCATION_PATH, "")))
                            val zoneName= extractString(obj, ParameterConstants.ASSET_LOCATION_NAME, extractString(obj, ParameterConstants.DEST_LOCATION_NAME, extractString(obj, ParameterConstants.MOVE_AT_ASSET_LOCATION_NAME, "")))
                            val loc = LocationModel(name=zoneName, path = zonePath)
                            if(filterHeaderList.contains(filterZoneKey) && chkNull(loc.name,"").isNotEmpty() && chkNull(loc.path,"").isNotEmpty()){
                                val listFilters = if(filterListMap.containsKey(filterZoneKey)) ArrayList<Any>(filterListMap.get(filterZoneKey))  else ArrayList<Any>()
                                if (!listFilters.contains(loc))listFilters.add(loc)
                                filterListMap.set(filterZoneKey,listFilters)
                            }
                            /*val array = JSONArray()
                            array.put("https://storage-cdn.weweb.io/75cef5c5-df84-444e-89c8-f3a5d1cc70e9/users-storage/b3906169/Captura+de+img.png")
                            array.put("https://pngimg.com/d/mario_PNG125.png")
                            obj.put("Images",array)*/
                            val listProductValues = ArrayList<Pair<String, String>>()
                            val keys = obj.keys()
                            while (keys.hasNext()) {
                                val key = keys.next()
                                if (key.isNullOrEmpty()) continue
                                if (!headers.contains(key)) continue
                                val value = chkNull(extractString(obj, key, ""), "")
                                //if (value.isNullOrEmpty() || value == "null") continue
                                listProductValues.add(key to value)
                                //code for saving filter
                                //LogUtils.showLog("filterHeaderList",key+"->"+value+"__"+filterHeaderList.contains(key))
                                if(filterHeaderList.contains(key) && chkNull(value,"").isNotEmpty()){
                                  val listFilters = if(filterListMap.containsKey(key)) ArrayList<Any>(filterListMap.get(key))  else ArrayList<Any>()
                                  if (!listFilters.contains(value))listFilters.add(value.toString())
                                  filterListMap.set(key,listFilters)
                                }
                            }
                            val productZoneDataModel = ProductZoneDataEntity(
                                topic = args.topic,
                                sessionType = args.menuCode,
                                transactionType = args.transactionType,
                                assetLocationPath = zonePath,
                                assetLocationName = zoneName,
                                quantity = extractInt(obj, ParameterConstants.QTY,0),
                                barcode = extractString(obj, ParameterConstants.BARCODE,extractString(obj, DataStoreManager.getBarcodeLabel(),extractString(obj, ParameterConstants.EAN, ""))),
                            )
                            val errMsg = extractString(obj, ParameterConstants.ERR_MSG, "")
                            if(errMsg.isNotEmpty()){
                                productZoneDataModel.errMsg = errMsg
                            }
                            if (listProductValues.isNotEmpty()) {
                                productZoneDataModel.displayData = listProductValues.toString()
                            }
                            val epcArray = extractJSONArray(obj, ParameterConstants.EPC, JSONArray())
                            if (epcArray != null && epcArray.length() > 0) {
                                for (i in 0 until epcArray.length()) {
                                    val epc = epcArray.getString(i)
                                    if (epc.isNotEmpty()) {
                                        if (i == 0) {
                                            productZoneDataModel.epc = epc
                                            dataList.add(productZoneDataModel)
                                        } else {
                                            val copiedProductZoneDataModel = productZoneDataModel.copy()
                                            copiedProductZoneDataModel.epc = epc
                                            dataList.add(copiedProductZoneDataModel)
                                        }
                                    }
                                }
                            }
                            else dataList.add(productZoneDataModel)

                            //val location = LocationRowItem()
                            /*id = obj.optString(ParameterConstants.ID),
                            name = obj.optString(ParameterConstants.NAME),
                            code = obj.optString(ParameterConstants.CODE),
                            path = obj.optString(ParameterConstants.PATH))*/
                            //dataList.add(location)
                        }
                    }
                    if (dataList.isNotEmpty()) {
                        LogUtils.showLog("FilterList", filterListMap.toString())
                        dynamicFilters.clear()
                        dynamicFilters.putAll(filterListMap)
                        LogUtils.showLog("D_FilterList", dynamicFilters.toString())

                        //saving custom field if required to display
                        LogUtils.showLog("dataList",""+dataList.toString())
                        CoroutineScope(Dispatchers.IO).launch {
                            db.productZoneDataDao().deleteAll(args.topic, args.menuCode, args.transactionType)
                            db.productZoneDataDao().insertAll(dataList)
                        }
                        //destLocationList.clear()
                        //destLocationList.addAll(dataZoneList)
                        //LogUtils.showLog("DataHolder.locationList", "StartInventoryContent:$dataList")
                    }
                    else {
                        //give custom error if responseArray is empty
                        CoroutineScope(Dispatchers.IO).launch {
                            db.productZoneDataDao().deleteAll(args.topic, args.menuCode, args.transactionType)
                        }
                        //snackbarController.show(ErrorAppSnackBarData(noDataErrMsg))

                    }
                }
                UrlConstants.GET_REPLENISHMENT_DASHBOARD -> {
                    val response = ParseUtils.extractJSONObject(jsonResponse, ParameterConstants.DATA, jsonResponse)
                    //set variables values for Dashboard
                    displayPendingCount.value =
                        extractInt(response, ParameterConstants.PENDING_COUNT,extractInt(response, ParameterConstants.PENDING_QTY,0))
                    displayCompleteCount.value =
                        extractInt(response, ParameterConstants.COMPLETED_TODAY_COUNT,extractInt(response, ParameterConstants.COMPLETED_TODAY_QTY,0))
                    val bucket = ParseUtils.extractJSONObject(response, ParameterConstants.BUCKET, response)
                    belowRange.value =extractString(bucket, ParameterConstants.BELOW_RANGE,"")
                    belowRangeCount.value =
                        extractInt(bucket, ParameterConstants.BELOW_RANGE_COUNT,extractInt(bucket, ParameterConstants.BELOW_RANGE_QTY,0))
                    withinRange.value = extractString(bucket, ParameterConstants.WITHIN_RANGE,"")
                    withinRangeCount.value =
                        extractInt(bucket, ParameterConstants.WITHIN_RANGE_COUNT,extractInt(bucket, ParameterConstants.WITHIN_RANGE_QTY,0))
                    aboveRange.value = extractString(bucket, ParameterConstants.ABOVE_RANGE,"")
                    aboveRangeCount.value =
                        extractInt(bucket, ParameterConstants.ABOVE_RANGE_COUNT,extractInt(bucket, ParameterConstants.ABOVE_RANGE_QTY,0))
                }
            }
        }
    }


    Box {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top
        )
        {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.Top) {
                TopBarContent(
                    args.label,
                    onBackClickL = {
                        if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                            //DataStoreManager.saveToPreferences(preHeader + ParameterConstants.READER_POWER,30)
                            //readerViewModel.clearSessionAndTransactionType()
                            navController.popBackStack()
                        }
                    }, onSettingClick = {
                        if (apiViewModel.isLoading.value == true) return@TopBarContent
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

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(dimensionResource(R.dimen.dp_16))
                    .imePadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                item {

                    ReplenishmentStatsCard(
                        items = listOf(
                            StatItem(
                                value = displayPendingCount.value.toString(),
                                label = stringResource(R.string.lbl_pending)
                            ),
                            StatItem(
                                value = displayCompleteCount.value.toString(),
                                label = stringResource(R.string.lbl_completed)
                            )
                        ) as List<StatItem>,
                        belowRange = belowRange.value,
                        belowRangeCount = belowRangeCount.value,
                        withinRange = withinRange.value,
                        withinRangeCount = withinRangeCount.value,
                        aboveRange = aboveRange.value,
                        aboveRangeCount = aboveRangeCount.value,
                    )

                    Spacer(modifier = Modifier.size(10.dp))

                    Row() {

                        if (selectedSrcLocationName.value.isNullOrEmpty()) {
                            Card(
                                modifier = Modifier
                                    .width(dimensionResource(R.dimen.dp_160))
                                    .height(dimensionResource(R.dimen.dp_44))
                                    .clickable(
                                        onClick = {
                                            if (srcLocationList.isNullOrEmpty()) {
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
                                        text = stringResource(R.string.select_src_zone),
                                        style = CommonTypography.current.noteText,
                                        color = BlackColor
                                    )
                                }
                            }
                        } else {
                            GernericBasicTextField(
                                sourceZone = selectedSrcLocationName.value,
                                destZone = "",
                                menuCode = menuCode,
                                value = selectedSrcLocationName.value,
                                labelRowAction = {
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
                                        selectedSrcLocationName.value = ""
                                        selectedSrcLocationId.value = null
                                        showLocationPicker.value = false
                                    }
                                },
                                isDestZone = false,
                            )
                        }
                        Spacer(modifier = Modifier.size(10.dp))
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
                            }
                            else {
                                GernericBasicTextField(
                                    sourceZone = selectedDynamicFilterName.value,
                                    destZone = "",
                                    menuCode = menuCode,
                                    value = selectedDynamicFilterName.value,
                                    labelRowAction = {
                                        args.scope.launch {
                                            dynamicFilterPicker.value = true
                                        }
                                    },
                                    destAction = {

                                    },
                                    clearAction = {
                                        args.scope.launch {
                                            selectedFilters.clear()
                                            selectedDynamicFilterName.value = ""
                                            dynamicFilterPicker.value = false
                                        }
                                    },
                                    isDestZone = false,
                                )
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
                            }),
                        modifier = Modifier
                            .padding(vertical = dimensionResource(R.dimen.dp_24))
                            .imePadding()
                    )

                }
                if (filteredList.isEmpty()) {

                    item {
                        Text(
                            text = stringResource(R.string.err_no_data),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                            style = CommonTypography.current.noteText,
                            color = RedColor
                        )
                    }

                } else {

                    items(
                        items = filteredList.toList(),
                        key = { it.first }
                    ) { (barcode, itemsInGroup) ->

                        // Use the first item in the group to extract common product details
                        val commonProduct = itemsInGroup.first()

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
                                        array.getString(index)
                                    }
                                }

                            } else {
                                emptyList()
                            }
                        } catch (e: Exception) {
                            emptyList()
                        }

                        val imagePainters = if (imageUrls.isEmpty()) {
                            listOf(rememberAsyncImagePainter(R.drawable.image))
                        } else {
                            imageUrls.map { rememberAsyncImagePainter(it) }
                        }

                        val title = values
                            .drop(1)
                            .take(2)
                            .filter { it.isNotBlank() }
                            .joinToString(",")

                        val sku = values
                            .drop(3)
                            .take(3)
                            .filter { it.isNotBlank() }
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

                                Spacer(
                                    modifier = Modifier.size(
                                        dimensionResource(R.dimen.dp_8)
                                    )
                                )

                                ProductDetailsShort(
                                    label = barcode,
                                    info = title,
                                    data = sku,
                                    commonProduct = commonProduct
                                )
                            }

                            HorizontalDivider(
                                thickness = dimensionResource(R.dimen.dp_1),
                                color = OutlineDefault,
                                modifier = Modifier.padding(
                                    vertical = dimensionResource(R.dimen.dp_10)
                                )
                            )

                            // Sub-list: Locations
                            itemsInGroup.forEach { locationItem ->

                                val cleanLocationName = locationItem.name
                                    .replace(Regex("\\(.*\\)"), "")
                                    .trim()

                                val displayLocation =
                                    if (cleanLocationName.length > 25) {
                                        cleanLocationName.take(25) + "..."
                                    } else {
                                        cleanLocationName
                                    }

                                val lblSrcZone =
                                    stringResource(R.string.source_zone)

                                val selErr =
                                    stringResource(R.string.err_select_)

                                InventoryInfoRow(
                                    location = displayLocation,
                                    quantity = "Qty:${locationItem.totalQty}",
                                    isQtyEnable = false,
                                    badgeText = "Pending:${locationItem.totalQty}",

                                    onItemClick = {

                                        if (selectedSrcLocationId.value == null) {

                                            snackbarController.show(
                                                ErrorAppSnackBarData(
                                                    String.format(
                                                        selErr,
                                                        lblSrcZone
                                                    )
                                                )
                                            )

                                            return@InventoryInfoRow
                                        }

                                        if (locationItem.errMsg.isNotEmpty()) {

                                            snackbarController.show(
                                                ErrorAppSnackBarData(
                                                    locationItem.errMsg
                                                )
                                            )

                                            return@InventoryInfoRow
                                        }

                                        val srcZone =
                                            selectedSrcLocationId.value!!

                                        val searchBundle = mapOf(
                                            "topic" to args.topic,
                                            "sessionType" to args.menuCode,
                                            "transactionType" to args.transactionType,
                                            locationItem::class.java.simpleName to locationItem,
                                            srcZone::class.java.simpleName to srcZone,
                                            "mode" to SearchDetailsActionMode.SEARCH_AND_PICK.toString(),
                                            "isShowUpload" to true,
                                        )

                                        val route =
                                            Screen.ProductSearch.createRoute(
                                                code = menuCode,
                                                label = label,
                                                params = searchBundle
                                            )

                                        navController.navigate(route)
                                    }
                                )

                                HorizontalDivider(
                                    thickness = 0.5.dp,
                                    color = OutlineDefault.copy(alpha = 0.4f),
                                    modifier = Modifier.padding(
                                        vertical = dimensionResource(R.dimen.dp_4)
                                    )
                                )
                            }

                            // Space between different product groups
                            Spacer(
                                modifier = Modifier.height(
                                    dimensionResource(R.dimen.dp_12)
                                )
                            )
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

                        val filterSelectionList = DataStoreManager.getFilterList().toMutableList()
                        filterSelectionList.add(0,"Destination Zone")
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
                            onApply = { selectedMap  ->
                                LogUtils.showLog("D_selectedMAp",selectedMap.toString())
                                val selectedValues = selectedMap.filter { it.isNotBlank() }.distinct()
                                LogUtils.showLog("D_selectedMAp1",selectedValues.toString())

                                selFilterKeys.clear()
                                selFilterKeys.addAll(selectedValues)

                                val userId = DataStoreManager.readFromPreferences(ParameterConstants.USER_ID,"")
                                DataStoreManager.saveListStr(userId+"_"+menuCode+"_filters",selectedValues)

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

                    else -> {

                    }
                }
            }
        }
        Box(modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center){
            if (showImagePreview.value) {
                ImageViewFullScreen(showImagePreview , selectedPainter)
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
fun ReplenishmentBottomBar(movementReplenishmentScreenArgs: ReplenishmentScreenArgs) {
    ItekFooter()
}


@Composable
fun ReplenishmentStatsCard(
    items: List<StatItem>,
    modifier: Modifier = Modifier,
    belowRange: String,
    belowRangeCount: Int,
    withinRange: String,
    withinRangeCount: Int,
    aboveRange: String,
    aboveRangeCount: Int
) {


    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(122.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        shape = RoundedCornerShape(dimensionResource(R.dimen.dp_16)),
        colors = CardDefaults.cardColors(containerColor = WhiteColor)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // --- 2. Header Row (Stat Items) ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimensionResource(R.dimen.dp_10))
                        .padding(
                            top = dimensionResource(R.dimen.dp_12),
                            bottom = dimensionResource(R.dimen.dp_8)
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items.forEachIndexed { index, item ->
                        val isLast = index == items.lastIndex

                        val conditionalModifier = if (isLast) {
                            Modifier
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(Color(0xFFFFF3D6), Color(0xFFFFFBF2))
                                    ), shape = RoundedCornerShape(dimensionResource(R.dimen.dp_12))
                                )
                                .padding(dimensionResource(R.dimen.dp_8))
                        } else Modifier

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .then(conditionalModifier),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = item.value,
                                style = CommonTypography.current.bigFont,
                                color = if (isLast) Yellow else BlackColor
                            )
                            Text(
                                text = item.label,
                                style = CommonTypography.current.smallTxt,
                                maxLines = 1,
                                modifier = Modifier.basicMarquee()
                            )
                        }

                        if (index == 0) {
                            VerticalDivider(
                                modifier = Modifier
                                    .height(43.dp)
                                    .padding(horizontal = 8.dp),
                                color = OutlineDefault
                            )
                        }
                    }
                }

                HorizontalDivider(thickness = 1.dp, color = OutlineDefault , modifier = Modifier.padding(top = 6.dp , bottom = 8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth() // Added to ensure it occupies the horizontal space
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Label 1: Below Range
                    Spacer(modifier = Modifier.size(5.dp))
                    BadgeText(label = "<$belowRange Hrs")
                    Text(
                        text = belowRangeCount.toString(),
                        style = CommonTypography.current.dashobardCount,
                        textAlign = TextAlign.Start
                    )

                    CustomDivider()

                    // Label 2: Within Range
                    BadgeText(label = "$withinRange Hrs")
                    Text(
                        text = withinRangeCount.toString(),
                        style = CommonTypography.current.dashobardCount,
                        textAlign = TextAlign.Center
                    )

                    CustomDivider()

                    // Label 3: Above Range
                    BadgeText(label = ">$aboveRange Hrs")
                    Text(
                        text = aboveRangeCount.toString(),
                        style = CommonTypography.current.dashobardCount,
                        textAlign = TextAlign.End,
                        modifier = Modifier.padding(end = 13.dp)
                    )
                }
            }
        }
    }
}


@Composable
fun BadgeText(label: String) {
    Text(
        text = label,
        style = CommonTypography.current.smallTxt,
        modifier = Modifier
            .background(color = TabColor, shape = RoundedCornerShape(32.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp) // Controlled padding
    )
}

@Composable
fun CustomDivider() {
    VerticalDivider(
        modifier = Modifier
            .height(22.dp)
            .padding(horizontal = 12.dp), // Balanced padding
        color = OutlineDefault
    )
}