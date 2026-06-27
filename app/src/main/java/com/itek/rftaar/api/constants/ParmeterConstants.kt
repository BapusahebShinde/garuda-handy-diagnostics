package com.itek.rftaar.api.constants

object ParameterConstants {

  const val BASE_URL: String = "baseUrl"
  const val BROKER_URL: String = "brokerUrl"

  //Constant Vals
  const val TOKEN_GRANT_TYPE: String = "grant_type"
  const val TOKEN_GRANT_TYPE_VAL: String = "password"
  const val DEVICE_TYPE_VAL: String = "handy"

  //Customer
  const val CUSTOMER_ID: String = "customerId"

  //User
  const val USER_ID: String = "userId"
  const val USERNAME: String = "username"
  const val USER_NAME: String = "userName"
  const val USER_FIRST_NAME: String = "firstName"
  const val USER_LAST_NAME: String = "lastName"
  const val PASSWORD: String = "password"
  const val EMAIL: String = "email"
  const val IS_EMAIL_VERIFIED: String = "emailVerified"
  const val ROLES: String = "roles"

  //Device
  const val FIRMWARE_VERSION: String = "firmwareVersion"
  const val DEVICE_KEY: String = "deviceKey"
  const val SERIAL: String = "serial"
  const val DEVICE_ID: String = "deviceId"
  const val DEVICE_TYPE: String = "deviceType"
  const val CLIENT_ID: String = "clientId"
  const val CLIENT_SECRET: String = "clientSecret"
  const val IP_ADDRESS: String = "ipAddress"
  const val MAKE: String = "make"
  const val MODEL: String = "model"


  //Location
  const val LOCATION_ID: String = "locationId"
  const val LOCATION_PATH: String = "locationPath"
  const val LOCATION_NAME: String = "locationName"
  const val LOCATION_CODE: String = "locationCode"
  const val BUSINESS_LINE: String = "businessLine"
  const val BUSINESS_LINES: String = "businessLines"
  const val IS_PHYSICAL: String = "isPhysical"
  const val TIME_ZONE: String = "timezone"
  const val PATH: String = "path"
  const val ANCESTORS: String = "ancestors"
  const val CHILDREN: String = "children"
  const val REPLENISHMENT_SOURCE: String = "isReplenishmentSource"

  //Replenishment
  const val DEST_LOCATION_PATH = "destinationLocationId"
  const val DEST_LOCATION_NAME = "destinationLocationName"
  const val PENDING_COUNT = "pendingCount"
  const val PENDING_QTY = "pendingQty"
  const val COMPLETED_TODAY_COUNT = "completedTodayCount"
  const val COMPLETED_TODAY_QTY = "completedTodayQty"
  const val IN_PROCESS_TODAY_COUNT = "inProcessTodayCount"
  const val IN_PROCESS_TODAY_QTY = "inProcessTodayQty"
  const val IN_PROGRESS_TODAY_COUNT = "inProgressTodayCount"
  const val IN_PROGRESS_TODAY_QTY = "inProgressTodayQty"
  const val BUCKET = "bucket"
  const val BELOW_RANGE = "belowRange"
  const val BELOW_RANGE_COUNT = "belowRangeCount"
  const val BELOW_RANGE_QTY = "belowRangeQty"
  const val WITHIN_RANGE = "withinRange"
  const val WITHIN_RANGE_COUNT = "withinRangeCount"
  const val WITHIN_RANGE_QTY = "withinRangeQty"
  const val ABOVE_RANGE = "aboveRange"
  const val ABOVE_RANGE_COUNT = "aboveRangeCount"
  const val ABOVE_RANGE_QTY = "aboveRangeQty"

  const val ORGANIZATION_ID: String = "organizationId"
  const val GROUP_ID: String = "groupId"
  const val BUSINESS_LINE_ID: String = "businessLineId"
  const val BUSINESS_LINE_ID1: String = "businessLineid"

