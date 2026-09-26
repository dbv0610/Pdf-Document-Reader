/*
 * 文件名称:           MasterData.java
 *  
 * 编译器:             android2.2
 * 时间:               上午9:07:18
 */
package com.wxiwei.office.fc.ppt.reader

import com.wxiwei.office.fc.dom4j.Element
import com.wxiwei.office.fc.ppt.ShapeManage.Companion.instance
import com.wxiwei.office.fc.ppt.attribute.ParaAttr
import com.wxiwei.office.fc.ppt.attribute.ParaAttr.Companion.instance
import com.wxiwei.office.fc.ppt.attribute.RunAttr
import com.wxiwei.office.fc.ppt.attribute.RunAttr.Companion.instance
import com.wxiwei.office.fc.ppt.attribute.SectionAttr
import com.wxiwei.office.fc.ppt.bulletnumber.BulletNumberManage
import com.wxiwei.office.fc.ppt.reader.ReaderKit.Companion.instance
import com.wxiwei.office.pg.model.PGMaster
import com.wxiwei.office.pg.model.PGStyle
import com.wxiwei.office.simpletext.model.AttrManage
import com.wxiwei.office.simpletext.model.AttributeSetImpl
import com.wxiwei.office.simpletext.model.IAttributeSet
import com.wxiwei.office.simpletext.model.Style
import com.wxiwei.office.simpletext.model.StyleManage
import com.wxiwei.office.system.IControl

/**
 * shape layout, margin, text style
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
 * 日期:           2012-2-29
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
class StyleReader {
    /**
     * get PGStyles
     */
    fun getStyles(control: IControl?, pgMaster: PGMaster?, sp: Element?, style: Element?): PGStyle {
        val pgStyle = PGStyle()
        processSp(pgStyle, sp)
        processStyle(control, pgStyle, pgMaster, style)
        return pgStyle
    }

    /**
     * shape layout, margin
     */
    private fun processSp(pgStyle: PGStyle, sp: Element?) {
        if (sp != null) {
            // anchor
            val spPr = sp.element("spPr")
            if (spPr != null) {
                pgStyle.setAnchor(
                    ReaderKit.instance().getShapeAnchor(spPr.element("xfrm"), 1.0f, 1.0f)
                )
            }
            // section attribute
            val txBody = sp.element("txBody")
            if (txBody != null) {
                val bodyPr = txBody.element("bodyPr")
                if (bodyPr != null) {
                    val attr: IAttributeSet = AttributeSetImpl()
                    SectionAttr.Companion.instance()
                        .setSectionAttribute(bodyPr, attr, null, null, false)
                    pgStyle.setSectionAttr(attr)
                }
            }
        }
    }

    /**
     * reader style
     */
    private fun processStyle(
        control: IControl?,
        pgStyle: PGStyle,
        pgMaster: PGMaster?,
        style: Element?
    ) {
        if (style != null) {
            val lvl1pPr = style.element("lvl1pPr")
            if (lvl1pPr != null) {
                processStyleAttribute(control, pgStyle, pgMaster, lvl1pPr, 1)
            }
            val lvl2pPr = style.element("lvl2pPr")
            if (lvl2pPr != null) {
                processStyleAttribute(control, pgStyle, pgMaster, lvl2pPr, 2)
            }
            val lvl3pPr = style.element("lvl3pPr")
            if (lvl3pPr != null) {
                processStyleAttribute(control, pgStyle, pgMaster, lvl3pPr, 3)
            }
            val lvl4pPr = style.element("lvl4pPr")
            if (lvl4pPr != null) {
                processStyleAttribute(control, pgStyle, pgMaster, lvl4pPr, 4)
            }
            val lvl5pPr = style.element("lvl5pPr")
            if (lvl5pPr != null) {
                processStyleAttribute(control, pgStyle, pgMaster, lvl5pPr, 5)
            }
            val lvl6pPr = style.element("lvl6pPr")
            if (lvl6pPr != null) {
                processStyleAttribute(control, pgStyle, pgMaster, lvl6pPr, 6)
            }
            val lvl7pPr = style.element("lvl7pPr")
            if (lvl7pPr != null) {
                processStyleAttribute(control, pgStyle, pgMaster, lvl7pPr, 7)
            }
            val lvl8pPr = style.element("lvl8pPr")
            if (lvl8pPr != null) {
                processStyleAttribute(control, pgStyle, pgMaster, lvl8pPr, 8)
            }
            val lvl9pPr = style.element("lvl9pPr")
            if (lvl9pPr != null) {
                processStyleAttribute(control, pgStyle, pgMaster, lvl9pPr, 9)
            }
        }
    }

    /**
     * set sytle attribute
     */
    private fun processStyleAttribute(
        control: IControl?,
        pgStyle: PGStyle,
        pgMaster: PGMaster?,
        paraStyle: Element,
        lvl: Int
    ) {
        val style = Style()
        style.setId(this.styleIndex)
        style.setType(0.toByte())
        // paragraph attribute set
        ParaAttr.instance().setParaAttribute(
            control,
            paraStyle,
            style.getAttrbuteSet()!!,
            null,
            -1,
            -1,
            0,
            false,
            false
        )
        val defRPr = paraStyle.element("defRPr")

        // character attribute set
        RunAttr.instance()
            .setRunAttribute(pgMaster, defRPr, style.getAttrbuteSet()!!, null, 100, -1, false)
        RunAttr.instance().setMaxFontSize(
            AttrManage.instance().getFontSize(
                style.getAttrbuteSet()!!,
                style.getAttrbuteSet()
            )
        )
        // 处理以行为单位的段前段后
        ParaAttr.instance().processParaWithPct(paraStyle, style.getAttrbuteSet()!!)
        RunAttr.instance().resetMaxFontSize()
        StyleManage.instance().addStyle(style)

        pgStyle.addStyle(lvl, this.styleIndex)
        BulletNumberManage.Companion.instance().addBulletNumber(control!!, this.styleIndex, paraStyle)
        this.styleIndex++
    }

    /**
     * 获取下次设置 style index 的开始值
     */
    /**
     * 设置 style index 的开始值
     */
    //
    private var styleIndex: Int = 0

    fun getStyleIndex(): Int = styleIndex

    fun setStyleIndex(index: Int) { styleIndex = index }

    companion object {
        private val style = StyleReader()

        /**
         * 
         */
        @JvmStatic
        fun instance(): StyleReader {
            return style
        }
    }
}
