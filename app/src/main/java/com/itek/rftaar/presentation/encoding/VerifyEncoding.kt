package com.itek.rftaar.presentation.encoding

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController
import com.itek.rftaar.R
import com.itek.rftaar.ReaderActivity
import com.itek.rftaar.api.ApiResult
import com.itek.rftaar.api.constants.ParameterConstants
import com.itek.rftaar.api.constants.UrlConstants
import com.itek.rftaar.core.common.utils.LogUtils
import com.itek.rftaar.core.database.DataStoreManager
import com.itek.rftaar.data.entity.TagInfoEntity
import com.itek.rftaar.mqtt.constants.TopicConstants
import com.itek.rftaar.presentation.commonComp.BottomSheetType
import com.itek.rftaar.data.model.TagTime
import com.itek.rftaar.presentation.commonComp.CommonButton
import com.itek.rftaar.presentation.commonComp.CommonTextField
import com.itek.rftaar.presentation.commonComp.ErrorAppSnackBarData
import com.itek.rftaar.presentation.commonComp.ImageViewFullScreen
import com.itek.rftaar.presentation.commonComp.ItekFooter
import com.itek.rftaar.presentation.commonComp.SnackbarController
import com.itek.rftaar.presentation.commonComp.StepIndicatorRow
import com.itek.rftaar.presentation.commonComp.StepItem
import com.itek.rftaar.presentation.commonComp.StepUiState
import com.itek.rftaar.presentation.commonComp.TextFieldConfig
import com.itek.rftaar.presentation.commonComp.TopBarContent
import com.itek.rftaar.presentation.commonComp.VerifyInfoCard
import com.itek.rftaar.presentation.commonComp.messageForStep
import com.itek.rftaar.presentation.commonComp.openSheet
import com.itek.rftaar.presentation.commonComp.statusForState
import com.itek.rftaar.presentation.decoding.parseProductDetails
import com.itek.rftaar.presentation.viewmodel.ApiViewModel
import com.itek.rftaar.presentation.viewmodel.ReaderViewModel
import com.itek.rftaar.ui.theme.BackGround
import com.itek.rftaar.ui.theme.BlackColor
import com.itek.rftaar.ui.theme.CommonTypography
import com.itek.rftaar.ui.theme.ErrorBgColor
import com.itek.rftaar.ui.theme.OutlineDefault
import com.itek.rftaar.ui.theme.RedColor
import com.itek.rftaar.ui.theme.WhiteColor
import com.itek.rftaar.utils.CommonUtils.chkNull
import com.itek.rftaar.utils.CommonUtils.chkTrue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifyEncoding(
    modifier: Modifier,
    navController: NavHostController,
    label: String,
    menuCode: String,
    apiViewModel: ApiViewModel = hiltViewModel(),
    searchParams: Map<String, Any> = emptyMap(),
) {
  val scope = rememberCoroutineScope()

    val barCodeNo = remember { mutableStateOf("") }
    val context = LocalContext.current
    val activity = context as? ReaderActivity
    val readerViewModel = if(activity!=null) activity.findReaderViewModel() else hiltViewModel()
    if (activity != null) {
        readerViewModel.onCreate()
        readerViewModel.setSessionAndTransactionType(menuCode)
    }
    val eanNumber = remember { mutableStateOf("") }
    val firstFieldList = remember { mutableStateListOf<Pair<String, String>>() }
    val secondFieldList = remember { mutableStateListOf<Pair<String, String>>() }

    val preHeader = TopicConstants.ENCODE + "_" + menuCode + "_"
    val readerPower = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.READER_POWER, 15)
    val setPower = rememberSaveable { mutableStateOf(readerPower) }
    readerViewModel.setPower(setPower.value)

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { sheetValue ->
            sheetValue != SheetValue.Hidden
        }
    )
    val showSheet = remember { mutableStateOf(false) }
    val currentSheet = remember { mutableStateOf(BottomSheetType.NONE) }

    BackHandler(enabled = true) {
        if(chkTrue(readerViewModel.isProcessOn().value)) return@BackHandler
        scope.launch {
            //readerViewModel.onDestroy()
            if(navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                readerViewModel.clearSessionAndTransactionType()
                navController.popBackStack()
            }
        }
    }

    Scaffold(
        bottomBar = {
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .background(BackGround),
                contentAlignment = Alignment.BottomCenter
            ) {
                VerifyEncodingBottomBar(
                    barCodeNo,
                    apiViewModel,
                    readerViewModel,
                    context,
                    firstFieldList,
                    secondFieldList,
                    eanNumber
                )
            }
        },
        modifier = Modifier.background(BackGround)
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .background(brush = Brush.verticalGradient(listOf(Color(0xFFF3F3F3), Color(0xFFFFFFFF), Color(0xFFFFFFFF), Color(0xFFFFFFFF))))
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            VerifyEncodingContent(
                navController,
                barCodeNo,
                apiViewModel,
                readerViewModel,
                label,
                eanNumber,
                firstFieldList,
                secondFieldList,
                sheetState,
                currentSheet,
                showSheet
            )
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
                        .wrapContentHeight()
                ) {
                    SetDevicePower(
                        showSheet = showSheet,
                        sheetState = sheetState,
                        scope = scope,
                        currentPower = setPower.value,
                        onPowerSet = {
                            setPower.value = it
                            readerViewModel.setPower(setPower.value)
                            DataStoreManager.saveToPreferences(preHeader + ParameterConstants.READER_POWER, setPower.value)
                        },
                    )
                }

                BottomSheetType.SETTINGS -> Box(
                    modifier = Modifier
                        .background(WhiteColor)
                        .fillMaxWidth()
                        .fillMaxHeight(0.26f)
                ) {
                    DeviceSettings(
                        showSheet = showSheet,
                        sheetState = sheetState,
                        scope = scope,
                        currentSheet = currentSheet
                    )
                }

                else -> {
                    currentSheet.value = BottomSheetType.NONE
                }
            }


        }
    }
}

