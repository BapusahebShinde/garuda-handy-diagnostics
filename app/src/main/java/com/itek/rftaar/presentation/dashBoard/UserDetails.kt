package com.itek.rftaar.presentation.dashBoard

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController
import com.itek.rftaar.R
import com.itek.rftaar.api.constants.ParameterConstants
import com.itek.rftaar.core.common.constants.LoginConstants
import com.itek.rftaar.core.common.utils.BaseUtils
import com.itek.rftaar.core.common.utils.FileUtils
import com.itek.rftaar.core.common.utils.LogUtils
import com.itek.rftaar.core.database.AppDatabase
import com.itek.rftaar.core.database.DataStoreManager
import com.itek.rftaar.mqtt.MqttManager
import com.itek.rftaar.presentation.commonComp.BottomSheetTextIconRow
import com.itek.rftaar.presentation.commonComp.BottomSheetType
import com.itek.rftaar.presentation.commonComp.CircularIcon
import com.itek.rftaar.presentation.commonComp.CircularText
import com.itek.rftaar.presentation.commonComp.DeviceSettingItem
import com.itek.rftaar.presentation.commonComp.ItekFooter
import com.itek.rftaar.presentation.commonComp.RowWithIcons
import com.itek.rftaar.presentation.commonComp.SessionBottomSheet
import com.itek.rftaar.presentation.commonComp.SessionUiActions
import com.itek.rftaar.presentation.commonComp.SessionUiState
import com.itek.rftaar.presentation.commonComp.getInitials
import com.itek.rftaar.presentation.commonComp.openSheet
import com.itek.rftaar.presentation.navigation.Screen
import com.itek.rftaar.ui.theme.BackGround
import com.itek.rftaar.ui.theme.BlackColor
import com.itek.rftaar.ui.theme.CommonTypography
import com.itek.rftaar.ui.theme.OutlineDefault
import com.itek.rftaar.ui.theme.RedColor
import com.itek.rftaar.ui.theme.WhiteColor
import com.itek.rftaar.ui.theme.Yellow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun UserDetails(modifier: Modifier, navController: NavHostController) {
    BackHandler(enabled = true) {
        if(navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED)
        navController.popBackStack()
    }
    Scaffold(
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BackGround),
                contentAlignment = Alignment.BottomCenter
            ) { UserDetailScreenBottomBar() }
        },
        modifier = modifier.background(BackGround)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .background(BackGround)
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            UserDetailScreenContent(navController)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailScreenContent(navController: NavHostController) {

    val menuItems = listOf(
        R.drawable.property_info to R.string.app_information,
        R.drawable.property_info to R.string.session_upload_count,
        R.drawable.property_logs to R.string.logs_information,
        R.drawable.property_support to R.string.contact_support_,
        R.drawable.property_signout to R.string.logout
    )
    val userFullName = DataStoreManager.readFromPreferences(ParameterConstants.USER_FIRST_NAME, "") +" "+ DataStoreManager.readFromPreferences(
        ParameterConstants.USER_LAST_NAME, "")
    val initials = getInitials(userFullName)
    val userId = DataStoreManager.readFromPreferences(ParameterConstants.USERNAME , "")
    val deviceLocationName = DataStoreManager.readFromPreferences(LoginConstants.DEVICE_LOCATION_NAME, "")
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { sheetValue ->
            sheetValue != SheetValue.Hidden
        }
    )
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val backgroundColor = Brush.verticalGradient(
        listOf(
            Color(0xFFFECF53), Color(0xFFF0B125)
        )
    )
    val showSheet = remember { mutableStateOf(false) }
    val currentSheet = remember { mutableStateOf(BottomSheetType.NONE) }
    val db = AppDatabase.getDbInstance(context)
    val notificationUserId = DataStoreManager.readFromPreferences(ParameterConstants.USER_ID, "")
    val notificationCount = db.menuNotificationDao().getTotalUnreadCount(notificationUserId).collectAsState(initial = 0)
    LogUtils.showLog("notificationCount", "UserDetailScreenContent: ${notificationCount.value} ")

    Box(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = dimensionResource(id = R.dimen.dp_16))
                .padding(top = dimensionResource(id = R.dimen.dp_16)),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top
        ) {

            UserDetailScreenHeader(navController)

            Text(
                text = stringResource(id = R.string.profile),
                style = CommonTypography.current.headingH1,
                modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.dp_10))
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dimensionResource(id = R.dimen.dp_163))
                    .border(
                        dimensionResource(id = R.dimen.dp_1),
                        OutlineDefault,
                        RoundedCornerShape(dimensionResource(id = R.dimen.dp_24))
                    ),
                elevation = CardDefaults.cardElevation(
                    dimensionResource(id = R.dimen.dp_24)
                ),
                shape = RoundedCornerShape(dimensionResource(id = R.dimen.dp_24)),
                colors = CardDefaults.cardColors(
                    containerColor = WhiteColor
                )
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = dimensionResource(id = R.dimen.dp_16))
                ) {

                    /* ---------------- CENTER CONTENT ---------------- */

                    Column(
                        modifier = Modifier.align(Alignment.TopCenter),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {

                        Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.dp_8)))
                        CircularText(
                            text = initials,
                            backgroundColor = backgroundColor,
                            textStyle = CommonTypography.current.bigFont.copy(WhiteColor)
                        )

                        Text(
                            text = userFullName,
                            style = CommonTypography.current.textSemiBold,
                            modifier = Modifier
                                .padding(
                                    top = dimensionResource(id = R.dimen.dp_12)
                                )
                                .basicMarquee()
                        )

                        Text(
                            text = userId,
                            style = CommonTypography.current.smallTxt,
                            modifier = Modifier.basicMarquee()
                        )

                        Text(
                            text = deviceLocationName,
                            style = CommonTypography.current.smallTxt,
                            modifier = Modifier.basicMarquee()
                        )
                    }
                    LogUtils.showLog("Deatils", "UserDetailScreenContent: $userFullName \n $userId \n  $deviceLocationName")

                    /* ---------------- END ICON ---------------- */

                    /**CircularIcon(
                        iconRes = R.drawable.property_edit,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(end = dimensionResource(id = R.dimen.dp_16))
                            .size(dimensionResource(R.dimen.dp_44))
                            .clickable(
                                onClick = {},
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ),
                        isShadow = false,
                        backgroundColor = SolidColor(TabColor),
                        outlineColor = Color.Transparent
                    )*/
                }
            }



            Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.dp_12)))
            val titleNotification = stringResource(id = R.string.notifications)
            Row(
                modifier = Modifier
                    .height(dimensionResource(id = R.dimen.dp_56))
                    .background(
                        WhiteColor,
                        RoundedCornerShape(dimensionResource(id = R.dimen.dp_24))
                    )
                    .border(
                        width = dimensionResource(id = R.dimen.dp_1),
                        color = OutlineDefault,
                        shape = RoundedCornerShape(dimensionResource(id = R.dimen.dp_24))
                    )
                    .padding(
                        vertical = dimensionResource(id = R.dimen.dp_16),
                        horizontal = dimensionResource(id = R.dimen.dp_16)
                    )
                    .clickable {
                        val route = Screen.NotificationScreen.createRoute(label = titleNotification)
                        navController.navigate(route)
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Row(
                    modifier = Modifier.weight(0.7f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.bell),
                        contentDescription = null,
                        modifier = Modifier.padding(end = dimensionResource(id = R.dimen.dp_16)),
                        tint = Yellow
                    )

                    Text(
                        text = titleNotification,
                        style = CommonTypography.current.textSubtext,
                        color = BlackColor
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(0.3f),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    if (notificationCount.value > 0){
                        CircularText(
                            circleSize = dimensionResource(id = R.dimen.dp_24),
                            text = notificationCount.value.toString(),
                            backgroundColor = SolidColor(RedColor),
                            textStyle = CommonTypography.current.textSemiBold.copy(color = WhiteColor, fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.dp_12)))

            val filteredMenuItems = menuItems.filter {
                when (it.second) {
                    R.string.contact_support_,
                    R.string.logs_information,
                    R.string.session_upload_count -> BaseUtils.isDebuggable()
                    else -> true
                }
            }

            RowWithIconsList(
                items = filteredMenuItems,
                onItemClicked = { selected ->
                    when (selected) {
                        R.string.app_information -> {
                            navController.navigate(Screen.AppInfo.route)
                        }

                        R.string.session_upload_count -> {
                            navController.navigate(Screen.SessionUploadCount.route)
                        }

                        R.string.logs_information -> {
                            scope.launch {
                                openSheet(
                                    scope = scope,
                                    sheetState = sheetState,
                                    showSheet = showSheet,
                                    currentSheet = currentSheet,
                                    sheet = BottomSheetType.LOGS
                                )
                            }
                        }

                        R.string.contact_support_ -> {
                            navController.navigate(Screen.ContactSupport.route)
                        }

                        R.string.logout -> {
                            scope.launch {
                                openSheet(
                                    scope = scope,
                                    sheetState = sheetState,
                                    showSheet = showSheet,
                                    currentSheet = currentSheet,
                                    sheet = BottomSheetType.LOGOUT
                                )
                            }
                        }
                    }
                }
            )
        }

        if (showSheet.value){
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
                    BottomSheetType.LOGS -> Box(
                        modifier = Modifier
                            .background(WhiteColor)
                            .fillMaxWidth()
                            .fillMaxHeight(0.26f)
                    ) {
                        LogsOptions(
                            showSheet = showSheet,
                            sheetState = sheetState,
                            scope = scope,
                        )
                    }

                    BottomSheetType.LOGOUT -> Box(
                        modifier = Modifier
                            .background(WhiteColor)
                            .fillMaxWidth()
                            .fillMaxHeight(0.36f)
                    ) {
                        LogoutAlert(
                            showSheet = showSheet,
                            sheetState = sheetState,
                            scope = scope,
                            navController = navController
                        )
                    }

                    else -> {
                        LogUtils.showLog("TAG", "UserDetailScreenContent: ")
                    }
                }
            }
        }
    }


}

