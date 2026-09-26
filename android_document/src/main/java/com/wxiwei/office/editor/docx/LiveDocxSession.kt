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
 * (a paragraph break, a range across paragraphs) are still saved: [needsReopen]. Formatting text
 * typed in this session is refused until saved ([lastError]). Main thread only.
 */
class LiveDocxSession(control: IControl, private val source: File) {
    private val word = control.getView() as? Word ?: error("Open a Word document first")
    private val editor = DocxEditor(source, DocxSourceMap.get(source.absolutePath) ?: error("Document is still loading"))
    private val am = AttrManage.instance()

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

    /** Left indent (twips) of the paragraph at [offset], to step it. */
    fun indentLeftAt(offset: Long): Int = word.getDocument().getParagraph(offset)?.let { am.getParaIndentLeft(it.getAttribute()) } ?: 0

    private fun paragraphFormat(start: Long, end: Long, fileOp: (DocxEditor, Long, Long) -> Boolean, apply: (IAttributeSet) -> Unit): Boolean {
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
        word.relayoutContent()
        fun restore(states: List<IAttributeSet>) {
            targets.forEachIndexed { i, p -> p.setAttribute(states[i].clone()) }
            word.relayoutContent()
        }
        undoStack.add(Step(
            undo = { editor.undoLast().also { if (it) restore(before) } },
            redo = { fileOp(editor, os, maxOf(oe, os)).also { if (it) restore(after) } },
        ))
        redoStack.clear()
        return true
    }

    fun insertText(offset: Long, text: String): Boolean {
        ownError = null
        if (text.isEmpty()) return refuse("Nothing to insert")
        val doc = word.getDocument() as? WPDocument ?: return refuse("Not a Word document")
        val last = undoStack.lastOrNull() as? TypingStep
        // typing on at the end of the previous insert: one queued insert, one undo step
        if (last != null && last.at + last.text.length == offset && doc.insertMainText(offset, text)) {
            editor.undoLast()
            last.text += text
            last.edit.length = last.text.length.toLong()
            editor.insertText(last.original, last.text)
            redoStack.clear()
            word.relayoutContent()
            return true
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
        undoStack.add(TypingStep(offset, original, text, edit)); redoStack.clear()
        word.relayoutContent()
        return true
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
            undo = { editor.undoLast() && doc.joinMainParagraph(offset).also { edits.remove(edit); word.relayoutContent() } },
            redo = { editor.insertText(original, "\n") && doc.splitMainParagraph(offset).also { edits.add(edit); word.relayoutContent() } },
        ))
        redoStack.clear()
        word.relayoutContent()
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
            undo = { editor.undoLast() && doc.splitMainParagraph(mark).also { edits.remove(edit); word.relayoutContent() } },
            redo = { editor.deleteText(os, oe) && doc.joinMainParagraph(mark).also { edits.add(edit); word.relayoutContent() } },
        ))
        redoStack.clear()
        word.relayoutContent()
        return true
    }

    fun deleteText(start: Long, end: Long): Boolean {
        ownError = null
        if (end <= start) return refuse("Empty range")
        // Backspace right after Enter at the same place: take the Enter back
        (undoStack.lastOrNull() as? SplitStep)?.let { if (it.at == start && end == start + 1) return undo() }
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
                    edits.remove(edit); word.relayoutContent()
                }
            },
            redo = {
                editor.deleteText(os, oe) && doc.deleteMainText(start, end).also {
                    edits.add(edit); word.relayoutContent()
                }
            },
        ))
        redoStack.clear()
        word.relayoutContent()
        return true
    }

    fun replaceText(start: Long, end: Long, text: String): Boolean {
        if (text.isEmpty()) return deleteText(start, end)
        ownError = null
        if (end <= start) return refuse("Empty range")
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
                    edits.remove(insert); edits.remove(delete); word.relayoutContent()
                }
            },
            redo = {
                editor.replaceText(os, oe, text) && doc.insertMainText(start, text).also {
                    doc.deleteMainText(start + n, end + n)
                    edits.add(delete); edits.add(insert); word.relayoutContent()
                }
            },
        ))
        redoStack.clear()
        word.relayoutContent()
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
            word.relayoutContent()
            return true
        }

        override fun runRedo(): Boolean {
            val doc = word.getDocument() as? WPDocument ?: return false
            if (!editor.insertText(original, text) || !doc.insertMainText(at, text)) return false
            edits.add(edit)
            word.relayoutContent()
            return true
        }
    }

    fun undo(): Boolean {
        val step = undoStack.lastOrNull() ?: return false
        if (!step.runUndo()) return false
        undoStack.removeAt(undoStack.lastIndex); redoStack.add(step); return true
    }

    fun redo(): Boolean {
        val step = redoStack.lastOrNull() ?: return false
        if (!step.runRedo()) return false
        redoStack.removeAt(redoStack.lastIndex); undoStack.add(step); return true
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
        ownError = null
        if (end <= start) return refuse("Empty range")
        if (touchesTyped(start, end)) return refuse("Save first to format text typed in this session")
        val os = toOriginal(start)
        val oe = toOriginal(end)
        if (!fileOp(editor, os, oe)) return false
        val targets = leaves(start, end)
        val before = targets.map { it.getAttribute().clone() }
        targets.forEach { apply(it.getAttribute()) }
        val after = targets.map { it.getAttribute().clone() }
        word.relayoutContent()
        fun restore(states: List<IAttributeSet>) {
            targets.forEachIndexed { i, leaf -> leaf.setAttribute(states[i].clone()) }
            word.relayoutContent()
        }
        undoStack.add(Step(
            undo = { editor.undoLast().also { if (it) restore(before) } },
            redo = { fileOp(editor, os, oe).also { if (it) restore(after) } },
        ))
        redoStack.clear()
        return true
    }
}
