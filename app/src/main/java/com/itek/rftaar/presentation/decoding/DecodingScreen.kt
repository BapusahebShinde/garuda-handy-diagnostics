package com.itek.rftaar.presentation.decoding

import android.content.Context
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.key
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.itek.rftaar.data.model.LabelName
import com.itek.rftaar.data.model.TagTime
import com.itek.rftaar.mqtt.constants.SearchTypeConstant
import com.itek.rftaar.mqtt.constants.TopicConstants
import com.itek.rftaar.presentation.commonComp.BottomSheetTextIconRow
import com.itek.rftaar.presentation.commonComp.BottomSheetType
import com.itek.rftaar.presentation.commonComp.CardViewWithHSI
import com.itek.rftaar.presentation.commonComp.CommonButton
import com.itek.rftaar.presentation.commonComp.CounterText
import com.itek.rftaar.presentation.commonComp.DeviceSettingItem
import com.itek.rftaar.presentation.commonComp.ImageViewFullScreen
import com.itek.rftaar.presentation.commonComp.MessageBottomSheet
import com.itek.rftaar.presentation.commonComp.MessageUiState
import com.itek.rftaar.presentation.commonComp.NoteUiState
import com.itek.rftaar.presentation.commonComp.NoteWithTextAndButton
import com.itek.rftaar.presentation.commonComp.SessionUiActions
import com.itek.rftaar.presentation.commonComp.StepIndicatorRow
import com.itek.rftaar.presentation.commonComp.StepItem
import com.itek.rftaar.presentation.commonComp.StepUiState
import com.itek.rftaar.presentation.commonComp.TopBarContent
import com.itek.rftaar.presentation.commonComp.UploadStatusChip
import com.itek.rftaar.presentation.commonComp.messageForStep
import com.itek.rftaar.presentation.commonComp.openSheet
import com.itek.rftaar.presentation.commonComp.statusForState
import com.itek.rftaar.presentation.encoding.SearchBottomSheetView
import com.itek.rftaar.presentation.encoding.SessionContent
import com.itek.rftaar.presentation.encoding.SetDevicePower
import com.itek.rftaar.presentation.encoding.callProductDetails
import com.itek.rftaar.presentation.navigation.Screen
import com.itek.rftaar.presentation.viewmodel.ApiViewModel
import com.itek.rftaar.presentation.viewmodel.ReaderViewModel
import com.itek.rftaar.ui.theme.BlackColor
import com.itek.rftaar.ui.theme.BorderGreen
import com.itek.rftaar.ui.theme.CommonTypography
import com.itek.rftaar.ui.theme.Green
import com.itek.rftaar.ui.theme.OutlineDefault
import com.itek.rftaar.ui.theme.RedColor
import com.itek.rftaar.ui.theme.TabColor
import com.itek.rftaar.ui.theme.TextGrey
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
fun DecodingScreen(
    modifier: Modifier,
    navController: NavHostController,
    label: String,
    menuCode: String,
    apiViewModel: ApiViewModel = hiltViewModel(),
    searchParams: Map<String, Any> = emptyMap(),
) {
    LogUtils.showLog("Screen","DecodingScreen")
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val activity = context as? ReaderActivity
    val readerViewModel = if (activity != null) activity.findReaderViewModel() else hiltViewModel()
    val preHeader = TopicConstants.DECODE + "_" + menuCode + "_"
    val deviceSessionId = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.DEVICE_SESSION_ID, "")
    val readerPower = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.READER_POWER, 7)
    val transactionType = rememberSaveable { mutableStateOf("") }
    val currentSelectedLabel = rememberSaveable { mutableStateOf<String?>(null) }
    //val currentSelectedName = rememberSaveable { mutableStateOf<String?>(null) }
    val pendingSelectedItemLabel = rememberSaveable { mutableStateOf<String?>(null) }
    val pendingSelectedItemName = rememberSaveable { mutableStateOf<String?>(null) }
    LogUtils.showLog("transactionType", "DecodingScreen: ${transactionType.value}")
    if (activity != null) {
        readerViewModel.onCreate()
        readerViewModel.setSessionAndTransactionType(menuCode, transactionType.value,topic= TopicConstants.DECODE)
        if(deviceSessionId.isNotEmpty()) readerViewModel.setSessionId(deviceSessionId)
    }
    val note = remember { mutableStateOf("") }
    val showReasonSheet = remember { mutableStateOf(false) }
    val isOthersDecoding = transactionType.value.equals("Other", true)

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
    val tagCount = db.tagInfoDao().getTagWriteCount(menuCode, transactionType.value).observeAsState(0)
    val uploadedTagCount = db.tagInfoDao().getUploadedTagWriteCount(menuCode, transactionType.value).observeAsState(0)
    val latestEan = db.tagInfoDao().getLatestEncodedTag(menuCode, transactionType.value).collectAsState(initial = null)

    LaunchedEffect(Unit) {
        if (isInternetConnected(context)) {
            val map = HashMap<String, String>()
            map.put(ParameterConstants.CUSTOMER_ID,DataStoreManager.readFromPreferences(ParameterConstants.CUSTOMER_ID, ""))
            map.put(ParameterConstants.BUSINESS_LINE_ID,DataStoreManager.readFromPreferences(ParameterConstants.BUSINESS_LINE_ID, ""))
            apiViewModel.callApi(UrlConstants.GET_DECODE_TYPES, queryMap = map)
        }
    }

    val focusManager = LocalFocusManager.current
    val isFocused = remember { mutableStateOf(false) }
    val decodedList = rememberSaveable { mutableStateListOf<LabelName>() }

    LaunchedEffect(transactionType.value,note.value) {
        readerViewModel.setSessionAndTransactionType(menuCode, transactionType.value,if(transactionType.value.equals("OTHER", true)) note.value else "", topic=TopicConstants.DECODE)
    }


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
            }else {
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(WhiteColor),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    val bottomBarArgs = BottomBarArgs(
                        scope = scope,
                        sheetState = sheetState,
                        showSheet = showSheet,
                        currentSheet = currentSheet,
                        transactionType = transactionType,
                        setPower = setPower,
                        currentSelectedLabel = currentSelectedLabel,
                        pendingSelectedItemLabel = pendingSelectedItemLabel,
                        pendingSelectedItemName = pendingSelectedItemName,
                        navController = navController,
                        decodedList = decodedList,
                    )
                    DecodingBottomBar(
                        apiViewModel, readerViewModel, label,menuCode, tagCount, args = bottomBarArgs,
                        navController
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
                val decodingArgs = DecodingScreenArgs(
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
                    note = note,
                    currentSelectedLabel = currentSelectedLabel,
                    pendingSelectedItemLabel = pendingSelectedItemLabel,
                    pendingSelectedItemName = pendingSelectedItemName,
                    latestEan = latestEan,
                    decodedList = decodedList,
                    focusManager = focusManager,
                    isFocused = isFocused

                )
                DecodingContent(
                    readerViewModel, apiViewModel, args = decodingArgs, navController
                )
            }
        }
    }
}

