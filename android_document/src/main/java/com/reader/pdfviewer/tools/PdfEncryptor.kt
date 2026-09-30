/*
 * Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * Proprietary and confidential. Unauthorized copying, modification or distribution of this
 * file, via any medium, is strictly prohibited without the written permission of dongb2002.
 */
package com.reader.pdfviewer.tools

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.io.RandomAccessFile
import java.math.BigInteger
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.security.MessageDigest
import java.security.SecureRandom
import java.text.Normalizer
import java.util.concurrent.CancellationException
import java.util.zip.Inflater
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Writes an encrypted copy of a PDF that has no security, with the standard security handler and
 * AES 256 (V 5, R 6 of ISO 32000-2). It reads classic cross-reference tables, cross-reference
 * streams with object streams, and their /Prev chain; it writes every object on its own, followed
 * by a classic cross-reference table.
 */
internal object PdfEncryptor {
    // permission bits (ISO 32000-2 table 22)
    const val PRINT = 1 shl 2
    const val COPY = 1 shl 4
    const val PRINT_HIGH = 1 shl 11
    /** Every right: bits 1 and 2 clear, all others set. */
    const val ALL = -4

    /**
     * Writes [input] to [output], asking [userPassword] to open it; [ownerPassword] also opens it,
     * with every right. [permissions] are the rights of the user password (see [ALL]).
     * [isCancelled] is checked between objects; when true a [CancellationException] is thrown.
     */
    fun encrypt(input: File, output: File, userPassword: String, ownerPassword: String,
                permissions: Int = ALL, isCancelled: () -> Boolean = { false }) {
        RandomAccessFile(input, "r").use { file ->
            if (file.length() > Int.MAX_VALUE) throw IOException("File too large")
            val reader = Reader(file.channel.map(FileChannel.MapMode.READ_ONLY, 0, file.length()))
            val trailer = reader.trailer
            if (trailer["Encrypt"] != null) throw IOException("The document is already encrypted")
            val root = trailer["Root"] as? Ref ?: throw IOException("No document catalog")
            val security = Security(userPassword, ownerPassword, permissions)
            FileOutputStream(output).buffered(1 shl 16).use { stream ->
                val out = Counting(stream)
                out.ascii("%PDF-${reader.version()}\n")
                out.write(byteArrayOf('%'.code.toByte(), 0xE2.toByte(), 0xE3.toByte(), 0xCF.toByte(), 0xD3.toByte(), '\n'.code.toByte()))
                val offsets = sortedMapOf<Int, Pair<Long, Int>>()
                for (num in reader.entries.keys.sorted()) {
                    if (isCancelled()) throw CancellationException()
                    if (num == 0 || reader.entries.getValue(num).type == 0) continue
                    val item = reader.load(num) ?: continue
                    val obj = item.obj
                    if (item.streamStart >= 0) {
                        val type = ((obj as Dict)["Type"] as? Name)?.raw
                        // the old cross-reference data: its objects are written on their own
                        if (type == "XRef" || type == "ObjStm") continue
                    }
                    offsets[num] = out.count to item.gen
                    out.ascii("$num ${item.gen} obj\n")
                    if (item.streamStart < 0) {
                        out.ascii(text(security.strings(obj)))
                    } else {
                        val dict = security.strings(obj) as Dict
                        dict.map["Length"] = Num(Security.encryptedLength(item.streamLength).toString())
                        out.ascii(text(dict))
                        out.ascii("\nstream\n")
                        security.encryptStream(reader.buf, item.streamStart, item.streamLength, out)
                        out.ascii("\nendstream")
                    }
                    out.ascii("\nendobj\n")
                }
                val encryptNum = maxOf((trailer["Size"] as? Num)?.int ?: 0, (reader.entries.keys.maxOrNull() ?: 0) + 1)
                offsets[encryptNum] = out.count to 0
                out.ascii("$encryptNum 0 obj\n${text(security.dictionary())}\nendobj\n")

                val xref = out.count
                val size = encryptNum + 1
                val table = StringBuilder("xref\n0 $size\n")
                for (num in 0 until size) {
                    val at = offsets[num]
                    if (at == null) table.append("0000000000 65535 f\r\n")
                    else table.append(String.format(java.util.Locale.ROOT, "%010d %05d n\r\n", at.first, at.second))
                }
                out.ascii(table.toString())
                val end = Dict(linkedMapOf("Size" to Num(size.toString()), "Root" to root))
                (trailer["Info"] as? Ref)?.let { end.map["Info"] = it }
                val id = (trailer["ID"] as? Arr)?.takeIf { a -> a.items.size == 2 && a.items.all { it is Str } }
                    ?: security.random(16).let { Arr(mutableListOf(Str(it), Str(it))) }
                end.map["ID"] = id
                end.map["Encrypt"] = Ref(encryptNum, 0)
                out.ascii("trailer\n${text(end)}\nstartxref\n$xref\n%%EOF\n")
            }
        }
    }

