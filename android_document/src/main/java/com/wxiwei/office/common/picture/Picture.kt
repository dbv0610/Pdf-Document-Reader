/*
 * 文件名称:          Picture.java
 *  
 * 编译器:            android2.2
 * 时间:              下午4:01:51
 */
package com.wxiwei.office.common.picture

/**
 * picture data class
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
 * 日期:            2012-2-14
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
class Picture {
    /**
     * @param typeName
     */
    fun setPictureType(typeName: String) {
        if (typeName.equals(EMF_TYPE, ignoreCase = true)) {
            this.pictureType = EMF
        } else if (typeName.equals(WMF_TYPE, ignoreCase = true)) {
            this.pictureType = WMF
        } else if (typeName.equals(PICT_TYPE, ignoreCase = true)) {
            this.pictureType = PICT
        } else if (typeName.equals(JPEG_TYPE, ignoreCase = true)) {
            this.pictureType = JPEG
        } else if (typeName.equals(PNG_TYPE, ignoreCase = true)) {
            this.pictureType = PNG
        } else if (typeName.equals(DIB_TYPE, ignoreCase = true)) {
            this.pictureType = DIB
        } else if (typeName.equals(GIF_TYPE, ignoreCase = true)) {
            this.pictureType = GIF
        }
    }

    /**
     * 
     */
    fun dispose() {
        tempFilePath = null
    }

    /**
     * @return Returns the type.
     */
    /**
     * @param type The type to set.
     */
    //
    var pictureType: Byte = 0
    /**
     * @return Returns the data.
     */
    /**
     * @param data The data to set.
     */
    //
    @JvmField
    var data: ByteArray? = null
    /**
     * @return Returns the zoomX.
     */
    /**
     * @param zoomX The zoomX to set.
     */
    // picture horizontal zoom
    var zoomX: Short = 0
    /**
     * @return Returns the zoomY.
     */
    /**
     * @param zoomY The zoomY to set.
     */
    // picture vertical zoom 
    var zoomY: Short = 0
    /**
     * @return Returns the tempFilePath.
     */
    /**
     * @param tempFilePath The tempFilePath to set.
     */
    // temp file path;
    var tempFilePath: String? = null

    companion object {
        // Windows Enhanced Metafile (EMF)
        const val EMF: Byte = 2
        // Windows Metafile (WMF)
        const val WMF: Byte = 3
        // Macintosh PICT
        const val PICT: Byte = 4
        // JPEG
        const val JPEG: Byte = 5
        //  PNG
        const val PNG: Byte = 6
        // Windows DIB (BMP)
        const val DIB: Byte = 7
        //  PNG
        const val GIF: Byte = 8
        const val EMF_TYPE: String = "emf"

        // Windows Metafile (WMF)
        const val WMF_TYPE: String = "wmf"

        // Macintosh PICT
        const val PICT_TYPE: String = "pict"

        // JPEG
        const val JPEG_TYPE: String = "jpeg"

        //  PNG
        const val PNG_TYPE: String = "png"

        // Windows DIB (BMP)
        const val DIB_TYPE: String = "dib"

        // GIF
        const val GIF_TYPE: String = "gif"
    }
}
