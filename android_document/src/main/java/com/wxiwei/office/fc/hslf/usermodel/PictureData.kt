package com.wxiwei.office.fc.hslf.usermodel

import com.wxiwei.office.fc.hslf.blip.DIB
import com.wxiwei.office.fc.hslf.blip.EMF
import com.wxiwei.office.fc.hslf.blip.ImagePainter
import com.wxiwei.office.fc.hslf.blip.JPEG
import com.wxiwei.office.fc.hslf.blip.PICT
import com.wxiwei.office.fc.hslf.blip.PNG
import com.wxiwei.office.fc.hslf.blip.WMF
import com.wxiwei.office.fc.hslf.exceptions.HSLFException
import com.wxiwei.office.fc.hslf.model.Picture
import com.wxiwei.office.fc.util.LittleEndian
import java.io.IOException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

abstract class PictureData {

    @JvmField
    var rawdata: ByteArray? = null

    @JvmField
    var offset: Int = 0

    @JvmField
    var tempFilePath: String? = null

    abstract fun getType(): Int

    abstract fun getData(): ByteArray?

    @Throws(IOException::class)
    abstract fun setData(data: ByteArray?)

    protected abstract fun getSignature(): Int

    open fun getRawData(): ByteArray? {
        return rawdata
    }

    open fun setRawData(data: ByteArray?) {
        rawdata = data
    }

    open fun getOffset(): Int {
        return offset
    }

    open fun setOffset(offset: Int) {
        this.offset = offset
    }

    open fun getUID(): ByteArray {
        return ByteArray(16)
    }

    open fun getHeader(): ByteArray {
        val header = ByteArray(16 + 8)
        LittleEndian.putInt(header, 0, getSignature())
        return header
    }

    @Deprecated("Use getData().length instead.")
    open fun getSize(): Int {
        return getData()?.size ?: 0
    }

    open fun getTempFilePath(): String? {
        return tempFilePath
    }

    open fun setTempFilePath(tempFilePath: String?) {
        this.tempFilePath = tempFilePath
    }

    open fun dispose() {
        tempFilePath = null
    }

    companion object {
        protected const val CHECKSUM_SIZE = 16

        @JvmStatic
        protected var painters: Array<ImagePainter?> = arrayOfNulls(8)

        @JvmStatic
        fun getChecksum(data: ByteArray?): ByteArray {
            if (data == null) return ByteArray(0)
            val sha: MessageDigest = try {
                MessageDigest.getInstance("MD5")
            } catch (e: NoSuchAlgorithmException) {
                throw HSLFException(e.message ?: "")
            }
            sha.update(data)
            return sha.digest()
        }

        @JvmStatic
        fun create(type: Int): PictureData {
            return when (type) {
                Picture.EMF.toInt() -> EMF()
                Picture.WMF.toInt() -> WMF()
                Picture.PICT.toInt() -> PICT()
                Picture.JPEG.toInt() -> JPEG()
                Picture.PNG.toInt() -> PNG()
                Picture.DIB.toInt() -> DIB()
                else -> throw IllegalArgumentException("Unsupported picture type: $type")
            }
        }
    }
}
