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

import com.wxiwei.office.fc.ShapeKit
import com.wxiwei.office.fc.hslf.exceptions.HSLFException
import com.wxiwei.office.fc.hslf.model.Picture
import com.wxiwei.office.fc.util.LittleEndian
import com.wxiwei.office.fc.util.LittleEndianConsts
import com.wxiwei.office.fc.util.LittleEndian.getInt
import com.wxiwei.office.fc.util.LittleEndian.getShort
import com.wxiwei.office.fc.util.LittleEndian.getUShort
import com.wxiwei.office.fc.util.LittleEndian.putInt
import com.wxiwei.office.fc.util.LittleEndian.putUShort
import com.wxiwei.office.java.awt.Dimension
import com.wxiwei.office.java.awt.Rectangle
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.InflaterInputStream

/**
 * Represents a WMF (Windows Metafile) picture data.
 * 
 * @author Yegor Kozlov
 */
class WMF : Metafile() {
    /**
     * Extract compressed WMF data from a ppt
     */
    public override fun getData(): ByteArray {
        try {
            val rawdata = getRawData()

            val out = ByteArrayOutputStream()
            val `is`: InputStream = ByteArrayInputStream(rawdata)
            `is`.skip(8)

            val header = Header()
            header.read(rawdata!!, CHECKSUM_SIZE)
            `is`.skip((header.getSize() + CHECKSUM_SIZE).toLong())

            //            AldusHeader aldus = new AldusHeader();
//            aldus.left = header.bounds.x;
//            aldus.top = header.bounds.y;
//            aldus.right = header.bounds.x + header.bounds!!.width;
//            aldus.bottom = header.bounds.y + header.bounds!!.height;
//            aldus.write(out);
            val inflater = InflaterInputStream(`is`)
            val chunk = ByteArray(4096)
            var count: Int
            while ((inflater.read(chunk).also { count = it }) >= 0) {
                out.write(chunk, 0, count)
            }
            inflater.close()
            return out.toByteArray()
        } catch (e: IOException) {
            throw HSLFException(e)
        }
    }

    @Throws(IOException::class)
    public override fun setData(data: ByteArray?) {
        val data = data!!
        var pos = 0
        val aldus = AldusHeader()
        aldus.read(data, pos)
        pos += aldus.size

        val compressed = compress(data, pos, data.size - pos)

        val header = Header()
        header.wmfsize = data.size - aldus.size
        header.bounds = Rectangle(
            aldus.left.toShort().toInt(),
            aldus.top.toShort().toInt(),
            aldus.right.toShort() - aldus.left.toShort(),
            aldus.bottom.toShort() - aldus.top.toShort()
        )
        //coefficient to translate from WMF dpi to 96pdi
        val coeff = 96 * ShapeKit.EMU_PER_POINT / aldus.inch
        header.size = Dimension(
            header.bounds!!.width * coeff, header.bounds!!.height
                    * coeff
        )
        header.zipsize = compressed.size

        val checksum = getChecksum(data)
        val out = ByteArrayOutputStream()
        out.write(checksum)
        header.write(out)
        out.write(compressed)

        setRawData(out.toByteArray())
    }

    /**
     * We are of type `Picture.WMF`
     */
    public override fun getType(): Int {
        return Picture.WMF
    }

    /**
     * WMF signature is `0x2160`
     */
    public override fun getSignature(): Int {
        return 0x2160
    }

    /**
     * Aldus Placeable Metafile header - 22 byte structure before WMF data.
     * 
     *  * int Key;               Magic number (always 9AC6CDD7h)
     *  * short  Handle;         Metafile HANDLE number (always 0)
     *  * short Left;            Left coordinate in metafile units
     *  * short Top;             Top coordinate in metafile units
     *  * short Right;           Right coordinate in metafile units
     *  * short Bottom;          Bottom coordinate in metafile units
     *  * short  Inch;           Number of metafile units per inch
     *  * int Reserved;          Reserved (always 0)
     *  * short  Checksum;       Checksum value for previous 10 shorts
     * 
     */
    class AldusHeader {
        var handle: Int = 0
        var left: Int = 0
        var top: Int = 0
        var right: Int = 0
        var bottom: Int = 0
        var inch: Int = 72 //default resolution is 72 dpi
        var reserved: Int = 0
        @JvmField
        var checksum: Int = 0

        fun read(data: ByteArray, offset: Int) {
            var pos = offset
            val key = getInt(data, pos)
            pos += LittleEndianConsts.INT_SIZE //header key
            if (key != APMHEADER_KEY) throw HSLFException("Not a valid WMF file")

            handle = getUShort(data, pos)
            pos += LittleEndianConsts.SHORT_SIZE
            left = getUShort(data, pos)
            pos += LittleEndianConsts.SHORT_SIZE
            top = getUShort(data, pos)
            pos += LittleEndianConsts.SHORT_SIZE
            right = getUShort(data, pos)
            pos += LittleEndianConsts.SHORT_SIZE
            bottom = getUShort(data, pos)
            pos += LittleEndianConsts.SHORT_SIZE

            inch = getUShort(data, pos)
            pos += LittleEndianConsts.SHORT_SIZE
            reserved = getInt(data, pos)
            pos += LittleEndianConsts.INT_SIZE

            checksum = getShort(data, pos).toInt()
            pos += LittleEndianConsts.SHORT_SIZE
            if (checksum != getChecksum()) {
                //logger.log(POILogger.WARN, "WMF checksum does not match the header data");
            }
        }

        /**
         * Returns a checksum value for the previous 10 shorts in the header.
         * The checksum is calculated by XORing each short value to an initial value of 0:
         */
        fun getChecksum(): Int {
            var checksum = 0
            checksum = checksum xor (APMHEADER_KEY and 0x0000FFFF)
            checksum = checksum xor ((APMHEADER_KEY and -0x10000) shr 16)
            checksum = checksum xor left
            checksum = checksum xor top
            checksum = checksum xor right
            checksum = checksum xor bottom
            checksum = checksum xor inch
            return checksum
        }

        @Throws(IOException::class)
        fun write(out: OutputStream) {
            val header = ByteArray(22)
            var pos = 0
            putInt(header, pos, APMHEADER_KEY)
            pos += LittleEndianConsts.INT_SIZE //header key
            putUShort(header, pos, 0)
            pos += LittleEndianConsts.SHORT_SIZE //hmf
            putUShort(header, pos, left)
            pos += LittleEndianConsts.SHORT_SIZE //left
            putUShort(header, pos, top)
            pos += LittleEndianConsts.SHORT_SIZE //top
            putUShort(header, pos, right)
            pos += LittleEndianConsts.SHORT_SIZE //right
            putUShort(header, pos, bottom)
            pos += LittleEndianConsts.SHORT_SIZE //bottom
            putUShort(header, pos, inch)
            pos += LittleEndianConsts.SHORT_SIZE //inch
            putInt(header, pos, 0)
            pos += LittleEndianConsts.INT_SIZE //reserved

            checksum = getChecksum()
            putUShort(header, pos, checksum)

            out.write(header)
        }

        val size: Int
            get() = 22

        companion object {
            const val APMHEADER_KEY: Int = -0x65393229
        }
    }
}
