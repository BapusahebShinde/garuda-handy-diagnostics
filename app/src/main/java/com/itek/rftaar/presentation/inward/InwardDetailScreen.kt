package com.itek.rftaar.presentation.inward

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController
import com.google.gson.Gson
import com.itek.rftaar.R
import com.itek.rftaar.ReaderActivity
import com.itek.rftaar.api.constants.ParameterConstants
import com.itek.rftaar.api.constants.UrlConstants
import com.itek.rftaar.core.common.constants.DateFormatUtils
import com.itek.rftaar.core.common.constants.LoginConstants
import com.itek.rftaar.core.common.utils.LogUtils
import com.itek.rftaar.core.common.utils.ParseUtils
import com.itek.rftaar.core.common.utils.ParseUtils.extractJSONArray
import com.itek.rftaar.core.common.utils.ParseUtils.extractJSONObject
import com.itek.rftaar.core.common.utils.ParseUtils.extractString
import com.itek.rftaar.core.common.utils.ToastUtils
import com.itek.rftaar.core.database.AppDatabase
import com.itek.rftaar.core.database.DataStoreManager
import com.itek.rftaar.data.entity.DataQtyEntity
import com.itek.rftaar.data.entity.InOutConfigEntity
import com.itek.rftaar.data.entity.TagInfoEntity
import com.itek.rftaar.data.model.EanFoundQty
import com.itek.rftaar.data.model.IOLevel
import com.itek.rftaar.mqtt.constants.TopicConstants
import com.itek.rftaar.network.NetworkMonitor
import com.itek.rftaar.presentation.commonComp.AppSnackBar
import com.itek.rftaar.presentation.commonComp.BottomSheetType
import com.itek.rftaar.presentation.commonComp.CommonButton
import com.itek.rftaar.presentation.commonComp.ErrorAppSnackBarData
import com.itek.rftaar.presentation.commonComp.InventoryPulseCircle
import com.itek.rftaar.presentation.commonComp.MessageComponent
import com.itek.rftaar.presentation.commonComp.SessionBottomSheet
import com.itek.rftaar.presentation.commonComp.SessionUiActions
import com.itek.rftaar.presentation.commonComp.SessionUiState
import com.itek.rftaar.presentation.commonComp.SnackbarController
import com.itek.rftaar.presentation.commonComp.SuccessAppSnackBarData
import com.itek.rftaar.presentation.commonComp.TopBarContent
import com.itek.rftaar.presentation.commonComp.openSheet
import com.itek.rftaar.presentation.encoding.DeviceSettings
import com.itek.rftaar.presentation.inventory.AcceptDataSheet
import com.itek.rftaar.presentation.inventory.RejectDataSheet
import com.itek.rftaar.presentation.inventory.SetInvDevicePower
import com.itek.rftaar.presentation.viewmodel.ApiViewModel
import com.itek.rftaar.presentation.viewmodel.InwardSharedViewModel
import com.itek.rftaar.presentation.viewmodel.ReaderViewModel
import com.itek.rftaar.reader.constants.StatusConstants
import com.itek.rftaar.ui.theme.BlackColor
import com.itek.rftaar.ui.theme.ButtonGray
import com.itek.rftaar.ui.theme.CommonTypography
import com.itek.rftaar.ui.theme.Green
import com.itek.rftaar.ui.theme.OutlineDefault
import com.itek.rftaar.ui.theme.RedColor
import com.itek.rftaar.ui.theme.ShadowColor
import com.itek.rftaar.ui.theme.TextSubtext
import com.itek.rftaar.ui.theme.WhiteColor
import com.itek.rftaar.utils.CommonUtils
import com.itek.rftaar.utils.CommonUtils.chkNull
import com.itek.rftaar.utils.CommonUtils.chkTrue
import com.itek.rftaar.utils.NetworkUtils.isInternetConnected
import com.itek.rftaar.utils.SessionUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InwardDetailScreen(
  modifier: Modifier,
  navController: NavHostController,
  menuCode: String,
  label: String,
  /*transactionType: String = "",*/
  searchParams: Map<String, Any> = emptyMap(),
  inwardSharedViewModel: InwardSharedViewModel = hiltViewModel(),
  apiViewModel: ApiViewModel = hiltViewModel()
) {

  val isInward = menuCode.matches(Regex("(?i)(^.*INW.*$)"))
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val sheetState = rememberModalBottomSheetState(
    skipPartiallyExpanded = true,
    confirmValueChange = { sheetValue ->
      sheetValue != SheetValue.Hidden
    }
  )
  val showSheet = remember { mutableStateOf(false) }
  val snackbarController = remember { SnackbarController() }
  val topic = if(isInward) TopicConstants.INWARD else TopicConstants.OUTWARD;
  val preHeader = topic + "_" + menuCode + "_"
  val transactionType = inwardSharedViewModel.generateOfflineSessionId(menuCode)
  val activity = context as? ReaderActivity
  val readerViewModel = if (activity != null) activity.findReaderViewModel() else hiltViewModel()
  if (activity != null) {
    clearSavedSessionValues(context, topic,menuCode, transactionType, preHeader)
    readerViewModel.onCreate()
    readerViewModel.setSessionAndTransactionType(menuCode,transactionType,topic=if (isInward) TopicConstants.INWARD else TopicConstants.OUTWARD)
  }
  val currentSheet = remember { mutableStateOf(BottomSheetType.NONE) }
  val selLevel = inwardSharedViewModel.getLastInwardLevel()
  val isDataUploaded = remember { mutableStateOf(false) }
  val isEncodingStarted = remember { mutableStateOf(false) }
  /*if(selLevel==null){
    inwardSharedViewModel.removeLastInwardLevel()
    clearSavedSessionValues(context, topic,menuCode, transactionType, preHeader)
    if(navController.currentBackStackEntry?.lifecycle?.currentStateAsState()?.value == Lifecycle.State.RESUMED) {
    readerViewModel.clearSessionAndTransactionType()
    navController.popBackStack()}
  }*/

  val listNonEncodedTags = remember { mutableStateOf(listOf<TagInfoEntity>()) }
  val listExpected = remember { mutableStateOf(listOf<DataQtyEntity>()) }
  val listExpectedBarcodes = remember { mutableStateOf(listOf<String>()) }
  val listExpectedEpcs = remember { mutableStateOf(listOf<String>()) }
  val readerPower = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.READER_POWER, 15)
  val setPower = rememberSaveable { mutableStateOf(readerPower) }
  readerViewModel.setPower(setPower.value)

  val isArticleBased = remember { mutableStateOf(false) }
  val lableArticle = remember { mutableStateOf("Article") }
  val lableBarcode = remember { mutableStateOf(DataStoreManager.getBarcodeLabel()) }
  val isQtyOnly = remember { mutableStateOf(false) }
  val enableEncoding = remember { mutableStateOf(false) }
  val allowBarcodeScan = remember { mutableStateOf(false) }
  val boxMaxLimit = remember { mutableStateOf(0) }
  val totalExpQty = chkNull(selLevel?.total,0)
  val isInvOn = readerViewModel.isInventoryOn().observeAsState(initial = false)
  val selectedTab = remember { mutableStateOf(0) }


  LaunchedEffect(Unit) {
    if (apiViewModel != null && selLevel!=null) {
      val map = hashMapOf(
        ParameterConstants.CUSTOMER_ID to DataStoreManager.readFromPreferences(ParameterConstants.CUSTOMER_ID, ""),
        ParameterConstants.BUSINESS_LINE_ID to DataStoreManager.readFromPreferences(ParameterConstants.BUSINESS_LINE_ID, ""),
        ParameterConstants.LOCATION_ID to DataStoreManager.readFromPreferences(LoginConstants.DEVICE_LOCATION_ID, ""),
        ParameterConstants.DEVICE_ID to DataStoreManager.readFromPreferences(ParameterConstants.DEVICE_ID, "")
      )
      val size = inwardSharedViewModel.inwardLevels.size;
      map.put(ParameterConstants.FLOW_ID, chkNull(selLevel?.flowId,""))
      map.put(ParameterConstants.LEVEL, chkNull(selLevel?.level,0).toString())
      map.put(ParameterConstants.NODE_LEVEL, chkNull(selLevel?.nodeLevel,0).toString())
      //for ((index, value) in inwardSharedViewModel.inwardLevels.withIndex()) {
      for (i in 0 until size) {
        val level = inwardSharedViewModel.inwardLevels[i]
        map.put(ParameterConstants.NODE + (level.level - i), level.node)
      }
      apiViewModel.callApi(if (isInward) UrlConstants.GET_INWARD_BOX_DETAILS else UrlConstants.GET_OUTWARD_BOX_DETAILS, queryMap = map)
    }
    else if(selLevel==null){
      inwardSharedViewModel.removeLastInwardLevel()
      clearSavedSessionValues(context, topic,menuCode, transactionType, preHeader)
      readerViewModel.clearSessionAndTransactionType()
      navController.popBackStack()
    }
  }

  BackHandler(enabled = true) {
    if (readerViewModel.isProcessOn().value == true || apiViewModel.isLoading.value == true) return@BackHandler
    scope.launch {
      val deviceSessionId = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.DEVICE_SESSION_ID, "")
      if (deviceSessionId.isNullOrEmpty()) {
        inwardSharedViewModel.removeLastInwardLevel()
        clearSavedSessionValues(context, topic,menuCode, transactionType, preHeader)
        if(navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
        readerViewModel.clearSessionAndTransactionType()
        navController.popBackStack()}
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

  val inwardDetailArgs = InwardDetailArgs(
    context = context,
    isInward = isInward,
    menuCode = menuCode,
    preHeader = preHeader,
    transactionType = transactionType,
    label = label,
    scope = scope,
    sheetState = sheetState,
    showSheet = showSheet,
    currentSheet = currentSheet,
    selLevel = selLevel,
    totalExpQty = totalExpQty,
    setPower = setPower,
    topic = topic,
    listExpected = listExpected,
    listExpectedBarcodes = listExpectedBarcodes,
    listExpectedEpcs = listExpectedEpcs,
    enableEncoding = enableEncoding,
    allowBarcodeScan = allowBarcodeScan,
    boxMaxLimit = boxMaxLimit,
    isArticleBased = isArticleBased,
    labelArticle = lableArticle,
    labelBarcode = lableBarcode,
    isQtyOnly =isQtyOnly,
    isDataUploaded = isDataUploaded,
    listNonEncodedTags = listNonEncodedTags,
    isEncodingStarted = isEncodingStarted,
    isInvOn = isInvOn,
    selectedTab = selectedTab
  )

  Box {
    Scaffold(
      bottomBar = { InwardDetailBottomBar(readerViewModel,inwardDetailArgs) }
    ) { innerPadding ->
      Box(
        modifier = modifier
          .background(
            brush = Brush.verticalGradient(
              listOf(
                Color(0xFFF3F3F3), Color(0xFFFFFFFF), Color(0xFFFFFFFF), Color(0xFFFFFFFF)
              )
            )
          )
          .padding(innerPadding)
          .fillMaxSize()
      ) {
        InwardDetailContent(
          inwardDetailArgs,
          readerViewModel,
          apiViewModel,
          inwardSharedViewModel,
          navController,
          snackbarController
        )
      }
    }
  }
}

@Composable
fun InwardDetailBottomBar(readerViewModel: ReaderViewModel, args: InwardDetailArgs) {
  if (args.selectedTab.value == 1){

    Column(
      modifier = Modifier
        .wrapContentHeight()
        .fillMaxWidth()
        .background(WhiteColor)
    ) {
      HorizontalDivider(color = OutlineDefault)
      Row(modifier = Modifier.padding(dimensionResource(R.dimen.dp_16))) {
        CommonButton(
          onClick = {
            readerViewModel.setTriggerValue(true)
          },
          icon = if (args.isInvOn.value == true) painterResource(R.drawable.stopicon) else null,
          text = stringResource(
            id = if (args.isInvOn.value == true) R.string.stop_scanning
            //else if (validCount.value > 0) R.string.continue_scanning
            else R.string.start_scanning
          ),
          modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
          contentColor = if (args.isInvOn.value == true) BlackColor else WhiteColor,
          gradientBrush = if (args.isInvOn.value == true) SolidColor(WhiteColor) else Brush.horizontalGradient(
            colors = listOf(
              BlackColor, ButtonGray
            )
          )
        )
      }
    }
  }
}

data class InwardDetailArgs @OptIn(ExperimentalMaterial3Api::class) constructor(
  val context: Context,
  val isInward: Boolean,
  val menuCode: String,
  val preHeader: String,
  val transactionType: String,
  val label: String,
  val scope: CoroutineScope,
  val sheetState: SheetState,
  val showSheet: MutableState<Boolean>,
  val currentSheet: MutableState<BottomSheetType>,
  val selLevel: IOLevel?,
  val totalExpQty: Int,
  val setPower: MutableState<Int>,
  val topic: String,
  val listExpected: MutableState<List<DataQtyEntity>>,
  val listExpectedBarcodes: MutableState<List<String>>,
  val listExpectedEpcs: MutableState<List<String>>,
  val enableEncoding: MutableState<Boolean>,
  val boxMaxLimit: MutableState<Int>,
  val allowBarcodeScan: MutableState<Boolean>,
  val isArticleBased: MutableState<Boolean>,
  val labelArticle: MutableState<String>,
  val labelBarcode: MutableState<String>,
  val isQtyOnly: MutableState<Boolean>,
  val isDataUploaded: MutableState<Boolean>,
  val listNonEncodedTags: MutableState<List<TagInfoEntity>>,
  val isEncodingStarted: MutableState<Boolean>,
  val isInvOn: State<Boolean>,
  val selectedTab: MutableState<Int>,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InwardDetailContent(
  args: InwardDetailArgs,
  readerViewModel: ReaderViewModel,
  apiViewModel: ApiViewModel,
  inwardSharedViewModel: InwardSharedViewModel,
  navController: NavHostController,
  snackbarController: SnackbarController
) {
  //val isScreenLoaded =  remember { mutableStateOf(false) }
  val db = AppDatabase.getDbInstance(args.context)
  val tagInfoDao = db.tagInfoDao()
  val isProcessOn = readerViewModel.isProcessOn().observeAsState(false)
  val triggerPressed = readerViewModel.isTriggerPressed().observeAsState(false)
  val isApiLoading = apiViewModel.isLoading.observeAsState(false)

  //val listNonEncodedTags = (if(!args.enableEncoding.value) MutableStateFlow<List<TagInfoEntity>>(emptyList()) else tagInfoDao.getAllPreEncodedTags(args.topic,args.menuCode,args.transactionType)).collectAsState(initial = emptyList())
  val totalCount =
    tagInfoDao.getTotalCount(args.menuCode, args.transactionType).observeAsState(initial = 0)
  val validCount =
    tagInfoDao.getValidCount(args.menuCode, args.transactionType).observeAsState(initial = 0)
  val extraCount =
    tagInfoDao.getExtraCount(args.menuCode, args.transactionType, args.listExpectedBarcodes.value)
      .observeAsState(initial = 0)
  val invalidCount =
    tagInfoDao.getInvalidCount(args.menuCode, args.transactionType).observeAsState(initial = 0)
  val unEncodedCount =
    tagInfoDao.getUnencodedCount(args.menuCode, args.transactionType).observeAsState(initial = 0)
  val alienCount = tagInfoDao.getAlienCount(args.menuCode, args.transactionType).observeAsState(0)
  val iOConfigObject = db.inOutConfigDao().getConfigObject(
    args.topic, if (args.selLevel == null) "" else CommonUtils.chkNull(args.selLevel!!.flowId, "")
  ).collectAsState(null)
  val preIOConfigObject = remember { mutableStateOf<InOutConfigEntity?>(null) }
  LogUtils.showLog(
    "__iOConfigObject",
    "" + if (iOConfigObject.value == null) "--null" else iOConfigObject.value.toString()
  )
  val checkQtyList = db.dataQtyDao()
    .getCheckedQtyList(args.topic, args.menuCode, args.transactionType, args.isArticleBased.value)
    .collectAsState(emptyList())
  val rules =
    if (iOConfigObject.value != null) JSONObject(iOConfigObject.value?.rulesJObj) else JSONObject()
  LogUtils.showLog("__rules", "" + rules.toString())
  val expectedJSONArray = remember { mutableStateOf("[]") }

  val totalQty = if (args.selLevel == null) 0 else args.selLevel!!.total
  val decisionState by remember(
    validCount.value,
    invalidCount.value,
    extraCount.value,
    unEncodedCount.value,
    totalCount.value,
    checkQtyList.value,
    totalQty,
    rules
  ) {
    derivedStateOf {
      calculateScanDecisionState(
        args = args,
        checkQtyList = checkQtyList,
        totalCount = totalCount,
        validCount = validCount,
        invalidCount = invalidCount,
        extraCount = extraCount,
        unEncodedCount = unEncodedCount,
        alienCount = alienCount,
        totalQty = totalQty,
        rules = rules,
        apiViewModel = apiViewModel,
        inwardSharedViewModel = inwardSharedViewModel,
        expectedJSONArray = expectedJSONArray,
      )
    }
  }

  val tagList = if (args.isArticleBased.value) tagInfoDao.getArticleWiseList(
    args.menuCode, args.transactionType, args.listExpectedBarcodes.value
  ).observeAsState(initial = emptyList()) else tagInfoDao.getBarcodeWiseList(
    args.menuCode, args.transactionType, args.listExpectedBarcodes.value
  ).observeAsState(initial = emptyList())
  Box(modifier = Modifier.fillMaxSize()) {
    Column(
      modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
    ) {

      Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.Top) {
        TopBarContent(args.label, onBackClickL = {
          if (readerViewModel.isProcessOn().value == true || apiViewModel.isLoading.value == true) return@TopBarContent
          args.scope.launch {
            val deviceSessionId = DataStoreManager.readFromPreferences(
              args.preHeader + ParameterConstants.DEVICE_SESSION_ID, ""
            )
            DataStoreManager.saveToPreferences(
              args.preHeader + ParameterConstants.READER_POWER, 15
            )

            // if (chkTrue(isProcessOn.value)) return@launch
            if (deviceSessionId.isNullOrEmpty()) {
              inwardSharedViewModel.removeLastInwardLevel()
              clearSavedSessionValues(
                args.context, args.topic, args.menuCode, args.transactionType, args.preHeader
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
        })
      }
      HorizontalDivider(thickness = dimensionResource(R.dimen.dp_1), color = OutlineDefault)
      Column(
        modifier = Modifier
          .wrapContentHeight()
          .fillMaxSize()
          .padding(dimensionResource(id = R.dimen.dp_16))/*.clickable(
            onClick = {},
            indication = null,
            interactionSource = remember { MutableInteractionSource() }
        )*/, horizontalAlignment = Alignment.CenterHorizontally
      ) {

        HorizontalListView(
          items = inwardSharedViewModel.inwardLevels,
        )

        Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_16)))
        SegmentedTabView(
          selectedIndex = args.selectedTab.value,
          tabs = listOf("View Details", "Scanning"),
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
          onTabSelected = {
            if (!chkTrue(isProcessOn.value) && !chkTrue(isApiLoading.value)) {
              args.selectedTab.value = it
            }
          })
        //Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_24)))

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .weight(1f)
        ) {
          when (args.selectedTab.value) {
            0 -> ViewDetailsContent(
              args = args,
              readerViewModel = readerViewModel,
              tagList = tagList,
              totalCount = totalCount,
              totalQty = totalQty,
              decisionState = remember {
                mutableStateOf(
                  calculateScanDecisionState(
                    args = args,
                    checkQtyList = checkQtyList,
                    totalCount = totalCount,
                    validCount = validCount,
                    invalidCount = invalidCount,
                    extraCount = extraCount,
                    unEncodedCount = unEncodedCount,
                    alienCount = alienCount,
                    totalQty = totalQty,
                    rules = rules,
                    apiViewModel = apiViewModel,
                    inwardSharedViewModel = inwardSharedViewModel,
                    expectedJSONArray = expectedJSONArray
                  )
                )
              })

            1 -> ScanningContent(
              args = args,
              readerViewModel = readerViewModel,
              apiViewModel = apiViewModel,
              inwardSharedViewModel = inwardSharedViewModel,
              navController = navController,
              selectedTab = args.selectedTab,
              isInvOn = args.isInvOn,
              expectedJSONArray = expectedJSONArray
            )
          }
        }
      }
    }
    /**if(isScreenLoaded.value==false){
    isScreenLoaded.value = true
    selectedTab.value=1
    }*/
  }

  val msgNoData = stringResource(R.string.err_no_data)
  val alreadyCompleted = stringResource(R.string.completed_already)
  val snackbarController = remember { SnackbarController() }
  val response = apiViewModel.apiResult.collectAsState(initial = null)

  LaunchedEffect(iOConfigObject.value) {
    if (iOConfigObject.value != null && rules != null && rules.length() > 3 && preIOConfigObject.value == null && totalCount.value <= 0) {
      preIOConfigObject.value = iOConfigObject.value
      try {
        val keys = rules.keys()
        while (keys.hasNext()) {
          val key = keys.next()
          if (key.isNullOrEmpty()) continue
          if (key.equals("boxMaxLimit", true)) {
            args.boxMaxLimit.value =
              ParseUtils.extractInt(rules.getJSONObject(key), ParameterConstants.VALUE, 0)
          }
          if (key.equals("allowBarcodeScan", true)) {
            args.allowBarcodeScan.value =
              ParseUtils.extractBoolean(rules.getJSONObject(key), ParameterConstants.VALUE, false)
          }
          if (key.equals("enableEncoding", true)) {
            var enableEncoding =
              ParseUtils.extractBoolean(rules.getJSONObject(key), ParameterConstants.VALUE, false)
            if (!args.enableEncoding.value && enableEncoding && args.listExpectedBarcodes.value.size == 1) args.enableEncoding.value =
              enableEncoding
          }
        }
      } catch (e: Exception) {
        e.printStackTrace()
      }


      LogUtils.showLog("csds_config_allowBarcodeScan", args.allowBarcodeScan.toString())
      LogUtils.showLog("csds_config_enableEncoding", args.enableEncoding.toString())
      LogUtils.showLog("csds_config_boxLimit", args.boxMaxLimit.value.toString())
    }
  }

  LaunchedEffect(response.value) {
    if (response.value == null) return@LaunchedEffect
    val result = response.value
    if (result?.isSuccess != true) {
      args.isDataUploaded.value = false
      val message = result?.errMsg
      if (!message.isNullOrBlank()) {
        snackbarController.show(
          ErrorAppSnackBarData(result?.errMsg.toString())
        )
        if (result.url.equals(UrlConstants.GET_INWARD_BOX_DETAILS, true) || result.url.equals(
            UrlConstants.GET_OUTWARD_BOX_DETAILS, true
          )
        ) {
          if (true || navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
            inwardSharedViewModel.removeLastInwardLevel()
            clearSavedSessionValues(
              args.context, args.topic, args.menuCode, args.transactionType, args.preHeader
            )
            delay(1000)
            readerViewModel.clearSessionAndTransactionType()
            navController.popBackStack()
          }
        }
      }
    } else if (result.response != null) {
      val jsonResponse = result.response
      when (result.url) {
        UrlConstants.GET_INWARD_BOX_DETAILS, UrlConstants.GET_OUTWARD_BOX_DETAILS -> {/*"flowId": "2db83a8f-123f-449d-a022-5b4c00ddb53f",
          "level": 4,
          "nodeLevel": 1,
          "node4": "T-176",
          "node3": "PO-173",
          "node2": "ASN-1",
          "node1": "BOX-13",
          "qty": 5,
          "status": "PENDING",
          "details": [
          {
              "article": "",
              "barcode": "EAN-100",//can be comma separated
              "qty": 5
          }
          ]*/
          val response = extractJSONObject(jsonResponse, ParameterConstants.DATA, jsonResponse)
          val status = extractString(response, ParameterConstants.STATUS, "")
          val enableEncoding = ParseUtils.extractBoolean(response, "enableEncoding", false)
          if (status.equals(
              StatusConstants.COMPLETED, true
            ) || status.equals(StatusConstants.ACCEPTED, true)
          ) {
            snackbarController.show(
              SuccessAppSnackBarData(
                String.format(
                  alreadyCompleted,
                  chkNull(args.selLevel?.label, ""),
                  chkNull(args.selLevel?.node, "")
                )
              )
            )
            if (true || navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
              inwardSharedViewModel.removeLastInwardLevel()
              clearSavedSessionValues(
                args.context, args.topic, args.menuCode, args.transactionType, args.preHeader
              )
              delay(100)
              readerViewModel.clearSessionAndTransactionType()
              navController.popBackStack()
            }
            return@LaunchedEffect
          }

          var labelArticle = extractString(response, ParameterConstants.LABEL_ARTICLE, "");
          var labelBarcode = extractString(response, ParameterConstants.LABEL_BARCODE, "");
          val detailsArray = extractJSONArray(response, ParameterConstants.DETAILS, JSONArray())
          //give error if detailsArray is empty
          var hasArticle = false
          if (detailsArray == null || detailsArray.length() <= 0) {
            //handle only qty
            args.isQtyOnly.value = true/*snackbarController.show(
              ErrorAppSnackBarData(msgNoData)
            )
            inwardSharedViewModel.removeLastInwardLevel()
            clearSavedSessionValues(
              args.context,
              args.topic,
              args.menuCode,
              args.transactionType,
              args.preHeader
            )
            readerViewModel.clearSessionAndTransactionType()
            navController.popBackStack()*/
          } else {
            expectedJSONArray.value = detailsArray.toString()
          }
          CoroutineScope(Dispatchers.IO).launch {
            val listExpData = ArrayList<DataQtyEntity>(0)
            val listExpBarcodes = ArrayList<String>(0)
            val listExpEpcs = ArrayList<String>(0)
            for (i in 0 until detailsArray.length()) {
              val jObj = detailsArray.getJSONObject(i)
              val detail = Gson().fromJson(jObj.toString(), DataQtyEntity::class.java)
              if (detail != null) {
                if (labelArticle.isNullOrEmpty()) labelArticle =
                  extractString(jObj, ParameterConstants.LABEL_ARTICLE, "")
                if (labelBarcode.isNullOrEmpty()) labelBarcode =
                  extractString(jObj, ParameterConstants.LABEL_BARCODE, "")
                detail.topic = args.topic
                detail.sessionType = args.menuCode
                detail.transactionType = args.transactionType
                if (detail.barcode.contains(",")) {
                  for (barcode in detail.barcode.split(",")) {
                    val dataQty = detail.copy()
                    dataQty.barcode = barcode.trim()
                    if (dataQty.barcode.isNotEmpty()) listExpBarcodes.add(dataQty.barcode)
                    if (dataQty.epc.contains(",")) {
                      for (epc in dataQty.epc.split(",")) {
                        val dataEpc = dataQty.copy()
                        dataEpc.epc = epc.trim()
                        if (dataEpc.epc.isNotEmpty()) listExpEpcs.add(dataEpc.epc)
                        listExpData.add(dataEpc)
                      }
                    } else {
                      if (dataQty.epc.isNotEmpty()) listExpEpcs.add(dataQty.epc)
                      listExpData.add(dataQty)
                    }
                  }
                } else {
                  if (detail.barcode.isNotEmpty()) listExpBarcodes.add(detail.barcode)
                  if (detail.epc.isNotEmpty()) listExpEpcs.add(detail.epc)
                  listExpData.add(detail)
                }
              }
            }
            args.isArticleBased.value =
              listExpData.size > 0 && listExpData.get(0).article.isNotEmpty()// && false//temp condition
            LogUtils.showLog("isArticleBased", "" + args.isArticleBased.value)
            if (labelArticle.isNotEmpty()) args.labelArticle.value = labelArticle
            LogUtils.showLog("labelArticle", "" + args.labelArticle.value)
            if (labelBarcode.isNotEmpty()) args.labelBarcode.value = labelBarcode
            LogUtils.showLog("labelBarcode", "" + args.labelBarcode.value)
            if (!args.enableEncoding.value && enableEncoding && listExpBarcodes.size == 1) {
              args.enableEncoding.value = enableEncoding
              LogUtils.showLog("enableEncoding", "" + args.enableEncoding.value)
            }
            db.dataQtyDao().deleteAll()
            db.dataQtyDao().insertAll(listExpData);
            args.listExpected.value = listExpData;
            args.listExpectedBarcodes.value = listExpBarcodes;
            args.listExpectedEpcs.value = listExpEpcs;
          }
        }

        UrlConstants.UPLOAD_INWARD_SCANNED, UrlConstants.UPLOAD_OUTWARD_SCANNED -> {
          val message = extractString(
            jsonResponse,
            ParameterConstants.MESSAGE,
            extractString(jsonResponse, ParameterConstants.MSG, "")
          )
          if (message.isNotEmpty()) {
            snackbarController.show(SuccessAppSnackBarData(message))
            delay(1000)
            if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
              clearSavedSessionValues(
                args.context, args.topic, args.menuCode, args.transactionType, args.preHeader
              )
              inwardSharedViewModel.removeLastInwardLevel()
              readerViewModel.clearSessionAndTransactionType()
              navController.popBackStack()
            }
          }
        }

        UrlConstants.ENCODING -> handleEncode(jsonResponse, readerViewModel, args)
      }
    }
  }

  LaunchedEffect(triggerPressed.value) {
    if(!triggerPressed.value) return@LaunchedEffect
    readerViewModel.setTriggerValue(false)
    if (chkTrue(isApiLoading.value)) return@LaunchedEffect
    if (args.showSheet.value != false && args.currentSheet.value != BottomSheetType.NONE) return@LaunchedEffect
    if (args.isDataUploaded.value == true) return@LaunchedEffect
    //if(selectedTab.value!=1) return@LaunchedEffect
    val deviceSessionId = DataStoreManager.readFromPreferences(
      args.preHeader + ParameterConstants.DEVICE_SESSION_ID, ""
    )
    if (deviceSessionId.isNullOrEmpty()) {
      val deviceSessionId =
        SessionUtils.generateOfflineSessionId(args.menuCode, args.transactionType)
      LogUtils.showLog("DeviceSessionId", deviceSessionId)
      DataStoreManager.saveToPreferences(
        args.preHeader + ParameterConstants.DEVICE_SESSION_ID, deviceSessionId
      )
    }
    if (args.boxMaxLimit.value > 0 && totalCount.value >= args.boxMaxLimit.value) {
      //if(isInvOn.value) readerViewModel.stopOperations()
      snackbarController.show(
        ErrorAppSnackBarData("Box Limit Reached.Can't Proceed with scanning")
      )
    } else {
      if (chkTrue(args.isInvOn.value)) ToastUtils.showShortToast("scanning stopped")
      readerViewModel.toggleInventory(
        invPower = args.setPower.value,
        isPublishToMqtt = false,
        updateFound = true,
        maxScanLimit = args.boxMaxLimit.value
      )
    }
  }

  LaunchedEffect(args.isInvOn.value) {
    if (args.isInvOn.value == null) return@LaunchedEffect
    args.selectedTab.value = if (args.isInvOn.value == true) 1 else 0
  }

  LaunchedEffect(totalCount.value) {
    if (args.boxMaxLimit.value > 0 && totalCount.value >= args.boxMaxLimit.value) {
      if (args.isInvOn.value) readerViewModel.stopOperations()
      //TODO discuss with BA & Delete extra records if required
      snackbarController.show(
        ErrorAppSnackBarData("Box Limit Reached.Can't Proceed with scanning")
      )
    }
  }

  if (args.showSheet.value) {
    ModalBottomSheet(
      sheetState = args.sheetState, onDismissRequest = {

      }, dragHandle = { }, modifier = Modifier
        .fillMaxWidth()
        .background(Color.Transparent)
    ) {
      when (args.currentSheet.value) {
        BottomSheetType.SETTINGS -> Box(
          modifier = Modifier
            .background(WhiteColor)
            .fillMaxWidth()
            .fillMaxHeight(0.35f)
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
            .fillMaxHeight(0.50f)
        ) {
          SetInvDevicePower(
            showSheet = args.showSheet,
            sheetState = args.sheetState,
            scope = args.scope,
            currentPower = args.setPower.value,
            onPowerSet = {
              args.setPower.value = it
              readerViewModel.setPower(args.setPower.value)
              DataStoreManager.saveToPreferences(
                args.preHeader + ParameterConstants.READER_POWER, args.setPower.value
              )
            },
          )
        }

        BottomSheetType.SESSION -> Box(
          modifier = Modifier
            .background(WhiteColor)
            .fillMaxWidth()
            .fillMaxHeight(0.35f)
        ) {
          InwardSessionContent(
            showSheet = args.showSheet,
            sheetState = args.sheetState,
            scope = args.scope,
            navController = navController,
            tagCount = validCount.value,
            onStopSession = {
              if (chkTrue(isProcessOn.value)) return@InwardSessionContent
              args.scope.launch {
                clearSavedSessionValues(
                  args.context, args.topic, args.menuCode, args.transactionType, args.preHeader
                )
                inwardSharedViewModel.removeLastInwardLevel()
                if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                  readerViewModel.clearSessionAndTransactionType()
                  navController.popBackStack()
                }
              }
            },
            readerViewModel
          )
        }

        BottomSheetType.MESSAGE -> Box(
          modifier = Modifier
            .background(WhiteColor)
            .fillMaxWidth()
            .fillMaxHeight(0.35f)
        ) {
          MessageComponent(
            icon = R.drawable.success_white,
            backGroundColor = Brush.verticalGradient(
              colors = listOf(
                Color(0xFF048204), Color(0xFF02AC02)
              )
            ),
            topHeading = "",
            heading = stringResource(R.string.ean_quantity_matched),
            description = stringResource(
              R.string.total_items_scanned, validCount.value + invalidCount.value
            ),
            buttonText = stringResource(R.string.ok),
            onClick = {
              callAPI(
                apiViewModel,
                inwardSharedViewModel,
                args,
                false,
                decisionState.message,
                expectedJSONArray
              )
            },
            onClose = {
              args.scope.launch {
                args.sheetState.hide()
                args.showSheet.value = false
              }
            },
            tintColor = WhiteColor
          )
        }

        BottomSheetType.REJECT -> Box(
          modifier = Modifier
            .background(WhiteColor)
            .fillMaxWidth()
            .fillMaxHeight(0.45f)
        ) {

          val formatedText = buildAnnotatedString {
            append("Expected: ${totalQty}" + "\t|\t" + "Scanned: ${totalCount.value}")
          }
          val headingText =
            decisionState.message?.substringAfter(",", "")?.substringAfter(",", "")?.trim()
              .takeUnless { it.isNullOrEmpty() } ?: decisionState.message
          RejectDataSheet(
            heading = headingText,
            subHeading = formatedText,
            showSheet = args.showSheet,
            sheetState = args.sheetState,
            scope = args.scope,
            onClick = {
              callAPI(
                apiViewModel,
                inwardSharedViewModel,
                args,
                true,
                decisionState.message,
                expectedJSONArray
              )
            })
        }

        BottomSheetType.ACCEPT -> Box(
          modifier = Modifier
            .background(WhiteColor)
            .fillMaxWidth()
            .fillMaxHeight(0.45f)
        ) {
          val headingText =
            decisionState.message?.substringAfter(",", "")?.substringAfter(",", "")?.trim()
              .takeUnless { it.isNullOrEmpty() } ?: decisionState.message

          val formatedText = buildAnnotatedString {
            append(
              if (decisionState.isSuccess) "Expected: ${totalQty}" + "\t|\t" + "Scanned: ${totalCount.value}" else stringResource(
                R.string.inward_exception_msg
              )
            )
          }
          AcceptDataSheet(
            heading = headingText,
            subHeading = formatedText,
            showSheet = args.showSheet,
            sheetState = args.sheetState,
            scope = args.scope,
            icon = if (decisionState.isSuccess) R.drawable.success_white else R.drawable.property_decode_error,
            color1 = if (decisionState.isSuccess) Green else Color(0xFFD50000),
            color2 = if (decisionState.isSuccess) Green else Color(0xFFFF3131),
            onClick = {
              callAPI(
                apiViewModel,
                inwardSharedViewModel,
                args,
                false,
                decisionState.message,
                expectedJSONArray
              )
            })
        }

        BottomSheetType.RESCAN -> Box(
          modifier = Modifier
            .background(WhiteColor)
            .fillMaxWidth()
            .fillMaxHeight(0.47f)
        ) {
          val formatedText = buildAnnotatedString {
            append(stringResource(R.string.txt_confirm_rescan))
          }
          AcceptDataSheet(
            title = stringResource(R.string.txt_rescan),
            heading = stringResource(R.string.txt_rescan_items),
            subHeading = formatedText,
            primaryButtonText = stringResource(R.string.txt_confirm_and_rescan),
            showSheet = args.showSheet,
            sheetState = args.sheetState,
            scope = args.scope,
            icon = R.drawable.property_know_more,
            color1 = Color(0xFFF3B100),
            color2 = Color(0xFFFECF53),
            onClick = {
              clearDB(args)
              args.selectedTab.value = 1
            })
        }

        else -> {

        }
      }
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp), contentAlignment = Alignment.BottomCenter
  ) {
    AppSnackBar(snackbarController)
  }
}

