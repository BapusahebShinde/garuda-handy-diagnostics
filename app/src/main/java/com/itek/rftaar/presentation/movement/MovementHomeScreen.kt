package com.itek.rftaar.presentation.movement

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Icon
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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController
import com.itek.rftaar.R
import com.itek.rftaar.ReaderActivity
import com.itek.rftaar.api.constants.ParameterConstants
import com.itek.rftaar.api.constants.UrlConstants
import com.itek.rftaar.core.common.constants.DateFormatUtils
import com.itek.rftaar.core.common.constants.LoginConstants
import com.itek.rftaar.core.common.utils.LogUtils
import com.itek.rftaar.core.common.utils.ParseUtils
import com.itek.rftaar.core.common.utils.ParseUtils.extractObject
import com.itek.rftaar.core.common.utils.ParseUtils.extractString
import com.itek.rftaar.core.database.AppDatabase
import com.itek.rftaar.core.database.DataStoreManager
import com.itek.rftaar.data.entity.TagInfoEntity
import com.itek.rftaar.data.model.TagTime
import com.itek.rftaar.domain.model.LocationModel
import com.itek.rftaar.mqtt.MqttManager
import com.itek.rftaar.mqtt.constants.SearchTypeConstant
import com.itek.rftaar.mqtt.constants.TopicConstants
import com.itek.rftaar.presentation.commonComp.AppSnackBar
import com.itek.rftaar.presentation.commonComp.BottomSheetType
import com.itek.rftaar.presentation.commonComp.CommonButton
import com.itek.rftaar.presentation.commonComp.CommonTextField
import com.itek.rftaar.presentation.commonComp.CounterText
import com.itek.rftaar.presentation.commonComp.ErrorAppSnackBarData
import com.itek.rftaar.presentation.commonComp.GernericBasicTextField
import com.itek.rftaar.presentation.commonComp.IconText
import com.itek.rftaar.presentation.commonComp.RowText
import com.itek.rftaar.presentation.commonComp.SnackbarController
import com.itek.rftaar.presentation.commonComp.TextFieldConfig
import com.itek.rftaar.presentation.commonComp.TopBarContent
import com.itek.rftaar.presentation.commonComp.TwoButtonView
import com.itek.rftaar.presentation.commonComp.openSheet
import com.itek.rftaar.presentation.decoding.ProductDetailsByEan
import com.itek.rftaar.presentation.decoding.handleProductDetails
import com.itek.rftaar.presentation.encoding.DeviceSettings
import com.itek.rftaar.presentation.encoding.SearchBottomSheetView
import com.itek.rftaar.presentation.encoding.SessionContent
import com.itek.rftaar.presentation.encoding.callProductDetails
import com.itek.rftaar.presentation.inventory.AcceptDataSheet
import com.itek.rftaar.presentation.inventory.InventoryUploadedSuccess
import com.itek.rftaar.presentation.inventory.LocationRow
import com.itek.rftaar.presentation.inventory.SearchBar
import com.itek.rftaar.presentation.inventory.SessionAlertBottomSheet
import com.itek.rftaar.presentation.inventory.SetInvDevicePower
import com.itek.rftaar.presentation.inventory.SwipeToUploadButton
import com.itek.rftaar.presentation.inventory.UploadDataSheet
import com.itek.rftaar.presentation.viewmodel.ApiViewModel
import com.itek.rftaar.presentation.viewmodel.ReaderViewModel
import com.itek.rftaar.ui.theme.BackGround
import com.itek.rftaar.ui.theme.BlackColor
import com.itek.rftaar.ui.theme.CommonTypography
import com.itek.rftaar.ui.theme.OutlineDefault
import com.itek.rftaar.ui.theme.RedColor
import com.itek.rftaar.ui.theme.TabColor
import com.itek.rftaar.ui.theme.WhiteColor
import com.itek.rftaar.utils.CommonUtils.chkTrue
import com.itek.rftaar.utils.NetworkUtils.isInternetConnected
import com.itek.rftaar.utils.SessionUtils
import com.itek.rftaar.utils.ZoneUtils.processChildren
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovementHomeScreen(
    modifier: Modifier,
    navController: NavHostController,
    label: String,
    menuCode: String,
    apiViewModel: ApiViewModel = hiltViewModel(),
    searchParams: Map<String, Any> = emptyMap()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val preHeader = TopicConstants.MOVEMENT + "_" + menuCode + "_"
    val transactionType = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.TRANSACTION_TYPE, "")
    val deviceSessionId = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.DEVICE_SESSION_ID, "")
    val assetLocationName = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.ASSET_LOCATION_NAME, "")
    val assetLocationPath = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.ASSET_LOCATION_PATH, "")
    val moveAtAssetLocationName = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.MOVE_AT_ASSET_LOCATION_NAME, "")
    val moveAtAssetLocationPath = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.MOVE_AT_ASSET_LOCATION_PATH, "")
    val readerPower = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.READER_POWER, 7)

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { sheetValue ->
            sheetValue != SheetValue.Hidden
        }
    )
    val showSheet = remember { mutableStateOf(false) }
    val currentSheet = remember { mutableStateOf(BottomSheetType.NONE) }
    val setPower = rememberSaveable { mutableStateOf(readerPower) }

    val filterStep = remember { mutableStateOf(FilterStep.NONE) }
    val isNewSession = remember { mutableStateOf(deviceSessionId.isNullOrEmpty()) }
    val optionPicker = remember { mutableStateOf(false) }
    val isDataUpload = remember { mutableStateOf(false) }
    val isOnDataUploaded = remember { mutableStateOf(false) }
    val isUploadData = remember { mutableStateOf(false) }

    val textValue = extractString(searchParams,"textValue","")
    val sourceZone = extractObject<LocationModel>(params = searchParams, cls = LocationModel::class)

    val locationList = remember { mutableStateListOf<LocationModel>() }
    val selectedSrcLocationId = remember { mutableStateOf<LocationModel?>(sourceZone) }
    val selectedDestLocationId = remember { mutableStateOf<LocationModel?>(null) }
    val selectedSrcLocationName = remember { mutableStateOf(sourceZone?.name ?: assetLocationName ?: "") }
    val selectedDestLocationName = remember { mutableStateOf(moveAtAssetLocationName ?: "") }
    LogUtils.showLog("selectedDestLocationId", "MovementHomeScreen: ${selectedSrcLocationId.value}")
    LogUtils.showLog("selectedSrcLocationName", "MovementHomeScreen: ${selectedSrcLocationName.value}")

    val activity = context as? ReaderActivity
    val readerViewModel = if (activity != null) activity.findReaderViewModel() else hiltViewModel()
    if (activity != null) {
        if (deviceSessionId.isNullOrEmpty()) readerViewModel.onCreate()
        readerViewModel.setSessionAndTransactionType(menuCode, transactionType,topic= TopicConstants.MOVEMENT)
        if (deviceSessionId.isNullOrEmpty()) {
            apiViewModel.callApi(
                UrlConstants.LOCATION_SUB_ZONES, appendData = DataStoreManager.readFromPreferences(
                    LoginConstants.DEVICE_LOCATION_ID,
                    ""
                )
            )
        } else {
            if (assetLocationPath.isNotEmpty() && assetLocationName.isNotEmpty()) {
                //TODO set value & disable location field
            } else if (moveAtAssetLocationPath.isNotEmpty() && moveAtAssetLocationName.isNotEmpty()) {
                //TODO set value & disable custom selection field (e.g. brand)
            }
        }
    }
    val isProcessOn = readerViewModel.isProcessOn().observeAsState()
    val hasActiveSession = deviceSessionId.isNotEmpty() && assetLocationPath.isNotEmpty() && moveAtAssetLocationPath.isNotEmpty()
    val showLocationPicker = remember { mutableStateOf(false) }
    val isInvOn = readerViewModel.isInventoryOn().observeAsState()
    val isApiLoading = apiViewModel.isLoading.observeAsState()
    val db = AppDatabase.getDbInstance(context)
    val customValuesList = remember { mutableStateListOf<String>() }

    val barcodeList = db.tagInfoDao().getBarcodeList(menuCode,transactionType).observeAsState(initial = emptyList())

    LaunchedEffect(isDataUpload.value) {
        if (isDataUpload.value == true) {
            if(!isInternetConnected(context, isShowErrToast = true)) {isDataUpload.value= false; return@LaunchedEffect}
            sheetState.hide()
            showSheet.value = false
            currentSheet.value = BottomSheetType.NONE
            val sessionId= DataStoreManager.readFromPreferences(preHeader + ParameterConstants.DEVICE_SESSION_ID,"")
            val sessionData= DataStoreManager.readFromPreferences(preHeader + "sessionData","")
            isOnDataUploaded.value=true
            delay(500)
            if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                clearSavedSessionValues(context, menuCode, transactionType,true)
                MqttManager.publishMovement(barcodeList.value, menuCode, transactionType, sessionId, sessionData=sessionData)
                readerViewModel.clearSessionAndTransactionType()
                navController.popBackStack()
            }
        }
    }


    BackHandler(enabled = true) {
        if (chkTrue(isInvOn.value) || chkTrue(isApiLoading.value)) return@BackHandler
        if(isUploadData.value) {isUploadData.value=false; return@BackHandler}
        scope.launch {
            val preHeader = TopicConstants.MOVEMENT + "_" + menuCode + "_"
            val deviceSessionId = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.DEVICE_SESSION_ID, "")
            if (deviceSessionId.isNullOrEmpty()) {
                clearSavedSessionValues(context, menuCode, transactionType)
                if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
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

    val movementArgs = MovementScreenArgs(
        context = context,
        label = label,
        menuCode = menuCode,
        transactionType = transactionType,
        scope = scope,
        sheetState = sheetState,
        showSheet = showSheet,
        currentSheet = currentSheet,
        readerPower = readerPower,
        setPower = setPower
    )

    Box() {
        Scaffold(
            bottomBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(WhiteColor),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    MovementBottomBar(
                        apiViewModel,
                        readerViewModel,
                        transactionType,
                        selectedSrcLocationId,
                        selectedSrcLocationName,
                        selectedDestLocationId,
                        selectedDestLocationName,
                        context,
                        menuCode,
                        navController,
                        filterStep.value,
                        optionPicker,
                        movementArgs,
                        isDataUpload,
                        isOnDataUploaded,
                        isUploadData,
                        barcodeList
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
                if (isOnDataUploaded.value) {
                    InventoryUploadedSuccess(barcodeList.value.size)
                }else {
                    MovementContent(
                        apiViewModel,
                        readerViewModel,
                        navController,
                        selectedSrcLocationName,
                        locationList,
                        selectedSrcLocationId,
                        selectedDestLocationId,
                        selectedDestLocationName,
                        movementArgs,
                        isDataUpload,
                        isOnDataUploaded,
                        isUploadData,
                        filterStep,
                        isNewSession,
                        hasActiveSession,
                        optionPicker,
                        textValue,
                        sourceZone,
                        barcodeList,
                    )
                }
            }
        }
        if (optionPicker.value) {
            OptionPicker(
                initialSource = selectedSrcLocationId.value,
                initialDestination = selectedDestLocationId.value,
                isDisableSourceSelection = sourceZone!=null,
                locationList = locationList,
                onDismiss = {
                    optionPicker.value = false
                },
                onFilterApplied = { source, destination ->

                    selectedSrcLocationId.value = source
                    selectedDestLocationId.value = destination

                    selectedSrcLocationName.value = source?.name ?: ""
                    selectedDestLocationName.value = destination?.name ?: ""

                    optionPicker.value = false
                },
                args = movementArgs
            )
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovementBottomBar(
    apiViewModel: ApiViewModel,
    readerViewModel: ReaderViewModel,
    transactionType: String,
    selectedSrcLocationId: MutableState<LocationModel?>,
    selectedSrcLocationName: MutableState<String>,
    selectedDestLocationId: MutableState<LocationModel?>,
    selectedDestLocationName: MutableState<String>,
    context: Context,
    menuCode: String,
    navController: NavHostController,
    filterStep: FilterStep,
    optionPicker: MutableState<Boolean>,
    args: MovementScreenArgs,
    isDataUpload: MutableState<Boolean>,
    isOnDataUploaded: MutableState<Boolean>,
    isUploadData: MutableState<Boolean>,
    barcodeList: State<List<TagInfoEntity>>
) {
    val isProcessOn = readerViewModel.isProcessOn().observeAsState()
    //val isPickOn = readerViewModel.isPickOn().observeAsState()
    val snackbarController = remember { SnackbarController() }
    //val isApiLoading = apiViewModel.isLoading.observeAsState()
    //val isUploadData = remember { mutableStateOf(false) }


    //val db = AppDatabase.getDbInstance(context)
    //val barcodeEntities = db.tagInfoDao().getBarcodeList(menuCode, transactionType).observeAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(WhiteColor)
    ) {
        HorizontalDivider(color = OutlineDefault)

        val hasScannedItems = barcodeList.value.isNotEmpty()
        if (!hasScannedItems && !isOnDataUploaded.value) {

            /* ---------------- INITIAL STATE → START SCANNING ---------------- */

            Row(modifier = Modifier.padding(dimensionResource(R.dimen.dp_16))) {
                CommonButton(
                    text = stringResource(R.string.txt_start_scanning),
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        readerViewModel.setTriggerValue(true)
                    },
                    enabled = selectedSrcLocationName.value.isNotEmpty() && selectedDestLocationName.value.isNotEmpty()
                )
            }

        }
        else if (hasScannedItems && isUploadData.value && !isOnDataUploaded.value) {

            SwipeToUploadButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimensionResource(R.dimen.dp_16)),
                text = stringResource(R.string.swipe_to_move,args.label)
            ) {
                openSheet(
                    scope = args.scope,
                    sheetState = args.sheetState,
                    showSheet = args.showSheet,
                    currentSheet = args.currentSheet,
                    sheet = BottomSheetType.UPLOAD
                )
                if(isUploadData.value) {
                    CoroutineScope(Dispatchers.IO).launch {
                        delay(100)
                        isUploadData.value = false
                    }
                }

            }

        }
        else if (hasScannedItems && !isOnDataUploaded.value) {
            TwoButtonView(
                primaryText = stringResource(
                    id = R.string.txt_scan_next_tag
                ),
                seconderText = stringResource(R.string.txt_upload_items),

                primaryAction = {
                    readerViewModel.setTriggerValue(true)
                },

                seconderAction = {
                    if (barcodeList.value.isEmpty()) {
                        snackbarController.show(
                            ErrorAppSnackBarData("No barcodes available to upload")
                        )
                    } else {
                        isUploadData.value = true
                    }
                },

                isSecondaryButton = true,

                modifier = Modifier.padding(dimensionResource(R.dimen.dp_16))
            )
        }
    }
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter
    ) {
        AppSnackBar(snackbarController)
    }
}

