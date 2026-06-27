package com.itek.rftaar

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.itek.rftaar.presentation.navigation.AppNavigation
import com.itek.rftaar.ui.theme.RFtaarTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ReaderActivity() {
  @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val targetRouteFromNotification = intent?.getStringExtra("targetRoute")

    if (targetRouteFromNotification != null) {
      DataHolder.currentScreen = ""
      DataHolder.targetRoute.value = null
    }
    //enableEdgeToEdge()
    setContent {
      RFtaarTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          val navController = rememberNavController()
          //Force navigation once controller is ready
          /*LaunchedEffect(targetRouteFromNotification) {
            if (targetRouteFromNotification != null) {
              navController.navigate(targetRouteFromNotification) {
                //popUpTo(Screen.DashBoardUiScreen.route)
                launchSingleTop = false
              }
            }
          }*/
          AppNavigation(navController = navController, modifier = Modifier.padding(innerPadding),targetRouteFromNotification)
          /*if(targetRouteFromNotification!=null) {
              navController.navigate(targetRouteFromNotification)
          }*/
        }
      }
    }
  }

  override fun onStart() {
    super.onStart()
    DataHolder.isAppInForeground = true
  }

  override fun onStop() {
    super.onStop()
    DataHolder.isAppInForeground = false
  }

  override fun onNewIntent(intent: Intent?) {
    super.onNewIntent(intent)
    intent?.getStringExtra("targetRoute")?.let { route ->
      DataHolder.targetRoute.value = route
    }
  }
}
