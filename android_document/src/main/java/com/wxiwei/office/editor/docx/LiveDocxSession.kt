package com.wxiwei.office.editor.docx

import android.graphics.Color
import com.wxiwei.office.editor.EditResult
import com.wxiwei.office.editor.Reason
import com.wxiwei.office.simpletext.model.AttrManage
import com.wxiwei.office.simpletext.model.IAttributeSet
import com.wxiwei.office.simpletext.model.LeafElement
import com.wxiwei.office.simpletext.model.ParagraphElement
import com.wxiwei.office.system.IControl
import com.wxiwei.office.wp.control.Word
import com.wxiwei.office.wp.model.WPDocument
import com.wxiwei.office.constant.wp.WPAttrConstant
import com.wxiwei.office.simpletext.model.IElement
import java.io.File

/**
 * Realtime editing of an open .docx. Formatting (bold, italic, underline, color, size,
 * highlight) and text changes inside one paragraph show at once: the model is updated and the
 * pages laid out again. Every change is also queued in [DocxEditor], which writes it on [save].
 *
 * Offsets are CURRENT model offsets (after the live text changes), end exclusive; the session
 * maps them to the file's original offsets for [DocxEditor]. Changes the view cannot show live
 * (a paragraph break, a range across paragraphs) are still saved: [needsReopen]. Text typed in
 * this session is formatted through its queued insert (DocxEditor.formatInserted). Main thread only.
 */
class LiveDocxSession(control: IControl, private val source: File) {
    private val word = control.getView() as? Word ?: error("Open a Word document first")
    private val editor = DocxEditor(source, DocxSourceMap.get(source.absolutePath) ?: error("Document is still loading"))
    private val am = AttrManage.instance()
    /** Held while the model changes: the background page layout takes it for every page it lays out. */
    private val layoutLock: Any get() = word.getDocument()

    private open class Step(val undo: () -> Boolean, val redo: () -> Boolean) {
        open fun runUndo(): Boolean = undo()
        open fun runRedo(): Boolean = redo()
    }

    /** An Enter at [at]. */
    private class SplitStep(val at: Long, undo: () -> Boolean, redo: () -> Boolean) : Step(undo, redo)
    private val undoStack = ArrayList<Step>()
    private val redoStack = ArrayList<Step>()

    /** True after a text change: it is saved, but the view shows it only after reopening. */
    var needsReopen = false
        private set
    private var ownError: EditResult.Error? = null
    val lastError: EditResult.Error? get() = ownError ?: editor.lastError
    fun canUndo() = undoStack.isNotEmpty()
    fun canRedo() = redoStack.isNotEmpty()
    fun hasChanges() = editor.pendingCount > 0

    fun setBold(start: Long, end: Long, on: Boolean) = format(start, end, { e, s, t -> e.setBold(s, t, on) }) { am.setFontBold(it, on) }
    fun setItalic(start: Long, end: Long, on: Boolean) = format(start, end, { e, s, t -> e.setItalic(s, t, on) }) { am.setFontItalic(it, on) }
    fun setUnderline(start: Long, end: Long, on: Boolean) = format(start, end, { e, s, t -> e.setUnderline(s, t, on) }) { am.setFontUnderline(it, if (on) 1 else 0) }
    fun setTextColor(start: Long, end: Long, rgbHex: String): Boolean {
        val rgb = rgbHex.removePrefix("#")
        val color = rgb.toIntOrNull(16)?.let { (0xFF shl 24) or it } ?: return editor.setTextColor(start, end, rgb)
        return format(start, end, { e, s, t -> e.setTextColor(s, t, rgb) }) { am.setFontColor(it, color) }
    }
    fun setFontSize(start: Long, end: Long, pt: Float) = format(start, end, { e, s, t -> e.setFontSize(s, t, pt) }) { am.setFontSize(it, pt) }
    fun highlight(start: Long, end: Long, rgbHex: String = "FFFF00"): Boolean {
        val rgb = rgbHex.removePrefix("#")
        val color = rgb.toIntOrNull(16)?.let { (0xFF shl 24) or it } ?: Color.YELLOW
        return format(start, end, { e, s, t -> e.highlight(s, t, rgb) }) { am.setFontHighLight(it, color) }
    }

    // ---- text changes --------------------------------------------------------------------

    /** A live text change, in current offsets at the time it was made. */
    private sealed class Edit {
        class Insert(val at: Long, var length: Long) : Edit()
        class Delete(val at: Long, val length: Long) : Edit()
    }
    private val edits = ArrayList<Edit>()

