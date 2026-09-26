package com.wxiwei.office.system.search

import com.wxiwei.office.ss.control.ExcelView
import com.wxiwei.office.ss.model.baseModel.Cell
import com.wxiwei.office.ss.other.FindingMgr

class ExcelSearch(private val view: ExcelView) : DocumentSearch {
    private class Hit(val sheetIndex: Int, val cell: Cell)
    private val hits = mutableListOf<Hit>()

    override fun search(query: String, isActive: () -> Boolean): Int {
        hits.clear()
        val queries = SearchText.queries(query)
        if (queries.isEmpty()) return 0
        val workbook = view.getSpreadsheet()?.getWorkbook() ?: return 0
        val finder = FindingMgr()
        for (sheetIndex in 0 until workbook.getSheetCount()) {
            if (!isActive()) break
            val sheet = workbook.getSheet(sheetIndex) ?: continue
            finder.findAll(sheet, queries, isActive).mapTo(hits) { Hit(sheetIndex, it) }
        }
        return hits.size
    }

    override fun focus(index: Int): Boolean {
        val hit = hits.getOrNull(index) ?: return false
        view.showSheet(hit.sheetIndex)
        val sheetView = view.getSheetView() ?: return false
        sheetView.goToFindedCell(hit.cell)
        return true
    }

    override fun clear() {
        hits.clear()
    }
}
