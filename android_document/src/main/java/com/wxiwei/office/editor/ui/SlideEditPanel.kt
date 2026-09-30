package com.wxiwei.office.editor.ui

import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.editor.docsdk.EditAction
import com.editor.docsdk.EditRequest
import com.editor.docsdk.EditFeature
import com.editor.docsdk.ShapeItem
import com.editor.docsdk.ShapeOrder
import com.editor.docsdk.TransitionFormat
import com.wxiwei.office.R
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
class SlideEditPanel @JvmOverloads constructor(
    activity: AppCompatActivity, reader: OfficeDocumentView, file: File, features: Set<EditFeature> = EditFeature.all(),
) : OfficeEditPanel(activity, reader, file, features) {

    // the file the view and the session work on: the original, or a working copy in the cache after
    // edits that needed a reopen (slides added, moved...); Save writes it over the original
    private var working = file
    private var workingChanged = false
    private var session = LivePptxSession(reader.control!!, file)
    private var shapeId = -1
    // position of the selected shape including moves not saved yet (listShapes reads the file)
    private var rect: Rect? = null
    // the text of the selected shape, as last read or written
    private var shapeText = ""

    override val tabs = listOf(
        Tab(R.string.docsdk_edit_tab_home, listOf(EditAction.BOLD, EditAction.ITALIC, EditAction.UNDERLINE, EditAction.TEXT_COLOR,
            EditAction.FONT_SIZE, EditAction.ALIGN_LEFT, EditAction.ALIGN_CENTER, EditAction.ALIGN_RIGHT, EditAction.SET_TEXT)),
        Tab(R.string.docsdk_edit_tab_insert, listOf(EditAction.ADD_TEXT_BOX, EditAction.INSERT_PICTURE, EditAction.ADD_SHAPE)),
        Tab(R.string.docsdk_edit_tab_arrange, listOf(EditAction.SHAPE_LIST, EditAction.MOVE_LEFT, EditAction.MOVE_RIGHT, EditAction.MOVE_UP,
            EditAction.MOVE_DOWN, EditAction.ROTATE, EditAction.DELETE)),
        Tab(R.string.docsdk_edit_tab_slides, listOf(EditAction.ADD_SLIDE, EditAction.DUPLICATE_SLIDE, EditAction.SLIDE_UP, EditAction.SLIDE_DOWN,
            EditAction.DELETE_SLIDE, EditAction.TRANSITION, EditAction.ANIMATIONS, EditAction.EXPORT_PNG, EditAction.EXPORT_PDF)),
    )

    private val overlay = SlideSelectionOverlay(context) { docView() as? Presentation }

    init {
        status = str(R.string.docsdk_edit_slide_hint)
        action(EditAction.UNDO, EditFeature.UNDO_REDO) { if (!session.undo()) toast(str(R.string.docsdk_edit_nothing_to_undo)) else afterUndo() }
        action(EditAction.REDO, EditFeature.UNDO_REDO) { if (!session.redo()) toast(str(R.string.docsdk_edit_nothing_to_redo)) else afterUndo() }
        action(EditAction.SAVE) { save() }
        action(EditAction.SAVE_COPY, EditFeature.SAVE_COPY) { stopInline(commit = true); saveCopy() }
        action(EditAction.BOLD, EditFeature.FORMAT) { toggle { TextFormat(bold = it.bold != true) } }
        action(EditAction.ITALIC, EditFeature.FORMAT) { toggle { TextFormat(italic = it.italic != true) } }
        action(EditAction.UNDERLINE, EditFeature.FORMAT) { toggle { TextFormat(underline = it.underline != true) } }
        action(EditAction.TEXT_COLOR, EditFeature.FORMAT) { pickColor(str(R.string.docsdk_edit_text_color)) { c -> c?.let { format(TextFormat(rgbHex = it)) } } }
        action(EditAction.FONT_SIZE, EditFeature.FORMAT) { pickSize { format(TextFormat(sizePt = it)) } }
        action(EditAction.ALIGN_LEFT, EditFeature.FORMAT) { format(TextFormat(align = "l")) }
        action(EditAction.ALIGN_CENTER, EditFeature.FORMAT) { format(TextFormat(align = "ctr")) }
        action(EditAction.ALIGN_RIGHT, EditFeature.FORMAT) { format(TextFormat(align = "r")) }
        action(EditAction.SET_TEXT, EditFeature.TEXT) { setText() }
        action(EditAction.SHAPE_LIST) { pickShape() }
        action(EditAction.ADD_TEXT_BOX, EditFeature.SHAPES) { addTextBox() }
        action(EditAction.INSERT_PICTURE, EditFeature.PICTURES) { pickImage() }
        action(EditAction.ADD_SHAPE, EditFeature.SHAPES) { askNewShape() }
        action(EditAction.MOVE_LEFT, EditFeature.SHAPES) { move(-1, 0) }
        action(EditAction.MOVE_RIGHT, EditFeature.SHAPES) { move(1, 0) }
        action(EditAction.MOVE_UP, EditFeature.SHAPES) { move(0, -1) }
        action(EditAction.MOVE_DOWN, EditFeature.SHAPES) { move(0, 1) }
        action(EditAction.ROTATE, EditFeature.SHAPES) { if (shapeId < 0) toast(str(R.string.docsdk_edit_select_shape_first)) else rotate((overlay.shapeRotation + 90f) % 360f) }
        action(EditAction.DELETE, EditFeature.SHAPES) { delete() }
        action(EditAction.ANIMATIONS, EditFeature.ANIMATIONS) { editEffects() }
        action(EditAction.TRANSITION, EditFeature.ANIMATIONS) { editTransition() }
        action(EditAction.ADD_SLIDE, EditFeature.SLIDES) { slideOp(R.string.docsdk_edit_slide_add_failed, { it + 1 }) { session.addBlankSlide(slide()) } }
        action(EditAction.DUPLICATE_SLIDE, EditFeature.SLIDES) { slideOp(R.string.docsdk_edit_slide_duplicate_failed, { it + 1 }) { session.duplicateSlide(slide()) } }
        action(EditAction.SLIDE_UP, EditFeature.SLIDES) { slideOp(R.string.docsdk_edit_slide_move_failed, { it - 1 }) { slide() > 0 && session.moveSlide(slide(), slide() - 1) } }
        action(EditAction.SLIDE_DOWN, EditFeature.SLIDES) { slideOp(R.string.docsdk_edit_slide_move_failed, { it + 1 }) { slide() < session.slideCount() - 1 && session.moveSlide(slide(), slide() + 1) } }
        action(EditAction.DELETE_SLIDE, EditFeature.SLIDES) { slideOp(R.string.docsdk_edit_slide_delete_failed, { it - 1 }) { session.deleteSlide(slide()) } }
        action(EditAction.EXPORT_PNG, EditFeature.EXPORT) { exportSlide(pdf = false) }
        action(EditAction.EXPORT_PDF, EditFeature.EXPORT) { exportSlide(pdf = true) }
    }

    override fun isActive(action: EditAction): Boolean {
        if (shapeId < 0) return false
        val f = runCatching { session.textFormatOf(slide(), shapeId) }.getOrNull() ?: return false
        return when (action) {
            EditAction.BOLD -> f.bold == true
            EditAction.ITALIC -> f.italic == true
            EditAction.UNDERLINE -> f.underline == true
            else -> false
        }
    }

    init {
        addOverlay(overlay)
        keepAboveKeyboard(true)
        reader.onDocumentGesture = gesture@{ type, event ->
            if (type != IMainFrame.ON_SINGLE_TAP_CONFIRMED) return@gesture false
            tapAt(event.rawX, event.rawY) || shapeId >= 0
        }
        overlay.onTap = { x, y -> tapAt(x, y) }
        overlay.movable = has(EditFeature.SHAPES)
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
            private val formats = if (!has(EditFeature.FORMAT)) emptyList() else listOf(
                str(R.string.docsdk_edit_bold) to TextFormat(bold = true), str(R.string.docsdk_edit_italic) to TextFormat(italic = true),
                str(R.string.docsdk_edit_underline) to TextFormat(underline = true), str(R.string.docsdk_edit_red_text) to TextFormat(rgbHex = "C00000"))
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
        toolbar?.visibility = View.GONE
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
        if (!session.setTextFormat(slide(), shapeId, start, end, format)) toast(session.lastError?.message ?: str(R.string.docsdk_edit_format_failed))
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
        // above the bar and the keyboard when the shape is under them
        val top = minOf(frame.top.toInt(), visibleBottom(reader) - h - dp(8)).coerceAtLeast(0)
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
        toolbar?.visibility = View.VISIBLE
        if (commit && shapeId >= 0 && value != shapeText) {
            if (!session.setShapeText(slide(), shapeId, value)) toast(session.lastError?.message ?: str(R.string.docsdk_edit_text_change_failed))
            else {
                shapeText = value
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
        if (!editing && hit != null && hit.id == shapeId && hit.kind == ShapeKind.TEXT && has(EditFeature.TEXT)) {
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
        status = s?.let { describe(it) } ?: str(R.string.docsdk_edit_no_shape_selected)
        shapeText = s?.text ?: ""
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
                toast(if (ok) str(R.string.docsdk_edit_exported_slide, index + 1) else str(R.string.docsdk_edit_slide_not_ready))
            } catch (e: Exception) {
                toast(str(R.string.docsdk_edit_export_failed, e.message ?: ""))
            }
        }
        launcher.launch(name)
    }

    /**
     * The shapes of the slide, the top layer first: a tap selects one; ▲ / ▼ move it one layer up or
     * down (z-order), a long press offers "to the front" / "to the back".
     */
    private fun pickShape() {
        if (session.listShapes(slide()).isEmpty()) return toast(str(R.string.docsdk_edit_slide_no_shapes))
        val orders = mapOf(ShapeOrder.FRONT to "front", ShapeOrder.FORWARD to "forward", ShapeOrder.BACKWARD to "backward", ShapeOrder.BACK to "back")
        if (appAnswers(EditRequest.Shapes(running, str(R.string.docsdk_edit_shape_list_title, slide() + 1),
                read = { session.listShapes(slide()).asReversed().map { ShapeItem(it.id, it.name, it.kind.name.lowercase(), it.text, it.id == shapeId) } },
                onSelect = { id -> session.listShapes(slide()).firstOrNull { it.id == id }?.let { select(it) } },
                onReorder = { id, order -> has(EditFeature.SHAPES) && reorder(id, orders.getValue(order)) }))) return
        dialogs.show(str(R.string.docsdk_edit_shape_list_title, slide() + 1)) {
            fun fill() {
                clear()
                for (s in session.listShapes(slide()).asReversed()) {
                    row(describe(s), bold = s.id == shapeId,
                        onClick = { select(s); dialog?.dismiss() },
                        onLongClick = if (!has(EditFeature.SHAPES)) null else fun() {
                            val moves = listOf(str(R.string.docsdk_edit_bring_to_front) to "front", str(R.string.docsdk_edit_bring_forward) to "forward", str(R.string.docsdk_edit_send_backward) to "backward", str(R.string.docsdk_edit_send_to_back) to "back")
                            dialogs.pick(describe(s), moves.map { it.first }) { i -> reorder(s.id, moves[i].second); fill() }
                        },
                        actions = if (!has(EditFeature.SHAPES)) emptyList() else listOf(
                            DialogKit.Action("▲", str(R.string.docsdk_edit_bring_forward_of, s.id)) { reorder(s.id, "forward"); fill() },
                            DialogKit.Action("▼", str(R.string.docsdk_edit_send_backward_of, s.id)) { reorder(s.id, "backward"); fill() },
                        ))
                }
            }
            fill()
            negative(str(R.string.docsdk_edit_close))
        }
    }

    /** The show script with the edits made here (animations just added play in the slideshow). */
    fun showScript(): List<com.wxiwei.office.editor.pptx.SlideScript> = session.showScript()

    // ---- animations ------------------------------------------------------------------------

    private fun effectName(e: SlideEffect): String {
        val name = when (e.effect) {
            SlideEffect.Effect.APPEAR -> if (e.kind == SlideEffect.Kind.EXIT) str(R.string.docsdk_edit_disappear) else str(R.string.docsdk_edit_appear)
            SlideEffect.Effect.FADE -> str(R.string.docsdk_edit_fade)
            SlideEffect.Effect.FLY -> str(if (e.kind == SlideEffect.Kind.EXIT) R.string.docsdk_edit_fly_out_from else R.string.docsdk_edit_fly_in_from, directionName(e.direction))
            SlideEffect.Effect.ZOOM -> if (e.kind == SlideEffect.Kind.EXIT) str(R.string.docsdk_edit_zoom_out) else str(R.string.docsdk_edit_zoom_in)
            SlideEffect.Effect.WIPE -> str(R.string.docsdk_edit_wipe_from, directionName(e.direction))
            SlideEffect.Effect.PULSE -> str(R.string.docsdk_edit_pulse)
            SlideEffect.Effect.OTHER -> str(R.string.docsdk_edit_other_effect)
        }
        val kind = when (e.kind) { SlideEffect.Kind.ENTRANCE -> str(R.string.docsdk_edit_kind_entrance); SlideEffect.Kind.EMPHASIS -> str(R.string.docsdk_edit_kind_emphasis); SlideEffect.Kind.EXIT -> str(R.string.docsdk_edit_kind_exit) }
        val start = when (e.start) { SlideEffect.Start.CLICK -> str(R.string.docsdk_edit_start_on_tap); SlideEffect.Start.WITH -> str(R.string.docsdk_edit_start_with); SlideEffect.Start.AFTER -> str(R.string.docsdk_edit_start_after) }
        return "#${e.shapeId} $name ($kind) — $start, ${"%.1f".format(java.util.Locale.ROOT, e.durationMs / 1000f)} s"
    }

    private fun directionName(d: SlideEffect.Direction) = when (d) {
        SlideEffect.Direction.LEFT -> str(R.string.docsdk_edit_from_left); SlideEffect.Direction.RIGHT -> str(R.string.docsdk_edit_from_right)
        SlideEffect.Direction.TOP -> str(R.string.docsdk_edit_from_top); SlideEffect.Direction.BOTTOM -> str(R.string.docsdk_edit_from_bottom)
    }

    /** The slide's animations in play order: remove, move up/down, or add one to the selected shape. */
    private fun editEffects() {
        val index = slide()
        fun apply(list: List<SlideEffect>): Boolean {
            if (session.setSlideEffects(index, list)) return true
            toast(session.lastError?.message ?: str(R.string.docsdk_edit_effects_save_failed))
            return false
        }
        if (appAnswers(EditRequest.Animations(running, str(R.string.docsdk_edit_effects_title, index + 1), index, shapeId,
                read = { session.slideEffects(index) }, write = { apply(it) }))) return
        dialogs.show(str(R.string.docsdk_edit_effects_title, index + 1)) {
            fun fill() {
                clear()
                val list = session.slideEffects(index)
                if (list.isEmpty()) text(str(R.string.docsdk_edit_no_effects))
                list.forEachIndexed { i, e ->
                    row("${i + 1}. " + effectName(e), bold = e.shapeId == shapeId, actions = listOf(
                        DialogKit.Action("▲", str(R.string.docsdk_edit_play_earlier, i + 1)) { if (i > 0) apply(list.toMutableList().apply { add(i - 1, removeAt(i)) }); fill() },
                        DialogKit.Action("▼", str(R.string.docsdk_edit_play_later, i + 1)) { if (i < list.lastIndex) apply(list.toMutableList().apply { add(i + 1, removeAt(i)) }); fill() },
                        DialogKit.Action("✕", str(R.string.docsdk_edit_delete_effect, i + 1)) { apply(list.toMutableList().apply { removeAt(i) }); fill() },
                    ))
                }
            }
            fill()
            positive(str(R.string.docsdk_edit_add_effect_to_selected)) {
                if (shapeId < 0) toast(str(R.string.docsdk_edit_select_shape_first_hint))
                else askEffect(index, shapeId) { e -> apply(session.slideEffects(index) + e); editEffects() }
            }
            negative(str(R.string.docsdk_edit_close))
        }
    }

    /** Kind, effect, direction, start and length of a new effect for shape [id]. */
    private fun askEffect(index: Int, id: Int, done: (SlideEffect) -> Unit) {
        val kinds = listOf(SlideEffect.Kind.ENTRANCE, SlideEffect.Kind.EMPHASIS, SlideEffect.Kind.EXIT)
        val effectsOf = mapOf(
            SlideEffect.Kind.ENTRANCE to listOf(str(R.string.docsdk_edit_appear) to SlideEffect.Effect.APPEAR, str(R.string.docsdk_edit_fade) to SlideEffect.Effect.FADE, str(R.string.docsdk_edit_fly_in) to SlideEffect.Effect.FLY, str(R.string.docsdk_edit_zoom_in) to SlideEffect.Effect.ZOOM, str(R.string.docsdk_edit_wipe) to SlideEffect.Effect.WIPE),
            SlideEffect.Kind.EMPHASIS to listOf(str(R.string.docsdk_edit_pulse) to SlideEffect.Effect.PULSE),
            SlideEffect.Kind.EXIT to listOf(str(R.string.docsdk_edit_disappear) to SlideEffect.Effect.APPEAR, str(R.string.docsdk_edit_fade) to SlideEffect.Effect.FADE, str(R.string.docsdk_edit_fly_out) to SlideEffect.Effect.FLY, str(R.string.docsdk_edit_zoom_out) to SlideEffect.Effect.ZOOM, str(R.string.docsdk_edit_wipe) to SlideEffect.Effect.WIPE),
        )
        val directions = listOf(SlideEffect.Direction.LEFT, SlideEffect.Direction.RIGHT, SlideEffect.Direction.TOP, SlideEffect.Direction.BOTTOM)
        val starts = listOf(SlideEffect.Start.CLICK, SlideEffect.Start.WITH, SlideEffect.Start.AFTER)
        val lengths = listOf(250, 500, 1000, 2000)
        dialogs.show(str(R.string.docsdk_edit_add_effect_title, id)) {
            caption(str(R.string.docsdk_edit_effect_kind))
            val kindGroup = choices(listOf(str(R.string.docsdk_edit_appear), str(R.string.docsdk_edit_emphasis), str(R.string.docsdk_edit_disappear)), 0, horizontal = true)
            caption(str(R.string.docsdk_edit_animations))
            // one group per kind; the one of the kind picked shows
            val effectGroups = kinds.map { k -> choices(effectsOf.getValue(k).map { it.first }, if (k == SlideEffect.Kind.EMPHASIS) 0 else 1) }
            fun showKind(k: Int) = effectGroups.forEachIndexed { i, g -> g.group.visibility = if (i == k) View.VISIBLE else View.GONE }
            showKind(0)
            kindGroup.onChange { showKind(it) }
            caption(str(R.string.docsdk_edit_effect_direction))
            val directionGroup = choices(listOf(str(R.string.docsdk_edit_left), str(R.string.docsdk_edit_right), str(R.string.docsdk_edit_top), str(R.string.docsdk_edit_bottom)), 3, horizontal = true)
            caption(str(R.string.docsdk_edit_effect_start))
            val startGroup = choices(listOf(str(R.string.docsdk_edit_on_tap), str(R.string.docsdk_edit_with_previous), str(R.string.docsdk_edit_after_previous)), 0, horizontal = true)
            caption(str(R.string.docsdk_edit_duration))
            val lengthGroup = choices(listOf(str(R.string.docsdk_edit_seconds_quarter), str(R.string.docsdk_edit_seconds_half), "1 s", "2 s"), 1, horizontal = true)
            positive(str(R.string.docsdk_edit_add)) {
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
        fun apply(f: TransitionFormat) {
            if (f.type !in TRANSITION_TYPES || f.direction !in listOf(null, "l", "r", "u", "d") || f.durationMs < 0) return toast(str(R.string.docsdk_edit_invalid_value))
            val t = if (f.type == "none" && f.advanceAfterMs == null) null
                else SlideTransition(f.type, f.direction, f.durationMs, f.advanceAfterMs)
            val slides = if (f.allSlides) (0 until session.slideCount()).toList() else listOf(index)
            if (!session.setSlideTransition(slides, t)) toast(session.lastError?.message ?: str(R.string.docsdk_edit_transition_failed))
            else toast(if (f.allSlides) str(R.string.docsdk_edit_set_for_slides, slides.size) else str(R.string.docsdk_edit_set_for_slide, index + 1))
        }
        val (given, value) = takePreset()
        if (given) return (value as? TransitionFormat)?.let { apply(it) } ?: toast(str(R.string.docsdk_edit_invalid_value))
        val current = now?.let { TransitionFormat(it.type, it.direction, it.durationMs, it.advanceAfterMs) }
        if (appAnswers(EditRequest.Transition(running, str(R.string.docsdk_edit_transition_title, index + 1), current) { apply(it) })) return
        val types = listOf("none" to str(R.string.docsdk_edit_none), "fade" to str(R.string.docsdk_edit_fade), "push" to str(R.string.docsdk_edit_push), "wipe" to str(R.string.docsdk_edit_wipe), "cover" to str(R.string.docsdk_edit_cover), "pull" to str(R.string.docsdk_edit_pull), "split" to str(R.string.docsdk_edit_split), "zoom" to str(R.string.docsdk_edit_zoom_in), "cut" to str(R.string.docsdk_edit_cut))
        val dirs = listOf("l" to str(R.string.docsdk_edit_to_left), "r" to str(R.string.docsdk_edit_to_right), "u" to str(R.string.docsdk_edit_up), "d" to str(R.string.docsdk_edit_down))
        val lengths = listOf(500, 750, 1000, 2000)
        val autos = listOf<Int?>(null, 3000, 5000, 10000, 20000)
        dialogs.show(str(R.string.docsdk_edit_transition_title, index + 1)) {
            caption(str(R.string.docsdk_edit_transition_type))
            val typeGroup = choices(types.map { it.second }, types.indexOfFirst { it.first == (now?.type ?: "none") }.coerceAtLeast(0))
            caption(str(R.string.docsdk_edit_transition_direction))
            val dirGroup = choices(dirs.map { it.second }, dirs.indexOfFirst { it.first == now?.direction }.coerceAtLeast(0), horizontal = true)
            caption(str(R.string.docsdk_edit_duration))
            val lengthGroup = choices(listOf(str(R.string.docsdk_edit_seconds_half), "0,75 s", "1 s", "2 s"), lengths.indexOfFirst { it >= (now?.durationMs ?: 750) }.let { if (it < 0) 3 else it }, horizontal = true)
            caption(str(R.string.docsdk_edit_advance_after))
            val autoGroup = choices(listOf(str(R.string.docsdk_edit_off), "3 s", "5 s", "10 s", "20 s"), autos.indexOf(now?.advanceAfterMs).coerceAtLeast(0), horizontal = true)
            val all = check(str(R.string.docsdk_edit_apply_to_all_slides))
            positive(str(R.string.docsdk_edit_apply)) {
                apply(TransitionFormat(types[typeGroup.picked.coerceAtLeast(0)].first, dirs[dirGroup.picked.coerceAtLeast(0)].first,
                    lengths[lengthGroup.picked.coerceAtLeast(0)], autos[autoGroup.picked.coerceAtLeast(0)], all.isChecked))
            }
            negative()
        }
    }

    /** Z-order move of a shape; the selection frame stays on the selected shape. */
    private fun reorder(id: Int, where: String): Boolean {
        if (session.reorderShape(slide(), id, where)) return true
        toast(session.lastError?.message ?: str(R.string.docsdk_edit_reorder_failed))
        return false
    }

    private fun setText() {
        if (shapeId < 0) return toast(str(R.string.docsdk_edit_select_shape_first))
        stopInline(commit = true)
        askText(str(R.string.docsdk_edit_set_text), str(R.string.docsdk_edit_shape_text_hint), shapeText) { value ->
            if (!session.setShapeText(slide(), shapeId, value)) toast(session.lastError?.message ?: str(R.string.docsdk_edit_text_change_failed))
            else {
                shapeText = value
                reopenHint()
            }
        }
    }

    /** B/I/U turn off when the shape's text already has them. */
    private fun toggle(next: (TextFormat) -> TextFormat) {
        if (shapeId < 0) return toast(str(R.string.docsdk_edit_select_shape_first))
        format(next(session.textFormatOf(slide(), shapeId) ?: TextFormat()))
    }

    private fun format(f: TextFormat) {
        if (shapeId < 0) return toast(str(R.string.docsdk_edit_select_shape_first))
        if (!session.setTextFormat(slide(), shapeId, f)) toast(session.lastError?.message ?: str(R.string.docsdk_edit_shape_no_text))
        else reopenHint()
    }

    private fun move(dx: Int, dy: Int) {
        val r = rect ?: return toast(str(R.string.docsdk_edit_select_shape_first))
        val size = session.slideSizeEmu()
        val step = maxOf(size.width, size.height) / 100 // 1% of the slide per tap
        setRect(Rect(r.x + dx * step, r.y + dy * step, r.width, r.height))
    }

    private fun rotate(degrees: Float) {
        if (shapeId < 0) return
        if (!session.rotateShape(slide(), shapeId, degrees)) toast(session.lastError?.message ?: str(R.string.docsdk_edit_rotate_failed))
        else {
            overlay.shapeRotation = degrees
            reopenHint()
        }
    }

    /** Moves or resizes the selected shape. */
    private fun setRect(moved: Rect) {
        if (shapeId < 0) return
        if (!session.moveShape(slide(), shapeId, moved)) toast(session.lastError?.message ?: str(R.string.docsdk_edit_move_failed))
        else {
            rect = moved
            overlay.selection = moved
            reopenHint()
        }
    }

    private fun delete() {
        stopInline(commit = false)
        if (shapeId < 0) return toast(str(R.string.docsdk_edit_select_shape_first))
        if (!session.deleteShape(slide(), shapeId)) toast(session.lastError?.message ?: str(R.string.docsdk_edit_delete_failed))
        else {
            select(null)
            status = str(R.string.docsdk_edit_deleted)
            reopenHint()
        }
    }

    /** Picks a picture from the device and puts it in the middle of the slide, half its width at most. */
    private fun pickImage() {
        pickPicture { uri -> addImage(uri) }
    }

    private fun addImage(uri: android.net.Uri) {
        val image = pictureFile(uri, "slide-image-") ?: return
        val bounds = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
        android.graphics.BitmapFactory.decodeFile(image.path, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return toast(str(R.string.docsdk_edit_picture_unreadable))
        val size = session.slideSizeEmu()
        // half the slide width, or less so it fits the height
        var w = size.width / 2
        var h = w * bounds.outHeight / bounds.outWidth
        if (h > size.height * 4 / 5) { h = size.height * 4 / 5; w = h * bounds.outWidth / bounds.outHeight }
        val rect = Rect((size.width - w) / 2, (size.height - h) / 2, w, h)
        val id = session.addImage(slide(), rect, image)
        if (id < 0) return toast(session.lastError?.message ?: str(R.string.docsdk_edit_picture_add_failed))
        shapeId = id
        this.rect = rect
        overlay.keepAspect = true
        overlay.slideIndex = slide()
        overlay.selection = rect
        status = str(R.string.docsdk_edit_new_picture, id)
        reopenHint()
    }

    /** A basic shape in the middle of the slide, in a color to pick (its outline a darker shade), selected. */
    private fun askNewShape() {
        val shapes = listOf(str(R.string.docsdk_edit_shape_rect) to "rect", str(R.string.docsdk_edit_shape_round_rect) to "roundRect", str(R.string.docsdk_edit_shape_ellipse) to "ellipse", str(R.string.docsdk_edit_shape_triangle) to "triangle", str(R.string.docsdk_edit_shape_arrow) to "rightArrow", str(R.string.docsdk_edit_shape_line) to "line")
        pickOne(str(R.string.docsdk_edit_add_shape_title), shapes) { prst ->
            pickColor(if (prst == "line") str(R.string.docsdk_edit_line_color) else str(R.string.docsdk_edit_fill_color)) { c -> c?.let { addShape(prst, it) } }
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
        if (id < 0) return toast(session.lastError?.message ?: str(R.string.docsdk_edit_shape_add_failed))
        shapeId = id
        this.rect = rect
        overlay.slideIndex = slide()
        overlay.selection = rect
        status = str(R.string.docsdk_edit_new_shape, id)
        reopenHint()
    }

    private fun addTextBox() {
        val (given, value) = takePreset()
        val content = (if (given) value?.toString() else null)?.ifBlank { null } ?: "Text box"
        val size = session.slideSizeEmu()
        val rect = Rect(size.width / 4, size.height * 2 / 5, size.width / 2, size.height / 5)
        val id = session.addTextBox(slide(), rect, content, sizePt = 32f)
        if (id < 0) toast(session.lastError?.message ?: str(R.string.docsdk_edit_add_failed)) else {
            shapeId = id
            this.rect = rect
            overlay.slideIndex = slide()
            overlay.selection = rect
            status = "#$id text: $content"
            reopenHint()
        }
    }

    /** A slide change, shown at once by reopening a working copy; [show] is the slide to go to. */
    private fun slideOp(failed: Int, show: (Int) -> Int = { it }, op: () -> Boolean) {
        stopInline(commit = true)
        val at = slide()
        if (!op()) return toast(session.lastError?.message ?: str(failed))
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
        val TRANSITION_TYPES = setOf("none", "fade", "push", "wipe", "cover", "pull", "split", "zoom", "cut")
    }
}

/**
 * Slide [index] of [p] into [out]: a PNG 1920 px wide, or a one-page PDF drawn as vectors, the
 * slide's size in points (the slide is laid out at 96 px per inch). False when not laid out yet.
 */
/** Slide [index] (0-based). */
fun writeSlide(p: com.wxiwei.office.pg.control.Presentation, index: Int, pdf: Boolean, out: java.io.OutputStream): Boolean {
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
fun writeDeckPdf(p: com.wxiwei.office.pg.control.Presentation, count: Int, out: java.io.OutputStream, progress: (Int) -> Unit = {}): Int {
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