fun handleEncode(
  response: JSONObject,
  readerViewModel: ReaderViewModel,
  args: InwardDetailArgs
) {
  LogUtils.showLog("ENCODING_API_END", "end")
  val rootJson = response
  val id = extractString(rootJson, ParameterConstants.ID, "")
  val deviceSessionId = extractString(rootJson, ParameterConstants.DEVICE_SESSION_ID, "")
  val sessionData = JSONObject()
  sessionData.put(ParameterConstants.DEVICE_SESSION_ID,deviceSessionId)
  sessionData.put(ParameterConstants.ID,id)
  readerViewModel.setSessionId(deviceSessionId,sessionData.toString())
  DataStoreManager.saveToPreferences(args.preHeader + ParameterConstants.ID, id)
  DataStoreManager.saveToPreferences(args.preHeader + ParameterConstants.DEVICE_SESSION_ID, deviceSessionId)

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
    val listNonEncodedTags = args.listNonEncodedTags.value
    for (i in 0 until detailsArray.length()) {
      val item = detailsArray.getJSONObject(i)

      val id = extractString(item, ParameterConstants.ID, "")
      val encodeLogId = extractString(item, ParameterConstants.ENCODE_LOG_ID, "")
      val customerId = extractString(item, ParameterConstants.CUSTOMER_ID, "")
      val oldEpc = extractString(item, ParameterConstants.OLD_EPC, "")
      val newEpc = extractString(item, ParameterConstants.NEW_EPC, "")
      val tid = extractString(item, ParameterConstants.TID, "")
      val barcode = extractString(rootJson, ParameterConstants.BARCODE, "")

      if(newEpc.isNullOrEmpty()){
        continue
      }

      val tagInfoData = listNonEncodedTags.filter { tagInfoEntity ->  tagInfoEntity.tid.equals(tid,true) }.first()
      tagInfoData.id = id
      tagInfoData.encodeLogId = encodeLogId
      tagInfoData.customerId = customerId
      tagInfoData.epc = oldEpc
      tagInfoData.newEpc = newEpc
      tagInfoData.tid = tid
      tagInfoData.barcode = barcode
      tagInfoData.isUploaded=false
    }
    LogUtils.showLog("ENCODING_ENCODE", "Start Encoding EPCs: ${listNonEncodedTags.map { t -> t.newEpc }.toString()}")
    readerViewModel.performEncoding(listNonEncodedTags, currentPassword, oldPasswords)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanningContent(
  args: InwardDetailArgs,
  apiViewModel: ApiViewModel,
  readerViewModel: ReaderViewModel,
  inwardSharedViewModel: InwardSharedViewModel,
  navController: NavHostController,
  selectedTab: MutableState<Int>,
  expectedJSONArray: MutableState<String>,
  isInvOn: State<Boolean>
) {

  val tagInfoDao = AppDatabase.getDbInstance(args.context).tagInfoDao()
  val totalCount = tagInfoDao.getTotalCount(args.menuCode, args.transactionType).observeAsState(initial = 0)
  val validCount = tagInfoDao.getValidCount(args.menuCode, args.transactionType).observeAsState(initial = 0)
  val invalidCount = tagInfoDao.getInvalidCount(args.menuCode, args.transactionType).observeAsState(initial = 0)
  val unEncodedCount = tagInfoDao.getUnencodedCount(args.menuCode, args.transactionType).observeAsState(0)
  val alienCount = if(DataStoreManager.readFromPreferences("enableAlienTags",false) && DataStoreManager.readFromPreferences("enableVendorSerialCode",false) && DataStoreManager.readFromPreferences("vendorSerialCode","").isNotEmpty()) tagInfoDao.getAlienCount(args.menuCode, args.transactionType,DataStoreManager.readFromPreferences("vendorSerialCode","")).observeAsState(0) else remember { mutableIntStateOf(0) }
  val snackbarController = remember { SnackbarController() }
  val error = readerViewModel.error().observeAsState()
  val isApiLoading = apiViewModel.isLoading.observeAsState(false)


  
  LaunchedEffect(Unit) {
    LogUtils.showLog("COUNT", "Valid Count: ${validCount.value}")
    LogUtils.showLog("COUNT", "Invalid Count: ${invalidCount.value}")
    LogUtils.showLog("COUNT", "Unencoded Count: ${unEncodedCount.value}")
  }

  LaunchedEffect(error.value) {
    val message = error.value
    if (!message.isNullOrBlank()) {
      snackbarController.show(
        ErrorAppSnackBarData(message)
      )
    }
  }
  Box() {
    Column(
      modifier = Modifier
        .wrapContentHeight()
        .fillMaxWidth(),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {

      Row(
        modifier = Modifier
          .padding(top = dimensionResource(R.dimen.dp_16))
          .wrapContentHeight()
          .heightIn(max = dimensionResource(R.dimen.dp_36))
          .background(
            color = Color(0xFFF2F2F2), shape = RoundedCornerShape(50)
          ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
      ) {
        if(DataStoreManager.readFromPreferences("enableAlienTags",false) && DataStoreManager.readFromPreferences("enableVendorSerialCode",false) && DataStoreManager.readFromPreferences("vendorSerialCode","").isNotEmpty()) {
          Text(
            stringResource(
              R.string.alien_tags,
              alienCount.value.toString()//invalidCount.value - unEncodedCount.value).toString()
            ),
            style = CommonTypography.current.noteText,
            color = TextSubtext,
            textAlign = TextAlign.Left,
            modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.dp_10)),
          )

          VerticalDivider(
            color = ShadowColor,
            thickness = dimensionResource(R.dimen.dp_1),
            modifier = Modifier.padding(
              vertical = dimensionResource(R.dimen.dp_10),
              horizontal = dimensionResource(R.dimen.dp_8)
            )
          )}

        Text(
          stringResource(R.string.unencoded_tags, unEncodedCount.value.toString()),
          style = CommonTypography.current.noteText,
          color = RedColor,
          textAlign = TextAlign.Right,
          modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.dp_10))
        )

      }

      Column(
        modifier = Modifier
          .fillMaxWidth()
          .aspectRatio(1f),
        verticalArrangement = Arrangement.Center) {
        InventoryPulseCircle(
          totalCount = totalCount.value,
          tagCount = validCount.value.toString(),
          isInvOn = isInvOn.value,
          lastTagCount = args.totalExpQty.toString(),
          showUploadSwipe = false,
          onClick = {
            selectedTab.value=0
          },
        )
      }
    }
  }

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewDetailsContent(
  args: InwardDetailArgs,
  tagList: State<List<EanFoundQty>>,
  totalCount: State<Int>,
  totalQty: Int = 0,
  decisionState: MutableState<ScanDecisionState>,
  readerViewModel: ReaderViewModel,
) {

  Column(
    modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
  ) {

    val resultColor = when {
      totalCount.value == 0 -> TextSubtext
      totalCount.value == totalQty -> Color(0xFF2E7D32)
      else -> RedColor
    }
    val diff = Math.abs(totalQty - totalCount.value)

    Row(
      modifier = Modifier
        .padding(top = dimensionResource(R.dimen.dp_16))
        .wrapContentWidth()
        .height(dimensionResource(R.dimen.dp_36))
        .background(
          color = Color(0xFFF2F2F2), shape = RoundedCornerShape(50)
        ),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center
    ) {

      Text(
        stringResource(R.string.total_exp, totalQty.toString()),
        style = CommonTypography.current.noteText,
        color = resultColor,
        textAlign = TextAlign.Right,
        modifier = Modifier.padding(start = dimensionResource(R.dimen.dp_10))
      )

      VerticalDivider(
        color = ShadowColor,
        thickness = dimensionResource(R.dimen.dp_1),
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
      )

      Text(
        stringResource(R.string.total_scan, totalCount.value.toString()),
        style = CommonTypography.current.noteText,
        color = resultColor,
        textAlign = TextAlign.Left,
        modifier = Modifier
      )

      VerticalDivider(
        color = ShadowColor,
        thickness = dimensionResource(R.dimen.dp_1),
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
      )

      Text(
        stringResource(R.string.total_diff, diff.toString()),
        style = CommonTypography.current.noteText,
        color = resultColor,
        textAlign = TextAlign.Left,
        modifier = Modifier.padding(end = dimensionResource(R.dimen.dp_10))
      )

    }


    if (decisionState.value.message.isNotEmpty()) {
      Row(
        modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = decisionState.value.message,
          style = CommonTypography.current.noteText,
          color = if (decisionState.value.isSuccess) Green else RedColor,
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          textAlign = TextAlign.Center
        )
      }
      Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_8)))
    }


    /*LazyColumn(
      modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
    ) {
      itemsIndexed(
        items = tagList.value,
        key = { index, tag ->
          "${tag.barcode ?: ""}_$index"
        }
      ) { _, tag ->
        TagRowItem(tag)
      }
    }*/

    Spacer(modifier = Modifier.height(8.dp))

    TagHeaderRow(args)

    Spacer(modifier = Modifier.height(4.dp))

    LazyColumn(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
    ) {
      itemsIndexed(
        items = tagList.value, key = { index, tag ->
          "${tag.barcode ?: ""}_$index"
        }) { _, tag ->
        TagRowItem(tag)
      }
    }

    HorizontalDivider(thickness = dimensionResource(R.dimen.dp_1), color = OutlineDefault)

    Box(
      modifier = Modifier
        .fillMaxWidth()
        .windowInsetsPadding(WindowInsets(0))
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {

        //if (decisionState.value.showReject) {
        CommonButton(
          text = stringResource(R.string.txt_reject),
          onClick = {
            //TODO openSheet before API call
            // callAPI(apiViewModel,inwardSharedViewModel,args, true,decisionState.value.message,expectedJSONArray)
            //handleReject(args)
            openSheet(
              args.scope, args.sheetState, args.showSheet, args.currentSheet, BottomSheetType.REJECT
            )
          },
          contentColor = WhiteColor,
          modifier = Modifier.weight(1f),
          enabled = decisionState.value.showReject
        )
        //}

        //if (decisionState.value.showReject && decisionState.value.showAccept) {
        Spacer(modifier = Modifier.width(12.dp))
        //}

        //if (decisionState.value.showAccept) {
        CommonButton(
          text = stringResource(R.string.txt_accept),
          onClick = {
            //callAPI(apiViewModel,inwardSharedViewModel,args,false,decisionState.value.message,expectedJSONArray)
            //TODO open Success/Confirm Sheet based on decisionState
            //(i.e. accept with Exception)
            openSheet(
              args.scope, args.sheetState, args.showSheet, args.currentSheet, BottomSheetType.ACCEPT
            )
          },
          contentColor = WhiteColor,
          modifier = Modifier.weight(1f),
          enabled = decisionState.value.showAccept
        )
        //}

        if (decisionState.value.showRescan) {
          Spacer(modifier = Modifier.width(12.dp))

          Box(
            modifier = Modifier
              .size(48.dp)
              .clip(RoundedCornerShape(50))
              .border(1.dp, OutlineDefault, RoundedCornerShape(50))
              .clickable {
                openSheet(
                  scope = args.scope,
                  sheetState = args.sheetState,
                  showSheet = args.showSheet,
                  currentSheet = args.currentSheet,
                  sheet = BottomSheetType.RESCAN
                )
              },//handleRescan(args) },
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.Default.Refresh, contentDescription = "Rescan", tint = BlackColor
            )
          }
        }
      }

    }

  }
}

