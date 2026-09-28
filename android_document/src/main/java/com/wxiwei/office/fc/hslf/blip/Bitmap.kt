/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
package com.wxiwei.office.fc.hslf.blip

import com.wxiwei.office.fc.hslf.usermodel.PictureData
import java.io.ByteArrayOutputStream
import java.io.IOException

abstract class Bitmap : PictureData() {
    public override fun getData(): ByteArray? {
        val rawdata = getRawData()
        if (rawdata == null || rawdata.size <= 17) return ByteArray(0)
        val imgdata = ByteArray(rawdata.size - 17)
        System.arraycopy(rawdata, 17, imgdata, 0, imgdata.size)
        return imgdata
    }

    @Throws(IOException::class)
    public override fun setData(data: ByteArray?) {
        if (data == null) return
        val out = ByteArrayOutputStream()
        val checksum = getChecksum(data)
        out.write(checksum)
        out.write(0)
        out.write(data)
        setRawData(out.toByteArray())
    }
}
