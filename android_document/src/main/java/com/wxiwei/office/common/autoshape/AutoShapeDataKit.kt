/*
 * 文件名称:           AutoShapeDataKit.java
 *  
 * 编译器:             android2.2
 * 时间:               下午4:36:49
 */
package com.wxiwei.office.common.autoshape

import android.graphics.Color
import com.wxiwei.office.common.bg.BackgroundAndFill
import com.wxiwei.office.common.pictureefftect.PictureStretchInfo
import com.wxiwei.office.common.shape.AbstractShape
import com.wxiwei.office.common.shape.ArbitraryPolygonShape
import com.wxiwei.office.common.shape.Arrow
import com.wxiwei.office.common.shape.AutoShape
import com.wxiwei.office.common.shape.LineShape
import com.wxiwei.office.common.shape.PictureShape
import com.wxiwei.office.common.shape.ShapeTypes
import com.wxiwei.office.common.shape.WPAutoShape
import com.wxiwei.office.constant.MainConstant
import com.wxiwei.office.fc.LineKit
import com.wxiwei.office.fc.ShaderKit
import com.wxiwei.office.fc.dom4j.Element
import com.wxiwei.office.fc.openxml4j.opc.PackagePart
import com.wxiwei.office.fc.openxml4j.opc.ZipPackage
import com.wxiwei.office.fc.ppt.reader.ReaderKit
import com.wxiwei.office.fc.ppt.reader.ReaderKit.Companion.instance
import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.ss.util.ColorUtil
import com.wxiwei.office.ss.util.ColorUtil.Companion.instance
import com.wxiwei.office.ss.util.ColorUtil.Companion.rgb
import com.wxiwei.office.system.IControl

