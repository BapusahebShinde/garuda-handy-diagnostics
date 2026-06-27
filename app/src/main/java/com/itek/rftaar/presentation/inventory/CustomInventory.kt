package com.itek.rftaar.presentation.inventory

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
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
import com.itek.rftaar.core.common.utils.ParseUtils.extractJSONArray
import com.itek.rftaar.core.common.utils.ParseUtils.extractString
import com.itek.rftaar.core.database.AppDatabase
import com.itek.rftaar.core.database.DataStoreManager
import com.itek.rftaar.data.entity.SessionListEntity
import com.itek.rftaar.domain.model.LocationModel
import com.itek.rftaar.mqtt.constants.InventoryConstants
import com.itek.rftaar.mqtt.constants.TopicConstants
import com.itek.rftaar.presentation.commonComp.AppSnackBar
import com.itek.rftaar.presentation.commonComp.BottomSheetType
import com.itek.rftaar.presentation.commonComp.CommonButton
import com.itek.rftaar.presentation.commonComp.ErrorAppSnackBarData
import com.itek.rftaar.presentation.commonComp.FilterPicker
import com.itek.rftaar.presentation.commonComp.GernericBasicTextField
import com.itek.rftaar.presentation.commonComp.IconText
import com.itek.rftaar.presentation.commonComp.InventoryPulseCircle
import com.itek.rftaar.presentation.commonComp.MultiFilterPicker
import com.itek.rftaar.presentation.commonComp.RowText
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
import com.itek.rftaar.ui.theme.OutlineDefault
import com.itek.rftaar.ui.theme.RedColor
import com.itek.rftaar.ui.theme.ShadowColor
import com.itek.rftaar.ui.theme.TabColor
import com.itek.rftaar.ui.theme.TextSubtext
import com.itek.rftaar.ui.theme.WhiteColor
import com.itek.rftaar.utils.CommonUtils
import com.itek.rftaar.utils.CommonUtils.chkNull
import com.itek.rftaar.utils.CommonUtils.chkTrue
import com.itek.rftaar.utils.NetworkUtils
import com.itek.rftaar.utils.ZoneUtils.processChildren
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomInventory(
    modifier: Modifier,
    navController: NavHostController,
    label: String,
    menuCode: String,
    apiViewModel: ApiViewModel = hiltViewModel(),
    searchParams: Map<String, Any> = emptyMap()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { sheetValue ->
            sheetValue != SheetValue.Hidden
        }
    )
    val showSheet = remember { mutableStateOf(false) }
    val currentSheet = remember { mutableStateOf(BottomSheetType.NONE) }

    val preHeader = TopicConstants.INVENTORY + "_" + menuCode + "_"
    val id = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.ID, "")
    val deviceSessionId = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.DEVICE_SESSION_ID, "")
    val transactionType = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.TRANSACTION_TYPE, if (menuCode.contains("add", true)) InventoryConstants.INVENTORY_ADD_BRAND else InventoryConstants.INVENTORY_BRAND)
    val assetLocationName = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.ASSET_LOCATION_NAME, "")
    val assetLocationPath = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.ASSET_LOCATION_PATH, "")
    val customParamLabel = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.CUSTOM_INVENTORY_PARAM_LABEL, "")
    val customParamName = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.CUSTOM_INVENTORY_PARAM_NAME, "")
    val customParamValue = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.CUSTOM_INVENTORY_PARAM_VALUE, "")

    val locationList = remember { mutableStateListOf<LocationModel>() }
    val selectedLocationId = remember { mutableStateOf<LocationModel?>(null) }
    val selectedLocation = remember { mutableStateOf(assetLocationName) }
    val customValuesList = remember { mutableStateListOf<String>() }
    val customLabel = remember { mutableStateOf(customParamLabel) }
    val selectedCustomValue = remember { mutableStateOf(customParamValue) }
    val lastTagCount = remember { mutableStateOf(0) }
    val isDataUploaded = remember { mutableStateOf(false) }
    val isOnDataUploaded = remember { mutableStateOf(false) }
    val showSelectionLocation = remember { mutableStateOf(true) }
    val showSelectionBrand = remember { mutableStateOf(true) }
    val filterStep = remember { mutableStateOf(FilterStep.NONE) }
    val isNewSession = remember { mutableStateOf(deviceSessionId.isNullOrEmpty()) }
    val optionPicker = remember { mutableStateOf(false) }
    //val isFilterApplied = remember { mutableStateOf(false) }

    val selFilterKeys = remember { mutableStateListOf<String>()}
    val dynamicFilterPicker = remember { mutableStateOf(false) }
    val dynamicFilters = remember { mutableStateMapOf<String, List<Any>>() }
    val selectedFilters = remember { mutableStateMapOf<String, List<Any>>() }
    val selectedDynamicFilterName = remember { mutableStateOf("") }

    val hasActiveSession = !deviceSessionId.isNullOrEmpty() && !id.isNullOrEmpty()
    val activity = context as? ReaderActivity
    val readerViewModel = if (activity != null) activity.findReaderViewModel() else hiltViewModel()
    if (activity != null) {
        if (deviceSessionId.isNullOrEmpty() || id.isNullOrEmpty()) readerViewModel.onCreate()
        readerViewModel.setSessionAndTransactionType(menuCode, transactionType,topic=TopicConstants.INVENTORY)
        /*if (deviceSessionId.isNullOrEmpty() || id.isNullOrEmpty()) {
            apiViewModel.callApi(UrlConstants.LOCATION_SUB_ZONES, appendData = DataStoreManager.readFromPreferences(
                    LoginConstants.DEVICE_LOCATION_ID,
                    ""
                ))

            val map = HashMap<String, String>()
            map.put(
                ParameterConstants.CUSTOMER_ID,
                DataStoreManager.readFromPreferences(ParameterConstants.CUSTOMER_ID, "")
            )
            map.put(
                ParameterConstants.BUSINESS_LINE_ID1,
                DataStoreManager.readFromPreferences(ParameterConstants.BUSINESS_LINE_ID, "")
            )
            apiViewModel.callApi(UrlConstants.CUSTOM_INVENTORY_PARAMS, queryMap = map)
        } */
       /* else {
            if (assetLocationPath.isNotEmpty() && assetLocationName.isNotEmpty()) {
                //TODO set value & disable location field
            } else if (customParamName.isNotEmpty() && customParamLabel.isNotEmpty() && customParamValue.isNotEmpty()) {
                //TODO set value & disable custom selection field (e.g. brand)
            }
        }*/
    }
    val isProcessOn = readerViewModel.isProcessOn().observeAsState()
    val isApiLoading = apiViewModel.isLoading.observeAsState()
    val db = AppDatabase.getDbInstance(context)
    val tagCount = db.tagInfoDao().getTotalCount(menuCode, transactionType).observeAsState(initial = 0)
    val uploadedTagCount = db.tagInfoDao().getUploadedCount(menuCode, transactionType).observeAsState(initial = 0)
    val isInvOn = readerViewModel.isInventoryOn().observeAsState()
    LogUtils.showLog("isInvOn", "CustomInventory: ${isInvOn.value}")
    val isLoader = remember { mutableStateOf(false) }


    LaunchedEffect(Unit) {
        if (deviceSessionId.isNullOrEmpty() || id.isNullOrEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                apiViewModel.callApi(
                    UrlConstants.LOCATION_SUB_ZONES,
                    appendData = DataStoreManager.readFromPreferences(
                        LoginConstants.DEVICE_LOCATION_ID,
                        ""
                    )
                )
            }

            delay(130)

            CoroutineScope(Dispatchers.IO).launch {
                val map = HashMap<String, String>()
                map.put(ParameterConstants.CUSTOMER_ID, DataStoreManager.readFromPreferences(ParameterConstants.CUSTOMER_ID, ""))
                map.put(ParameterConstants.BUSINESS_LINE_ID1, DataStoreManager.readFromPreferences(ParameterConstants.BUSINESS_LINE_ID, ""))
                apiViewModel.callApi(UrlConstants.CUSTOM_INVENTORY_PARAMS, queryMap = map)
            }
        }
        else {
            if (assetLocationPath.isNotEmpty() && assetLocationName.isNotEmpty()) {
                //TODO set value & disable location field
            } else if (customParamName.isNotEmpty() && customParamLabel.isNotEmpty() && customParamValue.isNotEmpty()) {
                //TODO set value & disable custom selection field (e.g. brand)
            }
        }
    }

    BackHandler(enabled = true) {
        if (chkTrue(isProcessOn.value) || chkTrue(isApiLoading.value)) return@BackHandler
        scope.launch {
            val preHeader = TopicConstants.INVENTORY + "_" + menuCode + "_"
            val deviceSessionId = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.DEVICE_SESSION_ID, "")
            val id = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.ID, "")
            if (deviceSessionId.isNullOrEmpty() || id.isNullOrEmpty()) {
                //readerViewModel.onDestroy()
                if(navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                DataStoreManager.saveToPreferences(preHeader + ParameterConstants.READER_POWER, 30)
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
                    CustomInventoryBottomBar(
                        apiViewModel,
                        readerViewModel,
                        selectedLocation,
                        selectedCustomValue,
                        optionPicker,
                        selFilterKeys,
                        dynamicFilterPicker,
                        dynamicFilters,
                        isOnDataUploaded,
                        tagCount,
                        uploadedTagCount,
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
                CustomInventoryContent(
                    label,
                    menuCode,
                    apiViewModel,
                    readerViewModel,
                    navController,
                    context,
                    scope,
                    transactionType,
                    locationList,
                    selectedLocationId,
                    selectedLocation,
                    customValuesList,
                    customLabel,
                    selectedCustomValue,
                    showSheet,
                    sheetState,
                    currentSheet,
                    lastTagCount,
                    isDataUploaded,
                    isOnDataUploaded,
                    filterStep,
                    isNewSession,
                    hasActiveSession,
                    optionPicker,
                    selFilterKeys,
                    dynamicFilterPicker,
                    dynamicFilters,
                    selectedDynamicFilterName,
                    selectedFilters,
                    isInvOn,
                    tagCount,
                    uploadedTagCount,
                    isLoader,
                )
            }
        }
        if (optionPicker.value) {
            OptionPicker(
                selectedLocationId,
                selectedCustomValue,
                customLabel,
                locationList,
                customValuesList,
                onDismiss = { optionPicker.value = false },
                onFilterApplied = { location, customField ->
                    if(location!=null && customField!=null && customField.isNotEmpty()) {
                        val isEmptyList = DataHolder.zoneWiseStockQtyList.isNullOrEmpty()
                        val zoneStockQty = if(isEmptyList) null else DataHolder.zoneWiseStockQtyList.filter { zs-> zs.path.equals(location.path) && zs.name.equals(location.name)}.firstOrNull()
                        LogUtils.showLog("zoneStockQty",""+if(zoneStockQty!=null) zoneStockQty.stockCount else "null")
                        selectedLocationId.value = location
                        selectedLocation.value = if (location == null) "" else chkNull(location?.name, "")
                        selectedCustomValue.value = chkNull(customField, "")
                        if(zoneStockQty!=null && zoneStockQty.stockCount>0 && zoneStockQty.customFieldWiseCount.isNotEmpty()) lastTagCount.value=chkNull(zoneStockQty.customFieldWiseCount.get(selectedCustomValue.value),0)
                        else if(zoneStockQty!=null && zoneStockQty.stockCount>0) lastTagCount.value=zoneStockQty.stockCount
                        callAdvFiltersAPI(apiViewModel,DataStoreManager.readFromPreferences(preHeader + ParameterConstants.CUSTOM_INVENTORY_PARAM_NAME, ""),selectedCustomValue.value)
                        clearSavedSessionValues(
                            context,
                            menuCode,
                            transactionType,
                            selectedLocation.value,
                            selectedCustomValue.value
                        )
                    }
                }
            )
        }
        if (dynamicFilterPicker.value) {
            LogUtils.showLog("D_Filters",dynamicFilters.values.toString())
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
        /**if (filterStep.value == FilterStep.LOCATION) {
            LocationPicker(
                locationList = locationList,
                onDismiss = {
                    filterStep.value = FilterStep.NONE
                },
                onLocationSelected = { location ->
                    selectedLocationId.value = location
                    selectedLocation.value = location.name
                    filterStep.value = FilterStep.NONE
                    showSelectionLocation.value = false
                    clearSavedSessionValues(
                        context,
                        menuCode,
                        transactionType,
                        selectedLocation.value,
                        selectedCustomValue.value
                    )
                }
            )
        }
        if (filterStep.value == FilterStep.BRAND) {
            BrandPicker(
                menuCode = menuCode,
                brandList = customValuesList,
                onDismiss = {
                    filterStep.value = FilterStep.NONE
                },
                onBrandSelected = { brand ->
                    selectedCustomValue.value = brand
                    filterStep.value = FilterStep.NONE
                    showSelectionBrand.value = false
                    clearSavedSessionValues(
                        context,
                        menuCode,
                        transactionType,
                        selectedLocation.value,
                        selectedCustomValue.value
                    )
                }
            )
        }*/

    }
}

