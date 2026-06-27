package com.itek.rftaar.presentation.commonComp

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itek.rftaar.R
import com.itek.rftaar.api.constants.ParameterConstants
import com.itek.rftaar.core.common.utils.LogUtils
import com.itek.rftaar.core.database.DataStoreManager
import com.itek.rftaar.domain.model.LocationModel
import com.itek.rftaar.presentation.inventory.SearchBar
import com.itek.rftaar.presentation.movement.PickerRow
import com.itek.rftaar.ui.theme.BlackColor
import com.itek.rftaar.ui.theme.ButtonGray
import com.itek.rftaar.ui.theme.CommonTypography
import com.itek.rftaar.ui.theme.ErrorBgColor
import com.itek.rftaar.ui.theme.Green
import com.itek.rftaar.ui.theme.GreenBorder
import com.itek.rftaar.ui.theme.LightGray
import com.itek.rftaar.ui.theme.LightYellow
import com.itek.rftaar.ui.theme.OutlineDefault
import com.itek.rftaar.ui.theme.RedColor
import com.itek.rftaar.ui.theme.TabColor
import com.itek.rftaar.ui.theme.TextGrey
import com.itek.rftaar.ui.theme.TextSubtext
import com.itek.rftaar.ui.theme.WhiteColor
import com.itek.rftaar.ui.theme.Yellow
import com.itek.rftaar.utils.SoundUtils
import kotlinx.coroutines.delay
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.platform.LocalConfiguration
import com.itek.rftaar.data.entity.MenuEntity
import com.itek.rftaar.utils.CommonUtils.getMenuIconByCode
import androidx.compose.foundation.layout.defaultMinSize


@Composable
fun CircularIcon(
    iconRes: Int,
    iconSize: Dp = dimensionResource(id = R.dimen.dp_24),
    circleSize: Dp = dimensionResource(id = R.dimen.dp_44),
    padding: Dp = dimensionResource(id = R.dimen.dp_10),
    shadowElevation: Dp = dimensionResource(id = R.dimen.dp_8),
    modifier: Modifier = Modifier,
    backgroundColor: Brush = SolidColor(WhiteColor),
    tintColor: Color = BlackColor,
    outlineColor: Color = OutlineDefault,
    isShadow: Boolean = true
) {
    Box(
        modifier = modifier
            .size(circleSize)
            .then(
                if (isShadow) Modifier.shadow(shadowElevation, CircleShape)
                else Modifier
            )
            .border(dimensionResource(id = R.dimen.dp_1), outlineColor, CircleShape)
            .background(backgroundColor, CircleShape)
            .padding(padding),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(iconSize),
            tint = tintColor
        )
    }
}

@Composable
fun CircularText(
    text : String,
    circleSize: Dp = dimensionResource(id = R.dimen.dp_64),
    shadowElevation: Dp = dimensionResource(id = R.dimen.dp_1),
    modifier: Modifier = Modifier,
    backgroundColor: Brush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFF3F3F3).copy(alpha = 0.5f), Color(0xFFFFFFFF)
        )
    ),
    textStyle: TextStyle = CommonTypography.current.bigFont
) {
    Box(
        modifier = modifier
            .size(circleSize)
            .shadow(shadowElevation, shape = CircleShape)
            .border(dimensionResource(id = R.dimen.dp_1), OutlineDefault, CircleShape)
            .background(backgroundColor, CircleShape)
            .padding(circleSize * 0.15f),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text,
            style = textStyle,
            textAlign = TextAlign.Center)
    }
}

@Composable
fun UploadStatusChip(
    tagUploadedEncodedCount: Int,
    tagEncodedCount: Int,
) {

    Card(
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.dp_24)),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 9.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = TabColor
        ),
        border = BorderStroke(
            width = dimensionResource(id = R.dimen.dp_1),
            color = TabColor
        )
    ) {

        Row(
            modifier = Modifier
                .wrapContentHeight()
                .padding(horizontal = 6.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                painter = painterResource(R.drawable.upload_icon),
                contentDescription = "Upload",
                tint = Color(0xFFFECF53),
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = "$tagUploadedEncodedCount/$tagEncodedCount",
                color = Color.DarkGray,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}


@Composable
fun CommonButton(
    text: String,
    onClick: () -> Unit,
    icon: Painter? = null,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    gradientBrush: Brush = Brush.horizontalGradient(
        colors = listOf(
            BlackColor, ButtonGray
        )
    ),
    contentColor: Color = WhiteColor,
    textStyle: TextStyle = CommonTypography.current.buttonSemiBold.copy(color = if (enabled) contentColor else LightGray),
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(dimensionResource(id = R.dimen.dp_48))
            .background(
                if (enabled) gradientBrush else SolidColor(TabColor),
                RoundedCornerShape(dimensionResource(id = R.dimen.dp_16))
            )
            .border(
                width = 1.dp,
                color = OutlineDefault,
                shape = RoundedCornerShape(dimensionResource(id = R.dimen.dp_16))
            ),
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.dp_16)),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent, disabledContainerColor = Color.Transparent
        ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {

            if (isLoading) {

                CircularProgressIndicator(
                    modifier = Modifier.size(dimensionResource(R.dimen.dp_22)),
                    strokeWidth = 2.dp,
                    color = Yellow
                )

                Spacer(
                    modifier = Modifier.size(
                        dimensionResource(R.dimen.dp_8)
                    )
                )
            }

            else if (icon != null) {
                Icon(
                    painter = icon,
                    contentDescription = null,
                    tint = Yellow
                )
                Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_8)))
            }

            Text(
                text = text,
                style = textStyle,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Visible,
                modifier = Modifier.basicMarquee()
            )
        }
    }
}

