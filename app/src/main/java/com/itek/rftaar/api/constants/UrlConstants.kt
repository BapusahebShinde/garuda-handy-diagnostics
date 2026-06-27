package com.itek.rftaar.api.constants

object UrlConstants {
  const val BASE_URL = "https://nextgendevapi.garudavigil.com"

  //Validate URL
  const val VALIDATE_URL = "/api"

  //forgot Password
  const val FORGOT_PASSWORD = "/api/auth/forgotpassword"

  //User
  const val USER_LOGIN = "/api/auth/login"
  const val USER_DETAILS = "/api/auth/me"

  //Device
  const val DEVICE_CREATE = "/api/devices"
  const val DEVICE_CHECK = "/api/devices/"
  const val DEVICE_LOGIN = "/api/auth/devices/login"

  const val DEVICE_MENUS = "/api/deviceMenu/maps/device/activeDeviceMenus"

  //Location
  const val LOCATION_LIST_PAGE_LIMIT = 1000;
  const val LOCATION_LIST = "/api/locations/customers/"
  const val LOCATION_CONFIG = "/api/locations/"

  //encoding
  const val GET_ENCODE_COUNT = "/api/encoders/encodeCount"//?customerId=f0f91964-b310-4f3e-9cd7-09c633299cae&operationLocationId=64c0c98b-c463-492b-9d15-c2f4d2eada58
  const val ENCODING = "/api/encoders/encode"

  //product details
  const val PRODUCTS = "/api/products"
  const val PRODUCT_MAPPING = "/api/productsMap"//?customerId={{customerId}}&locationId={{businessLineId}}
  const val PRODUCT_CHART = "/api/productSearch/style-search/ean-list"

  const val GET_SEARCH_COUNT = "/api/productSearch/searchCount"

  //Search
  const val PRODUCT_SEARCH_LIST_CONFIG = "/api/productSearch/list/config"
  const val PRODUCT_SEARCH_LIST = "/api/productSearch/list"
  const val PRODUCT_SEARCH_LIST_DETAILS = "/api/productSearch/list/details"
  const val PRODUCT_UPDATE = "/api/productSearch/update"

  const val GET_OMNI_DASHBOARD = "/api/dashboard/search-widget"

  //Decoding (Get Password)
  const val GET_PASSWORD = "/api/encoders/tagPasswords/"

  //Decoding Types
  //const val GET_DECODE_TYPES = "/api/decode/list/"
  const val GET_DECODE_TYPES = "/api/decode/allDecodeTypes"

  //Inventory (Stock Count)
  const val GET_STOCK_COUNT="/api/stocks/stockCount"//?customerId=f0f91964-b310-4f3e-9cd7-09c633299cae&operationLocationId=64c0c98b-c463-492b-9d15-c2f4d2eada58

  //Inventory (getZones)
  const val LOCATION_ZONES = "/api/locations/children"
  const val LOCATION_SUB_ZONES = "/api/locations/descendants"

  //Inventory (Session)
  const val START_DEVICE_SESSION_INVENTORY = "/api/inventoryDeviceSessions/startInvDeviceSession"
  const val STOP_DEVICE_SESSION_INVENTORY = "/api/inventoryDeviceSessions/endInvDeviceSession"
  const val CANCEL_DEVICE_SESSION_INVENTORY = "/api/inventoryDeviceSessions/cancelInvDeviceSession"

  //Custom Inventory
  const val CUSTOM_INVENTORY_PARAMS = "/api/inventory/custom-inventory-params"
  const val CUSTOM_INVENTORY_ADVANCED_FILTERS = "/api/inventory/custom-inventory-advance-filters"
  const val CUSTOM_INVENTORY_EAN_LIST ="/api/inventory/custom-inventory-ean-list"

  //Stock Correction
  const val STOCK_DISCREPANCY="/api/stockDiscrepancy"///location
  const val STOCK_DISCREPANCY_LOCATION="/api/stockDiscrepancy/location"

  //Replenishment
  const val GET_REPLENISHMENT_LIST = "/api/replenishment/pending"
  const val GET_REPLENISHMENT_DASHBOARD = "/api/dashboard/replenishment"

