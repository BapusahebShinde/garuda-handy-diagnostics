package com.itek.rftaar.presentation.commonComp

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.itek.rftaar.R
import com.itek.rftaar.domain.model.LocationModel
import com.itek.rftaar.presentation.inventory.LocationRow
import com.itek.rftaar.presentation.inventory.SearchBar
import com.itek.rftaar.ui.theme.RedColor
import com.itek.rftaar.ui.theme.WhiteColor

interface PickerItem {
    val id: String
    val displayName: String
}

enum class PickerType { NONE, ZONE, BAND }

data class PickerConfig<T : Any>(
    val label: String,
    val placeholder: String,
    val addLabel: String,
    val items: List<T>,
    val selectedItem: T?,
    val onItemSelected: (T) -> Unit,
    val itemToString: (T) -> String,
    val type: PickerType
)

@Composable
fun GenericOptionPicker(
    configs: List<PickerConfig<*>>,
    onDismiss: () -> Unit,
    onApply: () -> Unit,
    applyEnabled: Boolean
) {
    val searchQuery = remember { mutableStateOf("") }
    val activePicker = remember { mutableStateOf<PickerType?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.35f))
            .padding(top = 44.dp)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                activePicker.value = null
            },
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable(enabled = false) {} // Prevents clicking through to background
        ) {

            // Loop through all configurations
            configs.forEach { config ->
                PickerRowLogic(config, activePicker, searchQuery)
                Spacer(modifier = Modifier.height(12.dp))
            }

            ActionButtons(
                onCancel = onDismiss,
                onApply = onApply,
                applyEnabled = applyEnabled
            )

            Spacer(modifier = Modifier.height(14.dp))
        }
    }
}

/**
 * Helper to capture the generic type <T> and prevent the Star Projection error.
 */
@Composable
fun <T : Any> PickerRowLogic(
    config: PickerConfig<T>,
    activePicker: MutableState<PickerType?>,
    searchQuery: MutableState<String>
) {
    if (activePicker.value == config.type) {
        SelectionListCard(
            config = config,
            searchQuery = searchQuery.value,
            onQueryChange = { searchQuery.value = it },
            onSelect = {
                searchQuery.value = ""
                activePicker.value = null
            }
        )
    } else {
        CollapsedPickerRow(
            label = config.label,
            selectedText = config.selectedItem?.let { config.itemToString(it) },
            addLabel = config.addLabel,
            onClick = {
                searchQuery.value = ""
                activePicker.value = config.type
            }
        )
    }
}


@Composable
fun <T : Any> SelectionListCard(
    config: PickerConfig<T>,
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onSelect: () -> Unit
) {
    val filteredItems = remember(searchQuery, config.items) {
        config.items.filter { config.itemToString(it).contains(searchQuery, true) }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth(0.92f)
            .heightIn(max = 350.dp)
            .background(Color.White, RoundedCornerShape(32.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            SearchBar(
                searchQuery = searchQuery,
                onQueryChange = onQueryChange,
                placeholder = config.placeholder
            )
            Spacer(modifier = Modifier.height(12.dp))
            LazyColumn {
                items(filteredItems) { item ->
                    LocationRow(
                        location = config.itemToString(item),
                        onClick = {
                            config.onItemSelected(item)
                            onSelect()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CollapsedPickerRow(
    label: String,
    selectedText: String?,
    addLabel: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth(0.92f)
            .heightIn(48.dp)
            .background(Color.White, RoundedCornerShape(32.dp))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, color = Color.Gray)
            if (selectedText == null) {
                IconText(
                    icon = painterResource(R.drawable.property_add),
                    actionText = addLabel,
                    onActionClick = onClick
                )
            } else {
                RowText(actionText = selectedText, onActionClick = onClick)
            }
        }
    }
}

@Composable
fun ActionButtons(
    onCancel: () -> Unit,
    onApply: () -> Unit,
    applyEnabled: Boolean
) {
    Row(
        modifier = Modifier.padding(dimensionResource(R.dimen.dp_12)),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.dp_16)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // --- CANCEL BUTTON ---
        Box(
            modifier = Modifier
                .weight(1f)
                .heightIn(dimensionResource(R.dimen.dp_48))
                .background(WhiteColor, RoundedCornerShape(24.dp))
        ) {
            CommonButton(
                text = stringResource(id = R.string.cancel),
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth(),
                gradientBrush = SolidColor(WhiteColor),
                contentColor = RedColor
            )
        }

        Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_8)))

        // --- APPLY BUTTON ---
        Box(
            modifier = Modifier
                .weight(1f)
                .heightIn(dimensionResource(R.dimen.dp_48))
                .background(WhiteColor, RoundedCornerShape(24.dp))
        ) {
            CommonButton(
                text = stringResource(id = R.string.apply_filter),
                onClick = onApply,
                modifier = Modifier.fillMaxWidth(),
                enabled = applyEnabled
            )
        }
    }
}


@Composable
fun MyFilterScreen(
    selectedLocationId: MutableState<LocationModel?>,
    selectedCustomValue: MutableState<String>,
    customLabel: MutableState<String>,
    locationList: List<LocationModel>,
    customValuesList: SnapshotStateList<String>,
    onDismiss: () -> Unit,
    onFilterApplied: (LocationModel?, String?) -> Unit,
) {
    // 1. Prepare the configurations for the generic picker
    val pickerConfigs = listOf(
        PickerConfig(
            label = "Zone",
            placeholder = stringResource(id = R.string.search_by_location_code),
            addLabel = stringResource(R.string.add_location),
            items = locationList,
            selectedItem = selectedLocationId.value,
            onItemSelected = { selectedLocationId.value = it },
            itemToString = { it.name },
            type = PickerType.ZONE
        ),
        PickerConfig(
            label = customLabel.value,
            placeholder = String.format(stringResource(id = R.string.search_), customLabel.value),
            addLabel = String.format(stringResource(R.string.add_), customLabel.value),
            items = customValuesList,
            selectedItem = selectedCustomValue.value.takeIf { it.isNotEmpty() },
            onItemSelected = { selectedCustomValue.value = it },
            itemToString = { it },
            type = PickerType.BAND
        )
    )

    // 2. Call the generic component
    GenericOptionPicker(
        configs = pickerConfigs,
        onDismiss = onDismiss, // Passes the parameter from MyFilterScreen
        onApply = {
            // Executes the filter logic using the current state values
            onFilterApplied(selectedLocationId.value, selectedCustomValue.value)
        },
        // Validation logic for the Apply button
        applyEnabled = selectedLocationId.value != null && selectedCustomValue.value.isNotEmpty()
    )
}
