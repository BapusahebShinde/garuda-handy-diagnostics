package com.itek.rftaar.data.model

data class SearchConfig(
    val label: String = "Search",
    val menuCode: String = "GENERIC",
    val isTabView: Boolean = false,
    val isStatus: Boolean = false,
    val isSearchOptions: Boolean = true,
    val textValue: String = ""
)
