/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
package com.wxiwei.office.wp.model

import com.wxiwei.office.common.bg.BackgroundAndFill
import com.wxiwei.office.constant.wp.WPModelConstant
import com.wxiwei.office.simpletext.model.AttrManage
import com.wxiwei.office.simpletext.model.ElementCollectionImpl
import com.wxiwei.office.simpletext.model.IElement
import com.wxiwei.office.simpletext.model.LeafElement
import com.wxiwei.office.simpletext.model.ParagraphElement
import com.wxiwei.office.simpletext.model.STDocument
import com.wxiwei.office.simpletext.model.SectionElement

class WPDocument : STDocument() {
    private var root: Array<ElementCollectionImpl?>? = arrayOfNulls(6)
    private var para: Array<ElementCollectionImpl?>? = arrayOfNulls(6)
    private var table: Array<ElementCollectionImpl?>? = arrayOfNulls(4)
    private var pageBG: BackgroundAndFill? = null

    init {
        initRoot()
    }

    private fun initRoot() {
        val root = root ?: return
        val para = para ?: return
        val table = table ?: return
        root[0] = ElementCollectionImpl(5)
        root[1] = ElementCollectionImpl(3)
        root[2] = ElementCollectionImpl(3)
        root[3] = ElementCollectionImpl(5)
        root[4] = ElementCollectionImpl(5)
        root[5] = ElementCollectionImpl(5)

        para[0] = ElementCollectionImpl(100)
        para[1] = ElementCollectionImpl(3)
        para[2] = ElementCollectionImpl(3)
        para[3] = ElementCollectionImpl(5)
        para[4] = ElementCollectionImpl(5)
        para[5] = ElementCollectionImpl(5)

        table[0] = ElementCollectionImpl(5)
        table[1] = ElementCollectionImpl(5)
        table[2] = ElementCollectionImpl(5)
        table[3] = ElementCollectionImpl(5)
    }

    override fun getSection(offset: Long): IElement? = root?.get(0)?.getElement(offset)

    override fun appendSection(elem: IElement) {
        root?.get(0)?.addElement(elem)
    }

    override fun appendElement(elem: IElement, offset: Long) {
        if (elem.getType() == WPModelConstant.PARAGRAPH_ELEMENT) {
            appendParagraph(elem, offset)
        }
        getRootCollection(offset)?.addElement(elem)
    }

    /** Header or footer ([offset] gives the area) of [type] (HF_FIRST, HF_ODD, HF_EVEN), or null. */
    override fun getHFElement(offset: Long, type: Byte): IElement? {
        val c = getRootCollection(offset) ?: return null
        for (i in 0 until c.size()) {
            val e = c.getElementForIndex(i) as? HFElement ?: continue
            if (e.getHFType() == type) return e
        }
        return null
    }

    /** Word's titlePg: the first page has its own header and footer (none when it has no "first" one). */
    var titlePage = false
        private set
    /** settings evenAndOddHeaders: even pages have their own header and footer. */
    var evenAndOddHeaders = false
        private set

    fun setHeaderPages(titlePage: Boolean, evenAndOdd: Boolean) {
        this.titlePage = titlePage
        this.evenAndOddHeaders = evenAndOdd
    }

    /** Which header/footer a page shows (page numbers from 1). */
    fun hfTypeForPage(pageNumber: Int): Byte = when {
        titlePage && pageNumber == 1 -> WPModelConstant.HF_FIRST
        evenAndOddHeaders && pageNumber % 2 == 0 -> WPModelConstant.HF_EVEN
        else -> WPModelConstant.HF_ODD
    }

    override fun getFEElement(offset: Long): IElement? = null

    override fun getParagraph(offset: Long): IElement? {
        if ((offset and WPModelConstant.AREA_MASK) == WPModelConstant.TEXTBOX) {
            val e = getTextboxSectionElement(offset)
            if (e != null) {
                return (e as SectionElement).getParaCollection()!!.getElement(offset)
            }
        }
        return getParaCollection(offset)?.getElement(offset)
    }

