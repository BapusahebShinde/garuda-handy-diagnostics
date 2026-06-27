package com.itek.rftaar.presentation.login

import androidx.activity.compose.BackHandler
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.itek.rftaar.R
import com.itek.rftaar.api.constants.ParameterConstants
import com.itek.rftaar.api.constants.UrlConstants
import com.itek.rftaar.core.database.DataStoreManager
import com.itek.rftaar.presentation.commonComp.AppSnackBar
import com.itek.rftaar.presentation.commonComp.AppSnackBarData
import com.itek.rftaar.presentation.commonComp.CircularIcon
import com.itek.rftaar.presentation.commonComp.CommonButton
import com.itek.rftaar.presentation.commonComp.CommonTextField
import com.itek.rftaar.presentation.commonComp.ErrorAppSnackBarData
import com.itek.rftaar.presentation.commonComp.SnackbarController
import com.itek.rftaar.presentation.commonComp.TextFieldConfig
import com.itek.rftaar.presentation.viewmodel.ApiViewModel
import com.itek.rftaar.ui.theme.BackGround
import com.itek.rftaar.ui.theme.CommonTypography
import com.itek.rftaar.ui.theme.TextSubtext
import com.itek.rftaar.ui.theme.WhiteColor
import com.itek.rftaar.utils.CommonUtils.chkNull
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ServerConfigurationScreen(modifier: Modifier, navController: NavHostController,apiViewModel: ApiViewModel = hiltViewModel()) {

    val scope = rememberCoroutineScope()
    val snackbarController = remember { SnackbarController() }
    BackHandler(enabled = false) {
        if(navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED)
        navController.popBackStack()
    }
    val urlDefaultValue = DataStoreManager.readFromPreferences(ParameterConstants.BASE_URL, UrlConstants.BASE_URL)
    val serverUrl = remember { mutableStateOf(urlDefaultValue) }
    Scaffold(
        bottomBar = {
            Box(
                modifier = Modifier.background(WhiteColor).fillMaxWidth(),
                contentAlignment = Alignment.BottomCenter
            ) {
                ServerConfigurationScreenBottomBar(apiViewModel,scope,serverUrl,snackbarController)
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
            ServerConfigurationScreenContent(navController,apiViewModel,serverUrl,scope,snackbarController)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerTopBarContent(
    navController: NavController,
) {
    Box(
        modifier = Modifier
            .background(BackGround)
            .fillMaxWidth()
            .padding(top = 8.dp),
        contentAlignment = Alignment.TopStart
    ) {
        CircularIcon(iconRes = R.drawable.property_arrow_back,
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
}

@Composable
fun ServerConfigurationScreenContent(
    navController: NavHostController,
    apiViewModel: ApiViewModel,
    serverUrl: MutableState<String>,
    scope: CoroutineScope,
    snackbarController:SnackbarController
) {

    val focusManager = LocalFocusManager.current
    val successMessage = stringResource(R.string.successMessage)
    val defaultErrorMessage = stringResource(R.string.err_server_no_connect_config)
    val response = apiViewModel.apiResult.collectAsState(null)
    LaunchedEffect(response.value) {
        if(response.value==null) return@LaunchedEffect
        val result = response.value!!
        if (!result.isSuccess) {
                snackbarController.show(
                    ErrorAppSnackBarData(chkNull(result.errMsg.toString(),defaultErrorMessage))
                )
                return@LaunchedEffect
        }
        else if (result.response != null) {
                val jsonResponse = result.response
                when (result.url) {
                    UrlConstants.VALIDATE_URL -> {
                        snackbarController.show(
                            AppSnackBarData(
                                icon = R.drawable.property_1_success,
                                message = successMessage,
                                showCancel = true
                            )
                        )

                        delay(1200)

                        DataStoreManager.saveToPreferences(
                            ParameterConstants.BASE_URL,
                            serverUrl.value
                        )
                        if(navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED)
                        navController.popBackStack()
                    }
                }
        }
    }

    Box() {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = dimensionResource(id = R.dimen.dp_16))
                .padding(horizontal = dimensionResource(id = R.dimen.dp_16))
                .clickable(
                    onClick = {
                        focusManager.clearFocus()
                    },
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top
        )
        {

            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                ServerTopBarContent(navController)
            }

            Text(
                text = stringResource(id = R.string.server_configuration),
                style = CommonTypography.current.headingH1,
                modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.dp_10))
            )

            Text(
                text = stringResource(id = R.string.check_your_server_url),
                style = CommonTypography.current.buttonSemiBold,
                color = TextSubtext,
                modifier = Modifier.padding(top = dimensionResource(id = R.dimen.dp_14) , bottom = dimensionResource(id = R.dimen.dp_16))
            )
            CommonTextField(
                config = TextFieldConfig(
                    value = serverUrl.value,
                    onValueChange = { serverUrl.value = it },
                    label = stringResource(id = R.string.server_url),
                    isServerUrl = true,
                    imeAction = ImeAction.Done,
                    onImeAction = {
                        focusManager.clearFocus()
                    }
                )
            )

            Text(
                text = stringResource(id = R.string.server_url_note),
                style = CommonTypography.current.noteText,
                modifier = Modifier.padding(top = dimensionResource(id = R.dimen.dp_8))
            )


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
fun ServerConfigurationScreenBottomBar(
    apiViewModel: ApiViewModel,
    scope: CoroutineScope,
    serverUrl: MutableState<String>,
    snackbarController:SnackbarController
) {
    Column(modifier = Modifier.fillMaxWidth().padding(dimensionResource(id = R.dimen.dp_16))) {
        CommonButton(
            text = stringResource(id  = R.string.save_validate),
            onClick = {
                if(!serverUrl.value.matches(Regex("(?i)(^https://.*$)"))){
                    snackbarController.show(
                        ErrorAppSnackBarData("Invalid Server URL")
                    )
                    return@CommonButton
                }
                scope.launch {
                    apiViewModel.callApi(url = UrlConstants.VALIDATE_URL, baseUrl =serverUrl.value)
                }
            }
        )
    }
}