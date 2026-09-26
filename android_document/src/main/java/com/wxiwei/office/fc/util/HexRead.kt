package com.wxiwei.office.fc.util

import java.io.ByteArrayInputStream
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream

object HexRead {
    @JvmStatic
    @Throws(IOException::class)
    fun readData(filename: String): ByteArray = FileInputStream(filename).use { readData(it, -1) }

    @JvmStatic
    @Throws(IOException::class)
    fun readData(stream: InputStream, section: String): ByteArray {
        val text = StringBuilder()
        var inSection = false
        var c = stream.read()
        while (c != -1) {
            when (c) {
                '['.code -> inSection = true
                '\n'.code, '\r'.code -> { inSection = false; text.setLength(0) }
                ']' .code -> {
                    inSection = false
                    if (text.toString() == section) return readData(stream, '['.code)
                    text.setLength(0)
                }
                else -> if (inSection) text.append(c.toChar())
            }
            c = stream.read()
        }
        stream.close()
        throw IOException("Section '$section' not found")
    }

    @JvmStatic
    @Throws(IOException::class)
    fun readData(filename: String, section: String): ByteArray = FileInputStream(filename).use { readData(it, section) }

    @JvmStatic
    @Throws(IOException::class)
    fun readData(stream: InputStream, eofChar: Int): ByteArray {
        val result = ArrayList<Byte>()
        var countChars = 0
        var value = 0
        while (true) {
            val c = stream.read()
            if (c == eofChar) break
            when {
                c == '#'.code -> readToEol(stream)
                c in '0'.code..'9'.code -> { value = (value shl 4) + c - '0'.code; countChars++ }
                c in 'A'.code..'F'.code -> { value = (value shl 4) + c - 'A'.code + 10; countChars++ }
                c in 'a'.code..'f'.code -> { value = (value shl 4) + c - 'a'.code + 10; countChars++ }
                c == -1 -> break
            }
            if (countChars == 2) { result.add(value.toByte()); countChars = 0; value = 0 }
        }
        return result.toByteArray()
    }

    @JvmStatic
    fun readFromString(data: String): ByteArray = try { readData(ByteArrayInputStream(data.toByteArray()), -1) } catch (e: IOException) { throw RuntimeException(e) }

    private fun readToEol(stream: InputStream) {
        var c = stream.read()
        while (c != -1 && c != '\n'.code && c != '\r'.code) c = stream.read()
    }
}
