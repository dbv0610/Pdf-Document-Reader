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
package com.wxiwei.office.fc.hslf.blip

import com.wxiwei.office.fc.hslf.exceptions.HSLFException
import com.wxiwei.office.fc.hslf.usermodel.PictureData
import com.wxiwei.office.fc.util.LittleEndian
import com.wxiwei.office.fc.util.LittleEndianConsts
import com.wxiwei.office.fc.util.LittleEndian.getInt
import com.wxiwei.office.fc.util.LittleEndian.getUnsignedByte
import com.wxiwei.office.fc.util.LittleEndian.putInt
import com.wxiwei.office.java.awt.Dimension
import com.wxiwei.office.java.awt.Rectangle
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.DeflaterOutputStream
import java.util.zip.InflaterInputStream

/**
 * Represents a metafile picture which can be one of the following types: EMF, WMF, or PICT.
 * A metafile is stored compressed using the ZIP deflate/inflate algorithm.
 * 
 * @author Yegor Kozlov
 */
abstract class Metafile : PictureData() {
    /**
     * A structure which represents a 34-byte header preceeding the compressed metafile data
     * 
     * @author Yegor Kozlov
     */
    class Header {
        /**
         * size of the original file
         */
        var wmfsize: Int = 0

        /**
         * Boundary of the metafile drawing commands
         */
        var bounds: Rectangle? = null

        /**
         * Size of the metafile in EMUs
         */
        @JvmField
        var size: Dimension? = null

        /**
         * size of the compressed metafile data
         */
        var zipsize: Int = 0

        /**
         * Reserved. Always 0.
         */
        var compression: Int = 0

        /**
         * Reserved. Always 254.
         */
        var filter: Int = 254

        fun read(data: ByteArray, offset: Int) {
            var pos = offset
            wmfsize = getInt(data, pos)
            pos += LittleEndianConsts.INT_SIZE

            val left = getInt(data, pos)
            pos += LittleEndianConsts.INT_SIZE
            val top = getInt(data, pos)
            pos += LittleEndianConsts.INT_SIZE
            val right = getInt(data, pos)
            pos += LittleEndianConsts.INT_SIZE
            val bottom = getInt(data, pos)
            pos += LittleEndianConsts.INT_SIZE

            bounds = Rectangle(left, top, right - left, bottom - top)
            val width = getInt(data, pos)
            pos += LittleEndianConsts.INT_SIZE
            val height = getInt(data, pos)
            pos += LittleEndianConsts.INT_SIZE

            size = Dimension(width, height)

            zipsize = getInt(data, pos)
            pos += LittleEndianConsts.INT_SIZE

            compression = getUnsignedByte(data, pos)
            pos++
            filter = getUnsignedByte(data, pos)
            pos++
        }

        @Throws(IOException::class)
        fun write(out: OutputStream) {
            val header = ByteArray(34)
            var pos = 0
            putInt(header, pos, wmfsize)
            pos += LittleEndianConsts.INT_SIZE //hmf

            putInt(header, pos, bounds!!.x)
            pos += LittleEndianConsts.INT_SIZE //left
            putInt(header, pos, bounds!!.y)
            pos += LittleEndianConsts.INT_SIZE //top
            putInt(header, pos, bounds!!.x + bounds!!.width)
            pos += LittleEndianConsts.INT_SIZE //right
            putInt(header, pos, bounds!!.y + bounds!!.height)
            pos += LittleEndianConsts.INT_SIZE //bottom
            putInt(header, pos, size!!.width)
            pos += LittleEndianConsts.INT_SIZE //inch
            putInt(header, pos, size!!.height)
            pos += LittleEndianConsts.INT_SIZE //inch
            putInt(header, pos, zipsize)
            pos += LittleEndianConsts.INT_SIZE //inch

            header[pos] = 0
            pos++
            header[pos] = filter.toByte()
            pos++

            out.write(header)
        }

        fun getSize(): Int {
            return 34
        }
    }

    @Throws(IOException::class)
    protected fun compress(bytes: ByteArray?, offset: Int, length: Int): ByteArray {
        val out = ByteArrayOutputStream()
        val deflater = DeflaterOutputStream(out)
        deflater.write(bytes, offset, length)
        deflater.close()
        return out.toByteArray()
    }

    fun writeByte_WMFAndEMF(out: FileOutputStream) {
        try {
            val rawdata = getRawData()

            val `is`: InputStream = ByteArrayInputStream(rawdata)
            `is`.skip(8)

            val header = Header()
            header.read(rawdata!!, CHECKSUM_SIZE)
            `is`.skip((header.getSize() + CHECKSUM_SIZE).toLong())

            val inflater = InflaterInputStream(`is`)
            val chunk = ByteArray(4096)
            var count: Int
            while ((inflater.read(chunk).also { count = it }) >= 0) {
                out.write(chunk, 0, count)
            }
            inflater.close()
        } catch (e: IOException) {
            throw HSLFException(e)
        }
    }
}
