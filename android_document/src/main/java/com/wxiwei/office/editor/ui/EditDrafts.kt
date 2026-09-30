package com.wxiwei.office.editor.ui

import android.content.Context
import com.wxiwei.office.editor.EditResult
import java.io.File
import java.security.MessageDigest

/**
 * Unsaved edits written to the cache when the app goes to the background, one draft per
 * document. A draft counts only while it is newer than the document it was made from.
 */
object EditDrafts {
    private fun dir(context: Context) = File(context.cacheDir, "edit-drafts")

    fun draftOf(context: Context, file: File): File {
        val digest = MessageDigest.getInstance("SHA-1").digest(file.absolutePath.toByteArray())
        val name = digest.joinToString("") { "%02x".format(it) }
        return File(dir(context), "$name.${file.extension}")
    }

    /** The draft of [file] when it holds edits made after the file was last written. */
    fun pending(context: Context, file: File): File? =
        draftOf(context, file).takeIf { it.isFile && it.length() > 0 && it.lastModified() > file.lastModified() }

    fun write(context: Context, file: File, save: (File) -> EditResult) {
        dir(context).mkdirs()
        val draft = draftOf(context, file)
        val tmp = File(draft.parentFile, draft.name + ".tmp")
        if (save(tmp) is EditResult.Ok && !tmp.renameTo(draft)) {
            tmp.copyTo(draft, overwrite = true)
        }
        tmp.delete()
    }

    fun delete(context: Context, file: File) {
        draftOf(context, file).delete()
    }

    /** Puts the draft in place of [file]; false when it could not. */
    fun restore(context: Context, file: File): Boolean {
        val draft = pending(context, file) ?: return false
        return OfficeEditPanel.saveOver(file) { target ->
            draft.copyTo(target, overwrite = true)
            EditResult.Ok(target)
        }.let { it is EditResult.Ok }.also { if (it) draft.delete() }
    }
}
