package com.wxiwei.office.fc.util

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

abstract class LZWDecompresser(
    private val maskMeansCompressed: Boolean,
    private val codeLengthIncrease: Int,
    private val positionIsBigEndian: Boolean
) {
    protected abstract fun populateDictionary(dict: ByteArray): Int
    protected abstract fun adjustDictionaryOffset(offset: Int): Int

    @Throws(IOException::class)
    fun decompress(src: InputStream): ByteArray = ByteArrayOutputStream().also { decompress(src, it) }.toByteArray()

    @Throws(IOException::class)
    fun decompress(src: InputStream, res: OutputStream) {
        val buffer = ByteArray(4096)
        var pos = populateDictionary(buffer)
        val dataB = ByteArray(16 + codeLengthIncrease)
        var flag: Int
        while (src.read().also { flag = it } != -1) {
            var mask = 1
            while (mask < 256) {
                val set = flag and mask > 0
                if (set xor maskMeansCompressed) {
                    val data = src.read()
                    if (data != -1) { buffer[pos and 4095] = fromInt(data); pos++; res.write(fromInt(data).toInt()) }
                } else {
                    val p1 = src.read(); val p2 = src.read()
                    if (p1 == -1 || p2 == -1) break
                    val len = (p2 and 15) + codeLengthIncrease
                    var pointer = if (positionIsBigEndian) (p1 shl 4) + (p2 shr 4) else p1 + ((p2 and 0xf0) shl 4)
                    pointer = adjustDictionaryOffset(pointer)
                    for (i in 0 until len) { dataB[i] = buffer[(pointer + i) and 4095]; buffer[(pos + i) and 4095] = dataB[i] }
                    res.write(dataB, 0, len); pos += len
                }
                mask = mask shl 1
            }
        }
    }

    companion object {
        @JvmStatic fun fromInt(b: Int): Byte = if (b < 128) b.toByte() else (b - 256).toByte()
        @JvmStatic fun fromByte(b: Byte): Int = if (b >= 0) b.toInt() else b + 256
    }
}
