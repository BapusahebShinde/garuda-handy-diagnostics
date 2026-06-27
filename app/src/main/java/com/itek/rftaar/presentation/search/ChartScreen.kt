package com.itek.rftaar.presentation.search

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController
import com.itek.rftaar.R
import com.itek.rftaar.api.constants.ParameterConstants
import com.itek.rftaar.api.constants.UrlConstants
import com.itek.rftaar.core.common.constants.LoginConstants
import com.itek.rftaar.core.common.constants.MenuConstants
import com.itek.rftaar.core.common.utils.LogUtils
import com.itek.rftaar.core.common.utils.ParseUtils
import com.itek.rftaar.core.common.utils.ParseUtils.extractString
import com.itek.rftaar.core.common.utils.ToastUtils
import com.itek.rftaar.core.database.AppDatabase
import com.itek.rftaar.core.database.DataStoreManager
import com.itek.rftaar.data.entity.MenuEntity
import com.itek.rftaar.data.model.BarcodeZoneQtyFields
import com.itek.rftaar.domain.model.LocationModel
import com.itek.rftaar.mqtt.constants.TopicConstants
import com.itek.rftaar.presentation.commonComp.AppSnackBar
import com.itek.rftaar.presentation.commonComp.BottomSheetHeader
import com.itek.rftaar.presentation.commonComp.BottomSheetType
import com.itek.rftaar.presentation.commonComp.CounterText
import com.itek.rftaar.presentation.commonComp.ErrorAppSnackBarData
import com.itek.rftaar.presentation.commonComp.ItekFooter
import com.itek.rftaar.presentation.commonComp.SnackbarController
import com.itek.rftaar.presentation.commonComp.TopBarContent
import com.itek.rftaar.presentation.commonComp.openSheet
import com.itek.rftaar.presentation.dashBoard.navigateMenu
import com.itek.rftaar.presentation.viewmodel.ApiViewModel
import com.itek.rftaar.ui.theme.BlackColor
import com.itek.rftaar.ui.theme.CommonTypography
import com.itek.rftaar.ui.theme.OutlineDefault
import com.itek.rftaar.ui.theme.TabColor
import com.itek.rftaar.ui.theme.WhiteColor
import com.itek.rftaar.utils.CommonUtils.getMenuIconByCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.json.JSONArray

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ChartScreen(
    modifier: Modifier,
    navController: NavHostController,
    menuCode: String,
    label: String,
    searchParams: Map<String, Any>,
    apiViewModel: ApiViewModel = hiltViewModel()
) {

    val scope = rememberCoroutineScope()
    BackHandler(enabled = true)  {
        scope.launch {
            if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                    navController.popBackStack()
                }
        }
    }

    val topic = extractString(searchParams,"topic", TopicConstants.SEARCH)
    val code = extractString(searchParams,"menuCode", menuCode)
    val transactionType = extractString(searchParams,"transactionType", "")
    val barcode = extractString(searchParams, ParameterConstants.BARCODE,"")
    val filters = ParseUtils.extractStringList(searchParams,"selFilterKeys",emptyList())

    LogUtils.showLog("Chart_params",searchParams.toString())
    LogUtils.showLog("Chart_barcode",barcode)
    LogUtils.showLog("Chart_filters",filters.toString())

    if(filters.isNullOrEmpty() || filters.size<2){
        //return with error if filters are empty or size < 2
        ToastUtils.showLongToast("Invalid Number of Filters!! Need 2 Filters!")
        if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED)
            navController.popBackStack()
    }

    val filterKey1 = filters.get(0)
    val filterKey2 = filters.get(1)

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { sheetValue ->
            sheetValue != SheetValue.Hidden
        }
    )
    val showSheet = remember { mutableStateOf(false) }
    val currentSheet = remember { mutableStateOf(BottomSheetType.NONE) }

    val chartList = remember { mutableStateOf<List<BarcodeZoneQtyFields>>(emptyList()) }
    LogUtils.showLog("chartList", "ChartScreen: ${chartList.value} ")
    val selectedObj = remember { mutableStateOf(BarcodeZoneQtyFields()) }
    val snackbarController = remember { SnackbarController() }

    val totalCount = remember { mutableStateOf(0) }

    val context = LocalContext.current
    val menus = AppDatabase.getDbInstance(context).menuDao().getMenusByCodes(arrayOf(MenuConstants.PRODUCT_SEARCH,MenuConstants.MOVE_STOCK)).collectAsState(initial = emptyList())


    LaunchedEffect(Unit) {
        val map = HashMap<String,String>()
        if(barcode.isNotEmpty()) map.put(ParameterConstants.EAN,barcode)
        map.put(ParameterConstants.CUSTOMER_ID,DataStoreManager.readFromPreferences(ParameterConstants.CUSTOMER_ID,""))
        map.put(ParameterConstants.BUSINESS_LINE_ID,DataStoreManager.readFromPreferences(ParameterConstants.BUSINESS_LINE_ID,""))
        map.put(ParameterConstants.LOCATION_ID,DataStoreManager.readFromPreferences(LoginConstants.DEVICE_LOCATION_ID,""))
        if(filters.isNotEmpty() && filters.size>=2) {
            map.put(ParameterConstants.DISPLAY_VALUE_STYLE_PARAMETER_1, filterKey1)
            map.put(ParameterConstants.DISPLAY_VALUE_STYLE_PARAMETER_2, filterKey2)
        }
        apiViewModel.callApi(UrlConstants.PRODUCT_CHART, queryMap = map)
    }

    Scaffold(
        bottomBar = {
            ChartScreenBottomBar()
        }
    ){ innerPadding ->

        Box(
            modifier = modifier
                .background(WhiteColor)
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            ChartScreenContent(totalCount,chartList, selectedObj, navController,menuCode, label,transactionType,topic, scope = scope, sheetState = sheetState, showSheet = showSheet, currentSheet = currentSheet, apiViewModel =apiViewModel, snackbarController, filterKey1, filterKey2, menus)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartScreenContent(
    totalCount: MutableState<Int>,
    chartList: MutableState<List<BarcodeZoneQtyFields>>,
    selectedObj: MutableState<BarcodeZoneQtyFields>,
    navController: NavHostController,
    menuCode: String,
    label: String,
    transactionType: String = "",
    topic: String = "",
    scope: CoroutineScope,
    sheetState: SheetState,
    showSheet: MutableState<Boolean>,
    currentSheet: MutableState<BottomSheetType>,
    apiViewModel: ApiViewModel,
    snackbarController: SnackbarController,
    filterKey1: String,
    filterKey2: String,
    menus: State<List<MenuEntity>>
) {


    LogUtils.showLog("chartList**", "ChartScreen: ${chartList.value} ")

    val response = apiViewModel.apiResult.collectAsState(null)
    LaunchedEffect(response.value) {
        LogUtils.showLog("response", "ChartScreen:1  ")
        // Temp code for to be replaced after APi integration
        val result = response.value ?: return@LaunchedEffect
        if (result?.isSuccess!=true) {
            val message = result?.errMsg
            if (!message.isNullOrBlank()) {
                snackbarController.show(
                    ErrorAppSnackBarData(result?.errMsg.toString())
                )
            }
            LogUtils.showLog("response", "ChartScreen: 2")

            //temp code
            /**if(BaseUtils.isDebuggable()) {
                when (result.url) {
                    UrlConstants.PRODUCT_CHART -> {
                        LogUtils.showLog("response", "ChartScreen: 2")
                        val request = result.jsonRequest
                        val filter1Key = ParseUtils.extractString(request, ParameterConstants.DISPLAY_VALUE_STYLE_PARAMETER_1, "")
                        val filter2Key = ParseUtils.extractString(request, ParameterConstants.DISPLAY_VALUE_STYLE_PARAMETER_2, "")
                        LogUtils.showLog(ParameterConstants.DISPLAY_VALUE_STYLE_PARAMETER_1, filter1Key)
                        LogUtils.showLog(ParameterConstants.DISPLAY_VALUE_STYLE_PARAMETER_2, filter2Key)

                        val response = JsonUtils.getSampleJSON(UrlConstants.PRODUCT_CHART)
                        val responseArray = ParseUtils.extractJSONArray(response, ParameterConstants.DATA, JSONArray())

                        val listChart = ArrayList<BarcodeZoneQtyFields>()
                        var totalQty = 0
                        if (responseArray.length() > 0) {
                            for (i in 0 until responseArray.length()) {
                                val obj = responseArray.getJSONObject(i)
                                val chart = BarcodeZoneQtyFields()//Gson().fromJson(obj.toString(), BarcodeZoneQtyFields::class.java)
                                chart.barcode = extractString(obj, DataStoreManager.getBarcodeLabel(), ParseUtils.extractString(obj, ParameterConstants.BARCODE, ParseUtils.extractString(obj, ParameterConstants.EAN, "")))
                                chart.name = extractString(obj, ParameterConstants.ASSET_LOCATION_NAME, "")
                                chart.path = extractString(obj, ParameterConstants.ASSET_LOCATION_PATH, "")
                                chart.totalQty = ParseUtils.extractInt(obj, ParameterConstants.QTY, ParseUtils.extractInt(obj, ParameterConstants.STOCK_QTY, ParseUtils.extractInt(obj, ParameterConstants.TOTAL_QTY, 0)))
                                chart.field1 = extractString(obj, filter1Key, extractString(obj, ParameterConstants.DISPLAY_VALUE_STYLE_PARAMETER_1, ""))
                                chart.field2 = extractString(obj, filter2Key, extractString(obj, ParameterConstants.DISPLAY_VALUE_STYLE_PARAMETER_2, ""))
                                LogUtils.showLog("chart", "ChartScreen: $chart" + "->" + chart.isValid())
                                if (chart.isValid()) {
                                    totalQty += chart.totalQty
                                    listChart.add(chart)
                                }
                            }
                            if (listChart.isNotEmpty()) {
                                totalCount.value = totalQty
                                chartList.value = listChart
                            }
                        }
                        LogUtils.showLog("response", "ChartScreen: $response")
                    }
                }
            }*/

        }
        else if (response.value != null) {
            when (result.url) {
                UrlConstants.PRODUCT_CHART -> {
                    val request = result.jsonRequest
                    val filter1Key =
                        extractString(request ,ParameterConstants.DISPLAY_VALUE_STYLE_PARAMETER_1,"")
                    val filter2Key =
                        extractString(request ,ParameterConstants.DISPLAY_VALUE_STYLE_PARAMETER_2,"")
                    val response = result.response//JsonUtils.getSampleJSON("filter_mapping")
                    val responseArray = ParseUtils.extractJSONArray(response, ParameterConstants.DATA, JSONArray())

                    val listChart = ArrayList<BarcodeZoneQtyFields>()
                    var totalQty = 0
                    if (responseArray.length()>0){
                        for (i in 0 until responseArray.length()) {
                            val obj = responseArray.getJSONObject(i)
                            val chart = BarcodeZoneQtyFields()//Gson().fromJson(obj.toString(), BarcodeZoneQtyFields::class.java)
                            chart.barcode =
                                extractString(obj , DataStoreManager.getBarcodeLabel(),
                                    extractString(obj , ParameterConstants.BARCODE,
                                        extractString(obj , ParameterConstants.EAN,"")
                                    )
                                )
                            chart.name = extractString(obj, ParameterConstants.ASSET_LOCATION_NAME,"")
                            chart.path = extractString(obj, ParameterConstants.ASSET_LOCATION_PATH,"")
                            chart.totalQty = ParseUtils.extractInt(obj , ParameterConstants.QTY,ParseUtils.extractInt(obj , ParameterConstants.STOCK_QTY,ParseUtils.extractInt(obj , ParameterConstants.TOTAL_QTY,0)))
                            chart.field1 = extractString(obj ,filter1Key,"")
                            chart.field2 = extractString(obj ,filter2Key,"")
                            LogUtils.showLog("chart", "ChartScreen: $chart"+"->"+chart.isValid())
                            if (chart.isValid()) {
                                totalQty += chart.totalQty
                                listChart.add(chart)
                            }
                        }
                        if (listChart.isNotEmpty()) {
                            totalCount.value = totalQty
                            chartList.value = listChart
                        }
                    }
                    LogUtils.showLog("response##", "ChartScreen: $response")
                }
            }
        }
    }


    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(modifier = Modifier, verticalArrangement = Arrangement.Top) {
                TopBarContent(
                    label = "$filterKey1 & $filterKey2 Chart",
                    onBackClickL = {
                        scope.launch {
                        if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                            navController.popBackStack()
                        }
                    }
                    }, onSettingClick = {

                    },
                    isSetting = false
                )
            }
            HorizontalDivider(thickness = dimensionResource(R.dimen.dp_1), color = OutlineDefault)

            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {

                CounterText(
                    current = totalCount.value,
                    total = 0.toString(),
                    isLimitShow = false
                )

                Text(
                    text = stringResource(id = R.string.total_items_available),
                    style = CommonTypography.current.noteText,
                    color = BlackColor,
                    modifier = Modifier.padding(bottom = dimensionResource(R.dimen.dp_12))
                )

                InventoryTable(filterKey1, filterKey2, items = chartList.value, onItemClick = { item ->
                    if(menus.value.isNullOrEmpty()) return@InventoryTable
                    selectedObj.value = item
                    openSheet(
                        scope = scope,
                        sheetState = sheetState,
                        showSheet = showSheet,
                        currentSheet = currentSheet,
                        sheet = BottomSheetType.CHART_OPTIONS
                    )
                })
            }
        }

        if (showSheet.value ) {
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
                    BottomSheetType.CHART_OPTIONS -> Box(
                        modifier = Modifier
                            .background(WhiteColor)
                            .fillMaxWidth()
                            .fillMaxHeight(0.30f)
                    ) {
                        ChartScreenMenuOptions(
                            title = "Select Action",
                            showSheet = showSheet,
                            sheetState = sheetState,
                            scope = scope,
                            selectedObj = selectedObj,
                            menus = menus,
                            navController,
                            menuCode,
                            label,
                            transactionType,
                            topic
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
fun ChartScreenBottomBar() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(WhiteColor)
    ) {
        HorizontalDivider(color = OutlineDefault)

        Row(
            modifier = Modifier.padding(top =dimensionResource(R.dimen.dp_16)),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.dp_10)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ItekFooter()
        }
    }
}

data class InventoryItem(
    val color: String,
    val size: String,
    val quantity: Int,
    val zone: String
)

@Composable
fun InventoryTable(filterLabel1:String,filterLabel2:String,items: List<BarcodeZoneQtyFields>, onItemClick: (BarcodeZoneQtyFields) -> Unit) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .fillMaxWidth()
            .padding(16.dp)
            .background(Color.White)
    ) {
        // --- Header ---
        InventoryRow(
            color = filterLabel1,
            size = filterLabel2,
            qty = "Qty",
            zone = "Zone",
            isHeader = true,
            onClick = {}
        )

        // --- Data Rows ---
        items.forEach { item ->
            HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
            InventoryRow(
                color = item.field1,
                size = item.field2,
                qty = item.totalQty.toString(),
                zone = item.name,
                isHeader = false,
                onClick = { onItemClick(item) }
            )
        }
    }
}

