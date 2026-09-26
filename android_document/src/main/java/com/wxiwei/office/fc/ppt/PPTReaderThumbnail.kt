/*
 * 文件名称:          PTTReaderThumbnail.java
 *  
 * 编译器:            android2.2
 * 时间:              下午4:18:41
 */
package com.wxiwei.office.fc.ppt

import android.graphics.Bitmap
import com.wxiwei.office.constant.MainConstant
import java.util.Locale

/**
 * get thumbnail of PPT document
 * 
 * 
 * 
 * 
 * Read版本:        Read V1.0
 * 
 * 
 * 作者:            ljj8494
 * 
 * 
 * 日期:            2012-12-13
 * 
 * 
 * 负责人:          ljj8494
 * 
 * 
 * 负责小组:
 * 
 * 
 * 
 * 
 */
class PPTReaderThumbnail {
    /**
     * 
     */
    fun getThumbnail(filePath: String): Bitmap? {
        try {
            val fileName = filePath.lowercase(Locale.getDefault())
            // ppt
            if (fileName.endsWith(MainConstant.FILE_TYPE_PPT)
                || fileName.endsWith(MainConstant.FILE_TYPE_POT)
            ) {
                return getThumbnailForPPT(filePath)
            } else if (fileName.endsWith(MainConstant.FILE_TYPE_PPTX)
                || fileName.endsWith(MainConstant.FILE_TYPE_PPTM)
                || fileName.endsWith(MainConstant.FILE_TYPE_POTX)
                || fileName.endsWith(MainConstant.FILE_TYPE_POTM)
            ) {
                return getThumbnailForPPT(filePath)
            }
        } catch (e: Exception) {
            return null
        }
        return null
    }

    /**
     * 
     * @param filePath
     * @return
     * @throws Exception
     */
    @Throws(Exception::class)
    private fun getThumbnailForPPT(filePath: String?): Bitmap? {
        return null
    }

    /**
     * 
     * @param file
     * @return
     */
    @Throws(Exception::class)
    private fun getThumbnailForPPTX(filePath: String?): Bitmap? {
        return null
    }

    companion object {
        //
        private val kit = PPTReaderThumbnail()

        @JvmStatic

        fun instance(): PPTReaderThumbnail {
            return kit
        }
    }
}
