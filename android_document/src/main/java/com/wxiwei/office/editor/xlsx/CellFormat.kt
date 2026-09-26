package com.wxiwei.office.editor.xlsx

/**
 * A formatting change for cells. Null fields keep the cell's current value, so formats applied one
 * after the other merge with [plus]. Colors are "RRGGBB"; [fillColor] "none" removes the fill.
 * [horizontal]: general, left, center, right; [vertical]: top, center, bottom.
 * [numberFormat] is an Excel format code such as "0.00", "#,##0", "d/m/yyyy" or "0%".
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
) {
    /** [other]'s set fields win. */
    operator fun plus(other: CellFormat) = CellFormat(
        other.bold ?: bold, other.italic ?: italic, other.underline ?: underline, other.strike ?: strike,
        other.fontSize ?: fontSize, other.fontColor ?: fontColor, other.fillColor ?: fillColor,
        other.horizontal ?: horizontal, other.vertical ?: vertical, other.wrap ?: wrap,
        other.numberFormat ?: numberFormat,
    )

    val changesFont get() = bold != null || italic != null || underline != null || strike != null || fontSize != null || fontColor != null
    val changesAlignment get() = horizontal != null || vertical != null || wrap != null

    /** Checks values; returns an error message or null. */
    fun validate(): String? {
        val hex = Regex("(?i)[0-9a-f]{6}")
        if (fontColor != null && !fontColor.removePrefix("#").matches(hex)) return "fontColor must be RRGGBB"
        if (fillColor != null && fillColor != "none" && !fillColor.removePrefix("#").matches(hex)) return "fillColor must be RRGGBB or none"
        if (horizontal != null && horizontal !in setOf("general", "left", "center", "right")) return "bad horizontal alignment"
        if (vertical != null && vertical !in setOf("top", "center", "bottom")) return "bad vertical alignment"
        if (fontSize != null && (fontSize < 1 || fontSize > 409)) return "fontSize must be 1..409"
        if (numberFormat != null && numberFormat.isBlank()) return "empty number format"
        return null
    }
}
