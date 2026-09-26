package com.alf06.document.reader.ui.home.document.office.edit

import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.wxiwei.office.editor.EditResult
import com.wxiwei.office.editor.pptx.LivePptxSession
import com.wxiwei.office.editor.pptx.Rect
import com.wxiwei.office.editor.pptx.TextFormat
import com.wxiwei.office.reader.OfficeDocumentView
import java.io.File

/**
 * PowerPoint: pick a shape of the current slide, change its text, move or delete it, or add a
 * text box; the slide updates at once and Save writes the .pptx in place.
 */
internal class SlideEditPanel(activity: AppCompatActivity, reader: OfficeDocumentView, file: File) :
    OfficeEditPanel(activity, reader, file) {

    private var session = LivePptxSession(reader.control!!, file)
    private var shapeId = -1
    // position of the selected shape including moves not saved yet (listShapes reads the file)
    private var rect: Rect? = null
    private val selected = label("Chưa chọn shape")
    private val text = input("Nội dung chữ")

    override val view: View = column().apply {
        addView(line(button("Chọn shape", bold = true) { pickShape() }, selected, weights = floatArrayOf(0f, 1f)))
        addView(line(text, button("Đổi chữ") { setText() }, button("+ Text box") { addTextBox() }, weights = floatArrayOf(1f, 0f, 0f)))
        addView(toolRow(
            button("B", bold = true) { format(TextFormat(bold = true)) },
            button("I") { format(TextFormat(italic = true)) },
            button("U") { format(TextFormat(underline = true)) },
            button("Chữ đỏ") { format(TextFormat(rgbHex = "C00000")) },
            button("Chữ đen") { format(TextFormat(rgbHex = "000000")) },
            button("Cỡ 18") { format(TextFormat(sizePt = 18f)) },
            button("Cỡ 28") { format(TextFormat(sizePt = 28f)) },
            button("Cỡ 40") { format(TextFormat(sizePt = 40f)) },
            button("⇤") { format(TextFormat(align = "l")) },
            button("↔") { format(TextFormat(align = "ctr")) },
            button("⇥") { format(TextFormat(align = "r")) },
        ))
        addView(toolRow(
            button("←") { move(-1, 0) },
            button("→") { move(1, 0) },
            button("↑") { move(0, -1) },
            button("↓") { move(0, 1) },
            button("Xóa", color = 0xFFC00000.toInt()) { delete() },
            button("↶") { if (!session.undo()) toast("Không còn gì để hoàn tác") },
            button("↷") { if (!session.redo()) toast("Không còn gì để làm lại") },
            button("Lưu", bold = true, color = 0xFFD96D00.toInt()) { save() },
        ))
    }

    private fun slide(): Int = (reader.state.value.pageNumber - 1).coerceAtLeast(0)

    private fun pickShape() {
        val shapes = session.listShapes(slide())
        if (shapes.isEmpty()) {
            toast("Slide không có shape")
            return
        }
        val labels = shapes.map { s ->
            val t = s.text.replace('\n', ' ').take(40)
            "#${s.id} ${s.kind.name.lowercase()}" + if (t.isNotEmpty()) ": $t" else " (${s.name})"
        }.toTypedArray()
        AlertDialog.Builder(context)
            .setTitle("Slide ${slide() + 1}")
            .setItems(labels) { _, i ->
                shapeId = shapes[i].id
                rect = shapes[i].rectEmu
                selected.text = labels[i]
                text.setText(shapes[i].text)
            }
            .show()
    }

    private fun setText() {
        if (shapeId < 0) return toast("Chọn shape trước")
        if (!session.setShapeText(slide(), shapeId, text.text.toString())) toast(session.lastError?.message ?: "Không đổi được chữ")
        else reopenHint()
    }

    private fun format(f: TextFormat) {
        if (shapeId < 0) return toast("Chọn shape trước")
        if (!session.setTextFormat(slide(), shapeId, f)) toast(session.lastError?.message ?: "Shape này không có chữ")
        else reopenHint()
    }

    private fun move(dx: Int, dy: Int) {
        val r = rect ?: return toast("Chọn shape trước")
        val size = session.slideSizeEmu()
        val step = maxOf(size.width, size.height) / 100 // 1% of the slide per tap
        val moved = Rect(r.x + dx * step, r.y + dy * step, r.width, r.height)
        if (!session.moveShape(slide(), shapeId, moved)) toast(session.lastError?.message ?: "Không di chuyển được")
        else {
            rect = moved
            reopenHint()
        }
    }

    private fun delete() {
        if (shapeId < 0) return toast("Chọn shape trước")
        if (!session.deleteShape(slide(), shapeId)) toast(session.lastError?.message ?: "Không xóa được")
        else {
            shapeId = -1
            rect = null
            selected.text = "Đã xóa"
            reopenHint()
        }
    }

    private fun addTextBox() {
        val content = text.text.toString().ifBlank { "Text box" }
        val size = session.slideSizeEmu()
        val rect = Rect(size.width / 4, size.height * 2 / 5, size.width / 2, size.height / 5)
        val id = session.addTextBox(slide(), rect, content, sizePt = 32f)
        if (id < 0) toast(session.lastError?.message ?: "Không thêm được") else {
            shapeId = id
            this.rect = rect
            selected.text = "#$id text: $content"
            reopenHint()
        }
    }

    /** Some edits (shapes inside groups) are saved but not shown until the file is reopened. */
    private fun reopenHint() {
        if (session.needsReopen) toast("Thay đổi sẽ hiện đầy đủ sau khi lưu và mở lại")
    }

    private fun save() {
        if (!session.hasChanges()) return toast("Chưa có thay đổi")
        val result = saveOver(file) { target -> session.save(target) }
        report(result, "Đã lưu " + file.name)
        if (result is EditResult.Ok) session = LivePptxSession(reader.control!!, file)
    }
}
