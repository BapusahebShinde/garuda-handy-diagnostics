package com.itek.rftaar.presentation.search

import android.annotation.SuppressLint
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
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.key
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import com.itek.rftaar.core.common.utils.LogUtils
import com.itek.rftaar.core.common.utils.ParseUtils
import com.itek.rftaar.core.common.utils.ParseUtils.extractString
import com.itek.rftaar.core.database.AppDatabase
import com.itek.rftaar.core.database.DataStoreManager
import com.itek.rftaar.data.entity.TagInfoEntity
import com.itek.rftaar.data.model.LabelName
import com.itek.rftaar.data.model.ProductZoneFoundQty
import com.itek.rftaar.domain.model.LocationModel
import com.itek.rftaar.mqtt.MqttManager
import com.itek.rftaar.mqtt.constants.SearchTypeConstant
import com.itek.rftaar.mqtt.constants.TopicConstants
import com.itek.rftaar.presentation.commonComp.AppSnackBar
import com.itek.rftaar.presentation.commonComp.BottomSheetType
import com.itek.rftaar.presentation.commonComp.CodeType
import com.itek.rftaar.presentation.commonComp.CommonButton
import com.itek.rftaar.presentation.commonComp.CommonTextField
import com.itek.rftaar.presentation.commonComp.DirectionProximityGauge
import com.itek.rftaar.presentation.commonComp.ErrorAppSnackBarData
import com.itek.rftaar.presentation.commonComp.FilterPicker
import com.itek.rftaar.presentation.commonComp.ImageViewFullScreen
import com.itek.rftaar.presentation.commonComp.InventoryPulseCircle
import com.itek.rftaar.presentation.commonComp.RowViewWithHSI1
import com.itek.rftaar.presentation.commonComp.SnackbarController
import com.itek.rftaar.presentation.commonComp.TextFieldConfig
import com.itek.rftaar.presentation.commonComp.TopBarContent
import com.itek.rftaar.presentation.commonComp.TwoButtonView
import com.itek.rftaar.presentation.commonComp.VerifyInfoCard
import com.itek.rftaar.presentation.commonComp.openSheet
import com.itek.rftaar.presentation.dashBoard.InfoRows
import com.itek.rftaar.presentation.dashBoard.handleProductDetails
import com.itek.rftaar.presentation.decoding.ProductDetailsByEan
import com.itek.rftaar.presentation.decoding.handleDecode
import com.itek.rftaar.presentation.encoding.DeviceSettings
import com.itek.rftaar.presentation.encoding.SearchBottomSheetView
import com.itek.rftaar.presentation.encoding.SessionContent
import com.itek.rftaar.presentation.encoding.SetDevicePower
import com.itek.rftaar.presentation.encoding.VerificationResult
import com.itek.rftaar.presentation.encoding.VerificationUiState
import com.itek.rftaar.presentation.inventory.InventoryUploadedSuccess
import com.itek.rftaar.presentation.inventory.SwipeToUploadButton
import com.itek.rftaar.presentation.inventory.UploadDataSheet
import com.itek.rftaar.presentation.inward.SegmentedTabView
import com.itek.rftaar.presentation.movement.clearSavedSessionValues
import com.itek.rftaar.presentation.navigation.Screen
import com.itek.rftaar.presentation.viewmodel.ApiViewModel
import com.itek.rftaar.presentation.viewmodel.ReaderViewModel
import com.itek.rftaar.reader.epcwrapper.EpcEncoderDecoderWrapper
import com.itek.rftaar.ui.theme.BackGround
import com.itek.rftaar.ui.theme.BlackColor
import com.itek.rftaar.ui.theme.ButtonGray
import com.itek.rftaar.ui.theme.CommonTypography
import com.itek.rftaar.ui.theme.Green
import com.itek.rftaar.ui.theme.OutlineDefault
import com.itek.rftaar.ui.theme.TabColor
import com.itek.rftaar.ui.theme.TextGrey
import com.itek.rftaar.ui.theme.WhiteColor
import com.itek.rftaar.utils.CommonUtils.chkNull
import com.itek.rftaar.utils.CommonUtils.chkTrue
import com.itek.rftaar.utils.NetworkUtils.isInternetConnected
import com.itek.rftaar.utils.SessionUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductSearch(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    label: String = "",
    menuCode: String = "",
    searchParams: Map<String, Any> = emptyMap(),
    apiViewModel: ApiViewModel = hiltViewModel(),
){

    LogUtils.showLog("menuCode","--"+menuCode)
    val topic = extractString(searchParams,"topic", TopicConstants.SEARCH)//if(searchParams.containsKey("topic") && searchParams.get("topic") is String) searchParams.get("topic") as String else TopicConstants.SEARCH
    val sessionType = extractString(searchParams,"sessionType",menuCode)//if(searchParams.containsKey("sessionType") && searchParams.get("sessionType") is String) searchParams.get("sessionType") as String else menuCode
    val preHeader = topic + "_" + sessionType + "_"
    val transactionType = extractString(searchParams,"transactionType",DataStoreManager.readFromPreferences(preHeader+ ParameterConstants.TRANSACTION_TYPE,""))//if(searchParams.containsKey("transactionType") && searchParams.get("transactionType") is String) searchParams.get("transactionType") as String else DataStoreManager.readFromPreferences(preHeader+ ParameterConstants.TRANSACTION_TYPE,"")

    val searchTypeId = extractString(searchParams,"searchTypeId","")
    val searchTypeName = extractString(searchParams,"searchTypeName","")
    val sessionId= extractString(searchParams,"sessionId","")
    val searchValue= extractString(searchParams,"searchValue","")
    val referenceNumber= extractString(searchParams,"referenceNumber","")

    LogUtils.showLog("sessionType",""+sessionType)
    LogUtils.showLog("transactionType",""+transactionType)
    LogUtils.showLog("searchTypeId",""+searchTypeId)
    LogUtils.showLog("searchTypeName",""+searchTypeName)
    LogUtils.showLog("sessionId",""+sessionId)
    LogUtils.showLog("searchValue",""+searchValue)
    LogUtils.showLog("referenceNumber",""+referenceNumber)

    val prodObj = ParseUtils.extractObject<ProductZoneFoundQty>(searchParams,cls=ProductZoneFoundQty::class)
    LogUtils.showLog("prodObj",""+prodObj)
    val hasProdObjZone = prodObj!=null && prodObj.path.isNotEmpty() && prodObj.name.isNotEmpty()

    val mode = if(prodObj==null) SearchDetailsActionMode.SEARCH_ONLY else SearchDetailsActionMode.valueOf(extractString(searchParams,"mode",SearchDetailsActionMode.SEARCH_FOUND.toString()))
    LogUtils.showLog("mode",""+mode.toString())

    val srcLocation = ParseUtils.extractObject<LocationModel>(searchParams,cls=LocationModel::class)
    val destLocation = null//ParseUtils.extractObject<LocationModel>(searchParams,cls=LocationModel::class)

    val srcZone = if (srcLocation!=null) srcLocation else if(hasProdObjZone) LocationModel(id="",name=prodObj.name,code="",path=prodObj.path)  else null
    val destZone = if (destLocation!=null) destLocation else if(srcLocation!=null && hasProdObjZone) LocationModel(id="",name=prodObj.name,code="",path=prodObj.path)  else null

    LogUtils.showLog("prodObj",""+prodObj)

    val textValue = if(prodObj!=null) prodObj.barcode else extractString(searchParams,"textValue")
    val isSearchOptions = mode.equals(SearchDetailsActionMode.SEARCH_ONLY) && textValue.isNullOrEmpty()//searchParams["isSearchOptions"] as? Boolean ?: true

    val isShowUpload  = ParseUtils.extractBoolean(searchParams,"isShowUpload",false)

    val decTypeList = ParseUtils.extractTypedList<LabelName>(searchParams,"decodeTypesList")

    LogUtils.showLog("decTypeList1",decTypeList.toString())
    LogUtils.showLog("isShowUpload",""+isShowUpload)
    LogUtils.showLog("Screen","ProductSearch")
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val activity = context as? ReaderActivity
    val readerViewModel = if(activity!=null) activity.findReaderViewModel() else hiltViewModel()
    if (activity != null) {
        readerViewModel.onCreate()
        readerViewModel.setSessionAndTransactionType(menuCode,transactionType,topic=topic)
        if(prodObj!=null && sessionId.isNotEmpty()){
            val sessionData = DataStoreManager.readFromPreferences(preHeader + "sessionData","")
            try {
                val sessionData = JSONObject()
                sessionData.put(ParameterConstants.SESSION_ID, sessionId)
                sessionData.put(ParameterConstants.ID, "")
                sessionData.put(ParameterConstants.SESSION_TYPE, menuCode)
                sessionData.put(ParameterConstants.TRANSACTION_TYPE, transactionType)
                sessionData.put(ParameterConstants.SEARCH_TYPE_ID, searchTypeId)
                sessionData.put(ParameterConstants.SEARCH_TYPE_NAME, searchTypeName)
                sessionData.put(ParameterConstants.REFERENCE_NUMBER, referenceNumber)
                sessionData.put(ParameterConstants.SEARCH_VALUE, searchValue)
                sessionData.put(ParameterConstants.ARTICLE, prodObj.customField)
                if(srcZone!=null){
                    sessionData.put(ParameterConstants.ASSET_LOCATION_NAME, srcZone.name)
                    sessionData.put(ParameterConstants.ASSET_LOCATION_PATH, srcZone.path)
                }
                DataStoreManager.saveToPreferences(preHeader + ParameterConstants.DEVICE_SESSION_ID, sessionId)
                DataStoreManager.saveToPreferences(preHeader + "sessionData", sessionData.toString())
            }catch (e: Exception) {e.printStackTrace()}
            readerViewModel.setSessionId(sessionId,sessionData)
        }
    }

    val readerPower = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.READER_POWER, 7)
    val setPower = rememberSaveable { mutableStateOf(readerPower) }
    LaunchedEffect(setPower.value) {
        readerViewModel.setPower(setPower.value)
    }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { sheetValue ->
            sheetValue != SheetValue.Hidden
        }
    )
    val showSheet = remember { mutableStateOf(false) }
    val currentSheet = remember { mutableStateOf(BottomSheetType.NONE) }
    val selectedEan = remember {mutableStateOf<TagInfoEntity?>(null)}
    val selectedSearchType = remember{ mutableStateOf("")}

    val selFilterKeys = remember { mutableStateListOf<String>()}
    val userId = DataStoreManager.readFromPreferences(ParameterConstants.USER_ID,"")
    if(selFilterKeys.isEmpty()) selFilterKeys.addAll(DataStoreManager.getListStr(userId + "_" + menuCode + "_filters"))
    LogUtils.showLog("selFilterKeys",""+selFilterKeys.size)

    val barCodeNo = rememberSaveable { mutableStateOf(textValue.ifEmpty { "" }) }
    val prodEan = rememberSaveable { mutableStateOf(textValue.ifEmpty { "" }) }
    val firstFieldList = rememberSaveable { mutableStateListOf<Pair<String, String>>() }
    if(prodObj!=null) {
        firstFieldList.clear();
        firstFieldList.addAll(ParseUtils.reconvertToPairList(prodObj.displayData))
        firstFieldList.add(Pair("Zone",prodObj.name))
        firstFieldList.add(Pair("Qty",""+prodObj.totalQty))
        LogUtils.showLog("firstFieldList",""+firstFieldList)
    }
    val isBarcodeConfirmed = rememberSaveable { mutableStateOf(textValue.isNotEmpty()) }

    val searchTypeList = listOf(
        SearchTypeItem(DataStoreManager.getBarcodeLabel(), DataStoreManager.getBarcodeLabel(), R.drawable.property_barcode),
        SearchTypeItem("EPC", "EPC", R.drawable.property_rfidd),
        SearchTypeItem("TID", "TID", R.drawable.property_qr),
    )
    val selectedType = remember { mutableStateOf(searchTypeList.firstOrNull()?.name) }
    val selectedLabel = when (selectedType.value) {
        "Barcode" -> DataStoreManager.getBarcodeLabel()
        "EPC" -> stringResource(R.string.epc)
        "TID" -> stringResource(R.string.tid)
        else -> DataStoreManager.getBarcodeLabel()
    }
    /*val isProcessOn = readerViewModel.isProcessOn().observeAsState(false)
    val isSearchOn = readerViewModel.isSearchOn().observeAsState(false)
    val isPickOn = readerViewModel.isPickOn().observeAsState(false)
    val isTagWriteOn = readerViewModel.isTagWriteOn().observeAsState(false)*/
    val isApiLoading = apiViewModel.isLoading.observeAsState(false)


    val db = AppDatabase.getDbInstance(context)
    val totalCount = remember { mutableStateOf(if(prodObj!=null) prodObj!!.totalQty else 0)}
    val defFoundQty = db.productZoneDataDao().getFoundQty(topic,menuCode,transactionType,barCodeNo.value).collectAsState(0)
    val dbFoundCount= db.tagInfoDao().getTotalCount(menuCode, transactionType,barCodeNo.value).observeAsState(0)
    val foundCount = remember { mutableStateOf(defFoundQty.value + dbFoundCount.value)}
    //Only if decode is enabled
    LogUtils.showLog("defFoundCount",""+defFoundQty.value)
    LogUtils.showLog("dbFoundCount",""+dbFoundCount)
    LogUtils.showLog("foundCount",""+foundCount.value)
    val defDecodeQty = db.productZoneDataDao().getDecodeQty(topic,menuCode,transactionType,barCodeNo.value).collectAsState(0)
    val dbDecodeCount= db.tagInfoDao().getTagWriteCount(menuCode, transactionType,barCodeNo.value).observeAsState(0)
    val decodeCount= remember {mutableStateOf(defDecodeQty.value + dbDecodeCount.value)}//remember { mutableStateOf(if(prodObj!=null && foundCount.value>0 && mode.equals(SearchDetailsActionMode.SEARCH_AND_PICK_AND_DECODE)) defDecodeQty.value+dbDecodeCount.value else 0)}//if(foundCount.value>0 && mode.equals(SearchDetailsActionMode.SEARCH_AND_PICK_AND_DECODE)) db.tagInfoDao().getTagWriteCount(menuCode, transactionType,barCodeNo.value).observeAsState(0) else remember { mutableStateOf(0) }

    val listPickedTags =  if(foundCount.value>0 && (mode.equals(SearchDetailsActionMode.SEARCH_AND_PICK_AND_DECODE) || mode.equals(SearchDetailsActionMode.SEARCH_AND_PICK))) db.tagInfoDao().getAllForBarcode(topic,menuCode,transactionType,barCodeNo.value).collectAsState(emptyList()) else remember {mutableStateOf(emptyList()) }
    val listDecodedTags =  if(foundCount.value>0 && decodeCount.value>0 && mode.equals(SearchDetailsActionMode.SEARCH_AND_PICK_AND_DECODE)) db.tagInfoDao().getAllWrittenTagsAgainstBarcode(topic,menuCode,transactionType,barCodeNo.value).collectAsState(emptyList()) else remember {mutableStateOf(emptyList()) }
    val listNonDecodedTags = if(foundCount.value>0 && decodeCount.value>0 && mode.equals(SearchDetailsActionMode.SEARCH_AND_PICK_AND_DECODE)) db.tagInfoDao().getAllNonWrittenTagsAgainstBarcode(topic, menuCode, transactionType, barCodeNo.value).collectAsState(emptyList()) else remember {mutableStateOf(emptyList()) }
    val tabList = if(mode.equals(SearchDetailsActionMode.SEARCH_AND_PICK_AND_DECODE) && foundCount.value>0) listOf("Search", "Pick", "Decode") else if(mode.equals(SearchDetailsActionMode.SEARCH_AND_PICK) || mode.equals(SearchDetailsActionMode.SEARCH_AND_PICK_AND_DECODE)) listOf("Search", "Pick") else emptyList() //ParseUtils.extractStringList(searchParams,"tabList",emptyList())//if(searchParams.containsKey("tabList") && searchParams.get("tabList") is List<*> && searchParams.get("tabList").all{ it is String }) searchParams["tabList"] as? List<String> ?: emptyList()
    val isTabView = tabList.isNotEmpty()
    val selectedTab = remember { mutableStateOf(0) }
    val selectedTabName = if(tabList.isNotEmpty()) tabList[selectedTab.value] else ""
    val isShowMarkFoundBtn = remember { mutableStateOf(false) }

    //val isInvOn = readerViewModel.isInventoryOn().observeAsState()
    val isDataUpload = remember { mutableStateOf(false) }

    val isOnDataUploaded = remember { mutableStateOf(false) }
    val isUploadData = remember { mutableStateOf(false) }

    val decodeType = remember { mutableStateOf(if(decTypeList.size==1) decTypeList.get(0).name else "") }

    LogUtils.showLog("decodeType",decodeType.value.toString())

    val args = ProductSearchScreenArgs(
        context = context,
        label = label,
        topic = topic,
        setPower = setPower,
        menuCode = menuCode,
        preHeader = preHeader,
        transactionType = transactionType,
        searchTypeId = searchTypeId,
        searchTypeName = searchTypeName,
        sessionId = sessionId,
        searchValue = searchValue,
        referenceNumber = referenceNumber,
        scope = scope,
        sheetState = sheetState,
        showSheet = showSheet,
        currentSheet = currentSheet,
        barCodeNo =barCodeNo,
        firstFieldList = firstFieldList,
        prodObj = prodObj,
        totalCount = totalCount,
        foundCount = foundCount,
        dbFoundCount =  dbFoundCount,
        decodeCount = decodeCount,
        listPickedTags = listPickedTags,
        listDecodedTags = listDecodedTags,
        listNonDecodedTags = listNonDecodedTags,
        selectedEan = selectedEan,
        tabList =tabList,
        selectedTab = selectedTab,
        selectedTabName = selectedTabName,
        isBarcodeConfirmed= isBarcodeConfirmed,
        selectedType = selectedType,
        searchTypeList = searchTypeList,
        selectedLabel = selectedLabel,
        isShowUpload = isShowUpload,
        srcZone = srcZone,
        destZone = destZone,
        selectedSearchType = selectedSearchType,
        prodEan =prodEan,
        decodeTypesList = decTypeList,
        decodeType = decodeType,
        textValue = textValue
    )

    val snackbarController = remember { SnackbarController() }


    LaunchedEffect(Unit) {
        if(prodObj==null && textValue.isNotEmpty() && barCodeNo.value.isNotEmpty() && isBarcodeConfirmed.value && firstFieldList.isNullOrEmpty()){
            callProductDetails(
                barCodeNo,
                selectedType,
                apiViewModel,
                readerViewModel,
                args.context,
                null
            )
        }
    }

    LaunchedEffect(isDataUpload.value) {
        if (isDataUpload.value == true) {
            if(!isInternetConnected(context, isShowErrToast = true)) {isDataUpload.value= false; return@LaunchedEffect}
            sheetState.hide()
            showSheet.value = false
            currentSheet.value = BottomSheetType.NONE
            val sessionId= chkNull(sessionId,DataStoreManager.readFromPreferences(preHeader + ParameterConstants.DEVICE_SESSION_ID,""))
            val sessionData = DataStoreManager.readFromPreferences(preHeader + "sessionData","")
            isOnDataUploaded.value=true
            delay(500)
            if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                clearSavedSessionValues(context, menuCode, transactionType,true)
                if(args.topic.equals(TopicConstants.REPLENISHMENT))
                 MqttManager.publishReplenishment(listPickedTags.value, menuCode, transactionType, sessionId, sessionData=sessionData)
                else if(args.topic.equals(TopicConstants.SEARCH_LIST_UPDATE))
                 MqttManager.publishListUpdate(listPickedTags.value, menuCode, transactionType, sessionId, sessionData = sessionData, searchTypeId, searchTypeName, referenceNumber, searchValue)
                readerViewModel.clearSessionAndTransactionType()
                navController.popBackStack()
            }
        }
    }

    LaunchedEffect(dbFoundCount.value,defFoundQty.value) {
        foundCount.value=chkNull(dbFoundCount.value,0)+chkNull(defFoundQty.value,0)
        if(foundCount.value>totalCount.value && chkNull(dbFoundCount.value,0)>0){
         //Delete Extra Picked Tags if qty Completed/Uploaded by someone else
         CoroutineScope(Dispatchers.IO).launch {
          db.tagInfoDao().deleteActual(topic,sessionType,transactionType)
         }
        }
    }

    LaunchedEffect(dbDecodeCount.value,defDecodeQty.value) {
        decodeCount.value=chkNull(dbDecodeCount.value,0)+chkNull(defDecodeQty.value,0)
    }

    BackHandler(enabled = true) {
        LogUtils.showLog("method11",label+"->BackHandler")
        if(chkTrue(readerViewModel.isProcessOn().value) ||chkTrue(isApiLoading.value)) return@BackHandler
        LogUtils.showLog("method12",label+"->BackHandler")
        if(isUploadData.value) {isUploadData.value=false; return@BackHandler}
        LogUtils.showLog("method13",label+"->BackHandler")
        scope.launch {
            val deviceSessionId = chkNull(sessionId,DataStoreManager.readFromPreferences(preHeader + ParameterConstants.DEVICE_SESSION_ID, ""))
            if (deviceSessionId.isNullOrEmpty() || !isShowUpload) {
                if(isSearchOptions && barCodeNo.value.isNotEmpty() && firstFieldList.isNotEmpty()) {
                    barCodeNo.value = ""
                    prodEan.value = ""
                    isBarcodeConfirmed.value = false
                }
                else{
                    clearSavedSessionValues(context, menuCode, transactionType, topic = topic)
                    if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                        readerViewModel.clearSessionAndTransactionType()
                        navController.popBackStack()
                    }
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

    Scaffold(
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BackGround),
                contentAlignment = Alignment.BottomCenter
            ) {
                ProductSearchBottomBar(
                    args,
                    apiViewModel,
                    readerViewModel,
                    isShowUpload,
                    isDataUpload,
                    isOnDataUploaded,
                    isUploadData,
                    isShowMarkFoundBtn,
                    snackbarController,
                    isSearchOptions,
                    navController,
                    selFilterKeys,
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
            ProductSearchContent(
                navController,
                apiViewModel,
                readerViewModel,
                args,
                isTabView,
                isSearchOptions,
                snackbarController,
                isShowUpload,
                isUploadData,
                isDataUpload,
                isOnDataUploaded,
                isShowMarkFoundBtn,
                selFilterKeys,
            )
        }
    }

}

data class ProductSearchScreenArgs @OptIn(ExperimentalMaterial3Api::class) constructor(
    val context: Context,
    val label: String,
    val topic: String,
    val setPower: MutableState<Int>,
    val menuCode: String,
    val preHeader: String,
    val transactionType: String,
    val searchTypeId: String,
    val searchTypeName: String,
    val sessionId: String,
    val searchValue: String,
    val referenceNumber: String,
    val scope: CoroutineScope,
    val sheetState: SheetState,
    val showSheet: MutableState<Boolean>,
    val currentSheet: MutableState<BottomSheetType>,
    val barCodeNo: MutableState<String>,
    val firstFieldList: SnapshotStateList<Pair<String, String>>,
    val prodObj: ProductZoneFoundQty?,
    val totalCount: MutableState<Int>,
    val foundCount: State<Int>,
    val decodeCount: State<Int>,
    val listPickedTags: State<List<TagInfoEntity>>,
    val listDecodedTags: State<List<TagInfoEntity>>,
    val listNonDecodedTags: State<List<TagInfoEntity>>,
    val selectedEan: MutableState<TagInfoEntity?>,
    val tabList: List<String>,
    val selectedTab: MutableState<Int>,
    val selectedTabName: String,
    val isBarcodeConfirmed: MutableState<Boolean>,
    val selectedType: MutableState<String?>,
    val searchTypeList: List<SearchTypeItem>,
    val selectedLabel: String,
    val isShowUpload: Boolean,
    val srcZone: LocationModel?,
    val destZone: LocationModel?,
    val selectedSearchType: MutableState<String>,
    val prodEan: MutableState<String>,
    val decodeTypesList: List<LabelName>,
    val decodeType: MutableState<String>,
    val textValue: String,
    val dbFoundCount: State<Int>,
)

enum class VerificationResult {
    NONE, SUCCESS, ERROR,
}

enum class VerificationUiState {
    NONE,
    OVERLAY,
    BADGE
}

@SuppressLint("SuspiciousIndentation")
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductSearchContent(
    navController: NavHostController,
    apiViewModel: ApiViewModel,
    readerViewModel: ReaderViewModel,
    args: ProductSearchScreenArgs,
    isTabView: Boolean,
    isSearchOptions: Boolean,
    snackbarController: SnackbarController,
    isShowUpload: Boolean,
    isUploadData: MutableState<Boolean>,
    isDataUpload: MutableState<Boolean>,
    isOnDataUploaded: MutableState<Boolean>,
    isShowMarkFoundBtn: MutableState<Boolean>,
    selFilterKeys: SnapshotStateList<String>,
) {
    val scannedValue = readerViewModel.barcodeData().observeAsState("")
    val triggerPressed = readerViewModel.isTriggerPressed().observeAsState(false)
    //val error = readerViewModel.error().observeAsState("")
    //LogUtils.showLog("Val_errorMessage","--"+error.value)
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val searchPercentage = readerViewModel.searchPercentage().observeAsState()
    val searchRssi = readerViewModel.searchRssi().observeAsState()
    val searchPhase = readerViewModel.searchPhase().observeAsState()

    val isProcessOn = readerViewModel.isProcessOn().observeAsState(false)
    val isSearchOn = readerViewModel.isSearchOn().observeAsState(false)
    val isPickOn = readerViewModel.isPickOn().observeAsState(false)
    val isTagWriteOn = readerViewModel.isTagWriteOn().observeAsState(false)
    val isTagWriteDone = readerViewModel.isTagWriteDone().observeAsState(false)
    val isApiLoading = apiViewModel.isLoading.observeAsState(false)

    //val searchAngle = readerViewModel.angle.collectAsState()

    //LogUtils.showLog("ProdSearchRssi", "ProductSearchContent: ${searchRssi.value}")
    //LogUtils.showLog("ProdSearchPercentage", "ProductSearchContent: ${searchPercentage.value}")
    //LogUtils.showLog("ProdSearchAngle", "ProductSearchContent: ${searchAngle.value}")
    //LogUtils.showLog("ProdSearchPhase", "ProductSearchContent: ${searchPhase.value}")
    val directionAngle = remember { mutableFloatStateOf(90f) }
    val lastPhase = remember { mutableFloatStateOf(0f) }
    val isTagDetected = !searchPhase.value.isNullOrBlank();// && (searchRssi.value?.toFloatOrNull() ?: -100f) > -85f

    //Proximity from RSSI / percentage
    val proximityValue = (searchPercentage.value ?: 0.00f).coerceIn(0.00f, 100.00f).toFloat()

    //Direction from PHASE (0–180)
    val targetDirection = phaseTo180Degrees(searchPhase.value)

    //LogUtils.showLog("targetDirection", "ProductSearchContent: $targetDirection")

    var steeringBias = remember { mutableFloatStateOf(0f) }
    val showImagePreview = remember { mutableStateOf(false) }
    val selectedPainter = remember { mutableStateOf<Painter?>(null) }

    val readerError = readerViewModel.error().observeAsState("")
    val apiError = apiViewModel.apiErrorMsg.observeAsState("")
    val tableRows = remember {
        mutableStateListOf<InfoRows>()
    }

    LaunchedEffect(readerError.value,apiError.value) {
        LogUtils.showLog("readerError",readerError.value)
        LogUtils.showLog("apiError",apiError.value)
    }

    val response = apiViewModel.apiResult.collectAsState(null)
    LaunchedEffect(response.value) {
        val result = response.value ?: return@LaunchedEffect
        if (result?.isSuccess != true) {
            snackbarController.show(ErrorAppSnackBarData(result?.errMsg.toString()))
        }
        else if (result.response != null) {
            when (result.url) {
                UrlConstants.PRODUCTS -> {
                    handleProductDetails(apiResult = result, barcode = args.barCodeNo.value, fieldList = args.firstFieldList,tableRows = tableRows)
                    LogUtils.showLog("fieldList121",""+args.firstFieldList)
                    //get prodEan value from firstFieldList
                    if(args.firstFieldList.isNotEmpty()) {
                        val fieldList = args.firstFieldList.filter { p -> p.first.equals(
                            DataStoreManager.getBarcodeLabel(),true) }
                        if(fieldList.isNotEmpty()){
                            args.prodEan.value = fieldList.get(0).second
                            LogUtils.showLog("prodEan",""+args.prodEan.value)
                        }
                    }
                }
                UrlConstants.GET_PASSWORD -> {
                    CoroutineScope(Dispatchers.IO).launch {
                        LogUtils.showLog("handleRes",args.decodeType.value)
                        if(args.selectedEan.value!=null) args.scope.launch {  handleDecode(result, readerViewModel, tagInfoData = args.selectedEan.value,decodeType=args.decodeType.value)}
                        else{
                            val db = AppDatabase.getDbInstance(args.context)
                            val listNonDecoded = db.tagInfoDao().getAllNonWrittenTagsAgainstBarcode1(args.topic,args.menuCode,args.transactionType,args.barCodeNo.value)
                            LogUtils.showLog("listNonDecoded",""+listNonDecoded)
                            args.scope.launch {  handleDecode(result, readerViewModel, listTagInfoData = listNonDecoded,decodeType=args.decodeType.value)}
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(isProcessOn.value) {
        LogUtils.showLog("__isProcessOn1",""+isProcessOn.value)
        if (isProcessOn.value == false) {
            // Reset everything when search stops
            directionAngle.floatValue = 0f      //reset to 0°
            steeringBias.floatValue = 0f
            lastPhase.floatValue = 0f
        }
    }

    LaunchedEffect(isTagDetected) {
        if (!isTagDetected) {
            directionAngle.floatValue = 0f   // center
            steeringBias.floatValue = 0f
            lastPhase.floatValue = 0f
        }
    }

    LaunchedEffect(searchPercentage.value) {
        LogUtils.showLog("searchPercentage",""+searchPercentage.value)
        if(args.prodObj!=null && !isTabView && chkNull(searchPercentage.value,0.0f) >= 80.0f/*200.0f*/ && !isShowMarkFoundBtn.value){
            isShowMarkFoundBtn.value = true
        }
    }

    LaunchedEffect(searchPhase.value, proximityValue, searchRssi.value) {

        val (newBias, newPhase) = computeSteeringBias(
            currentPhase = searchPhase.value,
            lastPhase = lastPhase.floatValue,
            proximity = proximityValue,
            rssi = searchRssi.value,
            previousBias = steeringBias.floatValue
        )

        steeringBias.floatValue = newBias
        lastPhase.floatValue = newPhase

        val confidence = signalConfidence(proximityValue, searchRssi.value)

        val targetAngle = biasToAngle(newBias)

        directionAngle.floatValue = smoothAngle(
            previous = directionAngle.floatValue,
            target = targetAngle,
            confidence = confidence
        )
    }

    val msgInvalidScan =stringResource(R.string.err_invalid_scan)

    LaunchedEffect(scannedValue.value) {
        if (chkNull(scannedValue.value,"").isNotBlank()) {
            LogUtils.showLog("TEXTFIELD_UPDATE", "Setting text = "+chkNull(scannedValue.value,""))
            args.barCodeNo.value = chkNull(scannedValue.value,"")
            /*if(args.selectedType.value.equals("epc",true) && !(args.barCodeNo.value.matches(Regex("[0-9A-Fa-f]{8,36}")) && args.barCodeNo.value.length%4==0)){
                //readerViewModel.error().postValue(String.format(msgInvalidScan,args.selectedType.value,args.barCodeNo.value))
                /*snackbarController.show(
                    ErrorAppSnackBarData(String.format(msgInvalidScan,args.selectedType.value,args.barCodeNo.value))
                )*/
                return@LaunchedEffect
            }
            if(args.selectedType.value.equals("tid",true) && !(args.barCodeNo.value.matches(Regex("[0-9A-Fa-f]{8,}")) && args.barCodeNo.value.length%4==0)){
                //readerViewModel.error().postValue(String.format(msgInvalidScan,args.selectedType.value,args.barCodeNo.value))
                /*snackbarController.show(
                    ErrorAppSnackBarData(String.format(msgInvalidScan,args.selectedType.value,args.barCodeNo.value))
                )*/
                return@LaunchedEffect
            }*/
            args.isBarcodeConfirmed.value = true
            if (args.barCodeNo.value.isNotEmpty()) {
                callProductDetails(
                    args.barCodeNo,
                    args.selectedType,
                    apiViewModel,
                    readerViewModel,
                    args.context,
                    null
                )
            }
        }
    }

    /**LaunchedEffect(isBarcodeScan.value) {
        LogUtils.showLog("isBarcodeScan", "ProductSearchContent:${isBarcodeScan.value} ")
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

    /**LaunchedEffect(error.value) {
        val message = error.value
        if (!message.isNullOrBlank()) {
            LogUtils.showLog("isTagNotFound", "ProductSearchContent: $message")
            snackbarController.show(
                ErrorAppSnackBarData(message)
                *//**AppSnackBarData(
                    icon = R.drawable.property_1_error,
                    message = message,
                    showCancel = false
                )*//**
            )
        }
    }*/

    LaunchedEffect(triggerPressed.value) {
        if (triggerPressed.value == true) {
            readerViewModel.setTriggerValue(false)
            if (chkTrue(isApiLoading.value)) return@LaunchedEffect
            if(navController.currentBackStackEntry?.lifecycle?.currentState != Lifecycle.State.RESUMED) return@LaunchedEffect
            if(isUploadData.value || isDataUpload.value || isOnDataUploaded.value) return@LaunchedEffect
            //if(isUploadData.value) isUploadData.value=false;
            LogUtils.showLog("TriggerPressed", "starting scan")
            if (args.barCodeNo.value.isEmpty()) {
                readerViewModel.scanBarcode(args.selectedType.value.toString())
            }
            else if (args.firstFieldList.isNullOrEmpty()){
                if(args.selectedType.value.equals("epc",true) && !(args.barCodeNo.value.matches(Regex("[0-9A-Fa-f]{8,36}")) && args.barCodeNo.value.length%4==0)){
                    readerViewModel.error().postValue(String.format(msgInvalidScan,args.selectedType.value,args.barCodeNo.value))
                    /*snackbarController.show(
                        ErrorAppSnackBarData(String.format(msgInvalidScan,args.selectedType.value,args.barCodeNo.value))
                    )*/
                    return@LaunchedEffect
                }
                if(args.selectedType.value.equals("tid",true) && !(args.barCodeNo.value.matches(Regex("[0-9A-Fa-f]{8,}")) && args.barCodeNo.value.length%4==0)){
                    readerViewModel.error().postValue(String.format(msgInvalidScan,args.selectedType.value,args.barCodeNo.value))
                    /*snackbarController.show(
                        ErrorAppSnackBarData(String.format(msgInvalidScan,args.selectedType.value,args.barCodeNo.value))
                    )*/
                    return@LaunchedEffect
                }
                args.isBarcodeConfirmed.value = true
                callProductDetails(
                    args.barCodeNo,
                    args.selectedType,
                    apiViewModel,
                    readerViewModel,
                    args.context,
                    null
                )
            }
            else if (args.showSheet.value != false && args.currentSheet.value == BottomSheetType.SEARCH) {
                //Based on Selected Epc or TID
                if(args.selectedType.value!!.isNotEmpty() && args.selectedEan.value!=null)
                    readerViewModel.toggleSearch(if(args.selectedType.value.equals("epc",true)) SearchTypeConstant.EPC else SearchTypeConstant.TID,if(args.selectedType.value.equals("epc",true)) args.selectedEan.value!!.epc else args.selectedEan.value!!.tid)
            }
            else{
                val deviceId = chkNull(args.sessionId,DataStoreManager.readFromPreferences(args.preHeader + ParameterConstants.DEVICE_SESSION_ID, ""))
                if(isShowUpload && deviceId.isNullOrEmpty()){
                    val isSessionDataHasSrcAndDestZone = args.srcZone!=null && args.destZone!=null
                    val deviceSessionId = SessionUtils.generateOfflineSessionId(args.menuCode, args.transactionType)

                    val sessionData = JSONObject()
                    sessionData.put(ParameterConstants.SESSION_ID,deviceSessionId)
                    sessionData.put(ParameterConstants.ID,"")
                    sessionData.put(ParameterConstants.TRANSACTION_TYPE,args.transactionType)
                    if(isSessionDataHasSrcAndDestZone){
                     sessionData.put(ParameterConstants.ASSET_LOCATION_PATH,args.srcZone.path)
                     sessionData.put(ParameterConstants.MOVE_AT_ASSET_LOCATION_PATH,args.destZone.path)
                    }
                    DataStoreManager.saveToPreferences(args.preHeader + ParameterConstants.DEVICE_SESSION_ID, deviceSessionId)
                    DataStoreManager.saveToPreferences(args.preHeader + "sessionData", sessionData.toString())
                    if(isSessionDataHasSrcAndDestZone){
                        DataStoreManager.saveToPreferences(args.preHeader + ParameterConstants.ASSET_LOCATION_NAME, args.srcZone.name)
                        DataStoreManager.saveToPreferences(args.preHeader + ParameterConstants.ASSET_LOCATION_PATH, args.srcZone.path)
                        DataStoreManager.saveToPreferences(args.preHeader + ParameterConstants.MOVE_AT_ASSET_LOCATION_NAME, args.destZone.name)
                        DataStoreManager.saveToPreferences(args.preHeader + ParameterConstants.MOVE_AT_ASSET_LOCATION_PATH, args.destZone.path)
                    }
                    readerViewModel.setSessionId(deviceSessionId,sessionData.toString())
                }
                if(args.prodObj!=null && isTabView && args.selectedTab.value>0){
                    LogUtils.showLog("Dialog open 1", "ProductSearchContent: ")
                  val isQtyFullFilled = if(args.selectedTab.value==2) args.decodeCount.value >= args.foundCount.value else if(args.selectedTab.value==1) args.foundCount.value >= args.totalCount.value else false
                  if(isQtyFullFilled || chkTrue(isProcessOn.value)){
                      LogUtils.showLog("Dialog open 2", "ProductSearchCozxcntent: ")
                      //TODO give error
                      /*snackbarController.show(
                          ErrorAppSnackBarData(String.format(msgInvalidScan,args.selectedType.value,args.barCodeNo.value))
                      )*/
                      return@LaunchedEffect
                  }
                  else if(args.selectedTab.value==2)  {
                      args.selectedEan.value=null
                      LogUtils.showLog("Dialog open 3", "ProductSearchContent: ")
                      //call get passwords api (//TODO handle offline mode)
                      if(args.decodeTypesList.isNotEmpty() && args.decodeTypesList.size>1){
                          LogUtils.showLog("Dialog open 4", "ProductSearchContent: ")
                          args.decodeType.value = ""
                          openSheet(
                              scope = args.scope,
                              sheetState = args.sheetState,
                              showSheet = args.showSheet,
                              currentSheet = args.currentSheet,
                              sheet = BottomSheetType.DECODING_TYPE
                          )
                      }
                      else{
                          LogUtils.showLog("Dialog open 5", "ProductSearchContent: ")
                          apiViewModel.callApi(UrlConstants.GET_PASSWORD,DataStoreManager.readFromPreferences(LoginConstants.DEVICE_LOCATION_ID, ""))
                      }
                  }
                  else if(args.selectedTab.value==1) readerViewModel.performPick(pickPower = args.setPower.value,barcode=args.prodObj!!.barcode, isPostPicked = false/*args.topic.equals(TopicConstants.SEARCH_LIST_UPDATE,true)*/, isSavePickedToDB = true, isUpdateFound = false, isAllowNonEncodedTags = false, isAllowDuplicateTagRePick= false)
                }
                else {
                    val searchType = when (args.selectedType.value) {
                        "EPC" -> SearchTypeConstant.EPC
                        "TID" -> SearchTypeConstant.TID
                        else -> SearchTypeConstant.BARCODE
                    }
                    readerViewModel.toggleSearch(
                        searchType = searchType,
                        value = args.barCodeNo.value
                    )
                }
            }
        }
    }

    LaunchedEffect(isTagWriteDone.value) {
        LogUtils.showLog("isTagWriteDone11",""+isTagWriteDone.value)
        if(isTagWriteDone.value==true){
            if (args.showSheet.value != false && args.currentSheet.value == BottomSheetType.SEARCH){
              //Code for Auto clearing
              args.showSheet.value=false
              args.currentSheet.value = BottomSheetType.NONE
            }
            val sessionId= chkNull(args.sessionId,DataStoreManager.readFromPreferences(args.preHeader + ParameterConstants.DEVICE_SESSION_ID,""))
            val sessionData = DataStoreManager.readFromPreferences(args.preHeader + "sessionData","")
            LogUtils.showLog("isTagWriteDone11","Delay Start")
            //delay(50)
            LogUtils.showLog("isTagWriteDone11","Delay End")
            CoroutineScope(Dispatchers.IO).launch {
                val db = AppDatabase.getDbInstance(args.context)
                val listDecoded = db.tagInfoDao().getAllWrittenTagsAgainstBarcode1(args.topic,args.menuCode,args.transactionType,args.barCodeNo.value)
                LogUtils.showLog("isTagWriteDone11",""+listDecoded.size)
                LogUtils.showLog("listDecoded",""+listDecoded)
                MqttManager.publishListUpdate(listDecoded, args.menuCode, args.transactionType, sessionId, sessionData = sessionData, args.searchTypeId, args.searchTypeName, args.referenceNumber, args.searchValue)
            }
        }
    }

    if (isOnDataUploaded.value) {
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(dimensionResource(R.dimen.dp_16)),
            contentAlignment = Alignment.Center) {
            InventoryUploadedSuccess(count = args.foundCount.value)
        }
    }
    else {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(modifier = Modifier, verticalArrangement = Arrangement.Top) {
                    TopBarContent(args.label, onBackClickL = {
                        if (chkTrue(isProcessOn.value) || chkTrue(isApiLoading.value)) return@TopBarContent
                        if (isUploadData.value) {
                            isUploadData.value = false; return@TopBarContent
                        }
                        args.scope.launch {
                            val deviceSessionId = chkNull(args.sessionId,DataStoreManager.readFromPreferences(args.preHeader + ParameterConstants.DEVICE_SESSION_ID, ""))
                            if (deviceSessionId.isNullOrEmpty() || !isShowUpload) {
                                if(isSearchOptions && args.barCodeNo.value.isNotEmpty() && args.firstFieldList.isNotEmpty()) {
                                    args.barCodeNo.value = ""
                                    args.prodEan.value = ""
                                    args.isBarcodeConfirmed.value = false
                                }
                                else {
                                    clearSavedSessionValues(args.context, args.menuCode, args.transactionType, topic = args.topic)
                                    if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                                        readerViewModel.clearSessionAndTransactionType()
                                        navController.popBackStack()
                                    }
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
                        if (chkTrue(isProcessOn.value) || chkTrue(isApiLoading.value)) return@TopBarContent
                        openSheet(
                            scope = args.scope,
                            sheetState = args.sheetState,
                            showSheet = args.showSheet,
                            currentSheet = args.currentSheet,
                            sheet = BottomSheetType.SETTINGS
                        )
                    })
                }
                HorizontalDivider(
                    thickness = dimensionResource(R.dimen.dp_1),
                    color = OutlineDefault
                )

                if (isSearchOptions) {
                    if (args.barCodeNo.value.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = dimensionResource(R.dimen.dp_24)),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            FlowRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(
                                    dimensionResource(R.dimen.dp_8), Alignment.CenterHorizontally
                                ),
                                maxItemsInEachRow = 4
                            ) {

                                args.searchTypeList.forEach { data ->

                                    val isSelected =
                                        args.selectedType.value == data.name   // ✅ local state

                                    key(data.name) {

                                        Card(
                                            modifier = Modifier
                                                .width(dimensionResource(R.dimen.dp_76))
                                                .height(dimensionResource(R.dimen.dp_68))
                                                .border(
                                                    width = dimensionResource(R.dimen.dp_1),
                                                    color = if (isSelected) Green else TabColor,
                                                    shape = RoundedCornerShape(dimensionResource(R.dimen.dp_16))
                                                )
                                                .clickable(
                                                    onClick = {

                                                        val currentSelected =
                                                            args.selectedType.value
                                                        val clickedItem = data.name

                                                        if (currentSelected == clickedItem) return@clickable

                                                        args.selectedType.value =
                                                            clickedItem   // ✅ update locally

                                                    },
                                                    indication = null,
                                                    interactionSource = remember { MutableInteractionSource() }),
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
                                                        if (isSelected) R.drawable.success else data.icon
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
                                                    overflow = TextOverflow.Visible,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .basicMarquee()
                                                        .padding(horizontal = 5.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimensionResource(R.dimen.dp_12))
                ) {
                    val codeType = when (args.selectedType.value?.lowercase()) {
                        "barcode", "ean" -> CodeType.BARCODE
                        "epc" -> CodeType.EPC
                        "tid" -> CodeType.TID
                        else -> CodeType.NONE
                    }

                    if (!args.isBarcodeConfirmed.value) {
                        Spacer(modifier = Modifier.size(16.dp))
                        CommonTextField(
                            config = TextFieldConfig(
                                value = args.barCodeNo.value,
                                onValueChange = { newText ->
                                    if (newText != args.barCodeNo.value) {
                                        args.barCodeNo.value = newText
                                        args.firstFieldList.clear()
                                    }
                                    if (newText.isEmpty()) {
                                        args.isBarcodeConfirmed.value = false
                                    }
                                },
                                label = args.selectedLabel,//stringResource(id = R.string.enter_barcode),
                                imeAction = ImeAction.Done,
                                codeType = codeType,
                                onImeAction = {
                                    keyboardController?.hide()
                                    focusManager.clearFocus()
                                    if (args.barCodeNo.value.isNotBlank()) {
                                        if (args.selectedType.value.equals("epc", true) && !(args.barCodeNo.value.matches(Regex("[0-9A-Fa-f]{8,36}")) && args.barCodeNo.value.length % 4 == 0)) {
                                            readerViewModel.error().postValue(String.format(msgInvalidScan,args.selectedType.value,args.barCodeNo.value))
                                            /*snackbarController.show(
                                                ErrorAppSnackBarData(
                                                    String.format(
                                                        msgInvalidScan,
                                                        args.selectedType.value,
                                                        args.barCodeNo.value
                                                    )
                                                )
                                            )*/
                                            return@TextFieldConfig
                                        }
                                        if (args.selectedType.value.equals(
                                                "tid", true
                                            ) && !(args.barCodeNo.value.matches(Regex("[0-9A-Fa-f]{8,}")) && args.barCodeNo.value.length % 4 == 0)
                                        ) {
                                            readerViewModel.error().postValue(String.format(msgInvalidScan,args.selectedType.value,args.barCodeNo.value))
                                            /*snackbarController.show(
                                                ErrorAppSnackBarData(
                                                    String.format(
                                                        msgInvalidScan,
                                                        args.selectedType.value,
                                                        args.barCodeNo.value
                                                    )
                                                )
                                            )*/
                                            return@TextFieldConfig
                                        }
                                        args.isBarcodeConfirmed.value = true
                                        callProductDetails(
                                            args.barCodeNo,
                                            args.selectedType,
                                            apiViewModel,
                                            readerViewModel,
                                            args.context,
                                            null
                                        )
                                    }
                                },
                                onClick = {
                                    readerViewModel.setTriggerValue(true)
                                    /**isProcessOn.value?.let {
                                        if (!it) {
                                            readerViewModel.scanBarcode(args.selectedType.value.toString())
                                        }
                                    }*/
                                }
                            )
                        )
                        Text(
                            text = chkNull(readerError.value,apiError.value),
                            style = CommonTypography.current.textSemiBold,
                            textAlign = TextAlign.Center
                        )

                    }
                    else {
                        //val isGygerSearch=!isTabView
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(top = dimensionResource(R.dimen.dp_16))
                        ) {
                            val isProcessing = chkTrue(readerViewModel.isProcessOn().value) ||chkTrue(isApiLoading.value)
                            VerifyInfoCard(
                                value = if(args.isBarcodeConfirmed.value && args.firstFieldList.isNotEmpty() && args.prodEan.value.isNotEmpty()) args.prodEan.value else args.barCodeNo.value,
                                label = if(args.isBarcodeConfirmed.value && args.firstFieldList.isNotEmpty() && args.prodEan.value.isNotEmpty()) DataStoreManager.getBarcodeLabel() else args.selectedLabel,
                                onValueChange = { newText ->
                                    if (newText != args.barCodeNo.value) {
                                        args.barCodeNo.value = newText
                                    }

                                    if (newText.isEmpty()) {
                                        args.isBarcodeConfirmed.value = false
                                        args.firstFieldList.clear()
                                    }
                                },
                                onImeAction = {
                                    keyboardController?.hide()
                                    focusManager.clearFocus()
                                    if (args.barCodeNo.value.isNotBlank()) {
                                        if (args.selectedType.value.equals(
                                                "epc", true
                                            ) && !(args.barCodeNo.value.matches(Regex("[0-9A-Fa-f]{8,36}")) && args.barCodeNo.value.length % 4 == 0)
                                        ) {
                                            readerViewModel.error().postValue(String.format(msgInvalidScan,args.selectedType.value,args.barCodeNo.value))
                                            /*snackbarController.show(
                                                ErrorAppSnackBarData(
                                                    String.format(
                                                        msgInvalidScan,
                                                        args.selectedType.value,
                                                        args.barCodeNo.value
                                                    )
                                                )
                                            )*/
                                            return@VerifyInfoCard
                                        }
                                        if (args.selectedType.value.equals(
                                                "tid", true
                                            ) && !(args.barCodeNo.value.matches(Regex("[0-9A-Fa-f]{8,}")) && args.barCodeNo.value.length % 4 == 0)
                                        ) {
                                            readerViewModel.error().postValue(String.format(msgInvalidScan,args.selectedType.value,args.barCodeNo.value))
                                            /*snackbarController.show(
                                                ErrorAppSnackBarData(
                                                    String.format(
                                                        msgInvalidScan,
                                                        args.selectedType.value,
                                                        args.barCodeNo.value
                                                    )
                                                )
                                            )*/
                                            return@VerifyInfoCard
                                        }
                                        args.isBarcodeConfirmed.value = true
                                        callProductDetails(
                                            args.barCodeNo,
                                            args.selectedType,
                                            apiViewModel,
                                            readerViewModel,
                                            args.context,
                                            null
                                        )
                                    }
                                },
                                onScanClick = {
                                    readerViewModel.setTriggerValue(true)
                                    /**isProcessOn.value?.let {
                                        if (!it) {
                                            readerViewModel.scanBarcode(args.selectedType.value.toString())
                                        }
                                    }*/
                                },
                                verificationUiState = VerificationUiState.NONE,
                                verificationResult = VerificationResult.NONE,
                                infoList = args.firstFieldList,
                                showImagePreview = showImagePreview,
                                selectedPainter = selectedPainter,
                                modifier = Modifier.weight(if (!isTabView) 0.5f else 0.3f),
                                isTrillingIcon = isSearchOptions && !isProcessing,
                                isReadOnly = !isSearchOptions || isProcessing,
                                showTextFiled = if (args.textValue.isNotEmpty()) false else true
                            )

                            Column(
                                modifier = Modifier
                                    .weight(0.15f)
                                    .fillMaxWidth()
                                    .padding(top = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (isTabView) {
                                    SegmentedTabView(
                                        selectedIndex = args.selectedTab.value,
                                        tabs = args.tabList,
                                        modifier = Modifier
                                            //.weight(0.15f)
                                            .width(if(args.tabList.size == 3) 300.dp else 200.dp)
                                            .heightIn(max = 37.dp),
                                        onTabSelected = {
                                            if (chkTrue(isProcessOn.value) || chkTrue(isApiLoading.value)) return@SegmentedTabView
                                            args.selectedTab.value = it
                                        },
                                        padding = 2.dp
                                    )
                                    Spacer(modifier = Modifier.size(8.dp))
                                }
                                val err = chkNull(readerError.value, apiError.value)
                                if (args.prodObj != null && args.selectedTab.value == 0) {
                                    val color = /*if (args.foundCount.value <= 0) RedColor else */if (args.foundCount.value == args.totalCount.value) Green else TextGrey

                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {

                                        Text(text = "Found: " + args.foundCount.value + (if(args.foundCount.value>0 && args.foundCount.value<args.totalCount.value) "/" + args.totalCount.value else ""),
                                            color = color,
                                            style = CommonTypography.current.smallTxt,
                                            modifier = Modifier)

                                        if (args.decodeCount.value >= 1){
                                            val color = /*if (args.decodeCount.value <= 0) RedColor else */if (args.decodeCount.value == args.totalCount.value) Green else TextGrey
                                            Text(text = " | "+" Decoded: " + args.decodeCount.value + (if(args.decodeCount.value>0 && args.decodeCount.value<args.totalCount.value) "/" + args.totalCount.value else ""),
                                                color = color,
                                                style = CommonTypography.current.smallTxt,
                                                modifier = Modifier)
                                        }
                                    }
                                    /**InventoryBadge(
                                        text = if (args.foundCount.value <= 0) "Pending: " + args.totalCount.value else (if (isTabView) "Picked: " else "Found: ") + args.foundCount.value + "/" + args.totalCount.value,
                                        backGroundColor = if (args.foundCount.value <= 0) ErrorBgColor else if (args.foundCount.value == args.totalCount.value) CardGreen else OutlineDefault,
                                        textColor = color,//RedColor,
                                        iconColor = color,//RedColor,
                                        textStyle = CommonTypography.current.smallTxt,
                                        modifier = Modifier.weight(0.15f),
                                        padding = 2.dp
                                    )*/
                                }
                                else if (args.selectedTab.value > 0) {
                                    if (err.isNotBlank()) {
                                        Box(modifier = Modifier.weight(0.15f)) {
                                            Text(
                                                text = err,
                                                style = CommonTypography.current.textSemiBold,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.fillMaxSize().basicMarquee()
                                            )
                                        }
                                    }
                                }
                            }

                            /**if (isGygerSearch){
                            Column(modifier = Modifier.weight(0.60f)) {
                                Column(

                                    modifier = Modifier
                                        .weight(if(isGygerSearch) 1f else 0.30f)
                                        .fillMaxSize()
                                        .padding(horizontal = dimensionResource(R.dimen.dp_70))
                                        .padding(bottom = dimensionResource(R.dimen.dp_24)),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    DirectionProximityGauge(
                                        readerViewModel,
                                        modifier = Modifier,
                                        //searchRssi.value?.toFloatOrNull() ?: 0f,
                                    )
                                }


                                Column(
                                    modifier = Modifier
                                        .weight(0.12f)
                                        .fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {

                                    Text(
                                        text = stringResource(
                                            R.string.start_stop,
                                            if (isSearchOn.value == false) "Start Search" else "Stop Search",
                                            if (isSearchOn.value == false) "start" else "stop"
                                        ),
                                        style = CommonTypography.current.smallTxt,
                                        modifier = Modifier.padding(top = 30.dp),
                                        textAlign = TextAlign.Center
                                    )
                                }

                                Spacer(modifier = Modifier.size(24.dp))
                            }
                        }
                        else {*/
                            when (args.selectedTab.value) {
                                /*"Search"*/0 -> {
                                Column(
                                    modifier = Modifier
                                        .weight(0.30f)
                                        .fillMaxWidth()
                                        .padding(horizontal = dimensionResource(R.dimen.dp_70))
                                        .padding(bottom = dimensionResource(R.dimen.dp_50)),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    DirectionProximityGauge(
                                        readerViewModel,
                                        modifier = Modifier.fillMaxSize().aspectRatio(if(!isTabView) 2.5f else 1.3f),
                                        //searchRssi.value?.toFloatOrNull() ?: 0f,
                                    )
                                }

                                Column(
                                    modifier = Modifier
                                        .weight(0.12f)
                                        .fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = stringResource(
                                            R.string.start_stop,
                                            if (isSearchOn.value == false) "Start Search" else "Stop Search",
                                            if (isSearchOn.value == false) "start" else "stop"
                                        ),
                                        style = CommonTypography.current.smallTxt,
                                        modifier = Modifier.padding(top = 30.dp),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }


                                /*"Pick"*/1 -> {
                                Column(
                                    modifier = Modifier
                                        .weight(0.35f , false)
                                        .fillMaxWidth()
                                        .padding(bottom = dimensionResource(R.dimen.dp_12)),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    /**val err = chkNull(readerError.value,apiError.value)
                                    if (err.isNotBlank()) {
                                        Text(
                                            text = err,
                                            style = CommonTypography.current.textSemiBold,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.size(12.dp))
                                    }*/
                                    InventoryPulseCircle(
                                        totalCount = args.foundCount.value,
                                        tagCount = args.foundCount.value.toString(),
                                        isInvOn = chkTrue(isPickOn.value),
                                        lastTagCount = args.totalCount.value.toString(),
                                        showUploadSwipe = false,
                                        onClick = {},
                                        modifier = Modifier
                                            .fillMaxWidth(0.72f)
                                            .aspectRatio(1f)
                                    )
                                }
                            }

                                /*"Picked"*/2 -> {
                                Column(
                                    modifier = Modifier
                                        .weight(0.4f)
                                        .fillMaxSize()
                                        .padding(bottom = dimensionResource(R.dimen.dp_12)),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    /*val db = AppDatabase.getDbInstance(args.context)
                                val searchQuery = remember { mutableStateOf("") }
                                val eanList = db.tagInfoDao().getTagTime(args.menuCode, args.transactionType, args.barCodeNo.value, searchQuery.value).collectAsState(initial = emptyList())
                                val expandedEans = remember { mutableStateMapOf<String, Boolean>() }
                                val filteredBrands = if (searchQuery.value.isEmpty()) eanList.value else eanList.value.filter { ean ->
                                        ean.barcode.contains(
                                            searchQuery.value, ignoreCase = true
                                        )
                                    }*/

                                    RowViewWithHSI1(
                                        epcList = args.listPickedTags.value,
                                        onSearchClick = { tagTime ->
                                            args.selectedEan.value = tagTime
                                            openSheet(
                                                scope = args.scope,
                                                sheetState = args.sheetState,
                                                showSheet = args.showSheet,
                                                currentSheet = args.currentSheet,
                                                sheet = BottomSheetType.SEARCH
                                            )
                                        },
                                        /*onDetailsClick = { tagTime ->
                                            openSheet(
                                                scope = args.scope,
                                                sheetState = args.sheetState,
                                                showSheet = args.showSheet,
                                                currentSheet = args.currentSheet,
                                                sheet = BottomSheetType.PRODUCT_DETAILS
                                            )
                                        },*/
                                    )
                                }
                            }
                            }
                            //}
                        }
                    }
                }
            }
        }
    }

    if (args.showSheet.value) {
        ModalBottomSheet(
            sheetState = args.sheetState,
            onDismissRequest = {},
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
                            DataStoreManager.saveToPreferences(args.preHeader + ParameterConstants.READER_POWER,args.setPower.value)
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
                        tagCount = args.foundCount.value,
                        onStopSession = {
                            if (chkTrue(isProcessOn.value) || chkTrue(isApiLoading.value)) return@SessionContent
                            args.scope.launch {
                                if(navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                                    clearSavedSessionValues(args.context,args.menuCode,args.transactionType,false,args.topic)
                                    readerViewModel.clearSessionAndTransactionType()
                                    navController.popBackStack()
                                }
                            }
                        },
                        readerViewModel = readerViewModel,
                        limit = args.totalCount.value,
                        isAllowContinue = false
                    )
                }

                BottomSheetType.UPLOAD ->
                    Box(
                        modifier = Modifier
                            .background(WhiteColor)
                            .fillMaxWidth()
                            .wrapContentHeight()
                            //.fillMaxHeight(0.44f)
                    ) {
                      val formatedText = buildAnnotatedString {
                            append(stringResource(R.string.total_scanned_items))
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append("${args.foundCount.value}")
                            }
                        }
                      UploadDataSheet(
                            title = args.label,//stringResource(R.string.item_movement,args.label),
                            //heading = stringResource(R.string.confirm_movement,args.label),
                            subHeading = formatedText,
                            //primaryButtonText = R.string.confirm_and_move,args.label),
                            showSheet = args.showSheet,
                            sheetState = args.sheetState,
                            scope = args.scope,
                            tagCount = args.foundCount.value,
                            isDataUploaded = isDataUpload
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
                        isPowerSet = args.tabList.size>1,
                        isFilterEnable = args.barCodeNo.value.isNotEmpty() && args.firstFieldList.isNotEmpty() && (isSearchOptions || (args.prodObj==null && args.tabList.size==0))
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
                            //Build dynamic filters
                            //dynamicFilters.clear()

                            /*selectedFilters.forEach { filterName ->
                                val list = when (filterName) {
                                    "Brand" -> listOf("Nike", "Adidas", "Puma")
                                    "Category" -> listOf("Shirt", "Pant", "Shoes")
                                    "Size" -> listOf("S", "M", "L", "XL")
                                    "Color" -> listOf("Red", "Blue", "Black")
                                    else -> listOf("Default 1", "Default 2")
                                }

                                dynamicFilters.add(
                                    FilterItem(
                                        label = filterName,
                                        list = list.map {
                                            LocationModel(
                                                id = it,
                                                name = it,
                                                code = it,
                                                path = it
                                            )
                                        }
                                    )
                                )
                            }*/

                            val isSelFilterKeysEmpty = selFilterKeys.size<=0
                            LogUtils.showLog("D_selectedMAp",selectedMap.toString())
                            val selectedValues = selectedMap.filter { it.isNotBlank() }.distinct()
                            LogUtils.showLog("D_selectedMAp1",selectedValues.toString())

                            selFilterKeys.clear()
                            selFilterKeys.addAll(selectedValues)

                            val userId = DataStoreManager.readFromPreferences(ParameterConstants.USER_ID,"")
                            DataStoreManager.saveListStr(userId+"_"+args.menuCode+"_filters",selectedValues)

                            if(isSelFilterKeysEmpty) {
                                val searchBundle = mapOf(
                                    "topic" to args.topic,
                                    "sessionType" to args.menuCode,
                                    "transactionType" to args.transactionType,
                                    "selFilterKeys" to selFilterKeys,
                                    ParameterConstants.BARCODE to args.prodEan.value
                                )
                                val route = Screen.ChartScreen.createRoute(
                                    code = args.menuCode,
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
                        selectedSearchType = args.selectedSearchType,
                        tagInfoEntity = args.selectedEan,
                        readerViewModel = readerViewModel,
                        isDecode=true,//args.selectedEan.value!=null && !args.selectedEan.value!!.isUnencodedTag(),
                        decodeType=args.decodeType.value,
                        onDecodeClick = {
                            args.scope.launch {
                                args.sheetState.hide()
                                args.showSheet.value = false
                                delay(100)
                                if(args.decodeTypesList.isNotEmpty() && args.decodeTypesList.size>1){
                                    LogUtils.showLog("Dialog open 4", "ProductSearchContent: ")
                                    args.decodeType.value = ""
                                    openSheet(
                                        scope = args.scope,
                                        sheetState = args.sheetState,
                                        showSheet = args.showSheet,
                                        currentSheet = args.currentSheet,
                                        sheet = BottomSheetType.DECODING_TYPE
                                    )
                                }
                                else apiViewModel.callApi(UrlConstants.GET_PASSWORD,DataStoreManager.readFromPreferences(LoginConstants.DEVICE_LOCATION_ID, ""))
                            }
                        }
                    )
                }

                BottomSheetType.PRODUCT_DETAILS -> Box(
                    modifier = Modifier
                        .background(WhiteColor)
                        .fillMaxWidth()
                        .fillMaxHeight(0.55f)
                ) {
                        ProductDetailsByEan(
                            barcode = args.prodEan.value,
                            infoList = args.firstFieldList,
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

                BottomSheetType.DECODING_TYPE ->Box(
                    modifier = Modifier
                        .background(WhiteColor)
                        .fillMaxWidth()
                        .fillMaxHeight(0.35f)
                ) {
                    DecodingTypeList(
                        args.decodeTypesList,
                        args.scope,
                        args.showSheet,
                        args.sheetState,
                        args.decodeType,
                        onConfirmClick ={
                            args.scope.launch {

                                args.decodeType
                                apiViewModel.callApi(UrlConstants.GET_PASSWORD,DataStoreManager.readFromPreferences(LoginConstants.DEVICE_LOCATION_ID, ""))
                                args.sheetState.hide()
                                args.showSheet.value = false
                               /** val decodeType = selectedDecodeType.value
                                val tagInfo = tagInfoData.value

                                if (decodeType.isNullOrBlank() || tagInfo == null) {
                                    return@launch
                                }

                                LogUtils.showLog("selectedDecodeType", "DecodingTypeList: $decodeType")

                                // Perform decode
                                readerViewModel.performDecoding(
                                    tagInfo,
                                    decodeType = decodeType
                                )
                                LogUtils.showLog("selectedDecodeType", "DecodingTypeList: $decodeType")

                                // Close bottom sheet
                                args.sheetState.hide()
                                args.showSheet.value = false*/
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
        if (showImagePreview.value){
            if(chkTrue(isApiLoading.value) || chkTrue(isProcessOn.value))  showImagePreview.value=false
            else ImageViewFullScreen(showImagePreview , selectedPainter)
        }
    }

    /*Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        AppSnackBar(snackbarController)
    }*/
}

data class SearchTypeItem(
    val name: String,
    val label: String,
    val icon: Int
)

fun callProductDetails(
    barCodeNo: MutableState<String>,
    selectedType: MutableState<String?>,
    apiViewModel: ApiViewModel,
    readerViewModel: ReaderViewModel,
    context: Context,
    tagInfoData: TagInfoEntity?
) {
    val map = HashMap<String,String>()
    map.put(ParameterConstants.CUSTOMER_ID,DataStoreManager.readFromPreferences(ParameterConstants.CUSTOMER_ID,""))
    map.put(ParameterConstants.LOCATION_ID,DataStoreManager.readFromPreferences(ParameterConstants.BUSINESS_LINE_ID,""))
    if (tagInfoData != null && tagInfoData.barcode.isNotEmpty()) {
        map.put(ParameterConstants.EAN,tagInfoData.barcode)
        map.put(ParameterConstants.EPC,tagInfoData.epc)
        map.put(ParameterConstants.TID,tagInfoData.tid)
    }
    else {
        if(selectedType.value.equals(SearchTypeConstant.TID,true)) map.put(ParameterConstants.TID,barCodeNo.value)
        else if(selectedType.value.equals(SearchTypeConstant.EPC,true)){
         map.put(ParameterConstants.EPC,barCodeNo.value)
         map.put(ParameterConstants.EAN,EpcEncoderDecoderWrapper.getBarcodeFromEpc(barCodeNo.value))
        }
        else map.put(ParameterConstants.EAN,barCodeNo.value)
    }
    apiViewModel.callApi(url = UrlConstants.PRODUCTS, queryMap = map)
    LogUtils.showLog("PRODUCTS_API_START", "Start")
}


@SuppressLint("SuspiciousIndentation")
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductSearchBottomBar(
    args: ProductSearchScreenArgs,
    apiViewModel: ApiViewModel,
    readerViewModel: ReaderViewModel,
    isShowUpload: Boolean,
    isDataUpload: MutableState<Boolean>,
    isOnDataUploaded: MutableState<Boolean>,
    isUploadData: MutableState<Boolean>,
    isShowMarkFoundBtn: MutableState<Boolean>,
    snackbarController: SnackbarController,
    isSearchOptions: Boolean,
    navController: NavHostController,
    selFilterKeys: SnapshotStateList<String>,
) {
    val isProcessOn = readerViewModel.isProcessOn().observeAsState(false)
    val isSearchOn = readerViewModel.isSearchOn().observeAsState(false)
    val isPickOn = readerViewModel.isPickOn().observeAsState(false)
    val isTagWriteOn = readerViewModel.isTagWriteOn().observeAsState(false)
    val isApiLoading = apiViewModel.isLoading.observeAsState(false)

    val buttonText = if(args.selectedTab.value==2) stringResource(R.string.decode_all) else if(args.selectedTab.value==1) stringResource(R.string.pick_items) else stringResource(if(chkTrue(isSearchOn.value)) R.string.stop_search else R.string.start_search)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(WhiteColor)
    ) {
        HorizontalDivider(color = OutlineDefault)

        /*val hasNoItemToDecode =  args.selectedTab.value==2 && args.listNonDecodedTags.value.isNullOrEmpty()
        LogUtils.showLog("listNonDecodedTags",""+args.listNonDecodedTags.value.size)
        LogUtils.showLog("hasNoItemToDecode",""+hasNoItemToDecode)*/
        val isQtyFullFilled = if(args.selectedTab.value==2) args.decodeCount.value >= args.foundCount.value else if(args.selectedTab.value==1) args.foundCount.value >= args.totalCount.value else false
        val hasScannedItems = args.barCodeNo.value.isNotEmpty() && args.firstFieldList.isNotEmpty() && (args.prodObj==null || (isShowUpload && args.foundCount.value>0 && args.dbFoundCount.value>0 && args.foundCount.value <= args.totalCount.value) || (isShowMarkFoundBtn.value && args.foundCount.value < args.totalCount.value))
        LogUtils.showLog("hasScannedItems",""+hasScannedItems)
        LogUtils.showLog("isQtyFullFilled",""+isQtyFullFilled)
        LogUtils.showLog("isProcessOn",""+isProcessOn.value)
        if (args.barCodeNo.value.isNotEmpty() && args.firstFieldList.isNotEmpty() && !isQtyFullFilled && /*!hasNoItemToDecode &&*/ (chkTrue(isProcessOn.value) || (!hasScannedItems && !isOnDataUploaded.value))) {
            /* ---------------- INITIAL STATE → START SCANNING ---------------- */
            Row(modifier = Modifier.padding(dimensionResource(R.dimen.dp_16))) {
                CommonButton(
                    text = buttonText,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        readerViewModel.setTriggerValue(true)
                    },
                    icon = if (chkTrue(isSearchOn.value)) painterResource(R.drawable.stopicon) else null,
                    gradientBrush = if (chkTrue(isSearchOn.value)) SolidColor(WhiteColor)
                    else Brush.horizontalGradient(listOf(BlackColor, ButtonGray)),
                    contentColor = if (chkTrue(isSearchOn.value)) BlackColor else WhiteColor,
                    enabled = !chkTrue(isPickOn.value)  && !chkTrue(isTagWriteOn.value)  //true/*selectedSrcLocationName.value.isNotEmpty() && selectedDestLocationName.value.isNotEmpty()*/
                )
            }
        }
        else if (!chkTrue(isProcessOn.value) && hasScannedItems && isShowUpload && isUploadData.value && !isOnDataUploaded.value) {
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
        else if (!chkTrue(isProcessOn.value) && hasScannedItems && /*hasNoItemToDecode &&*/ !isOnDataUploaded.value) {
            TwoButtonView(
                primaryText = buttonText,
                seconderText = stringResource(if(isShowUpload) R.string.txt_upload_items else if(args.prodObj!=null) R.string.mark_as_found else R.string.check_availability),

                primaryAction = {
                    readerViewModel.setTriggerValue(true)
                },
                seconderAction = {
                    if(isShowUpload) {
                        //isUploadData.value = true
                        openSheet(
                            scope = args.scope,
                            sheetState = args.sheetState,
                            showSheet = args.showSheet,
                            currentSheet = args.currentSheet,
                            sheet = BottomSheetType.UPLOAD
                        )
                    }
                    else if(chkTrue(isShowMarkFoundBtn.value)) {
                       CoroutineScope(Dispatchers.IO).launch {
                           val db = AppDatabase.getDbInstance(args.context)
                           val productZoneDataDao = db.productZoneDataDao()
                           if(productZoneDataDao.hasBarcodeZone(args.topic, args.menuCode, args.transactionType,args.sessionId, args.prodObj!!.barcode, args.prodObj!!.name,args.prodObj!!.path))
                               productZoneDataDao.updateFoundBarcodeZone(args.topic, args.menuCode, args.transactionType, args.sessionId,args.prodObj!!.barcode, args.prodObj!!.name,args.prodObj!!.path)
                           val sessionData = DataStoreManager.readFromPreferences(args.preHeader + "sessionData","")
                           MqttManager.publishListUpdate(null,args.menuCode, args.transactionType, args.sessionId, sessionData = sessionData, args.searchTypeId, args.searchTypeName, args.referenceNumber, args.searchValue,args.prodObj!!.barcode,args.foundCount.value)
                       }
                    }
                    else if(args.prodObj==null) { // Check Availability
                        val userId = DataStoreManager.readFromPreferences(ParameterConstants.USER_ID,"")
                        val filterList = DataStoreManager.getListStr(userId + "_" + args.menuCode + "_filters")
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
                                    "topic" to args.topic,
                                    "sessionType" to args.menuCode,
                                    "transactionType" to args.transactionType,
                                    "selFilterKeys" to selFilterKeys,
                                    ParameterConstants.BARCODE to args.prodEan.value
                                )
                                val route = Screen.ChartScreen.createRoute(code = args.menuCode, label = "Check Availability", params = searchBundle)
                                navController.navigate(route)
                            }
                    }
                },
                isSwipeSecondaryActionForPrimaryIfNoSecondaryButton = true,
                isSecondaryButton = args.selectedTab.value==0 || (args.selectedTab.value==1 && args.foundCount.value<args.totalCount.value) || (args.selectedTab.value==2 && args.decodeCount.value<args.totalCount.value),

                modifier = Modifier.padding(dimensionResource(R.dimen.dp_16))
            )
        }
        /**else if(args.firstFieldList.isNotEmpty()){
                Row(modifier = Modifier.padding(dimensionResource(R.dimen.dp_16))) {
                    TwoButtonView(
                        seconderText = stringResource(R.string.check_availability),
                        primaryText = buttonText,
                        seconderAction = {

                        },
                        primaryAction = {
                            readerViewModel.setTriggerValue(true)
                        },
                        isSecondaryButton = !chkTrue(isSearchOn.value),
                        primaryTextColor = if (chkTrue(isSearchOn.value)) BlackColor else WhiteColor,
                        primaryBackGroundColor = if (chkTrue(isSearchOn.value)) SolidColor(WhiteColor) else SolidColor(BlackColor)
                    )
                }
        }*/
    }
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter
    ) {
        AppSnackBar(snackbarController)
    }

}



fun phaseTo180Degrees(phase: String?): Float {
    val phaseValue = phase?.toFloatOrNull() ?: return 0f
    val deg360 = ((phaseValue % 4096f) / 4096f) * 360f
    return if (deg360 > 180f) 360f - deg360 else deg360
}

private const val MIN_PROXIMITY_FOR_DIRECTION = 30f
private const val MAX_BIAS = 0f
private const val PHASE_SENSITIVITY = 0.0020f   // lower = smoother, higher = reactive
private const val centerAngle = 90f
private const val maxOffset = 45f              // widen to 60 if needed



fun computeSteeringBias(
    currentPhase: String?,
    lastPhase: Float,
    proximity: Float,
    rssi: String?,
    previousBias: Float
): Pair<Float, Float> {

    val phase = currentPhase?.toFloatOrNull() ?: return previousBias to lastPhase

    val confidence = signalConfidence(proximity, rssi)

    // Weak signal → drift back to center
    if (confidence < 0.25f) {
        return (previousBias * 0.96f) to lastPhase
    }

    // ✅ SMOOTH PHASE BEFORE DELTA
    val smoothedPhase = smoothPhaseDelta(
        current = phase,
        last = lastPhase
    )

    val delta = smoothedPhase - lastPhase

    // Convert phase delta → bias, scaled by confidence
    val biasChange = delta * PHASE_SENSITIVITY * confidence

    val newBias = (previousBias + biasChange)
        .coerceIn(-MAX_BIAS, MAX_BIAS)

    // Return smoothed phase as lastPhase
    return newBias to smoothedPhase
}


fun biasToAngle(bias: Float): Float {
    val center = 90f
    val maxOffset = 45f   // left/right span
    return center + bias * maxOffset
}

fun smoothAngle(
    previous: Float,
    target: Float,
    confidence: Float
): Float {
    val alpha = 0.04f + confidence * 0.12f   // 0.04 → 0.16
    return previous + alpha * (target - previous)
}

fun smoothPhaseDelta(
    current: Float,
    last: Float,
    alpha: Float = 0.2f
): Float {
    val rawDelta = current - last
    return last + alpha * rawDelta
}


fun signalConfidence(
    proximity: Float,
    rssi: String?
): Float {
    val rssiValue = rssi?.toFloatOrNull() ?: return 0f

    // ❌ Tag not detected
    if (rssiValue < -85f || proximity <= 0f) {
        return 0f
    }

    val proxWeight = (proximity / 100f).coerceIn(0f, 1f)
    val rssiWeight = ((rssiValue + 90f) / 40f).coerceIn(0f, 1f)

    return (0.6f * proxWeight + 0.4f * rssiWeight)
}



