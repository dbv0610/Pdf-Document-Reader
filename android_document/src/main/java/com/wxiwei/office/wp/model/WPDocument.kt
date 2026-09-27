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

    override fun getHFElement(offset: Long, type: Byte): IElement? = getRootCollection(offset)?.getElement(offset)

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
    /** The body, the header and the footer can be edited live (not text boxes, notes). */
    fun isEditableArea(offset: Long): Boolean = (offset and WPModelConstant.AREA_MASK).let {
        it == WPModelConstant.MAIN || it == WPModelConstant.HEADER || it == WPModelConstant.FOOTER
    }

    /** Elements of the story of [at] (body, header or footer) after [at] move by [delta]. */
    private fun shiftMain(at: Long, delta: Long, skip: IElement?) {
        fun move(e: IElement?) {
            if (e == null || e === skip) return
            val s = e.getStartOffset()
            val en = e.getEndOffset()
            if (s > at) {
                e.setStartOffset(s + delta); e.setEndOffset(en + delta)
            } else if (en > at) {
                e.setEndOffset(en + delta)
            }
        }
        getRootCollection(at)?.let { c -> for (i in 0 until c.size()) move(c.getElementForIndex(i)) }
        getParaCollection(at)?.let { c ->
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
            if (e == null || e in skip) return
            val s = e.getStartOffset()
            val en = e.getEndOffset()
            if (s >= end) { e.setStartOffset(s - delta); e.setEndOffset(en - delta) }
            else if (en > start) e.setEndOffset(maxOf(start, en - delta))
        }
        getRootCollection(start)?.let { c -> for (i in 0 until c.size()) move(c.getElementForIndex(i)) }
        getParaCollection(start)?.let { c ->
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
        val paragraphs = getParaCollection(offset) ?: return false
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
        val paragraphs = getParaCollection(markOffset) ?: return false
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

    /** One character removed at [at]: elements after it move back; [skip] are already right. */
    private fun shiftMainBack(at: Long, vararg skip: IElement) {
        fun move(e: IElement?) {
            if (e == null || skip.any { it === e }) return
            val s = e.getStartOffset()
            val en = e.getEndOffset()
            if (s > at) { e.setStartOffset(s - 1); e.setEndOffset(en - 1) } else if (en > at) e.setEndOffset(en - 1)
        }
        getRootCollection(at)?.let { c -> for (i in 0 until c.size()) move(c.getElementForIndex(i)) }
        getParaCollection(at)?.let { c ->
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
