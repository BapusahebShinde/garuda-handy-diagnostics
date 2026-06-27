package com.itek.rftaar.presentation.login

import android.annotation.SuppressLint
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imeNestedScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController
import com.itek.rftaar.R
import com.itek.rftaar.ReaderActivity
import com.itek.rftaar.api.ApiResult
import com.itek.rftaar.api.constants.ParameterConstants
import com.itek.rftaar.api.constants.UrlConstants
import com.itek.rftaar.core.common.constants.LoginConstants
import com.itek.rftaar.core.common.utils.BaseUtils
import com.itek.rftaar.core.common.utils.LogUtils
import com.itek.rftaar.core.common.utils.ParseUtils.extractBoolean
import com.itek.rftaar.core.common.utils.ParseUtils.extractInt
import com.itek.rftaar.core.common.utils.ParseUtils.extractJSONObject
import com.itek.rftaar.core.common.utils.ParseUtils.extractString
import com.itek.rftaar.core.common.utils.ToastUtils
import com.itek.rftaar.core.database.DataStoreManager
import com.itek.rftaar.mqtt.MqttManager
import com.itek.rftaar.presentation.commonComp.AppSnackBar
import com.itek.rftaar.presentation.commonComp.CircularIcon
import com.itek.rftaar.presentation.commonComp.CommonButton
import com.itek.rftaar.presentation.commonComp.CommonTextField
import com.itek.rftaar.presentation.commonComp.ErrorAppSnackBarData
import com.itek.rftaar.presentation.commonComp.SnackbarController
import com.itek.rftaar.presentation.commonComp.SuccessAppSnackBarData
import com.itek.rftaar.presentation.commonComp.TextFieldConfig
import com.itek.rftaar.presentation.navigation.Screen
import com.itek.rftaar.presentation.viewmodel.ApiViewModel
import com.itek.rftaar.presentation.viewmodel.MenuViewModel
import com.itek.rftaar.ui.theme.BackGround
import com.itek.rftaar.ui.theme.CommonTypography
import com.itek.rftaar.ui.theme.LightGray
import com.itek.rftaar.ui.theme.OutlineDefault
import com.itek.rftaar.ui.theme.ScrollerBackground
import com.itek.rftaar.ui.theme.ShadowGray
import com.itek.rftaar.ui.theme.TextGrey
import com.itek.rftaar.ui.theme.TextSubtext
import com.itek.rftaar.ui.theme.WhiteColor
import com.itek.rftaar.ui.theme.Yellow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun LoginScreen(modifier: Modifier, navController: NavHostController,apiViewModel: ApiViewModel = hiltViewModel()) {

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { sheetValue ->
            sheetValue != SheetValue.Hidden
        }
    )
    val showSheet = remember { mutableStateOf(false) }
    val context = LocalContext.current
    val isApiLoading = apiViewModel.isLoading.observeAsState(false)

    BackHandler(enabled = true) {
        if(isApiLoading.value) return@BackHandler
        if(showSheet.value!=false) return@BackHandler
        if (navController.currentBackStackEntry?.lifecycle?.currentState != Lifecycle.State.RESUMED) return@BackHandler
        val activity = context as? ReaderActivity
        activity?.finishAffinity()
    }

    Scaffold(
        bottomBar = {
            Box(
                modifier = Modifier
                    .background(BackGround)
                    .fillMaxWidth(),
                contentAlignment = Alignment.BottomCenter
            ) {
                LoginScreenBottomBar()
            }
        },

        modifier = modifier.background(WhiteColor)
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .background(BackGround)
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            LoginScreenContent(context,navController,showSheet,sheetState,apiViewModel)
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarContent(
    sheetState: SheetState,
    showSheet: MutableState<Boolean>,
    scope: CoroutineScope,
    apiViewModel: ApiViewModel
) {
    val isApiLoading = apiViewModel.isLoading.observeAsState(false)
    Box(
        modifier = Modifier
            .background(BackGround)
            .fillMaxWidth()
            .padding(top = 8.dp)
            .clickable(
                onClick = {
                    if(isApiLoading.value) return@clickable
                    scope.launch {
                        sheetState.show()
                        showSheet.value = true
                    }
                },
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ),
        contentAlignment = Alignment.TopEnd
    ) {
        CircularIcon(
            iconRes = R.drawable.property_setting
        )
    }
}

@SuppressLint("FrequentlyChangingValue")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LoginScreenContent(
    context: Context,
    navController: NavHostController,
    showSheet: MutableState<Boolean>,
    sheetState: SheetState,
    apiViewModel: ApiViewModel,
    menuViewModel: MenuViewModel = hiltViewModel()
) {
    val isApiLoading = apiViewModel.isLoading.observeAsState(false)
    val userName = rememberSaveable { mutableStateOf("") }
    val userNameError = remember { mutableStateOf(false) }
    val password = rememberSaveable { mutableStateOf("") }
    val passwordError = remember { mutableStateOf(false) }
    val passwordFocusRequester = remember { FocusRequester() }
    val scope = rememberCoroutineScope()


    /**
    val openDialog = remember { mutableStateOf(false) }
     */
    val focusManager = LocalFocusManager.current
    val images = listOf(
        R.drawable.handy,
        R.drawable.shell,
        R.drawable.pay,
        R.drawable.encoder
    )
    val pagerState = rememberPagerState(pageCount = { images.size })
    val snackbarController = remember { SnackbarController() }
    val userAndPasswordError = stringResource(id = R.string.please_fill_username_and_password)
    val userNameErrorMessage = stringResource(id = R.string.please_fill_username)
    val passwordErrorMessage = stringResource(id = R.string.please_fill_password)
    val serverUrlErrorMessage = stringResource(id = R.string.error_config_url)
    val mqttUrlErrorMessage = stringResource(id = R.string.error_mqtt_url)

    val mqttSuccessMessage = stringResource(R.string.mqttsuccessMessage)
    val mqttErrorMessage = stringResource(R.string.mqtterrorMessage)
    val detailsList = listOf(
        DetailsItem(
            icon = painterResource(R.drawable.scan_encoding),
            heading = "Smart Inventory Management",
            subHeading = "Real-time visibility. Zero shrinkage. Less manual work."
        ),
        DetailsItem(
            icon = painterResource(R.drawable.ic_inv),
            heading = "Intelligent Stock Management",
            subHeading = "Real-time visibility. Zero shrinkage. Less manual work."
        ),
        DetailsItem(
            icon = painterResource(R.drawable.product_search),
            heading = "Smart Search",
            subHeading = "Know your stock. No hunting. No guesswork. Just results."
        ),
         DetailsItem(
            icon = painterResource(R.drawable.ic_mov),
            heading = "Seamless Movement",
            subHeading = "Control every entry. Command every exit. Visibility at every gate."
        )

    )

    val response = apiViewModel.apiResult.collectAsState(null)

    LaunchedEffect(response.value) {
        if(response.value==null) return@LaunchedEffect
        val result = response.value
        if (result?.isSuccess != true) {
         val message = result?.errMsg
         if(!message.isNullOrEmpty())
          snackbarController.show(ErrorAppSnackBarData(result?.errMsg.toString()))
         return@LaunchedEffect
        }
        else if (result.response != null) {
          val json = result.response
          handleApiSuccess(
              context,
                    result,
                    json,
                    apiViewModel,
                    password,
                    navController,
                    snackbarController,
                    mqttUrlErrorMessage,
                    serverUrlErrorMessage,
                    mqttSuccessMessage,
                    mqttErrorMessage,
                    menuViewModel,
                    scope
                )
        }

    }

    LaunchedEffect(Unit) {
        DataStoreManager.readFromPreferences(LoginConstants.IS_LOGGED_IN, "")
        while (true) {
            delay(3000)
            val nextPage = (pagerState.currentPage + 1) % images.size
            pagerState.animateScrollToPage(nextPage)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .clickable(
                enabled = true,
                onClick = {},
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ))
    {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .imeNestedScroll()
                .padding(horizontal = 16.dp)
                .clickable(
                    onClick = {
                        focusManager.clearFocus()
                    },
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        )
        {
            Column(
                modifier = Modifier
                    .weight( 1f, fill = false)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.Top
                ) {
                    TopBarContent(sheetState,showSheet,scope,apiViewModel)
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {

                    Text(
                        text = stringResource(id = R.string.app_name),
                        style = CommonTypography.current.headingH1
                    )

                    Spacer(modifier = Modifier.size(2.dp))
                    Text(
                        text = stringResource(id = R.string.tag_track_transform),
                        style = CommonTypography.current.headingH1,
                        color = WhiteColor,
                        modifier = Modifier
                            .background(shape = RoundedCornerShape(8.dp), color = Yellow)
                            .padding(horizontal = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.size(12.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) { page ->
                        val pageOffset =
                            (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                        val scale = 1f - abs(pageOffset) * 0.2f

                        val item = detailsList[page]

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                    alpha = scale
                                }
                        ) {
                            DetailsView(
                                icon = item.icon,
                                heading = item.heading,
                                subHeading = item.subHeading
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        repeat(detailsList.size) { index ->
                            val selected = pagerState.currentPage == index

                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .height(4.dp)
                                    .width(if (selected) 13.dp else 4.dp)
                                    .clip(CircleShape)
                                    .background(if (selected) Yellow else ScrollerBackground)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            Box(
                modifier = Modifier
                    .wrapContentHeight()
                    .fillMaxWidth(),
                contentAlignment = Alignment.BottomCenter
            ) {
                Card(
                    modifier = Modifier
                        .shadow(
                            24.dp,
                            RoundedCornerShape(24.dp),
                            spotColor = ShadowGray,
                            ambientColor = ShadowGray
                        )
                        .border(1.dp, LightGray, RoundedCornerShape(24.dp))
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .heightIn(min = 320.dp, max = 337.dp)
                        .background(WhiteColor, shape = RoundedCornerShape(24.dp))
                        .padding(bottom = 8.dp),
                    shape = RoundedCornerShape(24.dp)
                )
                {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(WhiteColor, shape = RoundedCornerShape(24.dp))
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {

                        Spacer(modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.dp_8)))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = stringResource(id = R.string.get_started),
                                style = CommonTypography.current.headingH1,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                            )
                            Text(
                                text = stringResource(id = R.string.login_note),
                                style = CommonTypography.current.textSubtext,
                                color = TextGrey,
                                modifier = Modifier
                                    .fillMaxWidth(),
                                maxLines = Int.MAX_VALUE,
                                overflow = TextOverflow.Visible
                            )

                            Spacer(modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.dp_8)))
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight()
                            ) {
                                CommonTextField(
                                    config = TextFieldConfig(
                                        value = userName.value,
                                        onValueChange = {
                                            userName.value = it
                                            userNameError.value = false
                                        },
                                        label = stringResource(id = R.string.username),
                                        isUserName = true,
                                        imeAction = ImeAction.Next,
                                        onImeAction = { passwordFocusRequester.requestFocus() },
                                        isError = userNameError.value
                                    )
                                )
                            }

                            Spacer(Modifier.size(12.dp))

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight()
                            ) {
                                CommonTextField(
                                    config = TextFieldConfig(
                                        value = password.value,
                                        onValueChange = {
                                            password.value = it
                                            passwordError.value = false
                                        },
                                        label = stringResource(id = R.string.password),
                                        isPassword = true,
                                        focusRequester = passwordFocusRequester,
                                        imeAction = ImeAction.Done,
                                        onImeAction = { focusManager.clearFocus() },
                                        isError = passwordError.value
                                    )
                                )
                            }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = stringResource(id = R.string.forgot_password),
                                style = CommonTypography.current.textSubtext,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        navController.navigate(Screen.ForgotPasswordScreen.route)
                                    },
                                textAlign = TextAlign.End,
                                color = TextGrey
                            )
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),
                            verticalArrangement = Arrangement.Center
                        ) {
                            /**validation on button is pending*/
                            CommonButton(
                                text = stringResource(id = R.string.continue_),
                                onClick = {
                                    val usernameText = userName.value.trim()
                                    val passwordText = password.value.trim()

                                    LogUtils.showLog(
                                        "LOGIN_DEBUG",
                                        "username=$usernameText, password=$passwordText"
                                    )

                                    when {
                                        usernameText.isEmpty() && passwordText.isEmpty() -> {
                                            userNameError.value = true
                                            passwordError.value = true
                                            snackbarController.show(
                                                ErrorAppSnackBarData(userAndPasswordError)
                                                /*AppSnackBarData(
                                                icon = R.drawable.property_1_error,
                                                message = userAndPasswordError,
                                                showCancel = true
                                            )*/
                                            )
                                            return@CommonButton
                                        }

                                        usernameText.isEmpty() -> {
                                            userNameError.value = true
                                            snackbarController.show(
                                                ErrorAppSnackBarData(userNameErrorMessage)
                                                /*AppSnackBarData(
                                                icon = R.drawable.property_1_error,
                                                message = userNameErrorMessage,
                                                showCancel = true
                                            )*/
                                            )
                                            return@CommonButton
                                        }

                                        passwordText.isEmpty() -> {
                                            passwordError.value = true
                                            snackbarController.show(
                                                ErrorAppSnackBarData(passwordErrorMessage)
                                                /*AppSnackBarData(
                                                icon = R.drawable.property_1_error,
                                                message = passwordErrorMessage,
                                                showCancel = true
                                            )*/
                                            )
                                            return@CommonButton
                                        }

                                    }

                                    val serverConfigured = DataStoreManager.readFromPreferences(
                                        ParameterConstants.BASE_URL,
                                        ""
                                    ).isNotBlank()
                                    //Temp Commented
                                    val mqttConfigured =true// DataStoreManager.readFromPreferences(ParameterConstants.BROKER_URL,"").isNotBlank()
                                    if (!serverConfigured || !mqttConfigured) {
                                        snackbarController.show(ErrorAppSnackBarData(if (!serverConfigured) serverUrlErrorMessage else mqttUrlErrorMessage))
                                        return@CommonButton
                                    }

                                    // Build API body safely
                                    val jsonRequest = JSONObject().apply {
                                        put(ParameterConstants.USERNAME, userName.value)
                                        put(ParameterConstants.PASSWORD, password.value)
                                    }
                                    LogUtils.showLog(
                                        "USECASE_FINAL_BODY",
                                        "Final Body sent = $jsonRequest"
                                    )

                                    // Call API
                                    apiViewModel.callApi(
                                        url = UrlConstants.USER_LOGIN,
                                        jsonRequest = jsonRequest
                                    )


                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                }
            }
            Spacer(modifier = Modifier.size(12.dp))
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            AppSnackBar(snackbarController)
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
                DeviceSettings(navController,scope,sheetState,showSheet)
            }
        }
    }
}

private fun handleApiSuccess(
    context: Context,
    result: ApiResult,
    jsonResponse: JSONObject,
    apiViewModel: ApiViewModel,
    password: MutableState<String>,
    navController: NavHostController,
    snackbarController: SnackbarController,
    mqttUrlErrorMessage: String,
    serverUrlErrorMessage: String,
    mqttSuccessMessage: String,
    mqttErrorMessage: String,
    menuViewModel: MenuViewModel,
    scope: CoroutineScope
) {
    when (result.url) {
        UrlConstants.USER_LOGIN -> {
            scope.launch {
                handelUserLogin(
                    context,
                    scope,
                    apiViewModel,
                    jsonResponse,
                    snackbarController,
                    mqttSuccessMessage,
                    mqttErrorMessage
                )
            }
        }

        UrlConstants.USER_DETAILS -> {
            handelUserDetails(
                apiViewModel,
                jsonResponse,
                password,
                navController,
                snackbarController,
                mqttUrlErrorMessage,
                serverUrlErrorMessage
            )
        }

        UrlConstants.DEVICE_CHECK -> {
            handelDeviceCheck(apiViewModel, jsonResponse)
        }

        UrlConstants.DEVICE_LOGIN -> {
            handleDeviceLogin(apiViewModel)
        }

        /*UrlConstants.FORGOT_PASSWORD -> {
            handleForgotPassword(apiViewModel)
        }*/

        UrlConstants.LOCATION_CONFIG -> {
            handleLocationConfig(jsonResponse, menuViewModel,apiViewModel, navController,scope)
        }

        UrlConstants.DEVICE_MENUS -> {
            handleDeviceMenus(jsonResponse, menuViewModel,apiViewModel, navController,scope,true,snackbarController,
                mqttSuccessMessage,
                mqttErrorMessage)
        }
    }
}

private fun handleDeviceLogin(
    apiViewModel: ApiViewModel
) {
    LogUtils.showLog("method","handleDeviceLogin")
    DataStoreManager.saveToPreferences(LoginConstants.DEVICE_LOGIN_SUCCESS, true)
    apiViewModel.callApi(UrlConstants.LOCATION_CONFIG,DataStoreManager.readFromPreferences(LoginConstants.DEVICE_LOCATION_ID, ""))
}

/*private fun handleForgotPassword(
    apiViewModel: ApiViewModel
) {
    LogUtils.showLog("method","handleForgotPassword")
    DataStoreManager.saveToPreferences(LoginConstants.DEVICE_LOGIN_SUCCESS, true)
    apiViewModel.callApi(UrlConstants.LOCATION_CONFIG,DataStoreManager.readFromPreferences(LoginConstants.DEVICE_LOCATION_ID, ""))
}*/

@Composable
fun LoginScreenBottomBar() {
    Text(
        text = stringResource(id = R.string.copyright_txt),
        style = CommonTypography.current.smallTxt,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
        ,
        textAlign = TextAlign.Center
    )
}

fun callDeviceMenusApi(apiViewModel: ApiViewModel){
    LogUtils.showLog("method","callDeviceMenusApi")
    //call web-service for menus (deviceId, deviceType, customerId, businessLineId, locationId)
    try {
        val map = hashMapOf(
            ParameterConstants.CUSTOMER_ID to DataStoreManager.readFromPreferences(ParameterConstants.CUSTOMER_ID, ""),
            ParameterConstants.BUSINESS_LINE_ID to DataStoreManager.readFromPreferences(ParameterConstants.BUSINESS_LINE_ID, ""),
            ParameterConstants.LOCATION_ID to DataStoreManager.readFromPreferences(LoginConstants.DEVICE_LOCATION_ID, ""),
            ParameterConstants.DEVICE_ID to DataStoreManager.readFromPreferences(ParameterConstants.DEVICE_ID, ""),
            ParameterConstants.DEVICE_TYPE to ParameterConstants.DEVICE_TYPE_VAL
        )
        apiViewModel.callApi(UrlConstants.DEVICE_MENUS, queryMap = map)
    }catch (e: Exception) {e.printStackTrace()}
}

fun handleDeviceMenus(
    jsonResponse: JSONObject,
    menuViewModel: MenuViewModel,
    apiViewModel: ApiViewModel,
    navController: NavHostController,
    scope: CoroutineScope,
    isNaviageToDashboard: Boolean = false,
    snackbarController: SnackbarController,
    mqttSuccessMessage: String,
    mqttErrorMessage: String,
    isSuccessOnboarding: Boolean = false
){
    menuViewModel.setupMenus(jsonResponse)
    LogUtils.showLog("handleDeviceMenus_jsonResponse",jsonResponse.toString())
    if(isNaviageToDashboard) {
        DataStoreManager.saveToPreferences(LoginConstants.IS_LOGGED_IN, true)
        val isAllSubScribed = MqttManager.subscribeAfterLogin()
        if(!isAllSubScribed) {
                //DataStoreManager.readFromPreferences(ParameterConstants.BROKER_URL, "").isNullOrEmpty()) {
                /*if(BaseUtils.isDebuggable())*/ snackbarController.show(ErrorAppSnackBarData(mqttErrorMessage))
                return
        }
        else if(BaseUtils.isDebuggable()) snackbarController.show(SuccessAppSnackBarData(mqttSuccessMessage))
        //TODO check if all expected topics are subscribed before proceeding further
        if (isSuccessOnboarding) DataStoreManager.saveToPreferences("isSuccessOnboarding", true)
        navController.navigate(Screen.DashBoardUiScreen.route) {
            popUpTo(navController.graph.startDestinationId) {
                inclusive = true
            }
            launchSingleTop = true
        }
    }
}

private fun handleLocationConfig(
    jsonResponse: JSONObject,
    menuViewModel: MenuViewModel,
    apiViewModel: ApiViewModel,
    navController: NavHostController,
    scope: CoroutineScope
) {
    LogUtils.showLog("method","handleDeviceLogin")
    menuViewModel.insertMenu(jsonResponse)
    //call web-service for menus (deviceId, deviceType, customerId, businessLineId, locationId)
    callDeviceMenusApi(apiViewModel)
    //old code (Commented)
    /*if(!BaseUtils.isDebuggable()) callDeviceMenusApi(apiViewModel)
    else {
        DataStoreManager.saveToPreferences(LoginConstants.IS_LOGGED_IN, true)
        navController.navigate(Screen.DashBoardUiScreen.route) {
            popUpTo(navController.graph.startDestinationId) {
                inclusive = true
            }
            launchSingleTop = true
        }
    }*/

}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceSettings(
    navController: NavHostController,
    scope: CoroutineScope,
    sheetState: SheetState,
    showSheet: MutableState<Boolean>
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

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = dimensionResource(R.dimen.dp_16))
        ) {
            Text(
                text = stringResource(R.string.device_settings),
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


        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            IconWithTextRow(icon = painterResource(id = R.drawable.property_server),text = stringResource(id = R.string.server_configuration)) {
                navController.navigate(Screen.ServerConfigurationScreen.route)
            }
            if(BaseUtils.isDebuggable()) {
                IconWithTextRow(icon = painterResource(id = R.drawable.property_mqtt),text = stringResource(id = R.string.mqtt_configuration)) {
                    navController.navigate(Screen.MQQTConfigurationScreen.route)
                }
            }
            IconWithTextRow(icon = painterResource(id = R.drawable.property_info),text = stringResource(id = R.string.app_information)) {
                navController.navigate(Screen.AppInfo.route)
            }
            IconWithTextRow(icon = painterResource(id = R.drawable.property_update),text = stringResource(id = R.string.check_app_update)) { }
        }
    }
}


