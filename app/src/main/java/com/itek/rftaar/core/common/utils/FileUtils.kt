package com.itek.rftaar.core.common.utils

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Build
import androidx.core.content.FileProvider
import com.itek.rftaar.core.common.constants.DateFormatUtils
import com.itek.rftaar.core.database.DataStoreManager
import com.itek.rftaar.data.model.BarcodeRefMap
import com.itek.rftaar.utils.CommonUtils.chkNull
import com.opencsv.CSVWriter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.FileWriter
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream


@SuppressLint("StaticFieldLeak")
object FileUtils {
  private lateinit var context: Context
  private var isDebugApp: Boolean=false

  var fileCount: Int = 5
  var fileSize: Int = 5 // MB
  var maxFileSize: Long = fileSize * 1024 * 1024L

  fun init(context: Context) {
    this.context = context
    try {
      isDebugApp = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }catch (e:Exception){}

    /*if (isDebugApp) {
      createLogFolders()
    }*/
  }

  fun writeCsvFile(folderName: String="",fileName: String="",barcodeRefMap: BarcodeRefMap) {
    CoroutineScope(Dispatchers.IO).launch{
    // Define the file path (e.g., in app-specific external storage)
    // For modern Android, consider using getExternalFilesDir(null) or internal storage.
    // The following path uses a deprecated method but is a common example for older code:
    val folder = getBaseLogsDir(folderName)//File(context.getExternalCacheDir(),folderName)
    folder.mkdirs()
    LogUtils.showLog("writeCSV_folder",folder.absolutePath)
    val file  = File(folder,chkNull(fileName,"MyCsvFile")+".csv")
    //val file = File(csv)
    LogUtils.showLog("writeCSV_file",file.absolutePath)
    val isFileExist = file.exists() && file.length()>0
    LogUtils.showLog("writeCSV_isFileExist",""+isFileExist)
    try {
      // Create a FileWriter and wrap it in a CSVWriter
      val writer = CSVWriter(FileWriter(file,isFileExist))

      // Data to be written (example data)
      val data: MutableList<Array<String>> = ArrayList<Array<String>>()
      if(!isFileExist) data.add(arrayOf<String>("Old "+DataStoreManager.getBarcodeLabel(), "New "+DataStoreManager.getBarcodeLabel()))
      data.add(arrayOf<String>(barcodeRefMap.oldBarcode,barcodeRefMap.newBarcode))

      LogUtils.showLog("writeCSV_data",data.toString())
      // Write all data at once
      writer.writeAll(data)


      // Ensure the writer is closed to flush all data
      writer.close()


      // Handle success (e.g., show a Toast or log)
      LogUtils.showLog("writeCSV_CSV", "CSV file successfully written to " + file.getAbsolutePath())
    }
    catch (e: IOException) {
      e.printStackTrace()
      LogUtils.showLog("writeCSV_IOExp",chkNull(e.message,""))
      // Handle error
    }
    catch (e: Exception){
      e.printStackTrace()
      LogUtils.showLog("writeCSV_Exp",chkNull(e.message,""))
    }
   }
  }

  private fun getBaseLogsDir(folderName: String=""):File{
    return generateDir(getBaseDir("Logs"),folderName)
  }

  private fun generateDir(baseDir:File,folderName:String=""):File{
    return if(folderName.isNotEmpty()) {
      val dir =File(baseDir,folderName.replace(" ","_"))
      dir.mkdirs()
      return dir;
    } else baseDir
  }

  private fun getBaseDir(folderName:String=""):File{
    return generateDir(File(context.externalCacheDir!!.absolutePath),folderName)
  }

  fun writeApiLog(
    apiUrl: String,
    request: String? = null,
    response: String? = null,
    responseCode: Int? = null,
    fileCount: Int = 5
  ) {
    //if (!isDebugApp) return

    CoroutineScope(Dispatchers.IO).launch {
      try {
        val apiFolder = getBaseLogsDir("API")

        //val logFile = File(apiFolder, "api_logs.txt")
        val logFile = getRotatingLogFile(apiFolder, "api_logs", fileCount)

        deleteOldLogs(apiFolder, 7)

        val timeStamp = DateFormatUtils.getCurrentTime()

        val logBuilder = StringBuilder()

        logBuilder.append("\n----------------------------------------\n")
        logBuilder.append("$timeStamp : API URL -> $apiUrl\n")

        request?.let {
          logBuilder.append("$timeStamp : REQUEST -> $it\n")
        }

        response?.let {
          logBuilder.append("$timeStamp : RESPONSE -> $it\n")
        }

        responseCode?.let {
          logBuilder.append("$timeStamp : RESPONSE CODE -> $it\n")
        }

        logBuilder.append("----------------------------------------\n")

        //Write latest logs at the top
        val newLog = logBuilder.toString()

        val oldContent = if (logFile.exists()) {
          logFile.readText()
        } else {
          ""
        }

        FileWriter(logFile, false).use { writer ->
          writer.write(newLog + oldContent)
        }

       /* FileWriter(logFile, true).use {
          it.append(logBuilder.toString())
        }*/

      } catch (e: Exception) {
        e.printStackTrace()
        LogUtils.showLog("ApiLogError", e.message ?: "Error")
      }
    }
  }

  fun setLogConfig(count: Int, sizeMb: Int) {
    fileCount = count
    fileSize = sizeMb
    maxFileSize = fileSize * 1000 * 1000L
  }