fun callAdvFiltersAPI(apiViewModel: ApiViewModel,customFieldName:String,customFieldValue: String){
    LogUtils.showLog("callAdvFiltersAPI",customFieldName+"_"+customFieldValue)
    val map = HashMap<String, String>()
    map.put(ParameterConstants.CUSTOMER_ID, DataStoreManager.readFromPreferences(ParameterConstants.CUSTOMER_ID, ""))
    map.put(ParameterConstants.BUSINESS_LINE_ID1, DataStoreManager.readFromPreferences(ParameterConstants.BUSINESS_LINE_ID, ""))
    map.put(ParameterConstants.CUSTOM_INVENTORY_PARAM_NAME, customFieldName)
    map.put(ParameterConstants.CUSTOM_INVENTORY_PARAM_VALUE,customFieldValue)
    apiViewModel.callApi(UrlConstants.CUSTOM_INVENTORY_ADVANCED_FILTERS, queryMap = map)
}

enum class FilterStep {
    LOCATION,
    BRAND,
    NONE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomInventoryContent(
    label: String,
    menuCode: String,
    apiViewModel: ApiViewModel,
    readerViewModel: ReaderViewModel,
    navController: NavHostController,
    context: Context,
    scope: CoroutineScope,
    transactionType: String,
    locationList: SnapshotStateList<LocationModel>,
    selectedLocationId: MutableState<LocationModel?>,
    selectedLocationName: MutableState<String>,
    customValuesList: SnapshotStateList<String>,
    customLabel: MutableState<String>,
    selectedCustomValue: MutableState<String>,
    showSheet: MutableState<Boolean>,
    sheetState: SheetState,
    currentSheet: MutableState<BottomSheetType>,
    lastTagCount: MutableState<Int>,
    isDataUploaded: MutableState<Boolean>,
    isOnDataUploaded: MutableState<Boolean>,
    filterStep: MutableState<FilterStep>,
    isNewSession: MutableState<Boolean>,
    hasActiveSession: Boolean,
    optionPicker: MutableState<Boolean>,
    selFilterKeys: SnapshotStateList<String>,
    dynamicFilterPicker: MutableState<Boolean>,
    dynamicFilters: SnapshotStateMap<String, List<Any>>,
    selectedDynamicFilterName: MutableState<String>,
    selectedFilters: SnapshotStateMap<String, List<Any>>,
    isInvOn: State<Boolean?>,
    tagCount: State<Int>,
    uploadedTagCount: State<Int>,
    isLoader: MutableState<Boolean>
) {
    val strokeWidth = dimensionResource(R.dimen.dp_2)
    //val powerSheet = remember { mutableStateOf(false) }
    val triggerPressed = readerViewModel.isTriggerPressed().observeAsState(false)
    val db = AppDatabase.getDbInstance(context)
    val inventoryBarcodeList = db.sessionListDao().getValues(TopicConstants.INVENTORY, menuCode, transactionType).collectAsState(emptyList())
    val validCount = db.tagInfoDao().getValidCount(menuCode, transactionType).observeAsState(initial = 0) //collectAsState(initial=0)
    val invalidCount = db.tagInfoDao().getInvalidCount(menuCode, transactionType).observeAsState(initial = 0) //collectAsState(initial=0)
    val alienCount = db.tagInfoDao().getAlienCount(menuCode,transactionType).observeAsState(initial = 0) //collectAsState(initial=0)
    val snackbarController = remember { SnackbarController() }
    val error = readerViewModel.error().observeAsState()

    val preHeader = TopicConstants.INVENTORY + "_" + menuCode + "_"
    val id = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.ID, "")
    val deviceSessionId = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.DEVICE_SESSION_ID, "")
    val readerPower = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.READER_POWER, 30)
    LogUtils.showLog("readerPower",""+readerPower)
    val setPower = remember { mutableStateOf(readerPower) }
    LaunchedEffect(setPower.value) {
        readerViewModel.setPower(setPower.value)
    }
    val isProcessOn = readerViewModel.isProcessOn().observeAsState(false)
    val isApiLoading = apiViewModel.isLoading.observeAsState()
    val unEncodedCount = db.tagInfoDao().getUnencodedCount(menuCode,transactionType).observeAsState(0)

    val customFieldMessage = stringResource(R.string.err_select_location_and_)




    /**LaunchedEffect(Unit) {
        //!powerSheet.value
        !sessionSheet.value
    }*/

    /**LaunchedEffect(isInvOn.value) {
        LogUtils.showLog("isInvOn", "CustomInventoryContent:${isInvOn.value} ")
        if (isInvOn.value == true) {
            *//**snackbarController.show(
                AppSnackBarData(
                    icon = R.drawable.property_barcode_filled,
                    message = scaningMessage,
                    showCancel = false
                )
            )*//**
        }
    }*/

    LaunchedEffect(inventoryBarcodeList.value) {
        val preHeader = TopicConstants.INVENTORY + "_" + menuCode + "_"
        val deviceSessionId = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.DEVICE_SESSION_ID, "")
        val id = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.ID, "")
        if(inventoryBarcodeList.value.size>0 && deviceSessionId.isNotEmpty() && id.isNotEmpty() && isNewSession.value){
          isNewSession.value=false
          readerViewModel.setTriggerValue(true)
      }
    }

    LaunchedEffect(error.value) {
        val message = error.value
        if (!message.isNullOrBlank()) {
            LogUtils.showLog("isTagNotFound", "CustomInventoryContent: $message")
            snackbarController.show(
                ErrorAppSnackBarData(message)
            )
        }
    }

    LaunchedEffect(triggerPressed.value) {
        if (triggerPressed.value == true) {
            readerViewModel.setTriggerValue(false)
            val preHeader = TopicConstants.INVENTORY + "_" + menuCode + "_"
            val deviceSessionId = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.DEVICE_SESSION_ID, "")
            val id = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.ID, "")
            if (isLoader.value == true) return@LaunchedEffect
            LogUtils.showLog("TriggerPressed", "Trigger is pressed")
            if(chkTrue(isApiLoading.value)) return@LaunchedEffect
            if(navController.currentBackStackEntry?.lifecycle?.currentState != Lifecycle.State.RESUMED) return@LaunchedEffect
            if(isDataUploaded.value || isOnDataUploaded.value) return@LaunchedEffect
            if(showSheet.value!=false && currentSheet.value != BottomSheetType.NONE) return@LaunchedEffect
            val customFieldName = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.CUSTOM_INVENTORY_PARAM_NAME, "")
            val customFieldLabel = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.CUSTOM_INVENTORY_PARAM_LABEL, "")
            if (selectedLocationName.value.isNullOrEmpty() || selectedCustomValue.value.isNullOrEmpty()) {
                //showValidationError
                snackbarController.show(
                    ErrorAppSnackBarData(String.format(customFieldMessage,customFieldLabel))
                )
            }
            else if (inventoryBarcodeList.value.isNullOrEmpty() && (deviceSessionId.isNullOrEmpty() || id.isNullOrEmpty())) {
                val map = HashMap<String, String>()
                map[ParameterConstants.CUSTOMER_ID] = DataStoreManager.readFromPreferences(ParameterConstants.CUSTOMER_ID, "")
                map[ParameterConstants.BUSINESS_LINE_ID1] = DataStoreManager.readFromPreferences(ParameterConstants.BUSINESS_LINE_ID, "")
                map[ParameterConstants.CUSTOM_INVENTORY_PARAM_NAME] = customFieldName
                map[ParameterConstants.CUSTOM_INVENTORY_PARAM_VALUE] = selectedCustomValue.value
                if(selectedFilters.isNotEmpty()) {
                    val jFilters = JSONArray()
                    for ((key, values) in selectedFilters) {
                     if(key.isNotEmpty() && values.isNotEmpty()) {
                         val jFilter = JSONObject()
                         jFilter.put(ParameterConstants.NAME, key)
                         jFilter.put(ParameterConstants.VALUES, values)
                         jFilters.put(jFilter)
                     }
                    }
                    map[ParameterConstants.CUSTOM_INVENTORY_ADVANCE_FILTERS] = jFilters.toString()
                }
                apiViewModel.callApi(UrlConstants.CUSTOM_INVENTORY_EAN_LIST, queryMap = map)
            }
            else if (deviceSessionId.isNullOrEmpty() || id.isNullOrEmpty()) {
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
                    jsonRequest.put(ParameterConstants.TRANSACTION_TYPES, transactionType)
                    jsonRequest.put(ParameterConstants.ASSET_LOCATION_PATH, selectedLocationId.value!!.path)
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
            else if(inventoryBarcodeList.value.isNotEmpty() && deviceSessionId.isNotEmpty() && id.isNotEmpty()) {
                isNewSession.value=false
                readerViewModel.toggleInventory(setPower.value,eans = inventoryBarcodeList.value)
            }
        } else {
            LogUtils.showLog("TriggerPressed", "Trigger released")
        }
    }

    LaunchedEffect(isDataUploaded.value) {
        if (isDataUploaded.value==true) {
            try {
                delay(500)
                val preHeader = TopicConstants.INVENTORY + "_" + menuCode + "_"
                val id = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.ID, "")
                val jsonRequest = JSONObject().apply {
                    put(ParameterConstants.ID, id)
                    put(ParameterConstants.VALID_SCAN_TAG_COUNT,  validCount.value + alienCount.value)
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

    val response = apiViewModel.apiResult.collectAsState(initial = null)
    LaunchedEffect(response.value) {
        if(response.value==null) return@LaunchedEffect
        val result = response.value
        if (result?.isSuccess != true) {
            val message = result?.errMsg
            if (!message.isNullOrBlank()) {
                snackbarController.show(
                    ErrorAppSnackBarData(result?.errMsg.toString())
                )
            }
        } else if (result.response != null) {
            val jsonResponse = result.response
            val preHeader = TopicConstants.INVENTORY + "_" + menuCode + "_"
            val deviceSessionId = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.DEVICE_SESSION_ID, "")
            val id = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.ID, "")
            when (result.url) {
                UrlConstants.LOCATION_ZONES -> {
                    val responseArray = ParseUtils.extractJSONArray(jsonResponse, ParameterConstants.DATA, JSONArray())
                    val dataList = mutableListOf<LocationModel>()
                    if (responseArray != null && responseArray.length() > 0) {
                        processChildren(responseArray,dataList);
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

                UrlConstants.CUSTOM_INVENTORY_PARAMS -> {
                    val response = ParseUtils.extractJSONObject(jsonResponse, ParameterConstants.DATA, jsonResponse)
                    val custParamName = extractString(response, ParameterConstants.NAME, "")
                    val custParamLabel = extractString(response, ParameterConstants.LABEL_NAME, custParamName)
                    val preHeader = TopicConstants.INVENTORY + "_" + menuCode + "_"
                    DataStoreManager.saveToPreferences(preHeader + ParameterConstants.CUSTOM_INVENTORY_PARAM_LABEL, custParamLabel)
                    DataStoreManager.saveToPreferences(preHeader + ParameterConstants.CUSTOM_INVENTORY_PARAM_NAME, custParamName)
                    DataStoreManager.saveToPreferences(preHeader + ParameterConstants.CUSTOM_INVENTORY_PARAM_VALUE, "")

                    val valuesArray = ParseUtils.extractJSONArray(response, ParameterConstants.VALUES, JSONArray())
                    val dataList = mutableListOf<String>()
                    if (valuesArray != null && valuesArray.length() > 0) {
                        for (i in 0 until valuesArray.length()) {
                            val obj = valuesArray.getString(i)
                            if (CommonUtils.isNonEmpty(obj)) dataList.add(obj)
                        }
                    }
                    if (dataList.isNotEmpty()) {
                        customLabel.value=custParamLabel
                        customValuesList.clear()
                        customValuesList.addAll(dataList)
                    } else {
                        //TODO give custom error if custParamName or values are empty
                    }
                }

                UrlConstants.CUSTOM_INVENTORY_ADVANCED_FILTERS -> {
                    val response = ParseUtils.extractJSONObject(jsonResponse, ParameterConstants.DATA, jsonResponse)
                    val responseArray = extractJSONArray(response, ParameterConstants.DATA, JSONArray())
                    val filterListMap = HashMap<String, ArrayList<Any>>()
                    val filterKeys = ArrayList<String>()
                    if(responseArray!=null && responseArray.length()>0) {
                        for (i in 0 until responseArray.length()) {
                            val jObj = responseArray.getJSONObject(i)
                            val name = extractString(jObj, ParameterConstants.LABEL_NAME,"")
                            val values = extractJSONArray(jObj, ParameterConstants.VALUES, JSONArray())
                            LogUtils.showLog("Filtervalues->"+name, values.toString())
                            if(name.isNotEmpty() && values!=null && values.length()>0){
                                val listFilters = ArrayList<Any>()
                                for (i in 0 until values.length()){
                                  val filterValue =  values.getString(i)
                                  if(filterValue.isNotEmpty())  listFilters.add(filterValue)
                                }
                                if(listFilters.isNotEmpty()) {
                                    filterKeys.add(name)
                                    filterListMap.put(name,listFilters)
                                }
                            }
                        }
                        LogUtils.showLog("FilterList", filterListMap.toString())
                        if(filterListMap.isNotEmpty()){
                            dynamicFilters.clear()
                            dynamicFilters.putAll(filterListMap)
                            selFilterKeys.clear()
                            selFilterKeys.addAll(filterKeys)
                            val userId=DataStoreManager.readFromPreferences(ParameterConstants.USER_ID,"")
                            DataStoreManager.saveListStr(userId+"_"+menuCode+"_filters",filterKeys)
                            LogUtils.showLog("D_FilterList", dynamicFilters.toString())
                        }
                    }

                }

                UrlConstants.CUSTOM_INVENTORY_EAN_LIST -> {
                    val responseArray = ParseUtils.extractJSONArray(jsonResponse, ParameterConstants.DATA, JSONArray())
                    //val dataList = mutableListOf<String>()
                    val dataList = mutableListOf<SessionListEntity>()
                    if (responseArray != null && responseArray.length() > 0) {
                        for (i in 0 until responseArray.length()) {
                            val obj = responseArray.getString(i)
                            if (CommonUtils.isNonEmpty(obj)) {
                                //dataList.add(obj)
                                dataList.add(
                                    SessionListEntity(
                                        TopicConstants.INVENTORY,
                                        menuCode,
                                        transactionType,
                                        obj
                                    )
                                )
                            }
                        }
                    }

                    if (dataList.isNotEmpty()) {
                        /*inventoryBarcodeList.clear()
                        inventoryBarcodeList.addAll(dataList)*/

                        CoroutineScope(Dispatchers.IO).launch {
                            db.sessionListDao().insertAll(dataList)
                        }

                        //TODO save inventory barcode list in db or Data Store
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
                        jsonRequest.put(ParameterConstants.TRANSACTION_TYPES, transactionType)
                        jsonRequest.put(ParameterConstants.ASSET_LOCATION_PATH, selectedLocationId.value!!.path)
                        jsonRequest.put(ParameterConstants.DEVICE_SESSION_ID, deviceSessionId)
                        jsonRequest.put(ParameterConstants.VALID_SCAN_TAG_COUNT, validCount.value)
                        jsonRequest.put(ParameterConstants.INVALID_SCAN_TAG_COUNT, invalidCount.value)
                        jsonRequest.put(ParameterConstants.BARCODE, responseArray)
                        apiViewModel.callApi(UrlConstants.START_DEVICE_SESSION_INVENTORY, jsonRequest = jsonRequest)
                    } else {
                        //TODO give custom error if ean List is empty
                    }
                }

                UrlConstants.START_DEVICE_SESSION_INVENTORY -> {
                    val id = extractString(jsonResponse, ParameterConstants.ID, "")
                    val deviceSessionId = extractString(jsonResponse, ParameterConstants.DEVICE_SESSION_ID, "")
                    val transactionType = extractString(jsonResponse, ParameterConstants.TRANSACTION_TYPES, extractString(jsonResponse, ParameterConstants.TRANSACTION_TYPE, ""))
                    LogUtils.showLog("transactionType", "" + transactionType)
                    val assetLocationPath = extractString(jsonResponse, ParameterConstants.ASSET_LOCATION_PATH, "")
                    val stockCount = ParseUtils.extractInt(jsonResponse, ParameterConstants.STOCK_COUNT, 0)
                    val sessionData = JSONObject()
                    sessionData.put(ParameterConstants.SESSION_ID,deviceSessionId)
                    sessionData.put(ParameterConstants.ID,id)
                    sessionData.put(ParameterConstants.TRANSACTION_TYPE,transactionType)
                    sessionData.put(ParameterConstants.ASSET_LOCATION_PATH,assetLocationPath)
                    readerViewModel.setSessionId(deviceSessionId,sessionData.toString())
                    if(stockCount>0) lastTagCount.value=stockCount
                    val preHeader = TopicConstants.INVENTORY + "_" + menuCode + "_"
                    DataStoreManager.saveToPreferences(preHeader + ParameterConstants.ID, id)
                    DataStoreManager.saveToPreferences(preHeader + ParameterConstants.DEVICE_SESSION_ID, deviceSessionId)
                    DataStoreManager.saveToPreferences(preHeader + ParameterConstants.TRANSACTION_TYPE, transactionType)
                    DataStoreManager.saveToPreferences(preHeader + ParameterConstants.ASSET_LOCATION_NAME, selectedLocationId.value!!.name)
                    DataStoreManager.saveToPreferences(preHeader + ParameterConstants.ASSET_LOCATION_PATH, assetLocationPath)
                    DataStoreManager.saveToPreferences(preHeader + ParameterConstants.CUSTOM_INVENTORY_PARAM_VALUE, selectedCustomValue.value)
                    if(inventoryBarcodeList.value.isNotEmpty() && deviceSessionId.isNotEmpty() && id.isNotEmpty()) {
                        isNewSession.value=false
                        readerViewModel.toggleInventory(setPower.value,eans = inventoryBarcodeList.value)
                    }
                }

                UrlConstants.STOP_DEVICE_SESSION_INVENTORY, UrlConstants.CANCEL_DEVICE_SESSION_INVENTORY -> {
                    if(isDataUploaded.value) {
                        isOnDataUploaded.value=true
                        sheetState.hide()
                        showSheet.value = false
                        currentSheet.value = BottomSheetType.NONE
                        delay(500)
                    }
                    scope.launch(Dispatchers.IO) {
                        //delete dataStoreVals
                        if (tagCount.value > 0) {
                            val result = db.tagInfoDao().deleteBySessionTypeAndTransactionType(sessionType = menuCode, transactionType)
                            LogUtils.showLog("resultDeleteSession", "" + result)
                            if (result <= 0) return@launch
                        }
                        db.sessionListDao().deleteAll(TopicConstants.INVENTORY, menuCode, transactionType)
                        val preHeader = TopicConstants.INVENTORY + "_" + menuCode + "_"
                        DataStoreManager.saveToPreferences(preHeader + ParameterConstants.ID, "")
                        DataStoreManager.saveToPreferences(preHeader + ParameterConstants.DEVICE_SESSION_ID, "")
                        DataStoreManager.saveToPreferences(preHeader + ParameterConstants.TRANSACTION_TYPE, "")
                        DataStoreManager.saveToPreferences(preHeader + ParameterConstants.ASSET_LOCATION_NAME, "")
                        DataStoreManager.saveToPreferences(preHeader + ParameterConstants.ASSET_LOCATION_PATH, "")
                        DataStoreManager.saveToPreferences(preHeader + ParameterConstants.CUSTOM_INVENTORY_PARAM_NAME, "")
                        DataStoreManager.saveToPreferences(preHeader + ParameterConstants.CUSTOM_INVENTORY_PARAM_LABEL, "")
                        DataStoreManager.saveToPreferences(preHeader + ParameterConstants.CUSTOM_INVENTORY_PARAM_VALUE, "")
                        DataStoreManager.saveToPreferences(preHeader + ParameterConstants.READER_POWER, 30)
                        scope.launch {
                            readerViewModel.clearSessionAndTransactionType()
                            navController.popBackStack()
                        }
                    }
                    //readerViewModel.onDestroy()
                    //if(navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                    //readerViewModel.clearSessionAndTransactionType()
                    //navController.popBackStack()
                    //}
                }

            }
        }
    }

    Box() {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .clickable(
                    onClick = {},
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        )
        {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.Top) {
                TopBarContent(
                    label,
                    onBackClickL = {
                        if (chkTrue(isProcessOn.value) || chkTrue(isApiLoading.value)) return@TopBarContent
                        scope.launch {
                            val preHeader = TopicConstants.INVENTORY + "_" + menuCode + "_"
                            val id = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.ID, "")
                            val deviceSessionId = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.DEVICE_SESSION_ID, "")

                            if (deviceSessionId.isNullOrEmpty() || id.isNullOrEmpty()) {
                             if(navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                                DataStoreManager.saveToPreferences(preHeader + ParameterConstants.READER_POWER, 30)
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
                    },
                    onSettingClick = {
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

            Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_24)))
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (selectedLocationName.value.isNullOrEmpty() || selectedCustomValue.value.isNullOrEmpty()) {
                        Card(
                            modifier = Modifier
                                .wrapContentWidth()
                                .height(dimensionResource(R.dimen.dp_44))
                                .clickable(
                                    onClick = {
                                        scope.launch {
                                            optionPicker.value = true
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
                                    .wrapContentWidth()
                                    .height(dimensionResource(R.dimen.dp_56))
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
                                    text = String.format(
                                        stringResource(R.string.select_zone_and_),
                                        customLabel.value
                                    ),
                                    style = CommonTypography.current.noteText,
                                    color = BlackColor
                                )
                                Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_10)))
                            }
                        }
                    }
                    else {
                        GernericBasicTextField(
                            sourceZone = selectedLocationName.value + "," + selectedCustomValue.value,
                            destZone = "",
                            menuCode = menuCode,
                            value = selectedLocationName.value + "," + selectedCustomValue.value,
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
                                    optionPicker.value = true
                                }
                            },
                            destAction = {

                            },
                            clearAction = {
                                scope.launch {
                                    selectedLocationName.value = ""
                                    selectedLocationId.value = null
                                    selectedCustomValue.value = ""
                                    lastTagCount.value = 0
                                    optionPicker.value = false
                                }
                            },
                            isDestZone = false,
                            isSet = !(deviceSessionId.isNullOrEmpty() || id.isNullOrEmpty())
                        )
                        /** BasicTextField(
                        value = selectedLocationName.value + "," + selectedCustomValue.value,
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                        textStyle = CommonTypography.current.noteText.copy(
                        color = BlackColor,
                        textAlign = TextAlign.Start
                        ),
                        modifier = Modifier
                        .wrapContentWidth()
                        .shadow(12.dp, RoundedCornerShape(dimensionResource(R.dimen.dp_24)))
                        .height(dimensionResource(R.dimen.dp_44))
                        .border(
                        width = 1.dp,
                        color = if (isOnDataUploaded.value) GreenBorder else Yellow,
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
                        tint = if (isOnDataUploaded.value) Green else Yellow
                        )

                        Spacer(Modifier.width(6.dp))

                        Box(
                        modifier = Modifier.wrapContentWidth().clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                        ) {
                        scope.launch {
                        apiViewModel.callApi(
                        url = UrlConstants.LOCATION_SUB_ZONES,
                        appendData = DataStoreManager.readFromPreferences(
                        LoginConstants.DEVICE_LOCATION_ID, ""
                        )
                        )
                        optionPicker.value = true
                        }
                        },
                        contentAlignment = Alignment.CenterStart
                        ) {
                        innerTextField()
                        }

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
                        selectedCustomValue.value = ""
                        optionPicker.value = false
                        }
                        }
                        )

                        }
                        else {
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
                    if (false && dynamicFilters.isNotEmpty()) { //TEMP COMMENT
                        Spacer(modifier = Modifier.size(10.dp))
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
                        } else {
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
            }

            if(!isOnDataUploaded.value && (unEncodedCount.value > 0 || alienCount.value > 0)){
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
                                R.string.alien_tags,
                                alienCount.value.toString()//(invalidCount.value - unEncodedCount.value).toString()
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
                InventoryUploadedSuccess(
                    count = tagCount.value
                )
            }
            else {
                InventoryPulseCircle(
                    totalCount = tagCount.value,
                    tagCount = validCount.value.toString(),
                    isInvOn = chkTrue(isInvOn.value),
                    lastTagCount = if(lastTagCount.value>0) lastTagCount.value.toString() else "",
                    onClick = {
                        openSheet(
                            scope = scope,
                            sheetState = sheetState,
                            showSheet = showSheet,
                            currentSheet = currentSheet,
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


            /**Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
            ) {
            Box(
            modifier = Modifier
            .padding(top = dimensionResource(R.dimen.dp_97))
            .size(dimensionResource(R.dimen.dp_180))
            .drawBehind {
            drawCircle(
            brush = Brush.verticalGradient(
            colors = listOf(
            Color(0xFFFBBBBBB),
            Color(0xFFF3F3F3),
            )
            ),
            radius = strokeWidth.value,
            style = Stroke(width = strokeWidth.value)
            )
            }
            .background(
            Brush.verticalGradient(
            colors = listOf(Color(0xFFFFFFFF6), Color(0xFFF3F3F3))
            ), shape = CircleShape
            ), contentAlignment = Alignment.Center) {
            Text(
            text = tagCount.value.toString(),
            style = CommonTypography.current.headingH1,
            fontSize = dimensionResource(R.dimen.sp_40).value.sp
            )
            LogUtils.showLog("tagCount.value", "StartInventoryContent: ${tagCount.value.toString()}")
            }
            }*/
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
                                DataStoreManager.saveToPreferences(preHeader + ParameterConstants.READER_POWER, setPower.value)
                                readerViewModel.setPower(setPower.value)
                            }
                        )
                    }

                    BottomSheetType.SESSION -> Box(
                        modifier = Modifier
                            .background(WhiteColor)
                            .fillMaxWidth()
                            .fillMaxHeight(0.38f)
                    ) {
                        SessionContent(
                            showSheet = showSheet,
                            sheetState = sheetState,
                            scope = scope,
                            navController = navController,
                            tagCount = tagCount.value,
                            onStopSession = {
                                val preHeader = TopicConstants.INVENTORY + "_" + menuCode + "_"
                                val id = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.ID, "")
                                val deviceSessionId = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.DEVICE_SESSION_ID, "")
                                scope.launch {
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
                                showSheet = showSheet,
                                sheetState = sheetState,
                                scope = scope,
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
                                title = label,//stringResource(R.string.upload_stock),
                                //heading = stringResource(R.string.confirm_stock_upload),
                                subHeading = formatedText,
                                //primaryButtonText = stringResource(R.string.confirm_and_upload),
                                showSheet = showSheet,
                                sheetState = sheetState,
                                scope = scope,
                                tagCount = tagCount.value,
                                isDataUploaded = isDataUploaded
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
                    ) {
                        FilterPicker(
                            filters = DataStoreManager.getFilterList(),
                            onDismiss = {
                                scope.launch {
                                    sheetState.hide()
                                    showSheet.value = false
                                    currentSheet.value = BottomSheetType.NONE
                                }
                            },
                            onApply = {
                                showSheet.value = false
                            },
                        )
                    }

                    else -> {

                    }
                }
            }
        }

        Box(modifier = Modifier
            .fillMaxSize()
            .padding(16.dp), contentAlignment = Alignment.BottomCenter) {
            AppSnackBar(snackbarController)
        }
    }
}

