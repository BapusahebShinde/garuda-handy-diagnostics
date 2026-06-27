package com.itek.rftaar.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.itek.rftaar.data.entity.SessionListEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionListDao {

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(sessionList: SessionListEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(sessionLists: List<SessionListEntity>)

  /**
   * Update.
   * @param SessionListEntity the sessionList
   * */
  @Update
  suspend fun update(vararg sessionList: SessionListEntity): Int

  @Update
  suspend fun updateAll(sessionLists: List<SessionListEntity>) : Int

  /**
   * Delete.
   * @param SessionListEntity the sessionList
   */
  @Delete
  suspend fun delete(vararg sessionList: SessionListEntity): Int

  /**
   * Delete all.
   */
  @Query("DELETE FROM session_list")
  suspend fun deleteAll()

  /**
   * Delete all.
   */
  @Query("DELETE FROM session_list WHERE topic = :topic AND session_type = :sessionType AND transaction_type=:transactionType")
  suspend fun deleteAll(topic: String,sessionType: String,transactionType: String)

  @Transaction
  @Query("SELECT TRIM(DISTINCT value) FROM session_list WHERE value IS NOT NULL AND LENGTH(TRIM(value))>0 AND topic = :topic AND session_type = :sessionType AND transaction_type=:transactionType ORDER BY pk_id DESC")
  fun getValues(topic: String,sessionType: String,transactionType: String): Flow<List<String>>

  /**
   * Checks if value exists.
   */
  @Query("SELECT COALESCE(COUNT(*),0)>0 FROM session_list WHERE topic = :topic AND session_type = :sessionType AND transaction_type=:transactionType AND value=:value LIMIT 1")
  fun hasValue(topic: String,sessionType: String,transactionType: String,value: String): Boolean

  @Query("SELECT COALESCE(COUNT(*),0)>0 FROM session_list WHERE topic = :topic AND session_type = :sessionType AND transaction_type=:transactionType LIMIT 1")
  fun hasData(topic: String,sessionType: String,transactionType: String): Boolean

  /**
   * Gets table size.
   */
  @Query("SELECT COALESCE(COUNT(*),0) FROM session_list")
  fun getTableSize(): Int

  /**
   * Has data boolean.
   */
  @Query("SELECT COALESCE(COUNT(*),0)>0 FROM session_list")
  fun hasData(): Boolean
}