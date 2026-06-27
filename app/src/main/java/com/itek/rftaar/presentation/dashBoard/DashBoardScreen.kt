package com.itek.rftaar.presentation.dashBoard

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
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
import com.itek.rftaar.core.common.utils.ParseUtils.extractJSONArray
import com.itek.rftaar.core.common.utils.ParseUtils.extractJSONObject
import com.itek.rftaar.core.common.utils.ParseUtils.extractString
import com.itek.rftaar.core.database.AppDatabase
import com.itek.rftaar.core.database.DataStoreManager
import com.itek.rftaar.data.entity.MenuEntity
import com.itek.rftaar.data.model.ActiveSessionQty
import com.itek.rftaar.presentation.commonComp.BottomSheetType
import com.itek.rftaar.presentation.commonComp.CircularIcon
import com.itek.rftaar.presentation.commonComp.CommonButton
import com.itek.rftaar.presentation.commonComp.GenericIconCard
import com.itek.rftaar.presentation.commonComp.ItekFooter
import com.itek.rftaar.presentation.commonComp.openSheet
import com.itek.rftaar.presentation.inventory.InventoryMenuCycle
import com.itek.rftaar.presentation.navigation.Screen
import com.itek.rftaar.presentation.viewmodel.ApiViewModel
import com.itek.rftaar.presentation.viewmodel.MenuViewModel
import com.itek.rftaar.ui.theme.BackGround
import com.itek.rftaar.ui.theme.CommonTypography
import com.itek.rftaar.ui.theme.OutlineDefault
import com.itek.rftaar.ui.theme.TextGrey
import com.itek.rftaar.ui.theme.TextSubtext
import com.itek.rftaar.ui.theme.WhiteColor
import com.itek.rftaar.ui.theme.Yellow
import com.itek.rftaar.utils.CommonUtils.chkNull
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DashBoardUiScreen(
    modifier: Modifier,
    navController: NavHostController,
    menuViewModel: MenuViewModel = hiltViewModel(),
    apiViewModel: ApiViewModel = hiltViewModel()
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { sheetValue ->
            sheetValue != SheetValue.Hidden
        }
    )
    val showSheet = remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val currentSheet = remember { mutableStateOf(BottomSheetType.NONE) }
    val selectedMenu = remember { mutableStateOf<MenuEntity?>(null) }
    val isDialogShow = DataStoreManager.readFromPreferences("isSuccessOnboarding", false)

    LaunchedEffect(Unit) {
        val map = HashMap<String,String>()
        map.put(ParameterConstants.CUSTOMER_ID,DataStoreManager.readFromPreferences(ParameterConstants.CUSTOMER_ID,""))
        map.put(ParameterConstants.LOCATION_ID,DataStoreManager.readFromPreferences(ParameterConstants.BUSINESS_LINE_ID,""))
        apiViewModel.callApi(UrlConstants.PRODUCT_MAPPING, queryMap = map)
    }

    LaunchedEffect(isDialogShow) {

        if (isDialogShow) {
            openSheet(
                scope = scope,
                sheetState = sheetState,
                showSheet = showSheet,
                currentSheet = currentSheet,
                sheet = BottomSheetType.MESSAGE
            )
            DataStoreManager.saveToPreferences("isSuccessOnboarding", false)
        }
    }

    val response = apiViewModel.apiResult.collectAsState(initial = null)

    LaunchedEffect(currentSheet.value) {
        if (currentSheet.value == BottomSheetType.SUB_MENUS) {
            sheetState.expand()
        }
    }
    LaunchedEffect(response.value) {
        if(response.value==null) return@LaunchedEffect
        val result = response.value
        if (result?.isSuccess != true) {
/**
            val message = result?.errMsg
*/
            /**if (!message.isNullOrBlank()) {
                snackbarController.show(
                    ErrorAppSnackBarData(result?.errMsg.toString())
                )
            }*/
        } else if (result.response != null) {
            val jsonResponse = result.response
            when (result.url) {
                UrlConstants.PRODUCT_MAPPING ->{
                    val data = extractJSONObject(jsonResponse, ParameterConstants.DATA,jsonResponse)
                    val mapping = extractJSONObject(data, ParameterConstants.MAPPING,data)
                    val mapping1 = extractJSONObject(mapping, ParameterConstants.MAPPING,mapping)
                    val meta = extractJSONArray(mapping1, ParameterConstants.META, JSONArray())
                    val ean = extractJSONObject(mapping1, ParameterConstants.EAN,mapping1)
                    val labelBarcode = extractString(ean, ParameterConstants.DISPLAY_LABEL_NAME,"")
                    val images = extractJSONObject(mapping1, ParameterConstants.IMAGES,mapping1)
                    val labelImage = extractString(images, ParameterConstants.DISPLAY_LABEL_NAME,"")
                    if(labelBarcode.isNotEmpty()) DataStoreManager.saveToPreferences(ParameterConstants.LABEL_BARCODE,labelBarcode)
                    if(labelImage.isNotEmpty()) DataStoreManager.saveToPreferences(ParameterConstants.LABEL_IMAGE,labelImage)
                    val listNonFilterHeaders = listOf("id", "customerId", "locationId", "uom", "uomColumnName", "ean", "articleId", "description", "image", "images", "createdAt", "updatedAt")
                    val listFilterHeaders = ArrayList<String>(0)
                    val listProductLabels = ArrayList<String>()
                    val keys = mapping1.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        if (key.isNullOrEmpty()) continue
                        if(mapping1.get(key)==null || !(mapping1.get(key) is JSONObject)) continue
                        val obj = extractJSONObject(mapping1,key,mapping1)
                        val value = if(obj==null) "" else chkNull(extractString(obj, ParameterConstants.DISPLAY_LABEL_NAME, ""), "")
                        if(value.isNotEmpty()) {
                          if(listNonFilterHeaders.isNotEmpty() && !listNonFilterHeaders.contains(key)) listFilterHeaders.add(value)
                          listProductLabels.add(value)
                        }
                    }
                    if(meta!=null && meta.length()>0){
                        for (i in 0 until meta.length()) {
                            val obj = meta.getJSONObject(i)
                            val value = if(obj==null) "" else chkNull(extractString(obj, ParameterConstants.DISPLAY_LABEL_NAME, ""), "")
                            if(value.isNotEmpty()) {
                                listFilterHeaders.add(value)
                                listProductLabels.add(value)
                            }
                        }
                    }
                    DataStoreManager.saveFilterList(listFilterHeaders)
                    //Save Product Headers
                    if (listProductLabels.isNotEmpty()) DataStoreManager.saveListStr("listProductLabels", listProductLabels)
                    LogUtils.showLog("listProductLabels",listProductLabels.toString())
                    LogUtils.showLog("listFilterHeader",listFilterHeaders.toString())
                }
            }
        }
    }

    val menus = menuViewModel.menus.observeAsState(emptyList())
    val showDecodingSheet = remember { mutableStateOf(false) } //Decoding sheet state
    val context = LocalContext.current

    BackHandler(enabled = true) {
        if(showSheet.value!=false && currentSheet.value != BottomSheetType.NONE) return@BackHandler
        if (navController.currentBackStackEntry?.lifecycle?.currentState != Lifecycle.State.RESUMED) return@BackHandler
        val activity = context as? ReaderActivity
        activity?.finishAffinity()
    }

    Box(modifier = modifier.background(BackGround)) {
        //Dashboard content with optional blur when sheet open
        Box(
            modifier = if (showDecodingSheet.value) {
                Modifier
                    .fillMaxSize()
                    .blur(16.dp)
                    .background(Color.Black.copy(alpha = 0.3f)) // dim effect
            } else Modifier.fillMaxSize()
        ) {
            Scaffold(
                bottomBar = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BackGround),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        DashBoardScreenBottomBar()
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
                    DashBoardScreenContent(
                        navController = navController,
                        menus = menus.value,
                        context,
                        scope,
                        sheetState,
                        showSheet,
                        currentSheet,
                        selectedMenu
                    )
                }
            }
        }

        if (showSheet.value) {
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
                    BottomSheetType.MESSAGE ->
                        Box(
                            modifier = Modifier
                                .background(WhiteColor)
                                .fillMaxWidth()
                                .fillMaxHeight(0.35f)
                        ) {
                            LoginSuccess(sheetState, showSheet, scope)
                        }

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
                        currentSheet.value = BottomSheetType.NONE
                    }
                }


            }
        }

    }
}