fun clearSavedSessionValues(
    context: Context,
    menuCode: String,
    transactionType: String,
    selectedLocation: String,
    selectedCustomValue: String
) {
    val preHeader = TopicConstants.INVENTORY + "_" + menuCode + "_"
    val id = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.ID, "")
    val deviceSessionId = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.DEVICE_SESSION_ID, "")
    if (deviceSessionId.isNullOrEmpty() || id.isNullOrEmpty()) {
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getDbInstance(context)
            if (db.sessionListDao().hasData(TopicConstants.INVENTORY, menuCode, transactionType))
                db.sessionListDao().deleteAll(TopicConstants.INVENTORY, menuCode, transactionType)
        }
    }
}


@Composable
fun CustomInventoryBottomBar(
    apiViewModel: ApiViewModel,
    readerViewModel: ReaderViewModel,
    selectedLocation: MutableState<String>,
    selectedCustomValue: MutableState<String>,
    optionPicker: MutableState<Boolean>,
    selFilterKeys: SnapshotStateList<String>,
    dynamicFilterPicker: MutableState<Boolean>,
    dynamicFilters: SnapshotStateMap<String, List<Any>>,
    isOnDataUploaded: MutableState<Boolean>,
    tagCount: State<Int>,
    uploadedTagCount: State<Int>,
    isInvOn: State<Boolean?>,
    isLoader: MutableState<Boolean>,
) {
    /**val db = AppDatabase.getDbInstance(context)
    val inventoryBarcodeList = db.sessionListDao().getValues(TopicConstants.INVENTORY, menuCode, transactionType).collectAsState(emptyList())
    LogUtils.showLog("inventoryBarcodeList", "" + inventoryBarcodeList.value.size)
    val tagCount = db.tagInfoDao().getCount(menuCode, transactionType).observeAsState(initial = 0)
    val validCount = db.tagInfoDao().getValidCount(menuCode, transactionType).observeAsState(initial = 0)
    val invalidCount = db.tagInfoDao().getInvalidCount(menuCode, transactionType).observeAsState(initial = 0)*/

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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(WhiteColor)
    ) {
        HorizontalDivider(color = OutlineDefault)
        if (!optionPicker.value && !isOnDataUploaded.value) {
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
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentColor = if (isInvOn.value == false) WhiteColor else BlackColor,
                    gradientBrush = if (isInvOn.value == false)Brush.horizontalGradient(
                        colors = listOf(
                            BlackColor,
                            ButtonGray
                        )) else
                        SolidColor(WhiteColor),
                    enabled = selectedLocation.value.isNotEmpty() && selectedCustomValue.value.isNotEmpty() || isLoader.value,
                    isLoading = isLoader.value
                )
            }
        }
    }
}

