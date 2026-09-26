/*
 * 文件名称:           PPTXmlKit.java
 *  
 * 编译器:             android2.2
 * 时间:               上午9:18:41
 */
package com.wxiwei.office.fc.ppt.reader

import android.graphics.Color
import com.wxiwei.office.common.shape.IShape
import com.wxiwei.office.constant.MainConstant
import com.wxiwei.office.fc.dom4j.Element
import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.pg.model.PGMaster
import com.wxiwei.office.pg.model.PGPlaceholderUtil
import com.wxiwei.office.ss.util.ColorUtil

/**
 * 解析xml
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
 * 日期:           2012-2-21
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
class ReaderKit {
    /**
     * get place holder name
     */
    fun getPlaceholderName(sp: Element?): String? {
        if (sp != null) {
            var temp: Element? = null
            val name = sp.getName()
            if (name == "sp") {
                temp = sp.element("nvSpPr")
            } else if (name == "pic") {
                temp = sp.element("nvPicPr")
            } else if (name == "graphicFrame") {
                temp = sp.element("nvGraphicFramePr")
            } else if (name == "grpSp") {
                temp = sp.element("nvGrpSpPr")
            }
            if (temp != null) {
                val cNvPr = temp.element("cNvPr")
                if (cNvPr != null && cNvPr.attribute("name") != null) {
                    return cNvPr.attributeValue("name")
                }
            }
        }
        return null
    }

    /**
     * get place holder type
     */
    fun getPlaceholderType(sp: Element?): String? {
        if (sp != null) {
            var temp: Element? = null
            val name = sp.getName()
            if (name == "sp") {
                temp = sp.element("nvSpPr")
            } else if (name == "pic") {
                temp = sp.element("nvPicPr")
            } else if (name == "graphicFrame") {
                temp = sp.element("nvGraphicFramePr")
            } else if (name == "grpSp") {
                temp = sp.element("nvGrpSpPr")
            }
            if (temp != null) {
                val nvPr = temp.element("nvPr")
                if (nvPr != null) {
                    val ph = nvPr.element("ph")
                    if (ph != null && ph.attribute("type") != null) {
                        return ph.attributeValue("type")
                    }
                }
            }
        }
        return null
    }

    /**
     * get place holder index
     */
    fun getPlaceholderIdx(sp: Element?): Int {
        if (sp != null) {
            var temp: Element? = null
            val name = sp.getName()
            if (name == "sp") {
                temp = sp.element("nvSpPr")
            } else if (name == "pic") {
                temp = sp.element("nvPicPr")
            } else if (name == "graphicFrame") {
                temp = sp.element("nvGraphicFramePr")
            } else if (name == "grpSp") {
                temp = sp.element("nvGrpSpPr")
            }
            if (temp != null) {
                val nvPr = temp.element("nvPr")
                if (nvPr != null) {
                    val ph = nvPr.element("ph")
                    if (ph != null && ph.attributeValue("idx") != null) {
                        return ph.attributeValue("idx").toDouble().toInt()
                    }
                }
            }
        }
        return -1
    }

    /**
     * get shape anchor
     */
    fun getShapeAnchor(xfrm: Element?, zoomX: Float, zoomY: Float): Rectangle? {
        if (xfrm != null) {
            var `val`: String?
            val rect = Rectangle()
            val off = xfrm.element("off")
            if (off != null) {
                if (off.attribute("x") != null) {
                    `val` = off.attributeValue("x")
                    if (`val` != null && `val`.length > 0) {
                        if (isDecimal(`val`)) {
                            rect.x = ((`val`.toInt() * zoomX
                                    * MainConstant.PIXEL_DPI) / MainConstant.EMU_PER_INCH).toInt()
                        } else {
                            rect.x = ((`val`.toInt(16) * zoomX
                                    * MainConstant.PIXEL_DPI) / MainConstant.EMU_PER_INCH).toInt()
                        }
                    }
                }
                if (off.attribute("y") != null) {
                    `val` = off.attributeValue("y")
                    if (`val` != null && `val`.length > 0) {
                        if (isDecimal(`val`)) {
                            rect.y = ((`val`.toInt() * zoomY
                                    * MainConstant.PIXEL_DPI) / MainConstant.EMU_PER_INCH).toInt()
                        } else {
                            rect.y = ((`val`.toInt(16) * zoomY
                                    * MainConstant.PIXEL_DPI) / MainConstant.EMU_PER_INCH).toInt()
                        }
                    }
                }
            }
            val ext = xfrm.element("ext")
            if (ext != null) {
                if (ext.attribute("cx") != null) {
                    `val` = ext.attributeValue("cx")
                    if (`val` != null && `val`.length > 0) {
                        if (isDecimal(`val`)) {
                            rect.width = ((`val`.toInt() * zoomX
                                    * MainConstant.PIXEL_DPI) / MainConstant.EMU_PER_INCH).toInt()
                        } else {
                            rect.width = ((`val`.toInt(16) * zoomX
                                    * MainConstant.PIXEL_DPI) / MainConstant.EMU_PER_INCH).toInt()
                        }
                    }
                }
                if (ext.attributeValue("cy") != null) {
                    `val` = ext.attributeValue("cy")
                    if (`val` != null && `val`.length > 0) {
                        if (isDecimal(`val`)) {
                            rect.height = ((`val`.toInt() * zoomY
                                    * MainConstant.PIXEL_DPI) / MainConstant.EMU_PER_INCH).toInt()
                        } else {
                            rect.height = ((`val`.toInt(16) * zoomY
                                    * MainConstant.PIXEL_DPI) / MainConstant.EMU_PER_INCH).toInt()
                        }
                    }
                }
            }
            return rect
        }
        return null
    }

    /**
     * get shape anchor
     */
    fun getChildShapeAnchor(xfrm: Element?, zoomX: Float, zoomY: Float): Rectangle? {
        if (xfrm != null) {
            var `val`: String?
            val rect = Rectangle()
            val off = xfrm.element("chOff")
            if (off != null) {
                if (off.attribute("x") != null) {
                    `val` = off.attributeValue("x")
                    if (`val` != null && `val`.length > 0) {
                        if (isDecimal(`val`)) {
                            rect.x = ((`val`.toInt() * zoomX
                                    * MainConstant.PIXEL_DPI) / MainConstant.EMU_PER_INCH).toInt()
                        } else {
                            rect.x = ((`val`.toInt(16) * zoomX
                                    * MainConstant.PIXEL_DPI) / MainConstant.EMU_PER_INCH).toInt()
                        }
                    }
                }
                if (off.attribute("y") != null) {
                    `val` = off.attributeValue("y")
                    if (`val` != null && `val`.length > 0) {
                        if (isDecimal(`val`)) {
                            rect.y = ((`val`.toInt() * zoomY
                                    * MainConstant.PIXEL_DPI) / MainConstant.EMU_PER_INCH).toInt()
                        } else {
                            rect.y = ((`val`.toInt(16) * zoomY
                                    * MainConstant.PIXEL_DPI) / MainConstant.EMU_PER_INCH).toInt()
                        }
                    }
                }
            }
            val ext = xfrm.element("chExt")
            if (ext != null) {
                if (ext.attribute("cx") != null) {
                    `val` = ext.attributeValue("cx")
                    if (`val` != null && `val`.length > 0) {
                        if (isDecimal(`val`)) {
                            rect.width = ((`val`.toInt() * zoomX
                                    * MainConstant.PIXEL_DPI) / MainConstant.EMU_PER_INCH).toInt()
                        } else {
                            rect.width = ((`val`.toInt(16) * zoomX
                                    * MainConstant.PIXEL_DPI) / MainConstant.EMU_PER_INCH).toInt()
                        }
                    }
                }
                if (ext.attributeValue("cy") != null) {
                    `val` = ext.attributeValue("cy")
                    if (`val` != null && `val`.length > 0) {
                        if (isDecimal(`val`)) {
                            rect.height = ((`val`.toInt() * zoomY
                                    * MainConstant.PIXEL_DPI) / MainConstant.EMU_PER_INCH).toInt()
                        } else {
                            rect.height = ((`val`.toInt(16) * zoomY
                                    * MainConstant.PIXEL_DPI) / MainConstant.EMU_PER_INCH).toInt()
                        }
                    }
                }
            }
            return rect
        }
        return null
    }

    /**
     * get note
     */
    fun getNotes(root: Element): String? {
        val cSld = root.element("cSld")
        if (cSld != null) {
            val spTree = cSld.element("spTree")
            if (spTree != null) {
                val sps: MutableList<Element> = spTree.elements("sp") as MutableList<Element>
                for (sp in sps) {
                    val type = getPlaceholderType(sp)
                    if (PGPlaceholderUtil.BODY == type) {
                        var notes: String? = ""
                        val txBody = sp.element("txBody")
                        if (txBody != null) {
                            val ps: MutableList<Element> = txBody.elements("p") as MutableList<Element>
                            for (p in ps) {
                                val rs: MutableList<Element> = p.elements("r") as MutableList<Element>
                                for (r in rs) {
                                    val t = r.element("t")
                                    if (t != null) {
                                        val text = t.getText()
                                        notes += text
                                    }
                                }
                                notes += '\n'
                            }
                        }
                        val txt = notes!!.trim { it <= ' ' }
                        if (txt.length > 0) {
                            return txt
                        }
                    }
                }
            }
        }
        return null
    }

    fun getColor(master: PGMaster?, solidFill: Element?): Int {
        return getColor(master, solidFill, false)
    }

    private fun processColorAttribute(colorE: Element, color: Int, isTableStyle: Boolean): Int {
        var color = color
        if (colorE.element("tint") != null) {
            if (isTableStyle) {
                color = ColorUtil.instance().getColorWithTint(
                    color,
                    1 - colorE.element("tint").attributeValue("val").toInt() / 100000.0
                )
            } else {
                color = ColorUtil.instance().getColorWithTint(
                    color,
                    colorE.element("tint").attributeValue("val").toInt() / 100000.0
                )
            }
        } else if (colorE.element("lumOff") != null) {
            color = ColorUtil.instance().getColorWithTint(
                color,
                colorE.element("lumOff").attributeValue("val").toInt() / 100000.0
            )
        } else if (colorE.element("lumMod") != null) {
            color = ColorUtil.instance().getColorWithTint(
                color,
                colorE.element("lumMod").attributeValue("val").toInt() / 100000.0 - 1
            )
        } else if (colorE.element("shade") != null) {
            color = ColorUtil.instance().getColorWithTint(
                color,
                -colorE.element("shade").attributeValue("val").toInt() / 200000.0
            )
        }

        if (colorE.element("alpha") != null) {
            val `val` = colorE.element("alpha").attributeValue("val")
            if (`val` != null) {
                val alpha = (`val`.toInt() / 100000f * 255).toInt()
                color = (0xFFFFFF and color) or (alpha shl 24)
            }
        }
        return color
    }

    /**
     * get color
     */
    fun getColor(master: PGMaster?, solidFill: Element?, isTableStyle: Boolean): Int {
        if (solidFill != null) {
            val `val`: String?
            var temp = solidFill.element("srgbClr")
            if (temp != null && temp.attribute("val") != null) {
                `val` = temp.attributeValue("val")
                if (`val` != null && `val`.length > 0) {
                    return processColorAttribute(temp, Color.parseColor("#" + `val`), isTableStyle)
                }
            } else if ((solidFill.element("scrgbClr").also { temp = it }) != null) {
                val r = temp!!.attributeValue("r").toInt() * 255 / 100
                val g = temp.attributeValue("g").toInt() * 255 / 100
                val b = temp.attributeValue("b").toInt() * 255 / 100
                return processColorAttribute(temp, ColorUtil.rgb(r, g, b), isTableStyle)
            } else if ((solidFill.element("schemeClr")
                    .also { temp = it }) != null && temp!!.attribute("val") != null
            ) {
                `val` = temp.attributeValue("val")
                if (`val` != null && `val`.length > 0) {
                    var color = -1
                    if (master != null) {
                        color = master.getColor(`val`)
                    }

                    return processColorAttribute(temp, color, isTableStyle)
                }
            } else if ((solidFill.element("sysClr").also { temp = it }) != null) {
                `val` = temp!!.attributeValue("lastClr")
                if (`val` != null && `val`.length > 0) {
                    return Color.parseColor("#" + `val`)
                }
            } else if ((solidFill.element("prstClr").also { temp = it }) != null) {
                `val` = temp!!.attributeValue("val")
                if (`val`.contains("gray")) {
                    return Color.GRAY
                } else if (`val`.contains("white")) {
                    return Color.WHITE
                } else if (`val`.contains("red")) {
                    return Color.RED
                } else if (`val`.contains("green")) {
                    return Color.GREEN
                } else if (`val`.contains("blue")) {
                    return Color.BLUE
                } else if (`val`.contains("yellow")) {
                    return Color.YELLOW
                } else if (`val`.contains("cyan")) {
                    return Color.CYAN
                } else {
                    return Color.BLACK
                }
            }
        }
        return Color.WHITE
    }

    /**
     * 
     * @param num
     * @return
     */
    fun isDecimal(num: String): Boolean {
        val hexChars = "abcdefABCDEF"
        val len = hexChars.length
        var index = 0
        while (index < len) {
            if (num.indexOf(hexChars.get(index)) > -1) {
                return false
            }
            index++
        }
        return true
    }

    /**
     * 
     */
    fun isUserDrawn(sp: Element): Boolean {
        var temp: Element? = null
        val name = sp.getName()
        if (name == "sp") {
            temp = sp.element("nvSpPr")
        } else if (name == "pic") {
            temp = sp.element("nvPicPr")
        } else if (name == "graphicFrame") {
            temp = sp.element("nvGraphicFramePr")
        } else if (name == "grpSp") {
            temp = sp.element("nvGrpSpPr")
        }
        if (temp != null) {
            val nvPr = temp.element("nvPr")
            if (nvPr != null) {
                val ph = nvPr.element("ph")
                if (ph == null /*|| (ph != null && ph.attribute("type") == null && ph.attribute("idx") == null)*/) {
                    return true
                } else if (nvPr.attribute("userDrawn") != null) {
                    val `val` = nvPr.attributeValue("userDrawn")
                    if ((`val` != null && `val`.length > 0 && pptXmlBoolean(`val`))) {
                        return true
                    }
                }
            }
        }
        return false
    }

    /**
     * get shape anchor zoom
     */
    fun getAnchorFitZoom(xfrm: Element?): FloatArray {
        val zoom = floatArrayOf(1.0f, 1.0f)
        if (xfrm != null) {
            var `val`: String?
            var grpWidth = 0f
            var grpHeight = 0f
            var childWidth = 0f
            var childHeight = 0f
            var ext = xfrm.element("ext")
            if (ext != null) {
                if (ext.attribute("cx") != null) {
                    `val` = ext.attributeValue("cx")
                    if (`val` != null && `val`.length > 0) {
                        if (isDecimal(`val`)) {
                            grpWidth = `val`.toInt().toFloat()
                        } else {
                            grpWidth = `val`.toInt(16).toFloat()
                        }
                    }
                }
                if (ext.attributeValue("cy") != null) {
                    `val` = ext.attributeValue("cy")
                    if (`val` != null && `val`.length > 0) {
                        if (isDecimal(`val`)) {
                            grpHeight = `val`.toInt().toFloat()
                        } else {
                            grpHeight = `val`.toInt(16).toFloat()
                        }
                    }
                }
            }
            ext = xfrm.element("chExt")
            if (ext != null) {
                if (ext.attribute("cx") != null) {
                    `val` = ext.attributeValue("cx")
                    if (`val` != null && `val`.length > 0) {
                        if (isDecimal(`val`)) {
                            childWidth = `val`.toInt().toFloat()
                        } else {
                            childWidth = `val`.toInt(16).toFloat()
                        }
                    }
                }
                if (ext.attributeValue("cy") != null) {
                    `val` = ext.attributeValue("cy")
                    if (`val` != null && `val`.length > 0) {
                        if (isDecimal(`val`)) {
                            childHeight = `val`.toInt().toFloat()
                        } else {
                            childHeight = `val`.toInt(16).toFloat()
                        }
                    }
                }
            }
            if (childWidth != 0f && childHeight != 0f) {
                zoom[0] = grpWidth / childWidth
                zoom[1] = grpHeight / childHeight
            }
        }
        return zoom
    }

    /**
     * for shape property element
     * @param spPr
     * @param shape
     */
    fun processRotation(spPr: Element?, shape: IShape) {
        if (spPr != null) {
            processRotation(shape, spPr.element("xfrm"))
        }
    }

    /**
     * for xfrm element
     * @param shape
     * @param xfrm
     */
    fun processRotation(shape: IShape, xfrm: Element?) {
        if (xfrm != null) {
            var `val`: String?
            if (xfrm.attribute("flipH") != null) {
                `val` = xfrm.attributeValue("flipH")
                if (`val` != null && `val`.length > 0 && pptXmlBoolean(`val`)) {
                    shape.setFlipHorizontal(true)
                }
            }
            if (xfrm.attribute("flipV") != null) {
                `val` = xfrm.attributeValue("flipV")
                if (`val` != null && `val`.length > 0 && pptXmlBoolean(`val`)) {
                    shape.setFlipVertical(true)
                }
            }
            if (xfrm.attribute("rot") != null) {
                `val` = xfrm.attributeValue("rot")
                if (`val` != null && `val`.length > 0) {
                    shape.setRotation(`val`.toFloat() / 60000)
                }
            }
        }
    }

    /**
     * 
     */
    fun isHidden(sp: Element): Boolean {
        var temp: Element? = null
        val name = sp.getName()
        if (name == "sp") {
            temp = sp.element("nvSpPr")
        } else if (name == "pic") {
            temp = sp.element("nvPicPr")
        } else if (name == "graphicFrame") {
            temp = sp.element("nvGraphicFramePr")
        } else if (name == "grpSp") {
            temp = sp.element("nvGrpSpPr")
        }
        if (temp != null) {
            val cNvPr = temp.element("cNvPr")
            if (cNvPr != null && cNvPr.attribute("hidden") != null && pptXmlBoolean(cNvPr.attributeValue("hidden"))
            ) {
                return true
            }
        }
        return false
    }

    companion object {
        private val kit = ReaderKit()

        /**
         * 
         */
        @JvmStatic
        fun instance(): ReaderKit {
            return kit
        }
    }
}