@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun DashBoardScreenContent(
    navController: NavHostController,
    menus: List<MenuEntity>,
    context: Context,
    scope: CoroutineScope,
    sheetState: SheetState,
    showSheet: MutableState<Boolean>,
    currentSheet: MutableState<BottomSheetType>,
    selectedMenu: MutableState<MenuEntity?>
) {
    Column(
        modifier = Modifier
            .padding(horizontal = dimensionResource(id = R.dimen.dp_16))
            .padding(top = dimensionResource(id = R.dimen.dp_16))
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .clickable(
                onClick = {},
                indication = null,
                interactionSource = remember { MutableInteractionSource() }),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top
    ) {

        Column(modifier = Modifier.padding(top = dimensionResource(R.dimen.dp_10))) {
            HeaderSection(navController)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = dimensionResource(id = R.dimen.dp_16)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            QuickScanButton(navController)

            Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_16)))

            /**SegmentedTabView(
                selectedIndex = selectedTab.value,
                tabs = listOf("Home", "Favourites"),
                modifier = Modifier
                    .width(200.dp)
                    .height(48.dp),
                onTabSelected = {
                    selectedTab.value = it
                }
            )*/
        }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            maxItemsInEachRow = 3
        ) {
            val db = AppDatabase.getDbInstance(context)
/**
            val activeSessionList = db.tagInfoDao().getTopicWiseActiveSessionList(topic = TopicConstants.INVENTORY).collectAsState(initial = emptyList())
*/


            menus.forEach { menu ->
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
                            else navigateMenu(navController,menu)
                        },
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }),
                    showRedDot = activeMenuSession.value,
                )
            }
        }

        //SupportCard(navController)
    }
}