@Composable
fun CardContent(text: String) {
    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(R.drawable.property_slidershorizontal),
            contentDescription = null
        )

        Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_8)))

        Text(
            text = text,
            style = CommonTypography.current.buttonSemiBold,
            color = TextSubtext
        )
    }
}


@Composable
fun BrandPicker(
    menuCode: String,
    brandList: List<String>,
    onDismiss: () -> Unit,
    onBrandSelected: (String) -> Unit
) {
    val searchQuery = remember { mutableStateOf("") }
    val filteredBrands = if (searchQuery.value.isEmpty()) {
        brandList
    } else {
        brandList.filter {
            it.contains(searchQuery.value, ignoreCase = true)
        }
    }

    // 🔹 FULL SCREEN OVERLAY
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.35f))
            .padding(top = 44.dp)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onDismiss() },
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

                    Text(
                        text = stringResource(R.string.select_),
                        style = CommonTypography.current.headingH1,
                        modifier = Modifier.padding(bottom = dimensionResource(R.dimen.dp_16))
                    )

                    val preHeader = TopicConstants.INVENTORY + "_" + menuCode + "_"
                    SearchBar(
                        placeholder = "Search by " + DataStoreManager.readFromPreferences(
                            preHeader + ParameterConstants.CUSTOM_INVENTORY_PARAM_LABEL,
                            ""
                        ),
                        searchQuery = searchQuery.value,
                        onQueryChange = { searchQuery.value = it }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn {
                        items(filteredBrands) { brand ->
                            LocationRow( // reuse same row UI
                                location = brand,
                                onClick = { onBrandSelected(brand) }
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

enum class PickerType {
    NONE, BAND, ZONE
}

@Composable
fun OptionPicker(
    selectedLocationId: MutableState<LocationModel?>,
    selectedCustomValue: MutableState<String>,
    customLabel: MutableState<String>,
    locationList: List<LocationModel>,
    customValuesList: SnapshotStateList<String>,
    onDismiss: () -> Unit,
    onFilterApplied: (LocationModel?, String?) -> Unit,
) {
    val searchQuery = remember { mutableStateOf("") }
    val activePicker = remember { mutableStateOf(PickerType.NONE) }

    // 🔹 ZONE FILTER
    val filteredLocations = if (searchQuery.value.isEmpty()) {
        locationList
    } else {
        locationList.filter {
            it.name.contains(searchQuery.value, ignoreCase = true)
        }
    }

    // 🔹 CUSTOM FIELD FILTER
    val filteredCustomField = if (searchQuery.value.isEmpty()) { customValuesList
    } else {
        customValuesList.filter {
            it.contains(searchQuery.value, ignoreCase = true)
        }
    }

    val selectedZone = remember { mutableStateOf<LocationModel?>(selectedLocationId.value) }
    val selectedCustomField = remember { mutableStateOf<String?>(selectedCustomValue.value) }

    // 🔹 FULL SCREEN OVERLAY
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.35f))
            .padding(top = 44.dp)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                activePicker.value = PickerType.NONE
                searchQuery.value = ""
            },
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {


            /* -------------------- ZONE PICKER -------------------- */
            if (activePicker.value == PickerType.ZONE) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .heightIn(max = 350.dp)
                        .background(WhiteColor, RoundedCornerShape(32.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {

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
                                    onClick = {
                                        activePicker.value = PickerType.NONE
                                        searchQuery.value = ""
                                        selectedZone.value = location
                                        //onLocationSelected(location)
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_12)))
            }else{
                /* -------------------- ZONE ROW -------------------- */
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .heightIn(dimensionResource(R.dimen.dp_48))
                        .background(WhiteColor, RoundedCornerShape(32.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(dimensionResource(R.dimen.dp_10)),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Text(
                            text = "Zone",
                            style = CommonTypography.current.textMedium,
                            color = TextSubtext
                        )

                        if (selectedZone.value == null) {
                            // 🔹 Show IconText BEFORE selection
                            IconText(
                                icon = painterResource(R.drawable.property_add),
                                actionText = stringResource(R.string.add_location),
                                onActionClick = {
                                    searchQuery.value = ""
                                    activePicker.value = PickerType.ZONE
                                },
                                tintColor = BlackColor,
                                elevation = 0.dp,
                                verticalPadding = 10.dp
                            )
                        } else {
                            // 🔹 Show selected value AFTER selection
                            RowText(
                                actionText = selectedZone.value!!.name,
                                onActionClick = {
                                    searchQuery.value = ""
                                    activePicker.value = PickerType.ZONE
                                }
                            )
                        }
                    }
                }
            }



            Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_12)))

            /* -------------------- BRAND PICKER -------------------- */
            if (activePicker.value == PickerType.BAND) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .heightIn(max = 350.dp)
                        .background(WhiteColor, RoundedCornerShape(32.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {

                        SearchBar(
                            searchQuery = searchQuery.value,
                            onQueryChange = { searchQuery.value = it },
                            placeholder = String.format(stringResource(id = R.string.search_),customLabel.value)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        LazyColumn {
                            items(filteredCustomField) { brand ->
                                LocationRow(
                                    location = brand,
                                    onClick = {
                                        activePicker.value = PickerType.NONE
                                        searchQuery.value = ""
                                        selectedCustomField.value = brand
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_12)))
            }else{
                /* -------------------- BAND ROW -------------------- */
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .heightIn(dimensionResource(R.dimen.dp_48))
                        .background(WhiteColor, RoundedCornerShape(32.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(dimensionResource(R.dimen.dp_10)),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Text(
                            text = customLabel.value,
                            style = CommonTypography.current.textMedium,
                            color = TextSubtext
                        )

                        if (selectedCustomField.value.isNullOrEmpty()) {
                            // 🔹 Show IconText BEFORE selection
                            IconText(
                                icon = painterResource(R.drawable.property_add),
                                actionText = String.format(stringResource(R.string.add_),customLabel.value),
                                onActionClick = {
                                    searchQuery.value = ""
                                    activePicker.value = PickerType.BAND
                                },
                                tintColor = BlackColor,
                                elevation = 0.dp,
                                verticalPadding = 10.dp
                            )
                        }
                        else {
                            // 🔹 Show selected value AFTER selection
                            RowText(
                                actionText = selectedCustomField.value!!,
                                onActionClick = {
                                    searchQuery.value = ""
                                    activePicker.value = PickerType.BAND
                                }
                            )
                        }
                    }
                }
            }

            /* -------------------- ACTION BUTTONS -------------------- */
            Row(
                modifier = Modifier.padding(dimensionResource(R.dimen.dp_12)),
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.dp_16)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(dimensionResource(R.dimen.dp_48))
                        .background(WhiteColor, RoundedCornerShape(24.dp))
                ) {
                    Row() {
                        CommonButton(
                            text = stringResource(id = R.string.apply_filter),
                            onClick = {
                                onFilterApplied(selectedZone.value,selectedCustomField.value)
                                onDismiss()
                            },
                            modifier = Modifier.weight(1f),
                            enabled = (selectedZone.value?.name?.isNotEmpty() == true) &&
                                    (selectedCustomField.value?.isNotEmpty() == true)
                        )
                    }
                }

                Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_8)))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(dimensionResource(R.dimen.dp_48))
                        .background(WhiteColor, RoundedCornerShape(24.dp))
                ) {
                    Row() {
                        CommonButton(
                            text = stringResource(id = R.string.cancel),
                            onClick = {
                                selectedZone.value = null
                                selectedCustomField.value = null
                                onDismiss()
                            },
                            modifier = Modifier.weight(1f),
                            gradientBrush = SolidColor(WhiteColor),
                            contentColor = RedColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_14)))
        }
    }
}

