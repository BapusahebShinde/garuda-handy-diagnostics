package com.itek.rftaar.data.dao

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.itek.rftaar.core.database.DataStoreManager
import com.itek.rftaar.data.entity.TagInfoEntity
import com.itek.rftaar.data.model.ActiveSessionQty
import com.itek.rftaar.data.model.EanFoundQty
import com.itek.rftaar.data.model.EanQty
import com.itek.rftaar.data.model.TagTime
import com.itek.rftaar.mqtt.constants.TopicConstants
import com.itek.rftaar.reader.constants.StatusConstants
import com.itek.rftaar.reader.epcwrapper.constants.BarcodeConstants
import kotlinx.coroutines.flow.Flow

@Dao
interface TagInfoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tagInfoEntity: TagInfoEntity):Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertInv(tagInfoEntity: TagInfoEntity):Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(tagInfoEntities: List<TagInfoEntity>):List<Long>

    /**
     * Update.
     *
     * @param TagInfoEntity the tag info entity
     */
    @Update
    suspend fun update(vararg tagInfoEntity: TagInfoEntity): Int

    @Update
    suspend fun updateAll(tagInfoEntities: List<TagInfoEntity>) : Int

    /**
     * Delete.
     *
     * @param tagInfoEntity the tag info entity
     */
    @Delete
    suspend fun delete(vararg tagInfoEntity: TagInfoEntity): Int

    /**
     * Delete all.
     */
    @Query("DELETE FROM tag_info")
    suspend fun deleteAll()

    @Query("UPDATE tag_info set is_deleted=1 WHERE session_type=:sessionType AND is_deleted=0")
    suspend fun deleteBySessionType(sessionType: String): Int

    @Query("UPDATE tag_info set is_deleted=1 WHERE session_type=:sessionType AND transaction_type=:transactionType AND is_deleted=0")
    suspend fun deleteBySessionTypeAndTransactionType(sessionType: String, transactionType: String): Int

    @Query("UPDATE tag_info set is_deleted=1 WHERE session_type=:sessionType AND transaction_type=:transactionType AND session_id=:sessionId AND is_deleted=0")
    suspend fun markNonUploadedBySession(sessionType: String, transactionType: String,sessionId:String): Int

    @Query("UPDATE tag_info set is_uploaded=0, is_deleted=1 WHERE session_type=:sessionType AND transaction_type=:transactionType AND is_deleted=0")
    suspend fun deleteBySessionTypeAndTransactionTypeAndMarkNotUploaded(sessionType: String, transactionType: String): Int

    @Query("UPDATE tag_info set is_deleted=1 WHERE topic=:topic AND session_type=:sessionType AND is_deleted=0")
    suspend fun deleteByTopicAndSessionType(topic: String,sessionType: String): Int

    @Query("DELETE FROM tag_info WHERE topic=:topic AND session_type=:sessionType AND transaction_type=:transactionType")
    suspend fun deleteActual(topic: String,sessionType: String,transactionType: String): Int

    @Query("DELETE FROM tag_info WHERE barcode = :barcode AND session_type = :sessionType AND transaction_type = :transactionType AND is_deleted = 0")
    suspend fun deleteBarcode(barcode: String, sessionType: String, transactionType: String)

    @Query("DELETE FROM tag_info WHERE is_uploaded=1 AND is_deleted=1")
    suspend fun removeUploadedAndDeletedFromBackground(): Int

    @Query("SELECT COUNT(*) FROM tag_info WHERE session_type = :sessionType AND transaction_type = :transactionType AND is_verified=1 AND is_deleted=0")
    fun getVerifyCount(sessionType: String,transactionType: String): LiveData<Int>

    @Query("SELECT COUNT(*) FROM tag_info WHERE session_type = :sessionType AND transaction_type = :transactionType AND is_deleted=0")
    fun getTotalCount(sessionType: String, transactionType: String): LiveData<Int>

    @Query("SELECT COUNT(*) FROM tag_info WHERE session_type = :sessionType AND transaction_type = :transactionType AND barcode = :barcode AND is_deleted=0")
    fun getTotalCount(sessionType: String, transactionType: String, barcode: String): LiveData<Int>

    @Query("SELECT COUNT(*) FROM tag_info WHERE session_type = :sessionType AND transaction_type = :transactionType AND is_deleted=0 AND barcode IS NOT NULL AND LENGTH(TRIM(barcode))>0 AND barcode NOT IN('"+BarcodeConstants.NON_ENCODED+"','"+BarcodeConstants.UNKNOWN+"','"+BarcodeConstants.ALIEN+"') AND (LENGTH(:vendorSerial)<=0 OR (serial IS NOT NULL AND LENGTH(TRIM(serial))>0 AND serial LIKE :vendorSerial||'%'))")
    fun getValidCount(sessionType: String,transactionType: String,vendorSerial: String=if(!DataStoreManager.readFromPreferences("enableAlienTags",false) || !DataStoreManager.readFromPreferences("enableVendorSerialCode",false)) "" else DataStoreManager.readFromPreferences("vendorSerialCode","")): LiveData<Int>

    @Query("SELECT COUNT(*) FROM tag_info WHERE session_type = :sessionType AND transaction_type = :transactionType AND is_deleted=0 AND ((barcode IS NULL OR LENGTH(TRIM(barcode))<=0 OR barcode IN('"+BarcodeConstants.NON_ENCODED+"','"+BarcodeConstants.UNKNOWN+"','"+BarcodeConstants.ALIEN+"')) OR (LENGTH(:vendorSerial)<=0 OR (serial IS NULL AND LENGTH(TRIM(serial))>0 AND serial NOT LIKE :vendorSerial||'%')))")
    fun getInvalidCount(sessionType: String,transactionType: String,vendorSerial: String=if(!DataStoreManager.readFromPreferences("enableAlienTags",false) || !DataStoreManager.readFromPreferences("enableVendorSerialCode",false)) "-" else DataStoreManager.readFromPreferences("vendorSerialCode","")): LiveData<Int>

    @Query("SELECT COUNT(*) FROM tag_info WHERE session_type = :sessionType AND transaction_type = :transactionType AND is_deleted=0 AND barcode IS NOT NULL AND LENGTH(TRIM(barcode))>0 AND barcode NOT IN('"+BarcodeConstants.NON_ENCODED+"','"+BarcodeConstants.UNKNOWN+"','"+BarcodeConstants.ALIEN+"')  AND barcode NOT IN(:listExpectedBarcodes) AND (LENGTH(:vendorSerial)<=0 OR (serial IS NOT NULL AND LENGTH(TRIM(serial))>0 AND serial LIKE :vendorSerial||'%'))")
    fun getExtraCount(sessionType: String, transactionType: String, listExpectedBarcodes: List<String>,vendorSerial: String=if(!DataStoreManager.readFromPreferences("enableAlienTags",false) || !DataStoreManager.readFromPreferences("enableVendorSerialCode",false)) "" else DataStoreManager.readFromPreferences("vendorSerialCode","")): LiveData<Int>

    @Query("SELECT COUNT(*) FROM tag_info WHERE session_type = :sessionType AND transaction_type = :transactionType AND is_deleted=0 AND (barcode IS NULL OR LENGTH(TRIM(barcode))<=0 OR barcode IN('"+BarcodeConstants.NON_ENCODED+"','"+BarcodeConstants.UNKNOWN+"'))")
    fun getUnencodedCount(sessionType: String,transactionType: String): LiveData<Int>

    @Query("SELECT COUNT(*) FROM tag_info WHERE session_type = :sessionType AND transaction_type = :transactionType AND is_deleted=0 AND barcode IS NOT NULL AND LENGTH(TRIM(barcode))>0 AND barcode NOT IN('"+BarcodeConstants.NON_ENCODED+"','"+BarcodeConstants.UNKNOWN+"') AND LENGTH(:vendorSerial)>0 AND serial IS NOT NULL AND LENGTH(TRIM(serial))>0 AND serial NOT LIKE :vendorSerial||'%'")
    fun getAlienCount(sessionType: String,transactionType: String,vendorSerial:String = if(!DataStoreManager.readFromPreferences("enableAlienTags",false) || !DataStoreManager.readFromPreferences("enableVendorSerialCode",false)) "" else DataStoreManager.readFromPreferences("vendorSerialCode","")): LiveData<Int>

    @Query("SELECT * FROM tag_info WHERE session_type = :sessionType AND transaction_type = :transactionType AND is_deleted=0 AND (barcode IS NULL OR LENGTH(TRIM(barcode))<=0 OR barcode IN('"+BarcodeConstants.NON_ENCODED+"','"+BarcodeConstants.UNKNOWN+"'))")
    fun getUnencodedBarcodeList(sessionType: String,transactionType: String): LiveData<List<TagInfoEntity>>

    @Query("SELECT * FROM tag_info WHERE session_type = :sessionType AND transaction_type = :transactionType AND is_deleted=0 AND barcode IS NOT NULL AND LENGTH(TRIM(barcode))>0 ORDER BY is_found asc")
    fun getBarcodeList(sessionType: String,transactionType: String): LiveData<List<TagInfoEntity>>

   /*@Query("SELECT TRIM(tag_info.barcode) as barcode, COUNT(tag_info.epc) as foundQty, COALESCE(data_qty.qty,0) as totalQty FROM tag_info LEFT JOIN data_qty ON tag_info.topic = data_qty.topic AND tag_info.session_type = data_qty.session_type AND tag_info.transaction_type = data_qty.transaction_type AND tag_info.barcode=data_qty.barcode " +
      "WHERE tag_info.session_type = :sessionType AND tag_info.transaction_type = :transactionType AND tag_info.is_deleted=0 AND tag_info.barcode IS NOT NULL AND LENGTH(TRIM(tag_info.barcode))>0 AND (tag_info.barcode IN(:listExpectedBarcodes) OR tag_info.barcode NOT IN(:listExpectedBarcodes)) GROUP BY tag_info.barcode")
    fun getBarcodeWiseList(sessionType: String,transactionType: String,listExpectedBarcodes: List<String>): LiveData<List<EanFoundQty>>*/

    @Query("SELECT * FROM (SELECT TRIM(barcode) as barcode, SUM(COALESCE(scan_qty,0)) as foundQty, MAX(COALESCE(qty,0)) as totalQty FROM data_qty WHERE data_qty.session_type = :sessionType AND data_qty.transaction_type = :transactionType AND barcode IS NOT NULL AND LENGTH(TRIM(barcode))>0 GROUP BY 1"+ " UNION "+
    "SELECT TRIM(barcode) as barcode, COUNT(epc) as foundQty, 0 as totalQty FROM tag_info WHERE session_type = :sessionType AND transaction_type = :transactionType AND is_deleted=0 AND barcode IS NOT NULL AND LENGTH(TRIM(barcode))>0 AND barcode NOT IN(:listExpectedBarcodes) GROUP BY 1) ORDER BY CASE WHEN totalQty>0 THEN 1 ELSE 0 END DESC, barcode ASC")
    fun getBarcodeWiseList(sessionType: String,transactionType: String,listExpectedBarcodes: List<String>): LiveData<List<EanFoundQty>>

    @Query("SELECT * FROM (SELECT TRIM(article) as barcode, SUM(COALESCE(scan_qty,0)) as foundQty, MAX(COALESCE(qty,0)) as totalQty FROM data_qty WHERE data_qty.session_type = :sessionType AND data_qty.transaction_type = :transactionType AND article IS NOT NULL AND LENGTH(TRIM(article))>0 GROUP BY 1"+ " UNION "+
    "SELECT (CASE WHEN TRIM(barcode) = '"+BarcodeConstants.NON_ENCODED+"' THEN '"+BarcodeConstants.NON_ENCODED+"' ELSE '"+BarcodeConstants.UNKNOWN+"' END) as barcode, COUNT(epc) as foundQty, 0 as totalQty FROM tag_info WHERE session_type = :sessionType AND transaction_type = :transactionType AND is_deleted=0 AND barcode IS NOT NULL AND LENGTH(TRIM(barcode))>0 AND barcode NOT IN(:listExpectedBarcodes) GROUP BY 1) ORDER BY (CASE WHEN 3>0 THEN 1 ELSE 0 END) DESC, 1 ASC")
    fun getArticleWiseList(sessionType: String,transactionType: String,listExpectedBarcodes: List<String>): LiveData<List<EanFoundQty>>

   /* @Query("SELECT TRIM(article) as barcode, COUNT(epc) as foundQty FROM tag_info WHERE session_type = :sessionType AND transaction_type = :transactionType AND is_deleted=0 AND article IS NOT NULL AND LENGTH(TRIM(article))>0 GROUP BY article")
    fun getArticleWiseList(sessionType: String,transactionType: String): LiveData<List<EanFoundQty>>*/

    @Query("SELECT COUNT(*) FROM tag_info WHERE session_type = :sessionType AND transaction_type = :transactionType AND is_verified=1 AND is_deleted=0")
    fun getVerifyPendingCount(sessionType: String,transactionType: String): LiveData<Int>

    @Query("SELECT SUM(is_verified)==SUM(is_tag_write_done) FROM tag_info WHERE session_type = :sessionType AND transaction_type = :transactionType AND is_tag_write_done=1 AND is_deleted=0")
    fun isAllVerified(sessionType: String,transactionType: String): LiveData<Boolean?>

    @Query("SELECT (tag_verify_status='"+StatusConstants.PENDING+"' OR tag_verify_status='"+StatusConstants.VERIFIED+"') FROM tag_info WHERE session_type = :sessionType AND transaction_type = :transactionType AND is_tag_write_done=1 AND is_deleted=0")
    fun isAllPendingOrVerified(sessionType: String,transactionType: String): LiveData<Boolean>

    @Query("SELECT COUNT(*) FROM tag_info WHERE session_type = :sessionType AND transaction_type = :transactionType AND is_tag_write_done=1 AND is_deleted=0")
    fun getTagWriteCount(sessionType: String,transactionType: String): LiveData<Int>

    @Query("SELECT COUNT(*) FROM tag_info WHERE session_type = :sessionType AND transaction_type = :transactionType AND barcode = :barcode AND is_tag_write_done=1 AND is_deleted=0")
    fun getTagWriteCount(sessionType: String,transactionType: String,barcode: String): LiveData<Int>

    @Query("SELECT COUNT(*) FROM tag_info WHERE session_type = :sessionType AND transaction_type = :transactionType AND is_tag_write_done=1 AND is_uploaded=1 AND is_deleted=0")
    fun getUploadedTagWriteCount(sessionType: String,transactionType: String): LiveData<Int>

    @Query("SELECT COUNT(*) FROM tag_info WHERE session_type = :sessionType AND transaction_type = :transactionType AND is_tag_write_done=1 AND is_verified=1 AND is_deleted=0")
    fun getTagWriteVerifiedCount(sessionType: String,transactionType: String): LiveData<Int>

    @Query("SELECT COUNT(*) FROM tag_info WHERE session_type = :sessionType AND transaction_type = :transactionType AND is_found=1 AND is_deleted=0")
    fun getFoundCount(sessionType: String,transactionType: String): LiveData<Int>

    @Query("SELECT COUNT(*) FROM tag_info WHERE topic = :topic AND session_type = :sessionType AND transaction_type = :transactionType AND barcode = :barcode AND is_found=1 AND is_deleted=0")
    fun getFoundCount(topic:String,sessionType: String,transactionType: String,barcode: String): LiveData<Int>

    @Query("SELECT COUNT(*) FROM tag_info WHERE topic = :topic AND session_type = :sessionType AND transaction_type = :transactionType AND barcode = :barcode AND is_found=1 AND is_deleted=0")
    fun getFoundCount1(topic:String,sessionType: String,transactionType: String,barcode: String): Int

    @Query("SELECT COUNT(*) FROM tag_info WHERE session_type = :sessionType AND transaction_type = :transactionType AND is_uploaded=1 AND is_deleted=0")
    fun getUploadedCount(sessionType: String,transactionType: String): LiveData<Int>

    @Query("UPDATE tag_info set is_found=1 WHERE session_type = :sessionType AND transaction_type = :transactionType AND session_id=:sessionId AND tid=:tid AND epc=:epc AND barcode=:barcode AND is_deleted=0 AND is_found=0")
    suspend fun updateFound(sessionType: String, transactionType:String,sessionId: String, tid:String, epc:String, barcode:String): Int

    @Query("UPDATE tag_info set is_uploaded=1 WHERE is_uploaded<=0 AND topic=:topic AND id=:id AND epc=:epc AND new_epc=:newEpc AND tid=:tid AND session_id=:sessionId")
    suspend fun updateUploadedTagWriteForBackgroundUpload(topic: String, id:String, epc:String, newEpc:String, tid:String, sessionId: String):Int

    @Query("UPDATE tag_info set is_uploaded=1 WHERE is_uploaded<=0 AND topic=:topic AND transaction_type=:transactionType AND epc=:epc AND tid=:tid AND session_id=:sessionId")
    suspend fun updateUploadedInvForBackgroundUpload(topic: String, transactionType:String, epc:String, tid:String,sessionId: String):Int

    @Query("UPDATE tag_info set is_uploaded=1 WHERE is_uploaded<=0 AND topic=:topic AND transaction_type=:transactionType AND epc IN (:epcs) AND session_id=:sessionId")
    suspend fun updateUploadedInvForBackgroundUpload(topic: String, transactionType:String, epcs:List<String>, sessionId: String):Int

    @Query("UPDATE tag_info set is_uploaded=1 WHERE is_uploaded<=0 AND topic=:topic AND transaction_type=:transactionType AND session_id=:sessionId AND pk_id<=:invMaxId")
    suspend fun updateUploadedInvForBackgroundUpload(topic: String, transactionType:String, sessionId: String, invMaxId:Int):Int

