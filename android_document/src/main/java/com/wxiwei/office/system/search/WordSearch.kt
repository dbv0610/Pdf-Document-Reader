package com.wxiwei.office.system.search

import com.wxiwei.office.wp.control.Word

class WordSearch(private val word: Word) : DocumentSearch {
    private val finder = word.getFind()
    private val hits = mutableListOf<Pair<Long, Int>>()
    private var query: String? = null

    override fun search(query: String, isActive: () -> Boolean): Int {
        hits.clear()
        this.query = query
        val queries = SearchText.queries(query)
        if (queries.isEmpty()) return 0
        val doc = word.getDocument()
        var para = doc.getParagraph(0)
        while (para != null && isActive()) {
            for (at in SearchText.matchesIn(para.getText(doc) ?: "", queries)) {
                if (!isActive()) return hits.size
                hits.add(para.getStartOffset() to at)
            }
            para = doc.getParagraph(para.getEndOffset())
        }
        return hits.size
    }

    override fun focus(index: Int): Boolean {
        val hit = hits.getOrNull(index) ?: return false
        if (!finder.prepareQuery(query)) return false
        return finder.focusAt(hit.first, hit.second)
    }

    override fun clear() {
        hits.clear()
        query = null
        finder.resetSearchResult()
        word.getHighlight().removeHighlight()
        word.postInvalidate()
    }
}