/**@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogOutBottomSheet(sheetState: SheetState, scope: CoroutineScope) {
    val context = LocalContext.current
    val activity = context as? Activity
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.3f)
    ) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(dimensionResource(R.dimen.dp_16)), horizontalAlignment = Alignment.CenterHorizontally,) {
            Row() {
                Text(text = stringResource(R.string.logout),
                    style = CommonTypography.current.textSemiBold)

                Image(painter = painterResource(R.drawable.clear),
                    contentDescription = null,
                    modifier = Modifier.clickable(
                        onClick = {
                            scope.launch {
                                sheetState.hide()
                            }
                        },
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ),
                    alignment = Alignment.TopEnd)
            }

            Text(text = stringResource(R.string.confirm_logout),
                style = CommonTypography.current.headingH1,
                modifier = Modifier.padding(top = dimensionResource(R.dimen.dp_16), bottom = dimensionResource(R.dimen.dp_8)))

            Text(text = stringResource(R.string.end_your_current_session),
                style = CommonTypography.current.textSubtext)

            Column(modifier = Modifier
                .fillMaxWidth()
                .padding(top = dimensionResource(R.dimen.dp_24)), verticalArrangement = Arrangement.Bottom) {
                CommonButton(
                    text = stringResource(R.string.logout),
                    onClick = {
                        scope.launch {
                            activity?.let {
                                DataStoreManager.saveToPreferences(LoginConstants.IS_LOGGED_IN, false)
                                val intent = Intent(it, MainActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                it.startActivity(intent)
                                it.finish()
                            }
                            sheetState.hide()
                        }
                    }
                )
            }

        }
    }
}*/