  private fun getRotatingLogFile(
    folder: File,
    baseFileName: String,
    fileCount: Int
  ): File {

    val files = folder.listFiles()?.filter {
      it.name.startsWith(baseFileName)
    } ?: emptyList()

    val firstFile = File(folder, "$baseFileName-1.txt")

    //Case 1: No files → start with -1
    if (files.isEmpty()) {
      return firstFile
    }

    //Always write to file-1 unless full
    if (firstFile.exists() && firstFile.length() < maxFileSize) {
      return firstFile
    }


    //Step 1: Delete last file
    val lastFile = File(folder, "$baseFileName-$fileCount.txt")
    if (lastFile.exists()) {
      lastFile.delete()
    }

    //Step 2: Rename in reverse order
    for (i in fileCount - 1 downTo 1) {
      val current = File(folder, "$baseFileName-$i.txt")
      if (current.exists()) {
        val next = File(folder, "$baseFileName-${i + 1}.txt")
        current.renameTo(next)
      }
    }

    //Step 3: Return fresh file-1
    return firstFile
  }

  fun writeMqttLog(
    serverUrl: String? = null,
    topic: String? = null,
    message: String? = null,
    fileCount: Int = 5
  ) {
    //if (!isDebugApp) return

    CoroutineScope(Dispatchers.IO).launch {
      try {
        val mqttFolder = getBaseLogsDir("MQTT")

        //val logFile = File(mqttFolder, "mqtt_logs.txt")
        val logFile = getRotatingLogFile(mqttFolder, "mqtt_logs", fileCount)

        val timeStamp = DateFormatUtils.getCurrentTime()

        val logData = buildString {
          append("\n----------------------------------------\n")
          append("$timeStamp : SERVER   -> ${serverUrl}\n")
          append("$timeStamp : TOPIC    -> $topic\n")
          append("$timeStamp : MESSAGE  -> ${message ?: "N/A"}\n")
          append("----------------------------------------\n")
        }

        //Write latest logs at the top
        val oldContent = if (logFile.exists()) {
          logFile.readText()
        } else {
          ""
        }

        FileWriter(logFile, false).use { writer ->
          writer.write(logData + oldContent)
        }

       /* FileWriter(logFile, true).use {
          it.append(logData)
        }*/

      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
  }

  fun getDirList(): List<String> {
    return getBaseLogsDir().listFiles().filter { f-> f.isDirectory }.map { f-> f.name }.toList()
  }

  private fun deleteOldLogs(folder: File, daysToKeep: Int) {
    val now = System.currentTimeMillis()

    folder.listFiles()?.forEach { file ->
      val diff = now - file.lastModified()
      val days = diff / (1000 * 60 * 60 * 24)

      if (days > daysToKeep) {
        file.delete()
      }
    }
  }

  fun zipAndShare(context: Context,folderName: String="") {
    try {
      // 1. Logs folder inside external cache
      val folderDir = getBaseLogsDir(folderName)//File(context.getExternalCacheDir(), folderName)
      if (!folderDir.exists() || folderDir.listFiles() == null || folderDir.listFiles().size == 0) {
        LogUtils.showLog("zipShare_","No files to share")
        return
      }

      val files = folderDir.listFiles()
      LogUtils.showLog("zipShare_", "Found " + files!!.size + " log files")

      // 2. Create zip inside Logs folder (overwrite if exists)
      val cacheZip = File(context.getExternalCacheDir(), chkNull(folderName,"Logs")+".zip")
      if (cacheZip.exists()) {
        val deleted = cacheZip.delete()
        LogUtils.showLog("zipShare_old_zip_deleted?",""+deleted)
      }
      createZip(files, cacheZip)
      LogUtils.showLog("zipShare_zip_created", cacheZip.getAbsolutePath())

      // 3. Get URI from FileProvider
      val uri = FileProvider.getUriForFile(context, context.getPackageName(), cacheZip)

      // 4. Share intent
      val shareIntent = Intent(Intent.ACTION_SEND)
      shareIntent.setType("application/zip")
      shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
      shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
        shareIntent.setClipData(ClipData.newRawUri(folderName, uri))
      }

      context.startActivity(Intent.createChooser(shareIntent, "Share via"))
    } catch (e:Exception) {
      e.printStackTrace()
      LogUtils.showLog("Logs", "Error sharing logs: " + e)
      ToastUtils.showLongToast( "Error sharing logs: " + e.message)
    }
  }

  @Throws(IOException::class)
  private fun createZip(files: Array<File>, outFile: File) {
    val buffer = ByteArray(4096)
    FileOutputStream(outFile).use { fos ->
      ZipOutputStream(BufferedOutputStream(fos)).use { zos ->
        for (f in files) {
          if (!f.isFile()) continue

          LogUtils.showLog("createZip_Adding file",f.getName())

          BufferedInputStream(FileInputStream(f)).use { bis ->
            val entry = ZipEntry(f.getName())
            entry.setTime(f.lastModified())
            zos.putNextEntry(entry)

            var len: Int
            while ((bis.read(buffer).also { len = it }) != -1) {
              zos.write(buffer, 0, len)
            }
            zos.closeEntry()
          }
        }
      }
    }
    LogUtils.showLog("createZip_zip_successfully_created",outFile.getAbsolutePath())
  }
}