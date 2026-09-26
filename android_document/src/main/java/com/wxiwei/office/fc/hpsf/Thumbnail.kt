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
package com.wxiwei.office.fc.hpsf

import com.wxiwei.office.fc.hpsf.Thumbnail.Companion.CFTAG_WINDOWS
import com.wxiwei.office.fc.util.LittleEndian

/**
 * 
 * Class to manipulate data in the Clipboard Variant ([ ][Variant.VT_CF]) format.
 * 
 * @author Drew Varner (Drew.Varner inOrAround sc.edu)
 * @see SummaryInformation.getThumbnail
 */
class Thumbnail {
    /**
     * 
     * A `byte[]` to hold a thumbnail image in ([ ][Variant.VT_CF]) format.
     */
    private var _thumbnailData: ByteArray? = null


    /**
     * 
     * Default Constructor. If you use it then one you'll have to add
     * the thumbnail `byte[]` from [ ][SummaryInformation.getThumbnail] to do any useful
     * manipulations, otherwise you'll get a
     * `NullPointerException`.
     */
    constructor() : super()


    /**
     * 
     * Creates a `Thumbnail` instance and initializes
     * with the specified image bytes.
     * 
     * @param thumbnailData The thumbnail data
     */
    constructor(thumbnailData: ByteArray?) {
        this._thumbnailData = thumbnailData
    }


    var thumbnail: ByteArray?
        /**
         * 
         * Returns the thumbnail as a `byte[]` in [ ][Variant.VT_CF] format.
         * 
         * @return The thumbnail value
         * @see SummaryInformation.getThumbnail
         */
        get() = _thumbnailData
        /**
         * 
         * Sets the Thumbnail's underlying `byte[]` in
         * [VT_CF][Variant.VT_CF] format.
         * 
         * @param thumbnail The new thumbnail value
         * @see SummaryInformation.getThumbnail
         */
        set(thumbnail) {
            this._thumbnailData = thumbnail
        }


    val clipboardFormatTag: Long
        /**
         * 
         * Returns an `int` representing the Clipboard
         * Format Tag
         * 
         * 
         * Possible return values are:
         * 
         *  * [CFTAG_WINDOWS][.CFTAG_WINDOWS]
         *  * [CFTAG_MACINTOSH][.CFTAG_MACINTOSH]
         *  * [CFTAG_FMTID][.CFTAG_FMTID]
         *  * [CFTAG_NODATA][.CFTAG_NODATA]
         * 
         * 
         * @return A flag indicating the Clipboard Format Tag
         */
        get() {
            val clipboardFormatTag = LittleEndian.getUInt(
                this.thumbnail!!,
                OFFSET_CFTAG
            )
            return clipboardFormatTag
        }


    @get:Throws(HPSFException::class)
    val clipboardFormat: Long
        /**
         * 
         * Returns an `int` representing the Clipboard
         * Format
         * 
         * 
         * Will throw an exception if the Thumbnail's Clipboard Format
         * Tag is not [CFTAG_WINDOWS][CFTAG_WINDOWS].
         * 
         * 
         * Possible return values are:
         * 
         * 
         *  * [CF_METAFILEPICT][.CF_METAFILEPICT]
         *  * [CF_DIB][.CF_DIB]
         *  * [CF_ENHMETAFILE][.CF_ENHMETAFILE]
         *  * [CF_BITMAP][.CF_BITMAP]
         * 
         * 
         * @return a flag indicating the Clipboard Format
         * @throws HPSFException if the Thumbnail isn't CFTAG_WINDOWS
         */
        get() {
            if (this.clipboardFormatTag != CFTAG_WINDOWS.toLong()) throw HPSFException(
                "Clipboard Format Tag of Thumbnail must " +
                        "be CFTAG_WINDOWS."
            )

            return LittleEndian.getUInt(
                this.thumbnail!!,
                OFFSET_CF
            )
        }


