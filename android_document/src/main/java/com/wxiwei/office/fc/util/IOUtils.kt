package com.wxiwei.office.fc.util

import java.io.ByteArrayOutputStream
import java.io.Closeable
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.channels.ReadableByteChannel
import java.util.zip.CRC32

object IOUtils {
    private val logger = POILogFactory.getLogger(IOUtils::class.java)

    @JvmStatic
    @Throws(IOException::class)
    fun toByteArray(stream: InputStream): ByteArray {
        val out = ByteArrayOutputStream()
        val buffer = ByteArray(4096)
        var read = stream.read(buffer)
        while (read != -1) { if (read > 0) out.write(buffer, 0, read); read = stream.read(buffer) }
        return out.toByteArray()
    }

    @JvmStatic fun toByteArray(buffer: ByteBuffer, length: Int): ByteArray {
        if (buffer.hasArray() && buffer.arrayOffset() == 0) return buffer.array()
        return ByteArray(length).also { buffer.get(it) }
    }

    @JvmStatic @Throws(IOException::class) fun readFully(input: InputStream, b: ByteArray): Int = readFully(input, b, 0, b.size)

    @JvmStatic @Throws(IOException::class)
    fun readFully(input: InputStream, b: ByteArray, off: Int, len: Int): Int {
        var total = 0
        while (true) { val got = input.read(b, off + total, len - total); if (got < 0) return if (total == 0) -1 else total; total += got; if (total == len) return total }
    }

    @JvmStatic @Throws(IOException::class)
    fun readFully(channel: ReadableByteChannel, b: ByteBuffer): Int {
        var total = 0
        while (true) { val got = channel.read(b); if (got < 0) return if (total == 0) -1 else total; total += got; if (total == b.capacity() || b.position() == b.capacity()) return total }
    }

    @JvmStatic @Throws(IOException::class)
    fun copy(input: InputStream, output: OutputStream) { val buffer = ByteArray(4096); var count = input.read(buffer); while (count != -1) { if (count > 0) output.write(buffer, 0, count); count = input.read(buffer) } }

    @JvmStatic fun calculateChecksum(data: ByteArray): Long = CRC32().apply { update(data, 0, data.size) }.value

    @JvmStatic fun closeQuietly(closeable: Closeable?) { try { closeable?.close() } catch (e: Exception) { logger.log(POILogger.ERROR, "Unable to close resource: $e", e) } }
}
