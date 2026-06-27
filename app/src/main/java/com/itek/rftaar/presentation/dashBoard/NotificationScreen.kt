package com.itek.rftaar.presentation.dashBoard

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import com.itek.rftaar.R
import com.itek.rftaar.api.constants.ParameterConstants
import com.itek.rftaar.core.common.constants.DateFormatUtils
import com.itek.rftaar.core.common.constants.DateFormatUtils.API_DATE_TIME_FORMAT
import com.itek.rftaar.core.common.utils.LogUtils
import com.itek.rftaar.core.database.AppDatabase
import com.itek.rftaar.core.database.DataStoreManager
import com.itek.rftaar.data.model.MenuNotificationWithCount
import com.itek.rftaar.presentation.commonComp.CircularText
import com.itek.rftaar.presentation.commonComp.ItekFooter
import com.itek.rftaar.presentation.commonComp.TopBarContent
import com.itek.rftaar.presentation.navigation.Screen
import com.itek.rftaar.ui.theme.BlackColor
import com.itek.rftaar.ui.theme.CommonTypography
import com.itek.rftaar.ui.theme.OutlineDefault
import com.itek.rftaar.ui.theme.RedColor
import com.itek.rftaar.ui.theme.TabColor
import com.itek.rftaar.ui.theme.TextSubtext
import com.itek.rftaar.ui.theme.WhiteColor
import com.itek.rftaar.utils.CommonUtils.getMenuIconByCode


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NotificationScreen(
    modifier: Modifier,
    navController: NavController,
    menuCode: String,
    label: String,
    searchParams: Map<String, Any>,
) {

    BackHandler(enabled = true) {
        if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED)  navController.popBackStack()
    }

    Scaffold(
        bottomBar = {
            NotificationBottomBar()
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .background(WhiteColor)
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            NotificationContent(navController,label=stringResource(R.string.notifications))
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NotificationContent(navController: NavController,label:String) {

    val context = LocalContext.current
    val db = AppDatabase.getDbInstance(context)
    val userId = DataStoreManager.readFromPreferences(ParameterConstants.USER_ID,"")
    val notificationData = db.menuNotificationDao().getNotificationTypesList(userId).collectAsState(emptyList())

    LogUtils.showLog("notificationData", "NotificationContent: ${notificationData.value} \n ${notificationData.value?.size}")

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(modifier = Modifier, verticalArrangement = Arrangement.Top) {
                TopBarContent(
                    label=label,
                    onBackClickL = {
                        if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) navController.popBackStack()
                    }, onSettingClick = {

                    }, isSetting = false
                )
            }
            HorizontalDivider(thickness = dimensionResource(R.dimen.dp_1), color = OutlineDefault)

            LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .imePadding(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    contentPadding = PaddingValues(
                        vertical = dimensionResource(R.dimen.dp_12)
                    )
                ) {
              if(notificationData.value.isNullOrEmpty()){
                item {
                  Text(
                    text = stringResource(R.string.txt_no_data_found,label),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    style = CommonTypography.current.noteText,
                    color = RedColor
                  )
                }
              }
              else {
                items(items = notificationData.value ?: emptyList()) { item ->

                  NotificationListItem(
                    item = item,
                    modifier = Modifier,
                    onClick = {
                      val searchBundle = mapOf("type" to item.notification.type)

                      val route = Screen.NotificationDetailScreen.createRoute(code = item.notification.typeId, label = item.notification.title, params = searchBundle)

                      navController.navigate(route)
                    }
                  )

                  HorizontalDivider(
                    modifier = Modifier
                      .padding(horizontal = dimensionResource(R.dimen.dp_16))
                      .padding(top = dimensionResource(R.dimen.dp_12), bottom = dimensionResource(R.dimen.dp_16)),
                      color = OutlineDefault,
                      thickness = 1.dp
                  )
                }
              }
            }

        }
    }
}

@Composable
fun NotificationBottomBar() {
    ItekFooter()
}

@Composable
fun NotificationListItem(
    item: MenuNotificationWithCount,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {

    Row(modifier = modifier
        .padding(horizontal = dimensionResource(R.dimen.dp_16))
        .clickable(
            enabled = onClick != null
        ) {
            onClick?.invoke()
        }, horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = modifier
                .size(dimensionResource(R.dimen.dp_40))
                .background(
                    color = TabColor,
                    shape = RoundedCornerShape(dimensionResource(R.dimen.dp_8))
                ),
            contentAlignment = Alignment.Center
        ) {

            val iconId = getMenuIconByCode(item.notification.typeId)
            Icon(
                painter = painterResource(iconId),//R.drawable.stopicon),
                contentDescription = null,
            )
        }
        Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_8)))

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                text = item.notification.type,
                style = CommonTypography.current.textMedium.copy(
                    color = BlackColor
                )
            )

            Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_2)))

            Text(
                text = item.notification.title,
                style = CommonTypography.current.noteText.copy(
                    color = TextSubtext
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_2)))

            Text(
                text = DateFormatUtils.formatToDisplayTime(item.notification.date, inputFormat = API_DATE_TIME_FORMAT) ,
                style = CommonTypography.current.smallTxt,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

        }
        Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_14)))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            if (item.unreadCount > 0){
                CircularText(
                    circleSize = dimensionResource(id = R.dimen.dp_20),
                    text = item.unreadCount.toString(),
                    backgroundColor = SolidColor(RedColor),
                    textStyle = CommonTypography.current.smallTxt.copy(
                        color = WhiteColor,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}