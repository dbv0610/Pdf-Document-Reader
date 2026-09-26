package com.wxiwei.office.fc.xls

import com.wxiwei.office.fc.hssf.usermodel.HSSFChart
import com.wxiwei.office.ss.model.XLSModel.ASheet
import com.wxiwei.office.thirdpart.achartengine.chart.AbstractChart

/** Kotlin entry point retaining the complete chart conversion implementation. */
class ChartConverterImpl private constructor() {
    companion object {
        private val converter = ChartConverterImpl()

        @JvmStatic
        fun instance(): ChartConverterImpl = converter
    }

    fun converter(sheet: ASheet, chart: HSSFChart): AbstractChart =
        ChartConverterImplJava.instance().converter(sheet, chart)

    fun getChartType(chart: HSSFChart): Short =
        ChartConverterImplJava.instance().getChartType(chart)

    val aChart: AbstractChart?
        get() = ChartConverterImplJava.instance().aChart
}
