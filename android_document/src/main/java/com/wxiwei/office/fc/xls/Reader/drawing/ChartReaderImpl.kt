package com.wxiwei.office.fc.xls.Reader.drawing

import com.wxiwei.office.fc.openxml4j.opc.PackagePart
import com.wxiwei.office.fc.openxml4j.opc.ZipPackage
import com.wxiwei.office.system.IControl
import com.wxiwei.office.thirdpart.achartengine.chart.AbstractChart

/** Kotlin entry point retaining the complete chart reader implementation. */
class ChartReaderImpl private constructor() {
    companion object {
        private val reader = ChartReaderImpl()

        @JvmStatic
        fun instance(): ChartReaderImpl = reader
    }

    fun read(
        control: IControl,
        zipPackage: ZipPackage,
        chartPart: PackagePart,
        schemeColor: Map<String, Int>,
        appType: Byte
    ): AbstractChart? = ChartReaderImplJava.instance().read(control, zipPackage, chartPart, schemeColor, appType)
}
