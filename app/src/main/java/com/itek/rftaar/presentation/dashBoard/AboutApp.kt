package com.itek.rftaar.presentation.dashBoard

import android.content.Context
import android.icu.util.Calendar
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController
import com.itek.rftaar.BuildConfig
import com.itek.rftaar.R
import com.itek.rftaar.api.constants.ParameterConstants
import com.itek.rftaar.core.common.constants.DateFormatUtils
import com.itek.rftaar.core.database.DataStoreManager
import com.itek.rftaar.logs.ReleaseLogs
import com.itek.rftaar.presentation.commonComp.BottomSheetHeader
import com.itek.rftaar.presentation.commonComp.BottomSheetType
import com.itek.rftaar.presentation.commonComp.ItekFooter
import com.itek.rftaar.presentation.commonComp.TopBarContent
import com.itek.rftaar.reader.constants.ReaderConstants
import com.itek.rftaar.ui.theme.BackGround
import com.itek.rftaar.ui.theme.BlackColor
import com.itek.rftaar.ui.theme.CommonTypography
import com.itek.rftaar.ui.theme.OutlineDefault
import com.itek.rftaar.ui.theme.TextSubtext
import com.itek.rftaar.utils.CommonUtils.chkNull
import com.itek.rftaar.utils.NetworkUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun AboutApp(modifier: Modifier.Companion, navController: NavHostController) {
    BackHandler(enabled = true) {
        if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED)
            navController.popBackStack()
    }
    Scaffold(
        bottomBar = {
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .background(BackGround),
                contentAlignment = Alignment.BottomCenter
            ) { AboutAppBottomBar() }
        },
        modifier = modifier.background(BackGround)
    ) { innerPadding ->
        Box(
            modifier = modifier
                .background(BackGround)
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            AboutAppScreenContent(navController)
        }
    }
}

@Composable
fun AboutAppScreenContent(navController: NavHostController) {
    val context = LocalContext.current
    val install = getAppInstalledDate(context)
    val updated = getLastUpdateDate(context)
    val rows = listOf(
        InfoRow("IP Address:", chkNull(NetworkUtils.getIpAddress(context), "")),
        InfoRow("Serial No.:", DataStoreManager.readFromPreferences(ParameterConstants.SERIAL,"")),
        InfoRow("App Version:", BuildConfig.VERSION_NAME),
        InfoRow("Reader SDK Version:", DataStoreManager.readFromPreferences(ReaderConstants.READER_SDK_VERSION,"")),
        InfoRow("Android Version:", Build.VERSION.SDK_INT.toString()),
        InfoRow("Manufacturer:", Build.MANUFACTURER),
        InfoRow("Model Number:", Build.MODEL),
        InfoRow("Release Date:", ReleaseLogs.RELEASE_DATE),
        InfoRow("Release Notes:", ReleaseLogs.RELEASE_NOTES),
        InfoRow("Installed/Updated on:", if (install.toString().isNotEmpty()) updated.toString() else install.toString())
    ).filter { it.value.isNotBlank() }
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(modifier = Modifier, verticalArrangement = Arrangement.Top) {
                TopBarContent(
                    "App Information",
                    onBackClickL = {
                        if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) navController.popBackStack()
                    }, onSettingClick = {
                    },
                    isSetting = false
                )
            }
            HorizontalDivider(thickness = dimensionResource(R.dimen.dp_1), color = OutlineDefault)

            Spacer(modifier = Modifier.size(24.dp))
            Image(painter = painterResource(R.drawable.rftaar),
                contentDescription = null,
                modifier = Modifier.width(104.dp).height(57.dp))

          /*  Text(text = "i-TEK RFtaar",
                style = CommonTypography.current.textSemiBold,
                modifier = Modifier.padding(top = 8.dp)
            )
*/
            InfoTable(
                modifier = Modifier.padding(16.dp),
                rows = rows
            )
        }
    }
}

@Composable
fun AboutAppBottomBar() {
    ItekFooter()
}

data class DeviceInfoItem(
    val label: String,
    val value: String
)

