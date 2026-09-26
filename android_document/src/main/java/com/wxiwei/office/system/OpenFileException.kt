package com.wxiwei.office.system

import com.wxiwei.office.fc.OldFileFormatException
import com.wxiwei.office.fc.poifs.filesystem.OfficeXmlFileException
import java.io.EOFException
import java.io.File
import java.io.FileNotFoundException
import java.util.concurrent.CancellationException
import java.util.zip.ZipException

class OpenFileException(
    val reason: Reason,
    val filePath: String?,
    cause: Throwable
) : Exception(cause.message, cause) {
    enum class Reason {
        BAD_FILE, RTF_DOCUMENT, OLD_DOCUMENT, PASSWORD_REQUIRED, PASSWORD_INCORRECT,
        OUT_OF_MEMORY, FILE_NOT_FOUND, STORAGE, UNKNOWN
    }
}

object OpenFileErrors {
    internal data class Classification(
        val reason: OpenFileException.Reason,
        val resource: String? = null,
        val errorCode: Int = ErrorUtil.SYSTEM_CRASH,
        val message: String = ""
    )

    @JvmStatic
    fun classify(ex: Throwable): OpenFileException.Reason = details(ex).reason

    @JvmStatic
    fun wrap(ex: Throwable, filePath: String?): OpenFileException =
        if (ex is OpenFileException) ex else OpenFileException(classify(ex), filePath, ex)

    fun isCancellation(ex: Throwable): Boolean = causes(ex).any {
        it is AbortReaderError || it is CancellationException
    }

    /** Also used before TXT preflight, which runs before the reader factory. */
    fun requireReadable(filePath: String?) {
        val file = filePath?.takeIf { it.isNotBlank() }?.let(::File)
        try {
            if (file == null || !file.exists() || !file.isFile || !file.canRead()) {
                throw FileNotFoundException("File missing or unreadable: $filePath")
            }
        } catch (ex: SecurityException) {
            throw FileNotFoundException("File unreadable: $filePath").apply { initCause(ex) }
        }
    }

    private fun causes(ex: Throwable): List<Throwable> {
        val result = mutableListOf<Throwable>()
        var current: Throwable? = ex
        while (current != null && result.none { it === current }) {
            result.add(current)
            current = current.cause
        }
        return result
    }

    internal fun details(ex: Throwable, isReaderFile: Boolean = false, debug: Boolean = false): Classification {
        for (cause in causes(ex)) {
            val detail = classifyDirect(cause)
            if (detail.reason != OpenFileException.Reason.UNKNOWN) return detail
        }
        return when {
            isReaderFile -> Classification(OpenFileException.Reason.UNKNOWN, "DIALOG_PARSE_ERROR", ErrorUtil.PARSE_ERROR)
            ex is IllegalArgumentException -> Classification(OpenFileException.Reason.UNKNOWN, errorCode = ErrorUtil.SYSTEM_PERMISSION, message = ex.message ?: "")
            ex is NullPointerException || ex is ClassCastException || debug -> Classification(OpenFileException.Reason.UNKNOWN, "DIALOG_SYSTEM_CRASH", ErrorUtil.SYSTEM_CRASH)
            else -> Classification(OpenFileException.Reason.UNKNOWN)
        }
    }

    private fun classifyDirect(ex: Throwable): Classification {
        val error = ex.toString()
        return when {
            ex is OpenFileException -> when (ex.reason) {
                OpenFileException.Reason.BAD_FILE -> Classification(ex.reason, "DIALOG_FORMAT_ERROR", ErrorUtil.BAD_FILE)
                OpenFileException.Reason.RTF_DOCUMENT -> Classification(ex.reason, "DIALOG_RTF_FILE", ErrorUtil.RTF_DOCUMENT)
                OpenFileException.Reason.OLD_DOCUMENT -> Classification(ex.reason, "DIALOG_OLD_DOCUMENT", ErrorUtil.OLD_DOCUMENT)
                OpenFileException.Reason.PASSWORD_REQUIRED -> Classification(ex.reason, "DIALOG_CANNOT_ENCRYPTED_FILE", ErrorUtil.PASSWORD_DOCUMENT)
                OpenFileException.Reason.PASSWORD_INCORRECT -> Classification(ex.reason, "DIALOG_PASSWORD_INCORRECT", ErrorUtil.PASSWORD_INCORRECT)
                OpenFileException.Reason.OUT_OF_MEMORY -> Classification(ex.reason, "DIALOG_INSUFFICIENT_MEMORY", ErrorUtil.INSUFFICIENT_MEMORY)
                OpenFileException.Reason.FILE_NOT_FOUND -> Classification(ex.reason, "DIALOG_PARSE_ERROR", ErrorUtil.PARSE_ERROR)
                OpenFileException.Reason.STORAGE -> ex.cause?.let { details(it) }?.takeIf { it.reason == ex.reason }
                    ?: Classification(ex.reason, "SD_CARD", ErrorUtil.SD_CARD_ERROR)
                OpenFileException.Reason.UNKNOWN -> Classification(ex.reason)
            }
            ex is BadFileFormatException -> Classification(OpenFileException.Reason.BAD_FILE, "DIALOG_FORMAT_ERROR", ErrorUtil.BAD_FILE)
            ex is FileNotFoundException -> Classification(OpenFileException.Reason.FILE_NOT_FOUND, "DIALOG_PARSE_ERROR", ErrorUtil.PARSE_ERROR)
            error.contains("SD") -> Classification(OpenFileException.Reason.STORAGE, "SD_CARD", ErrorUtil.SD_CARD_ERROR)
            error.contains("Write Permission denied") -> Classification(OpenFileException.Reason.STORAGE, "SD_CARD_WRITEDENIED", ErrorUtil.SD_CARD_WRITEDENIED)
            error.contains("No space left on device") -> Classification(OpenFileException.Reason.STORAGE, "SD_CARD_NOSPACELEFT", ErrorUtil.SD_CARD_NOSPACELEFT)
            ex is OutOfMemoryError || error.contains("OutOfMemoryError") -> Classification(OpenFileException.Reason.OUT_OF_MEMORY, "DIALOG_INSUFFICIENT_MEMORY", ErrorUtil.INSUFFICIENT_MEMORY)
            ex is EOFException || ex is ZipException || error.contains("no such entry") || error.contains("Format error") || error.contains("Unable to read entire header") || ex is OfficeXmlFileException || error.contains("The text piece table is corrupted") || error.contains("Invalid header signature") -> Classification(OpenFileException.Reason.BAD_FILE, "DIALOG_FORMAT_ERROR", ErrorUtil.BAD_FILE)
            error.contains("The document is really a RTF file") -> Classification(OpenFileException.Reason.RTF_DOCUMENT, "DIALOG_RTF_FILE", ErrorUtil.RTF_DOCUMENT)
            ex is OldFileFormatException -> Classification(OpenFileException.Reason.OLD_DOCUMENT, "DIALOG_OLD_DOCUMENT", ErrorUtil.OLD_DOCUMENT)
            error.contains("Cannot process encrypted office file") || error.contains("Document with password") -> Classification(OpenFileException.Reason.PASSWORD_REQUIRED, "DIALOG_CANNOT_ENCRYPTED_FILE", ErrorUtil.PASSWORD_DOCUMENT)
            error.contains("Password is incorrect") -> Classification(OpenFileException.Reason.PASSWORD_INCORRECT, "DIALOG_PASSWORD_INCORRECT", ErrorUtil.PASSWORD_INCORRECT)
            else -> Classification(OpenFileException.Reason.UNKNOWN)
        }
    }
}