@Composable
fun IconWithTextRow(
    icon: Painter,
    text: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp)
    ) {

        Row(modifier = Modifier, verticalAlignment = Alignment.CenterVertically) {

            Image(
                painter = icon,
                contentDescription = null
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = text,
                style = CommonTypography.current.textSemiBold
            )
        }
    }
}

data class DetailsItem(
    val icon: Painter,
    val heading: String,
    val subHeading: String
)

@Composable
fun DetailsView(icon: Painter,
                heading: String,
                subHeading: String)
{
    Card(
        modifier = Modifier
            .border(
                dimensionResource(R.dimen.dp_1),
                OutlineDefault,
                RoundedCornerShape(dimensionResource(R.dimen.dp_16))
            )
            .fillMaxWidth()
            .height(dimensionResource(R.dimen.dp_82)),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        ),
        shape = RoundedCornerShape(dimensionResource(R.dimen.dp_16)),
        colors = CardDefaults.cardColors(containerColor = OutlineDefault)
    ) {
        Row(modifier = Modifier
            .weight(1f)
            .fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier
                .weight(0.3f)
                .wrapContentWidth()
                .background(color = WhiteColor),
                horizontalAlignment = Alignment.Start
            ) {
                Image(painter = icon,
                    contentDescription = null,
                    Modifier
                        .padding(
                            vertical = dimensionResource(R.dimen.dp_10),
                            horizontal = dimensionResource(R.dimen.dp_16)
                        )
                        .size(dimensionResource(R.dimen.dp_60))
                )
            }
            Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_12)))

            Column(modifier = Modifier
                .weight(0.7f)
                .padding(end = dimensionResource(R.dimen.dp_16))) {
                Text(
                    text = heading,
                    style = CommonTypography.current.textSemiBold,
                    textAlign = TextAlign.Start
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    text = subHeading,
                    style = CommonTypography.current.noteText,
                    color = TextSubtext,
                    textAlign = TextAlign.Start
                )

            }

        }
    }
}

