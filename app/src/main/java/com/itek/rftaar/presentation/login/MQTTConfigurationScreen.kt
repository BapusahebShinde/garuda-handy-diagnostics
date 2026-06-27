package com.itek.rftaar.presentation.login

import android.content.Context
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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import com.itek.rftaar.core.database.DataStoreManager
import com.itek.rftaar.mqtt.MqttManager
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun MQQTConfigurationScreen(modifier: Modifier, navController: NavHostController,apiViewModel: ApiViewModel = hiltViewModel()) {

    val scope = rememberCoroutineScope()
    BackHandler(enabled = false) {
        if(navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED)
        navController.popBackStack()
    }
    val urlDefaultValue = DataStoreManager.readFromPreferences(ParameterConstants.BROKER_URL,"")//, BrokerConstants.BROKER_URL)
    val serverMQTT = remember { mutableStateOf(urlDefaultValue) }
    val context = LocalContext.current
    val snackbarController = remember { SnackbarController() }

    Scaffold(
        bottomBar = {
            Box(
                modifier = Modifier.background(WhiteColor).fillMaxWidth(),
                contentAlignment = Alignment.BottomCenter
            ) {
                MQQTConfigurationBottomBar(apiViewModel,scope,serverMQTT,context,snackbarController, navController)
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
            MQQTConfigurationContent(navController,apiViewModel,serverMQTT,scope,snackbarController)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarContent(
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
            ))
    }
}
@Composable
fun MQQTConfigurationContent(
    navController: NavHostController,
    apiViewModel: ApiViewModel,
    serverMQTT: MutableState<String>,
    scope: CoroutineScope,
    snackbarController: SnackbarController,
) {

    val focusManager = LocalFocusManager.current
    Box( modifier = Modifier
        .fillMaxSize()
        .background(Color.Transparent)
        .clickable(
            enabled = true,
            onClick = {},
            indication = null,
            interactionSource = remember { MutableInteractionSource() }
        )) {

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
                TopBarContent(navController = navController)
            }

            Text(
                text = stringResource(id = R.string.mqtt_configuration),
                style = CommonTypography.current.headingH1,
                modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.dp_10))
            )

            Text(
                text = stringResource(id = R.string.check_your_server_mqtt),
                style = CommonTypography.current.buttonSemiBold,
                color = TextSubtext,
                modifier = Modifier.padding(top = dimensionResource(id = R.dimen.dp_14) , bottom = dimensionResource(id = R.dimen.dp_16))
            )
            CommonTextField(
                config = TextFieldConfig(
                    value = serverMQTT.value,
                    onValueChange = { serverMQTT.value = it },
                    label = stringResource(id = R.string.mqtt_url),
                    isServerUrl = true,
                    imeAction = ImeAction.Done,
                    onImeAction = {
                        focusManager.clearFocus()
                    }
                )
            )

            Text(
                text = stringResource(id = R.string.mqtt_url_note),
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
fun MQQTConfigurationBottomBar(
    apiViewModel: ApiViewModel,
    scope: CoroutineScope,
    serverMQTT: MutableState<String>,
    context: Context,
    snackbarController: SnackbarController,
    navController: NavController
) {
    val successMessage = stringResource(R.string.mqttsuccessMessage)
    val errorMessage = stringResource(R.string.mqtterrorMessage)
    val mqttStatus = MqttManager.connectionStatus.collectAsState(initial = "")
    val lastStatus = remember { mutableStateOf<String?>(null) }

    /*LaunchedEffect(mqttStatus.value) {
        val isConnected =mqttStatus.value.equals("Connected",true)
        if (isConnected) DataStoreManager.saveToPreferences(ParameterConstants.BROKER_URL,serverMQTT.value)
            snackbarController.show(
                AppSnackBarData(
                    icon = if (isConnected) R.drawable.property_1_success else R.drawable.property_1_error,
                    message = if (isConnected) successMessage else errorMessage,// MqttManager.connectionStatus.value,
                    showCancel = true
                )
            )
            if (isConnected) {
                delay(500)
                //if(navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED)
                navController.popBackStack()
            }
    }*/

    Column(modifier = Modifier.fillMaxWidth().padding(dimensionResource(id = R.dimen.dp_16))) {
        CommonButton(
            text = stringResource(id  = R.string.save_validate),
            onClick = {
                scope.launch {
                    if(!serverMQTT.value.trim().matches(Regex("(?i)(^(tcp:|wcp:|mqtt:|mqtts:|ws:|wss:|ssl:)//.*$)"))){
                        snackbarController.show(ErrorAppSnackBarData("Invalid MQTT URL"))
                     return@launch
                    }

                    MqttManager.initialize(context, serverMQTT.value.trim(), isSettingServerUrl = true)

                    val result = MqttManager.connectionStatus.first { it == "Connected" || it == "Failed to connect" }

                    if (result == "Connected") {
                        DataStoreManager.saveToPreferences(ParameterConstants.BROKER_URL, serverMQTT.value)
                    }

                    snackbarController.show(
                        AppSnackBarData(
                            icon = if (result == "Connected") R.drawable.property_1_success else R.drawable.property_1_error,
                            message = if (result == "Connected") successMessage else errorMessage,
                            showCancel = true
                        )
                    )

                    if (result == "Connected") {
                        delay(500)
                        if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                            navController.popBackStack()
                        }
                    }

                    /*MqttManager.connectionStatus.drop(1)
                        //.filter { it == "Connected" || it == "Failed to connect" }
                        .first()
                        .let {
                            if (it == "Connected") DataStoreManager.saveToPreferences(ParameterConstants.BROKER_URL,serverMQTT.value)
                            snackbarController.show(
                                AppSnackBarData(
                                    icon = if (it == "Connected") R.drawable.property_1_success
                                    else R.drawable.property_1_error,
                                    message = if (it == "Connected") successMessage else errorMessage,// MqttManager.connectionStatus.value,
                                    showCancel = true
                                )
                            )
                            if (it == "Connected") {
                             delay(500)
                             if(navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) navController.popBackStack()
                            }
                        }*/

                    /*if(MqttManager.connectionStatus.value.equals("Connected",true)){
                        delay(1200)
                        navController.popBackStack()
                    }*/
                }

            }
        )
    }
}

fun validateMQTTUrl(serverMQTT:String):Boolean{
   return serverMQTT.trim().matches(Regex("(?i)(^(tcp:|wcp:|mqtt:|mqtts:|ws:|wss:|ssl:)//.*$)"))
}

suspend fun checkMQTTURL(context: Context, scope: CoroutineScope, serverMQTT: String): Boolean{
    if(!validateMQTTUrl(serverMQTT)) return false
        MqttManager.initialize(context,serverMQTT.trim())
        val result = MqttManager.connectionStatus.first { it == "Connected" || it == "Failed to connect" }
        if (result == "Connected") DataStoreManager.saveToPreferences(ParameterConstants.BROKER_URL, serverMQTT)
        else DataStoreManager.saveToPreferences(ParameterConstants.BROKER_URL, "")
    return result == "Connected"

}