enum class CodeType {
    BARCODE,
    EPC,
    TID,
    RFID,
    NONE
}

data class TextFieldConfig(
    val value: String,
    val label: String,
    val isPassword: Boolean = false,
    val imeAction: ImeAction = ImeAction.Next,
    val focusRequester: FocusRequester? = null,
    val onValueChange: (String) -> Unit = {},
    val onImeAction: () -> Unit = {},
    val isError: Boolean = false,
    val isUserName : Boolean = false,
    val isServerUrl : Boolean = false,
    val maxLine : Boolean = false,
    val singleLine : Boolean = true,
    val email : Boolean = false,
    val isBarCode : Boolean = false,
    val codeType: CodeType = CodeType.NONE,
    val onClick: (() -> Unit)? = null,
    val isSearch: Boolean =false,
    val isNote: Boolean =false,
    val isTrillingIcon: Boolean = true,
    val readOnly: Boolean = false
)

@Composable
fun CommonTextField(
    config: TextFieldConfig,
    modifier: Modifier = Modifier
) {
    val passwordVisible = remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused = interactionSource.collectIsFocusedAsState()
    val borderColor = getBorderColor(config, isFocused.value)
    val backGroundColor = getBgColor(config)
    val labelStyle = getLabelStyle(config, isFocused.value)
    val keyboardType = getKeyboardType(config)
    val textStyle = getTextStyle(config,isFocused.value)
    val finalTextStyle = if (config.value.isEmpty()) {
        textStyle
    } else {
        textStyle.copy(
            textAlign = TextAlign.Start,
//            lineHeight =dimensionResource(id = R.dimen.sp_56).value.sp,
        )
    }

    val customSelectionColors = TextSelectionColors(
        handleColor = Color.Transparent,
        backgroundColor = Color.Transparent
    )
    CompositionLocalProvider(LocalTextSelectionColors provides customSelectionColors) {
        TextField(
            value = config.value,
            onValueChange = config.onValueChange,
            label = {
                Text(
                    text = if (config.isSearch || config.isServerUrl || config.isUserName || config.isPassword || config.isError) config.label else String.format(
                        stringResource(
                            if (config.value.isNotEmpty()) {
                                if (config.isBarCode) R.string.details else R.string.empty
                            } else R.string.enter
                        ), config.label
                    ), style = labelStyle
                )
            },
            singleLine = config.singleLine,
            maxLines = if (config.maxLine) 5 else 1,
            readOnly = config.readOnly,
            modifier = modifier
                .fillMaxWidth()
                .defaultMinSize(
                    minHeight = if (config.maxLine)
                        dimensionResource(id = R.dimen.dp_100)
                    else
                        dimensionResource(id = R.dimen.dp_56)
                )
                .border(
                    width = if (isFocused.value) dimensionResource(id = R.dimen.dp_2) else dimensionResource(
                        id = R.dimen.dp_1
                    ),
                    color = borderColor,
                    shape = RoundedCornerShape(dimensionResource(id = R.dimen.dp_16))
                )
                .background(backGroundColor, shape = RoundedCornerShape(16.dp))
                .then(config.focusRequester?.let { Modifier.focusRequester(it) } ?: Modifier),
            interactionSource = interactionSource,
            visualTransformation = getVisualTransformation(config, passwordVisible.value),
            keyboardOptions = KeyboardOptions(
                autoCorrect = false,
                keyboardType = keyboardType,
                imeAction = if (config.isSearch) ImeAction.Search else config.imeAction
            ),
            keyboardActions = KeyboardActions(onSearch = {
                if (config.isSearch) {
                    config.onImeAction()
                }
            }, onNext = {
                if (!config.isSearch) {
                    config.onImeAction()
                }
            }, onDone = {
                if (!config.isSearch) {
                    config.onImeAction()
                }
            }),
            trailingIcon = {
                if (config.isTrillingIcon) {
                    TrailingIcons(
                        config = config,
                        isFocused = isFocused.value,
                        passwordVisible = passwordVisible,
                        isError = config.isError,
                        isBarCode = config.isBarCode
                    )
                }
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                cursorColor = BlackColor,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent,
            ),
            textStyle = finalTextStyle,
        )
    }

}


