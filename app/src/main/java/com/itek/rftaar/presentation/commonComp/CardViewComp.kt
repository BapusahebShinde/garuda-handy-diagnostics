package com.itek.rftaar.presentation.commonComp

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter
import com.itek.rftaar.R
import com.itek.rftaar.core.common.utils.LogUtils
import com.itek.rftaar.core.database.DataStoreManager
import com.itek.rftaar.presentation.encoding.VerificationBadgeStatic
import com.itek.rftaar.presentation.encoding.VerificationOverlayStatic
import com.itek.rftaar.presentation.encoding.VerificationResult
import com.itek.rftaar.presentation.encoding.VerificationUiState
import com.itek.rftaar.ui.theme.BackGround
import com.itek.rftaar.ui.theme.BlackColor
import com.itek.rftaar.ui.theme.CommonTypography
import com.itek.rftaar.ui.theme.LightGray
import com.itek.rftaar.ui.theme.OutlineDefault
import com.itek.rftaar.ui.theme.ShadowGray
import com.itek.rftaar.ui.theme.TabColor
import com.itek.rftaar.ui.theme.TextGrey
import com.itek.rftaar.ui.theme.WhiteColor
import com.itek.rftaar.ui.theme.Yellow
import kotlinx.coroutines.launch
import org.json.JSONArray

data class StatItem(
    val value: String, val label: String, val valueColor: Color = BlackColor
)

@Composable
fun StatsCard(
    items: List<StatItem>,
    modifier: Modifier = Modifier,
    stockList: List<StockItem> = emptyList(),
    isSemiCircle: Boolean = false,
    isExpanded: Boolean = false,
    onExpandChange: (Boolean) -> Unit = {}
) {
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    // --- 1. Height Configuration ---
    // Increased collapsed height slightly to fit labels comfortably
    val collapsedHeightPx = with(density) { 80.dp.toPx() }
    // Expanded height should be enough for the header + list
    val expandedHeightPx = with(density) { 250.dp.toPx() }

    val animatedHeight = remember { Animatable(collapsedHeightPx) }
    val progress =
        ((animatedHeight.value - collapsedHeightPx) / (expandedHeightPx - collapsedHeightPx)).coerceIn(
            0f,
            1f
        )

    val listState = rememberLazyListState()

    LaunchedEffect(isExpanded) {
        val target = if (isExpanded) expandedHeightPx else collapsedHeightPx
        animatedHeight.animateTo(target, spring(Spring.DampingRatioLowBouncy, Spring.StiffnessLow))
    }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(with(density) { animatedHeight.value.toDp() }),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        shape = RoundedCornerShape(dimensionResource(R.dimen.dp_16)),
        colors = CardDefaults.cardColors(containerColor = WhiteColor)
    ) {
        // We use a Box as the main container so we can align the handle to the BottomCenter
        Box(modifier = Modifier.fillMaxSize()) {

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // --- 2. Header Row (Stat Items) ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimensionResource(R.dimen.dp_10))
                        .padding(
                            top = dimensionResource(R.dimen.dp_12),
                            bottom = dimensionResource(R.dimen.dp_8)
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items.forEachIndexed { index, item ->
                        val isLast = index == items.lastIndex

                        val conditionalModifier = if (isLast) {
                            Modifier
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(Color(0xFFFFF3D6), Color(0xFFFFFBF2))
                                    ), shape = RoundedCornerShape(dimensionResource(R.dimen.dp_12))
                                )
                                .padding(vertical = dimensionResource(R.dimen.dp_8))
                                .padding(start = dimensionResource(R.dimen.dp_8))
                        } else Modifier

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .then(conditionalModifier),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = item.value,
                                style = CommonTypography.current.bigFont,
                                color = if (isLast) Yellow else BlackColor
                            )
                            Text(
                                text = item.label,
                                style = CommonTypography.current.smallTxt,
                                maxLines = 1,
                                modifier = Modifier.basicMarquee()
                            )
                        }

                        if (index == 0) {
                            VerticalDivider(
                                modifier = Modifier
                                    .height(43.dp)
                                    .padding(horizontal = 8.dp),
                                color = OutlineDefault
                            )
                        }
                    }
                }

                // --- 3. Expandable Table ---
                if (progress > 0.05f) {
                    ExpandableTable(progress, listState, stockList)
                }
            }

            // --- 4. The Swipe Handle (Anchored to Bottom) ---
            if (isSemiCircle)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .size(37.dp, 17.dp) // Slightly larger hitbox for better UX
                        .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
                        .background(Yellow)
                        .draggable(
                            orientation = Orientation.Vertical,
                            state = rememberDraggableState { delta ->
                                coroutineScope.launch {
                                    val newHeight = (animatedHeight.value + delta).coerceIn(
                                        collapsedHeightPx, expandedHeightPx
                                    )
                                    animatedHeight.snapTo(newHeight)
                                }
                            },
                            onDragStopped = { velocity ->
                                coroutineScope.launch {
                                    val target =
                                        if (animatedHeight.value > (expandedHeightPx + collapsedHeightPx) / 2 || velocity > 600f) {
                                            expandedHeightPx
                                        } else {
                                            collapsedHeightPx
                                        }
                                    animatedHeight.animateTo(
                                        target,
                                        spring(Spring.DampingRatioLowBouncy, Spring.StiffnessLow)
                                    )
                                    val shouldExpand =
                                        animatedHeight.value > (expandedHeightPx + collapsedHeightPx) / 2 || velocity > 600f
                                    onExpandChange(shouldExpand)
                                }
                            }), contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.property_semi_circle),
                        contentDescription = null,
                        tint = Yellow
                    )
                }
        }
    }
}

