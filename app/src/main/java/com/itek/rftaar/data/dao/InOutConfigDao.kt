package com.itek.rftaar.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.itek.rftaar.data.entity.InOutConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InOutConfigDao {

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(sessionList: InOutConfigEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(inOutConfigEntity: List<InOutConfigEntity>)

  /**
   * Update.
   * @param InOutConfigEntity the inward outward config
   * */
  @Update
  suspend fun update(vararg inOutConfigEntity: InOutConfigEntity): Int

  @Update
  suspend fun updateAll(inOutConfigEntity: List<InOutConfigEntity>) : Int

  /**
   * Delete.
   * @param InOutConfigEntity the inward outward config
   */
  @Delete
  suspend fun delete(vararg sessionList: InOutConfigEntity): Int

  /**
   * Delete all.
   */
  @Query("DELETE FROM config")
  suspend fun deleteAll()

  /**
   * Delete all.
   */
  @Query("DELETE FROM config WHERE topic = :topic")
  suspend fun deleteAll(topic: String)

  @Transaction
  @Query("SELECT * FROM config WHERE topic = :topic")
  fun getConfigObjects(topic: String): Flow<List<InOutConfigEntity>>

  @Transaction
  @Query("SELECT * FROM config WHERE topic = :topic AND id = :flowId LIMIT 1")
  fun getConfigObject(topic: String,flowId: String): Flow<InOutConfigEntity?>

  /**
   * Checks if flowId exists.
   */
  @Query("SELECT COALESCE(COUNT(*),0)>0 FROM config WHERE topic = :topic AND id=:flowId LIMIT 1")
  fun hasData(topic: String,flowId: String): Boolean

  /**
   * Gets table size.
   */
  @Query("SELECT COALESCE(COUNT(*),0) FROM config")
  fun getTableSize(): Int

  /**
   * Has data boolean.
   */
  @Query("SELECT COALESCE(COUNT(*),0)>0 FROM config")
  fun hasData(): Boolean
}