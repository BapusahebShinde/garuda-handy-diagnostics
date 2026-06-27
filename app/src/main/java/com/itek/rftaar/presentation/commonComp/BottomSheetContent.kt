package com.itek.rftaar.presentation.commonComp

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.itek.rftaar.R
import com.itek.rftaar.core.common.utils.ToastUtils
import com.itek.rftaar.presentation.inventory.SearchBar
import com.itek.rftaar.presentation.login.IconWithTextRow
import com.itek.rftaar.ui.theme.BlackColor
import com.itek.rftaar.ui.theme.CommonTypography
import com.itek.rftaar.ui.theme.LightGray
import com.itek.rftaar.ui.theme.OutlineDefault
import com.itek.rftaar.ui.theme.RedColor
import com.itek.rftaar.ui.theme.TextBlack
import com.itek.rftaar.ui.theme.WhiteColor
import com.itek.rftaar.ui.theme.Yellow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

enum class BottomSheetType {
    NONE,
    SUB_MENUS,
    SETTINGS,
    POWER,
    SESSION,
    LIMIT,
    MESSAGE,
    COMMENT,
    NOTE,
    UPLOAD,
    SEARCH,
    PRODUCT_DETAILS,
    ACCEPT,
    REJECT,
    PARTIAL_COMPLETED,
    CONFIRM_DELETE,
    RESCAN,
    LOGOUT,
    LOGS,
    FILTER,
    CHART_OPTIONS,
    DECODING_TYPE,
    KNOW_MORE
}


@OptIn(ExperimentalMaterial3Api::class)
fun openSheet(
    scope: CoroutineScope,
    sheetState: SheetState,
    showSheet: MutableState<Boolean>,
    currentSheet: MutableState<BottomSheetType>,
    sheet: BottomSheetType
) {
    scope.launch {
        currentSheet.value = sheet
        showSheet.value = true
        sheetState.show()
    }
}
data class SetValueUiConfig(
    val title: String,
    val heading: String,
    val subHeading: String,
    val minValue: Int,
    val maxValue: Int,
    val initialValue: Int
)

data class SetValueActions(
    val onConfirm: (Int) -> Unit,
    val onClose: () -> Unit
)

data class DeviceSettingItem(
    val iconRes: Int,
    val title: String,
    val isSession: Boolean = false,
    val isEnabled: Boolean = true,
    val onClick: () -> Unit
)


data class SessionUiState(
    val title: String,
    val heading: String,
    val subHeading: AnnotatedString,
    val primaryButtonText: String,
    val secondaryButtonText: String,
)

data class MessageUiState(
    val title: String,
    val heading: String,
    val subHeading: AnnotatedString,
    val confirmationMessage: String = "",
    val primaryButtonText: String,
    val secondaryButtonText: String,
    val icon: Int,
    val color1: Color,
    val color2: Color,
)

data class SessionUiActions(
    val onClose: () -> Unit,
    val onPrimaryAction: () -> Unit,
    val onSecondaryAction: () -> Unit
)

data class NoteUiState(
    val title: String,
    val heading: String,
    val subHeading: String,
    val primaryButtonText: String,
    var note: String,
    val imeAction: ImeAction,
    val onTextChange: (String) -> Unit = {},
    val textValue: String,
    val isButtonEnabled: Boolean
)

data class ResultSheetUiState(
    val title: String,
    val message: String? = null,
    val expected: Int? = null,
    val scanned: Int? = null,
    val iconRes: Int,
    val iconBrush: Brush,
    val primaryText: String,
    val secondaryText: String? = null,
    val showAcceptReject: Boolean = false
)

