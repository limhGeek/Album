package com.limh.album

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * @author limh
 * @function
 * @date 2019/4/19 9:40
 */
object FileUtils {
    val BASE_PATH = "${Environment.getExternalStorageDirectory()}/DCIM/Image"
    /**
     * 根据文件名称 生成目录
     * @param fileName 文件名称
     */
    fun getFilePath(fileName: String): String {
        val dir = File(BASE_PATH)
        //如果目录不存在 先创建目录
        if (!dir.exists()) {
            dir.mkdir()
        }
        return "$BASE_PATH/$fileName"
    }

    /**
     * 获取文件Uri
     * @param fileName 文件名称
     */
    fun getFileUri(context: Context, fileName: String): Uri {
        val filePah = getFilePath(fileName)
        return if (Build.VERSION.SDK_INT >= 24) {
            FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.fileprovider", File(filePah))
        } else {
            Uri.fromFile(File(filePah))
        }
    }

    fun copyFile(oldPath: String, newPath: String) {
        try {
            var bytesum = 0
            var byteread = 0
            val oldfile = File(oldPath)
            if (!oldfile.exists()) { //文件不存在时
                val inStream = FileInputStream(oldPath) //读入原文件
                val fs = FileOutputStream(newPath)
                val buffer = ByteArray(1444)
                while (inStream.read(buffer).apply { byteread = this } != -1) {
                    bytesum += byteread //字节数 文件大小
                    println(bytesum)
                    fs.write(buffer, 0, byteread)
                }
                inStream.close()
            }
        } catch (e: Exception) {
            Log.e("Main","复制单个文件操作出错")
            e.printStackTrace()

        }

    }
}