    /** Current model offset -> original file offset (the start of typed text for positions inside it). */
    private fun toOriginal(offset: Long): Long {
        var x = offset
        for (e in edits.asReversed()) when (e) {
            is Edit.Insert -> if (x >= e.at + e.length) x -= e.length else if (x > e.at) x = e.at
            is Edit.Delete -> if (x > e.at) x += e.length
        }
        return x
    }

    /** True when [start, end) contains text typed in this session (it has no original offsets). */
    private fun touchesTyped(start: Long, end: Long): Boolean {
        var s = start
        var e = end
        for (edit in edits.asReversed()) when (edit) {
            is Edit.Insert -> {
                val a = edit.at
                val b = edit.at + edit.length
                if (s < b && e > a) return true
                if (s >= b) s -= edit.length
                if (e >= b) e -= edit.length
            }
            is Edit.Delete -> {
                if (s > edit.at) s += edit.length
                if (e > edit.at) e += edit.length
            }
        }
        return false
    }

    private fun refuse(message: String): Boolean {
        ownError = EditResult.Error(Reason.INVALID_ARGUMENT, message)
        return false
    }

    // ---- paragraph formatting ---------------------------------------------------------------

    fun setAlignment(start: Long, end: Long, align: String): Boolean {
        val value = when (align) {
            "center" -> WPAttrConstant.PARA_HOR_ALIGN_CENTER
            "right" -> WPAttrConstant.PARA_HOR_ALIGN_RIGHT
            "both" -> WPAttrConstant.PARA_HOR_ALIGN_JUSTIFIED
            else -> WPAttrConstant.PARA_HOR_ALIGN_LEFT
        }.toInt()
        return paragraphFormat(start, end, { e, s, t -> e.setParagraphAlignment(s, t, align) }) { am.setParaHorizontalAlign(it, value) }
    }

    fun setIndentLeft(start: Long, end: Long, twips: Int) =
        paragraphFormat(start, end, { e, s, t -> e.setParagraphIndent(s, t, twips) }) { am.setParaIndentLeft(it, twips) }

    fun setLineSpacing(start: Long, end: Long, multiple: Float) = paragraphFormat(start, end, { e, s, t -> e.setLineSpacing(s, t, multiple) }) {
        am.setParaLineSpaceType(it, WPAttrConstant.LINE_SAPCE_MULTIPLE.toInt())
        am.setParaLineSpace(it, multiple)
    }

    /** True when the character at [offset] is bold (italic, underlined): the B/I/U buttons toggle. */
    fun isBold(offset: Long) = charAttr(offset) { p, l -> am.getFontBold(p, l) }
    fun isItalic(offset: Long) = charAttr(offset) { p, l -> am.getFontItalic(p, l) }
    fun isUnderlined(offset: Long) = charAttr(offset) { p, l -> am.getFontUnderline(p, l) > 0 }
    private fun charAttr(offset: Long, read: (IAttributeSet, IAttributeSet) -> Boolean): Boolean {
        val doc = word.getDocument()
        val para = doc.getParagraph(offset) ?: return false
        val leaf = doc.getLeaf(offset) ?: return false
        return read(para.getAttribute()!!, leaf.getAttribute()!!)
    }

    /** Bullets on or off for the paragraphs touching [start, end). */
    fun setBullets(start: Long, end: Long, on: Boolean) = setList(start, end, on, bullet = true)

    /** Numbering (1. 2. 3.) on or off for the paragraphs touching [start, end). */
    fun setNumbering(start: Long, end: Long, on: Boolean) = setList(start, end, on, bullet = false)

    private fun setList(start: Long, end: Long, on: Boolean, bullet: Boolean): Boolean {
        val id = if (bullet) editor.bulletListId else editor.numberingListId
        if (on && id < 0) return refuse("Cannot read the document's lists")
        if (on) ensureList(id, bullet)
        val fileOp: (DocxEditor, Long, Long) -> Boolean =
            if (bullet) { e, s, t -> e.setBullets(s, t, on) } else { e, s, t -> e.setNumbering(s, t, on) }
        return paragraphFormat(start, end, fileOp) {
            // -1 also hides a list the paragraph style would give, like numId 0 in the file
            am.setParaListID(it, if (on) id else -1)
            am.setParaListLevel(it, 0)
        }
    }

    /** List level of the paragraph at [offset] (0 when not in a list). */
    fun listLevelAt(offset: Long): Int = word.getDocument().getParagraph(offset)?.let { maxOf(0, am.getParaListLevel(it.getAttribute())) } ?: 0

