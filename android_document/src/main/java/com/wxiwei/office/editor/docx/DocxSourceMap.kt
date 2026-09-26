package com.wxiwei.office.editor.docx

import java.io.File
import java.util.concurrent.ConcurrentHashMap

/** UTF-16 model offsets, end exclusive. Storage is columnar; records are allocated only on lookup. */
class DocxSourceMap {
    enum class Kind { TEXT, FIELD, OBJECT, PARA_END }
    data class Leaf(val start: Long, val end: Long, val runIndices: IntArray, val text: String, val kind: Kind)
    data class Paragraph(val paraIndex: Int, val start: Long, val end: Long)
    private var starts = LongArray(64)
    private var ends = LongArray(64)
    private var kinds = IntArray(64)
    private var runStarts = IntArray(64)
    private var runCounts = IntArray(64)
    private var texts = arrayOfNulls<String>(64)
    private var runs = IntArray(64)
    private var runSize = 0
    var size = 0; private set
    private var paraIds = IntArray(32)
    private var paraStarts = LongArray(32)
    private var paraEnds = LongArray(32)
    var paragraphCount = 0; private set

    @Synchronized fun addLeaf(start: Long, end: Long, runIndices: IntArray, text: String, kind: Kind) {
        require(start >= 0 && end >= start && end - start == text.length.toLong())
        if (size == starts.size) {
            val n = size * 2
            starts = starts.copyOf(n); ends = ends.copyOf(n); kinds = kinds.copyOf(n)
            runStarts = runStarts.copyOf(n); runCounts = runCounts.copyOf(n); texts = texts.copyOf(n)
        }
        while (runSize + runIndices.size > runs.size) runs = runs.copyOf(runs.size * 2)
        starts[size] = start; ends[size] = end; kinds[size] = kind.ordinal; texts[size] = text
        runStarts[size] = runSize; runCounts[size] = runIndices.size
        runIndices.copyInto(runs, runSize); runSize += runIndices.size; size++
    }
    @Synchronized fun addParagraph(paraIndex: Int, start: Long, end: Long) {
        if (paragraphCount == paraIds.size) {
            val n = paragraphCount * 2
            paraIds = paraIds.copyOf(n); paraStarts = paraStarts.copyOf(n); paraEnds = paraEnds.copyOf(n)
        }
        paraIds[paragraphCount] = paraIndex; paraStarts[paragraphCount] = start; paraEnds[paragraphCount++] = end
    }
    @Synchronized fun leaf(i: Int): Leaf {
        require(i in 0 until size)
        return Leaf(starts[i], ends[i], runs.copyOfRange(runStarts[i], runStarts[i] + runCounts[i]), texts[i]!!, Kind.values()[kinds[i]])
    }
    @Synchronized fun paragraph(i: Int): Paragraph {
        require(i in 0 until paragraphCount)
        return Paragraph(paraIds[i], paraStarts[i], paraEnds[i])
    }
    companion object {
        private val maps = ConcurrentHashMap<String, DocxSourceMap>()
        @JvmStatic fun get(path: String): DocxSourceMap? = maps[File(path).absolutePath]
        @JvmStatic fun clear(path: String) { maps.remove(File(path).absolutePath) }
        @JvmStatic fun begin(path: String): DocxSourceMap = DocxSourceMap().also { maps[File(path).absolutePath] = it }
    }
}