    // ---- the objects of a PDF ----

    private sealed class Obj
    private class Num(val raw: String) : Obj() {
        val int: Int get() = raw.toLongOrNull()?.toInt() ?: raw.toDouble().toInt()
    }
    /** A name as written, without its slash (#xx escapes kept). */
    private class Name(val raw: String) : Obj()
    private class Str(val bytes: ByteArray) : Obj()
    /** true, false or null. */
    private class Word(val raw: String) : Obj()
    private class Arr(val items: MutableList<Obj>) : Obj()
    private class Dict(val map: LinkedHashMap<String, Obj>) : Obj() {
        operator fun get(key: String) = map[key]
    }
    private class Ref(val num: Int, val gen: Int) : Obj()

    private fun text(obj: Obj): String = StringBuilder().also { write(obj, it) }.toString()

    private fun write(obj: Obj, out: StringBuilder) {
        when (obj) {
            is Num -> out.append(obj.raw)
            is Name -> out.append('/').append(obj.raw)
            is Word -> out.append(obj.raw)
            is Ref -> out.append(obj.num).append(' ').append(obj.gen).append(" R")
            is Str -> {
                out.append('<')
                for (b in obj.bytes) out.append(HEX[(b.toInt() shr 4) and 15]).append(HEX[b.toInt() and 15])
                out.append('>')
            }
            is Arr -> {
                out.append('[')
                obj.items.forEachIndexed { i, item -> if (i > 0) out.append(' '); write(item, out) }
                out.append(']')
            }
            is Dict -> {
                out.append("<<")
                for ((key, value) in obj.map) { out.append('/').append(key).append(' '); write(value, out); out.append(' ') }
                out.append(">>")
            }
        }
    }

    private const val HEX = "0123456789abcdef"

    // ---- reading ----

    private fun isSpace(c: Int) = c == 0 || c == 9 || c == 10 || c == 12 || c == 13 || c == 32
    private fun isDelimiter(c: Int) = c == '('.code || c == ')'.code || c == '<'.code || c == '>'.code || c == '['.code ||
        c == ']'.code || c == '{'.code || c == '}'.code || c == '/'.code || c == '%'.code
    private fun isRegular(c: Int) = !isSpace(c) && !isDelimiter(c)

    private class Lexer(val buf: ByteBuffer, var pos: Int = 0) {
        val end = buf.limit()
        fun at(i: Int) = buf.get(i).toInt() and 0xFF
        fun peek() = if (pos < end) at(pos) else -1

        fun skipSpace() {
            while (pos < end) {
                val c = at(pos)
                if (c == '%'.code) while (pos < end && at(pos) != 10 && at(pos) != 13) pos++
                else if (isSpace(c)) pos++
                else return
            }
        }

        fun ascii(from: Int, to: Int): String {
            val bytes = ByteArray(to - from)
            for (i in bytes.indices) bytes[i] = buf.get(from + i)
            return String(bytes, Charsets.ISO_8859_1)
        }

        /** The next run of regular characters; empty at a delimiter. */
        fun token(): String {
            skipSpace()
            val start = pos
            while (pos < end && isRegular(at(pos))) pos++
            return ascii(start, pos)
        }

