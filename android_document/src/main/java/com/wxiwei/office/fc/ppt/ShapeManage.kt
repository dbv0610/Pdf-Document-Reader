/*
 * 文件名称:           ShapeManage.java
 *  
 * 编译器:             android2.2
 * 时间:               下午4:25:43
 */
package com.wxiwei.office.fc.ppt

import com.wxiwei.office.fc.ppt.reader.pptXmlBoolean
import com.wxiwei.office.common.autoshape.ArbitraryPolygonShapePath
import com.wxiwei.office.common.autoshape.AutoShapeTypes
import com.wxiwei.office.common.bg.BackgroundAndFill
import com.wxiwei.office.common.pictureefftect.PictureEffectInfo
import com.wxiwei.office.common.pictureefftect.PictureEffectInfoFactory
import com.wxiwei.office.common.shape.AChart
import com.wxiwei.office.common.shape.AbstractShape
import com.wxiwei.office.common.shape.ArbitraryPolygonShape
import com.wxiwei.office.common.shape.Arrow
import com.wxiwei.office.common.shape.AutoShape
import com.wxiwei.office.common.shape.GroupShape
import com.wxiwei.office.common.shape.IShape
import com.wxiwei.office.common.shape.LineShape
import com.wxiwei.office.common.shape.PictureShape
import com.wxiwei.office.common.shape.ShapeTypes
import com.wxiwei.office.common.shape.TextBox
import com.wxiwei.office.constant.MainConstant
import com.wxiwei.office.fc.LineKit
import com.wxiwei.office.fc.dom4j.Element
import com.wxiwei.office.fc.openxml4j.opc.PackagePart
import com.wxiwei.office.fc.openxml4j.opc.PackageRelationshipTypes
import com.wxiwei.office.fc.openxml4j.opc.ZipPackage
import com.wxiwei.office.fc.ppt.attribute.ParaAttr
import com.wxiwei.office.fc.ppt.attribute.RunAttr
import com.wxiwei.office.fc.ppt.attribute.SectionAttr
import com.wxiwei.office.fc.ppt.reader.BackgroundReader
import com.wxiwei.office.fc.ppt.reader.PictureReader
import com.wxiwei.office.fc.ppt.reader.ReaderKit
import com.wxiwei.office.fc.ppt.reader.TableReader
import com.wxiwei.office.fc.xls.Reader.drawing.ChartReader
import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.pg.model.PGLayout
import com.wxiwei.office.pg.model.PGMaster
import com.wxiwei.office.pg.model.PGModel
import com.wxiwei.office.pg.model.PGPlaceholderUtil
import com.wxiwei.office.pg.model.PGSlide
import com.wxiwei.office.pg.model.PGStyle
import com.wxiwei.office.simpletext.model.AttrManage
import com.wxiwei.office.simpletext.model.IAttributeSet
import com.wxiwei.office.simpletext.model.SectionElement
import com.wxiwei.office.system.IControl

