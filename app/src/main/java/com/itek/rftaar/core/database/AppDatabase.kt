package com.itek.rftaar.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.itek.rftaar.core.common.utils.LogUtils
import com.itek.rftaar.data.dao.DataQtyDao
import com.itek.rftaar.data.dao.InOutConfigDao
import com.itek.rftaar.data.dao.MenuDao
import com.itek.rftaar.data.dao.MenuNotificationDao
import com.itek.rftaar.data.dao.ProductZoneDataDao
import com.itek.rftaar.data.dao.SearchLogDao
import com.itek.rftaar.data.dao.SessionListDao
import com.itek.rftaar.data.dao.TagInfoDao
import com.itek.rftaar.data.entity.DataQtyEntity
import com.itek.rftaar.data.entity.InOutConfigEntity
import com.itek.rftaar.data.entity.MenuEntity
import com.itek.rftaar.data.entity.MenuNotificationEntity
import com.itek.rftaar.data.entity.ProductZoneDataEntity
import com.itek.rftaar.data.entity.SearchLogEntity
import com.itek.rftaar.data.entity.SessionListEntity
import com.itek.rftaar.data.entity.TagInfoEntity
import java.util.concurrent.Executors


@Database(entities = [MenuEntity::class, TagInfoEntity::class, SessionListEntity::class, ProductZoneDataEntity::class, InOutConfigEntity::class, DataQtyEntity::class, SearchLogEntity::class, MenuNotificationEntity::class], version = 29, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun menuDao() : MenuDao
    abstract fun tagInfoDao() : TagInfoDao
    abstract fun sessionListDao() : SessionListDao
    abstract fun productZoneDataDao() : ProductZoneDataDao
    abstract fun inOutConfigDao() : InOutConfigDao
    abstract fun dataQtyDao() : DataQtyDao
    abstract fun searchLogDao() : SearchLogDao
    abstract fun menuNotificationDao() : MenuNotificationDao
    companion object {
        private const val DBNAME = "ITEK_APP_DB"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDbInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DBNAME
                )
                    .setQueryCallback({ sqlQuery, bindArgs ->
                        //LogUtils.showLog("RoomQuery", "SQL: $sqlQuery, Args: $bindArgs")
                    }, Executors.newSingleThreadExecutor())
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            LogUtils.showLog("RoomDB", "Database created")
                        }

                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            LogUtils.showLog("RoomDB", "Database opened")
                        }
                    })
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
