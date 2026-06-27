package com.itek.rftaar.presentation.inward

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.itek.rftaar.R
import com.itek.rftaar.data.model.IOFlowQty
import com.itek.rftaar.presentation.commonComp.CircularText
import com.itek.rftaar.presentation.inventory.SearchBar
import com.itek.rftaar.ui.theme.CommonTypography
import com.itek.rftaar.ui.theme.RedColor
import com.itek.rftaar.ui.theme.TabColor
import com.itek.rftaar.ui.theme.TextSubtext
import com.itek.rftaar.ui.theme.WhiteColor
import com.itek.rftaar.utils.CommonUtils.chkNull

@Composable
fun LabelPicker(
    listFlowQty: List<IOFlowQty>,
    onDismiss: () -> Unit,
    onLocationSelected: (IOFlowQty) -> Unit
) {
    val searchQuery = remember { mutableStateOf("") }
    val filteredLocations = if (searchQuery.value.isEmpty()) listFlowQty
    else listFlowQty.filter { chkNull(it.flowName,it.label).contains(searchQuery.value, ignoreCase = true) }


    // 🔹 FULL SCREEN OVERLAY
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.35f))
            .padding(top = 44.dp)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onDismiss() },
        contentAlignment = Alignment.BottomCenter   // ✅ CENTER EVERYTHING
    ) {
        Column(
            horizontalAlignment = Alignment.End
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .heightIn(max = 350.dp)
                    .background(WhiteColor, RoundedCornerShape(24.dp))
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {

                    Text(text = stringResource(R.string.select_type),
                        style = CommonTypography.current.headingH1,
                        modifier = Modifier.padding(bottom = dimensionResource(R.dimen.dp_16)))

                    SearchBar(
                        searchQuery = searchQuery.value,
                        onQueryChange = { searchQuery.value = it },
                        placeholder = stringResource(id = R.string.search_type)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn {
                        items(filteredLocations) { ioFlowQty ->
                            LabelRow(
                                location = chkNull(ioFlowQty.flowName,ioFlowQty.label),
                                onClick = { onLocationSelected(ioFlowQty) },
                                count = ioFlowQty.qty.toString()
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_12)))

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .heightIn(dimensionResource(R.dimen.dp_48))
                    .background(WhiteColor, RoundedCornerShape(24.dp))
            ) {
                Button(
                    onClick = {
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = WhiteColor,
                            shape = RoundedCornerShape(dimensionResource(R.dimen.dp_16))
                        )
                        .padding(horizontal = dimensionResource(R.dimen.dp_16)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WhiteColor,
                        contentColor = RedColor
                    )
                ) {
                    Text(
                        text = stringResource(R.string.cancel),
                        style = CommonTypography.current.buttonSemiBold,
                        color = RedColor
                    )
                }
            }

            Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_14)))

        }
    }
}

@Composable
fun LabelRow(
    location: String,
    onClick: () -> Unit,
    count: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp)
    ) {

        Row(verticalAlignment = Alignment.CenterVertically) {

            Image(
                painter = painterResource(R.drawable.icon_location),
                contentDescription = null
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = location,
                style = CommonTypography.current.textSemiBold
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                CircularText(
                    text = count,
                    circleSize = 40.dp,
                    shadowElevation = 0.dp,
                    backgroundColor = SolidColor(TabColor),
                    textStyle = CommonTypography.current.buttonSemiBold.copy(color = TextSubtext)
                )
            }
        }
    }
}