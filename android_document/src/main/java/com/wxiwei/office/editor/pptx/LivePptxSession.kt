package com.wxiwei.office.editor.pptx

import com.wxiwei.office.editor.EditResult
import com.wxiwei.office.system.IControl
import java.io.File

/**
 * Realtime PPTX editing: every call changes the open slide immediately ([LiveSlideDisplay]) and
 * queues the same change in [PptxEditor], which writes the file on [save]. No reopen is needed.
 *
 * Main thread only, like the viewer. When the view cannot show an edit live (for example a shape
 * inside a group that has to be deleted), the edit is still saved and [needsReopen] becomes true.
 */
class LivePptxSession internal constructor(private val editor: PptxEditor, private val display: LiveSlideDisplay) {
    constructor(control: IControl, source: File) : this(PptxEditor(source), LiveSlideModel(control))

    fun interface OnChangeListener { fun onChanged(canUndo: Boolean, canRedo: Boolean) }

    /** A reversible change, applied to both layers. */
    private class Step(val redo: () -> Boolean, val undo: () -> Boolean)

    private val undoStack = ArrayList<Step>()
    private val redoStack = ArrayList<Step>()
    private var changes = 0

    var listener: OnChangeListener? = null
    /** Last failure of the file layer (invalid id, missing slide...). */
    val lastError: EditResult.Error? get() = editor.lastError
    /** True when at least one saved edit could not be shown live; reopen the saved file to see it. */
    var needsReopen = false
        private set

    fun slideSizeEmu(): Size = editor.slideSizeEmu()
    fun listShapes(slideIndex: Int): List<PptxShapeInfo> = editor.listShapes(slideIndex)
    fun canUndo() = undoStack.isNotEmpty()
    fun canRedo() = redoStack.isNotEmpty()
    fun hasChanges() = changes != 0

    private fun live(ok: Boolean) { if (!ok) needsReopen = true }

    private fun push(step: Step) {
        undoStack.add(step); redoStack.clear(); changes++
        listener?.onChanged(canUndo(), canRedo())
    }

    /**
     * Every session call queues exactly one [PptxEditor] op, so undo drops the last queued op
     * ([PptxEditor.undoLast]) and redo queues it again; the display layer reverts on its own.
     */
    private fun record(redoFile: () -> Boolean, redoLive: () -> Boolean, undoLive: () -> Boolean) = push(Step(
        redo = { redoFile().also { if (it) live(redoLive()) } },
        undo = { editor.undoLast().also { if (it) live(undoLive()) } }))

    /** Returns the new shape id, or -1 ([lastError] says why). */
    fun addTextBox(slideIndex: Int, rectEmu: Rect, text: String, sizePt: Float = 18f, rgbHex: String = "000000", bold: Boolean = false): Int {
        val id = editor.addTextBox(slideIndex, rectEmu, text, sizePt, rgbHex, bold)
        if (id < 0) return -1
        val show = { display.addTextBox(slideIndex, id, rectEmu, text, sizePt, rgbHex, bold) }
        live(show())
        // Re-adding gets the same id: ids are max + 1 and the undone shape had the max id
        record({ editor.addTextBox(slideIndex, rectEmu, text, sizePt, rgbHex, bold) == id }, show) { display.removeShape(slideIndex, id) != null }
        return id
    }

    fun addImage(slideIndex: Int, rectEmu: Rect, imageFile: File): Int {
        val id = editor.addImage(slideIndex, rectEmu, imageFile)
        if (id < 0) return -1
        val show = { display.addImage(slideIndex, id, rectEmu, imageFile) }
        live(show())
        record({ editor.addImage(slideIndex, rectEmu, imageFile) == id }, show) { display.removeShape(slideIndex, id) != null }
        return id
    }

    fun setShapeText(slideIndex: Int, shapeId: Int, text: String): Boolean {
        val old = display.shapeText(slideIndex, shapeId) ?: listShapes(slideIndex).firstOrNull { it.id == shapeId }?.text
        if (!editor.setShapeText(slideIndex, shapeId, text)) return false
        val show = { display.setShapeText(slideIndex, shapeId, text) }
        live(show())
        record({ editor.setShapeText(slideIndex, shapeId, text) }, show) { old != null && display.setShapeText(slideIndex, shapeId, old) }
        return true
    }

    fun moveShape(slideIndex: Int, shapeId: Int, rectEmu: Rect): Boolean {
        val old = display.shapeRect(slideIndex, shapeId) ?: listShapes(slideIndex).firstOrNull { it.id == shapeId }?.rectEmu
        if (!editor.moveShape(slideIndex, shapeId, rectEmu)) return false
        val show = { display.moveShape(slideIndex, shapeId, rectEmu) }
        live(show())
        record({ editor.moveShape(slideIndex, shapeId, rectEmu) }, show) { old != null && display.moveShape(slideIndex, shapeId, old) }
        return true
    }

    fun deleteShape(slideIndex: Int, shapeId: Int): Boolean {
        if (!editor.deleteShape(slideIndex, shapeId)) return false
        var token = display.removeShape(slideIndex, shapeId)
        live(token != null)
        record({ editor.deleteShape(slideIndex, shapeId) },
            { display.removeShape(slideIndex, shapeId).also { token = it } != null },
            { token?.let { display.restoreShape(slideIndex, it) } ?: false })
        return true
    }

    fun undo(): Boolean {
        val step = undoStack.lastOrNull() ?: return false
        if (!step.undo()) return false
        undoStack.removeAt(undoStack.lastIndex); redoStack.add(step); changes--
        listener?.onChanged(canUndo(), canRedo())
        return true
    }

    fun redo(): Boolean {
        val step = redoStack.lastOrNull() ?: return false
        if (!step.redo()) return false
        redoStack.removeAt(redoStack.lastIndex); undoStack.add(step); changes++
        listener?.onChanged(canUndo(), canRedo())
        return true
    }

    /** Write the file. The live view already matches it, so no reopen is needed (see [needsReopen]). */
    fun save(target: File): EditResult = editor.save(target).also { if (it is EditResult.Ok) changes = 0 }
}