@Composable
fun UserDetailScreenBottomBar() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(WhiteColor)
    ) {
        HorizontalDivider(color = OutlineDefault)

        Row(
            modifier = Modifier.padding(top = dimensionResource(R.dimen.dp_16)),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.dp_10)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ItekFooter()
        }
    }
}

@Composable
fun UserDetailScreenHeader(navController: NavHostController) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
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
}

@Composable
fun RowWithIconsList(
    items: List<Pair<Int, Int>>,
    onItemClicked: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth()
    ) {
        items(items) { item ->

            RowWithIcons(
                icon = item.first,
                title = stringResource(id = item.second),
                onClick = { onItemClicked(item.second) }
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogsOptions(
    scope: CoroutineScope,
    sheetState: SheetState,
    showSheet: MutableState<Boolean>,
) {

    val context = LocalContext.current
    val items = FileUtils.getDirList().map { dir->
        DeviceSettingItem(
            iconRes = R.drawable.property_logs, title = "$dir Logs"
        ) {
            FileUtils.zipAndShare(context,dir)
        }
    }

    /**remember {
        listOf(
            DeviceSettingItem(
                iconRes = R.drawable.property_logs, title = apiLogs
            ) {
                FileUtils.zipAndShare(context,"API")
            }, DeviceSettingItem(
                iconRes = R.drawable.property_logs, title = mqttLogs
            ) {
                FileUtils.zipAndShare(context,"MQTT")
            })
    }*/

    BottomSheetTextIconRow(
        showSheet = showSheet,
        sheetState = sheetState,
        scope = scope,
        title = stringResource(R.string.logs_information),
        items = items
    )
}

fun logOut(scope: CoroutineScope,navController: NavHostController){
    scope.launch {
        DataStoreManager.saveToPreferences(LoginConstants.IS_LOGGED_IN, false)
        MqttManager.unsubscribeAfterLogout()
        navController.navigate(Screen.LoginScreen.route){ popUpTo(0) { inclusive = true }}
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogoutAlert(
    showSheet: MutableState<Boolean>,
    sheetState: SheetState,
    scope: CoroutineScope,
    navController: NavHostController,
) {


    val uiState = SessionUiState(
        title = stringResource(R.string.logout),
        heading = stringResource(R.string.confirm_logout),
        subHeading = AnnotatedString(stringResource(R.string.logout_msg)),
        primaryButtonText = stringResource(R.string.logout),
        secondaryButtonText = stringResource(R.string.logout)
    )

    val actions = SessionUiActions(
        onClose = {
            scope.launch {
                sheetState.hide()
                showSheet.value = false
            }
        }, onSecondaryAction = {
        }, onPrimaryAction = { logOut(scope,navController)}
            /*scope.launch {
                DataStoreManager.saveToPreferences(LoginConstants.IS_LOGGED_IN, false)
                navController.navigate(Screen.LoginScreen.route)
            }
        }*/
    )

    SessionBottomSheet(
        uiState = uiState, actions = actions, isSecondaryButton = false
    )
}