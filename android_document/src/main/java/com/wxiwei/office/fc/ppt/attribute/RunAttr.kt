/*
 * 文件名称:           RunArrt.java
 *  
 * 编译器:             android2.2
 * 时间:               下午2:21:14
 */
package com.wxiwei.office.fc.ppt.attribute

import com.wxiwei.office.fc.ppt.reader.pptXmlBoolean
import android.graphics.Color
import com.wxiwei.office.constant.wp.AttrIDConstant
import com.wxiwei.office.fc.dom4j.Element
import com.wxiwei.office.fc.ppt.reader.HyperlinkReader
import com.wxiwei.office.fc.ppt.reader.ReaderKit
import com.wxiwei.office.fc.xls.Reader.SchemeColorUtil
import com.wxiwei.office.pg.model.PGMaster
import com.wxiwei.office.simpletext.font.Font
import com.wxiwei.office.simpletext.font.FontTypefaceManage
import com.wxiwei.office.simpletext.model.AttrManage
import com.wxiwei.office.simpletext.model.IAttributeSet
import com.wxiwei.office.simpletext.model.LeafElement
import com.wxiwei.office.simpletext.model.ParagraphElement
import com.wxiwei.office.simpletext.model.StyleManage
import com.wxiwei.office.ss.model.baseModel.Cell
import com.wxiwei.office.ss.model.baseModel.Sheet
import com.wxiwei.office.ss.model.baseModel.Workbook
import com.wxiwei.office.ss.util.ColorUtil
import com.wxiwei.office.ss.util.format.NumericFormatter
import java.util.Date

/**
 * 管理text run 属性
 * 
 * 
 * 
 * 
 * Read版本:       Read V1.0
 * 
 * 
 * 作者:           jhy1790
 * 
 * 
 * 日期:           2012-3-12
 * 
 * 
 * 负责人:         jhy1790
 * 
 * 
 * 负责小组:
 * 
 * 
 * 
 * 
 */
class RunAttr {
    /**
     * 
     * @param pgMaster
     * @param paraElem
     * @param p
     * @param attrLayout
     * @param offset
     * @param fontScale
     * @return
     */
    fun processRun(
        pgMaster: PGMaster?, paraElem: ParagraphElement,
        p: Element, attrLayout: IAttributeSet?, offset: Int, fontScale: Int, styleID: Int
    ): Int {
        var offset = offset
        maxFontSize = 0
        var leaf: LeafElement? = null
        var pPr = p.element("pPr")
        // 如果没有 r 元素，说明只有一个回车符的段落
        if (p.elements("r").size == 0 && p.elements("fld").size == 0 && p.elements("br").size == 0) {
            leaf = LeafElement("\n")
            // 属性
            if (pPr != null) {
                pPr = pPr.element("rPr")
            }
            if (pPr == null) {
                pPr = p.element("endParaRPr")
            }
            setRunAttribute(
                pgMaster,
                pPr,
                leaf.getAttribute(),
                attrLayout,
                fontScale,
                styleID,
                true
            )
            // set max font size
            setMaxFontSize(
                AttrManage.instance().getFontSize(paraElem.getAttribute(), leaf.getAttribute())
            )
            leaf.setStartOffset(offset.toLong())
            offset++
            leaf.setEndOffset(offset.toLong())
            paraElem.appendLeaf(leaf)
            return offset
        }
        val it = p.elementIterator()
        while (it.hasNext()) {
            val r = it.next() as Element
            val name = r.getName()
            if (name == "r" || name == "fld" || name == "br") {
                var text: String? = null
                if (name == "fld"
                    && r.attributeValue("type") != null && r.attributeValue("type")
                        .contains("datetime")
                ) {
                    //field code : current time
                    text = NumericFormatter.instance()
                        .getFormatContents("yyyy/m/d", Date(System.currentTimeMillis()))
                } else {
                    val t = r.element("t")
                    if (name == "br") {
                        text = '\u000b'.toString()
                    } else if (t != null) {
                        text = t.getText()
                    }
                }

                if (text != null) {
                    text = text.replace(160.toChar(), ' ')
                    val len = text.length
                    if (len > 0) {
                        leaf = LeafElement(text)
                        // 属性
                        setRunAttribute(
                            pgMaster, r.element("rPr"), leaf.getAttribute(), attrLayout,
                            fontScale, styleID, "\n" == text
                        )
                        // set max font size
                        setMaxFontSize(
                            AttrManage.instance()
                                .getFontSize(paraElem.getAttribute(), leaf.getAttribute())
                        )
                        // 开始 offset
                        leaf.setStartOffset(offset.toLong())
                        offset += len
                        // 结束 offset
                        leaf.setEndOffset(offset.toLong())
                        paraElem.appendLeaf(leaf)
                    }
                }
            }
        }
        if (leaf != null) {
            leaf.setText(leaf.getText(null) + "\n")
            offset++
        }
        return offset
    }