@Composable
fun DeviceInfoCard(
    modifier: Modifier = Modifier,
    items: List<DeviceInfoItem>
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF2F2F2)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {

            items.forEachIndexed { index, item ->

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    // Label
                    Text(
                        text = item.label,
                        modifier = Modifier.weight(1f),
                        fontSize = 14.sp,
                        color = Color.DarkGray
                    )

                    // Value
                    Text(
                        text = item.value,
                        modifier = Modifier.weight(1f),
                        fontSize = 14.sp,
                        color = Color.Black,
                        textAlign = TextAlign.Start
                    )
                }

                // Divider (except last item)
                if (index != items.lastIndex) {
                    Divider(
                        color = Color.LightGray,
                        thickness = 0.5.dp
                    )
                }
            }
        }
    }
}

data class InfoRow(
    val label: String,
    val value: String
)

@Composable
fun InfoTable(
    modifier: Modifier = Modifier,
    rows: List<InfoRow>,
    labelColumnWidth: Dp = 150.dp,
) {
    Card(
        modifier = modifier.fillMaxWidth()
            .border(width = 1.dp, color = OutlineDefault, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
        ) {

            rows.forEachIndexed { index, row ->
                TableRow(
                    row = row,
                    labelWidth = labelColumnWidth,
                    showDivider = index != rows.lastIndex
                )
            }
        }
    }
}

@Composable
private fun TableRow(
    row: InfoRow,
    labelWidth: Dp,
    showDivider: Boolean
) {
    Column(modifier = Modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min) // important for full-height divider
        ) {

            // LEFT COLUMN (Gray)
            Box(
                modifier = Modifier
                    .width(labelWidth)
                    .fillMaxHeight()
                    .background(Color(0xFFF1F1F1))
                    .padding(horizontal = 12.dp, vertical = 14.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = row.label,
                    style = CommonTypography.current.noteText.copy(color = BlackColor)
                )
            }

            // VERTICAL DIVIDER
            Divider(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp),
                color = Color(0xFFE0E0E0)
            )

            // RIGHT COLUMN (White)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(Color.White)
                    .padding(horizontal = 12.dp, vertical = 14.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = row.value,
                    style = CommonTypography.current.noteText.copy(color = TextSubtext)
                )
            }
        }

        // Horizontal divider
        if (showDivider) {
            Divider(
                color = Color(0xFFE0E0E0),
                thickness = 1.dp
            )
        }
    }
}

fun getAppInstalledDate(context: Context): String {
    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
    val cc = Calendar.getInstance()
    cc.timeInMillis=packageInfo.firstInstallTime
    return DateFormatUtils.formatToDisplayTime(cc.time)
}

fun getLastUpdateDate(context: Context): String {
    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
    val cc = Calendar.getInstance()
    cc.timeInMillis=packageInfo.lastUpdateTime
    return DateFormatUtils.formatToDisplayTime(cc.time)
}

@Immutable
data class InfoRows(
    val label: String,
    val values: List<String>
)

