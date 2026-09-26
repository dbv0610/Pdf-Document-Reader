package com.reader.pdfviewer.model

/**
 * One line of the document containing a search match.
 * @param page 0-based page index
 * @param content the whole text line holding the match, trimmed
 */
data class SearchResult(val page: Int, val content: String)
