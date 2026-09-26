/*
 * 文件名称:          SectionElementFactory.java
 *
 * 编译器:            android2.2
 * 时间:              下午3:46:16
 */
package com.wxiwei.office.ss.util

import com.wxiwei.office.constant.MainConstant
import com.wxiwei.office.constant.SSConstant
import com.wxiwei.office.constant.wp.WPAttrConstant
import com.wxiwei.office.constant.wp.WPModelConstant
import com.wxiwei.office.fc.hssf.record.common.UnicodeString
import com.wxiwei.office.fc.hssf.record.common.UnicodeString.FormatRun
import com.wxiwei.office.fc.hssf.usermodel.HSSFTextbox
import com.wxiwei.office.fc.ppt.attribute.ParaAttr
import com.wxiwei.office.fc.ppt.attribute.RunAttr
import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.simpletext.model.AttrManage
import com.wxiwei.office.simpletext.model.AttributeSetImpl
import com.wxiwei.office.simpletext.model.LeafElement
import com.wxiwei.office.simpletext.model.ParagraphElement
import com.wxiwei.office.simpletext.model.SectionElement
import com.wxiwei.office.ss.model.baseModel.Cell
import com.wxiwei.office.ss.model.baseModel.Workbook
import com.wxiwei.office.ss.model.style.CellStyle

/**
 * TODO: 文件注释
 *
 * Read版本:        Read V1.0
 *
 * 作者:            jqin
 *
 * 日期:            2013-1-25
 *
 * 负责人:           jqin
 *
 * 负责小组:
 */