    fun getParagraph0(offset: Long): IElement? {
        val elem = getParagraph(offset) ?: return null
        if (AttrManage.instance().getParaLevel(elem.getAttribute()) >= 0) {
            val collection = getTableCollection(offset)
            if (collection != null) {
                return collection.getElement(offset)
            }
        }
        return elem
    }

    override fun getParagraphForIndex(index: Int, area: Long): IElement? {
        if ((area and WPModelConstant.AREA_MASK) == WPModelConstant.TEXTBOX) {
            val e = getTextboxSectionElement(area)
            if (e != null) {
                return (e as SectionElement).getParaCollection()!!.getElementForIndex(index)
            }
        }
        return getParaCollection(area)?.getElementForIndex(index)
    }

    override fun appendParagraph(element: IElement, offset: Long) {
        if (element.getType() == WPModelConstant.TABLE_ELEMENT) {
            getTableCollection(offset)?.addElement(element)
            return
        }
        if ((offset and WPModelConstant.AREA_MASK) == WPModelConstant.TEXTBOX) {
            val e = getTextboxSectionElement(offset)
            if (e != null) {
                (e as SectionElement).appendParagraph(element, offset)
                return
            }
        }
        getParaCollection(offset)?.addElement(element)
    }

    override fun getParaCount(area: Long): Int {
        if ((area and WPModelConstant.AREA_MASK) == WPModelConstant.TEXTBOX) {
            val e = getTextboxSectionElement(area)
            if (e != null) {
                return (e as SectionElement).getParaCollection()!!.size()
            }
        }
        val collection = getParaCollection(area)
        if (collection != null && collection.size() > 0) {
            return collection.size()
        }
        return 0
    }

    private fun getRootCollection(offset: Long): ElementCollectionImpl? {
        val collections = root ?: return null
        return when (offset and WPModelConstant.AREA_MASK) {
            WPModelConstant.MAIN -> collections[0]
            WPModelConstant.HEADER -> collections[1]
            WPModelConstant.FOOTER -> collections[2]
            WPModelConstant.FOOTNOTE -> collections[3]
            WPModelConstant.ENDNOTE -> collections[4]
            WPModelConstant.TEXTBOX -> collections[5]
            else -> null
        }
    }

    fun getParaCollection(offset: Long): ElementCollectionImpl? {
        val collections = para ?: return null
        return when (offset and WPModelConstant.AREA_MASK) {
            WPModelConstant.MAIN -> collections[0]
            WPModelConstant.HEADER -> collections[1]
            WPModelConstant.FOOTER -> collections[2]
            WPModelConstant.FOOTNOTE -> collections[3]
            WPModelConstant.ENDNOTE -> collections[4]
            WPModelConstant.TEXTBOX -> collections[5]
            else -> null
        }
    }

    fun getTableCollection(offset: Long): ElementCollectionImpl? {
        val collections = table ?: return null
        return when (offset and WPModelConstant.AREA_MASK) {
            WPModelConstant.MAIN -> collections[0]
            WPModelConstant.HEADER -> collections[1]
            WPModelConstant.FOOTER -> collections[2]
            WPModelConstant.TEXTBOX -> collections[3]
            else -> null
        }
    }

    override fun getLength(offset: Long): Long {
        val root = getRootCollection(offset)
        if (root != null) {
            if ((offset and WPModelConstant.AREA_MASK) == WPModelConstant.TEXTBOX) {
                val e = getTextboxSectionElement(offset)
                if (e != null) {
                    return e.getEndOffset() - e.getStartOffset()
                }
            }
            return root.getElementForIndex(root.size() - 1)!!.getEndOffset() -
                root.getElementForIndex(0)!!.getStartOffset()
        }
        return 0
    }

    private fun getTextboxSectionElement(offset: Long): IElement? {
        val collections = root ?: return null
        val index = (offset and WPModelConstant.TEXTBOX_MASK) shr 32
        return collections[5]?.getElementForIndex(index.toInt())
    }

    fun getTextboxSectionElementForIndex(index: Int): IElement? = root?.get(5)?.getElementForIndex(index)

    // ---- live text editing of the main text --------------------------------------------------

