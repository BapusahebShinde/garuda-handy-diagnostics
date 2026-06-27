package com.itek.rftaar.presentation.dashBoard

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
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
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.itek.rftaar.R
import com.itek.rftaar.ReaderActivity
import com.itek.rftaar.api.ApiResult
import com.itek.rftaar.api.constants.ParameterConstants
import com.itek.rftaar.api.constants.UrlConstants
import com.itek.rftaar.core.common.constants.MenuConstants
import com.itek.rftaar.core.common.utils.LogUtils
import com.itek.rftaar.core.common.utils.ParseUtils
import com.itek.rftaar.core.common.utils.ParseUtils.extractString
import com.itek.rftaar.core.database.AppDatabase
import com.itek.rftaar.core.database.DataStoreManager
import com.itek.rftaar.data.entity.TagInfoEntity
import com.itek.rftaar.data.model.ProductZoneFoundQty
import com.itek.rftaar.mqtt.constants.TopicConstants
import com.itek.rftaar.presentation.commonComp.AppSnackBarData
import com.itek.rftaar.presentation.commonComp.BottomSheetType
import com.itek.rftaar.presentation.commonComp.CodeType
import com.itek.rftaar.presentation.commonComp.CommonButton
import com.itek.rftaar.presentation.commonComp.CommonTextField
import com.itek.rftaar.presentation.commonComp.ErrorAppSnackBarData
import com.itek.rftaar.presentation.commonComp.FilterPicker
import com.itek.rftaar.presentation.commonComp.IconText
import com.itek.rftaar.presentation.commonComp.ImageViewFullScreen
import com.itek.rftaar.presentation.commonComp.ItekFooter
import com.itek.rftaar.presentation.commonComp.SnackbarController
import com.itek.rftaar.presentation.commonComp.TextFieldConfig
import com.itek.rftaar.presentation.commonComp.TopBarContent
import com.itek.rftaar.presentation.commonComp.openSheet
import com.itek.rftaar.presentation.decoding.parseProductDetails
import com.itek.rftaar.presentation.encoding.DeviceSettings
import com.itek.rftaar.presentation.encoding.VerificationResult
import com.itek.rftaar.presentation.encoding.VerificationUiState
import com.itek.rftaar.presentation.encoding.callProductDetails
import com.itek.rftaar.presentation.inventory.SetInvDevicePower
import com.itek.rftaar.presentation.inward.SegmentedTabView
import com.itek.rftaar.presentation.navigation.Screen
import com.itek.rftaar.presentation.search.SearchDetailsActionMode
import com.itek.rftaar.presentation.viewmodel.ApiViewModel
import com.itek.rftaar.presentation.viewmodel.ReaderViewModel
import com.itek.rftaar.ui.theme.BackGround
import com.itek.rftaar.ui.theme.BlackColor
import com.itek.rftaar.ui.theme.ButtonGray
import com.itek.rftaar.ui.theme.CommonTypography
import com.itek.rftaar.ui.theme.OutlineDefault
import com.itek.rftaar.ui.theme.WhiteColor
import com.itek.rftaar.ui.theme.Yellow
import com.itek.rftaar.utils.CommonUtils.chkNull
import com.itek.rftaar.utils.CommonUtils.chkTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

