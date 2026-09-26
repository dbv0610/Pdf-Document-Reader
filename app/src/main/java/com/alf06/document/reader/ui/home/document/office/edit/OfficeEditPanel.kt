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

    /** Called when the panel is hidden; stop listening to the document. */
    open fun close() {}

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