    /**
     * Moves every main-text element after [at] by [delta]: an element that starts after [at]
     * moves, one that contains it grows (or shrinks). [skip] is left alone (already updated).
     */
    /** The body, headers, footers and text boxes can be edited live (not notes). */
    fun isEditableArea(offset: Long): Boolean = (offset and WPModelConstant.AREA_MASK).let {
        it == WPModelConstant.MAIN || it == WPModelConstant.HEADER || it == WPModelConstant.FOOTER ||
            (it == WPModelConstant.TEXTBOX && getTextboxSectionElement(offset) != null)
    }

    /** The story of an offset: the body, the headers, the footers, or one text box. */
    private fun storyOf(offset: Long) = offset and (WPModelConstant.AREA_MASK or WPModelConstant.TEXTBOX_MASK)

    /** The paragraphs of the story of [offset]: a text box keeps its own in its section. */
    private fun storyParas(offset: Long): ElementCollectionImpl? =
        if ((offset and WPModelConstant.AREA_MASK) == WPModelConstant.TEXTBOX)
            (getTextboxSectionElement(offset) as? SectionElement)?.getParaCollection() as? ElementCollectionImpl
        else getParaCollection(offset)

    /** The top elements (sections, headers...) of the story of [offset]. */
    private fun storyRoots(offset: Long): List<IElement> {
        if ((offset and WPModelConstant.AREA_MASK) == WPModelConstant.TEXTBOX) return listOfNotNull(getTextboxSectionElement(offset))
        val c = getRootCollection(offset) ?: return emptyList()
        return (0 until c.size()).mapNotNull { c.getElementForIndex(it) }
    }

    /** End of the story of [offset] (after its last paragraph mark). */
    fun storyEnd(offset: Long): Long =
        if ((offset and WPModelConstant.AREA_MASK) == WPModelConstant.TEXTBOX) getTextboxSectionElement(offset)?.getEndOffset() ?: 0L
        else getAreaEnd(offset)

    /** Elements of the story of [at] (body, header or footer) after [at] move by [delta]. */
    private fun shiftMain(at: Long, delta: Long, skip: IElement?) {
        fun move(e: IElement?) {
            if (e == null || e === skip || storyOf(e.getStartOffset()) != storyOf(at)) return
            val s = e.getStartOffset()
            val en = e.getEndOffset()
            if (s > at) {
                e.setStartOffset(s + delta); e.setEndOffset(en + delta)
            } else if (en > at) {
                e.setEndOffset(en + delta)
            }
        }
        storyRoots(at).forEach { move(it) }
        storyParas(at)?.let { c ->
            for (i in 0 until c.size()) {
                val p = c.getElementForIndex(i)
                move(p)
                if (p is ParagraphElement) for (j in 0 until p.leafCount()) move(p.getElementForIndex(j))
            }
        }
        getTableCollection(at)?.let { c ->
            for (i in 0 until c.size()) {
                val t = c.getElementForIndex(i) as? TableElement ?: continue
                move(t)
                for (r in 0 until t.rowCount()) {
                    val row = t.getElementForIndex(r) as? RowElement ?: continue
                    move(row)
                    for (k in 0 until row.getCellNumber()) move(row.getElementForIndex(k))
                }
            }
        }
    }

    /**
     * Inserts [text] (no paragraph break) into the plain text run at [offset] of the main text.
     * Returns false when [offset] is not inside a plain text run (shape, field, other area).
     */
    fun insertMainText(offset: Long, text: String): Boolean {
        if (text.isEmpty() || !isEditableArea(offset)) return false
        if (text.any { it == '\n' || it == '\r' || it == '\u0007' || it == '\u000C' }) return false
        val paragraph = getParagraph(offset) as? ParagraphElement ?: return false
        val leaf = paragraph.getLeaf(offset) as? LeafElement ?: return false
        if (leaf.javaClass != LeafElement::class.java) return false
        val old = leaf.getText(null) ?: return false
        val k = (offset - leaf.getStartOffset()).toInt()
        if (k < 0 || k > old.length) return false
        leaf.setText(old.substring(0, k) + text + old.substring(k)) // also moves the leaf's end
        shiftMain(offset, text.length.toLong(), leaf)
        return true
    }