data class MovementScreenArgs @OptIn(ExperimentalMaterial3Api::class) constructor(
    val context: Context,
    val label: String,
    val menuCode: String,
    val transactionType: String,
    val scope: CoroutineScope,
    val sheetState: SheetState,
    val showSheet: MutableState<Boolean>,
    val currentSheet: MutableState<BottomSheetType>,
    val readerPower: Int,
    val setPower: MutableState<Int>
)

enum class FilterStep {
    NONE,
    SOURCE,
    DESTINATION
}

fun saveSourceLocation(menuCode: String, srcLocation: LocationModel?) {
    if (srcLocation == null) return
    val preHeader = TopicConstants.MOVEMENT + "_" + menuCode + "_"
    DataStoreManager.saveToPreferences(preHeader + ParameterConstants.ASSET_LOCATION_NAME, srcLocation.name)
    DataStoreManager.saveToPreferences(preHeader + ParameterConstants.ASSET_LOCATION_PATH, srcLocation.path)
}

fun saveDestinationLocation(menuCode: String, destLocation: LocationModel?) {
    if (destLocation == null) return
    val preHeader = TopicConstants.MOVEMENT + "_" + menuCode + "_"
    DataStoreManager.saveToPreferences(preHeader + ParameterConstants.MOVE_AT_ASSET_LOCATION_NAME, destLocation.name)
    DataStoreManager.saveToPreferences(preHeader + ParameterConstants.MOVE_AT_ASSET_LOCATION_PATH, destLocation.path)
}