@RequiresApi(Build.VERSION_CODES.O)
fun navigateMenu(
    navController: NavHostController,
    menu: MenuEntity,
    params: Map<String, Any> = emptyMap()
){
    if(!menu.isEnabled || !menu.isActive) return
    navigateMenu(navController,menu.code,menu.label,params,menu.parentCode)
    /**val dashboard = Screen.DynamicDashboard(
        code = menu.code,
        label = menu.label,
        params = params
    )
    val parentCode = menu.parentCode
    navController.navigate(dashboard.createRoute(parentCode))*/
}

@RequiresApi(Build.VERSION_CODES.O)
fun navigateMenu(
    navController: NavHostController,
    menuCode: String,
    menuName: String,
    params: Map<String, Any> = emptyMap(),
    parentCode: String=""
){
    val dashboard = Screen.DynamicDashboard(
        code = menuCode,
        label = menuName,
        params = params
    )
    val parentCode = parentCode
    navController.navigate(dashboard.createRoute(parentCode))
}

@RequiresApi(Build.VERSION_CODES.O)
fun navigateMenu(
    navController: NavHostController,
    activeSessionQty: ActiveSessionQty,
    params: Map<String, Any> = emptyMap()
){
    navigateMenu(navController,activeSessionQty.menuCode,activeSessionQty.menuName,params,activeSessionQty.parentCode)
    /**val dashboard = Screen.DynamicDashboard(
        code = activeSessionQty.menuCode,
        label = activeSessionQty.menuName,
        params = emptyMap()
    )
    val parentCode = activeSessionQty.parentCode
    navController.navigate(dashboard.createRoute(parentCode))*/
}

