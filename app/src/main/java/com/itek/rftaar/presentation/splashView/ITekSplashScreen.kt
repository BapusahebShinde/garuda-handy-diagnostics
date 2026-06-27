package com.itek.rftaar.presentation.splashView

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController
import com.itek.rftaar.DataHolder
import com.itek.rftaar.R
import com.itek.rftaar.core.common.constants.LoginConstants
import com.itek.rftaar.core.database.DataStoreManager
import com.itek.rftaar.presentation.navigation.Screen
import kotlinx.coroutines.delay

@Composable
fun ITekSplashScreen(navController: NavHostController,isFromNotification: Boolean) {

    LaunchedEffect(Unit) {
        //STOP if coming from notification
        if (isFromNotification) {
            DataHolder.targetRoute.value = Screen.NotificationScreen.route
            return@LaunchedEffect
        }

        delay(100)
        val isLoggedIn= DataStoreManager.readFromPreferences(LoginConstants.IS_LOGGED_IN, false)
        navController.navigate(if(isLoggedIn) Screen.DashBoardUiScreen.route else Screen.LoginScreen.route) {
            popUpTo(Screen.ITekSplashScreen.route) { inclusive = true }
        }
    }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Image(
            painter = painterResource(id = R.drawable.itek_logo_1), contentDescription = null,
        )
    }
}