    /**
     * 字号
     */
    private fun setFontSize(attrFrom: IAttributeSet?, attrTo: IAttributeSet) {
        if (attrFrom != null) {
            if (AttrManage.instance().hasAttribute(attrFrom, AttrIDConstant.FONT_SIZE_ID)) {
                AttrManage.instance()
                    .setFontSize(attrTo, AttrManage.instance().getFontSize(null, attrFrom))
            }
        }
    }

    /**
     * 字体
     */
    private fun setFontTypeface(attrFrom: IAttributeSet?, attrTo: IAttributeSet) {
        if (attrFrom != null) {
            if (AttrManage.instance().hasAttribute(attrFrom, AttrIDConstant.FONT_NAME_ID)) {
                AttrManage.instance()
                    .setFontName(attrTo, AttrManage.instance().getFontName(null, attrFrom))
            }
        }
    }

    /**
     * 字符颜色
     */
    private fun setFontColor(attrFrom: IAttributeSet?, attrTo: IAttributeSet) {
        if (attrFrom != null) {
            if (AttrManage.instance().hasAttribute(attrFrom, AttrIDConstant.FONT_COLOR_ID)) {
                AttrManage.instance()
                    .setFontColor(attrTo, AttrManage.instance().getFontColor(null, attrFrom))
            }
        }
    }

    /**
     * 粗体
     */
    private fun setFontBold(attrFrom: IAttributeSet?, attrTo: IAttributeSet) {
        if (attrFrom != null) {
            if (AttrManage.instance().hasAttribute(attrFrom, AttrIDConstant.FONT_BOLD_ID)) {
                AttrManage.instance()
                    .setFontBold(attrTo, AttrManage.instance().getFontBold(null, attrFrom))
            }
        }
    }

    /**
     * 斜体
     */
    private fun setFontItalic(attrFrom: IAttributeSet?, attrTo: IAttributeSet) {
        if (attrFrom != null) {
            if (AttrManage.instance().hasAttribute(attrFrom, AttrIDConstant.FONT_ITALIC_ID)) {
                AttrManage.instance()
                    .setFontItalic(attrTo, AttrManage.instance().getFontItalic(null, attrFrom))
            }
        }
    }

    /**
     * 删除线
     */
    private fun setFontStrike(attrFrom: IAttributeSet?, attrTo: IAttributeSet) {
        if (attrFrom != null) {
            if (AttrManage.instance().hasAttribute(attrFrom, AttrIDConstant.FONT_STRIKE_ID)) {
                AttrManage.instance()
                    .setFontStrike(attrTo, AttrManage.instance().getFontStrike(null, attrFrom))
            }
        }
    }

