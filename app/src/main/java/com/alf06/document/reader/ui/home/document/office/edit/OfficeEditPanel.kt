package com.alf06.document.reader.ui.home.document.office.edit

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.InputType
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.wxiwei.office.editor.EditResult
import com.wxiwei.office.reader.OfficeDocumentView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File

/**
 * Bottom toolbar for editing the open document in place. One subclass per format; the
 * activity shows [view] and calls [close] when editing ends.
 */
internal abstract class OfficeEditPanel(
    protected val activity: AppCompatActivity,
    protected val reader: OfficeDocumentView,
    protected val file: File,
) {
    abstract val view: View

    /** True when there are edits not written to the file yet. */
    abstract fun hasChanges(): Boolean

    /** Writes the document with every edit to [target]. */
    protected abstract fun writeTo(target: File): EditResult

    /** The file now holds every edit: start over on it. */
    protected abstract fun onSaved()

    /** Saves the edits over the file; true when there is nothing left to save. */
    fun save(): Boolean {
        if (!hasChanges()) {
            toast("Chưa có thay đổi")
            return true
        }
        val result = saveOver(file) { target -> writeTo(target) }
        report(result, "Đã lưu " + file.name)
        if (result !is EditResult.Ok) return false
        // other apps and the file list see the new size and date
        com.alf06.document.reader.utils.AppUtils.notifyMediaScanner(context, file.absolutePath)
        EditDrafts.delete(context, file)
        onSaved()
        return true
    }

    /**
     * Writes the document with every edit to a place the user picks (Downloads, Drive...), keeping
     * the open file as it is.
     */
    fun saveCopy() {
        val mime = when (file.extension.lowercase()) {
            "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
            else -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        }
        var launcher: androidx.activity.result.ActivityResultLauncher<String>? = null
        launcher = activity.activityResultRegistry.register("office-save-copy-" + System.nanoTime(),
            androidx.activity.result.contract.ActivityResultContracts.CreateDocument(mime)) { uri ->
            launcher?.unregister()
            if (uri == null) return@register
            val tmp = File(context.cacheDir, "save-copy." + file.extension).apply { delete() }
            val result = try { writeTo(tmp) } catch (e: Exception) { EditResult.Error(com.wxiwei.office.editor.Reason.IO, e.message ?: "Save failed", e) }
            if (result is EditResult.Ok) {
                try {
                    context.contentResolver.openOutputStream(uri, "wt")!!.use { out -> tmp.inputStream().use { it.copyTo(out) } }
                    toast("Đã lưu bản sao")
                } catch (e: Exception) {
                    toast("Không lưu được: " + (e.message ?: ""))
                }
            } else report(result, "")
            tmp.delete()
        }
        launcher.launch(file.nameWithoutExtension + " (bản sao)." + file.extension)
    }

    /**
     * Asks for a color: Office's theme colors with their tints and shades, the standard colors, the
     * last ones used, or a #RRGGBB code; [none] adds a "no color" choice (null). Hex without "#".
     */
    protected fun pickColor(title: String, none: String? = null, onPick: (String?) -> Unit) {
        val prefs = context.getSharedPreferences("office_edit", Context.MODE_PRIVATE)
        val recent = prefs.getString("recentColors", "")!!.split(',').filter { it.length == 6 }
        val kit = dialogs
        var dialog: androidx.appcompat.app.AlertDialog? = null
        fun choose(c: String) {
            prefs.edit().putString("recentColors", (listOf(c) + recent.filter { it != c }).take(10).joinToString(",")).apply()
            dialog?.dismiss()
            onPick(c)
        }
        fun swatch(c: String) = View(context).apply {
            background = GradientDrawable().apply {
                cornerRadius = dp(4).toFloat(); setColor(0xFF000000.toInt() or c.toInt(16)); setStroke(dp(1), kit.style.divider)
            }
            layoutParams = android.widget.GridLayout.LayoutParams().apply { width = dp(26); height = dp(26); setMargins(dp(2), dp(2), dp(2), dp(2)) }
            // read out by TalkBack (a plain square has nothing else to say)
            contentDescription = "Màu #$c"
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
            setOnClickListener { choose(c) }
        }
        fun grid(colors: List<String>) = android.widget.GridLayout(context).apply { columnCount = 10; colors.forEach { addView(swatch(it)) } }
        dialog = kit.show(title) {
            caption("Màu chủ đề")
            // rows: the theme colors, then 80/60/40 % lighter, then 25/50 % darker (like Office)
            val shades = listOf(0f, 0.8f, 0.6f, 0.4f, -0.25f, -0.5f)
            view(grid(shades.flatMap { k -> THEME_COLORS.map { shade(it, k) } }))
            caption("Màu chuẩn")
            view(grid(STANDARD_COLORS))
            if (recent.isNotEmpty()) { caption("Gần đây"); view(grid(recent)) }
            caption("Mã màu")
            val code = input("#RRGGBB").apply { inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS }
            val preview = View(context)
            // the code field and its preview side by side
            root.removeView(code)
            view(LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
                addView(code, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
                addView(preview, LinearLayout.LayoutParams(dp(32), dp(32)))
            })
            code.addTextChangedListener(object : android.text.TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
                override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
                override fun afterTextChanged(s: android.text.Editable?) {
                    val h = s.toString().trim().removePrefix("#")
                    preview.background = if (h.matches(Regex("(?i)[0-9a-f]{6}"))) GradientDrawable().apply { setColor(0xFF000000.toInt() or h.toInt(16)); setStroke(dp(1), kit.style.divider); cornerRadius = dp(4).toFloat() } else null
                }
            })
            negative()
            positive("OK") {
                val h = code.text.toString().trim().removePrefix("#").uppercase()
                if (h.matches(Regex("[0-9A-F]{6}"))) choose(h) else if (h.isNotEmpty()) toast("Mã màu phải có dạng #RRGGBB")
            }
            if (none != null) neutral(none) { onPick(null) }
        }
    }

    /** [hex] made lighter (k > 0: that share of the way to white) or darker (k < 0: that share less). */
    private fun shade(hex: String, k: Float): String {
        if (k == 0f) return hex
        val c = hex.toInt(16)
        fun ch(v: Int) = (if (k > 0) v + (255 - v) * k else v * (1 + k)).toInt().coerceIn(0, 255)
        return "%02X%02X%02X".format(ch(c shr 16 and 255), ch(c shr 8 and 255), ch(c and 255))
    }

    /** Every dialog of the panel is built by this kit (one look, see [DialogStyle]). */
    protected val dialogs get() = DialogKit(context)

    /** Asks for a font size in points. */
    protected fun pickSize(onPick: (Float) -> Unit) {
        val sizes = floatArrayOf(8f, 9f, 10f, 11f, 12f, 14f, 16f, 18f, 20f, 24f, 28f, 32f, 36f, 40f, 48f, 60f, 72f)
        dialogs.pick("Cỡ chữ", sizes.map { (if (it % 1f == 0f) it.toInt().toString() else it.toString()) + " pt" }) { i -> onPick(sizes[i]) }
    }

    /** Keeps the unsaved edits in a draft (the app may be killed in the background). */
    fun saveDraft() {
        if (!hasChanges()) return
        runCatching { EditDrafts.write(context, file) { target -> writeTo(target) } }
    }

    /** True while the viewer reopens a working copy for this panel: the activity keeps the panel. */
    var reopening = false
        private set

    /**
     * Shows [path] in the viewer (a working copy in the cache, or the saved file) and calls [then]
     * once it is open again, with the new document in reader.control.
     */
    protected fun reopen(path: File, then: () -> Unit) {
        reopening = true
        reader.open(path.absolutePath)
        activity.lifecycleScope.launch {
            // the viewer leaves Ready while it opens, then comes back to it
            kotlinx.coroutines.withTimeoutOrNull(5_000) { reader.state.first { it.status != com.wxiwei.office.reader.ReaderState.Status.Ready } }
            val state = reader.state.first {
                it.status == com.wxiwei.office.reader.ReaderState.Status.Ready || it.status == com.wxiwei.office.reader.ReaderState.Status.Failed
            }
            reopening = false
            // the new document's frame was added on top of the caret, handles, selection frame
            overlays.forEach { it.bringToFront() }
            if (state.status == com.wxiwei.office.reader.ReaderState.Status.Ready) then() else toast("Không mở lại được tài liệu")
        }
    }

    // views drawn over the document (caret, selection handles, shape frame, in-place editor)
    private val overlays = ArrayList<View>()

    /** Adds [overlay] over the document; it stays over it when the document is reopened. */
    protected fun addOverlay(overlay: View, params: android.view.ViewGroup.LayoutParams =
        android.widget.FrameLayout.LayoutParams(android.widget.FrameLayout.LayoutParams.MATCH_PARENT, android.widget.FrameLayout.LayoutParams.MATCH_PARENT)) {
        reader.addView(overlay, params)
        overlays.add(overlay)
    }

    protected fun removeOverlay(overlay: View) {
        reader.removeView(overlay)
        overlays.remove(overlay)
    }

    /** The document's view, or null while the viewer (re)opens a document. */
    protected fun docView(): View? = if (reopening) null else runCatching { reader.control?.getView() }.getOrNull()

    /** A new file for a working copy in the cache: edits live there until Save writes the original. */
    protected fun workingCopy(): File =
        File(File(context.cacheDir, "edit-work").apply { mkdirs() }, "work-" + System.nanoTime() + "." + file.extension)

    /** Called when the panel is hidden; stop listening to the document. Call super. */
    open fun close() {
        keepAboveKeyboard(false)
        autosave.cancel()
    }

    /**
     * Every [autosaveMs] while the bar is open, the unsaved edits go to a draft too: a crash (not
     * only the app going to the background) then loses at most that much; reopening the document
     * offers the draft back.
     */
    private val autosave = activity.lifecycleScope.launch {
        while (true) {
            kotlinx.coroutines.delay(autosaveMs)
            if (!reopening && hasChanges()) saveDraft()
        }
    }

    // The app draws edge to edge, so the keyboard does not resize the window: lift the panel's
    // container above it instead.
    private val imeWatcher = android.view.ViewTreeObserver.OnPreDrawListener { liftAboveKeyboard(); true }
    private var watching = false

    /** Starts (or stops) keeping the toolbar above the soft keyboard. */
    fun keepAboveKeyboard(on: Boolean) {
        val observer = reader.rootView.viewTreeObserver
        if (on && !watching) observer.addOnPreDrawListener(imeWatcher)
        if (!on && watching && observer.isAlive) observer.removeOnPreDrawListener(imeWatcher)
        watching = on
        if (!on) {
            (view.parent as? View)?.translationY = 0f
            if (reader.paddingBottom != 0) reader.setPadding(reader.paddingLeft, reader.paddingTop, reader.paddingRight, 0)
        }
    }

    private fun liftAboveKeyboard() {
        val container = view.parent as? View ?: return
        val insets = androidx.core.view.ViewCompat.getRootWindowInsets(container) ?: return
        val ime = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.ime()).bottom
        val location = IntArray(2)
        container.getLocationInWindow(location)
        // where the toolbar sits when not lifted
        val restingTop = location[1] - container.translationY
        val bottom = restingTop + container.height
        val lift = if (ime <= 0) 0f else maxOf(0f, bottom - (container.rootView.height - ime))
        if (container.translationY != -lift) {
            container.translationY = -lift
            onKeyboardMoved()
        }
        // the toolbar (and the keyboard under it) cover the bottom of the document: let it scroll
        // that far, so its end can come above them
        val readerAt = IntArray(2)
        reader.getLocationInWindow(readerAt)
        val overlap = maxOf(0, (readerAt[1] + reader.height - (restingTop - lift)).toInt())
        if (reader.paddingBottom != overlap) reader.setPadding(reader.paddingLeft, reader.paddingTop, reader.paddingRight, overlap)
    }

    /** The toolbar moved with the keyboard; the visible part of the document changed. */
    protected open fun onKeyboardMoved() {}

    /** Bottom of the document area not covered by the toolbar, in [of]'s coordinates. */
    protected fun visibleBottom(of: View): Int {
        val container = view.parent as? View ?: return of.height
        val a = IntArray(2); val b = IntArray(2)
        container.getLocationOnScreen(a); of.getLocationOnScreen(b)
        return minOf(of.height, a[1] - b[1])
    }

    protected val context: Context get() = activity

    protected fun dp(v: Int): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v.toFloat(), context.resources.displayMetrics).toInt()

    protected fun toast(message: String) = Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

    protected fun column(): LinearLayout = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setBackgroundColor(Color.WHITE)
        elevation = dp(8).toFloat()
        setPadding(dp(8), dp(6), dp(8), dp(6))
    }

    /** A row of buttons that scrolls sideways when it does not fit. */
    protected fun toolRow(vararg buttons: View): View = HorizontalScrollView(context).apply {
        isHorizontalScrollBarEnabled = false
        addView(LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            buttons.forEach { addView(it) }
        })
    }

    protected fun line(vararg views: View, weights: FloatArray? = null): LinearLayout = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        views.forEachIndexed { i, v ->
            val w = weights?.getOrNull(i) ?: 0f
            addView(v, LinearLayout.LayoutParams(if (w > 0) 0 else ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, w))
        }
    }

    protected fun button(text: String, bold: Boolean = false, color: Int = 0xFF333333.toInt(), onClick: () -> Unit): TextView =
        TextView(context).apply {
            this.text = text
            setTextColor(color)
            textSize = 13f
            if (bold) typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            minWidth = dp(40)
            setPadding(dp(10), dp(7), dp(10), dp(7))
            background = GradientDrawable().apply {
                cornerRadius = dp(6).toFloat()
                setColor(0xFFF3F3F3.toInt())
                setStroke(dp(1), 0xFFDDDDDD.toInt())
            }
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                .apply { setMargins(dp(3), dp(3), dp(3), dp(3)) }
            setOnClickListener { onClick() }
        }

    protected fun label(text: String = ""): TextView = TextView(context).apply {
        this.text = text
        textSize = 13f
        setTextColor(0xFF555555.toInt())
        setPadding(dp(4), dp(4), dp(8), dp(4))
    }

    protected fun input(hint: String): EditText = EditText(context).apply {
        this.hint = hint
        textSize = 14f
        isSingleLine = true
        inputType = InputType.TYPE_CLASS_TEXT
    }

    protected fun report(result: EditResult, saved: String) {
        when (result) {
            is EditResult.Ok -> toast(if (result.warnings.isEmpty()) saved else saved + " (" + result.warnings.first() + ")")
            is EditResult.Error -> toast(result.message)
        }
    }

    companion object {
        /** How often the open edits are kept in a draft (shorter in tests). */
        @JvmStatic internal var autosaveMs = 120_000L
        /** Office's theme colors: background/text light and dark, then accents 1-6. */
        private val THEME_COLORS = listOf("FFFFFF", "000000", "E7E6E6", "44546A", "4472C4", "ED7D31", "A5A5A5", "FFC000", "5B9BD5", "70AD47")
        private val STANDARD_COLORS = listOf("C00000", "FF0000", "FFC000", "FFFF00", "92D050", "00B050", "00B0F0", "0070C0", "002060", "7030A0")
        /**
         * Saves through a temporary sibling file and then replaces [original], so a failed save
         * never leaves a half written document behind.
         */
        fun saveOver(original: File, save: (File) -> EditResult): EditResult {
            val tmp = File(original.parentFile, ".${original.nameWithoutExtension}.saving.${original.extension}")
            tmp.delete()
            val result = try {
                save(tmp)
            } catch (e: Exception) {
                EditResult.Error(com.wxiwei.office.editor.Reason.IO, e.message ?: "Save failed", e)
            }
            if (result !is EditResult.Ok) {
                tmp.delete()
                return result
            }
            return try {
                if (!tmp.renameTo(original)) {
                    tmp.copyTo(original, overwrite = true)
                    tmp.delete()
                }
                EditResult.Ok(original, result.warnings)
            } catch (e: Exception) {
                EditResult.Error(com.wxiwei.office.editor.Reason.IO, e.message ?: "Cannot replace the file", e)
            }
        }
    }
}
