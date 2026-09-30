/*
 * Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * Proprietary and confidential. Unauthorized copying, modification or distribution of this
 * file, via any medium, is strictly prohibited without the written permission of dongb2002.
 */
package com.editor.docsdk


import android.widget.EditText

/**
 * Changes a text field of the SDK further (background, padding, cursor...) once its colors are set.
 * Give one to [EditStyle.inputStyler] for the fields of the edit bars, to [DialogStyle.inputStyler]
 * for those of the dialogs. Do not change its text, listeners or input type.
 */
fun interface InputStyler {
    fun style(input: EditText)
}
