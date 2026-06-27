package com.itek.rftaar.presentation.dashBoard

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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.dimensionResource
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
import com.itek.rftaar.api.constants.ParameterConstants
import com.itek.rftaar.core.common.constants.DateFormatUtils
import com.itek.rftaar.core.common.constants.MenuConstants
import com.itek.rftaar.core.common.utils.FileUtils
import com.itek.rftaar.core.common.utils.LogUtils
import com.itek.rftaar.core.database.AppDatabase
import com.itek.rftaar.core.database.DataStoreManager
import com.itek.rftaar.data.entity.TagInfoEntity
import com.itek.rftaar.data.model.BarcodeRefMap
import com.itek.rftaar.mqtt.constants.TopicConstants
import com.itek.rftaar.presentation.commonComp.BottomSheetTextIconRow
import com.itek.rftaar.presentation.commonComp.BottomSheetType
import com.itek.rftaar.presentation.commonComp.CommonButton
import com.itek.rftaar.presentation.commonComp.CommonTextField
import com.itek.rftaar.presentation.commonComp.CounterText
import com.itek.rftaar.presentation.commonComp.DeviceSettingItem
import com.itek.rftaar.presentation.commonComp.ItekFooter
import com.itek.rftaar.presentation.commonComp.StepIndicatorRow
import com.itek.rftaar.presentation.commonComp.StepItem
import com.itek.rftaar.presentation.commonComp.StepUiState
import com.itek.rftaar.presentation.commonComp.TextFieldConfig
import com.itek.rftaar.presentation.commonComp.TopBarContent
import com.itek.rftaar.presentation.commonComp.messageForStep
import com.itek.rftaar.presentation.commonComp.openSheet
import com.itek.rftaar.presentation.commonComp.statusForState
import com.itek.rftaar.presentation.encoding.SessionContent
import com.itek.rftaar.presentation.encoding.SetDevicePower
import com.itek.rftaar.presentation.encoding.SetLimitContent
import com.itek.rftaar.presentation.viewmodel.ApiViewModel
import com.itek.rftaar.presentation.viewmodel.ReaderViewModel
import com.itek.rftaar.ui.theme.BlackColor
import com.itek.rftaar.ui.theme.CommonTypography
import com.itek.rftaar.ui.theme.OutlineDefault
import com.itek.rftaar.ui.theme.WhiteColor
import com.itek.rftaar.utils.CommonUtils.chkNull
import com.itek.rftaar.utils.CommonUtils.chkTrue
import com.itek.rftaar.utils.SessionUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarcodeMap(
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
    val isScanScanEncode =  true
    LogUtils.showLog("isScanScanEncode",""+isScanScanEncode)
    val barCodeVal = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.BARCODE, "")
    val tidCodeVal = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.TID, "")
    val barCodeNo = remember { mutableStateOf(barCodeVal) }
    val tidCodeNo = remember { mutableStateOf(tidCodeVal) }
    val isTidCodeConfirmed = remember { mutableStateOf(false) }
    //val list=
    val context = LocalContext.current
    val activity = context as? ReaderActivity
    val readerViewModel = activity?.findReaderViewModel() ?: hiltViewModel()
    if (activity != null) {
        readerViewModel.onCreate()
        readerViewModel.setSessionAndTransactionType(menuCode)
    }
    val isProcessOn = readerViewModel.isProcessOn().observeAsState()
    //val list = remember { mutableStateOf }
    val listMapped = remember { mutableStateOf(arrayListOf<BarcodeRefMap>()) }
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

    //val db = AppDatabase.getDbInstance(context)
    //val tagCount = db.tagInfoDao().getTagWriteCount(menuCode, transactionType).observeAsState(0)

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
                  BarcodeMapBottomBar(
                        isScanScanEncode,
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
                        listMapped
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
                val encodingArgs = BarcodeMapScreenArgs(
                    context = context,
                    label = label,
                    menuCode = menuCode,
                    transactionType = transactionType,
                    scope = scope,
                    sheetState = sheetState,
                    showSheet = showSheet,
                    currentSheet = currentSheet,
                    setPower = setPower,
                    selectedLimit= selectedLimit,
                    limit = limit,
                    listMapped =listMapped
                )

              BarcodeMapContent(
                    navController = navController,
                    isScanScanEncode = isScanScanEncode,
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

data class BarcodeMapScreenArgs @OptIn(ExperimentalMaterial3Api::class) constructor(
  val context: Context,
  val label: String,
  val menuCode: String,
  val transactionType: String,
  val scope: CoroutineScope,
  val sheetState: SheetState,
  val showSheet: MutableState<Boolean>,
  val currentSheet: MutableState<BottomSheetType>,
  val setPower: MutableState<Int>,
  val selectedLimit: MutableState<Int>,
  val limit: Int,
  val listMapped: MutableState<ArrayList<BarcodeRefMap>>
)


@SuppressLint("SuspiciousIndentation")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarcodeMapContent(
  navController: NavHostController,
  isScanScanEncode: Boolean,
  barCodeNo: MutableState<String>,
  tidCodeNo: MutableState<String>,
  isTidCodeConfirmed: MutableState<Boolean>,
  apiViewModel: ApiViewModel,
  readerViewModel: ReaderViewModel,
  args: BarcodeMapScreenArgs,
) {
  val preHeader = TopicConstants.ENCODE + "_" + args.menuCode + "_"

  val isBarcodeScan = readerViewModel.isBarcodeOn().observeAsState(false)
  val scannedValue = readerViewModel.barcodeData().observeAsState("")
  val isPickOn = readerViewModel.isPickOn().observeAsState(false)
  val tagInfoData = readerViewModel.pickData().observeAsState()
  val isTagWriteDone = readerViewModel.isTagWriteDone().observeAsState(false)
  LogUtils.showLog("Val_isTagWriteDone","--"+isTagWriteDone.value)
  val isTagWriteOn = readerViewModel.isTagWriteOn().observeAsState(false)
  LogUtils.showLog("Val_isTagWriteOn","--"+isTagWriteOn.value)
  val isProcessOn = readerViewModel.isProcessOn().observeAsState(false)
  LogUtils.showLog("Val_isProcessOn","--"+isProcessOn.value)
  val triggerPressed = readerViewModel.isTriggerPressed().observeAsState(false)

  val errorMessage = readerViewModel.error().observeAsState("")
  LogUtils.showLog("Val_errorMessage","--"+errorMessage.value)
  val apiError = apiViewModel.apiErrorMsg.observeAsState("")
  val isApiLoading = apiViewModel.isLoading.observeAsState(false)

  val focusManager = LocalFocusManager.current

  //val db = AppDatabase.getDbInstance(args.context)
  //val tagEncodedCount = db.tagInfoDao().getTagWriteCount(args.menuCode, args.transactionType).observeAsState(initial = 0)
  //val tagUploadedEncodedCount = db.tagInfoDao().getUploadedTagWriteCount(args.menuCode, args.transactionType).observeAsState(initial = 0)
  //val latestEan = db.tagInfoDao().getLatestEncodedTag(args.menuCode, args.transactionType).collectAsState(initial = null)
  //val selectedSearchType = remember{ mutableStateOf("")}
  LogUtils.showLog("triggerPressed", "SingleEncodingContent: ${triggerPressed.value}")
  LogUtils.showLog("tagInfoData", "SingleEncodingContent: ${tagInfoData.value}")

  val msgInvalidScan =stringResource(R.string.err_invalid_scan)

  LaunchedEffect(tidCodeNo.value) {
    if(tidCodeNo.value.isNullOrEmpty()) isTidCodeConfirmed.value = false
    else isTidCodeConfirmed.value = true
  }


  LaunchedEffect(triggerPressed.value) {
    //val deviceId = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.DEVICE_SESSION_ID, "")
    if (!triggerPressed.value) return@LaunchedEffect
    readerViewModel.setTriggerValue(false)
    focusManager.clearFocus()
    if (chkTrue(isApiLoading.value)) return@LaunchedEffect
    if (navController.currentBackStackEntry?.lifecycle?.currentState != Lifecycle.State.RESUMED) return@LaunchedEffect
    //write condition for search dialog
    /*if (args.showSheet.value != false && args.currentSheet.value == BottomSheetType.SEARCH) {
      //Based on Selected Epc or TID
      if(selectedSearchType.value.isNotEmpty() && latestEan.value!=null)
          readerViewModel.toggleSearch(if(selectedSearchType.value.equals("epc",true)) SearchTypeConstant.EPC else SearchTypeConstant.TID,if(selectedSearchType.value.equals("epc",true)) latestEan.value!!.epc else latestEan.value!!.tid)
//        readerViewModel.toggleSearch(selectedSearchType.value,if(selectedSearchType.value.equals("epc",true)) latestEan.value!!.epc else latestEan.value!!.tid)
    } else {*/
      if (chkTrue(isProcessOn.value)) return@LaunchedEffect
      if(args.showSheet.value!=false && args.currentSheet.value != BottomSheetType.NONE) return@LaunchedEffect
      //Code for Auto clearing
      /*{
        args.showSheet.value=false
        args.currentSheet.value = BottomSheetType.NONE
      }*/
      if (barCodeNo.value.isNullOrEmpty()) readerViewModel.scanBarcode()
      else if(!isScanScanEncode) readerViewModel.performPick(pickPower = args.setPower.value, pickTime = 500, isAllowNonEncodedTags = false)
      else if (isScanScanEncode && tidCodeNo.value.isNullOrEmpty()) readerViewModel.scanBarcode()
      else if (isScanScanEncode && tidCodeNo.value.isNotEmpty()) {
        //if(tidCodeNo.value.matches(Regex("[0-9A-Fa-f]{8,}")) && tidCodeNo.value.length%4==0) {
          if(!isTidCodeConfirmed.value) isTidCodeConfirmed.value=true
          callEpcForEncoding(barCodeNo, tidCodeNo, readerViewModel,apiViewModel, args.context, tagInfoData.value, args)
        //}else readerViewModel.error().postValue(String.format(msgInvalidScan,"tid",tidCodeNo.value))
      }
    //}
  }

  LaunchedEffect(scannedValue.value) {
    if (chkNull(scannedValue.value, "").isNotEmpty()) {
      if(isScanScanEncode && barCodeNo.value.isNotEmpty()) {
        /*if(!(chkNull(scannedValue.value, "").matches(Regex("[0-9A-Fa-f]{8,}")) && chkNull(scannedValue.value, "").length%4==0))
        {
          readerViewModel.error().postValue(String.format(msgInvalidScan,"tid",scannedValue.value))
          return@LaunchedEffect
        }*/
        tidCodeNo.value = chkNull(scannedValue.value, "")
        LogUtils.showLog("scannedTidValue","--"+tidCodeNo.value)
        isTidCodeConfirmed.value=true
        callEpcForEncoding(barCodeNo, tidCodeNo,readerViewModel,apiViewModel, args.context, tagInfoData.value, args)
      }
      else barCodeNo.value = chkNull(scannedValue.value, "")

      //if (tagInfoData.value != null && barCodeNo.value.isNotEmpty() && (!isScanScanEncode || tidCodeNo.value.isNotEmpty())) {
      if (tagInfoData.value != null && barCodeNo.value.isNotEmpty() && !isScanScanEncode) {
        callEpcForEncoding(barCodeNo, tidCodeNo,readerViewModel,apiViewModel, args.context, tagInfoData.value, args)
      }
      //else if (barCodeNo.value.isNotEmpty() && (!isScanScanEncode || tidCodeNo.value.isNotEmpty())) {
      else if (barCodeNo.value.isNotEmpty() && !isScanScanEncode) {
        readerViewModel.performPick(pickPower = args.setPower.value, pickTime = 500, isAllowNonEncodedTags = false)
      }
    }
  }

  LaunchedEffect(tagInfoData.value) {
    LogUtils.showLog("tagInfoData",""+(tagInfoData.value!=null))
    if (tagInfoData.value != null && barCodeNo.value.isNotEmpty() && (!isScanScanEncode || tidCodeNo.value.isNotEmpty())) {
      callEpcForEncoding(barCodeNo,tidCodeNo, readerViewModel,apiViewModel, args.context, tagInfoData.value, args)
      //rfidStarted.value = false
    }
  }

  LaunchedEffect(isTagWriteDone.value) {
    if (isTagWriteDone.value == true && !args.menuCode.equals(MenuConstants.BULK_ENCODE,true) && !args.menuCode.equals(MenuConstants.MULTI_ENCODE,true)) {
      barCodeNo.value = ""
      if(isScanScanEncode) tidCodeNo.value = ""
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
        (tidCodeNo.value.isNotEmpty() && isTidCodeConfirmed.value==true) || isTagWriteDone.value == true -> StepUiState.COMPLETED
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

  val steps = if(isScanScanEncode) listOf(
    StepItem(
      icon = R.drawable.property_barcode,
      label = "Old",
      status = statusForState(barcodeStepState.value)
    ), StepItem(
      icon = R.drawable.property_barcode,
      label = "New",
      status = statusForState(tidScanStepState.value)
    ), StepItem(
      icon = R.drawable.property_mapped,
      label = "Mapped",
      status = statusForState(encodeStepState.value)
    )
  )
    else listOf(
    StepItem(
      icon = R.drawable.property_barcode,
      label = DataStoreManager.getBarcodeLabel(),
      status = statusForState(barcodeStepState.value)
    ), StepItem(
      icon = R.drawable.property_barcode,
      label = "RFID",
      status = statusForState(rfidStepState.value)
    ), StepItem(
      icon = R.drawable.property_mapped,
      label = "Mapped",
      status = statusForState(encodeStepState.value)
    )
  )


  /* -------------------- MESSAGES (NO remember/mutableState) -------------------- */

  val messageHeading: String = when {

    encodeStepState.value != StepUiState.IDLE -> messageForStep(
      encodeStepState.value,
      active = "Processing...",
      completed = DataStoreManager.getBarcodeLabel()+"s Mapped Successfully",
      error = chkNull(errorMessage.value, apiError.value)
    ) ?: ""

    tidScanStepState.value != StepUiState.IDLE -> messageForStep(
      tidScanStepState.value,
      active = "Processing...",
      completed = "New "+DataStoreManager.getBarcodeLabel()+" Scan Done",
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
      completed = "Old "+DataStoreManager.getBarcodeLabel()+" Scan Done",
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


  Box(modifier = Modifier.fillMaxSize()) {
    Column(
      modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Column(modifier = Modifier, verticalArrangement = Arrangement.Top) {
        TopBarContent(
          args.label,
          onBackClickL = {
            args.scope.launch {
              val preHeader = TopicConstants.ENCODE + "_" + args.menuCode + "_"
              val deviceSessionId = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.DEVICE_SESSION_ID, "")
              val id = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.ID, "")
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
          }
        )
      }
      HorizontalDivider(thickness = dimensionResource(R.dimen.dp_1), color = OutlineDefault)
      Box(modifier = Modifier
        .fillMaxSize()
        .weight(1f)) {
          /*if (tagEncodedCount.value > 0 || tagUploadedEncodedCount.value > 0){
              Column(
                  modifier = Modifier
                      .align(Alignment.TopEnd)
                      .padding(top = 16.dp, end = 16.dp)
              ) {
                  UploadStatusChip(tagUploadedEncodedCount.value,tagEncodedCount.value)
              }
          }*/

          Column(
              modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(dimensionResource(id = R.dimen.dp_16))
                .clickable(
                  onClick = {},
                  indication = null,
                  interactionSource = remember { MutableInteractionSource() })
              ,
              horizontalAlignment = Alignment.CenterHorizontally
          )
          {
              CounterText(
                  current = args.listMapped.value.size,
                  total = args.selectedLimit.value.toString(),
                  isLimitShow = args.selectedLimit.value > 0
              )

              Text(
                  text = String.format(stringResource(id = R.string.__mapped), DataStoreManager.getBarcodeLabel()),
                  style = CommonTypography.current.noteText,
                  color = BlackColor,
                  modifier = Modifier.padding(bottom = dimensionResource(R.dimen.dp_12))
              )

              /*IconText(
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

              Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_40)))*/

              CommonTextField(
                  config = TextFieldConfig(
                      value = barCodeNo.value,
                      onValueChange = {
                          //val wasNotEmpty = barCodeNo.value.isNotEmpty()
                          barCodeNo.value = it
                          if(isScanScanEncode) tidCodeNo.value = ""
                          LogUtils.showLog("onValueChange0","__"+tidCodeNo.value)
                          /*if (wasNotEmpty && it.isEmpty()) {
                              readerViewModel.clearTagData()
                          } else */readerViewModel.clearTagData()
                      },
                      label = "Old "+DataStoreManager.getBarcodeLabel(),
                      imeAction = ImeAction.Done,
                      isBarCode = true,
                      onImeAction = {
                          focusManager.clearFocus()
                          if (!chkTrue(isProcessOn.value) && !chkTrue(isApiLoading.value)) {
                              if (isTagWriteDone.value == true) readerViewModel.clearTagData()
                              if (barCodeNo.value.isNotEmpty() && !isScanScanEncode) {
                                  readerViewModel.performPick(pickPower = args.setPower.value, pickTime = 500, isAllowNonEncodedTags = false)
                              }
                              else if (barCodeNo.value.isNotEmpty() && tidCodeNo.value.isNullOrEmpty() && isScanScanEncode) {
                                  readerViewModel.scanBarcode()
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

              Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_12)))

              if(isScanScanEncode && barCodeNo.value.isNotEmpty()) {
                CommonTextField(
                  config = TextFieldConfig(
                    value = tidCodeNo.value,
                    onValueChange = {
                      LogUtils.showLog("onValueChange","__"+it)
                      //val wasNotEmpty = tidCodeNo.value.isNotEmpty()
                      tidCodeNo.value = it
                      //isTidCodeConfirmed.value = false
                      LogUtils.showLog("onValueChange","__"+tidCodeNo.value)
                      /*if (wasNotEmpty && it.isEmpty()) {
                        readerViewModel.clearTagData()
                      } else */readerViewModel.clearTagData()
                    },
                    label = "New "+DataStoreManager.getBarcodeLabel(),
                    imeAction = ImeAction.Done,
                    isBarCode = true,
                    onImeAction = {
                      focusManager.clearFocus()
                      if (!chkTrue(isProcessOn.value) && !chkTrue(isApiLoading.value)) {
                        if (isTagWriteDone.value == true) readerViewModel.clearTagData()
                        if (tidCodeNo.value.isNotEmpty()) {
                          //if(tidCodeNo.value.matches(Regex("[0-9A-Fa-f]{8,}")) && tidCodeNo.value.length%4==0) {
                            //isTidCodeConfirmed.value = true
                            callEpcForEncoding(barCodeNo, tidCodeNo, readerViewModel,apiViewModel, args.context, tagInfoData.value, args)
                          //}
                          //else readerViewModel.error().postValue(String.format(msgInvalidScan,"tid",tidCodeNo.value))
                          //readerViewModel.performPick(pickPower = args.setPower.value, pickTime = 500)
                        }
                      }
                    },
                    onClick = {
                      focusManager.clearFocus()
                      if (chkTrue(isProcessOn.value) || chkTrue(isApiLoading.value)) return@TextFieldConfig
                      if (isTagWriteDone.value == true) readerViewModel.clearTagData();
                      readerViewModel.scanBarcode()
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
                      barcodeStepState.value, if(isScanScanEncode) tidScanStepState.value else rfidStepState.value, encodeStepState.value
                  )
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
                .fillMaxHeight(0.45f)
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
              context = args.context,
              showSheet = args.showSheet,
              sheetState = args.sheetState,
              scope = args.scope,
              currentSheet = args.currentSheet,
                menuCode = args.menuCode,
            )
          }

          BottomSheetType.POWER -> Box(
            modifier = Modifier
                .background(WhiteColor)
                .fillMaxWidth()
                .fillMaxHeight(0.47f)
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

          /*BottomSheetType.SEARCH -> Box(
            modifier = Modifier
                .background(WhiteColor)
                .fillMaxWidth()
                .fillMaxHeight(0.53f)
          ) {
            SearchBottomSheetView(
              showSheet = args.showSheet,
              sheetState = args.sheetState,
              scope = args.scope,
              selectedSearchType = selectedSearchType,
              latestEan = latestEan,
              readerViewModel = readerViewModel,
              tagInfoEntity = tagInfoData
            )
          }*/

          BottomSheetType.SESSION -> Box(
            modifier = Modifier
              .background(WhiteColor)
              .fillMaxWidth()
              .fillMaxHeight(if (args.currentSheet.value == BottomSheetType.SETTINGS) 0f else 0.30f)
          ) {
            val tagCount = args.listMapped.value.size
            val subHeading = if (tagCount > 0) {
                  val countText =
                      if (args.limit > 0) "${tagCount}/${args.limit}"
                      else "${tagCount}"
                  buildAnnotatedString {

                      append(stringResource(R.string.barcode_map_end_the_session))

                      withStyle(
                          style = SpanStyle(fontWeight = FontWeight.Bold)
                      ) {
                          append(" $countText ")
                      }
                      append(stringResource(R.string.items))
                      append("\n")
                      append(stringResource(R.string.data_lost))
                      append("\n")
                      append(stringResource(R.string.please_confirm_))
                  }
              } else {
                  AnnotatedString(stringResource(R.string.barcode_map_end_the_session_0))
              }
            SessionContent(
              showSheet = args.showSheet,
              sheetState = args.sheetState,
              scope = args.scope,
              navController = navController,
              tagCount = tagCount,
              subHeading =  subHeading,
              onStopSession = {
                if (chkTrue(isProcessOn.value)) return@SessionContent
                CoroutineScope(Dispatchers.IO).launch {
                  val preHeader = TopicConstants.ENCODE + "_" + args.menuCode + "_"
                  DataStoreManager.saveToPreferences(preHeader + ParameterConstants.ID, "")
                  DataStoreManager.saveToPreferences(preHeader + ParameterConstants.DEVICE_SESSION_ID, "")
                  /*DataStoreManager.saveToPreferences(preHeader + ParameterConstants.LIMIT, 0)
                  DataStoreManager.saveToPreferences(preHeader + ParameterConstants.READER_POWER, 7)*/
                  //db.tagInfoDao().deleteBySessionTypeAndTransactionType(args.menuCode, args.transactionType)
                }
                args.scope.launch {
                  if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                    readerViewModel.clearSessionAndTransactionType()
                    navController.popBackStack()
                  }
                }
              },
              readerViewModel,
              isAllowContinue = false,
              limit = args.limit
            )
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
fun BarcodeMapBottomBar(
  isScanScanEncode: Boolean,
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
  listMapped: MutableState<ArrayList<BarcodeRefMap>>,
) {

/*  val tagInfoData = readerViewModel.pickData().observeAsState()
  val isTagWriteDone = readerViewModel.isTagWriteDone().observeAsState(false)
  val isProcessOn = readerViewModel.isProcessOn().observeAsState(false)
  val isApiLoading = apiViewModel.isLoading.observeAsState(false)*/

  //val db = AppDatabase.getDbInstance(context)
  //val tagCount = db.tagInfoDao().getTagWriteCount(menuCode, transactionType).observeAsState(0)
  //val tagVerifyCount = db.tagInfoDao().getTagWriteVerifiedCount(menuCode, transactionType).observeAsState(0)



  /*LaunchedEffect(Unit) {
    apiViewModel.apiResult.collect { result ->
      if (!result.isSuccess) {
        LogUtils.showLog("TAG", "SingleEncodingBottomBar: ")
      } else if (result.response != null) {
        when (result.url) {
          UrlConstants.ENCODING -> handleEncode(
            result, tagInfoData.value, readerViewModel, menuCode, selectedLimit
          )
        }
      }
    }
  }*/


  Column(
    modifier = Modifier
      .fillMaxWidth()
      .background(WhiteColor)
  ) {
    HorizontalDivider(color = OutlineDefault)

    when {
      barCodeNo.value.isNotEmpty() && (!isScanScanEncode || tidCodeNo.value.isNotEmpty()) -> {
        Row(modifier = Modifier.padding(dimensionResource(R.dimen.dp_16))) {
          CommonButton(
            text = stringResource(id =if(isScanScanEncode) R.string.start_barcode_map else R.string.scan_tag), onClick = {
              readerViewModel.setTriggerValue(true)
              // readerViewModel.performPick(pickPower = setPower.value, pickTime = 500)
            }, modifier = Modifier
              .fillMaxWidth()
          )
        }
      }

      /*tagCount.value > 0 && (menuCode.equals("CHILD_MENU_BULK_ENCODE", true) || barCodeNo.value.isEmpty()) -> {
        Row(
          modifier = Modifier.padding(dimensionResource(R.dimen.dp_16)),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          if (!menuCode.equals("CHILD_MENU_BULK_ENCODE", true) || barCodeNo.value.isEmpty()) {
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
                *//*if (chkTrue(isProcessOn.value) || chkTrue(isApiLoading.value)) return@CommonButton
                if (isTagWriteDone.value == true) readerViewModel.clearTagData()
                readerViewModel.performPick(
                    pickPower = setPower.value, pickTime = 500
                )*//*
              }, modifier = Modifier.weight(1f),
              gradientBrush = SolidColor(WhiteColor),
              contentColor = BlackColor
            )
          }

          Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_16)))

          CommonButton(
            text = stringResource(id = if ((tagCount.value - tagVerifyCount.value) > 0) R.string.verify_tags else R.string.verification_done),
            onClick = {
                val data = mapOf("menuCode" to "$menuCode", "transactionType" to "$transactionType","tagCount" to "${tagCount.value}")
              navController.navigate(Screen.VerifyLogsScreen.createRoute(data))
            },
            modifier = Modifier.weight(1f)
          )
        }
      }*/
    }
  }

  if (listMapped.value.size == 0 && barCodeNo.value.isNullOrEmpty() && (!isScanScanEncode || tidCodeNo.value.isNullOrEmpty())) {
    ItekFooter()
  }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceSettings(
  context: Context,
  scope: CoroutineScope,
  sheetState: SheetState,
  showSheet: MutableState<Boolean>,
  currentSheet: MutableState<BottomSheetType>,
  menuCode: String,
) {

  val share = "Share"
  val about = stringResource(R.string.about)
    val context = LocalContext.current
//    val activity = context as ReaderActivity

  val items = remember {
    listOf(
      DeviceSettingItem(
        iconRes = R.drawable.property_setting_power_decibel, title = share
      ) {
        FileUtils.zipAndShare(context,folderName = menuCode.replace(" ", "_"))
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

fun callEpcForEncoding(
  barCodeNo: MutableState<String>,
  tidCodeNo: MutableState<String>,
  readerViewModel: ReaderViewModel,
  apiViewModel: ApiViewModel,
  context: Context,
  tagInfoData: TagInfoEntity?,
  args: BarcodeMapScreenArgs
) {
  CoroutineScope(Dispatchers.IO).launch {
    readerViewModel.isTagWriteOn().postValue(true)
    val preHeader = TopicConstants.ENCODE + "_" + args.menuCode + "_"
    val existingSessionId = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.DEVICE_SESSION_ID, "")
    val deviceSessionId = if (existingSessionId.isNullOrEmpty()) SessionUtils.generateOfflineSessionId(args.menuCode, args.transactionType) else ""
    if (existingSessionId.isNullOrEmpty() && deviceSessionId.isNotEmpty()) {
      val deviceSessionId =
        SessionUtils.generateOfflineSessionId(args.menuCode, args.transactionType)
      DataStoreManager.saveToPreferences(preHeader + ParameterConstants.ID, deviceSessionId)
      DataStoreManager.saveToPreferences(
        preHeader + ParameterConstants.DEVICE_SESSION_ID,
        deviceSessionId
      )
    }
    val sessionIdToSend = chkNull(existingSessionId, deviceSessionId)
    if (barCodeNo.value.trim().equals(tidCodeNo.value.trim(), true)) {
      readerViewModel.error().postValue("Old & New "+DataStoreManager.getBarcodeLabel()+"s can't be same")
      readerViewModel.isTagWriteOn().postValue(false)
      return@launch
    }
    if (args.listMapped.value.filter { m -> m.oldBarcode.equals(barCodeNo.value) && m.newBarcode.equals(tidCodeNo.value) }.size > 0) {
      readerViewModel.error().postValue(DataStoreManager.getBarcodeLabel()+" already mapped")
      readerViewModel.isTagWriteOn().postValue(false)
      return@launch
    }
    val barcodeRefMap = BarcodeRefMap(
      barCodeNo.value,
      tidCodeNo.value,
      if (tagInfoData != null) tagInfoData.epc else "",
      if (tagInfoData != null) tagInfoData.tid else "",
      DateFormatUtils.getCurrentDisplayTime()
    )
    args.listMapped.value.add(barcodeRefMap)
    FileUtils.writeCsvFile(args.menuCode.replace(" ", "_"),sessionIdToSend, barcodeRefMap)
    readerViewModel.isTagWriteOn().postValue(false)
    readerViewModel.isTagWriteDone().postValue(true)
  }
 }

fun clearSavedSessionValues(context: Context, menuCode: String, transactionType: String) {
  val preHeader = TopicConstants.ENCODE + "_" + menuCode + "_"
  DataStoreManager.saveToPreferences(preHeader + ParameterConstants.DEVICE_SESSION_ID, "")
  CoroutineScope(Dispatchers.IO).launch {
    val db = AppDatabase.getDbInstance(context)
    db.tagInfoDao().deleteBySessionTypeAndTransactionType(menuCode, transactionType)
  }
}