    /** Moves the listed paragraphs touching [start, end) to list level [level] (0-8). */
    fun setListLevel(start: Long, end: Long, level: Int): Boolean {
        if (!hasBullet(start)) return refuse("Not in a list")
        return paragraphFormat(start, end, { e, s, t -> e.setListLevel(s, t, level) }) {
            if (am.getParaListID(it) >= 0) am.setParaListLevel(it, level)
        }
    }

    /** List id of the paragraph at [offset] (-1: none). */
    fun listAt(offset: Long): Int = word.getDocument().getParagraph(offset)?.let { am.getParaListID(it.getAttribute()) } ?: -1

    /** True when the paragraph at [offset] shows a bullet or number. */
    fun hasBullet(offset: Long): Boolean = listAt(offset) >= 0

    /** True when the paragraph at [offset] is in the numbered list [setNumbering] uses. */
    fun hasNumbering(offset: Long): Boolean = listAt(offset).let { it >= 0 && it == editor.numberingListId }

    /** The view draws a list from its ListData: add the one save will write when it is new. */
    private fun ensureList(id: Int, bullet: Boolean) {
        val lists = word.getControl().getSysKit().getListManage()
        if (lists.getListData(id) != null) return
        val bullets = charArrayOf('\u25CF', '\u25CB', '\u25A0')
        val formats = intArrayOf(0, 4, 2) // decimal, lowerLetter, lowerRoman
        lists.putListData(id, com.wxiwei.office.common.bulletnumber.ListData().apply {
            listID = id
            levels = Array(9) { i ->
                com.wxiwei.office.common.bulletnumber.ListLevel().apply {
                    startAt = 1
                    // a char below 9 stands for the number of that level ("%1." in the file)
                    numberText = if (bullet) charArrayOf(bullets[i % bullets.size]) else charArrayOf(i.toChar(), '.')
                    numberFormat = if (bullet) 0 else formats[i % formats.size]
                    textIndent = 720 * (i + 1)
                    specialIndent = -360
                }
            }
            simpleList = 9
        })
    }

    /** Left indent (twips) of the paragraph at [offset], to step it. */
    fun indentLeftAt(offset: Long): Int = word.getDocument().getParagraph(offset)?.let { am.getParaIndentLeft(it.getAttribute()) } ?: 0

    private fun paragraphFormat(start: Long, end: Long, fileOp: (DocxEditor, Long, Long) -> Boolean, apply: (IAttributeSet) -> Unit): Boolean {
        synchronized(layoutLock) {
            ownError = null
            val doc = word.getDocument()
            val targets = ArrayList<IElement>()
            var offset = start
            while (true) {
                val para = doc.getParagraph(offset) ?: break
                targets.add(para)
                if (para.getEndOffset() >= maxOf(end, start + 1) || para.getEndOffset() <= offset) break
                offset = para.getEndOffset()
            }
            if (targets.isEmpty()) return refuse("No paragraph here")
            // the file needs original offsets: use the paragraphs' own starts, which typed text never moves
            val os = toOriginal(targets.first().getStartOffset())
            val oe = toOriginal(targets.last().getEndOffset() - 1)
            if (!fileOp(editor, os, maxOf(oe, os))) return false
            val before = targets.map { it.getAttribute()!!.clone() }
            targets.forEach { apply(it.getAttribute()!!) }
            val after = targets.map { it.getAttribute()!!.clone() }
            word.relayoutContent(targets.first().getStartOffset())
            fun restore(states: List<IAttributeSet>) {
                targets.forEachIndexed { i, p -> p.setAttribute(states[i].clone()) }
                word.relayoutContent(targets.first().getStartOffset())
            }
            undoStack.add(Step(
                undo = { editor.undoLast().also { if (it) restore(before) } },
                redo = { fileOp(editor, os, maxOf(oe, os)).also { if (it) restore(after) } },
            ))
            redoStack.clear()
            return true
        }
    }