suspend fun handelUserLogin(
    context: Context,
    scope: CoroutineScope,
    apiViewModel: ApiViewModel,
    jsonResponse: JSONObject,
    snackbarController: SnackbarController,
    mqttSuccessMessage: String,
    mqttErrorMessage: String,
) {
    val token = "${jsonResponse.optString(ParameterConstants.TOKEN_TYPE)} ${jsonResponse.optString(ParameterConstants.ACCESS_TOKEN)}"
    val refreshToken = "${jsonResponse.optString(ParameterConstants.TOKEN_TYPE)} ${jsonResponse.optString(ParameterConstants.REFRESH_TOKEN)}"
    val expiry = jsonResponse.optLong(ParameterConstants.EXPIRES_IN)

    val isMqttSecure = extractBoolean(jsonResponse,ParameterConstants.IS_MQTT_SECURE,false)
    val mqttPort = extractInt(jsonResponse,ParameterConstants.MQTT_PORT,if(isMqttSecure) 8883 else 1883)
    val mqttDom = extractString(jsonResponse,ParameterConstants.MQTT_DOMAIN,extractString(jsonResponse,ParameterConstants.MQTT_IP,""))

    val fullMqttURL = (if(isMqttSecure) "ssl://" else "tcp://")+mqttDom+":"+mqttPort
    LogUtils.showLog("fullMqttURL",fullMqttURL)


    LogUtils.showLog("Broker_URL_0",DataStoreManager.readFromPreferences(ParameterConstants.BROKER_URL,""))
    //save in pref for Now if fullMqttURL is valid & connect to mqtt after successful login & menu init
    if(!fullMqttURL.trim().equals(DataStoreManager.readFromPreferences(ParameterConstants.BROKER_URL,"").trim())) {
        val isConnected = checkMQTTURL(context, scope, fullMqttURL)
        /*if(validateMQTTUrl(fullMqttURL)) DataStoreManager.saveToPreferences(ParameterConstants.BROKER_URL,fullMqttURL)
        else DataStoreManager.saveToPreferences(ParameterConstants.BROKER_URL,"") */
        LogUtils.showLog("Broker_URL_1", DataStoreManager.readFromPreferences(ParameterConstants.BROKER_URL, ""))
        if (!isConnected){//DataStoreManager.readFromPreferences(ParameterConstants.BROKER_URL, "").isNullOrEmpty()) {
            /*if(BaseUtils.isDebuggable())*/ snackbarController.show(ErrorAppSnackBarData(mqttErrorMessage))
            return
        }
        else if(BaseUtils.isDebuggable()) snackbarController.show(SuccessAppSnackBarData(mqttSuccessMessage))
    }

    if (token.isNotEmpty()) {
        DataStoreManager.saveToPreferences(ParameterConstants.ACCESS_TOKEN, token)
        DataStoreManager.saveToPreferences(ParameterConstants.REFRESH_TOKEN, refreshToken)
        DataStoreManager.setAccessTokenTime(expiry)
        /**
        apiViewModel.callApi(UrlConstants.DEVICE_CHECK,DataStoreManager.readFromPreferences(ParameterConstants.DEVICE_ID,""))
         */
        apiViewModel.callApi(UrlConstants.USER_DETAILS)
    }

}