    /**
     * Deletes [start, end) of the main text when it lies in one paragraph, leaves its paragraph
     * mark, and covers plain text runs only. Returns false otherwise (nothing changed).
     */
    fun deleteMainText(start: Long, end: Long): Boolean {
        if (end <= start || !isEditableArea(start)) return false
        val paragraph = getParagraph(start) as? ParagraphElement ?: return false
        if (end > paragraph.getEndOffset() - 1) return false // the paragraph mark stays
        // only plain runs in the range
        val touched = ArrayList<Int>()
        for (i in 0 until paragraph.leafCount()) {
            val l = paragraph.getElementForIndex(i) ?: continue
            if (l.getEndOffset() <= start || l.getStartOffset() >= end) continue
            if (l.javaClass != LeafElement::class.java) return false
            touched.add(i)
        }
        if (touched.isEmpty()) return false
        val removed = ArrayList<Int>()
        for (i in touched) {
            val l = paragraph.getElementForIndex(i) as LeafElement
            val ls = l.getStartOffset()
            val text = l.getText(null) ?: continue
            val a = (maxOf(start, ls) - ls).toInt()
            val b = (minOf(end, l.getEndOffset()) - ls).toInt()
            val kept = text.substring(0, a) + text.substring(b)
            // a run starting inside the range starts at [start] now
            if (ls > start) l.setStartOffset(start)
            l.setText(kept)
            if (kept.isEmpty()) removed.add(i)
        }
        val delta = end - start
        // everything after the range moves back; the touched leaves are already right
        val skip = touched.map { paragraph.getElementForIndex(it) }.toSet()
        fun move(e: IElement?) {
            if (e == null || e in skip || storyOf(e.getStartOffset()) != storyOf(start)) return
            val s = e.getStartOffset()
            val en = e.getEndOffset()
            if (s >= end) { e.setStartOffset(s - delta); e.setEndOffset(en - delta) }
            else if (en > start) e.setEndOffset(maxOf(start, en - delta))
        }
        storyRoots(start).forEach { move(it) }
        storyParas(start)?.let { c ->
            for (i in 0 until c.size()) {
                val p = c.getElementForIndex(i)
                move(p)
                if (p is ParagraphElement) for (j in 0 until p.leafCount()) move(p.getElementForIndex(j))
            }
        }
        getTableCollection(start)?.let { c ->
            for (i in 0 until c.size()) {
                val t = c.getElementForIndex(i) as? TableElement ?: continue
                move(t)
                for (r in 0 until t.rowCount()) {
                    val row = t.getElementForIndex(r) as? RowElement ?: continue
                    move(row)
                    for (k in 0 until row.getCellNumber()) move(row.getElementForIndex(k))
                }
            }
        }
        for (i in removed.asReversed()) paragraph.removeLeafAt(i)
        return true
    }

    /**
     * Splits the paragraph at [offset] (Enter): a paragraph mark is inserted there and the text
     * after it becomes a new paragraph with the same paragraph properties. Returns false when
     * [offset] is not in a plain run of a main-text paragraph.
     */
    fun splitMainParagraph(offset: Long): Boolean {
        if (!isEditableArea(offset)) return false
        val paragraphs = storyParas(offset) ?: return false
        val p = getParagraph(offset) as? ParagraphElement ?: return false
        if (p is TableElement || offset < p.getStartOffset() || offset >= p.getEndOffset()) return false
        val index = paragraphs.indexOf(p)
        if (index < 0) return false
        val leaf = p.getLeaf(offset) as? LeafElement ?: return false
        if (leaf.javaClass != LeafElement::class.java) return false
        val old = leaf.getText(null) ?: return false
        val k = (offset - leaf.getStartOffset()).toInt()
        leaf.setText(old.substring(0, k) + "\n" + old.substring(k))
        shiftMain(offset, 1, leaf)
        // everything after the new mark moves to a new paragraph
        p.leavesFor(offset + 1, p.getEndOffset())
        val next = ParagraphElement()
        next.setAttribute(p.getAttribute().clone())
        next.setStartOffset(offset + 1)
        next.setEndOffset(p.getEndOffset())
        var i = 0
        while (i < p.leafCount()) {
            val l = p.getElementForIndex(i)!!
            if (l.getStartOffset() >= offset + 1) next.appendLeaf(p.detachLeafAt(i) as LeafElement) else i++
        }
        p.setEndOffset(offset + 1)
        paragraphs.insertElementForIndex(next, index + 1)
        return true
    }