data class DecodingScreenArgs @OptIn(ExperimentalMaterial3Api::class) constructor(
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
    val note: MutableState<String>,
    val pendingSelectedItemLabel: MutableState<String?>,
    val latestEan: State<TagTime?>,
    val decodedList: SnapshotStateList<LabelName>,
    val focusManager: FocusManager,
    val isFocused: MutableState<Boolean>,
    val currentSelectedLabel: MutableState<String?>,
    val pendingSelectedItemName: MutableState<String?>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DecodingContent(
    readerViewModel: ReaderViewModel,
    apiViewModel: ApiViewModel,
    args: DecodingScreenArgs,
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

    val selectedSearchType = remember{ mutableStateOf("")}


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
            args.note.value = ""
        }
    }

    LaunchedEffect(triggerPressed.value) {
        if (triggerPressed.value != true) return@LaunchedEffect
        readerViewModel.setTriggerValue(false)
        if (chkTrue(isApiLoading.value)) return@LaunchedEffect
        if(navController.currentBackStackEntry?.lifecycle?.currentState != Lifecycle.State.RESUMED) return@LaunchedEffect
        if(args.transactionType.value.isNullOrEmpty()) return@LaunchedEffect
        if (args.showSheet.value != false && args.currentSheet.value == BottomSheetType.PRODUCT_DETAILS && args.latestEan.value!=null) {
            //Check If Selected EPC is decoded & not Returned
            //readerViewModel.performReturn(args.latestEan.value!!)
            /*if(tagTime!=null && tagTime.tagInfoId>0){
                LogUtils.showLog("selTagTimeId",""+tagTime.tagInfoId)
                CoroutineScope(Dispatchers.IO).launch {
                    val tagInfo = db.tagInfoDao().getById(tagTime.tagInfoId)
                    LogUtils.showLog("selTagInfo",""+tagInfo)
                    if(tagInfo!=null) {
                        args.scope.launch { readerViewModel.performReturn(tagInfo) }
                    }
                }
            }*/
        }
        else if (args.showSheet.value != false && args.currentSheet.value == BottomSheetType.SEARCH) {
            //Based on Selected Epc or TID
            if(selectedSearchType.value.isNotEmpty() && args.latestEan.value!=null)
                readerViewModel.toggleSearch(if(selectedSearchType.value.equals("epc",true)) SearchTypeConstant.EPC else SearchTypeConstant.TID,if(selectedSearchType.value.equals("epc",true)) args.latestEan.value!!.epc else args.latestEan.value!!.tid)
//        readerViewModel.toggleSearch(selectedSearchType.value,if(selectedSearchType.value.equals("epc",true)) latestEan.value!!.epc else latestEan.value!!.tid)
        }
        else {
            if (chkTrue(isProcessOn.value)) return@LaunchedEffect
            if(args.showSheet.value!=false && args.currentSheet.value != BottomSheetType.NONE) return@LaunchedEffect
            if(args.transactionType.value.isNotEmpty()) {
                if (args.transactionType.value.equals("OTHER", true) && args.note.value.isNullOrEmpty()) {
                    openSheet(
                        scope = args.scope,
                        sheetState = args.sheetState,
                        showSheet = args.showSheet,
                        currentSheet = args.currentSheet,
                        sheet = BottomSheetType.COMMENT
                    )
                    return@LaunchedEffect
                }
                val preHeader = TopicConstants.DECODE + "_" + args.menuCode + "_"
                val deviceSessionId = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.DEVICE_SESSION_ID, "")
                if (deviceSessionId.isEmpty()) {
                    val sessionId = SessionUtils.generateOfflineSessionId(args.menuCode)
                    DataStoreManager.saveToPreferences(preHeader + ParameterConstants.DEVICE_SESSION_ID, sessionId)
                    readerViewModel.setSessionId(sessionId)
                }
                readerViewModel.performPick(pickPower = args.setPower.value,pickTime=500)
            }
        }
    }

    LaunchedEffect(response.value) {
        if(response.value==null) return@LaunchedEffect
        val result = response.value!!
        if (!result.isSuccess) { }
        else if (response.value != null) {
                when (result.url) {
                    UrlConstants.GET_DECODE_TYPES -> {
                        val listDecodeTypes = parseDecodeTypes(result)
                        args.decodedList.clear()
                        if(listDecodeTypes.isNotEmpty()) args.decodedList.addAll(listDecodeTypes)
                    }
                    UrlConstants.GET_PASSWORD -> handleDecode(
                        result,
                        readerViewModel,
                        tagInfoData.value,
                    )
                    UrlConstants.PRODUCTS ->
                        handleProductDetails(
                            apiResult = result,
                            fieldList = infoList,
                        )
                }
            }
    }


    LaunchedEffect(args.decodedList) {
        LogUtils.showLog("DecodedList", "Size = ${args.decodedList.size}")
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

    val decodeStepState = remember {
        derivedStateOf {
            when {
                rfidStepState.value != StepUiState.COMPLETED -> StepUiState.IDLE
                isTagWriteDone.value == true -> StepUiState.COMPLETED
                isTagWriteOn.value == true -> StepUiState.ACTIVE
                !chkTrue(isTagWriteDone.value) && tagInfoData!=null && !isTagWriteOn.value && (errorMessage.value.isNotEmpty() || apiError.value.isNotEmpty()) -> StepUiState.ERROR
                else -> StepUiState.IDLE
            }
        }
    }

    val messageHeading: String = when {
        decodeStepState.value != StepUiState.IDLE -> messageForStep(
            decodeStepState.value,
            active = "Processing...",
            completed = "RFID Decoded Successfully",
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
    val messageSubHeading: String = when {
        decodeStepState.value == StepUiState.ERROR -> chkNull(errorMessage.value, apiError.value)

        rfidStepState.value == StepUiState.ERROR -> errorMessage.value

        decodeStepState.value == StepUiState.ACTIVE -> "Decoding in progress..."

        rfidStepState.value == StepUiState.ACTIVE -> "Bring the RFID tag close to the reader "

        decodeStepState.value == StepUiState.COMPLETED -> "RFID Decoded Successfully!"

        rfidStepState.value == StepUiState.COMPLETED -> "Decoding in progress..."

        else -> ""
    }

    val steps = listOf(
        StepItem(
            icon = R.drawable.property_rfidd,
            label = "RFID",
            status = statusForState(rfidStepState.value)
        ), StepItem(
            icon = R.drawable.property_rfid_encoded,
            label = "Decode",
            status = statusForState(decodeStepState.value)
        )
    )



    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.Top) {
                TopBarContent(args.label, onBackClickL = {
                    args.scope.launch {
                        if (chkTrue(isProcessOn.value)|| chkTrue(isApiLoading.value)) return@launch
                        val preHeader = TopicConstants.DECODE + "_" + args.menuCode + "_"
                        val deviceSessionId = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.DEVICE_SESSION_ID, "")
                        if (deviceSessionId.isEmpty()) {
                            clearSavedSessionValues(args.context, args.menuCode)
                            if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                                readerViewModel.clearSessionAndTransactionType()
                                navController.popBackStack()
                            }
                        }
                        else {
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
            Box(modifier = Modifier
                .fillMaxSize()
                .weight(1f)) {
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
                        text = stringResource(id = R.string.total_tags_decoded),
                        style = CommonTypography.current.noteText,
                        color = BlackColor,
                        modifier = Modifier.padding(bottom = dimensionResource(R.dimen.dp_12))
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {

                            Row(
                                modifier = Modifier
                                    .horizontalScroll(rememberScrollState())
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.dp_8))
                            )
                            {

                                args.decodedList.take(8).forEach { data ->

                                    val isSelected = args.transactionType.value == data.name

                                    key(data.name) {
                                        Card(
                                            modifier = Modifier
                                                .width(dimensionResource(R.dimen.dp_76))
                                                .height(dimensionResource(R.dimen.dp_68))
                                                .border(
                                                    width = dimensionResource(if (isSelected) R.dimen.dp_2 else R.dimen.dp_1),
                                                    color = if (isSelected) BorderGreen else TabColor,
                                                    shape = RoundedCornerShape(dimensionResource(R.dimen.dp_16))
                                                )
                                                .clickable(
                                                    onClick = {

                                                        val currentSelected = args.transactionType.value
                                                        //val clickedItem = data.label

                                                        if (currentSelected.isBlank()) { 
                                                            args.currentSelectedLabel.value = data.label
                                                            args.transactionType.value = data.name
                                                            return@clickable }

                                                        if (currentSelected == data.name) {
                                                            return@clickable
                                                        }

                                                        args.pendingSelectedItemName.value = data.name
                                                        args.pendingSelectedItemLabel.value = data.label


                                                        openSheet(
                                                            scope = args.scope,
                                                            sheetState = args.sheetState,
                                                            showSheet = args.showSheet,
                                                            currentSheet = args.currentSheet,
                                                            sheet = BottomSheetType.MESSAGE
                                                        )
                                                    },
                                                    indication = null,
                                                    interactionSource = remember { MutableInteractionSource() }
                                                ),
                                            colors = CardDefaults.cardColors(containerColor = Color.White),
                                            shape = RoundedCornerShape(dimensionResource(R.dimen.dp_16))
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .wrapContentHeight()
                                                    .padding(vertical = dimensionResource(R.dimen.dp_12)),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {

                                                Image(
                                                    painter = painterResource(
                                                        if (isSelected) R.drawable.success else getTypeIcon(
                                                            data.name
                                                        )
                                                    ),
                                                    contentDescription = null,
                                                    modifier = Modifier.size(dimensionResource(R.dimen.dp_24))
                                                )

                                                Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_4)))

                                                Text(
                                                    text = data.label,
                                                    style = CommonTypography.current.noteText.copy(
                                                        color = if (isSelected) Green else TextGrey
                                                    ),
                                                    textAlign = TextAlign.Center,
                                                    maxLines = 1,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .basicMarquee()
                                                        .padding(horizontal = 5.dp),
                                                )
                                            }
                                        }
                                    }
                                }
                            }
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

                    /**if (messageSubHeading.isNotBlank()) {
                    Text(
                    text = messageSubHeading,
                    style = CommonTypography.current.noteText,
                    textAlign = TextAlign.Center
                    )
                    }*/

                    StepIndicatorRow(
                        steps = steps, modifier = Modifier.padding(16.dp), stepStates = listOf(
                            rfidStepState.value, decodeStepState.value
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

            if (args.transactionType.value.isBlank()) {
                Text(
                    text = "Choose the decoding type to begin decoding RFID tags .",
                    style = CommonTypography.current.noteText,
                    textAlign = TextAlign.Center,
                    color = RedColor
                )
            }

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
                            .wrapContentHeight()
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
                            .fillMaxHeight(0.45f)
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
                                DataStoreManager.saveToPreferences(preHeader + ParameterConstants.READER_POWER, args.setPower.value)
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
                                append(stringResource(R.string.decoded_end_the_session))
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) { append(" ${args.tagCount.value} ") }
                                append(stringResource(R.string.please_confirm))
                            }
                        } else {
                            AnnotatedString(stringResource(R.string.decoded_end_the_session_0))
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
                                    clearSavedSessionValues(args.context,args.menuCode)
                                    if(navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                                        readerViewModel.clearSessionAndTransactionType()
                                        navController.popBackStack()
                                    }
                                }
                            },
                            readerViewModel = readerViewModel,
                            isAllowContinue = false
                        )
                    }

                    BottomSheetType.MESSAGE -> Box(
                        modifier = Modifier
                            .background(WhiteColor)
                            .fillMaxWidth()
                            .wrapContentHeight()
                    ) {
                        MessageBottomSheetContent(
                            showSheet = args.showSheet,
                            sheetState = args.sheetState,
                            scope = args.scope,
                            pendingSelectedItemLabel = args.pendingSelectedItemLabel,
                            pendingSelectedItemName = args.pendingSelectedItemName,
                            currentSelectedLabel = args.currentSelectedLabel,
                            transactionType = args.transactionType,
                        )
                    }


                    BottomSheetType.COMMENT -> Box(
                        modifier = Modifier
                            .background(WhiteColor)
                            .fillMaxWidth()
                            .imePadding()
                            .fillMaxHeight(0.43f)
                    ) {

                    val uiState = NoteUiState(
                            title = stringResource(R.string.decoding_reason),
                            heading = stringResource(R.string.reason_for_re_encoding),
                            subHeading = stringResource(R.string.item_was_recently_decoded),
                            primaryButtonText = stringResource(R.string.confirm),
                            note = args.note.value.trim(),
                            imeAction = ImeAction.Done,
                            onTextChange = {
                                args.note.value = it
                            },
                            textValue = stringResource(R.string.enter_reason),
                            isButtonEnabled = args.note.value.isNotBlank()
                        )

                        NoteWithTextAndButton(uiState = uiState, onClose = {
                            args.scope.launch {
                                args.sheetState.hide()
                                args.showSheet.value = false
                            }
                        }, onConfirm = {
                            args.scope.launch {
                                readerViewModel.setTriggerValue(true)
                                args.sheetState.hide()
                                args.showSheet.value = false
                            }
                        },
                            isFocused= args.isFocused
                        )
                    }

                    BottomSheetType.PRODUCT_DETAILS ->
                        Box(
                            modifier = Modifier
                                .background(WhiteColor)
                                .fillMaxWidth()
                                .fillMaxHeight(0.55f)
                        ) {
                            args.latestEan.value?.let{ tagTime ->
                                ProductDetailsByEan(
                                    tagTime = tagTime,
                                    infoList = infoList,
                                    title = stringResource(R.string.product_details_),
                                    sheetState = args.sheetState,
                                    scope =  args.scope,
                                    showSheet = args.showSheet,
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

        Box(modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center){
            if (showImagePreview.value) {
                ImageViewFullScreen(showImagePreview , selectedPainter)
            }
        }
    }
}

data class BottomBarArgs @OptIn(ExperimentalMaterial3Api::class) constructor(
    val scope: CoroutineScope,
    val sheetState: SheetState,
    val showSheet: MutableState<Boolean>,
    val currentSheet: MutableState<BottomSheetType>,
    val transactionType: MutableState<String>,
    val setPower: MutableState<Int>,
    val pendingSelectedItemLabel: MutableState<String?>,
    val navController: NavHostController,
    val decodedList: SnapshotStateList<LabelName>,
    val currentSelectedLabel: MutableState<String?>,
    val pendingSelectedItemName: MutableState<String?>,
)

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DecodingBottomBar(
    apiViewModel: ApiViewModel,
    readerViewModel: ReaderViewModel,
    label: String,
    menuCode: String,
    tagCount: State<Int>,
    args: BottomBarArgs,
    navController: NavHostController
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
                    text = stringResource(id = R.string.decode_tag),
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
                            args.navController.currentBackStackEntry?.savedStateHandle?.set(
                                "decodeList",
                                args.decodedList
                            )

                            val data = mapOf("transactionType" to "${args.transactionType.value}","tagCount" to "${tagCount.value}")
                            navController.navigate(Screen.DecodingTagListScreen.createRoute(label = label,code = menuCode, params = data))

/*                            args.navController.navigate(
                                "decodingTagListScreen/$menuCode/${args.transactionType.value}/${tagCount.value}"
                            )*/
                        }, modifier = Modifier.weight(1f),
                        gradientBrush = SolidColor(WhiteColor),
                        contentColor = BlackColor
                    )
                }
            }
        }

}

