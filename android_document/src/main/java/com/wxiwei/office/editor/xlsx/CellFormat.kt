/*
 * Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * Proprietary and confidential. Unauthorized copying, modification or distribution of this
 * file, via any medium, is strictly prohibited without the written permission of dongb2002.
 */
package com.wxiwei.office.editor.xlsx

/**
 * A formatting change for cells. Null fields keep the cell's current value, so formats applied one
 * after the other merge with [plus]. Colors are "RRGGBB"; [fillColor] "none" removes the fill.
 * [horizontal]: general, left, center, right, justify; [vertical]: top, center, bottom.
 * [numberFormat] is an Excel format code such as "0.00", "#,##0", "d/m/yyyy" or "0%".
 * [rotation] turns the text (textRotation): 1-90 up, 91-180 down by rotation - 90, 255 stacked, 0 flat.
 * [border]: none, all (thin, 4 sides), thick (4 sides), bottom (thin); [borderColor] RRGGBB (black when null).
 */
data class CellFormat(
    val bold: Boolean? = null,
    val italic: Boolean? = null,
    val underline: Boolean? = null,
    val strike: Boolean? = null,
    val fontSize: Double? = null,
    val fontColor: String? = null,
    val fillColor: String? = null,
    val horizontal: String? = null,
    val vertical: String? = null,
    val wrap: Boolean? = null,
    val numberFormat: String? = null,
    val rotation: Int? = null,
    val border: String? = null,
    val borderColor: String? = null,
    /** Indent level (each about 3 characters wide) for left/right/distributed text, 0..15. */
    val indent: Int? = null,
) {
    /** [other]'s set fields win. */
    operator fun plus(other: CellFormat) = CellFormat(
        other.bold ?: bold, other.italic ?: italic, other.underline ?: underline, other.strike ?: strike,
        other.fontSize ?: fontSize, other.fontColor ?: fontColor, other.fillColor ?: fillColor,
        other.horizontal ?: horizontal, other.vertical ?: vertical, other.wrap ?: wrap,
        other.numberFormat ?: numberFormat, other.rotation ?: rotation,
        other.border ?: border, if (other.border != null) other.borderColor else borderColor,
        other.indent ?: indent,
    )

    val changesFont get() = bold != null || italic != null || underline != null || strike != null || fontSize != null || fontColor != null
    val changesAlignment get() = horizontal != null || vertical != null || wrap != null || rotation != null || indent != null

    /** Checks values; returns an error message or null. */
    fun validate(): String? {
        val hex = Regex("(?i)[0-9a-f]{6}")
        if (fontColor != null && !fontColor.removePrefix("#").matches(hex)) return "fontColor must be RRGGBB"
        if (fillColor != null && fillColor != "none" && !fillColor.removePrefix("#").matches(hex)) return "fillColor must be RRGGBB or none"
        if (horizontal != null && horizontal !in HORIZONTAL) return "bad horizontal alignment"
        if (vertical != null && vertical !in VERTICAL) return "bad vertical alignment"
        if (fontSize != null && (fontSize < 1 || fontSize > 409)) return "fontSize must be 1..409"
        if (numberFormat != null && numberFormat.isBlank()) return "empty number format"
        if (rotation != null && rotation !in 0..180 && rotation != 255) return "rotation must be 0..180 or 255"
        if (border != null && border !in setOf("none", "all", "thick", "bottom")) return "bad border"
        if (borderColor != null && !borderColor.removePrefix("#").matches(hex)) return "borderColor must be RRGGBB"
        if (indent != null && indent !in 0..15) return "indent must be 0..15"
        return null
    }

    companion object {
        /** `horizontal` values of OOXML `<alignment>`. */
        val HORIZONTAL = setOf("general", "left", "center", "right", "fill", "justify", "centerContinuous", "distributed")
        val VERTICAL = setOf("top", "center", "bottom", "justify", "distributed")
    }
}