class SectionElementFactory {
    companion object {
        @JvmStatic
        fun getSectionElement(workbook: Workbook?, unicodeString: UnicodeString, cell: Cell): SectionElement? {
            book = workbook

            val cellStyle = cell.getCellStyle()!!

            var secElem: SectionElement? = SectionElement()
            // 开始Offset
            secElem!!.setStartOffset(0)
            // 属性
            val attr = secElem.getAttribute()

            // 左边距
            AttrManage.instance().setPageMarginLeft(attr, Math.round(SSConstant.SHEET_SPACETOBORDER * MainConstant.PIXEL_TO_TWIPS))
            // 右边距
            AttrManage.instance().setPageMarginRight(attr, Math.round(SSConstant.SHEET_SPACETOBORDER * MainConstant.PIXEL_TO_TWIPS))
            // 上边距
            AttrManage.instance().setPageMarginTop(attr, 0)
            // 下边框
            AttrManage.instance().setPageMarginBottom(attr, 0)

            val verAlign: Byte
            when (cellStyle.getVerticalAlign()) {
                CellStyle.VERTICAL_TOP -> verAlign = WPAttrConstant.PAGE_V_TOP
                CellStyle.VERTICAL_CENTER -> verAlign = WPAttrConstant.PAGE_V_CENTER
                CellStyle.VERTICAL_JUSTIFY -> verAlign = WPAttrConstant.PAGE_V_JUSTIFIED
                CellStyle.VERTICAL_BOTTOM -> verAlign = WPAttrConstant.PAGE_V_BOTTOM
                else -> verAlign = WPAttrConstant.PAGE_V_TOP
            }
            AttrManage.instance().setPageVerticalAlign(attr, verAlign)
            //font id
            val font = cellStyle.getFontIndex().toInt()

            offset = 0
            val pos = processParagraph(secElem, unicodeString, cellStyle, cell)
            if (pos != 0) {
                secElem.setEndOffset(pos.toLong())
            } else {
                secElem.dispose()
                secElem = null
            }

            dispose()

            return secElem
        }

        @JvmStatic
        fun getSectionElement(workbook: Workbook?, textbox: HSSFTextbox, rect: Rectangle?): SectionElement {
            book = workbook

            // ======== 处理文本 ========
            // 建立章节
            val secElem = SectionElement()
            // 开始Offset
            secElem.setStartOffset(0)
            // 属性
            val attr = secElem.getAttribute()
            // 宽度
            AttrManage.instance().setPageWidth(attr, Math.round(rect!!.width * MainConstant.PIXEL_TO_TWIPS))
            // 高度
            AttrManage.instance().setPageHeight(attr, Math.round(rect.height * MainConstant.PIXEL_TO_TWIPS))
            //
            AttrManage.instance().setPageMarginLeft(attr, Math.round(textbox.getMarginLeft() * MainConstant.PIXEL_TO_TWIPS))
            //
            AttrManage.instance().setPageMarginTop(attr, Math.round(textbox.getMarginTop() * MainConstant.PIXEL_TO_TWIPS))
            AttrManage.instance().setPageMarginRight(attr, Math.round(textbox.getMarginRight() * MainConstant.PIXEL_TO_TWIPS))
            AttrManage.instance().setPageMarginBottom(attr, Math.round(textbox.getMarginBottom() * MainConstant.PIXEL_TO_TWIPS))

            var valign: Byte = 0
            when (textbox.getVerticalAlignment()) {
                HSSFTextbox.VERTICAL_ALIGNMENT_TOP -> valign = WPAttrConstant.PAGE_V_TOP
                HSSFTextbox.VERTICAL_ALIGNMENT_CENTER,
                HSSFTextbox.VERTICAL_ALIGNMENT_JUSTIFY,
                HSSFTextbox.VERTICAL_ALIGNMENT_DISTRIBUTED -> valign = WPAttrConstant.PAGE_V_CENTER
                HSSFTextbox.VERTICAL_ALIGNMENT_BOTTOM -> valign = WPAttrConstant.PAGE_V_BOTTOM
            }
            AttrManage.instance().setPageVerticalAlign(attr, valign)

            val pos = processParagraph(secElem, textbox)
            secElem.setEndOffset(pos.toLong())

            dispose()

            return secElem
        }

        private fun processParagraph(secElem: SectionElement, unicodeString: UnicodeString, cellStyle: CellStyle, cell: Cell): Int {
            offset = 0
            val text = unicodeString.getString()

            var halign: Byte = 0
            when (cellStyle.getHorizontalAlign()) {
                CellStyle.ALIGN_LEFT -> halign = WPAttrConstant.PARA_HOR_ALIGN_LEFT
                CellStyle.ALIGN_CENTER,
                CellStyle.ALIGN_JUSTIFY,
                CellStyle.ALIGN_CENTER_SELECTION -> halign = WPAttrConstant.PARA_HOR_ALIGN_CENTER
                CellStyle.ALIGN_RIGHT -> halign = WPAttrConstant.PARA_HOR_ALIGN_RIGHT
            }

            paraElem = ParagraphElement()
            paraElem!!.setStartOffset(offset.toLong())
            attrLayout = AttributeSetImpl()
            ParaAttr.instance().setParaAttribute(cellStyle, paraElem!!.getAttribute(), attrLayout)
            AttrManage.instance().setParaHorizontalAlign(paraElem!!.getAttribute(), halign.toInt())

            if (unicodeString.getFormatRunCount() == 0) {
                processParagraph_SubString(
                    secElem,
                    cellStyle,
                    text,
                    cellStyle.getFontIndex().toInt(),
                    halign
                )
            } else {
                val iter: Iterator<FormatRun> = unicodeString.formatIterator()
                var begin: FormatRun? = null
                var end: FormatRun = iter.next()
                //first
                var subString = text.substring(0, end.getCharacterPos().toInt())
                if (!cellStyle.isWrapText()) {
                    subString = subString.replace("\n", "")
                }

                processParagraph_SubString(
                    secElem,
                    cellStyle,
                    subString,
                    cellStyle.getFontIndex().toInt(),
                    halign
                )

                begin = end

                //middle
                while (iter.hasNext()) {
                    end = iter.next()
                    if (end.getCharacterPos() > text.length) {
                        break
                    }

                    subString = text.substring(begin!!.getCharacterPos().toInt(), end.getCharacterPos().toInt())
                    if (!cellStyle.isWrapText()) {
                        subString = subString.replace("\n", "")
                    }

                    processParagraph_SubString(
                        secElem,
                        cellStyle,
                        subString,
                        begin.getFontIndex().toInt(),
                        halign
                    )

                    begin = end
                }

                //last
                subString = text.substring(begin!!.getCharacterPos().toInt())
                if (!cellStyle.isWrapText()) {
                    subString = subString.replace("\n", "")
                }

                processParagraph_SubString(
                    secElem,
                    cellStyle,
                    subString,
                    begin.getFontIndex().toInt(),
                    halign
                )

                if (leaf != null) {
                    leaf!!.setText(leaf!!.getText(null) + "\n")
                    offset++
                }
            }

            // 结束 offset
            if (leaf != null && paraElem!!.getLeaf(leaf!!.getStartOffset()) == null) {
                leaf!!.setEndOffset(offset.toLong())
                paraElem!!.appendLeaf(leaf)
            }

            if (paraElem != null && secElem.getElement(paraElem!!.getStartOffset()) == null) {
                paraElem!!.setEndOffset(offset.toLong())
                secElem.appendParagraph(paraElem, WPModelConstant.MAIN)
            }

            return offset
        }

        private fun processParagraph_SubString(secElem: SectionElement, cellStyle: CellStyle?, subString: String, fontIndex: Int, hAlign: Byte) {
            var subString: String? = subString
            if (!subString!!.contains("\n")) {
                leaf = LeafElement(subString)
                // 属性
                RunAttr.instance().setRunAttribute(book!!, fontIndex, null, leaf!!.getAttribute(), attrLayout)
                // 开始 offset
                leaf!!.setStartOffset(offset.toLong())
                offset += subString.length
                // 结束 offset
                leaf!!.setEndOffset(offset.toLong())
                paraElem!!.appendLeaf(leaf)
            } else {
                var index = subString.indexOf('\n')
                while (index >= 0) {
                    offset = processBreakLine(secElem, cellStyle, fontIndex, hAlign, subString!!.substring(0, index), true)
                    if (index + 1 < subString.length) {
                        subString = subString.substring(index + 1, subString.length)
                        index = subString.indexOf('\n')
                    } else {
                        subString = null
                        break
                    }
                }

                if (subString != null) {
                    offset = processBreakLine(secElem, cellStyle, fontIndex, hAlign, subString, true)
                }
            }
        }

        private fun processParagraph(secElem: SectionElement, textbox: HSSFTextbox): Int {
            offset = 0
            val richText = textbox.getString()
            val text = richText.getString()

            var halign: Byte = 0
            when (textbox.getHorizontalAlignment()) {
                HSSFTextbox.HORIZONTAL_ALIGNMENT_LEFT -> halign = WPAttrConstant.PARA_HOR_ALIGN_LEFT
                HSSFTextbox.HORIZONTAL_ALIGNMENT_CENTERED,
                HSSFTextbox.HORIZONTAL_ALIGNMENT_JUSTIFIED,
                HSSFTextbox.HORIZONTAL_ALIGNMENT_DISTRIBUTED -> halign = WPAttrConstant.PARA_HOR_ALIGN_CENTER
                HSSFTextbox.HORIZONTAL_ALIGNMENT_RIGHT -> halign = WPAttrConstant.PARA_HOR_ALIGN_RIGHT
            }

            paraElem = ParagraphElement()
            paraElem!!.setStartOffset(offset.toLong())
            attrLayout = AttributeSetImpl()
            AttrManage.instance().setParaHorizontalAlign(paraElem!!.getAttribute(), halign.toInt())

            val iter: Iterator<FormatRun> = richText.getUnicodeString().formatIterator()
            var begin: FormatRun = iter.next()
            //别忘了简单格式的情况，即没有end
            var end: FormatRun? = null
            while (iter.hasNext()) {
                end = iter.next()
                if (end.getCharacterPos() > text.length) {
                    break
                }

                processParagraph_SubString(
                    secElem,
                    null,
                    text.substring(begin.getCharacterPos().toInt(), end.getCharacterPos().toInt()),
                    begin.getFontIndex().toInt(),
                    halign
                )

                begin = end
            }

            processParagraph_SubString(secElem, null, text.substring(begin.getCharacterPos().toInt()), begin.getFontIndex().toInt(), halign)

            if (leaf != null && paraElem!!.getLeaf(leaf!!.getStartOffset()) == null) {
                leaf!!.setText(leaf!!.getText(null) + "\n")
                offset++
                leaf!!.setEndOffset(offset.toLong())
                paraElem!!.appendLeaf(leaf)
            }

            if (paraElem != null && secElem.getElement(paraElem!!.getStartOffset()) == null) {
                paraElem!!.setEndOffset(offset.toLong())
                secElem.appendParagraph(paraElem, WPModelConstant.MAIN)
            }

            return offset
        }

        /**
         *
         * @param cell
         * @param secElem
         * @param paraElem
         * @param attrLayout
         * @param fontID
         * @param r
         * @param leaf
         */
        private fun processBreakLine(secElem: SectionElement, cellStyle: CellStyle?, fontID: Int, hAlign: Byte, text: String?, paraEnd: Boolean): Int {
            if (text == null || text.length == 0) {
                if (leaf != null) {
                    //Text Line Break, for last paragrapha
                    leaf!!.setText(leaf!!.getText(null) + "\n")
                    offset++
                    leaf!!.setEndOffset(offset.toLong())
                } else {
                    //new paragraph that only has '\n'
                    leaf = LeafElement("\n")
                    // 属性
                    RunAttr.instance().setRunAttribute(book!!, fontID, null, leaf!!.getAttribute(), attrLayout)
                    // 开始 offset
                    leaf!!.setStartOffset(offset.toLong())
                    offset++
                    leaf!!.setEndOffset(offset.toLong())
                    paraElem!!.appendLeaf(leaf)
                }
                paraElem!!.setEndOffset(offset.toLong())
                secElem.appendParagraph(paraElem, WPModelConstant.MAIN)

                //new paragraph that only has '\n'
                paraElem = ParagraphElement()
                paraElem!!.setStartOffset(offset.toLong())
                attrLayout = AttributeSetImpl()
                ParaAttr.instance().setParaAttribute(cellStyle, paraElem!!.getAttribute(), attrLayout)
                AttrManage.instance().setParaHorizontalAlign(paraElem!!.getAttribute(), hAlign.toInt())
                leaf = null
            } else {
                leaf = LeafElement(text)
                // 属性
                RunAttr.instance().setRunAttribute(book!!, fontID, null, leaf!!.getAttribute(), attrLayout)
                // 开始 offset
                leaf!!.setStartOffset(offset.toLong())
                offset += text.length
                // 结束 offset
                leaf!!.setEndOffset(offset.toLong())
                paraElem!!.appendLeaf(leaf)

                if (paraEnd) {
                    leaf!!.setText(leaf!!.getText(null) + "\n")
                    offset++
                    leaf!!.setEndOffset(offset.toLong())
                    paraElem!!.setEndOffset(offset.toLong())
                    secElem.appendParagraph(paraElem, WPModelConstant.MAIN)

                    //new paragraph
                    paraElem = ParagraphElement()
                    paraElem!!.setStartOffset(offset.toLong())
                    attrLayout = AttributeSetImpl()
                    ParaAttr.instance().setParaAttribute(cellStyle, paraElem!!.getAttribute(), attrLayout)
                    AttrManage.instance().setParaHorizontalAlign(paraElem!!.getAttribute(), hAlign.toInt())
                    leaf = null
                }
            }

            return offset
        }

        private fun dispose() {
            leaf = null
            paraElem = null
            book = null
            offset = 0
            attrLayout = null
        }

        private var book: Workbook? = null
        private var offset = 0
        private var paraElem: ParagraphElement? = null
        private var attrLayout: AttributeSetImpl? = null
        private var leaf: LeafElement? = null
    }
}
