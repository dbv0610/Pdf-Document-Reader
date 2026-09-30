/*
 * Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * Proprietary and confidential. Unauthorized copying, modification or distribution of this
 * file, via any medium, is strictly prohibited without the written permission of dongb2002.
 */
package com.editor.docsdk

import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.wxiwei.office.R
import com.wxiwei.office.editor.ui.DialogKit

/**
 * The viewer screen started by [DocumentViewer]. Its views are made in code, so it adds no
 * layout to the app that uses the SDK; the texts are the SDK's strings (English and Vietnamese).
 */
class DocumentViewerActivity : AppCompatActivity() {

    private lateinit var documentView: DocumentView
    private lateinit var titleView: TextView
    private lateinit var pageView: TextView
    private lateinit var progress: ProgressBar
    private lateinit var messageView: TextView
    private lateinit var editButton: TextView
    private var editFeatures: Set<EditFeature> = emptySet()
    private var uri: Uri? = null
    private var passwordDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val uri = intent.data
        if (uri == null) {
            finish()
            return
        }
        this.uri = uri
        setContentView(buildViews())
        titleView.text = intent.getStringExtra(EXTRA_TITLE) ?: UriFiles.displayName(this, uri) ?: ""
        editFeatures = intent.getStringArrayExtra(EXTRA_EDIT_FEATURES).orEmpty()
            .mapNotNull { name -> EditFeature.entries.firstOrNull { it.name == name } }.toSet()
        documentView.listener = object : DocumentView.Listener {
            override fun onLoaded(pageCount: Int) {
                progress.visibility = View.GONE
                showPage(documentView.currentPage, pageCount)
                editButton.visibility = if (editFeatures.isNotEmpty() && documentView.canEdit) View.VISIBLE else View.GONE
            }

            override fun onEditingChanged(editing: Boolean) {
                editButton.setText(if (editing) R.string.docsdk_edit_done else R.string.docsdk_edit_start)
            }

            override fun onPageChanged(page: Int, pageCount: Int) = showPage(page, pageCount)

            override fun onError(error: DocumentException) = showError(error)
        }
        open(intent.getStringExtra(EXTRA_PASSWORD))
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = leave()
        })
    }

    /** Ends editing (asking about unsaved edits) or else closes the screen. */
    private fun leave() {
        if (documentView.isEditing) stopEditing() else finish()
    }

    private fun toggleEditing() {
        if (documentView.isEditing) stopEditing() else { documentView.startEditing(editFeatures) }
    }

    private fun stopEditing() {
        if (!documentView.hasUnsavedChanges()) return documentView.stopEditing()
        DialogKit(this).show(getString(R.string.docsdk_edit_unsaved_title)) {
            text(getString(R.string.docsdk_edit_unsaved_message, titleView.text))
            positive(getString(R.string.docsdk_edit_save)) { if (documentView.save()) documentView.stopEditing() }
            negative(getString(R.string.docsdk_edit_discard)) { documentView.stopEditing() }
            neutral(getString(R.string.docsdk_edit_keep_editing))
        }
    }

    override fun onDestroy() {
        passwordDialog?.dismiss()
        if (::documentView.isInitialized) documentView.close()
        super.onDestroy()
    }

    private fun open(password: String?) {
        progress.visibility = View.VISIBLE
        messageView.visibility = View.GONE
        documentView.open(uri ?: return, password)
    }

    private fun showPage(page: Int, pageCount: Int) {
        pageView.text = if (pageCount > 0) getString(R.string.docsdk_page_of, page + 1, pageCount) else ""
    }

    private fun showError(error: DocumentException) {
        progress.visibility = View.GONE
        when (error.reason) {
            DocumentException.Reason.PASSWORD_REQUIRED, DocumentException.Reason.PASSWORD_INCORRECT ->
                askPassword(error.reason == DocumentException.Reason.PASSWORD_INCORRECT)
            else -> {
                messageView.text = getString(
                    when (error.reason) {
                        DocumentException.Reason.UNSUPPORTED -> R.string.docsdk_error_unsupported
                        DocumentException.Reason.DAMAGED -> R.string.docsdk_error_damaged
                        DocumentException.Reason.NOT_FOUND -> R.string.docsdk_error_not_found
                        DocumentException.Reason.OUT_OF_MEMORY -> R.string.docsdk_error_memory
                        else -> R.string.docsdk_error_generic
                    }
                )
                messageView.visibility = View.VISIBLE
            }
        }
    }

    private fun askPassword(incorrect: Boolean) {
        if (isFinishing || passwordDialog?.isShowing == true) return
        passwordDialog = DialogKit(this).show(getString(R.string.docsdk_password_title)) {
            text(getString(if (incorrect) R.string.docsdk_password_incorrect else R.string.docsdk_password_required))
            val input = input(getString(R.string.docsdk_password_hint)).apply {
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            positive(getString(R.string.docsdk_open)) { open(input.text.toString()) }
            negative { finish() }
        }.apply { setOnCancelListener { finish() } }
    }

    private fun buildViews(): View {
        val ink = Color.rgb(0x1B, 0x23, 0x36)
        val quiet = Color.rgb(0x5C, 0x65, 0x77)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
        }
        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(4), 0, dp(16), 0)
            minimumHeight = dp(56)
        }
        val back = ImageButton(this).apply {
            setImageResource(EditStyle.of(this@DocumentViewerActivity).backIcon ?: R.drawable.docsdk_ic_back)
            setBackgroundColor(Color.TRANSPARENT)
            contentDescription = getString(R.string.docsdk_back)
            setOnClickListener { leave() }
        }
        bar.addView(back, LinearLayout.LayoutParams(dp(48), dp(48)))
        titleView = TextView(this).apply {
            setTextColor(ink)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f)
            typeface = Typeface.DEFAULT_BOLD
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.MIDDLE
        }
        bar.addView(titleView, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        pageView = TextView(this).apply {
            setTextColor(quiet)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
        }
        bar.addView(pageView)
        editButton = TextView(this).apply {
            setText(R.string.docsdk_edit_start)
            setTextColor(DialogStyle.of(this@DocumentViewerActivity).accent)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            minWidth = dp(48)
            minHeight = dp(48)
            setPadding(dp(12), 0, 0, 0)
            visibility = View.GONE
            setOnClickListener { toggleEditing() }
        }
        bar.addView(editButton)
        root.addView(bar, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        val content = FrameLayout(this)
        documentView = DocumentView(this)
        content.addView(documentView, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        progress = ProgressBar(this)
        content.addView(progress, FrameLayout.LayoutParams(dp(48), dp(48), Gravity.CENTER))
        messageView = TextView(this).apply {
            setTextColor(quiet)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            gravity = Gravity.CENTER
            setPadding(dp(32), 0, dp(32), 0)
            visibility = View.GONE
        }
        content.addView(messageView, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER))
        root.addView(content, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))

        // edge to edge on Android 15+: keep the bar below the status bar
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(top = bars.top, bottom = bars.bottom)
            insets
        }
        return root
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()

    internal companion object {
        const val EXTRA_TITLE = "com.editor.docsdk.TITLE"
        const val EXTRA_PASSWORD = "com.editor.docsdk.PASSWORD"
        const val EXTRA_EDIT_FEATURES = "com.editor.docsdk.EDIT_FEATURES"
    }
}