@Composable
fun ExpandableTable(progress: Float, listState: LazyListState, stockList: List<StockItem>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .graphicsLayer { alpha = progress }
    ) {
        HorizontalDivider(
            color = OutlineDefault.copy(alpha = 0.5f),
            modifier = Modifier.padding(vertical = 8.dp)
        )

        StockRow(
            item = StockItem("Location", "Scanned Stock", "Stock Correction"),
            isHeader = true
        )
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 4.dp),
            thickness = 0.5.dp
        )

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                // Set a fixed height or weight so the LazyColumn knows its bounds
                .heightIn(max = 280.dp)
                .padding(top = 8.dp)
        ) {

            // 2. Dynamic List Items
            items(stockList) { item ->
                StockRow(item = item)
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    thickness = 0.5.dp
                )
            }

            // 3. Extra space at bottom so the last item isn't hidden by the yellow handle
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
/**@Composable
fun StatsCard(
    items: List<StatItem>, modifier: Modifier = Modifier,
    isExpand: Boolean = false,
    isSemiCircle: Boolean = false,
    stockList: List<StockItem> = emptyList()
) {

    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    // Define Heights
    val collapsedHeightPx = with(density) { 80.dp.toPx() }
    val expandedHeightPx = with(density) { 250.dp.toPx() } // Adjust based on table content size

    // Animation State
    val animatedHeight = remember { Animatable(collapsedHeightPx) }

    // Calculate Progress (0f = Collapsed, 1f = Expanded)
    val progress = ((animatedHeight.value - collapsedHeightPx) / (expandedHeightPx - collapsedHeightPx)).coerceIn(0f, 1f)


    Card(
        modifier = modifier
            .border(
                dimensionResource(R.dimen.dp_1),
                OutlineDefault,
                RoundedCornerShape(dimensionResource(R.dimen.dp_16))
            )
            .fillMaxWidth()
            .height(with(density) { animatedHeight.value.toDp() })
            .draggable(
                orientation = Orientation.Vertical,
                state = rememberDraggableState { delta ->
                    coroutineScope.launch {
                        // DRAG DOWN TO OPEN: Positive delta increases height
                        val newHeight = (animatedHeight.value + delta)
                            .coerceIn(collapsedHeightPx, expandedHeightPx)
                        animatedHeight.snapTo(newHeight)
                    }
                },
                onDragStopped = { velocity ->
                    coroutineScope.launch {
                        // Logic: If height is more than halfway OR high downward velocity, EXPAND
                        val target = if (animatedHeight.value > (expandedHeightPx + collapsedHeightPx) / 2 || velocity > 600f) {
                            expandedHeightPx
                        } else {
                            collapsedHeightPx
                        }
                        animatedHeight.animateTo(
                            target,
                            spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)
                        )
                    }
                }
            ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 5.dp
        ),
        shape = RoundedCornerShape(dimensionResource(R.dimen.dp_16)),
        colors = CardDefaults.cardColors(containerColor = WhiteColor)
    ) {

        Box(modifier = Modifier.fillMaxWidth()) {

            Column(
                modifier = Modifier.fillMaxWidth()
                    .fillMaxHeight()
                    .padding(bottom = if (isExpand) 24.dp else 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {


                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = dimensionResource(R.dimen.dp_10))
                        .padding(vertical = dimensionResource(R.dimen.dp_7)),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    items.forEachIndexed { index, item ->

                        val conditionalModifier = if (index == items.lastIndex) {
                            Modifier
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(Color(0xFFFFF3D6), Color(0xFFFFFBF2))
                                    ), shape = RoundedCornerShape(
                                        topStart = dimensionResource(R.dimen.dp_16),
                                        bottomStart = dimensionResource(R.dimen.dp_16)
                                    )
                                )
                                .padding(dimensionResource(R.dimen.dp_8))
                        } else Modifier

                        val textColor = if (index == items.lastIndex) {
                            Yellow
                        } else {
                            BlackColor
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = if (index == 1) dimensionResource(R.dimen.dp_8) else 0.dp,)
                                .then(conditionalModifier),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = item.value,
                                style = CommonTypography.current.bigFont,
                                color = textColor
                            )

                            Text(
                                text = item.label,
                                style = CommonTypography.current.smallTxt,
                                maxLines = 1,
                                overflow = TextOverflow.Clip,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .basicMarquee()
                            )
                        }

                        if (index == 0) {
                            VerticalDivider(color = OutlineDefault)
                        }
                    }

                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer { alpha = progress }
                ) {

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = dimensionResource(R.dimen.dp_180))
                    ) {

                        item {
                            StockRow(
                                item = StockItem("Location", "Scanned Stock", "Stock Correction"),
                                isHeader = true
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        }

                        items(stockList) { item ->
                            StockRow(item = item)
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        }
                    }
                }

            }
            Box(
                modifier = Modifier
                    .size(44.dp, 22.dp)
                    .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
                    .background(Color(0xFFFFC107)),
                contentAlignment = Alignment.BottomCenter
            ) {
                Image(
                    painter = painterResource(R.drawable.property_semi_circle),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                )
            }
        }
    }
}*/

