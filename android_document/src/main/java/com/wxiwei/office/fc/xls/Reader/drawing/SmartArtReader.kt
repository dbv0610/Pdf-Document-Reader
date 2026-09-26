package com.wxiwei.office.fc.xls.Reader.drawing

import com.wxiwei.office.common.autoshape.AutoShapeDataKit
import com.wxiwei.office.common.bg.BackgroundAndFill
import com.wxiwei.office.common.borders.Line
import com.wxiwei.office.common.shape.AbstractShape
import com.wxiwei.office.common.shape.SmartArt
import com.wxiwei.office.common.shape.TextBox
import com.wxiwei.office.constant.MainConstant
import com.wxiwei.office.constant.SSConstant
import com.wxiwei.office.constant.wp.WPModelConstant
import com.wxiwei.office.fc.LineKit
import com.wxiwei.office.fc.dom4j.Element
import com.wxiwei.office.fc.dom4j.io.SAXReader
import com.wxiwei.office.fc.openxml4j.opc.PackagePart
import com.wxiwei.office.fc.openxml4j.opc.ZipPackage
import com.wxiwei.office.fc.ppt.attribute.ParaAttr
import com.wxiwei.office.fc.ppt.attribute.RunAttr
import com.wxiwei.office.fc.ppt.attribute.SectionAttr
import com.wxiwei.office.fc.ppt.reader.ReaderKit
import com.wxiwei.office.simpletext.model.AttrManage
import com.wxiwei.office.simpletext.model.IAttributeSet
import com.wxiwei.office.simpletext.model.LeafElement
import com.wxiwei.office.simpletext.model.ParagraphElement
import com.wxiwei.office.simpletext.model.SectionElement
import com.wxiwei.office.ss.model.baseModel.Sheet
import com.wxiwei.office.system.IControl

class SmartArtReader private constructor() {
    companion object {
        private val reader = SmartArtReader()
        @JvmStatic fun instance(): SmartArtReader = reader
    }

    private var sheet: Sheet? = null
    private var offset = 0

    @Throws(Exception::class)
    fun read(control: IControl, zipPackage: ZipPackage, slidePart: PackagePart, dataPart: PackagePart,
             schemeColor: Map<String, Int>, sheet: Sheet): SmartArt? {
        this.sheet = sheet
        val saxReader = SAXReader()
        var input = dataPart.inputStream
        val dataDoc = saxReader.read(input)
        input.close()
        var root = dataDoc.rootElement
        val fill: BackgroundAndFill? = AutoShapeDataKit.processBackground(control, zipPackage, dataPart, root.element("bg"), schemeColor)
        val line: Line? = LineKit.createLine(control, zipPackage, dataPart, root.element("whole")?.element("ln"), schemeColor)
        var drawingPart: PackagePart? = null
        var element: Element? = root.element("extLst")?.element("ext")?.element("dataModelExt")
        val relId = element?.attributeValue("relId")
        if (relId != null) {
            drawingPart = zipPackage.getPart(slidePart.getRelationship(relId).targetURI)
        }
        if (drawingPart == null) return null
        input = drawingPart.inputStream
        val smartArtDoc = saxReader.read(input)
        input.close()
        val smartArt = SmartArt()
        smartArt.setBackgroundAndFill(fill)
        smartArt.setLine(line)
        root = smartArtDoc.rootElement
        val spTree = root.element("spTree")
        val iterator = spTree.elementIterator("sp")
        while (iterator.hasNext()) {
            element = iterator.next() as Element
            val sp = element!!
            val spPr = sp.element("spPr")
            val rect = spPr?.let { ReaderKit.instance().getShapeAnchor(it.element("xfrm"), 1f, 1f) }
            val shape: AbstractShape? = AutoShapeDataKit.getAutoShape(control, zipPackage, dataPart, sp, rect, schemeColor, MainConstant.APPLICATION_TYPE_SS.toInt())
            if (shape != null) smartArt.appendShapes(shape)
            val textBox = getTextBoxData(control, sp)
            if (textBox != null) smartArt.appendShapes(textBox)
        }
        this.sheet = null
        return smartArt
    }