    /**
     * Joins the paragraph whose mark is at [markOffset] with the next one (undo of Enter, or
     * Backspace at a paragraph start). Returns false when that is not two plain main paragraphs.
     */
    fun joinMainParagraph(markOffset: Long): Boolean {
        if (!isEditableArea(markOffset)) return false
        val paragraphs = storyParas(markOffset) ?: return false
        val p = getParagraph(markOffset) as? ParagraphElement ?: return false
        if (p is TableElement || p.getEndOffset() != markOffset + 1) return false
        val index = paragraphs.indexOf(p)
        val next = paragraphs.getElementForIndex(index + 1) as? ParagraphElement ?: return false
        if (next is TableElement || next.getStartOffset() != markOffset + 1) return false
        // same table cell (or both outside tables)
        if (AttrManage.instance().getParaLevel(p.getAttribute()) != AttrManage.instance().getParaLevel(next.getAttribute())) return false
        // the last paragraph of a table cell: the next one is in another cell
        if (AttrManage.instance().getParaLevel(p.getAttribute()) >= 0) {
            val cell = try {
                ((getParagraph0(markOffset) as? TableElement)?.getRowElement(markOffset) as? RowElement)?.getCellElement(markOffset)
            } catch (e: RuntimeException) { null } ?: return false
            if (cell.getEndOffset() <= markOffset + 1) return false
        }
        val markLeaf = p.getLeaf(markOffset) as? LeafElement ?: return false
        if (markLeaf.javaClass != LeafElement::class.java) return false
        val text = markLeaf.getText(null) ?: return false
        val k = (markOffset - markLeaf.getStartOffset()).toInt()
        if (k !in text.indices || text[k] != '\n') return false
        // move the next paragraph's runs in, then drop the mark character
        p.setEndOffset(next.getEndOffset() - 1) // one character (the mark) goes away
        while (next.leafCount() > 0) p.appendLeaf(next.detachLeafAt(0) as LeafElement)
        paragraphs.detachElementForIndex(index + 1)
        markLeaf.setText(text.substring(0, k) + text.substring(k + 1))
        if (markLeaf.getText(null).isNullOrEmpty()) p.removeLeafAt(p.let { para -> (0 until para.leafCount()).first { para.getElementForIndex(it) === markLeaf } })
        // everything after the mark moves back by one
        shiftMainBack(markOffset, markLeaf, p)
        return true
    }