@Composable
private fun getBorderColor(config: TextFieldConfig, focused: Boolean): Color =
    when {
        config.isError -> RedColor
        focused -> BlackColor
        else -> LightGray
    }

@Composable
private fun getTextStyle(config: TextFieldConfig,focused: Boolean): TextStyle =
    when {
        focused -> CommonTypography.current.textSemiBold
        config.value.isNotEmpty() ->  CommonTypography.current.textSubtext.copy(BlackColor)
        else -> CommonTypography.current.textMedium
    }

@Composable
private fun getBgColor(config: TextFieldConfig): Color =
    when {
        config.isError -> ErrorBgColor
        else -> WhiteColor
    }


@Composable
private fun getLabelStyle(config: TextFieldConfig, focused: Boolean) =
    when {
        config.isError -> CommonTypography.current.textSubtext.copy(color = RedColor)
        focused && config.value.isEmpty() -> CommonTypography.current.smallTxt
        !focused && config.value.isNotEmpty() -> CommonTypography.current.smallTxt
        else -> CommonTypography.current.textSubtext
    }

private fun getKeyboardType(config: TextFieldConfig): KeyboardType =
    when {
        config.isPassword -> KeyboardType.Password
        config.email -> KeyboardType.Email
        else -> KeyboardType.Password
    }

private fun getVisualTransformation(
    config: TextFieldConfig,
    passwordVisible: Boolean
): VisualTransformation =
    if (config.isPassword && !passwordVisible) PasswordVisualTransformation()
    else VisualTransformation.None


@Composable
private fun TrailingIcons(
    config: TextFieldConfig,
    isFocused: Boolean,
    passwordVisible: MutableState<Boolean>,
    isError: Boolean,
    isBarCode: Boolean
) {

    Row {

        if (config.isSearch && config.value.isEmpty()){
            Icon(painterResource(R.drawable.property_search),
                contentDescription = null,
                modifier = Modifier.size(dimensionResource(R.dimen.dp_24)))
        }

        if (!config.isPassword && config.value.isNotEmpty()) {
            IconButton(onClick = { config.onValueChange("") }) {
                Icon(
                    painter = painterResource(R.drawable.clear),
                    contentDescription = "Clear text"
                )
            }
        }

        if (isError && !config.isPassword && config.value.isEmpty()) {
            Icon(
                painter = painterResource(R.drawable.error),
                contentDescription = "Error indicator",
                tint = RedColor
            )
        }

        if(config.isBarCode && config.value.isEmpty()){
                IconButton(onClick = {config.onClick?.invoke() }) {
                    Icon(
                        painter = painterResource(R.drawable.property_barcode),
                        contentDescription = "Barcode",
                    )
                }

        }

        if (config.value.isEmpty()) {
            when (config.codeType) {
                CodeType.BARCODE -> {
                    IconButton(onClick = { config.onClick?.invoke() }) {
                        Icon(
                            painter = painterResource(R.drawable.property_barcode),
                            contentDescription = "Barcode"
                        )
                    }
                }
                CodeType.EPC -> {
                    IconButton(onClick = { config.onClick?.invoke() }) {
                        Icon(
                            painter = painterResource(R.drawable.property_rfidd),
                            contentDescription = "EPC"
                        )
                    }
                }
                CodeType.TID -> {
                    IconButton(onClick = { config.onClick?.invoke() }) {
                        Icon(
                            painter = painterResource(R.drawable.property_qr),
                            contentDescription = "TID"
                        )
                    }
                }
                CodeType.RFID -> {
                    IconButton(onClick = { config.onClick?.invoke() }) {
                        Icon(
                            painter = painterResource(R.drawable.property_rfidd),
                            contentDescription = "RFID"
                        )
                    }
                }
                else -> {}
            }
        }

        if (config.isPassword) {

            if (isError && config.value.isEmpty()) {
                Icon(
                    painter = painterResource(R.drawable.error),
                    contentDescription = "Error indicator",
                    tint = RedColor
                )
            }else{
                IconButton(onClick = { passwordVisible.value = !passwordVisible.value }) {
                    Icon(
                        painter = painterResource(
                            if (passwordVisible.value)
                                R.drawable.property_1_eye
                            else
                                R.drawable.property_1_eye_close
                        ),
                        contentDescription = if (passwordVisible.value)
                            "Hide password"
                        else
                            "Show password"
                    )
                }
            }

        }
    }
}

