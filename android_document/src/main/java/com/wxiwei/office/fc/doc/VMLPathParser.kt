/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
package com.wxiwei.office.fc.doc

import android.graphics.Path
import android.graphics.PointF
import com.wxiwei.office.common.autoshape.pathbuilder.ArrowPathAndTail
import com.wxiwei.office.common.autoshape.pathbuilder.LineArrowPathBuilder
import com.wxiwei.office.common.shape.WPAutoShape

class VMLPathParser private constructor() {
    companion object {
        const val Command_Invalid: Byte = -1
        const val Command_MoveTo: Byte = 0
        const val Command_LineTo: Byte = 1
        const val Command_CurveTo: Byte = 2
        const val Command_Close: Byte = 3
        const val Command_End: Byte = 4
        const val Command_RMoveTo: Byte = 5
        const val Command_RLineTo: Byte = 6
        const val Command_RCurveTo: Byte = 7
        const val Command_NoFill: Byte = 8
        const val Command_NoStroke: Byte = 9
        const val Command_AngleEllipseTo: Byte = 10
        const val Command_AngleEllipse: Byte = 11
        const val Command_ArcTo: Byte = 12
        const val Command_Arc: Byte = 13
        const val Command_ClockwiseArcTo: Byte = 14
        const val Command_ClockwiseArc: Byte = 15
        private const val Command_EllipticalQaudrantX: Byte = 16
        private const val Command_EllipticalQaudrantY: Byte = 17
        private const val Command_QuadraticBezier: Byte = 18
        private val instance = VMLPathParser()

        @JvmStatic
        fun instance(): VMLPathParser {
            return instance
        }
    }

    private val nodeTypeInvalidate: Byte = -1
    private val nodeTypeStart: Byte = 0
    private val nodeTypeMiddle: Byte = 1
    private val nodeTypeEnd: Byte = 2
    private var currentNodeType = nodeTypeInvalidate
    private var preNodeType = nodeTypeInvalidate
    private val preNode = PointF()
    private val ctrNode1 = PointF()
    private val ctrNode2 = PointF()
    private val nextNode = PointF()
    private var startArrowPath: Path? = null
    private var endArrowPath: Path? = null
    private var index = 0
    private val builder = StringBuilder()
    private val paraList = ArrayList<Int>()

    fun createPath(autoshape: WPAutoShape?, pathContext: String, lineWidth: Int): PathWithArrow? {
        return try {
            index = 0
            startArrowPath = null
            endArrowPath = null
            val pathList = ArrayList<Path>()
            var path: Path? = null
            var newPath = true
            var command = nextCommand(pathContext)
            var nextCommand = command
            currentNodeType = nodeTypeStart
            preNodeType = nodeTypeInvalidate
            while (command != Command_Invalid) {
                if (command == Command_End) {
                    newPath = true
                    nextCommand = nextCommand(pathContext)
                    if (nextCommand == Command_Invalid) {
                        currentNodeType = nodeTypeEnd
                    }
                } else {
                    if (newPath) {
                        newPath = false
                        path = Path()
                        pathList.add(path)
                    }
                    val parameters = nextParameters(pathContext)
                    nextCommand = nextCommand(pathContext)
                    if (nextCommand == Command_Invalid || nextCommand == Command_End) {
                        currentNodeType = nodeTypeEnd
                    }
                    processPath(autoshape, lineWidth, path!!, command, parameters)
                    preNodeType = currentNodeType
                    currentNodeType = nodeTypeMiddle
                }
                command = nextCommand
            }
            PathWithArrow(pathList.toTypedArray(), startArrowPath, endArrowPath)
        } catch (_: Exception) {
            null
        }
    }

    private fun nextCommand(pathContext: String): Byte {
        builder.setLength(0)
        while (index < pathContext.length && pathContext[index].isLetter()) {
            builder.append(pathContext[index++])
        }
        var command = builder.toString()
        if (command.contains("h")) {
            command = command.substring(2)
        }
        return when {
            command.equals("m", true) -> Command_MoveTo
            command.equals("l", true) -> Command_LineTo
            command.equals("c", true) -> Command_CurveTo
            command.equals("x", true) -> Command_Close
            command.equals("e", true) -> Command_End
            command.equals("t", true) -> Command_RMoveTo
            command.equals("r", true) -> Command_RLineTo
            command.equals("v", true) -> Command_RCurveTo
            command.equals("nf", true) -> Command_NoFill
            command.equals("ns", true) -> Command_NoStroke
            command.equals("ae", true) -> Command_AngleEllipseTo
            command.equals("al", true) -> Command_AngleEllipse
            command.equals("at", true) -> Command_ArcTo
            command.equals("ar", true) -> Command_Arc
            command.equals("wa", true) -> Command_ClockwiseArcTo
            command.equals("wr", true) -> Command_ClockwiseArc
            command.equals("qx", true) -> Command_EllipticalQaudrantX
            command.equals("qy", true) -> Command_EllipticalQaudrantY
            command.equals("qb", true) -> Command_QuadraticBezier
            command.contains("x", true) -> {
                index -= command.length - 1
                Command_Close
            }
            else -> Command_Invalid
        }
    }

