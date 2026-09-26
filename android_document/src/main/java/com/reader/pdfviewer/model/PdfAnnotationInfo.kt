package com.reader.pdfviewer.model

import android.graphics.RectF

/** Annotation bounds in PDF coordinates (top > bottom); page is the viewer page index. */
data class PdfAnnotationInfo(val page: Int, val index: Int, val subtype: Int, val rect: RectF, val name: String?)
