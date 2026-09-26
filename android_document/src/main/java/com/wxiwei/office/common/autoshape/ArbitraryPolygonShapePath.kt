/*
 * 文件名称:           ArbitraryPolygonShapePath.java
 *  
 * 编译器:             android2.2
 * 时间:               上午9:55:22
 */
package com.wxiwei.office.common.autoshape

import android.graphics.Matrix
import android.graphics.Path
import android.graphics.PointF
import android.graphics.RectF
import com.wxiwei.office.common.autoshape.pathbuilder.ArrowPathAndTail
import com.wxiwei.office.common.autoshape.pathbuilder.LineArrowPathBuilder
import com.wxiwei.office.common.bg.BackgroundAndFill
import com.wxiwei.office.common.shape.ArbitraryPolygonShape
import com.wxiwei.office.common.shape.Arrow
import com.wxiwei.office.constant.MainConstant
import com.wxiwei.office.fc.dom4j.Element
import com.wxiwei.office.java.awt.Rectangle
import kotlin.math.min

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
 * 日期:           2013-3-13
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
object ArbitraryPolygonShapePath {
    @Throws(Exception::class)
    @JvmStatic
    fun processArbitraryPolygonShape(
        arbitraryPolygonShape: ArbitraryPolygonShape?,
        sp: Element,
        fill: BackgroundAndFill?,
        border: Boolean,
        lineFill: BackgroundAndFill?,
        ln: Element?,
        rect: Rectangle?
    ) {
        if (arbitraryPolygonShape == null) {
            return
        }

        var lineWidth = 1
        if (ln != null) {
            //line width
            if (ln.attributeValue("w") != null) {
                lineWidth = Math.round(
                    ln.attributeValue("w")
                        .toInt() * MainConstant.PIXEL_DPI / MainConstant.EMU_PER_INCH
                )
            }
        }

        val line = arbitraryPolygonShape.createLine()
        line.setBackgroundAndFill(lineFill)
        line.setLineWidth(lineWidth)


        // anchor
        arbitraryPolygonShape.setBounds(rect)


        // paths
        val spPr = sp.element("spPr")
        val pathElements: MutableList<Element> =
            spPr.element("custGeom").element("pathLst").elements("path") as MutableList<Element>

        var headArrowTailCenter: PointF? = null
        var tailArrowTailCenter: PointF? = null
        var pathExtend_Header: ExtendPath? = null
        var pathExtend_Tail: ExtendPath? = null
        // arrow
        if (pathElements.size == 1 && ln != null) {
            val width = pathElements.get(0).attributeValue("w")
            val height = pathElements.get(0).attributeValue("h")
            if (width != null && height != null) {
                val w = width.toInt() * MainConstant.PIXEL_DPI / MainConstant.EMU_PER_INCH
                val h = height.toInt() * MainConstant.PIXEL_DPI / MainConstant.EMU_PER_INCH
                lineWidth = (lineWidth * min(w / rect!!.width, h / rect!!.height)).toInt()
            }

            var temp = ln.element("headEnd")
            if (temp != null && temp.attribute("type") != null) {
                if ("none" != temp.attributeValue("type")) {
                    val arrowType = Arrow.getArrowType(temp.attributeValue("type"))
                    arbitraryPolygonShape.createStartArrow(
                        arrowType,
                        getArrowSize(temp.attributeValue("w")),
                        getArrowSize(temp.attributeValue("len"))
                    )
                    //
                    val pathElement1 = pathElements.get(0)
                    val arrowPathAndTail = getPathHeadArrowPath(
                        arbitraryPolygonShape.getStartArrow(), lineWidth,
                        pathElement1
                    )
                    if (arrowPathAndTail != null) {
                        val path1 = arrowPathAndTail.arrowPath
                        headArrowTailCenter = arrowPathAndTail.arrowTailCenter

                        if (path1 != null) {
                            // extendPath
                            pathExtend_Header = ExtendPath()
                            pathExtend_Header.setArrowFlag(true)
                            pathExtend_Header.path = path1
                            if (fill != null || border) {
                                if (border &&
                                    (pathElement1.attribute("stroke") == null
                                            || pathElement1.attributeValue("stroke").toInt() != 0)
                                ) {
                                    if (arrowType != Arrow.Arrow_Arrow) {
                                        pathExtend_Header.backgroundAndFill = lineFill
                                    } else {
                                        pathExtend_Header.setLine(line)
                                    }
                                } else if (fill != null) {
                                    pathExtend_Header.backgroundAndFill = fill
                                }
                            }
                        }
                    }
                }
            }


            temp = ln.element("tailEnd")
            if (temp != null && temp.attribute("type") != null) {
                if ("none" != temp.attributeValue("type")) {
                    val arrowType = Arrow.getArrowType(temp.attributeValue("type"))
                    arbitraryPolygonShape.createEndArrow(
                        arrowType,
                        getArrowSize(temp.attributeValue("w")),
                        getArrowSize(temp.attributeValue("len"))
                    )


                    //
                    val pathElement1 = pathElements.get(0)
                    val arrowPathAndTail = getPathTailArrowPath(
                        arbitraryPolygonShape.getEndArrow(), lineWidth,
                        pathElement1
                    )
                    if (arrowPathAndTail != null) {
                        val path1 = arrowPathAndTail.arrowPath
                        tailArrowTailCenter = arrowPathAndTail.arrowTailCenter
                        if (path1 != null) {
                            // extendPath
                            pathExtend_Tail = ExtendPath()
                            pathExtend_Tail.setArrowFlag(true)
                            pathExtend_Tail.path = path1
                            if (fill != null || border) {
                                if (border &&
                                    (pathElement1.attribute("stroke") == null
                                            || pathElement1.attributeValue("stroke").toInt() != 0)
                                ) {
                                    if (arrowType != Arrow.Arrow_Arrow) {
                                        pathExtend_Tail.backgroundAndFill = lineFill
                                    } else {
                                        pathExtend_Tail.setLine(line)
                                    }
                                } else if (fill != null) {
                                    pathExtend_Tail.backgroundAndFill = fill
                                }
                            }
                        }
                    }
                }
            }
        }

        for (j in pathElements.indices) {
            val pathExtend = ExtendPath()
            val path = getArrowPath(
                arbitraryPolygonShape,
                pathElements.get(j),
                rect!!,
                fill,
                border,
                headArrowTailCenter,
                tailArrowTailCenter
            )

            pathExtend.path = path
            val pathElement = pathElements.get(j)
            val width = pathElement.attributeValue("w")
            val height = pathElement.attributeValue("h")
            val m = Matrix()
            if (width != null && height != null) {
                val w = width.toInt() * MainConstant.PIXEL_DPI / MainConstant.EMU_PER_INCH
                val h = height.toInt() * MainConstant.PIXEL_DPI / MainConstant.EMU_PER_INCH

                m.postScale(rect!!.width / w, rect!!.height / h)
                path.transform(m)
            }


            // fill and border
            if (fill != null || border) {
                if (fill != null) {
                    if (pathElement.attribute("fill") != null
                        && "none" == pathElement.attributeValue("fill")
                    ) {
                        pathExtend.backgroundAndFill = null
                    } else {
                        pathExtend.backgroundAndFill = fill
                    }
                }

                if (border) {
                    if (pathElement.attribute("stroke") != null
                        && pathElement.attributeValue("stroke").toInt() == 0
                    ) {
                        pathExtend.setLine(false)
                    } else {
                        pathExtend.setLine(line)
                    }
                }
            }

            arbitraryPolygonShape.appendPath(pathExtend)


            //
            if (pathExtend_Header != null) {
                pathExtend_Header.path!!.transform(m)
                arbitraryPolygonShape.appendPath(pathExtend_Header)
            }

            if (pathExtend_Tail != null) {
                pathExtend_Tail.path!!.transform(m)
                arbitraryPolygonShape.appendPath(pathExtend_Tail)
            }
        }
    }

