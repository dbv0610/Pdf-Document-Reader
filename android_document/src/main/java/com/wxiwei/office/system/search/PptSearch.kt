package com.wxiwei.office.system.search

import com.wxiwei.office.pg.control.PGFind
import com.wxiwei.office.pg.control.Presentation

class PptSearch(private val presentation: Presentation) : DocumentSearch {
    private val finder = presentation.getFind()
    private var results: List<PGFind.SearchResult> = emptyList()

    override fun search(query: String, isActive: () -> Boolean): Int {
        results = finder?.findAll(query, isActive).orEmpty()
        return results.size
    }

    override fun focus(index: Int): Boolean {
        val result = results.getOrNull(index) ?: return false
        return finder?.focus(result) ?: false
    }

    override fun clear() {
        results = emptyList()
        finder?.resetSearchResult()
        presentation.getEditor().getHighlight()?.removeHighlight()
        presentation.postInvalidate()
    }
}