/*fun handleRescan(
  args: InwardDetailArgs
) {
  val db = AppDatabase.getDbInstance(args.context)
  CoroutineScope(Dispatchers.IO).launch {
    db.tagInfoDao().delete(args.menuCode, args.transactionType)
  }

  //Clear session data
  val preHeader = args.preHeader
  DataStoreManager.saveToPreferences(preHeader + ParameterConstants.ID, "")
  DataStoreManager.saveToPreferences(preHeader + ParameterConstants.DEVICE_SESSION_ID, "")

  LogUtils.showLog("RESCAN", "All scanning data cleared")
}

fun handleReject(
  args: InwardDetailArgs
) {
  val db = AppDatabase.getDbInstance(args.context)
  CoroutineScope(Dispatchers.IO).launch {
    db.tagInfoDao().delete(args.menuCode, args.transactionType)
  }

  //Mark session as rejected (local flag / API trigger point)
  val preHeader = args.preHeader
  DataStoreManager.saveToPreferences(preHeader + "SESSION_STATUS", "REJECTED")

  LogUtils.showLog("REJECT", "Session rejected and data cleared")

  // TODO: Call Reject API here if required
  // apiViewModel.rejectInward()

}

fun handleAccept(
  args: InwardDetailArgs
) {
  val db = AppDatabase.getDbInstance(args.context)
  CoroutineScope(Dispatchers.IO).launch {
    //Get valid tag count
    val validCount = db.tagInfoDao().getValidCount(args.menuCode, args.transactionType)

    if (validCount.value <= 0) {
      LogUtils.showLog("ACCEPT", "No valid tags, cannot accept")
      return@launch
    }
    // TODO: Call Accept API with tag list
    // val tagList = db.tagInfoDao().getAllTagsSync(menuCode, transactionType)
    // apiViewModel.submitInward(tagList)

    //Mark session completed
    val preHeader = args.preHeader
    DataStoreManager.saveToPreferences(preHeader + "SESSION_STATUS", "COMPLETED")

    LogUtils.showLog("ACCEPT", "Session accepted with $validCount tags")

    //Clear local data after success
    db.tagInfoDao().delete(args.menuCode, args.transactionType)
  }
}*/

