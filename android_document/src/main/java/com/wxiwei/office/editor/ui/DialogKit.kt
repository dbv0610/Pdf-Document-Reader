package com.wxiwei.office.editor.ui

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.text.InputType
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.editor.docsdk.DialogStyle

/**
 * Builds the dialogs from a description of their content, styled by [DialogStyle]:
 *
 * ```
 * DialogKit(context).show("Căn lề") {
 *     caption("Căn ngang"); val h = choices(listOf("Trái", "Giữa"), checked = 0)
 *     positive("Áp dụng") { apply(h.picked) }
 *     negative("Hủy")
 * }
 * ```
 */
class DialogKit(private val context: Context, val style: DialogStyle = DialogStyle.of(context)) {

    private fun dp(v: Number) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v.toFloat(), context.resources.displayMetrics).toInt()

    private val accentList get() = ColorStateList(arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()), intArrayOf(style.accent, style.caption))

    /** A group of radio buttons; [picked] is the index checked (-1: none). */
    class Choices internal constructor(val group: RadioGroup) {
        var picked: Int
            get() = (group.findViewById<View>(group.checkedRadioButtonId)?.tag as? Int) ?: -1
            set(value) { (0 until group.childCount).map { group.getChildAt(it) }.firstOrNull { it.tag == value }?.let { group.check(it.id) } }
        fun onChange(block: (Int) -> Unit) { group.setOnCheckedChangeListener { _, _ -> block(picked) } }
    }

    /** An action at the end of a [Form.row]: a symbol with a description for accessibility. */
    data class Action(val label: String, val description: String, val run: () -> Unit)

    /** The content of a dialog, top to bottom. */
    inner class Form internal constructor() {
        val root = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(style.paddingDp), dp(4), dp(style.paddingDp), dp(4))
        }
        internal var positive: Pair<String, () -> Unit>? = null
        internal var negative: Pair<String, () -> Unit>? = null
        internal var neutral: Pair<String, () -> Unit>? = null
        internal var keepOpen = false
        /** The dialog, once shown (rows that close it, content refreshed in place). */
        var dialog: AlertDialog? = null; internal set

        private fun <T : View> add(v: T): T { root.addView(v); return v }

        fun caption(text: String): TextView = add(TextView(context).apply {
            this.text = text
            setTextColor(style.caption); textSize = style.captionSp; typeface = Typeface.DEFAULT_BOLD
            setPadding(0, dp(12), 0, dp(4))
        })

        fun text(text: CharSequence): TextView = add(TextView(context).apply {
            this.text = text
            setTextColor(style.text); textSize = style.textSp; typeface = style.textTypeface
            setPadding(0, dp(6), 0, dp(6))
        })

        /** Radio buttons; [horizontal] ones scroll sideways when they do not fit. */
        fun choices(labels: List<String>, checked: Int = 0, horizontal: Boolean = false): Choices {
            val group = RadioGroup(context).apply {
                orientation = if (horizontal) RadioGroup.HORIZONTAL else RadioGroup.VERTICAL
                labels.forEachIndexed { i, t ->
                    addView(RadioButton(context).apply {
                        text = t; tag = i; id = View.generateViewId()
                        setTextColor(style.text); textSize = style.textSp
                        buttonTintList = accentList
                        isChecked = i == checked
                        if (horizontal) setPadding(paddingLeft, paddingTop, dp(10), paddingBottom)
                    })
                }
            }
            if (horizontal) add(HorizontalScrollView(context).apply { isHorizontalScrollBarEnabled = false; addView(group) }) else add(group)
            return Choices(group)
        }

        /** A text field; [numeric] takes a decimal number. */
        fun input(hint: String, value: String = "", numeric: Boolean = false): EditText = add(EditText(context).apply {
            this.hint = hint; setText(value); contentDescription = hint
            setTextColor(style.text); setHintTextColor(style.caption); textSize = style.textSp; typeface = style.textTypeface
            backgroundTintList = ColorStateList.valueOf(style.accent)
            if (numeric) inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            isSingleLine = true
            style.inputStyler?.style(this)
        })

        fun check(text: String, checked: Boolean = false): CheckBox = add(CheckBox(context).apply {
            this.text = text; isChecked = checked
            setTextColor(style.text); textSize = style.textSp
            buttonTintList = accentList
        })

        /** Rows to pick one of; a tap closes the dialog and runs [onPick] with the index. */
        fun items(labels: List<String>, onPick: (Int) -> Unit): List<TextView> =
            labels.mapIndexed { i, t -> add(rowView(t, false) { dialog?.dismiss(); onPick(i) }) }

        /** A row: [label] (tap: [onClick]), long press [onLongClick], and [actions] at its end. */
        fun row(label: String, bold: Boolean = false, onClick: (() -> Unit)? = null, onLongClick: (() -> Unit)? = null, actions: List<Action> = emptyList()): View {
            val line = LinearLayout(context).apply { gravity = Gravity.CENTER_VERTICAL }
            line.addView(rowView(label, bold, onClick).also { v ->
                onLongClick?.let { lc -> v.setOnLongClickListener { lc(); true } }
            }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            for (a in actions) line.addView(TextView(context).apply {
                text = a.label; contentDescription = a.description
                setTextColor(style.accent); textSize = style.textSp + 3
                setPadding(dp(12), dp(8), dp(12), dp(8))
                background = ripple()
                setOnClickListener { a.run() }
            })
            return add(line)
        }

        /** "−  n  +" for a whole number in [range]; the current value is read with the getter returned. */
        fun stepper(value: Int, range: IntRange, less: String, more: String): () -> Int {
            var n = value.coerceIn(range)
            val shown = TextView(context).apply {
                text = n.toString(); gravity = Gravity.CENTER; minWidth = dp(40)
                setTextColor(style.text); textSize = style.textSp + 1
            }
            fun button(label: String, description: String, by: Int) = TextView(context).apply {
                text = label; contentDescription = description; gravity = Gravity.CENTER
                setTextColor(style.accent); textSize = style.titleSp; minWidth = dp(48)
                setPadding(dp(8), dp(6), dp(8), dp(6))
                background = GradientDrawable().apply { setStroke(dp(1), style.divider); cornerRadius = dp(8).toFloat() }
                setOnClickListener { n = (n + by).coerceIn(range); shown.text = n.toString() }
            }
            add(LinearLayout(context).apply {
                gravity = Gravity.CENTER_VERTICAL
                addView(button("−", less, -1)); addView(shown); addView(button("+", more, 1))
            })
            return { n }
        }

        /** A thin line between parts. */
        fun divider(): View = add(View(context).apply { setBackgroundColor(style.divider) }).also {
            it.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(1)).apply { setMargins(0, dp(6), 0, dp(6)) }
        }

        /** Any view (a color grid, a preview...). */
        fun <T : View> view(v: T): T = add(v)

        /** Empties the content (to fill it again, e.g. after a change in a list). */
        fun clear() = root.removeAllViews()

        fun positive(label: String, run: () -> Unit = {}) { positive = label to run }
        fun negative(label: String = context.getString(android.R.string.cancel), run: () -> Unit = {}) { negative = label to run }
        fun neutral(label: String, run: () -> Unit = {}) { neutral = label to run }
        /** The buttons do not close the dialog (their actions run, it stays open; close it with [dialog]). */
        fun keepOpenOnButtons() { keepOpen = true }

        private fun rowView(label: String, bold: Boolean, onClick: (() -> Unit)?) = TextView(context).apply {
            text = label
            setTextColor(style.text); textSize = style.textSp
            typeface = if (bold) Typeface.DEFAULT_BOLD else style.textTypeface
            setPadding(dp(4), dp(style.rowPaddingDp), dp(4), dp(style.rowPaddingDp))
            if (onClick != null) { background = ripple(); setOnClickListener { onClick() } }
        }
    }

    private fun ripple() = RippleDrawable(ColorStateList.valueOf(style.rowPressed), null, ColorDrawable(0xFFFFFFFF.toInt()))

    /** Builds, styles and shows a dialog titled [title] with the content [build] describes. */
    fun show(title: String, scroll: Boolean = true, cancelable: Boolean = true, build: Form.() -> Unit): AlertDialog {
        val form = Form()
        form.build()
        val titleView = TextView(context).apply {
            text = title
            setTextColor(style.title); textSize = style.titleSp; typeface = style.titleTypeface
            setPadding(dp(style.paddingDp), dp(style.paddingDp), dp(style.paddingDp), dp(4))
        }
        val content: View = if (scroll) ScrollView(context).apply { addView(form.root) } else form.root
        val builder = AlertDialog.Builder(context).setCustomTitle(titleView).setView(content).setCancelable(cancelable)
        form.positive?.let { (label, run) -> builder.setPositiveButton(label) { _, _ -> if (!form.keepOpen) run() } }
        form.negative?.let { (label, run) -> builder.setNegativeButton(label) { _, _ -> if (!form.keepOpen) run() } }
        form.neutral?.let { (label, run) -> builder.setNeutralButton(label) { _, _ -> if (!form.keepOpen) run() } }
        val dialog = builder.create()
        form.dialog = dialog
        dialog.setOnShowListener {
            dialog.window?.setBackgroundDrawable(GradientDrawable().apply { setColor(style.background); cornerRadius = dp(style.cornerDp).toFloat() })
            for (which in listOf(AlertDialog.BUTTON_POSITIVE, AlertDialog.BUTTON_NEGATIVE, AlertDialog.BUTTON_NEUTRAL)) {
                dialog.getButton(which)?.apply { setTextColor(style.accent); isAllCaps = false; textSize = style.textSp }
            }
            if (form.keepOpen) {
                form.positive?.let { (_, run) -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setOnClickListener { run() } }
                form.negative?.let { (_, run) -> dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setOnClickListener { run() } }
                form.neutral?.let { (_, run) -> dialog.getButton(AlertDialog.BUTTON_NEUTRAL)?.setOnClickListener { run() } }
            }
        }
        dialog.show()
        return dialog
    }

    /** A question with two answers. */
    fun confirm(title: String, message: CharSequence, yes: String, no: String = context.getString(android.R.string.cancel), onYes: () -> Unit): AlertDialog =
        show(title) { text(message); positive(yes, onYes); negative(no) }

    /** A list to pick from, closing on a tap. */
    fun pick(title: String, labels: List<String>, cancel: String? = context.getString(android.R.string.cancel), onPick: (Int) -> Unit): AlertDialog =
        show(title) { items(labels, onPick); cancel?.let { negative(it) } }
}
