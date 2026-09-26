/*
 * 文件名称:          TableStyle.java
 *
 * 编译器:            android2.2
 * 时间:              下午4:06:41
 */
package com.wxiwei.office.ss.model.table

import com.wxiwei.office.ss.model.CellRangeAddress

/**
 * TODO: 文件注释
 *
 * Read版本:        Read V1.0
 * 作者:            jqin
 * 日期:            2013-4-17
 * 负责人:           jqin
 */
class SSTable {
    private var ref: CellRangeAddress? = null
    private var headerRowShown = true
    private var totalRowShown = false

    //table style information
    private var name: String? = null
    private var showFirstColumn = false
    private var showLastColumn = false
    private var showRowStripes = false
    private var showColumnStripes = false

    private var tableBorderDxfId = -1
    private var headerRowDxfId = -1
    private var headerRowBorderDxfId = -1
    private var totalsRowDxfId = -1
    private var totalsRowBorderDxfId = -1

    fun getTableReference(): CellRangeAddress? = ref

    fun setTableReference(ref: CellRangeAddress?) {
        this.ref = ref
    }

    fun isHeaderRowShown(): Boolean = headerRowShown

    fun setHeaderRowShown(headerRowShown: Boolean) {
        this.headerRowShown = headerRowShown
    }

    fun isTotalRowShown(): Boolean = totalRowShown

    fun setTotalRowShown(totalRowShown: Boolean) {
        this.totalRowShown = totalRowShown
    }

    fun getName(): String? = name

    fun setName(name: String?) {
        this.name = name
    }

    fun isShowFirstColumn(): Boolean = showFirstColumn

    fun setShowFirstColumn(showFirstColumn: Boolean) {
        this.showFirstColumn = showFirstColumn
    }

    fun isShowLastColumn(): Boolean = showLastColumn

    fun setShowLastColumn(showLastColumn: Boolean) {
        this.showLastColumn = showLastColumn
    }

    fun isShowRowStripes(): Boolean = showRowStripes

    fun setShowRowStripes(showRowStripes: Boolean) {
        this.showRowStripes = showRowStripes
    }

    fun isShowColumnStripes(): Boolean = showColumnStripes

    fun setShowColumnStripes(showColumnStripes: Boolean) {
        this.showColumnStripes = showColumnStripes
    }

    fun getTableBorderDxfId(): Int = tableBorderDxfId

    fun setTableBorderDxfId(tableBorderDxfId: Int) {
        this.tableBorderDxfId = tableBorderDxfId
    }

    fun getHeaderRowDxfId(): Int = headerRowDxfId

    fun setHeaderRowDxfId(headerRowDxfId: Int) {
        this.headerRowDxfId = headerRowDxfId
    }

    fun getHeaderRowBorderDxfId(): Int = headerRowBorderDxfId

    fun setHeaderRowBorderDxfId(headerRowBorderDxfId: Int) {
        this.headerRowBorderDxfId = headerRowBorderDxfId
    }

    fun getTotalsRowDxfId(): Int = totalsRowDxfId

    fun setTotalsRowDxfId(totalsRowDxfId: Int) {
        this.totalsRowDxfId = totalsRowDxfId
    }

    fun getTotalsRowBorderDxfId(): Int = totalsRowBorderDxfId

    fun setTotalsRowBorderDxfId(totalsRowBorderDxfId: Int) {
        this.totalsRowBorderDxfId = totalsRowBorderDxfId
    }
}