    private fun getTextBoxData(control: IControl, sp: Element): TextBox? {
        val txXfrm = sp.element("txXfrm")
        val rect = txXfrm?.let { ReaderKit.instance().getShapeAnchor(it, 1f, 1f) }
        val txBody = sp.element("txBody") ?: return null
        val textBox = TextBox()
        val section = SectionElement()
        section.setStartOffset(0)
        textBox.setElement(section)
        val attr = section.getAttribute()
        AttrManage.instance().setPageWidth(attr, Math.round(rect!!.width * MainConstant.PIXEL_TO_TWIPS))
        AttrManage.instance().setPageHeight(attr, Math.round(rect.height * MainConstant.PIXEL_TO_TWIPS))
        AttrManage.instance().setPageMarginLeft(attr, Math.round(SSConstant.SHEET_SPACETOBORDER * MainConstant.PIXEL_TO_TWIPS))
        AttrManage.instance().setPageMarginRight(attr, Math.round(SSConstant.SHEET_SPACETOBORDER * MainConstant.PIXEL_TO_TWIPS))
        AttrManage.instance().setPageMarginTop(attr, 0)
        AttrManage.instance().setPageMarginBottom(attr, 0)
        val bodyPr = txXfrm?.element("bodyPr")
        SectionAttr.instance().setSectionAttribute(bodyPr, attr, null, null, false)
        textBox.setWrapLine(bodyPr?.attributeValue("wrap")?.equals("square", true) != false)
        offset = processParagraph(control, section, txBody)
        section.setEndOffset(offset.toLong())
        textBox.setBounds(rect!!)
        val text = textBox.element?.getText(null)
        if (!text.isNullOrEmpty() && text != "\n") ReaderKit.instance().processRotation(textBox, sp.element("txXfrm"))
        return textBox
    }

    private fun processParagraph(control: IControl, section: SectionElement, txBody: Element): Int {
        offset = 0
        for (rawP in txBody.elements("p")) {
            val p = rawP as Element
            val paragraph = ParagraphElement()
            paragraph.setStartOffset(offset.toLong())
            val pPr = p.element("pPr")
            ParaAttr.instance().setParaAttribute(control, pPr, paragraph.getAttribute(), null, -1, -1, 0, false, false)
            val result = processRun(control, section, paragraph, p, null)
            result.setEndOffset(offset.toLong())
            section.appendParagraph(result, WPModelConstant.MAIN)
        }
        return offset
    }

    private fun processRun(control: IControl, section: SectionElement, paragraph: ParagraphElement, p: Element,
                           attrLayout: IAttributeSet?): ParagraphElement {
        val runs = p.elements("r")
        var leaf: LeafElement? = null
        if (runs.isEmpty()) {
            leaf = LeafElement("\n")
            val runProperties = p.element("pPr")?.element("rPr")
            if (runProperties != null) RunAttr.instance().setRunAttribute(sheet!!, runProperties, leaf.getAttribute(), attrLayout)
            leaf.setStartOffset(offset.toLong())
            offset++
            leaf.setEndOffset(offset.toLong())
            paragraph.appendLeaf(leaf)
            return paragraph
        }
        for (rawRun in runs) {
            val run = rawRun as Element
            if (run.name.equals("r", true)) {
                val t = run.element("t") ?: continue
                val text = t.text
                leaf = LeafElement(text)
                RunAttr.instance().setRunAttribute(sheet!!, run.element("rPr"), leaf.getAttribute(), attrLayout)
                leaf.setStartOffset(offset.toLong())
                offset += text.length
                leaf.setEndOffset(offset.toLong())
                paragraph.appendLeaf(leaf)
            } else if (run.name.equals("br", true)) {
                if (leaf != null) {
                    leaf.setText(leaf.getText(null) + "\n")
                    offset++
                }
                paragraph.setEndOffset(offset.toLong())
                section.appendParagraph(paragraph, WPModelConstant.MAIN)
                val next = ParagraphElement()
                next.setStartOffset(offset.toLong())
                ParaAttr.instance().setParaAttribute(control, p.element("pPr"), next.getAttribute(), null, -1, -1, 0, false, false)
                return processRun(control, section, next, p, null)
            }
        }
        if (leaf != null) {
            leaf.setText(leaf.getText(null) + "\n")
            offset++
        }
        return paragraph
    }
}
