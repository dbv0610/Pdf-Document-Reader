/*
 * 文件名称:           PGMaster.java
 *  
 * 编译器:             android2.2
 * 时间:               下午5:17:16
 */
package com.wxiwei.office.fc.ppt.reader

import com.wxiwei.office.fc.dom4j.Element
import com.wxiwei.office.fc.dom4j.io.SAXReader
import com.wxiwei.office.fc.openxml4j.opc.PackagePart
import com.wxiwei.office.fc.openxml4j.opc.PackageRelationshipTypes
import com.wxiwei.office.fc.openxml4j.opc.ZipPackage
import com.wxiwei.office.fc.ppt.ShapeManage
import com.wxiwei.office.fc.ppt.ShapeManage.Companion.instance
import com.wxiwei.office.fc.ppt.attribute.ParaAttr.Companion.instance
import com.wxiwei.office.fc.ppt.attribute.RunAttr.Companion.instance
import com.wxiwei.office.fc.ppt.reader.ReaderKit.Companion.instance
import com.wxiwei.office.pg.model.PGMaster
import com.wxiwei.office.pg.model.PGModel
import com.wxiwei.office.pg.model.PGPlaceholderUtil
import com.wxiwei.office.pg.model.PGSlide
import com.wxiwei.office.system.IControl

/**
 * 解析 master
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
 * 日期:           2012-2-16
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
class MasterReader {
    /**
     * 
     * @param zipPackage
     * @param masterPart
     * @return
     * @throws Exception
     */
    @Throws(Exception::class)
    fun getMasterData(
        control: IControl, zipPackage: ZipPackage, masterPart: PackagePart,
        pgModel: PGModel
    ): PGMaster? {
        val saxreader = SAXReader()
        val `in` = masterPart.getInputStream()
        val poiMaster = saxreader.read(`in`)
        val master = poiMaster.getRootElement()
        var pgMaster: PGMaster? = null
        if (master != null) {
            pgMaster = PGMaster()
            // color map
            processClrMap(pgMaster, zipPackage, masterPart, master)
            // text style
            processStyle(control, pgMaster, master)
            //
            val cSld = master.element("cSld")
            if (cSld != null) {
                // background 
                processBackgroundAndFill(control, pgMaster, zipPackage, masterPart, cSld)

                val spTree = cSld.element("spTree")
                if (spTree != null) {
                    processTextStyle(control, pgMaster, spTree)


                    // slidemaster
                    val pgSlide = PGSlide()
                    pgSlide.setSlideType(PGSlide.Slide_Master.toInt())
                    val it = spTree.elementIterator()
                    while (it.hasNext()) {
                        ShapeManage.instance().processShape(
                            control!!,
                            zipPackage,
                            masterPart,
                            null,
                            pgMaster,
                            null,
                            null,
                            pgSlide,
                            PGSlide.Slide_Master,
                            (it.next() as com.wxiwei.office.fc.dom4j.Element?)!!,
                            null,
                            1.0f,
                            1.0f
                        )
                    }
                    if (pgSlide.getShapeCount() > 0) {
                        pgMaster.setSlideMasterIndex(pgModel.appendSlideMaster(pgSlide))
                    }
                }
            }
        }
        `in`.close()
        return pgMaster
    }

    /**
     * process color map
     * @throws Exception
     */
    @Throws(Exception::class)
    private fun processClrMap(
        pgMaster: PGMaster, zipPackage: ZipPackage, masterPart: PackagePart,
        master: Element
    ) {
        // get theme part
        val themeShip = masterPart.getRelationshipsByType(
            PackageRelationshipTypes.THEME_PART
        ).getRelationship(0)
        if (themeShip != null) {
            val themePart = zipPackage.getPart(themeShip.getTargetURI())
            if (themePart != null) {
                val themeColor: MutableMap<String, Int> =
                    ThemeReader.Companion.instance().getThemeColorMap(themePart)!!

                val clrMap = master.element("clrMap")
                if (clrMap != null) {
                    for (i in 0..<clrMap.attributeCount()) {
                        val name = clrMap.attribute(i).getName()
                        val value = clrMap.attributeValue(name)
                        if (name != value) {
                            pgMaster.addColor(value, themeColor.get(value)!!)
                        }
                        pgMaster.addColor(name, themeColor.get(value)!!)
                    }
                }
            }
        }
    }

    /**
     * set background
     * @throws Exception
     */
    @Throws(Exception::class)
    private fun processBackgroundAndFill(
        control: IControl?, pgMaster: PGMaster, zipPackage: ZipPackage?,
        masterPart: PackagePart?, cSld: Element
    ) {
        val bg = cSld.element("bg")
        if (bg != null) {
            pgMaster.setBackgroundAndFill(
                BackgroundReader.Companion.instance().getBackground(
                    control!!,
                    zipPackage!!, masterPart!!, pgMaster, bg
                )
            )
        }
    }

    /**
     * 处理 Shape部分
     */
    @Throws(Exception::class)
    private fun processTextStyle(control: IControl?, pgMaster: PGMaster, spTree: Element) {
        val it = spTree.elementIterator()
        while (it.hasNext()) {
            val sp = it.next() as Element
            var type = ReaderKit.instance().getPlaceholderType(sp)
            type = PGPlaceholderUtil.instance().checkTypeName(type)
            val idx = ReaderKit.instance().getPlaceholderIdx(sp)
            val txBody = sp.element("txBody")
            if (txBody != null) {
                val lstStyle = txBody.element("lstStyle")
                StyleReader.Companion.instance().setStyleIndex(styleIndex)
                if (!PGPlaceholderUtil.instance().isBody(type)) {
                    pgMaster.addStyleByType(
                        type!!,
                        StyleReader.Companion.instance().getStyles(control, pgMaster, sp, lstStyle)
                    )
                } else if (idx > 0) {
                    pgMaster.addStyleByIdx(
                        idx,
                        StyleReader.Companion.instance().getStyles(control, pgMaster, sp, lstStyle)
                    )
                }

                styleIndex = StyleReader.Companion.instance().getStyleIndex()
            }
        }
    }

    /**
     * 处理text style部分
     */
    private fun processStyle(control: IControl?, pgMaster: PGMaster, master: Element) {
        val txStyles = master.element("txStyles")
        if (txStyles != null) {
            StyleReader.Companion.instance().setStyleIndex(styleIndex)

            var style = txStyles.element("titleStyle")
            pgMaster.setTitleStyle(
                StyleReader.Companion.instance().getStyles(control, pgMaster, null, style)
            )

            style = txStyles.element("bodyStyle")
            pgMaster.setBodyStyle(
                StyleReader.Companion.instance().getStyles(control, pgMaster, null, style)
            )

            style = txStyles.element("otherStyle")
            pgMaster.setDefaultStyle(
                StyleReader.Companion.instance().getStyles(control, pgMaster, null, style)
            )

            styleIndex = StyleReader.Companion.instance().getStyleIndex()
        }
    }

    /**
     * 
     */
    fun dispose() {
        styleIndex = 10
    }

    //
    private var styleIndex = 10

    companion object {
        private val masterReader = MasterReader()

        /**
         * 
         */
        @JvmStatic
        fun instance(): MasterReader {
            return masterReader
        }
    }
}