data class StockItem(
    val location: String,
    val scannedStock: String,
    val correction: String
)

@Composable
fun StockRow(
    item: StockItem,
    isHeader: Boolean = false
) {
    val textStyle = if (isHeader) {
        CommonTypography.current.smallTxt.copy(color = BlackColor)
    } else {
        CommonTypography.current.smallTxt
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = item.location,
            modifier = Modifier.weight(1f),
            style = textStyle,
            textAlign = TextAlign.Start
        )
        Text(
            text = item.scannedStock,
            modifier = Modifier.weight(1f),
            style = textStyle,
            textAlign = TextAlign.Center
        )
        Text(
            text = item.correction,
            modifier = Modifier.weight(1f),
            style = textStyle,
            textAlign = TextAlign.End
        )
    }
}



@Composable
fun StatsCardSearch(
    items: List<StatItem>, modifier: Modifier = Modifier
) {

    Card(
        modifier = modifier
            .border(
                dimensionResource(R.dimen.dp_1),
                OutlineDefault,
                RoundedCornerShape(dimensionResource(R.dimen.dp_16))
            )
            .fillMaxWidth()
            .height(dimensionResource(R.dimen.dp_75)),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 5.dp
        ),
        shape = RoundedCornerShape(dimensionResource(R.dimen.dp_16)),
        colors = CardDefaults.cardColors(containerColor = WhiteColor)
    ) {

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = dimensionResource(R.dimen.dp_16))
                    .padding(bottom = dimensionResource(R.dimen.dp_7))
                    .padding(top = dimensionResource(R.dimen.dp_7)),
                verticalAlignment = Alignment.CenterVertically
            ) {

                items.forEachIndexed { index, item ->

                    val conditionalModifier = if (index == items.lastIndex) {
                        Modifier
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFFFFF3D6), Color(0xFFFFFBF2)
                                    )
                                ), shape = RoundedCornerShape(
                                    topStart = dimensionResource(R.dimen.dp_16),
                                    bottomStart = dimensionResource(R.dimen.dp_16)
                                )
                            )
                            .padding(dimensionResource(R.dimen.dp_8))
                    } else {
                        Modifier
                    }

                    val textColor = if (index == items.lastIndex) {
                        Yellow
                    } else {
                        BlackColor
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .then(conditionalModifier),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = item.value,
                            style = CommonTypography.current.bigFont,
                            textAlign = TextAlign.Left,
                            color = textColor
                        )
                        Text(
                            text = item.label, style = CommonTypography.current.smallTxt
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InfoRow(
    label: String, value: String, showDivider: Boolean = true
) {
    Row(
        modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = CommonTypography.current.noteText,
            color = BlackColor,
            textAlign = TextAlign.Left
        )
        Text(
            text = value,
            style = CommonTypography.current.noteText,
            color = TextGrey,
            textAlign = TextAlign.Right
        )
    }

    if (showDivider) {
        HorizontalDivider(
            thickness = dimensionResource(R.dimen.dp_1),
            color = OutlineDefault,
            modifier = Modifier.padding(vertical = dimensionResource(R.dimen.dp_6))
        )
    }
}