    /**
     * Moves the one-char object (an in-line picture) at [from] to the text position [to] of the
     * same story, before the character now at [to]; it lands at [to] - 1 when [to] > [from].
     * Both must be in paragraphs of the main text (not a table's own element). Returns false when
     * nothing changed.
     */
    fun moveMainObject(from: Long, to: Long): Boolean {
        if (!isEditableArea(from) || storyOf(from) != storyOf(to) || to == from || to == from + 1) return false
        val src = getParagraph(from) as? ParagraphElement ?: return false
        if (src is TableElement) return false
        val index = (0 until src.leafCount()).firstOrNull { src.getElementForIndex(it)?.getStartOffset() == from } ?: return false
        val obj = src.getElementForIndex(index) as? LeafElement ?: return false
        if (obj.getEndOffset() != from + 1 || AttrManage.instance().getShapeID(obj.getAttribute()) < 0) return false
        val dst0 = getParagraph(to) as? ParagraphElement ?: return false
        if (dst0 is TableElement || to >= storyEnd(to)) return false
        // [to] must be a run boundary or inside a plain text run, which can be split there
        val hit = dst0.getLeaf(to) ?: return false
        if (hit.getStartOffset() != to && hit.javaClass != LeafElement::class.java) return false
        // out: everything after it moves back by one
        src.detachLeafAt(index)
        shiftMainBack(from)
        // in at [at]: a run crossing it is split, everything from it on moves forward by one
        val at = if (to > from) to - 1 else to
        val dst = getParagraph(at) as ParagraphElement
        dst.leavesFor(at, at)
        val dstStart = dst.getStartOffset()
        val dstEnd = dst.getEndOffset()
        fun move(e: IElement?, leaf: Boolean) {
            if (e == null || storyOf(e.getStartOffset()) != storyOf(at)) return
            val s = e.getStartOffset()
            val en = e.getEndOffset()
            when {
                // what holds the paragraph taking it (the section, a table, its row and cell, the
                // paragraph itself) grows, even when it starts at [at]
                !leaf && s <= dstStart && en >= dstEnd -> e.setEndOffset(en + 1)
                s >= at -> { e.setStartOffset(s + 1); e.setEndOffset(en + 1) }
                !leaf && en > at -> e.setEndOffset(en + 1)
            }
        }
        storyRoots(at).forEach { move(it, false) }
        storyParas(at)?.let { c ->
            for (i in 0 until c.size()) {
                val p = c.getElementForIndex(i)
                move(p, false)
                if (p is ParagraphElement) for (j in 0 until p.leafCount()) move(p.getElementForIndex(j), true)
            }
        }
        getTableCollection(at)?.let { c ->
            for (i in 0 until c.size()) {
                val t = c.getElementForIndex(i) as? TableElement ?: continue
                move(t, false)
                for (r in 0 until t.rowCount()) {
                    val row = t.getElementForIndex(r) as? RowElement ?: continue
                    move(row, false)
                    for (k in 0 until row.getCellNumber()) move(row.getElementForIndex(k), false)
                }
            }
        }
        obj.setStartOffset(at)
        obj.setEndOffset(at + 1)
        val slot = (0 until dst.leafCount()).firstOrNull { (dst.getElementForIndex(it)?.getStartOffset() ?: 0) > at } ?: dst.leafCount()
        dst.insertLeafAt(slot, obj)
        return true
    }

    /**
     * Moves the table of the body at [start, end) so it starts at [at] - before the body paragraph
     * or table starting at [at] ([at] < [start] or [at] >= [end]); what lay between shifts by the
     * table's length. The table lands at [at] when moved up, at [at] - ([end] - [start]) when moved
     * down. Returns false when nothing changed.
     */
    fun moveMainTable(start: Long, end: Long, at: Long): Boolean {
        if ((start and WPModelConstant.AREA_MASK) != WPModelConstant.MAIN || storyOf(at) != storyOf(start)) return false
        if (at in start..end || at >= storyEnd(at)) return false
        val tables = getTableCollection(start) ?: return false
        val table = (0 until tables.size()).map { tables.getElementForIndex(it) }.firstOrNull { it?.getStartOffset() == start && it.getEndOffset() == end } ?: return false
        // [at] must start a block of the body: a paragraph outside tables, or a table
        val target = getParagraph0(at) ?: return false
        if (target.getStartOffset() != at) return false
        val len = end - start
        // old offset -> new offset
        fun map(x: Long): Long = if (at < start) when {
            x in start until end -> x - (start - at)
            x in at until start -> x + len
            else -> x
        } else when {
            x in start until end -> x + (at - end)
            x in end until at -> x - len
            else -> x
        }
        fun move(e: IElement?) {
            if (e == null) return
            val s = e.getStartOffset(); val en = e.getEndOffset()
            // elements inside the moved span or the span between move whole (ends map as the last char + 1)
            if (en <= s) { e.setStartOffset(map(s)); e.setEndOffset(map(s)); return }
            e.setStartOffset(map(s)); e.setEndOffset(map(en - 1) + 1)
        }
        val lo = minOf(start, at); val hi = maxOf(end, at)
        val paras = storyParas(start) ?: return false
        for (i in 0 until paras.size()) {
            val p = paras.getElementForIndex(i) ?: continue
            if (p.getStartOffset() < lo || p.getStartOffset() >= hi) continue
            move(p)
            if (p is ParagraphElement) for (j in 0 until p.leafCount()) move(p.getElementForIndex(j))
        }
        for (i in 0 until tables.size()) {
            val t = tables.getElementForIndex(i) as? TableElement ?: continue
            if (t.getStartOffset() < lo || t.getStartOffset() >= hi) continue
            move(t)
            for (r in 0 until t.rowCount()) {
                val row = t.getElementForIndex(r) as? RowElement ?: continue
                move(row)
                for (k in 0 until row.getCellNumber()) move(row.getElementForIndex(k))
            }
        }
        paras.sortByOffset()
        tables.sortByOffset()
        return table.getStartOffset() == map(start)
    }