data class QuickSearchScreenArgs @OptIn(ExperimentalMaterial3Api::class) constructor(
    val context: Context,
    val scope: CoroutineScope,
    val sheetState: SheetState,
    val showSheet: MutableState<Boolean>,
    val setPower: MutableState<Int>,
    val preHeader: String,
    val currentSheet: MutableState<BottomSheetType>,
    val barCodeNo: MutableState<String>,
    val firstFieldList: SnapshotStateList<Pair<String, String>>,
    val selectedEan: MutableState<TagInfoEntity?>,
    val selectedSearchType: MutableState<String>,
    val tabList: List<String>,
    val prodObj: ProductZoneFoundQty?
)

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun QuickSearchScreen(
    modifier: Modifier,
    navController: NavController,
    apiViewModel: ApiViewModel = hiltViewModel(),
    menuCode: String,
    label: String,
    searchParams: Map<String, Any>,
) {

    val context = LocalContext.current
    val activity = context as? ReaderActivity
    val readerViewModel = if (activity != null) activity.findReaderViewModel() else hiltViewModel()
    if (activity != null) {
        readerViewModel.onCreate()
//        readerViewModel.setSessionAndTransactionType(menuCode)
    }
    val barCodeNo = rememberSaveable { mutableStateOf("") }
    val isBarcodeConfirmed = rememberSaveable { mutableStateOf(false) }
    val firstFieldList = rememberSaveable { mutableStateListOf<Pair<String, String>>() }

    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { sheetValue -> sheetValue != SheetValue.Hidden }
    )
    val showSheet = remember { mutableStateOf(false) }
    val currentSheet = remember { mutableStateOf(BottomSheetType.NONE) }
    val selectedEan = remember {mutableStateOf<TagInfoEntity?>(null)}
    val selectedSearchType = remember{ mutableStateOf("")}
    val selFilterKeys = remember { mutableStateListOf<String>()}
    val userId = DataStoreManager.readFromPreferences(ParameterConstants.USER_ID,"")
    if(selFilterKeys.isEmpty()) selFilterKeys.addAll(DataStoreManager.getListStr(userId + "_" + MenuConstants.PRODUCT_SEARCH + "_filters"))
    LogUtils.showLog("selFilterKeys",""+selFilterKeys.size)


    val db = AppDatabase.getDbInstance(context)
    val topic = extractString(searchParams,"topic", TopicConstants.SEARCH)//if(searchParams.containsKey("topic") && searchParams.get("topic") is String) searchParams.get("topic") as String else TopicConstants.SEARCH
    val sessionType = extractString(searchParams,"sessionType",menuCode)//if(searchParams.containsKey("sessionType") && searchParams.get("sessionType") is String) searchParams.get("sessionType") as String else menuCode
    val preHeader = topic + "_" + sessionType + "_"
    val transactionType = extractString(searchParams,"transactionType",DataStoreManager.readFromPreferences(preHeader+ ParameterConstants.TRANSACTION_TYPE,""))//if(searchParams.containsKey("transactionType") && searchParams.get("transactionType") is String) searchParams.get("transactionType") as String else DataStoreManager.readFromPreferences(preHeader+ ParameterConstants.TRANSACTION_TYPE,"")

    val prodObj = ParseUtils.extractObject<ProductZoneFoundQty>(searchParams,cls=ProductZoneFoundQty::class)
    LogUtils.showLog("prodObj",""+prodObj)
    val mode = if(prodObj==null) SearchDetailsActionMode.SEARCH_ONLY else SearchDetailsActionMode.valueOf(extractString(searchParams,"mode",SearchDetailsActionMode.SEARCH_FOUND.toString()))
    LogUtils.showLog("prodObj",""+prodObj)
    val foundCount= if(prodObj!=null) {if(mode.equals(SearchDetailsActionMode.SEARCH_FOUND)) db.productZoneDataDao().getFoundQty(topic,menuCode,transactionType,barCodeNo.value).collectAsState(0) else db.tagInfoDao().getTotalCount(menuCode, transactionType,barCodeNo.value).observeAsState(0)} else remember { mutableStateOf(0) }
    val tabList = if(mode.equals(SearchDetailsActionMode.SEARCH_AND_PICK_AND_DECODE) && foundCount.value>0) listOf("Search", "Pick", "Decode") else if(mode.equals(SearchDetailsActionMode.SEARCH_AND_PICK) || mode.equals(SearchDetailsActionMode.SEARCH_AND_PICK_AND_DECODE)) listOf("Search", "Pick") else emptyList() //ParseUtils.extractStringList(searchParams,"tabList",emptyList())//if(searchParams.containsKey("tabList") && searchParams.get("tabList") is List<*> && searchParams.get("tabList").all{ it is String }) searchParams["tabList"] as? List<String> ?: emptyList()

    val textValue = if(prodObj!=null) prodObj.barcode else extractString(searchParams,"textValue")
    val isSearchOptions = mode.equals(SearchDetailsActionMode.SEARCH_ONLY) && textValue.isNullOrEmpty()//searchParams["isSearchOptions"] as? Boolean ?: true

    val readerPower = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.READER_POWER, 7)
    val setPower = rememberSaveable { mutableStateOf(readerPower) }
    LaunchedEffect(setPower.value) {
        readerViewModel.setPower(setPower.value)
    }

    val args = QuickSearchScreenArgs(
        context = context,
        scope = scope,
        sheetState = sheetState,
        showSheet = showSheet,
        setPower = setPower,
        preHeader = preHeader,
        currentSheet = currentSheet,
        barCodeNo =barCodeNo,
        firstFieldList = firstFieldList,
        selectedEan = selectedEan,
        selectedSearchType = selectedSearchType,
        tabList =tabList,
        prodObj = prodObj,
    )
    BackHandler() {
        if(barCodeNo.value.isNotEmpty() && firstFieldList.isNotEmpty()) { barCodeNo.value = "" }
        else if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED)
            navController.popBackStack()
    }

    Scaffold(bottomBar = {
        SearchBottonBar(barCodeNo,readerViewModel, args,navController,selFilterKeys,isSearchOptions,firstFieldList)
    }) { innerPadding ->
        Box(
            modifier = modifier
                .background(BackGround)
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            SearchContent(readerViewModel, barCodeNo,isBarcodeConfirmed, firstFieldList, apiViewModel, navController,args,selFilterKeys,isSearchOptions)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBottonBar(
    barCodeNo: MutableState<String>,
    readerViewModel: ReaderViewModel,
    args: QuickSearchScreenArgs,
    navController: NavController,
    selFilterKeys: SnapshotStateList<String>,
    isSearchOptions: Boolean,
    firstFieldList: SnapshotStateList<Pair<String, String>>
) {

    val isSearchOn = readerViewModel.isSearchOn().observeAsState(false)
    val isPickOn = readerViewModel.isPickOn().observeAsState(false)
    val isTagWriteOn = readerViewModel.isTagWriteOn().observeAsState(false)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(WhiteColor)
    ) {
        HorizontalDivider(color = OutlineDefault)

        if (barCodeNo.value.isNotEmpty() && firstFieldList.isNotEmpty()){
            Row(modifier = Modifier.padding(dimensionResource(R.dimen.dp_16))) {
                CommonButton(
                    text = stringResource(R.string.check_availability),
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        val userId = DataStoreManager.readFromPreferences(ParameterConstants.USER_ID,"")
                        val filterList = DataStoreManager.getListStr(userId + "_" + MenuConstants.PRODUCT_SEARCH + "_filters")
                        if(filterList.isNullOrEmpty()){
                            openSheet(
                                scope = args.scope,
                                sheetState = args.sheetState,
                                showSheet = args.showSheet,
                                currentSheet = args.currentSheet,
                                sheet = BottomSheetType.FILTER
                            )
                        }
                        else {

                            val searchBundle = mapOf(
                                "topic" to "",
                                "sessionType" to MenuConstants.PRODUCT_SEARCH,
                                "transactionType" to "quikSearch",
                                "selFilterKeys" to selFilterKeys,
                                ParameterConstants.BARCODE to barCodeNo.value
                            )
                            val route = Screen.ChartScreen.createRoute(
                                code = MenuConstants.PRODUCT_SEARCH,
                                label = "Check Availability",
                                params = searchBundle)
                            navController.navigate(route)
                        }
                    },
                    icon =  null,
                    gradientBrush = if (chkTrue(isSearchOn.value)) SolidColor(WhiteColor)
                    else Brush.horizontalGradient(listOf(BlackColor, ButtonGray)),
                    contentColor = if (chkTrue(isSearchOn.value)) BlackColor else WhiteColor,
                    enabled = !chkTrue(isPickOn.value)  && !chkTrue(isTagWriteOn.value)  //true/*selectedSrcLocationName.value.isNotEmpty() && selectedDestLocationName.value.isNotEmpty()*/
                )
            }
        }else {
            ItekFooter()
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchContent(
    readerViewModel: ReaderViewModel,
    barCodeNo: MutableState<String>,
    isBarcodeConfirmed: MutableState<Boolean>,
    firstFieldList: SnapshotStateList<Pair<String, String>>,
    apiViewModel: ApiViewModel,
    navController: NavController,
    args: QuickSearchScreenArgs,
    selFilterKeys: SnapshotStateList<String>,
    isSearchOptions: Boolean
) {
    val selectedTab = remember { mutableStateOf(0) }
    val tabs = listOf(DataStoreManager.getBarcodeLabel(), "RFID")
    val seltedTab = tabs[selectedTab.value].uppercase()
    val isSearchOn = readerViewModel.isSearchOn().observeAsState(false)
    LogUtils.showLog("isSearchOn", "SearchBottomSheetView: ${isSearchOn.value}")
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val scannedValue = readerViewModel.barcodeData().observeAsState("")
    val tagInfoData = readerViewModel.pickData().observeAsState()
    val triggerPressed = readerViewModel.isTriggerPressed().observeAsState(false)
    val isBarcodeScan = readerViewModel.isBarcodeOn().observeAsState(false)
    val snackbarController = remember { SnackbarController() }
    val scaningMessage = stringResource(R.string.scanning_barcode)
    val isPickOn = readerViewModel.isPickOn().observeAsState()
    val error = readerViewModel.error().observeAsState()
    val scope = rememberCoroutineScope()
    val isTagWriteDone = readerViewModel.isTagWriteDone().observeAsState(false)
    val isTagWriteOn = readerViewModel.isTagWriteOn().observeAsState(false)
    val apiError = apiViewModel.apiErrorMsg.observeAsState("")
    val response = apiViewModel.apiResult.collectAsState(null)
    val scrollState = rememberScrollState()

    val imagesValue = firstFieldList.find { it.first.equals(DataStoreManager.getImageLabel(),true)  }?.second
    val imageUrls = try {
        if (!imagesValue.isNullOrEmpty() && imagesValue.startsWith("[")) {
            JSONArray(imagesValue).let { array ->
                List(array.length()) { index -> array.getString(index) }
            }
        } else {
            listOfNotNull(imagesValue)
        }
    } catch (e: Exception) {
        listOfNotNull(imagesValue)
    }.filter { it.isNotBlank() }
    val imagePainters = if (imageUrls.isEmpty()) {
        listOf(rememberAsyncImagePainter(R.drawable.image))
    } else {
        imageUrls.map { rememberAsyncImagePainter(it) }
    }
    val infoListfilteredList = firstFieldList.filter { !it.first.equals(DataStoreManager.getImageLabel(),true) }
    val showImagePreview = remember { mutableStateOf(false) }
    val selectedPainter = remember { mutableStateOf<Painter?>(null) }

    val isProcessOn = readerViewModel.isProcessOn().observeAsState(false)
    val isApiLoading = apiViewModel.isLoading.observeAsState(false)
    val tableRows = remember {
        mutableStateListOf<InfoRows>()
    }


    LogUtils.showLog("tagInfoData", "SearchContent: ${tagInfoData.value}")
    LaunchedEffect(barCodeNo.value) {
        if (barCodeNo.value.isEmpty()) {
            readerViewModel.clearTagData()
        }
    }

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

    LaunchedEffect(isBarcodeScan.value) {
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
    }

    LaunchedEffect(error.value) {
        val message = error.value
        if (!message.isNullOrBlank()) {
            LogUtils.showLog("isTagNotFound", "SingleEncodingContent: $message")
            snackbarController.show(
                ErrorAppSnackBarData(message)
                /*AppSnackBarData(
                    icon = R.drawable.property_1_error,
                    message = message,
                    showCancel = false
                )*/
            )
        }
    }

    LaunchedEffect(triggerPressed.value) {
        triggerPressed.value?.let { if (!it) return@LaunchedEffect }
        readerViewModel.setTriggerValue(false)
        if (chkTrue(readerViewModel.isProcessOn().value)) return@LaunchedEffect
        if(navController.currentBackStackEntry?.lifecycle?.currentState != Lifecycle.State.RESUMED) return@LaunchedEffect
        if (barCodeNo.value.isEmpty()) {
            VerificationUiState.NONE
            VerificationResult.NONE
            if (selectedTab.value == 0){
                readerViewModel.scanBarcode()
            }else{
                readerViewModel.performPick(pickPower = args.setPower.value,isAllowNonEncodedTags = false)
            }
        }
//        else if(barCodeNo.value.isNotEmpty()) {
//            readerViewModel.performPick(pickPower = setPower.value, pickTime = 500)
//        }
    }

    LaunchedEffect(tagInfoData.value) {
        val tag = tagInfoData.value ?: return@LaunchedEffect
        barCodeNo.value = tag.barcode
        isBarcodeConfirmed.value = true
        if (barCodeNo.value.isNotEmpty()) {
            callProductDetails(
                barCodeNo.value,
                apiViewModel,
                tag
            )
        }
    }
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
                            fieldList = firstFieldList,
                            tableRows = tableRows
                        )
                }
            }
    }


    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
        )
        {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.Top) {
                TopBarContent(
                    stringResource(R.string.search),
                    onBackClickL = {
                        if(barCodeNo.value.isNotEmpty() && firstFieldList.isNotEmpty()) { barCodeNo.value = "" }
                        else if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED)
                            navController.popBackStack()
                    }, onSettingClick = {
                        if (chkTrue(isProcessOn.value) || chkTrue(isApiLoading.value)) return@TopBarContent
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

            if ((barCodeNo.value.isEmpty() || !isBarcodeConfirmed.value) && tagInfoData.value == null){

                Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_24)))
                SegmentedTabView(
                    selectedIndex = selectedTab.value,
                    tabs = tabs,
                    modifier = Modifier
                        .width(160.dp)
                        .height(48.dp),
                    onTabSelected = { newIndex ->
                        if (barCodeNo.value.isNotEmpty()) return@SegmentedTabView
                        selectedTab.value = newIndex
                    },
                )
                Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_24)))
                CommonTextField(
                    config = TextFieldConfig(
                        value = barCodeNo.value,
                        onValueChange = { newText ->
                            if (newText != barCodeNo.value) {
                                barCodeNo.value = newText
                            }
                            if (newText.isEmpty()) {
                                isBarcodeConfirmed.value = false
                                firstFieldList.clear()
                            }
                        },
                        readOnly = seltedTab.equals("RFID"),
                        label = if (seltedTab.equals("RFID")) "RFID" else DataStoreManager.getBarcodeLabel(),//if (seltedTab.equals("Barcode")) "Barcode" else "EAN",//stringResource(id = R.string.enter_barcode),
                        imeAction = ImeAction.Done,
                        isBarCode = false,
                        codeType = if (seltedTab.equals("RFID")) CodeType.RFID else CodeType.BARCODE,
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
                            readerViewModel.setTriggerValue(true)
                            /*readerViewModel.isProcessOn().value?.let {
                                if (!it) {
                                    readerViewModel.scanBarcode("")
                                }
                            }*/
                        }
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }else{
                Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_24)))

                CommonTextField(
                    config = TextFieldConfig(
                        value = barCodeNo.value,
                        onValueChange = { newText ->
                            if (newText != barCodeNo.value) {
                                barCodeNo.value = newText
                            }
                            if (newText.isEmpty()) {
                                isBarcodeConfirmed.value = false
                                firstFieldList.clear()
                            }
                        },
                        label = /*if (seltedTab.equals("RFID")) "RFID" else*/ DataStoreManager.getBarcodeLabel(),//stringResource(id = R.string.enter_barcode),
                        imeAction = ImeAction.Done,
                        isBarCode = true,
                        //codeType =  if (seltedTab.equals("RFID")) CodeType.RFID else CodeType.BARCODE,
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
                            readerViewModel.setTriggerValue(true)
                            /*readerViewModel.isProcessOn().value?.let {
                                if (!it) {
                                    readerViewModel.scanBarcode("")
                                }
                            }*/
                        }
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_16)))

                    Box(
                        modifier = Modifier
                            .width(dimensionResource(R.dimen.dp_105))
                            .height(dimensionResource(R.dimen.dp_170))
                    ) {

                        if (imagePainters.size == 1) {

                            Image(
                                painter = imagePainters.first(),
                                contentDescription = null,
                                modifier = Modifier
                                    .matchParentSize()
                                    .background(
                                        WhiteColor,
                                        RoundedCornerShape(dimensionResource(R.dimen.dp_8))
                                    )
                                    .border(
                                        dimensionResource(R.dimen.dp_1),
                                        OutlineDefault,
                                        RoundedCornerShape(dimensionResource(R.dimen.dp_8))
                                    )
                                    .clickable(enabled = imageUrls.isNotEmpty())  {
                                            selectedPainter.value = imagePainters.first()
                                            showImagePreview.value = true
                                    }
                            )

                        } else {

                            Row(
                                modifier = Modifier
                                    .matchParentSize()
                                    .horizontalScroll(rememberScrollState())
                            ) {

                                imagePainters.forEach { painter ->

                                    Image(
                                        painter = painter,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .width(dimensionResource(R.dimen.dp_105))
                                            .height(dimensionResource(R.dimen.dp_170))
                                            .padding(end = 4.dp)
                                            .background(
                                                WhiteColor,
                                                RoundedCornerShape(dimensionResource(R.dimen.dp_8))
                                            )
                                            .border(
                                                dimensionResource(R.dimen.dp_1),
                                                OutlineDefault,
                                                RoundedCornerShape(dimensionResource(R.dimen.dp_8))
                                            )
                                            .clickable(enabled = imageUrls.isNotEmpty()) {
                                                selectedPainter.value = painter
                                                showImagePreview.value = true
                                            }
                                    )
                                }
                            }
                        }
                    }

                    if (tableRows.isNotEmpty()) {
                        IconText(
                            icon = painterResource(R.drawable.property_know_more),
                            actionText = "Know More",
                            onActionClick = {
                                openSheet(
                                    scope = args.scope,
                                    sheetState = args.sheetState,
                                    showSheet = args.showSheet,
                                    currentSheet = args.currentSheet,
                                    sheet = BottomSheetType.KNOW_MORE
                                )
                            },
                            tintColor = Yellow
                        )
                    }

                    Row() {
                        InfoTable(
                            modifier = Modifier.padding(16.dp).height(288.dp),
                            rows = infoListfilteredList.map { InfoRow(it.first, it.second) }
                        )
                    }
                }
            }
            Text(
                text = chkNull(error.value,apiError.value),
                style = CommonTypography.current.textSemiBold,
                textAlign = TextAlign.Center
            )
        }
        Box(modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center){
            if (showImagePreview.value) {
                ImageViewFullScreen(showImagePreview , selectedPainter)
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
                            minSelection = 2,
                            maxSelection = 2,
                            onDismiss = {
                                args.scope.launch {
                                    args.sheetState.hide()
                                    args.showSheet.value = false
                                    args.currentSheet.value = BottomSheetType.NONE
                                }
                            },

                            onApply = { selectedMap ->

                                val isSelFilterKeysEmpty = selFilterKeys.size<=0
                                LogUtils.showLog("D_selectedMAp",selectedMap.toString())
                                val selectedValues = selectedMap.filter { it.isNotBlank() }.distinct()
                                LogUtils.showLog("D_selectedMAp1",selectedValues.toString())

                                selFilterKeys.clear()
                                selFilterKeys.addAll(selectedValues)

                                val userId = DataStoreManager.readFromPreferences(ParameterConstants.USER_ID,"")
                                DataStoreManager.saveListStr(userId+"_"+ MenuConstants.PRODUCT_SEARCH+"_filters",selectedValues)

                                if(isSelFilterKeysEmpty) {
                                    val searchBundle = mapOf(
                                        "topic" to "",
                                        "sessionType" to MenuConstants.PRODUCT_SEARCH,
                                        "transactionType" to "quickSearch",
                                        "selFilterKeys" to selFilterKeys,
                                        ParameterConstants.BARCODE to barCodeNo.value
                                    )
                                    val route = Screen.ChartScreen.createRoute(
                                        code = MenuConstants.PRODUCT_SEARCH,
                                        label = "Check Availability",
                                        params = searchBundle
                                    )
                                    navController.navigate(route)
                                }

                                args.scope.launch {
                                    args.sheetState.hide()
                                    args.showSheet.value = false
                                    args.currentSheet.value = BottomSheetType.NONE
                                }
                            },
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
                                DataStoreManager.saveToPreferences(args.preHeader + ParameterConstants.READER_POWER,args.setPower.value)
                                readerViewModel.setPower(args.setPower.value)
                            },
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
                            isPowerSet = true,
                            isFilterEnable = args.barCodeNo.value.isNotEmpty() && args.firstFieldList.isNotEmpty()
                        )
                    }

                    BottomSheetType.KNOW_MORE ->
                        Box(
                            modifier = Modifier
                                .background(WhiteColor)
                                .fillMaxWidth()
                                .wrapContentHeight()
                        ) {
                            if (tableRows.isNotEmpty()) {
                                ScrollableInfoTable(
                                    modifier = Modifier,
                                    rows = tableRows,
                                    scope = args.scope,
                                    sheetState = args.sheetState,
                                    showSheet = args.showSheet,
                                    currentSheet = args.currentSheet
                                )
                            }
                        }


                    else -> {

                    }
                }
            }
        }
    }
}