fun callEpcForEncoding(
  barCodeNo: String,
  apiViewModel: ApiViewModel,
  args: InwardDetailArgs
) {
  if (!isInternetConnected(args.context, isShowErrDialog = false, isShowErrToast = true)) return
  val existingSessionId = DataStoreManager.readFromPreferences(args.preHeader + ParameterConstants.DEVICE_SESSION_ID, "")
  val now = DateFormatUtils.getCurrentUTCTime()
  val deviceSessionId = SessionUtils.generateOfflineSessionId(args.menuCode, args.transactionType)
  val sessionIdToSend = if (existingSessionId.isNullOrEmpty()) deviceSessionId else existingSessionId
  val listNonEncodedTags = args.listNonEncodedTags.value
  val jsonRequest = JSONObject().apply {
    put(ParameterConstants.USER_ID, DataStoreManager.readFromPreferences(ParameterConstants.USER_ID, ""))
    put(ParameterConstants.DEVICE_ID, DataStoreManager.readFromPreferences(ParameterConstants.DEVICE_ID, ""))
    put(ParameterConstants.CUSTOMER_ID, DataStoreManager.readFromPreferences(ParameterConstants.CUSTOMER_ID, ""))
    put(ParameterConstants.OPERATION_LOCATION_ID, DataStoreManager.readFromPreferences(LoginConstants.DEVICE_LOCATION_ID, ""))
    put(ParameterConstants.DEVICE_SESSION_ID, sessionIdToSend)
    put(ParameterConstants.BARCODE, barCodeNo)
    put(ParameterConstants.QTY, listNonEncodedTags.size)
    put(ParameterConstants.START_DATE, now)
    put(ParameterConstants.END_DATE, now)
    put(ParameterConstants.COMPLETION_STATUS, "PENDING")

    // DETAILS ARRAY
    val detailsArray = JSONArray()
    for(tagInfoData in listNonEncodedTags) {
      val detail = JSONObject().apply {
        put(ParameterConstants.OLD_EPC, if (tagInfoData != null) tagInfoData?.epc else "")
        put(ParameterConstants.TID, if (tagInfoData != null) tagInfoData?.tid else "")
        put(ParameterConstants.COMPLETION_STATUS, "PENDING")
        put(ParameterConstants.COMPLETION_REMARK, "")
      }
      detailsArray.put(detail)
    }
    put(ParameterConstants.DETAILS, detailsArray)
  }

  // Step 5: Call API
  apiViewModel.callApi(url = UrlConstants.ENCODING, jsonRequest = jsonRequest)
  LogUtils.showLog("ENCODING_API_START", "Start")
}

