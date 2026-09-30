/*
 * Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * Proprietary and confidential. Unauthorized copying, modification or distribution of this
 * file, via any medium, is strictly prohibited without the written permission of dongb2002.
 */
package com.reader.pdfviewer.pdfium

import android.graphics.RectF

/**
 * A field of a PDF form on a viewer [page]: [index] of its widget annotation, [rect] in page
 * points (top > bottom), [type] one of the TYPE_ values, [options] of a combo box or list.
 */
data class PdfFormField(
    val page: Int,
    val index: Int,
    val type: Int,
    val flags: Int,
    val checked: Boolean,
    val rect: RectF,
    val name: String,
    val value: String,
    val options: List<String>,
) {
    val readOnly: Boolean get() = flags and FLAG_READONLY != 0
    /** A text field whose value spans several lines. */
    val multiline: Boolean get() = type == TYPE_TEXT && flags and FLAG_MULTILINE != 0

    companion object {
        // FPDF_FORMFIELD_* of fpdf_formfill.h
        const val TYPE_PUSHBUTTON = 1
        const val TYPE_CHECKBOX = 2
        const val TYPE_RADIOBUTTON = 3
        const val TYPE_COMBOBOX = 4
        const val TYPE_LISTBOX = 5
        const val TYPE_TEXT = 6
        const val TYPE_SIGNATURE = 7
        private const val FLAG_READONLY = 1
        private const val FLAG_MULTILINE = 1 shl 12

        /** Fields as nativeGetFormFields lists them: a head "index type flags checked l t r b options", the name, the value, the options. */
        internal fun parse(page: Int, parts: Array<String>): List<PdfFormField> {
            val fields = ArrayList<PdfFormField>()
            var i = 0
            while (i + 2 < parts.size) {
                val h = parts[i].split(' ')
                if (h.size < 9) break
                val options = h[8].toIntOrNull() ?: 0
                if (i + 3 + options > parts.size) break
                fields += PdfFormField(page, h[0].toInt(), h[1].toInt(), h[2].toInt(), h[3] == "1",
                    RectF(h[4].toFloat(), h[5].toFloat(), h[6].toFloat(), h[7].toFloat()),
                    parts[i + 1], parts[i + 2], parts.copyOfRange(i + 3, i + 3 + options).toList())
                i += 3 + options
            }
            return fields
        }
    }
}
