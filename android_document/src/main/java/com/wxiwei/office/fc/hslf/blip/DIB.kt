package com.wxiwei.office.fc.hslf.blip

import com.wxiwei.office.fc.hslf.model.Picture
import com.wxiwei.office.fc.util.LittleEndian.getInt
import com.wxiwei.office.fc.util.LittleEndian.putInt

class DIB : Bitmap() {
    override fun getType() = Picture.DIB.toInt()

    override fun getData(): ByteArray? {
        val d = super.getData() ?: return null
        return addBMPHeader(d)
    }

    override fun setData(data: ByteArray?) {
        if (data == null) return
        val dib = ByteArray(data.size - HEADER_SIZE)
        System.arraycopy(data, HEADER_SIZE, dib, 0, dib.size)
        super.setData(dib)
    }

    override fun getSignature() = 0x7A80

    companion object {
        const val HEADER_SIZE: Int = 14

        fun addBMPHeader(data: ByteArray): ByteArray {
            val header = ByteArray(HEADER_SIZE)
            putInt(header, 0, 0x4D42)
            val imageSize = getInt(data, 0x22 - HEADER_SIZE)
            val fileSize: Int = data.size + HEADER_SIZE
            val offset = fileSize - imageSize

            putInt(header, 2, fileSize)
            putInt(header, 6, 0)
            putInt(header, 10, offset)

            val dib = ByteArray(header.size + data.size)
            System.arraycopy(header, 0, dib, 0, header.size)
            System.arraycopy(data, 0, dib, header.size, data.size)

            return dib
        }
    }
}
