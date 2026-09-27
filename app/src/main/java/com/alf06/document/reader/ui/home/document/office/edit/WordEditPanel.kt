package com.alf06.document.reader.ui.home.document.office.edit

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.wxiwei.office.editor.EditResult
import com.wxiwei.office.editor.docx.LiveDocxSession
import com.wxiwei.office.editor.word.WordSelection
import com.wxiwei.office.reader.OfficeDocumentView
import com.wxiwei.office.system.IMainFrame
import java.io.File

/**
 * Word: tap the text to put the caret there and type with the keyboard (Vietnamese IMEs
 * included); long-press a word to select it and drag its handles to extend the selection.
 * Formatting and text changes show at once; Save writes the .docx in place.
 */
internal class WordEditPanel(activity: AppCompatActivity, reader: OfficeDocumentView, file: File) :
    OfficeEditPanel(activity, reader, file) {

    private var session: LiveDocxSession? = null
    private var anchor: LongRange? = null
    private val selectionLabel = label(HINT)
    private val text = input("Chữ để chèn / thay thế")

    // Typing: the keyboard edits [typing], a hidden buffer whose text mirrors the document from
    // [base] on; every change of the buffer is replayed on the document at base + its index.
    private var base = -1L
    private var muted = false
    private val caret = WordCaretOverlay(context, { reader.control?.getView() }) {
        if (base < 0) null else selection()?.caretRect(base + typing.selectionEnd.coerceAtLeast(0))
    }
    private val typing: EditText = object : EditText(context) {
        override fun onSelectionChanged(selStart: Int, selEnd: Int) {
            super.onSelectionChanged(selStart, selEnd)
            if (base >= 0) caret.touch()
        }
    }.apply {
        alpha = 0f
        isCursorVisible = false
        inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        imeOptions = EditorInfo.IME_FLAG_NO_EXTRACT_UI
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (!muted && base >= 0) typed(start, before, s.subSequence(start, start + count).toString())
            }
        })
        setOnKeyListener { _, keyCode, event ->
            // Backspace with nothing typed before the caret deletes the document text before it
            if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN && base > 0 &&
                selectionStart == 0 && selectionEnd == 0) {
                val s = session() ?: return@setOnKeyListener true
                if (s.deleteText(base - 1, base)) { base -= 1; caret.touch(); reader.thumbnails?.invalidateAll() }
                else toast(s.lastError?.message ?: "Không xóa được")
                true
            } else false
        }
        setOnFocusChangeListener { _, focused -> if (!focused) stopTyping() }
    }

    override val view: View = column().apply {
        addView(typing, LinearLayout.LayoutParams(1, 1))
        addView(selectionLabel)
        addView(line(text, button("Thay") { replace() }, button("Chèn") { insert() }, weights = floatArrayOf(1f, 0f, 0f)))
        addView(toolRow(
            button("B", bold = true) { op { e, r -> e.setBold(r.first, r.last + 1, !e.isBold(r.first)) } },
            button("I") { op { e, r -> e.setItalic(r.first, r.last + 1, !e.isItalic(r.first)) } },
            button("U") { op { e, r -> e.setUnderline(r.first, r.last + 1, !e.isUnderlined(r.first)) } },
            button("Chữ đỏ") { op { e, r -> e.setTextColor(r.first, r.last + 1, "C00000") } },
            button("Chữ xanh") { op { e, r -> e.setTextColor(r.first, r.last + 1, "1F4E79") } },
            button("Cỡ 16") { op { e, r -> e.setFontSize(r.first, r.last + 1, 16f) } },
            button("Tô vàng") { op { e, r -> e.highlight(r.first, r.last + 1, "FFFF00") } },
            button("Chép") { copy() },
            button("Cắt") { copy(); op { e, r -> e.deleteText(r.first, r.last + 1) } },
            button("Dán") { paste() },
            button("Xóa", color = 0xFFC00000.toInt()) { op { e, r -> e.deleteText(r.first, r.last + 1) } },
            button("↵ Xuống dòng") { op { e, r -> e.insertText(r.first, "\n") } },
            button("• Đầu dòng") { paraOp { e, r -> e.setBullets(r.first, r.last + 1, !e.hasBullet(r.first)) } },
            button("1. Đánh số") { paraOp { e, r -> e.setNumbering(r.first, r.last + 1, !e.hasNumbering(r.first)) } },
            button("⇤") { paraOp { e, r -> e.setAlignment(r.first, r.last + 1, "left") } },
            button("↔") { paraOp { e, r -> e.setAlignment(r.first, r.last + 1, "center") } },
            button("⇥") { paraOp { e, r -> e.setAlignment(r.first, r.last + 1, "right") } },
            button("☰") { paraOp { e, r -> e.setAlignment(r.first, r.last + 1, "both") } },
            button("Thụt +") { paraOp { e, r ->
                // in a list: one level deeper, like Tab in Word
                if (e.hasBullet(r.first)) e.setListLevel(r.first, r.last + 1, minOf(8, e.listLevelAt(r.first) + 1))
                else e.setIndentLeft(r.first, r.last + 1, e.indentLeftAt(r.first) + 720)
            } },
            button("Thụt −") { paraOp { e, r ->
                if (e.hasBullet(r.first)) e.setListLevel(r.first, r.last + 1, maxOf(0, e.listLevelAt(r.first) - 1))
                else e.setIndentLeft(r.first, r.last + 1, maxOf(0, e.indentLeftAt(r.first) - 720))
            } },
            button("Dòng 1.0") { paraOp { e, r -> e.setLineSpacing(r.first, r.last + 1, 1f) } },
            button("Dòng 1.5") { paraOp { e, r -> e.setLineSpacing(r.first, r.last + 1, 1.5f) } },
            button("↶") { stopTyping(); session?.let { if (!it.undo()) toast("Không còn gì để hoàn tác") } },
            button("↷") { stopTyping(); session?.let { if (!it.redo()) toast("Không còn gì để làm lại") } },
            button("Bỏ chọn") { clearSelection() },
            button("Lưu", bold = true, color = 0xFFD96D00.toInt()) { save() },
        ))
    }

    init {
        reader.onDocumentGesture = gesture@{ type, event ->
            val selection = selection() ?: return@gesture false
            when (type) {
                IMainFrame.ON_LONG_PRESS -> {
                    stopTyping()
                    val offset = selection.offsetAtScreen(event.rawX, event.rawY)
                    if (offset < 0) return@gesture false
                    val word = selection.wordAt(offset)
                    // no word here (an empty paragraph, a space): just put the caret
                    if (word.isEmpty()) return@gesture startTyping(offset)
                    anchor = word
                    select(selection, word)
                    true
                }
                IMainFrame.ON_SINGLE_TAP_CONFIRMED -> {
                    // a tap ends any selection and puts the caret there; the handles extend a selection
                    val offset = selection.offsetAtScreen(event.rawX, event.rawY)
                    if (offset < 0) return@gesture false
                    clickAndType(selection, offset, event.rawX, event.rawY) || startTyping(offset)
                }
                else -> false
            }
        }
    }

    private val handles = WordSelectionHandles(
        context,
        source = { reader.control?.getView() },
        range = { selection()?.selection() },
        caret = { offset -> selection()?.caretRect(offset) },
        offsetAt = { x, y -> selection()?.offsetAtScreen(x, y) ?: -1 },
    ) { start, end ->
        selection()?.let { select(it, start until end) }
        anchor = start until end
    }

    init {
        reader.addView(caret, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
        reader.addView(handles, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
        keepAboveKeyboard(true)
    }

    override fun close() {
        super.close()
        reader.onDocumentGesture = null
        stopTyping()
        reader.removeView(caret)
        reader.removeView(handles)
        clearSelection()
    }

    /**
     * Word's "click and type": a tap on the empty page below the last paragraph adds empty
     * paragraphs down to it, aligned left, centre or right by where the tap was.
     */
    private fun clickAndType(sel: WordSelection, offset: Long, rawX: Float, rawY: Float): Boolean {
        val w = reader.control?.getView() as? com.wxiwei.office.wp.control.Word ?: return false
        val end = w.getDocument().getAreaEnd(0) - 1 // before the document's last paragraph mark
        if (offset != end) return false
        val origin = IntArray(2)
        w.getLocationOnScreen(origin)
        val x = rawX - origin[0]
        val y = rawY - origin[1]
        val last = sel.caretRect(end) ?: return false
        if (y < last.bottom + last.height() / 2) return false // on or next to the last line
        val bottom = sel.bodyBottomAt(end) ?: return false
        val target = minOf(y, bottom.toFloat())
        val s = session() ?: return false
        stopTyping()
        // one paragraph first: its height, with the paragraph spacing, tells how many are needed
        if (!s.insertText(end, "\n")) return false
        var at = end + 1
        val next = sel.caretRect(at)
        if (next != null) {
            val pitch = (next.top - last.top).coerceAtLeast(1)
            val more = Math.ceil(((target - next.bottom) / pitch).toDouble()).toInt().coerceIn(0, 80)
            if (more > 0 && s.insertText(at, "\n".repeat(more))) at += more
        }
        when {
            x > w.width * 2f / 3 -> s.setAlignment(at, at + 1, "right")
            x > w.width / 3f -> s.setAlignment(at, at + 1, "center")
        }
        reader.thumbnails?.invalidateAll()
        return startTyping(at)
    }

    /** Puts the caret before [offset] and opens the keyboard. */
    private fun startTyping(offset: Long): Boolean {
        session() ?: return false
        anchor = null
        selection()?.clearSelection()
        handles.refresh()
        resetBuffer(offset)
        typing.requestFocus()
        (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(typing, 0)
        caret.active = true
        selectionLabel.text = "Đang gõ — chạm chỗ khác để dời con trỏ, nhấn giữ để chọn" + pendingText()
        return true
    }

    private fun resetBuffer(offset: Long) {
        muted = true
        typing.setText("")
        muted = false
        base = offset
    }

    private fun stopTyping() {
        if (base < 0) return
        base = -1
        caret.active = false
        (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(typing.windowToken, 0)
    }

    /** Replays a change of the typing buffer on the document. */
    private fun typed(start: Int, removed: Int, added: String) {
        val s = session() ?: return
        val at = base + start
        val ok = when {
            removed > 0 && added.isNotEmpty() -> s.replaceText(at, at + removed, added)
            removed > 0 -> s.deleteText(at, at + removed)
            added.isNotEmpty() -> s.insertText(at, added)
            else -> true
        }
        if (!ok) {
            toast(s.lastError?.message ?: "Không gõ được ở đây")
            // the document did not change: start over at the caret the document still has
            resetBuffer(at)
            return
        }
        caret.touch()
        revealCaret()
        reader.thumbnails?.invalidateAll()
    }

    private val clipboard get() = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    private fun copy() {
        val t = selection()?.let { if (it.selection() != null) it.selectedText() else null }
        if (t.isNullOrEmpty()) return toast("Chọn chữ trước")
        clipboard.setPrimaryClip(ClipData.newPlainText("text", t))
        toast("Đã chép")
    }

    /** Pastes plain text at the caret while typing, else over the selection. */
    private fun paste() {
        val t = clipboard.primaryClip?.takeIf { it.itemCount > 0 }?.getItemAt(0)?.coerceToText(context)?.toString()
        if (t.isNullOrEmpty()) return toast("Bộ nhớ tạm trống")
        if (base >= 0) {
            // through the typing buffer, so it stays in step with the document
            val at = typing.selectionEnd.coerceAtLeast(0)
            typing.text.replace(typing.selectionStart.coerceAtLeast(0), at, t)
            return
        }
        op { e, r -> e.replaceText(r.first, r.last + 1, t) }
    }

    /** Keeps the caret above the keyboard and inside the screen. */
    private fun revealCaret() {
        if (base < 0) return
        val word = reader.control?.getView() ?: return
        selection()?.revealCaret(base + typing.selectionEnd.coerceAtLeast(0), dp(24), visibleBottom(word))
    }

    override fun onKeyboardMoved() {
        reader.post { revealCaret() }
    }

    private fun selection(): WordSelection? = reader.control?.let { runCatching { WordSelection(it) }.getOrNull() }

    private fun select(selection: WordSelection, range: LongRange) {
        if (range.isEmpty()) return
        selection.setSelection(range.first, range.last + 1)
        val t = selection.selectedText().replace('\n', ' ')
        selectionLabel.text = "Đã chọn: \"" + (if (t.length > 60) t.take(60) + "…" else t) + "\"" + pendingText()
        handles.refresh()
    }

    private fun clearSelection() {
        anchor = null
        selection()?.clearSelection()
        selectionLabel.text = HINT + pendingText()
        handles.refresh()
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
    /** A paragraph change: works on the selection, or on the caret's paragraph while typing. */
    private fun paraOp(action: (LiveDocxSession, LongRange) -> Boolean) {
        if (base < 0 || selection()?.selection() != null) return op(action)
        val caretAt = base + typing.selectionEnd.coerceAtLeast(0)
        val s = session() ?: return
        if (!action(s, caretAt..caretAt)) return toast(s.lastError?.message ?: "Không thực hiện được")
        // offsets did not move: keep typing at the same place
        resetBuffer(caretAt)
        caret.touch()
        reader.thumbnails?.invalidateAll()
    }

    private fun op(action: (LiveDocxSession, LongRange) -> Boolean) {
        stopTyping()
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

    override fun hasChanges() = session?.hasChanges() == true

    override fun writeTo(target: File) = session!!.save(target)

    override fun onSaved() {
        session = null
        anchor = null
        stopTyping()
        selectionLabel.text = HINT
        // show the saved text: reopen the document
        reader.open(file.absolutePath)
    }

    private companion object {
        const val HINT = "Chạm vào chữ để gõ, nhấn giữ một từ để chọn"
    }
}