@Composable
fun InventoryRow(
    color: String,
    size: String,
    qty: String,
    zone: String,
    isHeader: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isHeader) Color(0xFFF5F5F5) else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .then(
                if (!isHeader) Modifier.clickable { onClick() }
                else Modifier
            )
            .padding(vertical = 16.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(text = color, modifier = Modifier.weight(1.5f).basicMarquee(), style = CommonTypography.current.noteText, color = BlackColor, textAlign = TextAlign.Center)
        Text(text = size, modifier = Modifier.weight(1f).basicMarquee(), style = CommonTypography.current.noteText, color = BlackColor, textAlign = TextAlign.Center )
        Text(text = qty, modifier = Modifier.weight(1f).basicMarquee(), style = CommonTypography.current.noteText , fontWeight = FontWeight.Normal, textAlign = TextAlign.Center)
        Text(text = zone, modifier = Modifier.weight(1f).basicMarquee(), style = CommonTypography.current.noteText , fontWeight = FontWeight.Normal, textAlign = TextAlign.Center)

        // Chevron Icon (Only for data rows)
        if (!isHeader) {
            Icon(
                painter = painterResource(R.drawable.icon__next),
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        } else {
            Spacer(modifier = Modifier.size(20.dp)) // Maintain alignment
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartScreenMenuOptions(
    title: String,
    showSheet: MutableState<Boolean>,
    sheetState: SheetState,
    scope: CoroutineScope,
    selectedObj: MutableState<BarcodeZoneQtyFields>,
    menus: State<List<MenuEntity>>,
    navController: NavHostController,
    menuCode: String,
    label: String,
    transactionType: String,
    topic: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(
                color = WhiteColor,
                shape = RoundedCornerShape(
                    topStart = dimensionResource(R.dimen.dp_24),
                    topEnd = dimensionResource(R.dimen.dp_24)
                )
            )
            .padding(dimensionResource(R.dimen.dp_16)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        BottomSheetHeader(title = title) {
            scope.launch {
                sheetState.hide()
                showSheet.value = false
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp) // Adds space between the cards
        ) {
            //instead of HardCoding Pass List of MenuModels
            LogUtils.showLog("menus",menus.value.toString())
            for(menu in menus.value){
                MenuCard(
                    iconRes = getMenuIconByCode(menu.code),
                    label = menu.label,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        val srcZone = LocationModel(name=selectedObj.value.name, path = selectedObj.value.path)
                        val searchBundle = mapOf(
                            "textValue" to selectedObj.value.barcode,
                            srcZone::class.java.simpleName to srcZone,
                        )
                        navigateMenu(navController,menu,searchBundle)
                        /*val dashboard = Screen.DynamicDashboard(
                            code = menu.code,
                            label = menu.label,
                            params = searchBundle
                        )
                        navController.navigate(dashboard.createRoute(menu.parentCode))*/
                        /*val route = Screen.ProductSearch.createRoute(code = MenuConstants.PRODUCT_SEARCH, label = "Product Search", params = searchBundle)
                        navController.navigate(route)*/
                    }
                )
            }
            /*// --- Product Search Card ---
            MenuCard(
                iconRes = R.drawable.product_search,
                label = "Product Search",
                modifier = Modifier.weight(1f),
                onClick = {
                    val srcZone = LocationModel(name=selectedObj.value.name, path = selectedObj.value.path)
                    val searchBundle = mapOf(
                        "textValue" to selectedObj.value.barcode,
                        srcZone::class.java.simpleName to srcZone,
                    )
                    val route = Screen.ProductSearch.createRoute(code = MenuConstants.PRODUCT_SEARCH, label = "Product Search", params = searchBundle)
                    navController.navigate(route)
                }
            )

            // --- Product Movement Card ---
            MenuCard(
                iconRes = R.drawable.ic_mov,
                label = "Movement", // Fixed duplicate text
                modifier = Modifier.weight(1f),
                onClick = {
                    val srcZone = LocationModel(name=selectedObj.value.name, path = selectedObj.value.path)
                    val searchBundle = mapOf(
                        "textValue" to selectedObj.value.barcode,
                        srcZone::class.java.simpleName to srcZone
                    )
                    val route = Screen.MovementHomeScreen.createRoute(code = MenuConstants.MOVE_STOCK, label = "Movement", params = searchBundle)
                    navController.navigate(route)
                }
            )*/
        }
    }
}

@Composable
fun MenuCard(
    @DrawableRes iconRes: Int,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = TabColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp) // Internal padding for content
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = label,
                modifier = Modifier.size(32.dp),
                tint = Color.Unspecified // Keeps original icon colors
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}