  //Configuration
  const val CONFIGURATION="configuration";
  const val ID="id";
  const val ENCODE_LOG_ID="encodeLogId";
  const val INV_MAX_ID ="invMaxId";
  const val CODE="code";
  const val NAME="name";
  const val LABEL="label";
  const val LABEL_NAME="labelName";
  const val VALUE="value";
  const val VALUES="values";
  const val IS_SECRET="isSecret";

  //Menu
  const val DEVICE_MENU: String = "deviceMenu"
  const val ICON: String = "icon"
  const val IS_UN_PROVISIONED: String = "unprovisioned"
  const val IS_ACTIVE: String = "isActive"
  const val IS_ENABLED: String = "isEnabled"
  const val ENABLED: String = "enabled"
  const val SEQUENCE: String = "sequence"
  const val DEVICE_TYPES: String = "deviceTypes"
  const val PARENT_CODE: String = "parentCode"

  //Features
  const val ENCODING: String = "encoding"
  const val INVENTORY: String = "inventory"
  const val DECODE: String = "decode"

  //Tag Passwords
  const val TAG_PASSWORDS: String = "tagPasswords"
  const val CURRENT_PASSWORD: String = "currentPassword"
  const val OLD_PASSWORDS: String = "oldPasswords"

  //ENCODING
  const val ALLOWED_CHIPSETS: String = "allowedChipsets"
  const val ALLOWED_BARCODES: String = "allowedBarcodes"
  const val CUSTOMER_ENCODE_LIMIT: String = "customerEncodeLimit"
  const val LOCATION_ENCODE_LIMIT: String = "locationEncodeLimit"

  //Encoding Algorithms
  const val ENCODE_ALGORITHMS: String = "encodeAlgorithms"
  const val STANDARD_ALGORITHM: String = "standardAlgorithm"
  const val NON_STANDARD_ALGORITHM: String = "nonStandardAlgorithm"

  //Inventory
  const val ENABLE_ON_DEMAND_STOCK_DISCREPANCY: String = "enableOnDemandStockDiscrepancy"
  const val CUSTOM_INVENTORY_PARAM: String = "customInventoryParam"
  const val CUSTOM_INVENTORY_PARAM_LABEL: String = "customParamLabel"
  const val CUSTOM_INVENTORY_PARAM_NAME: String = "customParamName"
  const val CUSTOM_INVENTORY_PARAM_VALUE: String = "customParamValue"
  const val CUSTOM_INVENTORY_ADVANCE_FILTERS: String = "customInventoryAdvanceFilters"
  const val FILTERS: String = "filters"


  //Product
  const val MAPPING: String = "mapping"
  const val META: String = "meta"
  const val DISPLAY_LABEL_NAME: String = "displayLabelName"
  const val CUSTOM_COLUMN_NAME: String = "customColumnName"
  const val CUSTOM_DISPLAY_COLUMN_NAME: String = "customDisplayColumnName"

  //DECODE
  const val DECODE_BITS:String ="decodeBits"

  //Request
  const val APP_VERSION: String = "appVersion"
  const val APPLICATION_VERSION: String = "applicationVersion"
  const val MAC_ID: String = "macId"
  const val MAC_ADDRESS: String = "macAddress"
  const val LAT_LNG: String = "latLng"
  const val DEVICE_DATE_TIME: String = "deviceDateTime"
  const val TIME_ZONE_OFFSET_HOURS: String = "timeZoneOffsetInHours"

  //Response
  const val DATA: String = "data"
  const val LIST: String = "list"
  const val PAGE: String = "page"
  const val LIMIT: String = "limit"
  const val TOTAL: String = "total"
  const val UPDATED_AT =  "updatedAt"
  const val UUID =  "uuid"


  //Response (Token)
  const val TOKEN_TYPE: String = "tokenType" //tokenType
  const val ACCESS_TOKEN: String = "accessToken" //accessToken
  const val REFRESH_TOKEN: String = "refreshToken" //accessToken
  const val EXPIRES_IN: String = "expiresIn" //expiresIn

  //Response (MQTT)
  const val MQTT_IP:String = "mqttIp"
  const val MQTT_DOMAIN:String = "mqttDomain"
  const val MQTT_PORT:String = "mqttPort"
  const val IS_MQTT_SECURE:String = "mqttSecure"

