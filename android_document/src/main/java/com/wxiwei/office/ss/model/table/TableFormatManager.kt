package com.wxiwei.office.ss.model.table

import com.wxiwei.office.ss.model.style.CellStyle

class TableFormatManager(count: Int) {
    private var formats: MutableMap<Int, CellStyle?>? = HashMap(count)

    fun addFormat(format: CellStyle?): Int {
        val size = formats!!.size
        formats!![size] = format
        return size
    }

    fun getFormat(index: Int): CellStyle? {
        if (index >= 0 && index < formats!!.size) {
            return formats!![index]
        }

        return null
    }

    fun dispose() {
        formats!!.clear()
        formats = null
    }
}
