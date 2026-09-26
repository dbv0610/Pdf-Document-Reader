/*
 * 文件名称:           ParaAttr.java
 *  
 * 编译器:             android2.2
 * 时间:               下午1:52:05
 */
package com.wxiwei.office.fc.ppt.attribute

import com.wxiwei.office.constant.MainConstant
import com.wxiwei.office.constant.SSConstant
import com.wxiwei.office.constant.wp.AttrIDConstant
import com.wxiwei.office.constant.wp.WPAttrConstant
import com.wxiwei.office.constant.wp.WPModelConstant
import com.wxiwei.office.fc.dom4j.Element
import com.wxiwei.office.fc.ppt.bulletnumber.BulletNumberManage
import com.wxiwei.office.fc.ppt.reader.ReaderKit
import com.wxiwei.office.pg.model.PGLayout
import com.wxiwei.office.pg.model.PGMaster
import com.wxiwei.office.pg.model.PGPlaceholderUtil
import com.wxiwei.office.pg.model.PGStyle
import com.wxiwei.office.simpletext.model.AttrManage
import com.wxiwei.office.simpletext.model.AttributeSetImpl
import com.wxiwei.office.simpletext.model.IAttributeSet
import com.wxiwei.office.simpletext.model.ParagraphElement
import com.wxiwei.office.simpletext.model.SectionElement
import com.wxiwei.office.simpletext.model.StyleManage
import com.wxiwei.office.ss.model.style.CellStyle
import com.wxiwei.office.system.IControl
import kotlin.math.abs

