package com.wxiwei.office.fc.xls.Reader.drawing

import com.wxiwei.office.fc.openxml4j.opc.PackagePart
import com.wxiwei.office.fc.openxml4j.opc.ZipPackage
import com.wxiwei.office.ss.model.baseModel.Sheet
import com.wxiwei.office.system.IControl

class DrawingReader private constructor() {
    companion object {
        private val reader = DrawingReader()

        @JvmStatic
        fun instance(): DrawingReader = reader

        @JvmStatic
        fun getVerticalByString(anchor: String?): Short = DrawingReaderImpl.getVerticalByString(anchor)

        @JvmStatic
        fun getHorizontalByString(algn: String?): Short = DrawingReaderImpl.getHorizontalByString(algn)

        @JvmStatic
        fun getTextParagraph(paragraph: com.wxiwei.office.fc.dom4j.Element?): com.wxiwei.office.ss.model.drawing.TextParagraph? =
            DrawingReaderImpl.getTextParagraph(paragraph)
    }

    fun read(control: IControl, zipPackage: ZipPackage, drawingPart: PackagePart, sheet: Sheet) {
        DrawingReaderImpl.instance().read(control, zipPackage, drawingPart, sheet)
    }

    fun processOLEPicture(control: IControl, zipPackage: ZipPackage, sheetPart: PackagePart, sheet: Sheet, oleObjects: com.wxiwei.office.fc.dom4j.Element?) {
        DrawingReaderImpl.instance().processOLEPicture(control, zipPackage, sheetPart, sheet, oleObjects)
    }
}
