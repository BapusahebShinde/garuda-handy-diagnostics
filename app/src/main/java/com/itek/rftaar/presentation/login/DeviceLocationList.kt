package com.itek.rftaar.presentation.login

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.itek.rftaar.R
import com.itek.rftaar.api.ApiResult
import com.itek.rftaar.api.constants.ParameterConstants
import com.itek.rftaar.api.constants.UrlConstants
import com.itek.rftaar.core.common.constants.LoginConstants
import com.itek.rftaar.core.common.utils.ParseUtils.extractJSONObject
import com.itek.rftaar.core.common.utils.ParseUtils.extractString
import com.itek.rftaar.core.database.DataStoreManager
import com.itek.rftaar.domain.model.LocationModel
import com.itek.rftaar.presentation.commonComp.AppSnackBar
import com.itek.rftaar.presentation.commonComp.CircularIcon
import com.itek.rftaar.presentation.commonComp.CommonButton
import com.itek.rftaar.presentation.commonComp.ErrorAppSnackBarData
import com.itek.rftaar.presentation.commonComp.SimpleRowItem
import com.itek.rftaar.presentation.commonComp.SnackbarController
import com.itek.rftaar.presentation.navigation.Screen
import com.itek.rftaar.presentation.viewmodel.ApiViewModel
import com.itek.rftaar.presentation.viewmodel.MenuViewModel
import com.itek.rftaar.ui.theme.BackGround
import com.itek.rftaar.ui.theme.BlackColor
import com.itek.rftaar.ui.theme.CommonTypography
import com.itek.rftaar.ui.theme.LightGray
import com.itek.rftaar.ui.theme.TextSubtext
import com.itek.rftaar.ui.theme.WhiteColor
import com.itek.rftaar.ui.theme.Yellow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceLocationList(
    modifier: Modifier,
    navController: NavHostController,
    apiViewModel: ApiViewModel = hiltViewModel()
) {
    val selectedLocationId = remember { mutableStateOf<LocationModel?>(null) }
    val snackbarController = remember { SnackbarController() }
    val scope = rememberCoroutineScope()
    val locationList = remember { mutableStateListOf<LocationModel>() }
    val focusManager = LocalFocusManager.current


    BackHandler(enabled = true) {
        navController.navigate(Screen.LoginScreen.route)
    }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { sheetValue ->
            sheetValue != SheetValue.Hidden
        }
    )
    val showSheet = remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        sheetState.show()
    }



    Box(modifier = modifier.background(BackGround)) {

        Scaffold(
            bottomBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BackGround),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    DeviceLocationListBottomBar(selectedLocationId, navController, snackbarController, apiViewModel,focusManager)
                }
            },
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .background(BackGround)
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                DeviceLocationListContent(navController, selectedLocationId, snackbarController,locationList,focusManager)
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.7f) // 70% height
                ) {
                    SetupDeviceLocation(
                        modifier = modifier,
                        sheetState = sheetState,
                        showSheet = showSheet,
                        apiViewModel = apiViewModel,
                        locationList= locationList
                    )
                }
            }
        }
    }
}


