package com.reader.pdfviewer

/**
 * Text markup annotations that can be added to the selected text
 */
enum class TextMarkupType(internal val annotSubtype: Int) {
    // Values of FPDF_ANNOT_UNDERLINE and FPDF_ANNOT_STRIKEOUT in fpdf_annot.h
    HIGHLIGHT(9),
    UNDERLINE(10),
    STRIKETHROUGH(12),
}
