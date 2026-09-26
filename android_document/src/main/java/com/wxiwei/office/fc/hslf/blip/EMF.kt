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
import java.io.InputStream
import java.util.zip.InflaterInputStream

/**
 * Represents EMF (Windows Enhanced Metafile) picture data.
 * 
 * @author Yegor Kozlov
 */
class EMF : Metafile() {
    /**
     * Extract compressed EMF data from a ppt
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
        val compressed = compress(data, 0, data.size)

        val header = Header()
        header.wmfsize = data.size
        //we don't have a EMF reader in java, have to set default image size  200x200
        header.bounds = Rectangle(0, 0, 200, 200)
        header.size = Dimension(
            header.bounds!!.width * ShapeKit.EMU_PER_POINT,
            header.bounds!!.height * ShapeKit.EMU_PER_POINT
        )
        header.zipsize = compressed.size

        val checksum = getChecksum(data)
        val out = ByteArrayOutputStream()
        out.write(checksum)
        header.write(out)
        out.write(compressed)

        setRawData(out.toByteArray())
    }

    public override fun getType(): Int {
        return Picture.EMF
    }

    /**
     * EMF signature is `0x3D40`
     * 
     * @return EMF signature (`0x3D40`)
     */
    public override fun getSignature(): Int {
        return 0x3D40
    }
}
