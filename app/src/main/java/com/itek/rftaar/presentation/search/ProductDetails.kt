package com.itek.rftaar.presentation.search

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.itek.rftaar.R
import com.itek.rftaar.presentation.commonComp.CircularIcon
import com.itek.rftaar.presentation.commonComp.CommonButton
import com.itek.rftaar.ui.theme.BlackColor
import com.itek.rftaar.ui.theme.CommonTypography
import com.itek.rftaar.ui.theme.ErrorBgColor
import com.itek.rftaar.ui.theme.OutlineDefault
import com.itek.rftaar.ui.theme.RedColor
import com.itek.rftaar.ui.theme.ShadowGray
import com.itek.rftaar.ui.theme.WhiteColor

@Composable
fun ProductDetails(){
    val navController = rememberNavController()
    BackHandler(enabled = true) {

    }

    Box() {

        Scaffold(
            bottomBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(WhiteColor),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    ProductDetailsBottomBar()
                }
            },
            modifier = Modifier.background(WhiteColor)
        ) { innerPadding ->

            Box(
                modifier = Modifier
                    .background(WhiteColor)
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                ProductDetailsContent(navController)
            }
        }
    }
}

@Composable
fun ProductDetailsContent(navController: NavHostController) {
    val details = listOf(
        KeyValueUiModel("EAN", "1231232434343"),
        KeyValueUiModel("Quantity", "1"),
        KeyValueUiModel("Location", "BOH"),
        KeyValueUiModel("Brand", "Zara")
    )


    Box() {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(dimensionResource(id = R.dimen.dp_16))
                .clickable(
                    onClick = {},
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top
        )
        {
            Column(modifier = Modifier.fillMaxWidth()) {
                CircularIcon(
                    iconRes = R.drawable.property_arrow_back,
                    modifier = Modifier.clickable(
                        onClick = {
                            if(navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED)
                            navController.popBackStack()
                        },
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    )

                )
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(dimensionResource(R.dimen.dp_56))
                    .shadow(
                        elevation = dimensionResource(R.dimen.dp_24),
                        shape = RoundedCornerShape(dimensionResource(R.dimen.dp_16)),
                        ambientColor = ShadowGray,
                        spotColor = ShadowGray
                    ),
                shape = RoundedCornerShape(dimensionResource(R.dimen.dp_16)),
                colors = CardDefaults.cardColors(containerColor = WhiteColor)
            ){
                Column() {
                    Row {

//                        ProductImage(imageRes = R.drawable.image)

                        Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_8)))

                        Column(modifier = Modifier) {
                            Text(
                                text = "Classic Balck Zara Tee, Soft Cotton",
                                style = CommonTypography.current.noteText,
                                color = BlackColor
                            )

                            InventoryBadge(text = "Missing: 0/5", backGroundColor = ErrorBgColor, textColor = RedColor , iconColor = RedColor)
                        }
                    }

                    KeyValueSection(items = details)
                }
            }
        }
    }
}

@Composable
fun ProductDetailsBottomBar() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(WhiteColor)
    ) {
        HorizontalDivider(color = OutlineDefault)
        Row(modifier = Modifier.fillMaxWidth().padding(dimensionResource(R.dimen.dp_16))) {

            CommonButton(
                text = stringResource(id = R.string.start_scanning_inventory),
                onClick = {

                },
                modifier = Modifier,
                enabled = false,
                contentColor = BlackColor
            )
        }
    }
}

data class KeyValueUiModel(
    val key: String,
    val value: String
)

@Composable
fun KeyValueRow(
    item: KeyValueUiModel,
    modifier: Modifier = Modifier,
    keyTextStyle: TextStyle = CommonTypography.current.noteText,
    valueTextStyle: TextStyle = CommonTypography.current.noteText,
    keyColor: Color = BlackColor,
    valueColor: Color = LocalContentColor.current
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = item.key,
            style = keyTextStyle,
            color = keyColor
        )

        Text(
            text = item.value,
            style = valueTextStyle,
            color = valueColor
        )
    }
}

@Composable
fun GenericDivider(
    thickness: Dp,
    color: Color,
    verticalPadding: Dp
) {
    HorizontalDivider(
        thickness = thickness,
        color = color,
        modifier = Modifier.padding(vertical = verticalPadding)
    )
}

@Composable
fun KeyValueSection(
    items: List<KeyValueUiModel>,
    modifier: Modifier = Modifier,
    dividerThickness: Dp = dimensionResource(R.dimen.dp_10),
    dividerColor: Color = OutlineDefault,
    dividerPadding: Dp = dimensionResource(R.dimen.sp_8)
) {
    Column(modifier = modifier) {
        items.forEachIndexed { index, item ->
            KeyValueRow(item = item)

            if (index < items.lastIndex) {
                GenericDivider(
                    thickness = dividerThickness,
                    color = dividerColor,
                    verticalPadding = dividerPadding
                )
            }
        }
    }
}


