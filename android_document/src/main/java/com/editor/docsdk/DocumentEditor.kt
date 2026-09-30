/*
 * Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * Proprietary and confidential. Unauthorized copying, modification or distribution of this
 * file, via any medium, is strictly prohibited without the written permission of dongb2002.
 */
package com.editor.docsdk

import androidx.annotation.MainThread
import com.wxiwei.office.editor.ui.OfficeEditPanel

/**
 * Edits the document of a [DocumentView], from [DocumentView.startEditing]. With the SDK's bar
 * (the default) there is nothing more to do. Without it, put your own buttons where you want and
 * run the commands you choose:
 *
 * ```
 * val editor = documentView.startEditing(EditFeature.all(), showToolbar = false) ?: return
 * boldButton.isVisible = editor.isAvailable(EditAction.BOLD)
 * boldButton.setOnClickListener { editor.run(EditAction.BOLD) }
 * colorButton.setOnClickListener { editor.run(EditAction.TEXT_COLOR, "C00000") }
 * editor.listener = object : DocumentEditor.Listener {
 *     override fun onStateChanged() { boldButton.isSelected = editor.isActive(EditAction.BOLD) }
 * }
 * ```
 *
 * Taps and long presses on the document (typing in Word, selecting cells and shapes, dragging
 * pictures) work either way. Everything is called on the main thread.
 *
 * Every question a command asks, the big dialogs too (Paragraph, Find & replace, the shape list,
 * animations...), can be your own UI: see [dialogs] and [EditRequest].
 *
 * [panel] is the edit UI itself (`com.wxiwei.office.editor.ui`), for everything else: its tabs,
 * Excel's formula box ([OfficeEditPanel.header]) or the selected cell
 * ([com.wxiwei.office.editor.ui.ExcelEditPanel.selectedRange], `cellInput`), or the SDK's bar
 * placed where you want (`EditToolbar(panel).view`). With your own bar, set
 * [OfficeEditPanel.toolbar] to it so the caret and the text being edited stay above it; the SDK
 * lifts its parent above the keyboard, or set [OfficeEditPanel.liftToolbar] to false and move it
 * yourself (IME insets).
 */
class DocumentEditor internal constructor(val panel: OfficeEditPanel) {

    interface Listener {
        /** What is selected, or the state of a command, may have changed: refresh your buttons. */
        fun onStateChanged() {}

        /** [status] says what is selected or what to do next, like "Tap the text to type". */
        fun onStatusChanged(status: String) {}
    }

    var listener: Listener? = null

    init {
        panel.addStateListener { listener?.onStateChanged() }
        panel.addStatusListener { listener?.onStatusChanged(it) }
    }

    /** Your own dialogs for the questions the commands ask; null: the SDK's. */
    var dialogs: EditDialogs?
        get() = panel.dialogHandler
        set(value) {
            panel.dialogHandler = value
        }

    /** Type of the document edited. */
    val documentType: DocumentType
        get() = when (panel.file.extension.lowercase()) {
            "docx" -> DocumentType.WORD
            "pptx" -> DocumentType.POWERPOINT
            else -> DocumentType.EXCEL
        }

    /** The commands of this document that the [EditFeature]s allow, in the order of the SDK's bar. */
    val actions: List<EditAction> get() = panel.actions()

    /** Whether [action] can run on this document. */
    fun isAvailable(action: EditAction): Boolean = panel.isAvailable(action)

    /** Whether [action] is on where the caret or selection is (bold text, wrapped cell...). */
    fun isActive(action: EditAction): Boolean = panel.isActive(action)

    /** What is selected or what to do next, as a short sentence. */
    val status: String get() = panel.status

    /**
     * Runs [action]. When it needs a value it asks the user (with [dialogs], or the SDK's dialog).
     * Returns false when [action] is not available.
     */
    @MainThread
    fun run(action: EditAction): Boolean = panel.run(action, null, false)

    /** Runs [action] with [value] instead of asking the user; see [EditAction] for the value of each. */
    @MainThread
    fun run(action: EditAction, value: Any?): Boolean = panel.run(action, value, true)

    /** True when there are edits not saved yet. */
    fun hasUnsavedChanges(): Boolean = panel.hasChanges()

    /** Writes the edits to the document; false when it could not (the user is told why). */
    @MainThread
    fun save(): Boolean = panel.save()
}
