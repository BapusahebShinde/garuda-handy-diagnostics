package com.itek.rftaar.presentation.inward

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
import com.itek.rftaar.api.constants.UrlConstants
import com.itek.rftaar.core.common.constants.LoginConstants
import com.itek.rftaar.core.common.utils.LogUtils
import com.itek.rftaar.core.common.utils.ParseUtils.extractInt
import com.itek.rftaar.core.common.utils.ParseUtils.extractJSONArray
import com.itek.rftaar.core.common.utils.ParseUtils.extractString
import com.itek.rftaar.core.database.AppDatabase
import com.itek.rftaar.core.database.DataStoreManager
import com.itek.rftaar.data.model.IOFlowQty
import com.itek.rftaar.data.model.IOLevel
import com.itek.rftaar.mqtt.constants.TopicConstants
import com.itek.rftaar.presentation.commonComp.AppSnackBar
import com.itek.rftaar.presentation.commonComp.BottomSheetType
import com.itek.rftaar.presentation.commonComp.CommonButton
import com.itek.rftaar.presentation.commonComp.CommonTextField
import com.itek.rftaar.presentation.commonComp.CounterText
import com.itek.rftaar.presentation.commonComp.ErrorAppSnackBarData
import com.itek.rftaar.presentation.commonComp.GernericBasicTextField
import com.itek.rftaar.presentation.commonComp.SnackbarController
import com.itek.rftaar.presentation.commonComp.StatItem
import com.itek.rftaar.presentation.commonComp.StatsCard
import com.itek.rftaar.presentation.commonComp.SuccessAppSnackBarData
import com.itek.rftaar.presentation.commonComp.TextFieldConfig
import com.itek.rftaar.presentation.commonComp.TopBarContent
import com.itek.rftaar.presentation.commonComp.openSheet
import com.itek.rftaar.presentation.dashBoard.navigateMenu
import com.itek.rftaar.presentation.encoding.DeviceSettings
import com.itek.rftaar.presentation.inventory.AcceptDataSheet
import com.itek.rftaar.presentation.inventory.PartialCompletionDataSheet
import com.itek.rftaar.presentation.navigation.Screen
import com.itek.rftaar.presentation.search.InventoryBadge
import com.itek.rftaar.presentation.viewmodel.ApiViewModel
import com.itek.rftaar.presentation.viewmodel.InwardSharedViewModel
import com.itek.rftaar.presentation.viewmodel.ReaderViewModel
import com.itek.rftaar.reader.constants.StatusConstants
import com.itek.rftaar.ui.theme.BlackColor
import com.itek.rftaar.ui.theme.CardGreen
import com.itek.rftaar.ui.theme.CommonTypography
import com.itek.rftaar.ui.theme.ErrorBgColor
import com.itek.rftaar.ui.theme.Green
import com.itek.rftaar.ui.theme.LightYellow
import com.itek.rftaar.ui.theme.OutlineDefault
import com.itek.rftaar.ui.theme.RedColor
import com.itek.rftaar.ui.theme.TabColor
import com.itek.rftaar.ui.theme.WhiteColor
import com.itek.rftaar.ui.theme.Yellow
import com.itek.rftaar.utils.CommonUtils.chkNull
import com.itek.rftaar.utils.CommonUtils.chkTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("SuspiciousIndentation")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InwardScreen(
    modifier: Modifier,
    navController: NavHostController,
    label: String,
    menuCode: String,
    inwardSharedViewModel: InwardSharedViewModel = hiltViewModel(),
    apiViewModel: ApiViewModel = hiltViewModel(),
    searchParams: Map<String, Any> = emptyMap(),
) {
    val transactionType = if(searchParams.containsKey("transactionType") && searchParams["transactionType"] is String) chkNull(searchParams["transactionType"] as String,"") else ""
    val isInward =  menuCode.matches(Regex("(?i)(^.*INW.*$)"))
    val scope = rememberCoroutineScope()
    val barCodeNo = remember { mutableStateOf("") }
    val context = LocalContext.current
    val activity = context as? ReaderActivity
    val readerViewModel = activity?.findReaderViewModel() ?: hiltViewModel()
    if (activity != null) {
        readerViewModel.onCreate()
        readerViewModel.setSessionAndTransactionType(menuCode)
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
    val lbl = remember { mutableStateOf("") }
    val selLevel = remember { mutableStateOf(inwardSharedViewModel.getLastInwardLevel()) }
    val listIODataFlows =  remember { mutableStateOf(listOf<IOFlowQty>())}
    val selectedIOFlowQty =  remember { mutableStateOf<IOFlowQty?>(null) }
    val showLabelPicker = remember { mutableStateOf(false) }

    LogUtils.showLog("transactionType", "InwardScreen:$transactionType ")

    LaunchedEffect(Unit) {
        if (apiViewModel != null) {
            val map = hashMapOf(
                ParameterConstants.CUSTOMER_ID to DataStoreManager.readFromPreferences(ParameterConstants.CUSTOMER_ID, ""),
                ParameterConstants.BUSINESS_LINE_ID to DataStoreManager.readFromPreferences(ParameterConstants.BUSINESS_LINE_ID, ""),
                ParameterConstants.LOCATION_ID to DataStoreManager.readFromPreferences(LoginConstants.DEVICE_LOCATION_ID, ""),
                ParameterConstants.DEVICE_ID to DataStoreManager.readFromPreferences(ParameterConstants.DEVICE_ID, "")
            )
            val size = inwardSharedViewModel.inwardLevels.size;
            val isRootLevel = size<=0;
            if(!isRootLevel){
                map.put(ParameterConstants.FLOW_ID,selLevel.value!!.flowId)
                map.put(ParameterConstants.LEVEL,selLevel.value!!.level.toString())
                map.put(ParameterConstants.NODE_LEVEL,selLevel.value!!.nodeLevel?.minus(1).toString())
                //for ((index, value) in inwardSharedViewModel.inwardLevels.withIndex()) {
                for(i in 0 until size){
                    val level = inwardSharedViewModel.inwardLevels[i]
                    map.put(ParameterConstants.NODE+(level.level-i),level.node)
                }
            }
            val url = if(isInward){if(isRootLevel) UrlConstants.GET_INWARD_LIST else UrlConstants.GET_INWARD_CHILDREN_LIST} else {if(isRootLevel) UrlConstants.GET_OUTWARD_LIST else UrlConstants.GET_OUTWARD_CHILDREN_LIST}
            apiViewModel.callApi(url, queryMap = map)
        }
    }


    BackHandler(enabled = true) {
        scope.launch {
            if (chkTrue(isProcessOn.value)||chkTrue(isApiLoading.value)) return@launch
            //TODO ask for confirmation?
            if(navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                inwardSharedViewModel.removeLastInwardLevel()
                readerViewModel.clearSessionAndTransactionType()
                navController.popBackStack()
            }
        }
    }

    val inwardArgs = InwardScreenArgs(
        context = context,
        isInward = isInward,
        topic = if(isInward) TopicConstants.INWARD else TopicConstants.OUTWARD,
        label = label,
        menuCode = menuCode,
        transactionType = transactionType,
        scope = scope,
        sheetState = sheetState,
        showSheet = showSheet,
        currentSheet = currentSheet,
        listIODataFlows = listIODataFlows,
        selectedIOFlowQty = selectedIOFlowQty,
        showLabelPicker = showLabelPicker
    )

    Box {
        Scaffold(
            bottomBar = {
                Box(
                    modifier = modifier
                        .fillMaxWidth()
                        .background(WhiteColor),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    InwardBottomBar(
                        apiViewModel,
                        readerViewModel,
                        inwardSharedViewModel,
                        menuCode,
                        navController,
                        transactionType,
                        context,
                        lbl,
                        isInward,
                        selLevel,
                        inwardArgs
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

                InwardContent(
                    navController = navController,
                    barCodeNo = barCodeNo,
                    apiViewModel = apiViewModel,
                    readerViewModel = readerViewModel,
                    args = inwardArgs,
                    inwardSharedViewModel = inwardSharedViewModel,
                    lbl = lbl,
                    isProcessOn, isApiLoading,selLevel
                )
            }
        }

        if (showLabelPicker.value) {
            LabelPicker(
                listIODataFlows.value,
                onDismiss = { showLabelPicker.value = false },
                onLocationSelected = { iOFlowQty ->
                    selectedIOFlowQty.value = iOFlowQty
                    lbl.value = selectedIOFlowQty.value!!.label
                    showLabelPicker.value = false
                }
            )
        }

    }

}

data class InwardScreenArgs @OptIn(ExperimentalMaterial3Api::class) constructor(
    val context: Context,
    val isInward: Boolean,
    val topic: String,
    val label: String,
    val menuCode: String,
    val transactionType: String,
    val scope: CoroutineScope,
    val sheetState: SheetState,
    val showSheet: MutableState<Boolean>,
    val currentSheet: MutableState<BottomSheetType>,
    val listIODataFlows: MutableState<List<IOFlowQty>>,
    val selectedIOFlowQty: MutableState<IOFlowQty?>,
    val showLabelPicker: MutableState<Boolean>,
)

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InwardContent(
    navController: NavHostController,
    barCodeNo: MutableState<String>,
    apiViewModel: ApiViewModel,
    readerViewModel: ReaderViewModel,
    args: InwardScreenArgs,
    inwardSharedViewModel: InwardSharedViewModel,
    lbl: MutableState<String>,
    isProcessOn: State<Boolean?>,
    isApiLoading: State<Boolean>,
    selLevel:  MutableState<IOLevel?>,
) {
    val focusManager = LocalFocusManager.current
    val db = AppDatabase.getDbInstance(args.context)
    val listIOConfigObjects = db.inOutConfigDao().getConfigObjects(args.topic).collectAsState(initial = emptyList())
    val listIOData =  remember { mutableStateOf(listOf<IOLevel>())}
    /*val listIODataFlows =  remember { mutableStateOf(listOf<IOFlowQty>())}
    val selectedIOFlowQty =  remember { mutableStateOf<IOFlowQty?>(null) }
    val showLabelPicker = remember { mutableStateOf(false) }*/

        /*when(inwardSharedViewModel.inwardLevels.size){
        0 -> listOf(
            InwardLevel("Flow321",3,"RF1644",3, total=3, label = "Trip"),
            InwardLevel("Flow321",3,"RF1645", nodeLevel = 3,total=3, label = "Trip"),
            InwardLevel("Flow321",3,"RF1646", nodeLevel = 3,total=3, label = "Trip")
        )

        1-> listOf(
            InwardLevel("Flow321",3,"AN1644",2,3, label = "ASN"),
            InwardLevel("Flow321",3,"AN1645",2,3, label = "ASN"),
            InwardLevel("Flow321",3,"AN1646",2,3, label = "ASN")
        )

        2-> listOf(
            InwardLevel("Flow321",3,"BX1644",1,20, label = "BOX"),
            InwardLevel("Flow321",3,"BX1645",1,15, label = "BOX"),
            InwardLevel("Flow321",3,"BX1646",1,10, label = "BOX")
        )

        else -> listOf()
    })}*/
    val msgNoData = stringResource(R.string.err_no_data)
    val snackbarController = remember { SnackbarController() }
    val triggerPressed = readerViewModel.isTriggerPressed().observeAsState(false)
    val scannedValue = readerViewModel.barcodeData().observeAsState("")
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
        }
        else if (result.response != null) {
            val jsonResponse = result.response

            when (result.url) {
                UrlConstants.GET_INWARD_LIST , UrlConstants.GET_INWARD_CHILDREN_LIST,UrlConstants.GET_OUTWARD_LIST , UrlConstants.GET_OUTWARD_CHILDREN_LIST -> {
                    //val response = ParseUtils.extractJSONObject(jsonResponse, ParameterConstants.DATA, jsonResponse)
                    val responseArray = extractJSONArray(jsonResponse, ParameterConstants.DATA, JSONArray())
                    val dataList = mutableListOf<IOLevel>()
                    val dataTypeList = mutableListOf<IOFlowQty>()
                    var labels = "";
                    var completedCount=0
                    if (responseArray != null && responseArray.length() > 0) {
                        for (i in 0 until responseArray.length()) {
                            val obj = responseArray.getJSONObject(i)
                            val ioLevel = IOLevel(
                                flowId= extractString(obj, ParameterConstants.FLOW_ID,""),
                                flowName= extractString(obj, ParameterConstants.FLOW_NAME,""),
                                level=extractInt(obj, ParameterConstants.LEVEL,0),
                                node= extractString(obj, ParameterConstants.NODE,""),
                                nodeLevel=extractInt(obj, ParameterConstants.NODE_LEVEL,0),
                                total=extractInt(obj, ParameterConstants.TOTAL,extractInt(obj, ParameterConstants.QTY,0)),
                                completed=extractInt(obj, ParameterConstants.COMPLETED,extractInt(obj, ParameterConstants.SCAN_QTY,0)),
                                status=extractString(obj, ParameterConstants.STATUS,""),
                                label=extractString(obj, ParameterConstants.LABEL,"")
                            )

                            if (ioLevel != null) {
                                completedCount += if (ioLevel.status.equals(StatusConstants.COMPLETED, true)) 1 else 0
                                if (ioLevel.label.isNullOrEmpty() && !listIOConfigObjects.value.isNullOrEmpty()) {
                                    val ioConfig = listIOConfigObjects.value.firstOrNull {
                                        it.id.equals(ioLevel.flowId, true)
                                    }
                                    if(ioConfig!=null){
                                        val index = ioLevel.level - ioLevel.nodeLevel
                                        val levelsArray = JSONArray(ioConfig.levelsJArray)
                                        if (levelsArray.toString().isEmpty()) return@LaunchedEffect
                                        if (index in 0 until levelsArray.length()) {
                                            val label = extractString(
                                                levelsArray.getJSONObject(index),
                                                ParameterConstants.LABEL,
                                                ""
                                            )
                                            ioLevel.label = label
                                        }
                                    }
                                }
                                if (labels.isEmpty()) labels = ioLevel.label
                                else if (!labels.contains(ioLevel.label)) labels += "/${ioLevel.label}"
                                dataList.add(ioLevel)
                                if(selLevel.value==null) {
                                    val ioFlowQty = if (dataTypeList.isNullOrEmpty()) null else dataTypeList.firstOrNull { it.flowId.equals(ioLevel.flowId, true) }
                                    if (ioFlowQty == null)
                                        dataTypeList.add(IOFlowQty(ioLevel.flowId, ioLevel.flowName, 1,ioLevel.label))
                                    if (ioFlowQty != null) {
                                        ioFlowQty.qty += 1
                                        dataTypeList.set(dataTypeList.indexOf(ioFlowQty), ioFlowQty)
                                    }
                                }
                            }
                        }
                    }
                    if (dataList.isNotEmpty()) {
                        listIOData.value = dataList
                        if(selLevel.value==null) {
                            args.listIODataFlows.value = dataTypeList
                            args.selectedIOFlowQty.value = if (dataTypeList.isNullOrEmpty()) null else dataTypeList.get(0)
                        }
                        lbl.value = if (args.selectedIOFlowQty.value != null) args.selectedIOFlowQty?.value!!.label else labels
                        selLevel.value?.let { current ->
                            if (current.completed < completedCount) {
                                selLevel.value = current.copy(completed = completedCount)
                            }
                        }
                    }

                    else {
                        //TODO give custom error if responseArray is empty
                        snackbarController.show(
                            ErrorAppSnackBarData(chkNull(result?.errMsg.toString(),msgNoData)
                        ))
                    }
                }
                UrlConstants.COMPLETE_INWARD_NODE, UrlConstants.COMPLETE_OUTWARD_NODE -> {
                    val message = extractString(jsonResponse, ParameterConstants.MESSAGE,extractString(jsonResponse, ParameterConstants.MSG,""))
                    if(message.isNotEmpty()) {
                      snackbarController.show(SuccessAppSnackBarData(message))
                      delay(100)
                      if(navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                       inwardSharedViewModel.removeLastInwardLevel()
                       readerViewModel.clearSessionAndTransactionType()
                       navController.popBackStack()
                      }
                    }
                }
            }
        }
    }

    LaunchedEffect(triggerPressed.value) {
        if(!triggerPressed.value) return@LaunchedEffect
        readerViewModel.setTriggerValue(false)
        if (chkTrue(isProcessOn.value) || chkTrue(isApiLoading.value)) return@LaunchedEffect
        if (args.showSheet.value!=false && args.currentSheet.value!=BottomSheetType.NONE) return@LaunchedEffect
        if(navController.currentBackStackEntry?.lifecycle?.currentState != Lifecycle.State.RESUMED) return@LaunchedEffect
        readerViewModel.scanBarcode()
    }

    LaunchedEffect(scannedValue.value) {
        if (chkNull(scannedValue.value, "").isNotEmpty()) {
            barCodeNo.value = chkNull(scannedValue.value, "")
        }
    }

    val filteredList = remember(barCodeNo.value, listIOData.value, args.selectedIOFlowQty.value) {
        if (args.selectedIOFlowQty.value==null && barCodeNo.value.isBlank()) {
            listIOData.value
        }else {
            listIOData.value.filter { item ->
                val filterTerm = args.selectedIOFlowQty.value
                val searchTerm = barCodeNo.value.trim()
                // Adjust fields here for your filter matching
                (filterTerm==null || filterTerm.flowId.equals(item.flowId)) && ( searchTerm.isNullOrEmpty() ||
                (item.node?.contains(searchTerm, ignoreCase = true) == true) ||
                  (item.label?.contains(searchTerm, ignoreCase = true) == true))
            }
        }
    }

    /*LaunchedEffect(listData.value) {
        lbl.value = if(listData.value.isNullOrEmpty()) "barcode" else listData.value[0].label
    }*/


    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.Top) {
                TopBarContent(
                    args.label,
                    onBackClickL = {
                        args.scope.launch {
                            if (chkTrue(isProcessOn.value) && chkTrue(isApiLoading.value) ) return@launch
                            if(navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                                inwardSharedViewModel.removeLastInwardLevel()
                            readerViewModel.clearSessionAndTransactionType()
                            navController.popBackStack()}
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
                    },
                    isSetting = false
                )
            }
            HorizontalDivider(thickness = dimensionResource(R.dimen.dp_1), color = OutlineDefault)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(dimensionResource(id = R.dimen.dp_16))
                    /*.clickable(
                        onClick = {},
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() })*/,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                HorizontalListView(
                    items = inwardSharedViewModel.inwardLevels,
                    /*onItemClick = { index ->
                        // Handle click
                    }*/
                )

                Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_16)))

                if(selLevel.value!=null && selLevel.value!!.level>0) {
                    StatsCard(
                        items = listOf(
                            StatItem(
                                value = selLevel.value!!.total.toString(),
                                label = stringResource(R.string.txt_total_inward, lbl.value)
                            ),
                            StatItem(
                                value = (selLevel.value!!.total-selLevel.value!!.completed).toString(),
                                label = stringResource(R.string.txt_pending, lbl.value)
                            ),
                            StatItem(
                                value = selLevel.value!!.completed.toString(),
                                label = stringResource(R.string.txt_completed, lbl.value),
                                valueColor = Yellow
                            )
                        )
                    )
                }
                else{
                    /*if (showLabelPicker.value) {
                        LabelPicker(
                            listIODataFlows.value,
                            onDismiss = { showLabelPicker.value = false },
                            onLocationSelected = { iOFlowQty ->
                                selectedIOFlowQty.value = iOFlowQty
                                lbl.value = selectedIOFlowQty.value!!.label
                                showLabelPicker.value = false
                            }
                        )
                    }*/

                    CounterText(
                        current = listIOData.value.filter { item -> args.selectedIOFlowQty.value==null || args.selectedIOFlowQty?.value!!.flowId.equals(item.flowId)}.size,
                        total = "",
                        isLimitShow = false
                    )

                    Text(
                        text = stringResource(id = R.string.txt_total),
                        style = CommonTypography.current.noteText,
                        color = BlackColor,
                        modifier = Modifier.padding(bottom = dimensionResource(R.dimen.dp_12))
                    )
                }

                Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_12)))

                if(selLevel.value==null) {
                    if (args.selectedIOFlowQty.value == null) {
                        Card(
                            modifier = Modifier
                                .width(dimensionResource(R.dimen.dp_116))
                                .height(dimensionResource(R.dimen.dp_44))
                                .clickable(
                                    onClick = {
                                        if (args.listIODataFlows.value.isNotEmpty())
                                            args.scope.launch { args.showLabelPicker.value = true }
                                    },
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ),
                            shape = RoundedCornerShape(dimensionResource(R.dimen.dp_24)),
                            colors = CardDefaults.cardColors(containerColor = WhiteColor),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
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

                                androidx.compose.material.Text(
                                    text = stringResource(R.string.select_type),
                                    style = CommonTypography.current.noteText,
                                    color = BlackColor
                                )
                            }
                        }
                    }
                    else {
                        GernericBasicTextField(
                            sourceZone = chkNull(args.selectedIOFlowQty.value!!.flowName,args.selectedIOFlowQty.value!!.label),
                            destZone = "",
                            menuCode = args.menuCode,
                            value = chkNull(args.selectedIOFlowQty.value!!.flowName,args.selectedIOFlowQty.value!!.label),
                            labelRowAction = {
                                args.scope.launch {
                                    apiViewModel.callApi(
                                        url = UrlConstants.LOCATION_SUB_ZONES,
                                        appendData = DataStoreManager.readFromPreferences(
                                            LoginConstants.DEVICE_LOCATION_ID, ""
                                        )
                                    )
                                    args.showLabelPicker.value = true
                                }
                            },
                            destAction = {

                            },
                            clearAction = {
                                args.scope.launch {
                                    args.selectedIOFlowQty.value = null
                                    args.showLabelPicker.value = false
                                }
                            },
                            isDestZone = false,
                        )
                        /**BasicTextField(
                            value = chkNull(args.selectedIOFlowQty.value!!.flowName,args.selectedIOFlowQty.value!!.label),
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
                                    *//**if (args.isOnDataUploaded.value) GreenBorder else*//** Yellow,
                                    RoundedCornerShape(24.dp)
                                )
                                .background(WhiteColor, RoundedCornerShape(24.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            decorationBox = { innerTextField ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {

                                    // Leading icon
                                    Icon(
                                        painter = painterResource(id = R.drawable.icon_location),
                                        contentDescription = null,
                                        modifier = Modifier.size(dimensionResource(R.dimen.dp_18)),
                                        tint = *//**if (args.isOnDataUploaded.value) Green else*//** Yellow
                                    )

                                    Spacer(Modifier.width(6.dp))

                                    // Text
                                    innerTextField()

                                    Spacer(Modifier.width(6.dp))

                                    // Trailing icon
//                                val preHeader = TopicConstants.INWARD + "_" + args.menuCode + "_"
//                                val deviceSessionId = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.DEVICE_SESSION_ID, "")
//                                val id = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.ID, "")

                                    //if (deviceSessionId.isNullOrEmpty() || id.isNullOrEmpty()) {
                                    Image(
                                        painter = painterResource(id = R.drawable.property_edit),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(dimensionResource(R.dimen.dp_16))
                                            .clickable(
                                                indication = null,
                                                interactionSource = remember { MutableInteractionSource() }
                                            ) {
                                                args.scope.launch {
                                                    args.showLabelPicker.value = true
                                                }
                                            }
                                    )
                                    *//**} else {
                                    Icon(
                                        painter = painterResource(id = R.drawable.property_check_selected),
                                        contentDescription = null,
                                        tint = *//***//**if (args.isOnDataUploaded.value) Green else*//***//** LightGray
                                    )
                                }*//**
                                }
                            }
                        )*/

                    }
                    Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_24)))
                }

                CommonTextField(
                    config = TextFieldConfig(
                        value = barCodeNo.value,
                        onValueChange = {
                            val wasNotEmpty = barCodeNo.value.isNotEmpty()
                            barCodeNo.value = it
                            readerViewModel.clearTagData()
                        },
                        label = lbl.value,//if(listData.value.isNullOrEmpty()) "" else lbl.value,
                        imeAction = ImeAction.Done,
                        isBarCode = true,
                        onImeAction = {
                            focusManager.clearFocus()
                        },
                        onClick = {
                            if (chkTrue(isProcessOn.value) || chkTrue(isApiLoading.value)) return@TextFieldConfig
                            readerViewModel.scanBarcode()
                            LogUtils.showLog("scanBarcode", "Scanning started")
                        }
                    )
                )

                Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_16)))

                if (filteredList.isNotEmpty()){
                    RowListView(
                        selLevel,
                        items = filteredList,
                        navController = navController,
                        label = args.label,
                        lbl = lbl.value,
                        menuCode = args.menuCode,
                        transactionType = args.transactionType,
                        inwardSharedViewModel = inwardSharedViewModel,
                        snackbarController
                    )
                }else if(listIOData.value.isNotEmpty()){
                    LogUtils.showLog("SelLevel", lbl.value ?: "")
                    Text(
                        text = stringResource(R.string.txt_no_data_found,lbl.value),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f),
                        style = CommonTypography.current.noteText,
                        color = RedColor
                    )
                    HorizontalDivider(color = OutlineDefault, thickness = dimensionResource(R.dimen.dp_1))
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

                BottomSheetType.SETTINGS -> {
                    Box(
                        modifier = Modifier
                            .background(WhiteColor)
                            .fillMaxWidth()
                            .wrapContentHeight()
                    ) {
                        DeviceSettings(
                            showSheet = args.showSheet,
                            sheetState = args.sheetState,
                            scope = args.scope,
                            currentSheet = args.currentSheet
                        )
                    }

                }

                BottomSheetType.PARTIAL_COMPLETED -> {
                    val completedHU = selLevel.value!!.completed ?: 0
                    val totalHU = selLevel.value!!.total ?: 0   // change if your total source differs

                    val formatedText = buildAnnotatedString {
                        append("Completed: ")
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("$completedHU/$totalHU")
                        }
                    }
                    PartialCompletionDataSheet(
                        heading = stringResource(R.string.txt_partial_completion_detected),
                        subHeading = formatedText,
                        confirmationMsg = String.format(
                            stringResource(id = R.string.txt_confirmation_msg),
                            selLevel.value!!.label
                        ),
                        buttonText = String.format(
                            stringResource(id = R.string.txt_complete_trip_anyway),
                            selLevel.value!!.label
                        ),
                        showSheet = args.showSheet,
                        sheetState = args.sheetState,
                        scope = args.scope,
                        onClick = {
                            callCompleteAPI(args,selLevel,inwardSharedViewModel,apiViewModel)
                        }
                    )
                }

                BottomSheetType.ACCEPT ->
                    Box(
                        modifier = Modifier
                            .background(WhiteColor)
                            .fillMaxWidth()
                            .fillMaxHeight(0.45f)
                    ) {
                        val completedHU = selLevel.value!!.completed ?: 0
                        val totalHU = selLevel.value!!.total ?: 0   // change if your total source differs

                        val formatedText = buildAnnotatedString {
                            append("Completed: ")
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append("$completedHU/$totalHU")
                            }
                        }
                        AcceptDataSheet(
                            heading = "Complete "+if(selLevel.value==null) "" else selLevel.value!!.label,
                            title = stringResource(R.string.confirm),
                            subHeading = formatedText,
                            primaryButtonText = stringResource(R.string.confirm_and_complete),
                            showSheet = args.showSheet,
                            sheetState = args.sheetState,
                            scope = args.scope,
                            icon =  R.drawable.success_white,
                            color1 = Green,
                            color2 = Green,
                            onClick = {
                                callCompleteAPI(args,selLevel,inwardSharedViewModel,apiViewModel)
                            }
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

fun callCompleteAPI(
    args: InwardScreenArgs,
    selLevel: MutableState<IOLevel?>,
    inwardSharedViewModel: InwardSharedViewModel,
    apiViewModel: ApiViewModel
) {
    try {
        val url = if (args.isInward) UrlConstants.COMPLETE_INWARD_NODE else UrlConstants.COMPLETE_OUTWARD_NODE
        val jsonRequest = JSONObject()
        jsonRequest.put(
            ParameterConstants.CUSTOMER_ID,
            DataStoreManager.readFromPreferences(
                ParameterConstants.CUSTOMER_ID,
                ""
            )
        )
        jsonRequest.put(
            ParameterConstants.BUSINESS_LINE_ID,
            DataStoreManager.readFromPreferences(
                ParameterConstants.BUSINESS_LINE_ID,
                ""
            )
        )
        jsonRequest.put(
            ParameterConstants.LOCATION_ID,
            DataStoreManager.readFromPreferences(
                LoginConstants.DEVICE_LOCATION_ID,
                ""
            )
        )
        jsonRequest.put(
            ParameterConstants.DEVICE_ID,
            DataStoreManager.readFromPreferences(
                ParameterConstants.DEVICE_ID,
                ""
            )
        )
        jsonRequest.put(ParameterConstants.STATUS, StatusConstants.COMPLETED)
        jsonRequest.put(ParameterConstants.FLOW_ID, selLevel.value!!.flowId)
        jsonRequest.put(ParameterConstants.LEVEL, selLevel.value!!.level)
        jsonRequest.put(ParameterConstants.NODE_LEVEL, selLevel.value!!.nodeLevel)
        /*jsonRequest.put(
            ParameterConstants.NODE + selLevel.value!!.nodeLevel,
            selLevel.value!!.node
        )*/
        for (i in 0 until inwardSharedViewModel.inwardLevels.size) {
            val level = inwardSharedViewModel.inwardLevels[i]
            jsonRequest.put(ParameterConstants.NODE + (level.level - i), level.node)
        }
        apiViewModel.callApi(url, jsonRequest = jsonRequest)
    } catch (e: Exception) {
        e.printStackTrace()
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InwardBottomBar(
    apiViewModel: ApiViewModel,
    readerViewModel: ReaderViewModel,
    inwardSharedViewModel: InwardSharedViewModel,
    menuCode: String,
    navController: NavHostController,
    transactionType: String,
    context: Context,
    lbl: MutableState<String>,
    isInward: Boolean,
    selLevel:  MutableState<IOLevel?>,
    args: InwardScreenArgs
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(WhiteColor)
    ) {
        if(selLevel.value !=null && selLevel.value!!.completed >0) {
            HorizontalDivider(color = OutlineDefault)
            Row(modifier = Modifier.padding(dimensionResource(R.dimen.dp_16))) {
                CommonButton(
                    text = String.format(stringResource(id = R.string.complete), selLevel.value?.label),
                    onClick = {

                        if (readerViewModel.isProcessOn().value == true || apiViewModel.isLoading.value == true) return@CommonButton
                        //Call Complete API
                       /** scope.launch {
                            showSheet.value = true   // ✅ OPEN SHEET
                            sheetState.show()
                        }*/
                        if (selLevel.value!!.completed < selLevel.value!!.total){
                            openSheet(
                                scope = args.scope,
                                sheetState = args.sheetState,
                                showSheet = args.showSheet,
                                currentSheet = args.currentSheet,
                                sheet = BottomSheetType.PARTIAL_COMPLETED
                            )
                        }else if (selLevel.value!!.completed == selLevel.value!!.total){
                            openSheet(
                                scope = args.scope,
                                sheetState = args.sheetState,
                                showSheet = args.showSheet,
                                currentSheet = args.currentSheet,
                                sheet = BottomSheetType.ACCEPT
                            )
                        }

                        /**try {
                            val url = if (isInward) UrlConstants.COMPLETE_INWARD_NODE else UrlConstants.COMPLETE_OUTWARD_NODE
                            val jsonRequest = JSONObject()
                            jsonRequest.put(
                                ParameterConstants.CUSTOMER_ID,
                                DataStoreManager.readFromPreferences(
                                    ParameterConstants.CUSTOMER_ID,
                                    ""
                                )
                            )
                            jsonRequest.put(
                                ParameterConstants.BUSINESS_LINE_ID,
                                DataStoreManager.readFromPreferences(
                                    ParameterConstants.BUSINESS_LINE_ID,
                                    ""
                                )
                            )
                            jsonRequest.put(
                                ParameterConstants.LOCATION_ID,
                                DataStoreManager.readFromPreferences(
                                    LoginConstants.DEVICE_LOCATION_ID,
                                    ""
                                )
                            )
                            jsonRequest.put(
                                ParameterConstants.DEVICE_ID,
                                DataStoreManager.readFromPreferences(
                                    ParameterConstants.DEVICE_ID,
                                    ""
                                )
                            )
                            jsonRequest.put(ParameterConstants.STATUS, StatusConstants.COMPLETED)
                            jsonRequest.put(ParameterConstants.FLOW_ID, selLevel.flowId)
                            jsonRequest.put(ParameterConstants.LEVEL, selLevel.level)
                            jsonRequest.put(ParameterConstants.NODE_LEVEL, selLevel.nodeLevel)
                            jsonRequest.put(
                                ParameterConstants.NODE + selLevel.nodeLevel,
                                selLevel.node
                            )
                            apiViewModel.callApi(url, jsonRequest = jsonRequest)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }*/
                    },
                    modifier = Modifier.fillMaxWidth(),
                    contentColor = WhiteColor,
                )
            }
        }
    }
}


data class RowItem(
    val title: String,
    val totalHu: Int,
    val pendingCount: Int
)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RowListView(
    selLevel: MutableState<IOLevel?>,
    items: List<IOLevel>,
    navController: NavHostController,
    label: String,
    lbl: String,
    menuCode: String,
    transactionType: String,
    inwardSharedViewModel: InwardSharedViewModel,
    snackBarController: SnackbarController
) {
    val isLevel4 = (if(selLevel.value==null) 0 else chkNull(selLevel.value!!.nodeLevel,0)) == 2
    LazyColumn {
        // 🔹 HEADER ROW
        item {
         InwardHeaderRow(isLevel4,lbl)
        }

        items(items) { item ->

                InwardRowItem(
                    item = item,
                    isLevel4 = isLevel4,
                    onClick = {
                        if (item.status == StatusConstants.COMPLETED) {
                            snackBarController.show(ErrorAppSnackBarData("Already Completed. Cannot proceed"))
                        } else {
                            inwardSharedViewModel.addInwardLevel(item, menuCode)
                            /*val screen = Screen.DynamicDashboard(
                                code = menuCode,
                                label = label,
                                params = emptyMap()
                            )
                            val route = screen.createRoute(parentCode = "")*/
                            if (item.nodeLevel <= 1) {
                                navController.navigate(Screen.InwardDetailsScreen.createRoute(code = menuCode, label = label))
                                //navController.navigate(route)
                            } else {
                                navigateMenu(navController,menuCode,label)
                                //navController.navigate(route)
                            }
                        }
                    }
                )

            /*val status = chkNull(item.status,if(item.completed == item.total) StatusConstants.COMPLETED else if(item.completed>0 && item.completed<item.total) StatusConstants.INPROCESS else StatusConstants.PENDING)
            RowListViewWithTabIcon(
                title = item.node,
                subTitle = "Total: ${item.total}",
                actionText = stringResource(R.string.pending, item.total),
                onNextClick = {
                    inwardSharedViewModel.addInwardLevel(item, menuCode)
                    navController.navigate(if (item.nodeLevel<=1) "inwardDetailScreen/$label/$menuCode/$transactionType" else "inwardHome/$label/$menuCode/$transactionType")
                }
            )*/

            HorizontalDivider(color = OutlineDefault, thickness = dimensionResource(R.dimen.dp_1), modifier = Modifier.padding(vertical = dimensionResource(R.dimen.dp_16)))
        }
    }
}

@Composable
private fun HeaderText1(
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
fun InwardRowItem(
    item: IOLevel,
    isLevel4: Boolean,
    onClick: () -> Unit
) {

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Column 1 → Node
            Text(
                text = item.node,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
                style = CommonTypography.current.noteText,
                color = BlackColor
            )

            // Column 2
            Text(
                textAlign = TextAlign.Center,
                text = if(isLevel4) item.total.toString() else "${item.completed}/${item.total}",
                modifier = Modifier.weight(1f),
                style = CommonTypography.current.textSemiBold,
                color = Color.Gray
            )

            // Column 3 → only for level 4
            if (isLevel4) {
                Text(
                    textAlign = TextAlign.Center,
                    text = item.completed.toString(),
                    modifier = Modifier.weight(1f),
                    style = CommonTypography.current.textSemiBold,
                    color = Color.Gray
                )
            }

            // Column 4 → Status
            Row(
                modifier = Modifier
                    .weight(1f)
                    /*.background(
                        if (item.status.equals(
                                StatusConstants.COMPLETED,
                                true
                            )
                        ) GreenBorder else if (item.completed > 0) LightYellow else ErrorBgColor,
                        RoundedCornerShape(dimensionResource(id = R.dimen.dp_24))
                    )
                    .padding(
                        horizontal = dimensionResource(id = R.dimen.dp_4),
                        vertical = dimensionResource(id = R.dimen.dp_4)
                    )*/,
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
            ) {

                val badgeText = item.status.ifEmpty { if (item.status.equals(StatusConstants.COMPLETED,true)) StatusConstants.COMPLETED else if (item.completed > 0) StatusConstants.INPROCESS else StatusConstants.PENDING }
                val txtColor = if (item.status.equals(StatusConstants.COMPLETED,true)) Green else if (item.completed > 0) Yellow else RedColor
                val bgColor = if (item.status.equals(StatusConstants.COMPLETED,true)) CardGreen else if (item.completed > 0) LightYellow else ErrorBgColor
                InventoryBadge(
                    text = badgeText,
                    backGroundColor = bgColor,//if(isFound) CardGreen else ErrorBgColor,
                    textColor = txtColor,//if(isFound) Green else RedColor,
                    iconColor = txtColor,//if(isFound) Green else RedColor,
                    textStyle = CommonTypography.current.smallTxt,
                    padding = 0.dp
                )
                /*Box(
                    modifier = Modifier
                        .size(dimensionResource(R.dimen.dp_8))
                        .clip(CircleShape)
                        .background(
                            if (item.status.equals(
                                    StatusConstants.COMPLETED,
                                    true
                                )
                            ) Green else if (item.completed > 0) Yellow else RedColor
                        )
                )

                Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.dp_5)))

                Text(
                    text = item.status.ifEmpty { if (item.status.equals(StatusConstants.COMPLETED,true)) StatusConstants.COMPLETED else if (item.completed > 0) StatusConstants.INPROCESS else StatusConstants.PENDING },
                    style = CommonTypography.current.smallTxt,
                    color = if (item.status.equals(StatusConstants.COMPLETED,true)) Green else if (item.completed > 0) Yellow else RedColor,
                    modifier = Modifier.basicMarquee()
                )*/
            }

                Icon(
                    painter = painterResource(R.drawable.icon__next),
                    contentDescription = null,
                    modifier = Modifier.clickable(
                        onClick = onClick
                    )
                )
        }

        /*HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
            thickness = 0.6.dp,
            color = OutlineDefault
        )*/
    }
}

@Composable
fun InwardHeaderRow(isLevel4: Boolean, label: String) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp)
            .padding(top = 10.dp, bottom = 10.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFF4F5F7)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ) {

        Text(
            text = label,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            style = CommonTypography.current.noteText,
            color = BlackColor
        )

        if (isLevel4) {
            HeaderText1(
                text = "Exp. Qty.",
                modifier = Modifier.weight(1f),
                align = TextAlign.Center
            )
        }


        HeaderText1(
            text = if (isLevel4) "Scan Qty." else "Completed",
            modifier = Modifier.weight(1f),
            align = TextAlign.Center
        )

        HeaderText1(
            text = "Status",
            modifier = Modifier.weight(1f),
            align = TextAlign.Center
        )
    }
}