        fun readObject(): Obj {
            skipSpace()
            when (peek()) {
                -1 -> throw IOException("Unexpected end of file")
                '/'.code -> {
                    val start = ++pos
                    while (pos < end && isRegular(at(pos))) pos++
                    return Name(ascii(start, pos))
                }
                '('.code -> return Str(literal())
                '<'.code -> return if (pos + 1 < end && at(pos + 1) == '<'.code) dictionary() else Str(hex())
                '['.code -> {
                    pos++
                    val items = mutableListOf<Obj>()
                    while (true) {
                        skipSpace()
                        if (peek() == ']'.code) { pos++; return Arr(items) }
                        items.add(readObject())
                    }
                }
            }
            val word = token()
            if (word.isEmpty()) throw IOException("Unexpected character at $pos")
            val first = word[0]
            if (first.isDigit() || first == '+' || first == '-' || first == '.') {
                if (word.all { it.isDigit() }) {
                    // "num gen R"
                    val save = pos
                    val gen = token()
                    if (gen.isNotEmpty() && gen.all { it.isDigit() } && token() == "R") return Ref(word.toInt(), gen.toInt())
                    pos = save
                }
                return Num(word)
            }
            return Word(word)
        }

        private fun dictionary(): Dict {
            pos += 2
            val map = LinkedHashMap<String, Obj>()
            while (true) {
                skipSpace()
                if (peek() == '>'.code) { pos += 2; return Dict(map) }
                val key = readObject() as? Name ?: throw IOException("Dictionary key expected at $pos")
                map[key.raw] = readObject()
            }
        }

        private fun literal(): ByteArray {
            pos++
            val out = ByteArrayOutputStream()
            var depth = 1
            while (pos < end) {
                val c = at(pos++)
                when (c) {
                    '('.code -> { depth++; out.write(c) }
                    ')'.code -> { if (--depth == 0) return out.toByteArray(); out.write(c) }
                    13 -> { out.write(10); if (peek() == 10) pos++ }
                    '\\'.code -> {
                        if (pos >= end) break
                        when (val e = at(pos++)) {
                            'n'.code -> out.write(10)
                            'r'.code -> out.write(13)
                            't'.code -> out.write(9)
                            'b'.code -> out.write(8)
                            'f'.code -> out.write(12)
                            13 -> if (peek() == 10) pos++
                            10 -> {}
                            in '0'.code..'7'.code -> {
                                var value = e - '0'.code
                                repeat(2) {
                                    val d = peek()
                                    if (d in '0'.code..'7'.code) { value = value * 8 + d - '0'.code; pos++ }
                                }
                                out.write(value and 0xFF)
                            }
                            else -> out.write(e)
                        }
                    }
                    else -> out.write(c)
                }
            }
            throw IOException("Unterminated string")
        }

        private fun hex(): ByteArray {
            pos++
            val out = ByteArrayOutputStream()
            var high = -1
            while (pos < end) {
                val c = at(pos++)
                if (c == '>'.code) {
                    if (high >= 0) out.write(high shl 4)
                    return out.toByteArray()
                }
                val d = Character.digit(c, 16)
                if (d < 0) continue
                if (high < 0) high = d else { out.write(high * 16 + d); high = -1 }
            }
            throw IOException("Unterminated hex string")
        }
    }

    /** An xref entry: type 1 [a] = offset, [b] = generation; type 2 [a] = object stream, [b] = index; type 0 free. */
    private class Entry(val type: Int, val a: Int, val b: Int)

    /** An object read from the file; [streamStart] is -1 unless it is a stream. */
    private class Item(val gen: Int, val obj: Obj, val streamStart: Int = -1, val streamLength: Int = 0)

    private class Reader(val buf: ByteBuffer) {
        val entries = HashMap<Int, Entry>()
        val trailer: Dict
        private val objectStreams = object : LinkedHashMap<Int, Map<Int, Obj>>(8, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Int, Map<Int, Obj>>?) = size > 8
        }

