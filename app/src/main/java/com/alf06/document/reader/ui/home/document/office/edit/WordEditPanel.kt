package com.alf06.document.reader.ui.home.document.office.edit

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.wxiwei.office.editor.EditResult
import com.wxiwei.office.editor.docx.DocxEditor
import com.wxiwei.office.editor.docx.DocxSourceMap
import com.wxiwei.office.editor.word.WordSelection
import com.wxiwei.office.reader.OfficeDocumentView
import com.wxiwei.office.system.IMainFrame
import java.io.File

/**
 * Word: long-press a word to select it, tap another word to extend the selection, then format,
 * replace, insert or delete. Changes are queued on the original text and written on Save, after
 * which the document reopens to show them (Word text does not re-layout live yet).
 */
internal class WordEditPanel(activity: AppCompatActivity, reader: OfficeDocumentView, file: File) :
    OfficeEditPanel(activity, reader, file) {

    private var editor: DocxEditor? = null
    private var pending = 0
    private var anchor: LongRange? = null
    private val selectionLabel = label("Nhấn giữ một từ để chọn")
    private val text = input("Chữ để chèn / thay thế")

    override val view: View = column().apply {
        addView(selectionLabel)
        addView(line(text, button("Thay") { replace() }, button("Chèn") { insert() }, weights = floatArrayOf(1f, 0f, 0f)))
        addView(toolRow(
            button("B", bold = true) { op { e, r -> e.setBold(r.first, r.last + 1, true) } },
            button("I") { op { e, r -> e.setItalic(r.first, r.last + 1, true) } },
            button("U") { op { e, r -> e.setUnderline(r.first, r.last + 1, true) } },
            button("Chữ đỏ") { op { e, r -> e.setTextColor(r.first, r.last + 1, "C00000") } },
            button("Cỡ 16") { op { e, r -> e.setFontSize(r.first, r.last + 1, 16) } },
            button("Tô vàng") { op { e, r -> e.highlight(r.first, r.last + 1, "yellow") } },
            button("Xóa", color = 0xFFC00000.toInt()) { op { e, r -> e.deleteText(r.first, r.last + 1) } },
            button("Bỏ chọn") { clearSelection() },
            button("Lưu", bold = true, color = 0xFFD96D00.toInt()) { save() },
        ))
    }

    init {
        reader.onDocumentGesture = gesture@{ type, event ->
            val selection = selection() ?: return@gesture false
            when (type) {
                IMainFrame.ON_LONG_PRESS -> {
                    val offset = selection.offsetAtScreen(event.rawX, event.rawY)
                    if (offset < 0) return@gesture false
                    val word = selection.wordAt(offset)
                    anchor = word
                    select(selection, word)
                    true
                }
                IMainFrame.ON_SINGLE_TAP_CONFIRMED -> {
                    val start = anchor ?: return@gesture false
                    val offset = selection.offsetAtScreen(event.rawX, event.rawY)
                    if (offset < 0) return@gesture false
                    val word = selection.wordAt(offset)
                    select(selection, minOf(start.first, word.first)..maxOf(start.last, word.last))
                    true
                }
                else -> false
            }
        }
    }

    override fun close() {
        reader.onDocumentGesture = null
        clearSelection()
    }

    private fun selection(): WordSelection? = reader.control?.let { runCatching { WordSelection(it) }.getOrNull() }

    private fun select(selection: WordSelection, range: LongRange) {
        if (range.isEmpty()) return
        selection.setSelection(range.first, range.last + 1)
        val t = selection.selectedText().replace('\n', ' ')
        selectionLabel.text = "Đã chọn: \"" + (if (t.length > 60) t.take(60) + "…" else t) + "\"" + pendingText()
    }

    private fun clearSelection() {
        anchor = null
        selection()?.clearSelection()
        selectionLabel.text = "Nhấn giữ một từ để chọn" + pendingText()
    }

    private fun pendingText() = if (pending > 0) "  ·  $pending thay đổi chưa lưu" else ""

    private fun editor(): DocxEditor? {
        editor?.let { return it }
        val map = DocxSourceMap.get(file.absolutePath) ?: run {
            toast("Tài liệu chưa sẵn sàng để sửa")
            return null
        }
        return DocxEditor(file, map).also { editor = it }
    }

    /** Runs [action] on the selection; the change shows after Save. */
    private fun op(action: (DocxEditor, LongRange) -> Boolean) {
        val range = selection()?.selection() ?: return toast("Chọn chữ trước")
        val e = editor() ?: return
        if (!action(e, range)) return toast(e.lastError?.message ?: "Không thực hiện được")
        pending++
        selectionLabel.text = selectionLabel.text.toString().substringBefore("  ·  ") + pendingText()
        toast("Đã ghi nhận, bấm Lưu để áp dụng")
    }

    private fun replace() {
        val t = text.text.toString()
        op { e, r -> e.replaceText(r.first, r.last + 1, t) }
    }

    private fun insert() {
        val t = text.text.toString()
        if (t.isEmpty()) return toast("Nhập chữ cần chèn")
        op { e, r -> e.insertText(r.first, t) }
    }

    private fun save() {
        val e = editor ?: return toast("Chưa có thay đổi")
        if (pending == 0) return toast("Chưa có thay đổi")
        val result = saveOver(file) { target -> e.save(target) }
        report(result, "Đã lưu " + file.name)
        if (result is EditResult.Ok) {
            editor = null
            pending = 0
            anchor = null
            selectionLabel.text = "Nhấn giữ một từ để chọn"
            // show the saved text: reopen the document
            reader.open(file.absolutePath)
        }
    }
}
