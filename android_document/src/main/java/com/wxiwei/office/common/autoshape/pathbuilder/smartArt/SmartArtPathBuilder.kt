/*
 * 文件名称:          FunnelPathBuilder.java
 *  
 * 编译器:            android2.2
 * 时间:              上午9:28:01
 */
package com.wxiwei.office.common.autoshape.pathbuilder.smartArt

import android.graphics.Matrix
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import com.wxiwei.office.common.shape.AutoShape
import com.wxiwei.office.common.shape.ShapeTypes
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

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
 * 日期:            2013-5-10
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
object SmartArtPathBuilder {
    private val TODEGREE = 36000000f / 21600000f

    private val sm = Matrix()
    private val s_rect = RectF()
    private val path = Path()

    /**
     * get star path
     * @param shape
     * @param rect
     * @return
     */
    @JvmStatic
    fun getStarPath(shape: AutoShape, rect: Rect): Path? {
        path.reset()
        when (shape.getShapeType()) {
            ShapeTypes.Funnel -> return getFunnelPath(shape, rect)
            ShapeTypes.Gear6 -> return getGear6Path(shape, rect)

            ShapeTypes.Gear9 -> return getGear9Path(shape, rect)
            ShapeTypes.LeftCircularArrow -> return getLeftCircularArrowPath(shape, rect)
            ShapeTypes.PieWedge -> return getPieWedgePath(shape, rect)
            ShapeTypes.SwooshArrow -> return getSwooshArrowPath(shape, rect)
        }

        return null
    }

    private fun getFunnelPath(shape: AutoShape?, rect: Rect): Path {
        val width = 716f
        val height = 536f
        path.addOval(RectF(28f, 22f, 688f, 238f), Path.Direction.CCW)

        path.moveTo(0f, 130f)
        path.arcTo(RectF(0f, 0f, 716f, 260f), 180f, 180f)
        path.arcTo(RectF(258f, 444f, 458f, 536f), 30f, 150f)
        path.close()

        sm.reset()
        sm.postScale(rect.width() / width, rect.height() / height)
        path.transform(sm)

        path.offset(rect.left.toFloat(), rect.top.toFloat())

        return path
    }

    private fun getGear6Path(shape: AutoShape?, rect: Rect): Path {
        val len = 6858000f

        path.moveTo(5131482f, 1736961f)
        path.lineTo(6143269f, 1432030f)
        path.lineTo(6515568f, 2076873f)
        path.lineTo(5745593f, 2800638f)
        path.cubicTo(5857203f, 3212114f, 5857203f, 3645892f, 5745592f, 4057368f)

        path.lineTo(6515568f, 4781127f)
        path.lineTo(6143269f, 5425970f)
        path.lineTo(5131482f, 5121039f)
        path.cubicTo(4830937f, 5423437f, 4455271f, 5640328f, 4043114f, 5749407f)

        path.lineTo(3801303f, 6778110f)
        path.lineTo(3056697f, 6778110f)
        path.lineTo(2814884f, 5749410f)
        path.cubicTo(2402727f, 5640330f, 2027062f, 5423438f, 1726518f, 5121040f)

        path.lineTo(714731f, 5425970f)
        path.lineTo(342432f, 4781127f)
        path.lineTo(1112407f, 4057362f)
        path.cubicTo(1000796f, 3645886f, 1000796f, 3212108f, 1112407f, 2800632f)

        path.lineTo(342432f, 2076873f)
        path.lineTo(714731f, 1432030f)
        path.lineTo(1726518f, 1736961f)
        path.cubicTo(2027063f, 1434563f, 2402729f, 1217673f, 2814886f, 1108594f)

        path.lineTo(3056697f, 79890f)
        path.lineTo(3801303f, 79890f)
        path.lineTo(4043116f, 1108590f)
        path.cubicTo(4455273f, 1217671f, 4830938f, 1434562f, 5131482f, 1736961f)

        path.close()

        sm.reset()
        sm.postScale(rect.width() / len, rect.height() / len)
        path.transform(sm)

        path.offset(rect.left.toFloat(), rect.top.toFloat())

        return path
    }

