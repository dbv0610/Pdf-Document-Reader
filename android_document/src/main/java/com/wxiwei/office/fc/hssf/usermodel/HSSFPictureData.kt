/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */
package com.wxiwei.office.fc.hssf.usermodel

import com.wxiwei.office.fc.ddf.EscherBitmapBlip
import com.wxiwei.office.fc.ddf.EscherBlipRecord
import com.wxiwei.office.fc.ddf.EscherMetafileBlip
import com.wxiwei.office.fc.ss.usermodel.PictureData


/**
 * Represents binary data stored in the file.  Eg. A GIF, JPEG etc...
 * 
 * @author Daniel Noll
 */
class HSSFPictureData
/**
 * Constructs a picture object.
 * 
 * @param blip the underlying blip record containing the bitmap data.
 */(
    /**
     * Underlying escher blip record containing the bitmap data.
     */
    private val blip: EscherBlipRecord
) : PictureData {
    /* (non-Javadoc)
       * @see org.apache.poi.hssf.usermodel.PictureData#getData()
       */
    override fun getData(): ByteArray? {
        return blip.picturedata
    }

    val format: Int
        /**
         * 
         * @return format of the picture.
         * @see HSSFWorkbook.PICTURE_TYPE_DIB
         * 
         * @see HSSFWorkbook.PICTURE_TYPE_WMF
         * 
         * @see HSSFWorkbook.PICTURE_TYPE_EMF
         * 
         * @see HSSFWorkbook.PICTURE_TYPE_PNG
         * 
         * @see HSSFWorkbook.PICTURE_TYPE_JPEG
         * 
         * @see HSSFWorkbook.PICTURE_TYPE_PICT
         */
        get() = blip.recordId - 0xF018.toShort()

    /**
     * @see .getFormat
     * 
     * @return 'wmf', 'jpeg' etc depending on the format. never `null`
     */
    override fun suggestFileExtension(): String {
        when (blip.recordId) {
            EscherMetafileBlip.RECORD_ID_WMF -> return "wmf"
            EscherMetafileBlip.RECORD_ID_EMF -> return "emf"
            EscherMetafileBlip.RECORD_ID_PICT -> return "pict"
            EscherBitmapBlip.RECORD_ID_PNG -> return "png"
            EscherBitmapBlip.RECORD_ID_JPEG -> return "jpeg"
            EscherBitmapBlip.RECORD_ID_DIB -> return "dib"
            else -> return ""
        }
    }

    /**
     * Returns the mime type for the image
     */
    override fun getMimeType(): String {
        when (blip.recordId) {
            EscherMetafileBlip.RECORD_ID_WMF -> return "image/x-wmf"
            EscherMetafileBlip.RECORD_ID_EMF -> return "image/x-emf"
            EscherMetafileBlip.RECORD_ID_PICT -> return "image/x-pict"
            EscherBitmapBlip.RECORD_ID_PNG -> return "image/png"
            EscherBitmapBlip.RECORD_ID_JPEG -> return "image/jpeg"
            EscherBitmapBlip.RECORD_ID_DIB -> return "image/bmp"
            else -> return "image/unknown"
        }
    }

    companion object {
        // MSOBI constants for various formats.
        const val MSOBI_WMF: Short = 0x2160
        const val MSOBI_EMF: Short = 0x3D40
        const val MSOBI_PICT: Short = 0x5420
        const val MSOBI_PNG: Short = 0x6E00
        const val MSOBI_JPEG: Short = 0x46A0
        const val MSOBI_DIB: Short = 0x7A80

        // Mask of the bits in the options used to store the image format.
        val FORMAT_MASK: Short = 0xFFF0.toShort()
    }
}
