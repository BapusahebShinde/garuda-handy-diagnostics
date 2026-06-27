package com.itek.rftaar.presentation.inventory

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.layout.heightIn
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import com.itek.rftaar.R
import com.itek.rftaar.ReaderActivity
import com.itek.rftaar.api.constants.ParameterConstants
import com.itek.rftaar.api.constants.UrlConstants
import com.itek.rftaar.core.common.constants.LoginConstants
import com.itek.rftaar.core.common.utils.BaseUtils
import com.itek.rftaar.core.common.utils.LogUtils
import com.itek.rftaar.core.common.utils.ParseUtils
import com.itek.rftaar.core.common.utils.ParseUtils.extractJSONArray
import com.itek.rftaar.core.common.utils.ParseUtils.extractString
import com.itek.rftaar.core.database.AppDatabase
import com.itek.rftaar.core.database.DataStoreManager
import com.itek.rftaar.data.entity.ProductZoneDataEntity
import com.itek.rftaar.data.model.ProductZoneFoundQty
import com.itek.rftaar.data.model.ZoneFoundQty
import com.itek.rftaar.domain.model.LocationModel
import com.itek.rftaar.mqtt.MqttManager.showLog
import com.itek.rftaar.mqtt.constants.InventoryConstants
import com.itek.rftaar.mqtt.constants.TopicConstants
import com.itek.rftaar.presentation.commonComp.AppSnackBar
import com.itek.rftaar.presentation.commonComp.AppSnackBarData
import com.itek.rftaar.presentation.commonComp.BottomSheetType
import com.itek.rftaar.presentation.commonComp.CommonButton
import com.itek.rftaar.presentation.commonComp.CommonTextField
import com.itek.rftaar.presentation.commonComp.CounterText
import com.itek.rftaar.presentation.commonComp.ErrorAppSnackBarData
import com.itek.rftaar.presentation.commonComp.FilterPicker
import com.itek.rftaar.presentation.commonComp.GernericBasicTextField
import com.itek.rftaar.presentation.commonComp.IconText
import com.itek.rftaar.presentation.commonComp.ImageViewFullScreen
import com.itek.rftaar.presentation.commonComp.ItekFooter
import com.itek.rftaar.presentation.commonComp.MultiFilterPicker
import com.itek.rftaar.presentation.commonComp.SnackbarController
import com.itek.rftaar.presentation.commonComp.TextFieldConfig
import com.itek.rftaar.presentation.commonComp.TopBarContent
import com.itek.rftaar.presentation.commonComp.TwoButtonView
import com.itek.rftaar.presentation.commonComp.UploadStatusChip
import com.itek.rftaar.presentation.commonComp.openSheet
import com.itek.rftaar.presentation.decoding.saveProductHeaders
import com.itek.rftaar.presentation.encoding.DeviceSettings
import com.itek.rftaar.presentation.viewmodel.ApiViewModel
import com.itek.rftaar.presentation.viewmodel.ReaderViewModel
import com.itek.rftaar.reader.constants.ActionConstants
import com.itek.rftaar.ui.theme.BackGround
import com.itek.rftaar.ui.theme.BlackColor
import com.itek.rftaar.ui.theme.ButtonGray
import com.itek.rftaar.ui.theme.CardGreen
import com.itek.rftaar.ui.theme.CommonTypography
import com.itek.rftaar.ui.theme.ErrorBgColor
import com.itek.rftaar.ui.theme.Green
import com.itek.rftaar.ui.theme.OutlineDefault
import com.itek.rftaar.ui.theme.RedColor
import com.itek.rftaar.ui.theme.TabColor
import com.itek.rftaar.ui.theme.TextGrey
import com.itek.rftaar.ui.theme.TextSubtext
import com.itek.rftaar.ui.theme.WhiteColor
import com.itek.rftaar.ui.theme.Yellow
import com.itek.rftaar.utils.CommonUtils.chkNull
import com.itek.rftaar.utils.CommonUtils.chkTrue
import com.itek.rftaar.utils.NetworkUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.roundToInt