/**
 * 管理段落属性
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
class ParaAttr {
    /**
     * process paragraph
     * @param master
     * @param pgLayout
     * @param defautltStyle
     * @param secElem
     * @param txBody
     * @param type
     * @param idx
     * @return
     */
    fun processParagraph(
        control: IControl?, master: PGMaster?, pgLayout: PGLayout?, defaultStyle: PGStyle?,
        secElem: SectionElement, styleElement: Element?, txBody: Element, type: String?, idx: Int
    ): Int {
        var `val`: String?
        var fontScale = 100
        var lnSpcReduction = 0
        var defaultFontColor = false
        val bodyPr = txBody.element("bodyPr")
        if (bodyPr != null) {
            val normAutofit = bodyPr.element("normAutofit")
            if (normAutofit != null) {
                if (normAutofit.attribute("fontScale") != null) {
                    `val` = normAutofit.attributeValue("fontScale")
                    if (`val` != null && `val`.length > 0) {
                        fontScale = `val`.toInt() / 1000
                    }
                }
                if (normAutofit.attribute("lnSpcReduction") != null) {
                    `val` = normAutofit.attributeValue("lnSpcReduction")
                    if (`val` != null && `val`.length > 0) {
                        lnSpcReduction = `val`.toInt()
                    }
                }
            }
        }
        var offset = 0
        val subTitle = PGPlaceholderUtil.SUBTITLE == type
        val lstStyle = txBody.element("lstStyle")
        val ps: MutableList<Element> = txBody.elements("p") as MutableList<Element>
        for (p in ps) {
            var lvl = 1
            val pPr = p.element("pPr")
            if (pPr != null && pPr.attribute("lvl") != null) {
                `val` = pPr.attributeValue("lvl")
                if (`val` != null && `val`.length > 0) {
                    lvl += `val`.toInt()
                }
            }
            var layoutStyle = -1
            if (pgLayout != null) {
                layoutStyle = pgLayout.getStyleID(type, idx, lvl)
            }
            var masterStyle = -1
            if (master != null) {
                masterStyle = master.getTextStyle(type, idx, lvl)
            }
            if (masterStyle < 0 && defaultStyle != null) {
                defaultFontColor = true
                masterStyle = defaultStyle.getStyle(lvl)
            }

            val paraElem = ParagraphElement()
            paraElem.setStartOffset(offset.toLong())
            var attrLayout: IAttributeSet? = null

            if (lstStyle != null) {
                var ind = lvl
                if (lvl > 0 || lstStyle.element("defPPr") == null) {
                    ind += 1
                }
                var txStyle: Element? = null
                when (ind) {
                    1 -> txStyle = lstStyle.element("defPPr")
                    2 -> txStyle = lstStyle.element("lvl1pPr")
                    3 -> txStyle = lstStyle.element("lvl2pPr")
                    4 -> txStyle = lstStyle.element("lvl3pPr")
                    5 -> txStyle = lstStyle.element("lvl4pPr")
                    6 -> txStyle = lstStyle.element("lvl5pPr")
                    7 -> txStyle = lstStyle.element("lvl6pPr")
                    8 -> txStyle = lstStyle.element("lvl7pPr")
                    9 -> txStyle = lstStyle.element("lvl8pPr")
                    10 -> txStyle = lstStyle.element("lvl9pPr")
                    else -> {}
                }
                if (txStyle != null) {
                    attrLayout = AttributeSetImpl()
                    // paragraph attribute set
                    setParaAttribute(control, txStyle, attrLayout, null, -1, -1, 0, true, subTitle)
                    val defRPr = txStyle.element("defRPr")
                    // character attribute set
                    RunAttr.instance()
                        .setRunAttribute(master, defRPr, attrLayout, null, 100, -1, false)
                    processParaWithPct(txStyle, attrLayout)
                }
            }
            if (attrLayout == null && layoutStyle > 0) {
                val style = StyleManage.instance().getStyle(layoutStyle)
                if (style != null) {
                    attrLayout = style.getAttrbuteSet()
                }
            } else if (attrLayout == null && styleElement != null) {
                val fontRef = styleElement.element("fontRef")
                if (fontRef.elements().size > 0) {
                    val fontColor = ReaderKit.instance().getColor(master, fontRef)
                    attrLayout = AttributeSetImpl()
                    AttrManage.instance().setFontColor(attrLayout, fontColor)
                }
            } else if (defaultFontColor && attrLayout == null && defaultStyle != null) {
                val fontColor = defaultStyle.getDefaultFontColor(lvl)
                if (fontColor != null) {
                    attrLayout = AttributeSetImpl()
                    AttrManage.instance().setFontColor(attrLayout, master!!.getColor(fontColor))
                }
            }

            offset = RunAttr.instance()
                .processRun(master, paraElem, p, attrLayout, offset, fontScale, masterStyle)


            // when leafElem only contains \n, don't show bullet number 
            if (p.elements("r").size == 0 && p.elements("fld").size == 0) {
                setParaAttribute(
                    control, pPr, paraElem.getAttribute(), attrLayout, layoutStyle, masterStyle,
                    lnSpcReduction, false, subTitle
                )
            } else {
                setParaAttribute(
                    control, pPr, paraElem.getAttribute(), attrLayout, layoutStyle, masterStyle,
                    lnSpcReduction, true, subTitle
                )
            }

            processParaWithPct(pPr, paraElem.getAttribute())

            paraElem.setEndOffset(offset.toLong())
            secElem.appendParagraph(paraElem, WPModelConstant.MAIN)
        }
        BulletNumberManage.instance().clearData()
        RunAttr.instance().setMaxFontSize(0)
        return offset
    }

    /**
     * 
     * @param attr
     * @param align
     */
    fun setParaAlign(attr: IAttributeSet, align: String) {
        // 左对齐
        if (align == "l") {
            AttrManage.instance()
                .setParaHorizontalAlign(attr, WPAttrConstant.PARA_HOR_ALIGN_LEFT.toInt())
        } else if (align == "ctr") {
            AttrManage.instance()
                .setParaHorizontalAlign(attr, WPAttrConstant.PARA_HOR_ALIGN_CENTER.toInt())
        } else if (align == "r") {
            AttrManage.instance()
                .setParaHorizontalAlign(attr, WPAttrConstant.PARA_HOR_ALIGN_RIGHT.toInt())
        }
    }

    /**
     * 水平对齐方式
     */
    fun setParaHorizontalAlign(attrFrom: IAttributeSet?, attrTo: IAttributeSet) {
        if (attrFrom != null) {
            if (AttrManage.instance().hasAttribute(attrFrom, AttrIDConstant.PARA_HORIZONTAL_ID)) {
                AttrManage.instance().setParaHorizontalAlign(
                    attrTo,
                    AttrManage.instance().getParaHorizontalAlign(attrFrom)
                )
            }
        }
    }

    /**
     * 段前间距
     */
    fun setParaBefore(attrFrom: IAttributeSet?, attrTo: IAttributeSet) {
        if (attrFrom != null) {
            if (AttrManage.instance().hasAttribute(attrFrom, AttrIDConstant.PARA_BEFORE_ID)) {
                AttrManage.instance().setParaBefore(
                    attrTo,
                    AttrManage.instance().getParaBefore(attrFrom)
                )
            }
        }
    }

    /**
     * 段后间距
     */
    fun setParaAfter(attrFrom: IAttributeSet?, attrTo: IAttributeSet) {
        if (attrFrom != null) {
            if (AttrManage.instance().hasAttribute(attrFrom, AttrIDConstant.PARA_AFTER_ID)) {
                AttrManage.instance().setParaAfter(
                    attrTo,
                    AttrManage.instance().getParaAfter(attrFrom)
                )
            }
        }
    }

    /**
     * 行距
     */
    fun setParaLineSpace(attrFrom: IAttributeSet?, attrTo: IAttributeSet) {
        if (attrFrom != null) {
            if (AttrManage.instance()
                    .hasAttribute(attrFrom, AttrIDConstant.PARA_LINESPACE_TYPE_ID)
            ) {
                AttrManage.instance().setParaLineSpaceType(
                    attrTo,
                    AttrManage.instance().getParaLineSpaceType(attrFrom)
                )
            }
            if (AttrManage.instance().hasAttribute(attrFrom, AttrIDConstant.PARA_LINESPACE_ID)) {
                AttrManage.instance().setParaLineSpace(
                    attrTo,
                    AttrManage.instance().getParaLineSpace(attrFrom)
                )
            }
        }
    }

    /**
     * 左缩进
     */
    fun setParaIndentLeft(attrFrom: IAttributeSet?, attrTo: IAttributeSet) {
        if (attrFrom != null) {
            if (AttrManage.instance().hasAttribute(attrFrom, AttrIDConstant.PARA_INDENT_LEFT_ID)) {
                AttrManage.instance().setParaIndentLeft(
                    attrTo,
                    AttrManage.instance().getParaIndentLeft(attrFrom)
                )
            }
        }
    }

    /**
     * 右缩进
     */
    fun setParaIndentRight(attrFrom: IAttributeSet?, attrTo: IAttributeSet) {
        if (attrFrom != null) {
            if (AttrManage.instance().hasAttribute(attrFrom, AttrIDConstant.PARA_INDENT_RIGHT_ID)) {
                AttrManage.instance().setParaIndentRight(
                    attrTo,
                    AttrManage.instance().getParaIndentRight(attrFrom)
                )
            }
        }
    }

    /**
     * 特殊缩进
     */
    fun setParaSpecialIndent(attrFrom: IAttributeSet?, attrTo: IAttributeSet) {
        if (attrFrom != null) {
            if (AttrManage.instance()
                    .hasAttribute(attrFrom, AttrIDConstant.PARA_SPECIALINDENT_ID)
            ) {
                AttrManage.instance().setParaSpecialIndent(
                    attrTo,
                    AttrManage.instance().getParaSpecialIndent(attrFrom)
                )
            }
        }
    }

    /**
     * set paragraph attribute
     */
    fun setParaAttribute(
        control: IControl?,
        pPr: Element?,
        attr: IAttributeSet,
        attrLayout: IAttributeSet?,
        layoutStyle: Int,
        masterStyle: Int,
        lnSpcReduction: Int,
        addBullet: Boolean,
        subTitle: Boolean
    ) {
        var `val`: String?
        if (pPr != null) {
            // 水平对齐
            var temp: Element?
            if (pPr.attribute("algn") != null) {
                `val` = pPr.attributeValue("algn")
                setParaAlign(attr, `val`)
            } else {
                setParaHorizontalAlign(attrLayout, attr)
            }


            // 段前
            val spcBef = pPr.element("spcBef")
            if (spcBef != null) {
                // 固定值
                temp = spcBef.element("spcPts")
                if (temp != null && temp.attribute("val") != null) {
                    `val` = temp.attributeValue("val")
                    if (`val` != null && `val`.length > 0) {
                        AttrManage.instance().setParaBefore(
                            attr,
                            (`val`.toInt() / 100 * MainConstant.POINT_TO_TWIPS).toInt()
                        )
                    }
                }
            } else {
                setParaBefore(attrLayout, attr)
            }


            // 段后
            val spcAft = pPr.element("spcAft")
            if (spcAft != null) {
                // 固定值
                temp = spcAft.element("spcPts")
                if (temp != null && temp.attribute("val") != null) {
                    `val` = temp.attributeValue("val")
                    if (`val` != null && `val`.length > 0) {
                        AttrManage.instance().setParaAfter(
                            attr,
                            (`val`.toInt() / 100 * MainConstant.POINT_TO_TWIPS).toInt()
                        )
                    }
                }
            } else {
                setParaAfter(attrLayout, attr)
            }


            // 行距
            val lnSpc = pPr.element("lnSpc")
            if (lnSpc != null) {
                // 固定值
                temp = lnSpc.element("spcPts")
                if (temp != null && temp.attribute("val") != null) {
                    `val` = temp.attributeValue("val")
                    if (`val` != null && `val`.length > 0) {
                        // 行距类型
                        AttrManage.instance()
                            .setParaLineSpaceType(attr, WPAttrConstant.LINE_SPACE_EXACTLY.toInt())
                        // 行距
                        AttrManage.instance().setParaLineSpace(
                            attr,
                            (`val`.toInt() / 100 * MainConstant.POINT_TO_TWIPS).toInt().toFloat()
                        )
                    }
                }


                // 多倍行距
                temp = lnSpc.element("spcPct")
                if (temp != null && temp.attribute("val") != null) {
                    `val` = temp.attributeValue("val")
                    if (`val` != null && `val`.length > 0) {
                        // 行距类型
                        AttrManage.instance()
                            .setParaLineSpaceType(attr, WPAttrConstant.LINE_SAPCE_MULTIPLE.toInt())

                        // 行距
                        AttrManage.instance().setParaLineSpace(
                            attr,
                            (`val`.toInt() - lnSpcReduction).toFloat() / 100000
                        )
                    }
                }
            } else {
                if (lnSpcReduction > 0) {
                    // 行距类型
                    AttrManage.instance()
                        .setParaLineSpaceType(attr, WPAttrConstant.LINE_SAPCE_MULTIPLE.toInt())
                    // 行距
                    AttrManage.instance()
                        .setParaLineSpace(attr, (100000 - lnSpcReduction).toFloat() / 100000)
                } else {
                    setParaLineSpace(attrLayout, attr)
                }
            }


            // 右缩进
            if (pPr.attribute("marR") != null) {
                `val` = pPr.attributeValue("marR")
                if (`val` != null && `val`.length > 0) {
                    AttrManage.instance().setParaIndentRight(
                        attr,
                        (`val`.toInt() * MainConstant.POINT_DPI / MainConstant.EMU_PER_INCH
                                * MainConstant.POINT_TO_TWIPS).toInt()
                    )
                }
            } else {
                setParaIndentRight(attrLayout, attr)
            }
        } else {
            setParaHorizontalAlign(attrLayout, attr)
            setParaBefore(attrLayout, attr)
            setParaAfter(attrLayout, attr)
            // para linespace
            if (lnSpcReduction > 0) {
                // 行距类型
                AttrManage.instance()
                    .setParaLineSpaceType(attr, WPAttrConstant.LINE_SAPCE_MULTIPLE.toInt())
                // 行距
                AttrManage.instance()
                    .setParaLineSpace(attr, (100000 - lnSpcReduction).toFloat() / 100000)
            } else {
                setParaLineSpace(attrLayout, attr)
            }
            setParaIndentLeft(attrLayout, attr)
            setParaIndentRight(attrLayout, attr)
        }

        val style = StyleManage.instance().getStyle(masterStyle)


        // 缩进
        // 左缩进
        var leftFrom = 0
        var left = 0
        if (pPr != null && pPr.attribute("marL") != null) {
            `val` = pPr.attributeValue("marL")
            if (`val` != null && `val`.length > 0) {
                left = (`val`.toInt() * MainConstant.POINT_DPI / MainConstant.EMU_PER_INCH
                        * MainConstant.POINT_TO_TWIPS).toInt()
                AttrManage.instance().setParaIndentInitLeft(attr, left)
                AttrManage.instance().setParaIndentLeft(attr, left)
            }
        } else if (attrLayout != null) {
            if (AttrManage.instance()
                    .hasAttribute(attrLayout, AttrIDConstant.PARA_INDENT_LEFT_ID)
            ) {
                leftFrom = 1
                left = AttrManage.instance().getParaIndentInitLeft(attrLayout)
                AttrManage.instance().setParaIndentLeft(attr, left)
            }
        } else {
            if (style != null && style.getAttrbuteSet() != null && AttrManage.instance()
                    .hasAttribute(style.getAttrbuteSet(), AttrIDConstant.PARA_INDENT_LEFT_ID)
            ) {
                leftFrom = 2
                left = AttrManage.instance().getParaIndentInitLeft(style.getAttrbuteSet())
                AttrManage.instance().setParaIndentLeft(attr, left)
            }
        }


        // 首行、悬挂缩进
        var indent = 0
        if (pPr != null && pPr.attribute("indent") != null) {
            `val` = pPr.attributeValue("indent")
            if (`val` != null && `val`.length > 0) {
                indent = (`val`.toInt() * MainConstant.POINT_DPI / MainConstant.EMU_PER_INCH
                        * MainConstant.POINT_TO_TWIPS).toInt()
                setSpecialIndent(attr, left, indent, true)
            }
        } else if (attrLayout != null) {
            if (AttrManage.instance()
                    .hasAttribute(attrLayout, AttrIDConstant.PARA_SPECIALINDENT_ID)
            ) {
                indent = AttrManage.instance().getParaSpecialIndent(attrLayout)
                //                if (leftFrom == 1)
//                {
//                    setSpecialIndent(attr, left, indent, false);
//                }
//                else
                run {
                    setSpecialIndent(attr, left, indent, true)
                }
            }
        } else {
            if (style != null && style.getAttrbuteSet() != null && AttrManage.instance()
                    .hasAttribute(style.getAttrbuteSet(), AttrIDConstant.PARA_SPECIALINDENT_ID)
            ) {
                indent = AttrManage.instance().getParaSpecialIndent(style.getAttrbuteSet())
                //                if (leftFrom == 2)
//                {
//                    setSpecialIndent(attr, left, indent, false);
//                }
//                else
                run {
                    setSpecialIndent(attr, left, indent, true)
                }
            }
        }


        // bullet and number
        if (addBullet && (pPr == null || (pPr != null && pPr.element("buNone") == null))) {
            var id = BulletNumberManage.instance().addBulletNumber(control!!, -1, pPr)
            if (id == -1 && attrLayout != null) {
                id = AttrManage.instance().getPGParaBulletID(attrLayout)
            }
            if (id == -1 && layoutStyle >= 0) {
                id = BulletNumberManage.instance().getBulletID(layoutStyle)
            }
            if (id == -1 && masterStyle > 0 && !subTitle) {
                id = BulletNumberManage.instance().getBulletID(masterStyle)
            }
            if (id >= 0) {
                AttrManage.instance().setPGParaBulletID(attr, id)
            }
        }


        // set style
        if (masterStyle > 0) {
            AttrManage.instance().setParaStyleID(attr, masterStyle)
        }
    }

    /**
     * 
     * @param attr
     * @param left
     * @param indent
     * @param bSet
     */
    fun setSpecialIndent(attr: IAttributeSet, left: Int, indent: Int, bSet: Boolean) {
        // indent >= 0 为首行缩进，indent < 0 为悬挂缩进
        var indent = indent
        if (indent < 0 && abs(indent) > left) {
            indent = -left
        }
        AttrManage.instance().setParaSpecialIndent(attr, indent)
        // 悬挂缩进值也设置到左缩进，左缩进需要减去悬挂缩进
        if (bSet && indent < 0) {
            AttrManage.instance().setParaIndentLeft(attr, left + indent)
        }
    }

    /**
     * set paragraph attribute
     */
    fun setParaAttribute(style: CellStyle?, attr: IAttributeSet, attrLayout: IAttributeSet?) {
        if (style != null && attrLayout != null) {
            val indent = (style.getIndent() * SSConstant.INDENT_TO_PIXEL)
            // 水平对齐
            when (style.getHorizontalAlign()) {
                CellStyle.ALIGN_LEFT, CellStyle.ALIGN_GENERAL -> {
                    attrLayout.setAttribute(
                        AttrIDConstant.PARA_INDENT_LEFT_ID,
                        Math.round(indent * MainConstant.PIXEL_TO_TWIPS)
                    )
                    attrLayout.setAttribute(AttrIDConstant.PARA_INDENT_RIGHT_ID, 0)
                    AttrManage.instance()
                        .setParaHorizontalAlign(attr, WPAttrConstant.PARA_HOR_ALIGN_LEFT.toInt())
                }

                CellStyle.ALIGN_RIGHT -> {
                    attrLayout.setAttribute(AttrIDConstant.PARA_INDENT_LEFT_ID, 0)
                    attrLayout.setAttribute(
                        AttrIDConstant.PARA_INDENT_RIGHT_ID,
                        Math.round(indent * MainConstant.PIXEL_TO_TWIPS)
                    )
                    AttrManage.instance()
                        .setParaHorizontalAlign(attr, WPAttrConstant.PARA_HOR_ALIGN_RIGHT.toInt())
                }

                CellStyle.ALIGN_CENTER, CellStyle.ALIGN_FILL, CellStyle.ALIGN_JUSTIFY, CellStyle.ALIGN_CENTER_SELECTION -> AttrManage.instance()
                    .setParaHorizontalAlign(attr, WPAttrConstant.PARA_HOR_ALIGN_CENTER.toInt())
            }


            setParaBefore(attrLayout, attr)

            setParaAfter(attrLayout, attr)

            setParaLineSpace(attrLayout, attr)


            // 缩进
            // 左缩进
            setParaIndentLeft(attrLayout, attr)


            // 右缩进
            setParaIndentRight(attrLayout, attr)


            // 首行、悬挂缩进
            setParaSpecialIndent(attrLayout, attr)
        } else if (attrLayout != null) {
            setParaHorizontalAlign(attrLayout, attr)
            setParaBefore(attrLayout, attr)
            setParaAfter(attrLayout, attr)
            setParaLineSpace(attrLayout, attr)
        }
    }

    /**
     * 处理以行为单位的段前段后
     */
    fun processParaWithPct(pPr: Element?, attr: IAttributeSet) {
        val fontSize = RunAttr.instance().getMaxFontSize()
        if (pPr != null) {
            var temp: Element?
            var `val`: String?
            // 段前
            val spcBef = pPr.element("spcBef")
            if (spcBef != null) {
                // 段前 ？行
                temp = spcBef.element("spcPct")
                if (temp != null && temp.attribute("val") != null) {
                    `val` = temp.attributeValue("val")
                    if (`val` != null && `val`.length > 0) {
                        AttrManage.instance().setParaBefore(
                            attr,
                            (`val`.toInt() / 100000f * fontSize * POINT_PER_LINE_PER_FONTSIZE * MainConstant.POINT_TO_TWIPS).toInt()
                        )
                    }
                }
            }


            // 段后
            val spcAft = pPr.element("spcAft")
            if (spcAft != null) {
                // 段后 ？行
                temp = spcAft.element("spcPct")
                if (temp != null && temp.attribute("val") != null) {
                    `val` = temp.attributeValue("val")
                    if (`val` != null && `val`.length > 0) {
                        AttrManage.instance().setParaAfter(
                            attr,
                            (`val`.toInt() / 100000f * fontSize * POINT_PER_LINE_PER_FONTSIZE * MainConstant.POINT_TO_TWIPS).toInt()
                        )
                    }
                }
            }
        }
    }

    companion object {
        // 一行一磅字符对应的段前段后磅值
        const val POINT_PER_LINE_PER_FONTSIZE: Float = 1.2f

        private val kit = ParaAttr()

        /**
         * 
         */
        @JvmStatic
        fun instance(): ParaAttr {
            return kit
        }
    }
}