    private fun nextParameters(pathContext: String): Array<Int> {
        paraList.clear()
        while (hasNextPoint(pathContext)) {
            val point = nextPoint(pathContext)
            paraList.add(point[0])
            paraList.add(point[1])
        }
        return paraList.toTypedArray()
    }

    private fun hasNextPoint(pathContext: String): Boolean {
        return index < pathContext.length && !pathContext[index].isLetter()
    }

    private fun nextPoint(pathContext: String): IntArray {
        val point = IntArray(2)
        builder.setLength(0)
        while (index < pathContext.length && (pathContext[index].isDigit() || pathContext[index] == '-')) {
            builder.append(pathContext[index++])
        }
        if (builder.isNotEmpty()) {
            point[0] = builder.toString().toInt()
        }
        if (index < pathContext.length && pathContext[index] == ',') {
            index++
            builder.setLength(0)
            while (index < pathContext.length && (pathContext[index].isDigit() || pathContext[index] == '-')) {
                builder.append(pathContext[index++])
            }
            if (builder.isNotEmpty()) {
                point[1] = builder.toString().toInt()
            }
            if (index < pathContext.length && pathContext[index] == ',') {
                index++
            }
        }
        return point
    }

    private fun processPath(autoshape: WPAutoShape?, lineWidth: Int, path: Path, command: Byte, parameters: Array<Int>) {
        var start: ArrowPathAndTail? = null
        var end: ArrowPathAndTail? = null
        if (preNodeType == nodeTypeStart && autoshape != null && autoshape.startArrowhead) {
            start = arrow(autoshape, lineWidth, command, parameters, true)
        }
        if (currentNodeType == nodeTypeEnd && autoshape != null && autoshape.endArrowhead) {
            end = arrow(autoshape, lineWidth, command, parameters, false)
        }
        val startTail = start?.arrowTailCenter
        if (start != null && startTail != null) {
            startArrowPath = start.arrowPath
            path.reset()
            val position = LineArrowPathBuilder.getReferencedPosition(nextNode.x, nextNode.y, startTail.x, startTail.y, autoshape!!.startArrowType)
            path.moveTo(position.x, position.y)
        }
        val endTail = end?.arrowTailCenter
        if (end != null && endTail != null) {
            endArrowPath = end.arrowPath
            val count = parameters.size
            val position = LineArrowPathBuilder.getReferencedPosition(parameters[count - 2].toFloat(), parameters[count - 1].toFloat(), endTail.x, endTail.y, autoshape!!.endArrowType)
            parameters[count - 2] = position.x.toInt()
            parameters[count - 1] = position.y.toInt()
        }
        when (command) {
            Command_MoveTo -> moveTo(path, parameters)
            Command_LineTo -> lineTo(path, parameters)
            Command_CurveTo -> curveTo(path, parameters)
            Command_RMoveTo -> relativeMoveTo(path, parameters)
            Command_RLineTo -> relativeLineTo(path, parameters)
            Command_RCurveTo -> relativeCurveTo(path, parameters)
            Command_Close -> path.close()
        }
    }

