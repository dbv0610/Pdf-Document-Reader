/*
 * Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * Proprietary and confidential. Unauthorized copying, modification or distribution of this
 * file, via any medium, is strictly prohibited without the written permission of dongb2002.
 */
package com.reader.pdfviewer.util

import java.io.ByteArrayOutputStream
import java.io.File
import java.lang.ref.SoftReference
import java.nio.ByteBuffer
import java.security.MessageDigest

/**
 * A TrueType font cut down to the glyphs of a text. pdfium embeds a font whole, so a line of
 * text added with a system font would add megabytes to the file; the subset is a few kilobytes.
 * Glyphs keep their numbers (those not used are emptied, those past the last one used dropped),
 * tables only needed to shape or vary the font are left out.
 */
object FontSubset {
    private var last: Pair<String, SoftReference<ByteArray>>? = null

    /**
     * [font] with only the glyphs of [text], as a file in [folder] (made once for the same
     * characters), or null when it cannot be made: not a TrueType font with outlines, or unreadable.
     */
    @Synchronized
    fun file(font: File, text: String, folder: File): File? = try {
        val codePoints = sortedSetOf(' '.code).apply { var i = 0; while (i < text.length) { val c = text.codePointAt(i); add(c); i += Character.charCount(c) } }
        val key = MessageDigest.getInstance("SHA-1").digest("${font.path}:${font.length()}:${font.lastModified()}:$codePoints".toByteArray())
            .joinToString("") { "%02x".format(it) }
        val out = File(folder, "$key.ttf")
        if (out.length() > 0) out else {
            val data = last?.takeIf { it.first == font.path }?.second?.get() ?: font.readBytes().also { last = font.path to SoftReference(it) }
            subset(data, codePoints)?.let { bytes ->
                folder.mkdirs()
                val temp = File(folder, "$key.tmp")
                temp.writeBytes(bytes)
                if (temp.renameTo(out)) out else { temp.delete(); null }
            }
        }
    } catch (e: Exception) {
        null
    } catch (e: OutOfMemoryError) {
        null
    }

    private class Table(val offset: Int, val length: Int)

