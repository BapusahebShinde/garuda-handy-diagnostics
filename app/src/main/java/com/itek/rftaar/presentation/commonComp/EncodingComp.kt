package com.itek.rftaar.presentation.commonComp

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.itek.rftaar.R
import com.itek.rftaar.core.common.constants.DateFormatUtils
import com.itek.rftaar.data.entity.TagInfoEntity
import com.itek.rftaar.data.model.TagTime
import com.itek.rftaar.reader.constants.StatusConstants
import com.itek.rftaar.ui.theme.BlackColor
import com.itek.rftaar.ui.theme.CardGreen
import com.itek.rftaar.ui.theme.CommonTypography
import com.itek.rftaar.ui.theme.Green
import com.itek.rftaar.ui.theme.GreenBorder
import com.itek.rftaar.ui.theme.LightBlue
import com.itek.rftaar.ui.theme.OutlineDefault
import com.itek.rftaar.ui.theme.RedColor
import com.itek.rftaar.ui.theme.TabColor
import com.itek.rftaar.ui.theme.TextBlack
import com.itek.rftaar.ui.theme.TextSubtext
import com.itek.rftaar.ui.theme.WhiteColor


enum class VerifyUiState {
    VERIFIED,
    PENDING,
    ERROR
}
fun TagTime.toVerifyUiState(): VerifyUiState {
    return when (status) {
        StatusConstants.VERIFIED,
        StatusConstants.RE_ENCODED -> VerifyUiState.VERIFIED
        StatusConstants.PENDING -> VerifyUiState.PENDING
        StatusConstants.ERROR -> VerifyUiState.ERROR

        else -> VerifyUiState.ERROR
    }
}

fun TagInfoEntity.checkFound(): Int {
    return when (isFound) {
        true -> R.string.txt_found
        false -> R.string.txt_missing
        else -> 0
    }
}


@Composable
fun VerifyUiState.textColor(): Color = when (this) {
    VerifyUiState.VERIFIED -> Green
    VerifyUiState.PENDING -> TextBlack
    VerifyUiState.ERROR -> RedColor
}

@Composable
fun VerifyUiState.iconPainter(): Painter? = when (this) {
    VerifyUiState.VERIFIED -> painterResource(R.drawable.property_1_success)

    VerifyUiState.ERROR -> painterResource(R.drawable.property_1_error)

    VerifyUiState.PENDING -> null
}

fun statusForState(state: StepUiState): String {
    return when (state) {
        StepUiState.ACTIVE -> "Processing"
        StepUiState.COMPLETED -> "Completed"
        StepUiState.ERROR -> "Error"
        StepUiState.IDLE -> ""
    }
}

data class StepItem(
    val icon: Int,
    val label: String,
    val status: String
)

enum class StepUiState {
    IDLE,
    ACTIVE,
    COMPLETED,
    ERROR
}

fun messageForStep(
    state: StepUiState,
    idle: String = "",
    active: String,
    completed: String,
    error: String?
): String? {
    return when (state) {
        StepUiState.IDLE -> idle
        StepUiState.ACTIVE -> active
        StepUiState.COMPLETED -> completed
        StepUiState.ERROR -> error
    }
}

