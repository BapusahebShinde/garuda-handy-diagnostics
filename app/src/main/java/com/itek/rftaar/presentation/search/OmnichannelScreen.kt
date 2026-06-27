package com.itek.rftaar.presentation.search

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController
import com.google.gson.Gson
import com.itek.rftaar.R
import com.itek.rftaar.api.constants.ParameterConstants
import com.itek.rftaar.api.constants.UrlConstants
import com.itek.rftaar.core.common.constants.LoginConstants
import com.itek.rftaar.core.common.utils.JsonUtils
import com.itek.rftaar.core.common.utils.LogUtils
import com.itek.rftaar.core.common.utils.ParseUtils
import com.itek.rftaar.core.common.utils.ParseUtils.extractString
import com.itek.rftaar.core.database.DataStoreManager
import com.itek.rftaar.data.model.ListTypeInfo
import com.itek.rftaar.mqtt.constants.SearchListTypeConstant
import com.itek.rftaar.presentation.commonComp.BottomSheetType
import com.itek.rftaar.presentation.commonComp.CommonTextField
import com.itek.rftaar.presentation.commonComp.ItekFooter
import com.itek.rftaar.presentation.commonComp.StatItem
import com.itek.rftaar.presentation.commonComp.TextFieldConfig
import com.itek.rftaar.presentation.commonComp.TopBarContent
import com.itek.rftaar.presentation.inward.SegmentedTabView
import com.itek.rftaar.presentation.movement.ReplenishmentStatsCard
import com.itek.rftaar.presentation.navigation.Screen
import com.itek.rftaar.presentation.viewmodel.ApiViewModel
import com.itek.rftaar.ui.theme.CommonTypography
import com.itek.rftaar.ui.theme.OutlineDefault
import com.itek.rftaar.ui.theme.RedColor
import com.itek.rftaar.ui.theme.WhiteColor
import com.itek.rftaar.utils.CommonUtils
import com.itek.rftaar.utils.CommonUtils.chkNull
import com.itek.rftaar.utils.CommonUtils.chkTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun OmnichannelScreen(
    modifier: Modifier,
    navController: NavHostController,
    label: String,
    code: String,
    apiViewModel: ApiViewModel = hiltViewModel(),
    searchParams: Map<String, Any>
) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { sheetValue ->
            sheetValue != SheetValue.Hidden
        }
    )
    val showSheet = remember { mutableStateOf(false) }
    val currentSheet = remember { mutableStateOf(BottomSheetType.NONE) }
    val isApiLoading = apiViewModel.isLoading.observeAsState(initial = false)

    LaunchedEffect(Unit) {
        if (apiViewModel!=null) {
                val map = HashMap<String, String>()
                map.put(ParameterConstants.CUSTOMER_ID, DataStoreManager.readFromPreferences(ParameterConstants.CUSTOMER_ID, ""))
                map.put(ParameterConstants.BUSINESS_LINE_ID, DataStoreManager.readFromPreferences(ParameterConstants.BUSINESS_LINE_ID, ""))
                map.put(ParameterConstants.LOCATION_ID, DataStoreManager.readFromPreferences(LoginConstants.DEVICE_LOCATION_ID, ""))
                map.put(ParameterConstants.DEVICE_ID, DataStoreManager.readFromPreferences(ParameterConstants.DEVICE_ID, ""))
                map.put(ParameterConstants.SEARCH_TYPE_NAME, SearchListTypeConstant.OMNICHANNEL)
                apiViewModel.callApi(UrlConstants.PRODUCT_SEARCH_LIST, queryMap = map)

        }
    }

    //val decodingTypes = apiViewModel.apiResult.collectAsStateWithLifecycle(initialValue = null)

    BackHandler(enabled = true) {
        if(chkTrue(isApiLoading.value)) return@BackHandler
        scope.launch {
                /*CoroutineScope(Dispatchers.IO).launch {
                    clearSavedSessionValues(context, menuCode, transactionType, false, topic)
                    db.productZoneDataDao().deleteAll(topic,menuCode,transactionType)
                }*/
                if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                    navController.popBackStack()
                }
        }
    }

    Scaffold(
        bottomBar = {
            OmnichannelBottomBar()
        }
    ) { innerPadding ->

        Box(
            modifier = modifier
                .background(WhiteColor)
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            OmnichannelContent(apiViewModel, label, code, navController)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun OmnichannelContent(
    apiViewModel: ApiViewModel,
    label: String,
    menuCode: String,
    navController: NavHostController
) {

    val decodedList = remember { mutableStateListOf<ListTypeInfo>() }
    val tabList = remember { mutableStateListOf<String>() }

    val displayPendingCount = remember { mutableStateOf(0) }
    val displayCompleteCount = remember { mutableStateOf(0) }
    val displayInProcessCount = remember { mutableStateOf(0) }
    val belowRange = remember { mutableStateOf("") }
    val belowRangeCount = remember { mutableStateOf(0) }
    val withinRange = remember { mutableStateOf("") }
    val withinRangeCount = remember { mutableStateOf(0) }
    val aboveRange = remember { mutableStateOf("") }
    val aboveRangeCount = remember { mutableStateOf(0) }

    val selectedTab = remember { mutableStateOf(0) }
    val selectedItem = remember { mutableStateOf("")}//if(tabList.isNullOrEmpty() || tabList.size<=selectedTab.value) "" else tabList.getOrNull(selectedTab.value))}
    //var lastCalledIndex = remember { mutableStateOf(-1) }
    val selectedData = remember(decodedList, selectedItem.value) {
        LogUtils.showLog("selectedItem1",""+selectedItem.value)
       if(selectedItem.value.isNullOrEmpty()) decodedList else decodedList.filter { it.childSearchTypeName.equals(selectedItem.value,true) }
    }
    val keyboardController = LocalSoftwareKeyboardController.current

    LogUtils.showLog("selectedData",""+selectedData.size)

    val Search = remember { mutableStateOf("") }
    val filteredLocations = if (Search.value.isEmpty()) selectedData
    else selectedData.filter { chkNull(it.referenceNumber,it.childSearchTypeName).contains(Search.value, ignoreCase = true) }

    LaunchedEffect(selectedItem.value) {
        if(selectedItem.value.isNullOrEmpty()) return@LaunchedEffect
        LogUtils.showLog("selectedItem",selectedItem.value)
        val childSearchMasterId = if(selectedData.isNotEmpty()) selectedData.get(0).childSearchMasterId else ""
        LogUtils.showLog("childSearchMasterId","__"+childSearchMasterId)
        CoroutineScope(Dispatchers.IO).launch {
            val map = hashMapOf(
                ParameterConstants.CUSTOMER_ID to DataStoreManager.readFromPreferences(ParameterConstants.CUSTOMER_ID, ""),
                ParameterConstants.BUSINESS_LINE_ID to DataStoreManager.readFromPreferences(ParameterConstants.BUSINESS_LINE_ID, ""),
                ParameterConstants.LOCATION_ID to DataStoreManager.readFromPreferences(LoginConstants.DEVICE_LOCATION_ID, ""),
                ParameterConstants.DEVICE_ID to DataStoreManager.readFromPreferences(ParameterConstants.DEVICE_ID, ""),
                "childSearchMasterId" to CommonUtils.chkNull(childSearchMasterId,CommonUtils.chkNull(selectedItem.value,""))
            )
            apiViewModel?.callApi(UrlConstants.GET_OMNI_DASHBOARD, queryMap = map)
        }
    }

    val TextLabel = "Search ${selectedItem.value.replace(Regex("search", RegexOption.IGNORE_CASE), "").trim()}"
    val response = apiViewModel.apiResult.collectAsState(initial = null)
    val apiError = apiViewModel.apiErrorMsg.observeAsState("")
    LaunchedEffect(response.value) {
        LogUtils.showLog("view", "StockCorrectionContentView")
        val result = response.value ?: return@LaunchedEffect
        if (result?.isSuccess != true) {
            val message = result?.errMsg
            if (!message.isNullOrBlank()) {
                //snackbarController.show(ErrorAppSnackBarData(result.errMsg.toString()))
            }
        }
        else if (result.response != null) {
            val jsonResponse = result.response

            when (result.url) {
                UrlConstants.PRODUCT_SEARCH_LIST -> {
                    val listSearchListType = ArrayList<ListTypeInfo>(0)
                    val listTabs = ArrayList<String>(0)
                    val responseArray = ParseUtils.extractJSONArray(jsonResponse, ParameterConstants.DATA, JSONArray())
                    if (responseArray != null && responseArray.length() > 0) {
                        for (i in 0 until responseArray.length()) {
                            val obj = responseArray.getJSONObject(i)
                            if (obj == null) continue
                            val listTypeObj = Gson().fromJson(obj.toString(), ListTypeInfo::class.java)
                            if (listTypeObj != null && listTypeObj.isValid()) {
                                if(!listTabs.contains(listTypeObj.childSearchTypeName)) listTabs.add(listTypeObj.childSearchTypeName)
                                listSearchListType.add(listTypeObj)
                            }
                        }
                        if (listSearchListType.isNotEmpty()) {
                            if(listTabs.isNotEmpty()){
                                tabList.clear()
                                tabList.addAll(listTabs)
                                LogUtils.showLog("tabList",""+tabList.size)
                                selectedItem.value = tabList.get(selectedTab.value)
                                LogUtils.showLog("selectedItem",selectedItem.value)
                            }
                            decodedList.clear()
                            decodedList.addAll(listSearchListType)
                        }
                    }
                    else {
                        //TODO give custom error
                    }
                }
                UrlConstants.GET_OMNI_DASHBOARD -> {
                    //temp code for checking dashboard API
                    //val jsonResponse = JsonUtils.getSampleJSON(result.url)
                    val response = ParseUtils.extractJSONObject(jsonResponse, ParameterConstants.DATA, jsonResponse)
                    //set variables values for Dashboard
                    displayPendingCount.value = ParseUtils.extractInt(response, ParameterConstants.PENDING_COUNT,ParseUtils.extractInt(response, ParameterConstants.PENDING_QTY,0))
                    displayCompleteCount.value =  ParseUtils.extractInt(response, ParameterConstants.COMPLETED_TODAY_COUNT,ParseUtils.extractInt(response, ParameterConstants.COMPLETED_TODAY_QTY,0))
                    displayInProcessCount.value =  ParseUtils.extractInt(response, ParameterConstants.IN_PROCESS_TODAY_COUNT,ParseUtils.extractInt(response, ParameterConstants.IN_PROCESS_TODAY_QTY,ParseUtils.extractInt(response, ParameterConstants.IN_PROGRESS_TODAY_COUNT,ParseUtils.extractInt(response, ParameterConstants.IN_PROGRESS_TODAY_QTY,0))))
                    val bucket = ParseUtils.extractJSONObject(response, ParameterConstants.BUCKET, response)
                    belowRange.value = extractString(bucket, ParameterConstants.BELOW_RANGE,"")
                    belowRangeCount.value = ParseUtils.extractInt(bucket, ParameterConstants.BELOW_RANGE_COUNT,ParseUtils.extractInt(bucket, ParameterConstants.BELOW_RANGE_QTY,0))
                    withinRange.value = extractString(bucket, ParameterConstants.WITHIN_RANGE,"")
                    withinRangeCount.value = ParseUtils.extractInt(bucket, ParameterConstants.WITHIN_RANGE_COUNT,ParseUtils.extractInt(bucket, ParameterConstants.WITHIN_RANGE_QTY,0))
                    aboveRange.value = extractString(bucket, ParameterConstants.ABOVE_RANGE,"")
                    aboveRangeCount.value =ParseUtils.extractInt(bucket, ParameterConstants.ABOVE_RANGE_COUNT,ParseUtils.extractInt(bucket, ParameterConstants.ABOVE_RANGE_QTY,0))
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
                    label,
                    onBackClickL = {
                        //if(chkTrue(isApiLoading.value)) return@TopBarContent
                        //scope.launch {
                            /*CoroutineScope(Dispatchers.IO).launch {
                                clearSavedSessionValues(context, menuCode, transactionType, false, topic)
                                db.productZoneDataDao().deleteAll(topic,menuCode,transactionType)
                            }*/
                            if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                                navController.popBackStack()
                            }
                        //}
                    }, onSettingClick = {

                    },
                    isSetting = false
                )
            }
            HorizontalDivider(thickness = dimensionResource(R.dimen.dp_1), color = OutlineDefault)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
                    .padding(dimensionResource(R.dimen.dp_16)),
                horizontalAlignment = Alignment.CenterHorizontally
            )
            {

                if (tabList.size > 1) {
                    SegmentedTabView(
                        selectedIndex = selectedTab.value,
                        tabs = tabList,//decodedList.map { it.childSearchTypeName }.distinct(),
                        modifier = Modifier
                            .width(230.dp)
                            .height(44.dp),
                        onTabSelected = {
                            selectedTab.value = it
                            selectedItem.value = tabList.get(it)
                        }
                    )
                }

                ReplenishmentStatsCard(
                    items = if(displayInProcessCount.value>0)listOf(
                        StatItem(
                            value = displayPendingCount.value.toString(),
                            label = "Pending"
                        ),
                        StatItem(
                            value = displayCompleteCount.value.toString(),
                            label = "Total"
                        ),
                        StatItem(
                            value = displayInProcessCount.value.toString(),
                            label = "In-Progress"
                        )
                    )else listOf(
                        StatItem(
                            value = displayPendingCount.value.toString(),
                            label = "Pending"
                        ),
                        StatItem(
                            value = displayCompleteCount.value.toString(),
                            label = "Total"
                        )
                    ),
                    belowRange = belowRange.value,
                    belowRangeCount = belowRangeCount.value,
                    withinRange = withinRange.value,
                    withinRangeCount = withinRangeCount.value,
                    aboveRange = aboveRange.value,
                    aboveRangeCount = aboveRangeCount.value,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                    CommonTextField(
                    config = TextFieldConfig(
                        value = Search.value,
                        onValueChange = {newValue ->
                            Search.value = newValue
                        },
                        label = TextLabel,
                        imeAction = ImeAction.Search,
                        onImeAction = {
                            keyboardController?.hide()
                        },
                        isSearch = true,
                    )
                )

                Spacer(modifier = Modifier.size(16.dp))

                if (decodedList.isEmpty()) {
                    Text(
                        text = chkNull(apiError.value,""),//stringResource(R.string.err_no_data)),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        style = CommonTypography.current.noteText,
                        color = RedColor
                    )

                } else {

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    itemsIndexed(
                        items = filteredLocations,
                        key = { index, item -> "${item.sessionId}_${index}" }
                    ) { index, item ->

                        ListItem(
                            item = item,
                            onClick = {
                                val selectionLabel = item.getDisplayTitle()

                                val params = mapOf(
                                    "originalTitle" to selectionLabel,
                                    item::class.java.simpleName to item,
                                )

                                navController.navigate(
                                    Screen.ListBasedSearch.createRoute(
                                        label = selectionLabel,
                                        params = params
                                    )
                                )
                            }
                        )

                        if (index != selectedData.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                                color = OutlineDefault,
                                thickness = 1.dp
                            )
                        }
                    }
                }
                }
            }
        }
    }
}

@Composable
fun OmnichannelBottomBar() {
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