    /** The subset of the font in [data] for [codePoints], or null when the font is not one this handles. */
    fun subset(data: ByteArray, codePoints: Set<Int>): ByteArray? {
        val b = ByteBuffer.wrap(data)
        fun u16(at: Int) = b.getShort(at).toInt() and 0xFFFF
        fun u32(at: Int) = b.getInt(at).toLong() and 0xFFFFFFFFL
        if (data.size < 12 || (b.getInt(0) != 0x00010000 && b.getInt(0) != 0x74727565)) return null
        val tables = HashMap<String, Table>()
        for (i in 0 until u16(4)) {
            val at = 12 + i * 16
            val table = Table(u32(at + 8).toInt(), u32(at + 12).toInt())
            if (table.offset < 0 || table.length < 0 || table.offset.toLong() + table.length > data.size) return null
            tables[String(data, at, 4, Charsets.ISO_8859_1)] = table
        }
        val head = tables["head"] ?: return null
        val hhea = tables["hhea"] ?: return null
        val maxp = tables["maxp"] ?: return null
        val hmtx = tables["hmtx"] ?: return null
        val loca = tables["loca"] ?: return null
        val glyf = tables["glyf"] ?: return null
        val cmap = tables["cmap"] ?: return null
        val glyphCount = u16(maxp.offset + 4)
        val longLoca = b.getShort(head.offset + 50).toInt() == 1
        fun glyphStart(g: Int) = if (longLoca) u32(loca.offset + g * 4).toInt() else u16(loca.offset + g * 2) * 2

        val lookup = glyphLookup(b, cmap) ?: return null
        val mapped = java.util.TreeMap<Int, Int>()
        for (c in codePoints) lookup(c).takeIf { it in 1 until glyphCount }?.let { mapped[c] = it }
        // the glyphs of the text, and the parts composite glyphs are made of
        val used = java.util.TreeSet<Int>().apply { add(0); addAll(mapped.values) }
        val todo = ArrayDeque(used)
        while (todo.isNotEmpty()) {
            val g = todo.removeFirst()
            val start = glyf.offset + glyphStart(g)
            val end = glyf.offset + glyphStart(g + 1)
            if (end - start < 10 || end > glyf.offset + glyf.length || b.getShort(start) >= 0) continue
            var at = start + 10
            while (at + 4 <= end) {
                val flags = u16(at)
                val part = u16(at + 2)
                if (part < glyphCount && used.add(part)) todo.add(part)
                at += 4 + (if (flags and 0x1 != 0) 4 else 2) + when {
                    flags and 0x80 != 0 -> 8
                    flags and 0x40 != 0 -> 4
                    flags and 0x8 != 0 -> 2
                    else -> 0
                }
                if (flags and 0x20 == 0) break
            }
        }
        val count = used.last() + 1

        val newGlyf = ByteArrayOutputStream()
        val newLoca = ByteBuffer.allocate((count + 1) * 4)
        for (g in 0 until count) {
            newLoca.putInt(newGlyf.size())
            if (g !in used) continue
            val start = glyphStart(g)
            val end = glyphStart(g + 1)
            if (end <= start || end > glyf.length) continue
            newGlyf.write(data, glyf.offset + start, end - start)
            while (newGlyf.size() % 4 != 0) newGlyf.write(0)
        }
        newLoca.putInt(newGlyf.size())

        val metrics = u16(hhea.offset + 34)
        val newMetrics = minOf(metrics, count)
        val hmtxLength = newMetrics * 4 + (count - newMetrics) * 2
        if (hmtxLength > hmtx.length) return null

        val out = LinkedHashMap<String, ByteArray>()
        fun copy(tag: String) = tables[tag]?.let { data.copyOfRange(it.offset, it.offset + it.length) }
        out["head"] = copy("head")!!.also { ByteBuffer.wrap(it).putInt(8, 0).putShort(50, 1) }
        out["hhea"] = copy("hhea")!!.also { ByteBuffer.wrap(it).putShort(34, newMetrics.toShort()) }
        out["maxp"] = copy("maxp")!!.also { ByteBuffer.wrap(it).putShort(4, count.toShort()) }
        out["hmtx"] = data.copyOfRange(hmtx.offset, hmtx.offset + hmtxLength)
        out["loca"] = newLoca.array()
        out["glyf"] = newGlyf.toByteArray()
        out["cmap"] = unicodeCmap(mapped)
        // version 3: no glyph names
        tables["post"]?.takeIf { it.length >= 32 }?.let { out["post"] = data.copyOfRange(it.offset, it.offset + 32).also { p -> ByteBuffer.wrap(p).putInt(0, 0x00030000) } }
        for (tag in listOf("OS/2", "name", "cvt ", "fpgm", "prep", "gasp")) copy(tag)?.let { out[tag] = it }
        return write(b.getInt(0), out)
    }