@Composable
fun StepIndicatorRow(
    steps: List<StepItem>,
    stepStates: List<StepUiState>,
    modifier: Modifier = Modifier,
    circleSize: Dp = 44.dp,
    lineWidth: Dp = 36.dp,
    borderColor: Color = Color.LightGray,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, step ->

            val state = stepStates[index]

            val iconPainter = when (state) {
                StepUiState.COMPLETED ->
                    painterResource(R.drawable.property_check_selected)
                StepUiState.ERROR ->
                    painterResource(R.drawable.property_decode_error)
                else ->
                    painterResource(step.icon)
            }

            val iconTint = when (state) {
                StepUiState.ACTIVE -> BlackColor
                StepUiState.COMPLETED, StepUiState.ERROR -> WhiteColor
                else -> Color.Gray
            }

            val textColor = when (state) {
                StepUiState.ACTIVE -> BlackColor
                StepUiState.COMPLETED -> Green
                StepUiState.ERROR -> RedColor
                else -> Color.Gray
            }

            val backgroundBrush = when (state) {
                StepUiState.COMPLETED ->
                    Brush.verticalGradient(
                        listOf(Color(0xFF048204), Color(0xFF02AC02))
                    )
                StepUiState.ERROR ->
                    Brush.verticalGradient(
                        listOf(Color(0xFFD50000), Color(0xFFFF3131))
                    )
                else ->
                    Brush.verticalGradient(listOf(Color.White, Color.White))
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                /* ---------- ICON + LINES ---------- */

                Row(verticalAlignment = Alignment.CenterVertically) {

                    if (index != 0) {
                        AnimatedStepLine(
                            lineWidth = lineWidth,
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(circleSize)
                            .border(1.dp, borderColor, CircleShape)
                            .background(backgroundBrush, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = iconPainter,
                            contentDescription = step.label,
                            tint = iconTint
                        )
                    }

                    if (index != steps.lastIndex) {
                        AnimatedStepLine(
                            lineWidth = lineWidth,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                /* ---------- TEXT (FINAL FIX) ---------- */

                val leftLineWidth =
                    if (index != 0) lineWidth else 0.dp
                val rightLineWidth =
                    if (index != steps.lastIndex) lineWidth else 0.dp

                val centerOffset = (leftLineWidth - rightLineWidth) / 2

                Column(
                    modifier = Modifier
                        .width(72.dp)
                        .offset(x = centerOffset),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = step.label,
                        style = CommonTypography.current.textSemiBold,
                        color = textColor,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = step.status,
                        style = CommonTypography.current.smallTxt,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}


@Composable
fun AnimatedStepLine(
    modifier: Modifier = Modifier,
    lineWidth: Dp,
    height: Dp = 1.dp,
    inactiveColor: Color = Color.LightGray,
    activeColor: Color = OutlineDefault
) {

    Box(
        modifier = modifier
            .width(lineWidth)
            .height(height)
            .background(inactiveColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .background(activeColor)
        )
    }
}


@Composable
fun CompletionRays(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF048204)
) {
    Canvas(modifier = modifier) {
        val centerX = size.width / 2
        val startY = 0f
        val lineLength = size.height / 1

        // Middle line
        drawLine(
            color = color,
            start = Offset(centerX, startY),
            end = Offset(centerX, startY + lineLength),
            strokeWidth = 10f,
            cap = StrokeCap.Round
        )

        // Left line
        drawLine(
            color = color,
            start = Offset(centerX - 40, startY + 25),
            end = Offset(centerX - 5, startY + lineLength),
            strokeWidth = 10f,
            cap = StrokeCap.Round
        )

        // Right line
        drawLine(
            color = color,
            start = Offset(centerX + 40, startY + 25),
            end = Offset(centerX + 5, startY + lineLength),
            strokeWidth = 10f,
            cap = StrokeCap.Round
        )
    }
}


@Composable
fun VerifyTag(
    epcList: List<String>,
    onVerifyClick: (String) -> Unit,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        epcList.forEachIndexed { index, epc ->

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                EanListRow(
                    title = epc,
                    actionText = stringResource(id = R.string.to_verify),
                    onActionClick = { onVerifyClick(epc) },
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(dimensionResource(R.dimen.dp_8)))

                CircularIcon(
                    iconRes = R.drawable.property_search_radar,
                    modifier = Modifier.clickable(
                        onClick = { onClick() },
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ),
                    backgroundColor = SolidColor(TabColor)
                )
            }

            if (index < epcList.lastIndex) {
                HorizontalDivider(
                    thickness = dimensionResource(R.dimen.dp_1),
                    color = OutlineDefault,
                    modifier = Modifier.padding(vertical = dimensionResource(R.dimen.dp_14))
                )
            }
        }
    }
}


@Composable
fun CardViewWithHSI(
    searchOnClick: () -> Unit,
    detailOnClick: () -> Unit,
    epcNo: TagTime,
    searchIcon: Int,
    detailIcon: Int,
) {

    Card(modifier = Modifier
        .background(color = CardGreen, shape = RoundedCornerShape(dimensionResource(R.dimen.dp_16)))
        .border(
            width = dimensionResource(R.dimen.dp_1),
            color = GreenBorder,
            shape = RoundedCornerShape(dimensionResource(R.dimen.dp_16))
        ),
        shape = RoundedCornerShape(dimensionResource(R.dimen.dp_16))) {
        Row(
            modifier = Modifier
                .background(CardGreen)
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.dp_10)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.Center) {
                Text(
                    text = epcNo.barcode,
                    style = CommonTypography.current.textSemiBold,
                    color = Green
                )

                Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_4)))

                Text(
                    text = DateFormatUtils.formatToDisplayTime(epcNo.timeStamp),
                    style = CommonTypography.current.smallTxt,
                    color = TextSubtext
                )
            }

            Spacer(modifier = Modifier.width(dimensionResource(R.dimen.dp_8)))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularIcon(
                    iconRes = detailIcon,
                    modifier = Modifier.clickable(onClick = { detailOnClick() }),
                    backgroundColor = SolidColor(WhiteColor),
                    tintColor = Green,
                    outlineColor = GreenBorder,
                    isShadow = false
                )

                Spacer(modifier = Modifier.width(dimensionResource(R.dimen.dp_8)))

                CircularIcon(
                    iconRes = searchIcon,
                    modifier = Modifier.clickable(onClick = { searchOnClick() }),
                    backgroundColor = SolidColor(WhiteColor),
                    tintColor = Green,
                    outlineColor = GreenBorder,
                    isShadow = false
                )
            }
        }
    }

}


