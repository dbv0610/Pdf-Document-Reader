package com.wxiwei.office.system.search

import android.view.View
import com.wxiwei.office.wp.control.Word
import com.wxiwei.office.pg.control.Presentation
import com.wxiwei.office.ss.control.ExcelView

/**
 * Single-use per query: the activity creates a new instance for each search.
 * [search] runs on a worker thread and must not touch view or UI state.
 */
interface DocumentSearch {
    /** Worker thread. Collects all matches, returning those found when isActive() becomes false. */
    fun search(query: String, isActive: () -> Boolean): Int
    /** Main thread. Shows and highlights match [index] (0-based). */
    fun focus(index: Int): Boolean
    fun clear()

    companion object {
        fun of(view: View?): DocumentSearch? = when (view) {
            is Word -> WordSearch(view)
            is Presentation -> PptSearch(view)
            is ExcelView -> ExcelSearch(view)
            else -> null
        }
    }
}