fun handelUserDetails(
    apiViewModel: ApiViewModel,
    jsonResponse: JSONObject,
    password: MutableState<String>,
    navController: NavHostController,
    snackbarController: SnackbarController,
    mqttUrlErrorMessage: String,
    serverUrlErrorMessage: String
){
    if(DataStoreManager.readFromPreferences(ParameterConstants.BROKER_URL,"").isNullOrEmpty()){
        //give error & return
    }
    val userId = extractString(jsonResponse, ParameterConstants.ID)
    val userName = extractString(jsonResponse, ParameterConstants.USERNAME)
    val userFirstName = extractString(jsonResponse, ParameterConstants.USER_FIRST_NAME, "")
    val userLastName = extractString(jsonResponse, ParameterConstants.USER_LAST_NAME, "")
    val customerId = extractString(jsonResponse, ParameterConstants.CUSTOMER_ID)
    val userRootLocationId = extractString(jsonResponse, ParameterConstants.LOCATION_ID)
    LogUtils.showLog("customerId", "LoginScreenContent: $customerId")
    if (customerId.isNotEmpty()) {
        val deviceLoginSuccess = DataStoreManager.readFromPreferences(LoginConstants.DEVICE_LOGIN_SUCCESS, false)
        val deviceCustomerID = DataStoreManager.readFromPreferences(LoginConstants.DEVICE_CUSTOMER_ID, "")
        val deviceLocationId = DataStoreManager.readFromPreferences(LoginConstants.DEVICE_LOCATION_ID, "")
        val deviceLocationPath = DataStoreManager.readFromPreferences(LoginConstants.DEVICE_LOCATION_PATH, "")
        if (deviceCustomerID.isNotEmpty() && deviceLocationPath.isNotEmpty() && (!deviceCustomerID.equals(customerId, true) || !deviceLocationPath.contains(userRootLocationId))) {
            ToastUtils.showLongToast(
                if (!deviceCustomerID.equals(
                        customerId,
                        true
                    )
                ) R.string.err_device_mapped_to_other_customer else R.string.err_device_mapped_to_other_location
            )
            return
        }

        LogUtils.showLog("Login Conditions", "LoginScreenContent: $deviceLoginSuccess \n $deviceCustomerID \n $deviceLocationPath")
        DataStoreManager.saveToPreferences(ParameterConstants.USER_FIRST_NAME, userFirstName)
        DataStoreManager.saveToPreferences(ParameterConstants.USER_LAST_NAME, userLastName)

        DataStoreManager.saveToPreferences(ParameterConstants.USERNAME, userName)
        DataStoreManager.saveToPreferences(ParameterConstants.PASSWORD, password.value)
        DataStoreManager.saveToPreferences(ParameterConstants.USER_FIRST_NAME, userFirstName)
        DataStoreManager.saveToPreferences(ParameterConstants.USER_LAST_NAME, userLastName)
        DataStoreManager.saveToPreferences(ParameterConstants.USER_ID, userId)
        DataStoreManager.saveToPreferences(LoginConstants.USER_CUSTOMER_ID, customerId)
        DataStoreManager.saveToPreferences(ParameterConstants.CUSTOMER_ID, customerId)
        /**
        DataStoreManager.saveToPreferences(ParameterConstants.LOCATION_ID, userRootLocationId)
         */
        DataStoreManager.saveToPreferences(LoginConstants.USER_ROOT_LOCATION_ID, userRootLocationId)

        if (deviceLoginSuccess && deviceCustomerID.isNotEmpty() && deviceLocationPath.isNotEmpty()) {

            val jsonRequest = JSONObject().apply {
                put(ParameterConstants.CLIENT_ID, DataStoreManager.readFromPreferences(ParameterConstants.CLIENT_ID, ""))
                put(ParameterConstants.CLIENT_SECRET, DataStoreManager.readFromPreferences(ParameterConstants.CLIENT_SECRET, ""))
                put(ParameterConstants.CUSTOMER_ID, DataStoreManager.readFromPreferences(ParameterConstants.CUSTOMER_ID, ""))
                put(ParameterConstants.LOCATION_ID, deviceLocationId)
                put(ParameterConstants.USER_ID, DataStoreManager.readFromPreferences(ParameterConstants.USER_ID, ""))
            }

            apiViewModel.callApi(
                UrlConstants.DEVICE_LOGIN,
                jsonRequest = jsonRequest
            )
        }
        else navController.navigate(Screen.DeviceLocationList.route)

    }
}

