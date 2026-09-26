package com.reader.pdfviewer.listener

import android.graphics.Bitmap
import android.view.MotionEvent
import com.reader.pdfviewer.link.LinkHandler
import com.reader.pdfviewer.model.LinkTapEvent

class Callbacks {
    private var onReadyForPrintingListener: OnReadyForPrintingListener? = null

    /**
     * Call back object to call when the PDF is loaded
     */
    private var onLoadCompleteListener: OnLoadCompleteListener? = null
    private var onAttachCompleteListener: OnAttachCompleteListener? = null
    private var onDetachCompleteListener: OnDetachCompleteListener? = null

    /**
     * Call back object to call when document loading error occurs
     */
    var onError: OnErrorListener? = null

    /**
     * Call back object to call when the page load error occurs
     */
    private var onPageErrorListener: OnPageErrorListener? = null

    /**
     * Call back object to call when the document is initially rendered
     */
    private var onRenderListener: OnRenderListener? = null

    /**
     * Call back object to call when the page has changed
     */
    private var onPageChangeListener: OnPageChangeListener? = null

    /**
     * Call back object to call when the page is scrolled
     */
    private var onPageScrollListener: OnPageScrollListener? = null

    /**
     * Call back object to call when the above layer is to drawn
     */
    var onDraw: OnDrawListener? = null

    var onDrawAll: OnDrawListener? = null

    /**
     * Call back object to call when the user does a tap gesture
     */
    private var onTapListener: OnTapListener? = null

    /**
     * Call back object to call when the user does a long tap gesture
     */
    private var onLongPressListener: OnLongPressListener? = null

    /**
     * Call back object to call when clicking link
     */
    private var linkHandler: LinkHandler? = null

    /**
     * Call back object to call when the user triggers an action from the text selection popup
     */
    private var onSelectionActionListener: OnSelectionActionListener? = null

    /**
     * Call back object to call when the text selection state changes (active / cleared)
     */
    private var onSelectionChangeListener: OnSelectionChangeListener? = null

    fun setOnReadyForPrinting(onReadyForPrintingListener: OnReadyForPrintingListener?) {
        this.onReadyForPrintingListener = onReadyForPrintingListener
    }

    fun callsOnReadyForPrinting(pagesAsBitmaps: MutableList<Bitmap?>?) {
        if (onReadyForPrintingListener != null) {
            onReadyForPrintingListener!!.bitmapsReady(pagesAsBitmaps)
        }
    }

    fun setOnLoadComplete(onLoadCompleteListener: OnLoadCompleteListener?) {
        this.onLoadCompleteListener = onLoadCompleteListener
    }

    fun callOnLoadComplete(pagesCount: Int) {
        if (onLoadCompleteListener != null) {
            onLoadCompleteListener!!.loadComplete(pagesCount)
        }
    }

    fun setOnAttachCompleteListener(onAttachCompleteListener: OnAttachCompleteListener?) {
        this.onAttachCompleteListener = onAttachCompleteListener
    }

    fun setOnDetachCompleteListener(onDetachCompleteListener: OnDetachCompleteListener?) {
        this.onDetachCompleteListener = onDetachCompleteListener
    }

    fun callOnAttachComplete() {
        if (onAttachCompleteListener != null) {
            onAttachCompleteListener!!.onAttachComplete()
        }
    }

    fun callOnDetachComplete() {
        if (onDetachCompleteListener != null) {
            onDetachCompleteListener!!.onDetachComplete()
        }
    }

    fun setOnPageError(onPageErrorListener: OnPageErrorListener?) {
        this.onPageErrorListener = onPageErrorListener
    }

    fun callOnPageError(page: Int, error: Throwable?): Boolean {
        if (onPageErrorListener != null) {
            onPageErrorListener!!.onPageError(page, error)
            return true
        }
        return false
    }

    fun setOnRender(onRenderListener: OnRenderListener?) {
        this.onRenderListener = onRenderListener
    }

    fun callOnRender(pagesCount: Int) {
        if (onRenderListener != null) {
            onRenderListener!!.onInitiallyRendered(pagesCount)
        }
    }

    fun setOnPageChange(onPageChangeListener: OnPageChangeListener?) {
        this.onPageChangeListener = onPageChangeListener
    }

    fun callOnPageChange(page: Int, pagesCount: Int) {
        if (onPageChangeListener != null) {
            onPageChangeListener!!.onPageChanged(page, pagesCount)
        }
    }

    fun setOnPageScroll(onPageScrollListener: OnPageScrollListener?) {
        this.onPageScrollListener = onPageScrollListener
    }

    fun callOnPageScroll(currentPage: Int, offset: Float) {
        if (onPageScrollListener != null) {
            onPageScrollListener!!.onPageScrolled(currentPage, offset)
        }
    }

    fun setOnTap(onTapListener: OnTapListener?) {
        this.onTapListener = onTapListener
    }

    fun callOnTap(event: MotionEvent?): Boolean {
        return onTapListener != null && onTapListener!!.onTap(event)
    }

    fun setOnLongPress(onLongPressListener: OnLongPressListener?) {
        this.onLongPressListener = onLongPressListener
    }

    fun callOnLongPress(event: MotionEvent?) {
        if (onLongPressListener != null) {
            onLongPressListener!!.onLongPress(event)
        }
    }

    fun setLinkHandler(linkHandler: LinkHandler?) {
        this.linkHandler = linkHandler
    }

    fun callLinkHandler(event: LinkTapEvent?) {
        if (linkHandler != null) {
            linkHandler!!.handleLinkEvent(event)
        }
    }

    fun setOnSelectionActionListener(onSelectionActionListener: OnSelectionActionListener?) {
        this.onSelectionActionListener = onSelectionActionListener
    }

    fun hasSelectionActionListener(): Boolean {
        return onSelectionActionListener != null
    }

    fun callOnCopySelection(selectedText: String) {
        if (onSelectionActionListener != null) {
            onSelectionActionListener!!.onCopySelection(selectedText)
        }
    }

    var onTextMarkupListener: OnTextMarkupListener? = null

    fun callOnTextMarkupAdded(page: Int, type: com.reader.pdfviewer.TextMarkupType) {
        onTextMarkupListener?.onTextMarkupAdded(page, type)
    }

    fun setOnSelectionChangeListener(onSelectionChangeListener: OnSelectionChangeListener?) {
        this.onSelectionChangeListener = onSelectionChangeListener
    }

    fun callOnSelectionChanged(hasSelection: Boolean) {
        if (onSelectionChangeListener != null) {
            onSelectionChangeListener!!.onSelectionChanged(hasSelection)
        }
    }

    var onEditChangeListener: OnEditChangeListener? = null
    var onInkChangeListener: OnInkChangeListener? = null

    fun clear() {
        onEditChangeListener = null
        onInkChangeListener = null
        // Not clearing onAttach and onDetach listeners because those are called before view initialization
        onLoadCompleteListener = null
        this.onError = null
        onPageErrorListener = null
        onRenderListener = null
        onPageChangeListener = null
        onPageScrollListener = null
        this.onDraw = null
        this.onDrawAll = null
        onTapListener = null
        onLongPressListener = null
        linkHandler = null
        onSelectionActionListener = null
        onSelectionChangeListener = null
        onTextMarkupListener = null
    }
}