    private fun getArrowPath(
        arbitraryPolygonShape: ArbitraryPolygonShape,
        pathElement: Element,
        rect: Rectangle,
        fill: BackgroundAndFill?,
        border: Boolean,
        headerArrowTailCenter: PointF?,
        tailArrowTailCenter: PointF?
    ): Path {
        var headerArrowTailCenter = headerArrowTailCenter
        var tailArrowTailCenter = tailArrowTailCenter
        val path = Path()
        var pathClosed = false
        val eleList = pathElement.elements() as MutableList<Element?>
        val cnt = eleList.size
        var e = (eleList.get(cnt - 1) as Element)

        for (i in 0..<cnt) {
            e = eleList.get(i) as Element
            if (headerArrowTailCenter != null && i == 0 && e.getName() == "moveTo") {
                //header arrow
                pathClosed = false
                headerArrowTailCenter = LineArrowPathBuilder.getReferencedPosition(
                    e.element("pt"),
                    headerArrowTailCenter,
                    arbitraryPolygonShape.getStartArrowType()
                )

                path.moveTo(headerArrowTailCenter.x, headerArrowTailCenter.y)
            } else if (tailArrowTailCenter != null && i == cnt - 1) {
                //tail arrow
                if (e.getName() == "lnTo") {
                    tailArrowTailCenter = LineArrowPathBuilder.getReferencedPosition(
                        e.element("pt"),
                        tailArrowTailCenter,
                        arbitraryPolygonShape.getEndArrowType()
                    )
                    path.lineTo(tailArrowTailCenter.x, tailArrowTailCenter.y)
                } else if (e.getName() == "quadBezTo") {
                    val ptList = e.elements()
                    if (ptList.size != 2) {
                        break
                    }

                    tailArrowTailCenter = LineArrowPathBuilder.getReferencedPosition(
                        ptList.get(1) as Element,
                        tailArrowTailCenter,
                        arbitraryPolygonShape.getEndArrowType()
                    )

                    path.quadTo(
                        (ptList.get(0) as Element).attributeValue("x")
                            .toInt() * MainConstant.PIXEL_DPI / MainConstant.EMU_PER_INCH,
                        (ptList.get(0) as Element).attributeValue("y")
                            .toInt() * MainConstant.PIXEL_DPI / MainConstant.EMU_PER_INCH,
                        tailArrowTailCenter.x,
                        tailArrowTailCenter.y
                    )
                } else if (e.getName() == "cubicBezTo") {
                    val ptList = e.elements()
                    if (ptList.size != 3) {
                        break
                    }

                    tailArrowTailCenter = LineArrowPathBuilder.getReferencedPosition(
                        ptList.get(2) as Element,
                        tailArrowTailCenter,
                        arbitraryPolygonShape.getEndArrowType()
                    )

                    path.cubicTo(
                        (ptList.get(0) as Element).attributeValue("x")
                            .toInt() * MainConstant.PIXEL_DPI / MainConstant.EMU_PER_INCH,
                        (ptList.get(0) as Element).attributeValue("y")
                            .toInt() * MainConstant.PIXEL_DPI / MainConstant.EMU_PER_INCH,
                        (ptList.get(1) as Element).attributeValue("x")
                            .toInt() * MainConstant.PIXEL_DPI / MainConstant.EMU_PER_INCH,
                        (ptList.get(1) as Element).attributeValue("y")
                            .toInt() * MainConstant.PIXEL_DPI / MainConstant.EMU_PER_INCH,
                        tailArrowTailCenter.x,
                        tailArrowTailCenter.y
                    )
                } else if (e.getName() == "arcTo") {
                    val wR = e.attributeValue("wR")
                        .toInt() * MainConstant.PIXEL_DPI / MainConstant.EMU_PER_INCH
                    val hR = e.attributeValue("hR")
                        .toInt() * MainConstant.PIXEL_DPI / MainConstant.EMU_PER_INCH

                    path.arcTo(
                        RectF(
                            rect.getCenterX().toFloat() - wR - rect.x,
                            rect.getCenterY().toFloat() - hR - rect.y,
                            rect.getCenterX().toFloat() + wR - rect.x,
                            rect.getCenterY().toFloat() + hR - rect.y
                        ),
                        e.attributeValue("stAng").toInt() / 60000f,
                        e.attributeValue("swAng").toInt() / 60000f
                    )
                }
            } else {
                //no arrow
                if (e.getName() == "moveTo") {
                    pathClosed = false
                    e = e.element("pt")
                    path.moveTo(
                        e.attributeValue("x")
                            .toInt() * MainConstant.PIXEL_DPI / MainConstant.EMU_PER_INCH,
                        e.attributeValue("y")
                            .toInt() * MainConstant.PIXEL_DPI / MainConstant.EMU_PER_INCH
                    )
                } else if (e.getName() == "lnTo") {
                    e = e.element("pt")
                    path.lineTo(
                        e.attributeValue("x")
                            .toInt() * MainConstant.PIXEL_DPI / MainConstant.EMU_PER_INCH,
                        e.attributeValue("y")
                            .toInt() * MainConstant.PIXEL_DPI / MainConstant.EMU_PER_INCH
                    )
                } else if (e.getName() == "quadBezTo") {
                    val ptList = e.elements()
                    if (ptList.size != 2) {
                        break
                    }
                    path.quadTo(
                        (ptList.get(0) as Element).attributeValue("x")
                            .toInt() * MainConstant.PIXEL_DPI / MainConstant.EMU_PER_INCH,
                        (ptList.get(0) as Element).attributeValue("y")
                            .toInt() * MainConstant.PIXEL_DPI / MainConstant.EMU_PER_INCH,
                        (ptList.get(1) as Element).attributeValue("x")
                            .toInt() * MainConstant.PIXEL_DPI / MainConstant.EMU_PER_INCH,
                        (ptList.get(1) as Element).attributeValue("y")
                            .toInt() * MainConstant.PIXEL_DPI / MainConstant.EMU_PER_INCH
                    )
                } else if (e.getName() == "cubicBezTo") {
                    val ptList = e.elements()
                    if (ptList.size != 3) {
                        break
                    }
                    path.cubicTo(
                        (ptList.get(0) as Element).attributeValue("x")
                            .toInt() * MainConstant.PIXEL_DPI / MainConstant.EMU_PER_INCH,
                        (ptList.get(0) as Element).attributeValue("y")
                            .toInt() * MainConstant.PIXEL_DPI / MainConstant.EMU_PER_INCH,
                        (ptList.get(1) as Element).attributeValue("x")
                            .toInt() * MainConstant.PIXEL_DPI / MainConstant.EMU_PER_INCH,
                        (ptList.get(1) as Element).attributeValue("y")
                            .toInt() * MainConstant.PIXEL_DPI / MainConstant.EMU_PER_INCH,
                        (ptList.get(2) as Element).attributeValue("x")
                            .toInt() * MainConstant.PIXEL_DPI / MainConstant.EMU_PER_INCH,
                        (ptList.get(2) as Element).attributeValue("y")
                            .toInt() * MainConstant.PIXEL_DPI / MainConstant.EMU_PER_INCH
                    )
                } else if (e.getName() == "arcTo") {
                    val wR = e.attributeValue("wR")
                        .toInt() * MainConstant.PIXEL_DPI / MainConstant.EMU_PER_INCH
                    val hR = e.attributeValue("hR")
                        .toInt() * MainConstant.PIXEL_DPI / MainConstant.EMU_PER_INCH

                    path.arcTo(
                        RectF(
                            rect.getCenterX().toFloat() - wR - rect.x,
                            rect.getCenterY().toFloat() - hR - rect.y,
                            rect.getCenterX().toFloat() + wR - rect.x,
                            rect.getCenterY().toFloat() + hR - rect.y
                        ),
                        e.attributeValue("stAng").toInt() / 60000f,
                        e.attributeValue("swAng").toInt() / 60000f
                    )
                } else if (e.getName() == "close") {
                    pathClosed = true
                    path.close()
                }
            }
        }

        return path
    }

