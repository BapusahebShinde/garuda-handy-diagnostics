package com.itek.rftaar.presentation.search

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
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
import androidx.compose.ui.draw.clip
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
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController
import com.itek.rftaar.R
import com.itek.rftaar.ReaderActivity
import com.itek.rftaar.api.constants.ParameterConstants
import com.itek.rftaar.api.constants.UrlConstants
import com.itek.rftaar.core.common.constants.LoginConstants
import com.itek.rftaar.core.common.constants.MenuConstants
import com.itek.rftaar.core.common.utils.LogUtils
import com.itek.rftaar.core.common.utils.ParseUtils
import com.itek.rftaar.core.database.AppDatabase
import com.itek.rftaar.core.database.DataStoreManager
import com.itek.rftaar.data.entity.TagInfoEntity
import com.itek.rftaar.domain.model.LocationModel
import com.itek.rftaar.mqtt.constants.InventoryConstants
import com.itek.rftaar.mqtt.constants.SearchTypeConstant
import com.itek.rftaar.mqtt.constants.TopicConstants
import com.itek.rftaar.presentation.commonComp.AppSnackBar
import com.itek.rftaar.presentation.commonComp.AppSnackBarData
import com.itek.rftaar.presentation.commonComp.BottomSheetType
import com.itek.rftaar.presentation.commonComp.CircularIcon
import com.itek.rftaar.presentation.commonComp.CommonButton
import com.itek.rftaar.presentation.commonComp.CounterText
import com.itek.rftaar.presentation.commonComp.EanListRow
import com.itek.rftaar.presentation.commonComp.ErrorAppSnackBarData
import com.itek.rftaar.presentation.commonComp.GernericBasicTextField
import com.itek.rftaar.presentation.commonComp.InventoryPulseCircle
import com.itek.rftaar.presentation.commonComp.SnackbarController
import com.itek.rftaar.presentation.commonComp.TopBarContent
import com.itek.rftaar.presentation.commonComp.openSheet
import com.itek.rftaar.presentation.encoding.DeviceSettings
import com.itek.rftaar.presentation.encoding.SearchBottomSheetView
import com.itek.rftaar.presentation.encoding.SessionContent
import com.itek.rftaar.presentation.inventory.LocationPicker
import com.itek.rftaar.presentation.inventory.SessionAlertBottomSheet
import com.itek.rftaar.presentation.inventory.SetInvDevicePower
import com.itek.rftaar.presentation.movement.clearSavedSessionValues
import com.itek.rftaar.presentation.viewmodel.ApiViewModel
import com.itek.rftaar.presentation.viewmodel.ReaderViewModel
import com.itek.rftaar.ui.theme.BackGround
import com.itek.rftaar.ui.theme.BlackColor
import com.itek.rftaar.ui.theme.ButtonGray
import com.itek.rftaar.ui.theme.CommonTypography
import com.itek.rftaar.ui.theme.Green
import com.itek.rftaar.ui.theme.GreenBorder
import com.itek.rftaar.ui.theme.LightRed
import com.itek.rftaar.ui.theme.OutlineDefault
import com.itek.rftaar.ui.theme.RedColor
import com.itek.rftaar.ui.theme.TabColor
import com.itek.rftaar.ui.theme.WhiteColor
import com.itek.rftaar.utils.CommonUtils
import com.itek.rftaar.utils.CommonUtils.chkTrue
import com.itek.rftaar.utils.SessionUtils
import com.itek.rftaar.utils.ZoneUtils.processChildren
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.json.JSONArray


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnencodeSearch(
    modifier: Modifier,
    navController: NavHostController,
    label: String,
    menuCode: String,
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
    val preHeader = TopicConstants.SEARCH + "_" + menuCode + "_"
    val id = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.ID, "")
    val deviceSessionId = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.DEVICE_SESSION_ID, "")
    val isAlien =  menuCode.equals(MenuConstants.ALIEN_SEARCH,true)
    val transactionType = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.TRANSACTION_TYPE, if (isAlien) InventoryConstants.INVENTORY_ALIEN else InventoryConstants.INVENTORY_UNENCODE)

    LogUtils.showLog("isAlien",""+isAlien)
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

    if (activity != null) {
        if (deviceSessionId.isNullOrEmpty()) readerViewModel.onCreate()
        readerViewModel.setSessionAndTransactionType(menuCode, transactionType)
        if (deviceSessionId.isNullOrEmpty()) {
            apiViewModel.callApi(
                UrlConstants.LOCATION_SUB_ZONES, appendData = DataStoreManager.readFromPreferences(
                    LoginConstants.DEVICE_LOCATION_ID,
                    ""
                )
            )
        }
        else {
            if (assetLocationPath.isNotEmpty() && assetLocationName.isNotEmpty()) {
                //TODO set value & disable location field
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
    val validCount = db.tagInfoDao().getValidCount(menuCode, transactionType).observeAsState(initial = 0)
    val invalidCount = db.tagInfoDao().getInvalidCount(menuCode, transactionType).observeAsState(initial = 0)
    val foundCount = db.tagInfoDao().getFoundCount(menuCode,transactionType).observeAsState(initial = 0)

    LogUtils.showLog(
        "tagCount",
        "UnencodedSearchInventory: ${isInvOn.value}\n${tagCount.value} \n ${validCount.value} \n${invalidCount.value}"
    )

    LaunchedEffect(foundCount) {
        //TODO force re-compose UI ?
    }


    BackHandler(enabled = true) {
        if (chkTrue(isInvOn.value) || chkTrue(isApiLoading.value)) return@BackHandler
        scope.launch {
            val preHeader = TopicConstants.SEARCH + "_" + menuCode + "_"
            val deviceSessionId = DataStoreManager.readFromPreferences(
                preHeader + ParameterConstants.DEVICE_SESSION_ID,
                ""
            )
            if (deviceSessionId.isNullOrEmpty()) {
                clearSavedSessionValues(context, menuCode, transactionType,topic=TopicConstants.SEARCH)
                if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
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
                    modifier = modifier
                        .fillMaxWidth()
                        .background(WhiteColor),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    //UnencodeSearchBottomBar(isInvOn,selectedLocationId,selectedLocation,context,menuCode,transactionType,apiViewModel,readerViewModel,snackbarController,locationMessage)
                    UnencodeSearchBottomBar(
                        isAlien,
                        readerViewModel,
                        selectedLocation,
                        showLocationPicker.value,
                        tagCount,
                        isOnDataUploaded,
                        isInvOn
                    )
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
                //UnencodeSearchContent(navController,label,selectedLocationId,selectedLocation,menuCode,showLocationPicker,scope,context,transactionType,isInvOn,locationList,readerViewModel,apiViewModel,snackbarController,showSheet,sheetState,currentSheet,setPower,locationMessage)
                val unencodedSearchArgs = UnencodeSearchScreenArgs(
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
                UnencodeSearchContent(
                    label,
                    isAlien,
                    apiViewModel,
                    readerViewModel,
                    navController,
                    selectedLocation,
                    locationList,
                    selectedLocationId,
                    showLocationPicker,
                    args = unencodedSearchArgs,
                    validCount,
                    invalidCount,
                    tagCount,
                    isOnDataUploaded,
                    foundCount
                )
            }
        }

        if (showLocationPicker.value) {
            LocationPicker(
                locationList,
                onDismiss = { showLocationPicker.value = false },
                onLocationSelected = { location ->
                    selectedLocationId.value = location
                    selectedLocation.value = location.name
                    showLocationPicker.value = false
                }
            )
        }

    }
}

data class UnencodeSearchScreenArgs @OptIn(ExperimentalMaterial3Api::class) constructor(
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
fun UnencodeSearchContent(
    label: String,
    isAlien: Boolean,
    apiViewModel: ApiViewModel,
    readerViewModel: ReaderViewModel,
    navController: NavHostController,
    selectedLocation: MutableState<String>,
    locationList: SnapshotStateList<LocationModel>,
    selectedLocationId: MutableState<LocationModel?>,
    showLocationPicker: MutableState<Boolean>,
    args: UnencodeSearchScreenArgs,
    validCount: State<Int>,
    invalidCount: State<Int>,
    tagCount: State<Int>,
    isOnDataUploaded: MutableState<Boolean>,
    foundCount: State<Int>,
    //sessionTagCount: MutableState<Int>
) {
    val db = AppDatabase.getDbInstance(args.context)
    val strokeWidth = dimensionResource(R.dimen.dp_2)
    val isApiLoading = apiViewModel.isLoading.observeAsState()
    val triggerPressed = readerViewModel.isTriggerPressed().observeAsState(false)
    val unEncodedCount = db.tagInfoDao().getUnencodedCount(args.menuCode, args.transactionType).observeAsState(0)
    val alienCount = db.tagInfoDao().getAlienCount(args.menuCode, args.transactionType).observeAsState(initial = 0)

   /* val totalCount = db.productZoneDataDao().getTotalQty(TopicConstants.INVENTORY, menuCode, transactionType).collectAsState(0)
    val totalCountZone = db.productZoneDataDao().getTotalQty(TopicConstants.INVENTORY, menuCode, transactionType, selectedLocationName.value, selectedLocationPath.value).collectAsState(0)
    LogUtils.showLog(
        "Selected values",
        "StockCorrectionContentView: ${selectedLocationName.value} \n ${selectedLocationPath.value}"
    )
    val tagCount = db.tagInfoDao().getCount(menuCode,transactionType).observeAsState(initial = 0)*/

    val snackbarController = remember { SnackbarController() }
    val error = readerViewModel.error().observeAsState()
    val locationMessage = stringResource(R.string.select_location_)
    val preHeader = TopicConstants.SEARCH + "_" + args.menuCode + "_"
    val id = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.ID, "")
    val deviceSessionId =
        DataStoreManager.readFromPreferences(preHeader + ParameterConstants.DEVICE_SESSION_ID, "")
    val hasActiveSession = deviceSessionId.isNotEmpty() && id.isNotEmpty()
    val isProcessOn = readerViewModel.isProcessOn().observeAsState(false)
    val lastTagCount = remember { mutableStateOf(0) }
    val isDataUploaded = remember { mutableStateOf(false) }

    val response = apiViewModel.apiResult.collectAsState(initial = null)


    val searchSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val selectedSearchType = remember{ mutableStateOf("")}

    val selectedTagInfo = remember { mutableStateOf<TagInfoEntity?>(null) }

    val unencodedList = db.tagInfoDao().getBarcodeList(args.menuCode,args.transactionType).observeAsState(emptyList())

    LogUtils.showLog("unencodedList",""+unencodedList.value)

    //val unencodedList = db.tagInfoDao().getBarcodeList1(args.menuCode,args.transactionType).collectAsState(emptyList())
    //val foundCount = db.tagInfoDao().getFoundCount(args.menuCode,args.transactionType).observeAsState(0)

    LaunchedEffect(error.value) {
        val message = error.value
        if (!message.isNullOrBlank()) {
            LogUtils.showLog("isTagNotFound", "SingleEncodingContent: $message")
            snackbarController.show(
                ErrorAppSnackBarData(message)
            )
        }
    }

    LaunchedEffect(triggerPressed.value) {
        if (triggerPressed.value == true) {
            readerViewModel.setTriggerValue(false)
            val preHeader = TopicConstants.SEARCH + "_" + args.menuCode + "_"
            val deviceSessionId = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.DEVICE_SESSION_ID, "")
            LogUtils.showLog("TriggerPressed", "Trigger is pressed")
            if (selectedLocation.value.isNullOrEmpty()) {
                snackbarController.show(
                    AppSnackBarData(
                        icon = R.drawable.error,
                        message = locationMessage,
                        showCancel = false
                    )
                )
            }
            else {
                if (deviceSessionId.isNullOrEmpty()){
                    val deviceSessionId = SessionUtils.generateOfflineSessionId(args.menuCode,args.transactionType)
                    DataStoreManager.saveToPreferences(preHeader+ParameterConstants.DEVICE_SESSION_ID,deviceSessionId)
                    DataStoreManager.saveToPreferences(preHeader+ParameterConstants.ASSET_LOCATION_NAME,selectedLocationId.value?.name)
                    DataStoreManager.saveToPreferences(preHeader+ParameterConstants.ASSET_LOCATION_PATH,selectedLocationId.value?.path)
                }
                if (args.showSheet.value != false && args.currentSheet.value == BottomSheetType.SEARCH) {
                    //Based on Selected Epc or TID
                    if(selectedSearchType.value.isNotEmpty() && selectedTagInfo.value!=null)
                        readerViewModel.toggleSearch(if(selectedSearchType.value.equals("epc",true)) SearchTypeConstant.EPC else SearchTypeConstant.TID,if(selectedSearchType.value.equals("epc",true)) selectedTagInfo.value!!.epc else selectedTagInfo.value!!.tid)
                }
                else {
                    if (isAlien) readerViewModel.toggleInventory(args.setPower.value, onlyAlien = true)
                    else readerViewModel.toggleInventory(args.setPower.value,onlyUnencoded = true)
                }
            }
        } else {
            LogUtils.showLog("TriggerPressed", "Trigger released")
        }
    }

    LaunchedEffect(foundCount) {

    }


    LaunchedEffect(response.value) {
        val result = response.value ?: return@LaunchedEffect
        if (result?.isSuccess != true) {
            val message = result?.errMsg
            if (!message.isNullOrBlank()) {
                snackbarController.show(
                    ErrorAppSnackBarData(result?.errMsg.toString())
                )
            }
        } else if (result.response != null) {
            val jsonResponse = result.response

            when (result.url) {
                UrlConstants.LOCATION_ZONES -> {
                    val responseArray =
                        ParseUtils.extractJSONArray(
                            jsonResponse,
                            ParameterConstants.DATA,
                            JSONArray()
                        )
                    val dataList = mutableListOf<LocationModel>()
                    if (responseArray != null && responseArray.length() > 0) {
                        processChildren(responseArray, dataList)
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
                    val dataObj = ParseUtils.extractJSONObject(
                        jsonResponse,
                        ParameterConstants.DATA,
                        jsonResponse
                    )
                    val responseArray = ParseUtils.extractJSONArray(
                        dataObj,
                        ParameterConstants.CHILDREN,
                        JSONArray()
                    )
                    val dataList = mutableListOf<LocationModel>()
                    if (responseArray != null && responseArray.length() > 0) {
                        processChildren(responseArray, dataList);
                    }
                    if (dataList.isNotEmpty()) {
                        locationList.clear()
                        locationList.addAll(dataList)
                        LogUtils.showLog("DataHolder.locationList", "StartInventoryContent:$dataList")
                    } else {
                        //TODO give custom error if responseArray is empty
                    }
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        )
        {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.Top) {
                TopBarContent(
                    args.label,
                    onBackClickL = {
                        args.scope.launch {
                            val preHeader = TopicConstants.SEARCH + "_" + args.menuCode + "_"
                            val deviceSessionId = DataStoreManager.readFromPreferences(
                                preHeader + ParameterConstants.DEVICE_SESSION_ID,
                                ""
                            )
                            if (chkTrue(isProcessOn.value)) return@launch
                            if (deviceSessionId.isNullOrEmpty()) {
                                DataStoreManager.saveToPreferences(
                                    preHeader + ParameterConstants.READER_POWER,
                                    30
                                )
                                if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
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

            Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_24)))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ){

                //val totalValue = if (deviceSessionId.isNotEmpty() && selectedLocation.value.isNotEmpty()) chkNull("0", "0") else ""
                if (unencodedList.value.isNotEmpty()){
                    CounterText(
                        current = foundCount.value,//currentValue,
                        total = unencodedList.value.size.toString(),
                        isLimitShow = deviceSessionId.isNotEmpty()
                    )
                    Text(
                        text = if (selectedLocation.value.isNullOrEmpty()) label else stringResource(
                            R.string.txt_items_found
                        ),
                        style = CommonTypography.current.noteText.copy(color = BlackColor)
                    )

                    Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_12)))
                }


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

                            Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_8)))
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
                                showLocationPicker.value = false
                            }
                        },
                        isDestZone = false,
                        isSet = !(deviceSessionId.isNullOrEmpty())
                    )
                   /** BasicTextField(
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
                                androidx.compose.material3.Icon(
                                    painter = painterResource(id = R.drawable.icon_location),
                                    contentDescription = null,
                                    modifier = Modifier.size(dimensionResource(R.dimen.dp_18)),
                                    tint = Yellow
                                )

                                Spacer(Modifier.width(6.dp))

                                // Text
                                innerTextField()

                                Spacer(Modifier.width(6.dp))

                                // Trailing icon
                                val preHeader = TopicConstants.SEARCH + "_" + args.menuCode + "_"
                                val deviceSessionId = DataStoreManager.readFromPreferences(
                                    preHeader + ParameterConstants.DEVICE_SESSION_ID,
                                    ""
                                )
                                if (deviceSessionId.isNullOrEmpty()) {
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
                                                }
                                            }
                                    )
                                } else {
                                    androidx.compose.material3.Icon(
                                        painter = painterResource(id = R.drawable.property_check_selected),
                                        contentDescription = null,
                                        tint = if (isOnDataUploaded.value) Green else LightGray
                                    )
                                }
                            }
                        }
                    )*/
                }

                if (args.isInvOn.value == true || tagCount.value == 0) {
                    InventoryPulseCircle(
                        totalCount = tagCount.value,
                        tagCount = tagCount.value.toString(),
                        isInvOn = args.isInvOn.value == true,
                        lastTagCount = "",
                        onClick = {},
                    )
                }

                if (args.isInvOn.value == false && tagCount.value > 0) {
                    Column(
                        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    )
                    {
                        EanListViewDetails(
                        epcList = unencodedList,
                        isUnencodedSearch = !isAlien,
                        onSearchClick = { epc ->
                            selectedTagInfo.value = epc//TagTime(epc.barcode,epc.epc,epc.tid,epc.insertTime,if(epc.isFound) "Found" else "Missing")
                            args.showSheet.value = true
                            openSheet(
                                scope = args.scope,
                                sheetState = args.sheetState,
                                showSheet = args.showSheet,
                                currentSheet = args.currentSheet,
                                sheet = BottomSheetType.SEARCH
                            )
                        }
                    )
                    }
                   /* UnencodedListView(
                        menuCode = args.menuCode,
                        transactionType = args.transactionType,
                        tagList = unencodedList,
                        onEpcClick = { epc ->
                            //val tagTime= TagTime(epc.barcode,epc.epc,epc.tid,epc.insertTime,if(epc.isFound) "Found" else "Missing")
                            selectedTagInfo.value = epc//TagTime(epc.barcode,epc.epc,epc.tid,epc.insertTime,if(epc.isFound) "Found" else "Missing")
                            args.showSheet.value = true
                            openSheet(
                                scope = args.scope,
                                sheetState = args.sheetState,
                                showSheet = args.showSheet,
                                currentSheet = args.currentSheet,
                                sheet = BottomSheetType.SEARCH
                            )
                        }
                    )*/
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
                                DataStoreManager.saveToPreferences(
                                    preHeader + ParameterConstants.READER_POWER,
                                    args.setPower.value
                                )
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
                        val formatedText = buildAnnotatedString {
                            append(stringResource(R.string.txt_found))
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold,color = BlackColor)) {
                                append(": ${foundCount.value}/${tagCount.value}")
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
                            tagCount =  tagCount.value,
                            subHeading = formatedText,
                            onStopSession = {
                                if (chkTrue(isProcessOn.value) || chkTrue(isApiLoading.value)) return@SessionContent
                                args.scope.launch {
                                    clearSavedSessionValues(
                                        args.context,
                                        args.menuCode,
                                        args.transactionType,
                                        topic=TopicConstants.SEARCH
                                    )
                                    if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                                        readerViewModel.clearSessionAndTransactionType()
                                        navController.popBackStack()
                                    }
                                }
                            },
                            readerViewModel = null,
                            limit = tagCount.value,
                            isAllowContinue = false
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

                    BottomSheetType.SEARCH -> Box(
                        modifier = Modifier
                            .background(WhiteColor)
                            .fillMaxWidth()
                            .fillMaxHeight(0.55f)
                    ) {
                        SearchBottomSheetView(
                            showSheet = args.showSheet,
                            sheetState = args.sheetState,
                            scope = args.scope,
                            selectedSearchType = selectedSearchType,
                            tagInfoEntity = selectedTagInfo,
                            showMarkFoundPercent = 80F,
                            readerViewModel = readerViewModel
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
fun UnencodeSearchBottomBar(
    isAlien: Boolean,
    readerViewModel: ReaderViewModel,
    selectedLocation: MutableState<String>,
    showLocationPicker: Boolean,
    tagCount: State<Int>,
    isOnDataUploaded: MutableState<Boolean>,
    isInvOn: State<Boolean?>,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(WhiteColor)
    ) {
        HorizontalDivider(color = OutlineDefault)
        if (!showLocationPicker) {
            Row(modifier = Modifier.padding(dimensionResource(R.dimen.dp_16))) {
                CommonButton(
                    text = stringResource(id = if (isInvOn.value == true) R.string.stop_scanning else if (tagCount.value > 0) R.string.continue_scanning else R.string.start_scanning_inventory),
                    onClick = {
                        readerViewModel.setTriggerValue(true)
                    },
                    icon = if (isInvOn.value == true) painterResource(R.drawable.stopicon) else null,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = selectedLocation.value.isNotEmpty(),
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
    }
}


@Composable
fun EanListViewDetails(
    epcList: State<List<TagInfoEntity>>,
    onSearchClick: (TagInfoEntity) -> Unit,
    isUnencodedSearch: Boolean
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BackGround)
            .padding(horizontal = dimensionResource(R.dimen.dp_10))
    ) {
        epcList.value.forEachIndexed { index, epc ->

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BackGround)
                    .padding(
                        horizontal = dimensionResource(R.dimen.dp_10),
                        vertical = dimensionResource(R.dimen.dp_8)
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isFound = epc.isFound//epc.status.equals("Found",true)
                LogUtils.showLog("EPC->",epc.epc+"_"+isFound)
                EanListRow(
                    title = if(isUnencodedSearch) CommonUtils.getMaskedString(epc.epc) else epc.barcode,
                    isShowingMaskedTitle = isUnencodedSearch,
                    actionText = stringResource(
                        id = if (isFound) R.string.txt_found else R.string.lbl_pending
                    ),
                    circleColor = if (isFound) Green else RedColor,
                    backgroundColor = if (isFound) GreenBorder else LightRed,
                    onActionClick = { /* NO-OP */ },
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(dimensionResource(R.dimen.dp_8)))

                CircularIcon(
                    iconRes = R.drawable.property_gieger_og,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable(
                            onClick = { onSearchClick(epc) },
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ),
                    backgroundColor = SolidColor(TabColor)
                )
            }

            if (index < epcList.value.lastIndex) {
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = OutlineDefault.copy(alpha = 0.5f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}