    /** Inserts [text] before [offset]; line breaks in it split the paragraph (pasting several lines). */
    fun insertText(offset: Long, text: String): Boolean {
        synchronized(layoutLock) {
            ownError = null
            if (text.isEmpty()) return refuse("Nothing to insert")
            val lines = text.replace("\r\n", "\n").replace('\r', '\n')
            if (lines.length > 1 && lines.contains('\n')) {
                var at = offset
                for ((i, part) in lines.split('\n').withIndex()) {
                    if (i > 0) { if (!insertText(at, "\n")) return false; at += 1 }
                    if (part.isNotEmpty()) { if (!insertText(at, part)) return false; at += part.length }
                }
                return true
            }
            val doc = word.getDocument() as? WPDocument ?: return refuse("Not a Word document")
            val last = undoStack.lastOrNull() as? TypingStep
            // typing on at the end of the previous insert: one queued insert, one undo step
            if (last != null && last.at + last.text.length == offset && doc.insertMainText(offset, text)) {
                editor.undoLast()
                last.text += text
                last.edit.length = last.text.length.toLong()
                editor.insertText(last.original, last.text)
                last.handle = editor.lastOp()
                redoStack.clear()
                word.relayoutContent(offset)
                return true
            }
            // inside the text being typed (an IME editing its composing word)
            if (last != null && text != "\n" && offset >= last.at && offset < last.at + last.text.length) {
                editTyping(offset, offset, text)?.let { return it }
            }
            val original = toOriginal(offset)
            if (text == "\n") return splitParagraph(doc, offset, original)
            if (!editor.insertText(original, text)) return false
            if (!doc.insertMainText(offset, text)) {
                // a paragraph break or a non text position: saved, shown after reopening
                needsReopen = true
                undoStack.add(Step({ editor.undoLast() }, { editor.insertText(original, text) })); redoStack.clear()
                return true
            }
            val edit = Edit.Insert(offset, text.length.toLong())
            edits.add(edit)
            undoStack.add(TypingStep(offset, original, text, edit).also { it.handle = editor.lastOp() }); redoStack.clear()
            word.relayoutContent(offset)
            return true
        }
    }

    /** Enter at [offset]: the paragraph splits at once; the file gets a new w:p. */
    private fun splitParagraph(doc: WPDocument, offset: Long, original: Long): Boolean {
        if (!editor.insertText(original, "\n")) return false
        if (!doc.splitMainParagraph(offset)) {
            needsReopen = true
            undoStack.add(Step({ editor.undoLast() }, { editor.insertText(original, "\n") })); redoStack.clear()
            return true
        }
        val edit = Edit.Insert(offset, 1)
        edits.add(edit)
        undoStack.add(SplitStep(offset,
            undo = { editor.undoLast() && doc.joinMainParagraph(offset).also { edits.remove(edit); word.relayoutContent(offset) } },
            redo = { editor.insertText(original, "\n") && doc.splitMainParagraph(offset).also { edits.add(edit); word.relayoutContent(offset) } },
        ))
        redoStack.clear()
        word.relayoutContent(offset)
        return true
    }

    private fun joinParagraphs(doc: WPDocument, mark: Long, os: Long, oe: Long): Boolean {
        if (!editor.deleteText(os, oe)) return false
        if (!doc.joinMainParagraph(mark)) {
            needsReopen = true
            undoStack.add(Step({ editor.undoLast() }, { editor.deleteText(os, oe) })); redoStack.clear()
            return true
        }
        val edit = Edit.Delete(mark, 1)
        edits.add(edit)
        undoStack.add(Step(
            undo = { editor.undoLast() && doc.splitMainParagraph(mark).also { edits.remove(edit); word.relayoutContent(mark) } },
            redo = { editor.deleteText(os, oe) && doc.joinMainParagraph(mark).also { edits.add(edit); word.relayoutContent(mark) } },
        ))
        redoStack.clear()
        word.relayoutContent(mark)
        return true
    }

    fun deleteText(start: Long, end: Long): Boolean {
        synchronized(layoutLock) {
            ownError = null
            if (end <= start) return refuse("Empty range")
            // Backspace right after Enter at the same place: take the Enter back
            (undoStack.lastOrNull() as? SplitStep)?.let { if (it.at == start && end == start + 1) return undo() }
            editTyping(start, end, "")?.let { return it }
            if (touchesTyped(start, end)) return refuse("Save first to delete text typed in this session")
            val doc = word.getDocument() as? WPDocument ?: return refuse("Not a Word document")
            val os = toOriginal(start)
            val oe = toOriginal(end)
            val removed = doc.getText(start, end)
            // a lone paragraph mark: join the two paragraphs (Backspace at a paragraph start)
            if (removed == "\n") return joinParagraphs(doc, start, os, oe)
            if (!editor.deleteText(os, oe)) return false
            if (!doc.deleteMainText(start, end)) {
                needsReopen = true
                undoStack.add(Step({ editor.undoLast() }, { editor.deleteText(os, oe) })); redoStack.clear()
                return true
            }
            val edit = Edit.Delete(start, end - start)
            edits.add(edit)
            undoStack.add(Step(
                undo = {
                    editor.undoLast() && doc.insertMainText(start, removed).also {
                        edits.remove(edit); word.relayoutContent(start)
                    }
                },
                redo = {
                    editor.deleteText(os, oe) && doc.deleteMainText(start, end).also {
                        edits.add(edit); word.relayoutContent(start)
                    }
                },
            ))
            redoStack.clear()
            word.relayoutContent(start)
            return true
        }
    }