@Composable
fun StatsCountCard(
    items: List<StatItem>, modifier: Modifier = Modifier
) {

    Card(
        modifier = modifier
            .shadow(
                elevation = dimensionResource(R.dimen.dp_24),
                shape = RoundedCornerShape(dimensionResource(R.dimen.dp_24)),
                ambientColor = ShadowGray,
                spotColor = ShadowGray
            )
            .border(
                dimensionResource(R.dimen.dp_1),
                LightGray,
                RoundedCornerShape(dimensionResource(R.dimen.dp_24))
            )
            .fillMaxWidth()
            .height(dimensionResource(R.dimen.dp_97)),
        shape = RoundedCornerShape(dimensionResource(R.dimen.dp_24)),
        colors = CardDefaults.cardColors(containerColor = WhiteColor)
    ) {

        Column(
            modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimensionResource(R.dimen.dp_16))
                    .padding(vertical = dimensionResource(R.dimen.dp_16)),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                items.forEachIndexed { index, item ->
                    val itemModifier = if (index == 2) {
                        Modifier
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFFFFF3D6), Color(0xFFFFFBF2)
                                    )
                                ), shape = RoundedCornerShape(dimensionResource(R.dimen.dp_16))
                            )
                            .padding(
                                horizontal = dimensionResource(R.dimen.dp_12),
                                vertical = dimensionResource(R.dimen.dp_8)
                            )
                    } else {
                        Modifier
                    }
                    Column(
                        modifier = itemModifier, horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = item.value,
                            style = CommonTypography.current.bigBoldFont.copy(color = item.valueColor)
                        )
                        Text(
                            text = item.label, style = CommonTypography.current.smallTxt
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun RowWithIconsNext(
    icons: Painter,
    title: String,
    subtitle: String,
    actionText: String,
    onClick: () -> Unit,
    isSubTitle: Boolean = false,
    modifier: Modifier,
    textStyle: TextStyle = CommonTypography.current.textSemiBold
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = dimensionResource(R.dimen.dp_10)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f), horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = title, style = textStyle
            )
            if (isSubTitle) {
                Text(
                    text = subtitle, style = CommonTypography.current.noteText
                )
            }
        }

        Row(
            modifier = Modifier.clickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Row(
                modifier = Modifier
                    .background(
                        TabColor, RoundedCornerShape(dimensionResource(R.dimen.dp_24))
                    )
                    .padding(
                        horizontal = dimensionResource(R.dimen.dp_10),
                        vertical = dimensionResource(R.dimen.dp_10)
                    ), verticalAlignment = Alignment.CenterVertically
            ) {

                Icon(
                    painter = icons,
                    contentDescription = actionText,
                    modifier = Modifier.size(dimensionResource(R.dimen.dp_8))
                )

                Spacer(modifier = Modifier.width(dimensionResource(R.dimen.dp_5)))

                Text(
                    text = actionText,
                    style = CommonTypography.current.noteText,
                    color = BlackColor
                )
            }

            Spacer(modifier = Modifier.width(dimensionResource(R.dimen.dp_10)))

            Image(
                painter = painterResource(id = R.drawable.icon__next), contentDescription = null
            )
        }
    }

    HorizontalDivider(
        thickness = dimensionResource(R.dimen.dp_1),
        modifier = Modifier.padding(vertical = dimensionResource(R.dimen.dp_10)),
        color = OutlineDefault
    )
}