/*    @Query("SELECT session_id||'-'||topic||'-'||session_type||'-'||transaction_type as barcode,COALESCE(COUNT(is_uploaded),0) as foundQty,COALESCE(COUNT(*),0) as uploadedQty FROM tag_info GROUP BY 1")
    fun getSessionWiseUploadedCounts(): Flow<List<EanFoundQty>>*/

    @Query("""
    SELECT 
        TRIM(session_id || ';' || topic || ';' || session_type || ';' || transaction_type) AS barcode,
        COUNT(*) AS foundQty,
        COALESCE(SUM(CASE WHEN is_uploaded = 1 THEN 1 ELSE 0 END), 0) AS totalQty
    FROM tag_info
    GROUP BY session_id, topic, session_type, transaction_type
""")
    fun getSessionWiseUploadedCounts(): Flow<List<EanFoundQty>>

    @Query("UPDATE tag_info set is_verified=1,tag_verify_status='"+StatusConstants.VERIFIED+"' WHERE session_type = :sessionType AND transaction_type = :transactionType AND new_epc=:newEpc AND tid=:tid AND is_tag_write_done AND (is_verified<=0 OR tag_verify_status!='"+ StatusConstants.VERIFIED+"') AND is_deleted=0")
    suspend fun updateVerified(sessionType: String,transactionType:String,newEpc:String,tid:String):Int

    @Query("SELECT * FROM tag_info where is_uploaded<=0 AND is_deleted=0")
    fun getNonUploadedForBackgroundUpload():List<TagInfoEntity>

    @Query("SELECT * FROM tag_info where is_uploaded<=0 AND topic=:topic AND session_type=:sessionType AND transaction_type=:transactionType AND session_id=:sessionId LIMIT :limit OFFSET :offset")
    fun getNonUploadedForBackgroundUpload(topic: String,sessionType: String,transactionType: String,sessionId: String,offset: Int=0,limit: Int=1000):List<TagInfoEntity>

    @Transaction
    @Query("SELECT TRIM(barcode) as barcode, COALESCE(COUNT(barcode),0) as qty FROM tag_info WHERE barcode IS NOT NULL AND LENGTH(TRIM(barcode))>0 AND topic=:topic AND session_type = :sessionType AND transaction_type = :transactionType AND is_deleted=0 GROUP BY TRIM(barcode) ORDER BY pk_id DESC")
    fun getBarcodeQty(topic: String,sessionType: String,transactionType: String): List<EanQty>

    @Query("SELECT * FROM tag_info where /*is_uploaded<=0 AND*/ topic=:topic AND session_type=:sessionType AND transaction_type=:transactionType AND TRIM(barcode)=:barcode AND is_deleted=0")
    fun getAllAgainstBarcode(topic: String,sessionType: String,transactionType: String,barcode: String):List<TagInfoEntity>

    @Query("SELECT * FROM tag_info where /*is_uploaded<=0 AND*/ topic=:topic AND session_type=:sessionType AND transaction_type=:transactionType AND TRIM(barcode)=:barcode AND is_tag_write_done=0 AND is_deleted=0")
    fun getAllNonWrittenTagsAgainstBarcode(topic: String,sessionType: String,transactionType: String,barcode: String):Flow<List<TagInfoEntity>>

    @Query("SELECT * FROM tag_info where /*is_uploaded<=0 AND*/ topic=:topic AND session_type=:sessionType AND transaction_type=:transactionType AND TRIM(barcode)=:barcode AND is_tag_write_done=0 AND is_deleted=0")
    fun getAllNonWrittenTagsAgainstBarcode1(topic: String,sessionType: String,transactionType: String,barcode: String):List<TagInfoEntity>

    @Query("SELECT * FROM tag_info where /*is_uploaded<=0 AND*/ topic=:topic AND session_type=:sessionType AND transaction_type=:transactionType AND TRIM(barcode)=:barcode AND is_deleted=0")
    fun getAllForBarcode(topic: String,sessionType: String,transactionType: String,barcode: String):Flow<List<TagInfoEntity>>

    @Query("SELECT * FROM tag_info where is_uploaded<=0 AND topic=:topic AND session_type=:sessionType AND transaction_type=:transactionType AND TRIM(barcode)=:barcode AND is_tag_write_done=1 AND is_deleted=0")
    fun getAllWrittenTagsAgainstBarcode(topic: String,sessionType: String,transactionType: String,barcode: String):Flow<List<TagInfoEntity>>

    @Query("SELECT * FROM tag_info where is_uploaded<=0 AND topic=:topic AND session_type=:sessionType AND transaction_type=:transactionType AND TRIM(barcode)=:barcode AND is_tag_write_done=1 AND is_deleted=0")
    fun getAllWrittenTagsAgainstBarcode1(topic: String,sessionType: String,transactionType: String,barcode: String):List<TagInfoEntity>

    @Query("SELECT * FROM tag_info where /*is_uploaded=0 AND*/ is_tag_write_done=0 AND topic=:topic AND session_type=:sessionType AND transaction_type=:transactionType AND is_deleted=0")
    fun getAllPreEncodedTags(topic: String,sessionType: String,transactionType: String):List<TagInfoEntity>

    @Query("SELECT DISTINCT session_type FROM tag_info where is_uploaded<=0 AND topic=:topic")
    fun getNonUploadedSessionTypesForBackgroundUpload(topic: String):List<String>

    @Query("SELECT DISTINCT transaction_type FROM tag_info where is_uploaded<=0 AND topic=:topic AND session_type=:sessionType")
    fun getNonUploadedTransactionTypesForBackgroundUpload(topic: String,sessionType: String):List<String>

    @Query("SELECT DISTINCT session_id FROM tag_info where is_uploaded<=0 AND topic=:topic AND session_type=:sessionType AND transaction_type=:transactionType")
    fun getNonUploadedSessionIdsForBackgroundUpload(topic: String,sessionType: String,transactionType:String):List<String>

    @Query("SELECT DISTINCT topic FROM tag_info where is_uploaded<=0 GROUP BY topic")
    fun getNonUploadedTopicsForBackgroundUpload():List<String>

    @Query("SELECT * FROM tag_info WHERE session_type = :sessionType AND transaction_type=:transactionType AND  tid=:tid AND is_deleted=0")
    fun getByTid(sessionType: String,transactionType: String,tid: String): List<TagInfoEntity>

    @Query("SELECT * FROM tag_info WHERE pk_id=:tagInfoId AND is_deleted=0")
    fun getById(tagInfoId:Int): TagInfoEntity

    @Transaction
    @Query("SELECT pk_id as tagInfoId, TRIM(barcode) as barcode, TRIM(COALESCE(new_epc,epc)) AS epc, TRIM(tid) AS tid, insert_time as timeStamp, tag_verify_status as status FROM tag_info WHERE barcode IS NOT NULL AND LENGTH(TRIM(barcode))>0 AND session_type = :sessionType AND transaction_type = :transactionType AND (:searchQuery IS NULL OR LENGTH(COALESCE(:searchQuery,'')<=0) OR TRIM(barcode) LIKE '%'||:searchQuery||'%') AND is_tag_write_done=1 AND is_deleted=0 ORDER BY pk_id DESC")
    fun getEncodedTagTime(sessionType: String,transactionType: String,searchQuery:String): Flow<List<TagTime>>

    @Transaction
    @Query("SELECT pk_id as tagInfoId, TRIM(barcode) AS barcode, TRIM(COALESCE(new_epc,epc)) AS epc, TRIM(tid) AS tid, insert_time AS timeStamp FROM tag_info WHERE barcode IS NOT NULL AND LENGTH(TRIM(barcode)) > 0 AND session_type = :sessionType AND transaction_type = :transactionType AND is_tag_write_done = 1 AND is_deleted = 0 ORDER BY pk_id DESC LIMIT 1 ")
    fun getLatestEncodedTag(sessionType: String, transactionType: String): Flow<TagTime?>


    @Transaction
    @Query("SELECT TRIM(DISTINCT barcode) FROM tag_info WHERE barcode IS NOT NULL AND LENGTH(TRIM(barcode))>0 AND session_type = :sessionType AND transaction_type = :transactionType AND is_tag_write_done=1 AND is_deleted=0 ORDER BY pk_id DESC")
    fun getEncodedBarcodes(sessionType: String,transactionType: String): Flow<List<String>>

    /**
     * Checks if epc exists.
     */
    @Query("SELECT COALESCE(COUNT(*),0)>0 FROM tag_info WHERE session_type = :sessionType AND transaction_type = :transactionType AND epc=:epc AND is_deleted=0 LIMIT 1")
    fun hasEpc(sessionType: String,transactionType:String,epc: String): Boolean

    /**
     * Checks if tid exists.
     */
    @Query("SELECT COALESCE(COUNT(*),0)>0 FROM tag_info WHERE session_type = :sessionType AND transaction_type = :transactionType AND tid=:tid AND is_deleted=0 LIMIT 1")
    fun hasTid(sessionType: String,transactionType:String, tid: String): Boolean

    @Query("SELECT COALESCE(COUNT(*),0)>0 FROM tag_info WHERE topic = :topic AND transaction_type = :transactionType AND barcode = :barcode AND tid=:tid AND is_deleted=0 LIMIT 1")
    fun hasTid(topic: String, transactionType:String, barcode:String, tid: String): Boolean

    @Query("SELECT tag_info.topic as topic, menus.label as menuName, menus.parent_code as parentCode, menus.path as menuPath, tag_info.session_type as menuCode, tag_info.transaction_type as transactionType , COALESCE(COUNT(tag_info.session_type),0) as scanCount, 0 as foundCount FROM tag_info JOIN menus ON tag_info.session_type = menus.code WHERE tag_info.topic=:topic AND (tag_info.topic NOT IN('"+ TopicConstants.ENCODE+"','"+ TopicConstants.DECODE+"') OR tag_info.is_tag_write_done=1) AND tag_info.is_deleted=0 GROUP BY tag_info.session_type")
    fun getTopicWiseActiveSessionList(topic: String):Flow<List<ActiveSessionQty>>

    @Query("SELECT COALESCE(COUNT(*),0)>0 FROM tag_info JOIN menus ON tag_info.session_type = menus.code WHERE (tag_info.session_type = :menuCode OR menus.path LIKE '%'||:menuCode||'%') AND (tag_info.topic NOT IN('"+ TopicConstants.ENCODE+"','"+ TopicConstants.DECODE+"') OR tag_info.is_tag_write_done=1) AND tag_info.is_deleted=0")
    fun hasActiveMenuSession(menuCode:String):LiveData<Boolean>

    /**
     * Checks if epc and tid exists.
     */
    @Query("SELECT COALESCE(COUNT(*),0)>0 FROM tag_info WHERE session_type = :sessionType AND transaction_type = :transactionType AND tid=:tid AND epc=:epc AND is_deleted=0 LIMIT 1")
    fun isTagExist(sessionType: String,transactionType:String, tid: String, epc: String): Boolean

    /**
     * Gets table size.
     */
    @Query("SELECT COALESCE(COUNT(*),0) FROM tag_info WHERE is_deleted=0")
    fun getTableSize(): Int
    /**
     * Has data boolean.
     */
    @Query("SELECT COALESCE(COUNT(*),0)>0 FROM tag_info WHERE is_deleted=0")
    fun hasData(): Boolean
}