        init {
            trailer = readXref(startXref())
        }

        fun version(): String {
            val lexer = Lexer(buf)
            val head = lexer.ascii(0, minOf(1024, lexer.end))
            val at = head.indexOf("%PDF-")
            val version = if (at >= 0) head.substring(at + 5).takeWhile { it.isDigit() || it == '.' } else ""
            // AES 256 needs PDF 1.7 (Adobe extension level 8) or later
            return if (version.toDoubleOrNull()?.let { it >= 1.7 } == true) version else "1.7"
        }

        private fun startXref(): Int {
            val lexer = Lexer(buf)
            val from = maxOf(0, lexer.end - 2048)
            val tail = lexer.ascii(from, lexer.end)
            val at = tail.lastIndexOf("startxref")
            if (at < 0) throw IOException("No startxref")
            lexer.pos = from + at + "startxref".length
            return lexer.token().toIntOrNull() ?: throw IOException("Bad startxref")
        }

        /** Reads the cross-reference sections from [start] back along /Prev; returns the newest trailer. */
        private fun readXref(start: Int): Dict {
            var newest: Dict? = null
            var offset = start
            val seen = HashSet<Int>()
            while (offset >= 0 && seen.add(offset)) {
                val section = HashMap<Int, Entry>()
                val lexer = Lexer(buf, offset)
                val trailer: Dict
                if (lexer.token() == "xref") {
                    while (true) {
                        val word = lexer.token()
                        if (word == "trailer") break
                        val first = word.toIntOrNull() ?: throw IOException("Bad xref table at $offset")
                        val count = lexer.token().toInt()
                        for (k in 0 until count) {
                            val at = lexer.token().toLong()
                            val gen = lexer.token().toInt()
                            section[first + k] = if (lexer.token() == "n") Entry(1, at.toInt(), gen) else Entry(0, 0, 0)
                        }
                    }
                    trailer = lexer.readObject() as? Dict ?: throw IOException("Bad trailer at $offset")
                    // a hybrid file lists the objects of its object streams in a cross-reference stream too
                    (trailer["XRefStm"] as? Num)?.let { at ->
                        val hidden = HashMap<Int, Entry>()
                        streamEntries(read(at.int), hidden)
                        for ((num, entry) in hidden) if ((section[num]?.type ?: 0) == 0) section[num] = entry
                    }
                } else {
                    val item = read(offset)
                    trailer = item.obj as? Dict ?: throw IOException("Bad xref stream at $offset")
                    streamEntries(item, section)
                }
                // newer sections come first and win
                for ((num, entry) in section) entries.putIfAbsent(num, entry)
                if (newest == null) newest = trailer
                offset = (trailer["Prev"] as? Num)?.int ?: -1
            }
            return newest ?: throw IOException("No trailer")
        }

        private fun streamEntries(item: Item, into: MutableMap<Int, Entry>) {
            val dict = item.obj as Dict
            val data = decode(item)
            val w = (dict["W"] as Arr).items.map { (it as Num).int }
            val size = (dict["Size"] as Num).int
            val index = (dict["Index"] as? Arr)?.items?.map { (it as Num).int } ?: listOf(0, size)
            val width = w.sum()
            var i = 0
            fun field(n: Int): Long {
                var v = 0L
                repeat(n) { v = (v shl 8) or (data[i++].toLong() and 0xFF) }
                return v
            }
            for (pair in index.chunked(2)) for (k in 0 until pair[1]) {
                if (i + width > data.size) return
                val type = if (w[0] == 0) 1 else field(w[0]).toInt()
                val a = field(w[1]).toInt()
                val b = if (w[2] == 0) 0 else field(w[2]).toInt()
                into[pair[0] + k] = when (type) {
                    1 -> Entry(1, a, b)
                    2 -> Entry(2, a, b)
                    else -> Entry(0, 0, 0)
                }
            }
        }

