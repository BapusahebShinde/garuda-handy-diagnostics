package com.itek.rftaar.presentation.dashBoard

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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
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
import com.itek.rftaar.core.common.constants.LoginConstants
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
import com.itek.rftaar.presentation.commonComp.BottomSheetType
import com.itek.rftaar.presentation.commonComp.CardViewWithHSI
import com.itek.rftaar.presentation.commonComp.CommonButton
import com.itek.rftaar.presentation.commonComp.CounterText
import com.itek.rftaar.presentation.commonComp.ImageViewFullScreen
import com.itek.rftaar.presentation.commonComp.StepIndicatorRow
import com.itek.rftaar.presentation.commonComp.StepItem
import com.itek.rftaar.presentation.commonComp.StepUiState
import com.itek.rftaar.presentation.commonComp.TopBarContent
import com.itek.rftaar.presentation.commonComp.UploadStatusChip
import com.itek.rftaar.presentation.commonComp.messageForStep
import com.itek.rftaar.presentation.commonComp.openSheet
import com.itek.rftaar.presentation.commonComp.statusForState
import com.itek.rftaar.presentation.decoding.DecodeDeviceSettings
import com.itek.rftaar.presentation.decoding.ProductDetailsByEan
import com.itek.rftaar.presentation.decoding.handleProductDetails
import com.itek.rftaar.presentation.encoding.SearchBottomSheetView
import com.itek.rftaar.presentation.encoding.SessionContent
import com.itek.rftaar.presentation.encoding.SetDevicePower
import com.itek.rftaar.presentation.encoding.callProductDetails
import com.itek.rftaar.presentation.navigation.Screen
import com.itek.rftaar.presentation.viewmodel.ApiViewModel
import com.itek.rftaar.presentation.viewmodel.ReaderViewModel
import com.itek.rftaar.ui.theme.BlackColor
import com.itek.rftaar.ui.theme.CommonTypography
import com.itek.rftaar.ui.theme.OutlineDefault
import com.itek.rftaar.ui.theme.WhiteColor
import com.itek.rftaar.utils.CommonUtils.chkNull
import com.itek.rftaar.utils.CommonUtils.chkTrue
import com.itek.rftaar.utils.NetworkUtils.isInternetConnected
import com.itek.rftaar.utils.SessionUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReturnScreen(
    modifier: Modifier,
    navController: NavHostController,
    label: String,
    menuCode: String,
    apiViewModel: ApiViewModel = hiltViewModel(),
    searchParams: Map<String, Any> = emptyMap(),
) {

    LogUtils.showLog("Screen", "DecodingScreen")
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val activity = context as? ReaderActivity
    val readerViewModel = if (activity != null) activity.findReaderViewModel() else hiltViewModel()
    val preHeader = TopicConstants.DECODE + "_" + menuCode + "_"
    val deviceSessionId =
        DataStoreManager.readFromPreferences(preHeader + ParameterConstants.DEVICE_SESSION_ID, "")
    val readerPower =
        DataStoreManager.readFromPreferences(preHeader + ParameterConstants.READER_POWER, 7)
    val transactionType = rememberSaveable { mutableStateOf("Return") }
    LogUtils.showLog("transactionType", "ReturnScreen: ${transactionType.value}")
    if (activity != null) {
        readerViewModel.onCreate()
        readerViewModel.setSessionAndTransactionType(
            menuCode,
            transactionType.value,
            topic = TopicConstants.DECODE
        )
        if (deviceSessionId.isNotEmpty()) readerViewModel.setSessionId(deviceSessionId)
    }

    val isProcessOn = readerViewModel.isProcessOn().observeAsState()
    val isApiLoading = apiViewModel.isLoading.observeAsState(false)

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { sheetValue ->
            sheetValue != SheetValue.Hidden
        }
    )
    val showSheet = remember { mutableStateOf(false) }
    val currentSheet = remember { mutableStateOf(BottomSheetType.NONE) }
    val setPower = rememberSaveable { mutableStateOf(readerPower) }

    val db = AppDatabase.getDbInstance(context)
    val tagCount =
        db.tagInfoDao().getTagWriteCount(menuCode, transactionType.value).observeAsState(0)
    val uploadedTagCount =
        db.tagInfoDao().getUploadedTagWriteCount(menuCode, transactionType.value).observeAsState(0)
    val latestEan = db.tagInfoDao().getLatestEncodedTag(menuCode, transactionType.value)
        .collectAsState(initial = null)

    BackHandler(enabled = true) {
        scope.launch {
            if (chkTrue(isProcessOn.value) || chkTrue(isApiLoading.value)) return@launch
            val preHeader = TopicConstants.DECODE + "_" + menuCode + "_"
            val deviceSessionId = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.DEVICE_SESSION_ID, "")
            if (deviceSessionId.isEmpty()) {
                clearSavedSessionValues(context, menuCode)
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

    val returnScreenArgs = ReturnScreenArgs(
        context = context,
        label = label,
        menuCode = menuCode,
        scope = scope,
        sheetState = sheetState,
        showSheet = showSheet,
        currentSheet = currentSheet,
        tagCount = tagCount,
        uploadedTagCount = uploadedTagCount,
        setPower = setPower,
        deviceSessionId = deviceSessionId,
        transactionType = transactionType,
        latestEan = latestEan
    )

    Box {

        Scaffold(
            bottomBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(WhiteColor),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    ReturnScreenBottomBar(
                        apiViewModel,
                        readerViewModel,
                        menuCode,
                        tagCount,
                        navController,
                        returnScreenArgs
                    )
                }
            }, modifier = modifier.background(WhiteColor)
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

                ReturnScreenContent(
                    readerViewModel, apiViewModel, args = returnScreenArgs, navController
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReturnScreenContent(
    readerViewModel: ReaderViewModel,
    apiViewModel: ApiViewModel,
    args: ReturnScreenArgs,
    navController: NavHostController
) {

    val errorMessage = readerViewModel.error().observeAsState("")
    val apiError = apiViewModel.apiErrorMsg.observeAsState("")
    val isApiLoading = apiViewModel.isLoading.observeAsState(false)

    val isPickOn = readerViewModel.isPickOn().observeAsState(false)
    val tagInfoData = readerViewModel.pickData().observeAsState()
    val isTagWriteDone = readerViewModel.isTagWriteDone().observeAsState(false)
    val isTagWriteOn = readerViewModel.isTagWriteOn().observeAsState(false)
    val isProcessOn = readerViewModel.isProcessOn().observeAsState(false)
    val triggerPressed = readerViewModel.isTriggerPressed().observeAsState(false)
    //val note = remember { mutableStateOf("") }
    val infoList = remember { mutableStateListOf<Pair<String, String>>() }
    val response = apiViewModel.apiResult.collectAsState(null)
    val showImagePreview = remember { mutableStateOf(false) }
    val selectedPainter = remember { mutableStateOf<Painter?>(null) }

    val selectedSearchType = remember { mutableStateOf("") }


    LaunchedEffect(tagInfoData.value) {
        if (tagInfoData.value != null) {
            if (isInternetConnected(args.context)) {
                apiViewModel.callApi(
                    UrlConstants.GET_PASSWORD, appendData = DataStoreManager.readFromPreferences(
                        LoginConstants.DEVICE_LOCATION_ID, ""
                    )
                )
            }
        }
    }

    LaunchedEffect(errorMessage.value) {
        val message = errorMessage.value
        if (!message.isNullOrBlank()) {
            LogUtils.showLog("isTagNotFound", "DecodingContent: $message")

        }
    }

    LaunchedEffect(isTagWriteDone.value) {
        if (isTagWriteDone.value == true) {
            LogUtils.showLog("isTagWriteDone", "DecodingBottomBar: ${isTagWriteDone.value}")

        }
    }

    LaunchedEffect(triggerPressed.value) {
        if (triggerPressed.value != true) return@LaunchedEffect
        readerViewModel.setTriggerValue(false)
        if (chkTrue(isApiLoading.value)) return@LaunchedEffect
        if (navController.currentBackStackEntry?.lifecycle?.currentState != Lifecycle.State.RESUMED) return@LaunchedEffect
        //if(args.transactionType.value.isNullOrEmpty()) return@LaunchedEffect
        if (args.showSheet.value != false && args.currentSheet.value == BottomSheetType.SEARCH) {
            //Based on Selected Epc or TID
            if (selectedSearchType.value.isNotEmpty() && args.latestEan.value != null)
                readerViewModel.toggleSearch(
                    if (selectedSearchType.value.equals(
                            "epc",
                            true
                        )
                    ) SearchTypeConstant.EPC else SearchTypeConstant.TID,
                    if (selectedSearchType.value.equals(
                            "epc",
                            true
                        )
                    ) args.latestEan.value!!.epc else args.latestEan.value!!.tid
                )
//        readerViewModel.toggleSearch(selectedSearchType.value,if(selectedSearchType.value.equals("epc",true)) latestEan.value!!.epc else latestEan.value!!.tid)
        } else {
            if (chkTrue(isProcessOn.value)) return@LaunchedEffect
            if (args.showSheet.value != false && args.currentSheet.value != BottomSheetType.NONE) return@LaunchedEffect
            if (args.transactionType.value.isNotEmpty()) {
                val preHeader = TopicConstants.DECODE + "_" + args.menuCode + "_"
                val deviceSessionId = DataStoreManager.readFromPreferences(
                    preHeader + ParameterConstants.DEVICE_SESSION_ID,
                    ""
                )
                if (deviceSessionId.isEmpty()) {
                    val sessionId = SessionUtils.generateOfflineSessionId(args.menuCode)
                    DataStoreManager.saveToPreferences(
                        preHeader + ParameterConstants.DEVICE_SESSION_ID,
                        sessionId
                    )
                    readerViewModel.setSessionId(sessionId)
                }
                readerViewModel.performPick(pickPower = args.setPower.value, pickTime = 500)
            }
        }
    }

    LaunchedEffect(response.value) {
        if(response.value==null) return@LaunchedEffect
        val result = response.value
        if (result?.isSuccess != true) {
            val message = result?.errMsg
            if (!message.isNullOrBlank()) {
                /*snackbarController.show(
                    ErrorAppSnackBarData(result?.errMsg.toString())
                )*/
            }
        } else if (result.response != null) {
            val jsonResponse = result.response
            when (result.url) {
                UrlConstants.GET_PASSWORD -> {
                    handleReturn(result, readerViewModel, tagInfoData.value)
                }

                UrlConstants.PRODUCTS -> {
                    handleProductDetails(
                        apiResult = result,
                        fieldList = infoList,
                    )
                }
            }
        }
    }

    val rfidStepState = remember {
        derivedStateOf {
            when {
                tagInfoData.value != null || isTagWriteDone.value == true -> StepUiState.COMPLETED
                isPickOn.value == true -> StepUiState.ACTIVE
                tagInfoData.value == null && errorMessage.value.isNotEmpty() -> StepUiState.ERROR
                else -> StepUiState.IDLE
            }
        }
    }

    val returnStepState = remember {
        derivedStateOf {
            when {
                rfidStepState.value != StepUiState.COMPLETED -> StepUiState.IDLE
                isTagWriteDone.value == true -> StepUiState.COMPLETED
                isTagWriteOn.value == true -> StepUiState.ACTIVE
                !chkTrue(isTagWriteDone.value) && tagInfoData != null && !isTagWriteOn.value && (errorMessage.value.isNotEmpty() || apiError.value.isNotEmpty()) -> StepUiState.ERROR
                else -> StepUiState.IDLE
            }
        }
    }

    val messageHeading: String = when {
        returnStepState.value != StepUiState.IDLE -> messageForStep(
            returnStepState.value,
            active = "Processing...",
            completed = "Tag Returned Successfully",
            error = chkNull(errorMessage.value, apiError.value)
        ) ?: ""

        rfidStepState.value != StepUiState.IDLE -> messageForStep(
            rfidStepState.value,
            active = "Scanning RFID...",
            completed = "RFID Scan Done",
            error = errorMessage.value
        ) ?: ""

        else -> ""
    }

    val steps = listOf(
        StepItem(
            icon = R.drawable.property_rfidd,
            label = "RFID",
            status = statusForState(rfidStepState.value)
        ), StepItem(
            icon = R.drawable.property_return,
            label = "Return",
            status = statusForState(returnStepState.value)
        )
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.Top) {
                TopBarContent(args.label, onBackClickL = {
                    args.scope.launch {
                        if (chkTrue(isProcessOn.value) || chkTrue(isApiLoading.value)) return@launch
                        val preHeader = TopicConstants.DECODE + "_" + args.menuCode + "_"
                        val deviceSessionId = DataStoreManager.readFromPreferences(
                            preHeader + ParameterConstants.DEVICE_SESSION_ID,
                            ""
                        )
                        if (deviceSessionId.isEmpty()) {
                            clearSavedSessionValues(args.context, args.menuCode)
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
                    .fillMaxSize()
                    .weight(1f)
            ) {
                if (args.tagCount.value > 0 || args.uploadedTagCount.value > 0) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 16.dp, end = 16.dp)
                    ) {
                        UploadStatusChip(args.uploadedTagCount.value, args.tagCount.value)
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(dimensionResource(id = R.dimen.dp_16))
                        .clickable(
                            onClick = {},
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }),
                    horizontalAlignment = Alignment.CenterHorizontally
                )
                {
                    CounterText(
                        current = args.tagCount.value, total = "", isLimitShow = false
                    )

                    Text(
                        text = stringResource(id = R.string.total_tags_returned),
                        style = CommonTypography.current.noteText,
                        color = BlackColor,
                        modifier = Modifier.padding(bottom = dimensionResource(R.dimen.dp_12))
                    )

                    Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_56)))

                    if (messageHeading.isNotBlank()) {
                        Text(
                            text = messageHeading,
                            style = CommonTypography.current.textSemiBold,
                            textAlign = TextAlign.Center
                        )
                    }

                    StepIndicatorRow(
                        steps = steps, modifier = Modifier.padding(16.dp), stepStates = listOf(
                            rfidStepState.value, returnStepState.value
                        )
                    )

                }
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {

            args.latestEan.value?.let {
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
                    epcNo = it,
                    searchIcon = R.drawable.property_gieger_og
                )
            }
        }

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
                            .fillMaxHeight(0.33f)
                    ) {
                        DecodeDeviceSettings(
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
                            .fillMaxHeight(0.48f)
                    ) {
                        SetDevicePower(
                            showSheet = args.showSheet,
                            sheetState = args.sheetState,
                            scope = args.scope,
                            currentPower = args.setPower.value,
                            onPowerSet = {
                                args.setPower.value = it
                                readerViewModel.setPower(args.setPower.value)
                                val preHeader = TopicConstants.DECODE + "_" + args.menuCode + "_"
                                DataStoreManager.saveToPreferences(
                                    preHeader + ParameterConstants.READER_POWER,
                                    args.setPower.value
                                )
                            },
                        )
                    }

                    BottomSheetType.SEARCH -> Box(
                        modifier = Modifier
                            .background(WhiteColor)
                            .fillMaxWidth()
                            .fillMaxHeight(0.57f)
                    ) {
                        SearchBottomSheetView(
                            showSheet = args.showSheet,
                            sheetState = args.sheetState,
                            scope = args.scope,
                            selectedSearchType = selectedSearchType,
                            latestEan = args.latestEan,
                            readerViewModel = readerViewModel
                        )
                    }

                    BottomSheetType.SESSION -> Box(
                        modifier = Modifier
                            .background(WhiteColor)
                            .fillMaxWidth()
                            .fillMaxHeight(0.35f)
                    ) {
                        val subHeading = if (args.tagCount.value > 0) {
                            buildAnnotatedString {
                                append(stringResource(R.string.return_end_the_session))
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append(
                                        " ${args.tagCount.value} "
                                    )
                                }
                                append(stringResource(R.string.please_confirm))
                            }
                        } else {
                            AnnotatedString(stringResource(R.string.return_end_the_session_0))
                        }
                        SessionContent(
                            showSheet = args.showSheet,
                            sheetState = args.sheetState,
                            scope = args.scope,
                            navController = navController,
                            tagCount = args.tagCount.value,
                            subHeading = subHeading,
                            onStopSession = {
                                if (chkTrue(isProcessOn.value) || chkTrue(isApiLoading.value)) return@SessionContent
                                args.scope.launch {
                                    clearSavedSessionValues(args.context, args.menuCode)
                                    if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                                        readerViewModel.clearSessionAndTransactionType()
                                        navController.popBackStack()
                                    }
                                }
                            },
                            readerViewModel = readerViewModel,
                            isAllowContinue = false
                        )
                    }

                    BottomSheetType.PRODUCT_DETAILS ->
                        Box(
                            modifier = Modifier
                                .background(WhiteColor)
                                .fillMaxWidth()
                                .fillMaxHeight(0.55f)
                        ) {
                            args.latestEan.value?.let{ ean ->
                                ProductDetailsByEan(
                                    tagTime=ean,
                                    infoList = infoList,
                                    title = stringResource(R.string.product_details_),
                                    sheetState= args.sheetState,
                                    scope =args.scope,
                                    showSheet= args.showSheet,
                                    isTrillingIcon = false,
                                    showImagePreview = showImagePreview,
                                    selectedPainter = selectedPainter,
                                    readerViewModel = readerViewModel,
                                 )
                            }
                        }

                    else -> LogUtils.showLog("TAG", "DecodingContent: ")

                }
            }
        }

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            if (showImagePreview.value) {
                ImageViewFullScreen(showImagePreview, selectedPainter)
            }
        }
    }
}