    /**
     * 
     * @param pathElement
     * @return
     */
    @JvmStatic
    fun getPathHeadArrowPath(
        arrow: Arrow,
        lineWidth: Int,
        pathElement: Element
    ): ArrowPathAndTail? {
        val eleList = pathElement.elements() as MutableList<Element?>?
        if (eleList == null || eleList.size < 2) {
            return null
        }

        var e = eleList.get(0)!!.element("pt") as Element

        var path: ArrowPathAndTail? = null
        val p0X = e.attributeValue("x").toInt() * MainConstant.PIXEL_DPI / MainConstant.EMU_PER_INCH
        val p0Y = e.attributeValue("y").toInt() * MainConstant.PIXEL_DPI / MainConstant.EMU_PER_INCH
        var p1X = 0f
        var p1Y = 0f

        e = eleList.get(1) as Element
        if (e.getName() == "lnTo") {
            e = e.element("pt")
            p1X = e.attributeValue("x").toInt() * MainConstant.PIXEL_DPI / MainConstant.EMU_PER_INCH
            p1Y = e.attributeValue("y").toInt() * MainConstant.PIXEL_DPI / MainConstant.EMU_PER_INCH

            path = LineArrowPathBuilder.getDirectLineArrowPath(p1X, p1Y, p0X, p0Y, arrow, lineWidth)
        } else if (e.getName() == "quadBezTo") {
            val ptList = e.elements()
            if (ptList.size == 2) {
                val ctrX1 = (ptList.get(0) as Element).attributeValue("x")
                    .toInt() * MainConstant.PIXEL_DPI / MainConstant.EMU_PER_INCH
                val ctrY1 = (ptList.get(0) as Element).attributeValue("y")
                    .toInt() * MainConstant.PIXEL_DPI / MainConstant.EMU_PER_INCH
                p1X = (ptList.get(1) as Element).attributeValue("x")
                    .toInt() * MainConstant.PIXEL_DPI / MainConstant.EMU_PER_INCH
                p1Y = (ptList.get(1) as Element).attributeValue("y")
                    .toInt() * MainConstant.PIXEL_DPI / MainConstant.EMU_PER_INCH

                path = LineArrowPathBuilder.getQuadBezArrowPath(
                    p1X,
                    p1Y,
                    ctrX1,
                    ctrY1,
                    p0X,
                    p0Y,
                    arrow,
                    lineWidth
                )
            }
        } else if (e.getName() == "cubicBezTo") {
            val ptList = e.elements()
            if (ptList.size == 3) {
                val ctrX1 = (ptList.get(0) as Element).attributeValue("x")
                    .toInt() * MainConstant.PIXEL_DPI / MainConstant.EMU_PER_INCH
                val ctrY1 = (ptList.get(0) as Element).attributeValue("y")
                    .toInt() * MainConstant.PIXEL_DPI / MainConstant.EMU_PER_INCH
                val ctrX2 = (ptList.get(1) as Element).attributeValue("x")
                    .toInt() * MainConstant.PIXEL_DPI / MainConstant.EMU_PER_INCH
                val ctrY2 = (ptList.get(1) as Element).attributeValue("y")
                    .toInt() * MainConstant.PIXEL_DPI / MainConstant.EMU_PER_INCH
                p1X = (ptList.get(2) as Element).attributeValue("x")
                    .toInt() * MainConstant.PIXEL_DPI / MainConstant.EMU_PER_INCH
                p1Y = (ptList.get(2) as Element).attributeValue("y")
                    .toInt() * MainConstant.PIXEL_DPI / MainConstant.EMU_PER_INCH

                path = LineArrowPathBuilder.getCubicBezArrowPath(
                    p1X,
                    p1Y,
                    ctrX2,
                    ctrY2,
                    ctrX1,
                    ctrY1,
                    p0X,
                    p0Y,
                    arrow,
                    lineWidth
                )
            }
        }

        return path
    }