@Composable
fun InfoRow(
    icon: Int,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = dimensionResource(id = R.dimen.dp_10)),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.padding(end = dimensionResource(id = R.dimen.dp_10))
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = CommonTypography.current.textSemiBold
            )

            Text(
                text = description,
                style = CommonTypography.current.textSubtext,
                fontSize = dimensionResource(R.dimen.sp_12).value.sp
            )
        }
    }
}


@Composable
fun RowWithIcons(
    icon: Int,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = { onClick() },
                indication = null,
                interactionSource = remember { MutableInteractionSource() }),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Row(
            modifier = Modifier.weight(0.7f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.padding(end = dimensionResource(id = R.dimen.dp_10))
            )

            Text(
                text = title,
                style = CommonTypography.current.textSemiBold,
                modifier = Modifier.basicMarquee()
            )

        }

        Box(
            modifier = Modifier
                .weight(0.3f),
            contentAlignment = Alignment.CenterEnd
        ) {
            Image(
                painter = painterResource(id = R.drawable.icon__next),
                contentDescription = null,
                modifier = Modifier
                    .padding(end = dimensionResource(id = R.dimen.dp_10))
            )
        }
    }

    HorizontalDivider(thickness = dimensionResource(id = R.dimen.dp_1), modifier = Modifier
        .padding(vertical = dimensionResource(id = R.dimen.dp_10)) , color = OutlineDefault)
}

@Composable
fun SimpleRowItem(
    leftText: String,
    modifier: Modifier,
    textStyle: TextStyle = CommonTypography.current.textSemiBold,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = {
                onClick()
            }, indication = null, interactionSource = remember { MutableInteractionSource() }),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {


        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Image(
                painter = painterResource(id = R.drawable.icon_location),
                contentDescription = null
            )

            Text(
                text = leftText,
                style = textStyle,
                modifier = modifier.padding(start = dimensionResource(R.dimen.dp_8))
            )
        }

            Icon(
                painter = painterResource(id = R.drawable.icon__next),
                contentDescription = null
            )

    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun <T> GenericDropdown(
    items: List<T>,
    labelMapper: (T) -> String,
    onItemSelected: (T) -> Unit = {},
    modifier:  Modifier = Modifier
) {
    val expanded = remember { mutableStateOf(false) }
    val selectedText = remember { mutableStateOf("") }
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused = interactionSource.collectIsFocusedAsState()
    val borderWidth = if (isFocused.value) dimensionResource(id = R.dimen.dp_2) else dimensionResource(id = R.dimen.dp_1)
    val borderColor = when {
        isFocused.value -> BlackColor
        else -> LightGray
    }
    val labelStyle =
        when{
            isFocused.value -> CommonTypography.current.smallTxt
            else -> CommonTypography.current.textSubtext
        }

    ExposedDropdownMenuBox(
        expanded = expanded.value,
        onExpandedChange = { expanded.value = !expanded.value }
    ) {
        TextField(
            value = selectedText.value,
            onValueChange = {},
            readOnly = true,
            label = {  Text(
                text = stringResource(id = R.string.select_option),
                style = labelStyle,
                textAlign = TextAlign.Center
            ) },
            trailingIcon = {
                Icon(
                    imageVector =
                        if (expanded.value) Icons.Default.KeyboardArrowUp
                        else Icons.Default.KeyboardArrowDown,
                    contentDescription = null
                )
            },
            modifier = modifier
                .fillMaxWidth()
                .clickable(
                    onClick = {
                    expanded.value = true
                },
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() })
                .border(
                    width = borderWidth,
                    color = borderColor,
                    shape = RoundedCornerShape(dimensionResource(id = R.dimen.dp_16))
                )
                .background(WhiteColor, RoundedCornerShape(dimensionResource(id = R.dimen.dp_16))),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                cursorColor = BlackColor,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            textStyle = CommonTypography.current.textSemiBold.copy(
                color = BlackColor
            ),
        )

        ExposedDropdownMenu(
            expanded = expanded.value,
            onDismissRequest = { expanded.value = false }
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(labelMapper(item)) },
                    onClick = {
                        selectedText.value = labelMapper(item)
                        expanded.value = false
                        onItemSelected(item)
                    }
                )
            }
        }
    }
}