@Composable
fun VerificationCardComposable(
    textValue: String,
    onTextChange: (String) -> Unit,
    label: String,
    imeAction: ImeAction = ImeAction.Done,
    onImeDone: () -> Unit,
    onScanClick: () -> Unit,
    verificationResult: VerificationResult,
    verificationUiState: VerificationUiState,
    infoList: List<Pair<String, String>>,
    modifier: Modifier = Modifier,
    isTextfiled: Boolean = false
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    Card(
        modifier = modifier
            .shadow(
                dimensionResource(R.dimen.dp_24),
                RoundedCornerShape(dimensionResource(R.dimen.dp_16)),
                spotColor = ShadowGray,
                ambientColor = ShadowGray
            )
            .border(
                dimensionResource(R.dimen.dp_1),
                LightGray,
                RoundedCornerShape(dimensionResource(R.dimen.dp_16))
            )
            .fillMaxWidth()
            .height(dimensionResource(R.dimen.dp_223)),
        shape = RoundedCornerShape(24.dp)
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(WhiteColor, RoundedCornerShape(24.dp)),
            verticalArrangement = Arrangement.Top
        ) {

            if (isTextfiled == true) {
                // 🔹 TEXT FIELD
                CommonTextField(
                    config = TextFieldConfig(
                        value = textValue,
                        onValueChange = onTextChange,
                        label = label,
                        isUserName = true,
                        imeAction = imeAction,
                        isBarCode = true,
                        onImeAction = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                            onImeDone()
                        },
                        onClick = onScanClick
                    )
                )
                Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_12)))
            }


            // 🔹 IMAGE + INFO
            Row(
                modifier = Modifier.padding(
                    start = dimensionResource(R.dimen.dp_8),
                    end = dimensionResource(R.dimen.dp_16),
                    bottom = dimensionResource(R.dimen.dp_12)
                )
            ) {

                // IMAGE + BADGE
                Box(
                    modifier = Modifier
                        .width(dimensionResource(R.dimen.dp_105))
                        .height(dimensionResource(R.dimen.dp_170))
                ) {

                    Image(
                        painter = painterResource(R.drawable.image),
                        contentDescription = null,
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                WhiteColor, RoundedCornerShape(dimensionResource(R.dimen.dp_8))
                            )
                            .border(
                                dimensionResource(R.dimen.dp_1),
                                OutlineDefault,
                                RoundedCornerShape(dimensionResource(R.dimen.dp_8))
                            )
                    )

                    Box(modifier = Modifier.align(Alignment.TopStart)) {
                        VerificationBadgeStatic(
                            result = verificationResult,
                            visible = verificationUiState == VerificationUiState.BADGE
                        )
                    }

                    VerificationOverlayStatic(
                        result = verificationResult,
                        visible = verificationUiState == VerificationUiState.OVERLAY
                    )
                }

                Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_8)))

                // INFO LIST
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_8)))

                    infoList.forEachIndexed { index, item ->
                        InfoRow(
                            label = item.first,
                            value = item.second,
                            showDivider = index != infoList.lastIndex
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatsCardWithExpand(
    items: List<StatItem>, onArrowClick: () -> Unit, modifier: Modifier = Modifier
) {

    Card(
        modifier = modifier
            .shadow(
                elevation = dimensionResource(R.dimen.dp_24),
                shape = RoundedCornerShape(dimensionResource(R.dimen.dp_24)),
                ambientColor = ShadowGray,
                spotColor = ShadowGray
            )
            .border(
                dimensionResource(R.dimen.dp_1),
                LightGray,
                RoundedCornerShape(dimensionResource(R.dimen.dp_24))
            )
            .fillMaxWidth()
            .height(dimensionResource(R.dimen.dp_97)),
        shape = RoundedCornerShape(dimensionResource(R.dimen.dp_24)),
        colors = CardDefaults.cardColors(containerColor = WhiteColor)
    ) {

        Column(
            modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimensionResource(R.dimen.dp_16))
                    .padding(bottom = dimensionResource(R.dimen.dp_13))
                    .padding(top = dimensionResource(R.dimen.dp_16)),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                items.forEachIndexed { index, item ->

                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = item.value,
                            style = CommonTypography.current.bigBoldFont.copy(color = item.valueColor)
                        )
                        Text(
                            text = item.label, style = CommonTypography.current.smallTxt
                        )
                    }
                }
            }

            // BOTTOM ARROW SECTION
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = TabColor, shape = RoundedCornerShape(
                            bottomStart = dimensionResource(R.dimen.dp_24),
                            bottomEnd = dimensionResource(R.dimen.dp_24)
                        )
                    )
                    .clickable { onArrowClick() }, contentAlignment = Alignment.Center
            ) {

                Icon(
                    painter = painterResource(id = R.drawable.property_1_arrow_down),
                    contentDescription = null,
                    tint = BlackColor,
                    modifier = Modifier.padding(
                        start = dimensionResource(R.dimen.dp_16),
                        top = dimensionResource(R.dimen.dp_4)
                    )
                )
            }
        }
    }

}


