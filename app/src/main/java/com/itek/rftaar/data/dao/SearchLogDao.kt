package com.itek.rftaar.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.itek.rftaar.data.entity.SearchLogEntity

@Dao
interface SearchLogDao {

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  suspend fun insert(searchLogEntity: SearchLogEntity)

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  suspend fun insertAll(dataQtyEntities: List<SearchLogEntity>)

  /**
   * Update.
   * @param searchLogEntity the search log
   * */
  @Update
  suspend fun update(vararg searchLogEntity: SearchLogEntity): Int

  @Update
  suspend fun updateAll(dataQtyEntities: List<SearchLogEntity>) : Int

  /**
   * Delete.
   * @param searchLogEntity the search log
   */
  @Delete
  suspend fun delete(vararg sessionList: SearchLogEntity): Int

  /**
   * Delete all.
   */
  @Query("DELETE FROM search_log")
  suspend fun deleteAll()

  /**
   * Delete all.
   */
  @Query("DELETE FROM search_log WHERE topic = :topic AND session_type=:sessionType AND transaction_type=:transactionType")
  suspend fun deleteAll(topic: String,sessionType:String,transactionType:String)

  @Query("DELETE FROM search_log WHERE is_uploaded=1")
  suspend fun removeUploadedAndDeletedFromBackground(): Int

  /**
   * Update Uploaded
   */
  @Query("UPDATE search_log set is_uploaded=1 WHERE topic=:topic AND search_type=:searchType AND search_value=:searchValue AND start_time=:startTime AND end_time=:endTime")
  suspend fun updateUploadedForBackgroundUpload(topic: String, searchType: String, searchValue:String, startTime: String,endTime: String):Int

  @Query("UPDATE search_log set is_uploaded=1 WHERE pk_id=:id")
  suspend fun updateUploadedForBackgroundUpload(id: Int):Int

  @Query("UPDATE search_log set is_uploaded=1 WHERE topic=:topic AND search_type=:searchType AND search_value=:searchValue AND pk_id=:id")
  suspend fun updateUploadedForBackgroundUpload(topic: String, searchType: String, searchValue:String,id: Int):Int

  @Transaction
  @Query("SELECT * FROM search_log WHERE is_uploaded=0")
  fun getNonUploadedForBackgroundUpload(): List<SearchLogEntity>


  /**
   * Checks if flowId exists.
   */
  @Query("SELECT COALESCE(COUNT(*),0)>0 FROM search_log WHERE topic = :topic AND session_type=:sessionType AND transaction_type=:transactionType LIMIT 1")
  fun hasData(topic: String,sessionType:String,transactionType:String): Boolean

  /**
   * Gets table size.
   */
  @Query("SELECT COALESCE(COUNT(*),0) FROM search_log")
  fun getTableSize(): Int

  /**
   * Has data boolean.
   */
  @Query("SELECT COALESCE(COUNT(*),0)>0 FROM search_log")
  fun hasData(): Boolean
}