  //Response (Error/Fail)
  const val MESSAGE = "message"
  const val MSG = "msg"
  const val ERR_MSG = "errMsg"
  const val ERROR = "error"
  const val ERROR_DESCRIPTION = "error_description"
  const val SUCCESS = "success"
  
  //Session
  const val DEVICE_SESSION_ID = "deviceSessionId"
  const val ACTION = "action"

  //Encode & Decode
  const val OPERATION_LOCATION_ID= "operationLocationId"
  const val BARCODE= "barcode"
  const val QTY= "qty"
  const val SCAN_QTY= "scanQty"
  const val TYPE= "type"
  const val DECODE_TYPE= "decodeType"
  const val START_DATE= "startDate"
  const val END_DATE= "endDate"
  const val OLD_EPC= "oldEpc"
  const val EPC= "epc"
  const val TID= "tid"
  const val COMPLETION_STATUS= "completionStatus"
  const val COMPLETION_REMARK= "completionRemark"
  const val REMARK= "remark"
  const val DETAILS= "details"
  const val NEW_EPC= "newEpc"
  const val EAN= "ean"
  const val IMAGES= "images"

  //Inventory
  const val ASSET_LOCATION_NAME = "assetLocationName"
  const val ASSET_LOCATION_PATH = "assetLocationPath"
  const val MOVE_AT_ASSET_LOCATION_NAME = "moveAtAssetLocationName"
  const val MOVE_AT_ASSET_LOCATION_PATH = "moveAtAssetLocationPath"
  const val DEVICE_LOCATION_ID: String = "deviceLocationId"
  const val SESSION_ID: String = "sessionId"
  const val SESSION_TYPE: String = "sessionType"
  const val TRANSACTION_TYPE: String = "transactionType"
  const val TRANSACTION_TYPES: String = "transactionTypes"
  const val ITEMS: String = "items"
  const val NON_ENCODED_ITEMS: String = "nonEncodedItems"
  const val READER_POWER: String = "readerPower"
  const val STOCK_COUNT: String = "stockCount"

  //Search
  const val SEARCH_TYPE_ID = "searchTypeId"
  const val SEARCH_TYPE_NAME = "searchTypeName"
  const val SEARCH_VALUE_TYPE = "searchValueType"
  const val SEARCH_VALUE = "searchValue"
  const val OPERATION = "operation"
  const val REFERENCE_NUMBER = "referenceNumber"
  const val CREATED_AT = "createdAt"
  const val CREATED_BY = "createdBy"
  const val START_DATE_TIME = "startDateTime"
  const val END_DATE_TIME = "endDateTime"
  const val DISPLAY_VALUE_STYLE_PARAMETER_1 = "displayValueStyleParameter1"
  const val DISPLAY_VALUE_STYLE_PARAMETER_2 = "displayValueStyleParameter2"

  const val STOCK_QTY = "stockQty"
  const val TOTAL_QTY = "totalQty"

  //Scan Count
  const val VALID_SCAN_TAG_COUNT: String = "validScanTagCount"
  const val INVALID_SCAN_TAG_COUNT: String = "invalidScanTagCount"


  //Product Details
  const val HEADERS= "headers"
  const val PRODUCTS= "products"

  //Inward/Outward
  const val FLOW_ID="flowId"
  const val FLOW_NAME="flowName"
  const val LEVEL="level"
  const val NODE="node"
  const val NODE_LEVEL="nodeLevel"
  const val COMPLETED="completed"
  const val STATUS="status"
  const val ARTICLE="article"
  const val LABEL_ARTICLE="articleLabel"
  const val LABEL_BARCODE="barcodeLabel"
  const val LABEL_IMAGE="imageLabel"
  const val EXPECTED="expected"
  const val ACTUAL="actual"
  const val EXPECTED_QTY="expectedQty"
  const val ACTUAL_QTY="actualQty"
  const val RFID="rfid"
  const val RSSI="rssi"
  const val PHASE="phase"
  const val ANTENNA="antenna"
}