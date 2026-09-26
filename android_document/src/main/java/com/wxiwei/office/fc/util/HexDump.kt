package com.wxiwei.office.fc.util

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.BufferedInputStream
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.PrintStream
import java.text.DecimalFormat

object HexDump {
    @JvmField val EOL: String = System.getProperty("line.separator")
    private val hexcodes = "0123456789ABCDEF".toCharArray()
    private val shifts = intArrayOf(60, 56, 52, 48, 44, 40, 36, 32, 28, 24, 20, 16, 12, 8, 4, 0)

    @JvmStatic
    @Throws(IOException::class)
    fun dump(data: ByteArray, offset: Long, stream: OutputStream?, index: Int, length: Int) {
        if (data.isEmpty()) { stream!!.write(("No Data" + System.getProperty("line.separator")).toByteArray()); stream.flush(); return }
        if (index < 0 || index >= data.size) throw ArrayIndexOutOfBoundsException("illegal index: $index into array of length ${data.size}")
        if (stream == null) throw IllegalArgumentException("cannot write to nullstream")
        var displayOffset = offset + index
        val end = minOf(data.size, index + length)
        for (j in index until end step 16) {
            val charsRead = minOf(16, end - j)
            val buffer = StringBuilder(74)
            buffer.append(dump(displayOffset)).append(' ')
            for (k in 0 until 16) { if (k < charsRead) buffer.append(dump(data[k + j])) else buffer.append("  "); buffer.append(' ') }
            for (k in 0 until charsRead) buffer.append(if (data[k + j].toInt() >= ' '.code && data[k + j].toInt() < 127) data[k + j].toInt().toChar() else '.')
            buffer.append(EOL)
            stream.write(buffer.toString().toByteArray())
            stream.flush()
            displayOffset += charsRead
        }
    }

    @JvmStatic @Synchronized @Throws(IOException::class)
    fun dump(data: ByteArray, offset: Long, stream: OutputStream?, index: Int) = dump(data, offset, stream, index, data.size - index)

    @JvmStatic fun dump(data: ByteArray, offset: Long, index: Int): String {
        if (index < 0 || index >= data.size) throw ArrayIndexOutOfBoundsException("illegal index: $index into array of length ${data.size}")
        var displayOffset = offset + index
        val buffer = StringBuilder(74)
        for (j in index until data.size step 16) {
            val charsRead = minOf(16, data.size - j)
            buffer.append(dump(displayOffset)).append(' ')
            for (k in 0 until 16) { if (k < charsRead) buffer.append(dump(data[k + j])) else buffer.append("  "); buffer.append(' ') }
            for (k in 0 until charsRead) buffer.append(if (data[k + j].toInt() >= ' '.code && data[k + j].toInt() < 127) data[k + j].toInt().toChar() else '.')
            buffer.append(EOL)
            displayOffset += charsRead
        }
        return buffer.toString()
    }

    private fun dump(value: Long): String = buildString { for (j in 0 until 8) append(hexcodes[((value ushr shifts[j + 8]) and 15).toInt()]) }
    private fun dump(value: Byte): String = buildString { for (j in 0 until 2) append(hexcodes[((value.toLong() ushr shifts[j + 6]) and 15).toInt()]) }
    @JvmStatic fun toHex(value: ByteArray): String = value.joinToString(", ", "[", "]") { toHex(it) }
    @JvmStatic fun toHex(value: ShortArray): String = value.joinToString(", ", "[", "]") { toHex(it) }
    @JvmStatic fun toHex(value: ByteArray, bytesPerLine: Int): String { val digits = Math.round(Math.log(value.size.toDouble()) / Math.log(10.0) + 0.5).toInt(); val format = DecimalFormat("0".repeat(digits) + ": "); val result = StringBuilder(format.format(0)); var i = -1; for (x in value.indices) { if (++i == bytesPerLine) { result.append('\n').append(format.format(x)); i = 0 }; result.append(toHex(x)).append(", ") }; return result.toString() }
    @JvmStatic fun toHex(value: Short): String = toHex(value.toLong(), 4)
    @JvmStatic fun toHex(value: Byte): String = toHex(value.toLong(), 2)
    @JvmStatic fun toHex(value: Int): String = toHex(value.toLong(), 8)
    @JvmStatic fun toHex(value: Long): String = toHex(value, 16)
    private fun toHex(value: Long, digits: Int): String = buildString { for (j in 0 until digits) append(hexcodes[((value ushr shifts[j + (16 - digits)]) and 15).toInt()]) }

    @JvmStatic @Throws(IOException::class)
    fun dump(input: InputStream, out: PrintStream, start: Int, bytesToDump: Int) { val buffer = ByteArrayOutputStream(); var remaining = bytesToDump; while (bytesToDump == -1 || remaining-- > 0) { val c = input.read(); if (c == -1) break; buffer.write(c) }; val data = buffer.toByteArray(); dump(data, 0, out, start, data.size) }
    private fun toHexChars(value: Long, bytes: Int): CharArray { var pos = 2 + bytes * 2; val result = CharArray(pos); var v = value; do { result[--pos] = hexcodes[(v and 15).toInt()]; v = v ushr 4 } while (pos > 1); result[0] = '0'; result[1] = 'x'; return result }
    @JvmStatic fun longToHex(value: Long): CharArray = toHexChars(value, 8)
    @JvmStatic fun intToHex(value: Int): CharArray = toHexChars(value.toLong(), 4)
    @JvmStatic fun shortToHex(value: Int): CharArray = toHexChars(value.toLong(), 2)
    @JvmStatic fun byteToHex(value: Int): CharArray = toHexChars(value.toLong(), 1)
    @JvmStatic fun main(args: Array<String>) { val file = File(args[0]); val input = BufferedInputStream(FileInputStream(file)); val bytes = ByteArray(file.length().toInt()); input.read(bytes); println(dump(bytes, 0, 0)); input.close() }
}