        /** The object [num], or null when the file does not have it. */
        fun load(num: Int): Item? {
            val entry = entries[num] ?: return null
            return when (entry.type) {
                1 -> read(entry.a)
                2 -> objectStream(entry.a)[num]?.let { Item(0, it) }
                else -> null
            }
        }

        private fun resolve(obj: Obj?): Obj? = if (obj is Ref) load(obj.num)?.obj else obj

        /** The indirect object written at [offset]. */
        fun read(offset: Int): Item {
            val lexer = Lexer(buf, offset)
            lexer.token().toIntOrNull() ?: throw IOException("No object at $offset")
            val gen = lexer.token().toIntOrNull() ?: throw IOException("No object at $offset")
            if (lexer.token() != "obj") throw IOException("No object at $offset")
            val obj = lexer.readObject()
            if (obj !is Dict || lexer.token() != "stream") return Item(gen, obj)
            // the data starts after the end of line that follows "stream"
            if (lexer.peek() == 13) lexer.pos++
            if (lexer.peek() == 10) lexer.pos++
            val start = lexer.pos
            val declared = (resolve(obj["Length"]) as? Num)?.int
            val length = if (declared != null && declared >= 0 && start + declared <= lexer.end && endsAt(start + declared)) declared
                else searchEnd(start)
            return Item(gen, obj, start, length)
        }

        private fun endsAt(at: Int): Boolean = Lexer(buf, at).token() == "endstream"

        /** The length of stream data starting at [start], found from its "endstream". */
        private fun searchEnd(start: Int): Int {
            val key = "endstream".toByteArray(Charsets.ISO_8859_1)
            val lexer = Lexer(buf)
            var at = start
            while (at + key.size <= lexer.end) {
                if (key.indices.all { lexer.at(at + it) == key[it].toInt() }) {
                    var end = at
                    if (end > start && lexer.at(end - 1) == 10) end--
                    if (end > start && lexer.at(end - 1) == 13) end--
                    return end - start
                }
                at++
            }
            throw IOException("Unterminated stream at $start")
        }

        private fun objectStream(num: Int): Map<Int, Obj> = objectStreams.getOrPut(num) {
            val item = load(num) ?: throw IOException("Missing object stream $num")
            val dict = item.obj as Dict
            val data = decode(item)
            val lexer = Lexer(ByteBuffer.wrap(data))
            val count = (dict["N"] as Num).int
            val first = (dict["First"] as Num).int
            val offsets = List(count) { lexer.token().toInt() to lexer.token().toInt() }
            offsets.associate { (n, at) -> lexer.pos = first + at; n to lexer.readObject() }
        }

        /** The decoded data of a cross-reference or object stream (Flate, with or without a PNG predictor). */
        private fun decode(item: Item): ByteArray {
            val dict = item.obj as Dict
            var data = ByteArray(item.streamLength)
            buf.duplicate().apply { position(item.streamStart) }.get(data)
            val filters = when (val f = dict["Filter"]) {
                is Name -> listOf(f.raw)
                is Arr -> f.items.map { (it as Name).raw }
                else -> emptyList()
            }
            val parameters = when (val p = dict["DecodeParms"]) {
                is Dict -> listOf(p)
                is Arr -> p.items.map { it as? Dict }
                else -> emptyList()
            }
            filters.forEachIndexed { i, filter ->
                if (filter != "FlateDecode" && filter != "Fl") throw IOException("Unsupported filter $filter")
                data = inflate(data)
                val p = parameters.getOrNull(i) ?: return@forEachIndexed
                val predictor = (p["Predictor"] as? Num)?.int ?: 1
                if (predictor >= 10) data = unpredict(data, (p["Columns"] as? Num)?.int ?: 1,
                    (p["Colors"] as? Num)?.int ?: 1, (p["BitsPerComponent"] as? Num)?.int ?: 8)
                else if (predictor != 1) throw IOException("Unsupported predictor $predictor")
            }
            return data
        }

