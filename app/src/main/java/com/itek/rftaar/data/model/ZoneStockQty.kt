package com.itek.rftaar.data.model

data class ZoneStockQty(val name:String, val path:String, val stockCount:Int, val stockDiscrepancy:Int,val customFieldWiseCount: Map<String, Int> = emptyMap())