  //Inward
  const val GET_INWARD_LIST="/api/inward/pending"//?customerId=4e051e2c-0409-45fc-bf29-681af1f73da7&businessLineId=14b5906f-773f-4835-933a-149ab8bac1b8&locationId=97a2e811-39b9-4e36-8329-bbbd16344231&deviceId=97a2e811-39b9-4e36-8329-bbbd16344231
  const val GET_INWARD_CHILDREN_LIST="/api/inward/children"//?customerId=4e051e2c-0409-45fc-bf29-681af1f73da7&businessLineId=14b5906f-773f-4835-933a-149ab8bac1b8&locationId=97a2e811-39b9-4e36-8329-bbbd16344231&deviceId=97a2e811-39b9-4e36-8329-bbbd16344231&flowId=2db83a8f-123f-449d-a022-5b4c00ddb53f&level=4&nodeLevel=1&node4=T-176&node3=PO-173&node2=ASN-1&node1=null"
  const val GET_INWARD_BOX_DETAILS="/api/inward/status"//?customerId=4e051e2c-0409-45fc-bf29-681af1f73da7&businessLineId=14b5906f-773f-4835-933a-149ab8bac1b8&locationId=97a2e811-39b9-4e36-8329-bbbd16344231&deviceId=97a2e811-39b9-4e36-8329-bbbd16344231&flowId=2db83a8f-123f-449d-a022-5b4c00ddb53f&level=4&nodeLevel=1&node4=T-176&node3=PO-173&node2=ASN-1&node1=BOX-13
  const val UPLOAD_INWARD_SCANNED="/api/inward/scanned"//{"deviceId": "14b5906f-773f-4835-933a-149ab8bac1b8","customerId": "4e051e2c-0409-45fc-bf29-681af1f73da7","businessLineId": "14b5906f-773f-4835-933a-149ab8bac1b8","locationId": "962b2c61-1b60-4e75-817d-096583ac14bf","flowId": "2db83a8f-123f-449d-a022-5b4c00ddb53c","level": 3,"nodeLevel": 1,"node4": null,"node3": "PO-172","node2": "ASN-2","node1": "BOX-2","expectedQty": 2,"actualQty": 2,"remark": "Exact Same Qty Found","reScanned": false,"status": "ACCEPTED",//ACCEPTED/REJECTED "expected": [{ "article": "AD-100","barcode": "EAN-101,EAN-102","qty": 2 }],"scanned": [{ "article": "AD-100","barcode": "EAN-101","remark": "","qty": 10,"rfid": [{ "epc": "xfsf","tid": "dasds","rssi": [{ "antenna": 1,"rssi": 60,"phase": 10 }] },{ "epc": "xfsf2","tid": "dasds2","rssi": [{ "antenna": 1,"rssi": 60,"phase": 10 }] }]}]}
  const val COMPLETE_INWARD_NODE="/api/inward/updateStatus"//{"customerId": "4e051e2c-0409-45fc-bf29-681af1f73da7","businessLineId": "14b5906f-773f-4835-933a-149ab8bac1b8","locationId": "97a2e811-39b9-4e36-8329-bbbd16344231","status": "COMPLETED","level": 4,"nodeLevel": 4,"node4": "T-173","deviceId": "2db83a8f-123f-449d-a022-5b4c00ddb53f","flowId": "2db83a8f-123f-449d-a022-5b4c00ddb53f" }


  // Outward
  const val GET_OUTWARD_LIST="/api/outward/pending"//?customerId=4e051e2c-0409-45fc-bf29-681af1f73da7&businessLineId=14b5906f-773f-4835-933a-149ab8bac1b8&locationId=97a2e811-39b9-4e36-8329-bbbd16344231&deviceId=97a2e811-39b9-4e36-8329-bbbd16344231
  const val GET_OUTWARD_CHILDREN_LIST="/api/outward/children"//?customerId=4e051e2c-0409-45fc-bf29-681af1f73da7&businessLineId=14b5906f-773f-4835-933a-149ab8bac1b8&locationId=97a2e811-39b9-4e36-8329-bbbd16344231&deviceId=97a2e811-39b9-4e36-8329-bbbd16344231&flowId=2db83a8f-123f-449d-a022-5b4c00ddb53f&level=4&nodeLevel=1&node4=T-176&node3=PO-173&node2=ASN-1&node1=null"
  const val GET_OUTWARD_BOX_DETAILS="/api/outward/status"//?customerId=4e051e2c-0409-45fc-bf29-681af1f73da7&businessLineId=14b5906f-773f-4835-933a-149ab8bac1b8&locationId=97a2e811-39b9-4e36-8329-bbbd16344231&deviceId=97a2e811-39b9-4e36-8329-bbbd16344231&flowId=2db83a8f-123f-449d-a022-5b4c00ddb53f&level=4&nodeLevel=1&node4=T-176&node3=PO-173&node2=ASN-1&node1=BOX-13
  const val UPLOAD_OUTWARD_SCANNED="/api/outward/scanned"//{"deviceId": "14b5906f-773f-4835-933a-149ab8bac1b8","customerId": "4e051e2c-0409-45fc-bf29-681af1f73da7","businessLineId": "14b5906f-773f-4835-933a-149ab8bac1b8","locationId": "962b2c61-1b60-4e75-817d-096583ac14bf","flowId": "2db83a8f-123f-449d-a022-5b4c00ddb53c","level": 3,"nodeLevel": 1,"node4": null,"node3": "PO-172","node2": "ASN-2","node1": "BOX-2","expectedQty": 2,"actualQty": 2,"remark": "Exact Same Qty Found","reScanned": false,"status": "ACCEPTED",//ACCEPTED/REJECTED "expected": [{ "article": "AD-100","barcode": "EAN-101,EAN-102","qty": 2 }],"scanned": [{ "article": "AD-100","barcode": "EAN-101","remark": "","qty": 10,"rfid": [{ "epc": "xfsf","tid": "dasds","rssi": [{ "antenna": 1,"rssi": 60,"phase": 10 }] },{ "epc": "xfsf2","tid": "dasds2","rssi": [{ "antenna": 1,"rssi": 60,"phase": 10 }] }]}]}
  const val COMPLETE_OUTWARD_NODE="/api/outward/updateStatus"//{"customerId": "4e051e2c-0409-45fc-bf29-681af1f73da7","businessLineId": "14b5906f-773f-4835-933a-149ab8bac1b8","locationId": "97a2e811-39b9-4e36-8329-bbbd16344231","status": "COMPLETED","level": 4,"nodeLevel": 4,"node4": "T-173","deviceId": "2db83a8f-123f-449d-a022-5b4c00ddb53f","flowId": "2db83a8f-123f-449d-a022-5b4c00ddb53f" }
}