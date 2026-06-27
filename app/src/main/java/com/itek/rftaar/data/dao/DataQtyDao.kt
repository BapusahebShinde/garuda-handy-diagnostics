package com.itek.rftaar.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.itek.rftaar.data.entity.DataQtyEntity
import com.itek.rftaar.data.model.EanFoundQty
import kotlinx.coroutines.flow.Flow

@Dao
interface DataQtyDao {

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(dataQtyEntity: DataQtyEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(dataQtyEntities: List<DataQtyEntity>)

  /**
   * Update.
   * @param dataQtyEntity the data qty
   * */
  @Update
  suspend fun update(vararg dataQtyEntity: DataQtyEntity): Int

  @Update
  suspend fun updateAll(dataQtyEntities: List<DataQtyEntity>) : Int

  /**
   * Delete.
   * @param dataQtyEntity the data qty
   */
  @Delete
  suspend fun delete(vararg sessionList: DataQtyEntity): Int

  /**
   * Delete all.
   */
  @Query("DELETE FROM data_qty")
  suspend fun deleteAll()

  /**
   * Delete all.
   */
  @Query("DELETE FROM data_qty WHERE topic = :topic AND session_type=:sessionType AND transaction_type=:transactionType")
  suspend fun deleteAll(topic: String,sessionType:String,transactionType:String)

  /**
   * Checks if barcode exists.
   */
  @Query("SELECT COALESCE(COUNT(*),0)>0 FROM data_qty WHERE topic = :topic AND session_type = :sessionType AND transaction_type=:transactionType AND TRIM(barcode)=:barcode LIMIT 1")
  fun hasBarcode(topic: String,sessionType: String,transactionType: String,barcode: String): Boolean

  @Query("SELECT COALESCE(COUNT(*),0)>0 FROM data_qty WHERE topic = :topic AND session_type = :sessionType AND transaction_type=:transactionType AND TRIM(barcode) IN(:barcodes)")
  fun hasBarcodes(topic: String,sessionType: String,transactionType: String,barcodes: List<String>): Boolean

  @Query("UPDATE data_qty SET scan_qty=0  WHERE topic = :topic AND session_type = :sessionType AND transaction_type=:transactionType AND scan_qty>0")
  suspend fun clearScanQty(topic: String,sessionType: String,transactionType: String):Int

  /**
   * Update Found Barcode
   */
  @Transaction
  @Query("UPDATE data_qty SET scan_qty=(scan_qty+1) WHERE topic = :topic AND session_type = :sessionType AND transaction_type=:transactionType AND TRIM(barcode)=:barcode")
  suspend fun updateFoundBarcode(topic: String,sessionType: String,transactionType: String,barcode: String): Int

  @Transaction
  @Query("UPDATE data_qty SET scan_qty=(scan_qty+:qty) WHERE topic = :topic AND session_type = :sessionType AND transaction_type=:transactionType AND TRIM(barcode)=:barcode")
  suspend fun updateFoundBarcodeQty(topic: String,sessionType: String,transactionType: String,barcode: String,qty:Int): Int

  @Transaction
  @Query("UPDATE data_qty SET scan_qty=:qty WHERE topic = :topic AND session_type = :sessionType AND transaction_type=:transactionType AND TRIM(barcode)=:barcode")
  suspend fun updateFoundBarcodeQty1(topic: String,sessionType: String,transactionType: String,barcode: String,qty:Int): Int

  @Transaction
  @Query("UPDATE data_qty SET scan_qty=(scan_qty+1) WHERE topic = :topic AND session_type = :sessionType AND transaction_type=:transactionType AND TRIM(barcode) IN(:barcodes)")
  suspend fun updateFoundBarcodes(topic: String,sessionType: String,transactionType: String,barcodes: List<String>): Int

  /**
   * Checks if epc exists.
   */
  @Query("SELECT COALESCE(COUNT(*),0)>0 FROM data_qty WHERE topic = :topic AND session_type = :sessionType AND transaction_type=:transactionType AND epc IS NOT NULL AND LENGTH(TRIM(epc))>0 AND TRIM(epc)=:epc")
  fun hasEpc(topic: String,sessionType: String,transactionType: String,epc: String): Boolean


  @Query("SELECT COALESCE(COUNT(*),0)>0 FROM data_qty WHERE topic = :topic AND session_type = :sessionType AND transaction_type=:transactionType AND epc IS NOT NULL AND LENGTH(TRIM(epc))>0 AND TRIM(epc) IN(:epcs)")
  fun hasEpcs(topic: String,sessionType: String,transactionType: String,epcs: List<String>): Boolean

  /**
   * Checks if epc exists.
   */
  @Query("SELECT COALESCE(COUNT(*),0)>0 FROM data_qty WHERE topic = :topic AND session_type = :sessionType AND transaction_type=:transactionType AND epc IS NOT NULL AND LENGTH(TRIM(epc))>0")
  fun hasEpcData(topic: String,sessionType: String,transactionType: String): Boolean

  /**
   * Update Found
   */
  @Transaction
  @Query("UPDATE data_qty SET scan_qty=(scan_qty+1) WHERE topic = :topic AND session_type = :sessionType AND transaction_type=:transactionType AND TRIM(epc)=:epc AND COALESCE(scan_qty,0)<=0")
  suspend fun updateFoundEpc(topic: String,sessionType: String,transactionType: String,epc: String): Int

  @Transaction
  @Query("UPDATE data_qty SET scan_qty=(scan_qty+1) WHERE topic = :topic AND session_type = :sessionType AND transaction_type=:transactionType AND TRIM(epc) IN(:epcs) AND COALESCE(scan_qty,0)<=0")
  suspend fun updateFoundEpcs(topic: String,sessionType: String,transactionType: String,epcs: List<String>): Int
  
  

  @Transaction
  @Query("SELECT * FROM data_qty WHERE topic = :topic AND session_type=:sessionType AND transaction_type=:transactionType")
  fun getDataQtyList(topic: String,sessionType:String,transactionType:String): Flow<List<DataQtyEntity>>

  @Transaction
  @Query("SELECT COALESCE(SUM(scan_qty),0) == COALESCE(qty,0) FROM data_qty WHERE topic = :topic AND session_type=:sessionType AND transaction_type=:transactionType GROUP BY CASE WHEN :isArticleBased THEN article ELSE barcode END")
  fun getCheckedQtyList(topic: String,sessionType:String,transactionType:String,isArticleBased:Boolean): Flow<List<Boolean>>

  @Transaction
  @Query("SELECT TRIM(barcode) as barcode, COALESCE(SUM(scan_qty),0) as foundQty, COALESCE(qty,0) as totalQty FROM data_qty WHERE topic = :topic AND session_type=:sessionType AND transaction_type=:transactionType GROUP BY barcode")
  fun getBarcodeWiseList(topic: String,sessionType:String,transactionType:String): Flow<List<EanFoundQty>>

  @Transaction
  @Query("SELECT TRIM(article) as barcode, COALESCE(SUM(scan_qty),0) as foundQty, COALESCE(qty,0) as totalQty FROM data_qty WHERE topic = :topic AND session_type=:sessionType AND transaction_type=:transactionType GROUP BY article")
  fun getArticleWiseList(topic: String,sessionType:String,transactionType:String): Flow<List<EanFoundQty>>


  @Query("SELECT DISTINCT TRIM(article) FROM data_qty WHERE topic = :topic AND session_type=:sessionType AND transaction_type=:transactionType AND barcode=:barcode")
  fun getArticleFromBarcode(topic: String,sessionType:String,transactionType:String,barcode:String):String


  /**
   * Checks if flowId exists.
   */
  @Query("SELECT COALESCE(COUNT(*),0)>0 FROM data_qty WHERE topic = :topic AND session_type=:sessionType AND transaction_type=:transactionType LIMIT 1")
  fun hasData(topic: String,sessionType:String,transactionType:String): Boolean

  /**
   * Gets table size.
   */
  @Query("SELECT COALESCE(COUNT(*),0) FROM data_qty")
  fun getTableSize(): Int

  /**
   * Has data boolean.
   */
  @Query("SELECT COALESCE(COUNT(*),0)>0 FROM data_qty")
  fun hasData(): Boolean
}