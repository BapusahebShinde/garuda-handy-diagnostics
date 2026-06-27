package com.itek.rftaar.presentation.encoding

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController
import com.itek.rftaar.R
import com.itek.rftaar.ReaderActivity
import com.itek.rftaar.api.ApiResult
import com.itek.rftaar.api.constants.ParameterConstants
import com.itek.rftaar.api.constants.UrlConstants
import com.itek.rftaar.core.common.constants.DateFormatUtils
import com.itek.rftaar.core.common.constants.LoginConstants
import com.itek.rftaar.core.common.constants.MenuConstants
import com.itek.rftaar.core.common.utils.LogUtils
import com.itek.rftaar.core.common.utils.ParseUtils.extractJSONArray
import com.itek.rftaar.core.common.utils.ParseUtils.extractJSONObject
import com.itek.rftaar.core.common.utils.ParseUtils.extractString
import com.itek.rftaar.core.database.AppDatabase
import com.itek.rftaar.core.database.DataStoreManager
import com.itek.rftaar.data.entity.TagInfoEntity
import com.itek.rftaar.data.model.TagTime
import com.itek.rftaar.mqtt.constants.SearchTypeConstant
import com.itek.rftaar.mqtt.constants.TopicConstants
import com.itek.rftaar.presentation.commonComp.BottomSheetTextIconRow
import com.itek.rftaar.presentation.commonComp.BottomSheetType
import com.itek.rftaar.presentation.commonComp.CardViewWithHSI
import com.itek.rftaar.presentation.commonComp.CommonButton
import com.itek.rftaar.presentation.commonComp.CommonTextField
import com.itek.rftaar.presentation.commonComp.CounterText
import com.itek.rftaar.presentation.commonComp.DeviceSettingItem
import com.itek.rftaar.presentation.commonComp.DirectionProximityGauge
import com.itek.rftaar.presentation.commonComp.IconText
import com.itek.rftaar.presentation.commonComp.ItekFooter
import com.itek.rftaar.presentation.commonComp.SessionBottomSheet
import com.itek.rftaar.presentation.commonComp.SessionUiActions
import com.itek.rftaar.presentation.commonComp.SessionUiState
import com.itek.rftaar.presentation.commonComp.SetValueActions
import com.itek.rftaar.presentation.commonComp.SetValueContent
import com.itek.rftaar.presentation.commonComp.SetValueUiConfig
import com.itek.rftaar.presentation.commonComp.StepIndicatorRow
import com.itek.rftaar.presentation.commonComp.StepItem
import com.itek.rftaar.presentation.commonComp.StepUiState
import com.itek.rftaar.presentation.commonComp.TextFieldConfig
import com.itek.rftaar.presentation.commonComp.TopBarContent
import com.itek.rftaar.presentation.commonComp.UploadStatusChip
import com.itek.rftaar.presentation.commonComp.messageForStep
import com.itek.rftaar.presentation.commonComp.openSheet
import com.itek.rftaar.presentation.commonComp.statusForState
import com.itek.rftaar.presentation.decoding.ProductDetailsByEan
import com.itek.rftaar.presentation.decoding.handleProductDetails
import com.itek.rftaar.presentation.inward.SegmentedTabView
import com.itek.rftaar.presentation.navigation.Screen
import com.itek.rftaar.presentation.viewmodel.ApiViewModel
import com.itek.rftaar.presentation.viewmodel.ReaderViewModel
import com.itek.rftaar.ui.theme.BlackColor
import com.itek.rftaar.ui.theme.ButtonGray
import com.itek.rftaar.ui.theme.CommonTypography
import com.itek.rftaar.ui.theme.OutlineDefault
import com.itek.rftaar.ui.theme.TextGrey
import com.itek.rftaar.ui.theme.WhiteColor
import com.itek.rftaar.ui.theme.Yellow
import com.itek.rftaar.utils.CommonUtils.chkNull
import com.itek.rftaar.utils.CommonUtils.chkTrue
import com.itek.rftaar.utils.NetworkUtils.isInternetConnected
import com.itek.rftaar.utils.SessionUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SingleEncoding(
    modifier: Modifier,
    navController: NavHostController,
    label: String,
    menuCode: String,
    apiViewModel: ApiViewModel = hiltViewModel(),
    searchParams: Map<String, Any> = emptyMap(),
) {
    val scope = rememberCoroutineScope()
    val preHeader = TopicConstants.ENCODE + "_" + menuCode + "_"
    val transactionType = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.TRANSACTION_TYPE, "")
    val isScanScanEncode =  menuCode.equals(MenuConstants.SCAN_SCAN_ENCODE,true)
    val isMultiEncode =  !isScanScanEncode && menuCode.equals(MenuConstants.BULK_ENCODE,true) || menuCode.equals(MenuConstants.MULTI_ENCODE,true)
    LogUtils.showLog("isScanScanEncode",""+isScanScanEncode)
    LogUtils.showLog("isMultiEncode",""+isMultiEncode)
    val barCodeVal = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.BARCODE, "")
    val tidCodeVal = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.TID, "")
    val barCodeNo = remember { mutableStateOf(barCodeVal) }
    val tidCodeNo = remember { mutableStateOf(tidCodeVal) }
    val isTidCodeConfirmed = remember { mutableStateOf(false) }
    val context = LocalContext.current
    val activity = context as? ReaderActivity
    val readerViewModel = activity?.findReaderViewModel() ?: hiltViewModel()
    if (activity != null) {
        readerViewModel.onCreate()
        readerViewModel.setSessionAndTransactionType(menuCode,topic= TopicConstants.ENCODE)
    }
    val isProcessOn = readerViewModel.isProcessOn().observeAsState()

    val limit = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.LIMIT, 0)
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
    val selectedLimit = rememberSaveable { mutableStateOf(limit) }

    readerViewModel.setPower(setPower.value)

    val db = AppDatabase.getDbInstance(context)
    val tagCount = db.tagInfoDao().getTagWriteCount(menuCode, transactionType).observeAsState(0)
    val tagVerifyCount = db.tagInfoDao().getTagWriteVerifiedCount(menuCode, transactionType).observeAsState(0)
    val tagUploadedEncodedCount = db.tagInfoDao().getUploadedTagWriteCount(menuCode, transactionType).observeAsState(initial = 0)
    val latestEan = db.tagInfoDao().getLatestEncodedTag(menuCode, transactionType).collectAsState(initial = null)

    BackHandler(enabled = true) {
        scope.launch {
            val preHeader = TopicConstants.ENCODE + "_" + menuCode + "_"
            val deviceSessionId = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.DEVICE_SESSION_ID, "")
            val id = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.ID, "")
            if (chkTrue(isProcessOn.value)) return@launch
            if (deviceSessionId.isEmpty() || id.isEmpty()) {
                clearSavedSessionValues(context, menuCode, transactionType)
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

    Box {
        Scaffold(
            bottomBar = {
                Box(
                    modifier = modifier
                        .fillMaxWidth()
                        .background(WhiteColor),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    SingleEncodingBottomBar(
                        isScanScanEncode,
                        isMultiEncode,
                        barCodeNo,
                        tidCodeNo,
                        apiViewModel,
                        readerViewModel,
                        menuCode,
                        context,
                        navController,
                        transactionType,
                        setPower,
                        selectedLimit,
                        tagCount,
                        tagVerifyCount
                    )
                }
            }) { innerPadding ->

            Box(
                modifier = modifier
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
                val encodingArgs = EncodingScreenArgs(
                    context = context,
                    label = label,
                    menuCode = menuCode,
                    transactionType = transactionType,
                    scope = scope,
                    sheetState = sheetState,
                    showSheet = showSheet,
                    currentSheet = currentSheet,
                    tagCount = tagCount,
                    tagVerifyCount = tagVerifyCount,
                    tagUploadedEncodedCount = tagUploadedEncodedCount,
                    latestEan = latestEan,
                    setPower = setPower,
                    selectedLimit = selectedLimit,
                    limit = limit
                )

                SingleEncodingContent(
                    navController = navController,
                    isScanScanEncode = isScanScanEncode,
                    isMultiEncode = isMultiEncode,
                    barCodeNo = barCodeNo,
                    tidCodeNo = tidCodeNo,
                    isTidCodeConfirmed = isTidCodeConfirmed,
                    apiViewModel = apiViewModel,
                    readerViewModel = readerViewModel,
                    args = encodingArgs
                )
            }
        }
    }
}