@Composable
fun DeviceLocationListContent(
    navController: NavHostController,
    selectedLocationId: MutableState<LocationModel?>,
    snackbarController: SnackbarController,
    locationList: SnapshotStateList<LocationModel>,
    focusManager: FocusManager
) {

    val searchQuery = remember { mutableStateOf("") }
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused = interactionSource.collectIsFocusedAsState()
    val borderColor = if (searchQuery.value.isNotEmpty()) Yellow else LightGray
    val borderWidth = if (searchQuery.value.isNotEmpty()) 2.dp else 1.dp
    val filteredList = if (searchQuery.value.isEmpty()) {
        locationList.take(5)
    } else {
        locationList.filter {
            it.name.contains(searchQuery.value, ignoreCase = true) ||
                    it.code.contains(searchQuery.value, ignoreCase = true)
        }
    }
    val listVisible = remember { mutableStateOf(true) }
    val fullText = stringResource(id = R.string.contact_support)
    val linkText = stringResource(id = R.string.contact_support_link)
    val supportTag = stringResource(id = R.string.support)
    val annotatedText = buildAnnotatedString {
        val parts = fullText.split(linkText)
        append(parts[0])
        pushStringAnnotation(tag = supportTag, annotation = linkText)
        withStyle(
            style = SpanStyle(
                color = BlackColor,
                textDecoration = TextDecoration.Underline
            )
        ) {
            append(linkText)
        }
        pop()
        if (parts.size > 1) append(parts[1])
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .pointerInput(Unit) {
                detectTapGestures {
                    focusManager.clearFocus()
                }
            }
            ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = dimensionResource(id = R.dimen.dp_16))
                .padding(horizontal = dimensionResource(id = R.dimen.dp_16)),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.End
            ) {
                DeviceLocationHeader(navController)
            }

            Text(
                text = stringResource(id = R.string.enter_location_code),
                style = CommonTypography.current.headingH1,
                modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.dp_10))
            )

            ContactSupportLink(annotatedText, supportTag, navController)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(2f)
            ) {
                LocationSearchField(
                    searchQuery,
                    listVisible,
                    interactionSource,
                    borderWidth,
                    borderColor
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (listVisible.value) {
                    LocationSearchList(
                        filteredItems = filteredList,
                        onItemSelected = { item ->
                            searchQuery.value = item.code
                            selectedLocationId.value = item
                            listVisible.value = false
                        }
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
fun DeviceLocationHeader(navController: NavHostController) {
    Box(
        modifier = Modifier
            .background(BackGround)
            .fillMaxWidth()
            .padding(top = 8.dp),
        contentAlignment = Alignment.TopEnd
    ) {
        CircularIcon(
            iconRes = R.drawable.clear,
            modifier = Modifier.clickable(
                onClick = {
                    navController.navigate(Screen.LoginScreen.route)
                },
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
        )
    }
}

@Composable
fun ContactSupportLink(
    annotatedText: AnnotatedString,
    supportTag: String,
    navController: NavHostController
) {
    ClickableText(
        text = annotatedText,
        style = CommonTypography.current.textMedium.copy(color = TextSubtext),
        modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.dp_16)),
        onClick = { offset ->
            annotatedText.getStringAnnotations(
                tag = supportTag,
                start = offset,
                end = offset
            ).firstOrNull()?.let {
                navController.navigate(Screen.ContactSupport.route)
            }
        }
    )
}


@Composable
fun LocationSearchList(
    filteredItems: List<LocationModel>,
    onItemSelected: (LocationModel) -> Unit
) {
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        filteredItems.forEachIndexed { index, item ->

            val formatted = buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(item.code)
                }
                append(" - ")
                withStyle(style = SpanStyle(fontWeight = FontWeight.Normal)) {
                    append(item.name)
                }
            }

            SimpleRowItem(
                leftText = formatted.toString(),
                modifier = Modifier
            ){
                onItemSelected(item)
            }

            if (index < filteredItems.lastIndex) {
                HorizontalDivider(
                    thickness = dimensionResource(id = R.dimen.dp_1),
                    modifier = Modifier.padding(
                        vertical = dimensionResource(id = R.dimen.dp_10)
                    )
                )
            }
        }
    }
}


@Composable
fun LocationSearchField(
    searchQuery: MutableState<String>,
    listVisible: MutableState<Boolean>,
    interactionSource: MutableInteractionSource,
    borderWidth: Dp,
    borderColor: Color
) {
    TextField(
        value = searchQuery.value,
        onValueChange = {
            searchQuery.value = it
            if (it.isNotEmpty()) listVisible.value = true
        },
        placeholder = { Text(stringResource(id = R.string.search_by_location_code)) },
        leadingIcon = if (searchQuery.value.isNotEmpty()) {
            {
                Image(
                    painter = painterResource(id = R.drawable.icon_location),
                    contentDescription = null
                )
            }
        } else null,
        trailingIcon = {
            if (searchQuery.value.isNotEmpty())

                IconButton(onClick = {
                    searchQuery.value = ""
                    listVisible.value = true
                }) {
                    Image(
                        painter = painterResource(id = R.drawable.clear),
                        contentDescription = null
                    )
                }

        },
        singleLine = true,
        interactionSource = interactionSource,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = borderWidth,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .background(WhiteColor, RoundedCornerShape(16.dp)),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            cursorColor = BlackColor,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        textStyle = CommonTypography.current.textSemiBold.copy(color = BlackColor).copy(textAlign = TextAlign.Start)
    )
}




