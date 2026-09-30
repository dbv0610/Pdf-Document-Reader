/*
 * Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * Proprietary and confidential. Unauthorized copying, modification or distribution of this
 * file, via any medium, is strictly prohibited without the written permission of dongb2002.
 */
package com.editor.docsdk

import com.reader.pdfviewer.pdfium.PdfPasswordException
import com.reader.pdfviewer.tools.PdfSourceException
import com.wxiwei.office.system.OpenFileException
import java.io.FileNotFoundException

/** Why a document could not be opened or processed; [reason] tells what to show the user. */
class DocumentException(val reason: Reason, message: String? = null, cause: Throwable? = null) :
    Exception(message ?: reason.name, cause) {

    enum class Reason {
        /** The document is protected: open it again with a password. */
        PASSWORD_REQUIRED,
        /** The password given does not open the document. */
        PASSWORD_INCORRECT,
        /** Not a type the SDK opens (see [DocumentType]), or an old format it cannot read. */
        UNSUPPORTED,
        /** The file is damaged or is not what its name says. */
        DAMAGED,
        /** The file or the Uri cannot be found or read. */
        NOT_FOUND,
        /** The device has not enough memory for this document. */
        OUT_OF_MEMORY,
        /** Storage could not be written (full, or not allowed). */
        STORAGE,
        /** Anything else; see [cause]. */
        UNKNOWN,
    }

    internal companion object {
        /** A [DocumentException] for any failure of the engines, [hadPassword] telling a wrong password from a missing one. */
        fun from(error: Throwable?, hadPassword: Boolean = false): DocumentException {
            if (error is DocumentException) return error
            val chain = generateSequence(error) { it.cause }.take(10).toList()
            chain.filterIsInstance<OpenFileException>().firstOrNull()?.let { e ->
                val reason = when (e.reason) {
                    OpenFileException.Reason.PASSWORD_REQUIRED -> Reason.PASSWORD_REQUIRED
                    OpenFileException.Reason.PASSWORD_INCORRECT -> Reason.PASSWORD_INCORRECT
                    OpenFileException.Reason.BAD_FILE -> Reason.DAMAGED
                    OpenFileException.Reason.RTF_DOCUMENT, OpenFileException.Reason.OLD_DOCUMENT -> Reason.UNSUPPORTED
                    OpenFileException.Reason.OUT_OF_MEMORY -> Reason.OUT_OF_MEMORY
                    OpenFileException.Reason.FILE_NOT_FOUND -> Reason.NOT_FOUND
                    OpenFileException.Reason.STORAGE -> Reason.STORAGE
                    OpenFileException.Reason.UNKNOWN -> Reason.UNKNOWN
                }
                return DocumentException(reason, e.message, e)
            }
            if (chain.any { it is PdfPasswordException }) {
                return DocumentException(if (hadPassword) Reason.PASSWORD_INCORRECT else Reason.PASSWORD_REQUIRED, error?.message, error)
            }
            return when {
                chain.any { it is OutOfMemoryError } -> DocumentException(Reason.OUT_OF_MEMORY, error?.message, error)
                chain.any { it is FileNotFoundException || it is SecurityException } -> DocumentException(Reason.NOT_FOUND, error?.message, error)
                chain.any { it is PdfSourceException } -> DocumentException(Reason.DAMAGED, error?.message, error)
                else -> DocumentException(Reason.UNKNOWN, error?.message, error)
            }
        }
    }
}
