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
package com.wxiwei.office.fc.hslf.blip

import com.wxiwei.office.fc.ShapeKit
import com.wxiwei.office.fc.hslf.exceptions.HSLFException
import com.wxiwei.office.fc.hslf.model.Picture
import com.wxiwei.office.java.awt.Dimension
import com.wxiwei.office.java.awt.Rectangle
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.zip.InflaterInputStream

/**
 * Represents Macintosh PICT picture data.
 * 
 * @author Yegor Kozlov
 */
class PICT : Metafile() {
    /**
     * Extract compressed PICT data from a ppt
     */
    public override fun getData(): ByteArray {
        val rawdata = getRawData()
        try {
            val macheader = ByteArray(512)
            val out = ByteArrayOutputStream()
            out.write(macheader)
            val pos = CHECKSUM_SIZE
            var pict: ByteArray?
            try {
                pict = read(rawdata, pos)
            } catch (e: IOException) {
                //weird MAC behaviour.
                //if failed to read right after the checksum - skip 16 bytes and try again
                pict = read(rawdata, pos + 16)
            }
            out.write(pict)
            return out.toByteArray()
        } catch (e: IOException) {
            throw HSLFException(e)
        }
    }

    @Throws(IOException::class)
    private fun read(data: ByteArray?, pos: Int): ByteArray {
        val out = ByteArrayOutputStream()
        val bis = ByteArrayInputStream(data)
        val header = Header()
        header.read(data!!, pos)
        bis.skip((pos + header.getSize()).toLong())
        val inflater = InflaterInputStream(bis)
        val chunk = ByteArray(4096)
        var count: Int
        while ((inflater.read(chunk).also { count = it }) >= 0) {
            out.write(chunk, 0, count)
        }
        inflater.close()
        return out.toByteArray()
    }

    @Throws(IOException::class)
    public override fun setData(data: ByteArray?) {
        val data = data!!
        val pos = 512 //skip the first 512 bytes - they are MAC specific crap
        val compressed = compress(data, pos, data.size - pos)

        val header = Header()
        header.wmfsize = data.size - 512
        //we don't have a PICT reader in java, have to set default image size  200x200
        header.bounds = Rectangle(0, 0, 200, 200)
        header.size = Dimension(
            header.bounds!!.width * ShapeKit.EMU_PER_POINT, header.bounds!!.height
                    * ShapeKit.EMU_PER_POINT
        )
        header.zipsize = compressed.size

        val checksum = getChecksum(data)
        val out = ByteArrayOutputStream()
        out.write(checksum)

        out.write(ByteArray(16)) //16-byte prefix which is safe to ignore
        header.write(out)
        out.write(compressed)

        setRawData(out.toByteArray())
    }

    /**
     * @see Picture.PICT
     */
    public override fun getType(): Int {
        return Picture.PICT
    }

    /**
     * PICT signature is `0x5430`
     * 
     * @return PICT signature (`0x5430`)
     */
    public override fun getSignature(): Int {
        return 0x5430
    }
}