@Composable
fun FilterPicker(
    maxSelection:Int = -1,
    minSelection:Int = 0,
    filters: List<String>,
    onDismiss: () -> Unit,
    onApply: (List<String>) -> Unit,
    selFilterKeys: SnapshotStateList<String> = remember { mutableStateListOf<String>() }
) {

    val searchQuery = remember { mutableStateOf("") }
    val selectedFilters = remember { mutableStateListOf<String>().apply { addAll(selFilterKeys.distinct()) } }

    val filteredList = if (searchQuery.value.isEmpty()) {
        filters
    } else {
        filters.filter {
            it.contains(searchQuery.value, ignoreCase = true)
        }
    }

    // 🔹 FULL SCREEN OVERLAY
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.35f))
            .padding(top = 44.dp),
        contentAlignment = Alignment.BottomCenter
    ) {

        Column(horizontalAlignment = Alignment.End) {

            // 🔹 MAIN CARD
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .heightIn(max = 400.dp)
                    .background(WhiteColor, RoundedCornerShape(24.dp))
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {

                    Text(
                        text = "Select Filters",
                        style = CommonTypography.current.headingH1,
                        modifier = Modifier.padding(bottom = dimensionResource(R.dimen.dp_16))
                    )

                    SearchBar(
                        searchQuery = searchQuery.value,
                        onQueryChange = { searchQuery.value = it },
                        placeholder = "Search Filters"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn {

                        items(filteredList) { item ->

                            val isSelected = selectedFilters.contains(item)

                            FilterRow(
                                title = item,
                                isSelected = isSelected,
                                onClick = {
                                    if (isSelected) selectedFilters.remove(item)
                                    else selectedFilters.add(item)
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_12)))

            // 🔹 BUTTON CARD (Cancel + Apply)
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .background(Color.Transparent)
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    // Cancel
                    CommonButton(
                        text = stringResource(id = R.string.cancel),
                        onClick = {
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        gradientBrush = SolidColor(WhiteColor),
                        contentColor = RedColor
                    )

                    // Apply
                    CommonButton(
                        text = stringResource(id = R.string.set_filters),
                        onClick = {
                            if(minSelection>0 && maxSelection>0 && minSelection==maxSelection && selectedFilters.size!=minSelection){
                                // show Error
                                ToastUtils.showShortToast("Please select any "+minSelection+" filters!")
                                return@CommonButton
                            }
                            if(minSelection>0 && selectedFilters.size<minSelection){
                                // show Error
                                ToastUtils.showShortToast("Please select at least "+minSelection+" filters!")
                                return@CommonButton
                            }
                            if(maxSelection>0 && selectedFilters.size>maxSelection){
                                // show Error
                                ToastUtils.showShortToast("Please select at most "+maxSelection+" filters!")
                                return@CommonButton
                            }
                            onApply(selectedFilters)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_14)))
        }
    }
}

@Composable
fun FilterRow(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(20.dp)
                .border(1.dp, LightGray, RoundedCornerShape(4.dp))
                .background(
                    if (isSelected) Yellow else Color.Transparent,
                    RoundedCornerShape(4.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Text(
                    text = "✓",
                    color = WhiteColor,
                    style = CommonTypography.current.smallTxt
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        /*Text(
            text = title,
            style = CommonTypography.current.textSemiBold
        )*/
        Text(
            text = title ?: "",
            maxLines = 1,
            modifier = Modifier
                .fillMaxWidth()
                .basicMarquee(),
            style = CommonTypography.current.textSemiBold
        )
    }
}

@Composable
fun AppResultBottomSheetContent(
    uiState: ResultSheetUiState,
    onClose: () -> Unit,
    onPrimary: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
            .wrapContentHeight(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Box(Modifier.fillMaxWidth()) {
            Text(
                text = uiState.title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.Center)
            )

            IconButton(
                onClick = onClose,
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(Icons.Default.Close, contentDescription = null)
            }
        }

        Spacer(Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(uiState.iconBrush),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(uiState.iconRes),
                contentDescription = null,
                tint = Color.White
            )
        }

        Spacer(Modifier.height(20.dp))

        uiState.message?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        if (uiState.expected != null && uiState.scanned != null) {
            Spacer(Modifier.height(8.dp))
            Row {
                Text("Expected: ${uiState.expected}")
                Spacer(Modifier.width(12.dp))
                Text("|")
                Spacer(Modifier.width(12.dp))
                Text("Scanned: ${uiState.scanned}")
            }
        }

        Spacer(Modifier.height(28.dp))


        Button(
                onClick = onPrimary,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(uiState.primaryText ?: "Reject")
            }
        }

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionBottomSheet(
    uiState: SessionUiState,
    actions: SessionUiActions,
    modifier: Modifier = Modifier,
    isSecondaryButton: Boolean = true
) {

    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .navigationBarsPadding()
            .imePadding()
            .background(
                WhiteColor,
                RoundedCornerShape(
                    topStart = dimensionResource(R.dimen.dp_24),
                    topEnd = dimensionResource(R.dimen.dp_24)
                )
            )
            .padding(dimensionResource(R.dimen.dp_16)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        /* ---------- HEADER ---------- */
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = dimensionResource(R.dimen.dp_16))
        ) {
            Text(
                text = uiState.title,
                style = CommonTypography.current.textSemiBold,
                modifier = Modifier.align(Alignment.Center),
            )

            Image(
                painter = painterResource(R.drawable.clear),
                contentDescription = "Close",
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .clickable { actions.onClose() }
            )
        }

        /* ---------- HEADING ---------- */
        Text(
            text = uiState.heading,
            style = CommonTypography.current.headingH1,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        /* ---------- SUB HEADING ---------- */
        Text(
            text = uiState.subHeading,
            style = CommonTypography.current.textSubtext,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        /* ---------- ACTION BUTTONS ---------- */

        TwoButtonView(
            seconderText = uiState.secondaryButtonText,
            primaryText = uiState.primaryButtonText,
            seconderAction = actions.onSecondaryAction,
            primaryAction = actions.onPrimaryAction,
            isSecondaryButton = isSecondaryButton
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageBottomSheetSingleButton(
    uiState: MessageUiState,
    actions: SessionUiActions,
    modifier: Modifier = Modifier,
    isButton: Boolean = true
) {

    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(
                WhiteColor,
                RoundedCornerShape(
                    topStart = dimensionResource(R.dimen.dp_24),
                    topEnd = dimensionResource(R.dimen.dp_24)
                )
            )
            .padding(horizontal = dimensionResource(R.dimen.dp_16))
            .padding(
                top = dimensionResource(R.dimen.dp_11),
                bottom = dimensionResource(R.dimen.dp_24)
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        /* ---------- HEADER ---------- */
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = dimensionResource(R.dimen.dp_12))
        ) {
            Text(
                text = uiState.title,
                style = CommonTypography.current.textSemiBold,
                modifier = Modifier.align(Alignment.Center).basicMarquee()
            )

            Image(
                painter = painterResource(R.drawable.clear),
                contentDescription = "Close",
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .clickable { actions.onClose() }
            )
        }

        CircularIcon(
            iconRes = uiState.icon,
            modifier = Modifier.background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        uiState.color1,
                        uiState.color2
                    )
                ),
                shape = CircleShape
            ),
            backgroundColor = Brush.verticalGradient(
                colors = listOf(
                    uiState.color1,
                    uiState.color2
                )
            ),
            circleSize = dimensionResource(R.dimen.dp_64),
            iconSize = dimensionResource(R.dimen.dp_34),
            tintColor = WhiteColor
        )

        Spacer(modifier = Modifier.height(16.dp))

        /* ---------- HEADING ---------- */
        Text(
            text = uiState.heading,
            style = CommonTypography.current.headingH1,
            textAlign = TextAlign.Center,
            //modifier = Modifier.basicMarquee()
        )

        Spacer(modifier = Modifier.height(8.dp))

        /* ---------- SUB HEADING ---------- */
        Text(
            text = uiState.subHeading,
            style = CommonTypography.current.textSubtext,
            textAlign = TextAlign.Center
        )

        /* ---------- CONFIRMATION TEXT ---------- */
        if (uiState.confirmationMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = uiState.confirmationMessage,
                style = CommonTypography.current.textSubtext,
                textAlign = TextAlign.Center
            )
        }


        if (isButton){
            Spacer(modifier = Modifier.height(24.dp))

            /* ---------- ACTION BUTTONS ---------- */
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                CommonButton(
                    text = uiState.primaryButtonText,
                    modifier = Modifier.basicMarquee()
                        .fillMaxWidth().weight(1f),
                    onClick = actions.onPrimaryAction
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageBottomSheet(
    uiState: MessageUiState,
    actions: SessionUiActions,
    modifier: Modifier = Modifier,
    isButton: Boolean = true
) {

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                WhiteColor,
                RoundedCornerShape(
                    topStart = dimensionResource(R.dimen.dp_24),
                    topEnd = dimensionResource(R.dimen.dp_24)
                )
            )
            .padding(horizontal = dimensionResource(R.dimen.dp_16)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        /* ---------- HEADER ---------- */
        Spacer(modifier = Modifier.size(11.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                text = uiState.title,
                style = CommonTypography.current.textSemiBold,
                modifier = Modifier.align(Alignment.Center)
            )

            Image(
                painter = painterResource(R.drawable.clear),
                contentDescription = "Close",
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .clickable { actions.onClose() }
            )
        }

        Spacer(modifier = Modifier.size(16.dp))
        /* ---------- ICON ---------- */
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            CircularIcon(
                iconRes = uiState.icon,
                modifier = Modifier.background(
                    brush = Brush.verticalGradient(
                        colors = listOf(uiState.color1, uiState.color2)
                    ),
                    shape = CircleShape
                ),
                backgroundColor = Brush.verticalGradient(
                    colors = listOf(uiState.color1, uiState.color2)
                ),
                circleSize = dimensionResource(R.dimen.dp_64),
                iconSize = dimensionResource(R.dimen.dp_34),
                tintColor = WhiteColor
            )
        }

        Spacer(modifier = Modifier.size(16.dp))


        /* ---------- HEADING ---------- */
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = uiState.heading,
                style = CommonTypography.current.headingH1,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.size(8.dp))
        /* ---------- SUB HEADING ---------- */
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = uiState.subHeading,
                style = CommonTypography.current.textSubtext,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.size(24.dp))


        /* ---------- ACTION BUTTONS ---------- */
        if (isButton) {

            TwoButtonView(
                seconderText = uiState.secondaryButtonText,
                primaryText = uiState.primaryButtonText,
                seconderAction = actions.onSecondaryAction,
                primaryAction = actions.onPrimaryAction,
                isSecondaryButton = true
            )

        }
    }
}