fun callAPI(
  apiViewModel: ApiViewModel,
  inwardSharedViewModel: InwardSharedViewModel,
  args: InwardDetailArgs,
  isRejected: Boolean = false,
  remark: String,
  expectedJSONArray: MutableState<String>
) {
  CoroutineScope(Dispatchers.IO).launch {
    try {
      val url = if (args.isInward) UrlConstants.UPLOAD_INWARD_SCANNED else UrlConstants.UPLOAD_OUTWARD_SCANNED
      val jsonRequest = JSONObject()
      jsonRequest.put(ParameterConstants.CUSTOMER_ID, DataStoreManager.readFromPreferences(ParameterConstants.CUSTOMER_ID, ""))
      jsonRequest.put(ParameterConstants.BUSINESS_LINE_ID, DataStoreManager.readFromPreferences(ParameterConstants.BUSINESS_LINE_ID, ""))
      jsonRequest.put(ParameterConstants.LOCATION_ID, DataStoreManager.readFromPreferences(LoginConstants.DEVICE_LOCATION_ID, ""))
      jsonRequest.put(ParameterConstants.DEVICE_ID, DataStoreManager.readFromPreferences(ParameterConstants.DEVICE_ID, ""))
      jsonRequest.put(ParameterConstants.CREATED_BY, DataStoreManager.readFromPreferences(ParameterConstants.USER_ID, ""))
      jsonRequest.put(ParameterConstants.STATUS, StatusConstants.COMPLETED)
      jsonRequest.put(ParameterConstants.FLOW_ID, if(args.selLevel==null) "" else args.selLevel!!.flowId)
      jsonRequest.put(ParameterConstants.LEVEL, args.selLevel?.level)
      jsonRequest.put(ParameterConstants.NODE_LEVEL, args.selLevel?.nodeLevel)
      for (i in 0 until inwardSharedViewModel.inwardLevels.size) {
        val level = inwardSharedViewModel.inwardLevels[i]
        jsonRequest.put(ParameterConstants.NODE + (level.level - i), level.node)
      }
      jsonRequest.put(ParameterConstants.REMARK, remark)
      jsonRequest.put(ParameterConstants.STATUS, if (isRejected) StatusConstants.REJECTED else StatusConstants.ACCEPTED)
      //TODO get,save from server API & return it here
      jsonRequest.put(ParameterConstants.EXPECTED_QTY, chkNull(args.totalExpQty,0))
      LogUtils.showLog("expectedJSONArray",expectedJSONArray.value.toString())
      jsonRequest.put(ParameterConstants.EXPECTED, JSONArray(expectedJSONArray.value))
      val dataQtyDao = AppDatabase.getDbInstance(args.context).dataQtyDao()
      val tagInfoDao = AppDatabase.getDbInstance(args.context).tagInfoDao()
      val listBarcodeQty = tagInfoDao.getBarcodeQty(if (args.isInward) TopicConstants.INWARD else TopicConstants.OUTWARD, args.menuCode, args.transactionType)

      val jsonArrayActual = JSONArray()
      listBarcodeQty.forEach { eanQty ->
        val article = if(!args.isArticleBased.value) "" else chkNull(dataQtyDao.getArticleFromBarcode(if (args.isInward) TopicConstants.INWARD else TopicConstants.OUTWARD, args.menuCode, args.transactionType,eanQty.barcode),"")
        val jsonObjActual = JSONObject()
        jsonObjActual.put(ParameterConstants.ARTICLE,article)//getArticle(eanQty.barcode))
        jsonObjActual.put(ParameterConstants.BARCODE,eanQty.barcode)
        jsonObjActual.put(ParameterConstants.QTY,eanQty.qty)
        jsonObjActual.put(ParameterConstants.REMARK,"")
        val tagInfoEntities = tagInfoDao.getAllAgainstBarcode(if (args.isInward) TopicConstants.INWARD else TopicConstants.OUTWARD, args.menuCode, args.transactionType,eanQty.barcode)
        val jsonArrayRFID = JSONArray()
        for(tag in tagInfoEntities){
          val item = JSONObject()
          item.put(ParameterConstants.EPC, tag.epc)
          item.put(ParameterConstants.TID, tag.tid)
          item.put(ParameterConstants.BARCODE, tag.barcode)
          val jsonArrayRSSI = JSONArray()
          val jsonRssiItem = JSONObject()
          jsonRssiItem.put(ParameterConstants.ANTENNA,1)
          jsonRssiItem.put(ParameterConstants.RSSI,if(tag.rssi.isNotEmpty() && tag.rssi.matches(Regex("[0-9]+"))) Integer.parseInt(tag.rssi) else 0)
          jsonRssiItem.put(ParameterConstants.PHASE,if(tag.phase.isNotEmpty() && tag.phase.matches(Regex("[0-9]+"))) Integer.parseInt(tag.phase) else 0)
          jsonArrayRSSI.put(jsonRssiItem)
          item.put(ParameterConstants.RSSI,jsonArrayRSSI)
          jsonArrayRFID.put(item)
        }
        jsonObjActual.put(ParameterConstants.RFID,jsonArrayRFID)
        jsonArrayActual.put(jsonObjActual)
      }
      jsonRequest.put(ParameterConstants.ACTUAL_QTY, jsonArrayActual.length())
      jsonRequest.put(ParameterConstants.ACTUAL, jsonArrayActual)
      LogUtils.showLog("API_R",jsonRequest.toString())
      if(NetworkMonitor.isNetworkConnected) args.isDataUploaded.value=true
      apiViewModel.callApi(url, jsonRequest = jsonRequest)
    } catch (e: Exception) {
      e.printStackTrace(); }
  }
}

