/*
 * Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * Proprietary and confidential. Unauthorized copying, modification or distribution of this
 * file, via any medium, is strictly prohibited without the written permission of dongb2002.
 */
package com.wxiwei.office.editor.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.HorizontalScrollView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.editor.docsdk.EditAction
import com.editor.docsdk.EditStyle

/**
 * The format bar shown over the keyboard while the text of a PowerPoint shape is typed in place:
 * a button per action of [actions] (FONT shows the font's name, FONT_SIZE shows − size +), each
 * selected ([View.isSelected]) when [state] says it is on. Colors, icons and sizes come from
 * [style] (textBar* and the bar's colors); its item styler is applied to every button once, and
 * what it sets stays: being on only changes the selected state, which the SDK's background and
 * tint follow. A button runs [onAction] with the action and, for the size steps, the new size in points.
 */
@SuppressLint("ViewConstructor")
internal class SlideTextBar(
    context: Context,
    private val style: EditStyle,
    private val actions: List<EditAction>,
    private val onAction: (EditAction, Any?) -> Unit,
) : HorizontalScrollView(context) {

    /** What the selected text has now, to light the buttons. */
    class State(
        val bold: Boolean = false, val italic: Boolean = false, val underline: Boolean = false,
        val font: String? = null, val sizePt: Float? = null, val color: Int? = null, val align: String? = null,
    )

    private fun dp(v: Float) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, resources.displayMetrics).toInt()

    private val buttonPx = dp(style.textBarButtonDp.toFloat())
    private val iconPx = dp(style.textBarIconDp.toFloat()).coerceAtMost(buttonPx)
    private val gapPx = dp(style.textBarSpacingDp.toFloat())

    private val row = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(gapPx, 0, gapPx, 0)
    }
    private val buttons = HashMap<EditAction, View>()
    private var fontName: TextView? = null
    private var sizeText: TextView? = null
    private var colorBar: View? = null
    private var size = 18f

    init {
        isHorizontalScrollBarEnabled = false
        isFillViewport = true
        setBackgroundColor(style.textBarBackground ?: style.toolbarBackground)
        elevation = dp(4f).toFloat()
        addView(row, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT))
        for (action in actions) {
            when (action) {
                EditAction.FONT -> add(fontButton())
                EditAction.FONT_SIZE -> sizeControl()
                EditAction.TEXT_COLOR -> add(colorButton())
                else -> add(iconButton(action))
            }
        }
    }

    /** Adds [v] to the row, [gapPx] after the one before; its size is what it (or the item styler) set. */
    private fun add(v: View) {
        val lp = v.layoutParams as? LinearLayout.LayoutParams ?: LinearLayout.LayoutParams(buttonPx, buttonPx)
        if (row.childCount > 0) lp.marginStart = gapPx
        row.addView(v, lp)
    }

    private fun icon(action: EditAction) = style.icons?.icon(action) ?: action.icon

    /** Round background of a button that is on, nothing otherwise. */
    private fun onBackground() = StateListDrawable().apply {
        addState(intArrayOf(android.R.attr.state_selected), GradientDrawable().apply { cornerRadius = dp(8f).toFloat(); setColor(style.activeBackground) })
    }

    private fun tint() = ColorStateList(arrayOf(intArrayOf(android.R.attr.state_selected), intArrayOf()), intArrayOf(style.iconActive, style.icon))

    private fun iconButton(action: EditAction): View = ImageButton(context).apply {
        setImageResource(icon(action))
        imageTintList = tint()
        scaleType = ImageView.ScaleType.FIT_CENTER
        val pad = (buttonPx - iconPx) / 2
        setPadding(pad, pad, pad, pad)
        background = onBackground()
        contentDescription = context.getString(action.label)
        tag = action.name
        layoutParams = LinearLayout.LayoutParams(buttonPx, buttonPx)
        setOnClickListener { onAction(action, null) }
        buttons[action] = this
        style.itemStyler?.style(this, action)
    }

    /** The text color icon over a bar of the selected text's color. */
    private fun colorButton(): View {
        val box = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            contentDescription = context.getString(EditAction.TEXT_COLOR.label)
            tag = EditAction.TEXT_COLOR.name
            layoutParams = LinearLayout.LayoutParams(buttonPx, buttonPx)
            setOnClickListener { onAction(EditAction.TEXT_COLOR, null) }
        }
        box.addView(ImageView(context).apply {
            setImageResource(icon(EditAction.TEXT_COLOR))
            imageTintList = ColorStateList.valueOf(style.icon)
            layoutParams = LinearLayout.LayoutParams(iconPx, iconPx)
        })
        colorBar = View(context).apply { layoutParams = LinearLayout.LayoutParams(iconPx * 3 / 4, dp(3f)) }
        box.addView(colorBar)
        buttons[EditAction.TEXT_COLOR] = box
        style.itemStyler?.style(box, EditAction.TEXT_COLOR)
        return box
    }

    /** The font's name, tap to pick another. */
    private fun fontButton(): View = TextView(context).apply {
        text = context.getString(EditAction.FONT.label)
        setTextColor(style.icon)
        typeface = style.typeface
        textSize = style.tabTextSp
        maxLines = 1
        ellipsize = TextUtils.TruncateAt.END
        maxWidth = dp(120f)
        gravity = Gravity.CENTER_VERTICAL
        setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, com.wxiwei.office.R.drawable.docsdk_ic_expand_more, 0)
        compoundDrawableTintList = ColorStateList.valueOf(style.icon)
        setPadding(dp(10f), 0, dp(4f), 0)
        background = GradientDrawable().apply { cornerRadius = dp(6f).toFloat(); setStroke(dp(1f), style.divider) }
        contentDescription = context.getString(EditAction.FONT.label)
        tag = EditAction.FONT.name
        layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, buttonPx)
        setOnClickListener { onAction(EditAction.FONT, null) }
        fontName = this
        buttons[EditAction.FONT] = this
        style.itemStyler?.style(this, EditAction.FONT)
    }

    /** − size +: a step down or up through the usual sizes; the size itself asks for any size. */
    private fun sizeControl() {
        fun step(label: String, up: Boolean) = TextView(context).apply {
            text = label
            setTextColor(style.icon)
            textSize = style.tabTextSp + 4f
            gravity = Gravity.CENTER
            contentDescription = context.getString(EditAction.FONT_SIZE.label) + " " + label
            layoutParams = LinearLayout.LayoutParams(buttonPx, buttonPx)
            setOnClickListener { onAction(EditAction.FONT_SIZE, nextSize(size, up)) }
        }
        add(step("−", false))
        sizeText = TextView(context).apply {
            setTextColor(style.icon)
            typeface = style.typeface
            textSize = style.tabTextSp
            gravity = Gravity.CENTER
            contentDescription = context.getString(EditAction.FONT_SIZE.label)
            tag = EditAction.FONT_SIZE.name
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, buttonPx)
            minWidth = buttonPx
            setOnClickListener { onAction(EditAction.FONT_SIZE, null) }
        }
        buttons[EditAction.FONT_SIZE] = sizeText!!
        style.itemStyler?.style(sizeText!!, EditAction.FONT_SIZE)
        add(sizeText!!)
        add(step("+", true))
    }

    /** Lights the buttons for what the selected text has. */
    fun show(state: State) {
        fun lit(action: EditAction, on: Boolean) {
            buttons[action]?.isSelected = on
        }
        lit(EditAction.BOLD, state.bold)
        lit(EditAction.ITALIC, state.italic)
        lit(EditAction.UNDERLINE, state.underline)
        lit(EditAction.ALIGN_LEFT, state.align == "l")
        lit(EditAction.ALIGN_CENTER, state.align == "ctr")
        lit(EditAction.ALIGN_RIGHT, state.align == "r")
        fontName?.text = state.font ?: context.getString(EditAction.FONT.label)
        state.sizePt?.let { size = it }
        sizeText?.text = if (size % 1f == 0f) size.toInt().toString() else "%.1f".format(size)
        colorBar?.setBackgroundColor(state.color ?: style.icon)
    }

    private companion object {
        val SIZES = listOf(8f, 9f, 10f, 11f, 12f, 14f, 16f, 18f, 20f, 24f, 28f, 32f, 36f, 40f, 44f, 48f, 54f, 60f, 66f, 72f, 80f, 88f, 96f)

        fun nextSize(now: Float, up: Boolean): Float =
            if (up) SIZES.firstOrNull { it > now + 0.01f } ?: (now + 8f)
            else SIZES.lastOrNull { it < now - 0.01f } ?: maxOf(1f, now - 1f)
    }
}
