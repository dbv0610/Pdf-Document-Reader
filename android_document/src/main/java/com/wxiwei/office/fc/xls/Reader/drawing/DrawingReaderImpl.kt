package com.wxiwei.office.fc.xls.Reader.drawing

import com.wxiwei.office.fc.dom4j.Element
import com.wxiwei.office.fc.openxml4j.opc.PackagePart
import com.wxiwei.office.fc.openxml4j.opc.ZipPackage
import com.wxiwei.office.ss.model.baseModel.Sheet
import com.wxiwei.office.ss.model.drawing.TextParagraph
import com.wxiwei.office.system.IControl

/** Kotlin entry point retaining the complete drawing reader implementation. */
class DrawingReaderImpl private constructor() {
    companion object {
        private val reader = DrawingReaderImpl()

        @JvmStatic
        fun instance(): DrawingReaderImpl = reader

        @JvmStatic
        fun getVerticalByString(anchor: String?): Short = DrawingReaderImplJava.getVerticalByString(anchor)

        @JvmStatic
        fun getHorizontalByString(algn: String?): Short = DrawingReaderImplJava.getHorizontalByString(algn)

        @JvmStatic
        fun getTextParagraph(paragraph: Element?): TextParagraph? = DrawingReaderImplJava.getTextParagraph(paragraph)
    }

    fun read(control: IControl, zipPackage: ZipPackage, drawingPart: PackagePart, sheet: Sheet) {
        DrawingReaderImplJava.instance().read(control, zipPackage, drawingPart, sheet)
    }

    fun processOLEPicture(
        control: IControl,
        zipPackage: ZipPackage,
        sheetPart: PackagePart,
        sheet: Sheet,
        oleObjects: Element?
    ) {
        DrawingReaderImplJava.instance().processOLEPicture(control, zipPackage, sheetPart, sheet, oleObjects)
    }
}