@Composable
fun TagHeaderRow(args: InwardDetailArgs) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(Color(0xFFF4F5F7))
      .padding(horizontal = 16.dp, vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceAround
  ) {
    HeaderText(if(args.isArticleBased.value) args.labelArticle.value else args.labelBarcode.value, Modifier.weight(2.2f), TextAlign.Start)
    HeaderText("Exp Qty.", Modifier.weight(1f), TextAlign.Center)
    HeaderText("Scan Qty.", Modifier.weight(1f), TextAlign.Center)
    HeaderText("Diff.", Modifier.weight(1f), TextAlign.Center)
  }
}

@Composable
private fun HeaderText(
  text: String,
  modifier: Modifier,
  align: TextAlign
) {
  Text(
    text = text,
    modifier = modifier,
    textAlign = align,
    style = CommonTypography.current.noteText,
    color = Color.Gray
  )
}


@Composable
fun TagRowItem(tag: EanFoundQty) {

  val expQty = tag.totalQty?: 0
  val scanQty = tag.foundQty ?: 0
  val diff = Math.abs(expQty - scanQty)

  Column {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {

      Text(
        text = tag.barcode ?: "",
        modifier = Modifier.weight(2.2f),
        style = CommonTypography.current.noteText,
        color = BlackColor
      )

      ValueText(expQty, Modifier.weight(1f))
      ValueText(scanQty, Modifier.weight(1f))
      ValueText(diff, Modifier.weight(1f))
    }

    HorizontalDivider(
      modifier = Modifier.padding(horizontal = 16.dp),
      thickness = 0.6.dp,
      color = OutlineDefault
    )
  }
}

