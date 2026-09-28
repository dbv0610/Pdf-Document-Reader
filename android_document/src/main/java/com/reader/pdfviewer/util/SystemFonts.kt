package com.reader.pdfviewer.util

import java.io.File

/** Fonts of the system that text written into a PDF can use. */
object SystemFonts {
    /** A font with Latin, Vietnamese and more, or null (pdfium then falls back to Helvetica). */
    fun unicode(): String? = listOf("/system/fonts/Roboto-Regular.ttf", "/system/fonts/NotoSans-Regular.ttf",
        "/system/fonts/DroidSans.ttf").firstOrNull { File(it).canRead() }
}