    private fun getGear9Path(shape: AutoShape?, rect: Rect): Path {
        val len = 5715040f

        path.moveTo(4056564f, 911200f)
        path.lineTo(4501105f, 538168f)
        path.lineTo(4856239f, 836163f)
        path.lineTo(4566066f, 1338725f)
        path.cubicTo(4772395f, 1570831f, 4929267f, 1842544f, 5027111f, 2137283f)

        path.lineTo(5607429f, 2137269f)
        path.lineTo(5687931f, 2593823f)
        path.lineTo(5142605f, 2792288f)
        path.cubicTo(5151467f, 3102716f, 5096985f, 3411694f, 4982485f, 3700369f)

        path.lineTo(5427044f, 4073378f)
        path.lineTo(5195245f, 4474864f)
        path.lineTo(4649930f, 4276370f)
        path.cubicTo(4457179f, 4519870f, 4216835f, 4721542f, 3943563f, 4869081f)

        path.lineTo(4044350f, 5440580f)
        path.lineTo(3608711f, 5599139f)
        path.lineTo(3318566f, 5096561f)
        path.cubicTo(3014392f, 5159194f, 2700646f, 5159194f, 2396472f, 5096561f)

        path.lineTo(2106329f, 5599139f)
        path.lineTo(1670690f, 5440580f)
        path.lineTo(1771476f, 4869081f)
        path.cubicTo(1498205f, 4721541f, 1257861f, 4519869f, 1065110f, 4276369f)

        path.lineTo(519795f, 4474864f)
        path.lineTo(287996f, 4073378f)
        path.lineTo(732555f, 3700369f)
        path.cubicTo(618055f, 3411694f, 563574f, 3102715f, 572436f, 2792288f)

        path.lineTo(27109f, 2593823f)
        path.lineTo(107611f, 2137269f)
        path.lineTo(687928f, 2137283f)
        path.cubicTo(785773f, 1842544f, 942647f, 1570832f, 1148976f, 1338726f)

        path.lineTo(858801f, 836163f)
        path.lineTo(1213935f, 538168f)
        path.lineTo(1658476f, 911200f)
        path.cubicTo(1922884f, 748311f, 2217710f, 641003f, 2524962f, 595826f)

        path.lineTo(2625719f, 24319f)
        path.lineTo(3089321f, 24319f)
        path.lineTo(3190077f, 595823f)
        path.cubicTo(3497329f, 641001f, 3792154f, 748309f, 4056562f, 911199f)

        path.cubicTo(4056563f, 911199f, 4056563f, 911200f, 4056564f, 911200f)

        path.close()

        sm.reset()
        sm.postScale(rect.width() / len, rect.height() / len)
        path.transform(sm)

        path.offset(rect.left.toFloat(), rect.top.toFloat())

        return path
    }

