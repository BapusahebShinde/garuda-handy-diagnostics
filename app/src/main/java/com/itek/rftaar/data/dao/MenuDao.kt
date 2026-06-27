package com.itek.rftaar.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.itek.rftaar.data.entity.MenuEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MenuDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(menu: MenuEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(menus: List<MenuEntity>)
    /**
     * Update.
     *
     * @param MenuEntitys the menu models
     */
    @Update
    suspend fun update(vararg MenuEntitys: MenuEntity): Int

    /**
     * Delete.
     *
     * @param MenuEntitys the menu models
     */
    @Delete
    suspend fun delete(vararg MenuEntitys: MenuEntity): Int

    /**
     * Delete all.
     */
    @Query("DELETE FROM menus")
    suspend fun deleteAll()

    /**
     * Gets all home menus.
     */
    @Query("SELECT * FROM menus WHERE parent_code IS NULL OR LENGTH(TRIM(parent_code))<=0 AND is_active>0 ORDER BY sequence")
    fun getAllHomeMenus(): Flow<List<MenuEntity>>

    /*@Query("SELECT * FROM menus WHERE parent_code IS NULL OR LENGTH(TRIM(parent_code))<=0 AND is_active>0 ORDER BY sequence")
    fun getAllHomeMenus1(): List<MenuEntity>*/

    /**
     * Gets sub menus.
     */
    @Query("SELECT * FROM menus WHERE parent_code = :parentCode AND is_active>0 ORDER BY sequence")
    fun getSubMenus(parentCode: String): Flow<List<MenuEntity>>

    /**
     * Gets parent.
     */

    @Query("SELECT * FROM menus WHERE parent_code IS NOT NULL AND LENGTH(TRIM(parent_code))> 0 AND code=:parentCode AND is_active>0 LIMIT 1")
    fun getParent(parentCode: Int): MenuEntity?

    /**
     * Gets menu by code.
     */
    @Query("SELECT * FROM menus WHERE code=:code AND is_active>0 LIMIT 1")
    fun getMenuByCode(code: String): MenuEntity?

    /**
     * Checks if menu exists.
     */
    @Query("SELECT COALESCE(COUNT(*),0)>0 FROM menus WHERE code=:code AND is_active>0 LIMIT 1")
    fun hasMenu(code: String): Boolean

    /**
     * Gets menus by codes.
     */
    @Transaction
    @Query("SELECT * FROM menus WHERE code IN(:codes) AND is_active>0 ORDER BY sequence")
    fun getMenusByCodes(codes: Array<String>): Flow<List<MenuEntity>>

    /*@Transaction
    @Query("SELECT * FROM menus WHERE code IN(:codes) AND is_active>0 ORDER BY sequence")
    fun getMenusByCodes1(codes: Array<String>): List<MenuEntity>*/

    /**
     * Gets table size.
     */
    @Query("SELECT COALESCE(COUNT(*),0) FROM menus")
    fun getTableSize(): Int
    /**
     * Has data boolean.
     */
    @Query("SELECT COALESCE(COUNT(*),0)>0 FROM menus WHERE is_active>0")
    fun hasData(): Boolean
}