    /**
     * 双删除线
     */
    private fun setFontDoubleStrike(attrFrom: IAttributeSet?, attrTo: IAttributeSet) {
        if (attrFrom != null) {
            if (AttrManage.instance().hasAttribute(attrFrom, AttrIDConstant.FONT_DOUBLESTRIKE_ID)) {
                AttrManage.instance().setFontDoubleStrike(
                    attrTo,
                    AttrManage.instance().getFontDoubleStrike(null, attrFrom)
                )
            }
        }
    }

    /**
     * 下划线
     */
    private fun setFontUnderline(attrFrom: IAttributeSet?, attrTo: IAttributeSet) {
        if (attrFrom != null) {
            if (AttrManage.instance().hasAttribute(attrFrom, AttrIDConstant.FONT_UNDERLINE_ID)) {
                AttrManage.instance().setFontUnderline(
                    attrTo,
                    AttrManage.instance().getFontUnderline(null, attrFrom)
                )
                if (AttrManage.instance()
                        .hasAttribute(attrFrom, AttrIDConstant.FONT_UNDERLINE_COLOR_ID)
                ) {
                    AttrManage.instance().setFontUnderlineColr(
                        attrTo,
                        AttrManage.instance().getFontUnderlineColor(null, attrFrom)
                    )
                } else {
                    if (AttrManage.instance()
                            .hasAttribute(attrFrom, AttrIDConstant.FONT_COLOR_ID)
                    ) {
                        AttrManage.instance().setFontUnderlineColr(
                            attrTo,
                            AttrManage.instance().getFontColor(null, attrFrom)
                        )
                    }
                }
            }
        }
    }

    /**
     * 上下标
     */
    private fun setFontScript(attrFrom: IAttributeSet?, attrTo: IAttributeSet) {
        if (attrFrom != null) {
            if (AttrManage.instance().hasAttribute(attrFrom, AttrIDConstant.FONT_SCRIPT_ID)) {
                AttrManage.instance()
                    .setFontScript(attrTo, AttrManage.instance().getFontScript(null, attrFrom))
            }
        }
    }

    /**
     * 超链接
     */
    private fun setHyperlinkID(attrFrom: IAttributeSet?, attrTo: IAttributeSet) {
        if (attrFrom != null) {
            if (AttrManage.instance().hasAttribute(attrFrom, AttrIDConstant.FONT_HYPERLINK_ID)) {
                AttrManage.instance()
                    .setHyperlinkID(attrTo, AttrManage.instance().getHperlinkID(attrFrom))
            }
        }
    }