data class ErrorAppSnackBarData(val msg: String) : AppSnackBarData(R.drawable.property_1_error,msg,false,true,false)
data class SuccessAppSnackBarData(val msg: String) : AppSnackBarData(R.drawable.success,msg,false,false,true)

open class AppSnackBarData(
    val icon: Int? = null,
    val message: String,
    val showCancel: Boolean = false,
    val isError: Boolean = false,
    val isSuccess: Boolean = false,
)

class SnackbarController {
    var snackbarData: MutableState<AppSnackBarData?> = mutableStateOf(null)

    fun show(data: AppSnackBarData) {
        snackbarData.value = data
        if(data.isError) SoundUtils.errorBeep()
        if(data.isSuccess) SoundUtils.successBeep()
    }

    fun dismiss() {
        snackbarData.value = null
    }
}


@Composable
fun AppSnackBar(
    snackbarController: SnackbarController
) {
    val data = snackbarController.snackbarData.value
    if (data != null) {

        LaunchedEffect(data) {
            delay(5000)
            snackbarController.dismiss()
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(id = R.dimen.dp_16)),
            contentAlignment = Alignment.BottomCenter
        ) {

            Card(
                shape = RoundedCornerShape(dimensionResource(id = R.dimen.dp_16)),
                elevation = CardDefaults.cardElevation(dimensionResource(id = R.dimen.dp_24)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(
                            horizontal = dimensionResource(id = R.dimen.dp_10),
                            vertical = dimensionResource(id = R.dimen.dp_10)
                        )
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        data.icon?.let {
                            Image(
                                painter = painterResource(id = it),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(dimensionResource(id = R.dimen.dp_24))
                            )
                            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.dp_8)))
                        }

                        // Message
                        Text(
                            text = data.message,
                            style = CommonTypography.current.noteText,
                            color = TextGrey
                        )
                    }

                    if (data.showCancel) {

                        IconButton(onClick = { snackbarController.dismiss() }) {
                            Icon(
                                painter = painterResource(R.drawable.clear),
                                contentDescription = "Clear text"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ItekFooter(){
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {

        Text(
            text = stringResource(id = R.string.copyright_txt),
            style = CommonTypography.current.smallTxt,
            modifier = Modifier
                .padding(bottom = dimensionResource(id = R.dimen.dp_12))
            ,
            textAlign = TextAlign.Center
        )
    }
}


fun getInitials(name: String): String {
    val parts = name.trim().split(" ")
    return when {
        parts.size >= 2 -> {
            "${parts.first().first()}${parts.last().first()}".uppercase()
        }
        parts.size == 1 -> {
            parts.first().first().uppercase().toString()
        }
        else -> ""
    }
}


@Composable
fun CircularLoader(
    size: Dp = dimensionResource(id = R.dimen.dp_40),
    strokeWidth: Dp = dimensionResource(id = R.dimen.dp_4),
    color: Color = Color(0xFF4CAF50)
) {
    val transition = rememberInfiniteTransition()

    val sweep = transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing)
        )
    )

    Canvas(modifier = Modifier.size(size)) {
        drawArc(
            color = color,
            startAngle = sweep.value,
            sweepAngle = 270f,
            useCenter = false,
            style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
        )
    }
}

/*@Preview
@Composable
fun Image() {

    val infiniteTransition = rememberInfiniteTransition(label = "3d")

    val shadowOffsetX = infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shadowX"
    )

    val shadowOffsetY = infiniteTransition.animateFloat(
        initialValue = 6f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shadowY"
    )

    Card(
        modifier = Modifier
            .padding(25.dp)
            .size(dimensionResource(id = R.dimen.dp_104)),
        shape = RoundedCornerShape(dimensionResource(R.dimen.dp_16)),
        colors = CardDefaults.cardColors(containerColor = TabColor)
    ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {

                // 🔹 IMAGE-SHAPE SHADOW (STATIC, FIXED)
                Image(
                    painter = painterResource(id = R.drawable.ic_inv),
                    contentDescription = null,
                    modifier = Modifier
                        .matchParentSize()
                        .offset(y = 6.dp)            // fixed light direction
                        .blur(12.dp)                // softness
                        .graphicsLayer {
                            scaleX = 0.60f
                            scaleY = 0.60f
                            alpha = 0.8f
                        },
                    colorFilter = ColorFilter.tint(
                        BlackColor
                    ),
                    contentScale = ContentScale.Fit
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                // 🔹 MAIN IMAGE (ANIMATED OR STATIC)
                Image(
                    painter = painterResource(id = R.drawable.ic_inv),
                    contentDescription = null,
                    modifier = Modifier
                        .size(50.dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.dp_8)))

                Text(
                    text = "title",
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = CommonTypography.current.noteText,
                    modifier = Modifier.fillMaxWidth()
                )
            }

        }

    }
}*/