data class ReturnScreenArgs @OptIn(ExperimentalMaterial3Api::class) constructor(
    val context: Context,
    val label: String,
    val menuCode: String,
    val scope: CoroutineScope,
    val sheetState: SheetState,
    val showSheet: MutableState<Boolean>,
    val currentSheet: MutableState<BottomSheetType>,
    val tagCount: State<Int>,
    val uploadedTagCount: State<Int>,
    val setPower: MutableState<Int>,
    val deviceSessionId: String,
    val transactionType: MutableState<String>,
    val latestEan: State<TagTime?>
)

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReturnScreenBottomBar(
    apiViewModel: ApiViewModel,
    readerViewModel: ReaderViewModel,
    menuCode: String,
    tagCount: State<Int>,
    navController: NavHostController,
    args: ReturnScreenArgs,

    ) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(WhiteColor)
    ) {
        HorizontalDivider(color = OutlineDefault)

        Row(
            modifier = Modifier.padding(dimensionResource(R.dimen.dp_16)),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.dp_16)),
            verticalAlignment = Alignment.CenterVertically
        ) {

            CommonButton(
                text = stringResource(id = R.string.return_tag),
                onClick = {
                    readerViewModel.setTriggerValue(true)
                },
                modifier = Modifier.weight(1f),
                enabled = args.transactionType.value.isNotEmpty()
            )

            if (tagCount.value > 0 && args.transactionType.value.isNotEmpty()) {
                CommonButton(
                    text = stringResource(id = R.string.view_tags_list, tagCount.value),
                    onClick = {
                        val data = mapOf(
                            "transactionType" to "${args.transactionType.value}",
                            "tagCount" to "${tagCount.value}"
                        )
                        navController.navigate(
                            Screen.DecodingTagListScreen.createRoute(
                                label = args.label,
                                code = menuCode,
                                params = data
                            )
                        )
                    }, modifier = Modifier.weight(1f),
                    gradientBrush = SolidColor(WhiteColor),
                    contentColor = BlackColor
                )
            }
        }
    }

}

