package com.wxiwei.office.fc.xls

import com.wxiwei.office.fc.hssf.usermodel.HSSFChart
import com.wxiwei.office.ss.model.XLSModel.ASheet
import com.wxiwei.office.thirdpart.achartengine.chart.AbstractChart

class ChartConverter private constructor() {
    companion object {
        private val converter = ChartConverter()

        @JvmStatic
        fun instance(): ChartConverter = converter
    }

    fun converter(sheet: ASheet, chart: HSSFChart): AbstractChart = ChartConverterImpl.instance().converter(sheet, chart)

    fun getChartType(chart: HSSFChart): Short = ChartConverterImpl.instance().getChartType(chart)

    fun getAChart(): AbstractChart? = ChartConverterImpl.instance().aChart
}