    /**
     * set text run attribute
     */
    fun setRunAttribute(
        master: PGMaster?, rPr: Element?, attr: IAttributeSet, attrLayout: IAttributeSet?,
        fontScale: Int, styleID: Int, newLine: Boolean
    ) {
        if (rPr != null) {
            var `val`: String?
            // 字号
            if (rPr.attribute("sz") != null) {
                `val` = rPr.attributeValue("sz")
                if (`val` != null && `val`.length > 0) {
                    AttrManage.instance().setFontSize(attr, (`val`.toFloat() / 100).toInt())
                }
            } else {
                setFontSize(attrLayout, attr)
            }

            if (!newLine) {
                // 字体
                var temp = rPr.element("latin")
                if (temp != null || rPr.element("ea") != null) {
                    if (temp == null) {
                        temp = rPr.element("ea")
                    }
                    `val` = temp.attributeValue("typeface")
                    if (`val` != null) {
                        val index = FontTypefaceManage.instance().addFontName(`val`)
                        if (index >= 0) {
                            AttrManage.instance().setFontName(attr, index)
                        }
                    }
                } else {
                    setFontTypeface(attrLayout, attr)
                }


                // 字符颜色
                temp = rPr.element("solidFill")
                var fontColor: Int? = null
                if (temp != null) {
                    fontColor = ReaderKit.instance().getColor(master, temp)
                    AttrManage.instance().setFontColor(attr, fontColor)
                } else if ((rPr.element("gradFill").also { temp = it }) != null) {
                    val gsLst = temp!!.element("gsLst")
                    if (gsLst != null) {
                        fontColor = ReaderKit.instance().getColor(master, gsLst.element("gs"))
                        AttrManage.instance().setFontColor(attr, fontColor)
                    }
                } else {
                    setFontColor(attrLayout, attr)
                }


                // 粗体
                if (rPr.attribute("b") != null) {
                    `val` = rPr.attributeValue("b")
                    if (`val` != null && `val`.length > 0 && pptXmlBoolean(`val`)) {
                        AttrManage.instance().setFontBold(attr, true)
                    }
                } else {
                    setFontBold(attrLayout, attr)
                }


                // 斜体
                if (rPr.attribute("i") != null) {
                    `val` = rPr.attributeValue("i")
                    if (`val` != null && `val`.length > 0) {
                        AttrManage.instance().setFontItalic(attr, pptXmlBoolean(`val`))
                    }
                } else {
                    setFontItalic(attrLayout, attr)
                }


                // 下划线
                if (rPr.attribute("u") != null) {
                    `val` = rPr.attributeValue("u")
                    if (`val` != null && `val`.length > 0) {
                        if (!`val`.equals("none", ignoreCase = true)) {
                            AttrManage.instance().setFontUnderline(attr, 1)

                            val uFill = rPr.element("uFill")
                            if (uFill != null && (uFill.element("solidFill")
                                    .also { temp = it }) != null
                            ) {
                                AttrManage.instance().setFontUnderlineColr(
                                    attr,
                                    ReaderKit.instance().getColor(master, temp)
                                )
                            } else {
                                if (fontColor != null) {
                                    AttrManage.instance().setFontUnderlineColr(attr, fontColor)
                                }
                            }
                        }
                    }
                } else {
                    setFontUnderline(attrLayout, attr)
                }


                // 删除线
                if (rPr.attribute("strike") != null) {
                    `val` = rPr.attributeValue("strike")
                    if (`val` == "dblStrike") {
                        // 双删除线
                        AttrManage.instance().setFontDoubleStrike(attr, true)
                    } else if (`val` == "sngStrike") {
                        AttrManage.instance().setFontStrike(attr, true)
                    }
                } else {
                    setFontStrike(attrLayout, attr)
                    setFontDoubleStrike(attrLayout, attr)
                }


                // 上下标
                if (rPr.attribute("baseline") != null) {
                    `val` = rPr.attributeValue("baseline")
                    if (`val` != null && `val`.length > 0) {
                        val value = `val`.toInt()
                        if (value != 0) {
                            AttrManage.instance().setFontScript(attr, if (value > 0) 1 else 2)
                        }
                    }
                } else {
                    setFontScript(attrLayout, attr)
                }


                // hyperlink
                temp = rPr.element("hlinkClick")
                if (temp != null) {
                    var color = Color.BLUE
                    if (master != null) {
                        color = master.getSchemeColor()!!.get("hlink")!!
                    }
                    AttrManage.instance().setFontColor(attr, color)
                    AttrManage.instance().setFontUnderline(attr, 1)
                    AttrManage.instance().setFontUnderlineColr(attr, color)

                    `val` = temp.attributeValue("id")
                    if (`val` != null && `val`.length > 0) {
                        AttrManage.instance()
                            .setHyperlinkID(attr, HyperlinkReader.instance().getLinkIndex(`val`))
                    }
                } else {
                    setHyperlinkID(attrLayout, attr)
                }
            }
        } else if (attrLayout != null) {
            setFontSize(attrLayout, attr)
            if (!newLine) {
                setFontTypeface(attrLayout, attr)
                setFontColor(attrLayout, attr)
                setFontBold(attrLayout, attr)
                setFontItalic(attrLayout, attr)
                setFontUnderline(attrLayout, attr)
                setFontStrike(attrLayout, attr)
                setFontDoubleStrike(attrLayout, attr)
                setFontScript(attrLayout, attr)
                setHyperlinkID(attrLayout, attr)
            }
        }
        AttrManage.instance().setFontScale(attr, fontScale)


        // set default font size 18
        if (!AttrManage.instance().hasAttribute(attr, AttrIDConstant.FONT_SIZE_ID)) {
            val style = StyleManage.instance().getStyle(styleID)
            if (style != null && style.getAttrbuteSet() != null && AttrManage.instance()
                    .hasAttribute(style.getAttrbuteSet(), AttrIDConstant.FONT_SIZE_ID)
            ) {
                return
            } else {
                if (!this.table && this.slide) {
                    AttrManage.instance().setFontSize(attr, 18)
                }
            }
        }
    }

