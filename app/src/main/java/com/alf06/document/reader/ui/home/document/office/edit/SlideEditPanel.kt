package com.alf06.document.reader.ui.home.document.office.edit

import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.wxiwei.office.editor.EditResult
import com.wxiwei.office.editor.pptx.LivePptxSession
import com.wxiwei.office.editor.pptx.PptxShapeInfo
import com.wxiwei.office.editor.pptx.ShapeKind
import com.wxiwei.office.editor.pptx.Rect
import com.wxiwei.office.editor.pptx.TextFormat
import com.wxiwei.office.editor.slide.SlideGeometry
import com.wxiwei.office.pg.control.Presentation
import com.wxiwei.office.reader.OfficeDocumentView
import com.wxiwei.office.system.IMainFrame
import java.io.File

/**
 * PowerPoint: tap a shape on the slide (or pick it from a list), change its text, move or delete it, or add a
 * text box; the slide updates at once and Save writes the .pptx in place.
 */
internal class SlideEditPanel(activity: AppCompatActivity, reader: OfficeDocumentView, file: File) :
    OfficeEditPanel(activity, reader, file) {

    private var session = LivePptxSession(reader.control!!, file)
    private var shapeId = -1
    // position of the selected shape including moves not saved yet (listShapes reads the file)
    private var rect: Rect? = null
    private val selected = label("Chạm vào shape trên slide để chọn")
    private val text = input("Nội dung chữ")

    override val view: View = column().apply {
        addView(line(button("Danh sách", bold = true) { pickShape() }, selected, weights = floatArrayOf(0f, 1f)))
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
            button("⟳ 90°") { if (shapeId < 0) toast("Chọn shape trước") else rotate((this@SlideEditPanel.overlay.shapeRotation + 90f) % 360f) },
            button("Xóa", color = 0xFFC00000.toInt()) { delete() },
            button("⧉ Nhân bản slide") { slideOp("nhân bản") { session.duplicateSlide(slide()) } },
            button("Slide ↑") { slideOp("di chuyển") { slide() > 0 && session.moveSlide(slide(), slide() - 1) } },
            button("Slide ↓") { slideOp("di chuyển") { slide() < session.slideCount() - 1 && session.moveSlide(slide(), slide() + 1) } },
            button("Xóa slide", color = 0xFFC00000.toInt()) { slideOp("xóa") { session.deleteSlide(slide()) } },
            button("↶") { if (!session.undo()) toast("Không còn gì để hoàn tác") else afterUndo() },
            button("↷") { if (!session.redo()) toast("Không còn gì để làm lại") else afterUndo() },
            button("Lưu", bold = true, color = 0xFFD96D00.toInt()) { save() },
        ))
    }

    private val overlay = SlideSelectionOverlay(context) { reader.control?.getView() as? Presentation }

    init {
        reader.addView(overlay, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
        reader.onDocumentGesture = gesture@{ type, event ->
            if (type != IMainFrame.ON_SINGLE_TAP_CONFIRMED) return@gesture false
            tapAt(event.rawX, event.rawY) || shapeId >= 0
        }
        overlay.onTap = { x, y -> tapAt(x, y) }
        overlay.onChange = { r -> setRect(r) }
        overlay.onRotate = { deg -> rotate(deg) }
    }

    /** Selects the top shape under a screen point, or clears the selection. */
    private fun tapAt(rawX: Float, rawY: Float): Boolean {
        val p = reader.control?.getView() as? Presentation ?: return false
        val origin = IntArray(2)
        p.getLocationOnScreen(origin)
        val point = SlideGeometry.viewToEmu(p, rawX - origin[0], rawY - origin[1])
        val hit = point?.let { SlideGeometry.hitTest(session.listShapes(slide()), it) }
        select(hit)
        return hit != null
    }

    override fun close() {
        reader.onDocumentGesture = null
        reader.removeView(overlay)
    }

    /** Undo/redo may move the selected shape: re-read its frame and redraw the thumbnail. */
    private fun afterUndo() {
        reader.invalidateThumbnail(slide() + 1)
        if (shapeId < 0) return
        val s = session.listShapes(slide()).firstOrNull { it.id == shapeId }
        rect = s?.rectEmu
        overlay.shapeRotation = s?.rotationDeg ?: 0f
        overlay.selection = rect
        if (s == null) select(null)
    }

    private fun describe(s: PptxShapeInfo): String {
        val t = s.text.replace('\n', ' ').take(40)
        return "#${s.id} ${s.kind.name.lowercase()}" + if (t.isNotEmpty()) ": $t" else " (${s.name})"
    }

    private fun select(s: PptxShapeInfo?) {
        shapeId = s?.id ?: -1
        rect = s?.rectEmu
        overlay.keepAspect = s?.kind == ShapeKind.PICTURE
        overlay.shapeRotation = s?.rotationDeg ?: 0f
        overlay.slideIndex = slide()
        overlay.selection = rect
        selected.text = s?.let { describe(it) } ?: "Chưa chọn shape"
        text.setText(s?.text ?: "")
    }

    private fun slide(): Int = (reader.state.value.pageNumber - 1).coerceAtLeast(0)

    private fun pickShape() {
        val shapes = session.listShapes(slide())
        if (shapes.isEmpty()) {
            toast("Slide không có shape")
            return
        }
        val labels = shapes.map { describe(it) }.toTypedArray()
        AlertDialog.Builder(context)
            .setTitle("Slide ${slide() + 1}")
            .setItems(labels) { _, i -> select(shapes[i]) }
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
        setRect(Rect(r.x + dx * step, r.y + dy * step, r.width, r.height))
    }

    private fun rotate(degrees: Float) {
        if (shapeId < 0) return
        if (!session.rotateShape(slide(), shapeId, degrees)) toast(session.lastError?.message ?: "Không xoay được")
        else {
            overlay.shapeRotation = degrees
            reopenHint()
        }
    }

    /** Moves or resizes the selected shape. */
    private fun setRect(moved: Rect) {
        if (shapeId < 0) return
        if (!session.moveShape(slide(), shapeId, moved)) toast(session.lastError?.message ?: "Không di chuyển được")
        else {
            rect = moved
            overlay.selection = moved
            reopenHint()
        }
    }

    private fun delete() {
        if (shapeId < 0) return toast("Chọn shape trước")
        if (!session.deleteShape(slide(), shapeId)) toast(session.lastError?.message ?: "Không xóa được")
        else {
            select(null)
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
            overlay.slideIndex = slide()
            overlay.selection = rect
            selected.text = "#$id text: $content"
            reopenHint()
        }
    }

    private fun slideOp(what: String, op: () -> Boolean) {
        if (!op()) toast(session.lastError?.message ?: "Không $what được slide")
        else toast("Đã $what slide, sẽ hiện sau khi Lưu")
    }

    /** Some edits (shapes inside groups) are saved but not shown until the file is reopened. */
    private fun reopenHint() {
        reader.invalidateThumbnail(slide() + 1)
        if (session.needsReopen) toast("Thay đổi sẽ hiện đầy đủ sau khi lưu và mở lại")
    }

    private fun save() {
        if (!session.hasChanges()) return toast("Chưa có thay đổi")
        val reopen = session.needsReopen
        val result = saveOver(file) { target -> session.save(target) }
        report(result, "Đã lưu " + file.name)
        if (result is EditResult.Ok) {
            select(null)
            // slide changes and some shape edits only show after reading the file again
            if (reopen) reader.open(file.absolutePath)
            session = LivePptxSession(reader.control!!, file)
        }
    }
}
