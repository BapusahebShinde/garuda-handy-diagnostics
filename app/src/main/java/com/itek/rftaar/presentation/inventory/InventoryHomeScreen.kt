package com.itek.rftaar.presentation.inventory

import android.annotation.SuppressLint
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController
import com.itek.rftaar.DataHolder
import com.itek.rftaar.R
import com.itek.rftaar.api.constants.ParameterConstants
import com.itek.rftaar.api.constants.UrlConstants
import com.itek.rftaar.core.common.constants.DateFormatUtils
import com.itek.rftaar.core.common.constants.LoginConstants
import com.itek.rftaar.core.common.utils.LogUtils
import com.itek.rftaar.core.common.utils.ParseUtils
import com.itek.rftaar.core.database.AppDatabase
import com.itek.rftaar.core.database.DataStoreManager
import com.itek.rftaar.data.entity.MenuEntity
import com.itek.rftaar.data.model.ZoneStockQty
import com.itek.rftaar.presentation.commonComp.AppSnackBar
import com.itek.rftaar.presentation.commonComp.BottomSheetSubMenu
import com.itek.rftaar.presentation.commonComp.BottomSheetType
import com.itek.rftaar.presentation.commonComp.CircularIcon
import com.itek.rftaar.presentation.commonComp.DeviceSettingItem
import com.itek.rftaar.presentation.commonComp.ErrorAppSnackBarData
import com.itek.rftaar.presentation.commonComp.GenericIconCard
import com.itek.rftaar.presentation.commonComp.IconText
import com.itek.rftaar.presentation.commonComp.ItekFooter
import com.itek.rftaar.presentation.commonComp.SnackbarController
import com.itek.rftaar.presentation.commonComp.StatItem
import com.itek.rftaar.presentation.commonComp.StatsCard
import com.itek.rftaar.presentation.commonComp.StockItem
import com.itek.rftaar.presentation.commonComp.openSheet
import com.itek.rftaar.presentation.dashBoard.navigateMenu
import com.itek.rftaar.presentation.viewmodel.ApiViewModel
import com.itek.rftaar.ui.theme.BackGround
import com.itek.rftaar.ui.theme.BlackColor
import com.itek.rftaar.ui.theme.CommonTypography
import com.itek.rftaar.ui.theme.TabColor
import com.itek.rftaar.ui.theme.WhiteColor
import com.itek.rftaar.ui.theme.Yellow
import com.itek.rftaar.utils.CommonUtils.getMenuIconByCode
import kotlinx.coroutines.CoroutineScope
import org.json.JSONArray
import org.json.JSONObject

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun InventoryHomeScreen(
    modifier: Modifier,
    navController: NavHostController,
    label: String,
    code: String,
    apiViewModel: ApiViewModel = hiltViewModel(),
    searchParams: Map<String, Any> = emptyMap()
) {
    
    BackHandler(enabled = true) {
        if(navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED)
        navController.popBackStack()
    }
    LaunchedEffect(Unit) {
        val map = HashMap<String,String>()
        map.put(ParameterConstants.CUSTOMER_ID,DataStoreManager.readFromPreferences(ParameterConstants.CUSTOMER_ID,""))
        map.put(ParameterConstants.BUSINESS_LINE_ID,DataStoreManager.readFromPreferences(ParameterConstants.BUSINESS_LINE_ID, ""))
        map.put(ParameterConstants.OPERATION_LOCATION_ID,DataStoreManager.readFromPreferences(LoginConstants.DEVICE_LOCATION_ID, ""))
        apiViewModel.callApi(UrlConstants.GET_STOCK_COUNT, queryMap = map)
    }
    Scaffold(
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BackGround),
                contentAlignment = Alignment.BottomCenter
            ) {
                InventoryBottomBar()
            }
        },
        modifier = modifier.background(BackGround)
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .background(BackGround)
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            InventoryScreenContent(navController,code,label,apiViewModel)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreenContent(navController: NavHostController, code: String, label: String, apiViewModel: ApiViewModel) {
    //val cardWidth = (LocalConfiguration.current.screenWidthDp.dp - 64.dp) / 3
    val context = LocalContext.current
    val db = AppDatabase.getDbInstance(context)
    val subMenus = db.menuDao().getSubMenus(code).collectAsState(initial = emptyList())
    //val activeSessionList = db.tagInfoDao().getTopicWiseActiveSessionList(topic = TopicConstants.INVENTORY).collectAsState(initial = emptyList())
    LogUtils.showLog("code =", "InventoryScreenContent: $code")

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { sheetValue ->
            sheetValue != SheetValue.Hidden
        }
    )
    val showSheet = remember { mutableStateOf(false) }
    val currentSheet = remember { mutableStateOf(BottomSheetType.NONE) }
    val scope = rememberCoroutineScope()
    val selectedMenu = remember { mutableStateOf<MenuEntity?>(null) }

    val snackbarController = remember { SnackbarController() }
    val stockCount = remember { mutableStateOf(0) }
    val discrepancyCount = remember { mutableStateOf(0) }
    val totalCount = remember { mutableStateOf(0) }
    val lastInventoryDate = remember { mutableStateOf("") }
    val lastInventoryType = remember { mutableStateOf("") }
    val lastInventoryExpectedCount = remember { mutableStateOf(0) }
    val lastInventoryValidCount = remember { mutableStateOf(0) }
    val lastInventoryInvalidCount = remember { mutableStateOf(0) }
    val isExpand = remember { mutableStateOf(false) }
    val stockListState = remember { mutableStateOf<List<StockItem>>(emptyList()) }


    LaunchedEffect(currentSheet.value) {
        if (currentSheet.value == BottomSheetType.SUB_MENUS) {
            sheetState.expand()
        }
    }
    LaunchedEffect(subMenus.value) {
        if (subMenus.value.isNotEmpty() && selectedMenu.value == null) {
            selectedMenu.value = subMenus.value.first()
        }
    }

    val response = apiViewModel.apiResult.collectAsState(initial = null)
    LaunchedEffect(response.value) {
        if(response.value==null) return@LaunchedEffect
        val result = response.value
        if (result?.isSuccess != true) {
            val message = result?.errMsg
            if (!message.isNullOrBlank()) {
                snackbarController.show(
                    ErrorAppSnackBarData(result.errMsg.toString())
                )
            }
        } else if (result.response != null) {
            //Condition for Expand and Collapse
            isExpand.value = false

            val jsonResponse = result.response
            when (result.url) {
                UrlConstants.GET_STOCK_COUNT -> {
                    val data = ParseUtils.extractJSONObject(jsonResponse, ParameterConstants.DATA, JSONObject())
                    stockCount.value = ParseUtils.extractInt(data,"stockCount",0)
                    discrepancyCount.value = ParseUtils.extractInt(data,"discrepancyCount",0)
                    totalCount.value = ParseUtils.extractInt(data,"totalCount",0)
                    lastInventoryDate.value = ParseUtils.extractString(data,"lastInventory","")//2025-12-28T05:10:22.276Z")
                    lastInventoryType.value = ParseUtils.extractString(data,"lastInventoryType","")//""Inventory")
                    lastInventoryValidCount.value = ParseUtils.extractInt(data,"lastInventoryValidCount",0)
                    lastInventoryInvalidCount.value = ParseUtils.extractInt(data,"lastInventoryInvalidCount",0)
                    lastInventoryExpectedCount.value = ParseUtils.extractInt(data,"lastInventoryExpectedCount",0)

                    val stockArray = data.optJSONArray("stock") ?: JSONArray()

                    val tempList = mutableListOf<StockItem>()
                    val tempZoneQtyList = mutableListOf<ZoneStockQty>()
                    for (i in 0 until stockArray.length()) {
                        val obj = stockArray.optJSONObject(i) ?: continue
                        val locationName = obj.optString("assetLocationName").ifEmpty { "-" }
                        val locationPath = obj.optString("assetLocationPath").ifEmpty { "-" }
                        val stockCount = obj.optInt("stockCount", 0)
                        val discrepancy = obj.optInt("stockDiscrepancy", 0)
                        val customFieldWiseCountObject = obj.optJSONObject("customFieldWiseCount") ?: JSONObject()

                        val customFieldMap = mutableMapOf<String, Int>()
                        val keys = customFieldWiseCountObject.keys()
                        while (keys.hasNext()) {
                            val key = keys.next()
                            val value = customFieldWiseCountObject.optInt(key, 0)
                            customFieldMap[key] = value
                        }

                        val zoneStockQty= ZoneStockQty(locationName,locationPath,stockCount,discrepancy,customFieldMap)
                        if(zoneStockQty.name.isNotEmpty() && zoneStockQty.path.isNotEmpty() && zoneStockQty.stockCount>0) tempZoneQtyList.add(zoneStockQty)

                        tempList.add(
                            StockItem(
                                location = locationName,
                                scannedStock = stockCount.toString(),
                                correction = discrepancy.toString()
                            )
                        )
                    }

                    stockListState.value = tempList
                    DataHolder.zoneWiseStockQtyList.clear()
                    DataHolder.zoneWiseStockQtyList.addAll(tempZoneQtyList)
                }
            }
        }
    }


    Box {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(dimensionResource(id = R.dimen.dp_16))
                .clickable(
                    onClick = {},
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                CircularIcon(
                    iconRes = R.drawable.property_arrow_back,
                    modifier = Modifier.clickable(
                        onClick = {
                            if(navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED)
                            navController.popBackStack()
                        },
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    )

                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = dimensionResource(R.dimen.dp_10),
                        bottom = dimensionResource(R.dimen.dp_16)
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Text(
                    text = label,
                    style = CommonTypography.current.headingH1,
                    textAlign = TextAlign.Start
                )

                IconText(
                    icon = painterResource(R.drawable.property_update),
                    actionText = stringResource(R.string.refresh_now),
                    tintColor = BlackColor,
                    onActionClick = {
                        val map = HashMap<String,String>()
                        map.put(ParameterConstants.CUSTOMER_ID,DataStoreManager.readFromPreferences(ParameterConstants.CUSTOMER_ID,""))
                        map.put(ParameterConstants.BUSINESS_LINE_ID,DataStoreManager.readFromPreferences(ParameterConstants.BUSINESS_LINE_ID, ""))
                        map.put(ParameterConstants.OPERATION_LOCATION_ID,DataStoreManager.readFromPreferences(LoginConstants.DEVICE_LOCATION_ID, ""))
                        apiViewModel.callApi(UrlConstants.GET_STOCK_COUNT, queryMap = map)
                    },
                    elevation = 0.dp,
                    backGroundColor = TabColor,
                    textStyle = CommonTypography.current.smallTxt
                )
            }

            StatsCard(
                items = listOf(
                    StatItem(
                        value = stockCount.value.toString(),
                        label = stringResource(R.string.scanned_stock)
                    ),
                    StatItem(
                        value = discrepancyCount.value.toString(),
                        label = stringResource(R.string.stock_correction)
                    ),
                    StatItem(
                        value = totalCount.value.toString(),
                        label = stringResource(R.string.total_stock),
                        valueColor = Yellow
                    )
                ),
                stockList = stockListState.value,
                isSemiCircle = stockListState.value.isNotEmpty(),
                isExpanded = isExpand.value,
                onExpandChange = { newState -> isExpand.value = newState }
            )

            if (stockListState.value.isNotEmpty()){
                if (isExpand.value){
                    Image(painter = painterResource(R.drawable.icon_arrow_up),
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .clickable() {
                                isExpand.value = false
                            })
                }else{
                    Image(painter = painterResource(R.drawable.icon_arrow_down),
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .clickable() {
                                isExpand.value = true
                            })
                }
            }


            Spacer(modifier = Modifier.padding(dimensionResource(R.dimen.dp_16)))
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                maxItemsInEachRow = 3
            ) {

                subMenus.value.forEach { menu ->
                    val activeMenuSession = db.tagInfoDao().hasActiveMenuSession(menuCode = menu.code).observeAsState(initial = false)
                    val subMenus = db.menuDao().getSubMenus(menu.code).collectAsState(initial = emptyList())
                    GenericIconCard(
                        menu = menu,
                        modifier = Modifier.clickable(
                            enabled = menu.isActive && menu.isEnabled,
                            onClick = {
                                if(!menu.isActive || !menu.isEnabled) return@clickable
                                if (subMenus.value.size>0 && !menu.hasDashboard){
                                    if(subMenus.value.size>1) {
                                        selectedMenu.value = menu
                                        openSheet(
                                            scope = scope,
                                            sheetState = sheetState,
                                            showSheet = showSheet,
                                            currentSheet = currentSheet,
                                            sheet = BottomSheetType.SUB_MENUS
                                        )
                                    }
                                    else if(subMenus.value.size==1){
                                        val subMenu = subMenus.value.get(0)
                                        navigateMenu(navController,subMenu)
                                    }
                                    return@clickable
                                }
                                navigateMenu(navController,menu)
                            },
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ),
                        showRedDot = activeMenuSession.value,
                    )
                }
            }

            Spacer(modifier = Modifier.padding(top = dimensionResource(R.dimen.dp_24)))

            if(lastInventoryType.value.isNotEmpty()) {

                Text(
                    text = stringResource(R.string.last_activity),
                    style = CommonTypography.current.textMedium,
                    color = BlackColor,
                    fontSize = dimensionResource(R.dimen.sp_16).value.sp
                )

                Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_16)))


                StockCard(
                    title = lastInventoryType.value,//"Take Stock",
                    total = lastInventoryValidCount.value,
                    date = if (lastInventoryDate.value.isNullOrEmpty()) "" else DateFormatUtils.formatToDisplayTime(
                        lastInventoryDate.value,
                        DateFormatUtils.UTC_DATE_TIME_FORMAT
                    ),//  "24 DEC 2026, 12:00:35",
                    expected = lastInventoryExpectedCount.value//"1,50,0000"
                )
            }
        }

        if (showSheet.value && selectedMenu.value != null) {
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
                    BottomSheetType.SUB_MENUS -> Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .background(WhiteColor)
                            .imePadding()
                            .navigationBarsPadding()
                    ) {
                            InventoryMenuCycle(
                                showSheet = showSheet,
                                sheetState = sheetState,
                                scope = scope,
                                menu = selectedMenu.value!!,
                                navController = navController,
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
}

@Composable
fun InventoryBottomBar() {
    ItekFooter()
}


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryMenuCycle(
    scope: CoroutineScope,
    sheetState: SheetState,
    showSheet: MutableState<Boolean>,
    menu: MenuEntity,
    navController: NavHostController,
    iconTint: Color = Color.Unspecified,
) {

    val context = LocalContext.current
    val db = AppDatabase.getDbInstance(context)
    val subMenus = db.menuDao().getSubMenus(menu.code).collectAsState(initial = emptyList())

    val items = subMenus.value.map { childMenu ->
            val activeMenuSession by db.tagInfoDao().hasActiveMenuSession(menuCode = childMenu.code).observeAsState(false)
            DeviceSettingItem(
                iconRes = getMenuIconByCode(childMenu.code),
                title = childMenu.label,
                isSession = if (activeMenuSession) true else false,
                isEnabled = childMenu.isActive && childMenu.isEnabled,
            ) {
                if (childMenu.code.isNotEmpty() && childMenu.isActive && childMenu.isEnabled) {
                    navigateMenu(navController,childMenu) }
                }
        }

    BottomSheetSubMenu(
        showSheet = showSheet,
        sheetState = sheetState,
        scope = scope,
        title = menu.label,
        items = items,
        iconTint = iconTint,
    )
}