@Composable
fun VerificationCardWithDetails(
    textValue: String, infoList: List<Pair<String, String>>, modifier: Modifier = Modifier
) {

    Card(
        modifier = modifier
            .shadow(
                dimensionResource(R.dimen.dp_24),
                RoundedCornerShape(dimensionResource(R.dimen.dp_16)),
                spotColor = ShadowGray,
                ambientColor = ShadowGray
            )
            .border(
                dimensionResource(R.dimen.dp_1),
                LightGray,
                RoundedCornerShape(dimensionResource(R.dimen.dp_16))
            )
            .background(BackGround, RoundedCornerShape(dimensionResource(R.dimen.dp_16)))
            .fillMaxWidth()
            .height(dimensionResource(R.dimen.dp_223)),
        shape = RoundedCornerShape(24.dp)
    ) {

        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(BackGround, RoundedCornerShape(24.dp)),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_12)))
            TextField(
                value = textValue,
                onValueChange = {

                },
                readOnly = true,
                singleLine = true,
                modifier = Modifier
                    .border(
                        width = 1.dp, color = TabColor, shape = RoundedCornerShape(16.dp)
                    )
                    .background(BackGround, RoundedCornerShape(16.dp)),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    cursorColor = BlackColor,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                textStyle = CommonTypography.current.textSubtext.copy(color = BlackColor)
                    .copy(textAlign = TextAlign.Start)
            )

            Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_12)))

            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(
                        start = dimensionResource(R.dimen.dp_8),
                        end = dimensionResource(R.dimen.dp_16),
                        bottom = dimensionResource(R.dimen.dp_12)
                    )
            ) {

                // ================= IMAGE =================
                Box(
                    modifier = Modifier
                        .width(dimensionResource(R.dimen.dp_105))
                        .fillMaxHeight()
                ) {

                    Image(
                        painter = painterResource(R.drawable.image),
                        contentDescription = null,
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                WhiteColor, RoundedCornerShape(dimensionResource(R.dimen.dp_8))
                            )
                            .border(
                                dimensionResource(R.dimen.dp_1),
                                OutlineDefault,
                                RoundedCornerShape(dimensionResource(R.dimen.dp_8))
                            )
                    )
                }

                Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_8)))

                // ================= INFO LIST + SCROLLBAR =================
                Row(modifier = Modifier.weight(1f)) {

                    // ===== Scrollable content =====
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(scrollState)
                            .padding(end = 6.dp)
                    ) {
                        Spacer(modifier = Modifier.size(8.dp))

                        infoList.forEachIndexed { index, item ->
                            TableView(
                                label = item.first,
                                value = item.second,
                                showDivider = index != infoList.lastIndex
                            )
                        }
                    }

                    // ===== Custom scrollbar (same style you wanted) =====
                    if (scrollState.maxValue > 0) {

                        val scrollRatio =
                            scrollState.value.toFloat() /
                                    scrollState.maxValue.coerceAtLeast(1)

                        val visibleRatio =
                            scrollState.viewportSize.toFloat() /
                                    (scrollState.viewportSize + scrollState.maxValue)

                        val thumbHeight = visibleRatio.coerceIn(0.15f, 1f)

                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .fillMaxHeight()
                        ) {

                            // track
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .background(
                                        OutlineDefault.copy(alpha = 0.25f), RoundedCornerShape(2.dp)
                                    )
                            )

                            // thumb
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(thumbHeight)
                                    .offset {
                                        val track = scrollState.viewportSize.toFloat()
                                        val thumbPx = track * thumbHeight
                                        val offsetPx = (track - thumbPx) * scrollRatio
                                        IntOffset(0, offsetPx.toInt())
                                    }
                                    .background(
                                        OutlineDefault, RoundedCornerShape(2.dp)
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun VerifyInfoCard(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    imeAction: ImeAction = ImeAction.Done,
    onValueChange: (String) -> Unit,
    onImeAction: () -> Unit = {},
    onScanClick: () -> Unit,
    verificationUiState: VerificationUiState,
    verificationResult: VerificationResult,
    infoList: List<Pair<String, String>>,
    enabled: Boolean = true,
    isTrillingIcon: Boolean = true,
    showImagePreview: MutableState<Boolean>,
    selectedPainter: MutableState<Painter?>,
    isReadOnly: Boolean = false,
    showTextFiled: Boolean = false,
) {

    val imageValue = infoList.find {
        it.first.equals(DataStoreManager.getImageLabel(), true)
    }?.second
    val imageUrls = try {
        if (imageValue.isNullOrEmpty()) {
            emptyList()
        } else if (!imageValue.isNullOrEmpty() && imageValue.startsWith("[")) {
            JSONArray(imageValue).let { array ->
                List(array.length()) { index -> array.getString(index) }
            }
        } else {
            listOfNotNull(imageValue)
        }
    } catch (e: Exception) {
        emptyList()
    }
    LogUtils.showLog("imagen url", "VerifyInfoCard: $imageUrls")
    val imagePainters = if (imageUrls.isEmpty()) {
        listOf(rememberAsyncImagePainter(R.drawable.image))
    } else {
        imageUrls.map { rememberAsyncImagePainter(it) }
    }
    val filteredList =
        infoList.filter { !it.first.equals(DataStoreManager.getImageLabel(), true) }

    val scrollState = rememberScrollState()
    val listState = rememberLazyListState()
    val currentIndex = remember { derivedStateOf { listState.firstVisibleItemIndex } }

    Card(
        modifier = modifier
            .shadow(
                dimensionResource(R.dimen.dp_24),
                RoundedCornerShape(dimensionResource(R.dimen.dp_16)),
                spotColor = ShadowGray,
                ambientColor = ShadowGray
            )
            .border(
                dimensionResource(R.dimen.dp_1),
                LightGray,
                RoundedCornerShape(dimensionResource(R.dimen.dp_16))
            )
            .fillMaxWidth()
            .height(dimensionResource(R.dimen.dp_223)),
        shape = RoundedCornerShape(dimensionResource(R.dimen.dp_16))
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(WhiteColor)
        ) {

            // ================= TEXT FIELD =================
            if (showTextFiled){
                CommonTextField(
                    config = TextFieldConfig(
                        value = value,
                        onValueChange = onValueChange,
                        label = label,
                        imeAction = imeAction,
                        isBarCode = true,
                        onImeAction = onImeAction,
                        onClick = {
                            if (enabled) onScanClick()
                        },
                        isTrillingIcon = isTrillingIcon,
                        readOnly = isReadOnly
                    )
                )
            }

            Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_12)))

            // ================= BODY =================
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(
                        start = dimensionResource(R.dimen.dp_8),
                        end = dimensionResource(R.dimen.dp_16),
                        bottom = dimensionResource(R.dimen.dp_12)
                    )
            ) {

                // ================= IMAGE =================
                Box(
                    modifier = Modifier
                        .width(dimensionResource(R.dimen.dp_105))
                        .fillMaxHeight()
                ) {

                    if (imagePainters.size == 1) {

                        Image(
                            painter = imagePainters.first(),
                            contentDescription = null,
                            modifier = Modifier
                                .matchParentSize()
                                .background(
                                    WhiteColor, RoundedCornerShape(dimensionResource(R.dimen.dp_8))
                                )
                                .border(
                                    dimensionResource(R.dimen.dp_1),
                                    OutlineDefault,
                                    RoundedCornerShape(dimensionResource(R.dimen.dp_8))
                                )
                                .clickable(enabled = imageUrls.isNotEmpty()) {
                                    selectedPainter.value = imagePainters.first()
                                    showImagePreview.value = true
                                }
                        )

                    } else {

                        LazyRow(
                            state = listState, modifier = Modifier.matchParentSize()
                        ) {

                            itemsIndexed(imagePainters) { index, painter ->

                                Image(
                                    painter = painter,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .width(dimensionResource(R.dimen.dp_105))
                                        .fillMaxHeight()
                                        .padding(end = 4.dp)
                                        .background(
                                            WhiteColor,
                                            RoundedCornerShape(dimensionResource(R.dimen.dp_8))
                                        )
                                        .border(
                                            dimensionResource(R.dimen.dp_1),
                                            OutlineDefault,
                                            RoundedCornerShape(dimensionResource(R.dimen.dp_8))
                                        )
                                        .clickable(enabled = imageUrls.isNotEmpty()) {
                                            selectedPainter.value = painter
                                            showImagePreview.value = true
                                        })
                            }
                        }

                        Text(
                            text = "${currentIndex.value + 1}/${imagePainters.size}",
                            style = CommonTypography.current.noteText.copy(WhiteColor),
                            color = Color.White,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(6.dp)
                                .background(
                                    Color.Black.copy(alpha = 0.6f), RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Box(Modifier.align(Alignment.TopStart)) {
                        VerificationBadgeStatic(
                            result = verificationResult,
                            visible = verificationUiState == VerificationUiState.BADGE
                        )
                    }

                    VerificationOverlayStatic(
                        result = verificationResult,
                        visible = verificationUiState == VerificationUiState.OVERLAY
                    )
                }


                Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_8)))

                // ================= INFO LIST + SCROLLBAR =================
                Row(modifier = Modifier.weight(1f)) {

                    // ===== Scrollable content =====
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(scrollState)
                            .padding(end = 6.dp)
                    ) {
                        Spacer(modifier = Modifier.size(8.dp))

                        filteredList.forEachIndexed { index, item ->
                            TableView(
                                label = item.first,
                                value = item.second,
                                showDivider = index != infoList.lastIndex
                            )
                        }
                    }

                    // ===== Custom scrollbar (same style you wanted) =====
                    if (scrollState.maxValue > 0) {

                        val scrollRatio =
                            scrollState.value.toFloat() / scrollState.maxValue.coerceAtLeast(1)

                        val visibleRatio =
                            scrollState.viewportSize.toFloat() / (scrollState.viewportSize + scrollState.maxValue)

                        val thumbHeight = visibleRatio.coerceIn(0.15f, 1f)

                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .fillMaxHeight()
                        ) {

                            // track
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .background(
                                        OutlineDefault.copy(alpha = 0.25f), RoundedCornerShape(2.dp)
                                    )
                            )

                            // thumb
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(thumbHeight)
                                    .offset {
                                        val track = scrollState.viewportSize.toFloat()
                                        val thumbPx = track * thumbHeight
                                        val offsetPx = (track - thumbPx) * scrollRatio
                                        IntOffset(0, offsetPx.toInt())
                                    }
                                    .background(
                                        OutlineDefault, RoundedCornerShape(2.dp)
                                    ))
                        }
                    }
                }
            }
        }
    }
}




