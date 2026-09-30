/*
 * Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * Proprietary and confidential. Unauthorized copying, modification or distribution of this
 * file, via any medium, is strictly prohibited without the written permission of dongb2002.
 */
package com.editor.docsdk

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import java.io.File

/**
 * The ready-made viewer screen: one call opens a PDF, Word, Excel, PowerPoint or text document
 * full screen, with its title, the page number, a password prompt and error messages.
 *
 * ```
 * DocumentViewer.open(context, uri)
 * DocumentViewer.open(context, file, DocumentViewer.Options(title = "Hợp đồng"))
 * ```
 */
object DocumentViewer {

    /**
     * [title] shown at the top (the file name when null); [password] tried first for a protected
     * document (the user is asked when it is missing or wrong). [editFeatures] not empty: Word,
     * Excel and PowerPoint documents get an Edit button that edits them with these features
     * (see [EditFeature]); empty, the default, only shows the document.
     */
    data class Options @JvmOverloads constructor(
        val title: String? = null,
        val password: String? = null,
        val editFeatures: Set<EditFeature> = emptySet(),
    )

    /** Opens [uri] (content:// or file://) in the viewer screen. */
    @JvmStatic
    @JvmOverloads
    fun open(context: Context, uri: Uri, options: Options = Options()) {
        context.startActivity(intent(context, uri, options))
    }

    /** Opens [file] in the viewer screen. */
    @JvmStatic
    @JvmOverloads
    fun open(context: Context, file: File, options: Options = Options()) {
        open(context, Uri.fromFile(file), options)
    }

    /** The Intent of the viewer screen, to start it yourself (for a result, with flags...). */
    @JvmStatic
    @JvmOverloads
    fun intent(context: Context, uri: Uri, options: Options = Options()): Intent =
        Intent(context, DocumentViewerActivity::class.java)
            .setData(uri)
            .putExtra(DocumentViewerActivity.EXTRA_TITLE, options.title)
            .putExtra(DocumentViewerActivity.EXTRA_PASSWORD, options.password)
            .putExtra(DocumentViewerActivity.EXTRA_EDIT_FEATURES, options.editFeatures.map { it.name }.toTypedArray())
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            .apply { if (options.editFeatures.isNotEmpty()) addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION) }
            .apply { if (context !is Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }

    /** Whether the viewer can open a file named [fileName]. */
    @JvmStatic
    fun canOpen(fileName: String?): Boolean = DocumentType.fromFileName(fileName) != null
}