    private fun arrow(shape: WPAutoShape, width: Int, command: Byte, p: Array<Int>, start: Boolean): ArrowPathAndTail? {
        val arrow = (if (start) shape.startArrow else shape.endArrow) ?: return null
        val count = p.size
        return when (command) {
            Command_LineTo -> if (start) direct(p[0].toFloat(), p[1].toFloat(), nextNode.x, nextNode.y, arrow, width) else direct(if (count > 2) p[count - 4].toFloat() else nextNode.x, if (count > 2) p[count - 3].toFloat() else nextNode.y, p[count - 2].toFloat(), p[count - 1].toFloat(), arrow, width)
            Command_CurveTo -> if (start) cubic(p[4].toFloat(), p[5].toFloat(), p[2].toFloat(), p[3].toFloat(), p[0].toFloat(), p[1].toFloat(), nextNode.x, nextNode.y, arrow, width) else cubic(nextNode.x, nextNode.y, p[count - 6].toFloat(), p[count - 5].toFloat(), p[count - 4].toFloat(), p[count - 3].toFloat(), p[count - 2].toFloat(), p[count - 1].toFloat(), arrow, width)
            Command_RLineTo -> if (start) direct(p[0] + nextNode.x, p[1] + nextNode.y, nextNode.x, nextNode.y, arrow, width) else direct(nextNode.x, nextNode.y, p[count - 2] + nextNode.x, p[count - 1] + nextNode.y, arrow, width)
            Command_RCurveTo -> if (start) cubic(p[4] + nextNode.x, p[5] + nextNode.y, p[2] + nextNode.x, p[3] + nextNode.y, p[0] + nextNode.x, p[1] + nextNode.y, nextNode.x, nextNode.y, arrow, width) else cubic(nextNode.x, nextNode.y, p[count - 6] + nextNode.x, p[count - 5] + nextNode.y, p[count - 4] + nextNode.x, p[count - 3] + nextNode.y, p[count - 2] + nextNode.x, p[count - 1] + nextNode.y, arrow, width)
            else -> null
        }
    }

    private fun direct(x1: Float, y1: Float, x2: Float, y2: Float, arrow: com.wxiwei.office.common.shape.Arrow, width: Int): ArrowPathAndTail {
        return LineArrowPathBuilder.getDirectLineArrowPath(x1, y1, x2, y2, arrow, width)
    }

    private fun cubic(x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float, x4: Float, y4: Float, arrow: com.wxiwei.office.common.shape.Arrow, width: Int): ArrowPathAndTail {
        return LineArrowPathBuilder.getCubicBezArrowPath(x1, y1, x2, y2, x3, y3, x4, y4, arrow, width)
    }

    private fun moveTo(path: Path, p: Array<Int>) {
        val x = if (p.isNotEmpty()) p[0].toFloat() else 0f
        val y = if (p.size > 1) p[1].toFloat() else 0f
        path.moveTo(x, y)
        nextNode.set(x, y)
    }

    private fun lineTo(path: Path, p: Array<Int>) {
        var i = 0
        while (i < p.size - 1) {
            path.lineTo(p[i].toFloat(), p[i + 1].toFloat())
            preNode.set(nextNode)
            nextNode.set(p[i].toFloat(), p[i + 1].toFloat())
            i += 2
        }
    }

    private fun curveTo(path: Path, p: Array<Int>) {
        var i = 0
        while (i < p.size - 5) {
            path.cubicTo(p[i].toFloat(), p[i + 1].toFloat(), p[i + 2].toFloat(), p[i + 3].toFloat(), p[i + 4].toFloat(), p[i + 5].toFloat())
            preNode.set(nextNode)
            ctrNode1.set(p[i].toFloat(), p[i + 1].toFloat())
            ctrNode2.set(p[i + 2].toFloat(), p[i + 3].toFloat())
            nextNode.set(p[i + 4].toFloat(), p[i + 5].toFloat())
            i += 6
        }
    }

    private fun relativeMoveTo(path: Path, p: Array<Int>) {
        val x = if (p.isNotEmpty()) p[0] else 0
        val y = if (p.size > 1) p[1] else 0
        path.rMoveTo(x.toFloat(), y.toFloat())
        preNode.set(nextNode)
        nextNode.offset(x.toFloat(), y.toFloat())
    }

    private fun relativeLineTo(path: Path, p: Array<Int>) {
        var i = 0
        while (i < p.size - 1) {
            path.rLineTo(p[i].toFloat(), p[i + 1].toFloat())
            preNode.set(nextNode)
            nextNode.offset(p[i].toFloat(), p[i + 1].toFloat())
            i += 2
        }
    }

    private fun relativeCurveTo(path: Path, p: Array<Int>) {
        var i = 0
        while (i < p.size - 5) {
            path.rCubicTo(p[i].toFloat(), p[i + 1].toFloat(), p[i + 2].toFloat(), p[i + 3].toFloat(), p[i + 4].toFloat(), p[i + 5].toFloat())
            preNode.set(nextNode)
            ctrNode1.offset(p[i].toFloat(), p[i + 1].toFloat())
            ctrNode2.offset(p[i + 2].toFloat(), p[i + 3].toFloat())
            nextNode.offset(p[i + 4].toFloat(), p[i + 5].toFloat())
            i += 6
        }
    }
}