    // ---- table rows and cells, live ----------------------------------------------------------

    /**
     * Every element of the main text from [at] on moves by [delta]; the elements in [grow] (the
     * table, row... that take the new content) and the sections holding [at] get longer instead.
     * A negative [delta] takes out [at] + delta until [at] (already detached).
     */
    private fun shiftMainBlock(at: Long, delta: Long, grow: Collection<IElement>) {
        fun move(e: IElement?, container: Boolean) {
            if (e == null || storyOf(e.getStartOffset()) != storyOf(at)) return
            val s = e.getStartOffset(); val en = e.getEndOffset()
            when {
                grow.any { it === e } -> e.setEndOffset(en + delta)
                // a filler cell of a merge has no offsets of its own
                !container && s == en && s == 0L -> {}
                s >= at -> { e.setStartOffset(s + delta); e.setEndOffset(en + delta) }
                container && en >= at -> e.setEndOffset(en + delta)
            }
        }
        storyRoots(at).forEach { move(it, true) }
        storyParas(at)?.let { c ->
            for (i in 0 until c.size()) {
                val p = c.getElementForIndex(i)
                move(p, false)
                if (p is ParagraphElement) for (j in 0 until p.leafCount()) move(p.getElementForIndex(j), false)
            }
        }
        getTableCollection(at)?.let { c ->
            for (i in 0 until c.size()) {
                val t = c.getElementForIndex(i) as? TableElement ?: continue
                move(t, false)
                for (r in 0 until t.rowCount()) {
                    val row = t.getElementForIndex(r) as? RowElement ?: continue
                    move(row, false)
                    for (k in 0 until row.getCellNumber()) move(row.getElementForIndex(k), false)
                }
            }
        }
    }

    /** Places [paras] (one mark each, or more) from [at] on and puts them in the paragraph list; their length. */
    private fun placeParagraphs(at: Long, paras: List<ParagraphElement>): Long {
        var o = at
        for (p in paras) {
            val len = maxOf(1L, p.getEndOffset() - p.getStartOffset())
            val shift = o - p.getStartOffset()
            p.setStartOffset(o); p.setEndOffset(o + len)
            for (j in 0 until p.leafCount()) p.getElementForIndex(j)?.let { l -> l.setStartOffset(l.getStartOffset() + shift); l.setEndOffset(l.getEndOffset() + shift) }
            o += len
        }
        val list = storyParas(at) ?: return 0
        val index = (0 until list.size()).firstOrNull { (list.getElementForIndex(it)?.getStartOffset() ?: 0) >= at } ?: list.size()
        paras.forEachIndexed { k, p -> list.insertElementForIndex(p, index + k) }
        return o - at
    }

    private fun removeParagraphs(start: Long, end: Long) {
        val list = storyParas(start) ?: return
        var i = 0
        while (i < list.size()) {
            val p = list.getElementForIndex(i)
            if (p != null && p.getStartOffset() >= start && p.getEndOffset() <= end && p.getEndOffset() > p.getStartOffset()) list.detachElementForIndex(i) else i++
        }
    }

    /**
     * Puts the new [row] in [table] at [index]: cell k holds [paras][k] (null for the filler of a
     * merged cell, which has no text). Offsets are given to it and everything after moves.
     */
    fun insertTableRow(table: TableElement, index: Int, row: RowElement, paras: List<List<ParagraphElement>?>) {
        val at = if (index < table.rowCount()) table.getElementForIndex(index)!!.getStartOffset() else table.getEndOffset()
        val len = paras.sumOf { cell -> cell?.sumOf { maxOf(1L, it.getEndOffset() - it.getStartOffset()) } ?: 0L }
        shiftMainBlock(at, len, listOf(table))
        var o = at
        for (c in 0 until row.getCellNumber()) {
            val mine = paras.getOrNull(c) ?: continue
            val cell = row.getElementForIndex(c) ?: continue
            val n = placeParagraphs(o, mine)
            cell.setStartOffset(o); cell.setEndOffset(o + n)
            o += n
        }
        row.setStartOffset(at); row.setEndOffset(at + len)
        table.insertRow(index, row)
    }