/**
 * process shape
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
 * 日期:           2012-6-12
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
class ShapeManage {
    /**
     * process shape
     * @param zipPackage
     * @param packagePart
     * @param pgModel TODO
     * @param pgMaster
     * @param pgLayout
     * @param defaultStyle
     * @param pgSlide
     * @param slideType TODO
     * @param sp
     * @param pgMasterSlide TODO
     * @param grprect for shapeGroup
     * @param offsetRect for shapeGroup
     * @throws Exception
     */
    @Throws(Exception::class)
    fun processShape(
        control: IControl,
        zipPackage: ZipPackage,
        packagePart: PackagePart,
        pgModel: PGModel?,
        pgMaster: PGMaster?,
        pgLayout: PGLayout?,
        defaultStyle: PGStyle?,
        pgSlide: PGSlide,
        slideType: Byte,
        sp: Element,
        parent: GroupShape?,
        zoomX: Float,
        zoomY: Float
    ): Int? {
        if (ReaderKit.instance().isHidden(sp)) {
            return null
        }
        var addShape = packagePart.getPartName().getName().contains("/ppt/slides/")
        addShape = addShape || (!addShape && ReaderKit.instance().isUserDrawn(sp))
        RunAttr.instance().setSlide(addShape)
        val name = sp.getName()
        if (name == "sp" || name == "cxnSp") {
            // auto shape
            return processAutoShapeAndTextShape(
                control,
                zipPackage,
                packagePart,
                pgModel,
                pgMaster,
                pgLayout,
                defaultStyle,
                pgSlide,
                slideType,
                sp,
                parent,
                zoomX,
                zoomY,
                addShape
            )
        } else if (name == "pic") {
            // picture shape
            if (addShape) {
                return processPicture(
                    control!!,
                    zipPackage!!,
                    packagePart!!,
                    pgMaster,
                    pgLayout,
                    pgSlide,
                    sp,
                    parent,
                    zoomX,
                    zoomY
                )
            }
        } else if (name == "graphicFrame") {
            // graphicFrame
            if (addShape) {
                return processGraphicFrame(
                    control!!, zipPackage, packagePart, pgModel,
                    pgMaster, pgLayout, pgSlide, sp, parent, zoomX, zoomY
                )
            }
        } else if (name == "grpSp") {
            //shape id
            var e = sp.element("nvGrpSpPr")
            var grpShapeID = 0
            if (e != null && (e.element("cNvPr").also { e = it }) != null) {
                grpShapeID = e!!.attributeValue("id").toInt()
            }


            // shapeGroup
            var groupShape: GroupShape? = null
            var zoomXY: FloatArray? = null
            val grpSpPr = sp.element("grpSpPr")
            if (grpSpPr != null) {
                var rect: Rectangle? = null
                var childRect: Rectangle? = null
                rect = ReaderKit.instance().getShapeAnchor(grpSpPr.element("xfrm"), zoomX, zoomY)
                rect = processGrpSpRect(parent, rect!!)

                zoomXY = ReaderKit.instance().getAnchorFitZoom(grpSpPr.element("xfrm"))
                childRect = ReaderKit.instance().getChildShapeAnchor(
                    grpSpPr.element("xfrm"),
                    zoomXY[0] * zoomX,
                    zoomXY[1] * zoomY
                )

                groupShape = GroupShape()
                groupShape.setOffPostion(
                    rect.x - childRect!!.x,
                    rect.y - childRect!!.y
                )
                groupShape.setShapeID(grpShapeID)

                groupShape.setBounds(rect)
                groupShape.setParent(parent)
                processGrpRotation(parent, groupShape, grpSpPr)
            }

            val childShapeLst: MutableList<Int> = ArrayList<Int>()
            var shapeId: Int?
            val it = sp.elementIterator()
            while (it.hasNext()) {
                shapeId = processShape(
                    control!!,
                    zipPackage!!,
                    packagePart!!,
                    pgModel,
                    pgMaster,
                    pgLayout,
                    defaultStyle,
                    pgSlide,
                    slideType,
                    (it.next() as com.wxiwei.office.fc.dom4j.Element?)!!,
                    groupShape,
                    zoomXY!![0] * zoomX,
                    zoomXY[1] * zoomY
                )
                if (shapeId != null) {
                    childShapeLst.add(shapeId)
                }
            }

            if (parent == null) {
                pgSlide.appendShapes(groupShape)
            } else {
                parent.appendShapes(groupShape)
            }
            pgSlide.addGroupShape(grpShapeID, childShapeLst)

            return grpShapeID
        } else if (name == "AlternateContent") {
            val choice = sp.element("Fallback")
            if (choice != null) {
                val it = choice.elementIterator()
                while (it.hasNext()) {
                    processShape(
                        control!!,
                        zipPackage!!,
                        packagePart!!,
                        pgModel,
                        pgMaster,
                        pgLayout,
                        defaultStyle,
                        pgSlide,
                        slideType,
                        (it.next() as com.wxiwei.office.fc.dom4j.Element?)!!,
                        parent,
                        zoomX,
                        zoomY
                    )
                }
            }
        }
        RunAttr.instance().setSlide(false)

        return null
    }

    /**
     * process textbox
     * @param zipPackage
     * @param packagePart
     * @param pgMasterSlide TODO
     * @param pgMaster
     * @param pgLayout
     * @param defaultStyle
     * @param sp
     * @param tb
     * @param grpRect
     * @param offsetRect
     * @throws Exception
     */
    @Throws(Exception::class)
    fun processAutoShapeAndTextShape(
        control: IControl?,
        zipPackage: ZipPackage?,
        packagePart: PackagePart?,
        pgModel: PGModel?,
        pgMaster: PGMaster?,
        pgLayout: PGLayout?,
        defaultStyle: PGStyle?,
        pgSlide: PGSlide,
        slideType: Byte,
        sp: Element,
        parent: GroupShape?,
        zoomX: Float,
        zoomY: Float,
        addShape: Boolean
    ): Int {
        //shape id
        var id = 0
        var temp = sp.element("nvSpPr")
        if (temp == null) {
            temp = sp.element("nvCxnSpPr")
        }
        temp = temp.element("cNvPr")
        id = temp.attributeValue("id").toInt()

        var type = ReaderKit.instance().getPlaceholderType(sp)
        val idx = ReaderKit.instance().getPlaceholderIdx(sp)
        if (slideType == PGSlide.Slide_Layout) {
            pgLayout!!.addShapeType(idx, type)
        } else if (type == null && pgMaster != null && idx >= 0) {
            type = pgLayout!!.getShapeType(idx)
        }

        var placeHolderID = -1
        if (PGPlaceholderUtil.instance().isTitleOrBody(type)) {
            if (slideType == PGSlide.Slide_Master) {
                pgMaster!!.addTitleBodyID(idx, idx)
            } else if (slideType == PGSlide.Slide_Layout) {
                pgLayout!!.addTitleBodyID(idx, id)
            }
        } else if ((slideType == PGSlide.Slide_Master || slideType == PGSlide.Slide_Layout)
            && ReaderKit.instance().isUserDrawn(sp)
        ) {
            placeHolderID = 0
        }


        // get anchor
        var rect: Rectangle? = null
        temp = sp.element("spPr")
        if (temp != null) {
            rect = ReaderKit.instance().getShapeAnchor(temp.element("xfrm"), zoomX, zoomY)
        }
        if (rect == null && pgLayout != null) {
            rect = pgLayout.getAnchor(type, idx)
            if (rect == null && pgMaster != null) {
                rect = pgMaster.getAnchor(type, idx)
            }
        }
        if (rect != null) {
            rect = processGrpSpRect(parent, rect)

            var shape: AbstractShape? = null
            // autoshape
            if (addShape || (!addShape && !PGPlaceholderUtil.instance().isHeaderFooter(type))) {
                shape = processAutoShape(
                    control!!,
                    zipPackage!!,
                    packagePart!!,
                    pgModel,
                    pgMaster,
                    pgLayout,
                    pgSlide,
                    sp,
                    id,
                    idx,
                    rect,
                    isRect(type, idx),
                    parent,
                    slideType,
                    type,
                    !addShape && PGPlaceholderUtil.instance().isTitleOrBody(type)
                )
            }

            if (shape != null) {
                if (parent == null) {
                    pgSlide.appendShapes(shape)
                } else {
                    parent.appendShapes(shape)
                }

                shape.setPlaceHolderID(placeHolderID)
                processGrpRotation(parent, shape, sp.element("spPr"))
            }


            // ======== 处理文本 ========
            temp = sp.element("txBody")
            if (temp != null && addShape) {
                val tb = TextBox()
                // anchor 
                tb.setBounds(rect)
                tb.setPlaceHolderID(placeHolderID)
                tb.setShapeID(id)
                // 建立章节
                val secElem = SectionElement()
                // 开始Offset
                secElem.setStartOffset(0)
                tb.setElement(secElem)
                // 属性
                val attr = secElem.getAttribute()
                // 宽度
                AttrManage.instance()
                    .setPageWidth(attr, (rect.width * MainConstant.PIXEL_TO_TWIPS).toInt())
                // 高度
                AttrManage.instance()
                    .setPageHeight(attr, (rect.height * MainConstant.PIXEL_TO_TWIPS).toInt())

                var attrLayout: IAttributeSet? = null
                var attrMaster: IAttributeSet? = null
                if (pgLayout != null) {
                    attrLayout = pgLayout.getSectionAttr(type, idx)
                }
                if (pgMaster != null) {
                    attrMaster = pgMaster.getSectionAttr(type, idx)
                }
                SectionAttr.instance().setSectionAttribute(
                    temp.element("bodyPr"),
                    attr,
                    attrLayout,
                    attrMaster,
                    false
                )
                val offset = ParaAttr.instance().processParagraph(
                    control!!, pgMaster, pgLayout, defaultStyle,
                    secElem, sp.element("style"), temp, type, idx
                )
                secElem.setEndOffset(offset.toLong())
                if (tb.getElement() != null && tb.getElement()
                        .getText(null) != null && tb.getElement()
                        .getText(null)!!.length > 0 && ("\n" != tb.getElement().getText(null))
                ) {
                    processGrpRotation(parent, tb, sp.element("spPr"))

                    if (parent == null) {
                        pgSlide.appendShapes(tb)
                    } else {
                        parent.appendShapes(tb)
                    }
                } else if (shape != null) {
                    //process autoshape rotation
                    processGrpRotation(parent, shape, sp.element("spPr"))
                }


                // wrap line
                val wrap = temp.element("bodyPr")
                if (wrap != null) {
                    // 文本框内自动换行
                    val value = wrap.attributeValue("wrap")
                    tb.setWrapLine(value == null || "square".equals(value, ignoreCase = true))
                }
            }
        }

        return id
    }


    /**
     * process picture
     * @param zipPackage
     * @param packagePart
     * @param pgSlide
     * @param sp
     * @param grpRect
     * @param offsetRect
     * @throws Exception
     */
    @Throws(Exception::class)
    fun processPicture(
        control: IControl,
        zipPackage: ZipPackage,
        packagePart: PackagePart,
        pgMaster: PGMaster?,
        pgLayout: PGLayout?,
        pgSlide: PGSlide,
        sp: Element,
        parent: GroupShape?,
        zoomX: Float,
        zoomY: Float
    ): Int {
        //shape id
        var e = sp.element("nvPicPr")
        var shapeID = 0
        if (e != null && (e.element("cNvPr").also { e = it }) != null) {
            shapeID = e!!.attributeValue("id").toInt()
        }

        var blipFill = sp.element("blipFill")
        if (blipFill == null) {
            val alternateContent = sp.element("AlternateContent")
            if (alternateContent != null) {
                val fallback = alternateContent.element("Fallback")
                if (fallback != null) {
                    blipFill = fallback.element("blipFill")
                }
            }
        }
        if (blipFill != null) {
            val blip = blipFill.element("blip")
            if (blip != null && blip.attribute("embed") != null) {
                val id = blip.attributeValue("embed")
                if (id != null) {
                    val spPr = sp.element("spPr")
                    if (spPr != null) {
                        var rect =
                            ReaderKit.instance().getShapeAnchor(spPr.element("xfrm"), zoomX, zoomY)
                        if (rect == null && pgLayout != null) {
                            //String name = ReaderKit.instance().getPlaceholderName(sp);
                            val type = ReaderKit.instance().getPlaceholderType(sp)
                            //type = PGPlaceholderUtil.instance().processType(name, type);
                            val idx = ReaderKit.instance().getPlaceholderIdx(sp)
                            rect = pgLayout.getAnchor(type, idx)
                            if (rect == null && pgMaster != null) {
                                rect = pgMaster.getAnchor(type, idx)
                            }
                        }
                        if (rect != null) {
                            rect = processGrpSpRect(parent, rect!!)
                            val imageShip = packagePart.getRelationship(id)
                            if (imageShip != null) {
                                val fill = BackgroundReader.instance().processBackground(
                                    control!!,
                                    zipPackage!!,
                                    packagePart!!,
                                    pgMaster,
                                    spPr
                                )
                                val line = LineKit.createShapeLine(
                                    control!!,
                                    zipPackage!!,
                                    packagePart!!,
                                    pgMaster,
                                    sp
                                )


//                            	if(fill == null)
//                            	{
//                            		//slide background fill
//                            		fill = pgSlide.getBackgroundAndFill();
//                                    if (fill == null)
//                                    {
//                                        if (pgLayout != null)
//                                        {
//                                            fill = pgLayout.getBackgroundAndFill();
//                                        }
//                                        if (fill == null && pgMaster != null)
//                                        {
//                                            fill = pgMaster.getBackgroundAndFill();
//                                        }
//                                    }
//                                    
//                                    if(fill != null)
//                                    {
//                                    	fill.setSlideBackgroundFill(true);
//                                    }
//                            	}
                                val picPart = zipPackage.getPart(imageShip.getTargetURI())
                                val picShape = addPicture(
                                    control!!,
                                    picPart,
                                    pgSlide,
                                    shapeID,
                                    rect,
                                    sp.element("spPr"),
                                    parent,
                                    PictureEffectInfoFactory.getPictureEffectInfor(blipFill)
                                )
                                if (picShape != null) {
                                    picShape.setBackgroundAndFill(fill)
                                    picShape.setLine(line)
                                }
                            }
                        }
                    }
                }
            }
        }

        return shapeID
    }

    /**
     * add picture to slide
     * @param picPart
     * @param pgSlide
     * @param rect
     * @throws Exception
     */
    @Throws(Exception::class)
    fun addPicture(
        control: IControl,
        picPart: PackagePart?,
        pgSlide: PGSlide,
        shapeID: Int,
        rect: Rectangle?,
        spPr: Element?,
        parent: GroupShape?,
        effectInfor: PictureEffectInfo?
    ): PictureShape? {
        var picShape: PictureShape? = null
        if (picPart != null) {
            picShape = PictureShape()
            picShape.setPictureIndex(control.getSysKit().getPictureManage().addPicture(picPart))
            picShape.setBounds(rect)
            processGrpRotation(parent, picShape, spPr)
            picShape.setShapeID(shapeID)
            picShape.setPictureEffectInfor(effectInfor)
            if (parent == null) {
                pgSlide.appendShapes(picShape)
            } else {
                parent.appendShapes(picShape)
            }
        }

        return picShape
    }

    /**
     * process grahicFrame
     * @param zipPackage
     * @param packagePart
     * @param pgMaster
     * @param pgSlide
     * @param sp
     * @param grpRect
     * @param offsetRect
     * @throws Exception
     */
    @Throws(Exception::class)
    fun processGraphicFrame(
        control: IControl,
        zipPackage: ZipPackage,
        packagePart: PackagePart,
        pgModel: PGModel?,
        pgMaster: PGMaster?,
        pgLayout: PGLayout?,
        pgSlide: PGSlide,
        sp: Element,
        parent: GroupShape?,
        zoomX: Float,
        zoomY: Float
    ): Int {
        //shape id
        var nvGraphicFramePr = sp.element("nvGraphicFramePr")
        var shapeId = 0
        if (nvGraphicFramePr != null && (nvGraphicFramePr.element("cNvPr")
                .also { nvGraphicFramePr = it }) != null
        ) {
            shapeId = nvGraphicFramePr!!.attributeValue("id").toInt()
        }

        val xfrm = sp.element("xfrm")
        var rect = ReaderKit.instance().getShapeAnchor(xfrm, zoomX, zoomY)
        if (rect == null && pgLayout != null) {
            //String name = ReaderKit.instance().getPlaceholderName(sp);
            val type = ReaderKit.instance().getPlaceholderType(sp)
            //type = PGPlaceholderUtil.instance().processType(name, type);
            val idx = ReaderKit.instance().getPlaceholderIdx(sp)
            rect = pgLayout.getAnchor(type, idx)
            if (rect == null && pgMaster != null) {
                rect = pgMaster.getAnchor(type, idx)
            }
        }
        if (rect != null) {
            rect = processGrpSpRect(parent, rect)
            val graphic = sp.element("graphic")
            if (graphic != null) {
                val graphicData = graphic.element("graphicData")
                if (graphicData != null && graphicData.attribute("uri") != null) {
                    val uri = graphicData.attributeValue("uri")
                    if (uri == PackageRelationshipTypes.OLE_TYPE) {
                        var oleObj = graphicData.element("oleObj")
                        if (oleObj == null) {
                            val alternateContent = graphicData.element("AlternateContent")
                            if (alternateContent != null) {
                                val fallback = alternateContent.element("Fallback")
                                if (fallback != null) {
                                    oleObj = fallback.element("oleObj")
                                    if (oleObj != null) {
                                        val pic = oleObj.element("pic")
                                        if (pic != null) {
                                            processPicture(
                                                control!!,
                                                zipPackage!!,
                                                packagePart!!,
                                                pgMaster,
                                                pgLayout,
                                                pgSlide,
                                                pic,
                                                parent,
                                                zoomX,
                                                zoomY
                                            )
                                        }
                                    }
                                }
                            }
                        } else if (oleObj.attribute("spid") != null) {
                            val spid = oleObj.attributeValue("spid")
                            val picPart = PictureReader.instance()
                                .getOLEPart(zipPackage, packagePart, spid, false)
                            addPicture(
                                control!!,
                                picPart,
                                pgSlide,
                                shapeId,
                                rect,
                                sp.element("spPr"),
                                parent,
                                null
                            )
                        }
                    } else if (uri == PackageRelationshipTypes.CHART_TYPE) {
                        val chart = graphicData.element("chart")
                        if (chart != null && chart.attribute("id") != null) {
                            val id = chart.attributeValue("id")
                            val ship = packagePart.getRelationship(id)
                            if (ship != null) {
                                val chartPart = zipPackage.getPart(ship.getTargetURI())
                                val abstrChart = ChartReader.instance().read(
                                    control!!,
                                    zipPackage!!,
                                    chartPart,
                                    pgMaster!!.getSchemeColor()!!,
                                    MainConstant.APPLICATION_TYPE_PPT
                                )
                                if (abstrChart != null) {
                                    val shape = AChart()
                                    shape.setAChart(abstrChart)
                                    shape.setBounds(rect)
                                    shape.setShapeID(shapeId)
                                    pgSlide.appendShapes(shape)
                                }
                            }
                        }
                    } else if (uri == PackageRelationshipTypes.TABLE_TYPE) {
                        val tbl = graphicData.element("tbl")
                        if (tbl != null) {
                            val temp = tbl.element("tblPr")
                            if (temp != null) {
                                val table = TableReader.instance().getTable(
                                    control!!, zipPackage, packagePart, pgModel!!,
                                    pgMaster, tbl, rect
                                )
                                if (table != null) {
                                    table.setBounds(rect)
                                    table.setShapeID(shapeId)
                                    pgSlide.appendShapes(table)
                                }
                            }
                        }
                    } else if (uri == PackageRelationshipTypes.DIAGRAM_TYPE) {
                        processSmartArt(pgSlide, graphicData, rect)
                    }
                }
            }
        }

        return shapeId
    }

    /**
     * 重新计算group shape中的child shape的位置
     * @param grpRect
     * @param offsetRect
     * @param rect
     * @return
     */
    private fun processGrpSpRect(parent: GroupShape?, rect: Rectangle): Rectangle {
        if (parent != null) {
            rect.x += parent.getOffX()
            rect.y += parent.getOffY()
        }
        return rect
    }

    /**
     * process group rotate
     * @param parent
     * @param shape
     * @return
     */
    private fun processGrpRotation(parent: IShape?, shape: IShape?, spPr: Element?) {
        ReaderKit.instance().processRotation(spPr, shape!!)
        /*if (shape != null && parent != null)
        {
            shape.setRotation(shape.getRotation() + parent.getRotation());
        }*/
    }

    /**
     * check the type of a shape is or not a rectangle
     * @param name
     * @return
     */
    private fun isRect(type: String?, idx: Int): Boolean {
        if (type != null && (type.contains("Title") || type.contains("title")
                    || type.contains("ctrTitle") || type.contains("subTitle") || type.contains("body")
                    || type.contains("body") || type.contains("half") || type.contains("dt")
                    || type.contains("ftr") || type.contains("sldNum"))
        ) {
            return true
        } else if (idx > 0) {
            return true
        }
        return false
    }


    /**
     * 
     * @param zipPackage
     * @param packagePart
     * @param pgModel
     * @param pgMaster
     * @param pgLayout
     * @param pgSlide
     * @param sp
     * @param shapeID
     * @param slideType Normal, layout, master
     * @param phType title, body, ft, dt and so on
     * @param shapeType auto shape type
     * @return
     * @throws Exception
     */
    @Throws(Exception::class)
    private fun getBackgrouond(
        control: IControl?,
        zipPackage: ZipPackage?,
        packagePart: PackagePart?,
        pgModel: PGModel?,
        pgMaster: PGMaster?,
        pgLayout: PGLayout?,
        pgSlide: PGSlide,
        sp: Element,
        shapeIDX: Int,
        slideType: Byte,
        phType: String?,
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

                if (fill != null) {
                    fill.setSlideBackgroundFill(true)
                }

                return fill
            }
        }

        val spPr = sp.element("spPr")
        val spName = sp.getName()
        if (fill == null && spPr.element("noFill") == null && (spName != "cxnSp")) {
            fill = BackgroundReader.instance()
                .processBackground(control!!, zipPackage!!, packagePart!!, pgMaster, spPr)
            if (fill == null && shapeType != ShapeTypes.Arc && shapeType != ShapeTypes.BracketPair && shapeType != ShapeTypes.LeftBracket && shapeType != ShapeTypes.RightBracket && shapeType != ShapeTypes.BracePair && shapeType != ShapeTypes.LeftBrace && shapeType != ShapeTypes.RightBrace && shapeType != ShapeTypes.ArbitraryPolygon) {
                fill = BackgroundReader.instance().processBackground(
                    control!!,
                    zipPackage!!,
                    packagePart!!,
                    pgMaster,
                    sp.element("style")
                )
                if (fill != null && fill.getFillType() == BackgroundAndFill.FILL_SOLID && (fill.getForegroundColor() and 0xFFFFFF) == 0) {
                    fill = null
                }
            }
        }

        var shapeID: Int? = null


        //get bg from layout slide
        if (fill == null && slideType == PGSlide.Slide_Normal && PGPlaceholderUtil.instance()
                .isTitleOrBody(phType)
            && pgLayout != null && pgLayout.getSlideMasterIndex() >= 0 && shapeIDX >= 0
        ) {
            val layoutSlide = pgModel!!.getSlideMaster(pgLayout.getSlideMasterIndex())
            shapeID = pgLayout.getTitleBodyID(shapeIDX)
            if (shapeID != null) {
                val shapes = layoutSlide!!.getShapes()
                for (i in shapes.indices) {
                    if (shapeID == shapes[i]!!.getShapeID() && shapes[i] is AutoShape) {
                        fill = (shapes[i] as AutoShape).getBackgroundAndFill()
                        break
                    }
                }
            }
        }


        //get bg from master slide
        if (fill == null && slideType == PGSlide.Slide_Normal && pgMaster != null && pgMaster.getSlideMasterIndex() >= 0 && shapeIDX >= 0) {
            val masterSlide = pgModel!!.getSlideMaster(pgMaster.getSlideMasterIndex())
            val shapes = masterSlide!!.getShapes()
            if (pgMaster.getTitleBodyID(shapeIDX) != null) {
                shapeID = pgMaster.getTitleBodyID(shapeIDX)
                if (shapeID != null) {
                    for (i in shapes.indices) {
                        if (shapeID == shapes[i]!!.getShapeID() && shapes[i] is AutoShape) {
                            fill = (shapes[i] as AutoShape).getBackgroundAndFill()
                            break
                        }
                    }
                }
            }
        }

        return fill
    }

    /**
     * 
     * @param zipPackage
     * @param packagePart
     * @param pgModel
     * @param pgMaster
     * @param pgLayout
     * @param pgSlide
     * @param sp
     * @param id
     * @param rect
     * @param isRect
     * @param parent
     * @param titleOrBody
     * @param hidden
     * @return shapetype
     * @throws Exception
     */
    @Throws(Exception::class)
    fun processAutoShape(
        control: IControl?,
        zipPackage: ZipPackage?,
        packagePart: PackagePart?,
        pgModel: PGModel?,
        pgMaster: PGMaster?,
        pgLayout: PGLayout?,
        pgSlide: PGSlide,
        sp: Element,
        id: Int,
        idx: Int,
        rect: Rectangle?,
        isRect: Boolean,
        parent: GroupShape?,
        slideType: Byte,
        phType: String?,
        hidden: Boolean
    ): AbstractShape? {
        var shape: AbstractShape? = null
        var shapeType = ShapeTypes.NotPrimitive
        val spPr = sp.element("spPr")
        if (spPr != null) {
            var `val`: String?
            var values: Array<Float?>? = null
            var border = true
            val name = ReaderKit.instance().getPlaceholderName(sp)
            val spName = sp.getName()
            if (spName == "cxnSp") {
                border = true
                shapeType = ShapeTypes.StraightConnector1
            } else if (isRect || name!!.contains("Text Box") || name!!.contains("TextBox")) {
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
                    val gds: MutableList<Element> = avLst.elements("gd") as MutableList<Element>
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
            } else if (isRect) {
                shapeType = ShapeTypes.Rectangle
            }

            val fill = getBackgrouond(
                control,
                zipPackage,
                packagePart,
                pgModel,
                pgMaster,
                pgLayout,
                pgSlide,
                sp,
                idx,
                slideType,
                phType,
                shapeType
            )
            val line = LineKit.createShapeLine(control, zipPackage, packagePart, pgMaster, sp)


            // border
            val ln = spPr.element("ln")
            val style = sp.element("style")
            if (ln != null) {
                //border                
                if (ln.element("noFill") != null) {
                    border = false
                }
            } else if (border) {
                if (style == null || style.element("lnRef") == null) {
                    border = false
                }
            }


            // lineShape or autoShape
            if (shapeType == ShapeTypes.Line || shapeType == ShapeTypes.StraightConnector1 || shapeType == ShapeTypes.BentConnector2 || shapeType == ShapeTypes.BentConnector3 || shapeType == ShapeTypes.CurvedConnector2 || shapeType == ShapeTypes.CurvedConnector3 || shapeType == ShapeTypes.CurvedConnector4 || shapeType == ShapeTypes.CurvedConnector5) {
                if (!border) {
                    return shape
                }
                val lineShape = LineShape()
                lineShape.setShapeType(shapeType)
                lineShape.setBounds(rect)
                lineShape.setShapeID(id)
                lineShape.setHidden(hidden)
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

                return lineShape.also { shape = it }
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
                arbitraryPolygonShape.setShapeID(id)
                processGrpRotation(parent, arbitraryPolygonShape, spPr)
                arbitraryPolygonShape.setHidden(hidden)
                arbitraryPolygonShape.setLine(line)

                return arbitraryPolygonShape.also { shape = it }
            } else if (fill != null || line != null) {
                val autoShape = AutoShape(shapeType)
                autoShape.setBounds(rect)
                autoShape.setShapeID(id)
                autoShape.setHidden(hidden)

                if (fill != null) {
                    autoShape.setBackgroundAndFill(fill)
                }
                if (line != null) {
                    autoShape.setLine(line)
                }
                autoShape.setAdjustData(values)

                return autoShape.also { shape = it }
            }
        }
        return shape
    }

    private fun processSmartArt(pgslide: PGSlide, graphicData: Element?, rect: Rectangle?) {
        try {
            if (graphicData != null) {
                val relIds = graphicData.element("relIds")
                val cs = relIds.attributeValue("dm")
                val id = cs.substring("rId".length).toInt()
                if (cs != null) {
                    val smartArt = pgslide.getSmartArt(cs)
                    if (smartArt != null) {
                        smartArt.setBounds(rect)
                        val shapes = smartArt.getShapes()
                        for (shape in shapes) {
                            shape.setShapeID(id)
                        }

                        pgslide.appendShapes(smartArt)
                    }
                }
            }
        } catch (e: Exception) {
        }
    }

    companion object {
        private val kit = ShapeManage()

        /**
         * 
         */
        @JvmStatic
        fun instance(): ShapeManage {
            return kit
        }
    }
}