fun handleDecode(
    apiResult: ApiResult?,
    readerViewModel: ReaderViewModel,
    tagInfoData: TagInfoEntity?=null,
    listTagInfoData: List<TagInfoEntity>?=null,
    decodeType:String="",
) {
    LogUtils.showLog("handleDecode",decodeType)
    val rootJson = apiResult?.response ?: return

    if (tagInfoData == null && listTagInfoData.isNullOrEmpty()) {
        LogUtils.showLog("Decoding", "tagInfoData is NULL")
        return
    }

    //Current Password
    val currentPasswordObj = extractJSONObject(rootJson, ParameterConstants.CURRENT_PASSWORD, JSONObject())
    val currentPassword = extractString(currentPasswordObj, ParameterConstants.VALUE, "")

    //Old Passwords
    val oldPasswordsObj = extractJSONObject(rootJson, ParameterConstants.OLD_PASSWORDS, JSONObject())
    val oldPasswordArray = extractJSONArray(oldPasswordsObj, ParameterConstants.VALUE, JSONArray())

    val oldPasswords = mutableListOf<String>()
    for (i in 0 until oldPasswordArray.length()) {
        oldPasswords.add(oldPasswordArray.optString(i))
    }

    DataStoreManager.savePasswords(currentPassword,oldPasswords)
    if(!listTagInfoData.isNullOrEmpty()) readerViewModel.performDecoding(listTagInfoData, currentPassword, oldPasswords,decodeType)
    else if(tagInfoData!=null) readerViewModel.performDecoding(tagInfoData, currentPassword, oldPasswords,decodeType)
}


