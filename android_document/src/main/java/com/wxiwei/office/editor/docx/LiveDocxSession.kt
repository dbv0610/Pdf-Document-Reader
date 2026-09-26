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
import java.io.File

/**
 * Realtime formatting for an open .docx: bold, italic, underline, color, size and highlight show
 * at once (the model leaves are updated and the pages laid out again) and are queued in
 * [DocxEditor], which writes them on [save]. Text changes (insert/delete/replace) are queued too
 * but only show after saving and reopening: [needsReopen].
 *
 * Offsets are model offsets, end exclusive; formatting does not move them. Main thread only.
 */
class LiveDocxSession(control: IControl, private val source: File) {
    private val word = control.getView() as? Word ?: error("Open a Word document first")
    private val editor = DocxEditor(source, DocxSourceMap.get(source.absolutePath) ?: error("Document is still loading"))
    private val am = AttrManage.instance()

    private class Step(val undo: () -> Boolean, val redo: () -> Boolean)
    private val undoStack = ArrayList<Step>()
    private val redoStack = ArrayList<Step>()

    /** True after a text change: it is saved, but the view shows it only after reopening. */
    var needsReopen = false
        private set
    val lastError: EditResult.Error? get() = editor.lastError
    fun canUndo() = undoStack.isNotEmpty()
    fun canRedo() = redoStack.isNotEmpty()
    fun hasChanges() = editor.pendingCount > 0

    fun setBold(start: Long, end: Long, on: Boolean) = format(start, end, { it.setBold(start, end, on) }) { am.setFontBold(it, on) }
    fun setItalic(start: Long, end: Long, on: Boolean) = format(start, end, { it.setItalic(start, end, on) }) { am.setFontItalic(it, on) }
    fun setUnderline(start: Long, end: Long, on: Boolean) = format(start, end, { it.setUnderline(start, end, on) }) { am.setFontUnderline(it, if (on) 1 else 0) }
    fun setTextColor(start: Long, end: Long, rgbHex: String): Boolean {
        val rgb = rgbHex.removePrefix("#")
        val color = rgb.toIntOrNull(16)?.let { (0xFF shl 24) or it } ?: return editor.setTextColor(start, end, rgb)
        return format(start, end, { it.setTextColor(start, end, rgb) }) { am.setFontColor(it, color) }
    }
    fun setFontSize(start: Long, end: Long, pt: Float) = format(start, end, { it.setFontSize(start, end, pt) }) { am.setFontSize(it, pt) }
    fun highlight(start: Long, end: Long, rgbHex: String = "FFFF00"): Boolean {
        val rgb = rgbHex.removePrefix("#")
        val color = rgb.toIntOrNull(16)?.let { (0xFF shl 24) or it } ?: Color.YELLOW
        return format(start, end, { it.highlight(start, end, rgb) }) { am.setFontHighLight(it, color) }
    }

    fun insertText(offset: Long, text: String) = textChange { it.insertText(offset, text) }
    fun deleteText(start: Long, end: Long) = textChange { it.deleteText(start, end) }
    fun replaceText(start: Long, end: Long, text: String) = textChange { it.replaceText(start, end, text) }

    fun undo(): Boolean {
        val step = undoStack.lastOrNull() ?: return false
        if (!step.undo()) return false
        undoStack.removeAt(undoStack.lastIndex); redoStack.add(step); return true
    }

    fun redo(): Boolean {
        val step = redoStack.lastOrNull() ?: return false
        if (!step.redo()) return false
        redoStack.removeAt(redoStack.lastIndex); undoStack.add(step); return true
    }

    fun save(target: File): EditResult = editor.save(target)

    private fun textChange(op: (DocxEditor) -> Boolean): Boolean {
        if (!op(editor)) return false
        needsReopen = true
        undoStack.add(Step({ editor.undoLast() }, { op(editor) })); redoStack.clear()
        return true
    }

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

    private fun format(start: Long, end: Long, fileOp: (DocxEditor) -> Boolean, apply: (IAttributeSet) -> Unit): Boolean {
        if (end <= start) return false
        if (!fileOp(editor)) return false
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
            redo = { fileOp(editor).also { if (it) restore(after) } },
        ))
        redoStack.clear()
        return true
    }
}