@SuppressLint("SuspiciousIndentation")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockCorrection(
    modifier: Modifier,
    navController: NavHostController,
    label: String,
    menuCode: String,
    apiViewModel: ApiViewModel = hiltViewModel(),
    searchParams: Map<String, Any> = emptyMap(),
) {
    val userId = DataStoreManager.readFromPreferences(ParameterConstants.USER_ID,"")
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = AppDatabase.getDbInstance(context)
    val hasData = db.productZoneDataDao().hasData().collectAsState(false)

    val preHeader = TopicConstants.INVENTORY + "_" + menuCode + "_"
    val deviceSessionId = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.DEVICE_SESSION_ID, "")
    val id = DataStoreManager.readFromPreferences(preHeader+ParameterConstants.ID,"")
    val transactionType = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.TRANSACTION_TYPE, InventoryConstants.INVENTORY_CORRECTION)
    LogUtils.showLog("transactionType", "StockCorrection: ${transactionType}")
    val snackbarController = remember { SnackbarController() }

    val selFilterKeys = remember { mutableStateListOf<String>()}
    if(selFilterKeys.isEmpty()) selFilterKeys.addAll(DataStoreManager.getListStr(userId + "_" + menuCode + "_filters"))
    LogUtils.showLog("selFilterKeys",""+selFilterKeys.size)

    val dynamicFilterPicker = remember { mutableStateOf(false) }
    val dynamicFilters = remember { mutableStateMapOf<String, List<Any>>() }
    val selectedFilters = remember { mutableStateMapOf<String, List<Any>>() }
    val selectedDynamicFilterName = remember { mutableStateOf("") }

    val showLocationPicker = remember { mutableStateOf(false) }
    val assetLocationName = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.ASSET_LOCATION_NAME, "")
    val assetLocationPath = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.ASSET_LOCATION_PATH, "")
    val selectedLocationName = remember { mutableStateOf(assetLocationName) }
    val selectedLocationPath = remember { mutableStateOf(assetLocationPath) }
    val locationList = remember { mutableStateListOf<LocationModel>() }
    val selectedLocationId = remember { mutableStateOf<LocationModel?>(null) }
    val activity = context as? ReaderActivity
    val readerViewModel = if (activity != null) activity.findReaderViewModel() else hiltViewModel()
    if (activity != null) {
        readerViewModel.onCreate()
        readerViewModel.setSessionAndTransactionType(menuCode, transactionType, topic=TopicConstants.INVENTORY)
        if(deviceSessionId.isNotEmpty() && id.isNotEmpty()){
            val sessionData = JSONObject()
            sessionData.put(ParameterConstants.SESSION_ID,deviceSessionId)
            sessionData.put(ParameterConstants.ID,id)
            sessionData.put(ParameterConstants.TRANSACTION_TYPE,transactionType)
            sessionData.put(ParameterConstants.ASSET_LOCATION_PATH,assetLocationPath)
            readerViewModel.setSessionId(deviceSessionId,sessionData.toString())
        }
    }
    val zoneName = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.ASSET_LOCATION_NAME, "")
    val zonePath = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.ASSET_LOCATION_PATH, "")
    val readerPower = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.READER_POWER, 30)
    val isInvOn = readerViewModel.isInventoryOn().observeAsState()
    val isApiLoading = apiViewModel.isLoading.observeAsState()

    val getAllZones = db.productZoneDataDao().getAllZones(topic = TopicConstants.INVENTORY, menuCode, transactionType).collectAsState(initial = emptyList())
    val getAll = (if (selectedLocationName.value.isNotEmpty() && selectedLocationPath.value.isNotEmpty()) db.productZoneDataDao().getAll(TopicConstants.INVENTORY, menuCode, transactionType, selectedLocationName.value, selectedLocationPath.value) else db.productZoneDataDao().getAll(topic = TopicConstants.INVENTORY, menuCode, transactionType)).collectAsState(initial = emptyList())
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { sheetValue ->
            sheetValue != SheetValue.Hidden
        }
    )
    val showSheet = remember { mutableStateOf(false) }
    val currentSheet = remember { mutableStateOf(BottomSheetType.NONE) }
    val setPower = rememberSaveable { mutableStateOf(readerPower) }
    readerViewModel.setPower(setPower.value)
    val foundQty = db.productZoneDataDao().getFoundQty(TopicConstants.INVENTORY,menuCode,transactionType).collectAsState(0)
    val isShowUploadSwipe = remember { mutableStateOf(false) }
    val isDataUploaded = remember { mutableStateOf(false) }
    val isOnDataUploaded = remember { mutableStateOf(false) }
    val validCount = db.tagInfoDao().getValidCount(menuCode,transactionType).observeAsState(initial = 0)
    val invalidCount = db.tagInfoDao().getInvalidCount(menuCode,transactionType).observeAsState(initial = 0)

    BackHandler(enabled = true) {
        if(chkTrue(isInvOn.value) && chkTrue(isApiLoading.value)) return@BackHandler
        if(chkTrue(isShowUploadSwipe.value)) {isShowUploadSwipe.value=false; return@BackHandler }
        scope.launch {
            val preHeader = TopicConstants.INVENTORY + "_" + menuCode + "_"
            val deviceSessionId = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.DEVICE_SESSION_ID, "")
            val id = DataStoreManager.readFromPreferences(preHeader+ParameterConstants.ID,"")
            if(deviceSessionId.isNullOrEmpty() || id.isNullOrEmpty()){
                if(navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                    DataStoreManager.saveToPreferences(preHeader + ParameterConstants.READER_POWER,30)
                    readerViewModel.clearSessionAndTransactionType()
                    navController.popBackStack()
                }
            }
            else {
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

    LaunchedEffect(Unit) {
        if (apiViewModel != null && (deviceSessionId.isNullOrEmpty() || id.isNullOrEmpty())) {

            val map = hashMapOf(
                ParameterConstants.CUSTOMER_ID to
                        DataStoreManager.readFromPreferences(
                            ParameterConstants.CUSTOMER_ID, ""
                        ),

                ParameterConstants.OPERATION_LOCATION_ID to
                        DataStoreManager.readFromPreferences(
                            LoginConstants.DEVICE_LOCATION_ID, ""
                        ),

                ParameterConstants.BUSINESS_LINE_ID1 to
                        DataStoreManager.readFromPreferences(
                            ParameterConstants.BUSINESS_LINE_ID, ""
                        )
            )

            apiViewModel.callApi(
                UrlConstants.STOCK_DISCREPANCY_LOCATION,
                queryMap = map
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                Box(
                    modifier = modifier
                        .fillMaxWidth()
                        .background(WhiteColor),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    /*StockCorrectionBottomBar()*/
                    StockCorrectionBottomBarView(
                        readerViewModel,
                        showLocationPicker.value,
                        showSheet,
                        sheetState,
                        currentSheet,
                        scope,
                        foundQty,
                        isOnDataUploaded,
                        isInvOn,
                        isShowUploadSwipe
                    )
                }
            },
            modifier = Modifier.background(BackGround)
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
                /* StockCorrectionContent(
                     context,
                     scope,
                     label,
                     menuCode,
                     transactionType,
                     navController,
                     apiViewModel
                 )*/
                 if (isOnDataUploaded.value) {
                     SuccessComponent(chkNull(foundQty.value,0),selectedLocationName)
                 }else{
                     StockCorrectionContentView(
                         context,
                         scope,
                         label,
                         menuCode,
                         transactionType,
                         navController,
                         apiViewModel,
                         showLocationPicker,
                         locationList,
                         getAllZones,
                         getAll,
                         readerViewModel,
                         selectedLocationName,
                         selectedLocationPath,
                         setPower,
                         showSheet,
                         sheetState,
                         currentSheet,
                         isDataUploaded,
                         isOnDataUploaded,
                         foundQty,
                         isInvOn,
                         isApiLoading,
                         snackbarController,
                         validCount,
                         invalidCount,
                         isShowUploadSwipe,
                         dynamicFilters,
                         dynamicFilterPicker,
                         selFilterKeys,
                         selectedFilters,
                         selectedDynamicFilterName,
                         selectedLocationId
                     )
                 }

            }
        }

        if (showLocationPicker.value) {
            LocationPicker(
                getAllZones.value,
                onDismiss = { showLocationPicker.value = false },
                onLocationSelected = { location ->
                    selectedLocationId.value = location
                    selectedLocationName.value = location.name
                    selectedLocationPath.value = location.path
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

@Composable
fun EanQtyListView(
    epcList: List<ZoneFoundQty>,
    onVerifyClick: (ZoneFoundQty) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {

        epcList.forEach { item ->

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        // Action to perform on click
                        onVerifyClick(item)
                        println("Row clicked!")
                    }
                    .padding(
                        horizontal = dimensionResource(R.dimen.dp_16),
                        vertical = dimensionResource(R.dimen.dp_8)
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {


                Text(
                    text = item.name,
                    modifier = Modifier.weight(1f),
                    style = CommonTypography.current.textSemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.width(dimensionResource(R.dimen.dp_12)))

                // Timestamp (right)
                Text(
                    text = item.foundQty.toString() + "/" + item.totalQty,
                    style = CommonTypography.current.smallTxt,
                    color = TextSubtext
                )

                //LogUtils.showLog("formatTimestamp", "EanListView: ${formatTimestamp(item.timeStamp)}")

            }

            HorizontalDivider(
                thickness = dimensionResource(R.dimen.dp_1),
                color = OutlineDefault
            )
        }
    }
}


@Composable
fun StockCorrectionBottomBar() {
    ItekFooter()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockCorrectionContentView(
    context: Context,
    scope: CoroutineScope,
    label: String,
    menuCode: String,
    transactionType: String,
    navController: NavHostController,
    apiViewModel: ApiViewModel,
    showLocationPicker: MutableState<Boolean>,
    locationList: SnapshotStateList<LocationModel>,
    getAllZones: State<List<LocationModel>>,
    getAll: State<List<ProductZoneFoundQty>>,
    readerViewModel: ReaderViewModel,
    selectedLocationName: MutableState<String>,
    selectedLocationPath: MutableState<String>,
    setPower: MutableState<Int>,
    showSheet: MutableState<Boolean>,
    sheetState: SheetState,
    currentSheet: MutableState<BottomSheetType>,
    isDataUploaded: MutableState<Boolean>,
    isOnDataUploaded: MutableState<Boolean>,
    foundQty: State<Int>,
    isInvOn: State<Boolean?>,
    isApiLoading: State<Boolean?>,
    snackbarController: SnackbarController,
    validCount: State<Int>,
    invalidCount: State<Int>,
    isShowUploadSwipe: MutableState<Boolean>,
    dynamicFilters: SnapshotStateMap<String, List<Any>>,
    dynamicFilterPicker: MutableState<Boolean>,
    selFilterKeys: SnapshotStateList<String>,
    selectedFilters: SnapshotStateMap<String, List<Any>>,
    selectedDynamicFilterName: MutableState<String>,
    selectedLocationId: MutableState<LocationModel?>,
) {
    val preHeader = TopicConstants.INVENTORY + "_" + menuCode + "_"
    val id = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.ID, "")
    val deviceSessionId = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.DEVICE_SESSION_ID, "")
    val locationMessage = stringResource(R.string.err_select_location)
    val epcMessage = stringResource(R.string.err_no_data)
    val db = AppDatabase.getDbInstance(context)
    val triggerPressed = readerViewModel.isTriggerPressed().observeAsState(false)
    val listEpcs = db.productZoneDataDao().getEpcs(
        TopicConstants.INVENTORY,
        menuCode,
        transactionType,
        selectedLocationName.value,
        selectedLocationPath.value
    ).collectAsState(emptyList())
    val totalCount = db.productZoneDataDao().getTotalQty(TopicConstants.INVENTORY, menuCode, transactionType).collectAsState(0)
    val totalCountZone = db.productZoneDataDao().getTotalQty(TopicConstants.INVENTORY, menuCode, transactionType, selectedLocationName.value, selectedLocationPath.value).collectAsState(0)
    LogUtils.showLog(
        "Selected values",
        "StockCorrectionContentView: ${selectedLocationName.value} \n ${selectedLocationPath.value}"
    )
    val tagCount = db.tagInfoDao().getTotalCount(menuCode,transactionType).observeAsState(initial = 0)
    val uploadedTagCount = db.tagInfoDao().getUploadedCount(menuCode,transactionType).observeAsState(initial = 0)

    val searchText = remember { mutableStateOf("") }
    val showImagePreview = remember { mutableStateOf(false) }
    val selectedPainter = remember { mutableStateOf<Painter?>(null) }

    val filteredList = remember(getAll.value, searchText.value,selectedFilters.toMap()) {
        val query = searchText.value.trim()
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

        sourceList
        /*if (searchText.value.isBlank()) {
            getAll.value
        } else {
            val query = searchText.value.trim()
            getAll.value.filter {
                it.barcode.contains(query, ignoreCase = true)
            }
        }*/
    }

    LaunchedEffect(filteredList) {
        showLog("FilteredList:",filteredList.toString())
    }

    LaunchedEffect(Unit) {
        LogUtils.showLog("DB_PARAMS", "topic=${TopicConstants.INVENTORY}")
        LogUtils.showLog("DB_PARAMS", "sessionType=$menuCode")
        LogUtils.showLog("DB_PARAMS", "transactionType=$transactionType")
        LogUtils.showLog("getAllZones", "getAllZones=${getAllZones}")
    }

    val response = apiViewModel.apiResult.collectAsState(initial = null)
    LaunchedEffect(response.value) {
        if(response.value==null) return@LaunchedEffect
        LogUtils.showLog("view","StockCorrectionContentView")
        val result = response.value
        if (result?.isSuccess != true) {
            isDataUploaded.value=false
            val message = result?.errMsg
            if (!message.isNullOrBlank()) {
                  snackbarController.show(ErrorAppSnackBarData(result.errMsg.toString()))
                if(result.url == UrlConstants.STOCK_DISCREPANCY_LOCATION) {
                    CoroutineScope(Dispatchers.IO).launch{
                     db.productZoneDataDao().deleteAll(TopicConstants.INVENTORY, menuCode, transactionType)
                   }
                }
            }
        }
        else if (result.response != null) {
            val jsonResponse = result.response

            when (result.url) {
                UrlConstants.STOCK_DISCREPANCY_LOCATION -> {
                    val response = ParseUtils.extractJSONObject(jsonResponse, ParameterConstants.DATA, jsonResponse)
                    val customField = extractString(response, ParameterConstants.CUSTOM_DISPLAY_COLUMN_NAME, "")
                    val responseArray = extractJSONArray(response, ParameterConstants.LIST, JSONArray())
                    val dataList = mutableListOf<ProductZoneDataEntity>()
                    val headers  = saveProductHeaders(response)
                    val filterSelectionList = DataStoreManager.getFilterList().toMutableList()
                    val filterHeaderList = filterSelectionList
                    val filterListMap = HashMap<String, ArrayList<Any>>()
                    if (responseArray != null && responseArray.length() > 0) {
                        for (i in 0 until responseArray.length()) {
                            val obj = responseArray.getJSONObject(i)
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
                                if(filterHeaderList.contains(key) && chkNull(value,"").isNotEmpty()){
                                    val listFilters = if(filterListMap.containsKey(key)) ArrayList<Any>(filterListMap.get(key))  else ArrayList<Any>()
                                    if (!listFilters.contains(value))listFilters.add(value.toString())
                                    filterListMap.set(key,listFilters)
                                }
                            }
                            val productZoneDataModel = ProductZoneDataEntity(
                                topic = TopicConstants.INVENTORY,
                                sessionType = menuCode,
                                transactionType = transactionType,
                                assetLocationPath = extractString(
                                    obj,
                                    ParameterConstants.ASSET_LOCATION_PATH,
                                    ""
                                ),
                                assetLocationName = extractString(
                                    obj,
                                    ParameterConstants.ASSET_LOCATION_NAME,
                                    ""
                                ),
                                barcode = extractString(obj, ParameterConstants.BARCODE, "")
                            )
                            if (customField.isNotEmpty() && obj.has(customField)) {
                                productZoneDataModel.customField = extractString(obj, customField, "")
                            }
                            if (listProductValues.isNotEmpty()) {
                                LogUtils.showLog("listProductValues", listProductValues.toString())
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
                        }
                    }
                    if (dataList.isNotEmpty()) {
                        LogUtils.showLog("FilterList", filterListMap.toString())
                        dynamicFilters.clear()
                        dynamicFilters.putAll(filterListMap)
                        LogUtils.showLog("D_FilterList", dynamicFilters.toString())
                        //saving cusom field if required to display
                        val preHeader = TopicConstants.INVENTORY + "_" + menuCode + "_"
                        DataStoreManager.saveToPreferences(preHeader + ParameterConstants.CUSTOM_DISPLAY_COLUMN_NAME, customField)
                        CoroutineScope(Dispatchers.IO).launch {
                            db.productZoneDataDao().deleteAll(TopicConstants.INVENTORY, menuCode, transactionType)
                            db.productZoneDataDao().insertAll(dataList)
                        }
                        /*locationList.clear()
                        locationList.addAll(dataList)*/
                        //LogUtils.showLog("DataHolder.locationList", "StartInventoryContent:$dataList")
                    } else {
                        //TODO give custom error if responseArray is empty
                    }
                }

                UrlConstants.START_DEVICE_SESSION_INVENTORY -> {
                    val id = extractString(jsonResponse, ParameterConstants.ID, "")
                    val deviceSessionId = extractString(jsonResponse, ParameterConstants.DEVICE_SESSION_ID, "")
                    val transactionType = extractString(jsonResponse, ParameterConstants.TRANSACTION_TYPES, extractString(jsonResponse, ParameterConstants.TRANSACTION_TYPE, ""))
                    val assetLocationPath = extractString(jsonResponse, ParameterConstants.ASSET_LOCATION_PATH, "")
                    val sessionData = JSONObject()
                    sessionData.put(ParameterConstants.SESSION_ID,deviceSessionId)
                    sessionData.put(ParameterConstants.ID,id)
                    sessionData.put(ParameterConstants.TRANSACTION_TYPE,transactionType)
                    sessionData.put(ParameterConstants.ASSET_LOCATION_PATH,assetLocationPath)
                    readerViewModel.setSessionId(deviceSessionId,sessionData.toString())
                    val preHeader = TopicConstants.INVENTORY + "_" + menuCode + "_"
                    DataStoreManager.saveToPreferences(preHeader + ParameterConstants.ID, id)
                    DataStoreManager.saveToPreferences(preHeader + ParameterConstants.DEVICE_SESSION_ID, deviceSessionId)
                    DataStoreManager.saveToPreferences(preHeader + ParameterConstants.TRANSACTION_TYPE, transactionType)
                    DataStoreManager.saveToPreferences(preHeader + ParameterConstants.ASSET_LOCATION_NAME, selectedLocationName.value)
                    DataStoreManager.saveToPreferences(preHeader + ParameterConstants.ASSET_LOCATION_PATH, assetLocationPath)
                    readerViewModel.toggleInventory(setPower.value,epcs = listEpcs.value, updateFound = true)
                }

                UrlConstants.STOP_DEVICE_SESSION_INVENTORY, UrlConstants.CANCEL_DEVICE_SESSION_INVENTORY -> {
                    if(isDataUploaded.value) {
                        try {
                            isOnDataUploaded.value = true
                            sheetState.hide()
                            showSheet.value = false
                            currentSheet.value = BottomSheetType.NONE
                            delay(500)
                        }catch (e: Exception){e.printStackTrace()}
                    }
                    CoroutineScope(Dispatchers.IO).launch {
                        if(isDataUploaded.value) delay(500)
                        val preHeader = TopicConstants.INVENTORY + "_" + menuCode + "_"
                        DataStoreManager.saveToPreferences(preHeader + ParameterConstants.ID, "")
                        DataStoreManager.saveToPreferences(preHeader + ParameterConstants.DEVICE_SESSION_ID, "")
                        DataStoreManager.saveToPreferences(preHeader + ParameterConstants.TRANSACTION_TYPE, "")
                        DataStoreManager.saveToPreferences(preHeader + ParameterConstants.ASSET_LOCATION_NAME, "")
                        DataStoreManager.saveToPreferences(preHeader + ParameterConstants.ASSET_LOCATION_PATH, "")
                        DataStoreManager.saveToPreferences(preHeader + ParameterConstants.CUSTOM_DISPLAY_COLUMN_NAME, "")
                        DataStoreManager.saveToPreferences(preHeader + ParameterConstants.READER_POWER, 30)
                        db.tagInfoDao().deleteBySessionTypeAndTransactionType(sessionType = menuCode, transactionType)
                        db.productZoneDataDao().deleteAll()
                    }
                    readerViewModel.clearSessionAndTransactionType()
                    navController.popBackStack()
                }
            }
        }
    }

    LaunchedEffect(isDataUploaded.value) {
        LogUtils.showLog("isDataUploaded", "StockCorrectionContentView: ${isDataUploaded.value}")
        if (isDataUploaded.value == true) {
            try {
                if (chkTrue(readerViewModel.isProcessOn().value)) return@LaunchedEffect
                val preHeader = TopicConstants.INVENTORY + "_" + menuCode + "_"
                val id = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.ID, "")
                val jsonRequest = JSONObject().apply {
                    put(ParameterConstants.ID, id)
                    put(ParameterConstants.VALID_SCAN_TAG_COUNT, validCount.value)
                    put(ParameterConstants.INVALID_SCAN_TAG_COUNT, invalidCount.value)
                    put(ParameterConstants.ACTION,ActionConstants.UPLOAD)
                }
                apiViewModel.callApi(UrlConstants.STOP_DEVICE_SESSION_INVENTORY, jsonRequest = jsonRequest)
                if(!NetworkUtils.isInternetConnected(context)) isDataUploaded.value=false
            }catch (e: Exception) {
                e.printStackTrace()
                isDataUploaded.value=false;
            }
        }
    }

    LaunchedEffect(triggerPressed.value) {
        if (triggerPressed.value == true) {
            readerViewModel.setTriggerValue(false)
            LogUtils.showLog("TriggerPressed", "Trigger is pressed")
            if (chkTrue(isApiLoading.value)) return@LaunchedEffect
            if(navController.currentBackStackEntry?.lifecycle?.currentState != Lifecycle.State.RESUMED) return@LaunchedEffect
            if(isDataUploaded.value || isOnDataUploaded.value) return@LaunchedEffect
            if(showSheet.value!=false && currentSheet.value != BottomSheetType.NONE) return@LaunchedEffect
            val preHeader = TopicConstants.INVENTORY + "_" + menuCode + "_"
            val deviceSessionId = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.DEVICE_SESSION_ID, "")
            val id = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.ID, "")
            if(chkTrue(isShowUploadSwipe.value)) isShowUploadSwipe.value=false
            if (selectedLocationPath.value.isNullOrEmpty() || selectedLocationName.value.isNullOrEmpty()) {
                snackbarController.show(
                    AppSnackBarData(
                        icon = R.drawable.error,
                        message = locationMessage,
                        showCancel = false
                    )
                )
                //readerViewModel.onDestroy()
                //navController.popBackStack()
            }
            else if (listEpcs.value.isNullOrEmpty()) {
                snackbarController.show(
                    AppSnackBarData(
                        icon = R.drawable.error,
                        message = epcMessage,
                        showCancel = false
                    )
                )
                //readerViewModel.onDestroy()
                //navController.popBackStack()
            }
            else if (deviceSessionId.isNullOrEmpty() || id.isNullOrEmpty()) {
                try {
                    val jsonRequest = JSONObject()
                    jsonRequest.put(ParameterConstants.CUSTOMER_ID, DataStoreManager.readFromPreferences(ParameterConstants.CUSTOMER_ID, ""))
                    jsonRequest.put(ParameterConstants.OPERATION_LOCATION_ID, DataStoreManager.readFromPreferences(LoginConstants.DEVICE_LOCATION_ID, ""))
                    jsonRequest.put(ParameterConstants.USER_ID, DataStoreManager.readFromPreferences(ParameterConstants.USER_ID, ""))
                    jsonRequest.put(ParameterConstants.DEVICE_ID, DataStoreManager.readFromPreferences(ParameterConstants.DEVICE_ID, ""))
                    jsonRequest.put(ParameterConstants.TRANSACTION_TYPES, transactionType)
                    jsonRequest.put(ParameterConstants.ASSET_LOCATION_PATH, selectedLocationPath.value)
                    jsonRequest.put(ParameterConstants.DEVICE_SESSION_ID, "")
                    jsonRequest.put(ParameterConstants.VALID_SCAN_TAG_COUNT, 0)
                    jsonRequest.put(ParameterConstants.INVALID_SCAN_TAG_COUNT, 0)
                    apiViewModel.callApi(UrlConstants.START_DEVICE_SESSION_INVENTORY, jsonRequest = jsonRequest)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            else readerViewModel.toggleInventory(setPower.value,epcs = listEpcs.value, updateFound = true)
        } else {
            LogUtils.showLog("TriggerPressed", "Trigger released")
        }
    }


    val hasActiveSession = deviceSessionId.isNotEmpty() && id.isNotEmpty()

    Box(modifier = Modifier.fillMaxSize().imePadding()) {
        Column(
            modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
        )
        {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.Top) {
                TopBarContent(
                    label,
                    onBackClickL = {
                        if(chkTrue(isInvOn.value) && chkTrue(isApiLoading.value)) return@TopBarContent
                        if(chkTrue(isShowUploadSwipe.value)) {isShowUploadSwipe.value=false; return@TopBarContent }
                        scope.launch {
                            val preHeader = TopicConstants.INVENTORY + "_" + menuCode + "_"
                            val deviceSessionId = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.DEVICE_SESSION_ID, "")
                            val id = DataStoreManager.readFromPreferences(preHeader+ParameterConstants.ID,"")
                            if(deviceSessionId.isNullOrEmpty() || id.isNullOrEmpty()){
                                if(navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                                    DataStoreManager.saveToPreferences(preHeader + ParameterConstants.READER_POWER, 30)
                                    readerViewModel.clearSessionAndTransactionType()
                                    navController.popBackStack()}
                            }
                            else {
                                openSheet(
                                    scope = scope,
                                    sheetState = sheetState,
                                    showSheet = showSheet,
                                    currentSheet = currentSheet,
                                    sheet = BottomSheetType.SESSION
                                )
                            }
                        }
                    }, onSettingClick = {
                        if (readerViewModel.isProcessOn().value == true || apiViewModel.isLoading.value == true) return@TopBarContent
                        openSheet(
                            scope = scope,
                            sheetState = sheetState,
                            showSheet = showSheet,
                            currentSheet = currentSheet,
                            sheet = BottomSheetType.SETTINGS
                        )
                    }
                )
            }
            HorizontalDivider(thickness = dimensionResource(R.dimen.dp_1), color = OutlineDefault)
            //Temp condition for showing uploaded count
            if (BaseUtils.isDebuggable() && (tagCount.value > 0 || uploadedTagCount.value > 0)){
                UploadStatusChip(uploadedTagCount.value,tagCount.value)
            }
            Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_16)))
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                val currentValue = when {
                    deviceSessionId.isNullOrEmpty() && selectedLocationName.value.isNotEmpty() ->
                        chkNull(totalCountZone.value, 0)

                    deviceSessionId.isNullOrEmpty() && selectedLocationName.value.isNullOrEmpty() ->
                        chkNull(totalCount.value, 0)

                    else ->
                        chkNull(foundQty.value, 0)
                }

                val totalValue = if (deviceSessionId.isNotEmpty() && selectedLocationName.value.isNotEmpty()) chkNull(totalCountZone.value.toString(), "0") else ""

                CounterText(
                    current = currentValue,
                    total = totalValue,
                    isLimitShow = deviceSessionId.isNotEmpty() && id.isNotEmpty() //chkNull(foundQty.value, 0) > 0
                )


                Text(
                    text = if (selectedLocationName.value.isNullOrEmpty())label else stringResource(R.string.in_zone_items),
                    style = CommonTypography.current.noteText.copy(color = BlackColor)
                )

                Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_12)))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!hasActiveSession && selectedLocationName.value.isNullOrEmpty()) {
                        Card(
                            modifier = Modifier
                                .width(dimensionResource(R.dimen.dp_160))
                                .height(dimensionResource(R.dimen.dp_44))
                                .clickable(
                                    onClick = { if (locationList.isNullOrEmpty()) {
                                        apiViewModel.callApi(
                                            url = UrlConstants.LOCATION_SUB_ZONES,
                                            appendData = DataStoreManager.readFromPreferences(
                                                LoginConstants.DEVICE_LOCATION_ID,
                                                ""
                                            )
                                        )
                                    }
                                        scope.launch {
                                            showLocationPicker.value = true
                                        }

                                        //showLocationPicker.value = true
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

                                androidx.compose.material.Text(
                                    text = stringResource(R.string.select_zone),
                                    style = CommonTypography.current.noteText,
                                    color = BlackColor
                                )
                            }
                        }
                    }
                    else {

                        GernericBasicTextField(
                            sourceZone = selectedLocationName.value,
                            destZone = "",
                            menuCode = menuCode,
                            value = selectedLocationName.value,
                            isOnDataUploaded = isOnDataUploaded.value,
                            labelRowAction = {
                                if(deviceSessionId.isNotEmpty()) return@GernericBasicTextField
                                scope.launch {
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
                                scope.launch {
                                    selectedLocationName.value = ""
                                    selectedLocationId.value = null
                                    showLocationPicker.value = false
                                }
                            },
                            isDestZone = false,
                            isSet = !(deviceSessionId.isNullOrEmpty() || id.isNullOrEmpty())
                        )
                        /**BasicTextField(
                            value = selectedLocationName.value,
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
                                .width(dimensionResource(R.dimen.dp_160))
                                .border(
                                    1.dp,
                                    Yellow,
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
                                        tint = Yellow
                                    )

                                    Spacer(Modifier.width(6.dp))

                                    // Text
                                    innerTextField()

                                    Spacer(Modifier.width(6.dp))

                                    val preHeader = TopicConstants.INVENTORY + "_" + menuCode + "_"

                                    val deviceSessionId = DataStoreManager.readFromPreferences(
                                        preHeader + ParameterConstants.DEVICE_SESSION_ID, ""
                                    )

                                    val id = DataStoreManager.readFromPreferences(
                                        preHeader + ParameterConstants.ID, ""
                                    )

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
                                                    scope.launch {
                                                        selectedLocationName.value = ""
                                                        selectedLocationPath.value = ""
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
                    Spacer(modifier = Modifier.size(10.dp))
                    if (selFilterKeys.isNotEmpty() && dynamicFilters.isNotEmpty()) {
                        if (selectedDynamicFilterName.value.isNullOrEmpty()) {
                            Card(
                                modifier = Modifier
                                    .width(dimensionResource(R.dimen.dp_160))
                                    .height(dimensionResource(R.dimen.dp_44))
                                    .clickable(
                                        onClick = {
                                            dynamicFilterPicker.value = true
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

                                    androidx.compose.material.Text(
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
                                    scope.launch {
                                        dynamicFilterPicker.value = true
                                    }
                                },
                                destAction = {

                                },
                                clearAction = {
                                    scope.launch {
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

                Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_8)))

                CommonTextField(
                    config = TextFieldConfig(
                        value = searchText.value,
                        onValueChange = { input ->
                            searchText.value = input
                        },
                        label = "Search By EAN",//if(listData.value.isNullOrEmpty()) "" else lbl.value,
                        imeAction = ImeAction.Search,
                        isSearch = true
                    ),
                    modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.dp_16))
                )

                Spacer(modifier = Modifier.size(16.dp))
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 600.dp)
                    ) {

                        items(items = filteredList) { product ->

                            val values = remember(product.displayData) {
                                parseDisplayValues(product.displayData)
                            }

                            val displayMap = remember(product.displayData) {
                                parseDisplayData(product.displayData)
                            }


                            val imagesValue = displayMap[DataStoreManager.getImageLabel()]
                            LogUtils.showLog("imagesValue", "StockCorrectionContentView: ${imagesValue.toString()} \n ${displayMap.toString()}")


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

                            val imagePainters = if (imageUrls.isEmpty()) { listOf(rememberAsyncImagePainter(R.drawable.image)) }
                            else { imageUrls.map { rememberAsyncImagePainter(it) } }

                            val title = values.drop(1).take(2).filter { it.isNotBlank() }.joinToString(",")
                            val sku = values.drop(3).take(3).filter { it.isNotBlank() }.joinToString(".")

                            ProductItemCard(
                                barcode = product.barcode,
                                title = title,
                                sku = sku,
                                location = product.name,
                                missingCount = if (product.foundQty <= 0) product.totalQty else product.foundQty,
                                modifier = Modifier.fillMaxWidth(),
                                status = if (product.foundQty <= 0) "Missing" else "Found",
                                tintColor = if (product.foundQty <= 0) RedColor else Green,
                                backGroundColor = if (product.foundQty <= 0) ErrorBgColor else CardGreen,
                                textColor = if (product.foundQty <= 0) RedColor else Green,
                                isData = true,
                                imagePainters = imagePainters,
                                showImagePreview = showImagePreview,
                                selectedPainter = selectedPainter,
                                imageUrls = imageUrls
                            )

                            //DataStoreManager.saveToPreferences("foundCount", product.foundQty)

                            HorizontalDivider(
                                thickness = 1.dp,
                                color = OutlineDefault,
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 16.dp)
                            )
                        }
                    }
                }
            }
        }

        if (showSheet.value) {
            ModalBottomSheet(
                sheetState = sheetState,
                onDismissRequest = {

                },
                dragHandle = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent)
            ) {
                when (currentSheet.value) {
                    BottomSheetType.POWER -> Box(
                        modifier = Modifier
                            .background(WhiteColor)
                            .fillMaxWidth()
                            .fillMaxHeight(0.45f)
                    ) {
                        SetInvDevicePower(
                            showSheet = showSheet,
                            sheetState = sheetState,
                            scope = scope,
                            currentPower = setPower.value,
                            onPowerSet = {
                                setPower.value = it
                                readerViewModel.setPower(setPower.value)
                                val preHeader = TopicConstants.INVENTORY + "_" + menuCode + "_"
                                DataStoreManager.saveToPreferences(preHeader + ParameterConstants.READER_POWER,setPower.value)
                            }
                        )
                    }

                    BottomSheetType.SETTINGS -> Box(
                        modifier = Modifier
                            .background(WhiteColor)
                            .fillMaxWidth()
                            .wrapContentHeight()
                    ) {
                        DeviceSettings(
                            showSheet = showSheet,
                            sheetState = sheetState,
                            scope = scope,
                            currentSheet = currentSheet,
                            isFilterEnable = true
                        )
                    }

                    BottomSheetType.FILTER -> Box(
                        modifier = Modifier
                            .background(WhiteColor)
                            .fillMaxWidth()
                            .wrapContentHeight()
                    )
                    {
                        FilterPicker(
                            filters = DataStoreManager.getFilterList(),
                            selFilterKeys = selFilterKeys,
                            onDismiss = {
                                scope.launch {
                                    sheetState.hide()
                                    showSheet.value = false
                                    currentSheet.value = BottomSheetType.NONE
                                }
                            },
                            onApply = { selectedMap  ->
                                LogUtils.showLog("D_selectedMAp",selectedMap.toString())
                                val selectedValues = selectedMap
                                    .filter { it.isNotBlank() }.distinct()
                                LogUtils.showLog("D_selectedMAp1",selectedValues.toString())

                                selFilterKeys.clear()
                                selFilterKeys.addAll(selectedValues)

                                val userId = DataStoreManager.readFromPreferences(ParameterConstants.USER_ID,"")
                                DataStoreManager.saveListStr(userId+"_"+menuCode+"_filters",selectedValues)

                                /*selectedDynamicFilterName.value =
                                    selectedValues.joinToString(", ")*/

                                scope.launch {
                                    sheetState.hide()
                                    showSheet.value = false
                                    currentSheet.value = BottomSheetType.NONE
                                }
                            }
                        )
                    }

                    BottomSheetType.SESSION -> Box(
                        modifier = Modifier
                            .background(WhiteColor)
                            .fillMaxWidth()
                            .fillMaxHeight(0.38f)
                    ) {
                        StockCorrectionSessionContent(
                            showSheet = showSheet,
                            sheetState = sheetState,
                            scope = scope,
                            navController = navController,
                            tagCount = tagCount.value,
                            onStopSession = {
                                scope.launch {
                                    if (chkTrue(readerViewModel.isProcessOn().value)) return@launch
                                    val preHeader = TopicConstants.INVENTORY + "_" + menuCode + "_"
                                    val id = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.ID, "")
                                    val jsonRequest = JSONObject().apply {
                                        put(ParameterConstants.ID, id)
                                        put(ParameterConstants.VALID_SCAN_TAG_COUNT, validCount.value)
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
                            readerViewModel
                        )
                    }

                    BottomSheetType.UPLOAD ->
                        Box(
                            modifier = Modifier
                                .background(WhiteColor)
                                .fillMaxWidth()
                                .fillMaxHeight(0.44f)
                        ) {
                            val totalValue = if (deviceSessionId.isNotEmpty() && selectedLocationName.value.isNotEmpty()) chkNull(totalCountZone.value.toString(), "0") else ""

                            val scannedText = if (foundQty.value <= 0) {
                                tagCount.value.toString()
                            } else {
                                "${foundQty.value}/${totalValue}"
                            }

                            val formatedText = buildAnnotatedString {
                                append(stringResource(R.string.scanned_stock_))
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append(scannedText)
                                }
                            }
                            UploadDataSheet(
                                title = label,//stringResource(R.string.upload_stock),
                                //heading = stringResource(R.string.confirm_stock_upload),
                                subHeading = formatedText,
                                //primaryButtonText = stringResource(R.string.confirm_and_upload),
                                showSheet = showSheet,
                                sheetState = sheetState,
                                scope = scope,
                                tagCount = foundQty.value,
                                isDataUploaded = isDataUploaded
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
fun parseDisplayValues(displayData: String): List<String> {
    val regex = Regex("""\(([^,]+),\s*(.*?)\)""")
    return regex.findAll(displayData)
        .map { it.groupValues[2].trim() }.filter { !it.startsWith("[", true) }
        .toList()
}

fun parseDisplayData(displayData: String): Map<String, String> {
    val regex = Regex("""\(([^,]+),\s*(.*?)\)""")

    return regex.findAll(displayData)
        .associate {
            val key = it.groupValues[1].trim()
            val value = it.groupValues[2].trim()
            key to value
        }
}

data class ProductItem(
    val imageRes: Int,
    val barcode: String,
    val title: String,
    val sku: String,
    val location: String,
    val missingCount: Int
)


@Composable
fun ProductItemCard(
    barcode: String,
    title: String,
    sku: String,
    location: String,
    missingCount: Int,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    status: String,
    tintColor: Color,
    backGroundColor: Color,
    isData: Boolean = false,
    textColor: Color = BlackColor,
    imagePainters: List<AsyncImagePainter>,
    showImagePreview: MutableState<Boolean>,
    selectedPainter: MutableState<Painter?>,
    imageUrls: List<String>
) {
    val trimmedText = if (location.length > 25) {
        location.take(25)
    } else {
        location
    }
    val listState = rememberLazyListState()
    val currentIndex = remember { derivedStateOf { listState.firstVisibleItemIndex } }

    Row(
        modifier = modifier
            .padding(horizontal = dimensionResource(R.dimen.dp_16))
            .fillMaxHeight()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        verticalAlignment = Alignment.CenterVertically
    ){

        Column(modifier = Modifier,
            verticalArrangement = Arrangement.Center) {
            // Image Box
            Box(
                modifier = Modifier
                    .width(dimensionResource(R.dimen.dp_90))
                    .height(dimensionResource(R.dimen.dp_85)),
                contentAlignment = Alignment.Center
            ) {
                if (imagePainters.size == 1) {

                    Image(
                        painter = imagePainters.first(),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .matchParentSize()
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
                        contentScale = ContentScale.Crop
                    )

                } else {

                    LazyRow(
                        state = listState, modifier = Modifier.matchParentSize()
                    ) {

                        itemsIndexed(imagePainters) { index, painter ->

                            Image(
                                painter = painter,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .aspectRatio(1f) // Optional: forces images to be square
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
                                contentScale = ContentScale.Crop
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

        Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_8)))

        Column(
            modifier = Modifier
                .weight(1f),
            verticalArrangement = Arrangement.Center
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = barcode,
                    style = CommonTypography.current.textSubtext,
                    fontWeight = FontWeight.SemiBold,
                    color = BlackColor,
                    modifier = Modifier
                        .weight(1f, fill = true)
                        .basicMarquee()
                )
                Spacer(modifier = Modifier.width(dimensionResource(R.dimen.dp_4)))

                IconText(
                    icon = painterResource(R.drawable.property_red_dot),
                    actionText = "$status:$missingCount",
                    onActionClick = {},
                    iconSize = dimensionResource(R.dimen.dp_12),
                    textStyle = CommonTypography.current.smallTxt,
                    verticalPadding = dimensionResource(R.dimen.dp_8),
                    elevation = 0.dp,
                    tintColor = tintColor,
                    backGroundColor = backGroundColor,
                    textColor = textColor,
                    modifier = Modifier.wrapContentWidth()
                )
            }


            if (isData){
                // Title with ellipsis
                if (title.isNotBlank()) {
                    Text(
                        text = title,
                        style = CommonTypography.current.noteText,
                        fontWeight = FontWeight.SemiBold,
                        color = TextGrey,
                        modifier = Modifier.basicMarquee()
                    )
                }

                // SKU
                if (sku.isNotBlank()) {
                    Text(
                        text = sku,
                        style = CommonTypography.current.smallTxt,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.basicMarquee()
                    )
                }
            }

            // Bottom Row
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {

                Image(
                    painter = painterResource(R.drawable.icon_location),
                    contentDescription = null,
                    modifier = Modifier.size(dimensionResource(R.dimen.dp_16))
                )

                Spacer(modifier = Modifier.width(dimensionResource(R.dimen.dp_4)))

                Text(
                    text = trimmedText,
                    style = CommonTypography.current.smallTxt,
                    fontWeight = FontWeight.Medium,
                    color = BlackColor,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                    modifier = Modifier
                        .weight(1f)
                        .basicMarquee()
                )
            }

            Spacer(modifier = Modifier.size(12.dp))
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockCorrectionBottomBarView(
    readerViewModel: ReaderViewModel,
    showLocationPicker: Boolean,
    showSheet: MutableState<Boolean>,
    sheetState: SheetState,
    currentSheet: MutableState<BottomSheetType>,
    scope: CoroutineScope,
    foundQty: State<Int>,
    isOnDataUploaded: MutableState<Boolean>,
    isInvOn: State<Boolean?>,
    isShowUploadSwipe: MutableState<Boolean>
) {

    Box() {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(WhiteColor)
        ) {
            HorizontalDivider(color = OutlineDefault)
            if (!showLocationPicker && !isOnDataUploaded.value) {
                if (chkTrue(isInvOn.value) || chkNull(foundQty.value,0) <= 0) {
                    Row(modifier = Modifier.padding(dimensionResource(R.dimen.dp_16))) {
                        CommonButton(
                            text = stringResource(id = if (isInvOn.value == true) R.string.stop_scanning else R.string.start_scanning_inventory),
                            onClick = {
                               readerViewModel.setTriggerValue(true)
                            },
                            icon = if (isInvOn.value == true) painterResource(R.drawable.stopicon) else null,
                            modifier = Modifier
                                .fillMaxWidth(),
//                    enabled = false,//selectedLocation.value.isNotEmpty(),
                            contentColor = if (isInvOn.value == true) BlackColor else WhiteColor,
                            gradientBrush = if (isInvOn.value == true) SolidColor(WhiteColor) else Brush.horizontalGradient(
                                colors = listOf(
                                    BlackColor,
                                    ButtonGray
                                )
                            )
                        )
                    }
                }
                else if (!chkTrue(isInvOn.value) && isShowUploadSwipe.value && isOnDataUploaded.value == false){
                    SwipeToUploadButton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        text = stringResource(R.string.swipe_to_upload)
                    ) {
                        openSheet(
                            scope = scope,
                            sheetState = sheetState,
                            showSheet = showSheet,
                            currentSheet = currentSheet,
                            sheet = BottomSheetType.UPLOAD
                        )
                        if (isShowUploadSwipe.value) {
                            CoroutineScope(Dispatchers.IO).launch {
                                delay(100)
                                isShowUploadSwipe.value = false
                            }
                        }
                    }
                }
                else{
                    TwoButtonView(
                        primaryText= stringResource(id = if (isInvOn.value == true) R.string.stop_scanning else R.string.continue_scanning),
                        seconderText = stringResource(R.string.upload_stock),
                        primaryAction = {
                            readerViewModel.setTriggerValue(true)
                        },
                        seconderAction = {
                            isShowUploadSwipe.value = true
                        },
                        isSecondaryButton = true,
                        modifier = Modifier
                            .padding(dimensionResource(R.dimen.dp_16))
                    )
                }
            }
            else{
                Row(modifier = Modifier.padding(dimensionResource(R.dimen.dp_16))) {
                    ItekFooter()
                }
            }
        }
    }
}

@Composable
fun SwipeToUploadButton(
    modifier: Modifier = Modifier,
    text: String,
    onSwiped: () -> Unit
) {
    val thumbSize = 50.dp
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    val containerWidth = remember { mutableStateOf(0f) }
    val offsetX = remember { Animatable(0f) }

    val maxOffset = remember(containerWidth.value) {
        containerWidth.value - with(density) { thumbSize.toPx() }
    }

    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(42))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFFF3B100),
                        Color(0xFFFECF53)
                    )
                )
            )
            .onSizeChanged {
                containerWidth.value = it.width.toFloat()
            }
    ) {
        // Center Text
        Text(
            text = text,
            modifier = Modifier.align(Alignment.Center),
            style = CommonTypography.current.textSemiBold.copy(color = WhiteColor)
        )

        // Draggable Thumb
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .size(thumbSize)
                .padding(3.dp)
                .border(
                    1.dp,
                    Yellow,
                    RoundedCornerShape(14.dp)
                )
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White)
                .align(Alignment.CenterStart)
                .pointerInput(containerWidth) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val newOffset = (offsetX.value + dragAmount.x)
                                .coerceIn(0f, maxOffset)
                            scope.launch {
                                offsetX.snapTo(newOffset)
                            }
                        },
                        onDragEnd = {
                            val threshold = maxOffset * 0.65f
                            if (offsetX.value > threshold) {
                                // Complete swipe
                                scope.launch {
                                    offsetX.animateTo(maxOffset)
                                    onSwiped()

                                    // reset
                                    delay(250)
                                    offsetX.snapTo(0f)
                                }
                            } else {
                                // Reset
                                scope.launch {
                                    offsetX.animateTo(
                                        0f,
                                        animationSpec = spring(
                                            stiffness = Spring.StiffnessMedium
                                        )
                                    )
                                }
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = Color(0xFFFFC107),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}


@Composable
fun SuccessComponent(tagCount: Int, selectedLocationName: MutableState<String>) {

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {

            Spacer(modifier = Modifier.size(100.dp))
            BasicTextField(
                value = selectedLocationName.value,
                onValueChange = {},
                readOnly = true,
                singleLine = true,
                textStyle = CommonTypography.current.noteText.copy(
                    color = Green,
                    textAlign = TextAlign.Start
                ),
                modifier = Modifier
                    .height(dimensionResource(R.dimen.dp_44))
                    .wrapContentWidth()   // 🔥 Important
                    .border(1.dp, Green, RoundedCornerShape(16.dp))
                    .background(WhiteColor, RoundedCornerShape(16.dp))
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
                            tint = Green
                        )

                        Spacer(Modifier.width(6.dp))

                        // Text
                        innerTextField()

                        Spacer(Modifier.width(6.dp))

                            Icon(
                                painter = painterResource(id = R.drawable.property_check_selected),
                                contentDescription = null,
                                tint = Green
                            )

                    }
                }
            )
            InventoryUploadedSuccess(count = tagCount)
        }
    }
}