fun handleReturn(
    apiResult: ApiResult?,
    readerViewModel: ReaderViewModel,
    tagInfoData: TagInfoEntity? = null,
    listTagInfoData: List<TagInfoEntity>? = null,
) {
    val rootJson = apiResult?.response ?: return

    if (tagInfoData == null && listTagInfoData.isNullOrEmpty()) {
        LogUtils.showLog("Decoding", "tagInfoData is NULL")
        return
    }

    //Current Password
    val currentPasswordObj =
        extractJSONObject(rootJson, ParameterConstants.CURRENT_PASSWORD, JSONObject())
    val currentPassword = extractString(currentPasswordObj, ParameterConstants.VALUE, "")

    //Old Passwords
    val oldPasswordsObj =
        extractJSONObject(rootJson, ParameterConstants.OLD_PASSWORDS, JSONObject())
    val oldPasswordArray = extractJSONArray(oldPasswordsObj, ParameterConstants.VALUE, JSONArray())

    val oldPasswords = mutableListOf<String>()
    for (i in 0 until oldPasswordArray.length()) {
        oldPasswords.add(oldPasswordArray.optString(i))
    }

    DataStoreManager.savePasswords(currentPassword, oldPasswords)
    /*if(!listTagInfoData.isNullOrEmpty()) readerViewModel.performReturn(listTagInfoData, currentPassword, oldPasswords)
    else*/ if (tagInfoData != null) readerViewModel.performReturn(
        tagInfoData,
        currentPassword,
        oldPasswords
    )
}


fun clearSavedSessionValues(args: ReturnScreenArgs) {
    clearSavedSessionValues(args.context, args.menuCode)
}

fun clearSavedSessionValues(context: Context, menuCode: String) {
    val preHeader = TopicConstants.DECODE + "_" + menuCode + "_"
    DataStoreManager.saveToPreferences(preHeader + ParameterConstants.DEVICE_SESSION_ID, "")
    DataStoreManager.saveToPreferences(preHeader + ParameterConstants.TRANSACTION_TYPE, "")
    DataStoreManager.saveToPreferences(preHeader + ParameterConstants.READER_POWER, 7)
    CoroutineScope(Dispatchers.IO).launch {
        val db = AppDatabase.getDbInstance(context)
        db.tagInfoDao().deleteByTopicAndSessionType(TopicConstants.DECODE, menuCode)
    }
}