@Composable
fun GenericIconCard(
    menu: MenuEntity,
    modifier: Modifier = Modifier,
    showRedDot: Boolean = false,
    //cardWidth: Dp = (LocalConfiguration.current.screenWidthDp.dp - 64.dp) / 3,//dimensionResource(id = R.dimen.dp_104),
    shadowColor: Color = Color.LightGray,
    textStyle: TextStyle = CommonTypography.current.noteText.copy(BlackColor),
    iconContentScale: ContentScale = ContentScale.Fit,
    iconTint: Color = Color.Unspecified,
) {
    val iconRes = getMenuIconByCode(menu.code)
    val title = menu.label
    val isEnabled = menu.isEnabled

    val cardWidth: Dp = (LocalConfiguration.current.screenWidthDp.dp - 64.dp) / 3//dimensionResource(id = R.dimen.dp_104),

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

    Card(
        modifier = modifier
            .background(TabColor, shape = RoundedCornerShape(dimensionResource(R.dimen.dp_16)))
            .width(cardWidth)
            .height(dimensionResource(R.dimen.dp_104))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(TabColor)
                .padding(
                    horizontal = dimensionResource(id = R.dimen.dp_16),
                    vertical = dimensionResource(id = R.dimen.dp_12)
                ),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // 🔹 ICON + RED DOT CONTAINER
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.TopEnd
            ) {

                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = title,
                    contentScale = iconContentScale,
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
                        .fillMaxSize()
                        .then(
                            if (!isEnabled) {
                                Modifier.blur(2.dp)
                            } else {
                                Modifier
                            }
                        )
                        .alpha(if (isEnabled) 1f else 0.35f)
                        .graphicsLayer {
                            this.rotationX = rotationX.value
                            this.rotationY = rotationY.value
                            cameraDistance = 12 * density
                        }
                )

                if (showRedDot) {
                    Image(
                        painter = painterResource(id = R.drawable.property_red_dot),
                        contentDescription = null,
                        modifier = Modifier
                            .size(12.dp)
                            .offset(x = 4.dp, y = (-4).dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.dp_8)))

            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Visible,
                softWrap = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .basicMarquee(),
                style = textStyle.copy(color = if (isEnabled) { BlackColor } else { BlackColor.copy(alpha = 0.4f) }),
                textAlign = TextAlign.Center
            )
        }
    }
}



@Composable
fun InfoActionRow(
    title: String,
    isSubTitle: Boolean = true,
    subtitle: String,
    icon: Painter,
    actionText: String,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = CommonTypography.current.textSemiBold,
    isIcon : Boolean = true
) {

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = dimensionResource(R.dimen.dp_10)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        // LEFT SIDE
        Column(horizontalAlignment = Alignment.Start) {
            Text(
                text = title,
                style = textStyle
            )
            if (isSubTitle){
                Text(
                    text = subtitle,
                    style = CommonTypography.current.noteText.copy(TextGrey)
                )
            }
        }

        Row(
            modifier = Modifier
                .background(
                    TabColor, RoundedCornerShape(dimensionResource(id = R.dimen.dp_24))
                )
                .clickable(
                    onClick = {
                    onActionClick()
                },
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() })
                .padding(
                    horizontal = dimensionResource(id = R.dimen.dp_10),
                    vertical = dimensionResource(id = R.dimen.dp_10)
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (isIcon){
                Icon(
                    painter = icon,
                    contentDescription = actionText,
                    tint = BlackColor
                )

                Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.dp_5)))
            }

            Text(
                text = actionText,
                style = CommonTypography.current.noteText,
                color = BlackColor
            )
        }
    }
}



@Composable
fun EanListRow(
    title: String,
    isShowingMaskedTitle: Boolean=false,
    actionText: String,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier,
    circleColor: Color = Yellow,
    backgroundColor: Color = LightYellow
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = title,
            style = if(isShowingMaskedTitle) CommonTypography.current.smallTxtBold else CommonTypography.current.textSemiBold,
            modifier = Modifier.weight(1f).basicMarquee(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.width(dimensionResource(R.dimen.dp_8)))

        Row(
            modifier = Modifier
                .background(
                    backgroundColor.copy(alpha = 0.2f), RoundedCornerShape(50)
                )
                .clickable { onActionClick() }
                .padding(
                    horizontal = 10.dp, vertical = 5.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(circleColor)
            )

            Spacer(modifier = Modifier.width(5.dp))

            Text(
                text = actionText,
                style = CommonTypography.current.noteText,
                color = if (circleColor == Green) Green else RedColor
            )
        }
    }
}