@Composable
fun RowViewWithHSI(
    onClick: () -> Unit,
    epcList: List<TagTime>,
    isIcon: Boolean = false,
    icon: Painter?,
    grouped: Boolean,
    expandedEans: SnapshotStateMap<String, Boolean>,
    clickableIconSearch: Int,
    clickableIconDetails: Int,
    onSearchClick: (TagTime) -> Unit,
    onDetailsClick: (TagTime) -> Unit,
    iconBackGroundColor: Brush = SolidColor(TabColor),
    listState: LazyListState
) {
    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxWidth()
            //.verticalScroll(rememberScrollState())
    ) {

        // -------------------------
        // NOT GROUPED
        // -------------------------
        if (!grouped) {

            items(
                items = epcList,
                key = { it.epc }
            ) { item ->
                val uiState = item.toVerifyUiState()

                FullRow(
                    item = item,
                    isIcon = false,
                    stateIcon = uiState.iconPainter(), // ✅ only state icon
                    icon = null,                      // ✅ NO duplicate
                    onIconClick = onClick,
                    texColor = uiState.textColor(),
                    clickableIconSearch = clickableIconSearch,
                    clickableIconDetails = clickableIconDetails,
                    onSearchClick = { onSearchClick(item) },
                    onDetailsClick = { onDetailsClick(item) },
                    isVerified = uiState == VerifyUiState.VERIFIED,
                    iconBackGroundColor = iconBackGroundColor
                )
            }

        }
        else {

            // -------------------------
            // GROUPED
            // -------------------------
            epcList.groupBy { it.barcode }.forEach { (barcode, items) ->

                val isExpanded = expandedEans[barcode] ?: false

                val parentState = when {
                    items.any { it.toVerifyUiState() == VerifyUiState.ERROR } ->
                        VerifyUiState.ERROR

                    items.any { it.toVerifyUiState() == VerifyUiState.PENDING } ->
                        VerifyUiState.PENDING

                    else -> VerifyUiState.VERIFIED
                }

                val showExpandIcon = items.size > 1

                // ---------- PARENT ROW ----------
                item(key = barcode) {
                FullRow(
                    item = items.first(),
                    isIcon = showExpandIcon, // arrow only if grouped
                    stateIcon = parentState.iconPainter(), // ✅ correct icon
                    icon = if (showExpandIcon)
                        painterResource(R.drawable.icon__next)
                    else null,
                    onIconClick = {
                        expandedEans[barcode] = !isExpanded
                    },
                    texColor = parentState.textColor(),
                    clickableIconSearch = clickableIconSearch,
                    clickableIconDetails = clickableIconDetails,
                    onSearchClick = { onSearchClick(items.first()) },
                    onDetailsClick = { onDetailsClick(items.first()) },
                    isVerified = parentState == VerifyUiState.VERIFIED,
                    count = if (showExpandIcon && !isExpanded) items.size else 0,
                    isExpanded = isExpanded,
                    iconBackGroundColor = iconBackGroundColor
                )
                }

                // ---------- CHILDREN ----------
                if (isExpanded) {
                    items(
                        items.drop(1),
                        key = { it.epc }
                    ) { child ->
                        val childState = child.toVerifyUiState()

                        FullRow(
                            item = child,
                            isIcon = false,
                            stateIcon = childState.iconPainter(),
                            icon = null,
                            indent = true,
                            onIconClick = {},
                            texColor = childState.textColor(),
                            clickableIconSearch = clickableIconSearch,
                            clickableIconDetails = clickableIconDetails,
                            onSearchClick = { onSearchClick(child) },
                            onDetailsClick = { onDetailsClick(child) },
                            isVerified = childState == VerifyUiState.VERIFIED,
                            iconBackGroundColor = iconBackGroundColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RowViewWithHSI1(
    epcList: List<TagInfoEntity>,
    onSearchClick: (TagInfoEntity) -> Unit,
    //onDetailsClick: (TagInfoEntity) -> Unit,
    iconBackGroundColor: Brush = SolidColor(WhiteColor)
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {

            epcList.forEach { item ->
                //val uiState = item.toVerifyUiState()

                FullRow1(
                    item = item,
                    texColor = if(item.isTagWriteDone) Green else TextBlack,
                    onSearchClick = { onSearchClick(item) },
                    //onDetailsClick = { onDetailsClick(item) },
                    iconBackGroundColor = iconBackGroundColor
                )
            }

    }
}



@Composable
private fun FullRow(
    item: TagTime,
    isIcon: Boolean,
    stateIcon: Painter?,
    icon: Painter?,
    indent: Boolean = false,
    onIconClick: () -> Unit,
    texColor: Color = TextBlack,
    clickableIconSearch: Int,
    clickableIconDetails: Int,
    onSearchClick: () -> Unit,
    onDetailsClick: () -> Unit,
    isVerified: Boolean = false,
    count: Int = 0,
    isExpanded: Boolean = false,
    iconBackGroundColor : Brush
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = if (indent) 24.dp else 0.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Column(modifier = Modifier.weight(1f)) {

            Row(verticalAlignment = Alignment.CenterVertically) {

                Text(
                    text = item.barcode,
                    style = CommonTypography.current.textSemiBold,
                    color = texColor,
                    maxLines = 1
                )

                if (count > 0) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "($count)",
                        style = CommonTypography.current.textSemiBold,
                        color = TextBlack,
                    )
                }

                // ✅ state icon only
                if (stateIcon != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Image(
                        painter = stateIcon,
                        modifier = Modifier.size(dimensionResource(R.dimen.dp_16)),
                        contentDescription = null
                    )
                }

                // ✅ arrow only
                if (isIcon && icon != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        painter = icon,
                        modifier = Modifier
                            .size(dimensionResource(R.dimen.dp_16))
                            .graphicsLayer {
                                rotationZ = if (isExpanded) 90f else 0f
                            }
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                onIconClick()
                            },
                        contentDescription = null,
                        tint = LightBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = DateFormatUtils.formatToDisplayTime(item.timeStamp),
                style = CommonTypography.current.smallTxt,
                color = TextSubtext
            )
        }

        Spacer(modifier = Modifier.width(dimensionResource(R.dimen.dp_8)))

        CircularIcon(
            iconRes = clickableIconDetails,
            modifier = Modifier.clickable { onDetailsClick() },
            backgroundColor = iconBackGroundColor,
            outlineColor = OutlineDefault,
            isShadow = false,
        )

        Spacer(modifier = Modifier.size(8.dp))

        CircularIcon(
            iconRes = clickableIconSearch,
            modifier = Modifier.clickable { onSearchClick() },
            backgroundColor = iconBackGroundColor,
            outlineColor = OutlineDefault,
            isShadow = false,
        )
    }

    HorizontalDivider(
        color = OutlineDefault,
        modifier = Modifier.padding(vertical = dimensionResource(R.dimen.dp_10))
    )
}

@Composable
private fun FullRow1(
    item: TagInfoEntity,
    indent: Boolean = false,
    texColor: Color = TextBlack,
    searchIcon: Int = R.drawable.property_gieger_og,
    detailsIcon: Int = R.drawable.property_info,
    onSearchClick: () -> Unit,
    iconBackGroundColor: Brush,
    //onDetailsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = if (indent) 24.dp else 0.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Column(modifier = Modifier.weight(1f)) {

            Row(verticalAlignment = Alignment.CenterVertically) {

                Text(
                    text = item.barcode,
                    style = CommonTypography.current.textSemiBold,
                    color = texColor,
                    maxLines = 1
                )

                /*// ✅ state icon only
                if (stateIcon != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Image(
                        painter = stateIcon,
                        modifier = Modifier.size(dimensionResource(R.dimen.dp_16)),
                        contentDescription = null
                    )
                }*/

                // ✅ arrow only
                /*if (isIcon && icon != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        painter = icon,
                        modifier = Modifier
                            .size(dimensionResource(R.dimen.dp_16))
                            .graphicsLayer {
                                rotationZ = 0f
                            }
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                onIconClick()
                            },
                        contentDescription = null,
                        tint = LightBlue
                    )
                }*/
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if(item.isTagWriteDone) "Decoded" else  "Not Decoded",
                style = CommonTypography.current.smallTxt,
                color = if(item.isTagWriteDone) Green else TextSubtext
            )
        }


        /*Spacer(modifier = Modifier.width(dimensionResource(R.dimen.dp_8)))

        CircularIcon(
            iconRes = detailsIcon,
            modifier = Modifier.clickable { onDetailsClick() },
            backgroundColor = iconBackGroundColor,
            outlineColor = OutlineDefault,
            isShadow = false,
        )*/

        Spacer(modifier = Modifier.size(8.dp))

        CircularIcon(
            iconRes = searchIcon,
            modifier = Modifier.clickable { onSearchClick() },
            backgroundColor = iconBackGroundColor,
            outlineColor = OutlineDefault,
            isShadow = false,
        )
    }

    HorizontalDivider(
        color = OutlineDefault,
        modifier = Modifier.padding(vertical = dimensionResource(R.dimen.dp_10))
    )
}



data class ToggleArgs(val modifier: Modifier = Modifier,
                      val width: Dp = 45.dp,
                      val height: Dp = 25.dp,
                      val thumbSize: Dp = 20.dp,
                      val checkedColor: Color = Color(0xFFFFC107),
                      val uncheckedColor: Color = Color(0xFFDADADA) )

@Composable
fun GroupEanToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    args : ToggleArgs = ToggleArgs()
) {

    val thumbOffset = animateDpAsState(
        targetValue = if (checked) args.width - args.thumbSize - 3.dp else 3.dp,
        label = "ThumbOffset"
    )

    Row(
        modifier = args.modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .width(args.width)
                .height(args.height)
                .clip(RoundedCornerShape(args.height / 2))
                .background(if (checked) args.checkedColor else args.uncheckedColor)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    onCheckedChange(!checked)
                }
        ) {

            Box(
                modifier = Modifier
                    .offset(x = thumbOffset.value)
                    .size(args.thumbSize)
                    .align(Alignment.CenterStart)
                    .clip(CircleShape)
                    .background(Color.White)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = label,
            style = CommonTypography.current.noteText,
            fontWeight = FontWeight.SemiBold,
            color = TextSubtext
        )
    }
}