enum class VerificationResult {
    NONE, SUCCESS, ERROR,
}

enum class VerificationUiState {
    NONE,
    OVERLAY,
    BADGE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifyEncodingContent(
    navController: NavHostController,
    barCodeNo: MutableState<String>,
    apiViewModel: ApiViewModel,
    readerViewModel: ReaderViewModel,
    label: String,
    eanNumber: MutableState<String>,
    firstFieldList: SnapshotStateList<Pair<String, String>>,
    secondFieldList: SnapshotStateList<Pair<String, String>>,
    sheetState: SheetState,
    currentSheet: MutableState<BottomSheetType>,
    showSheet: MutableState<Boolean>
) {
    val scannedValue = readerViewModel.barcodeData().observeAsState("")
    val tagInfoData = readerViewModel.pickData().observeAsState()
    val triggerPressed = readerViewModel.isTriggerPressed().observeAsState(false)
    val isBarcodeScan = readerViewModel.isBarcodeOn().observeAsState(false)
    val snackbarController = remember { SnackbarController() }
    //val scaningMessage = stringResource(R.string.scanning_barcode)
    val isPickOn = readerViewModel.isPickOn().observeAsState()
    //val error = readerViewModel.error().observeAsState("")
    val scope = rememberCoroutineScope()
    val isTagWriteDone = readerViewModel.isTagWriteDone().observeAsState(false)
    val errorMessage = readerViewModel.error().observeAsState("")
    val isTagWriteOn = readerViewModel.isTagWriteOn().observeAsState(false)
    val apiError = apiViewModel.apiErrorMsg.observeAsState("")


    val verificationUiState = remember { mutableStateOf(VerificationUiState.NONE) }
    val verificationResult = remember { mutableStateOf(VerificationResult.NONE) }
    val isBarcodeConfirmed = remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val setPower = rememberSaveable { mutableStateOf(7) }

    LaunchedEffect(barCodeNo.value) {
        if (barCodeNo.value.isEmpty()) {
            readerViewModel.clearTagData()
        }
    }
    val barcodeStepState = remember {
        derivedStateOf {
            when {

                // ACTIVE first (highest priority)
                isBarcodeScan.value ->
                    StepUiState.ACTIVE

                // COMPLETED
                barCodeNo.value.isNotEmpty() -> StepUiState.COMPLETED

                // ERROR only when barcode step actually failed
                barCodeNo.value.isEmpty() && errorMessage.value.isNotEmpty() ->
                    StepUiState.ERROR

                // true idle
                else ->
                    StepUiState.IDLE
            }
        }
    }


    val rfidStepState = remember {
        derivedStateOf {
            when {

                // ⭐ FORCE RESET FIRST
                barCodeNo.value.isEmpty() ->
                    StepUiState.IDLE

                isPickOn.value == true ->
                    StepUiState.ACTIVE

                barCodeNo.value.isNotEmpty() && eanNumber.value.isNullOrEmpty()  && errorMessage.value.isNotEmpty() ->
                    StepUiState.ERROR

                eanNumber.value.isNotBlank() ->
                    StepUiState.COMPLETED

                else ->
                    StepUiState.IDLE
            }
        }
    }


    val verifyStepState = remember {
        derivedStateOf {
            when {

                // ⭐ force reset
                barCodeNo.value.isEmpty() ->
                    StepUiState.IDLE

                isTagWriteOn.value == true ->
                    StepUiState.ACTIVE

                eanNumber.value.isNotBlank() &&
                        barCodeNo.value.isNotBlank() &&
                        eanNumber.value.trim() == barCodeNo.value.trim() ->
                    StepUiState.COMPLETED

                eanNumber.value.isNotBlank() &&
                        barCodeNo.value.isNotBlank() &&
                        eanNumber.value.trim() != barCodeNo.value.trim() ->
                    StepUiState.ERROR

                else ->
                    StepUiState.IDLE
            }
        }
    }

    val steps = listOf(
        StepItem(
            icon = R.drawable.property_barcode,
            label = DataStoreManager.getBarcodeLabel(),
            status = statusForState(barcodeStepState.value)
        ),
        StepItem(
            icon = R.drawable.property_rfidd,
            label = "RFID",
            status = statusForState(rfidStepState.value)
        ),
        StepItem(
            icon = R.drawable.property_rfid_encoded,
            label = "Matched",
            status = statusForState(verifyStepState.value)
        )
    )

    val isOperationLocked = remember {
        derivedStateOf {
            barCodeNo.value.isNotBlank() &&
                    (
                            verifyStepState.value == StepUiState.COMPLETED ||
                                    verifyStepState.value == StepUiState.ERROR
                            )
        }
    }

    val showImagePreview = remember { mutableStateOf(false) }
    val selectedPainter = remember { mutableStateOf<Painter?>(null) }


    LaunchedEffect(scannedValue.value) {
        if (chkNull(scannedValue.value,"").isNotBlank()) {
            LogUtils.showLog("TEXTFIELD_UPDATE", "Setting text = "+chkNull(scannedValue.value,""))
            barCodeNo.value = chkNull(scannedValue.value,"")
            isBarcodeConfirmed.value = true
            if (barCodeNo.value.isNotEmpty()) {
                callProductDetails(
                    barCodeNo.value,
                    apiViewModel,
                    null
                )
            }

        }
    }


    /*LaunchedEffect(isBarcodeScan.value) {
        LogUtils.showLog("isBarcodeScan", "SingleEncodingContent:${isBarcodeScan.value} ")
        if (isBarcodeScan.value == true) {
            snackbarController.show(
                AppSnackBarData(
                    icon = R.drawable.property_barcode_filled,
                    message = scaningMessage,
                    showCancel = false
                )
            )
        }
    }*/

    /*LaunchedEffect(error.value) {
        val message = error.value
        if (!message.isNullOrBlank()) {
            LogUtils.showLog("isTagNotFound", "SingleEncodingContent: $message")
            snackbarController.show(
                ErrorAppSnackBarData(message)
                *//*AppSnackBarData(
                    icon = R.drawable.property_1_error,
                    message = message,
                    showCancel = false
                )*//*
            )
        }
    }*/

    LaunchedEffect(triggerPressed.value) {
        triggerPressed.value?.let { if (!it) return@LaunchedEffect }
        if (isOperationLocked.value) return@LaunchedEffect
        readerViewModel.setTriggerValue(false)
        if (chkTrue(readerViewModel.isProcessOn().value)) return@LaunchedEffect
        if(navController.currentBackStackEntry?.lifecycle?.currentState != Lifecycle.State.RESUMED) return@LaunchedEffect
        if (barCodeNo.value.isEmpty()) {
            VerificationUiState.NONE
            VerificationResult.NONE
            readerViewModel.scanBarcode()
        } else if(barCodeNo.value.isNotEmpty()) {
            readerViewModel.performPick(pickPower = setPower.value, pickTime = 500, isAllowNonEncodedTags = false)
        }
    }

    LaunchedEffect(tagInfoData.value) {
        val tag = tagInfoData.value ?: return@LaunchedEffect

        eanNumber.value = tag.barcode.orEmpty()

        verificationResult.value =
            if (eanNumber.value.trim() == barCodeNo.value.trim())
                VerificationResult.SUCCESS
            else
                VerificationResult.ERROR

        verificationUiState.value = VerificationUiState.OVERLAY

        delay(2000)

        verificationUiState.value = VerificationUiState.BADGE

        callProductDetails(
            barCodeNo.value,
            apiViewModel,
            tag
        )
    }

    LaunchedEffect(barCodeNo.value) {
        if (barCodeNo.value.isEmpty()) {
            eanNumber.value = ""

            // 🔑 IMPORTANT RESET
            isBarcodeConfirmed.value = false

            verificationUiState.value = VerificationUiState.NONE
            verificationResult.value = VerificationResult.NONE
        }
    }



    /* -------------------- MESSAGES (NO remember/mutableState) -------------------- */

    val messageHeading: String = when {
        verifyStepState.value != StepUiState.IDLE -> messageForStep(
            verifyStepState.value,
            active = "Processing...",
            completed = "Data Matched!",
            error = "Data Not Matched!"
        ) ?: ""

        rfidStepState.value != StepUiState.IDLE -> messageForStep(
            rfidStepState.value,
            active = "Processing...",
            completed = "RFID Scan Done",
            error = errorMessage.value
        ) ?: ""

        barcodeStepState.value != StepUiState.IDLE -> messageForStep(
            barcodeStepState.value,
            active = "Scanning "+DataStoreManager.getBarcodeLabel()+"...",
            completed = DataStoreManager.getBarcodeLabel()+" Scan Done",
            error = errorMessage.value
        ) ?: ""

        else -> ""
    }

    val messageSubHeading: String = when {
        verifyStepState.value == StepUiState.ERROR ->
            chkNull(errorMessage.value, apiError.value)

        rfidStepState.value == StepUiState.ERROR ->
            errorMessage.value

        barcodeStepState.value == StepUiState.ERROR ->
            errorMessage.value

        verifyStepState.value == StepUiState.ACTIVE ->
            "Please keep the device steady"

        rfidStepState.value == StepUiState.ACTIVE ->
            "Bring RFID tag near the reader"

        barcodeStepState.value == StepUiState.ACTIVE ->
            stringResource(R.string.scanning_barcode)

        verifyStepState.value == StepUiState.COMPLETED ->
            DataStoreManager.getBarcodeLabel()+" & RFID data match the same product"

        rfidStepState.value == StepUiState.COMPLETED ->
            ""
        barcodeStepState.value == StepUiState.COMPLETED ->
            DataStoreManager.getBarcodeLabel()+" scanned successfully"

        else -> ""
    }


    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.Top) {
                TopBarContent(
                    label,
                    onBackClickL = {
                        scope.launch {
                            if (chkTrue(readerViewModel.isProcessOn().value) || chkTrue(apiViewModel.isLoading.value)) return@launch
                            if(navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                                readerViewModel.clearSessionAndTransactionType()
                                navController.popBackStack()
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

            Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_24)))

            Column(modifier = Modifier.padding(dimensionResource(R.dimen.dp_16)), horizontalAlignment = Alignment.CenterHorizontally) {
                if (!isBarcodeConfirmed.value) {
                    CommonTextField(
                        config = TextFieldConfig(
                            value = barCodeNo.value,
                            onValueChange = { newText ->
                                if (newText != barCodeNo.value) {
                                    barCodeNo.value = newText
                                }
                                if (newText.isEmpty()) {
                                    isBarcodeConfirmed.value = false

                                    eanNumber.value = ""

                                    verificationUiState.value = VerificationUiState.NONE
                                    verificationResult.value = VerificationResult.NONE

                                    firstFieldList.clear()
                                    secondFieldList.clear()
                                }
                            },
                            label = DataStoreManager.getBarcodeLabel(),//stringResource(id = R.string.enter_barcode),
                            imeAction = ImeAction.Done,
                            isBarCode = true,
                            onImeAction = {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                                if (barCodeNo.value.isNotBlank()) {
                                    isBarcodeConfirmed.value = true
                                    callProductDetails(
                                        barCodeNo.value,
                                        apiViewModel,
                                        null
                                    )
                                }
                            },
                            onClick = {
                                if (isOperationLocked.value) return@TextFieldConfig
                                readerViewModel.isProcessOn().value?.let {
                                    if (!it) {
                                        readerViewModel.setTriggerValue(false)
                                        if (chkTrue(readerViewModel.isProcessOn().value)) return@TextFieldConfig
                                        if (barCodeNo.value.isEmpty()) {
                                            VerificationUiState.NONE
                                            VerificationResult.NONE
                                            readerViewModel.scanBarcode()
                                        } else if(barCodeNo.value.isNotEmpty()) {
                                            readerViewModel.performPick(pickPower = setPower.value, pickTime = 500, isAllowNonEncodedTags = false)
                                        }
                                    }
                                }
                            }
                        )
                    )

                }
                else {

                    VerifyInfoCard(
                        value = barCodeNo.value,
                        label = DataStoreManager.getBarcodeLabel(),
                        imeAction = ImeAction.Done,
                        onValueChange = { newText ->
                            if (newText != barCodeNo.value) {
                                barCodeNo.value = newText
                            }

                            if (newText.isEmpty()) {
                                isBarcodeConfirmed.value = false

                                eanNumber.value = ""

                                verificationUiState.value = VerificationUiState.NONE
                                verificationResult.value = VerificationResult.NONE

                                firstFieldList.clear()
                                secondFieldList.clear()

                            }
                        },
                        onImeAction = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                            if (barCodeNo.value.isNotBlank()) {
                                isBarcodeConfirmed.value = true
                                callProductDetails(
                                    barCodeNo.value,
                                    apiViewModel,
                                    null
                                )
                            }
                        },
                        onScanClick = {
                            if (isOperationLocked.value) return@VerifyInfoCard
                            readerViewModel.isProcessOn().value?.let {
                                if (!it) {
                                    readerViewModel.setTriggerValue(false)
                                    if (chkTrue(readerViewModel.isProcessOn().value)) return@VerifyInfoCard
                                    if (barCodeNo.value.isEmpty()) {
                                        VerificationUiState.NONE
                                        VerificationResult.NONE
                                        readerViewModel.scanBarcode()
                                    } else if(barCodeNo.value.isNotEmpty()) {
                                        readerViewModel.performPick(pickPower = setPower.value, pickTime = 500, isAllowNonEncodedTags = false)
                                    }
                                }
                            }
                        },
                        verificationUiState = verificationUiState.value,
                        verificationResult = verificationResult.value,
                        infoList = firstFieldList,
                        showImagePreview = showImagePreview,
                        selectedPainter = selectedPainter
                    )

                    Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_16)))