@Composable
fun TopBarContent(label: String, onBackClickL: () -> Unit, onSettingClick: () -> Unit, isSetting: Boolean = true) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(WhiteColor)
            .padding(
                horizontal = dimensionResource(R.dimen.dp_16),
                vertical = dimensionResource(R.dimen.dp_12)
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {

        IconButton(
            onClick = onBackClickL,
            modifier = Modifier.size(24.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.property_arrow_back),
                contentDescription = "Back"
            )
        }

        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = CommonTypography.current.textSemiBold,
            color = TextSubtext,
            textAlign = TextAlign.Center
        )

        if(isSetting){
            IconButton(
                onClick = onSettingClick,
                modifier = Modifier.size(24.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.property_setting),
                    contentDescription = "Settings"
                )
            }
        }
    }
}


@Composable
fun CounterText(
    current: Int,
    total: String,
    modifier: Modifier = Modifier,
    separator: String = "/",
    isLimitShow: Boolean = true,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center
    ) {

        Text(
            text = current.toString(),
            style = CommonTypography.current.countText,
            modifier = Modifier.alignByBaseline()
        )

        if (isLimitShow) {

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = separator,
                style = CommonTypography.current.bigFont,
                fontWeight = FontWeight.Medium,
                color = LightGray,
                modifier = Modifier.alignByBaseline()
            )

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = total,
                style = CommonTypography.current.bigFont,
                fontWeight = FontWeight.Medium,
                color = LightGray,
                modifier = Modifier.alignByBaseline()
            )
        }
    }
}

data class FilterItem(
    val label: String,
    val list: List<LocationModel>,
    var selected: LocationModel? = null
)

@Composable
fun <T : Any>MultiFilterPicker(
    filters: Map<String, List<T>>,
    selectedMap: SnapshotStateMap<String, List<T>>,
    onDismiss: () -> Unit,
    onApply: (Map<String, List<T>>) -> Unit,
    menuCode: String,
) {

    val context = LocalContext.current
    val searchQuery = remember { mutableStateOf("") }
    val activeKey = remember { mutableStateOf<String?>(null) }
    val snackbarController = remember { SnackbarController() }
    val selectedFilters = remember { mutableStateMapOf<String, List<T>>().apply { putAll(selectedMap) } }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(0.35f))
            .padding(top = 44.dp)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                activeKey.value = null
                searchQuery.value = ""
                onDismiss()
            },
        contentAlignment = Alignment.BottomCenter
    ) {
        LogUtils.showLog("Filters",filters.size.toString())
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            val userId=DataStoreManager.readFromPreferences(ParameterConstants.USER_ID,"")
            val selFilterKeyList = DataStoreManager.getListStr(userId+"_"+menuCode+"_filters")

            filters.filter { (key, values) -> selFilterKeyList.contains(key) }.forEach { (key, values) ->

                val filteredList = if (searchQuery.value.isEmpty()) {
                    values
                } else {
                    values.filter {
                        it.toString().contains(searchQuery.value, true)
                    }
                }


                /* -------------------- EXPANDED PICKER -------------------- */
                if (activeKey.value == key) {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .heightIn(max = 350.dp)
                            .background(WhiteColor, RoundedCornerShape(32.dp))
                    ) {

                        Column(Modifier.padding(16.dp)) {

                            Text(
                                text = key,
                                style = CommonTypography.current.mediumText
                            )

                            Spacer(Modifier.height(8.dp))

                            SearchBar(
                                searchQuery = searchQuery.value,
                                onQueryChange = { searchQuery.value = it },
                                placeholder = "Search $key"
                            )

                            Spacer(Modifier.height(12.dp))

                            LazyColumn {
                                items(filteredList) { item ->
                                    val selectedList = (selectedFilters[key] ?: emptyList<T>()).toMutableList()
                                    LogUtils.showLog("selectedList0",key+"->"+selectedList.toString())
                                    val isSelected = selectedList.contains(item)
                                    LogUtils.showLog("is_Selected",item.toString()+"->"+isSelected)
                                    FilterRow(
                                        title = item.toString(),
                                        isSelected = isSelected,
                                        onClick = {
                                            //val newList = selectedList.toMutableList()

                                            if (isSelected) {
                                                selectedList.remove(item)
                                            } else {
                                                selectedList.add(item)
                                            }
                                            LogUtils.showLog("selectedList1",key+"->"+selectedList.toString())
                                            //IMPORTANT: reassign new list
                                            if(selectedList.isNullOrEmpty()) selectedFilters.remove(key)
                                            else selectedFilters[key] = selectedList
                                            LogUtils.showLog("selectedList2",key+"->"+selectedFilters[key])
                                            //activeKey.value = null
                                            //searchQuery.value = ""
                                        }
                                    )


                                    /*LocationRow(
                                        location = item,
                                        onClick = {
                                            selectedFilters[key] = item
                                            activeKey.value = null
                                            searchQuery.value = ""
                                        }
                                    )*/
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                }

                /* -------------------- COLLAPSED ROW -------------------- */
                if (activeKey.value != key) {

                    PickerRow(
                        title = key,
                        value = selectedFilters[key]?.joinToString( ", " ),
                        onClick = {
                            searchQuery.value = ""
                            activeKey.value = key
                        }
                    )

                    Spacer(Modifier.height(14.dp))
                }
            }

            /* -------------------- ACTION BUTTONS -------------------- */
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                CommonButton(
                    text = stringResource(R.string.cancel),
                    onClick = {
                        selectedFilters.keys.forEach { selectedFilters[it] = emptyList<T>() }
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f),
                    gradientBrush = SolidColor(WhiteColor),
                    contentColor = RedColor
                )

                CommonButton(
                    text = stringResource(R.string.apply_filter),
                    onClick = {

                        /*// Example validation
                        val values = selectedFilters.values.filterNotNull()
                        if (values.size >= 2 && values[0] == values[1]) {
                            snackbarController.show(
                                ErrorAppSnackBarData(
                                    context.getString(R.string.source_and_destination_cannot_be_same)
                                )
                            )
                            return@CommonButton
                        }*/

                        onApply(selectedFilters)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f),
                    //enabled = selectedFilters.values.none { it == null }
                )
            }

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.BottomCenter
            ) {
                AppSnackBar(snackbarController)
            }
        }
    }
}


