package com.reader.pdfviewer.listener

/** Receives unified edit history availability on the main thread. */
fun interface OnEditChangeListener {
    /** Called after an annotation edit, undo, or redo. */
    fun onEditChanged(canUndo: Boolean, canRedo: Boolean)
}
