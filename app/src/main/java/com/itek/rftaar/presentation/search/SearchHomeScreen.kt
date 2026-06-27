package com.itek.rftaar.presentation.search

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController
import com.itek.rftaar.R
import com.itek.rftaar.api.constants.ParameterConstants
import com.itek.rftaar.api.constants.UrlConstants
import com.itek.rftaar.core.common.constants.LoginConstants
import com.itek.rftaar.core.common.utils.LogUtils
import com.itek.rftaar.core.common.utils.ParseUtils
import com.itek.rftaar.core.database.AppDatabase
import com.itek.rftaar.core.database.DataStoreManager
import com.itek.rftaar.data.entity.MenuEntity
import com.itek.rftaar.presentation.commonComp.AppSnackBar
import com.itek.rftaar.presentation.commonComp.BottomSheetType
import com.itek.rftaar.presentation.commonComp.CircularIcon
import com.itek.rftaar.presentation.commonComp.ErrorAppSnackBarData
import com.itek.rftaar.presentation.commonComp.GenericIconCard
import com.itek.rftaar.presentation.commonComp.ItekFooter
import com.itek.rftaar.presentation.commonComp.SnackbarController
import com.itek.rftaar.presentation.commonComp.StatItem
import com.itek.rftaar.presentation.commonComp.StatsCardSearch
import com.itek.rftaar.presentation.commonComp.openSheet
import com.itek.rftaar.presentation.dashBoard.navigateMenu
import com.itek.rftaar.presentation.inventory.InventoryMenuCycle
import com.itek.rftaar.presentation.viewmodel.ApiViewModel
import com.itek.rftaar.ui.theme.BackGround
import com.itek.rftaar.ui.theme.CommonTypography
import com.itek.rftaar.ui.theme.OutlineDefault
import com.itek.rftaar.ui.theme.WhiteColor
import com.itek.rftaar.ui.theme.Yellow
import org.json.JSONObject

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ProductSearchHomeScreen(
    modifier: Modifier,
    navController: NavHostController,
    menuCode: String,
    label: String,
    searchParams: Map<String, Any> = emptyMap(),
    apiViewModel: ApiViewModel = hiltViewModel(),
) {
    LogUtils.showLog("searchParams", "ProductSearchHomeScreen: $searchParams")

    BackHandler(enabled = true) {
        if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED)
            navController.popBackStack()
    }

    LaunchedEffect(Unit) {
        val map = HashMap<String, String>()
        map.put(ParameterConstants.CUSTOMER_ID, DataStoreManager.readFromPreferences(ParameterConstants.CUSTOMER_ID, ""))
        map.put(ParameterConstants.BUSINESS_LINE_ID, DataStoreManager.readFromPreferences(ParameterConstants.BUSINESS_LINE_ID, ""))
        map.put(ParameterConstants.LOCATION_ID, DataStoreManager.readFromPreferences(LoginConstants.DEVICE_LOCATION_ID, ""))
        apiViewModel.callApi(UrlConstants.GET_SEARCH_COUNT, queryMap = map)
    }

    Scaffold(
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BackGround),
                contentAlignment = Alignment.BottomCenter
            ) {
                ProductSearchHomeBottomBar()
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
            ProductSearchHomeContent(navController,label,menuCode,apiViewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ProductSearchHomeContent(
    navController: NavHostController,
    code: String,
    label: String,
    apiViewModel: ApiViewModel
) {
    val context = LocalContext.current
    val db = AppDatabase.getDbInstance(context)
    val subMenus = db.menuDao().getSubMenus(code).collectAsState(initial = emptyList())
    LogUtils.showLog("code =", "EncodingScreenContent: $code")

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true, confirmValueChange = { sheetValue -> sheetValue != SheetValue.Hidden })
    val showSheet = remember { mutableStateOf(false) }
    val currentSheet = remember { mutableStateOf(BottomSheetType.NONE) }
    val scope = rememberCoroutineScope()
    val selectedMenu = remember { mutableStateOf<MenuEntity?>(null) }

    val snackbarController = remember { SnackbarController() }
    val searchCount = remember {mutableStateOf(0)}
    val avgTime = remember {mutableStateOf(0.0)}

    LaunchedEffect(currentSheet.value) {
        if (currentSheet.value == BottomSheetType.SUB_MENUS) {
            sheetState.expand()
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
        }
        else if (result.response != null) {
            val jsonResponse = result.response

            when (result.url) {
                UrlConstants.GET_SEARCH_COUNT -> {
                    val data = ParseUtils.extractJSONObject(jsonResponse, ParameterConstants.DATA, JSONObject())
                    searchCount.value = ParseUtils.extractInt(data,"searchCount",0)
                    avgTime.value = ParseUtils.extractDouble(data,"avgTime",0.0)
                }
            }
        }
    }

    val avgTimeInSeconds = String.format("%.2f", avgTime.value / 1000)

    Box {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(dimensionResource(id = R.dimen.dp_16))
                .clickable(
                    onClick = {},
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                CircularIcon(
                    iconRes = R.drawable.property_arrow_back, modifier = Modifier.clickable(
                        onClick = {
                            if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) navController.popBackStack()
                        },
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() })

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
                    text = label, style = CommonTypography.current.headingH1
                )
            }

            StatsCardSearch(
                items = listOf(
                    StatItem(
                        value = searchCount.value.toString(),
                        label = stringResource(R.string.total_search_today)
                    ), StatItem(
                        value = avgTimeInSeconds,
                        label = stringResource(R.string.average_time_search),
                        valueColor = Yellow
                    )
                )
            )
            Spacer(modifier = Modifier.padding(dimensionResource(R.dimen.dp_16)))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
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
                                if (subMenus.value.size > 0 && !menu.hasDashboard) {
                                    if (subMenus.value.size > 1) {
                                        selectedMenu.value = menu
                                        openSheet(
                                            scope = scope,
                                            sheetState = sheetState,
                                            showSheet = showSheet,
                                            currentSheet = currentSheet,
                                            sheet = BottomSheetType.SUB_MENUS
                                        )
                                    } else if (subMenus.value.size == 1) {
                                        val subMenu = subMenus.value.get(0)
                                        navigateMenu(navController, subMenu)
                                    }
                                    return@clickable
                                }
                                navigateMenu(navController, menu)
                            },
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }),
                        showRedDot = activeMenuSession.value,
                    )
                }
            }

            Spacer(modifier = Modifier.padding(top = dimensionResource(R.dimen.dp_10)))
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
fun ProductSearchHomeBottomBar() {
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