        private fun inflate(data: ByteArray): ByteArray {
            val inflater = Inflater()
            try {
                inflater.setInput(data)
                val out = ByteArrayOutputStream(data.size * 4)
                val chunk = ByteArray(1 shl 14)
                while (!inflater.finished()) {
                    val n = inflater.inflate(chunk)
                    if (n == 0 && (inflater.needsInput() || inflater.needsDictionary())) break
                    out.write(chunk, 0, n)
                }
                return out.toByteArray()
            } finally {
                inflater.end()
            }
        }

        private fun unpredict(data: ByteArray, columns: Int, colors: Int, bits: Int): ByteArray {
            val pixel = maxOf(1, colors * bits / 8)
            val row = (columns * colors * bits + 7) / 8
            val out = ByteArrayOutputStream(data.size)
            var previous = ByteArray(row)
            var i = 0
            while (i + 1 + row <= data.size) {
                val type = data[i].toInt()
                val current = data.copyOfRange(i + 1, i + 1 + row)
                for (x in 0 until row) {
                    val left = if (x >= pixel) current[x - pixel].toInt() and 0xFF else 0
                    val up = previous[x].toInt() and 0xFF
                    val corner = if (x >= pixel) previous[x - pixel].toInt() and 0xFF else 0
                    val add = when (type) {
                        1 -> left
                        2 -> up
                        3 -> (left + up) / 2
                        4 -> {
                            val p = left + up - corner
                            val pa = Math.abs(p - left); val pb = Math.abs(p - up); val pc = Math.abs(p - corner)
                            if (pa <= pb && pa <= pc) left else if (pb <= pc) up else corner
                        }
                        else -> 0
                    }
                    current[x] = (current[x] + add).toByte()
                }
                out.write(current)
                previous = current
                i += 1 + row
            }
            return out.toByteArray()
        }
    }

    // ---- writing ----

    private class Counting(val out: OutputStream) : OutputStream() {
        var count = 0L
        override fun write(b: Int) { out.write(b); count++ }
        override fun write(b: ByteArray, off: Int, len: Int) { out.write(b, off, len); count += len }
        fun ascii(s: String) = write(s.toByteArray(Charsets.ISO_8859_1))
    }

    /** The keys and the /Encrypt dictionary of the standard security handler, revision 6. */
    private class Security(userPassword: String, ownerPassword: String, private val permissions: Int) {
        private val secure = SecureRandom()
        private val fileKey = random(32)
        private val u: ByteArray
        private val ue: ByteArray
        private val o: ByteArray
        private val oe: ByteArray
        private val perms: ByteArray

        init {
            val user = password(userPassword)
            val owner = password(ownerPassword)
            val userSalts = random(16)
            u = hash(user, userSalts.copyOfRange(0, 8), EMPTY) + userSalts
            ue = aes256(hash(user, userSalts.copyOfRange(8, 16), EMPTY), fileKey)
            val ownerSalts = random(16)
            o = hash(owner, ownerSalts.copyOfRange(0, 8), u) + ownerSalts
            oe = aes256(hash(owner, ownerSalts.copyOfRange(8, 16), u), fileKey)
            val p = ByteArray(16)
            for (i in 0 until 4) p[i] = (permissions shr (8 * i)).toByte()
            for (i in 4 until 8) p[i] = 0xFF.toByte()
            "Tadb".forEachIndexed { i, c -> p[8 + i] = c.code.toByte() }
            random(4).copyInto(p, 12)
            perms = Cipher.getInstance("AES/ECB/NoPadding").run {
                init(Cipher.ENCRYPT_MODE, SecretKeySpec(fileKey, "AES"))
                doFinal(p)
            }
        }

        fun random(n: Int) = ByteArray(n).also { secure.nextBytes(it) }

        fun dictionary() = Dict(linkedMapOf(
            "Filter" to Name("Standard"),
            "V" to Num("5"),
            "R" to Num("6"),
            "Length" to Num("256"),
            "CF" to Dict(linkedMapOf("StdCF" to Dict(linkedMapOf(
                "Type" to Name("CryptFilter"), "CFM" to Name("AESV3"), "AuthEvent" to Name("DocOpen"), "Length" to Num("32"))))),
            "StmF" to Name("StdCF"),
            "StrF" to Name("StdCF"),
            "O" to Str(o), "U" to Str(u), "OE" to Str(oe), "UE" to Str(ue), "Perms" to Str(perms),
            "P" to Num(permissions.toString()),
            "EncryptMetadata" to Word("true"),
        ))

        /** [obj] with its strings encrypted (not the /Contents of a signature, which stays as signed). */
        fun strings(obj: Obj): Obj = when (obj) {
            is Str -> Str(encrypt(obj.bytes))
            is Arr -> Arr(obj.items.mapTo(mutableListOf()) { strings(it) })
            is Dict -> {
                val type = (obj["Type"] as? Name)?.raw
                val signature = type == "Sig" || type == "DocTimeStamp" || obj["ByteRange"] != null
                Dict(LinkedHashMap<String, Obj>().also { map ->
                    for ((key, value) in obj.map) map[key] = if (signature && key == "Contents") value else strings(value)
                })
            }
            else -> obj
        }

        private fun cipher(iv: ByteArray) = Cipher.getInstance("AES/CBC/PKCS5Padding").apply {
            init(Cipher.ENCRYPT_MODE, SecretKeySpec(fileKey, "AES"), IvParameterSpec(iv))
        }

        /** 16 bytes of initialization vector, then the data encrypted with PKCS#5 padding. */
        fun encrypt(data: ByteArray): ByteArray {
            val iv = random(16)
            return iv + cipher(iv).doFinal(data)
        }

        fun encryptStream(buf: ByteBuffer, start: Int, length: Int, out: OutputStream) {
            val iv = random(16)
            out.write(iv)
            val cipher = cipher(iv)
            val source = buf.duplicate().apply { position(start) }
            val chunk = ByteArray(1 shl 16)
            var left = length
            while (left > 0) {
                val n = minOf(left, chunk.size)
                source.get(chunk, 0, n)
                cipher.update(chunk, 0, n)?.let { out.write(it) }
                left -= n
            }
            out.write(cipher.doFinal())
        }

        companion object {
            private val EMPTY = ByteArray(0)

            fun encryptedLength(length: Int) = 16 + (length / 16 + 1) * 16

            /** The password as ISO 32000-2 asks: normalized (SASLprep keeps NFKC), UTF-8, at most 127 bytes. */
            fun password(text: String): ByteArray =
                Normalizer.normalize(text, Normalizer.Form.NFKC).toByteArray(Charsets.UTF_8).let { if (it.size > 127) it.copyOf(127) else it }

            private fun aes256(key: ByteArray, data: ByteArray): ByteArray = Cipher.getInstance("AES/CBC/NoPadding").run {
                init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"), IvParameterSpec(ByteArray(16)))
                doFinal(data)
            }

            /** Algorithm 2.B of ISO 32000-2: the hash of a revision 6 password. */
            fun hash(password: ByteArray, salt: ByteArray, userKey: ByteArray): ByteArray {
                var k = MessageDigest.getInstance("SHA-256").digest(password + salt + userKey)
                var round = 0
                while (true) {
                    val block = password + k + userKey
                    val k1 = ByteArray(block.size * 64)
                    for (i in 0 until 64) block.copyInto(k1, i * block.size)
                    val e = Cipher.getInstance("AES/CBC/NoPadding").run {
                        init(Cipher.ENCRYPT_MODE, SecretKeySpec(k.copyOfRange(0, 16), "AES"), IvParameterSpec(k.copyOfRange(16, 32)))
                        doFinal(k1)
                    }
                    val digest = when (BigInteger(1, e.copyOfRange(0, 16)).mod(BigInteger.valueOf(3)).toInt()) {
                        0 -> "SHA-256"
                        1 -> "SHA-384"
                        else -> "SHA-512"
                    }
                    k = MessageDigest.getInstance(digest).digest(e)
                    round++
                    if (round >= 64 && (e[e.size - 1].toInt() and 0xFF) <= round - 32) break
                }
                return k.copyOf(32)
            }
        }
    }
}
