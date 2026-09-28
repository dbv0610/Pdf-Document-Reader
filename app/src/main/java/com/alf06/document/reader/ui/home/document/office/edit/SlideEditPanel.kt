package com.alf06.document.reader.ui.home.document.office.edit

import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.wxiwei.office.editor.EditResult
import com.wxiwei.office.editor.pptx.LivePptxSession
import com.wxiwei.office.editor.pptx.PptxShapeInfo
import com.wxiwei.office.editor.pptx.ShapeKind
import com.wxiwei.office.editor.pptx.SlideEffect
import com.wxiwei.office.editor.pptx.SlideTransition
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

    // the file the view and the session work on: the original, or a working copy in the cache after
    // edits that needed a reopen (slides added, moved...); Save writes it over the original
    private var working = file
    private var workingChanged = false
    private var session = LivePptxSession(reader.control!!, file)
    private var shapeId = -1
    // position of the selected shape including moves not saved yet (listShapes reads the file)
    private var rect: Rect? = null
    private val selected = label("Chạm vào shape trên slide để chọn")
    private val text = input("Nội dung chữ")

    override val view: View = column().apply {
        addView(line(button("Danh sách", bold = true) { pickShape() }, selected, weights = floatArrayOf(0f, 1f)))
        addView(line(text, button("Đổi chữ") { setText() }, button("+ Text box") { addTextBox() }, button("+ Ảnh") { pickImage() }, button("+ Hình") { askNewShape() },
            weights = floatArrayOf(1f, 0f, 0f, 0f, 0f)))
        addView(toolRow(
            button("B", bold = true) { toggle { TextFormat(bold = it.bold != true) } },
            button("I") { toggle { TextFormat(italic = it.italic != true) } },
            button("U") { toggle { TextFormat(underline = it.underline != true) } },
            button("Màu chữ") { pickColor("Màu chữ") { c -> c?.let { format(TextFormat(rgbHex = it)) } } },
            button("Cỡ chữ") { pickSize { format(TextFormat(sizePt = it)) } },
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
            button("Hiệu ứng") { editEffects() },
            button("Chuyển slide") { editTransition() },
            button("+ Slide trống") { slideOp("thêm", { it + 1 }) { session.addBlankSlide(slide()) } },
            button("⧉ Nhân bản slide") { slideOp("nhân bản", { it + 1 }) { session.duplicateSlide(slide()) } },
            button("Slide ↑") { slideOp("di chuyển", { it - 1 }) { slide() > 0 && session.moveSlide(slide(), slide() - 1) } },
            button("Slide ↓") { slideOp("di chuyển", { it + 1 }) { slide() < session.slideCount() - 1 && session.moveSlide(slide(), slide() + 1) } },
            button("Xóa slide", color = 0xFFC00000.toInt()) { slideOp("xóa", { it - 1 }) { session.deleteSlide(slide()) } },
            button("Xuất PNG") { exportSlide(pdf = false) },
            button("Xuất PDF") { exportSlide(pdf = true) },
            button("↶") { if (!session.undo()) toast("Không còn gì để hoàn tác") else afterUndo() },
            button("↷") { if (!session.redo()) toast("Không còn gì để làm lại") else afterUndo() },
            button("Lưu", bold = true, color = 0xFFD96D00.toInt()) { save() },
            button("Lưu bản sao") { stopInline(commit = true); saveCopy() },
        ))
    }

    private val overlay = SlideSelectionOverlay(context) { docView() as? Presentation }

    init {
        addOverlay(overlay)
        keepAboveKeyboard(true)
        reader.onDocumentGesture = gesture@{ type, event ->
            if (type != IMainFrame.ON_SINGLE_TAP_CONFIRMED) return@gesture false
            tapAt(event.rawX, event.rawY) || shapeId >= 0
        }
        overlay.onTap = { x, y -> tapAt(x, y) }
        overlay.onChange = { r -> setRect(r) }
        overlay.onRotate = { deg -> rotate(deg) }
        overlay.onFrame = { frame -> placeInline(frame) }
    }

    // In-place text editing: a second tap on the selected text shape opens an editor over it.
    private var inline: EditText? = null

    private fun startInline(s: PptxShapeInfo) {
        stopInline(commit = true)
        val edit = EditText(context).apply {
            setText(s.text)
            setSelection(text.length)
            textSize = 16f
            setTextColor(0xFF111111.toInt())
            setBackgroundColor(0xF0FFFFFF.toInt())
            // the shape's own look, at the slide's zoom (points at 96 dpi, then the view zoom)
            val p = docView() as? Presentation
            session.textStyle(slide(), s.id)?.let { st ->
                val zoom = p?.getZoom() ?: 1f
                setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, (st.sizePt * 96f / 72f * zoom).coerceIn(dp(12).toFloat(), dp(48).toFloat()))
                typeface = st.typeface
                val c = st.color or 0xFF000000.toInt()
                // light text keeps a dark backdrop so it stays readable
                val light = android.graphics.Color.luminance(c) > 0.6f
                setTextColor(c)
                setBackgroundColor(if (light) 0xE0303030.toInt() else 0xF0FFFFFF.toInt())
            }
            setPadding(dp(6), dp(4), dp(6), dp(4))
            gravity = android.view.Gravity.TOP or android.view.Gravity.START
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE
        }
        // formatting of the selected part of the text, from the text selection menu
        edit.customSelectionActionModeCallback = object : android.view.ActionMode.Callback {
            private val formats = listOf(
                "Đậm" to TextFormat(bold = true), "Nghiêng" to TextFormat(italic = true),
                "Gạch chân" to TextFormat(underline = true), "Chữ đỏ" to TextFormat(rgbHex = "C00000"))
            override fun onCreateActionMode(mode: android.view.ActionMode, menu: android.view.Menu): Boolean {
                formats.forEachIndexed { i, (label, _) -> menu.add(android.view.Menu.NONE, FORMAT_MENU_ID + i, 100 + i, label) }
                return true
            }
            override fun onPrepareActionMode(mode: android.view.ActionMode, menu: android.view.Menu) = false
            override fun onActionItemClicked(mode: android.view.ActionMode, item: android.view.MenuItem): Boolean {
                val format = formats.getOrNull(item.itemId - FORMAT_MENU_ID)?.second ?: return false
                val start = minOf(edit.selectionStart, edit.selectionEnd)
                val end = maxOf(edit.selectionStart, edit.selectionEnd)
                mode.finish()
                formatRange(start, end, format)
                return true
            }
            override fun onDestroyActionMode(mode: android.view.ActionMode) {}
        }
        inline = edit
        // the keyboard takes half the screen: give the slide the rest while typing on it
        view.visibility = View.GONE
        addOverlay(edit, FrameLayout.LayoutParams(1, 1))
        placeInline(overlay.frameOnScreen())
        edit.requestFocus()
        (context.getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager)
            .showSoftInput(edit, 0)
    }

    /** Saves the text being edited in place, then formats its chars [start, end) and shows the slide. */
    private fun formatRange(start: Int, end: Int, format: TextFormat) {
        if (shapeId < 0 || end <= start) return
        // positions are in the edited text: write it to the shape first
        stopInline(commit = true)
        if (!session.setTextFormat(slide(), shapeId, start, end, format)) toast(session.lastError?.message ?: "Không định dạng được")
        else reopenHint()
    }

    /** Keeps the editor on the shape frame, at least a few lines tall. */
    private fun placeInline(frame: android.graphics.RectF?) {
        val edit = inline ?: return
        if (frame == null) { edit.visibility = View.INVISIBLE; return }
        edit.visibility = View.VISIBLE
        val lp = edit.layoutParams as FrameLayout.LayoutParams
        val w = maxOf(frame.width().toInt(), dp(160)).coerceAtMost(reader.width)
        val h = maxOf(frame.height().toInt(), dp(96))
        val left = frame.left.toInt().coerceIn(0, maxOf(0, reader.width - w))
        // above the keyboard when the shape is under it
        val insets = androidx.core.view.ViewCompat.getRootWindowInsets(reader)
        val ime = insets?.getInsets(androidx.core.view.WindowInsetsCompat.Type.ime())?.bottom ?: 0
        val at = IntArray(2)
        reader.getLocationInWindow(at)
        val keyboardTop = if (ime > 0) reader.rootView.height - ime - at[1] else reader.height
        val top = minOf(frame.top.toInt(), keyboardTop - h - dp(8)).coerceAtLeast(0)
        if (lp.width != w || lp.height != h || lp.leftMargin != left || lp.topMargin != top) {
            lp.width = w; lp.height = h; lp.leftMargin = left; lp.topMargin = top
            edit.layoutParams = lp
        }
    }

    /** Closes the in-place editor, writing its text to the shape when it changed. */
    private fun stopInline(commit: Boolean) {
        val edit = inline ?: return
        inline = null
        val value = edit.text.toString()
        (context.getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager)
            .hideSoftInputFromWindow(edit.windowToken, 0)
        removeOverlay(edit)
        view.visibility = View.VISIBLE
        if (commit && shapeId >= 0 && value != text.text.toString()) {
            if (!session.setShapeText(slide(), shapeId, value)) toast(session.lastError?.message ?: "Không đổi được chữ")
            else {
                text.setText(value)
                reopenHint()
            }
        }
    }

    /** Selects the top shape under a screen point, or clears the selection. */
    private fun tapAt(rawX: Float, rawY: Float): Boolean {
        val p = docView() as? Presentation ?: return false
        val origin = IntArray(2)
        p.getLocationOnScreen(origin)
        val point = SlideGeometry.viewToEmu(p, rawX - origin[0], rawY - origin[1])
        val hit = point?.let { SlideGeometry.hitTest(session.listShapes(slide()), it) }
        val editing = inline != null
        stopInline(commit = true)
        // a second tap on the selected text shape edits its text in place
        if (!editing && hit != null && hit.id == shapeId && hit.kind == ShapeKind.TEXT) {
            startInline(hit)
            return true
        }
        select(hit)
        return hit != null
    }

    override fun onKeyboardMoved() {
        placeInline(overlay.frameOnScreen())
    }

    override fun close() {
        stopInline(commit = true)
        super.close()
        reader.onDocumentGesture = null
        removeOverlay(overlay)
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

    /** The slide shown, as a PNG (1920 px wide) or a one-page vector PDF, to a file the user picks. */
    private fun exportSlide(pdf: Boolean) {
        val index = slide()
        val p = reader.control?.getView() as? com.wxiwei.office.pg.control.Presentation ?: return
        val name = file.nameWithoutExtension + " - slide " + (index + 1) + if (pdf) ".pdf" else ".png"
        var launcher: androidx.activity.result.ActivityResultLauncher<String>? = null
        launcher = activity.activityResultRegistry.register("slide-export-" + System.nanoTime(),
            androidx.activity.result.contract.ActivityResultContracts.CreateDocument(if (pdf) "application/pdf" else "image/png")) { uri ->
            launcher?.unregister()
            if (uri == null) return@register
            try {
                val ok = context.contentResolver.openOutputStream(uri, "wt")!!.use { out -> writeSlide(p, index, pdf, out) }
                toast(if (ok) "Đã xuất slide ${index + 1}" else "Chưa mở xong slide")
            } catch (e: Exception) {
                toast("Không xuất được: " + (e.message ?: ""))
            }
        }
        launcher.launch(name)
    }

    /**
     * The shapes of the slide, the top layer first: a tap selects one; ▲ / ▼ move it one layer up or
     * down (z-order), a long press offers "to the front" / "to the back".
     */
    private fun pickShape() {
        if (session.listShapes(slide()).isEmpty()) return toast("Slide không có shape")
        dialogs.show("Slide ${slide() + 1} — trên cùng ở đầu") {
            fun fill() {
                clear()
                for (s in session.listShapes(slide()).asReversed()) {
                    row(describe(s), bold = s.id == shapeId,
                        onClick = { select(s); dialog?.dismiss() },
                        onLongClick = {
                            val moves = listOf("Đưa lên trên cùng" to "front", "Lên một lớp" to "forward", "Xuống một lớp" to "backward", "Đưa xuống dưới cùng" to "back")
                            dialogs.pick(describe(s), moves.map { it.first }) { i -> reorder(s.id, moves[i].second); fill() }
                        },
                        actions = listOf(
                            DialogKit.Action("▲", "Lên một lớp ${s.id}") { reorder(s.id, "forward"); fill() },
                            DialogKit.Action("▼", "Xuống một lớp ${s.id}") { reorder(s.id, "backward"); fill() },
                        ))
                }
            }
            fill()
            negative("Đóng")
        }
    }

    /** The show script with the edits made here (animations just added play in the slideshow). */
    internal fun showScript(): List<com.wxiwei.office.editor.pptx.SlideScript> = session.showScript()

    // ---- animations ------------------------------------------------------------------------

    private fun effectName(e: SlideEffect): String {
        val name = when (e.effect) {
            SlideEffect.Effect.APPEAR -> if (e.kind == SlideEffect.Kind.EXIT) "Biến mất" else "Xuất hiện"
            SlideEffect.Effect.FADE -> "Mờ dần"
            SlideEffect.Effect.FLY -> (if (e.kind == SlideEffect.Kind.EXIT) "Bay ra " else "Bay vào ") + directionName(e.direction)
            SlideEffect.Effect.ZOOM -> if (e.kind == SlideEffect.Kind.EXIT) "Thu nhỏ" else "Phóng to"
            SlideEffect.Effect.WIPE -> "Lau " + directionName(e.direction)
            SlideEffect.Effect.PULSE -> "Phóng to rồi thu lại"
            SlideEffect.Effect.OTHER -> "Hiệu ứng khác"
        }
        val kind = when (e.kind) { SlideEffect.Kind.ENTRANCE -> "vào"; SlideEffect.Kind.EMPHASIS -> "nhấn mạnh"; SlideEffect.Kind.EXIT -> "ra" }
        val start = when (e.start) { SlideEffect.Start.CLICK -> "khi chạm"; SlideEffect.Start.WITH -> "cùng lúc"; SlideEffect.Start.AFTER -> "sau đó" }
        return "#${e.shapeId} $name ($kind) — $start, ${"%.1f".format(java.util.Locale.ROOT, e.durationMs / 1000f)} s"
    }

    private fun directionName(d: SlideEffect.Direction) = when (d) {
        SlideEffect.Direction.LEFT -> "từ trái"; SlideEffect.Direction.RIGHT -> "từ phải"
        SlideEffect.Direction.TOP -> "từ trên"; SlideEffect.Direction.BOTTOM -> "từ dưới"
    }

    /** The slide's animations in play order: remove, move up/down, or add one to the selected shape. */
    private fun editEffects() {
        val index = slide()
        fun apply(list: List<SlideEffect>) {
            if (!session.setSlideEffects(index, list)) toast(session.lastError?.message ?: "Không lưu được hiệu ứng")
        }
        dialogs.show("Hiệu ứng — slide ${index + 1}") {
            fun fill() {
                clear()
                val list = session.slideEffects(index)
                if (list.isEmpty()) text("Slide chưa có hiệu ứng")
                list.forEachIndexed { i, e ->
                    row("${i + 1}. " + effectName(e), bold = e.shapeId == shapeId, actions = listOf(
                        DialogKit.Action("▲", "Chạy sớm hơn ${i + 1}") { if (i > 0) apply(list.toMutableList().apply { add(i - 1, removeAt(i)) }); fill() },
                        DialogKit.Action("▼", "Chạy muộn hơn ${i + 1}") { if (i < list.lastIndex) apply(list.toMutableList().apply { add(i + 1, removeAt(i)) }); fill() },
                        DialogKit.Action("✕", "Xóa hiệu ứng ${i + 1}") { apply(list.toMutableList().apply { removeAt(i) }); fill() },
                    ))
                }
            }
            fill()
            positive("+ Thêm cho hình đang chọn") {
                if (shapeId < 0) toast("Chọn hình trước (chạm vào hình hoặc dùng Danh sách)")
                else askEffect(index, shapeId) { e -> apply(session.slideEffects(index) + e); editEffects() }
            }
            negative("Đóng")
        }
    }

    /** Kind, effect, direction, start and length of a new effect for shape [id]. */
    private fun askEffect(index: Int, id: Int, done: (SlideEffect) -> Unit) {
        val kinds = listOf(SlideEffect.Kind.ENTRANCE, SlideEffect.Kind.EMPHASIS, SlideEffect.Kind.EXIT)
        val effectsOf = mapOf(
            SlideEffect.Kind.ENTRANCE to listOf("Xuất hiện" to SlideEffect.Effect.APPEAR, "Mờ dần" to SlideEffect.Effect.FADE, "Bay vào" to SlideEffect.Effect.FLY, "Phóng to" to SlideEffect.Effect.ZOOM, "Lau" to SlideEffect.Effect.WIPE),
            SlideEffect.Kind.EMPHASIS to listOf("Phóng to rồi thu lại" to SlideEffect.Effect.PULSE),
            SlideEffect.Kind.EXIT to listOf("Biến mất" to SlideEffect.Effect.APPEAR, "Mờ dần" to SlideEffect.Effect.FADE, "Bay ra" to SlideEffect.Effect.FLY, "Thu nhỏ" to SlideEffect.Effect.ZOOM, "Lau" to SlideEffect.Effect.WIPE),
        )
        val directions = listOf(SlideEffect.Direction.LEFT, SlideEffect.Direction.RIGHT, SlideEffect.Direction.TOP, SlideEffect.Direction.BOTTOM)
        val starts = listOf(SlideEffect.Start.CLICK, SlideEffect.Start.WITH, SlideEffect.Start.AFTER)
        val lengths = listOf(250, 500, 1000, 2000)
        dialogs.show("Thêm hiệu ứng cho #$id") {
            caption("Loại")
            val kindGroup = choices(listOf("Xuất hiện", "Nhấn mạnh", "Biến mất"), 0, horizontal = true)
            caption("Hiệu ứng")
            // one group per kind; the one of the kind picked shows
            val effectGroups = kinds.map { k -> choices(effectsOf.getValue(k).map { it.first }, if (k == SlideEffect.Kind.EMPHASIS) 0 else 1) }
            fun showKind(k: Int) = effectGroups.forEachIndexed { i, g -> g.group.visibility = if (i == k) View.VISIBLE else View.GONE }
            showKind(0)
            kindGroup.onChange { showKind(it) }
            caption("Hướng (bay, lau)")
            val directionGroup = choices(listOf("Trái", "Phải", "Trên", "Dưới"), 3, horizontal = true)
            caption("Bắt đầu")
            val startGroup = choices(listOf("Khi chạm", "Cùng lúc", "Sau đó"), 0, horizontal = true)
            caption("Thời lượng")
            val lengthGroup = choices(listOf("0,25 s", "0,5 s", "1 s", "2 s"), 1, horizontal = true)
            positive("Thêm") {
                val k = kindGroup.picked.coerceAtLeast(0)
                val kind = kinds[k]
                val effect = effectsOf.getValue(kind)[effectGroups[k].picked.coerceAtLeast(0)].second
                done(SlideEffect(id, kind, effect, directions[directionGroup.picked.coerceAtLeast(0)], starts[startGroup.picked.coerceAtLeast(0)],
                    if (effect == SlideEffect.Effect.APPEAR) 0 else lengths[lengthGroup.picked.coerceAtLeast(0)]))
            }
            negative()
        }
    }

    /** How the slide comes in during the slideshow, for this slide or all of them. */
    private fun editTransition() {
        val index = slide()
        val now = session.slideTransition(index)
        val types = listOf("none" to "Không", "fade" to "Mờ dần", "push" to "Đẩy", "wipe" to "Lau", "cover" to "Che", "pull" to "Kéo ra", "split" to "Tách", "zoom" to "Phóng to", "cut" to "Cắt")
        val dirs = listOf("l" to "Sang trái", "r" to "Sang phải", "u" to "Lên", "d" to "Xuống")
        val lengths = listOf(500, 750, 1000, 2000)
        val autos = listOf<Int?>(null, 3000, 5000, 10000, 20000)
        dialogs.show("Chuyển slide ${index + 1}") {
            caption("Kiểu")
            val typeGroup = choices(types.map { it.second }, types.indexOfFirst { it.first == (now?.type ?: "none") }.coerceAtLeast(0))
            caption("Hướng (đẩy, lau, che, kéo ra)")
            val dirGroup = choices(dirs.map { it.second }, dirs.indexOfFirst { it.first == now?.direction }.coerceAtLeast(0), horizontal = true)
            caption("Thời lượng")
            val lengthGroup = choices(listOf("0,5 s", "0,75 s", "1 s", "2 s"), lengths.indexOfFirst { it >= (now?.durationMs ?: 750) }.let { if (it < 0) 3 else it }, horizontal = true)
            caption("Tự chuyển sau")
            val autoGroup = choices(listOf("Tắt", "3 s", "5 s", "10 s", "20 s"), autos.indexOf(now?.advanceAfterMs).coerceAtLeast(0), horizontal = true)
            val all = check("Áp dụng cho tất cả slide")
            positive("Áp dụng") {
                val type = types[typeGroup.picked.coerceAtLeast(0)].first
                val auto = autos[autoGroup.picked.coerceAtLeast(0)]
                val t = if (type == "none" && auto == null) null
                    else SlideTransition(type, dirs[dirGroup.picked.coerceAtLeast(0)].first, lengths[lengthGroup.picked.coerceAtLeast(0)], auto)
                val slides = if (all.isChecked) (0 until session.slideCount()).toList() else listOf(index)
                if (!session.setSlideTransition(slides, t)) toast(session.lastError?.message ?: "Không đặt được chuyển slide")
                else toast(if (all.isChecked) "Đã đặt cho ${slides.size} slide" else "Đã đặt cho slide ${index + 1}")
            }
            negative()
        }
    }

    /** Z-order move of a shape; the selection frame stays on the selected shape. */
    private fun reorder(id: Int, where: String) {
        if (!session.reorderShape(slide(), id, where)) toast(session.lastError?.message ?: "Không đổi được thứ tự")
    }

    private fun setText() {
        if (shapeId < 0) return toast("Chọn shape trước")
        if (!session.setShapeText(slide(), shapeId, text.text.toString())) toast(session.lastError?.message ?: "Không đổi được chữ")
        else reopenHint()
    }

    /** B/I/U turn off when the shape's text already has them. */
    private fun toggle(next: (TextFormat) -> TextFormat) {
        if (shapeId < 0) return toast("Chọn shape trước")
        format(next(session.textFormatOf(slide(), shapeId) ?: TextFormat()))
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
        stopInline(commit = false)
        if (shapeId < 0) return toast("Chọn shape trước")
        if (!session.deleteShape(slide(), shapeId)) toast(session.lastError?.message ?: "Không xóa được")
        else {
            select(null)
            selected.text = "Đã xóa"
            reopenHint()
        }
    }

    /** Picks a picture from the device and puts it in the middle of the slide, half its width at most. */
    private fun pickImage() {
        var launcher: androidx.activity.result.ActivityResultLauncher<String>? = null
        launcher = activity.activityResultRegistry.register("slide-image-" + System.nanoTime(),
            androidx.activity.result.contract.ActivityResultContracts.GetContent()) { uri ->
            launcher?.unregister()
            if (uri != null) addImage(uri)
        }
        launcher.launch("image/*")
    }

    private fun addImage(uri: android.net.Uri) {
        val type = context.contentResolver.getType(uri) ?: "image/jpeg"
        val ext = when { type.contains("png") -> "png"; type.contains("gif") -> "gif"; type.contains("bmp") -> "bmp"; else -> "jpeg" }
        val image = File(context.cacheDir, "slide-image-" + System.nanoTime() + "." + ext)
        try {
            context.contentResolver.openInputStream(uri)!!.use { input -> image.outputStream().use { input.copyTo(it) } }
        } catch (e: Exception) {
            return toast("Không đọc được ảnh")
        }
        val bounds = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
        android.graphics.BitmapFactory.decodeFile(image.path, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return toast("Không đọc được ảnh")
        val size = session.slideSizeEmu()
        // half the slide width, or less so it fits the height
        var w = size.width / 2
        var h = w * bounds.outHeight / bounds.outWidth
        if (h > size.height * 4 / 5) { h = size.height * 4 / 5; w = h * bounds.outWidth / bounds.outHeight }
        val rect = Rect((size.width - w) / 2, (size.height - h) / 2, w, h)
        val id = session.addImage(slide(), rect, image)
        if (id < 0) return toast(session.lastError?.message ?: "Không thêm được ảnh")
        shapeId = id
        this.rect = rect
        overlay.keepAspect = true
        overlay.slideIndex = slide()
        overlay.selection = rect
        selected.text = "#$id ảnh"
        reopenHint()
    }

    /** A basic shape in the middle of the slide, in a color to pick (its outline a darker shade), selected. */
    private fun askNewShape() {
        val shapes = listOf("Chữ nhật" to "rect", "Chữ nhật bo góc" to "roundRect", "Elip" to "ellipse", "Tam giác" to "triangle", "Mũi tên" to "rightArrow", "Đường thẳng" to "line")
        dialogs.pick("Thêm hình", shapes.map { it.first }) { i ->
            val prst = shapes[i].second
            pickColor(if (prst == "line") "Màu đường" else "Màu nền") { c -> c?.let { addShape(prst, it) } }
        }
    }

    private fun addShape(prst: String, color: String) {
        stopInline(commit = true)
        val size = session.slideSizeEmu()
        val line = prst == "line"
        val rect = if (line) Rect(size.width / 3, size.height / 2, size.width / 3, 0)
            else Rect(size.width * 3 / 8, size.height * 3 / 8, size.width / 4, size.height / 4)
        // Office outlines a filled shape in its color, 25 % darker
        val c = color.toInt(16)
        val darker = "%02X%02X%02X".format((c shr 16 and 255) * 3 / 4, (c shr 8 and 255) * 3 / 4, (c and 255) * 3 / 4)
        val id = session.addShape(slide(), rect, prst, if (line) null else color, if (line) color else darker, if (line) 2f else 1.5f)
        if (id < 0) return toast(session.lastError?.message ?: "Không thêm được hình")
        shapeId = id
        this.rect = rect
        overlay.slideIndex = slide()
        overlay.selection = rect
        selected.text = "#$id hình"
        reopenHint()
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

    /** A slide change, shown at once by reopening a working copy; [show] is the slide to go to. */
    private fun slideOp(what: String, show: (Int) -> Int = { it }, op: () -> Boolean) {
        stopInline(commit = true)
        val at = slide()
        if (!op()) return toast(session.lastError?.message ?: "Không $what được slide")
        reloadWorking(show(at).coerceIn(0, maxOf(0, session.slideCount() - 1)))
    }

    /**
     * Writes every edit to a new working copy in the cache and shows it: the original file is only
     * written by Save.
     */
    private fun reloadWorking(slideIndex: Int) {
        select(null)
        val next = workingCopy()
        val result = session.save(next)
        if (result !is EditResult.Ok) return report(result, "")
        val previous = working
        working = next
        workingChanged = true
        reopen(next) {
            session = LivePptxSession(reader.control!!, next)
            if (previous != file) previous.delete()
            (docView() as? Presentation)?.showSlide(slideIndex, false)
            reader.invalidateThumbnail(slideIndex + 1)
        }
    }

    /** Edits the view cannot show at once (shapes inside groups) are shown by reopening a working copy. */
    private fun reopenHint() {
        reader.invalidateThumbnail(slide() + 1)
        if (session.needsReopen) reloadWorking(slide())
    }

    override fun hasChanges(): Boolean {
        stopInline(commit = true)
        return session.hasChanges() || workingChanged
    }

    override fun writeTo(target: File): EditResult {
        if (session.hasChanges()) return session.save(target)
        working.copyTo(target, overwrite = true)
        return EditResult.Ok(target)
    }

    override fun onSaved() {
        select(null)
        // the view already shows what was saved; edit the original from now on
        if (working != file) working.delete()
        working = file
        workingChanged = false
        session = LivePptxSession(reader.control!!, file)
    }

    private companion object {
        // ids of the formatting items added to the text selection menu
        const val FORMAT_MENU_ID = 0x5E10
    }
}

/**
 * Slide [index] of [p] into [out]: a PNG 1920 px wide, or a one-page PDF drawn as vectors, the
 * slide's size in points (the slide is laid out at 96 px per inch). False when not laid out yet.
 */
/** Slide [index] (0-based). */
internal fun writeSlide(p: com.wxiwei.office.pg.control.Presentation, index: Int, pdf: Boolean, out: java.io.OutputStream): Boolean {
    val size = p.getPageSize() ?: return false
    val w = size.getWidth().toInt(); val h = size.getHeight().toInt()
    if (w <= 0 || h <= 0) return false
    if (pdf) {
        val k = 72f / 96f
        val doc = android.graphics.pdf.PdfDocument()
        try {
            val page = doc.startPage(android.graphics.pdf.PdfDocument.PageInfo.Builder(Math.round(w * k), Math.round(h * k), 1).create())
            page.canvas.drawColor(android.graphics.Color.WHITE)
            page.canvas.scale(k, k)
            p.drawSlide(index + 1, page.canvas)
            doc.finishPage(page)
            doc.writeTo(out)
        } finally { doc.close() }
    } else {
        val scale = 1920f / w
        val bitmap = android.graphics.Bitmap.createBitmap(1920, Math.round(h * scale), android.graphics.Bitmap.Config.ARGB_8888)
        try {
            val canvas = android.graphics.Canvas(bitmap)
            canvas.drawColor(android.graphics.Color.WHITE)
            canvas.scale(scale, scale)
            p.drawSlide(index + 1, canvas)
            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out)
        } finally { bitmap.recycle() }
    }
    return true
}

/** Every slide as one page of a vector PDF; the number of slides written. */
internal fun writeDeckPdf(p: com.wxiwei.office.pg.control.Presentation, count: Int, out: java.io.OutputStream, progress: (Int) -> Unit = {}): Int {
    val size = p.getPageSize() ?: return 0
    val w = size.getWidth().toInt(); val h = size.getHeight().toInt()
    if (w <= 0 || h <= 0) return 0
    val k = 72f / 96f
    val doc = android.graphics.pdf.PdfDocument()
    var written = 0
    try {
        for (i in 0 until count) {
            val page = doc.startPage(android.graphics.pdf.PdfDocument.PageInfo.Builder(Math.round(w * k), Math.round(h * k), i + 1).create())
            page.canvas.drawColor(android.graphics.Color.WHITE)
            page.canvas.scale(k, k)
            if (p.drawSlide(i + 1, page.canvas)) written++
            doc.finishPage(page)
            progress(i + 1)
        }
        doc.writeTo(out)
    } finally { doc.close() }
    return written
}