fun handelDeviceCheck(
    apiViewModel: ApiViewModel,
    jsonResponse: JSONObject,
){
    val data = extractJSONObject(jsonResponse, ParameterConstants.DATA, jsonResponse)
    val devicePkId = extractString(data, ParameterConstants.ID, "")
    val deviceId = extractString(data, ParameterConstants.DEVICE_KEY, "")
    val clientId = extractString(data, ParameterConstants.CLIENT_ID, "")
    val clientSecret = extractString(data, ParameterConstants.CLIENT_SECRET, "")
    val deviceCustomerId = extractString(data, ParameterConstants.CUSTOMER_ID, "")
    val deviceLocationId = extractString(data, ParameterConstants.LOCATION_ID, "")
    val deviceLocationPath = extractString(data, ParameterConstants.PATH, "")
    if (devicePkId.isNotEmpty() && clientId.isNotEmpty() && clientSecret.isNotEmpty() && deviceLocationId.isNotEmpty() && deviceCustomerId.isNotEmpty()) {
        DataStoreManager.saveToPreferences(
            ParameterConstants.DEVICE_ID,
            deviceId
        )
        DataStoreManager.saveToPreferences(
            ParameterConstants.CLIENT_ID,
            clientId
        )
        DataStoreManager.saveToPreferences(
            ParameterConstants.CLIENT_SECRET,
            clientSecret
        )
        DataStoreManager.saveToPreferences(LoginConstants.DEVICE_PK_ID, devicePkId)
        DataStoreManager.saveToPreferences(
            LoginConstants.DEVICE_CUSTOMER_ID,
            deviceCustomerId
        )
        DataStoreManager.saveToPreferences(
            LoginConstants.DEVICE_LOCATION_ID,
            deviceLocationId
        )
        DataStoreManager.saveToPreferences(
            LoginConstants.DEVICE_LOCATION_PATH,
            deviceLocationPath
        )
    }
    apiViewModel.callApi(UrlConstants.USER_DETAILS)
}


@Composable
fun rememberKeyboardVisibility(): Boolean {
    val imeInsets = WindowInsets.ime
    val density = LocalDensity.current
    val imeBottom = imeInsets.getBottom(density)
    return imeBottom > 0
}