    /**
     * xlsx shared item color
     * @param book
     * @param clr
     * @return
     */
    private fun getRunPropColor(book: Workbook, clr: Element): Int {
        var color = -1
        val `val`: String
        if (clr.attributeValue("indexed") != null) {
            `val` = clr.attributeValue("indexed")
            color = book.getColor(`val`.toInt())
        } else if (clr.attributeValue("theme") != null) {
            `val` = clr.attributeValue("theme")
            //get scheme color
            color = SchemeColorUtil.getThemeColor(book, `val`.toInt())
        } else if (clr.attributeValue("rgb") != null) {
            `val` = clr.attributeValue("rgb")
            //get system color
            color = `val`.toLong(16).toInt()
        }

        if (clr.attributeValue("tint") != null) {
            val tint = clr.attributeValue("tint").toDouble()
            color = ColorUtil.instance().getColorWithTint(color, tint)
        }

        return color
    }

    /**
     * 
     * @param themeColor
     * @param solidFillElement
     * @return
     */
    fun getColor(book: Workbook?, solidFillElement: Element): Int {
        val `val`: String?
        val clr: Element
        var color = -1
        if (solidFillElement.element("srgbClr") != null) {
            clr = solidFillElement.element("srgbClr")
            color = clr.attributeValue("val").toLong(16).toInt()
            color = (0xFF shl 24) or color
        } else if (solidFillElement.element("schemeClr") != null) {
            clr = solidFillElement.element("schemeClr")
            //get scheme color
            val schemeColor = SchemeColorUtil.getSchemeColor(book!!)
            color = schemeColor.get(clr.attributeValue("val"))!!

            if (clr.element("tint") != null) {
                color = ColorUtil.instance().getColorWithTint(
                    color,
                    clr.element("tint").attributeValue("val").toInt() / 100000.0
                )
            } else if (clr.element("lumOff") != null) {
                color = ColorUtil.instance().getColorWithTint(
                    color,
                    clr.element("lumOff").attributeValue("val").toInt() / 100000.0
                )
            } else if (clr.element("lumMod") != null) {
                color = ColorUtil.instance().getColorWithTint(
                    color,
                    clr.element("lumMod").attributeValue("val").toInt() / 100000.0 - 1
                )
            } else if (clr.element("shade") != null) {
                color = ColorUtil.instance().getColorWithTint(
                    color,
                    -clr.element("shade").attributeValue("val").toInt() / 200000.0
                )
            }

            if (clr.element("alpha") != null) {
                `val` = clr.element("alpha").attributeValue("val")
                if (`val` != null) {
                    val alpha = (`val`.toInt() / 100000f * 255).toInt()
                    color = (0xFFFFFF and color) or (alpha shl 24)
                }
            }
        } else if (solidFillElement.element("sysClr") != null) {
            clr = solidFillElement.element("sysClr")
            //get system color
            color = clr.attributeValue("lastClr").toInt(16)
            color = (0xFF shl 24) or color
        }
        return color
    }

