package com.itek.rftaar.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.itek.rftaar.data.entity.ProductZoneDataEntity
import com.itek.rftaar.data.model.EanFoundQty
import com.itek.rftaar.data.model.ProductZoneFoundQty
import com.itek.rftaar.data.model.ZoneFoundQty
import com.itek.rftaar.domain.model.LocationModel
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductZoneDataDao {

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(productZone: ProductZoneDataEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(productZones: List<ProductZoneDataEntity>)

  /**
   * Update.
   * @param ProductZoneDataEntity the productZone
   * */
  @Update
  suspend fun update(vararg productZone: ProductZoneDataEntity): Int

  @Update
  suspend fun updateAll(productZones: List<ProductZoneDataEntity>) : Int

  /**
   * Delete.
   * @param ProductZoneDataEntity the productZone
   */
  @Delete
  suspend fun delete(vararg productZone: ProductZoneDataEntity): Int

  /**
   * Delete all.
   */
  @Query("DELETE FROM product_zones")
  suspend fun deleteAll()

  /**
   * Delete all.
   */
  @Query("DELETE FROM product_zones WHERE topic = :topic AND session_type = :sessionType AND transaction_type=:transactionType")
  suspend fun deleteAll(topic: String,sessionType: String,transactionType: String)

    /*@Query("SELECT *,COUNT(barcode) as quantity,SUM(found_qty) as foundQty FROM product_zones WHERE topic = :topic AND session_type = :sessionType AND transaction_type=:transactionType GROUP BY barcode")
    fun getAll(topic: String, sessionType: String, transactionType: String): Flow<List<ProductZoneDataEntity>>*/

    /*@Transaction
    @Query("SELECT *,COUNT() as quantity,SUM(found_qty) as foundQty FROM product_zones WHERE asset_location_name IS NOT NULL AND LENGTH(TRIM(asset_location_name))>0 AND topic = :topic AND session_type = :sessionType AND transaction_type=:transactionType AND asset_location_name=:zoneName AND asset_location_path=:zonePath GROUP BY barcode")
    fun getAll(
        topic: String,
        sessionType: String,
        transactionType: String,
        zoneName: String,
        zonePath: String
    ): Flow<List<ProductZoneDataEntity>> */


  @Query("SELECT COALESCE(barcode,'') as barcode, COALESCE(custom_field,'') as customField, COALESCE(display_data ,'')as displayData, COALESCE(asset_location_name,'') as name, COALESCE(asset_location_path,'') as path ,COALESCE(COUNT(barcode),0) as totalQty, COALESCE(stock,0) as stockQty, COALESCE(total_stock,0) as totalAvailableStockQty,  COALESCE(SUM(found_qty),0) as foundQty,  COALESCE(SUM(decode_qty),0) as decodeQty,  COALESCE(err_msg,'') as errMsg FROM product_zones WHERE barcode IS NOT NULL AND LENGTH(TRIM(barcode))>0 AND topic = :topic AND session_type = :sessionType AND transaction_type=:transactionType GROUP BY barcode")
  fun getAll(topic: String, sessionType: String, transactionType: String): Flow<List<ProductZoneFoundQty>>

  @Query("SELECT COALESCE(barcode,'') as barcode, COALESCE(custom_field,'') as customField, COALESCE(display_data,'') as displayData, COALESCE(asset_location_name,'') as name, COALESCE(asset_location_path,'') as path, COALESCE(quantity,0) as totalQty, COALESCE(total_stock,0) as totalAvailableStockQty, COALESCE(stock,0) as stockQty, COALESCE(found_qty,0) as foundQty, COALESCE(decode_qty,0) as decodeQty, COALESCE(err_msg,'') as errMsg FROM product_zones WHERE barcode IS NOT NULL AND LENGTH(TRIM(barcode))>0 AND topic = :topic AND session_type = :sessionType AND transaction_type=:transactionType ORDER BY barcode asc")
  fun getAll1(topic: String, sessionType: String, transactionType: String): Flow<List<ProductZoneFoundQty>>

    @Transaction
    @Query("SELECT COALESCE(barcode,'') as barcode, COALESCE(custom_field,'') as customField, COALESCE(display_data,'') as displayData, COALESCE(asset_location_name,'') as name, COALESCE(asset_location_path,'') as path, COALESCE(COUNT(barcode),0) as totalQty, COALESCE(stock,0) as stockQty, COALESCE(total_stock,0) as totalAvailableStockQty,  COALESCE(SUM(found_qty),0) as foundQty,  COALESCE(SUM(decode_qty),0) as decodeQty, COALESCE(err_msg,'') as errMsg FROM product_zones WHERE asset_location_name IS NOT NULL AND LENGTH(TRIM(asset_location_name))>0 AND topic = :topic AND session_type = :sessionType AND transaction_type=:transactionType AND asset_location_name=:zoneName AND asset_location_path=:zonePath GROUP BY barcode")
    fun getAll(
        topic: String,
        sessionType: String,
        transactionType: String,
        zoneName: String,
        zonePath: String
    ): Flow<List<ProductZoneFoundQty>>


   @Query("SELECT 0 as id, asset_location_name as name, '' as code, asset_location_path as path FROM product_zones WHERE topic = :topic AND session_type = :sessionType AND transaction_type=:transactionType AND asset_location_path IS NOT NULL AND LENGTH(TRIM(asset_location_path)) > 0 GROUP BY asset_location_path")
   fun getAllZones(topic: String, sessionType: String, transactionType: String): Flow<List<LocationModel>>

  @Transaction
  @Query("SELECT TRIM(DISTINCT asset_location_name) as name, TRIM(DISTINCT asset_location_path) as path, SUM(found_qty) as foundQty, COUNT(asset_location_name) as totalQty, TRIM(DISTINCT barcode) as barcode FROM product_zones WHERE asset_location_name IS NOT NULL AND LENGTH(TRIM(asset_location_name))>0 AND topic = :topic AND session_type = :sessionType AND transaction_type=:transactionType GROUP BY asset_location_name ORDER BY asset_location_name ASC")
  fun getZoneQty(topic: String,sessionType: String,transactionType: String): Flow<List<ZoneFoundQty>>

  @Transaction
  @Query("SELECT TRIM(DISTINCT barcode) as barcode, SUM(found_qty) as foundQty, COUNT(barcode) as totalQty  FROM product_zones WHERE barcode IS NOT NULL AND LENGTH(TRIM(barcode))>0 AND topic = :topic AND session_type = :sessionType AND transaction_type=:transactionType AND asset_location_name=:zoneName AND asset_location_path=:zonePath GROUP BY barcode ORDER BY barcode ASC")
  fun getEanQty(topic: String,sessionType: String,transactionType: String,zoneName:String,zonePath:String): Flow<List<EanFoundQty>>

  @Transaction
  @Query("SELECT TRIM(DISTINCT custom_field) as barcode, SUM(found_qty) as foundQty, COUNT(custom_field) as totalQty  FROM product_zones WHERE custom_field IS NOT NULL AND LENGTH(TRIM(custom_field))>0 AND topic = :topic AND session_type = :sessionType AND transaction_type=:transactionType AND asset_location_name=:zoneName AND asset_location_path=:zonePath GROUP BY custom_field ORDER BY custom_field ASC")
  fun getCustomQty(topic: String,sessionType: String,transactionType: String,zoneName:String,zonePath:String): Flow<List<EanFoundQty>>

  @Transaction
  @Query("SELECT TRIM(DISTINCT epc) FROM product_zones WHERE epc IS NOT NULL AND LENGTH(TRIM(epc))>0 AND topic = :topic AND session_type = :sessionType AND transaction_type=:transactionType")
  fun getEpcs(topic: String,sessionType: String,transactionType: String): Flow<List<String>>

  @Transaction
  @Query("SELECT TRIM(DISTINCT epc) FROM product_zones WHERE epc IS NOT NULL AND LENGTH(TRIM(epc))>0 AND topic = :topic AND session_type = :sessionType AND transaction_type=:transactionType AND asset_location_name=:zoneName AND asset_location_path=:zonePath")
  fun getEpcs(topic: String,sessionType: String,transactionType: String,zoneName:String,zonePath:String): Flow<List<String>>

  @Transaction
  @Query("SELECT SUM(COALESCE(found_qty,0)) FROM product_zones WHERE topic = :topic AND session_type = :sessionType AND transaction_type=:transactionType")
  fun getFoundQty(topic: String,sessionType: String,transactionType: String): Flow<Int>

  @Transaction
  @Query("SELECT SUM(COALESCE(found_qty,0)) FROM product_zones WHERE topic = :topic AND session_type = :sessionType AND transaction_type=:transactionType AND barcode=:barcode")
  fun getFoundQty(topic: String,sessionType: String,transactionType: String,barcode: String): Flow<Int>

  @Transaction
  @Query("SELECT SUM(COALESCE(decode_qty,0)) FROM product_zones WHERE topic = :topic AND session_type = :sessionType AND transaction_type=:transactionType")
  fun getDecodeQty(topic: String,sessionType: String,transactionType: String): Flow<Int>


  @Transaction
  @Query("SELECT SUM(COALESCE(decode_qty,0)) FROM product_zones WHERE topic = :topic AND session_type = :sessionType AND transaction_type=:transactionType AND barcode=:barcode")
  fun getDecodeQty(topic: String,sessionType: String,transactionType: String,barcode: String): Flow<Int>

  @Transaction
  @Query("SELECT COALESCE(COUNT(1),0) FROM product_zones WHERE topic = :topic AND session_type = :sessionType AND transaction_type=:transactionType")
  fun getTotalQty(topic: String,sessionType: String,transactionType: String): Flow<Int>


  @Transaction
  @Query("SELECT COALESCE(COUNT(1),0) FROM product_zones WHERE topic = :topic AND session_type = :sessionType AND transaction_type=:transactionType AND asset_location_name=:zoneName AND asset_location_path=:zonePath")
  fun getTotalQty(topic: String,sessionType: String,transactionType: String,zoneName:String,zonePath:String): Flow<Int>

  @Query("SELECT COALESCE(COUNT(*),0)>0 FROM product_zones WHERE topic = :topic AND session_type = :sessionType AND transaction_type=:transactionType AND epc IS NOT NULL AND LENGTH(TRIM(epc))>0")
  fun hasEpcData(topic: String,sessionType: String,transactionType: String): Boolean

  @Query("SELECT COALESCE(COUNT(*),0)>0 FROM product_zones WHERE topic = :topic AND session_type = :sessionType AND transaction_type=:transactionType")
  fun hasData(topic: String,sessionType: String,transactionType: String): LiveData<Boolean>


  /**
   * Checks if barcode exists.
   */
  @Query("SELECT COALESCE(COUNT(*),0)>0 FROM product_zones WHERE topic = :topic AND session_type = :sessionType AND transaction_type=:transactionType AND TRIM(barcode)=:barcode LIMIT 1")
  fun hasBarcode(topic: String,sessionType: String,transactionType: String,barcode: String): Boolean

  @Query("SELECT COALESCE(COUNT(*),0)>0 FROM product_zones WHERE topic = :topic AND session_type = :sessionType AND transaction_type=:transactionType AND session_id=:sessionId AND TRIM(barcode)=:barcode AND TRIM(asset_location_name)=:zoneName AND TRIM(asset_location_path)=:zonePath LIMIT 1")
  fun hasBarcodeZone(topic: String,sessionType: String,transactionType: String,sessionId: String,barcode: String,zoneName: String,zonePath: String): Boolean

  @Query("SELECT COALESCE(COUNT(*),0)>0 FROM product_zones WHERE topic = :topic AND session_type = :sessionType AND transaction_type=:transactionType AND TRIM(barcode) IN(:barcodes)")
  fun hasBarcodes(topic: String,sessionType: String,transactionType: String,barcodes: List<String>): Boolean

  @Query("SELECT COALESCE(COUNT(*),0)>0 FROM product_zones WHERE custom_field IS NOT NULL AND LENGTH(TRIM(custom_field))>0 AND topic = :topic AND session_type = :sessionType AND transaction_type=:transactionType AND asset_location_name=:zoneName AND asset_location_path=:zonePath LIMIT 1")
  fun hasCustomField(topic: String,sessionType: String,transactionType: String,zoneName: String,zonePath: String): Boolean

  /**
   * Update Found Barcode
   */
  @Transaction
  @Query("UPDATE product_zones SET found_qty=(found_qty+1) WHERE topic = :topic AND session_type = :sessionType AND transaction_type=:transactionType AND TRIM(barcode)=:barcode")
  fun updateFoundBarcode(topic: String,sessionType: String,transactionType: String,barcode: String): Int

  @Transaction
  @Query("UPDATE product_zones SET found_qty=(found_qty+1),decode_qty=(decode_qty+1) WHERE topic = :topic AND session_type = :sessionType AND transaction_type=:transactionType AND TRIM(barcode)=:barcode")
  suspend fun updateFoundAndDecodeBarcodeCount(topic: String,sessionType: String,transactionType: String,barcode: String): Int

  @Transaction
  @Query("UPDATE product_zones SET found_qty=(found_qty+1) WHERE topic = :topic AND session_type = :sessionType AND transaction_type=:transactionType AND session_id=:sessionId AND TRIM(barcode)=:barcode AND TRIM(asset_location_name)=:zoneName AND TRIM(asset_location_path)=:zonePath")
  suspend fun updateFoundBarcodeZone(topic: String,sessionType: String,transactionType: String,sessionId: String,barcode: String,zoneName: String,zonePath: String): Int

  @Transaction
  @Query("UPDATE product_zones SET found_qty=(found_qty+1),decode_qty=(decode_qty+1) WHERE topic = :topic AND session_type = :sessionType AND transaction_type=:transactionType AND session_id=:sessionId AND TRIM(barcode)=:barcode AND TRIM(asset_location_name)=:zoneName AND TRIM(asset_location_path)=:zonePath")
  suspend fun updateFoundAndDecodeBarcodeZone(topic: String,sessionType: String,transactionType: String,sessionId:String,barcode: String,zoneName: String,zonePath: String): Int

  @Transaction
  @Query("UPDATE product_zones SET found_qty=(found_qty+:qty) WHERE topic = :topic AND session_type = :sessionType AND transaction_type=:transactionType AND TRIM(barcode)=:barcode")
  suspend fun updateFoundBarcodeQty(topic: String,sessionType: String,transactionType: String,barcode: String,qty:Int): Int

  @Transaction
  @Query("UPDATE product_zones SET found_qty=(found_qty+1) WHERE topic = :topic AND session_type = :sessionType AND transaction_type=:transactionType AND TRIM(barcode) IN (:barcodes)")
  suspend fun updateFoundBarcodes(topic: String,sessionType: String,transactionType: String,barcodes: List<String>): Int

  /**
   * Checks if epc exists.
   */
  @Query("SELECT COALESCE(COUNT(*),0)>0 FROM product_zones WHERE topic = :topic AND session_type = :sessionType AND transaction_type=:transactionType AND TRIM(epc)=:epc LIMIT 1")
  fun hasEpc(topic: String,sessionType: String,transactionType: String,epc: String): Boolean

  @Query("SELECT COALESCE(COUNT(*),0)>0 FROM product_zones WHERE topic = :topic AND session_type = :sessionType AND transaction_type=:transactionType AND TRIM(epc) IN(:epcs)")
  fun hasEpcs(topic: String,sessionType: String,transactionType: String,epcs: List<String>): Boolean

  /**
   * Update Found
   */
  @Transaction
  @Query("UPDATE product_zones SET found_qty=(found_qty+1) WHERE topic = :topic AND session_type = :sessionType AND transaction_type=:transactionType AND TRIM(epc)=:epc AND COALESCE(found_qty,0)<=0")
  suspend fun updateFoundEpc(topic: String,sessionType: String,transactionType: String,epc: String): Int

  @Transaction
  @Query("UPDATE product_zones SET found_qty=(found_qty+1) WHERE topic = :topic AND session_type = :sessionType AND transaction_type=:transactionType AND TRIM(epc) IN(:epcs) AND COALESCE(found_qty,0)<=0")
  suspend fun updateFoundEpcs(topic: String,sessionType: String,transactionType: String,epcs: List<String>): Int

  /**
   * Gets table size.
   */
  @Query("SELECT COALESCE(COUNT(*),0) FROM product_zones")
  fun getTableSize(): Flow<Int>

  /**
   * Has data boolean.
   */
  @Query("SELECT COUNT(*) > 0 FROM product_zones")
  fun hasData(): Flow<Boolean>
}