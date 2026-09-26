package com.alf06.document.reader.ui.home.document.office.edit

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.wxiwei.office.editor.EditResult
import com.wxiwei.office.editor.docx.LiveDocxSession
import com.wxiwei.office.editor.word.WordSelection
import com.wxiwei.office.reader.OfficeDocumentView
import com.wxiwei.office.system.IMainFrame
import java.io.File

/**
 * Word: long-press a word to select it, tap another word to extend the selection. Formatting and
 * text changes inside a paragraph show at once; Save writes the .docx in place.
 */
internal class WordEditPanel(activity: AppCompatActivity, reader: OfficeDocumentView, file: File) :
    OfficeEditPanel(activity, reader, file) {

    private var session: LiveDocxSession? = null
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
            button("Chữ xanh") { op { e, r -> e.setTextColor(r.first, r.last + 1, "1F4E79") } },
            button("Cỡ 16") { op { e, r -> e.setFontSize(r.first, r.last + 1, 16f) } },
            button("Tô vàng") { op { e, r -> e.highlight(r.first, r.last + 1, "FFFF00") } },
            button("Xóa", color = 0xFFC00000.toInt()) { op { e, r -> e.deleteText(r.first, r.last + 1) } },
            button("↵ Xuống dòng") { op { e, r -> e.insertText(r.first, "\n") } },
            button("⇤") { op { e, r -> e.setAlignment(r.first, r.last + 1, "left") } },
            button("↔") { op { e, r -> e.setAlignment(r.first, r.last + 1, "center") } },
            button("⇥") { op { e, r -> e.setAlignment(r.first, r.last + 1, "right") } },
            button("☰") { op { e, r -> e.setAlignment(r.first, r.last + 1, "both") } },
            button("Thụt +") { op { e, r -> e.setIndentLeft(r.first, r.last + 1, e.indentLeftAt(r.first) + 720) } },
            button("Thụt −") { op { e, r -> e.setIndentLeft(r.first, r.last + 1, maxOf(0, e.indentLeftAt(r.first) - 720)) } },
            button("Dòng 1.0") { op { e, r -> e.setLineSpacing(r.first, r.last + 1, 1f) } },
            button("Dòng 1.5") { op { e, r -> e.setLineSpacing(r.first, r.last + 1, 1.5f) } },
            button("↶") { session?.let { if (!it.undo()) toast("Không còn gì để hoàn tác") } },
            button("↷") { session?.let { if (!it.redo()) toast("Không còn gì để làm lại") } },
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

    private fun pendingText() = if (session?.needsReopen == true) "  ·  một số thay đổi hiện sau khi Lưu" else ""

    private fun session(): LiveDocxSession? {
        session?.let { return it }
        val control = reader.control ?: return null
        return runCatching { LiveDocxSession(control, file) }.getOrElse {
            toast("Tài liệu chưa sẵn sàng để sửa")
            null
        }?.also { session = it }
    }

    /** Runs [action] on the selection: formatting shows at once, text changes after Save. */
    private fun op(action: (LiveDocxSession, LongRange) -> Boolean) {
        val range = selection()?.selection() ?: return toast("Chọn chữ trước")
        val s = session() ?: return
        if (!action(s, range)) return toast(s.lastError?.message ?: "Không thực hiện được")
        // the pages were laid out again: show the selection on the new layout, refresh thumbnails
        selection()?.let { select(it, range) }
        reader.thumbnails?.invalidateAll()
        if (s.needsReopen) toast("Đã ghi nhận, sẽ hiện sau khi Lưu")
    }

    private fun replace() {
        val t = text.text.toString()
        op { e, r -> e.replaceText(r.first, r.last + 1, t) }
    }

    private fun insert() {
        val t = text.text.toString()
        if (t.isEmpty()) return toast("Nhập chữ cần chèn")
        val at = selection()?.selection()?.first ?: return toast("Chọn chữ trước")
        op { e, _ -> e.insertText(at, t) }
        // show the inserted text selected (Thay / B / I apply to it after saving)
        selection()?.let { select(it, at until at + t.length) }
    }

    private fun save() {
        val s = session ?: return toast("Chưa có thay đổi")
        if (!s.hasChanges()) return toast("Chưa có thay đổi")
        val result = saveOver(file) { target -> s.save(target) }
        report(result, "Đã lưu " + file.name)
        if (result is EditResult.Ok) {
            session = null
            anchor = null
            selectionLabel.text = "Nhấn giữ một từ để chọn"
            // show the saved text: reopen the document
            reader.open(file.absolutePath)
        }
    }
}