    /**
     * just for sheet xml parsed
     * @param sheet
     * @param rPr
     * @param attr
     * @param attrLayout
     */
    fun setRunAttribute(
        sheet: Sheet,
        rPr: Element?,
        attr: IAttributeSet,
        attrLayout: IAttributeSet?
    ) {
        if (rPr != null) {
            var `val`: String?
            // 字号
            if (rPr.attribute("sz") != null) {
                `val` = rPr.attributeValue("sz")
                if (`val` != null && `val`.length > 0) {
                    AttrManage.instance().setFontSize(attr, (`val`.toFloat() / 100).toInt())
                }
            } else {
                setFontSize(attrLayout, attr)
            }


            // 字符颜色
            var temp = rPr.element("solidFill")
            if (temp != null) {
                AttrManage.instance().setFontColor(attr, getColor(sheet.getWorkbook(), temp))
            } else {
                setFontColor(attrLayout, attr)
            }


            // 粗体
            if (rPr.attribute("b") != null) {
                AttrManage.instance()
                    .setFontBold(attr, pptXmlBoolean(rPr.attributeValue("b")))
            } else {
                setFontBold(attrLayout, attr)
            }


            // 斜体
            if (rPr.attribute("i") != null) {
                AttrManage.instance()
                    .setFontItalic(attr, pptXmlBoolean(rPr.attributeValue("i")))
            } else {
                setFontItalic(attrLayout, attr)
            }


            // 下划线
            if (rPr.attributeValue("u") != null && !rPr.attributeValue("u")
                    .equals("none", ignoreCase = true)
            ) {
                AttrManage.instance().setFontUnderline(attr, 1)
                val uFill = rPr.element("uFill")
                if (uFill != null) {
                    temp = uFill.element("solidFill")
                    if (temp != null) {
                        AttrManage.instance()
                            .setFontUnderlineColr(attr, getColor(sheet.getWorkbook(), temp))
                    }
                }
            } else {
                setFontUnderline(attrLayout, attr)
            }


            // 删除线
            if (rPr.attribute("strike") != null) {
                `val` = rPr.attributeValue("strike")
                if (`val` == "dblStrike") {
                    // 双删除线
                    AttrManage.instance().setFontDoubleStrike(attr, true)
                } else if (`val` == "sngStrike") {
                    AttrManage.instance().setFontStrike(attr, true)
                }
            } else {
                setFontStrike(attrLayout, attr)
                setFontDoubleStrike(attrLayout, attr)
            }


            // 上下标
            if (rPr.attribute("baseline") != null) {
                `val` = rPr.attributeValue("baseline")
                if (`val` != null && !`val`.equals("0", ignoreCase = true)) {
                    AttrManage.instance().setFontScript(attr, if (`val`.toInt() > 0) 1 else 2)
                }
            } else {
                setFontScript(attrLayout, attr)
            }


            // hyperlink
            temp = rPr.element("hlinkClick")
            if (temp != null && temp.attribute("id") != null) {
                `val` = temp.attributeValue("id")
                if (`val` != null && `val`.length > 0) {
                    AttrManage.instance().setFontColor(attr, Color.BLUE)
                    AttrManage.instance().setFontUnderline(attr, 1)
                    AttrManage.instance().setFontUnderlineColr(attr, Color.BLUE)
                    AttrManage.instance()
                        .setHyperlinkID(attr, HyperlinkReader.instance().getLinkIndex(`val`))
                }
            } else {
                setHyperlinkID(attrLayout, attr)
            }
        } else if (attrLayout != null) {
            setFontSize(attrLayout, attr)
            setFontColor(attrLayout, attr)
            setFontBold(attrLayout, attr)
            setFontItalic(attrLayout, attr)
            setFontUnderline(attrLayout, attr)
            setFontStrike(attrLayout, attr)
            setFontDoubleStrike(attrLayout, attr)
            setFontScript(attrLayout, attr)
            setHyperlinkID(attrLayout, attr)
        }
    }

