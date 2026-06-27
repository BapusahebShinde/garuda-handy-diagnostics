package com.itek.rftaar.domain.model

data class LocationModel(
    val id: String="",
    val name: String="",
    val code: String="",
    val path: String=""
){
    override fun toString(): String {
        return name.trim()
    }
}

