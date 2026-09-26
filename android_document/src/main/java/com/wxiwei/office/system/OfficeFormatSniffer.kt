package com.wxiwei.office.system

import java.io.File
import java.io.IOException
import java.io.RandomAccessFile

class BadFileFormatException(message: String) : Exception("Format error: $message")

/** Bounded signature checks only; the selected parser still validates the document. */
object OfficeFormatSniffer {
    enum class Format { ZIP, OLE2, PDF, RTF, UNKNOWN }

    fun sniff(file: File): Format = try {
        RandomAccessFile(file, "r").use { input ->
            if (input.length() < 8) return Format.UNKNOWN
            val header = ByteArray(8)
            input.readFully(header)
            when {
                matches(header, 0, 0x50, 0x4B, 0x03, 0x04) ||
                    matches(header, 0, 0x50, 0x4B, 0x05, 0x06) -> Format.ZIP
                matches(header, 0, 0xD0, 0xCF, 0x11, 0xE0, 0xA1, 0xB1, 0x1A, 0xE1) -> Format.OLE2
                matches(header, 0, 0x7B, 0x5C, 0x72, 0x74, 0x66) -> Format.RTF
                else -> {
                    val prefix = ByteArray(minOf(input.length(), 1024L).toInt())
                    header.copyInto(prefix)
                    input.readFully(prefix, header.size, prefix.size - header.size)
                    if ((0..prefix.size - 4).any { matches(prefix, it, 0x25, 0x50, 0x44, 0x46) }) {
                        Format.PDF
                    } else Format.UNKNOWN
                }
            }
        }
    } catch (_: IOException) {
        Format.UNKNOWN
    } catch (_: SecurityException) {
        Format.UNKNOWN
    }

    fun hasZipEndRecord(file: File): Boolean = try {
        RandomAccessFile(file, "r").use { input ->
            val size = input.length()
            val tail = ByteArray(minOf(size, 65557L).toInt())
            input.seek(size - tail.size)
            input.readFully(tail)
            (tail.size - 4 downTo 0).any { matches(tail, it, 0x50, 0x4B, 0x05, 0x06) }
        }
    } catch (_: IOException) {
        false
    } catch (_: SecurityException) {
        false
    }

    internal fun firstBytes(file: File): String = try {
        RandomAccessFile(file, "r").use { input ->
            val bytes = ByteArray(minOf(input.length(), 8L).toInt())
            input.readFully(bytes)
            if (bytes.isEmpty()) "empty" else bytes.joinToString(" ") {
                (it.toInt() and 0xFF).toString(16).uppercase().padStart(2, '0')
            }
        }
    } catch (_: IOException) {
        "unreadable"
    } catch (_: SecurityException) {
        "unreadable"
    }

    private fun matches(bytes: ByteArray, offset: Int, vararg signature: Int): Boolean =
        offset >= 0 && offset + signature.size <= bytes.size &&
            signature.indices.all { (bytes[offset + it].toInt() and 0xFF) == signature[it] }
}
