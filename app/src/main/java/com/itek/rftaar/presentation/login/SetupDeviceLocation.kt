package com.itek.rftaar.presentation.login

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import com.itek.rftaar.R
import com.itek.rftaar.api.constants.ParameterConstants
import com.itek.rftaar.api.constants.UrlConstants
import com.itek.rftaar.core.common.constants.LoginConstants
import com.itek.rftaar.core.common.utils.LogUtils
import com.itek.rftaar.core.common.utils.ParseUtils
import com.itek.rftaar.core.common.utils.ParseUtils.extractLong
import com.itek.rftaar.core.database.DataStoreManager
import com.itek.rftaar.domain.model.LocationModel
import com.itek.rftaar.presentation.commonComp.CommonButton
import com.itek.rftaar.presentation.commonComp.InfoRow
import com.itek.rftaar.presentation.viewmodel.ApiViewModel
import com.itek.rftaar.presentation.viewmodel.MenuViewModel
import com.itek.rftaar.ui.theme.CommonTypography
import com.itek.rftaar.ui.theme.OutlineDefault
import com.itek.rftaar.ui.theme.TextSubtext
import com.itek.rftaar.ui.theme.WhiteColor
import kotlinx.coroutines.launch
import org.json.JSONArray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupDeviceLocation(
    modifier: Modifier,
    sheetState: SheetState,
    showSheet : MutableState<Boolean>,
    apiViewModel: ApiViewModel = hiltViewModel(),
    menuViewModel: MenuViewModel = hiltViewModel(),
    locationList: SnapshotStateList<LocationModel>
){
    BackHandler(enabled = true) {}
    val isLoading = apiViewModel.isLoading.observeAsState(initial = false)
    Scaffold(
        bottomBar = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.BottomCenter
            ) {
                SetupDeviceLocationBottomBar( apiViewModel, menuViewModel,sheetState , showSheet,isLoading,locationList)
            }
        },

        modifier = modifier.background(WhiteColor)
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .background(WhiteColor)
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            SetupDeviceLocationContent( isLoading,sheetState , showSheet)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupDeviceLocationContent(
    isLoading: State<Boolean>,
    sheetState: SheetState,
    showSheet: MutableState<Boolean>,) {

    val scope = rememberCoroutineScope()

    Box( modifier = Modifier
        .fillMaxSize()
        .background(Color.Transparent)
        .clickable(
            enabled = true,
            onClick = {},
            indication = null,
            interactionSource = remember { MutableInteractionSource() }
        )){

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(dimensionResource(id = R.dimen.dp_16))
                .clickable(
                    onClick = {},
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = dimensionResource(R.dimen.dp_12))
                ) {
                    Text(
                        text = stringResource(R.string.set_device_location),
                        style = CommonTypography.current.textSemiBold,
                        modifier = Modifier.align(Alignment.Center)
                    )

                    Image(
                        painter = painterResource(R.drawable.clear),
                        contentDescription = "Close",
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .clickable {
                                scope.launch {
                                    sheetState.hide()
                                    showSheet.value = false
                                }
                            }
                    )
                }
            }
            Column(modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(id = R.string.setup_your_device_location),
                    style = CommonTypography.current.headingH1,
                    modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.dp_8)),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = stringResource(id = R.string.setup_Location_note),
                    style = CommonTypography.current.textMedium,
                    color = TextSubtext,
                    modifier = Modifier.padding(
                        bottom = dimensionResource(id = R.dimen.dp_34)
                    ),
                    textAlign = TextAlign.Center

                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.8f)
            ) {
                InfoRow(
                    icon = R.drawable.ic_fluent_lock_closed,
                    title = stringResource(R.string.secure_device_activation),
                    description = stringResource(R.string.se_tup_Location)
                )

                HorizontalDivider(
                    thickness = dimensionResource(id = R.dimen.dp_1),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = dimensionResource(id = R.dimen.dp_30)),
                    color = OutlineDefault
                )

                InfoRow(
                    icon = R.drawable.ic_fluent_locatio1,
                    title = stringResource(R.string.accurate_site_tracking),
                    description = stringResource(R.string.all_scans_data)
                )

                HorizontalDivider(
                    thickness = dimensionResource(id = R.dimen.dp_1),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = dimensionResource(id = R.dimen.dp_30)),
                    color = OutlineDefault
                )

                InfoRow(
                    icon = R.drawable.ic_fluent_fast_forward,
                    title = stringResource(R.string.quick_access),
                    description = stringResource(R.string.activate_once)
                )
            }
        }

        /**Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading.value){
                CircularLoader(
                    size = 50.dp,
                    strokeWidth = 6.dp,
                    color = BlackColor
                )
            }
        }*/
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupDeviceLocationBottomBar(
    apiViewModel: ApiViewModel,
    menuViewModel: MenuViewModel,
    sheetState: SheetState,
    showSheet: MutableState<Boolean>,
    isLoading: State<Boolean>,
    locationList: SnapshotStateList<LocationModel>
) {
    val response = apiViewModel.apiResult.collectAsState(initial = null)
    val scope = rememberCoroutineScope()
    LaunchedEffect(response.value) {
        if(response.value==null) return@LaunchedEffect
        val result = response.value
        if (result?.isSuccess != true) {
            LogUtils.showLog("Error", "SetupDeviceLocationBottomBar: ")
        } else if(result.response!=null) {
            val jsonResponse = result.response

            when (result.url) {
                UrlConstants.LOCATION_LIST -> {
                    //val menuData = menuViewModel.insertMenu(jsonResponse)
                    //LogUtils.showLog("menuData", "SetupDeviceLocationBottomBar: $menuData")
                    val total = extractLong(jsonResponse,ParameterConstants.TOTAL)
                    val page = extractLong(jsonResponse,ParameterConstants.PAGE)
                    val limit = extractLong(jsonResponse,ParameterConstants.LIMIT)
                    val responseArray = ParseUtils.extractJSONArray(jsonResponse,ParameterConstants.DATA, JSONArray())
                    val dataList = mutableListOf<LocationModel>()
                    if(page>1) dataList.addAll(locationList)
                    if (responseArray != null) {
                        for (i in 0 until responseArray.length()) {
                            val obj = responseArray.getJSONObject(i)
                            if (obj.optBoolean(ParameterConstants.IS_PHYSICAL,false) &&
                                obj.optBoolean(ParameterConstants.IS_ACTIVE,false)){

                                val location = LocationModel(
                                    id = obj.optString(ParameterConstants.ID),
                                    name = obj.optString(ParameterConstants.NAME),
                                    code = obj.optString(ParameterConstants.CODE),
                                    path = obj.optString(ParameterConstants.PATH)
                                )

                                dataList.add(location)
                            }

                        }
                    }
                    locationList.clear()
                    locationList.addAll(dataList)
                    //Pending handle auto-select for single location (Now Commented)
                    if(total==1L && dataList.size==1){
                        showSheet.value = false
                    }
                    else if(total>(page*limit)) {
                        val args:Bundle  = Bundle()
                        args.putLong(ParameterConstants.PAGE,page+1)
                        val customerId = DataStoreManager.readFromPreferences(LoginConstants.USER_CUSTOMER_ID , "")
                        apiViewModel.callApi(url = UrlConstants.LOCATION_LIST , appendData = customerId, args =args)
                    }
                    else
                        showSheet.value = false
//                     navController.navigate(Screen.DeviceLocationList.route)
                }
               /** UrlConstants.DEVICE_CREATE -> {
                    val data =  extractJSONObject(jsonResponse,ParameterConstants.DATA,jsonResponse)
                    val devicePkId = extractString(data, ParameterConstants.ID, "")
                    val deviceId = extractString(data, ParameterConstants.DEVICE_ID, "")
                    val clientId = extractString(data, ParameterConstants.CLIENT_ID, "")
                    val clientSecret = extractString(data, ParameterConstants.CLIENT_SECRET, "")
                    val deviceCustomerId = extractString(data, ParameterConstants.CUSTOMER_ID, "")
                    val deviceLocationId = extractString(data, ParameterConstants.LOCATION_ID, "")
                    val deviceLocationPath = selectedLocationId.value?.path
                    if(devicePkId.isNotEmpty() && clientId.isNotEmpty() && clientSecret.isNotEmpty() && deviceLocationId.isNotEmpty() && deviceCustomerId.isNotEmpty()){
                        DataStoreManager.saveToPreferences(ParameterConstants.DEVICE_ID,deviceId)
                        DataStoreManager.saveToPreferences(ParameterConstants.CLIENT_ID,clientId)
                        DataStoreManager.saveToPreferences(ParameterConstants.CLIENT_SECRET,clientSecret)
                        DataStoreManager.saveToPreferences(LoginConstants.DEVICE_PK_ID,devicePkId)
                        DataStoreManager.saveToPreferences(LoginConstants.DEVICE_CUSTOMER_ID,deviceCustomerId)
                        DataStoreManager.saveToPreferences(LoginConstants.DEVICE_LOCATION_ID,deviceLocationId)
                        DataStoreManager.saveToPreferences(LoginConstants.DEVICE_LOCATION_PATH,deviceLocationPath)
                        apiViewModel.callApi(UrlConstants.LOCATION_CONFIG, appendData = deviceLocationId)
                    }
                    else apiViewModel.callApi(UrlConstants.LOCATION_CONFIG, appendData = selectedLocationId.value?.id)
                }*/
                /*UrlConstants.DEVICE_LOGIN-> {

                }*/
                /**UrlConstants.USER_LOGIN -> {
                    isLoading.value
                }*/
                /*UrlConstants.LOCATION_CONFIG -> {
                    //TODO process
                    DataStoreManager.saveToPreferences(LoginConstants.IS_LOGGED_IN, true)

                }*/

            }
            scope.launch{
                sheetState.hide()
            }
            !showSheet.value
        }
    }
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = dimensionResource(id = R.dimen.dp_16))
        .padding(horizontal = dimensionResource(id = R.dimen.dp_16))) {

        CommonButton(
            text = stringResource(id  = R.string.setup_device_location),
            onClick = {
                val customerId = DataStoreManager.readFromPreferences(LoginConstants.USER_CUSTOMER_ID , "")
                apiViewModel.callApi(url = UrlConstants.LOCATION_LIST , appendData = customerId)

            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}