    @get:Throws(HPSFException::class)
    val thumbnailAsWMF: ByteArray
        /**
         * 
         * Returns the Thumbnail as a `byte[]` of WMF data
         * if the Thumbnail's Clipboard Format Tag is [ ][.CFTAG_WINDOWS] and its Clipboard Format is
         * [CF_METAFILEPICT][.CF_METAFILEPICT] 
         *
         *This
         * `byte[]` is in the traditional WMF file, not the
         * clipboard-specific version with special headers.
         * 
         * 
         * See [http://www.wvware.com/caolan/ora-wmf.html](http://www.wvware.com/caolan/ora-wmf.html)
         * for more information on the WMF image format.
         * 
         * @return A WMF image of the Thumbnail
         * @throws HPSFException if the Thumbnail isn't CFTAG_WINDOWS and
         * CF_METAFILEPICT
         */
        get() {
            if (this.clipboardFormatTag != CFTAG_WINDOWS.toLong()) throw HPSFException(
                "Clipboard Format Tag of Thumbnail must " +
                        "be CFTAG_WINDOWS."
            )
            if (this.clipboardFormat != CF_METAFILEPICT.toLong()) {
                throw HPSFException(
                    "Clipboard Format of Thumbnail must " +
                            "be CF_METAFILEPICT."
                )
            }
            val thumbnail = this.thumbnail!!
            val wmfImageLength: Int = thumbnail.size - OFFSET_WMFDATA
            val wmfImage = ByteArray(wmfImageLength)
            System.arraycopy(
                thumbnail,
                OFFSET_WMFDATA,
                wmfImage,
                0,
                wmfImageLength
            )
            return wmfImage
        }

    companion object {
        /**
         * 
         * Offset in bytes where the Clipboard Format Tag starts in the
         * `byte[]` returned by [ ][SummaryInformation.getThumbnail]
         */
        var OFFSET_CFTAG: Int = 4

        /**
         * 
         * Offset in bytes where the Clipboard Format starts in the
         * `byte[]` returned by [ ][SummaryInformation.getThumbnail]
         * 
         * 
         * This is only valid if the Clipboard Format Tag is [ ][.CFTAG_WINDOWS]
         */
        var OFFSET_CF: Int = 8

        /**
         * 
         * Offset in bytes where the Windows Metafile (WMF) image data
         * starts in the `byte[]` returned by [ ][SummaryInformation.getThumbnail]
         * 
         * 
         * There is only WMF data at this point in the
         * `byte[]` if the Clipboard Format Tag is [ ][.CFTAG_WINDOWS] and the Clipboard Format is [ ][.CF_METAFILEPICT].
         * 
         * 
         * Note: The `byte[]` that starts at
         * `OFFSET_WMFDATA` and ends at
         * `getThumbnail().length - 1` forms a complete WMF
         * image. It can be saved to disk with a `.wmf` file
         * type and read using a WMF-capable image viewer.
         */
        var OFFSET_WMFDATA: Int = 20

        /**
         * 
         * Clipboard Format Tag - Windows clipboard format
         * 
         * 
         * A `DWORD` indicating a built-in Windows clipboard
         * format value
         */
        var CFTAG_WINDOWS: Int = -1

        /**
         * 
         * Clipboard Format Tag - Macintosh clipboard format
         * 
         * 
         * A `DWORD` indicating a Macintosh clipboard format
         * value
         */
        var CFTAG_MACINTOSH: Int = -2

        /**
         * 
         * Clipboard Format Tag - Format ID
         * 
         * 
         * A GUID containing a format identifier (FMTID). This is
         * rarely used.
         */
        var CFTAG_FMTID: Int = -3

        /**
         * 
         * Clipboard Format Tag - No Data
         * 
         * 
         * A `DWORD` indicating No data. This is rarely
         * used.
         */
        var CFTAG_NODATA: Int = 0

        /**
         * 
         * Clipboard Format - Windows metafile format. This is the
         * recommended way to store thumbnails in Property Streams.
         * 
         * 
         * **Note:** This is not the same format used in
         * regular WMF images. The clipboard version of this format has an
         * extra clipboard-specific header.
         */
        var CF_METAFILEPICT: Int = 3

        /**
         * 
         * Clipboard Format - Device Independent Bitmap
         */
        var CF_DIB: Int = 8

        /**
         * 
         * Clipboard Format - Enhanced Windows metafile format
         */
        var CF_ENHMETAFILE: Int = 14

        /**
         * 
         * Clipboard Format - Bitmap
         * 
         * 
         * Obsolete, see [](msdn.microsoft.com/library/en-us/dnw98bk/html/clipboardoperations.asp
        target=)">msdn.microsoft.com/library/en-us/dnw98bk/html/clipboardoperations.asp.
         */
        var CF_BITMAP: Int = 2
    }
}
