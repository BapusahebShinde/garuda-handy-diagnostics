package com.itek.rftaar.presentation.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.itek.rftaar.DataHolder
import com.itek.rftaar.core.common.constants.LoginConstants
import com.itek.rftaar.core.common.constants.MenuConstants
import com.itek.rftaar.core.common.utils.LogUtils
import com.itek.rftaar.core.common.utils.ToastUtils
import com.itek.rftaar.core.database.DataStoreManager
import com.itek.rftaar.presentation.dashBoard.AboutApp
import com.itek.rftaar.presentation.dashBoard.BarcodeMap
import com.itek.rftaar.presentation.dashBoard.DashBoardUiScreen
import com.itek.rftaar.presentation.dashBoard.NotificationDetailScreen
import com.itek.rftaar.presentation.dashBoard.NotificationScreen
import com.itek.rftaar.presentation.dashBoard.QuickSearchScreen
import com.itek.rftaar.presentation.dashBoard.ReturnScreen
import com.itek.rftaar.presentation.dashBoard.SessionUploadCount
import com.itek.rftaar.presentation.dashBoard.UserDetails
import com.itek.rftaar.presentation.decoding.DecodingScreen
import com.itek.rftaar.presentation.decoding.DecodingTagListScreen
import com.itek.rftaar.presentation.encoding.EncodingHomeScreen
import com.itek.rftaar.presentation.encoding.SingleEncoding
import com.itek.rftaar.presentation.encoding.VerifyEncoding
import com.itek.rftaar.presentation.encoding.VerifyLogsScreen
import com.itek.rftaar.presentation.inventory.CustomInventory
import com.itek.rftaar.presentation.inventory.InventoryHomeScreen
import com.itek.rftaar.presentation.inventory.StartInventory
import com.itek.rftaar.presentation.inventory.StockCorrection
import com.itek.rftaar.presentation.inward.InwardDetailScreen
import com.itek.rftaar.presentation.inward.InwardScreen
import com.itek.rftaar.presentation.login.ContactSupport
import com.itek.rftaar.presentation.login.DeviceLocationList
import com.itek.rftaar.presentation.login.ForgotPasswordScreen
import com.itek.rftaar.presentation.login.LoginScreen
import com.itek.rftaar.presentation.login.MQQTConfigurationScreen
import com.itek.rftaar.presentation.login.ServerConfigurationScreen
import com.itek.rftaar.presentation.movement.MovementHomeScreen
import com.itek.rftaar.presentation.movement.ReplenishmentScreen
import com.itek.rftaar.presentation.search.ChartScreen
import com.itek.rftaar.presentation.search.ListBasedSearch
import com.itek.rftaar.presentation.search.ListBasedSearchListView
import com.itek.rftaar.presentation.search.OmnichannelScreen
import com.itek.rftaar.presentation.search.ProductSearch
import com.itek.rftaar.presentation.search.ProductSearchHomeScreen
import com.itek.rftaar.presentation.search.UnencodeSearch
import com.itek.rftaar.presentation.splashView.AnimatedSplashScreen
import com.itek.rftaar.presentation.splashView.ITekSplashScreen
import com.itek.rftaar.utils.CommonUtils

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
fun AppNavigation(navController: NavHostController, modifier: Modifier,startRoute: String?) {
    navController.addOnDestinationChangedListener { controller, destination, arguments ->
        DataHolder.currentScreen = CommonUtils.chkNull(destination.route, "")
        LogUtils.showLog("currentScreen", DataHolder.currentScreen)
    }

    val isForceLogOut = DataHolder.isForceLogOut.collectAsState()

    LaunchedEffect(isForceLogOut.value) {
      if(isForceLogOut.value && DataStoreManager.readFromPreferences(LoginConstants.IS_LOGGED_IN,false)) {
        val forceLogOutErrMsg = DataStoreManager.readFromPreferences("forceLogoutErrorMsg","")
        if(forceLogOutErrMsg.isNotEmpty()) ToastUtils.showLongToast(forceLogOutErrMsg)
        navController.navigate(Screen.LoginScreen.route) { popUpTo(0) { inclusive = true }}
        DataHolder.isForceLogOut.value=false
        DataStoreManager.saveToPreferences(LoginConstants.IS_LOGGED_IN,false)
      }
    }

    LaunchedEffect(Unit) {
        DataHolder.targetRoute.collect { route ->
            route?.let {
                if (it != DataHolder.currentScreen) {
                    navController.navigate(it) {
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
                DataHolder.targetRoute.value = null
            }
        }
    }

    var isFromNotification = startRoute != null
    //val startDestination = Screen.AnimatedSplashScreen.route
    val startDestination = if(isFromNotification /*|| DataStoreManager.readFromPreferences(
            LoginConstants.IS_LOGGED_IN,false)*/) Screen.DashBoardUiScreen.route else Screen.AnimatedSplashScreen.route
    //val startDestination = if(isFromNotification) startRoute else Screen.AnimatedSplashScreen.route


    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.AnimatedSplashScreen.route) {
           /* AnimatedSplashScreen {
                navController.navigate(Screen.ITekSplashScreen.route) {
                    popUpTo(Screen.AnimatedSplashScreen.route) { inclusive = true }
                }
            }*/
            if (!isFromNotification) {
                AnimatedSplashScreen {
                    navController.navigate(Screen.ITekSplashScreen.route) {
                        popUpTo(Screen.AnimatedSplashScreen.route) { inclusive = true }
                    }
                }
            } else {
                //Safety fallback (rare case)
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.NotificationScreen.route) {
                        popUpTo(Screen.DashBoardUiScreen.route) { inclusive = false }
                    }
                }

            }
        }
        composable(Screen.LoginScreen.route) {
            LoginScreen(modifier = modifier, navController)
        }
        composable(Screen.AppInfo.route) {
            AboutApp(modifier = Modifier, navController)
        }
        composable(Screen.SessionUploadCount.route) {
            SessionUploadCount(modifier = Modifier, navController)
        }
        composable(Screen.ServerConfigurationScreen.route) {
            ServerConfigurationScreen(modifier, navController)
        }
        composable(Screen.ForgotPasswordScreen.route) {
            ForgotPasswordScreen(modifier, navController)
        }
        /**composable(Screen.SetupDeviceLocation.route){
        SetupDeviceLocation(modifier, navController,)
        }*/
        composable(Screen.DeviceLocationList.route) {
            DeviceLocationList(modifier, navController)
        }
        composable(Screen.ITekSplashScreen.route) {
            ITekSplashScreen(navController,isFromNotification)
        }
        composable(Screen.ContactSupport.route) {
            ContactSupport(modifier, navController)
        }
        composable(Screen.UserDetails.route) {
            UserDetails(modifier, navController)
        }
        composable(Screen.DashBoardUiScreen.route) {
            DashBoardUiScreen(modifier, navController)
            if(isFromNotification && startRoute!=null) {
                navController.navigate(startRoute)
                isFromNotification = false
            }
        }
        /**  composable(Screen.VerifyLogsScreen.route){
        VerifyLogsScreen(modifier, navController,)
        }*/
        composable(Screen.MQQTConfigurationScreen.route) {
            MQQTConfigurationScreen(modifier, navController)
        }

    /**    composable(Screen.QuickSearchScreen.route) {
            QuickSearchScreen(modifier, navController,)
        }*/


        composable(
            route = "menu/{code}/{parentCode}/{label}/{params}",
            arguments = listOf(
                navArgument("code") { type = NavType.StringType },
                navArgument("parentCode") { type = NavType.StringType },
                navArgument("label") { type = NavType.StringType },
                navArgument("params") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val code = backStackEntry.arguments?.getString("code") ?: ""
            val label = backStackEntry.arguments?.getString("label") ?: ""
            val encodedParams = backStackEntry.arguments?.getString("params")
            val paramsMap = NavMapper.decodeToMap(encodedParams)
            when (code.uppercase()) {
                //ENCODE
                MenuConstants.ENCODE -> EncodingHomeScreen(modifier, navController, label, code,searchParams = paramsMap)
                MenuConstants.NORMAL_ENCODE,MenuConstants.SINGLE_ENCODE -> SingleEncoding(modifier, navController, label, code,searchParams = paramsMap)
                MenuConstants.BULK_ENCODE,MenuConstants.MULTI_ENCODE -> SingleEncoding(modifier, navController, label, code,searchParams = paramsMap)
                MenuConstants.SCAN_SCAN_ENCODE -> SingleEncoding(modifier, navController, label, code,searchParams = paramsMap)
                MenuConstants.VERIFY_ENCODE -> VerifyEncoding(modifier, navController, label, code,searchParams = paramsMap)
                //DECODE
                MenuConstants.DECODE -> DecodingScreen(modifier, navController, label, code,searchParams = paramsMap)
                MenuConstants.RETURN -> ReturnScreen(modifier, navController, label, code,searchParams = paramsMap)
                //INVENTORY
                MenuConstants.INVENTORY -> InventoryHomeScreen(modifier, navController, label, code,searchParams = paramsMap)
                //MenuConstants.CYCLE_COUNT_INVENTORY -> StartInventory(modifier,navController, label,code)
                MenuConstants.TAKE_INVENTORY -> StartInventory(modifier, navController, label, code,searchParams = paramsMap)
                MenuConstants.ADD_INVENTORY -> StartInventory(modifier, navController, label, code,searchParams = paramsMap)
                //MenuConstants.CUSTOM_INVENTORY -> CustomInventory(modifier,navController, label,code)
                MenuConstants.CUSTOM_ADD_INVENTORY -> CustomInventory(modifier, navController, label, code,searchParams = paramsMap)
                MenuConstants.CUSTOM_TAKE_INVENTORY-> CustomInventory(modifier, navController, label, code,searchParams = paramsMap)
                MenuConstants.STOCK_CORRECTION -> StockCorrection(modifier, navController, label, code,searchParams = paramsMap)
                //MOVEMENT
                //MenuConstants.MOVEMENT -> MovementHomeScreen(modifier, navController, label, code,searchParams = paramsMap)
                MenuConstants.MOVE_STOCK -> MovementHomeScreen(modifier, navController, label, code,searchParams = paramsMap)
                MenuConstants.REPLENISH_STOCK-> ReplenishmentScreen(modifier, navController, label, code,searchParams = paramsMap)
                //SEARCH
                //MenuConstants.SEARCH" -> ProductSearch(modifier, navController,label,code) // temp code
                MenuConstants.SEARCH -> ProductSearchHomeScreen(modifier, navController, label, code,searchParams = paramsMap)
                MenuConstants.NORMAL_SEARCH,MenuConstants.PRODUCT_SEARCH -> ProductSearch(modifier, navController, label, code,searchParams = paramsMap)
                MenuConstants.LIST_SEARCH -> ListBasedSearchListView(modifier, navController, label, code,searchParams = paramsMap)
                MenuConstants.OMNICHANNEL_SEARCH -> OmnichannelScreen(modifier, navController, label, code,searchParams = paramsMap)
//                "CHILD_MENU_LIST_SEARCH" -> ListBasedSearch(modifier,navController, label,code)
                MenuConstants.UNENCODED_SEARCH -> UnencodeSearch(modifier, navController, label, code,searchParams = paramsMap)
                MenuConstants.ALIEN_SEARCH -> UnencodeSearch(modifier, navController, label, code,searchParams = paramsMap)
                //INWARD
                MenuConstants.INWARD -> InwardScreen(modifier, navController, label, code,searchParams = paramsMap)
                //"CHILD_MENU_INWARD" -> InwardScreen(modifier, navController,label,code)
                //OUTWARD
                MenuConstants.OUTWARD -> InwardScreen(modifier, navController, label, code,searchParams = paramsMap)
                //OTHER
                MenuConstants.ASSOCIATE_BARCODE -> BarcodeMap(modifier, navController, label, code,searchParams = paramsMap)
                else -> Text("No screen found for code=$code")
            }
        }

        // Inside NavHost...
        fun NavGraphBuilder.genericComposable(
            screen: Screen,
            content: @Composable (code: String?, label: String?, params: Map<String, Any>?) -> Unit
        ) {
            composable(
                route = screen.route,
                arguments = listOf(
                    navArgument("code") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                    navArgument("label") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                    navArgument("params") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val code = backStackEntry.arguments?.getString("code")
                val label = backStackEntry.arguments?.getString("label")
                val encodedParams = backStackEntry.arguments?.getString("params")

                // Only decode if encodedParams is not null/empty
                val paramsMap = if (!encodedParams.isNullOrEmpty()) {
                    NavMapper.decodeToMap(encodedParams)
                } else {
                    null // Or emptyMap() depending on your preference
                }
                content(code, label, paramsMap)
            }
        }

        /*Screens.dynamicRoutes.forEach { routeConfig ->
            genericComposable(routeConfig.screen) { code, label, params ->
                routeConfig.content(
                    modifier,
                    navController,
                    code ?: "",
                    label ?: "",
                    params ?: emptyMap()
                )
            }
        }*/

        genericComposable(Screen.VerifyLogsScreen) { code, label, params ->
            VerifyLogsScreen(
                modifier = modifier, navController = navController,menuCode = code?: "", label = label?: "", searchParams = params?: emptyMap()
            )
        }

        genericComposable(Screen.InwardDetailsScreen) { code, label, params ->
            InwardDetailScreen(
                modifier = modifier, navController = navController,menuCode = code?: "", label = label?: "", searchParams = params?: emptyMap()
            )
        }

        genericComposable(Screen.ProductSearch) { code, label, params ->
            ProductSearch(
                modifier = modifier, navController = navController,menuCode = code?: "", label = label?: "", searchParams = params?: emptyMap()
            )
        }

        genericComposable(Screen.DecodingTagListScreen) { code, label, params ->
            DecodingTagListScreen(
                modifier = modifier, navController = navController,menuCode = code?: "", label = label?: "", searchParams = params?: emptyMap()
            )
        }

        genericComposable(Screen.ListBasedSearch) { code, label, params ->
            ListBasedSearch(
                modifier = modifier, navController = navController,menuCode = code?: "", label = label?: "", searchParams = params?: emptyMap()
            )
        }

        genericComposable(Screen.InwardScreen) { code, label, params ->
            InwardScreen(
                modifier = modifier, navController = navController,menuCode = code?: "", label = label?: "", searchParams = params?: emptyMap()
            )
        }

        genericComposable(Screen.ChartScreen) { code, label, params ->
            ChartScreen(
                modifier = modifier, navController = navController,menuCode = code?: "", label = label?: "", searchParams = params?: emptyMap()
            )
        }

         genericComposable(Screen.MovementHomeScreen) { code, label, params ->
             MovementHomeScreen(
                modifier = modifier, navController = navController,menuCode = code?: "", label = label?: "", searchParams = params?: emptyMap()
            )
        }

        genericComposable(Screen.QuickSearchScreen) { code, label, params ->
            QuickSearchScreen(
                modifier = modifier, navController = navController,menuCode = code?: "", label = label?: "", searchParams = params?: emptyMap()
            )
        }

        genericComposable(Screen.NotificationScreen) { code, label, params ->
            NotificationScreen(
                modifier = modifier, navController = navController,menuCode = code?: "", label = label?: "", searchParams = params?: emptyMap()
            )
        }

        genericComposable(Screen.NotificationDetailScreen) { code, label, params ->
            NotificationDetailScreen(
                modifier = modifier, navController = navController,menuCode = code?: "", label = label?: "", searchParams = params?: emptyMap()
            )
        }



        /** composable(
        route = "verifyLogs/{menuCode}/{transactionType}/{tagCount}", arguments = listOf(
        navArgument("menuCode") { type = NavType.StringType },
        navArgument("transactionType") { type = NavType.StringType },
        navArgument("tagCount") { type = NavType.StringType },
        )
        ) { backStackEntry ->

        val menuCode = backStackEntry.arguments?.getString("menuCode") ?: ""
        val transactionType = backStackEntry.arguments?.getString("transactionType") ?: ""
        val tagCount = backStackEntry.arguments?.getString("tagCount") ?: ""

        VerifyLogsScreen(
        modifier = modifier,
        navController = navController,
        menuCode = menuCode,
        transactionType = transactionType,
        tagCount = tagCount
        )
        }*/


        /**composable(
        route = "decodingTagListScreen/{menuCode}/{transactionType}/{tagCount}",
        arguments = listOf(
        navArgument("menuCode") { type = NavType.StringType },
        navArgument("transactionType") { type = NavType.StringType },
        navArgument("tagCount") { type = NavType.StringType },
        )
        ) { backStackEntry ->

        val menuCode = backStackEntry.arguments?.getString("menuCode") ?: ""
        val transactionType = backStackEntry.arguments?.getString("transactionType") ?: ""
        val tagCount = backStackEntry.arguments?.getString("tagCount") ?: ""
        val decodeList = backStackEntry.arguments?.getStringArrayList("decodeList") ?: ""

        DecodingTagListScreen(
        modifier = modifier,
        navController = navController,
        menuCode = menuCode,
        transactionType = transactionType,
        tagCount = tagCount
        )
        }*/

        /**navigation(
            startDestination = "inwardHome/{label}/{menuCode}/{transactionType}",
            route = "inward_graph"
        ) {

            composable(
                route = "inwardHome/{label}/{menuCode}/{transactionType}/{params}", arguments = listOf(
                    navArgument("label") { type = NavType.StringType },
                    navArgument("menuCode") { type = NavType.StringType },
                    navArgument("transactionType") { type = NavType.StringType },
                    navArgument("params") { type = NavType.StringType}
                )
            ) { backStackEntry ->

                val encodedParams = backStackEntry.arguments?.getString("params")
                val paramsMap = NavMapper.decodeToMap(encodedParams)
                InwardScreen(
                    modifier = modifier,
                    navController = navController,
                    label = backStackEntry.arguments?.getString("label") ?: "",
                    menuCode = backStackEntry.arguments?.getString("menuCode") ?: "",
                    transactionType = backStackEntry.arguments?.getString("transactionType") ?: "",
                    searchParams = paramsMap
                )
            }

           *//** composable(
                route = "inwardDetailScreen/{label}/{menuCode}/{transactionType}",
                arguments = listOf(
                    navArgument("label") { type = NavType.StringType },
                    navArgument("menuCode") { type = NavType.StringType },
                    navArgument("transactionType") { type = NavType.StringType },
                )
            ) { backStackEntry ->

                InwardDetailScreen(
                    modifier = modifier,
                    navController = navController,
                    label = backStackEntry.arguments?.getString("label") ?: "",
                    menuCode = backStackEntry.arguments?.getString("menuCode") ?: "",
                    transactionType = backStackEntry.arguments?.getString("transactionType") ?: "",
                )
            }*//*

           *//** composable(
                route = "listBasedSearch/{label}", arguments = listOf(
                    navArgument("label") { type = NavType.StringType },
                )
            ) { backStackEntry ->
                val label = backStackEntry.arguments?.getString("label") ?: ""

                ListBasedSearch(
                    modifier = Modifier, navController = navController, label = label,
                )
            }*//*

        }*/
    }

    fun NavController.navigateToDynamicScreen(
        screen: Screen,
        code: String = "",
        label: String = "",
        params: Map<String, String> = emptyMap()
    ) {
        // Convert the map into a query string if your system uses query params
        val queryParams = params.entries.joinToString(separator = "&") { "${it.key}=${it.value}" }

        // Construct the final route matching your genericComposable pattern
        val finalRoute = "${screen.route}/$code/$label?$queryParams"

        this.navigate(finalRoute)
    }
}
