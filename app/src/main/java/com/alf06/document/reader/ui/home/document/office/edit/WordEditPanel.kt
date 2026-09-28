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
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
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
    private val caret = WordCaretOverlay(context, { docView() }) {
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
                if (s.deleteText(base - 1, base)) { base -= 1; caret.touch(); pagesChangedFrom(base) }
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
            button("B", bold = true) { toggle("Đậm", { e, a -> e.isBold(a) }) { e, a, b, on -> e.setBold(a, b, on) } },
            button("I") { toggle("Nghiêng", { e, a -> e.isItalic(a) }) { e, a, b, on -> e.setItalic(a, b, on) } },
            button("U") { toggle("Gạch chân", { e, a -> e.isUnderlined(a) }) { e, a, b, on -> e.setUnderline(a, b, on) } },
            button("S̶") { toggle("Gạch giữa", { e, a -> e.isStruck(a) }) { e, a, b, on -> e.setStrike(a, b, on) } },
            button("x²") { toggle("Chỉ số trên", { e, a -> e.isSuperscript(a) }) { e, a, b, on -> e.setScript(a, b, if (on) 1 else 0) } },
            button("x₂") { toggle("Chỉ số dưới", { e, a -> e.isSubscript(a) }) { e, a, b, on -> e.setScript(a, b, if (on) 2 else 0) } },
            button("Màu chữ") { needSelection { pickColor("Màu chữ") { c -> c?.let { op { e, r -> e.setTextColor(r.first, r.last + 1, it) } } } } },
            button("Font") { needSelection { pickFont { name -> op { e, r -> e.setFont(r.first, r.last + 1, name) } } } },
            button("Cỡ chữ") { needSelection { pickSize { pt -> op { e, r -> e.setFontSize(r.first, r.last + 1, pt) } } } },
            button("Tô màu") { needSelection { pickColor("Tô màu", none = "Bỏ tô") { c -> op { e, r -> e.highlight(r.first, r.last + 1, c ?: "none") } } } },
            button("Chọn từ") { selectAround { sel, at -> sel.wordAt(at) } },
            button("Chọn đoạn") { selectAround { _, at -> paragraphAt(at) } },
            button("Chọn hết") { selectAround { _, _ -> wholeDocument() } },
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
            button("Đoạn văn…") { askParagraph() },
            button("Thụt +") { paraOp { e, r ->
                // in a list: one level deeper, like Tab in Word
                if (e.hasBullet(r.first)) e.setListLevel(r.first, r.last + 1, minOf(8, e.listLevelAt(r.first) + 1))
                else e.setIndentLeft(r.first, r.last + 1, e.indentLeftAt(r.first) + 720)
            } },
            button("Thụt −") { paraOp { e, r ->
                if (e.hasBullet(r.first)) e.setListLevel(r.first, r.last + 1, maxOf(0, e.listLevelAt(r.first) - 1))
                else e.setIndentLeft(r.first, r.last + 1, maxOf(0, e.indentLeftAt(r.first) - 720))
            } },
            button("Giãn dòng") { askLineSpacing() },
            button("↶") { stopTyping(); session?.let { if (!it.undo()) toast("Không còn gì để hoàn tác") } },
            button("↷") { stopTyping(); session?.let { if (!it.redo()) toast("Không còn gì để làm lại") } },
            button("Bỏ chọn") { clearSelection() },
            button("Tìm & thay") { findReplace() },
            button("+ Ảnh") { pickImage() },
            button("+ Bảng") { askTable() },
            button("+ Hàng trên") { insertRowOrColumn(row = true, after = false) },
            button("+ Hàng dưới") { insertRowOrColumn(row = true, after = true) },
            button("+ Cột trái") { insertRowOrColumn(row = false, after = false) },
            button("+ Cột phải") { insertRowOrColumn(row = false, after = true) },
            button("− Hàng") { deleteRowOrColumn(row = true) },
            button("− Cột") { deleteRowOrColumn(row = false) },
            button("Lưu", bold = true, color = 0xFFD96D00.toInt()) { save() },
            button("Lưu bản sao") { stopTyping(); saveCopy() },
        ))
    }

    // the file the view and the session work on: the original, or a working copy in the cache after
    // an edit that needed a reopen (a picture, a table); Save writes it over the original
    private var working = file
    private var workingChanged = false

    /** Where the caret or the selection starts, or -1. */
    private fun here(): Long = selection()?.selection()?.first ?: if (base >= 0) base + typing.selectionStart.coerceAtLeast(0) else -1L

    /** [action] on the selected text; with only the caret in a word, on that word (like Word). */
    private fun needSelection(action: () -> Unit) {
        val sel = selection() ?: return
        if (sel.selection() == null && base >= 0) {
            val caretAt = base + typing.selectionEnd.coerceAtLeast(0)
            val word = sel.wordAt(caretAt)
            if (!word.isEmpty() && caretAt >= word.first && caretAt <= word.last + 1) {
                stopTyping()
                anchor = word
                select(sel, word)
            }
        }
        if (sel.selection() == null) return toast("Chọn chữ trước (nhấn giữ một từ, hoặc Chọn từ/đoạn)")
        action()
    }

    /** Selects the range [pick] gives around the caret (or the current selection). */
    private fun selectAround(pick: (WordSelection, Long) -> LongRange) {
        val sel = selection() ?: return
        val at = here()
        if (at < 0) return toast("Chạm vào chữ trước")
        stopTyping()
        val range = pick(sel, at)
        if (range.isEmpty()) return toast("Không có chữ ở đây")
        anchor = range
        select(sel, range)
    }

    private fun paragraphAt(offset: Long): LongRange {
        val w = docView() as? com.wxiwei.office.wp.control.Word ?: return LongRange.EMPTY
        val p = w.getDocument().getParagraph(offset) ?: return LongRange.EMPTY
        // without its paragraph mark
        return p.getStartOffset() until maxOf(p.getStartOffset(), p.getEndOffset() - 1)
    }

    private fun wholeDocument(): LongRange {
        val w = docView() as? com.wxiwei.office.wp.control.Word ?: return LongRange.EMPTY
        return 0L until maxOf(0L, w.getDocument().getAreaEnd(0) - 1)
    }

    private fun pickImage() {
        val at = here()
        if (at < 0) return toast("Chạm vào chỗ muốn chèn ảnh trước")
        var launcher: androidx.activity.result.ActivityResultLauncher<String>? = null
        launcher = activity.activityResultRegistry.register("word-image-" + System.nanoTime(),
            androidx.activity.result.contract.ActivityResultContracts.GetContent()) { uri ->
            launcher?.unregister()
            if (uri != null) insertImage(at, uri)
        }
        launcher.launch("image/*")
    }

    private fun insertImage(at: Long, uri: android.net.Uri) {
        val type = context.contentResolver.getType(uri) ?: "image/jpeg"
        val ext = when { type.contains("png") -> "png"; type.contains("gif") -> "gif"; type.contains("bmp") -> "bmp"; else -> "jpeg" }
        val image = File(context.cacheDir, "word-image-" + System.nanoTime() + "." + ext)
        try {
            context.contentResolver.openInputStream(uri)!!.use { input -> image.outputStream().use { input.copyTo(it) } }
        } catch (e: Exception) {
            return toast("Không đọc được ảnh")
        }
        val bounds = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
        android.graphics.BitmapFactory.decodeFile(image.path, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return toast("Không đọc được ảnh")
        // at most 6 inches wide (576 px at 96 dpi), keeping its proportions
        val w = minOf(576, bounds.outWidth)
        val h = maxOf(1, w * bounds.outHeight / bounds.outWidth)
        val s = session() ?: return
        stopTyping()
        if (!s.insertImage(at, image, w, h)) return toast(s.lastError?.message ?: "Không chèn được ảnh")
        reloadWorking()
    }

    /**
     * A new empty row above/below ([after]) or column left/right of the cell with the caret (or
     * the first cell of the selected table). The working copy is reopened (a new cell has no place
     * in the file yet), the view stays where it was and the caret goes into the new cell.
     */
    private fun insertRowOrColumn(row: Boolean, after: Boolean) {
        val s = session() ?: return
        val at = here().takeIf { it >= 0 } ?: tableAt?.first ?: -1L
        val place = if (at >= 0) s.cellAt(at) else null
        if (place == null) return toast("Chạm vào một ô của bảng trước")
        stopTyping()
        clearPicture()
        val shown = !s.needsReopen
        val ok = if (row) s.insertTableRow(at, after) else s.insertTableColumn(at, after)
        // next to a cell added just now: save the working copy first (it has no place in the file yet), then again
        // the same text is read back: the caret goes back to [at], then again
        if (!ok && s.needsFlush) return reloadWorking { startTyping(at); insertRowOrColumn(row, after) }
        if (!ok) return toast(s.lastError?.message ?: if (row) "Không thêm được hàng" else "Không thêm được cột")
        // the new cell: same column in the new row, or the new column in the same row
        val (r, c) = if (row) (if (after) place.row + 1 else place.row) to place.cell
            else place.row to (if (after) place.cell + place.span else place.cell)
        val typeThere = { session()?.cellStart(place.table, r, c)?.takeIf { it >= 0 }?.let { startTyping(it) } }
        pagesChangedFrom(at)
        // shown at once (a table without merged cells); otherwise the file is read again
        if (shown && s.needsReopen) reloadWorking { typeThere() } else typeThere()
    }

    /**
     * Removes the row or the column of the cell with the caret (or the first cell of the selected
     * table); reopened like [insertRowOrColumn], with the caret in the cell that takes its place.
     */
    private fun deleteRowOrColumn(row: Boolean) {
        val s = session() ?: return
        val at = here().takeIf { it >= 0 } ?: tableAt?.first ?: -1L
        val place = if (at >= 0) s.cellAt(at) else null
        if (place == null) return toast("Chạm vào một ô của bảng trước")
        stopTyping()
        clearPicture()
        val shown = !s.needsReopen
        val ok = if (row) s.deleteTableRow(at) else s.deleteTableColumn(at)
        if (!ok && s.needsFlush) return reloadWorking { startTyping(at); deleteRowOrColumn(row) }
        if (!ok) return toast(s.lastError?.message ?: if (row) "Không xóa được hàng" else "Không xóa được cột")
        pagesChangedFrom(at)
        val caretBack: () -> Unit = {
            val t = session()
            // the row now in its place (or the one above, for the last row); the column before it
            val tries = if (row) listOf(place.row to place.cell, place.row - 1 to place.cell, place.row to 0, place.row - 1 to 0)
                else listOf(place.row to place.cell - 1, place.row to place.cell, place.row to 0)
            tries.firstNotNullOfOrNull { (r, c) -> if (r < 0 || c < 0 || t == null) null else t.cellStart(place.table, r, c).takeIf { it >= 0 } }?.let { startTyping(it) }
        }
        if (shown && s.needsReopen) reloadWorking { caretBack() } else caretBack()
    }

    /**
     * Line spacing of the paragraphs at the caret (or selected): 1.0 / 1.15 / 1.5 / 2.0, a multiple,
     * exactly or at least some points; and the space before and after them. Filled with what they have.
     */
    /**
     * Word's Paragraph dialog: alignment, left/right indent and first-line / hanging indent (cm),
     * space before/after (pt), starting from the paragraph at the caret; one undoable step.
     */
    private fun askParagraph() {
        val s = session() ?: return
        val at = here()
        if (at < 0) return toast("Chạm vào đoạn văn trước")
        val now = s.paragraphLayoutAt(at)
        val twipsPerCm = 1440f / 2.54f
        fun cm(tw: Int) = "%.2f".format(java.util.Locale.ROOT, tw / twipsPerCm).trimEnd('0').trimEnd('.')
        fun num(v: Float) = if (v % 1f == 0f) v.toInt().toString() else "%.1f".format(java.util.Locale.ROOT, v)
        val aligns = listOf("Trái" to "left", "Giữa" to "center", "Phải" to "right", "Đều hai bên" to "both")
        dialogs.show("Đoạn văn") {
            caption("Căn lề")
            val alignGroup = choices(aligns.map { it.first }, aligns.indexOfFirst { it.second == now.align })
            caption("Thụt lề (cm)")
            val left = input("Thụt trái (cm)", cm(now.leftTwips), numeric = true)
            val right = input("Thụt phải (cm)", cm(now.rightTwips), numeric = true)
            caption("Thụt đặc biệt")
            val specialGroup = choices(listOf("Không", "Dòng đầu", "Treo"), when { now.specialTwips > 0 -> 1; now.specialTwips < 0 -> 2; else -> 0 }, horizontal = true)
            val special = input("Thụt đặc biệt (cm)", cm(Math.abs(now.specialTwips)), numeric = true)
            special.isEnabled = now.specialTwips != 0
            specialGroup.onChange { special.isEnabled = it != 0 }
            caption("Khoảng cách (pt)")
            val before = input("Trước đoạn (pt)", num(now.beforePt), numeric = true)
            val after = input("Sau đoạn (pt)", num(now.afterPt), numeric = true)
            positive("Áp dụng") {
                fun f(e: android.widget.EditText) = e.text.toString().replace(',', '.').ifBlank { "0" }.toFloatOrNull()
                val l = f(left); val r = f(right); val sp = f(special); val b = f(before); val a = f(after)
                if (l == null || r == null || sp == null || b == null || a == null || sp < 0f || b < 0f || a < 0f) return@positive toast("Giá trị không hợp lệ")
                val layout = com.wxiwei.office.editor.docx.LiveDocxSession.ParagraphLayout(
                    align = aligns.getOrNull(alignGroup.picked)?.second ?: now.align,
                    leftTwips = Math.round(l * twipsPerCm), rightTwips = Math.round(r * twipsPerCm),
                    specialTwips = when (specialGroup.picked) { 1 -> Math.round(sp * twipsPerCm); 2 -> -Math.round(sp * twipsPerCm); else -> 0 },
                    beforePt = b, afterPt = a,
                )
                if (layout != now) paraOp { e, rg -> e.setParagraphLayout(rg.first, rg.last + 1, layout) }
            }
            negative()
        }
    }

    private fun askLineSpacing() {
        val s = session() ?: return
        val at = here()
        if (at < 0) return toast("Chạm vào đoạn văn trước")
        val (kind, value) = s.lineSpacingAt(at)
        val (before, after) = s.paragraphSpacingAt(at)
        val labels = listOf("1.0", "1.15", "1.5", "2.0", "Bội số", "Chính xác (pt)", "Tối thiểu (pt)")
        fun num(v: Float) = if (v % 1f == 0f) v.toInt().toString() else "%.2f".format(java.util.Locale.ROOT, v).trimEnd('0').trimEnd('.')
        // what the paragraph has now
        val presets = listOf(1f, 1.15f, 1.5f, 2f)
        val checked = when (kind) {
            com.wxiwei.office.constant.wp.WPAttrConstant.LINE_SPACE_EXACTLY.toInt() -> 5
            com.wxiwei.office.constant.wp.WPAttrConstant.LINE_SAPCE_LEAST.toInt() -> 6
            else -> presets.indexOfFirst { Math.abs(it - value) < 0.01f }.let { if (it >= 0) it else 4 }
        }
        dialogs.show("Giãn dòng") {
            val group = choices(labels, checked)
            val amount = input("Giá trị", num(value), numeric = true)
            amount.isEnabled = checked >= 4
            group.onChange { amount.isEnabled = it >= 4 }
            caption("Khoảng cách đoạn")
            val spaceBefore = input("Trước đoạn (pt)", num(before), numeric = true)
            val spaceAfter = input("Sau đoạn (pt)", num(after), numeric = true)
            positive("OK") {
                val pick = group.picked
                val v = amount.text.toString().replace(',', '.').toFloatOrNull()
                val b = spaceBefore.text.toString().replace(',', '.').toFloatOrNull() ?: before
                val a = spaceAfter.text.toString().replace(',', '.').toFloatOrNull() ?: after
                if (pick >= 4 && (v == null || v <= 0f)) return@positive toast("Giá trị không hợp lệ")
                paraOp { e, r ->
                    val lineOk = when (pick) {
                        in 0..3 -> e.setLineSpacing(r.first, r.last + 1, presets[pick])
                        4 -> e.setLineSpacing(r.first, r.last + 1, v!!)
                        else -> e.setLineSpacingPoints(r.first, r.last + 1, v!!, exactly = pick == 5)
                    }
                    lineOk && (b == before && a == after || e.setParagraphSpacing(r.first, r.last + 1, b, a))
                }
            }
            negative()
        }
    }

    /** The fonts the app offers ([EditFonts]), each shown in itself; the one of the selection checked. */
    private fun pickFont(onPick: (String) -> Unit) {
        val names = EditFonts.names
        val current = selection()?.selection()?.let { session()?.fontAt(it.first) }
        dialogs.show("Font") {
            items(names.map { if (it.equals(current, ignoreCase = true)) "✓ $it" else it }) { i -> onPick(names[i]) }
                .forEachIndexed { i, row -> row.typeface = EditFonts.typeface(names[i]); row.textSize = 18f }
            negative()
        }
    }

    /**
     * Find and replace in the body: "Tìm tiếp" selects the next match (round), "Thay" replaces the
     * selected one and goes on, "Thay tất cả" replaces all (one undo step). The dialog sits at the
     * top and stays open; the match is scrolled into view under it.
     */
    private fun findReplace() {
        stopTyping()
        var atTop = true
        val dialog = dialogs.show("Tìm & thay", scroll = false) {
            val find = input("Tìm")
            val with = input("Thay bằng")
            val matchCase = check("Phân biệt hoa thường")
            val status = text("")
            fun matches() = session()?.find(find.text.toString(), matchCase.isChecked).orEmpty()
            fun show(r: LongRange, all: List<LongRange>) {
                val sel = selection() ?: return
                val word = docView() ?: return
                select(sel, r)
                val tall = (dialog?.window?.decorView?.height ?: 0) + dp(24)
                val top = IntArray(2).also { word.getLocationOnScreen(it) }[1]
                // the dialog covers the top of the view (it sits at the top) or its bottom
                fun coveredTop() = if (atTop) maxOf(0, tall - top) else dp(24)
                fun visibleBottom() = if (atTop) word.height else word.height - tall
                sel.revealCaret(r.first, coveredTop(), visibleBottom())
                // near the start of the document the page cannot scroll under the dialog: move it down
                val caret = sel.caretRect(r.first)
                if (atTop && caret != null && caret.top < coveredTop()) {
                    atTop = false
                    dialog?.window?.setGravity(android.view.Gravity.BOTTOM)
                    sel.revealCaret(r.first, coveredTop(), visibleBottom())
                }
                handles.refresh()
                status.text = "Kết quả ${all.indexOf(r) + 1}/${all.size}"
            }
            fun next(from: Long) {
                val all = matches()
                if (all.isEmpty()) { status.text = "Không tìm thấy"; return }
                show(all.firstOrNull { it.first >= from } ?: all.first(), all)
            }
            keepOpenOnButtons()
            neutral("Tìm tiếp") { next(selection()?.selection()?.let { it.first + 1 } ?: 0L) }
            positive("Thay") {
                val s = session() ?: return@positive
                val current = selection()?.selection()
                if (current == null || matches().none { it == current }) return@positive next(0L)
                val text = with.text.toString()
                if (!s.replaceText(current.first, current.last + 1, text)) { status.text = s.lastError?.message ?: "Không thay được chỗ này"; return@positive }
                pagesChangedFrom(current.first)
                next(current.first + text.length)
            }
            negative("Thay tất cả") {
                val s = session() ?: return@negative
                val (done, skipped) = s.replaceAll(find.text.toString(), with.text.toString(), matchCase.isChecked)
                clearSelection()
                pagesChangedFrom(0)
                status.text = "Đã thay $done chỗ" + if (skipped > 0) " ($skipped chỗ không thay được)" else ""
            }
        }
        dialog.window?.setGravity(android.view.Gravity.TOP)
        dialog.window?.setDimAmount(0f)
    }

    private fun askTable() {
        val at = here()
        if (at < 0) return toast("Chạm vào đoạn muốn chèn bảng phía sau trước")
        dialogs.show("Chèn bảng") {
            caption("Số hàng")
            val rows = input("Số hàng", "3").apply { inputType = InputType.TYPE_CLASS_NUMBER }
            caption("Số cột")
            val cols = input("Số cột", "3").apply { inputType = InputType.TYPE_CLASS_NUMBER }
            positive("Chèn") {
                val r = rows.text.toString().toIntOrNull() ?: 0
                val c = cols.text.toString().toIntOrNull() ?: 0
                val s = session() ?: return@positive
                stopTyping()
                if (!s.insertTable(at, r, c)) toast(s.lastError?.message ?: "Không chèn được bảng") else reloadWorking()
            }
            negative()
        }
    }

    /** Writes every edit to a working copy in the cache and shows it; the original waits for Save. Then [then], once the view is back where it was. */
    private fun reloadWorking(then: (() -> Unit)? = null) {
        val s = session ?: return
        val next = workingCopy()
        val result = s.save(next)
        if (result !is com.wxiwei.office.editor.EditResult.Ok) return report(result, "")
        val previous = working
        working = next
        workingChanged = true
        session = null
        anchor = null
        selectionLabel.text = HINT
        // the reopened document starts at page 1: bring back the place the edit was made
        val w = docView() as? com.wxiwei.office.wp.control.Word
        val place = w?.let { Triple(it.scrollX, it.scrollY, it.getZoom()) }
        reopen(next) {
            if (previous != file) previous.delete()
            if (place != null) restoreScroll(place.first, place.second, place.third, then) else then?.invoke()
        }
    }

    /** Scrolls the reopened document to ([x], [y]) at [zoom], once its pages are laid out that far. */
    private fun restoreScroll(x: Int, y: Int, zoom: Float, then: (() -> Unit)? = null) {
        activity.lifecycleScope.launch {
            val end = System.currentTimeMillis() + 5_000
            while (System.currentTimeMillis() < end) {
                val w = docView() as? com.wxiwei.office.wp.control.Word ?: return@launch
                val scale = w.getZoom() / zoom
                val tx = Math.round(x * scale); val ty = Math.round(y * scale)
                if (w.getWordHeight() * w.getZoom() - w.height >= ty) {
                    w.scrollTo(tx, ty)
                    w.postInvalidate()
                    then?.invoke()
                    return@launch
                }
                kotlinx.coroutines.delay(50)
            }
            then?.invoke()
        }
    }

    init {
        reader.onDocumentGesture = gesture@{ type, event ->
            val selection = selection() ?: return@gesture false
            when (type) {
                IMainFrame.ON_LONG_PRESS -> {
                    stopTyping()
                    // a picture: selected, and the finger still down moves it
                    if (pictureTap(selection, event.rawX, event.rawY)) {
                        if (picture.grab(event.rawX, event.rawY)) reader.touchCapture = { picture.follow(it) }
                        return@gesture true
                    }
                    clearPicture()
                    val offset = selection.offsetAtScreen(event.rawX, event.rawY)
                    if (offset < 0) return@gesture false
                    // in a table: the word is selected like anywhere (to type over, format...) and the
                    // table's frame shows; the finger moving on picks the whole table up instead
                    session()?.tableAt(offset)?.let { table ->
                        val word = selection.wordAt(offset)
                        if (word.isEmpty()) startTyping(offset) else { anchor = word; select(selection, word) }
                        showTableFrame(table)
                        dragTableFrom(table, event.rawX, event.rawY)
                        return@gesture true
                    }
                    val word = selection.wordAt(offset)
                    // no word here (an empty paragraph, a space): just put the caret
                    if (word.isEmpty()) return@gesture startTyping(offset)
                    anchor = word
                    select(selection, word)
                    true
                }
                IMainFrame.ON_SINGLE_TAP_CONFIRMED -> {
                    tapAt(event.rawX, event.rawY)
                }
                else -> false
            }
        }
    }

    private val handles = WordSelectionHandles(
        context,
        source = { docView() },
        range = { selection()?.selection() },
        caret = { offset -> selection()?.caretRect(offset) },
        offsetAt = { x, y -> selection()?.offsetAtScreen(x, y) ?: -1 },
        onChange = { start, end ->
            selection()?.let { select(it, start until end) }
            anchor = start until end
        },
        onTap = { x, y -> tapAt(x, y) },
    )

    // the selected picture: its one-char object in the text, and whether it floats on the page
    private var pictureAt = -1L
    private var pictureFloats = false
    // the table being dragged: its offsets
    private var tableAt: LongRange? = null
    private val picture = WordPictureOverlay(context, { docView() },
        frame = {
            val sel = selection()
            val table = currentTable()
            when {
                sel == null -> null
                table != null -> sel.tableRect(table.first, table.last + 1)
                pictureAt < 0 -> null
                pictureFloats -> sel.floatingShapeRect(pictureAt)
                else -> sel.inlineObjectRect(pictureAt)
            }
        },
        onMove = { rawX, rawY, dx, dy -> if (tableAt != null) moveTable(rawX, rawY, dy) else movePicture(rawX, rawY, dx, dy) },
        // an in-line picture goes to a text position: a caret shows it under the finger
        dropAt = { rawX, rawY, dy ->
            val sel = selection()
            val table = tableAt
            when {
                sel == null -> null
                table != null -> tableDrop(sel, table, rawX, rawY, dy)
                pictureFloats -> null
                else -> sel.offsetAtScreen(rawX, rawY).takeIf { it >= 0 }?.let { sel.caretRect(it) }
            }
        },
        guides = { currentTable()?.let { t -> selection()?.tableGuides(t.first, t.last + 1) } },
        onColumn = { index, dx -> resizeColumn(index, dx) },
        onRow = { start, height -> resizeRow(start, height) },
        onResize = { w, h -> resizePicture(w, h) },
    )

    init {
        EditFonts.register()
        addOverlay(caret)
        addOverlay(handles)
        addOverlay(picture)
        keepAboveKeyboard(true)
    }

    override fun close() {
        super.close()
        reader.onDocumentGesture = null
        reader.touchCapture = null
        stopTyping()
        removeOverlay(caret)
        removeOverlay(handles)
        removeOverlay(picture)
        clearSelection()
    }

    private fun zoom(): Float = (docView() as? com.wxiwei.office.wp.control.Word)?.getZoom() ?: 1f

    private fun isPicture(shape: com.wxiwei.office.common.shape.IShape?) =
        shape != null && (shape is com.wxiwei.office.common.shape.PictureShape || shape is com.wxiwei.office.common.shape.WPPictureShape ||
            shape.type.toInt() == com.wxiwei.office.common.shape.AbstractShape.SHAPE_PICTURE.toInt())

    /** Selects the picture under a tap (floating, or in a line of text); false when there is none. */
    private fun pictureTap(sel: WordSelection, rawX: Float, rawY: Float): Boolean {
        val word = docView() ?: return false
        val at = IntArray(2)
        word.getLocationOnScreen(at)
        sel.floatingShapeAt(rawX - at[0], rawY - at[1])?.takeIf { isPicture(it.shape) }?.let { return selectPicture(it.offset, true) }
        val s = session() ?: return false
        val offset = sel.offsetAtScreen(rawX, rawY)
        if (offset < 0) return false
        for (o in listOf(offset, offset - 1)) {
            if (o >= 0 && isPicture(s.shapeAt(o))) {
                // in a line: only when the tap is on the picture itself
                val r = sel.inlineObjectRect(o) ?: continue
                if (r.contains((rawX - at[0]).toInt(), (rawY - at[1]).toInt())) return selectPicture(o, false)
            }
        }
        return false
    }

    private fun selectPicture(offset: Long, floats: Boolean): Boolean {
        stopTyping()
        clearSelection()
        pictureAt = offset
        pictureFloats = floats
        picture.active = true
        selectionLabel.text = "Đã chọn ảnh: kéo để di chuyển, kéo góc để đổi cỡ"
        return true
    }

    private fun clearPicture() {
        if (pictureAt < 0 && tableAt == null) return
        pictureAt = -1
        tableAt = null
        picture.active = false
        picture.resizable = true
        selectionLabel.text = HINT
    }

    /**
     * Where a dragged [table] would go for the finger at ([rawX], [rawY]): a line across the table's
     * width at the top of the paragraph under it, or at its bottom when dragged down ([dy] > 0).
     */
    private fun tableDrop(sel: WordSelection, table: LongRange, rawX: Float, rawY: Float, dy: Float): android.graphics.Rect? {
        val to = sel.offsetAtScreen(rawX, rawY)
        if (to < 0 || to in table) return null
        val doc = (docView() as? com.wxiwei.office.wp.control.Word)?.getDocument() as? com.wxiwei.office.wp.model.WPDocument ?: return null
        // the body paragraph or table the drop goes next to
        val block = doc.getParagraph0(to) ?: return null
        val lines = if (block is com.wxiwei.office.wp.model.TableElement) listOfNotNull(sel.tableRect(block.getStartOffset(), block.getEndOffset()))
            else sel.rectsFor(block.getStartOffset(), block.getEndOffset())
        if (lines.isEmpty()) return null
        val y = if (dy > 0) lines.maxOf { it.bottom } else lines.minOf { it.top }
        val span = sel.tableRect(table.first, table.last + 1) ?: return null
        val half = Math.round(1.5f * context.resources.displayMetrics.density)
        return android.graphics.Rect(span.left, y - half, span.right, y + half)
    }

    /**
     * Thumbnails of the pages from the one holding [offset] on are drawn again (an edit there can
     * push the rest down); the pages before it did not change and keep theirs.
     */
    private fun pagesChangedFrom(offset: Long) {
        val thumbs = reader.thumbnails ?: return
        val root = (docView() as? com.wxiwei.office.wp.control.Word)?.getRoot(com.wxiwei.office.constant.wp.WPViewConstant.PAGE_ROOT.toInt()) as? com.wxiwei.office.wp.view.PageRoot
        val count = maxOf(reader.state.value.pageCount, root?.getPageCount() ?: 0)
        val first = root?.let { r -> (0 until r.getPageCount()).lastOrNull { (r.getPageView(it)?.getStartOffset(null) ?: Long.MAX_VALUE) <= offset } } ?: 0
        for (page in first + 1..maxOf(first + 1, count)) thumbs.invalidate(page)
    }

    /** Pixels shown at the current zoom -> twips of the document. */
    private fun twips(px: Float): Int = Math.round(px / zoom() * com.wxiwei.office.constant.MainConstant.PIXEL_TO_TWIPS)

    private fun resizeColumn(index: Int, dx: Float) {
        val s = session() ?: return
        val table = currentTable() ?: return
        if (s.resizeTableColumn(table.first, index, twips(dx)) == null) toast(s.lastError?.message ?: "Không đổi được độ rộng cột")
        pagesChangedFrom(table.first)
        picture.invalidate()
    }

    private fun resizeRow(start: Long, height: Float) {
        val s = session() ?: return
        if (!s.setTableRowHeight(start, twips(height))) toast(s.lastError?.message ?: "Không đổi được chiều cao hàng")
        pagesChangedFrom(start)
        picture.invalidate()
    }

    /** The table's frame with its handles, over whatever text is selected or typed in it. */
    private fun showTableFrame(table: LongRange) {
        pictureAt = -1
        tableAt = table
        picture.resizable = false
        picture.active = true
    }

    /** The table picked up to move: the text selection goes. */
    private fun selectTable(table: LongRange) {
        stopTyping()
        clearSelection()
        showTableFrame(table)
        selectionLabel.text = "Kéo bảng đến đoạn muốn đặt (vạch đỏ)"
    }

    /** The table shown now, [tableAt] as the text typed in it grew it (its start stays). */
    private fun currentTable(): LongRange? = tableAt?.let { t -> session()?.tableAt(t.first) ?: t }

    /**
     * After a long press in [table]: when the finger then moves, the table is picked up and follows
     * it; lifted in place, the word stays selected (and the frame shows).
     */
    private fun dragTableFrom(table: LongRange, rawX: Float, rawY: Float) {
        val slop = android.view.ViewConfiguration.get(context).scaledTouchSlop
        var picked = false
        reader.touchCapture = { e ->
            if (!picked && e.actionMasked == android.view.MotionEvent.ACTION_MOVE &&
                Math.hypot((e.rawX - rawX).toDouble(), (e.rawY - rawY).toDouble()) > slop) {
                picked = true
                selectTable(table)
                if (!picture.grab(rawX, rawY)) clearPicture()
            }
            if (picked) picture.follow(e)
        }
    }

    /** Puts the dragged table before the paragraph where the finger was lifted, or after it when dragged down. */
    private fun moveTable(rawX: Float, rawY: Float, dy: Float) {
        val table = currentTable() ?: return
        val s = session() ?: return
        val to = selection()?.offsetAtScreen(rawX, rawY) ?: return
        // dropped on itself: it stays selected
        if (to < 0 || to in table) return picture.invalidate()
        val shown = !s.needsReopen
        if (!s.moveTable(table.first, to, after = dy > 0)) { clearPicture(); return toast(s.lastError?.message ?: "Không di chuyển được bảng") }
        pagesChangedFrom(minOf(table.first, to))
        if (shown && s.needsReopen) { clearPicture(); return reloadWorking() }
        // shown at once: keep it selected where it is now
        tableAt = s.movedTable ?: return clearPicture()
        picture.active = true
    }

    private fun movePicture(rawX: Float, rawY: Float, dx: Float, dy: Float) {
        val s = session() ?: return
        val sel = selection() ?: return
        val z = zoom()
        val shown = !s.needsReopen
        var at = pictureAt
        val ok = if (pictureFloats) s.shiftObject(pictureAt, Math.round(dx / z), Math.round(dy / z))
        else {
            // to the text position where the finger was lifted (the caret shown while dragging)
            val to = sel.offsetAtScreen(rawX, rawY)
            if (to == pictureAt || to == pictureAt + 1) return picture.invalidate()
            (to >= 0 && s.moveObject(pictureAt, to)).also { if (it) at = if (to > pictureAt) to - 1 else to }
        }
        if (!ok) { clearPicture(); return toast(s.lastError?.message ?: "Không di chuyển được ảnh") }
        pictureEdited(s, shown, at)
    }

    /**
     * After a picture edit: the view shows it already (the picture stays selected at [at]), or,
     * when the session could not show it, the working copy is reopened.
     */
    private fun pictureEdited(s: LiveDocxSession, shown: Boolean, at: Long) {
        pagesChangedFrom(minOf(at, pictureAt.takeIf { it >= 0 } ?: at))
        if (shown && s.needsReopen) { clearPicture(); return reloadWorking() }
        pictureAt = at
        picture.active = true
    }

    private fun resizePicture(width: Float, height: Float) {
        val s = session() ?: return
        val z = zoom()
        val shown = !s.needsReopen
        val ok = s.resizeObject(pictureAt, maxOf(1, Math.round(width / z)), maxOf(1, Math.round(height / z)))
        if (!ok) { clearPicture(); return toast(s.lastError?.message ?: "Không đổi cỡ được ảnh") }
        pictureEdited(s, shown, pictureAt)
    }

    /** A tap ends any selection and puts the caret there; the handles extend a selection. */
    private fun tapAt(rawX: Float, rawY: Float): Boolean {
        val sel = selection() ?: return false
        if (pictureTap(sel, rawX, rawY)) return true
        val offset = sel.offsetAtScreen(rawX, rawY)
        // a tap in the table keeps its frame (typing in a cell); anywhere else takes it off
        if (currentTable()?.let { offset in it } != true) clearPicture()
        if (offset < 0) return false
        return clickAndType(sel, offset, rawX, rawY) || startTyping(offset)
    }

    /**
     * Word's "click and type": a tap on the empty page below the last paragraph adds empty
     * paragraphs down to it, aligned left, centre or right by where the tap was.
     */
    private fun clickAndType(sel: WordSelection, offset: Long, rawX: Float, rawY: Float): Boolean {
        val w = docView() as? com.wxiwei.office.wp.control.Word ?: return false
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
        pagesChangedFrom(at)
        return startTyping(at)
    }

    /** Puts the caret before [offset] and opens the keyboard. */
    private fun startTyping(offset: Long): Boolean {
        // the caret moved: B / I / U pressed before apply at the old place only
        pending.clear()
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
        pending.clear()
        if (base < 0) return
        base = -1
        caret.active = false
        (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(typing.windowToken, 0)
    }

    /** Replays a change of the typing buffer on the document. */
    /** B / I / U turned on or off with only the caret: for the text typed next (like Word), by name. */
    private val pending = LinkedHashMap<String, Pair<Boolean, (LiveDocxSession, Long, Long, Boolean) -> Boolean>>()

    /**
     * B / I / U: on the selected text; with only the caret, on the word it is in (like Word), or,
     * between words, for the text typed next.
     */
    private fun toggle(name: String, isOn: (LiveDocxSession, Long) -> Boolean, set: (LiveDocxSession, Long, Long, Boolean) -> Boolean) {
        if (base < 0 || selection()?.selection() != null) return op { e, r -> set(e, r.first, r.last + 1, !isOn(e, r.first)) }
        val s = session() ?: return
        val caretAt = base + typing.selectionEnd.coerceAtLeast(0)
        val word = selection()?.wordAt(caretAt)
        if (word != null && !word.isEmpty() && caretAt > word.first && caretAt <= word.last) {
            if (!set(s, word.first, word.last + 1, !isOn(s, word.first))) {
                if (s.needsFlush) { stopTyping(); return reloadWorking { startTyping(caretAt); toggle(name, isOn, set) } }
                return toast(s.lastError?.message ?: "Không thực hiện được")
            }
            resetBuffer(caretAt)
            caret.touch()
            pagesChangedFrom(word.first)
            return
        }
        val on = !(pending[name]?.first ?: (caretAt > 0 && isOn(s, caretAt - 1)))
        pending[name] = on to set
        selectionLabel.text = "$name: ${if (on) "bật" else "tắt"} cho chữ gõ tiếp"
    }

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
        // B / I / U pressed before typing: on what was just typed
        if (added.isNotEmpty()) for ((on, set) in pending.values) set(s, at, at + added.length, on)
        caret.touch()
        revealCaret()
        pagesChangedFrom(at)
    }

    private val clipboard get() = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    /** What was last copied from this document, with its formatting (see [paste]). */
    private var formattedClip: com.wxiwei.office.editor.docx.LiveDocxSession.FormattedText? = null

    private fun copy() {
        val sel = selection()
        val range = sel?.selection()
        val t = if (range != null) sel.selectedText() else null
        if (t.isNullOrEmpty()) return toast("Chọn chữ trước")
        formattedClip = session()?.copyFormatted(range!!.first, range.last + 1)
        clipboard.setPrimaryClip(ClipData.newPlainText("text", t))
        toast("Đã chép")
    }

    /** The formatted copy when the clipboard still holds its text (nothing else was copied since). */
    private fun formattedFor(text: String) = formattedClip?.takeIf { it.text.replace('\r', '\n') == text.replace('\r', '\n') }

    /**
     * Pastes at the caret while typing, else over the selection: text copied from this document
     * keeps its formatting, other text is pasted plain.
     */
    private fun paste() {
        val t = clipboard.primaryClip?.takeIf { it.itemCount > 0 }?.getItemAt(0)?.coerceToText(context)?.toString()
        if (t.isNullOrEmpty()) return toast("Bộ nhớ tạm trống")
        val formatted = formattedFor(t)
        if (formatted != null) {
            val s = session() ?: return
            if (base >= 0) {
                val at = base + typing.selectionStart.coerceAtLeast(0)
                val until = base + typing.selectionEnd.coerceAtLeast(0)
                stopTyping()
                if (!s.pasteFormatted(minOf(at, until), maxOf(at, until), formatted)) return toast(s.lastError?.message ?: "Không dán được")
                pagesChangedFrom(minOf(at, until))
                startTyping(minOf(at, until) + formatted.text.length)
                return
            }
            return op { e, r -> e.pasteFormatted(r.first, r.last + 1, formatted) }
        }
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
        val word = docView() ?: return
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
        return runCatching { LiveDocxSession(control, working) }.getOrElse {
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
        if (!action(s, caretAt..caretAt)) {
            // in a cell added just now: save the working copy first (the cell gets its place), then again
            if (s.needsFlush) { stopTyping(); return reloadWorking { startTyping(caretAt); paraOp(action) } }
            return toast(s.lastError?.message ?: "Không thực hiện được")
        }
        // offsets did not move: keep typing at the same place
        resetBuffer(caretAt)
        caret.touch()
        pagesChangedFrom(caretAt)
    }

    private fun op(action: (LiveDocxSession, LongRange) -> Boolean) {
        stopTyping()
        val range = selection()?.selection() ?: return toast("Chọn chữ trước")
        val s = session() ?: return
        if (!action(s, range)) {
            if (s.needsFlush) return reloadWorking { selection()?.let { select(it, range) }; op(action) }
            return toast(s.lastError?.message ?: "Không thực hiện được")
        }
        // the pages were laid out again: show the selection on the new layout, refresh thumbnails
        selection()?.let { select(it, range) }
        pagesChangedFrom(range.first)
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

    override fun hasChanges() = session?.hasChanges() == true || workingChanged

    override fun writeTo(target: File): com.wxiwei.office.editor.EditResult {
        session?.takeIf { it.hasChanges() }?.let { return it.save(target) }
        working.copyTo(target, overwrite = true)
        return com.wxiwei.office.editor.EditResult.Ok(target)
    }

    override fun onSaved() {
        session = null
        anchor = null
        stopTyping()
        selectionLabel.text = HINT
        if (working != file) working.delete()
        working = file
        workingChanged = false
        // show the saved text: reopen the document (the panel stays open)
        reopen(file) {}
    }

    private companion object {
        const val HINT = "Chạm vào chữ để gõ, nhấn giữ một từ để chọn"
    }
}
