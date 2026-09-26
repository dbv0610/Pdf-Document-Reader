/*
 * 文件名称:           LayoutReader.java
 *  
 * 编译器:             android2.2
 * 时间:               下午4:04:50
 */
package com.wxiwei.office.fc.ppt.reader

import com.wxiwei.office.fc.dom4j.Element
import com.wxiwei.office.fc.dom4j.io.SAXReader
import com.wxiwei.office.fc.openxml4j.opc.PackagePart
import com.wxiwei.office.fc.openxml4j.opc.ZipPackage
import com.wxiwei.office.fc.ppt.ShapeManage
import com.wxiwei.office.fc.ppt.ShapeManage.Companion.instance
import com.wxiwei.office.fc.ppt.attribute.ParaAttr.Companion.instance
import com.wxiwei.office.fc.ppt.attribute.RunAttr.Companion.instance
import com.wxiwei.office.fc.ppt.reader.ReaderKit.Companion.instance
import com.wxiwei.office.pg.model.PGLayout
import com.wxiwei.office.pg.model.PGMaster
import com.wxiwei.office.pg.model.PGModel
import com.wxiwei.office.pg.model.PGPlaceholderUtil
import com.wxiwei.office.pg.model.PGSlide
import com.wxiwei.office.pg.model.PGStyle
import com.wxiwei.office.system.IControl

/**
 * 解析 layout
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
 * 日期:           2012-3-2
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
class LayoutReader {
    /**
     * get PGLayout
     * @param layoutPart
     * @return
     */
    @Throws(Exception::class)
    fun getLayouts(
        control: IControl, zipPackage: ZipPackage, layoutPart: PackagePart, pgModel: PGModel,
        pgMaster: PGMaster?, defaultStyle: PGStyle?
    ): PGLayout? {
        // layout xml
        val saxreader = SAXReader()
        val `in` = layoutPart.getInputStream()
        val poiLayout = saxreader.read(`in`)
        val layout = poiLayout.getRootElement()
        var pgLayout: PGLayout? = null
        if (layout != null) {
            pgLayout = PGLayout()
            if (layout.attribute("showMasterSp") != null) {
                val `val` = layout.attributeValue("showMasterSp")
                if (`val` != null && `val`.length > 0 && !pptXmlBoolean(`val`, true)) {
                    pgLayout.setAddShapes(false)
                }
            }
            val cSld = layout.element("cSld")
            if (cSld != null) {
                val spTree = cSld.element("spTree")
                if (spTree != null) {
                    // background
                    processBackgroundAndFill(
                        control!!,
                        zipPackage,
                        layoutPart,
                        pgMaster,
                        pgLayout,
                        cSld
                    )
                    // text style
                    processTextStyle(control, layoutPart, pgMaster, pgLayout, spTree)


                    // slidemaster
                    val pgSlide = PGSlide()
                    pgSlide.setSlideType(PGSlide.Slide_Layout.toInt())
                    val it = spTree.elementIterator()
                    while (it.hasNext()) {
                        ShapeManage.instance().processShape(
                            control!!,
                            zipPackage,
                            layoutPart,
                            null,
                            pgMaster,
                            pgLayout,
                            defaultStyle,
                            pgSlide,
                            PGSlide.Slide_Layout,
                            (it.next() as com.wxiwei.office.fc.dom4j.Element?)!!,
                            null,
                            1.0f,
                            1.0f
                        )
                    }
                    if (pgSlide.getShapeCount() > 0) {
                        pgLayout.setSlideMasterIndex(pgModel.appendSlideMaster(pgSlide))
                    }
                }
            }
        }
        `in`.close()
        return pgLayout
    }

    /**
     * 获取 sp 位置
     * @param layoutPart
     */
    private fun processTextStyle(
        control: IControl?,
        layoutPart: PackagePart?,
        pgMaster: PGMaster?,
        pgLayout: PGLayout,
        spTree: Element
    ) {
        val it = spTree.elementIterator()
        while (it.hasNext()) {
            val sp = it.next() as Element
            val type = ReaderKit.instance().getPlaceholderType(sp)
            val idx = ReaderKit.instance().getPlaceholderIdx(sp)
            val txBody = sp.element("txBody")
            if (txBody != null) {
                val lstStyle = txBody.element("lstStyle")
                StyleReader.Companion.instance().setStyleIndex(style)
                if (!PGPlaceholderUtil.instance().isBody(type)) {
                    pgLayout.setStyleByType(
                        type!!,
                        StyleReader.Companion.instance().getStyles(control, pgMaster, sp, lstStyle)
                    )
                } else if (idx > 0) {
                    pgLayout.setStyleByIdx(
                        idx,
                        StyleReader.Companion.instance().getStyles(control, pgMaster, sp, lstStyle)
                    )
                }

                style = StyleReader.Companion.instance().getStyleIndex()
            }
        }
    }

    /**
     * set background
     * @throws Exception
     */
    @Throws(Exception::class)
    private fun processBackgroundAndFill(
        control: IControl?, zipPackage: ZipPackage?, layoutPart: PackagePart?,
        pgMaster: PGMaster?, pgLayout: PGLayout, cSld: Element
    ) {
        val bg = cSld.element("bg")
        if (bg != null) {
            pgLayout.setBackgroundAndFill(
                BackgroundReader.Companion.instance().getBackground(
                    control!!,
                    zipPackage!!, layoutPart!!, pgMaster, bg
                )
            )
        }
    }

    /**
     * 
     */
    fun dispose() {
        style = 1001
    }

    //
    private var style = 1001

    companion object {
        private val layoutReader = LayoutReader()

        /**
         * 
         */
        @JvmStatic
        fun instance(): LayoutReader {
            return layoutReader
        }
    }
}