fun handleProductDetails(
    apiResult: ApiResult?,
    barcode: String="",
    tagInfo: TagInfoEntity?=null,
    fieldList: SnapshotStateList<Pair<String, String>>,
    tableRows: SnapshotStateList<InfoRows>,
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
             *//*   if (BaseUtils.isDebuggable() && key.equals("Images", true)){
             value = "[" +
//                     "\"https://storage-cdn.weweb.io/75cef5c5-df84-444e-89c8-f3a5d1cc70e9/users-storage/b3906169/Captura+de+img.png\"," +
//                     "\"https://pngimg.com/d/mario_PNG125.png\"" +
//                     "\"https://img.freepik.com/free-vector/tropical-plant-transparent-background_1308-75855.jpg\"" +
//                     "\"https://img.freepik.com/free-psd/monarch-butterfly-vibrant-symbol-transformation_632498-24128.jpg\"" +
//                     "\"https://global.discourse-cdn.com/twitter/original/2X/1/1f8d67fe1366937e20970fbcc4c374f366447819.gif\"" +
//                     "\"https://upload.wikimedia.org/wikipedia/commons/2/2c/Rotating_earth_%28large%29.gif\"" +
//                     "\"https://www.svgrepo.com/show/521128/email-1.svg\"" +
//                     "\"https://www.svgrepo.com/show/532034/cloud-arrow-down.svg\"" +
//                     "\"https://www.align.vn/wp-content/uploads/2025/04/What-is-WebP_.webp\"" +
//                     "\"https://www.align.vn/wp-content/uploads/2025/04/What-is-WebP_.webp\"" +
                     "]"

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

        tagInfo != null || barcode.isNotEmpty() -> {
            fieldList.clear()
            fieldList.addAll(listProductValues)
        }
    }

    val parsedTableRows = parseNestedTableData(rootJson)
    tableRows.clear()
    tableRows.addAll(parsedTableRows)
}


fun parseNestedTableData(
    rootJson: JSONObject
): List<InfoRows> {

    val nestedArray = rootJson.optJSONArray("nestedData")
        ?: return emptyList()

    if (nestedArray.length() == 0) return emptyList()

    val firstObject = nestedArray.optJSONObject(0)
        ?: return emptyList()

    val keys = firstObject.keys().asSequence().toList()

    return keys.map { key ->

        val values = mutableListOf<String>()

        for (i in 0 until nestedArray.length()) {

            val item = nestedArray.optJSONObject(i)

            values.add(
                item?.optString(key).orEmpty()
            )
        }

        InfoRows(
            label = key,
            values = values
        )
    }
}