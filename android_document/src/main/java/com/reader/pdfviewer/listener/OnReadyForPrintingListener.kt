package com.reader.pdfviewer.listener

import android.graphics.Bitmap

/**
 * Implement this interface to receive events from PDFView
 * when bitmaps has been generated. Used to print password protected PDF.
 */
interface OnReadyForPrintingListener {
    /**
     * Called when bitmaps has been generated
     * 
     * @param bitmaps pages of the PDF as bitmaps
     */
    fun bitmapsReady(bitmaps: MutableList<Bitmap?>?)
}
