package com.reader.pdfviewer.listener

fun interface OnSelectionChangeListener {
    /**
     * Called whenever the text selection state changes.
     * @param hasSelection `true` if text is currently selected, `false` if selection was cleared.
     */
    fun onSelectionChanged(hasSelection: Boolean)
}