@Composable
private fun ValueText(value: Int, modifier: Modifier) {
  Text(
    text = value.toString(),
    modifier = modifier,
    textAlign = TextAlign.Center,
    style = CommonTypography.current.noteText,
    color = BlackColor
  )
}

@Composable
fun HorizontalListView(
  items: SnapshotStateList<IOLevel>,
  modifier: Modifier = Modifier,
  /* onItemClick: ((index: Int) -> Unit)? = null*/
) {
  var selectedIndex by remember { mutableStateOf(items.lastIndex) }

  val screenWidth = LocalConfiguration.current.screenWidthDp.dp
  val itemWidth = 120.dp
  val spacing = dimensionResource(R.dimen.dp_4)
  val listState = rememberLazyListState()

  val horizontalPadding =
    if (items.size <= 1) (screenWidth / 2) - (itemWidth / 2)
    else dimensionResource(R.dimen.dp_4)

  LaunchedEffect(items.size) {
    if (items.isNotEmpty()) {
      selectedIndex = items.lastIndex
      listState.animateScrollToItem(items.lastIndex)
    }
  }

  LazyRow(
    state = listState,
    modifier = modifier.fillMaxWidth(),
    contentPadding = PaddingValues(horizontal = horizontalPadding),
    verticalAlignment = Alignment.CenterVertically
  ) {
    itemsIndexed(items) { index, item ->

      HorizontalListItem(
        title = item.label,
        value = item.node,
        isSelected = index == selectedIndex,
       /* onClick = {
          selectedIndex = index
          onItemClick?.invoke(index)
        }*/
      )

      if (index < items.lastIndex) {
        Spacer(Modifier.width(spacing))
        Icon(
          imageVector = Icons.Default.ArrowForward,
          contentDescription = null,
          tint = Color(0xFFFFC107),
          modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(spacing))
      }
    }
  }
}


@Composable
fun HorizontalListItem(
  title: String,
  value: String,
  isSelected: Boolean,
  modifier: Modifier = Modifier,
  onClick: (() -> Unit)? = null
) {
  val shape = RoundedCornerShape(10.dp)

  val selectedBrush = Brush.verticalGradient(
    listOf(Color(0xFFFFCFA2), Color(0xFFF3B100))
  )

  val defaultBackground = Color(0xFFF2F2F2)

  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(4.dp)
      .clip(shape)
      .background(if (isSelected) selectedBrush else SolidColor(defaultBackground))
      .let { if (onClick != null) it.clickable { onClick() } else it }
      .padding(top = 10.dp, bottom = 12.dp, start = 10.dp, end = 10.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {

    Text(
      text = title,
      textAlign = TextAlign.Center,
      style = CommonTypography.current.smallTxt,
      color = if (isSelected) Color.Black else TextSubtext
    )

    Spacer(modifier = Modifier.height(2.dp))

    Text(
      text = value,
      textAlign = TextAlign.Center,
      style = CommonTypography.current.noteText,
      color = if (isSelected) Color.Black else TextSubtext
    )
  }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InwardSessionContent(
  showSheet: MutableState<Boolean>,
  sheetState: SheetState,
  scope: CoroutineScope,
  navController: NavHostController,
  tagCount: Int,
  onStopSession: () -> Unit,
  readerViewModel: ReaderViewModel
) {

  val uiState = SessionUiState(
    title = stringResource(R.string.stop_session),
    heading = stringResource(R.string.stop_current_session),
    subHeading = AnnotatedString( stringResource(
      R.string.inward_end_the_session
    )),
    primaryButtonText = stringResource(R.string.stop_session_),
    secondaryButtonText = stringResource(R.string.continue_later)
  )

  val actions = SessionUiActions(
    onClose = {
      scope.launch {
        sheetState.hide()
        showSheet.value = false
      }
    }, onSecondaryAction = {},
    onPrimaryAction = onStopSession
  )

  SessionBottomSheet(
    uiState = uiState, actions = actions, isSecondaryButton = false
  )
}

@Composable
fun SegmentedTabView(
  selectedIndex: Int,
  tabs: List<String>,
  onTabSelected: (Int) -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  padding: Dp = 10.dp
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(50))
      .background(Color(0xFFF2F2F2))
  ) {
    Row(
      modifier = Modifier.fillMaxWidth()
    ) {
      tabs.forEachIndexed { index, title ->
        val isSelected = selectedIndex == index

        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clip(RoundedCornerShape(50))
            .background(
              if (isSelected) Color.White else Color.Transparent
            )
            .border(
              width = if (isSelected) 2.dp else 0.dp,
              color = if (isSelected) Color(0xFFFFC94A) else Color.Transparent,
              shape = RoundedCornerShape(50)
            )
            .clickable { if (enabled) onTabSelected(index) },
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = title,
            style = CommonTypography.current.noteText,
            color = if (isSelected) Color.Black else Color.Gray,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
            modifier = Modifier.padding(vertical = padding),
          )
        }
      }
    }
  }
}