data class EncodingScreenArgs @OptIn(ExperimentalMaterial3Api::class) constructor(
    val context: Context,
    val label: String,
    val menuCode: String,
    val transactionType: String,
    val scope: CoroutineScope,
    val sheetState: SheetState,
    val showSheet: MutableState<Boolean>,
    val currentSheet: MutableState<BottomSheetType>,
    val tagCount: State<Int>,
    val setPower: MutableState<Int>,
    val selectedLimit: MutableState<Int>,
    val limit: Int,
    val tagVerifyCount: State<Int>,
    val tagUploadedEncodedCount: State<Int>,
    val latestEan: State<TagTime?>
)


@SuppressLint("SuspiciousIndentation")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SingleEncodingContent(
    navController: NavHostController,
    isScanScanEncode: Boolean,
    isMultiEncode: Boolean,
    barCodeNo: MutableState<String>,
    tidCodeNo: MutableState<String>,
    isTidCodeConfirmed: MutableState<Boolean>,
    apiViewModel: ApiViewModel,
    readerViewModel: ReaderViewModel,
    args: EncodingScreenArgs,
) {
    val preHeader = TopicConstants.ENCODE + "_" + args.menuCode + "_"

    val isBarcodeScan = readerViewModel.isBarcodeOn().observeAsState(false)
    val scannedValue = readerViewModel.barcodeData().observeAsState("")
    val isPickOn = readerViewModel.isPickOn().observeAsState(false)
    val tagInfoData = readerViewModel.pickData().observeAsState()
    val isTagWriteDone = readerViewModel.isTagWriteDone().observeAsState(false)
    LogUtils.showLog("Val_isTagWriteDone", "--" + isTagWriteDone.value)
    val isTagWriteOn = readerViewModel.isTagWriteOn().observeAsState(false)
    LogUtils.showLog("Val_isTagWriteOn", "--" + isTagWriteOn.value)
    val isProcessOn = readerViewModel.isProcessOn().observeAsState(false)
    LogUtils.showLog("Val_isProcessOn", "--" + isProcessOn.value)
    val triggerPressed = readerViewModel.isTriggerPressed().observeAsState(false)

    val errorMessage = readerViewModel.error().observeAsState("")
    LogUtils.showLog("Val_errorMessage", "--" + errorMessage.value)
    val apiError = apiViewModel.apiErrorMsg.observeAsState("")
    val isApiLoading = apiViewModel.isLoading.observeAsState(false)

    val focusManager = LocalFocusManager.current

    //val db = AppDatabase.getDbInstance(args.context)
    //val tagEncodedCount = db.tagInfoDao().getTagWriteCount(args.menuCode, args.transactionType).observeAsState(initial = 0)
    //val tagUploadedEncodedCount = db.tagInfoDao().getUploadedTagWriteCount(args.menuCode, args.transactionType).observeAsState(initial = 0)
    //val latestEan = db.tagInfoDao().getLatestEncodedTag(args.menuCode, args.transactionType).collectAsState(initial = null)
    val selectedSearchType = remember { mutableStateOf("") }
    LogUtils.showLog("triggerPressed", "SingleEncodingContent: ${triggerPressed.value}")
    LogUtils.showLog("tagInfoData", "SingleEncodingContent: ${tagInfoData.value}")
    val showImagePreview = remember { mutableStateOf(false) }
    val selectedPainter = remember { mutableStateOf<Painter?>(null) }
    val infoList = remember { mutableStateListOf<Pair<String, String>>() }
    val imeVisible = WindowInsets.isImeVisible


    val msgInvalidScan = stringResource(R.string.err_invalid_scan)

    LaunchedEffect(tidCodeNo.value) {
        if (tidCodeNo.value.isNullOrEmpty()) isTidCodeConfirmed.value = false
    }


    LaunchedEffect(triggerPressed.value) {
        //val deviceId = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.DEVICE_SESSION_ID, "")
        if (!triggerPressed.value) return@LaunchedEffect
        readerViewModel.setTriggerValue(false)
        focusManager.clearFocus()
        if (chkTrue(isApiLoading.value)) return@LaunchedEffect
        if (navController.currentBackStackEntry?.lifecycle?.currentState != Lifecycle.State.RESUMED) return@LaunchedEffect
        //write condition for search dialog
        if (args.showSheet.value != false && args.currentSheet.value == BottomSheetType.SEARCH) {
            //Based on Selected Epc or TID
            if (selectedSearchType.value.isNotEmpty() && args.latestEan.value != null) readerViewModel.toggleSearch(
                if (selectedSearchType.value.equals(
                        "epc", true
                    )
                ) SearchTypeConstant.EPC else SearchTypeConstant.TID,
                if (selectedSearchType.value.equals(
                        "epc", true
                    )
                ) args.latestEan.value!!.epc else args.latestEan.value!!.tid
            )
//        readerViewModel.toggleSearch(selectedSearchType.value,if(selectedSearchType.value.equals("epc",true)) latestEan.value!!.epc else latestEan.value!!.tid)
        }
        else {
            if (chkTrue(isProcessOn.value)) return@LaunchedEffect
            if (args.showSheet.value != false && args.currentSheet.value != BottomSheetType.NONE) return@LaunchedEffect
            //Code for Auto clearing
            /*{
              args.showSheet.value=false
              args.currentSheet.value = BottomSheetType.NONE
            }*/
            if (barCodeNo.value.isNullOrEmpty()) readerViewModel.scanBarcode()
            else if (!isScanScanEncode) readerViewModel.performPick(
                pickPower = args.setPower.value, pickTime = 500
            )
            else if (isScanScanEncode && tidCodeNo.value.isNullOrEmpty()) readerViewModel.scanBarcode(SearchTypeConstant.TID)
            else if (isScanScanEncode && tidCodeNo.value.isNotEmpty()) {
                if (tidCodeNo.value.matches(Regex("[0-9A-Fa-f]{8,}")) && tidCodeNo.value.length % 4 == 0) {
                    if (!isTidCodeConfirmed.value) isTidCodeConfirmed.value = true
                    callEpcForEncoding(
                        barCodeNo, tidCodeNo, apiViewModel, args.context, tagInfoData.value, args
                    )
                } else readerViewModel.error()
                    .postValue(String.format(msgInvalidScan, "tid", tidCodeNo.value))
            }
        }
    }

    LaunchedEffect(scannedValue.value) {
        if (chkNull(scannedValue.value, "").isNotEmpty()) {
            if (isScanScanEncode && barCodeNo.value.isNotEmpty()) {
                if (!(chkNull(scannedValue.value, "").matches(Regex("[0-9A-Fa-f]{8,}")) && chkNull(
                        scannedValue.value, ""
                    ).length % 4 == 0)
                ) {
                    readerViewModel.error()
                        .postValue(String.format(msgInvalidScan, "tid", scannedValue.value))
                    return@LaunchedEffect
                }
                tidCodeNo.value = chkNull(scannedValue.value, "")
                LogUtils.showLog("scannedTidValue", "--" + tidCodeNo.value)
                isTidCodeConfirmed.value = true
                callEpcForEncoding(
                    barCodeNo, tidCodeNo, apiViewModel, args.context, tagInfoData.value, args
                )
            } else barCodeNo.value = chkNull(scannedValue.value, "")

            //if (tagInfoData.value != null && barCodeNo.value.isNotEmpty() && (!isScanScanEncode || tidCodeNo.value.isNotEmpty())) {
            if (tagInfoData.value != null && barCodeNo.value.isNotEmpty() && !isScanScanEncode) {
                callEpcForEncoding(
                    barCodeNo, tidCodeNo, apiViewModel, args.context, tagInfoData.value, args
                )
            }
            //else if (barCodeNo.value.isNotEmpty() && (!isScanScanEncode || tidCodeNo.value.isNotEmpty())) {
            else if (barCodeNo.value.isNotEmpty() && !isScanScanEncode) {
                readerViewModel.performPick(pickPower = args.setPower.value, pickTime = 500)
            }
        }
    }

    LaunchedEffect(tagInfoData.value) {
        LogUtils.showLog("tagInfoData", "" + (tagInfoData.value != null))
        if (tagInfoData.value != null && barCodeNo.value.isNotEmpty() && (!isScanScanEncode || tidCodeNo.value.isNotEmpty())) {
            callEpcForEncoding(
                barCodeNo, tidCodeNo, apiViewModel, args.context, tagInfoData.value, args
            )
            //rfidStarted.value = false
        }
    }

    LaunchedEffect(isTagWriteDone.value) {
        if (isTagWriteDone.value == true && !isMultiEncode) {
            barCodeNo.value = ""
            if (isScanScanEncode) tidCodeNo.value = ""
        }
    }
    val response = apiViewModel.apiResult.collectAsState(null)
    LaunchedEffect(response.value) {
        if(response.value==null) return@LaunchedEffect
        val result = response.value!!
            if (!result.isSuccess) {
             LogUtils.showLog("TAG", "SingleEncodingBottomBar: "+result.errMsg)
            }
            else if (response.value != null) {
                when (result.url) {
                    UrlConstants.ENCODING -> handleEncode(result, tagInfoData.value, readerViewModel, args.menuCode, args.selectedLimit)
                    UrlConstants.PRODUCTS ->
                        handleProductDetails(
                            apiResult = result,
                            fieldList = infoList
                        )
                }
            }
    }



    val barcodeStepState = remember {
        derivedStateOf {
            when {
                barCodeNo.value.isNotEmpty() || isTagWriteDone.value == true -> StepUiState.COMPLETED
                isBarcodeScan.value == true -> StepUiState.ACTIVE
                !isBarcodeScan.value && barCodeNo.value.isEmpty() && errorMessage.value.isNotEmpty() -> StepUiState.ERROR
                else -> StepUiState.IDLE
            }
        }
    }

    val tidScanStepState = remember {
        derivedStateOf {
            when {
                !isScanScanEncode && barCodeNo.value.isNullOrEmpty() -> StepUiState.IDLE
                (tidCodeNo.value.isNotEmpty() && isTidCodeConfirmed.value == true) || isTagWriteDone.value == true -> StepUiState.COMPLETED
                barCodeNo.value.isNotEmpty() && tidCodeNo.value.isNullOrEmpty() && isBarcodeScan.value == true -> StepUiState.ACTIVE
                barCodeNo.value.isNotEmpty() && !isBarcodeScan.value && (tidCodeNo.value.isNullOrEmpty() || !isTidCodeConfirmed.value) && errorMessage.value.isNotEmpty() -> StepUiState.ERROR
                else -> StepUiState.IDLE
            }
        }
    }

    val rfidStepState = remember {
        derivedStateOf {
            when {
                //barcodeStepState.value != StepUiState.COMPLETED -> StepUiState.IDLE
                tagInfoData.value != null || isTagWriteDone.value == true -> StepUiState.COMPLETED
                isPickOn.value == true -> StepUiState.ACTIVE
                !isPickOn.value && barCodeNo.value.isNotEmpty() && tagInfoData.value == null && errorMessage.value.isNotEmpty() -> StepUiState.ERROR
                else -> StepUiState.IDLE
            }
        }
    }

    val encodeStepState = remember {
        derivedStateOf {
            when {
                barcodeStepState.value != StepUiState.COMPLETED || (isScanScanEncode && tidScanStepState.value != StepUiState.COMPLETED) || (!isScanScanEncode && rfidStepState.value != StepUiState.COMPLETED) -> StepUiState.IDLE
                isTagWriteDone.value == true -> StepUiState.COMPLETED
                isTagWriteOn.value == true -> StepUiState.ACTIVE
                !isTagWriteOn.value && barCodeNo.value.isNotEmpty() && ((isScanScanEncode && tidCodeNo.value.isNotEmpty()) || (!isScanScanEncode && tagInfoData != null)) && (errorMessage.value.isNotEmpty() || apiError.value.isNotEmpty()) -> StepUiState.ERROR
                //!isTagWriteOn.value && barCodeNo.value.isNotEmpty() && tagInfoData != null && (errorMessage.value.isNotEmpty() || apiError.value.isNotEmpty()) -> StepUiState.ERROR
                else -> StepUiState.IDLE
            }
        }
    }

    val steps = if (isScanScanEncode) listOf(
        StepItem(
            icon = R.drawable.property_barcode,
            label = DataStoreManager.getBarcodeLabel(),
            status = statusForState(barcodeStepState.value)
        ), StepItem(
            icon = R.drawable.property_qr,
            label = "TID",
            status = statusForState(tidScanStepState.value)
        ), StepItem(
            icon = R.drawable.property_rfid_encoded,
            label = "Encode",
            status = statusForState(encodeStepState.value)
        )
    )
    else listOf(
        StepItem(
            icon = R.drawable.property_barcode,
            label = DataStoreManager.getBarcodeLabel(),
            status = statusForState(barcodeStepState.value)
        ), StepItem(
            icon = R.drawable.property_rfidd,
            label = "RFID",
            status = statusForState(rfidStepState.value)
        ), StepItem(
            icon = R.drawable.property_rfid_encoded,
            label = "Encode",
            status = statusForState(encodeStepState.value)
        )
    )


    /* -------------------- MESSAGES (NO remember/mutableState) -------------------- */

    val messageHeading: String = when {

        encodeStepState.value != StepUiState.IDLE -> messageForStep(
            encodeStepState.value,
            active = "Processing...",
            completed = "RFID Encoded Successfully",
            error = chkNull(errorMessage.value, apiError.value)
        ) ?: ""

        tidScanStepState.value != StepUiState.IDLE -> messageForStep(
            tidScanStepState.value,
            active = "Processing...",
            completed = "Tid Scan Done",
            error = errorMessage.value
        ) ?: ""

        rfidStepState.value != StepUiState.IDLE -> messageForStep(
            rfidStepState.value,
            active = "Processing...",
            completed = "RFID Scan Done",
            error = errorMessage.value
        ) ?: ""

        barcodeStepState.value != StepUiState.IDLE -> messageForStep(
            barcodeStepState.value,
            active = "Processing...",
            completed = DataStoreManager.getBarcodeLabel() + " Scan Done",
            error = errorMessage.value
        ) ?: ""

        else -> ""
    }

    /** val messageSubHeading: String = when {
    encodeStepState.value == StepUiState.ERROR -> chkNull(errorMessage.value, apiError.value)

    rfidStepState.value == StepUiState.ERROR -> errorMessage.value

    barcodeStepState.value == StepUiState.ERROR -> errorMessage.value

    encodeStepState.value == StepUiState.ACTIVE -> "Please keep the device steady"

    rfidStepState.value == StepUiState.ACTIVE -> "Bring RFID tag near the reader"

    barcodeStepState.value == StepUiState.ACTIVE -> stringResource(R.string.scanning_barcode)

    encodeStepState.value == StepUiState.COMPLETED -> stringResource(R.string.encoded_successfully)

    rfidStepState.value == StepUiState.COMPLETED -> latestEan.value?.timeStamp.orEmpty()

    barcodeStepState.value == StepUiState.COMPLETED -> "Barcode scanned successfully"

    else -> ""
    }*/


    Box(modifier = Modifier.fillMaxSize().imePadding()) {
        Column(
            modifier = Modifier.imePadding().fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(modifier = Modifier, verticalArrangement = Arrangement.Top) {
                TopBarContent(args.label, onBackClickL = {
                    args.scope.launch {
                        val preHeader = TopicConstants.ENCODE + "_" + args.menuCode + "_"
                        val deviceSessionId = DataStoreManager.readFromPreferences(
                            preHeader + ParameterConstants.DEVICE_SESSION_ID, ""
                        )
                        val id = DataStoreManager.readFromPreferences(
                            preHeader + ParameterConstants.ID, ""
                        )
                        if (chkTrue(isProcessOn.value)) return@launch
                        if (deviceSessionId.isEmpty() || id.isEmpty()) {
                            clearSavedSessionValues(args.context, args.menuCode, args.transactionType)
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
                })
            }
            HorizontalDivider(thickness = dimensionResource(R.dimen.dp_1), color = OutlineDefault)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (args.tagCount.value > 0 || args.tagUploadedEncodedCount.value > 0) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 16.dp, end = 16.dp)
                    ) {
                        UploadStatusChip(args.tagUploadedEncodedCount.value, args.tagCount.value)
                    }
                }

                Column(
                    modifier = Modifier
                        .padding(dimensionResource(id = R.dimen.dp_16))
                        .clickable(
                            onClick = {},
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CounterText(
                        current = args.tagCount.value,
                        total = args.selectedLimit.value.toString(),
                        isLimitShow = args.selectedLimit.value > 0
                    )

                    Text(
                        text = stringResource(id = R.string.tags_encoded),
                        style = CommonTypography.current.noteText,
                        color = BlackColor,
                        modifier = Modifier.padding(bottom = dimensionResource(R.dimen.dp_12))
                    )

                    IconText(
                        icon = painterResource(R.drawable.property_know_more),
                        actionText = stringResource(R.string.set_limit),
                        onActionClick = {
                            val preHeader = TopicConstants.ENCODE + "_" + args.menuCode + "_"
                            val deviceSessionId = DataStoreManager.readFromPreferences(
                                preHeader + ParameterConstants.DEVICE_SESSION_ID, ""
                            )
                            val id = DataStoreManager.readFromPreferences(
                                preHeader + ParameterConstants.ID, ""
                            )
                            if (deviceSessionId.isEmpty() || id.isEmpty()) {
                                openSheet(
                                    scope = args.scope,
                                    sheetState = args.sheetState,
                                    showSheet = args.showSheet,
                                    currentSheet = args.currentSheet,
                                    sheet = BottomSheetType.LIMIT
                                )
                            }
                        },
                        tintColor = Yellow,
                        backGroundColor = WhiteColor,
                        borderColor = OutlineDefault
                    )

                    Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_20)))

                    CommonTextField(
                        config = TextFieldConfig(
                            value = barCodeNo.value,
                            onValueChange = {
                                //val wasNotEmpty = barCodeNo.value.isNotEmpty()
                                barCodeNo.value = it
                                if (isScanScanEncode) tidCodeNo.value = ""
                                LogUtils.showLog("onValueChange0", "__" + tidCodeNo.value)/*if (wasNotEmpty && it.isEmpty()) {
                                    readerViewModel.clearTagData()
                                } else */
                                readerViewModel.clearTagData()
                            },
                            label = DataStoreManager.getBarcodeLabel(),//stringResource(id = R.string.enter_barcode),
                            imeAction = ImeAction.Done,
                            isBarCode = true,
                            onImeAction = {
                                focusManager.clearFocus()
                                if (!chkTrue(isProcessOn.value) && !chkTrue(isApiLoading.value)) {
                                    if (isTagWriteDone.value == true) readerViewModel.clearTagData()
                                    if (barCodeNo.value.isNotEmpty() && !isScanScanEncode) {
                                        readerViewModel.performPick(
                                            pickPower = args.setPower.value, pickTime = 500
                                        )
                                    } else if (barCodeNo.value.isNotEmpty() && tidCodeNo.value.isNullOrEmpty() && isScanScanEncode) {
                                        readerViewModel.scanBarcode(SearchTypeConstant.TID)
                                    }
                                }
                            },
                            onClick = {
                                focusManager.clearFocus()
                                if (chkTrue(isProcessOn.value) || chkTrue(isApiLoading.value)) return@TextFieldConfig
                                if (isTagWriteDone.value == true) readerViewModel.clearTagData();
                                readerViewModel.scanBarcode()
                                LogUtils.showLog("scanBarcode", "Scanning started")
                            })
                    )

                    Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_10)))

                    if (isScanScanEncode && barCodeNo.value.isNotEmpty()) {
                        CommonTextField(
                            config = TextFieldConfig(
                                value = tidCodeNo.value, onValueChange = {
                                LogUtils.showLog("onValueChange", "__" + it)
                                //val wasNotEmpty = tidCodeNo.value.isNotEmpty()
                                tidCodeNo.value = it
                                isTidCodeConfirmed.value = false
                                LogUtils.showLog("onValueChange", "__" + tidCodeNo.value)/*if (wasNotEmpty && it.isEmpty()) {
                                      readerViewModel.clearTagData()
                                    } else */
                                    readerViewModel.clearTagData()
                            }, label = "Tid",//stringResource(id = R.string.enter_barcode),
                                imeAction = ImeAction.Done, isBarCode = true, onImeAction = {
                                    focusManager.clearFocus()
                                    if (!chkTrue(isProcessOn.value) && !chkTrue(isApiLoading.value)) {
                                        if (isTagWriteDone.value == true) readerViewModel.clearTagData()
                                        if (tidCodeNo.value.isNotEmpty()) {
                                            if (tidCodeNo.value.matches(Regex("[0-9A-Fa-f]{8,}")) && tidCodeNo.value.length % 4 == 0) {
                                                isTidCodeConfirmed.value = true
                                                callEpcForEncoding(
                                                    barCodeNo,
                                                    tidCodeNo,
                                                    apiViewModel,
                                                    args.context,
                                                    tagInfoData.value,
                                                    args
                                                )
                                            } else readerViewModel.error().postValue(
                                                String.format(
                                                    msgInvalidScan, "tid", tidCodeNo.value
                                                )
                                            )
                                            //readerViewModel.performPick(pickPower = args.setPower.value, pickTime = 500)
                                        }
                                    }
                                }, onClick = {
                                    focusManager.clearFocus()
                                    if (chkTrue(isProcessOn.value) || chkTrue(isApiLoading.value)) return@TextFieldConfig
                                    if (isTagWriteDone.value == true) readerViewModel.clearTagData();
                                    readerViewModel.scanBarcode(SearchTypeConstant.TID)
                                    LogUtils.showLog("scanTid", "Scanning started")
                                })
                        )

                        Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_24)))
                    }
                    if (messageHeading.isNotBlank()) {
                        Text(
                            text = messageHeading,
                            style = CommonTypography.current.textSemiBold,
                            textAlign = TextAlign.Center
                        )
                    }

                    /** if (messageSubHeading.isNotBlank()) {
                    Text(
                    text = messageSubHeading,
                    style = CommonTypography.current.noteText,
                    textAlign = TextAlign.Center
                    )
                    }*/

                    StepIndicatorRow(
                        steps = steps, modifier = Modifier.padding(16.dp), stepStates = listOf(
                            barcodeStepState.value,
                            if (isScanScanEncode) tidScanStepState.value else rfidStepState.value,
                            encodeStepState.value
                        )
                    )

                }
            }
        }
        if (args.tagCount.value > 0 && (isMultiEncode || barCodeNo.value.isEmpty())
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                args.latestEan.value?.let { eanTime ->
                    CardViewWithHSI(
                        searchOnClick = {
                        openSheet(
                            scope = args.scope,
                            sheetState = args.sheetState,
                            showSheet = args.showSheet,
                            currentSheet = args.currentSheet,
                            sheet = BottomSheetType.SEARCH
                        )
                    },
                        detailOnClick = {
                            args.latestEan.value.let { barcode ->
                                callProductDetails(
                                    barCodeNo = barcode!!.barcode,
                                    apiViewModel = apiViewModel,
                                    tagInfoData = tagInfoData.value
                                )
                            }
                            openSheet(
                                scope = args.scope,
                                sheetState = args.sheetState,
                                showSheet = args.showSheet,
                                currentSheet = args.currentSheet,
                                sheet = BottomSheetType.PRODUCT_DETAILS
                            )
                        },
                        detailIcon = R.drawable.property_know_more,
                        epcNo = eanTime,
                        searchIcon = R.drawable.property_gieger_og
                    )
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
                    BottomSheetType.LIMIT -> Box(
                        modifier = Modifier
                            .background(WhiteColor)
                            .fillMaxWidth()
                            .wrapContentHeight()
                    ) {
                        SetLimitContent(
                            showSheet = args.showSheet,
                            sheetState = args.sheetState,
                            scope = args.scope,
                            currentPower = args.selectedLimit.value,
                            onPowerSet = { args.selectedLimit.value = it })
                    }

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
                            .wrapContentHeight()
                    ) {
                        SetDevicePower(
                            showSheet = args.showSheet,
                            sheetState = args.sheetState,
                            scope = args.scope,
                            currentPower = args.setPower.value,
                            onPowerSet = {
                                args.setPower.value = it
                                readerViewModel.setPower(args.setPower.value)
                                DataStoreManager.saveToPreferences(
                                    preHeader + ParameterConstants.READER_POWER, args.setPower.value
                                )
                            },
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
                            latestEan = args.latestEan,
                            readerViewModel = readerViewModel,
                            tagInfoEntity = tagInfoData
                        )
                    }

                    BottomSheetType.SESSION -> Box(
                        modifier = Modifier
                            .background(WhiteColor)
                            .fillMaxWidth()
                            .fillMaxHeight(if (args.currentSheet.value == BottomSheetType.SETTINGS) 0f else 0.35f)
                    ) {
                        val subHeading = if (args.tagCount.value > 0) {
                            val countText =
                                if (args.limit > 0) "${args.tagCount.value}/${args.limit}" else "${args.tagCount.value}"
                            buildAnnotatedString {
                                append(stringResource(R.string.encoded_end_the_session))
                                withStyle(
                                    style = SpanStyle(fontWeight = FontWeight.Bold)
                                ) {
                                    append(" $countText ")
                                }

                                append(stringResource(R.string.please_confirm))
                            }
                        } else {
                            AnnotatedString(stringResource(R.string.encoded_end_the_session_0))
                        }
                        SessionContent(
                            showSheet = args.showSheet,
                            sheetState = args.sheetState,
                            scope = args.scope,
                            navController = navController,
                            tagCount = args.tagCount.value,
                            subHeading = subHeading,
                            onStopSession = {
                                if (chkTrue(isProcessOn.value)) return@SessionContent
                                clearSavedSessionValues(args.context,args.menuCode,args.transactionType)
                                /*CoroutineScope(Dispatchers.IO).launch {
                                    val preHeader = TopicConstants.ENCODE + "_" + args.menuCode + "_"
                                    DataStoreManager.saveToPreferences(preHeader + ParameterConstants.ID, "")
                                    DataStoreManager.saveToPreferences(preHeader + ParameterConstants.DEVICE_SESSION_ID, "")
                                    DataStoreManager.saveToPreferences(preHeader + ParameterConstants.LIMIT, 0)
                                    DataStoreManager.saveToPreferences(preHeader + ParameterConstants.READER_POWER, 7)
                                    db.tagInfoDao().deleteBySessionTypeAndTransactionType(
                                        args.menuCode, args.transactionType
                                    )
                                }*/
                                args.scope.launch {
                                    if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                                        readerViewModel.clearSessionAndTransactionType()
                                        navController.popBackStack()
                                    }
                                }
                            },
                            readerViewModel,
                            limit = args.limit
                        )
                    }

                    BottomSheetType.PRODUCT_DETAILS -> Box(
                        modifier = Modifier
                            .background(WhiteColor)
                            .fillMaxWidth()
                            .fillMaxHeight(0.55f)
                    ) {
                        args.latestEan.value?.let { tagTime ->
                            ProductDetailsByEan(
                                tagTime = tagTime,
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

                    else -> {
                        args.currentSheet.value = BottomSheetType.NONE
                    }
                }


            }

        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SingleEncodingBottomBar(
    isScanScanEncode: Boolean,
    isMultiEncode: Boolean,
    barCodeNo: MutableState<String>,
    tidCodeNo: MutableState<String>,
    apiViewModel: ApiViewModel,
    readerViewModel: ReaderViewModel,
    menuCode: String,
    context: Context,
    navController: NavHostController,
    transactionType: String,
    setPower: MutableState<Int>,
    selectedLimit: MutableState<Int>,
    tagCount: State<Int>,
    tagVerifyCount: State<Int>,
) {

  /*val db = AppDatabase.getDbInstance(context)
  val tagCount = db.tagInfoDao().getTagWriteCount(menuCode, transactionType).observeAsState(0)
  val tagVerifyCount = db.tagInfoDao().getTagWriteVerifiedCount(menuCode, transactionType).observeAsState(0)*/


  Column(
    modifier = Modifier
        .fillMaxWidth()
        .background(WhiteColor)
  ) {
    HorizontalDivider(color = OutlineDefault)

    when {
      barCodeNo.value.isNotEmpty() && (!isScanScanEncode || tidCodeNo.value.isNotEmpty()) && (!isMultiEncode || tagCount.value <= 0) -> {
        Row(modifier = Modifier.padding(dimensionResource(R.dimen.dp_16))) {
          CommonButton(
            text = stringResource(id =if(isScanScanEncode) R.string.start_encode else R.string.scan_tag), onClick = {
              readerViewModel.setTriggerValue(true)
              // readerViewModel.performPick(pickPower = setPower.value, pickTime = 500)
            }, modifier = Modifier
              .fillMaxWidth()
          )
        }
      }

      tagCount.value > 0 && (isMultiEncode || barCodeNo.value.isEmpty()) -> {
        Row(
          modifier = Modifier.padding(dimensionResource(R.dimen.dp_16)),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          if (!isMultiEncode || barCodeNo.value.isEmpty()) {
            Column {
              Text(
                text = stringResource(
                  id = R.string.tag_count, (tagCount.value - tagVerifyCount.value)
                ), style = CommonTypography.current.textSemiBold
              )
              Text(
                text = stringResource(id = R.string.verification_pending),
                style = CommonTypography.current.noteText.copy(TextGrey)
              )
            }
          }
          else {
            CommonButton(
              text = stringResource(id = if(isScanScanEncode) R.string.start_encode else R.string.scan_tag), onClick = {
                readerViewModel.setTriggerValue(true)
                /*if (chkTrue(isProcessOn.value) || chkTrue(isApiLoading.value)) return@CommonButton
                if (isTagWriteDone.value == true) readerViewModel.clearTagData()
                readerViewModel.performPick(
                    pickPower = setPower.value, pickTime = 500
                )*/
              }, modifier = Modifier.weight(1f)
            )
          }

          Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_16)))

          CommonButton(
            text = stringResource(id = if ((tagCount.value - tagVerifyCount.value) > 0) R.string.verify_tags else R.string.verification_done),
            onClick = {
                val data = mapOf("transactionType" to "$transactionType","tagCount" to "${tagCount.value}")
              navController.navigate(Screen.VerifyLogsScreen.createRoute(code = menuCode, params = data))
            },
            modifier = Modifier.weight(1f),
            gradientBrush = SolidColor(WhiteColor),
            contentColor = BlackColor
          )
        }
      }
    }
  }

  if (tagCount.value == 0 && barCodeNo.value.isNullOrEmpty() && (!isScanScanEncode || tidCodeNo.value.isNullOrEmpty())) {
    Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_16)))
    ItekFooter()
  }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionContent(
  showSheet: MutableState<Boolean>,
  sheetState: SheetState,
  scope: CoroutineScope,
  navController: NavHostController,
  tagCount: Int,
  subHeading: AnnotatedString? = null,
  onStopSession: () -> Unit,
  readerViewModel: ReaderViewModel?,
  limit: Int = 0,
  isAllowContinue:Boolean = true
) {

    val countText = if (limit > 0) "$tagCount/$limit" else "$tagCount"

    val defaultSubHeading = buildAnnotatedString {


        append(stringResource(R.string.like_to_end_the_session))
        withStyle(
            style = SpanStyle(fontWeight = FontWeight.Bold)
        ) {
            append(" "+ countText +" ")
        }
        append(stringResource(R.string.items))
        append("\n")
        append(stringResource(R.string.data_lost))
        append("\n")
        append(stringResource(R.string.please_confirm_))

    }

    val finalSubHeading = subHeading ?: if (tagCount > 0) {
        defaultSubHeading
    } else {
        AnnotatedString(stringResource(R.string.like_to_end_the_session_0))
    }

  val uiState = SessionUiState(
    title = stringResource(R.string.stop_session),
    heading = stringResource(R.string.stop_current_session),
    subHeading = finalSubHeading,
    primaryButtonText = stringResource(R.string.stop_session_),
    secondaryButtonText = if(!isAllowContinue) "" else stringResource(R.string.continue_later)
  )

  val actions = SessionUiActions(
    onClose = {
      scope.launch {
        sheetState.hide()
        showSheet.value = false
      }
    }, onSecondaryAction = {
      if(isAllowContinue) {
        scope.launch {
          sheetState.hide()
          showSheet.value = false
          if (readerViewModel!=null && navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
            readerViewModel?.clearSessionAndTransactionType()
            navController.popBackStack()
          }
        }
      }
    },
    onPrimaryAction = onStopSession
  )

  SessionBottomSheet(
    uiState = uiState, actions = actions, isSecondaryButton = isAllowContinue
  )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceSettings(
    scope: CoroutineScope,
    sheetState: SheetState,
    showSheet: MutableState<Boolean>,
    currentSheet: MutableState<BottomSheetType>,
    isPowerSet: Boolean = true,
    isFilterEnable: Boolean = false
) {

    val setPower = stringResource(R.string.set_power)
    val setFilters = stringResource(R.string.set_filters)
    val about = stringResource(R.string.about)

    val items = remember(isFilterEnable) {
        buildList {
            //Power (Only if isPowerSet flag is true)
            if(isPowerSet) {
                add(
                    DeviceSettingItem(
                        iconRes = R.drawable.property_setting_power_decibel,
                        title = setPower
                    ) {
                        currentSheet.value = BottomSheetType.POWER
                    }
                )
            }

            //Filter (ONLY if enabled)
            if (isFilterEnable) {
                add(
                    DeviceSettingItem(
                        iconRes = R.drawable.property_slidershorizontal,
                        title = setFilters
                    ) {
                        currentSheet.value = BottomSheetType.FILTER
                    }
                )
            }

            add(
                DeviceSettingItem(
                    iconRes = R.drawable.property_know_more,
                    title = about
                ) {
                    // handle about click
                }
            )
        }
    }

    BottomSheetTextIconRow(
        showSheet = showSheet,
        sheetState = sheetState,
        scope = scope,
        title = stringResource(R.string.settings),
        items = items
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetLimitContent(
  showSheet: MutableState<Boolean>,
  sheetState: SheetState,
  scope: CoroutineScope,
  currentPower: Int,
  onPowerSet: (Int) -> Unit
) {

  val setPower = stringResource(R.string.encode_quantity)
  val heading = stringResource(R.string.set_tag_limit)
  val subHeading = stringResource(R.string.number_of_tags_you_plan)

  val config = remember(currentPower) {
    SetValueUiConfig(
      title = setPower,
      heading = heading,
      subHeading = subHeading,
      minValue = 0,
      maxValue = 500,
      initialValue = currentPower
    )
  }

  val actions = remember {
    SetValueActions(onConfirm = { value ->
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetDevicePower(
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
      maxValue = 15,
      initialValue = currentPower
    )
  }

  val actions = remember {
    SetValueActions(onConfirm = { value ->
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


fun callEpcForEncoding(
  barCodeNo: MutableState<String>,
  tidCodeNo: MutableState<String>,
  apiViewModel: ApiViewModel,
  context: Context,
  tagInfoData: TagInfoEntity?,
  args: EncodingScreenArgs
) {
  val preHeader = TopicConstants.ENCODE + "_" + args.menuCode + "_"

  if (!isInternetConnected(context, isShowErrDialog = false, isShowErrToast = true)) return
  val existingSessionId = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.DEVICE_SESSION_ID, "")
  val now = DateFormatUtils.getCurrentUTCTime()
  val deviceSessionId = SessionUtils.generateOfflineSessionId(args.menuCode, args.transactionType)
  //val uuid = UUIDV5.generateUUID(deviceSessionId)
  val sessionIdToSend = if (existingSessionId.isNullOrEmpty()) deviceSessionId else existingSessionId
  val jsonRequest = JSONObject().apply {
    put(ParameterConstants.USER_ID, DataStoreManager.readFromPreferences(ParameterConstants.USER_ID, ""))
    put(ParameterConstants.DEVICE_ID, DataStoreManager.readFromPreferences(ParameterConstants.DEVICE_ID, ""))
    put(ParameterConstants.CUSTOMER_ID, DataStoreManager.readFromPreferences(ParameterConstants.CUSTOMER_ID, ""))
    put(ParameterConstants.OPERATION_LOCATION_ID, DataStoreManager.readFromPreferences(LoginConstants.DEVICE_LOCATION_ID, ""))
    put(ParameterConstants.DEVICE_SESSION_ID, sessionIdToSend)
    put(ParameterConstants.BARCODE, barCodeNo.value)
    put(ParameterConstants.QTY, 1)
    put(ParameterConstants.START_DATE, now)
    put(ParameterConstants.END_DATE, now)
    put(ParameterConstants.COMPLETION_STATUS, "PENDING")

    // DETAILS ARRAY
    val detailsArray = JSONArray()
    val detail = JSONObject().apply {
      put(ParameterConstants.OLD_EPC,if(tagInfoData!=null)tagInfoData?.epc else "")
      put(ParameterConstants.TID, if(tagInfoData!=null)tagInfoData?.tid else tidCodeNo.value)
      put(ParameterConstants.COMPLETION_STATUS, "PENDING")
      put(ParameterConstants.COMPLETION_REMARK, "")
    }
    detailsArray.put(detail)
    put(ParameterConstants.DETAILS, detailsArray)
  }

  // Step 5: Call API
  apiViewModel.callApi(url = UrlConstants.ENCODING, jsonRequest = jsonRequest)
  LogUtils.showLog("ENCODING_API_START", "Start")
}


fun handleEncode(
  apiResult: ApiResult?,
  tagInfoData1: TagInfoEntity?,
  readerViewModel: ReaderViewModel,
  menuCode: String,
  selectedLimit: MutableState<Int>,
) {
  LogUtils.showLog("ENCODING_API_END", "end")
  val rootJson = apiResult?.response ?: return

  val isScanScanEncode =  menuCode.equals(MenuConstants.SCAN_SCAN_ENCODE,true)

  if (tagInfoData1 == null) {
    LogUtils.showLog("ENCODING", "tagInfoData.value is NULL")
    if(!isScanScanEncode) return
  }
  val tagInfoData = if(tagInfoData1!=null) tagInfoData1 else TagInfoEntity()

  val id = extractString(rootJson, ParameterConstants.ID, "")
  val deviceSessionId = extractString(rootJson, ParameterConstants.DEVICE_SESSION_ID, "")
  val sessionData = JSONObject()
  sessionData.put(ParameterConstants.DEVICE_SESSION_ID,deviceSessionId)
  sessionData.put(ParameterConstants.ID,id)
  readerViewModel.setSessionId(deviceSessionId,sessionData.toString())
  val preHeader = TopicConstants.ENCODE + "_" + menuCode + "_"
  DataStoreManager.saveToPreferences(preHeader + ParameterConstants.ID, id)
  DataStoreManager.saveToPreferences(preHeader + ParameterConstants.DEVICE_SESSION_ID, deviceSessionId)
  DataStoreManager.saveToPreferences(preHeader + ParameterConstants.LIMIT, selectedLimit.value)
  LogUtils.showLog("selectedLimit", "" + selectedLimit.value)

  val tagPasswordsObj = extractJSONObject(rootJson, ParameterConstants.TAG_PASSWORDS, JSONObject())
  val currentPasswordObj = extractJSONObject(tagPasswordsObj, ParameterConstants.CURRENT_PASSWORD, JSONObject())
  val currentPassword = currentPasswordObj.optString(ParameterConstants.VALUE)

  val oldPasswordsObj = extractJSONObject(tagPasswordsObj, ParameterConstants.OLD_PASSWORDS, JSONObject())
  val oldPasswordArray = extractJSONArray(oldPasswordsObj, ParameterConstants.VALUE, JSONArray())

  val oldPasswords = MutableList(oldPasswordArray.length()) {
    oldPasswordArray.optString(it)
  }
  DataStoreManager.savePasswords(currentPassword,oldPasswords)

  val detailsArray = extractJSONArray(rootJson, ParameterConstants.DETAILS, JSONArray())

  for (i in 0 until detailsArray.length()) {
    val item = detailsArray.getJSONObject(i)

    val id = extractString(item, ParameterConstants.ID, "")
    val encodeLogId = extractString(item, ParameterConstants.ENCODE_LOG_ID, "")
    val customerId = extractString(item, ParameterConstants.CUSTOMER_ID, "")
    val oldEpc = extractString(item, ParameterConstants.OLD_EPC, "")
    val newEpc = extractString(item, ParameterConstants.NEW_EPC, "")
    val tid = extractString(item, ParameterConstants.TID, "")
    val barcode = extractString(rootJson, ParameterConstants.BARCODE, "")

    tagInfoData.id = id
    tagInfoData.encodeLogId = encodeLogId
    tagInfoData.customerId = customerId
    tagInfoData.epc = oldEpc
    tagInfoData.newEpc = newEpc
    tagInfoData.tid = tid
    tagInfoData.barcode = barcode

    if (newEpc.isEmpty()) {
      LogUtils.showLog("Error", "Encoding Failed")
      return
    }

    LogUtils.showLog("ENCODING_ENCODE", "Start Encoding EPC: $newEpc")

    readerViewModel.performEncoding(tagInfoData, currentPassword, oldPasswords)
  }
}

@SuppressLint("SuspiciousIndentation")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBottomSheetView(
    showSheet: MutableState<Boolean>,
    sheetState: SheetState,
    scope: CoroutineScope,
    selectedSearchType: MutableState<String>,
    latestEan: State<TagTime?> = mutableStateOf(null),
    tagInfoEntity: State<TagInfoEntity?> = mutableStateOf(null),
    showMarkFoundPercent: Float = 0F,
    isDecode: Boolean = false,
    decodeType: String = "",
    readerViewModel: ReaderViewModel,
    onDecodeClick: (() -> Unit)? = null,
) {
    val tabs = listOf("EPC", "TID")
    val selected = remember { mutableStateOf("") }
    val selectedTab = remember { mutableStateOf(0) }
    selectedSearchType.value = tabs[selectedTab.value]
    val searchPercentage = readerViewModel.searchPercentage().observeAsState()
    val proximityValue = (searchPercentage.value ?: 0.00f).coerceIn(0.00f, 100.00f)
    val isSearchOn = readerViewModel.isSearchOn().observeAsState(false)
    val isProcessOn = readerViewModel.isProcessOn().observeAsState(false)
    LogUtils.showLog("isSearchOn", "SearchBottomSheetView: ${isSearchOn.value}")
    val context = LocalContext.current
    val tagInfoDao = AppDatabase.getDbInstance(context).tagInfoDao()
    val productZoneDataDao = AppDatabase.getDbInstance(context).productZoneDataDao()

    val showMarkFoundButton = remember { mutableStateOf(false) }

    LaunchedEffect(tagInfoEntity.value) {
        showMarkFoundButton.value = false
    }

    LaunchedEffect(searchPercentage.value) {
        if (showMarkFoundPercent > 0 && chkNull(
                searchPercentage.value, 0F
            ) >= showMarkFoundPercent
        ) {
            showMarkFoundButton.value = true
        }
    }

    val showRightButton =
        !chkTrue(isProcessOn.value) && tagInfoEntity.value != null && if (isDecode) {
            !tagInfoEntity.value!!.isUnencodedTag()
        } else {
            showMarkFoundButton.value
        }
    LogUtils.showLog("showRightButton", "SearchBottomSheetView: $showRightButton")
    LogUtils.showLog("showMarkFoundButton", "SearchBottomSheetView: ${showMarkFoundButton.value}")

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(16.dp), // important
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ---------------- HEADER ----------------

            Box(
                modifier = Modifier
                    .weight(0.1f)
                    .fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.search),
                    style = CommonTypography.current.textSemiBold,
                    modifier = Modifier.align(Alignment.Center)
                )

                Icon(
                    painter = painterResource(R.drawable.clear),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .clickable {
                            if (readerViewModel.isProcessOn().value == false) scope.launch {
                                sheetState.hide()
                                showSheet.value = false
                            }
                        })
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ---------------- TABS ----------------
            SegmentedTabView(
                selectedIndex = selectedTab.value,
                tabs = tabs,
                modifier = Modifier
                    .weight(0.15f)
                    .width(200.dp)
                    .height(40.dp),
                onTabSelected = {
                    selected.value = tabs[it]
                    selectedSearchType.value = selected.value
                    selectedTab.value = it
                },
                enabled = !chkTrue(readerViewModel.isProcessOn().value)
            )

            // ---------------- GAUGE ----------------
            Box(
                modifier = Modifier
                    .weight(0.7f)
                    .fillMaxWidth()
                    .padding(70.dp),
                contentAlignment = Alignment.Center
            ) {
                DirectionProximityGauge(
                    readerViewModel = readerViewModel, modifier = Modifier.fillMaxWidth()
                )
            }

            Row(
                modifier = Modifier
                    .weight(0.15f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                CommonButton(
                    text = stringResource(if (isSearchOn.value) R.string.stop_search else R.string.start_search),
                    modifier = Modifier
                        .then(
                            if (showRightButton) Modifier.weight(1f)
                            else Modifier.fillMaxWidth()
                        )
                        .height(48.dp),
                    onClick = {
                        readerViewModel.setTriggerValue(true)
                    },
                    icon = if (isSearchOn.value) painterResource(R.drawable.stopicon) else null,
                    gradientBrush = if (isSearchOn.value) SolidColor(WhiteColor)
                    else Brush.horizontalGradient(listOf(BlackColor, ButtonGray)),
                    contentColor = if (isSearchOn.value) BlackColor else WhiteColor
                )

                if (showRightButton) {
                    CommonButton(
                        text = if (isDecode) stringResource(R.string.decode) else stringResource(R.string.mark_as_found),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        onClick = {
                            if (tagInfoEntity.value == null) return@CommonButton
                            val tagInfo = tagInfoEntity.value!!
                            if (isDecode) {
                                // Decode logic
                                if (onDecodeClick != null) {
                                    onDecodeClick?.invoke()
                                } else {
                                    readerViewModel.performDecoding(
                                        tagInfo, decodeType = decodeType
                                    )
                                }
                            } else {
                                scope.launch(Dispatchers.IO) {
                                    //tagInfo.isFound = true
                                    //tagInfoDao.update(tagInfo)
                                    tagInfoDao.updateFound(
                                        tagInfo.session_type,
                                        tagInfo.transactionType,
                                        tagInfo.sessionId,
                                        tagInfo.tid,
                                        tagInfo.epc,
                                        tagInfo.barcode
                                    )

                                    val hasEpcData = productZoneDataDao.hasEpcData(
                                        tagInfo.topic, tagInfo.session_type, tagInfo.transactionType
                                    )
                                    if (hasEpcData && productZoneDataDao.hasEpc(
                                            tagInfo.topic,
                                            tagInfo.session_type,
                                            tagInfo.transactionType,
                                            tagInfo.epc
                                        )
                                    ) productZoneDataDao.updateFoundEpc(
                                        tagInfo.topic,
                                        tagInfo.session_type,
                                        tagInfo.transactionType,
                                        tagInfo.epc
                                    )
                                    else if (!hasEpcData && productZoneDataDao.hasBarcode(
                                            tagInfo.topic,
                                            tagInfo.session_type,
                                            tagInfo.transactionType,
                                            tagInfo.barcode
                                        )
                                    ) productZoneDataDao.updateFoundBarcode(
                                        tagInfo.topic,
                                        tagInfo.session_type,
                                        tagInfo.transactionType,
                                        tagInfo.barcode
                                    )
                                    productZoneDataDao.updateFoundBarcode(
                                        tagInfo.topic,
                                        tagInfo.session_type,
                                        tagInfo.transactionType,
                                        tagInfo.barcode
                                    )
                                    withContext(Dispatchers.Main) {
                                        sheetState.hide()
                                        showSheet.value = false
                                    }
                                }
                            }
                        },
                        gradientBrush = SolidColor(WhiteColor),
                        contentColor = BlackColor
                    )
                }

            }
        }
    }

}

fun clearSavedSessionValues(context: Context, menuCode: String, transactionType: String) {
  val preHeader = TopicConstants.ENCODE + "_" + menuCode + "_"
  DataStoreManager.saveToPreferences(preHeader + ParameterConstants.DEVICE_SESSION_ID, "")
  DataStoreManager.saveToPreferences(preHeader + ParameterConstants.ID, "")
  DataStoreManager.saveToPreferences(preHeader + ParameterConstants.LIMIT, 0)
  DataStoreManager.saveToPreferences(preHeader + ParameterConstants.READER_POWER, 7)
  CoroutineScope(Dispatchers.IO).launch {
    val db = AppDatabase.getDbInstance(context)
    db.tagInfoDao().deleteBySessionTypeAndTransactionType(menuCode, transactionType)
  }
}