@Composable
fun DeviceLocationListBottomBar(
    selectedLocationId: MutableState<LocationModel?>,
    navController: NavHostController,
    snackbarController: SnackbarController,
    apiViewModel: ApiViewModel,
    focusManager: FocusManager,
    menuViewModel: MenuViewModel = hiltViewModel()
) {

    val response = apiViewModel.apiResult.collectAsState(initial = null)
    val scope = rememberCoroutineScope()
    val mqttSuccessMessage = stringResource(R.string.mqttsuccessMessage)
    val mqttErrorMessage = stringResource(R.string.mqtterrorMessage)

    LaunchedEffect(response.value) {
        if(response.value==null) return@LaunchedEffect
        response.value?.let { handleApiResult(it, selectedLocationId, snackbarController, apiViewModel, menuViewModel, navController,scope,mqttSuccessMessage,mqttErrorMessage) }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = dimensionResource(id = R.dimen.dp_16))
            .padding(horizontal = dimensionResource(id = R.dimen.dp_16))
            .pointerInput(Unit) {
                detectTapGestures {
                    focusManager.clearFocus()
                }
            }
    ) {

        CommonButton(
            text = stringResource(id = R.string.activate_device),
            enabled = selectedLocationId.value != null,
            onClick = {
                selectedLocationId.value?.let { location ->
                    triggerDeviceCreate(location, apiViewModel)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private fun handleApiResult(
    result: ApiResult,
    selectedLocationId: MutableState<LocationModel?>,
    snackbarController: SnackbarController,
    apiViewModel: ApiViewModel,
    menuViewModel: MenuViewModel,
    navController: NavHostController,
    scope: CoroutineScope,
    mqttSuccessMessage: String,
    mqttErrorMessage: String
) {
    if (!result.isSuccess) {
        showErrorSnackbar(result.errMsg, snackbarController)
        return
    }

    val responseJson = result.response ?: return

    when (result.url) {
        UrlConstants.DEVICE_CREATE -> handleDeviceCreate(responseJson, selectedLocationId, apiViewModel)
        UrlConstants.DEVICE_LOGIN -> handleDeviceLogin(selectedLocationId, apiViewModel)
//        UrlConstants.USER_LOGIN -> handleDeviceLogin(selectedLocationId, apiViewModel)
        UrlConstants.LOCATION_CONFIG -> handleLocationConfig(responseJson, menuViewModel,apiViewModel, navController,scope)
        UrlConstants.DEVICE_MENUS -> handleDeviceMenus(
            responseJson,
            menuViewModel,
            apiViewModel,
            navController,
            scope,
            true,
            snackbarController,
            mqttSuccessMessage,
            mqttErrorMessage,
            true
        )
    }
}

fun showErrorSnackbar(
    message: String?,
    snackbarController: SnackbarController
) {
    if (!message.isNullOrEmpty()) {
        snackbarController.show(
            ErrorAppSnackBarData(message)
            /*AppSnackBarData(
                icon = R.drawable.property_1_error,
                message = message,
                showCancel = false
            )*/
        )
    }
}


private fun handleDeviceCreate(
    jsonResponse: JSONObject,
    selectedLocationId: MutableState<LocationModel?>,
    apiViewModel: ApiViewModel
) {
    val data = extractJSONObject(jsonResponse, ParameterConstants.DATA, jsonResponse)

    val devicePkId = extractString(data, ParameterConstants.ID, "")
    val deviceId = extractString(data, ParameterConstants.DEVICE_KEY, "")
    val clientId = extractString(data, ParameterConstants.CLIENT_ID, "")
    val clientSecret = extractString(data, ParameterConstants.CLIENT_SECRET, "")
    val deviceCustomerId = extractString(data, ParameterConstants.CUSTOMER_ID, "")
    val deviceLocationId = extractString(data, ParameterConstants.LOCATION_ID, "")

    saveDeviceData(
        devicePkId, deviceId, clientId, clientSecret,
        deviceCustomerId, deviceLocationId, selectedLocationId.value
    )

    val jsonRequest = JSONObject().apply {
        put(ParameterConstants.CLIENT_ID, clientId)
        put(ParameterConstants.CLIENT_SECRET, clientSecret)
        put(ParameterConstants.CUSTOMER_ID, deviceCustomerId)
        put(ParameterConstants.LOCATION_ID, deviceLocationId)
        put(ParameterConstants.USER_ID, DataStoreManager.readFromPreferences(ParameterConstants.USER_ID, ""))
    }

    apiViewModel.callApi(UrlConstants.DEVICE_LOGIN, jsonRequest = jsonRequest)
}

private fun saveDeviceData(
    pkId: String,
    deviceId: String,
    clientId: String,
    secret: String,
    customerId: String,
    locationId: String,
    location: LocationModel?
) {
    if (pkId.isEmpty() || clientId.isEmpty() || secret.isEmpty() ||
        customerId.isEmpty() || locationId.isEmpty()
    ) return

    DataStoreManager.saveToPreferences(ParameterConstants.DEVICE_ID, deviceId)
    DataStoreManager.saveToPreferences(ParameterConstants.CLIENT_ID, clientId)
    DataStoreManager.saveToPreferences(ParameterConstants.CLIENT_SECRET, secret)
    DataStoreManager.saveToPreferences(LoginConstants.DEVICE_PK_ID, pkId)
    DataStoreManager.saveToPreferences(LoginConstants.DEVICE_CUSTOMER_ID, customerId)
    DataStoreManager.saveToPreferences(LoginConstants.DEVICE_LOCATION_ID, locationId)

    location?.let {
        DataStoreManager.saveToPreferences(LoginConstants.DEVICE_LOCATION_PATH, it.path)
        DataStoreManager.saveToPreferences(LoginConstants.DEVICE_LOCATION_CODE, it.code)
        DataStoreManager.saveToPreferences(LoginConstants.DEVICE_LOCATION_NAME, it.name)
    }
}


private fun handleDeviceLogin(
    selectedLocationId: MutableState<LocationModel?>,
    apiViewModel: ApiViewModel
) {
    DataStoreManager.saveToPreferences(LoginConstants.DEVICE_LOGIN_SUCCESS, true)
    apiViewModel.callApi(
        UrlConstants.LOCATION_CONFIG,
        appendData = selectedLocationId.value?.id
    )
}


private fun handleLocationConfig(
    jsonResponse: JSONObject,
    menuViewModel: MenuViewModel,
    apiViewModel: ApiViewModel,
    navController: NavHostController,
    scope: CoroutineScope
) {
    scope.launch {
        menuViewModel.insertMenu(jsonResponse)
        //call web-service for menus (deviceId, deviceType, customerId, businessLineId, locationId)
        callDeviceMenusApi(apiViewModel)
        //Old Code (commented for now)
        /*if(!BaseUtils.isDebuggable()) callDeviceMenusApi(apiViewModel)
        else {
            DataStoreManager.saveToPreferences(LoginConstants.IS_LOGGED_IN, true)
            DataStoreManager.saveToPreferences("isSuccessOnboarding", true)
            navController.navigate(Screen.DashBoardUiScreen.route)
        }*/
    }
}


private fun triggerDeviceCreate(location: LocationModel, apiViewModel: ApiViewModel) {
    val deviceId = DataStoreManager.readFromPreferences(ParameterConstants.DEVICE_ID, "")
    val customerId = DataStoreManager.readFromPreferences(LoginConstants.USER_CUSTOMER_ID, "")

    if (deviceId.isEmpty() || customerId.isEmpty()) return

    val jsonRequest = JSONObject().apply {
        put(ParameterConstants.DEVICE_KEY, deviceId)
        put(ParameterConstants.DEVICE_TYPE, ParameterConstants.DEVICE_TYPE_VAL)
        put(ParameterConstants.IS_ACTIVE, true)
        put(ParameterConstants.CUSTOMER_ID, customerId)
        put(ParameterConstants.LOCATION_ID, location.id)
        put(ParameterConstants.MAKE, android.os.Build.MANUFACTURER)
        put(ParameterConstants.MODEL, android.os.Build.MODEL)
        put(ParameterConstants.FIRMWARE_VERSION, "-")
        put(ParameterConstants.MAC_ADDRESS, "-")
    }

    apiViewModel.callApi(UrlConstants.DEVICE_CREATE, jsonRequest = jsonRequest)
}