fun parseDecodeTypes(apiResult: ApiResult?): List<LabelName> {
    val rootJson = apiResult?.response ?: return emptyList()
    val decodeArray = extractJSONArray(rootJson, ParameterConstants.DATA, JSONArray())
    val result = mutableListOf<LabelName>()
    for (i in 0 until decodeArray.length()) {
        val obj = decodeArray.optJSONObject(i) ?: continue
        result.add(LabelName(label = extractString(obj, "label",""), name = extractString(obj, "name","")))
    }
    return result
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageBottomSheetContent(
    showSheet: MutableState<Boolean>,
    sheetState: SheetState,
    scope: CoroutineScope,
    pendingSelectedItemLabel: MutableState<String?>,
    transactionType: MutableState<String>,
    pendingSelectedItemName: MutableState<String?>,
    currentSelectedLabel: MutableState<String?>
) {
    val formatedText = buildAnnotatedString {
        append(stringResource(R.string.want_to_change_type, currentSelectedLabel.value?:"",pendingSelectedItemLabel.value?: ""))
    }

    val uiState = MessageUiState(
        title = stringResource(R.string.tab_changed),
        heading = stringResource(R.string.switching_tabs),
        subHeading = formatedText,
        primaryButtonText = stringResource(R.string.switch_tab),
        secondaryButtonText = stringResource(R.string.stay_here),
        icon = R.drawable.property_know_more,
        color1 = Color(0xFFF3B100),
        color2 = Color(0xFFFECF53)
    )

    val actions = SessionUiActions(onClose = {
        scope.launch {
            pendingSelectedItemLabel.value = null
            pendingSelectedItemName.value = null
            sheetState.hide()
            showSheet.value = false
        }
    }, onSecondaryAction = {
        scope.launch {
            pendingSelectedItemLabel.value = null
            pendingSelectedItemName.value = null
            sheetState.hide()
            showSheet.value = false
        }
    }, onPrimaryAction = {
        scope.launch {
            currentSelectedLabel.value = pendingSelectedItemLabel.value.toString()
            transactionType.value = pendingSelectedItemName.value.toString()
            pendingSelectedItemLabel.value = null
            pendingSelectedItemName.value = null
            sheetState.hide()
            showSheet.value = false
        }
    })

    MessageBottomSheet(
        uiState = uiState, actions = actions
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectTypeSheetContent(
    showSheet: MutableState<Boolean>,
    sheetState: SheetState,
    scope: CoroutineScope,
) {

    val formatedText = buildAnnotatedString {
        append(stringResource(R.string.choose_the_decoding_type))
    }

    val uiState = MessageUiState(
        title = stringResource(R.string.type_select),
        heading = stringResource(R.string.select_decoding_type),
        subHeading = formatedText,
        primaryButtonText = stringResource(R.string.switch_tab),
        secondaryButtonText = stringResource(R.string.stay_here),
        icon = R.drawable.property_decode_error,
        color1 = Color(0xFFD50000),
        color2 = Color(0xFFFF3131)
    )

    val actions = SessionUiActions(onClose = {
        scope.launch {
            sheetState.hide()
            showSheet.value = false
        }
    }, onSecondaryAction = {}, onPrimaryAction = {})

    MessageBottomSheet(
        uiState = uiState, actions = actions, isButton = false
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DecodeDeviceSettings(
    scope: CoroutineScope,
    sheetState: SheetState,
    showSheet: MutableState<Boolean>,
    currentSheet: MutableState<BottomSheetType>
) {

    val setPower = stringResource(R.string.set_power)
    val about = stringResource(R.string.about)

    val items = remember {
        listOf(
            DeviceSettingItem(
                iconRes = R.drawable.property_setting_power_decibel, title = setPower
            ) {
                currentSheet.value = BottomSheetType.POWER
            }, DeviceSettingItem(
                iconRes = R.drawable.property_know_more, title = about
            ) {

            })
    }

    BottomSheetTextIconRow(
        showSheet = showSheet,
        sheetState = sheetState,
        scope = scope,
        title = stringResource(R.string.settings),
        items = items
    )
}



fun getTypeIcon(name: String): Int {
    return when (name) {
        "OTHER" -> R.drawable.others_decoding
        "RTV" -> R.drawable.property_rtv
        "POS" -> R.drawable.property_decode_error
        "RETURN"-> R.drawable.property_return
        "IST" -> R.drawable.property_1_storefront
        "OMNI" -> R.drawable.omnichannel_decoding
        "GRDC" -> R.drawable.property_grdc
        else -> R.drawable.property_info
    }
}
fun clearSavedSessionValues(args: DecodingScreenArgs){
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