/**
 * TODO: 文件注释
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
 * 日期:           2013-4-8
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
object AutoShapeDataKit {
    /**
     * 
     * @param schemeColor
     * @param solidFillElement
     * @return
     */
    @JvmStatic
    fun getColor(schemeColor: Map<String, Int>?, solidFillElement: Element): Int {
        var `val`: String?
        var clr: Element?
        var color = -1
        if (solidFillElement.element("srgbClr") != null) {
            clr = solidFillElement.element("srgbClr")
            color = clr.attributeValue("val").toLong(16).toInt()
            color = (0xFF shl 24) or color
        } else if ((solidFillElement.element("scrgbClr").also { clr = it }) != null) {
            val r = clr!!.attributeValue("r").toInt() * 255 / 100
            val g = clr.attributeValue("g").toInt() * 255 / 100
            val b = clr.attributeValue("b").toInt() * 255 / 100
            return rgb(r, g, b)
        } else if (solidFillElement.element("schemeClr") != null
            || solidFillElement.element("prstClr") != null
        ) {
            if (schemeColor != null && schemeColor.size > 0) {
                clr = solidFillElement.element("schemeClr")
                if (clr == null) {
                    clr = solidFillElement.element("prstClr")
                }
                `val` = clr.attributeValue("val")
                if ("black" == `val`) {
                    color = Color.BLACK
                } else if ("red" == `val`) {
                    color = Color.RED
                } else if ("gray" == `val`) {
                    color = Color.GRAY
                } else if ("blue" == `val`) {
                    color = Color.BLUE
                } else if ("green" == `val`) {
                    color = Color.GREEN
                }
                //get scheme color
                if (color == -1) {
                    color = schemeColor.get(`val`)!!
                }
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
     * 
     * @param control
     * @param zipPackage
     * @param drawingPart
     * @param bgPr
     * @param schemeColor
     * @return
     * @throws Exception
     */
    @JvmStatic
    fun processBackground(
        control: IControl?, zipPackage: ZipPackage?, drawingPart: PackagePart?,
        bgPr: Element?, schemeColor: Map<String, Int>?
    ): BackgroundAndFill? {
        try {
            if (bgPr != null) {
                val bgFill = BackgroundAndFill()
                var fill = bgPr.element("solidFill")
                if (fill != null) {
                    bgFill.setFillType(BackgroundAndFill.FILL_SOLID)
                    bgFill.setForegroundColor(getColor(schemeColor, fill))
                    return bgFill
                } else if ((bgPr.element("blipFill").also { fill = it }) != null) {
                    val blip = fill!!.element("blip")
                    if (blip != null && blip.attribute("embed") != null) {
                        val id = blip.attributeValue("embed")
                        if (id != null) {
                            val imageShip = drawingPart!!.getRelationship(id)
                            if (imageShip != null) {
                                val picPart = zipPackage!!.getPart(imageShip.getTargetURI())
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
                                            control!!.getSysKit().getPictureManage()
                                                .addPicture(picPart)
                                        )
                                    } else {
                                        val index = control!!.getSysKit().getPictureManage()
                                            .addPicture(picPart)
                                        bgFill.setFillType(BackgroundAndFill.FILL_SHADE_TILE)
                                        val tileShader = ShaderKit.readTile(
                                            control!!.getSysKit().getPictureManage()
                                                .getPicture(index), tile
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
                    run {
                        bgFill.setFillType(ShaderKit.getGradientType(fill))
                        bgFill.setShader(ShaderKit.readGradient(schemeColor, fill))
                        return bgFill
                    }
                } else if ((bgPr.element("fillRef").also { fill = it }) != null) {
                    bgFill.setFillType(BackgroundAndFill.FILL_SOLID)
                    bgFill.setForegroundColor(AutoShapeDataKit.getColor(schemeColor, fill!!))
                    return bgFill
                } else if ((bgPr.element("pattFill").also { fill = it }) != null) {
                    val bgClr = fill!!.element("bgClr")
                    run {
                        bgFill.setFillType(BackgroundAndFill.FILL_SOLID)
                        bgFill.setForegroundColor(AutoShapeDataKit.getColor(schemeColor, bgClr))
                        return bgFill
                    }
                }
            }
            return null
        } catch (e: Exception) {
            return null
        }
    }

    @Throws(Exception::class)
    @JvmStatic
    fun getAutoShape(
        control: IControl?, zipPackage: ZipPackage?, drawingPart: PackagePart?,
        sp: Element?, rect: Rectangle?, schemeColor: Map<String, Int>?, type: Int
    ): AbstractShape? {
        return getAutoShape(control, zipPackage, drawingPart, sp, rect, schemeColor, type, false)
    }

    /**
     * 
     * @param control
     * @param zipPackage
     * @param drawingPart
     * @param sp
     * @param rect
     * @param schemeColor
     * @param type
     * @param hasTextbox
     * @return
     * @throws Exception
     */
    @Throws(Exception::class)
    @JvmStatic
    fun getAutoShape(
        control: IControl?,
        zipPackage: ZipPackage?,
        drawingPart: PackagePart?,
        sp: Element?,
        rect: Rectangle?,
        schemeColor: Map<String, Int>?,
        type: Int,
        hasTextbox: Boolean
    ): AbstractShape? {
        if (rect == null || sp == null) {
            return null
        }

        var shapeType = ShapeTypes.ArbitraryPolygon
        val spPr = sp.element("spPr")
        if (spPr != null) {
            var `val`: String?
            var values: Array<Float?>? = null
            var border = true

            val name = ReaderKit.instance().getPlaceholderName(sp)
            val spName = sp.getName()
            if (spName == "cxnSp") {
                border = true
                shapeType = ShapeTypes.Line
            } else if (name != null) {
                if (name.contains("Text Box") || name.contains("TextBox")) {
                    shapeType = ShapeTypes.Rectangle
                }
            }


            // type
            val prstGeom = spPr.element("prstGeom")
            if (prstGeom != null) {
                if (prstGeom.attribute("prst") != null) {
                    `val` = prstGeom.attributeValue("prst")
                    if (`val` != null && `val`.length > 0) {
                        shapeType = AutoShapeTypes.Companion.instance().getAutoShapeType(`val`)
                    }
                }


                // adjust data
                val avLst = prstGeom.element("avLst")
                if (avLst != null) {
                    val gds = avLst.elements("gd") as MutableList<Element>
                    if (gds.size > 0) {
                        values = arrayOfNulls<Float>(gds.size)
                        for (i in gds.indices) {
                            val gd = gds.get(i)
                            `val` = gd.attributeValue("fmla")
                            `val` = `val`.substring(4)
                            values[i] = `val`.toFloat() / 100000
                        }
                    }
                }
            } else if (spPr.element("custGeom") != null) {
                //beizer line or direct line
                shapeType = ShapeTypes.ArbitraryPolygon
            }

            var fill: BackgroundAndFill? = null
            if (fill == null && spPr.element("noFill") == null && (spName != "cxnSp")) {
                fill = processBackground(control, zipPackage, drawingPart, spPr, schemeColor)
                if (fill == null && shapeType != ShapeTypes.Arc && shapeType != ShapeTypes.BracketPair && shapeType != ShapeTypes.LeftBracket && shapeType != ShapeTypes.RightBracket && shapeType != ShapeTypes.BracePair && shapeType != ShapeTypes.LeftBrace && shapeType != ShapeTypes.RightBrace && shapeType != ShapeTypes.ArbitraryPolygon) {
                    fill = processBackground(
                        control,
                        zipPackage,
                        drawingPart,
                        sp.element("style"),
                        schemeColor
                    )
                }
            }

            val line = LineKit.createShapeLine(control, zipPackage, drawingPart, sp, schemeColor)

            val ln = spPr.element("ln")
            val style = sp.element("style")
            if (ln != null) {
                if (ln.element("noFill") != null) {
                    border = false
                }
            } else if (border) {
                if (style == null || style.element("lnRef") == null) {
                    border = false
                }
            }

            if (shapeType != ShapeTypes.Line && shapeType != ShapeTypes.StraightConnector1 && rect != null && (rect.width == 0 || rect.height == 0)) {
                return null
            }


            // lineShape or autoShape
            if (shapeType == ShapeTypes.Line || shapeType == ShapeTypes.StraightConnector1 || shapeType == ShapeTypes.BentConnector3 || shapeType == ShapeTypes.CurvedConnector3) {
                var lineShape: LineShape? = null
                if (type == MainConstant.APPLICATION_TYPE_WP.toInt()) {
                    lineShape = WPAutoShape()
                } else {
                    lineShape = LineShape()
                }
                lineShape.setShapeType(shapeType)
                lineShape.setBounds(rect)
                lineShape.setAdjustData(values)
                lineShape.setLine(line)

                if (ln != null) {
                    var temp = ln.element("headEnd")
                    if (temp != null && temp.attribute("type") != null) {
                        val arrowType = Arrow.getArrowType(temp.attributeValue("type"))
                        if (arrowType != Arrow.Arrow_None) {
                            lineShape.createStartArrow(
                                arrowType,
                                Arrow.getArrowSize(temp.attributeValue("w")),
                                Arrow.getArrowSize(temp.attributeValue("len"))
                            )
                        }
                    }
                    temp = ln.element("tailEnd")
                    if (temp != null && temp.attribute("type") != null) {
                        val arrowType = Arrow.getArrowType(temp.attributeValue("type"))
                        if (arrowType != Arrow.Arrow_None) {
                            lineShape.createEndArrow(
                                arrowType,
                                Arrow.getArrowSize(temp.attributeValue("w")),
                                Arrow.getArrowSize(temp.attributeValue("len"))
                            )
                        }
                    }
                }
                ReaderKit.instance().processRotation(spPr, lineShape)
                return lineShape
            } else if (shapeType == ShapeTypes.ArbitraryPolygon) {
                var arbitraryPolygonShape: ArbitraryPolygonShape? = null
                if (type == MainConstant.APPLICATION_TYPE_WP.toInt()) {
                    arbitraryPolygonShape = WPAutoShape()
                } else {
                    arbitraryPolygonShape = ArbitraryPolygonShape()
                }

                var lineFill: BackgroundAndFill? = null
                if (line != null) {
                    lineFill = line.getBackgroundAndFill()
                }
                ArbitraryPolygonShapePath.processArbitraryPolygonShape(
                    arbitraryPolygonShape,
                    sp,
                    fill,
                    border,
                    lineFill,
                    ln,
                    rect
                )

                arbitraryPolygonShape.setShapeType(shapeType)
                arbitraryPolygonShape.setLine(line)
                ReaderKit.instance().processRotation(spPr, arbitraryPolygonShape)
                return arbitraryPolygonShape
            } else if (hasTextbox || fill != null || border) {
                var autoShape: AutoShape? = null
                if (type == MainConstant.APPLICATION_TYPE_WP.toInt()) {
                    autoShape = WPAutoShape()
                    autoShape.setShapeType(shapeType)
                } else {
                    autoShape = AutoShape(shapeType)
                }
                autoShape.setBounds(rect)

                if (fill != null) {
                    autoShape.setBackgroundAndFill(fill)
                }
                if (line != null) {
                    autoShape.setLine(line)
                }
                autoShape.setAdjustData(values)
                ReaderKit.instance().processRotation(spPr, autoShape)

                return autoShape
            }
        }
        return null
    }

    @JvmStatic
    fun processPictureShape(
        control: IControl?, zipPackage: ZipPackage?, drawingPart: PackagePart?,
        bgPr: Element?, schemeColor: Map<String, Int>?, shape: PictureShape?
    ) {
        if (shape == null) {
            return
        }

        if (bgPr != null) {
            val fill =
                processBackground(
                    control, zipPackage, drawingPart,
                    bgPr, schemeColor
                )

            shape.setBackgroundAndFill(fill)
            val line = LineKit.createLine(
                control,
                zipPackage,
                drawingPart,
                bgPr.element("ln"),
                schemeColor
            )
            shape.setLine(line)
        }
    }
}
