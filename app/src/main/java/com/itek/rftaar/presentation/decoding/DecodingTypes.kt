package com.itek.rftaar.presentation.decoding

import com.itek.rftaar.data.model.LabelName

object DecodingTypes {

    val SALES = LabelName(
        label = "Sales Decoding",
        name = "SALES"
    )

    val OMNICHANNEL = LabelName(
        label = "Omnichannel Decoding",
        name = "OMNICHANNEL"
    )

    val GRDC = LabelName(
        label = "GRDC Decoding",
        name = "GRDC"
    )

    val OTHERS = LabelName(
        label = "Other Decoding",
        name = "OTHERS"
    )

    fun fromName(type: String): LabelName {
        return when (type.uppercase()) {
            "SALES" -> SALES
            "OMNICHANNEL" -> OMNICHANNEL
            "GRDC" -> GRDC
            else -> OTHERS
        }
    }
}