                    if (verifyStepState.value == StepUiState.ERROR && eanNumber.value != barCodeNo.value) {

                        VerifyInfoCard(
                            value = eanNumber.value,
                            label = stringResource(R.string.rfid),
                            onValueChange = { newText ->
                                if (newText != eanNumber.value) {
                                    eanNumber.value = newText
                                }

                                if (newText.isEmpty()) {
                                    verificationUiState.value = VerificationUiState.NONE
                                    verificationResult.value = VerificationResult.NONE

                                    secondFieldList.clear()
                                }
                            },
                            onImeAction = {   keyboardController?.hide()
                                focusManager.clearFocus()
                                          },
                            onScanClick = {
                                if (isOperationLocked.value) return@VerifyInfoCard
                                readerViewModel.isProcessOn().value?.let {
                                    if (!it) {
                                        readerViewModel.performPick(pickPower = setPower.value, pickTime = 500, isAllowNonEncodedTags = false)
                                        LogUtils.showLog("scanBarcode", "Scanning started")
                                        LogUtils.showLog(
                                            "scanBarcode",
                                            "SingleEncodingContent: ${
                                                readerViewModel.scanBarcode(
                                                    ""
                                                )
                                            }"
                                        )
                                    }
                                }
                            },
                            verificationUiState = verificationUiState.value,
                            verificationResult = verificationResult.value,
                            infoList = secondFieldList,
                            showImagePreview = showImagePreview,
                            selectedPainter = selectedPainter
                        )

                    }

                }

                Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_24)))

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

                Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_32)))

                StepIndicatorRow(
                    steps = steps, modifier = Modifier.padding(16.dp), stepStates = listOf(
                        barcodeStepState.value, rfidStepState.value, verifyStepState.value
                    )
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter){
            if (verifyStepState.value == StepUiState.ERROR && eanNumber.value != barCodeNo.value) {
                Card(
                    modifier = Modifier
                        .wrapContentWidth()
                        .wrapContentHeight(),
                    shape = RoundedCornerShape(dimensionResource(R.dimen.dp_16)),
                    colors = CardDefaults.cardColors(containerColor = ErrorBgColor)
                ) {

                    Text(
                        text = "Data Not Matched!",
                        style = CommonTypography.current.buttonSemiBold.copy(color = RedColor),
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }

        Box(modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center){
            if (showImagePreview.value) {
                ImageViewFullScreen(showImagePreview , selectedPainter)
            }
        }
    }
}

fun callProductDetails(
    barCodeNo: String,
    apiViewModel: ApiViewModel,
    tagInfoData: TagInfoEntity?=null,
    tagTime: TagTime?=null
) {
    val map = HashMap<String,String>()
    map.put(ParameterConstants.CUSTOMER_ID,DataStoreManager.readFromPreferences(ParameterConstants.CUSTOMER_ID,""))
    map.put(ParameterConstants.LOCATION_ID,DataStoreManager.readFromPreferences(ParameterConstants.BUSINESS_LINE_ID,""))
    if (tagInfoData != null && tagInfoData.barcode.isNotEmpty()) {
        map.put(ParameterConstants.EAN,tagInfoData.barcode)
        map.put(ParameterConstants.EPC,tagInfoData.epc)
        map.put(ParameterConstants.TID,tagInfoData.tid)
    }
    else if (tagTime != null && tagTime.barcode.isNotEmpty()) {
        map.put(ParameterConstants.EAN,tagTime.barcode)
        map.put(ParameterConstants.EPC,tagTime.epc)
        map.put(ParameterConstants.TID,tagTime.tid)
    }
    else map.put(ParameterConstants.EAN,barCodeNo)
    apiViewModel.callApi(
    url = UrlConstants.PRODUCTS, queryMap = map)
    LogUtils.showLog("PRODUCTS_API_START", "Start")
}


fun handleProductDetails(
  apiResult: ApiResult?,
  barcode: String?,
  tagInfo: TagInfoEntity?,
  firstFieldList: SnapshotStateList<Pair<String, String>>,
  secondFieldList: SnapshotStateList<Pair<String, String>>
) {
  val rootJson = apiResult?.response ?: return
  val listProductValues = parseProductDetails(rootJson)
  /*val data = extractJSONObject(rootJson, ParameterConstants.DATA, rootJson)
  val headerArray = extractJSONArray(data, ParameterConstants.HEADERS, JSONArray())
  val productsArray = extractJSONArray(data, ParameterConstants.PRODUCTS,extractJSONArray(data, ParameterConstants.DATA, JSONArray()))

  val listProductLabels = ArrayList<String>()
  if(headerArray!=null && headerArray.length()>0)
      for (i in 0 until headerArray.length()) {
          val key = headerArray.getString(i)
          if (key.isNullOrEmpty() && !key.equals(DataStoreManager.getImageLabel(),true)) continue
          listProductLabels.add(key.trim().replace("[","").replace("]",""))

      }
  if(listProductLabels.isNotEmpty()) DataStoreManager.saveToPreferences("listProductLabels",listProductLabels.toString())
  val listProductValues = ArrayList<Pair<String, String>>()
  if (productsArray != null && productsArray.length() > 0) {
    for (i in 0 until productsArray.length()) {
      val product = productsArray.getJSONObject(i)
      val keys = product.keys()

      while (keys.hasNext()) {
        val key = keys.next()
        if (key.isNullOrEmpty()) continue
        var value = chkNull(extractString(product, key, ""), "")
        //if (value.isNullOrEmpty() || value == "null") continue
    *//*      if (BaseUtils.isDebuggable() && key.equals("Images", true)){
              value = "[\"https://storage-cdn.weweb.io/75cef5c5-df84-444e-89c8-f3a5d1cc70e9/users-storage/b3906169/Captura+de+img.png\",\"https://pngimg.com/d/mario_PNG125.png\"]"
          }*//*
        listProductValues.add(key to value)
      }
    }
  }
  else if (headerArray != null && headerArray.length() > 0) {
    for (i in 0 until headerArray.length()) {
      val key = headerArray.getString(i)
      if (key.isNullOrEmpty() && !key.equals(DataStoreManager.getImageLabel(),true)) continue
      listProductValues.add(key to "")
    }
  }
  if (listProductValues.isEmpty()) listProductValues.add("Product" to "Data Not Available")*/

  when {
    tagInfo != null -> {
      secondFieldList.clear()
      secondFieldList.addAll(listProductValues)
    }

    tagInfo == null && !barcode.isNullOrEmpty() -> {
      firstFieldList.clear()
      firstFieldList.addAll(listProductValues)
    }
  }
}


@Composable
fun VerifyEncodingBottomBar(
    barCodeNo: MutableState<String>,
    apiViewModel: ApiViewModel,
    readerViewModel: ReaderViewModel,
    context: Context,
    firstFieldList: SnapshotStateList<Pair<String, String>>,
    secondFieldList: SnapshotStateList<Pair<String, String>>,
    eanNumber: MutableState<String>
) {
  val snackbarController = remember { SnackbarController() }
  val canAddNextTag = remember { mutableStateOf(false) }
  val tagInfoData = readerViewModel.pickData().observeAsState()
  //val scannedValue = readerViewModel.barcodeData.observeAsState("")
  val response = apiViewModel.apiResult.collectAsState(null)

  LaunchedEffect(response.value) {
    if(response.value==null) return@LaunchedEffect
    val result = response.value!!
      if (!result.isSuccess) {
        snackbarController.show(
          ErrorAppSnackBarData(result.errMsg.toString())
        )
      }
      else if (response.value != null) {
        val jsonResponse = response.value
        when (result.url) {
          UrlConstants.PRODUCTS ->
            handleProductDetails(
              apiResult = result,
              barcode = barCodeNo.value,
              tagInfo = tagInfoData.value,
              firstFieldList = firstFieldList,
              secondFieldList = secondFieldList
            )
        }
      }
  }

    if (canAddNextTag.value) {
        Button(
            modifier = Modifier.fillMaxWidth(), onClick = {
                canAddNextTag.value = false
                readerViewModel.setTriggerValue(true)
                /**readerViewModel.isProcessOn().value?.let {
                    if (!it) {
                        readerViewModel.performPick(pickPower = setPower.value,pickTime=500)
                    }
                }*/
            }, colors = ButtonDefaults.buttonColors(
                containerColor = WhiteColor,
                contentColor = BlackColor,
                disabledContainerColor = WhiteColor.copy(alpha = 0.4f),
                disabledContentColor = BlackColor.copy(alpha = 0.6f),
            ),
            border = BorderStroke(dimensionResource(R.dimen.dp_1), BlackColor)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimensionResource(R.dimen.dp_16)),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.property_rfidd),
                    contentDescription = null
                )
                Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_10)))
                Text(
                    text = stringResource(R.string.add_next_tag),
                    style = CommonTypography.current.textSemiBold
                )
            }
        }
    }
    if(barCodeNo.value.isNotEmpty() && eanNumber.value.isEmpty()){
        Row(modifier = Modifier.padding(dimensionResource(R.dimen.dp_16))) {

            CommonButton(
                text = stringResource(id = R.string.scan_tag),
                onClick = {
                    readerViewModel.setTriggerValue(true)
                    /**readerViewModel.isProcessOn().value?.let {
                        if (!it) {
                            readerViewModel.performPick(pickPower = setPower.value,pickTime=500)
                        }
                    }*/
                },
                modifier = Modifier
            )
        }
    }
    else {
        ItekFooter()
    }
}


@Composable
fun VerificationOverlayStatic(
    result: VerificationResult,
    visible: Boolean
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    when (result) {
                        VerificationResult.SUCCESS ->
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF048204).copy(alpha = 0.55f),
                                    Color(0xFF141414).copy(alpha = 0.35f)
                                )
                            )

                        VerificationResult.ERROR ->
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFFD50000).copy(alpha = 0.55f),
                                    Color(0xFF141414).copy(alpha = 0.35f)
                                )
                            )

                        else -> Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Transparent)
                        )
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(
                    if (result == VerificationResult.SUCCESS)
                        R.drawable.property_sealcheck_1
                    else
                        R.drawable.property_error_1
                ),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(64.dp)
            )
        }
    }
}


@Composable
fun VerificationBadgeStatic(
    result: VerificationResult,
    visible: Boolean
) {
    if (visible && result != VerificationResult.NONE) {
        Image(
            painter = painterResource(
                if (result == VerificationResult.SUCCESS)
                    R.drawable.property_success
                else
                    R.drawable.property_1_error
            ),
            contentDescription = null
        )
    }
}



