package com.wxiwei.office.editor

import java.io.File
import java.io.IOException

sealed class EditResult {
    data class Ok(val file: File, val warnings: List<String> = emptyList()) : EditResult()
    data class Error(val reason: Reason, val message: String, val cause: Throwable? = null) : EditResult()
}

enum class Reason { IO, UNSUPPORTED_FORMAT, ENCRYPTED, MAP_MISMATCH, NOT_FOUND, INVALID_ARGUMENT, INTERNAL }

inline fun runEdit(block: () -> EditResult): EditResult = try {
    block()
} catch (e: IOException) {
    EditResult.Error(Reason.IO, e.message ?: "I/O error", e)
} catch (e: Exception) {
    EditResult.Error(Reason.INTERNAL, e.message ?: "Edit failed", e)
}