/*@Composable
fun BrandPicker(
    brandList: List<String>,
    onDismiss: () -> Unit,
    onBrandSelected: (String) -> Unit
) {

    val searchQuery = remember { mutableStateOf("") }

    val filteredBrands = if (searchQuery.value.isEmpty()) {
        brandList
    } else {
        brandList.filter {
            it.contains(searchQuery.value, ignoreCase = true)
        }
    }

    // 🔹 Background dim (same as LocationPicker)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.35f))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onDismiss() }
            .zIndex(1f)
    )

    // 🔹 Floating Card container (same as LocationPicker)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(356.dp)
            .zIndex(2f),
        contentAlignment = Alignment.TopCenter
    ) {

        Column(modifier = Modifier.fillMaxWidth()) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 24.dp),
                horizontalArrangement = Arrangement.End
            ) {
                CircularIcon(
                    iconRes = R.drawable.clear
                )
            }

            Card(
                modifier = Modifier
                    .padding(top = 80.dp)
                    .fillMaxWidth(0.92f),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(14.dp)
            ) {

                Column(
                    modifier = Modifier
                        .background(WhiteColor, RoundedCornerShape(24.dp))
                        .padding(16.dp)
                ) {

                    // 🔍 Search bar (same as LocationPicker)
                    SearchBar(
                        searchQuery = searchQuery.value,
                        onQueryChange = { searchQuery.value = it }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn {
                        items(filteredBrands) { brand ->
                            LocationRow( // reuse same row UI
                                location = brand,
                                onClick = { onBrandSelected(brand) }
                            )
                        }
                    }
                }
            }
        }
    }
}*/



