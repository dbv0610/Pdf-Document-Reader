package com.wxiwei.office.fc.hslf.record

abstract class SheetContainer : PositionDependentRecordContainer() {
    abstract fun getPPDrawing(): PPDrawing?
    abstract fun getColorScheme(): ColorSchemeAtom?
}
