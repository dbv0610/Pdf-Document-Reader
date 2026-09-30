/*
 * Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * Proprietary and confidential. Unauthorized copying, modification or distribution of this
 * file, via any medium, is strictly prohibited without the written permission of dongb2002.
 */
package com.editor.docsdk

import android.content.Context
import android.graphics.Typeface
import androidx.core.content.ContextCompat
import com.wxiwei.office.R

/**
 * The look of every dialog of the edit bars and the slideshow. The defaults follow the light or
 * dark theme of the device; set [customizer] once (for example in `Application.onCreate`) to
 * change them:
 *
 * ```
 * DialogStyle.customizer = DialogStyle.Customizer { context, style ->
 *     style.copy(accent = ContextCompat.getColor(context, R.color.brand), cornerDp = 8f)
 * }
 * ```
 */
data class DialogStyle @JvmOverloads constructor(
    /** Color of the dialog window. */
    val background: Int,
    val title: Int,
    val text: Int,
    /** Section captions, hints and unchecked boxes. */
    val caption: Int,
    /** Buttons, checked boxes and choices, the progress bar. */
    val accent: Int,
    val divider: Int,
    /** Ripple of a pressed row. */
    val rowPressed: Int,
    val cornerDp: Float = 16f,
    val titleSp: Float = 18f,
    val textSp: Float = 15f,
    val captionSp: Float = 13f,
    val paddingDp: Int = 20,
    val rowPaddingDp: Int = 12,
    val titleTypeface: Typeface = Typeface.DEFAULT_BOLD,
    val textTypeface: Typeface = Typeface.DEFAULT,
    /** Called on every text field of the dialogs once it is made; null: nothing more. */
    val inputStyler: InputStyler? = null,
) {
    /** Changes [style], the default of [context], into the style used; called for every dialog. */
    fun interface Customizer {
        fun customize(context: Context, style: DialogStyle): DialogStyle
    }

    companion object {
        /** Changes the style of every dialog; null keeps the defaults. */
        @JvmStatic
        var customizer: Customizer? = null

        /** The style without [customizer]: light or dark as [context] is. */
        @JvmStatic
        fun defaults(context: Context): DialogStyle = DialogStyle(
            background = ContextCompat.getColor(context, R.color.docsdk_dialog_background),
            title = ContextCompat.getColor(context, R.color.docsdk_dialog_title),
            text = ContextCompat.getColor(context, R.color.docsdk_dialog_text),
            caption = ContextCompat.getColor(context, R.color.docsdk_dialog_caption),
            accent = ContextCompat.getColor(context, R.color.docsdk_dialog_accent),
            divider = ContextCompat.getColor(context, R.color.docsdk_dialog_divider),
            rowPressed = ContextCompat.getColor(context, R.color.docsdk_dialog_row_pressed),
        )

        /** The style the dialogs of [context] use. */
        @JvmStatic
        fun of(context: Context): DialogStyle = defaults(context).let { customizer?.customize(context, it) ?: it }
    }
}