    fun replaceText(start: Long, end: Long, text: String): Boolean {
        synchronized(layoutLock) {
            if (text.isEmpty()) return deleteText(start, end)
            ownError = null
            if (end <= start) return refuse("Empty range")
            val nl = text.indexOf('\n')
            if (nl >= 0) {
                // several lines: the first replaces the range, the rest is inserted after it
                if (nl == 0) return insertText(start, "\n") && (if (text.length > 1) replaceText(start + 1, end + 1, text.substring(1)) else deleteText(start + 1, end + 1))
                return replaceText(start, end, text.substring(0, nl)) && insertText(start + nl, text.substring(nl))
            }
            editTyping(start, end, text)?.let { return it }
            if (touchesTyped(start, end)) return refuse("Save first to replace text typed in this session")
            val doc = word.getDocument() as? WPDocument ?: return refuse("Not a Word document")
            val os = toOriginal(start)
            val oe = toOriginal(end)
            val removed = doc.getText(start, end)
            // one file operation: a delete then an insert at the same place would lose the insert
            if (!editor.replaceText(os, oe, text)) return false
            // the new text goes into the run of the replaced text: insert first, then delete the old
            if (!doc.insertMainText(start, text) ) {
                needsReopen = true
                undoStack.add(Step({ editor.undoLast() }, { editor.replaceText(os, oe, text) })); redoStack.clear()
                return true
            }
            val n = text.length.toLong()
            doc.deleteMainText(start + n, end + n)
            val delete = Edit.Delete(start, end - start)
            val insert = Edit.Insert(start, n)
            edits.add(delete); edits.add(insert)
            undoStack.add(Step(
                undo = {
                    editor.undoLast() && doc.insertMainText(start + n, removed).also {
                        doc.deleteMainText(start, start + n)
                        edits.remove(insert); edits.remove(delete); word.relayoutContent(start)
                    }
                },
                redo = {
                    editor.replaceText(os, oe, text) && doc.insertMainText(start, text).also {
                        doc.deleteMainText(start + n, end + n)
                        edits.add(delete); edits.add(insert); word.relayoutContent(start)
                    }
                },
            ))
            redoStack.clear()
            word.relayoutContent(start)
            return true
        }
    }

    /**
     * Replaces [start, end) with [text] when the range lies in the text of the last typing step
     * (Backspace while typing, an IME changing its composing word): the step's text changes in
     * place, still one queued insert and one undo step. Null when the range is elsewhere.
     */
    private fun editTyping(start: Long, end: Long, text: String): Boolean? {
        val last = undoStack.lastOrNull() as? TypingStep ?: return null
        if (start < last.at || end > last.at + last.text.length) return null
        val doc = word.getDocument() as? WPDocument ?: return null
        // the new text goes into the run first, then the old text is removed
        if (text.isNotEmpty() && !doc.insertMainText(start, text)) return null
        if (end > start) doc.deleteMainText(start + text.length, end + text.length)
        val from = (start - last.at).toInt()
        val updated = last.text.substring(0, from) + text + last.text.substring((end - last.at).toInt())
        editor.undoLast()
        if (updated.isEmpty()) {
            undoStack.removeAt(undoStack.lastIndex)
            edits.remove(last.edit)
        } else {
            last.text = updated
            last.edit.length = updated.length.toLong()
            editor.insertText(last.original, updated)
            last.handle = editor.lastOp()
        }
        redoStack.clear()
        word.relayoutContent(start)
        return true
    }