@Composable
fun TableView(
    label: String,
    value: String,
    showDivider: Boolean
) {
    Column {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {

            val textStyle = CommonTypography.current.noteText.copy(
                platformStyle = PlatformTextStyle(includeFontPadding = false) // ⭐ FIX
            )

            Text(
                text = "$label:",
                style = textStyle,
                color = BlackColor,
                textAlign = TextAlign.Start,
                maxLines = 1,
                overflow = TextOverflow.Visible,
                modifier = Modifier
                    .weight(0.7f)
                    .padding(end = 4.dp)
                    .fillMaxWidth()
                    .basicMarquee(),
            )

            Box(
                modifier = Modifier
                    .width(dimensionResource(R.dimen.dp_1))
                    .fillMaxHeight()
                    .background(OutlineDefault)
            )

            Text(
                text = value,
                style = textStyle,
                color = TextGrey,
                textAlign = TextAlign.End,
                maxLines = 1,
                overflow = TextOverflow.Visible,
                modifier = Modifier
                    .weight(0.7f)
                    .padding(end = 4.dp)
                    .fillMaxWidth()
                    .basicMarquee(),
            )
        }

        if (showDivider) {
            HorizontalDivider(
                thickness = dimensionResource(R.dimen.dp_1),
                color = OutlineDefault,
                modifier = Modifier.padding(vertical = dimensionResource(R.dimen.dp_6))
            )
        }
    }
}