@Composable
fun DashBoardScreenBottomBar() {
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


/**@Composable
fun TabView(navController: NavHostController, menus: List<MenuEntity>) {
val tabs = listOf(
TabItem("Home", R.drawable.property_home),
TabItem("Favourites", R.drawable.property_favourite)
)

val selectedTab = remember { mutableStateOf(0) }

Column(modifier = Modifier.fillMaxWidth()) {
TabRow(
selectedTabIndex = selectedTab.value,
indicator = {},
divider = {},
modifier = Modifier.fillMaxWidth()
) {
tabs.forEachIndexed { index, tabItem ->
Box(
modifier = Modifier
.width(dimensionResource(id = R.dimen.dp_102))
.padding(
end = dimensionResource(id = R.dimen.dp_8),
bottom = dimensionResource(id = R.dimen.dp_16)
)
.height(dimensionResource(id = R.dimen.dp_36))
.background(
color = if (selectedTab.value == index) BlackColor else TabColor,
shape = RoundedCornerShape(24.dp)
)
.clickable { selectedTab.value = index },
contentAlignment = Alignment.Center
) {
Row(verticalAlignment = Alignment.CenterVertically) {
Icon(
painter = painterResource(id = tabItem.icon),
contentDescription = tabItem.title,
tint = if (selectedTab.value == index) WhiteColor else BlackColor
)
Spacer(modifier = Modifier.width(6.dp))
Text(
text = tabItem.title,
color = if (selectedTab.value == index) WhiteColor else BlackColor
)
}
}
}
}

when (selectedTab.value) {
0 -> {} // Home content already included
1 -> FavoriteTabContent()
}
}
}*/

/**@Composable
fun FavoriteTabContent() {}*/

@Composable
fun HeaderSection(navController: NavHostController) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val deviceLocationCode = DataStoreManager.readFromPreferences(LoginConstants.DEVICE_LOCATION_CODE, "")
        val deviceLocationName = DataStoreManager.readFromPreferences(LoginConstants.DEVICE_LOCATION_NAME, "").trim().removeSuffix("("+deviceLocationCode+")").trim()

        Column(modifier = Modifier.fillMaxWidth(0.85f)) {
            Text(
                text = deviceLocationName,//String.format(stringResource(id = R.string.hi_name), DataStoreManager.readFromPreferences(ParameterConstants.USER_FIRST_NAME, "")),
                style = CommonTypography.current.headingH1,
                textAlign = TextAlign.Start,
                modifier = Modifier.basicMarquee()
            )


            Text(
                text = deviceLocationCode,
                style = CommonTypography.current.smallTxt,
                textAlign = TextAlign.Start,
                modifier = Modifier.basicMarquee()

            )
        }
        CircularIcon(
            iconRes = R.drawable.property_user,
            modifier = Modifier.clickable(
                onClick = {
                    navController.navigate(Screen.UserDetails.route)
                },
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )

        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun QuickScanButton(navController: NavHostController) {
    Card(
        onClick = {
            val route = Screen.QuickSearchScreen.createRoute()
            navController.navigate(route)
        },
        modifier = Modifier
            .width(328.dp)
            .height(48.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 9.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = WhiteColor
        )
    ) {
        Row(
            modifier = Modifier
                .width(328.dp)
                .height(48.dp)
                .background(color = WhiteColor, shape = RoundedCornerShape(size = 16.dp)),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.property_search),
                contentDescription = "navigation",
                modifier = Modifier
                    .size(dimensionResource(R.dimen.dp_24))
            )

            Text(
                text = stringResource(id = R.string.quick_scan_select),
                style = CommonTypography.current.textSemiBold,
                color = TextSubtext
            )
        }
    }
}

@Composable
fun SupportCard(navController: NavHostController) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = dimensionResource(id = R.dimen.dp_16)),
        contentAlignment = Alignment.BottomCenter
    ) {
        Image(painter = painterResource(R.drawable.card), contentDescription = null)
        Column(
            modifier = Modifier
                .clickable {
                    navController.navigate(Screen.ContactSupport.route)
                }
                .fillMaxWidth()
                .padding(dimensionResource(id = R.dimen.dp_16)),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center) {
            Text(
                text = stringResource(id = R.string.looking_for_assistance),
                style = CommonTypography.current.mediumText
            )
            Text(
                text = stringResource(id = R.string.have_a_question),
                style = CommonTypography.current.noteText,
                color = TextGrey,
                modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.dp_8))
            )
            Text(
                text = stringResource(id = R.string.contact_us),
                style = CommonTypography.current.noteText,
                color = Yellow,
                modifier = Modifier,
                textDecoration = TextDecoration.Underline
            )

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginSuccess(
    sheetState: SheetState,
    showSheet: MutableState<Boolean>,
    scope: CoroutineScope
) {
    val msgActivationSuccess = stringResource(R.string.successfully_activated)
    val deviceLocationName = DataStoreManager.readFromPreferences(LoginConstants.DEVICE_LOCATION_NAME, "").trim()
    val fullText = String.format(msgActivationSuccess, deviceLocationName)

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.dp_16)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Image(
                painter = painterResource(R.drawable.property_1_success),
                contentDescription = null,
                modifier = Modifier.size(dimensionResource(R.dimen.dp_48))
            )

            Text(
                text = stringResource(R.string.all_set),
                style = CommonTypography.current.headingH1,
                textAlign = TextAlign.Center
            )

            Text(
                text = buildAnnotatedString {
                    append(fullText)

                    if (deviceLocationName.isNotEmpty()) {
                        val start = fullText.indexOf(deviceLocationName)

                        if (start >= 0) {
                            addStyle(
                                style = SpanStyle(
                                    fontWeight = FontWeight.Bold,
                                    textDecoration = TextDecoration.Underline
                                ),
                                start = start,
                                end = start + deviceLocationName.length
                            )
                        }
                    }
                },
                style = CommonTypography.current.textSubtext,
                modifier = Modifier.padding(
                    vertical = dimensionResource(R.dimen.dp_16)
                ).basicMarquee(),
                textAlign = TextAlign.Center
            )

            CommonButton(
                text = stringResource(id = R.string.start_operations),
                onClick = {
                    scope.launch {
                        sheetState.hide()
                        showSheet.value = false
                    }
                }
            )
        }
    }
}