    /**
     * just for xlsx shared item parsed
     * @param sheet
     * @param rPr
     * @param attr
     * @param attrLayout
     */
    fun setRunAttribute(
        book: Workbook,
        fontID: Int,
        rPr: Element?,
        attr: IAttributeSet,
        attrLayout: IAttributeSet?
    ) {
        if (rPr != null) {
            var `val`: String?
            //           Font font = book.getFont(fontID);
            var temp = rPr.element("sz")
            // 字号
            if (temp != null) {
                `val` = temp.attributeValue("val")
                if (`val` != null && `val`.length > 0) {
                    AttrManage.instance().setFontSize(attr, `val`.toFloat().toInt())
                }
            } else {
                setFontSize(attrLayout, attr)
            }


            // 字符颜色
            temp = rPr.element("color")
            if (temp != null) {
                AttrManage.instance().setFontColor(attr, getRunPropColor(book, temp))
            } else {
                setFontColor(attrLayout, attr)
            }


            // 粗体
            temp = rPr.element("b")
            if (temp != null) {
                AttrManage.instance().setFontBold(attr, true)
            } else {
                setFontBold(attrLayout, attr)
            }


            // 斜体
            temp = rPr.element("i")
            if (temp != null) {
                AttrManage.instance().setFontItalic(attr, true)
            } else {
                setFontItalic(attrLayout, attr)
            }


            // 下划线
            temp = rPr.element("u")
            if (temp != null) {
                AttrManage.instance().setFontUnderline(attr, 1)
            } else {
                setFontUnderline(attrLayout, attr)
            }


            // 删除线
            temp = rPr.element("strike")
            if (temp != null) {
                AttrManage.instance().setFontStrike(attr, true)
                setFontDoubleStrike(attrLayout, attr)
            } else {
                setFontStrike(attrLayout, attr)
                setFontDoubleStrike(attrLayout, attr)
            }


            // 上下标
            temp = rPr.element("vertAlign")
            if (temp != null) {
                `val` = temp.attributeValue("val")
                if (`val`.equals("superscript", ignoreCase = true)) {
                    AttrManage.instance().setFontScript(attr, Font.SS_SUPER.toInt())
                } else if (`val`.equals("subscript", ignoreCase = true)) {
                    AttrManage.instance().setFontScript(attr, Font.SS_SUB.toInt())
                } else {
                    AttrManage.instance().setFontScript(attr, Font.SS_NONE.toInt())
                }
            } else {
                setFontScript(attrLayout, attr)
            }


            // hyperlink
            setHyperlinkID(attrLayout, attr)
        } else if (attrLayout != null) {
            val font = book.getFont(fontID)
            if (font != null) {
                AttrManage.instance().setFontSize(attr, font.getFontSize().toInt())
                AttrManage.instance().setFontColor(attr, book!!.getColor(font!!.getColorIndex()))
                AttrManage.instance().setFontBold(attr, font!!.isBold())
                AttrManage.instance().setFontItalic(attr, font!!.isItalic())
                AttrManage.instance().setFontUnderline(attr, font!!.getUnderline())
                AttrManage.instance().setFontStrike(attr, font!!.isStrikeline())
                setFontDoubleStrike(attrLayout, attr)
                AttrManage.instance().setFontScript(attr, font.getSuperSubScript().toInt())
                setHyperlinkID(attrLayout, attr)
            } else {
                setFontSize(attrLayout, attr)
                setFontColor(attrLayout, attr)
                setFontBold(attrLayout, attr)
                setFontItalic(attrLayout, attr)
                setFontUnderline(attrLayout, attr)
                setFontStrike(attrLayout, attr)
                setFontDoubleStrike(attrLayout, attr)
                setFontScript(attrLayout, attr)
                setHyperlinkID(attrLayout, attr)
            }
        }
    }