@Composable
fun GernericBasicTextField(
    sourceZone: String,
    destZone: String,
    menuCode: String,
    destAction: ()-> Unit,
    clearAction: () -> Unit ,
    value: String,
    isOnDataUploaded: Boolean = false,
    labelRowAction: ()-> Unit,
    isDestZone: Boolean = false,
    isSet: Boolean = false){
    BasicTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        singleLine = true,
        textStyle = CommonTypography.current.noteText.copy(
            color = BlackColor,
            textAlign = TextAlign.Start
        ),
        modifier = Modifier
            .height(dimensionResource(R.dimen.dp_44))
            .shadow(12.dp, RoundedCornerShape(dimensionResource(R.dimen.dp_24)))
            .wrapContentWidth()   // 🔥 Important
            .border(
                1.dp,
                if (isOnDataUploaded) GreenBorder else Yellow,
                RoundedCornerShape(24.dp)
            )
            .background(WhiteColor, RoundedCornerShape(24.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        decorationBox = {

            Row(
                modifier = Modifier.clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    labelRowAction()
                },
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Source location icon
                Icon(
                    painter = painterResource(id = R.drawable.icon_location),
                    contentDescription = null,
                    modifier = Modifier.size(dimensionResource(R.dimen.dp_18)),
                    tint = if (isOnDataUploaded) Green else Yellow
                )

                Spacer(Modifier.width(4.dp))

                // Source text
                Text(
                    text = sourceZone,
                    style = CommonTypography.current.noteText,
                    color = BlackColor
                )

                Spacer(Modifier.width(8.dp))

                if(isDestZone){
                    // Arrow
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(Modifier.width(8.dp))

                    // Destination location icon
                    Icon(
                        painter = painterResource(id = R.drawable.icon_location),
                        contentDescription = null,
                        modifier = Modifier.size(dimensionResource(R.dimen.dp_18)),
                        tint = if (isOnDataUploaded) Green else Yellow
                    )

                    Spacer(Modifier.width(4.dp))
                    // Destination text
                    if (destZone.isNullOrEmpty()){
                        Text(
                            text = stringResource(R.string.select_zone),
                            style = CommonTypography.current.noteText,
                            color = BlackColor,
                            modifier = Modifier.clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                destAction()
                            }
                        )
                    }else{

                        Text(
                            text = destZone,
                            style = CommonTypography.current.noteText,
                            color = BlackColor
                        )
                    }
                }
                Spacer(Modifier.width(8.dp))
                if(isSet) {
                    Icon(
                    painter = painterResource(id = R.drawable.property_check_selected),
                    contentDescription = null,
                    tint = if (isOnDataUploaded) Green else LightGray
                    )
                }
                else if (!isDestZone || destZone.isNotEmpty()){
                    Image(
                        painter =  painterResource(id = R.drawable.clear),
                        contentDescription = null,
                        modifier = Modifier
                            .size(dimensionResource(R.dimen.dp_16))
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                clearAction()
                            }
                    )
                }
            }
        }
    )
}