    /** The glyph of a code point (0: none), from the Unicode subtable of the cmap table. */
    private fun glyphLookup(b: ByteBuffer, cmap: Table): ((Int) -> Int)? {
        fun u16(at: Int) = b.getShort(at).toInt() and 0xFFFF
        var best = -1
        var bestRank = 0
        for (i in 0 until u16(cmap.offset + 2)) {
            val at = cmap.offset + 4 + i * 8
            val platform = u16(at)
            val encoding = u16(at + 2)
            val sub = cmap.offset + b.getInt(at + 4)
            if (sub < cmap.offset || sub + 4 > cmap.offset + cmap.length) continue
            val format = u16(sub)
            val rank = when {
                format == 12 && (platform == 3 && encoding == 10 || platform == 0) -> 3
                format == 4 && platform == 3 && encoding == 1 -> 2
                format == 4 && platform == 0 -> 1
                else -> 0
            }
            if (rank > bestRank) { bestRank = rank; best = sub }
        }
        if (best < 0) return null
        if (u16(best) == 12) {
            val groups = b.getInt(best + 12)
            return { c ->
                var low = 0
                var high = groups - 1
                var glyph = 0
                while (low <= high) {
                    val mid = (low + high) ushr 1
                    val at = best + 16 + mid * 12
                    val start = b.getInt(at)
                    val end = b.getInt(at + 4)
                    if (c < start) high = mid - 1 else if (c > end) low = mid + 1 else { glyph = b.getInt(at + 8) + (c - start); break }
                }
                glyph
            }
        }
        val segments = u16(best + 6) / 2
        val ends = best + 14
        val starts = ends + segments * 2 + 2
        val deltas = starts + segments * 2
        val offsets = deltas + segments * 2
        return look@{ c ->
            if (c > 0xFFFF) return@look 0
            for (i in 0 until segments) {
                if (u16(ends + i * 2) < c) continue
                val start = u16(starts + i * 2)
                if (start > c) return@look 0
                val delta = u16(deltas + i * 2)
                val offset = u16(offsets + i * 2)
                if (offset == 0) return@look (c + delta) and 0xFFFF
                val glyph = u16(offsets + i * 2 + offset + (c - start) * 2)
                return@look if (glyph == 0) 0 else (glyph + delta) and 0xFFFF
            }
            0
        }
    }

    /** A cmap table with one Unicode subtable (format 12) for [mapped]: code point to glyph. */
    private fun unicodeCmap(mapped: java.util.SortedMap<Int, Int>): ByteArray {
        val groups = ArrayList<IntArray>() // first code point, last, glyph of the first
        for ((c, g) in mapped) {
            val group = groups.lastOrNull()
            if (group != null && c == group[1] + 1 && g == group[2] + (c - group[0])) group[1] = c else groups += intArrayOf(c, c, g)
        }
        val length = 16 + groups.size * 12
        val out = ByteBuffer.allocate(12 + length)
        out.putShort(0).putShort(1).putShort(3).putShort(10).putInt(12)
        out.putShort(12).putShort(0).putInt(length).putInt(0).putInt(groups.size)
        for (group in groups) out.putInt(group[0]).putInt(group[1]).putInt(group[2])
        return out.array()
    }

    private fun checksum(bytes: ByteArray): Int {
        var sum = 0
        var i = 0
        while (i < bytes.size) {
            var word = 0
            for (k in 0 until 4) word = (word shl 8) or (if (i + k < bytes.size) bytes[i + k].toInt() and 0xFF else 0)
            sum += word
            i += 4
        }
        return sum
    }

    private fun write(version: Int, tables: Map<String, ByteArray>): ByteArray {
        val tags = tables.keys.sorted()
        var offset = 12 + tags.size * 16
        var size = offset
        for (tag in tags) size += (tables[tag]!!.size + 3) and 3.inv()
        val out = ByteBuffer.allocate(size)
        val power = Integer.highestOneBit(tags.size)
        out.putInt(version).putShort(tags.size.toShort()).putShort((power * 16).toShort())
            .putShort(Integer.numberOfTrailingZeros(power).toShort()).putShort((tags.size * 16 - power * 16).toShort())
        var headAt = -1
        for (tag in tags) {
            val bytes = tables[tag]!!
            out.put(tag.toByteArray(Charsets.ISO_8859_1)).putInt(checksum(bytes)).putInt(offset).putInt(bytes.size)
            if (tag == "head") headAt = offset
            offset += (bytes.size + 3) and 3.inv()
        }
        for (tag in tags) {
            val bytes = tables[tag]!!
            out.put(bytes)
            repeat(((bytes.size + 3) and 3.inv()) - bytes.size) { out.put(0) }
        }
        val result = out.array()
        if (headAt >= 0) ByteBuffer.wrap(result).putInt(headAt + 8, 0xB1B0AFBA.toInt() - checksum(result))
        return result
    }
}
