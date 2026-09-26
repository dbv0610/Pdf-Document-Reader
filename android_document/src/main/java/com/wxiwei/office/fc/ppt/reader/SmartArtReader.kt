/*
 * 文件名称:          SmartArtReader.java
 *  
 * 编译器:            android2.2
 * 时间:              下午1:47:12
 */
package com.wxiwei.office.fc.ppt.reader

import com.wxiwei.office.common.autoshape.ArbitraryPolygonShapePath
import com.wxiwei.office.common.autoshape.AutoShapeTypes
import com.wxiwei.office.common.bg.BackgroundAndFill
import com.wxiwei.office.common.shape.ArbitraryPolygonShape
import com.wxiwei.office.common.shape.Arrow
import com.wxiwei.office.common.shape.AutoShape
import com.wxiwei.office.common.shape.IShape
import com.wxiwei.office.common.shape.LineShape
import com.wxiwei.office.common.shape.ShapeTypes
import com.wxiwei.office.common.shape.SmartArt
import com.wxiwei.office.common.shape.TextBox
import com.wxiwei.office.constant.MainConstant
import com.wxiwei.office.fc.LineKit
import com.wxiwei.office.fc.dom4j.Element
import com.wxiwei.office.fc.dom4j.io.SAXReader
import com.wxiwei.office.fc.openxml4j.opc.PackagePart
import com.wxiwei.office.fc.openxml4j.opc.ZipPackage
import com.wxiwei.office.fc.ppt.ShapeManage.Companion.instance
import com.wxiwei.office.fc.ppt.attribute.ParaAttr
import com.wxiwei.office.fc.ppt.attribute.ParaAttr.Companion.instance
import com.wxiwei.office.fc.ppt.attribute.RunAttr.Companion.instance
import com.wxiwei.office.fc.ppt.attribute.SectionAttr
import com.wxiwei.office.fc.ppt.reader.ReaderKit.Companion.instance
import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.pg.model.PGLayout
import com.wxiwei.office.pg.model.PGMaster
import com.wxiwei.office.pg.model.PGModel
import com.wxiwei.office.pg.model.PGPlaceholderUtil
import com.wxiwei.office.pg.model.PGSlide
import com.wxiwei.office.simpletext.model.AttrManage
import com.wxiwei.office.simpletext.model.IAttributeSet
import com.wxiwei.office.simpletext.model.SectionElement
import com.wxiwei.office.system.IControl

/**
 * TODO: 文件注释
 * 
 * 
 * 
 * 
 * Read版本:        Read V1.0
 * 
 * 
 * 作者:            jqin
 * 
 * 
 * 日期:            2013-4-27
 * 
 * 
 * 负责人:           jqin
 * 
 * 
 * 负责小组:
 * 
 * 
 * 
 * 
 */
class SmartArtReader {
    /**
     * get AbstractChart
     * @param chartPart
     * @param sheet
     * @return
     */
    @Throws(Exception::class)
    fun read(
        control: IControl?, zipPackage: ZipPackage, pgModel: PGModel?, pgMaster: PGMaster?,
        pgLayout: PGLayout?, pgSlide: PGSlide, slidePart: PackagePart, dataPart: PackagePart
    ): SmartArt? {
        val saxreader = SAXReader()
        var `in` = dataPart.getInputStream()
        val dataDoc = saxreader.read(`in`)
        `in`.close()
        var root = dataDoc.getRootElement()

        val fill: BackgroundAndFill? = BackgroundReader.Companion.instance()
            .processBackground(control!!, zipPackage, dataPart, pgMaster, root.element("bg"))

        val line = LineKit.createLine(
            control,
            zipPackage,
            dataPart,
            pgMaster,
            root.element("whole").element("ln")
        )
        var drawingPart: PackagePart? = null
        var e: Element? = null
        if ((root.element("extLst").also { e = it }) != null && (e!!.element("ext")
                .also { e = it }) != null && (e!!.element("dataModelExt").also { e = it }) != null
        ) {
            val relId = e!!.attributeValue("relId")
            if (relId != null) {
                val smartArDrawingRel = slidePart.getRelationship(relId)
                if (smartArDrawingRel != null) {
                    drawingPart = zipPackage.getPart(smartArDrawingRel.getTargetURI())
                }
            }
        }
        if (drawingPart == null) {
            return null
        }

        `in` = drawingPart.getInputStream()
        val smartArtDoc = saxreader.read(`in`)
        `in`.close()

        val smartArt = SmartArt()
        smartArt.setBackgroundAndFill(fill)
        smartArt.setLine(line)

        root = smartArtDoc.getRootElement()
        val spTree = root.element("spTree")
        if (spTree != null) {
            val it = spTree.elementIterator("sp")
            while (it.hasNext()) {
                val sp = it.next() as Element
                var shape: IShape? = null

                shape = processAutoShape(
                    control!!, zipPackage, drawingPart, pgModel, pgMaster,
                    pgLayout, pgSlide, sp
                )
                if (shape != null) {
                    shape.setParent(smartArt)
                    smartArt.appendShapes(shape)
                }

                shape = getTextBoxData(control, pgMaster, pgLayout, sp)
                if (shape != null) {
                    smartArt.appendShapes(shape)
                }
            }
        }

        return smartArt
    }