    @JvmStatic
    fun getPathTailArrowPath(
        arrow: Arrow,
        lineWidth: Int,
        pathElement: Element
    ): ArrowPathAndTail? {
        val eleList = pathElement.elements() as MutableList<Element?>?
        var arrowPathAndTail: ArrowPathAndTail? = null

        var cnt = 0
        if (eleList == null || (eleList.size.also { cnt = it }) < 2 || eleList.get(cnt - 1)!!
                .getName() == "close"
        ) {
            return null
        }


        //find start point
        var e = eleList.get(cnt - 2) as Element
        var p0X = 0f
        var p0Y = 0f
        if (e.getName() == "lnTo") {
            e = e.element("pt")
            p0X = e.attributeValue("x").toInt() * MainConstant.PIXEL_DPI / MainConstant.EMU_PER_INCH
            p0Y = e.attributeValue("y").toInt() * MainConstant.PIXEL_DPI / MainConstant.EMU_PER_INCH
        } else if (e.getName() == "quadBezTo") {
            val ptList = e.elements()
            if (ptList.size == 2) {
                p0X = (ptList.get(1) as Element).attributeValue("x")
                    .toInt() * MainConstant.PIXEL_DPI / MainConstant.EMU_PER_INCH
                p0Y = (ptList.get(1) as Element).attributeValue("y")
                    .toInt() * MainConstant.PIXEL_DPI / MainConstant.EMU_PER_INCH
            }
        } else if (e.getName() == "cubicBezTo") {
            val ptList = e.elements()
            if (ptList.size == 3) {
                p0X = (ptList.get(2) as Element).attributeValue("x")
                    .toInt() * MainConstant.PIXEL_DPI / MainConstant.EMU_PER_INCH
                p0Y = (ptList.get(2) as Element).attributeValue("y")
                    .toInt() * MainConstant.PIXEL_DPI / MainConstant.EMU_PER_INCH
            }
        }


        //next point(s)
        e = eleList.get(cnt - 1) as Element
        var p1X = 0f
        var p1Y = 0f
        if (e.getName() == "lnTo") {
            e = e.element("pt")
            p1X = e.attributeValue("x").toInt() * MainConstant.PIXEL_DPI / MainConstant.EMU_PER_INCH
            p1Y = e.attributeValue("y").toInt() * MainConstant.PIXEL_DPI / MainConstant.EMU_PER_INCH

            arrowPathAndTail =
                LineArrowPathBuilder.getDirectLineArrowPath(p0X, p0Y, p1X, p1Y, arrow, lineWidth)
        } else if (e.getName() == "quadBezTo") {
            val ptList = e.elements()
            if (ptList.size == 2) {
                val ctrX1 = (ptList.get(0) as Element).attributeValue("x")
                    .toInt() * MainConstant.PIXEL_DPI / MainConstant.EMU_PER_INCH
                val ctrY1 = (ptList.get(0) as Element).attributeValue("y")
                    .toInt() * MainConstant.PIXEL_DPI / MainConstant.EMU_PER_INCH
                p1X = (ptList.get(1) as Element).attributeValue("x")
                    .toInt() * MainConstant.PIXEL_DPI / MainConstant.EMU_PER_INCH
                p1Y = (ptList.get(1) as Element).attributeValue("y")
                    .toInt() * MainConstant.PIXEL_DPI / MainConstant.EMU_PER_INCH

                arrowPathAndTail = LineArrowPathBuilder.getQuadBezArrowPath(
                    p0X,
                    p0Y,
                    ctrX1,
                    ctrY1,
                    p1X,
                    p1Y,
                    arrow,
                    lineWidth
                )
            }
        } else if (e.getName() == "cubicBezTo") {
            val ptList = e.elements()
            if (ptList.size == 3) {
                val ctrX1 = (ptList.get(0) as Element).attributeValue("x")
                    .toInt() * MainConstant.PIXEL_DPI / MainConstant.EMU_PER_INCH
                val ctrY1 = (ptList.get(0) as Element).attributeValue("y")
                    .toInt() * MainConstant.PIXEL_DPI / MainConstant.EMU_PER_INCH
                val ctrX2 = (ptList.get(1) as Element).attributeValue("x")
                    .toInt() * MainConstant.PIXEL_DPI / MainConstant.EMU_PER_INCH
                val ctrY2 = (ptList.get(1) as Element).attributeValue("y")
                    .toInt() * MainConstant.PIXEL_DPI / MainConstant.EMU_PER_INCH
                p1X = (ptList.get(2) as Element).attributeValue("x")
                    .toInt() * MainConstant.PIXEL_DPI / MainConstant.EMU_PER_INCH
                p1Y = (ptList.get(2) as Element).attributeValue("y")
                    .toInt() * MainConstant.PIXEL_DPI / MainConstant.EMU_PER_INCH

                arrowPathAndTail = LineArrowPathBuilder.getCubicBezArrowPath(
                    p0X,
                    p0Y,
                    ctrX1,
                    ctrY1,
                    ctrX2,
                    ctrY2,
                    p1X,
                    p1Y,
                    arrow,
                    lineWidth
                )
            }
        }
        return arrowPathAndTail
    }

    @JvmStatic
    fun getArrowSize(size: String?): Int {
        if (size == null || size == "med") {
            return 1
        }
        if (size == "sm") {
            return 0
        } else if (size == "lg") {
            return 2
        } else {
            return 1
        }
    }
}