@Composable
fun BottomSheetHeader(
    title: String,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = dimensionResource(R.dimen.dp_8))
    ) {
        Text(
            text = title,
            style = CommonTypography.current.textSemiBold,
            modifier = Modifier.align(Alignment.Center)
        )

        Image(
            painter = painterResource(R.drawable.clear),
            contentDescription = "Close",
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .clickable { onClose() }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetTextIconRow(
    showSheet: MutableState<Boolean>,
    sheetState: SheetState,
    scope: CoroutineScope,
    title: String,
    items: List<DeviceSettingItem>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(
                color = WhiteColor,
                shape = RoundedCornerShape(
                    topStart = dimensionResource(R.dimen.dp_24),
                    topEnd = dimensionResource(R.dimen.dp_24)
                )
            )
            .padding(dimensionResource(R.dimen.dp_16)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        BottomSheetHeader(title = title) {
            scope.launch {
                sheetState.hide()
                showSheet.value = false
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items.forEach { item ->
                IconWithTextRow(
                    icon = painterResource(item.iconRes),
                    text = item.title,
                    onClick = item.onClick
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetSubMenu(
    showSheet: MutableState<Boolean>,
    sheetState: SheetState,
    scope: CoroutineScope,
    title: String,
    items: List<DeviceSettingItem>,
    iconTint: Color = Color.Unspecified,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(
                color = WhiteColor,
                shape = RoundedCornerShape(
                    topStart = dimensionResource(R.dimen.dp_24),
                    topEnd = dimensionResource(R.dimen.dp_24)
                )
            )
            .padding(dimensionResource(R.dimen.dp_16))
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        BottomSheetHeader(title = title) {
            scope.launch {
                sheetState.hide()
                showSheet.value = false
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth()
                .wrapContentHeight(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items.forEach { item ->
                IconTextIcon(
                    icon = painterResource(item.iconRes),
                    text = item.title,
                    onClick = item.onClick,
                    session = item.isSession,
                    iconTint = iconTint,
                    isEnabled = item.isEnabled
                )
            }
        }
    }
}

@Composable
fun IconTextIcon(
    icon: Painter,
    text: String,
    onClick: () -> Unit,
    session: Boolean = false,
    iconTint: Color = Color.Unspecified,
    isEnabled: Boolean = true
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clickable(
                enabled = isEnabled,
                onClick = { onClick() }
            )
            .padding(vertical = 10.dp)
    ) {

        val infiniteTransition = rememberInfiniteTransition(label = "3d")

        val rotationY = infiniteTransition.animateFloat(
            initialValue = -8f,
            targetValue = 8f,
            animationSpec = infiniteRepeatable(
                animation = tween(4000, easing = EaseInOut),
                repeatMode = RepeatMode.Reverse
            ),
            label = "rotationY"
        )

        val rotationX = infiniteTransition.animateFloat(
            initialValue = 6f,
            targetValue = -6f,
            animationSpec = infiniteRepeatable(
                animation = tween(4000, easing = EaseInOut),
                repeatMode = RepeatMode.Reverse
            ),
            label = "rotationX"
        )
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {

            Row(
                modifier = Modifier.weight(1f), // ✅ takes remaining space
                verticalAlignment = Alignment.CenterVertically
            ) {

                /*Image(
                    painter = icon,
                    contentDescription = null
                )*/
                Image(
                    painter = icon,
                    contentDescription = null,
                    colorFilter = if (isEnabled) {
                        if (iconTint != Color.Unspecified) {
                            ColorFilter.tint(iconTint)
                        } else {
                            null
                        }
                    } else {
                        ColorFilter.colorMatrix(
                            ColorMatrix().apply {
                                setToSaturation(0f)
                            }
                        )
                    },
                    modifier = Modifier
                        .size(24.dp)
                        .then(
                            if (!isEnabled) {
                                Modifier.blur(2.dp)
                            } else {
                                Modifier
                            }
                        )
                        .alpha(if (isEnabled) 1f else 0.35f)
                        .graphicsLayer {
                            this.rotationX = rotationX. value
                            this.rotationY = rotationY.value
                            cameraDistance = 12 * density
                        }
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = text,
                    style = CommonTypography.current.textSemiBold.copy(color = if (isEnabled) { TextBlack } else { BlackColor.copy(alpha = 0.4f) })
                )
            }

            if (session) {
                Row(
                    modifier = Modifier,
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Image(
                        painter = painterResource(R.drawable.property_red_dot),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetValueContent(
    showSheet: MutableState<Boolean>,
    sheetState: SheetState,
    scope: CoroutineScope,
    config: SetValueUiConfig,
    actions: SetValueActions
) {

    val value = rememberSaveable(config.initialValue) {
        mutableStateOf(config.initialValue)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(WhiteColor)
            .imePadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(dimensionResource(R.dimen.dp_16)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        /** Header */
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = config.title,
                style = CommonTypography.current.textSemiBold,
                modifier = Modifier.align(Alignment.Center),
                textAlign = TextAlign.Center
            )

            Icon(
                painter = painterResource(R.drawable.clear),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        scope.launch {
                            sheetState.hide()
                            showSheet.value = false
                            actions.onClose()
                        }
                    }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = config.heading,
            style = CommonTypography.current.headingH1,
            textAlign = TextAlign.Center
        )

        Text(
            text = config.subHeading,
            style = CommonTypography.current.textSubtext,
            modifier = Modifier.padding(vertical = 20.dp),
            textAlign = TextAlign.Center
        )

        ManualLimitSlider(
            minValue = config.minValue,
            maxValue = config.maxValue,
            value = value
        )

        Spacer(modifier = Modifier.size(16.dp))

        CommonButton(
            text = stringResource(R.string.continue_),
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                scope.launch {
                    actions.onConfirm(value.value)
                    sheetState.hide()
                    showSheet.value = false
                }
            }
        )
    }
}

@Composable
fun ManualLimitSlider(
    minValue: Int = 5,
    maxValue: Int = 30,
    value: MutableState<Int>,
) {
    val thumbSize = 40.dp
    val sliderHeight = 40.dp
    val tooltipSize = 35.dp

    val sliderWidthPx = remember { mutableStateOf(1f) }
    val density = LocalDensity.current
    val thumbSizePx = with(density) { thumbSize.toPx() }

    var thumbPx by remember(value.value, sliderWidthPx.value) {
        mutableStateOf(
            ((value.value - minValue).toFloat() / (maxValue - minValue))
                .coerceIn(0f, 1f) *
                    (sliderWidthPx.value - thumbSizePx)
        )
    }

    Column(modifier = Modifier.fillMaxWidth()) {

        /** MIN MAX LABELS */
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(minValue.toString())
            Text(maxValue.toString())
        }

        Box(
            modifier = Modifier
                .height(50.dp) // extra height for tooltip
                .fillMaxWidth()
        ) {

            /** TRACK BORDER */
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .height(sliderHeight)
                    .fillMaxWidth()
                    .border(1.dp, OutlineDefault, RoundedCornerShape(50))
                    .onGloballyPositioned {
                        sliderWidthPx.value = it.size.width.toFloat().coerceAtLeast(1f)
                    }
            )

            /** PROGRESS */
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .height(sliderHeight)
                    .fillMaxWidth(
                        ((thumbPx + thumbSizePx) / sliderWidthPx.value)
                            .coerceIn(0f, 1f)
                    )
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFFFFC107), Color(0xFFFFA000))
                        ),
                        RoundedCornerShape(50)
                    )
            )

            /** TOOLTIP (NEW) */
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            thumbPx.toInt(),
                            -90 // move above slider
                        )
                    }
                    .size(tooltipSize)
                    .background(Color(0xFFEDEDED), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    value.value.toString(),
                    style = CommonTypography.current.noteText.copy(BlackColor)
                )
            }

            /** THUMB (EMPTY NOW) */
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset { IntOffset(thumbPx.toInt(), 0) }
                    .size(thumbSize)
                    .background(WhiteColor, CircleShape)
                    .border(3.dp, Color(0xFFFFC107), CircleShape)
                    .draggable(
                        orientation = Orientation.Horizontal,
                        state = rememberDraggableState { delta ->

                            val maxPx =
                                (sliderWidthPx.value - thumbSizePx).coerceAtLeast(1f)

                            thumbPx = (thumbPx + delta).coerceIn(0f, maxPx)

                            value.value =
                                minValue + ((thumbPx / maxPx) * (maxValue - minValue)).roundToInt()
                        }
                    )
            )
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
fun showSheetSafely(
    scope: CoroutineScope,
    sheetState: SheetState,
    showSheet: MutableState<Boolean>,
    powerSheet: MutableState<Boolean> = mutableStateOf(false),
    sessionSheet: MutableState<Boolean> = mutableStateOf(false),
    settingSheet: MutableState<Boolean> = mutableStateOf(false),
    showPower: Boolean = false,
    showSession: Boolean = false,
    showSetting: Boolean = false,
) {
    scope.launch {
        // 🔥 RESET FIRST
        powerSheet.value = false
        sessionSheet.value = false
        settingSheet.value = false

        // ✅ SET TARGET
        powerSheet.value = showPower
        sessionSheet.value = showSession
        settingSheet.value = showSetting

        showSheet.value = true
        sheetState.show()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteWithTextAndButton(
    uiState: NoteUiState,
    onClose: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    isFocused: MutableState<Boolean>
) {

    val keyboardController = LocalSoftwareKeyboardController.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WhiteColor)
            .fillMaxWidth()
            .imePadding()
    ) {

        Column(
            modifier = modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .background(
                    WhiteColor,
                    RoundedCornerShape(
                        topStart = dimensionResource(R.dimen.dp_24),
                        topEnd = dimensionResource(R.dimen.dp_24)
                    )
                )
                .padding(dimensionResource(R.dimen.dp_16)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            /* ---------- HEADER ---------- */
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = dimensionResource(R.dimen.dp_16))
            ) {
                Text(
                    text = uiState.title,
                    style = CommonTypography.current.textSemiBold,
                    modifier = Modifier.align(Alignment.Center)
                )

                Image(
                    painter = painterResource(R.drawable.clear),
                    contentDescription = "Close",
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .clickable { onClose() }
                )
            }

            Text(
                text = uiState.heading,
                style = CommonTypography.current.headingH1,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            /* ---------- SUB HEADING ---------- */
            Text(
                text = uiState.subHeading,
                style = CommonTypography.current.textSubtext,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))


            CommonTextField(
                config = TextFieldConfig(
                    value = uiState.note,
                    onValueChange = uiState.onTextChange,
                    label = stringResource(id = R.string.reason),
                    imeAction = ImeAction.Done,
                    isNote = true,
                    onImeAction = {
                        keyboardController?.hide()
                    },
                    onClick = { }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged {
                        isFocused.value = it.isFocused
                    }
            )

            Spacer(modifier = Modifier.height(24.dp))

            CommonButton(
                text = uiState.primaryButtonText,
                modifier = Modifier,
                enabled = uiState.isButtonEnabled,
                onClick = { onConfirm() }
            )

            Spacer(modifier = Modifier.height(16.dp))

        }
    }

}