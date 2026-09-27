package com.wxiwei.office.editor.docx

import com.wxiwei.office.constant.wp.WPModelConstant
import android.graphics.Color
import com.wxiwei.office.editor.EditResult
import com.wxiwei.office.editor.UndoStack
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
    private companion object { const val EMU_PER_PX = 9525L }

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
    private val undoStack = UndoStack<Step>()
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
        // "none" takes the highlight away
        if (rgbHex == "none") return format(start, end, { e, s, t -> e.highlight(s, t, "none") }) { am.setFontHighLight(it, -1) }
        val rgb = rgbHex.removePrefix("#")
        val color = rgb.toIntOrNull(16)?.let { (0xFF shl 24) or it } ?: Color.YELLOW
        return format(start, end, { e, s, t -> e.highlight(s, t, rgb) }) { am.setFontHighLight(it, color) }
    }

    // ---- text changes --------------------------------------------------------------------

    /** A live text change, in current offsets at the time it was made. */
    private sealed class Edit {
        abstract val at: Long
        class Insert(override val at: Long, var length: Long) : Edit()
        class Delete(override val at: Long, val length: Long) : Edit()
    }
    private val edits = ArrayList<Edit>()
    /** The queued file insert of each live insert, whose text is taken from the view on save. */
    private val handles = java.util.IdentityHashMap<Edit.Insert, Any>()
    private fun track(edit: Edit.Insert) { editor.lastOp()?.let { handles[edit] = it } }

    /** The story (body, headers, footers, one text box) of an offset: edits in one never move offsets of another. */
    private fun area(offset: Long) = offset and (WPModelConstant.AREA_MASK or WPModelConstant.TEXTBOX_MASK)

    /** Current model offset -> original file offset (the start of typed text for positions inside it). */
    private fun toOriginal(offset: Long): Long {
        var x = offset
        for (e in edits.asReversed()) if (area(e.at) == area(offset)) when (e) {
            is Edit.Insert -> if (x >= e.at + e.length) x -= e.length else if (x > e.at) x = e.at
            is Edit.Delete -> if (x > e.at) x += e.length
        }
        return x
    }

    /** True when [start, end) contains text typed in this session (it has no original offsets). */
    private fun touchesTyped(start: Long, end: Long): Boolean {
        var s = start
        var e = end
        for (edit in edits.asReversed()) if (area(edit.at) == area(start)) when (edit) {
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
            // after the last paragraph mark there is no paragraph to hold the text
            val wp = word.getDocument() as? WPDocument ?: return refuse("Not a Word document")
            if (!wp.isEditableArea(offset)) return refuse("Only the body, headers, footers and text boxes are editable")
            if (offset < 0 || offset >= wp.storyEnd(offset)) return refuse("Cannot insert after the end of the document")
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
                track(last.edit)
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
            track(edit)
            undoStack.add(TypingStep(offset, original, text, edit)); redoStack.clear()
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
        track(edit)
        undoStack.add(SplitStep(offset,
            undo = { editor.undoLast() && doc.joinMainParagraph(offset).also { edits.remove(edit); word.relayoutContent(offset) } },
            redo = { editor.insertText(original, "\n") && doc.splitMainParagraph(offset).also { track(edit); edits.add(edit); word.relayoutContent(offset) } },
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
            val doc = word.getDocument() as? WPDocument ?: return refuse("Not a Word document")
            val removedAll = doc.getText(start, end)
            // text inserted in this session, paragraph marks, or several paragraphs: piece by piece
            if (touchesTyped(start, end) || (removedAll.length > 1 && removedAll.contains('\n'))) return deletePieces(doc, start, end)
            val os = toOriginal(start)
            val oe = toOriginal(end)
            val removed = removedAll
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
            // over text inserted in this session: delete, then insert (its text is taken on save)
            if (touchesTyped(start, end)) return grouped { deleteText(start, end) && insertText(start, text) }
            // across paragraphs: the marks go piece by piece
            if ((word.getDocument() as? WPDocument)?.getText(start, end)?.contains('\n') == true) return grouped { deleteText(start, end) && insertText(start, text) }
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
            track(insert)
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
                        track(insert)
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
            track(last.edit)
        }
        redoStack.clear()
        word.relayoutContent(start)
        return true
    }

    /** Text typed at one place; grows while the user keeps typing there. */
    private inner class TypingStep(val at: Long, val original: Long, var text: String, val edit: Edit.Insert) : Step(
        undo = { false }, redo = { false },
    ) {

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
            track(edit)
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

    fun save(target: File): EditResult = synchronized(layoutLock) { editor.save(target, overrides()) }


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
            // text inserted in this session is written from the view on save: only original text
            // takes file operations
            val parts = ArrayList<Pair<Long, Long>>()
            var i = start
            while (i < end) {
                val inserted = insertedAt(i)
                var j = i + 1
                while (j < end && insertedAt(j) == inserted) j++
                if (!inserted) parts.add(i to j)
                i = j
            }
            val fileOps = parts.map { (s, e) ->
                val os = toOriginal(s); val oe = toOriginal(e)
                ({ fileOp(editor, os, oe) } to { editor.undoLast() })
            }
            fun redoFile(): Boolean {
                for ((k, op) in fileOps.withIndex()) if (!op.first()) { for (u in fileOps.take(k).asReversed()) u.second(); return false }
                return true
            }
            if (!redoFile()) return false
            // by offsets, not leaf objects: undoing and redoing text before this step makes new leaves
            val before = leaves(start, end).map { Triple(it.getStartOffset(), it.getEndOffset(), it.getAttribute().clone()) }
            fun applyNow() {
                leaves(start, end).forEach { apply(it.getAttribute()) }
                word.relayoutContent(start)
            }
            fun restoreBefore() {
                for ((s, e, attr) in before) leaves(s, e).forEach { it.setAttribute(attr.clone()) }
                word.relayoutContent(start)
            }
            applyNow()
            undoStack.add(Step(
                undo = { fileOps.asReversed().all { it.second() }.also { restoreBefore() } },
                redo = { redoFile().also { if (it) applyNow() } },
            ))
            redoStack.clear()
            return true
        }
    }

    /** True when the character at [pos] was inserted in this session (typed, pasted, an Enter). */
    private fun insertedAt(pos: Long): Boolean {
        var x = pos
        for (edit in edits.asReversed()) if (area(edit.at) == area(pos)) when (edit) {
            is Edit.Insert -> {
                if (x >= edit.at && x < edit.at + edit.length) return true
                if (x >= edit.at + edit.length) x -= edit.length
            }
            is Edit.Delete -> if (x >= edit.at) x += edit.length
        }
        return false
    }

    /**
     * A picture at [offset], [widthPx] x [heightPx] (96 dpi). It is written to the file; the view shows
     * it after the file is read again ([needsReopen]).
     */
    fun insertImage(offset: Long, image: File, widthPx: Int, heightPx: Int): Boolean = synchronized(layoutLock) {
        ownError = null
        if (!editor.insertImage(toOriginal(offset), image, widthPx, heightPx)) return false
        needsReopen = true
        undoStack.add(Step({ editor.undoLast() }, { editor.insertImage(toOriginal(offset), image, widthPx, heightPx) })); redoStack.clear()
        true
    }

    /** The picture or shape whose one-char object is at [offset], or null. */
    fun shapeAt(offset: Long): com.wxiwei.office.common.shape.IShape? {
        val doc = word.getDocument()
        val leaf = doc.getLeaf(offset) ?: return null
        if (leaf.getEndOffset() - leaf.getStartOffset() != 1L) return null
        val id = am.getShapeID(leaf.getAttribute())
        if (id < 0) return null
        return word.getControl().getSysKit().getWPShapeManage().getShape(id)
    }

    private fun objectEdit(fileOp: () -> Boolean): Boolean = synchronized(layoutLock) {
        ownError = null
        if (!fileOp()) return false
        needsReopen = true
        undoStack.add(Step({ editor.undoLast() }, fileOp)); redoStack.clear()
        true
    }

    /** New size (pixels at 96 dpi) of the picture at [offset]; shown after the file is read again. */
    fun resizeObject(offset: Long, widthPx: Int, heightPx: Int): Boolean =
        objectEdit { editor.resizeObject(toOriginal(offset), widthPx * EMU_PER_PX, heightPx * EMU_PER_PX) }

    /** Moves the in-line picture at [from] to the text position [to]; shown after the file is read again. */
    fun moveObject(from: Long, to: Long): Boolean = objectEdit { editor.moveObject(toOriginal(from), toOriginal(to)) }

    /** Moves the floating picture at [offset] by [dxPx], [dyPx]; shown after the file is read again. */
    fun shiftObject(offset: Long, dxPx: Int, dyPx: Int): Boolean =
        objectEdit { editor.shiftObject(toOriginal(offset), dxPx * EMU_PER_PX, dyPx * EMU_PER_PX) }

    /** A [rows] x [cols] table after the paragraph at [offset]; shown after the file is read again ([needsReopen]). */
    fun insertTable(offset: Long, rows: Int, cols: Int): Boolean = synchronized(layoutLock) {
        ownError = null
        if (!editor.insertTable(toOriginal(offset), rows, cols)) return false
        needsReopen = true
        undoStack.add(Step({ editor.undoLast() }, { editor.insertTable(toOriginal(offset), rows, cols) })); redoStack.clear()
        true
    }

    /** Text with its character formatting, copied with [copyFormatted] to paste with [pasteFormatted]. */
    class FormattedText(val text: String, val spans: List<Span>) {
        /** Formatting of chars [from, to); [highlight] is an ARGB fill or null. */
        data class Span(val from: Int, val to: Int, val bold: Boolean, val italic: Boolean, val underline: Boolean,
                        val rgb: Int, val sizePt: Float, val highlight: Int?)
    }

    /** The text of [start, end) with the formatting shown on it. */
    fun copyFormatted(start: Long, end: Long): FormattedText = synchronized(layoutLock) {
        val doc = word.getDocument()
        val spans = ArrayList<FormattedText.Span>()
        var pos = start
        while (pos < end) {
            val para = doc.getParagraph(pos) ?: break
            val leaf = doc.getLeaf(pos) ?: break
            val stop = minOf(end, leaf.getEndOffset()).let { if (it <= pos) pos + 1 else it }
            val p = para.getAttribute(); val l = leaf.getAttribute()
            val fill = am.getFontHighLight(p, l).takeIf { it != -1 && it != Int.MIN_VALUE && (it ushr 24) != 0 }
            spans.add(FormattedText.Span((pos - start).toInt(), (stop - start).toInt(), am.getFontBold(p, l), am.getFontItalic(p, l),
                am.getFontUnderline(p, l) > 0, am.getFontColor(p, l) and 0xFFFFFF, am.getFontSizeF(p, l), fill))
            pos = stop
        }
        FormattedText(doc.getText(start, end), spans)
    }

    /**
     * Puts [clip] over [start, end) (an insert when empty) with its formatting, as one undo step.
     * Only what differs from how the pasted text shows is set.
     */
    fun pasteFormatted(start: Long, end: Long, clip: FormattedText): Boolean = grouped {
        if (clip.text.isEmpty()) return@grouped refuse("Nothing to paste")
        val placed = if (end > start) replaceText(start, end, clip.text) else insertText(start, clip.text)
        placed && clip.spans.all { s ->
            val a = start + s.from
            val b = start + s.to
            // the paragraph marks of a multi-paragraph paste carry no text formatting
            if (clip.text.substring(s.from, s.to).all { it == '\n' }) return@all true
            (isBold(a) == s.bold || setBold(a, b, s.bold)) &&
                (isItalic(a) == s.italic || setItalic(a, b, s.italic)) &&
                (isUnderlined(a) == s.underline || setUnderline(a, b, s.underline)) &&
                (colorAt(a) == s.rgb || setTextColor(a, b, "%06X".format(s.rgb))) &&
                (Math.abs(sizeAt(a) - s.sizePt) < 0.01f || setFontSize(a, b, s.sizePt)) &&
                (s.highlight == null || highlight(a, b, "%06X".format(s.highlight and 0xFFFFFF)))
        }
    }

    private fun colorAt(offset: Long): Int {
        val doc = word.getDocument()
        val para = doc.getParagraph(offset) ?: return -1
        val leaf = doc.getLeaf(offset) ?: return -1
        return am.getFontColor(para.getAttribute(), leaf.getAttribute()) and 0xFFFFFF
    }

    private fun sizeAt(offset: Long): Float {
        val doc = word.getDocument()
        val para = doc.getParagraph(offset) ?: return 0f
        val leaf = doc.getLeaf(offset) ?: return 0f
        return am.getFontSizeF(para.getAttribute(), leaf.getAttribute())
    }

    /** Runs [block] as one undo step, whatever steps it records. */
    private fun grouped(block: () -> Boolean): Boolean {
        val mark = undoStack.size
        val ok = block()
        if (undoStack.size - mark > 1) {
            val steps = ArrayList(undoStack.subList(mark, undoStack.size))
            repeat(steps.size) { undoStack.removeAt(undoStack.lastIndex) }
            undoStack.add(Step(
                undo = { steps.asReversed().all { it.runUndo() } },
                redo = { steps.all { it.runRedo() } },
            ))
        }
        return ok
    }

    /**
     * Deletes [start, end) from the end backwards: paragraph marks join paragraphs, text inserted
     * in this session only leaves the view (its file text is taken from the view on save),
     * original text is also deleted in the file. One undo step.
     */
    private fun deletePieces(doc: WPDocument, start: Long, end: Long): Boolean = grouped {
        var e = end
        var ok = true
        while (e > start && ok) {
            val c = doc.getText(e - 1, e)
            val inserted = insertedAt(e - 1)
            var s = e - 1
            if (c != "\n") while (s > start && doc.getText(s - 1, s) != "\n" && insertedAt(s - 1) == inserted) s--
            ok = deletePiece(doc, s, e, c == "\n", inserted)
            e = s
        }
        ok
    }

    private fun deletePiece(doc: WPDocument, s: Long, e: Long, mark: Boolean, inserted: Boolean): Boolean {
        val os = toOriginal(s)
        val oe = toOriginal(e)
        val removed = doc.getText(s, e)
        if (!inserted && !editor.deleteText(os, oe)) return false
        val fileUndo = { if (inserted) true else editor.undoLast() }
        val fileRedo = { if (inserted) true else editor.deleteText(os, oe) }
        val live = { if (mark) doc.joinMainParagraph(s) else doc.deleteMainText(s, e) }
        val back = { if (mark) doc.splitMainParagraph(s) else doc.insertMainText(s, removed) }
        if (!live()) {
            if (!inserted) editor.undoLast()
            return refuse("Cannot delete here")
        }
        val edit = Edit.Delete(s, e - s)
        edits.add(edit)
        undoStack.add(Step(
            undo = { fileUndo() && back().also { edits.remove(edit); word.relayoutContent(s) } },
            redo = { fileRedo() && live().also { edits.add(edit); word.relayoutContent(s) } },
        ))
        redoStack.clear()
        word.relayoutContent(s)
        return true
    }

    /** Text and run formatting of the live inserts, as shown now, for [DocxEditor.save]. */
    private fun overrides(): Map<Any, DocxEditor.InsertOverride> {
        class Group(var s: Long, var e: Long, val leader: Any)
        val groups = ArrayList<Group>()
        val member = java.util.IdentityHashMap<Any, Group>()
        for ((k, edit) in edits.withIndex()) {
            if (edit !is Edit.Insert) continue
            val op = handles[edit] ?: continue
            // where that text is now
            var s = edit.at
            var e = edit.at + edit.length
            for (later in edits.subList(k + 1, edits.size)) if (area(later.at) == area(edit.at)) when (later) {
                is Edit.Insert -> if (later.at < s) { s += later.length; e += later.length } else if (later.at <= e) e += later.length
                is Edit.Delete -> {
                    val a = later.at
                    val b = a + later.length
                    fun cut(v: Long) = when { v >= b -> v - later.length; v > a -> a; else -> v }
                    s = cut(s); e = cut(e)
                }
            }
            // inserts touching each other are written together, by the first of them
            val touching = groups.filter { s <= it.e && e >= it.s }
            val group = touching.firstOrNull() ?: Group(s, e, op).also { groups.add(it) }
            for (other in touching.drop(1)) {
                group.s = minOf(group.s, other.s); group.e = maxOf(group.e, other.e)
                member.entries.filter { it.value === other }.forEach { it.setValue(group) }
                groups.remove(other)
            }
            group.s = minOf(group.s, s); group.e = maxOf(group.e, e)
            member[op] = group
        }
        val doc = word.getDocument()
        val result = java.util.IdentityHashMap<Any, DocxEditor.InsertOverride>()
        for ((op, g) in member) {
            result[op] = if (g.leader === op) DocxEditor.InsertOverride(doc.getText(g.s, g.e), runFormats(g.s, g.e))
            else DocxEditor.InsertOverride("", emptyList())
        }
        return result
    }

    /** The formatting shown on [s, e), as run properties relative to [s]. */
    private fun runFormats(s: Long, e: Long): List<DocxEditor.RunFormat> {
        val doc = word.getDocument()
        val out = ArrayList<DocxEditor.RunFormat>()
        var pos = s
        while (pos < e) {
            val para = doc.getParagraph(pos) ?: break
            val leaf = doc.getLeaf(pos) ?: break
            val stop = minOf(e, leaf.getEndOffset()).let { if (it <= pos) pos + 1 else it }
            val p = para.getAttribute()
            val l = leaf.getAttribute()
            val props = arrayListOf(
                "b" to (if (am.getFontBold(p, l)) "1" else "0"),
                "i" to (if (am.getFontItalic(p, l)) "1" else "0"),
                "u" to (if (am.getFontUnderline(p, l) > 0) "single" else "none"),
                "color" to "%06X".format(am.getFontColor(p, l) and 0xFFFFFF),
                "sz" to Math.round(am.getFontSizeF(p, l) * 2).toString(),
            )
            val highlight = am.getFontHighLight(p, l)
            if (highlight != -1 && highlight != Int.MIN_VALUE && (highlight ushr 24) != 0) props.add("shd" to "%06X".format(highlight and 0xFFFFFF))
            out.add(DocxEditor.RunFormat((pos - s).toInt(), (stop - s).toInt(), props))
            pos = stop
        }
        return out
    }
}