@Composable
fun ImageViewFullScreen(
    showImagePreview: MutableState<Boolean>,
    selectedPainter: MutableState<Painter?>
) {
    Dialog(onDismissRequest = { showImagePreview.value = false }) {

        Box(
            modifier = Modifier
                .width(330.dp)
                .height(534.dp)
                .background(Color.Black.copy(alpha = 0.8f))
        ) {

            selectedPainter.value?.let {

                Image(
                    painter = it,
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .align(Alignment.Center)
                )
            }

            IconButton(
                onClick = { showImagePreview.value = false },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {

                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White
                )
            }
        }
    }
}


enum class CardState { Collapsed, Expanded }

data class StockItems(val location: String, val scanned: String, val correction: String)

@Preview
@Composable
fun SwipeableStockCard() {
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    // Define Heights
    val collapsedHeightPx = with(density) { 140.dp.toPx() }
    val expandedHeightPx = with(density) { 420.dp.toPx() } // Adjust based on table content size

    // Animation State
    val animatedHeight = remember { Animatable(collapsedHeightPx) }

    // Calculate Progress (0f = Collapsed, 1f = Expanded)
    val progress = ((animatedHeight.value - collapsedHeightPx) / (expandedHeightPx - collapsedHeightPx)).coerceIn(0f, 1f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(with(density) { animatedHeight.value.toDp() })
            .draggable(orientation = Orientation.Vertical, state = rememberDraggableState { delta ->
                coroutineScope.launch {
                    // DRAG DOWN TO OPEN: Positive delta increases height
                    val newHeight = (animatedHeight.value + delta).coerceIn(
                        collapsedHeightPx,
                        expandedHeightPx
                    )
                    animatedHeight.snapTo(newHeight)
                }
            }, onDragStopped = { velocity ->
                coroutineScope.launch {
                    // Logic: If height is more than halfway OR high downward velocity, EXPAND
                    val target =
                        if (animatedHeight.value > (expandedHeightPx + collapsedHeightPx) / 2 || velocity > 600f) {
                            expandedHeightPx
                        } else {
                            collapsedHeightPx
                        }
                    animatedHeight.animateTo(
                        target, spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                }
            }),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            // --- HEADER SECTION (Always Visible) ---
            StockHeaderSection()

            // --- TABLE CONTENT (Fades in based on swipe) ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer { alpha = progress }
            ) {
                if (progress > 0.1f) {
                    StockTableContent()
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // --- SWIPE HANDLE INDICATOR ---
            SwipeHandleIndicator(progress = progress)
        }
    }
}

@Composable
fun StockHeaderSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("4,14,000", fontWeight = FontWeight.Bold, fontSize = 22.sp)
            Text("Scanned Stock", fontSize = 12.sp, color = Color.Gray)
        }

        // Vertical Divider
        Box(modifier = Modifier
            .width(1.dp)
            .height(35.dp)
            .background(Color.LightGray.copy(alpha = 0.5f)))

        Column {
            Text("10", fontWeight = FontWeight.Bold, fontSize = 22.sp)
            Text("Stock Correction", fontSize = 12.sp, color = Color.Gray)
        }

        // Total Stock Yellow Badge
        Surface(
            color = Color(0xFFFFF9E6),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("4,14,010", fontWeight = FontWeight.Bold, color = Color(0xFFD4AF37), fontSize = 18.sp)
                Text("Total RFID Stock", fontSize = 10.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun StockTableContent() {
    val stockList = remember {
        listOf(
            StockItems("FOH", "1,50,000", "1"),
            StockItems("BOH", "1,20,000", "3"),
            StockItems("First Floor FOH", "90,000", "4"),
            StockItems("Second Floor FOH", "54,000", "2")
        )
    }

    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))

        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)) {
            Text("Location", fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1.5f), fontSize = 13.sp)
            Text("Scanned Stock", fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1.2f), fontSize = 13.sp)
            Text("Stock Correction", fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f), textAlign = TextAlign.End, fontSize = 13.sp)
        }

        stockList.forEach { item ->
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp)) {
                Text(item.location, color = Color.Gray, modifier = Modifier.weight(1.5f), fontSize = 14.sp)
                Text(item.scanned, color = Color.Gray, modifier = Modifier.weight(1.2f), fontSize = 14.sp)
                Text(item.correction, color = Color.Gray, modifier = Modifier.weight(1f), textAlign = TextAlign.End, fontSize = 14.sp)
            }
            HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.2f))
        }
    }
}

@Composable
fun SwipeHandleIndicator(progress: Float) {
    // Arrow rotates 180 degrees as you swipe down
    val rotation = progress * 180f

    Box(
        modifier = Modifier
            .size(44.dp, 22.dp)
            .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
            .background(Color(0xFFFFC107)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier
                .size(20.dp)
                .graphicsLayer { rotationZ = rotation }
        )
    }
}