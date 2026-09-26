/*
 * 文件名称:           BackgroundReader.java
 *  
 * 编译器:             android2.2
 * 时间:               下午5:16:06
 */
package com.wxiwei.office.fc.ppt.reader

import com.wxiwei.office.common.bg.BackgroundAndFill
import com.wxiwei.office.common.pictureefftect.PictureStretchInfo
import com.wxiwei.office.fc.ShaderKit
import com.wxiwei.office.fc.dom4j.Element
import com.wxiwei.office.fc.openxml4j.opc.PackagePart
import com.wxiwei.office.fc.openxml4j.opc.ZipPackage
import com.wxiwei.office.pg.model.PGMaster
import com.wxiwei.office.system.IControl

/**
 * 解析 background
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
class BackgroundReader {
    /**
     * get background
     * @throws Exception
     */
    @Throws(Exception::class)
    fun getBackground(
        control: IControl, zipPackage: ZipPackage,
        packagePart: PackagePart, master: PGMaster?, bg: Element?
    ): BackgroundAndFill? {
        if (bg != null) {
            val bgPr = bg.element("bgPr")
            val bgRef = bg.element("bgRef")
            if (bgRef != null) {
                val bgFill = BackgroundAndFill()
                bgFill.setFillType(BackgroundAndFill.FILL_SOLID)
                bgFill.setForegroundColor(ReaderKit.instance().getColor(master, bgRef))
                return bgFill
            } else {
                return processBackground(control, zipPackage, packagePart, master, bgPr)
            }
        }
        return null
    }

    /**
     * 
     * @param zipPackage
     * @param packagePart
     * @param master
     * @param bgPr
     * @return
     * @throws Exception
     */
    @Throws(Exception::class)
    fun processBackground(
        control: IControl, zipPackage: ZipPackage, packagePart: PackagePart,
        master: PGMaster?, bgPr: Element?
    ): BackgroundAndFill? {
        return processBackground(control, zipPackage, packagePart, master, bgPr, false)
    }

    /**
     * 
     * @param zipPackage
     * @param packagePart
     * @param master
     * @param bgPr
     * @param isTableStyle
     * @return
     * @throws Exception
     */
    @Throws(Exception::class)
    fun processBackground(
        control: IControl, zipPackage: ZipPackage, packagePart: PackagePart,
        master: PGMaster?, bgPr: Element?, isTableStyle: Boolean
    ): BackgroundAndFill? {
        if (bgPr != null) {
            val bgFill = BackgroundAndFill()
            var fill = bgPr.element("solidFill")
            if (fill != null) {
                bgFill.setFillType(BackgroundAndFill.FILL_SOLID)
                bgFill.setForegroundColor(ReaderKit.instance().getColor(master, fill, isTableStyle))
                return bgFill
            } else if ((bgPr.element("blipFill").also { fill = it }) != null) {
                val blip = fill!!.element("blip")
                if (blip != null && blip.attribute("embed") != null) {
                    val id = blip.attributeValue("embed")
                    if (id != null) {
                        val imageShip = packagePart.getRelationship(id)
                        if (imageShip != null) {
                            val picPart = zipPackage.getPart(imageShip.getTargetURI())
                            if (picPart != null) {
                                val tile = fill.element("tile")
                                if (tile == null) {
                                    bgFill.setFillType(BackgroundAndFill.FILL_PICTURE)
                                    val stretch = fill.element("stretch")
                                    if (stretch != null) {
                                        val fillRect = stretch.element("fillRect")
                                        if (fillRect != null) {
                                            val stretchInfo = PictureStretchInfo()
                                            var validate = false
                                            var str = fillRect.attributeValue("l")
                                            if (str != null) {
                                                validate = true
                                                stretchInfo.setLeftOffset(str.toFloat() / 100000)
                                            }

                                            str = fillRect.attributeValue("r")
                                            if (str != null) {
                                                validate = true
                                                stretchInfo.setRightOffset(str.toFloat() / 100000)
                                            }

                                            str = fillRect.attributeValue("t")
                                            if (str != null) {
                                                validate = true
                                                stretchInfo.setTopOffset(str.toFloat() / 100000)
                                            }

                                            str = fillRect.attributeValue("b")
                                            if (str != null) {
                                                validate = true
                                                stretchInfo.setBottomOffset(str.toFloat() / 100000)
                                            }

                                            if (validate) {
                                                bgFill.setStretch(stretchInfo)
                                            }
                                        }
                                    }

                                    bgFill.setPictureIndex(
                                        control.getSysKit().getPictureManage().addPicture(picPart)
                                    )
                                } else {
                                    val index =
                                        control.getSysKit().getPictureManage().addPicture(picPart)
                                    bgFill.setFillType(BackgroundAndFill.FILL_SHADE_TILE)
                                    val tileShader = ShaderKit.readTile(
                                        control.getSysKit().getPictureManage().getPicture(index),
                                        tile
                                    )
                                    val alphaModFix = blip.element("alphaModFix")
                                    if (alphaModFix != null) {
                                        val amt = alphaModFix.attributeValue("amt")
                                        if (amt != null) {
                                            tileShader.setAlpha(Math.round(amt.toInt() / 100000f * 255))
                                        }
                                    }
                                    bgFill.setShader(tileShader)
                                }

                                return bgFill
                            }
                        }
                    }
                }
            } else if ((bgPr.element("gradFill").also { fill = it }) != null) {
                val gsLst = fill!!.element("gsLst")
                if (gsLst != null) {
                    bgFill.setFillType(ShaderKit.getGradientType(fill))
                    bgFill.setShader(ShaderKit.readGradient(master, fill))
                    return bgFill
                }
            } else if ((bgPr.element("fillRef").also { fill = it }) != null) {
                bgFill.setFillType(BackgroundAndFill.FILL_SOLID)
                bgFill.setForegroundColor(ReaderKit.instance().getColor(master, fill))
                return bgFill
            } else if ((bgPr.element("pattFill").also { fill = it }) != null) {
                val bgClr = fill!!.element("bgClr")
                run {
                    bgFill.setFillType(BackgroundAndFill.FILL_SOLID)
                    bgFill.setForegroundColor(ReaderKit.instance().getColor(master, bgClr))
                    return bgFill
                }
            }
        }
        return null
    }

    /**
     * get background
     * @throws Exception
     */
    @Throws(Exception::class)
    fun getBackgroundColor(
        zipPackage: ZipPackage?, packagePart: PackagePart?,
        master: PGMaster?, bgPr: Element?, isTableStyle: Boolean
    ): Int {
        if (bgPr != null) {
            var fill = bgPr.element("solidFill")
            if (fill != null) {
                return ReaderKit.instance().getColor(master, fill, isTableStyle)
            } else if ((bgPr.element("gradFill").also { fill = it }) != null) {
                val gsLst = fill!!.element("gsLst")
                if (gsLst != null) {
                    return ReaderKit.instance().getColor(master, gsLst.element("gs"))
                }
            } else if ((bgPr.element("fillRef").also { fill = it }) != null) {
                return ReaderKit.instance().getColor(master, fill)
            } else if ((bgPr.element("pattFill").also { fill = it }) != null) {
                val bgClr = fill!!.element("bgClr")
                run {
                    return ReaderKit.instance().getColor(master, bgClr)
                }
            }
        }
        return 0
    }

    companion object {
        private val bgReader = BackgroundReader()

        /**
         * 
         */
        @JvmStatic
        fun instance(): BackgroundReader {
            return bgReader
        }
    }
}