    private fun getLeftCircularArrowPath(shape: AutoShape, rect: Rect): Path {
        shape.setFlipVertical(true)

        val values = shape.getAdjustData()

        var adj1 = 0
        var adj2 = 0
        var adj3 = 0
        var adj4 = 0
        var adj5 = 0
        val len = 100
        if (values != null && values.size == 5) {
            adj1 = Math.round(len * values[0]!!)
            adj2 = Math.round(values[1]!! * TODEGREE)
            adj3 = Math.round(values[2]!! * TODEGREE)
            adj4 = -Math.round(values[3]!! * TODEGREE)
            adj5 = Math.round(len * values[4]!!)
        } else {
            adj1 = Math.round(len * 0.125f)
            adj2 = 20
            adj3 = 340
            adj4 = -180
            adj5 = Math.round(len * 0.125f)
        }


        //radius of circal between outer and inner
        val insideRadius = len / 2 - adj5


        //outer arc line
        //path.moveTo((insideRadius + adj1 / 2) * (float)Math.cos(adj4 *  Math.PI / 180f), (insideRadius + adj1 / 2) * (float)Math.sin(adj4 *  Math.PI / 180f));

        //point of the arrow tail line
        val y = insideRadius * sin(adj3 * Math.PI / 180f)
        val x = insideRadius * cos(adj3 * Math.PI / 180f)


        //arrow tail line  y = kx + b  
        val k = tan((adj3 + adj2) * Math.PI / 180f)
        val b = y - k * x


        //The distance between arrow tail center and tail endpoint
        var offX1 = sqrt(adj5.toDouble().pow(2.0) / (k.pow(2.0) + 1))
        //The distance between arrow tail center and intersetion of arrow tail and circle
        var offX2 = sqrt((adj1 / 2).toDouble().pow(2.0) / (k.pow(2.0) + 1))

        if (adj3 > 90 && adj3 < 270) {
            offX1 = -offX1
            offX2 = -offX2
        }

        val outerDegree = getAngle(x + offX2, k * (x + offX2) + b)
        val innerDegree = getAngle(x - offX2, k * (x - offX2) + b)


        s_rect.set(
            (adj5 - adj1 / 2 - len / 2).toFloat(),
            (adj5 - adj1 / 2 - len / 2).toFloat(),
            (len / 2 - adj5 + adj1 / 2).toFloat(),
            (len / 2 - adj5 + adj1 / 2).toFloat()
        )
        path.arcTo(s_rect, adj4.toFloat(), (outerDegree - adj4 + 360).toFloat() % 360)

        path.lineTo((x + offX1).toFloat(), (k * (x + offX1) + b).toFloat())
        path.lineTo(
            (insideRadius * cos((adj3 + adj2) * Math.PI / 180f)).toFloat(),
            (insideRadius * sin((adj3 + adj2) * Math.PI / 180f)).toFloat()
        )
        path.lineTo((x - offX1).toFloat(), (k * (x - offX1) + b).toFloat())

        s_rect.set(
            (adj5 + adj1 / 2 - len / 2).toFloat(),
            (adj5 + adj1 / 2 - len / 2).toFloat(),
            (len / 2 - adj5 - adj1 / 2).toFloat(),
            (len / 2 - adj5 - adj1 / 2).toFloat()
        )
        path.arcTo(s_rect, innerDegree.toFloat(), (adj4 - innerDegree - 360).toFloat() % 360)

        path.close()

        val m = Matrix()
        m.postScale(rect.width() / 100f, rect.height() / 100f)
        path.transform(m)

        path.offset(rect.centerX().toFloat(), rect.centerY().toFloat())

        return path
    }

    private fun getAngle(x: Double, y: Double): Double {
        var angle = acos(x / sqrt(x * x + y * y)) * 180 / Math.PI

        if (y < 0) {
            angle = 360 - angle
        }

        return angle
    }

    private fun getPieWedgePath(shape: AutoShape?, rect: Rect): Path {
        path.moveTo(rect.right.toFloat(), rect.bottom.toFloat())
        path.lineTo(rect.left.toFloat(), rect.bottom.toFloat())
        path.arcTo(
            RectF(
                rect.left.toFloat(),
                rect.top.toFloat(),
                (rect.left + rect.width() * 2).toFloat(),
                (rect.top + rect.height() * 2).toFloat()
            ), 180f, 90f
        )
        path.close()
        return path
    }

    private fun getSwooshArrowPath(shape: AutoShape?, rect: Rect): Path {
        val len = 3600000f
        path.moveTo(0f, 3600000f)
        path.cubicTo(400000f, 2000000f, 1300000f, 950000f, 2700000f, 450000f)
        path.lineTo(2649297f, 0f)
        path.lineTo(3600000f, 720000f)
        path.lineTo(2852109f, 1800000f)
        path.lineTo(2801406f, 1350000f)
        path.cubicTo(1533802f, 1550000f, 600000f, 2300000f, 0f, 3600000f)
        path.close()

        sm.reset()
        sm.postScale(rect.width() / len, rect.height() / len)
        path.transform(sm)

        path.offset(rect.left.toFloat(), rect.top.toFloat())

        return path
    }
}
