package com.reader.pdfviewer.listener

/** Receives the undo/redo availability after an ink edit. */
fun interface OnInkChangeListener {
    /** Called after a successful stroke, undo, or redo. */
    fun onInkChanged(canUndo: Boolean, canRedo: Boolean)
}
