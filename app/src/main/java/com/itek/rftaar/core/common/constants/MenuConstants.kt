package com.itek.rftaar.core.common.constants

object MenuConstants {

  //ENCODE
  const val ENCODE = "MENU_ENCODE"
  const val NORMAL_ENCODE = "MENU_NORMAL_ENCODE"
  const val SINGLE_ENCODE = "MENU_SINGLE_ENCODE"
  const val MULTI_ENCODE = "MENU_MULTI_ENCODE"
  const val BULK_ENCODE = "MENU_BULK_ENCODE"
  const val VERIFY_ENCODE = "MENU_VERIFY_ENCODE"
  const val SCAN_SCAN_ENCODE = "MENU_SCAN_SCAN_ENCODE"
  //DECODE
  const val DECODE = "MENU_DECODE"
  const val RETURN = "MENU_RETURN"

  //INVENTORY
  const val INVENTORY = "MENU_INVENTORY"
  const val CYCLE_COUNT_INVENTORY = "MENU_CYCLE_COUNT_INVENTORY"
  const val TAKE_INVENTORY = "MENU_TAKE_INVENTORY"
  const val ADD_INVENTORY = "MENU_ADD_INVENTORY"
  const val CUSTOM_INVENTORY = "MENU_CUSTOM_INVENTORY"
  const val CUSTOM_TAKE_INVENTORY = "MENU_CUSTOM_TAKE_INVENTORY"
  const val CUSTOM_ADD_INVENTORY = "MENU_CUSTOM_ADD_INVENTORY"
  const val STOCK_CORRECTION = "MENU_STOCK_CORRECTION"

  //SEARCH
  const val SEARCH = "MENU_SEARCH"
  const val NORMAL_SEARCH = "MENU_NORMAL_SEARCH"
  const val PRODUCT_SEARCH = "MENU_PRODUCT_SEARCH"
  const val LIST_SEARCH = "MENU_LIST_SEARCH"
  const val OMNICHANNEL_SEARCH = "MENU_OMNICHANNEL_SEARCH"
  const val UNENCODED_SEARCH = "MENU_UNENCODED_SEARCH"
  const val ALIEN_SEARCH = "MENU_ALIEN_SEARCH"

  //MOVEMENT
  const val MOVEMENT = "MENU_MOVEMENT"
  const val MOVE_STOCK = "MENU_MOVE_STOCK"
  const val REPLENISH_STOCK = "MENU_REPLENISH_STOCK"

  //INWARD
  const val INWARD = "MENU_INWARD"

  //OUTWARD
  const val OUTWARD = "MENU_OUTWARD"

  //OTHER
  const val ASSOCIATE_BARCODE = "MENU_ASSOCIATE_BARCODE"

  fun isValueInConstants(valueToFind: String): Boolean {
        return this::class.java.declaredFields.any { field ->
            try {
                field.isAccessible = true
                field.get(this) == valueToFind
            } catch (e: Exception) {
                false
            }
        }
  }
}




