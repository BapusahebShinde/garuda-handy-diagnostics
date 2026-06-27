package com.itek.rftaar.data.model

import kotlinx.serialization.Serializable

@Serializable
data class LabelName(
    val label: String,
    val name: String
)