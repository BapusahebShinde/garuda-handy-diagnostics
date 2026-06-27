package com.itek.rftaar.presentation.dashBoard

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController
import com.itek.rftaar.R
import com.itek.rftaar.api.constants.ParameterConstants
import com.itek.rftaar.core.common.constants.DateFormatUtils
import com.itek.rftaar.core.common.constants.DateFormatUtils.API_DATE_TIME_FORMAT
import com.itek.rftaar.core.common.utils.LogUtils
import com.itek.rftaar.core.common.utils.ParseUtils.extractString
import com.itek.rftaar.core.database.AppDatabase
import com.itek.rftaar.core.database.DataStoreManager
import com.itek.rftaar.data.entity.MenuNotificationEntity
import com.itek.rftaar.presentation.commonComp.ItekFooter
import com.itek.rftaar.presentation.commonComp.TopBarContent
import com.itek.rftaar.ui.theme.CommonTypography
import com.itek.rftaar.ui.theme.OutlineDefault
import com.itek.rftaar.ui.theme.TextSubtext
import com.itek.rftaar.ui.theme.WhiteColor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NotificationDetailScreen(
    modifier: Modifier,
    navController: NavHostController,
    menuCode: String,
    label: String,
    searchParams: Map<String, Any>
) {

    val type = extractString(searchParams,"type")
    val scope = rememberCoroutineScope()

    BackHandler(enabled = true) {
        if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) navController.popBackStack()
    }

    Scaffold(
        bottomBar = {
            NotificationDetailBottomBar()
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .background(WhiteColor)
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            NotificationDetailContent(navController,label,menuCode, type,scope)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NotificationDetailContent(navController: NavHostController, label: String,menuCode: String,type: String, scope: CoroutineScope) {

    val context = LocalContext.current
    val db = AppDatabase.getDbInstance(context)
    val userId = DataStoreManager.readFromPreferences(ParameterConstants.USER_ID,"")
    val notificationDetails = db.menuNotificationDao().getNotificationList(type = type,userId).collectAsState(emptyList())

    LogUtils.showLog("notificationDetails", "NotificationContent: ${notificationDetails.value} \n ${notificationDetails.value?.size}")

    LaunchedEffect(Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            db.menuNotificationDao().updateReadCount(type = type,userId)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(modifier = Modifier, verticalArrangement = Arrangement.Top) {
                TopBarContent(
                    label = type ,
                    onBackClickL = {
                        if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) navController.popBackStack()
                    }, onSettingClick = {

                    }, isSetting = false
                )
            }
            HorizontalDivider(thickness = dimensionResource(R.dimen.dp_1), color = OutlineDefault)
            Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_16)))
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(
                    items = notificationDetails.value
                ) { index, item ->

                    BatchNotificationItem(
                        item = item,
                        showArrow = index == 0, // only first item
                        modifier = Modifier.padding(
                            horizontal = dimensionResource(R.dimen.dp_16),
                            vertical = dimensionResource(R.dimen.dp_12)
                        ),
                        onClick = {
                            if(index==0 && item.typeId.isNotEmpty()){
                                CoroutineScope(Dispatchers.IO).launch {
                                    val menu = db.menuDao().getMenuByCode(item.typeId)
                                    if(menu!=null) {
                                        scope.launch {
                                            //navController.popBackStack(Screen.DashBoardUiScreen.route,inclusive = false)
                                            navigateMenu(navController,menu)
                                            /*val dashboard = Screen.DynamicDashboard(
                                                code = menu.code,
                                                label = menu.label,
                                                params = emptyMap()
                                            )
                                            val parentCode = menu.parentCode
                                            navController.navigate(dashboard.createRoute(parentCode))*/
                                        }
                                    }
                                }
                            }
                        }
                    )

                    if (index != notificationDetails.value.lastIndex) {

                        HorizontalDivider(
                            color = OutlineDefault,
                            thickness = dimensionResource(R.dimen.dp_1),
                            modifier = Modifier.padding(
                                horizontal = dimensionResource(R.dimen.dp_16)
                            )
                        )
                    }
                }
            }

        }
    }
}

@Composable
fun NotificationDetailBottomBar() {
    ItekFooter()
}

@Immutable
data class BatchNotificationUi(
    val id: String,
    val title: String,
    val dateTime: String
)

@Composable
fun BatchNotificationItem(
    item: MenuNotificationEntity,
    showArrow: Boolean,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {

    Row(
        modifier = modifier.fillMaxWidth().clickable(
                enabled = onClick != null
            ) {
                onClick?.invoke()
            }, verticalAlignment = Alignment.CenterVertically
    ) {

        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
            Text(text = item.title,
                style = CommonTypography.current.noteText.copy(color = TextSubtext)
            )
            Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_4)))
            Text(text = item.message,
                style = CommonTypography.current.noteText.copy(color = TextSubtext),
                maxLines = 3
            )
            Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_4)))
            Text(text = DateFormatUtils.formatToDisplayTime(item.date, inputFormat = API_DATE_TIME_FORMAT),
                style = CommonTypography.current.noteText.copy(color = TextSubtext)
            )

        }

        if (showArrow) {
            Icon(painter = painterResource(R.drawable.icon__next),
                contentDescription = null)
        }
    }
}
