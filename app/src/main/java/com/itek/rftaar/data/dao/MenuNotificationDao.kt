package com.itek.rftaar.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.itek.rftaar.data.entity.MenuNotificationEntity
import com.itek.rftaar.data.model.MenuNotificationWithCount
import kotlinx.coroutines.flow.Flow

@Dao
interface MenuNotificationDao {

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(menuNotificationEntity: MenuNotificationEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(menuNotificationEntities: List<MenuNotificationEntity>)

  /**
   * Update.
   * @param menuNotificationEntity the data qty
   * */
  @Update
  suspend fun update(vararg menuNotificationEntity: MenuNotificationEntity): Int

  @Update
  suspend fun updateAll(menuNotificationEntities: List<MenuNotificationEntity>) : Int

  /**
   * Delete.
   * @param menuNotificationEntity the data qty
   */
  @Delete
  suspend fun delete(vararg sessionList: MenuNotificationEntity): Int

  /**
   * Delete all.
   */
  @Query("DELETE FROM menu_notifications")
  suspend fun deleteAll()

  /**
   * Delete all.
   */
  @Query("DELETE FROM menu_notifications WHERE valid_till_date < :currentDate")
  suspend fun deleteExpired(currentDate:String)


  @Query("UPDATE menu_notifications set is_read=1 WHERE TRIM(type) = :type AND (:userId IS NULL OR LENGTH(:userId)<=0 OR TRIM(user_id)=:userId) AND is_read=0")
  suspend fun updateReadCount(type:String,userId: String) : Int


  @Transaction
  @Query("SELECT * FROM menu_notifications WHERE TRIM(type) = :type AND (:userId IS NULL OR LENGTH(:userId)<=0 OR TRIM(user_id)=:userId)")
  fun getNotificationList(type:String,userId: String): Flow<List<MenuNotificationEntity>>


    //@Query("SELECT * FROM notifications GROUP BY type ORDER BY type")
  @Transaction
  @Query("SELECT t1.*, t2.qty AS unreadCount FROM menu_notifications AS t1 JOIN (SELECT type, COALESCE(MAX(pk_id),0)  AS pk_id, (COALESCE(COUNT(*),0)-COALESCE(SUM(is_read),0)) AS qty FROM menu_notifications WHERE (:userId IS NULL OR LENGTH(:userId)<=0 OR TRIM(user_id)=:userId) GROUP BY type) AS t2 ON t1.pk_id = t2.pk_id AND t1.type = t2.type")
  fun getNotificationTypesList(userId: String): Flow<List<MenuNotificationWithCount>>

  @Transaction
  @Query("SELECT COALESCE(COUNT(*),0) FROM menu_notifications WHERE (:userId IS NULL OR LENGTH(:userId)<=0 OR TRIM(user_id)=:userId) AND is_read=0")
  fun getTotalUnreadCount(userId: String):Flow<Int>

  /**
   * Checks if flowId exists.
   */
  @Query("SELECT COALESCE(COUNT(*),0)>0 FROM menu_notifications WHERE type_id = :typeId LIMIT 1")
  fun hasData(typeId: String): Boolean

  /**
   * Gets table size.
   */
  @Query("SELECT COALESCE(COUNT(*),0) FROM menu_notifications")
  fun getTableSize(): Int

  /**
   * Has data boolean.
   */
  @Query("SELECT COALESCE(COUNT(*),0)>0 FROM menu_notifications")
  fun hasData(): Boolean
}