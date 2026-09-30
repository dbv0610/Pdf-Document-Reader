/*
 * Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * Proprietary and confidential. Unauthorized copying, modification or distribution of this
 * file, via any medium, is strictly prohibited without the written permission of dongb2002.
 */
package com.editor.docsdk

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.annotation.WorkerThread
import java.io.File
import java.io.FileNotFoundException

/**
 * The engines read files, so a content:// Uri is first copied to the app's cache (a folder of
 * the SDK, emptied of copies over a day old). A file:// Uri or a path is used as it is.
 */
internal object UriFiles {

    /** The name the Uri shows to the user, used to know the document type. */
    fun displayName(context: Context, uri: Uri): String? {
        if (uri.scheme == ContentResolver.SCHEME_FILE) return uri.lastPathSegment
        return try {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
                if (c.moveToFirst()) c.getString(0) else null
            }
        } catch (e: Exception) {
            null
        } ?: uri.lastPathSegment
    }

    fun typeOf(context: Context, uri: Uri): DocumentType? =
        DocumentType.fromFileName(displayName(context, uri)) ?: DocumentType.fromMimeType(context.contentResolver.getType(uri))

    /** A readable file for [uri]: the file itself, or a copy in the cache named like the document. */
    @WorkerThread
    fun toFile(context: Context, uri: Uri): File {
        if (uri.scheme == null || uri.scheme == ContentResolver.SCHEME_FILE) {
            val file = File(uri.path ?: throw FileNotFoundException(uri.toString()))
            if (!file.canRead()) throw FileNotFoundException(file.path)
            return file
        }
        val dir = File(context.cacheDir, "docsdk").apply { mkdirs() }
        cleanOld(dir)
        val type = typeOf(context, uri)
        var name = displayName(context, uri)?.replace(Regex("[\\\\/:*?\"<>|]"), "_") ?: "document"
        if (DocumentType.fromFileName(name) == null && type != null) name += "." + type.extensions.first()
        val target = File(File(dir, System.nanoTime().toString()).apply { mkdirs() }, name)
        val input = context.contentResolver.openInputStream(uri) ?: throw FileNotFoundException(uri.toString())
        input.use { i -> target.outputStream().use { i.copyTo(it) } }
        return target
    }

    private fun cleanOld(dir: File) {
        val old = System.currentTimeMillis() - 24 * 60 * 60 * 1000L
        dir.listFiles()?.filter { it.lastModified() < old }?.forEach { it.deleteRecursively() }
    }
}
