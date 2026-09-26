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