    /** Takes row [index] out of [table]: its text goes, everything after moves back. */
    fun removeTableRow(table: TableElement, index: Int): RowElement? {
        val row = table.getElementForIndex(index) as? RowElement ?: return null
        val start = row.getStartOffset(); val end = row.getEndOffset()
        removeParagraphs(start, end)
        table.detachRowAt(index)
        shiftMainBlock(end, -(end - start), listOf(table))
        return row
    }

    /** Puts the new [cell] (holding [paras]) in [row] of [table] at cell [index]; everything after moves. */
    fun insertTableCell(table: TableElement, row: RowElement, index: Int, cell: CellElement, paras: List<ParagraphElement>) {
        val next = (index until row.getCellNumber()).mapNotNull { row.getElementForIndex(it) }.firstOrNull { it.getEndOffset() > it.getStartOffset() }
        val at = next?.getStartOffset() ?: row.getEndOffset()
        val len = paras.sumOf { maxOf(1L, it.getEndOffset() - it.getStartOffset()) }
        shiftMainBlock(at, len, listOf(table, row))
        placeParagraphs(at, paras)
        cell.setStartOffset(at); cell.setEndOffset(at + len)
        row.insertElementForIndex(cell, index)
    }

    /** Takes cell [index] out of [row] of [table]: its text goes, everything after moves back. */
    fun removeTableCell(table: TableElement, row: RowElement, index: Int): IElement? {
        val cell = row.getElementForIndex(index) ?: return null
        val start = cell.getStartOffset(); val end = cell.getEndOffset()
        removeParagraphs(start, end)
        row.detachCellAt(index)
        if (end > start) shiftMainBlock(end, -(end - start), listOf(table, row))
        return cell
    }

    /** One character removed at [at]: elements after it move back; [skip] are already right. */
    private fun shiftMainBack(at: Long, vararg skip: IElement) {
        fun move(e: IElement?) {
            if (e == null || skip.any { it === e } || storyOf(e.getStartOffset()) != storyOf(at)) return
            val s = e.getStartOffset()
            val en = e.getEndOffset()
            if (s > at) { e.setStartOffset(s - 1); e.setEndOffset(en - 1) } else if (en > at) e.setEndOffset(en - 1)
        }
        storyRoots(at).forEach { move(it) }
        storyParas(at)?.let { c ->
            for (i in 0 until c.size()) {
                val p = c.getElementForIndex(i)
                move(p)
                if (p is ParagraphElement) for (j in 0 until p.leafCount()) move(p.getElementForIndex(j))
            }
        }
        getTableCollection(at)?.let { c ->
            for (i in 0 until c.size()) {
                val t = c.getElementForIndex(i) as? TableElement ?: continue
                move(t)
                for (r in 0 until t.rowCount()) {
                    val row = t.getElementForIndex(r) as? RowElement ?: continue
                    move(row)
                    for (k in 0 until row.getCellNumber()) move(row.getElementForIndex(k))
                }
            }
        }
    }

    fun setPageBackground(pageBG: BackgroundAndFill?) {
        this.pageBG = pageBG
    }

    fun getPageBackground(): BackgroundAndFill? = pageBG

    override fun dispose() {
        super.dispose()
        root?.let { roots ->
            for (i in roots.indices) {
                roots[i]?.dispose()
                roots[i] = null
            }
        }
        root = null
        para?.let { paras ->
            for (i in paras.indices) {
                paras[i]?.dispose()
                paras[i] = null
            }
        }
        para = null
        table?.let { tables ->
            for (i in tables.indices) {
                tables[i]?.dispose()
                tables[i] = null
            }
        }
        table = null
    }
}
