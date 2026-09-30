/*
 * Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * Proprietary and confidential. Unauthorized copying, modification or distribution of this
 * file, via any medium, is strictly prohibited without the written permission of dongb2002.
 */
package com.editor.docsdk

import java.util.Locale

/** The kinds of documents the SDK opens. */
enum class DocumentType(internal vararg val extensions: String) {
    PDF("pdf"),
    WORD("docx", "doc", "dotx", "dot", "docm"),
    EXCEL("xlsx", "xls", "xltx", "xlt", "xlsm"),
    POWERPOINT("pptx", "ppt", "potx", "pot", "ppsx", "pps", "pptm"),
    TEXT("txt");

    companion object {
        /** The type of a file named [fileName] (only its extension counts), or null when the SDK cannot open it. */
        @JvmStatic
        fun fromFileName(fileName: String?): DocumentType? {
            val extension = fileName?.substringAfterLast('.', "")?.lowercase(Locale.ROOT) ?: return null
            return entries.firstOrNull { extension in it.extensions }
        }

        /** The type for a MIME type such as "application/pdf", or null when it is not one the SDK opens. */
        @JvmStatic
        fun fromMimeType(mimeType: String?): DocumentType? = when (mimeType?.lowercase(Locale.ROOT)) {
            null -> null
            "application/pdf" -> PDF
            "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.template" -> WORD
            "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.template" -> EXCEL
            "application/vnd.ms-powerpoint", "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "application/vnd.openxmlformats-officedocument.presentationml.slideshow",
            "application/vnd.openxmlformats-officedocument.presentationml.template" -> POWERPOINT
            "text/plain" -> TEXT
            else -> null
        }
    }
}
