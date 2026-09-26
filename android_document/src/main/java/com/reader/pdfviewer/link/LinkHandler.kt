package com.reader.pdfviewer.link

import com.reader.pdfviewer.model.LinkTapEvent

interface LinkHandler {
    /**
     * Called when link was tapped by user
     * 
     * @param event current event
     */
    fun handleLinkEvent(event: LinkTapEvent?)
}
