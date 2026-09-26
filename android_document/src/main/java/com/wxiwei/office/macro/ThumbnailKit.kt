/*
 * 文件名称:          ThumbnailKit.java
 *
 * 编译器:            android2.2
 * 时间:              上午10:52:03
 */
package com.wxiwei.office.macro

import android.graphics.Bitmap
import com.wxiwei.office.constant.MainConstant
import com.wxiwei.office.fc.ReaderThumbnail

/**
 * get Thumbnail kit
 */
class ThumbnailKit {

    /**
     * @param filePath
     * @param width thumbnail width
     * @param height thumbnail height
     */
    fun getPPTThumbnail(filePath: String, width: Int, height: Int): Bitmap? {
        try {
            val lowerCase = filePath.lowercase()
            if (lowerCase.indexOf(".") > 0
                && width > 0
                && height > 0
                && (lowerCase.endsWith(MainConstant.FILE_TYPE_PPT)
                        || lowerCase.endsWith(MainConstant.FILE_TYPE_POT))
            ) {
                return ReaderThumbnail.instance().getThumbnailForPPT(filePath, width, height)
            }
        } catch (e: Exception) {
        }

        return null
    }

    /**
     * @param filePath
     */
    fun getPPTXThumbnail(filePath: String): Bitmap? {
        try {
            val lowerCase = filePath.lowercase()
            if (lowerCase.indexOf(".") > 0
                && (lowerCase.endsWith(MainConstant.FILE_TYPE_PPTX)
                        || lowerCase.endsWith(MainConstant.FILE_TYPE_PPTM)
                        || lowerCase.endsWith(MainConstant.FILE_TYPE_POTX)
                        || lowerCase.endsWith(MainConstant.FILE_TYPE_POTM))
            ) {
                return ReaderThumbnail.instance().getThumbnailForPPTX(filePath)
            }
        } catch (e: Exception) {
        }

        return null
    }


    companion object {
        private val kit = ThumbnailKit()

        @JvmStatic
        fun instance(): ThumbnailKit {
            return kit
        }
    }
}