fun clearSavedSessionValues(context: Context, menuCode: String, transactionType: String,isUpload: Boolean=false,  topic:String= TopicConstants.MOVEMENT) {
    LogUtils.showLog("clearSavedSessionValues","")
    val preHeader =  topic+ "_" + menuCode + "_"
    DataStoreManager.saveToPreferences(preHeader + ParameterConstants.ASSET_LOCATION_NAME, "")
    DataStoreManager.saveToPreferences(preHeader + ParameterConstants.ASSET_LOCATION_PATH, "")
    DataStoreManager.saveToPreferences(preHeader + ParameterConstants.MOVE_AT_ASSET_LOCATION_NAME, "")
    DataStoreManager.saveToPreferences(preHeader + ParameterConstants.MOVE_AT_ASSET_LOCATION_PATH, "")
    DataStoreManager.saveToPreferences(preHeader + ParameterConstants.DEVICE_SESSION_ID, "")
    DataStoreManager.saveToPreferences(preHeader + "sessionData", "")
    DataStoreManager.saveToPreferences(preHeader + ParameterConstants.READER_POWER, 7)

    CoroutineScope(Dispatchers.IO).launch {
        val db = AppDatabase.getDbInstance(context)
        if(isUpload) db.tagInfoDao().deleteBySessionTypeAndTransactionTypeAndMarkNotUploaded(menuCode, transactionType)
        //if(isDelete) db.tagInfoDao().deleteActual(TopicConstants.MOVEMENT,menuCode,transactionType)
        else db.tagInfoDao().deleteBySessionTypeAndTransactionType(menuCode, transactionType)
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovementContent(
    apiViewModel: ApiViewModel,
    readerViewModel: ReaderViewModel,
    navController: NavHostController,
    selectedSrcLocationName: MutableState<String>,
    locationList: SnapshotStateList<LocationModel>,
    selectedSrcLocationId: MutableState<LocationModel?>,
    selectedDestLocationId: MutableState<LocationModel?>,
    selectedDestLocationName: MutableState<String>,
    args: MovementScreenArgs,
    isDataUpload: MutableState<Boolean>,
    isOnDataUploaded: MutableState<Boolean>,
    isUploadData: MutableState<Boolean>,
    filterStep: MutableState<FilterStep>,
    isNewSession: MutableState<Boolean>,
    hasActiveSession: Boolean,
    optionPicker: MutableState<Boolean>,
    textValue: String,
    sourceZone: LocationModel?,
    barcodeList: State<List<TagInfoEntity>>
) {
    val db = AppDatabase.getDbInstance(args.context)
    //val strokeWidth = dimensionResource(R.dimen.dp_2)
    //val powerSheet = remember { mutableStateOf(false) }
    val isApiLoading = apiViewModel.isLoading.observeAsState()
    val showSelectionLocation = remember { mutableStateOf(true) }
    val triggerPressed = readerViewModel.isTriggerPressed().observeAsState(false)
    val snackbarController = remember { SnackbarController() }
    val error = readerViewModel.error().observeAsState()
    //val tagInfoData = readerViewModel.pickData().observeAsState()
    //val isPickOn = readerViewModel.isPickOn().observeAsState(false)
    val isProcessOn = readerViewModel.isProcessOn().observeAsState(false)

    val selectedBarcode = remember { mutableStateOf<TagInfoEntity?>(null) }
    val selectedSearchType = remember { mutableStateOf("") }

    val preHeader = TopicConstants.MOVEMENT + "_" + args.menuCode + "_"
    val deviceSessionId = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.DEVICE_SESSION_ID, "")
    //val hasActiveSession = deviceSessionId.isNotEmpty()
    val infoList = remember { mutableStateListOf<Pair<String, String>>() }
    val showImagePreview = remember { mutableStateOf(false) }
    val selectedPainter = remember { mutableStateOf<Painter?>(null) }
    val barcodeLabel = DataStoreManager.getBarcodeLabel()

    val displayDestName = if (selectedDestLocationName.value.isEmpty()) {
        "Select Destination"
    } else {
        selectedDestLocationName.value
    }

    /*LaunchedEffect(Unit) {
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

    LaunchedEffect(triggerPressed.value) {
        if (triggerPressed.value == true) {
            readerViewModel.setTriggerValue(false)
            LogUtils.showLog("TriggerPressed", "Trigger is pressed")
            if (chkTrue(isApiLoading.value)) return@LaunchedEffect
            //if (chkTrue(isProcessOn.value)) return@LaunchedEffect
            if(navController.currentBackStackEntry?.lifecycle?.currentState != Lifecycle.State.RESUMED) return@LaunchedEffect
            if(isDataUpload.value  || isUploadData.value || isOnDataUploaded.value) return@LaunchedEffect
            val preHeader = TopicConstants.MOVEMENT + "_" + args.menuCode + "_"
            val deviceId = DataStoreManager.readFromPreferences(
                preHeader + ParameterConstants.DEVICE_SESSION_ID,
                ""
            )
            if (selectedSrcLocationName.value.isNullOrEmpty()) {
                snackbarController.show(
                    ErrorAppSnackBarData(
                        args.context.getString(R.string.select_src_zone)
                    )
                )
            }
            else if (selectedDestLocationName.value.isNullOrEmpty()) {
                snackbarController.show(
                    ErrorAppSnackBarData(
                        args.context.getString(R.string.select_destination_zone)
                    )
                )
            }
            else if (deviceId.isNullOrEmpty() && selectedSrcLocationId.value?.path.equals(selectedDestLocationId.value?.path, true)) {
                snackbarController.show(
                    ErrorAppSnackBarData(
                        args.context.getString(R.string.source_and_destination_cannot_be_same)
                    )
                )
            }
            //write condition for search dialog
            else if (args.showSheet.value != false && args.currentSheet.value == BottomSheetType.SEARCH) {
                //Based on Selected Epc or TID
                if (selectedSearchType.value.isNotEmpty() && selectedBarcode.value != null)
                    readerViewModel.toggleSearch(if (selectedSearchType.value.equals("epc", true)) SearchTypeConstant.EPC else SearchTypeConstant.TID, if (selectedSearchType.value.equals("epc", true)) selectedBarcode.value!!.epc else selectedBarcode.value!!.tid)
            }
            else {
                if (chkTrue(isProcessOn.value)) return@LaunchedEffect
                if (args.showSheet.value != false && args.currentSheet.value != BottomSheetType.NONE) return@LaunchedEffect
                if (deviceId.isEmpty()) {
                    val deviceSessionId = SessionUtils.generateOfflineSessionId(args.menuCode, args.transactionType)
                    val sessionData = JSONObject()
                    sessionData.put(ParameterConstants.SESSION_ID,deviceSessionId)
                    sessionData.put(ParameterConstants.ID,"")
                    sessionData.put(ParameterConstants.TRANSACTION_TYPE,args.transactionType)
                    sessionData.put(ParameterConstants.ASSET_LOCATION_PATH,selectedSrcLocationId.value!!.path)
                    sessionData.put(ParameterConstants.MOVE_AT_ASSET_LOCATION_PATH,selectedDestLocationId.value!!.path)
                    DataStoreManager.saveToPreferences(preHeader + ParameterConstants.DEVICE_SESSION_ID, deviceSessionId)
                    DataStoreManager.saveToPreferences(preHeader + "sessionData", sessionData.toString())
                    saveSourceLocation(args.menuCode, selectedSrcLocationId.value)
                    saveDestinationLocation(args.menuCode, selectedDestLocationId.value)
                    readerViewModel.setSessionId(deviceSessionId,sessionData.toString())
                }
                readerViewModel.performPick(
                    barcode = textValue,
                    pickPower = args.setPower.value,
                    isPostPicked = false,
                    isSavePickedToDB = true,
                    isAllowNonEncodedTags = false,
                    isAllowDuplicateTagRePick = false,
                )
            }
        } else {
            LogUtils.showLog("TriggerPressed", "Trigger released")
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
            when (result.url) {
                UrlConstants.LOCATION_ZONES -> {
                    val responseArray = ParseUtils.extractJSONArray(
                        jsonResponse,
                        ParameterConstants.DATA,
                        JSONArray()
                    )
                    val dataList = mutableListOf<LocationModel>()
                    if (responseArray != null && responseArray.length() > 0) {
                        processChildren(responseArray, dataList)
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

                UrlConstants.PRODUCTS ->
                    handleProductDetails(
                        apiResult = result,
                        fieldList = infoList,
                    )
            }
        }
    }

    Box() {
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
                        if (chkTrue(isProcessOn.value) || chkTrue(isApiLoading.value)) return@TopBarContent
                        if(isUploadData.value) {isUploadData.value=false; return@TopBarContent}
                        args.scope.launch {
                            val preHeader = TopicConstants.MOVEMENT + "_" + args.menuCode + "_"
                            val deviceSessionId = DataStoreManager.readFromPreferences(
                                preHeader + ParameterConstants.DEVICE_SESSION_ID,
                                ""
                            )
                            if (deviceSessionId.isNullOrEmpty()) {
                                clearSavedSessionValues(
                                    args.context,
                                    args.menuCode,
                                    args.transactionType
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

            Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_16)))

            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {

                CounterText(
                    current = barcodeList.value.size,
                    total = "",
                    isLimitShow = false
                )

                Text(
                    text = stringResource(id = R.string.txt_total_qty),
                    style = CommonTypography.current.noteText,
                    color = BlackColor,
                    modifier = Modifier.padding(bottom = dimensionResource(R.dimen.dp_8))
                )

                sourceZone?.let { zone ->
                    if (zone.name.isNotEmpty()) {
                        Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_28)))

                        CommonTextField(
                            config = TextFieldConfig(
                                value = textValue,
                                onValueChange = {
                                },
                                label = barcodeLabel,
                                imeAction = ImeAction.Done,
                                isBarCode = true,
                                isTrillingIcon = false,
                                readOnly = true,
                                onImeAction = {},
                                onClick = {}
                            ),
                            modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.dp_16))
                        )
                    }
                }

                Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_16)))

               if (selectedSrcLocationName.value.isEmpty() && selectedDestLocationName.value.isEmpty()) {
                    Card(
                        modifier = Modifier
                            .width(dimensionResource(R.dimen.dp_116))
                            .height(dimensionResource(R.dimen.dp_44))
                            /* .shadow(
                                elevation = dimensionResource(R.dimen.dp_24),
                                shape = RoundedCornerShape(dimensionResource(R.dimen.dp_16)),
                                ambientColor = ShadowGray,
                                spotColor = ShadowGray
                            )*/
                            .clickable(
                                onClick = {
                                    args.scope.launch {
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
                        )
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
                                text = String.format(stringResource(R.string.select_zone)),
                                style = CommonTypography.current.noteText,
                                color = BlackColor
                            )
                            Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_10)))
                        }
                    }
               }
               else {
                    /**BasicTextField(
                        value = selectedSrcLocationName.value + " -  " + selectedDestLocationName.value,
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
                        decorationBox = {

                            Row(
                                modifier = Modifier.clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) {
                                    args.scope.launch {
                                        if (selectedSrcLocationName.value.isEmpty() &&
                                            selectedDestLocationName.value.isEmpty()
                                        ) {
                                            // fresh open
                                        }

                                        apiViewModel.callApi(
                                            url = UrlConstants.LOCATION_SUB_ZONES,
                                            appendData = DataStoreManager.readFromPreferences(
                                                LoginConstants.DEVICE_LOCATION_ID, ""
                                            )
                                        )

                                        optionPicker.value = true
                                    }
                                },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Source location icon
                                Icon(
                                    painter = painterResource(id = R.drawable.icon_location),
                                    contentDescription = null,
                                    modifier = Modifier.size(dimensionResource(R.dimen.dp_18)),
                                    tint = if (isOnDataUploaded.value) Green else Yellow
                                )

                                Spacer(Modifier.width(4.dp))

                                // Source text
                                Text(
                                    text = selectedSrcLocationName.value,
                                    style = CommonTypography.current.noteText,
                                    color = BlackColor
                                )

                                Spacer(Modifier.width(8.dp))

                                // Arrow
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    tint = Color(0xFFFFC107),
                                    modifier = Modifier.size(20.dp)
                                )

                                Spacer(Modifier.width(8.dp))

                                // Destination location icon
                                Icon(
                                    painter = painterResource(id = R.drawable.icon_location),
                                    contentDescription = null,
                                    modifier = Modifier.size(dimensionResource(R.dimen.dp_18)),
                                    tint = if (isOnDataUploaded.value) Green else Yellow
                                )

                                Spacer(Modifier.width(4.dp))

                                // Destination text
                                if (selectedDestLocationName.value.isNullOrEmpty()){
                                    Text(
                                        text = stringResource(R.string.select_zone),
                                        style = CommonTypography.current.noteText,
                                        color = BlackColor,
                                        modifier = Modifier.clickable(
                                            indication = null,
                                            interactionSource = remember { MutableInteractionSource() }
                                        ) {
                                            args.scope.launch {
                                                apiViewModel.callApi(
                                                    url = UrlConstants.LOCATION_SUB_ZONES,
                                                    appendData = DataStoreManager.readFromPreferences(
                                                        LoginConstants.DEVICE_LOCATION_ID, ""
                                                    )
                                                )
                                                optionPicker.value = true
                                            }
                                        }
                                    )
                                }else{

                                    Text(
                                        text = selectedDestLocationName.value,
                                        style = CommonTypography.current.noteText,
                                        color = BlackColor
                                    )
                                }
                                Spacer(Modifier.width(8.dp))
                                val preHeader = TopicConstants.MOVEMENT + "_" + args.menuCode + "_"
                                val deviceSessionId = DataStoreManager.readFromPreferences(
                                    preHeader + ParameterConstants.DEVICE_SESSION_ID, ""
                                )
                                val id = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.ID, "")
                                if (deviceSessionId.isNullOrEmpty() && id.isNullOrEmpty()) {
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
                                                    if (textValue.isNullOrEmpty()) {
                                                        selectedSrcLocationName.value = ""
                                                        selectedSrcLocationId.value = null

                                                        selectedDestLocationName.value = ""
                                                        selectedDestLocationId.value = null
                                                    } else {
                                                        selectedDestLocationName.value = ""
                                                        selectedDestLocationId.value = null
                                                    }
                                                    optionPicker.value = false
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

                   GernericBasicTextField(
                       sourceZone = selectedSrcLocationName.value,
                       destZone = selectedDestLocationName.value,
                       menuCode = args.menuCode,
                       value = selectedSrcLocationName.value + " - " + selectedDestLocationName.value,
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
                               optionPicker.value = true
                           }
                       },
                       destAction = {
                           args.scope.launch {
                               apiViewModel.callApi(
                                   url = UrlConstants.LOCATION_SUB_ZONES,
                                   appendData = DataStoreManager.readFromPreferences(
                                       LoginConstants.DEVICE_LOCATION_ID, ""
                                   )
                               )
                               optionPicker.value = true
                           }
                       },
                       clearAction = {
                           args.scope.launch {

                               if (textValue.isNullOrEmpty()) {
                                   selectedSrcLocationName.value = ""
                                   selectedSrcLocationId.value = null

                                   selectedDestLocationName.value = ""
                                   selectedDestLocationId.value = null
                               } else {
                                   selectedDestLocationName.value = ""
                                   selectedDestLocationId.value = null
                               }

                               optionPicker.value = false
                           }
                       },
                       isDestZone = true,
                       isSet = !(deviceSessionId.isNullOrEmpty())
                   )
               }

                Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_16)))

                LazyColumn( modifier = Modifier.weight(1f)) {
                    items(barcodeList.value) { tag ->
                        ScanItemCard(
                            number = tag.barcode ?: "",
                            date = tag.insertTime ?: "",
                            onInfoClick = {
                                selectedBarcode.value = tag
                                callProductDetails(
                                    barCodeNo = tag!!.barcode,
                                    apiViewModel = apiViewModel,
                                    tagInfoData = tag
                                )
                                openSheet(
                                    scope = args.scope,
                                    sheetState = args.sheetState,
                                    showSheet = args.showSheet,
                                    currentSheet = args.currentSheet,
                                    sheet = BottomSheetType.PRODUCT_DETAILS
                                )
                            },
                            onSearchClick = {
                                selectedBarcode.value = tag
                                openSheet(
                                    scope = args.scope,
                                    sheetState = args.sheetState,
                                    showSheet = args.showSheet,
                                    currentSheet = args.currentSheet,
                                    sheet = BottomSheetType.SEARCH
                                )
                            },
                            onDeleteClick = {
                                selectedBarcode.value = tag
                                openSheet(
                                    scope = args.scope,
                                    sheetState = args.sheetState,
                                    showSheet = args.showSheet,
                                    currentSheet = args.currentSheet,
                                    sheet = BottomSheetType.CONFIRM_DELETE
                                )
                            }
                        )
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
                    BottomSheetType.POWER -> Box(
                        modifier = Modifier
                            .background(WhiteColor)
                            .fillMaxWidth()
                            .fillMaxHeight(0.50f)
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
                            }
                        )
                    }

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
                            .fillMaxHeight(0.35f)
                    ) {
                        SessionContent(
                            showSheet = args.showSheet,
                            sheetState = args.sheetState,
                            scope = args.scope,
                            navController = navController,
                            tagCount = barcodeList.value.size,
                            onStopSession = {
                                if (chkTrue(isProcessOn.value) || chkTrue(isApiLoading.value)) return@SessionContent
                                args.scope.launch {
                                    clearSavedSessionValues(args.context,args.menuCode,args.transactionType)
                                    if(navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                                        readerViewModel.clearSessionAndTransactionType()
                                        navController.popBackStack()}
                                }
                            },
                            readerViewModel = readerViewModel,
                            isAllowContinue = false
                        )
                    }

                    BottomSheetType.PRODUCT_DETAILS -> Box(
                        modifier = Modifier
                            .background(WhiteColor)
                            .fillMaxWidth()
                            .fillMaxHeight(0.55f)
                    ) {
                        selectedBarcode.value?.let { tagInfo ->
                            ProductDetailsByEan(
                                tagTime = TagTime(tagInfo.pkId,tagInfo.barcode,tagInfo.epc,tagInfo.tid,tagInfo.insertTime,tagInfo.tagVerifyStatus),
                                infoList = infoList,
                                title = stringResource(R.string.product_details_),
                                sheetState = args.sheetState,
                                scope = args.scope,
                                showSheet = args.showSheet,
                                isTrillingIcon = false,
                                showImagePreview = showImagePreview,
                                selectedPainter = selectedPainter,
                                readerViewModel = readerViewModel
                            )
                        }
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
                            readerViewModel = readerViewModel,
                            tagInfoEntity = selectedBarcode
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
                            val formatedText = buildAnnotatedString {
                                append(stringResource(R.string.total_scanned_items))
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("${barcodeList.value.size}")
                                }
                            }
                            UploadDataSheet(
                                title = args.label,//stringResource(R.string.item_movement,args.label),
                                //heading = stringResource(R.string.confirm_movement,args.label),
                                subHeading = formatedText,
                                //primaryButtonText = stringResource(R.string.confirm_and_move,args.label),
                                showSheet = args.showSheet,
                                sheetState = args.sheetState,
                                scope = args.scope,
                                tagCount = barcodeList.value.size,
                                isDataUploaded = isDataUpload
                            )
                        }

                    BottomSheetType.CONFIRM_DELETE ->
                        Box(
                            modifier = Modifier
                                .background(WhiteColor)
                                .fillMaxWidth()
                                .fillMaxHeight(0.47f)
                        ) {
                            val item = selectedBarcode.value
                            item?.let {

                                val formatedText = buildAnnotatedString {
                                    append(stringResource(R.string.confirm_deletion_msg, it.barcode))
                                }

                                AcceptDataSheet(
                                    title = stringResource(R.string.delete_item),
                                    heading = stringResource(R.string.confirm_deletion),
                                    subHeading = formatedText,
                                    primaryButtonText = stringResource(R.string.txt_confirm_and_delete),
                                    showSheet = args.showSheet,
                                    sheetState = args.sheetState,
                                    scope = args.scope,
                                    icon = R.drawable.property_know_more,
                                    color1 = Color(0xFFF3B100),
                                    color2 = Color(0xFFFECF53),
                                    onClick = {
                                        args.scope.launch(Dispatchers.IO) {
                                            db.tagInfoDao().delete(it)
                                        }
                                    }
                                )
                            }
                        }

                    BottomSheetType.SETTINGS -> Box(
                        modifier = Modifier
                            .background(WhiteColor)
                            .fillMaxWidth()
                            .fillMaxHeight(0.37f)
                    ) {
                        DeviceSettings(
                            showSheet = args.showSheet,
                            sheetState = args.sheetState,
                            scope = args.scope,
                            currentSheet = args.currentSheet
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

@Composable
fun ScanItemCard(
    number: String,
    date: String,
    onInfoClick: () -> Unit,
    onSearchClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BackGround)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = number,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF2B2B2B)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = DateFormatUtils.formatToDisplayTime(date),//date,
                    fontSize = 12.sp,
                    color = Color(0xFF9E9E9E)
                )
            }

            ActionCircleButton(
                painter = painterResource(id = R.drawable.property_know_more),
                onClick = onInfoClick
            )

            Spacer(modifier = Modifier.width(10.dp))

            ActionCircleButton(
                painter = painterResource(id = R.drawable.property_gieger_og),
                onClick = onSearchClick
            )

            Spacer(modifier = Modifier.width(10.dp))

            ActionCircleButton(
                painter = painterResource(id = R.drawable.property_1_delete),
                onClick = onDeleteClick
            )
        }

        Divider(
            color = Color(0xFFEAEAEA),
            thickness = 1.dp
        )
    }
}

@Composable
fun ActionCircleButton(
    painter: Painter,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(Color(0xFFF2F2F2))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painter,
            contentDescription = null,
            tint = Color.Black,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun OptionPicker(
    initialSource: LocationModel? = null,
    initialDestination: LocationModel? = null,
    isDisableSourceSelection:Boolean = false,
    isDisableDestinationSelection:Boolean = false,
    onDismiss: () -> Unit,
    locationList: List<LocationModel>,
    onFilterApplied: (LocationModel?, LocationModel?) -> Unit,
    args: MovementScreenArgs
) {
    val searchQuery = remember { mutableStateOf("") }
    val activePicker = remember { mutableStateOf(FilterStep.NONE) }

    val selectedSourceZone = remember { mutableStateOf(initialSource) }
    val selectedDestinationZone = remember { mutableStateOf(initialDestination) }

    val snackbarController = remember { SnackbarController() }


    val filteredLocations = if (searchQuery.value.isEmpty()) {
        locationList
    } else {
        locationList.filter {
            it.name.contains(searchQuery.value, ignoreCase = true)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.35f))
            .padding(top = 44.dp)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                activePicker.value = FilterStep.NONE
                searchQuery.value = ""
            },
        contentAlignment = Alignment.BottomCenter
    ) {

        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            /* -------------------- SOURCE PICKER -------------------- */
            if (activePicker.value == FilterStep.SOURCE) {

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

                        Text(
                            text = stringResource(R.string.source_zone),
                            style = CommonTypography.current.mediumText
                        )

                        Spacer(Modifier.height(8.dp))

                        SearchBar(
                            searchQuery = searchQuery.value,
                            onQueryChange = { searchQuery.value = it },
                            placeholder = stringResource(R.string.search_location)
                        )

                        Spacer(Modifier.height(12.dp))

                        LazyColumn {
                            items(filteredLocations) { location ->
                                LocationRow(
                                    location = location.name,
                                    onClick = {
                                        selectedSourceZone.value = location
                                        activePicker.value = FilterStep.NONE
                                        searchQuery.value = ""
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
            }

            /* -------------------- SOURCE ROW -------------------- */
            if (activePicker.value != FilterStep.SOURCE) {
                PickerRow(
                    title = stringResource(R.string.source_zone),
                    value = selectedSourceZone.value?.name,
                    onClick = {
                        if(isDisableSourceSelection) return@PickerRow
                        searchQuery.value = ""
                        activePicker.value = FilterStep.SOURCE
                    }
                )
            }

            Spacer(Modifier.height(14.dp))

            /* -------------------- DESTINATION PICKER -------------------- */
            if (activePicker.value == FilterStep.DESTINATION) {

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

                        Text(
                            text = stringResource(R.string.destination_zone),
                            style = CommonTypography.current.mediumText
                        )

                        Spacer(Modifier.height(8.dp))

                        SearchBar(
                            searchQuery = searchQuery.value,
                            onQueryChange = { searchQuery.value = it },
                            placeholder = stringResource(R.string.search_location)
                        )

                        Spacer(Modifier.height(12.dp))

                        LazyColumn {
                            items(filteredLocations) { location ->
                                LocationRow(
                                    location = location.name,
                                    onClick = {
                                        selectedDestinationZone.value = location
                                        activePicker.value = FilterStep.NONE
                                        searchQuery.value = ""
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
            }

            /* -------------------- DESTINATION ROW -------------------- */
            if (activePicker.value != FilterStep.DESTINATION) {
                PickerRow(
                    title = stringResource(R.string.destination_zone),
                    value = selectedDestinationZone.value?.name,
                    onClick = {
                        if(isDisableDestinationSelection) return@PickerRow
                        searchQuery.value = ""
                        activePicker.value = FilterStep.DESTINATION
                    }
                )
            }

          //  Spacer(Modifier.height(12.dp))

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
                    Row(modifier = Modifier.heightIn(dimensionResource(R.dimen.dp_48))) {
                        CommonButton(
                            text = stringResource(id = R.string.apply_filter),
                            onClick = {
                                val source = selectedSourceZone.value
                                val destination = selectedDestinationZone.value
                                if (source?.id == destination?.id) {
                                    snackbarController.show(
                                        ErrorAppSnackBarData(args.context.getString(R.string.source_and_destination_cannot_be_same))
                                    )
                                    return@CommonButton
                                }

                                onFilterApplied(source, destination)
                                onDismiss()
                            },
                            modifier = Modifier.weight(1f),
                            enabled =
                                selectedSourceZone.value != null &&
                                        selectedDestinationZone.value != null
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

                    Row(modifier = Modifier.heightIn(dimensionResource(R.dimen.dp_48))) {
                        CommonButton(
                            text = stringResource(id = R.string.cancel),
                            onClick = {
                                selectedSourceZone.value = null
                                selectedDestinationZone.value = null
                                onDismiss()
                            },
                            modifier = Modifier.weight(1f),
                            gradientBrush = SolidColor(WhiteColor),
                            contentColor = RedColor
                        )
                    }
                }

            }

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.BottomCenter
            ) {
                AppSnackBar(snackbarController)
            }

           // Spacer(Modifier.height(14.dp))
        }
    }
}

@Composable
fun PickerRow(
    title: String,
    value: String?,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth(0.92f)
            .height(48.dp)
            .background(WhiteColor, RoundedCornerShape(24.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = title,
                style = CommonTypography.current.noteText,
                modifier = Modifier.weight(1f) // pushes action to right
            )

            if (value.isNullOrEmpty()) {
                IconText(
                    icon = painterResource(R.drawable.property_add),
                    actionText = stringResource(R.string.add_,title),
                    tintColor = BlackColor,
                    onActionClick = onClick,
                    elevation = 0.dp
                )
            } else {
                RowText(
                    actionText = value,
                    onActionClick = onClick
                )
            }
        }
    }
}








