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

    /** Asks for a color from a palette; [none] adds a "no color" choice (null). */
    protected fun pickColor(title: String, none: String? = null, onPick: (String?) -> Unit) {
        val colors = listOf("000000", "404040", "7F7F7F", "BFBFBF", "FFFFFF", "C00000", "FF0000", "FFC000", "FFFF00",
            "92D050", "00B050", "00B0F0", "0070C0", "002060", "7030A0", "FF66CC", "F4B183", "FFF2CC", "DDEBF7", "E2EFDA")
        val grid = android.widget.GridLayout(context).apply { columnCount = 5; setPadding(dp(12), dp(8), dp(12), dp(8)) }
        var dialog: androidx.appcompat.app.AlertDialog? = null
        for (c in colors) grid.addView(View(context).apply {
            background = GradientDrawable().apply {
                cornerRadius = dp(6).toFloat(); setColor(0xFF000000.toInt() or c.toInt(16)); setStroke(dp(1), 0xFFBBBBBB.toInt())
            }
            layoutParams = android.widget.GridLayout.LayoutParams().apply { width = dp(44); height = dp(44); setMargins(dp(5), dp(5), dp(5), dp(5)) }
            contentDescription = c
            setOnClickListener { dialog?.dismiss(); onPick(c) }
        })
        val builder = androidx.appcompat.app.AlertDialog.Builder(context).setTitle(title).setView(grid).setNegativeButton("Hủy", null)
        if (none != null) builder.setNeutralButton(none) { _, _ -> onPick(null) }
        dialog = builder.show()
    }

    /** Asks for a font size in points. */
    protected fun pickSize(onPick: (Float) -> Unit) {
        val sizes = floatArrayOf(8f, 9f, 10f, 11f, 12f, 14f, 16f, 18f, 20f, 24f, 28f, 32f, 36f, 40f, 48f, 60f, 72f)
        androidx.appcompat.app.AlertDialog.Builder(context).setTitle("Cỡ chữ")
            .setItems(sizes.map { (if (it % 1f == 0f) it.toInt().toString() else it.toString()) + " pt" }.toTypedArray()) { _, i -> onPick(sizes[i]) }
            .setNegativeButton("Hủy", null).show()
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
            if (state.status == com.wxiwei.office.reader.ReaderState.Status.Ready) then() else toast("Không mở lại được tài liệu")
        }
    }

    /** A new file for a working copy in the cache: edits live there until Save writes the original. */
    protected fun workingCopy(): File =
        File(File(context.cacheDir, "edit-work").apply { mkdirs() }, "work-" + System.nanoTime() + "." + file.extension)

    /** Called when the panel is hidden; stop listening to the document. Call super. */
    open fun close() {
        keepAboveKeyboard(false)
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
        if (!on) (view.parent as? View)?.translationY = 0f
    }

    private fun liftAboveKeyboard() {
        val container = view.parent as? View ?: return
        val insets = androidx.core.view.ViewCompat.getRootWindowInsets(container) ?: return
        val ime = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.ime()).bottom
        val location = IntArray(2)
        container.getLocationInWindow(location)
        val bottom = location[1] - container.translationY + container.height
        val lift = if (ime <= 0) 0f else maxOf(0f, bottom - (container.rootView.height - ime))
        if (container.translationY != -lift) {
            container.translationY = -lift
            onKeyboardMoved()
        }
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
