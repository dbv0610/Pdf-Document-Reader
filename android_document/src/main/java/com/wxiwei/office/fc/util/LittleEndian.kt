package com.wxiwei.office.fc.util

import java.io.IOException
import java.io.InputStream

object LittleEndian : LittleEndianConsts {
    @JvmStatic fun getShort(data: ByteArray, offset: Int): Short = ((data[offset].toInt() and 0xff) or ((data[offset + 1].toInt() and 0xff) shl 8)).toShort()
    @JvmStatic fun getUShort(data: ByteArray, offset: Int): Int = (data[offset].toInt() and 0xff) or ((data[offset + 1].toInt() and 0xff) shl 8)
    @JvmStatic fun getShort(data: ByteArray): Short = getShort(data, 0)
    @JvmStatic fun getUShort(data: ByteArray): Int = getUShort(data, 0)
    @JvmStatic fun getInt(data: ByteArray, offset: Int): Int = (data[offset].toInt() and 0xff) or ((data[offset + 1].toInt() and 0xff) shl 8) or ((data[offset + 2].toInt() and 0xff) shl 16) or ((data[offset + 3].toInt() and 0xff) shl 24)
    @JvmStatic fun getInt(data: ByteArray): Int = getInt(data, 0)
    @JvmStatic fun getUInt(data: ByteArray, offset: Int): Long = getInt(data, offset).toLong() and 0xffffffffL
    @JvmStatic fun getUInt(data: ByteArray): Long = getUInt(data, 0)
    @JvmStatic fun getLong(data: ByteArray, offset: Int): Long {
        var result = 0L
        for (j in offset + LittleEndianConsts.LONG_SIZE - 1 downTo offset) result = (result shl 8) or (data[j].toLong() and 0xff)
        return result
    }
    @JvmStatic fun getFloat(data: ByteArray, offset: Int): Float = java.lang.Float.intBitsToFloat(getInt(data, offset))
    @JvmStatic fun getDouble(data: ByteArray, offset: Int): Double = java.lang.Double.longBitsToDouble(getLong(data, offset))
    @JvmStatic fun putShort(data: ByteArray, offset: Int, value: Short) { data[offset] = value.toInt().toByte(); data[offset + 1] = (value.toInt() ushr 8).toByte() }
    @JvmStatic fun putByte(data: ByteArray, offset: Int, value: Int) { data[offset] = value.toByte() }
    @JvmStatic fun putUShort(data: ByteArray, offset: Int, value: Int) { data[offset] = value.toByte(); data[offset + 1] = (value ushr 8).toByte() }
    @JvmStatic fun putShort(data: ByteArray, value: Short) = putShort(data, 0, value)
    @JvmStatic fun putInt(data: ByteArray, offset: Int, value: Int) { for (i in 0 until 4) data[offset + i] = (value ushr (i * 8)).toByte() }
    @JvmStatic fun putInt(data: ByteArray, value: Int) = putInt(data, 0, value)
    @JvmStatic fun putLong(data: ByteArray, offset: Int, value: Long) { for (i in 0 until LittleEndianConsts.LONG_SIZE) data[offset + i] = (value ushr (i * 8)).toByte() }
    @JvmStatic fun putDouble(data: ByteArray, offset: Int, value: Double) = putLong(data, offset, value.toBits())

    class BufferUnderrunException : IOException("buffer underrun")

    @JvmStatic @Throws(IOException::class) fun readShort(stream: InputStream): Short = readUShort(stream).toShort()
    @JvmStatic @Throws(IOException::class) fun readUShort(stream: InputStream): Int { val a = stream.read(); val b = stream.read(); if (a < 0 || b < 0) throw BufferUnderrunException(); return a or (b shl 8) }
    @JvmStatic @Throws(IOException::class) fun readInt(stream: InputStream): Int { var result = 0; for (i in 0 until 4) { val b = stream.read(); if (b < 0) throw BufferUnderrunException(); result = result or (b shl (i * 8)) }; return result }
    @JvmStatic @Throws(IOException::class) fun readLong(stream: InputStream): Long { var result = 0L; for (i in 0 until 8) { val b = stream.read(); if (b < 0) throw BufferUnderrunException(); result = result or (b.toLong() shl (i * 8)) }; return result }
    @JvmStatic fun ubyteToInt(b: Byte): Int = b.toInt() and 0xff
    @JvmStatic fun getUnsignedByte(data: ByteArray, offset: Int): Int = data[offset].toInt() and 0xff
    @JvmStatic fun getByteArray(data: ByteArray, offset: Int, size: Int): ByteArray = data.copyOfRange(offset, offset + size)
}