    /** Text typed at one place; grows while the user keeps typing there. */
    private inner class TypingStep(val at: Long, val original: Long, var text: String, val edit: Edit.Insert) : Step(
        undo = { false }, redo = { false },
    ) {
        /** The queued insert of [text] (DocxEditor.lastOp), to format parts of it. */
        var handle: Any? = null

        override fun runUndo(): Boolean {
            val doc = word.getDocument() as? WPDocument ?: return false
            if (!editor.undoLast()) return false
            doc.deleteMainText(at, at + text.length)
            edits.remove(edit)
            word.relayoutContent(at)
            return true
        }

        override fun runRedo(): Boolean {
            val doc = word.getDocument() as? WPDocument ?: return false
            if (!editor.insertText(original, text) || !doc.insertMainText(at, text)) return false
            handle = editor.lastOp()
            edits.add(edit)
            word.relayoutContent(at)
            return true
        }
    }

    fun undo(): Boolean {
        synchronized(layoutLock) {
            val step = undoStack.lastOrNull() ?: return false
            if (!step.runUndo()) return false
            undoStack.removeAt(undoStack.lastIndex); redoStack.add(step); return true
        }
    }

    fun redo(): Boolean {
        synchronized(layoutLock) {
            val step = redoStack.lastOrNull() ?: return false
            if (!step.runRedo()) return false
            redoStack.removeAt(redoStack.lastIndex); undoStack.add(step); return true
        }
    }

    fun save(target: File): EditResult = editor.save(target)


    /** Leaves of [start, end) in every paragraph it touches, split at the ends. */
    private fun leaves(start: Long, end: Long): List<LeafElement> {
        val doc = word.getDocument()
        val result = ArrayList<LeafElement>()
        var offset = start
        while (offset < end) {
            val para = doc.getParagraph(offset) as? ParagraphElement ?: break
            result.addAll(para.leavesFor(maxOf(start, para.getStartOffset()), minOf(end, para.getEndOffset())))
            if (para.getEndOffset() <= offset) break
            offset = para.getEndOffset()
        }
        return result
    }

    private fun format(start: Long, end: Long, fileOp: (DocxEditor, Long, Long) -> Boolean, apply: (IAttributeSet) -> Unit): Boolean {
        synchronized(layoutLock) {
            ownError = null
            if (end <= start) return refuse("Empty range")
            // original text and text typed in this session take different file operations
            val parts = ArrayList<Triple<Long, Long, Pair<TypingStep, Int>?>>()
            var i = start
            while (i < end) {
                val typed = typedAt(i)
                var j = i + 1
                while (j < end && typedAt(j)?.first === typed?.first) j++
                if (typed == null && touchesTyped(i, j)) return refuse("Save first to format this text")
                parts.add(Triple(i, j, typed))
                i = j
            }
            val fileOps = parts.map { (s, e, typed) ->
                if (typed == null) {
                    val os = toOriginal(s); val oe = toOriginal(e)
                    ({ fileOp(editor, os, oe) } to { editor.undoLast() })
                } else {
                    val (step, from) = typed
                    val to = from + (e - s)
                    ({ step.handle?.let { h -> editor.formatInserted(h) { fileOp(editor, from.toLong(), to) } } == true } to
                        { step.handle?.let { editor.unformatInserted(it) } == true })
                }
            }
            fun redoFile(): Boolean {
                for ((k, op) in fileOps.withIndex()) if (!op.first()) { for (u in fileOps.take(k).asReversed()) u.second(); return false }
                return true
            }
            if (!redoFile()) return false
            val targets = leaves(start, end)
            val before = targets.map { it.getAttribute().clone() }
            targets.forEach { apply(it.getAttribute()) }
            val after = targets.map { it.getAttribute().clone() }
            word.relayoutContent(start)
            fun restore(states: List<IAttributeSet>) {
                targets.forEachIndexed { i, leaf -> leaf.setAttribute(states[i].clone()) }
                word.relayoutContent(start)
            }
            undoStack.add(Step(
                undo = { fileOps.asReversed().all { it.second() }.also { restore(before) } },
                redo = { redoFile().also { if (it) restore(after) } },
            ))
            redoStack.clear()
            return true
        }
    }

    /** The typing step whose text holds the character at [pos], and its index there. */
    private fun typedAt(pos: Long): Pair<TypingStep, Int>? {
        var x = pos
        for (edit in edits.asReversed()) when (edit) {
            is Edit.Insert -> {
                val a = edit.at
                if (x >= a && x < a + edit.length) {
                    val step = undoStack.firstOrNull { it is TypingStep && it.edit === edit } as? TypingStep ?: return null
                    return step to (x - a).toInt()
                }
                if (x >= a + edit.length) x -= edit.length
            }
            is Edit.Delete -> if (x >= edit.at) x += edit.length
        }
        return null
    }
}
