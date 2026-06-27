package com.itek.rftaar.presentation.commonComp

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.itek.rftaar.R
import com.itek.rftaar.ui.theme.BlackColor
import com.itek.rftaar.ui.theme.CommonTypography
import com.itek.rftaar.ui.theme.ShadedYellow
import com.itek.rftaar.ui.theme.TabColor

@Composable
fun IconText(icon: Painter,
             actionText: String,
             onActionClick: () -> Unit,
             borderColor : Color = TabColor,
             tintColor : Color = TabColor,
             textColor: Color = BlackColor,
             backGroundColor: Color = TabColor,
             textStyle: TextStyle = CommonTypography.current.noteText,
             iconSize : Dp = dimensionResource(R.dimen.dp_16),
             verticalPadding : Dp = dimensionResource(id = R.dimen.dp_10),
             elevation: Dp = 9.dp,
             modifier: Modifier = Modifier
){
    Card(
        onClick = { onActionClick() },   // ✅ use Card click
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.dp_24)),
        elevation = CardDefaults.cardElevation(
            defaultElevation = elevation
        ),
        colors = CardDefaults.cardColors(
            containerColor = backGroundColor   // ✅ background here
        ),
        border = BorderStroke(
            width = dimensionResource(id = R.dimen.dp_1),
            color = borderColor
        )
    ) {

        Row(
            modifier = modifier
                .padding(
                    horizontal = dimensionResource(id = R.dimen.dp_10),
                    vertical = verticalPadding
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                painter = icon,
                contentDescription = actionText,
                tint = tintColor,
                modifier = Modifier.size(iconSize)
            )

            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.dp_5)))

            Text(
                text = actionText,
                style = textStyle,
                color = textColor
            )
        }
    }

}


@Composable
fun RowText(
    actionText: String,
    onActionClick: () -> Unit){
    Row(
        modifier = Modifier
            .widthIn(min = 100.dp)
            .background(
                Brush.horizontalGradient(
                    colors = listOf(Color(0xFFF8B800), ShadedYellow)
                ),
                shape = RoundedCornerShape(dimensionResource(id = R.dimen.dp_24))
            )
            .border(width = dimensionResource(R.dimen.dp_1),
                color = ShadedYellow,
                shape = RoundedCornerShape(dimensionResource(id = R.dimen.dp_24))
            )
            .clickable(
                onClick = {
                    onActionClick()
                },
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
            .padding(
                horizontal = dimensionResource(id = R.dimen.dp_10),
                vertical = dimensionResource(id = R.dimen.dp_10)
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {

        Text(
            text = actionText,
            style = CommonTypography.current.noteText,
            color = BlackColor,
            textAlign = TextAlign.Center
        )
    }
}