    /**
     * 
     * @param sheet
     * @param cell
     * @param attr
     * @param attrLayout
     */
    fun setRunAttribute(
        sheet: Sheet,
        cell: Cell?,
        attr: IAttributeSet,
        attrLayout: IAttributeSet?
    ) {
        if (cell != null) {
            val style = cell.getCellStyle()
            val book = sheet.getWorkbook()
            val font = book!!.getFont(style!!.getFontIndex().toInt())


            // 字号
            AttrManage.instance().setFontSize(attr, (font!!.getFontSize() + 0.5f).toInt())


            // 字符颜色
            AttrManage.instance().setFontColor(attr, book!!.getColor(font!!.getColorIndex()))


            // 粗体
            AttrManage.instance().setFontBold(attr, font!!.isBold())


            // 斜体
            AttrManage.instance().setFontItalic(attr, font!!.isItalic())


            // 下划线
            AttrManage.instance().setFontUnderline(attr, font!!.getUnderline())


            // 删除线
            AttrManage.instance().setFontStrike(attr, font!!.isStrikeline())
        } else if (attrLayout != null) {
            setFontSize(attrLayout, attr)
            setFontColor(attrLayout, attr)
            setFontBold(attrLayout, attr)
            setFontItalic(attrLayout, attr)
            setFontUnderline(attrLayout, attr)
            setFontStrike(attrLayout, attr)
            setFontDoubleStrike(attrLayout, attr)
            setFontScript(attrLayout, attr)
            setHyperlinkID(attrLayout, attr)
        }
    }

    /**
     * 
     * @param sheet
     * @param font
     * @param attr
     * @param attrLayout
     */
    fun setRunAttribute(
        sheet: Sheet,
        font: Font?,
        attr: IAttributeSet,
        attrLayout: IAttributeSet?
    ) {
        if (font != null) {
            val book = sheet.getWorkbook()


            // 字号
            AttrManage.instance().setFontSize(attr, (font!!.getFontSize() + 0.5f).toInt())


            // 字符颜色
            AttrManage.instance().setFontColor(attr, book!!.getColor(font!!.getColorIndex()))


            // 粗体
            AttrManage.instance().setFontBold(attr, font!!.isBold())


            // 斜体
            AttrManage.instance().setFontItalic(attr, font!!.isItalic())


            // 下划线
            AttrManage.instance().setFontUnderline(attr, font!!.getUnderline())


            // 删除线
            AttrManage.instance().setFontStrike(attr, font!!.isStrikeline())
        } else if (attrLayout != null) {
            setFontSize(attrLayout, attr)
            setFontColor(attrLayout, attr)
            setFontBold(attrLayout, attr)
            setFontItalic(attrLayout, attr)
            setFontUnderline(attrLayout, attr)
            setFontStrike(attrLayout, attr)
            setFontDoubleStrike(attrLayout, attr)
            setFontScript(attrLayout, attr)
            setHyperlinkID(attrLayout, attr)
        }
    }

    /**
     * 
     * @return
     */
    fun getMaxFontSize(): Int {
        return maxFontSize
    }

    /**
     * 
     * @param size
     */
    fun setMaxFontSize(size: Int) {
        if (size > maxFontSize) {
            maxFontSize = size
        }
    }

    /**
     * 
     */
    fun resetMaxFontSize() {
        maxFontSize = 0
    }

    /**
     * 
     */
    fun dispose() {
        maxFontSize = 0
    }

    // 一个段落下字号的最大值
    private var maxFontSize = 0
    /**
     * 
     * @return
     */
    /**
     * 
     * @param bTable
     */
    // text of table or not
    private var table: Boolean = false

    fun isTable(): Boolean = table

    fun setTable(value: Boolean) { table = value }
    /**
     * 
     * @return
     */
    /**
     * 
     * @param slide
     */
    // text of slide or not
    private var slide: Boolean = false

    fun isSlide(): Boolean = slide

    fun setSlide(value: Boolean) { slide = value }

    companion object {
        private val kit = RunAttr()

        /**
         * 
         */
        @JvmStatic
        fun instance(): RunAttr {
            return kit
        }
    }
}
