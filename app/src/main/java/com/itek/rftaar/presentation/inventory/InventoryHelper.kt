package com.itek.rftaar.presentation.inventory

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.itek.rftaar.R
import com.itek.rftaar.presentation.commonComp.MessageBottomSheet
import com.itek.rftaar.presentation.commonComp.MessageBottomSheetSingleButton
import com.itek.rftaar.presentation.commonComp.MessageUiState
import com.itek.rftaar.presentation.commonComp.SessionBottomSheet
import com.itek.rftaar.presentation.commonComp.SessionUiActions
import com.itek.rftaar.presentation.commonComp.SessionUiState
import com.itek.rftaar.presentation.viewmodel.ReaderViewModel
import com.itek.rftaar.ui.theme.CommonTypography
import com.itek.rftaar.ui.theme.OutlineDefault
import com.itek.rftaar.ui.theme.TabColor
import com.itek.rftaar.ui.theme.TextGrey
import com.itek.rftaar.ui.theme.WhiteColor
import com.itek.rftaar.utils.CommonUtils.chkNull
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun  SessionAlertBottomSheet(
    showSheet: MutableState<Boolean>,
    sheetState: SheetState,
    scope: CoroutineScope
) {

    val formatedText = buildAnnotatedString {
        append(stringResource(R.string.session_is_already_active))

    }
    val uiState = MessageUiState(
        title = stringResource(R.string.stock_session_alert),
        heading = stringResource(R.string.join_active_session),
        subHeading = formatedText,
        primaryButtonText = stringResource(R.string.start_group_stock),
        secondaryButtonText = stringResource(R.string.cancel),
        icon = R.drawable.property_know_more,
        color1 = Color(0xFFF3B100),
        color2 = Color(0xFFFECF53)
    )

    val actions = SessionUiActions(
        onClose = {
            scope.launch {
                sheetState.hide()
                showSheet.value = false
            }
        }, onSecondaryAction = {
            scope.launch {
                sheetState.hide()
                showSheet.value = false
            }
        }, onPrimaryAction = {
            scope.launch {

            }
        }
    )

    MessageBottomSheet(
        uiState = uiState, actions = actions
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun  UploadDataSheet(
    title: String = "",
    heading: String = stringResource(R.string.confirm_movement,title),
    subHeading: AnnotatedString,
    primaryButtonText: String = stringResource(R.string.confirm_and_upload),
    showSheet: MutableState<Boolean>,
    sheetState: SheetState,
    scope: CoroutineScope,
    tagCount: Int,
    isDataUploaded: MutableState<Boolean>
) {

    val uiState = MessageUiState(
        title = chkNull(title,"").split("(")[0].trim(),
        heading = heading,
        subHeading = subHeading,
        primaryButtonText = primaryButtonText,
        secondaryButtonText = "",
        icon = R.drawable.property_know_more,
        color1 = Color(0xFFF3B100),
        color2 = Color(0xFFFECF53)
    )

    val actions = SessionUiActions(
        onClose = {
            scope.launch {
                sheetState.hide()
                showSheet.value = false
            }
        }, onSecondaryAction = {

        }, onPrimaryAction = {

            scope.launch {
                isDataUploaded.value=true
                //DataStoreManager.saveToPreferences("isDataUploaded",true)
                //sheetState.hide()
                //showSheet.value = false
            }
        }
    )

    MessageBottomSheetSingleButton(
        uiState = uiState, actions = actions
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun  RejectDataSheet(
    heading: String,
    subHeading: AnnotatedString,
    showSheet: MutableState<Boolean>,
    sheetState: SheetState,
    scope: CoroutineScope,
    onClick: () -> Unit,
) {

    val uiState = MessageUiState(
        title = stringResource(R.string.txt_reject),
        heading = heading,
        subHeading = subHeading,
        primaryButtonText = stringResource(R.string.txt_confirm_and_reject),
        secondaryButtonText = "",
        icon = R.drawable.property_decode_error,
        color1 = Color(0xFFD50000),
        color2 = Color(0xFFFF3131)
    )

    val actions = SessionUiActions(
        onClose = {
            scope.launch {
                sheetState.hide()
                showSheet.value = false
            }
        }, onSecondaryAction = {

        }, onPrimaryAction = {

            scope.launch {
                onClick()
                //DataStoreManager.saveToPreferences("isDataUploaded",true)
                sheetState.hide()
                showSheet.value = false
            }
        }
    )

    MessageBottomSheetSingleButton(
        uiState = uiState, actions = actions
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun  AcceptDataSheet(
    title: String = "",
    heading: String,
    subHeading: AnnotatedString,
    primaryButtonText: String = "",
    showSheet: MutableState<Boolean>,
    sheetState: SheetState,
    scope: CoroutineScope,
    icon: Int,
    color1: Color,
    color2: Color,
    onClick: () -> Unit
) {

    val uiState = MessageUiState(
        title = chkNull(title,stringResource(R.string.txt_accept)),
        heading = heading,
        subHeading = subHeading,
        primaryButtonText = chkNull(primaryButtonText,stringResource(R.string.txt_confirm_and_accept)),
        secondaryButtonText = "",
        icon = icon,
        color1 = color1,
        color2 = color2
    )

    val actions = SessionUiActions(
        onClose = {
            scope.launch {
                sheetState.hide()
                showSheet.value = false
            }
        }, onSecondaryAction = {

        }, onPrimaryAction = {

            scope.launch {
                onClick()
                //DataStoreManager.saveToPreferences("isDataUploaded",true)
                sheetState.hide()
                showSheet.value = false
            }
        }
    )

    MessageBottomSheetSingleButton(
        uiState = uiState, actions = actions
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun  PartialCompletionDataSheet(
    heading: String,
    subHeading: AnnotatedString,
    confirmationMsg: String,
    buttonText: String,
    showSheet: MutableState<Boolean>,
    sheetState: SheetState,
    scope: CoroutineScope,
    onClick: () -> Unit
) {

    val uiState = MessageUiState(
        title = stringResource(R.string.txt_confirm),
        heading = heading,
        subHeading = subHeading,
        confirmationMessage = confirmationMsg,
        primaryButtonText = buttonText,
        secondaryButtonText = "",
        icon = R.drawable.property_know_more,
        color1 = Color(0xFFF3B100),
        color2 = Color(0xFFFECF53)
    )

    val actions = SessionUiActions(
        onClose = {
            scope.launch {
                sheetState.hide()
                showSheet.value = false
            }
        }, onSecondaryAction = {

        }, onPrimaryAction = {

            scope.launch {
                onClick()
                //DataStoreManager.saveToPreferences("isDataUploaded",true)
                sheetState.hide()
                showSheet.value = false
            }
        }
    )

    MessageBottomSheetSingleButton(
        uiState = uiState, actions = actions
    )
}


@Composable
fun InventoryUploadedSuccess(
    count: Int,
    modifier: Modifier = Modifier,
    animatePulse: Boolean = true
) {

    val infiniteTransition = rememberInfiniteTransition(label = "successPulse")

    val pulse = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAnim"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Box(
            modifier = Modifier
                .padding(top = dimensionResource(R.dimen.dp_60))
                .size(dimensionResource(R.dimen.dp_260)),
            contentAlignment = Alignment.Center
        ) {

            // 🌿 OUTER PULSE RINGS
            if (animatePulse) {
                Canvas(modifier = Modifier.matchParentSize()) {

                    val center = this.center
                    val maxRadius = size.minDimension / 2
                    val startRadius = 90.dp.toPx()

                    // ✅ Static border circle (missing in your code)
                    drawCircle(
                        color = Color(0xFF0A8F08),
                        radius = startRadius,
                        center = center,
                        style = Stroke(width = 2.dp.toPx())
                    )

                    // 🌿 Animated ripple rings
                    repeat(3) { index ->

                        val progress = (pulse.value + index * 0.33f) % 1f

                        val radius = startRadius + (maxRadius - startRadius) * progress

                        val alpha = (1f - progress) * 0.5f

                        drawCircle(
                            color = Color(0xFF2DBE60).copy(alpha = alpha),
                            radius = radius,
                            center = center,
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                }
            }

            // 🟢 INNER GREEN CIRCLE
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0A8F08)),
                contentAlignment = Alignment.Center
            ) {

                Icon(
                    painter = painterResource(R.drawable.succsess_white),
                    contentDescription = "Success",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "$count Items Uploaded",
            style = CommonTypography.current.textSemiBold,
            color = Color(0xFF0A8F08)
        )
    }
}

@Preview
@Composable
fun DemoUploadedSuccess() {
    val animatePulse = remember { mutableStateOf(true) }

    val infiniteTransition = rememberInfiniteTransition(label = "successPulse")

    val pulse = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAnim"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Box(
            modifier = Modifier
                .padding(top = dimensionResource(R.dimen.dp_60))
                .size(dimensionResource(R.dimen.dp_260)),
            contentAlignment = Alignment.Center
        ) {

            // 🌿 OUTER PULSE RINGS
            if (animatePulse.value) {
                Canvas(modifier = Modifier.matchParentSize()) {

                    val center = this.center
                    val maxRadius = size.minDimension / 2
                    val startRadius = 90.dp.toPx()

                    // ✅ Static border circle (missing in your code)
                    drawCircle(
                        color = Color(0xFF0A8F08),
                        radius = startRadius,
                        center = center,
                        style = Stroke(width = 2.dp.toPx())
                    )

                    // 🌿 Animated ripple rings
                    repeat(3) { index ->

                        val progress = (pulse.value + index * 0.33f) % 1f

                        val radius = startRadius + (maxRadius - startRadius) * progress

                        val alpha = (1f - progress) * 0.5f

                        drawCircle(
                            color = Color(0xFF2DBE60).copy(alpha = alpha),
                            radius = radius,
                            center = center,
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                }


            }

            // 🟢 INNER GREEN CIRCLE
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0A8F08)),
                contentAlignment = Alignment.Center
            ) {

                Icon(
                    painter = painterResource(R.drawable.succsess_white),
                    contentDescription = "Success",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "$ Items Uploaded",
            style = CommonTypography.current.textSemiBold,
            color = Color(0xFF0A8F08)
        )
    }
}

@Composable
fun StockCard(
    title: String,
    total: Int,
    date: String,
    expected: Int,
    modifier: Modifier = Modifier
) {

    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(dimensionResource(R.dimen.dp_16)),
        border = BorderStroke(
            dimensionResource(R.dimen.dp_1),
            OutlineDefault
        ),
        colors = CardDefaults.cardColors(containerColor = TabColor)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = dimensionResource(R.dimen.dp_12),
                    vertical = dimensionResource(R.dimen.dp_12)
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = CommonTypography.current.textSemiBold
                )

                Spacer(modifier = Modifier.size(4.dp))

                Text(
                    text = date,
                    style = CommonTypography.current.smallTxt.copy(color = TextGrey),
                    fontWeight = FontWeight.Medium
                )
            }

            if(total>0 || expected>0) {
                Card(
                    shape = RoundedCornerShape(dimensionResource(R.dimen.dp_16)),
                    colors = CardDefaults.cardColors(containerColor = WhiteColor)
                ) {
                    Box(
                        modifier = Modifier
                            .padding(
                                horizontal = dimensionResource(R.dimen.dp_9),
                                vertical = dimensionResource(R.dimen.dp_12)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        /*Text(
                        text = buildAnnotatedString {
                            withStyle(
                                style = SpanStyle(
                                    color = BlackColor,
                                    fontWeight = FontWeight.SemiBold
                                )
                            ) {
                                append(total.toString())
                            }

                            withStyle(
                                style = SpanStyle(
                                    color = TextGrey,
                                    fontWeight = FontWeight.SemiBold
                                )
                            ) {
                                append("/$expected")
                            }
                        },
                        style = CommonTypography.current.smallTxt
                    )*/

                        Text(
                            text = if (expected > 0) "$total/$expected" else "$total",
                            style = CommonTypography.current.smallTxt.copy(color = TextGrey),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockCorrectionSessionContent(
    showSheet: MutableState<Boolean>,
    sheetState: SheetState,
    scope: CoroutineScope,
    navController: NavHostController,
    tagCount: Int,
    onStopSession: () -> Unit,
    readerViewModel: ReaderViewModel
) {

    val uiState = SessionUiState(
        title = stringResource(R.string.stop_session),
        heading = stringResource(R.string.stop_current_session),
        subHeading = AnnotatedString(stringResource(
            R.string.stock_correction_end_the_session
        )),
        primaryButtonText = stringResource(R.string.stop_session_),
        secondaryButtonText = stringResource(R.string.continue_later)
    )

    val actions = SessionUiActions(
        onClose = {
            scope.launch {
                sheetState.hide()
                showSheet.value = false
            }
        }, onSecondaryAction = {},
        onPrimaryAction = onStopSession
    )

    SessionBottomSheet(
        uiState = uiState, actions = actions, isSecondaryButton = false
    )
}