data class ScanDecisionState(
  val showAccept: Boolean = false,
  val showReject: Boolean = false,
  val showRescan: Boolean = false,
  val message: String ="",
  val isSuccess: Boolean = false
)


fun calculateScanDecisionState(
  args: InwardDetailArgs,
  checkQtyList: State<List<Boolean>>,
  totalCount: State<Int>,
  validCount: State<Int>,
  invalidCount: State<Int>,
  extraCount: State<Int>,
  unEncodedCount: State<Int>,
  alienCount: State<Int>,
  totalQty: Int = 0,
  rules: JSONObject,
  apiViewModel: ApiViewModel,
  inwardSharedViewModel: InwardSharedViewModel,
  expectedJSONArray: MutableState<String>
): ScanDecisionState {
  LogUtils.showLog("method","calculateScanDecisionState")

  var lowerTolerance =0
  var upperTolerance =0
  var enableUnencodedTags =false
  var enableAutoReject =false
  try{
    val keys = rules.keys()
    while (keys.hasNext()) {
      val key = keys.next()
      if (key.isNullOrEmpty()) continue
      if(key.equals("boxMaxLimit",true)){
          args.boxMaxLimit.value = ParseUtils.extractInt(rules.getJSONObject(key), ParameterConstants.VALUE,0)
        }
      if(key.equals("maxTolerance",true)){
          upperTolerance = ParseUtils.extractInt(rules.getJSONObject(key), ParameterConstants.VALUE,0)
        }
      if(key.equals("minTolerance",true)){
          lowerTolerance =ParseUtils.extractInt(rules.getJSONObject(key), ParameterConstants.VALUE,0)
        }
      if(key.equals("enableUnencodedTags",true)){
          enableUnencodedTags = ParseUtils.extractBoolean(rules.getJSONObject(key), ParameterConstants.VALUE,false)
        }
      if(key.equals("enableAutoReject",true)){ //temp code
        enableAutoReject = ParseUtils.extractBoolean(rules.getJSONObject(key), ParameterConstants.VALUE,false)
      }
      if(key.equals("allowBarcodeScan",true)){
          args.allowBarcodeScan.value = ParseUtils.extractBoolean(rules.getJSONObject(key), ParameterConstants.VALUE,false)
        }
      if(key.equals("enableEncoding",true)){
          var enableEncoding = ParseUtils.extractBoolean(rules.getJSONObject(key), ParameterConstants.VALUE,false)
          if(!args.enableEncoding.value && enableEncoding && args.listExpectedBarcodes.value.size==1)
            args.enableEncoding.value = enableEncoding
        }
    }
  }catch (e: Exception){e.printStackTrace()}


  LogUtils.showLog("csds_config_lowerTolerance",lowerTolerance.toString())
  LogUtils.showLog("csds_config_upperTolerance",upperTolerance.toString())
  LogUtils.showLog("csds_config_enableUnencodedTags",enableUnencodedTags.toString())
  LogUtils.showLog("csds_config_allowBarcodeScan",args.allowBarcodeScan.toString())
  LogUtils.showLog("csds_config_enableEncoding",args.enableEncoding.toString())
  LogUtils.showLog("csds_config_enableAutoReject",enableAutoReject.toString())
  LogUtils.showLog("csds_check_checkQtyList",checkQtyList.value.toString())
  LogUtils.showLog("csds_config_boxLimit",args.boxMaxLimit.value.toString())

  if (totalCount.value == 0) {
    return ScanDecisionState(
      showAccept = false,
      showReject = false,
      showRescan = false,
      message = "",
      isSuccess = false
    )
  }

  val isNonEncoded = !args.enableEncoding.value && unEncodedCount.value > 0
  val isAlien = !args.enableEncoding.value && alienCount.value > 0
  val isExtraEan = !args.enableEncoding.value && !args.isQtyOnly.value && extraCount.value > 0 // other then expected list of eans
  //args.listExpected
  val isQuantityMatch = if(args.enableEncoding.value)  totalCount.value==totalQty else (validCount.value+alienCount.value) == totalQty

  val isLessQty = totalCount.value < totalQty
  val isExcessQty = totalCount.value > totalQty

  val isLessAcceptable = /*!args.enableEncoding.value &&*/ isLessQty && lowerTolerance>0 && totalCount.value >= Math.round(totalQty * (100.0 - lowerTolerance) / 100.0)
  val isLessRejected =  isLessQty && !isLessAcceptable

  val isGreaterAcceptable = /*!args.enableEncoding.value &&*/ isExcessQty && upperTolerance > 0 && totalCount.value <= Math.round(totalQty * (100.0 + upperTolerance) / 100.0)
  val isGreaterRejected = isExcessQty && !isGreaterAcceptable

  val isHappyFlow = isQuantityMatch && !isNonEncoded && !isExtraEan && (args.isQtyOnly.value || args.enableEncoding.value || !checkQtyList.value.contains(false));//isQuantityMatch && !isNonEncoded && !isExtraEan //Check Properly

  val isRejectUnencoded = !args.enableEncoding.value && !enableUnencodedTags && isNonEncoded
  val isRejected = !isHappyFlow && (isRejectUnencoded || isLessRejected || isGreaterRejected)


  // ---------- Message building ----------
  val messages = mutableListOf<String>()

  if (isNonEncoded) messages.add("Non encoded tags found: ${unEncodedCount.value}")
  //if (isAlien) messages.add("Alien tags found: ${alienCount.value}")
  //if (isExtraEan) messages.add("Unexpected "+(if(args.isArticleBased.value) args.labelArticle.value else args.labelBarcode.value)+" found: ${extraCount.value}")
  if (isLessQty) messages.add("Shortage Quantity")//Quantity less than expected")
  if (isExcessQty) messages.add("Excess Quantity")//Quantity more than expected")
  if (isExtraEan) messages.add("Unexpected "+(if(args.isArticleBased.value) args.labelArticle.value else args.labelBarcode.value)+" found: ${extraCount.value}")
  if (!args.enableEncoding.value && isQuantityMatch && !isHappyFlow && !isNonEncoded && !isExtraEan) messages.add((if(args.isArticleBased.value) args.labelArticle.value else args.labelBarcode.value)+" quantity mismatch")

  val finalMessage =
    if (messages.isNotEmpty()) messages.joinToString(", ")
    else if (isHappyFlow) "Quantity matched successfully"
    else ""

  LogUtils.showLog("finalMessage",finalMessage);
  LogUtils.showLog("showAccept",""+!isRejected);
  LogUtils.showLog("showReject",""+(!isHappyFlow || isRejected));

  if (isHappyFlow){
    callAPI(apiViewModel,inwardSharedViewModel,args,false,finalMessage,expectedJSONArray)
  }
  else if (!isHappyFlow && (enableAutoReject || isNonEncoded) ){
    callAPI(apiViewModel,inwardSharedViewModel,args,true,finalMessage,expectedJSONArray)
  }
  else if(args.enableEncoding.value && (isHappyFlow || isQuantityMatch)){//!isRejected){
    CoroutineScope(Dispatchers.IO).launch {
    args.listNonEncodedTags.value = AppDatabase.getDbInstance(args.context).tagInfoDao().getAllPreEncodedTags(args.topic,args.menuCode,args.transactionType)
    callEpcForEncoding(args.listExpectedBarcodes.value.get(0),apiViewModel, args)
   }
  }

  // ---------- Final UI State ----------
  return ScanDecisionState(
    showAccept = !isRejected,
    showReject = !isHappyFlow || isRejected,
    showRescan = !isHappyFlow,
    message = finalMessage,
    isSuccess = isHappyFlow,
  )
}

fun clearDB(args: InwardDetailArgs){ clearDB(args.context,args.topic,args.menuCode,args.transactionType)}
fun clearDB(context: Context, topic: String,menuCode: String, transactionType: String){
  CoroutineScope(Dispatchers.IO).launch {
    val db = AppDatabase.getDbInstance(context)
    db.tagInfoDao().deleteActual(topic,menuCode, transactionType)
    db.dataQtyDao().clearScanQty(topic,menuCode, transactionType)
  }
}

fun clearSavedSessionValues(
  context: Context,
  topic: String,
  menuCode: String,
  transactionType: String,
  preHeader: String
) {
  clearDB(context,topic,menuCode,transactionType)
  DataStoreManager.saveToPreferences(preHeader + ParameterConstants.DEVICE_SESSION_ID, "")
  DataStoreManager.saveToPreferences(preHeader + ParameterConstants.READER_POWER, 15)

}