    @Throws(Exception::class)
    private fun getBackgrouond(
        control: IControl?,
        zipPackage: ZipPackage?,
        smartArtPart: PackagePart?,
        pgModel: PGModel?,
        pgMaster: PGMaster?,
        pgLayout: PGLayout?,
        pgSlide: PGSlide,
        sp: Element,
        shapeType: Int
    ): BackgroundAndFill? {
        // fill
        var fill: BackgroundAndFill? = null
        if (sp.attribute("useBgFill") != null) {
            val `val` = sp.attributeValue("useBgFill")
            if (`val` != null && `val`.length > 0 && pptXmlBoolean(`val`)) {
                fill = pgSlide.getBackgroundAndFill()
                if (fill == null) {
                    if (pgLayout != null) {
                        fill = pgLayout.getBackgroundAndFill()
                    }
                    if (fill == null && pgMaster != null) {
                        fill = pgMaster.getBackgroundAndFill()
                    }
                }
            }
        }

        val spPr = sp.element("spPr")
        val spName = sp.getName()
        if (fill == null && spPr.element("noFill") == null && (spName != "cxnSp")) {
            fill = BackgroundReader.Companion.instance()
                .processBackground(control!!, zipPackage!!, smartArtPart!!, pgMaster, spPr)
            if (fill == null && shapeType != ShapeTypes.Arc && shapeType != ShapeTypes.BracketPair && shapeType != ShapeTypes.LeftBracket && shapeType != ShapeTypes.RightBracket && shapeType != ShapeTypes.BracePair && shapeType != ShapeTypes.LeftBrace && shapeType != ShapeTypes.RightBrace && shapeType != ShapeTypes.ArbitraryPolygon) {
                fill = BackgroundReader.Companion.instance().processBackground(
                    control!!,
                    zipPackage!!,
                    smartArtPart!!,
                    pgMaster,
                    sp.element("style")
                )
            }
        }

        return fill
    }

    /**
     * process group rotate
     * @param parent
     * @param shape
     * @return
     */
    private fun processGrpRotation(shape: IShape, spPr: Element?) {
        ReaderKit.instance().processRotation(spPr, shape)
    }

