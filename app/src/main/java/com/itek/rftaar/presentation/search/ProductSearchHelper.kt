package com.itek.rftaar.presentation.search

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.itek.rftaar.R
import com.itek.rftaar.core.common.utils.LogUtils
import com.itek.rftaar.data.model.LabelName
import com.itek.rftaar.presentation.commonComp.BottomSheetHeader
import com.itek.rftaar.presentation.commonComp.CommonButton
import com.itek.rftaar.presentation.decoding.getTypeIcon
import com.itek.rftaar.ui.theme.BlackColor
import com.itek.rftaar.ui.theme.ButtonGray
import com.itek.rftaar.ui.theme.CommonTypography
import com.itek.rftaar.ui.theme.Green
import com.itek.rftaar.ui.theme.TabColor
import com.itek.rftaar.ui.theme.TextGrey
import com.itek.rftaar.ui.theme.WhiteColor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DecodingTypeList(
    decodedList: List<LabelName>,
    scope: CoroutineScope,
    showSheet: MutableState<Boolean>,
    sheetState: SheetState,
    selectedDecodeType: MutableState<String>,
    onConfirmClick:  (() -> Unit)? = null,
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

        BottomSheetHeader(title = "Select Decode Type") {
            scope.launch {
                sheetState.hide()
                showSheet.value = false
            }
        }
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.dp_8))
        )
        {

            decodedList.take(8).forEach { data ->

                val isSelected = selectedDecodeType.value == data.name

                key(data.name) {
                    Card(
                        modifier = Modifier
                            .width(dimensionResource(R.dimen.dp_76))
                            .height(dimensionResource(R.dimen.dp_68))
                            .border(
                                width = dimensionResource(if (isSelected) R.dimen.dp_2 else R.dimen.dp_1),
                                color = if (isSelected) Green else TabColor,
                                shape = RoundedCornerShape(dimensionResource(R.dimen.dp_16))
                            )
                            .clickable(
                                onClick = {
                                    val clickedItem = data.name

                                    if (selectedDecodeType.value == clickedItem) {
                                        return@clickable
                                    }

                                    selectedDecodeType.value = clickedItem

                                    LogUtils.showLog("selectedDecodeType", "DecodingTypeList: ${selectedDecodeType.value}")
                                },
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(dimensionResource(R.dimen.dp_16))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .padding(vertical = dimensionResource(R.dimen.dp_12)),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {

                            Image(
                                painter = painterResource(
                                    if (isSelected) R.drawable.success else getTypeIcon(
                                        data.name
                                    )
                                ),
                                contentDescription = null,
                                modifier = Modifier.size(dimensionResource(R.dimen.dp_24))
                            )

                            Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_4)))

                            Text(
                                text = data.label,
                                style = CommonTypography.current.noteText.copy(
                                    color = if (isSelected) Green else TextGrey
                                ),
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .basicMarquee()
                                    .padding(horizontal = 5.dp),
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.size(16.dp))
        CommonButton(
            text = stringResource(R.string.confirm),
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                scope.launch {
                    if(selectedDecodeType.value.isNullOrEmpty()) return@launch
                    onConfirmClick?.invoke()
                }
            },
            gradientBrush = Brush.horizontalGradient(listOf(BlackColor, ButtonGray)),
            contentColor =  WhiteColor,
        )
    }
}
