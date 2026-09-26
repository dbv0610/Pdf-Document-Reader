/*
 * 文件名称:           ParaAttr.java
 *  
 * 编译器:             android2.2
 * 时间:               下午1:04:08
 */
package com.wxiwei.office.fc.ppt.attribute

import com.wxiwei.office.fc.ppt.reader.pptXmlBoolean
import com.wxiwei.office.constant.MainConstant
import com.wxiwei.office.constant.wp.AttrIDConstant
import com.wxiwei.office.constant.wp.WPAttrConstant
import com.wxiwei.office.fc.dom4j.Element
import com.wxiwei.office.simpletext.model.AttrManage
import com.wxiwei.office.simpletext.model.AttributeSetImpl
import com.wxiwei.office.simpletext.model.IAttributeSet

/**
 * 管理章节属性
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
class SectionAttr {
    /**
     * set margin left
     */
    fun setPageMarginLeft(attrFrom: IAttributeSet?, attrTo: IAttributeSet) {
        if (attrFrom != null) {
            if (AttrManage.instance().hasAttribute(attrFrom, AttrIDConstant.PAGE_LEFT_ID)) {
                AttrManage.instance().setPageMarginLeft(
                    attrTo,
                    AttrManage.instance().getPageMarginLeft(attrFrom)
                )
            }
        }
    }

    /**
     * set margin right
     */
    fun setPageMarginRight(attrFrom: IAttributeSet?, attrTo: IAttributeSet) {
        if (attrFrom != null) {
            if (AttrManage.instance().hasAttribute(attrFrom, AttrIDConstant.PAGE_RIGHT_ID)) {
                AttrManage.instance().setPageMarginRight(
                    attrTo,
                    AttrManage.instance().getPageMarginRight(attrFrom)
                )
            }
        }
    }

    /**
     * set margin top
     */
    fun setPageMarginTop(attrFrom: IAttributeSet?, attrTo: IAttributeSet) {
        if (attrFrom != null) {
            if (AttrManage.instance().hasAttribute(attrFrom, AttrIDConstant.PAGE_TOP_ID)) {
                AttrManage.instance().setPageMarginTop(
                    attrTo,
                    AttrManage.instance().getPageMarginTop(attrFrom)
                )
            }
        }
    }

    /**
     * set margin bottom
     */
    fun setPageMarginBottom(attrFrom: IAttributeSet?, attrTo: IAttributeSet) {
        if (attrFrom != null) {
            if (AttrManage.instance().hasAttribute(attrFrom, AttrIDConstant.PAGE_BOTTOM_ID)) {
                AttrManage.instance().setPageMarginBottom(
                    attrTo,
                    AttrManage.instance().getPageMarginBottom(attrFrom)
                )
            }
        }
    }

    /**
     * set vertical alignment
     */
    fun setPageVerticalAlign(attrFrom: IAttributeSet?, attrTo: IAttributeSet) {
        if (attrFrom != null) {
            if (AttrManage.instance().hasAttribute(attrFrom, AttrIDConstant.PAGE_VERTICAL_ID)) {
                AttrManage.instance().setPageVerticalAlign(
                    attrTo,
                    AttrManage.instance().getPageVerticalAlign(attrFrom)
                )
            }
        }
    }

    /**
     * 首先取 layout里的值，没有再取 master里的值
     */
    fun getDefautSectionAttr(
        attrLayout: IAttributeSet?,
        attrMaster: IAttributeSet?
    ): IAttributeSet? {
        if (attrLayout != null || attrMaster != null) {
            val attr: IAttributeSet = AttributeSetImpl()
            if (attrLayout == null) {
                setPageMarginLeft(attrMaster, attr)
                setPageMarginRight(attrMaster, attr)
                setPageMarginTop(attrMaster, attr)
                setPageMarginBottom(attrMaster, attr)
                setPageVerticalAlign(attrMaster, attr)
            } else {
                if (attrMaster == null) {
                    setPageMarginLeft(attrLayout, attr)
                    setPageMarginRight(attrLayout, attr)
                    setPageMarginTop(attrLayout, attr)
                    setPageMarginBottom(attrLayout, attr)
                    setPageVerticalAlign(attrLayout, attr)
                } else {
                    if (AttrManage.instance()
                            .hasAttribute(attrLayout, AttrIDConstant.PAGE_LEFT_ID)
                    ) {
                        setPageMarginLeft(attrLayout, attr)
                    } else {
                        setPageMarginLeft(attrMaster, attr)
                    }

                    if (AttrManage.instance()
                            .hasAttribute(attrLayout, AttrIDConstant.PAGE_RIGHT_ID)
                    ) {
                        setPageMarginRight(attrLayout, attr)
                    } else {
                        setPageMarginRight(attrMaster, attr)
                    }

                    if (AttrManage.instance()
                            .hasAttribute(attrLayout, AttrIDConstant.PAGE_TOP_ID)
                    ) {
                        setPageMarginTop(attrLayout, attr)
                    } else {
                        setPageMarginTop(attrMaster, attr)
                    }

                    if (AttrManage.instance()
                            .hasAttribute(attrLayout, AttrIDConstant.PAGE_BOTTOM_ID)
                    ) {
                        setPageMarginBottom(attrLayout, attr)
                    } else {
                        setPageMarginBottom(attrMaster, attr)
                    }
                    if (AttrManage.instance()
                            .hasAttribute(attrLayout, AttrIDConstant.PAGE_VERTICAL_ID)
                    ) {
                        setPageVerticalAlign(attrLayout, attr)
                    } else {
                        setPageVerticalAlign(attrMaster, attr)
                    }
                }
            }
            return attr
        }
        return null
    }

    /**
     * 首先取slide 里的值，没有取 layout里的值，最后才取 master里的值
     * set section attribute
     */
    fun setSectionAttribute(
        bodyPr: Element?, attr: IAttributeSet, attrLayout: IAttributeSet?,
        attrMaster: IAttributeSet?, table: Boolean
    ) {
        var verAlign = WPAttrConstant.PAGE_V_TOP

        //byte horAlign = WPAttrConstant.PAGE_H_LEFT;
        /*if (table)
        {
            verAlign = WPAttrConstant.PAGE_V_CENTER;
        }*/
        val attrStyle = getDefautSectionAttr(attrLayout, attrMaster)
        if (bodyPr != null) {
            var `val`: String?
            // 左边距
            if (bodyPr.attribute("lIns") != null) {
                `val` = bodyPr.attributeValue("lIns")
                if (`val` != null && `val`.length > 0) {
                    val value =
                        (`val`.toInt() * MainConstant.POINT_DPI / MainConstant.EMU_PER_INCH * MainConstant.POINT_TO_TWIPS).toInt()
                    AttrManage.instance().setPageMarginLeft(attr, value)
                }
            } else {
                setPageMarginLeft(attrStyle, attr)
            }


            // 右边距
            if (bodyPr.attribute("rIns") != null) {
                `val` = bodyPr.attributeValue("rIns")
                if (`val` != null && `val`.length > 0) {
                    val value =
                        (`val`.toInt() * MainConstant.POINT_DPI / MainConstant.EMU_PER_INCH * MainConstant.POINT_TO_TWIPS).toInt()
                    AttrManage.instance().setPageMarginRight(attr, value)
                }
            } else {
                setPageMarginRight(attrStyle, attr)
            }


            // 上边距
            if (bodyPr.attribute("tIns") != null) {
                `val` = bodyPr.attributeValue("tIns")
                if (`val` != null && `val`.length > 0) {
                    val value =
                        (`val`.toInt() * MainConstant.POINT_DPI / MainConstant.EMU_PER_INCH * MainConstant.POINT_TO_TWIPS).toInt()
                    AttrManage.instance().setPageMarginTop(attr, value)
                }
            } else {
                setPageMarginTop(attrStyle, attr)
            }


            // 下边距
            if (bodyPr.attribute("bIns") != null) {
                `val` = bodyPr.attributeValue("bIns")
                if (`val` != null && `val`.length > 0) {
                    val value =
                        (`val`.toInt() * MainConstant.POINT_DPI / MainConstant.EMU_PER_INCH * MainConstant.POINT_TO_TWIPS).toInt()
                    AttrManage.instance().setPageMarginBottom(attr, value)
                }
            } else {
                setPageMarginBottom(attrStyle, attr)
            }


            //alignment in vertical
            if ((bodyPr.attributeValue("anchor").also { `val` = it }) != null) {
                if (`val` == "t") {
                    verAlign = WPAttrConstant.PAGE_V_TOP
                } else if (`val` == "ctr") {
                    verAlign = WPAttrConstant.PAGE_V_CENTER
                } else if (`val` == "b") {
                    verAlign = WPAttrConstant.PAGE_V_BOTTOM
                } else if (`val` == "just") {
                    verAlign = WPAttrConstant.PAGE_V_CENTER
                } else if (`val` == "dist") {
                    verAlign = WPAttrConstant.PAGE_V_CENTER
                }
                AttrManage.instance().setPageVerticalAlign(attr, verAlign)
            } else {
                setPageVerticalAlign(attrStyle, attr)
            }


            //alignment in horizontal
            if ((bodyPr.attributeValue("anchorCtr").also { `val` = it }) != null) {
                if (pptXmlBoolean(`val`)) {
                    AttrManage.instance().setPageHorizontalAlign(attr, WPAttrConstant.PAGE_H_CENTER)
                }
            } else {
                if (attrStyle != null) {
                    if (AttrManage.instance()
                            .hasAttribute(attrStyle, AttrIDConstant.PAGE_HORIZONTAL_ID)
                    ) {
                        AttrManage.instance().setPageHorizontalAlign(
                            attr,
                            AttrManage.instance().getPageHorizontalAlign(attrStyle)
                        )
                    }
                }
            }
        } else if (attrStyle != null) {
            setPageMarginLeft(attrStyle, attr)
            setPageMarginRight(attrStyle, attr)
            setPageMarginTop(attrStyle, attr)
            setPageMarginBottom(attrStyle, attr)
            setPageVerticalAlign(attrStyle, attr)
        }


        // set default value
        if (RunAttr.instance().isSlide()) {
            /*if (!AttrManage.instance().hasAttribute(attr, AttrIDConstant.PAGE_VERTICAL_ID))
            {
                AttrManage.instance().setPageVerticalAlign(attr, verAlign);
            }*/
            if (!AttrManage.instance().hasAttribute(attr, AttrIDConstant.PAGE_LEFT_ID)) {
                if (table) {
                    AttrManage.instance().setPageMarginLeft(attr, DEFAULT_TABLE_MARGIN)
                } else {
                    AttrManage.instance().setPageMarginLeft(attr, DEFAULT_MARGIN_LEFT_RIGHT)
                }
            }
            if (!AttrManage.instance().hasAttribute(attr, AttrIDConstant.PAGE_RIGHT_ID)) {
                if (table) {
                    AttrManage.instance().setPageMarginRight(attr, DEFAULT_TABLE_MARGIN)
                } else {
                    AttrManage.instance().setPageMarginRight(attr, DEFAULT_MARGIN_LEFT_RIGHT)
                }
            }
            if (!AttrManage.instance().hasAttribute(attr, AttrIDConstant.PAGE_TOP_ID)) {
                if (table) {
                    AttrManage.instance().setPageMarginTop(attr, DEFAULT_TABLE_MARGIN)
                } else {
                    AttrManage.instance().setPageMarginTop(attr, DEFAULT_MARGIN_TOP_BOTTOM)
                }
            }
            if (!AttrManage.instance().hasAttribute(attr, AttrIDConstant.PAGE_BOTTOM_ID)) {
                if (table) {
                    AttrManage.instance().setPageMarginBottom(attr, 0)
                } else {
                    AttrManage.instance().setPageMarginBottom(attr, DEFAULT_MARGIN_TOP_BOTTOM)
                }
            }
        }
    }

    companion object {
        // default left and right margin value
        const val DEFAULT_MARGIN_LEFT_RIGHT: Int = 144

        // default top and bottom margin value
        const val DEFAULT_MARGIN_TOP_BOTTOM: Int = 72

        // default table margin value
        const val DEFAULT_TABLE_MARGIN: Int = 30

        private val kit = SectionAttr()

        /**
         * 
         */
        @JvmStatic
        fun instance(): SectionAttr {
            return kit
        }
    }
}
