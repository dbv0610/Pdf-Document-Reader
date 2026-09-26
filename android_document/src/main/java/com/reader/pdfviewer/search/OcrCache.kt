package com.reader.pdfviewer.search

import android.graphics.RectF
import android.util.Log
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.RandomAccessFile
import java.security.MessageDigest

/**
 * OCR results of one PDF file kept in the app's internal storage, so a page is recognized once
 * and not every time the file is opened. Pages are keyed by document page index.
 *
 * The file is identified by its size, modification time and first bytes, not its path, so a
 * renamed or moved file keeps its results while a changed one (e.g. annotations saved) starts
 * over. Each page is appended as it is recognized; a record cut short by the process dying is
 * ignored. The directory is capped at [maxBytes], dropping the files used least recently.
 */
internal class OcrCache(
    private val dir: File,
    private val file: File,
    private val maxBytes: Long = DEFAULT_MAX_BYTES,
) {
    private val entries = HashMap<Int, List<OcrLine>>()
    private var storage: File? = null
    private var loaded = false

    /** Recognized lines of document page [docPage], null when it was never recognized. */
    @Synchronized
    fun get(docPage: Int): List<OcrLine>? {
        load()
        return entries[docPage]
    }

    /** Keeps the lines of document page [docPage]; call only with a successful recognition. */
    @Synchronized
    fun put(docPage: Int, lines: List<OcrLine>) {
        load()
        val target = storage ?: return
        entries[docPage] = lines
        try {
            val header = if (target.isFile && target.length() > 0) ByteArray(0) else header()
            // One write per page: appends of a record never interleave with a partial one.
            FileOutputStream(target, true).use { it.write(header + record(docPage, lines)) }
            trim(keep = target)
        } catch (e: IOException) {
            Log.w(TAG, "Cannot store OCR of page $docPage", e)
        }
    }

    // Deferred to the first use, which runs off the main thread: it reads the file's first bytes.
    private fun load() {
        if (loaded) return
        loaded = true
        val key = try {
            key()
        } catch (e: IOException) {
            Log.w(TAG, "Cannot identify ${file.name}, OCR results are not kept", e)
            return
        }
        if (!dir.isDirectory && !dir.mkdirs()) return
        val target = File(dir, "$key$EXTENSION")
        storage = target
        if (!target.isFile) return
        target.setLastModified(System.currentTimeMillis()) // most recently used, for trim
        try {
            val bytes = target.readBytes()
            val valid = read(bytes)
            // Cut a record left half written, or the next append would land after garbage.
            if (valid < bytes.size) RandomAccessFile(target, "rw").use { it.setLength(valid.toLong()) }
        } catch (e: IOException) {
            Log.w(TAG, "Cannot read OCR cache of ${file.name}, starting over", e)
            entries.clear()
            target.delete()
        }
    }

    private fun key(): String {
        val digest = MessageDigest.getInstance("SHA-1")
        digest.update("${file.length()}:${file.lastModified()}:".toByteArray())
        file.inputStream().use { input ->
            val buffer = ByteArray(IDENTITY_BYTES)
            var read = 0
            while (read < buffer.size) {
                val count = input.read(buffer, read, buffer.size - read)
                if (count < 0) break
                read += count
            }
            digest.update(buffer, 0, read)
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    private fun header(): ByteArray = ByteArrayOutputStream().also { bytes ->
        DataOutputStream(bytes).use { it.writeInt(MAGIC); it.writeInt(VERSION) }
    }.toByteArray()

    private fun record(docPage: Int, lines: List<OcrLine>): ByteArray {
        val payload = ByteArrayOutputStream()
        DataOutputStream(payload).use { out ->
            out.writeInt(docPage)
            out.writeInt(lines.size)
            for (line in lines) {
                val text = line.text.toByteArray(Charsets.UTF_8)
                out.writeInt(text.size)
                out.write(text)
                out.writeInt(line.words.size)
                for (word in line.words) {
                    out.writeInt(word.start)
                    out.writeInt(word.end)
                    out.writeFloat(word.box.left)
                    out.writeFloat(word.box.top)
                    out.writeFloat(word.box.right)
                    out.writeFloat(word.box.bottom)
                }
            }
        }
        val bytes = ByteArrayOutputStream(payload.size() + 4)
        DataOutputStream(bytes).use { it.writeInt(payload.size()); payload.writeTo(it) }
        return bytes.toByteArray()
    }

    /** Reads the records into [entries]; returns how many bytes of [bytes] hold whole records. */
    private fun read(bytes: ByteArray): Int {
        val input = DataInputStream(ByteArrayInputStream(bytes))
        if (input.readInt() != MAGIC || input.readInt() != VERSION) throw IOException("Unknown cache format")
        var valid = HEADER_BYTES
        while (true) {
            if (input.available() < 4) return valid
            val length = input.readInt()
            // A record cut short when the process died: keep what was read before it.
            if (length < 0 || length > input.available()) return valid
            val payload = ByteArray(length)
            input.readFully(payload)
            val page = DataInputStream(ByteArrayInputStream(payload))
            val docPage = page.readInt()
            entries[docPage] = List(page.readInt()) {
                val text = ByteArray(page.readInt()).also(page::readFully).toString(Charsets.UTF_8)
                val words = List(page.readInt()) {
                    OcrWord(page.readInt(), page.readInt(), RectF(page.readFloat(), page.readFloat(), page.readFloat(), page.readFloat()))
                }
                OcrLine(text, words)
            }
            valid += 4 + length
        }
    }

    /** Drops the least recently used files once the directory is over [maxBytes]. */
    private fun trim(keep: File) {
        val files = dir.listFiles { f -> f.isFile && f.name.endsWith(EXTENSION) } ?: return
        var total = files.sumOf { it.length() }
        for (old in files.sortedBy { it.lastModified() }) {
            if (total <= maxBytes) break
            if (old == keep) continue
            total -= old.length()
            old.delete()
        }
    }

    companion object {
        private const val TAG = "OcrCache"
        const val DIR_NAME = "pdf_ocr_cache"
        private const val EXTENSION = ".ocr"
        private const val MAGIC = 0x4F435243 // "OCRC"
        private const val VERSION = 1
        private const val HEADER_BYTES = 8
        private const val IDENTITY_BYTES = 64 * 1024
        private const val DEFAULT_MAX_BYTES = 32L * 1024 * 1024
    }
}
