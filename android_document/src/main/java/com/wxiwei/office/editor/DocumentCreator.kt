package com.wxiwei.office.editor

import android.content.Context
import androidx.annotation.WorkerThread
import java.io.File
import java.nio.file.FileAlreadyExistsException
import java.nio.file.Files

/** Creates editable Office documents from the bundled, content-free templates. */
object DocumentCreator {
    enum class Format(val extension: String) {
        WORD("docx"), EXCEL("xlsx"), POWERPOINT("pptx")
    }

    /** Writes a complete package before publishing it. Never replaces an existing file. */
    @WorkerThread
    fun create(context: Context, format: Format, target: File): EditResult {
        if (!target.extension.equals(format.extension, ignoreCase = true)) {
            return EditResult.Error(Reason.INVALID_ARGUMENT, "Expected .${format.extension}")
        }
        if (target.exists()) return EditResult.Error(Reason.INVALID_ARGUMENT, "File already exists")
        return runEdit {
            val parent = target.absoluteFile.parentFile!!
            if (!parent.isDirectory && !parent.mkdirs()) {
                return@runEdit EditResult.Error(Reason.IO, "Cannot create document directory")
            }
            val pending = File.createTempFile(".new-document-", ".tmp", parent)
            try {
                context.assets.open("document_templates/blank.${format.extension}").use { input ->
                    pending.outputStream().use { output ->
                        input.copyTo(output)
                        output.fd.sync()
                    }
                }
                // No REPLACE_EXISTING: a name collision must leave the other document intact.
                Files.move(pending.toPath(), target.toPath())
                EditResult.Ok(target)
            } catch (e: FileAlreadyExistsException) {
                EditResult.Error(Reason.INVALID_ARGUMENT, "File already exists", e)
            } finally {
                pending.delete()
            }
        }
    }
}
