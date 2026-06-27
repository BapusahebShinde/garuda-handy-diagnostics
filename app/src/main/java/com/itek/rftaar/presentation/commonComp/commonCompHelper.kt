package com.itek.rftaar.presentation.commonComp

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.itek.rftaar.R
import com.itek.rftaar.ui.theme.BlackColor
import com.itek.rftaar.ui.theme.CommonTypography
import com.itek.rftaar.ui.theme.WhiteColor

//Unused
/*@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetPower(
    showSheet: MutableState<Boolean>,
    sheetState: SheetState,
    scope: CoroutineScope,
    readerViewModel: ReaderViewModel
) {

    val power by readerViewModel.power.collectAsState()
    val value = remember { mutableStateOf(power) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(WhiteColor)
            .padding(dimensionResource(R.dimen.dp_16)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = dimensionResource(R.dimen.dp_16))
        ) {

            Text(
                text = stringResource(R.string.set_power),
                style = CommonTypography.current.textSemiBold,
                modifier = Modifier.align(Alignment.Center)
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
                        }
                    }
            )
        }

        Text(
            text = stringResource(R.string.set_device_power),
            style = CommonTypography.current.headingH1
        )

        Text(
            text = stringResource(R.string.adjust_the_reader),
            style = CommonTypography.current.textSubtext,
            modifier = Modifier.padding(
                top = dimensionResource(R.dimen.dp_8),
                bottom = dimensionResource(R.dimen.dp_24)
            )
        )

        ManualLimitSlider(
            value = value
            //onValueChange = { readerViewModel.setPower(it) }
        )

        Spacer(modifier = Modifier.weight(1f))

        CommonButton(
            text = stringResource(R.string.continue_),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            onClick = {
                scope.launch {
                    readerViewModel.setPower(value.value)
                    sheetState.hide()
                    showLog("Power", power.toString())
                    showSheet.value = false
                }
            }
        )
    }
}*/


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageComponent(
    icon: Int,
    backGroundColor: Brush = SolidColor(WhiteColor),
    topHeading: String ="",
    heading: String,
    description: String,
    buttonText: String,
    onClick: () -> Unit,
    onClose: () -> Unit,
    tintColor: androidx.compose.ui.graphics.Color = BlackColor
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                vertical = dimensionResource(R.dimen.dp_24),
                horizontal = dimensionResource(R.dimen.dp_16)
            ), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        /*Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = dimensionResource(R.dimen.dp_16))
        ) {
            Image(
                painter = painterResource(R.drawable.clear),
                contentDescription = "Close",
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .clickable {
                        onClose()
                    }
            )
        }*/
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = dimensionResource(R.dimen.dp_16))
        ) {
            if(topHeading.isNotEmpty()) {
                Text(
                    text = topHeading,
                    style = CommonTypography.current.textSemiBold,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Image(
                painter = painterResource(R.drawable.clear),
                contentDescription = "Close",
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .clickable { onClose() }
            )
        }

       /* if(topHeading.isNotEmpty()) {
            Text(
                text = topHeading,
                style = CommonTypography.current.headingH1,
                modifier = Modifier.padding(
                    top = dimensionResource(R.dimen.dp_16),
                    bottom = dimensionResource(R.dimen.dp_8)
                )
            )
        }*/

        CircularIcon(
            iconRes = icon,
            iconSize = dimensionResource(R.dimen.dp_34),
            circleSize = dimensionResource(R.dimen.dp_64),
            backgroundColor = backGroundColor,
            tintColor = tintColor
        )

        if(heading.isNotEmpty()) {
            Text(
                text = heading,
                style = CommonTypography.current.headingH1,
               /* modifier = Modifier.padding(
                    top = dimensionResource(R.dimen.dp_16),
                    bottom = dimensionResource(R.dimen.dp_8)
                )*/
            )
            Spacer(modifier = Modifier.height(8.dp))
        }


        if(description.isNotEmpty()) {
            Text(
                text = description,
                style = CommonTypography.current.textSemiBold,
                modifier = Modifier.padding(
                    bottom = dimensionResource(R.dimen.dp_12)
                )
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            CommonButton(
                text = buttonText,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                onClick = {onClick()}
            )
        }

    }
}


@Composable
fun TwoButtonView(
    seconderText: String,
    primaryText: String,
    seconderAction: () -> Unit,
    primaryAction: () -> Unit,
    isSecondaryButton: Boolean = false,
    isSwipeSecondaryActionForPrimaryIfNoSecondaryButton: Boolean = false,
    modifier: Modifier = Modifier,
    primaryTextColor: Color = WhiteColor,
    primaryBackGroundColor: Brush = SolidColor(BlackColor),
    icon: Painter? = null,
    enabled: Boolean = true
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CommonButton(
            text = if(!isSecondaryButton && isSwipeSecondaryActionForPrimaryIfNoSecondaryButton) seconderText else primaryText,
            onClick = {
                if(!isSecondaryButton && isSwipeSecondaryActionForPrimaryIfNoSecondaryButton)  seconderAction()
                else primaryAction()
            },
            modifier = Modifier.weight(1f),
            contentColor = primaryTextColor,
            gradientBrush = primaryBackGroundColor,
            icon = icon,
            enabled = enabled
        )

        if (isSecondaryButton){
            CommonButton(
                text = seconderText,
                onClick = {
                    seconderAction()
                },
                modifier = Modifier.weight(1f),
                gradientBrush = SolidColor(WhiteColor),
                contentColor = BlackColor,
            )
        }
    }
}