    @Throws(Exception::class)
    private fun processAutoShape(
        control: IControl?,
        zipPackage: ZipPackage?,
        smartArtPart: PackagePart?,
        pgModel: PGModel?,
        pgMaster: PGMaster?,
        pgLayout: PGLayout?,
        pgSlide: PGSlide,
        sp: Element
    ): IShape? {
        val spPr = sp.element("spPr")
        var rect: Rectangle? = null
        if (spPr == null) {
            return null
        }
        rect = ReaderKit.instance().getShapeAnchor(spPr.element("xfrm"), 1f, 1f)

        var shapeType = ShapeTypes.NotPrimitive
        //        if (spPr != null)
        run {
            var `val`: String?
            var values: Array<Float?>? = null
            var border = true
            val name = ReaderKit.instance().getPlaceholderName(sp)
            val spName = sp.getName()
            if (spName == "cxnSp") {
                border = true
                shapeType = ShapeTypes.Line
            } else if (name!!.contains("Text Box") || name.contains("TextBox")) {
                shapeType = ShapeTypes.Rectangle
            }


            // type
            val prstGeom = spPr.element("prstGeom")
            if (prstGeom != null) {
                if (prstGeom.attribute("prst") != null) {
                    `val` = prstGeom.attributeValue("prst")
                    if (`val` != null && `val`.length > 0) {
                        shapeType = AutoShapeTypes.instance().getAutoShapeType(`val`)
                    }
                }


                // adjust data
                val avLst = prstGeom.element("avLst")
                if (avLst != null) {
                    val gds: MutableList<Element>? = avLst.elements("gd") as MutableList<Element>
                    if (gds != null && gds.size > 0) {
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

            val fill = getBackgrouond(
                control,
                zipPackage,
                smartArtPart,
                pgModel,
                pgMaster,
                pgLayout,
                pgSlide,
                sp,
                shapeType
            )
            val line = LineKit.createShapeLine(control, zipPackage, smartArtPart, pgMaster, sp)

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


            // lineShape or autoShape
            if (shapeType == ShapeTypes.Line || shapeType == ShapeTypes.StraightConnector1 || shapeType == ShapeTypes.BentConnector3 || shapeType == ShapeTypes.CurvedConnector3) {
                val lineShape = LineShape()
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
                            lineShape.createStartArrow(
                                arrowType,
                                Arrow.getArrowSize(temp.attributeValue("w")),
                                Arrow.getArrowSize(temp.attributeValue("len"))
                            )
                        }
                    }
                }
                processGrpRotation(lineShape, spPr)

                return lineShape
            } else if (shapeType == ShapeTypes.ArbitraryPolygon) {
                val arbitraryPolygonShape = ArbitraryPolygonShape()
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
                processGrpRotation(arbitraryPolygonShape, spPr)
                arbitraryPolygonShape.setLine(line)

                return arbitraryPolygonShape
            } else if (fill != null || line != null) {
                val autoShape = AutoShape(shapeType)
                autoShape.setBounds(rect)
                if (fill != null) {
                    autoShape.setBackgroundAndFill(fill)
                }
                if (line != null) {
                    autoShape.setLine(line)
                }
                autoShape.setAdjustData(values)
                processGrpRotation(autoShape, spPr)
                return autoShape
            }
        }
        return null
    }

    private fun getTextBoxData(
        control: IControl?,
        pgMaster: PGMaster?,
        pgLayout: PGLayout?,
        sp: Element
    ): IShape? {
        val temp = sp.element("txXfrm")
        var rect: Rectangle? = null
        if (temp != null) {
            rect = ReaderKit.instance().getShapeAnchor(temp, 1f, 1f)
        }

        val txBody = sp.element("txBody")
        if (txBody != null) {
            val tb = TextBox()
            // anchor 
            tb.setBounds(rect)


            // 建立章节
            val secElem = SectionElement()
            // 开始Offset
            secElem.setStartOffset(0)
            tb.setElement(secElem)
            // 属性
            val attr = secElem.getAttribute()
            // 宽度
            AttrManage.instance()
                .setPageWidth(attr, (rect!!.width * MainConstant.PIXEL_TO_TWIPS).toInt())
            // 高度
            AttrManage.instance()
                .setPageHeight(attr, (rect.height * MainConstant.PIXEL_TO_TWIPS).toInt())

            var attrLayout: IAttributeSet? = null
            var attrMaster: IAttributeSet? = null

            val type = PGPlaceholderUtil.DIAGRAM
            val idx = 0
            if (pgLayout != null) {
                attrLayout = pgLayout.getSectionAttr(null, idx)
            }
            if (pgMaster != null) {
                attrMaster = pgMaster.getSectionAttr(null, idx)
            }

            SectionAttr.Companion.instance()
                .setSectionAttribute(txBody.element("bodyPr"), attr, attrLayout, attrMaster, false)
            val offset = ParaAttr.instance().processParagraph(
                control, pgMaster, pgLayout, null,
                secElem, sp.element("style"), txBody, type, idx
            )

            secElem.setEndOffset(offset.toLong())
            if (tb.getElement() != null && tb.getElement().getText(null) != null && tb.getElement()
                    .getText(null)!!.length > 0 && ("\n" != tb.getElement().getText(null))
            ) {
                ReaderKit.instance().processRotation(tb, sp.element("txXfrm"))
            }


            // wrap line
            val wrap = txBody.element("bodyPr")
            if (wrap != null) {
                // 文本框内自动换行
                val value = wrap.attributeValue("wrap")
                tb.setWrapLine(value == null || "square".equals(value, ignoreCase = true))
            }

            return tb
        }

        return null
    }

    companion object {
        private val reader = SmartArtReader()

        /**
         * 
         */
        @JvmStatic
        fun instance(): SmartArtReader {
            return reader
        }
    }
}
