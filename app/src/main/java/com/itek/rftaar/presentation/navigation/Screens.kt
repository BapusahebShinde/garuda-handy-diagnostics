package com.itek.rftaar.presentation.navigation

import android.os.Build
import androidx.annotation.RequiresApi

sealed class Screen(val route: String) {
    object LoginScreen : Screen("loginScreen")
    object ForgotPasswordScreen : Screen("forgotPasswordScreen")
    object ServerConfigurationScreen : Screen("serverConfigurationScreen")
    object MQQTConfigurationScreen : Screen("MQQTConfigurationScreen")
    object DeviceLocationList : Screen("deviceLocationList")
    object ITekSplashScreen : Screen("iTekSplashScreen")
    object AnimatedSplashScreen : Screen("splashScreen")
    object ContactSupport : Screen("contactSupport")
    object UserDetails : Screen("userDetails")
    object DashBoardUiScreen : Screen("dashBoardUiScreen")
    object AppInfo : Screen("AppInfo")
    object SessionUploadCount : Screen("Session Upload Count(s)")

        // This is now generic! Any screen can call this.
        @RequiresApi(Build.VERSION_CODES.O)
        fun createRoute(
            code: String? = null,
            label: String? = null,
            params: Map<String, Any>? = null
        ): String {
            val base = route.substringBefore("?")
            val encodedParams = if (!params.isNullOrEmpty()) NavMapper.encodeMap(params) else ""
            val safeCode = code ?: ""
            val safeLabel = label?:""//let { java.net.URLEncoder.encode(it, "UTF-8") } ?: ""

            return "$base?code=$safeCode&label=$safeLabel&params=$encodedParams"
        }

    object ProductSearch : Screen("productSearch?code={code}&label={label}&params={params}")
    object VerifyLogsScreen : Screen("verifyLogs?code={code}&label={label}&params={params}")
    object InwardDetailsScreen : Screen("inwardDetailScreen?code={code}&label={label}&params={params}")
    object DecodingTagListScreen : Screen("decodingTagListScreen?code={code}&label={label}&params={params}")
    object ListBasedSearch : Screen("listBasedSearch?code={code}&label={label}&params={params}")
    object InwardScreen : Screen("inwardHome?code={code}&label={label}&params={params}")
    object ChartScreen : Screen("ChartScreen?code={code}&label={label}&params={params}")
    object MovementHomeScreen : Screen("movementHomeScreen?code={code}&label={label}&params={params}")
    object QuickSearchScreen : Screen("quickSearchScreen?code={code}&label={label}&params={params}")
    object NotificationScreen : Screen("notificationScreen?code={code}&label={label}&params={params}")
    object NotificationDetailScreen : Screen("notificationDetailScreen?code={code}&label={label}&params={params}")

    data class DynamicDashboard(
        val code: String?,
        val label: String?,
        val params: Map<String, Any>
    ) : Screen("menu/{code}/{parentCode}/{label}/{params}") {

        @RequiresApi(Build.VERSION_CODES.O)
        fun createRoute(parentCode: String?): String {
            // IMPORTANT: Encode the map using NavMapper
            val encodedParams = NavMapper.encodeMap(params)

            val safeCode = code?.uppercase() ?: "UNKNOWN"
            val safeParent = parentCode ?: "ROOT"
            val safeLabel = label ?: "Menu"

            // This creates: menu/ROOT_MENU_DECODE/ROOT/Decode/ZXlKaG...
            return "menu/$safeCode/$safeParent/$safeLabel/$encodedParams"
        }
    }

    /*data class DynamicScreenRoute(
        val screen: Screen,
        val content: @Composable (modifier: Modifier, navController: NavHostController, code: String, label: String, params: Map<String, String>) -> Unit
    )

    @RequiresApi(Build.VERSION_CODES.O)
    val dynamicRoutes = listOf(
        DynamicScreenRoute(Screen.InwardDetailsScreen) { mod, nav, code, label, params ->
            InwardDetailScreen(modifier = mod, navController = nav, menuCode = code, label = label, searchParams = params)
        },
        DynamicScreenRoute(Screen.ChartScreen) { mod, nav, code, label, params ->
            ChartScreen(modifier = mod, navController = nav, menuCode = code, label = label, searchParams = params)
        },
    )*/


}