val rows = listOf(
    InfoRows(
        label = "Family",
        values = listOf(
            "WESTERN WEAR",
            "MEN",
            "PREMIUM"
        )
    ),
    InfoRows(
        label = "Class",
        values = listOf(
            "WINTER WEAR",
            "HOODIES",
            "2025"
        )
    ),
    InfoRows(
        label = "MC Desc",
        values = listOf(
            "PULLOVERS",
            "REGULAR FIT",
            "COTTON"
        )
    ),
    InfoRows(
        label = "Style Code",
        values = listOf(
            "AW22PUL9907CAMEL",
            "SKU-88990",
            "NEW"
        )
    ),
    InfoRows(
        label = "Colors",
        values = listOf(
            "LT. KHAKI",
            "BLACK",
            "WHITE"
        )
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScrollableInfoTable(
    modifier: Modifier = Modifier,
    rows: List<InfoRows>,
    labelColumnWidth: Dp = 140.dp,
    valueColumnWidth: Dp = 180.dp,
    scope: CoroutineScope,
    sheetState: SheetState,
    showSheet: MutableState<Boolean>,
    currentSheet: MutableState<BottomSheetType>,
) {

    val horizontalScrollState = rememberScrollState()
    val showStartIndicator = remember {
        derivedStateOf {
            horizontalScrollState.value > 0
        }
    }

    val showEndIndicator = remember {
        derivedStateOf {
            horizontalScrollState.value < horizontalScrollState.maxValue
        }
    }

    val dynamicColumnWidth = rememberDynamicColumnWidth(rows)

    Column(modifier = Modifier.padding(16.dp)) {
        BottomSheetHeader(title = stringResource(R.string.product_details_)) {
            scope.launch {
                sheetState.hide()
                showSheet.value = false
            }
        }

        Spacer(modifier = Modifier.size(16.dp))

        Card(
            modifier = modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = OutlineDefault,
                    shape = RoundedCornerShape(12.dp)
                ),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {

            Column(
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {

                rows.forEachIndexed { index, row ->

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min)
                    ) {

                        Box(
                            modifier = Modifier
                                .width(labelColumnWidth)
                                .background(Color(0xFFF3F3F3))
                                .padding(horizontal = 12.dp, vertical = 16.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = row.label,
                                style = CommonTypography.current.noteText.copy(
                                    color = BlackColor
                                )
                            )
                        }

                        Divider(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(1.dp),
                            color = Color(0xFFE0E0E0)
                        )

                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .horizontalScroll(horizontalScrollState)
                        ) {

                            row.values.forEachIndexed { valueIndex, value ->

                                Box(
                                    modifier = Modifier
                                        .width(dynamicColumnWidth)
                                        .background(Color.White)
                                        .padding(
                                            horizontal = 12.dp,
                                            vertical = 16.dp
                                        ),
                                    contentAlignment = Alignment.CenterStart
                                ) {

                                    Text(
                                        text = value,
                                        style = CommonTypography.current.noteText.copy(
                                            color = TextSubtext
                                        )
                                    )
                                }

                                // Divider between value columns
                                if (valueIndex != row.values.lastIndex) {
                                    Divider(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .width(1.dp),
                                        color = Color(0xFFE0E0E0)
                                    )
                                }
                            }
                        }
                    }

                    // Horizontal Divider
                    if (index != rows.lastIndex) {
                        Divider(
                            thickness = 1.dp,
                            color = Color(0xFFE0E0E0)
                        )
                    }
                }
            }
        }

        HorizontalScrollbar(
            scrollState = horizontalScrollState,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp)
                .padding(horizontal = 16.dp)
        )
    }
}

@Composable
fun rememberDynamicColumnWidth(
    rows: List<InfoRows>,
    minWidth: Dp = 120.dp,
): Dp {

    val maxLength = remember(rows) {

        rows.flatMap { it.values }
            .maxOfOrNull { it.length }
            ?: 10
    }

    return remember(maxLength) {

        when {
            maxLength < 10 -> minWidth
            maxLength < 20 -> 160.dp
            maxLength < 30 -> 220.dp
            else -> 300.dp
        }
    }
}

@Composable
fun HorizontalScrollbar(
    scrollState: ScrollState,
    modifier: Modifier = Modifier,
    height: Dp = 4.dp,
    thumbWidth: Dp = 40.dp,
) {

    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = modifier
            .height(height)
            .background(
                color = Color.LightGray.copy(alpha = 0.5f),
                shape = RoundedCornerShape(50)
            )
    ) {

        val maxWidthPx = constraints.maxWidth.toFloat()

        val thumbWidthPx = with(density) {
            thumbWidth.toPx()
        }

        val maxScroll = scrollState.maxValue.toFloat()

        val offsetX = if (maxScroll > 0) {
            (scrollState.value / maxScroll) * (maxWidthPx - thumbWidthPx)
        } else {
            0f
        }

        Box(
            modifier = Modifier
                .offset {
                    IntOffset(offsetX.roundToInt(), 0)
                }
                .width(thumbWidth)
                .fillMaxHeight()
                .background(
                    color = Color.DarkGray,
                    shape = RoundedCornerShape(50)
                )
        )
    }
}