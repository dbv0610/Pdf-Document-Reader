/*
 * 文件名称:           FlowChartDrawing.java
 *  
 * 编译器:             android2.2
 * 时间:               下午3:24:00
 */
package com.wxiwei.office.common.autoshape.pathbuilder.flowChart

import android.graphics.Canvas
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import com.wxiwei.office.common.autoshape.AutoShapeKit
import com.wxiwei.office.common.shape.AutoShape
import com.wxiwei.office.common.shape.ShapeTypes
import com.wxiwei.office.fc.ppt.reader.ReaderKit.Companion.instance
import com.wxiwei.office.ss.util.ColorUtil.Companion.instance
import com.wxiwei.office.system.IControl
import kotlin.math.min
import kotlin.math.sqrt

/**
 * draw flowChart
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
 * 日期:           2012-9-21
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
class FlowChartDrawing {
    /**
     * 
     * @param canvas
     * @param shape
     * @param rect
     */
    fun drawFlowChart(
        canvas: Canvas,
        control: IControl?,
        viewIndex: Int,
        shape: AutoShape,
        rect: Rect,
        zoom: Float
    ) {
        val type = shape.getShapeType()
        when (type) {
            ShapeTypes.FlowChartProcess -> drawFlowChartProcess(
                canvas,
                control,
                viewIndex,
                shape,
                rect,
                zoom
            )

            ShapeTypes.FlowChartAlternateProcess -> drawFlowChartAlternateProcess(
                canvas,
                control,
                viewIndex,
                shape,
                rect,
                zoom
            )

            ShapeTypes.FlowChartDecision -> drawFlowChartDecision(
                canvas,
                control,
                viewIndex,
                shape,
                rect,
                zoom
            )

            ShapeTypes.FlowChartInputOutput -> drawFlowChartInputOutput(
                canvas,
                control,
                viewIndex,
                shape,
                rect,
                zoom
            )

            ShapeTypes.FlowChartPredefinedProcess -> drawFlowChartPredefinedProcess(
                canvas,
                control,
                viewIndex,
                shape,
                rect,
                zoom
            )

            ShapeTypes.FlowChartInternalStorage -> drawFlowChartInternalStorage(
                canvas,
                control,
                viewIndex,
                shape,
                rect,
                zoom
            )

            ShapeTypes.FlowChartDocument -> drawFlowChartDocument(
                canvas,
                control,
                viewIndex,
                shape,
                rect,
                zoom
            )

            ShapeTypes.FlowChartMultidocument -> drawFlowChartMultidocument(
                canvas,
                control,
                viewIndex,
                shape,
                rect,
                zoom
            )

            ShapeTypes.FlowChartTerminator -> drawFlowChartTerminator(
                canvas,
                control,
                viewIndex,
                shape,
                rect,
                zoom
            )

            ShapeTypes.FlowChartPreparation -> drawFlowChartPreparation(
                canvas,
                control,
                viewIndex,
                shape,
                rect,
                zoom
            )

            ShapeTypes.FlowChartManualInput -> drawFlowChartManualInput(
                canvas,
                control,
                viewIndex,
                shape,
                rect,
                zoom
            )

            ShapeTypes.FlowChartManualOperation -> drawFlowChartManualOperation(
                canvas,
                control,
                viewIndex,
                shape,
                rect,
                zoom
            )

            ShapeTypes.FlowChartConnector -> drawFlowChartConnector(
                canvas,
                control,
                viewIndex,
                shape,
                rect,
                zoom
            )

            ShapeTypes.FlowChartOffpageConnector -> drawFlowChartOffpageConnector(
                canvas,
                control,
                viewIndex,
                shape,
                rect,
                zoom
            )

            ShapeTypes.FlowChartPunchedCard -> drawFlowChartPunchedCard(
                canvas,
                control,
                viewIndex,
                shape,
                rect,
                zoom
            )

            ShapeTypes.FlowChartPunchedTape -> drawFlowChartPunchedTape(
                canvas,
                control,
                viewIndex,
                shape,
                rect,
                zoom
            )

            ShapeTypes.FlowChartSummingJunction -> drawFlowChartSummingJunction(
                canvas,
                control,
                viewIndex,
                shape,
                rect,
                zoom
            )

            ShapeTypes.FlowChartOr -> drawFlowChartOr(canvas, control, viewIndex, shape, rect, zoom)
            ShapeTypes.FlowChartCollate -> drawFlowChartCollate(
                canvas,
                control,
                viewIndex,
                shape,
                rect,
                zoom
            )

            ShapeTypes.FlowChartSort -> drawFlowChartSort(
                canvas,
                control,
                viewIndex,
                shape,
                rect,
                zoom
            )

            ShapeTypes.FlowChartExtract -> drawFlowChartExtract(
                canvas,
                control,
                viewIndex,
                shape,
                rect,
                zoom
            )

            ShapeTypes.FlowChartMerge -> drawFlowChartMerge(
                canvas,
                control,
                viewIndex,
                shape,
                rect,
                zoom
            )

            ShapeTypes.FlowChartOnlineStorage -> drawFlowChartOnlineStorage(
                canvas,
                control,
                viewIndex,
                shape,
                rect,
                zoom
            )

            ShapeTypes.FlowChartDelay -> drawFlowChartDelay(
                canvas,
                control,
                viewIndex,
                shape,
                rect,
                zoom
            )

            ShapeTypes.FlowChartMagneticTape -> drawFlowChartMagneticTape(
                canvas,
                control,
                viewIndex,
                shape,
                rect,
                zoom
            )

            ShapeTypes.FlowChartMagneticDisk -> drawFlowChartMagneticDisk(
                canvas,
                control,
                viewIndex,
                shape,
                rect,
                zoom
            )

            ShapeTypes.FlowChartMagneticDrum -> drawFlowChartMagneticDrum(
                canvas,
                control,
                viewIndex,
                shape,
                rect,
                zoom
            )

            ShapeTypes.FlowChartDisplay -> drawFlowChartDisplay(
                canvas,
                control,
                viewIndex,
                shape,
                rect,
                zoom
            )

            else -> {}
        }
    }

    /**
     * 过程
     * @param canvas
     * @param shape
     * @param rect
     */
    private fun drawFlowChartProcess(
        canvas: Canvas,
        control: IControl?,
        viewIndex: Int,
        shape: AutoShape,
        rect: Rect,
        zoom: Float
    ) {
        path.reset()
        path.addRect(
            rect.left.toFloat(),
            rect.top.toFloat(),
            rect.right.toFloat(),
            rect.bottom.toFloat(),
            Path.Direction.CW
        )

        AutoShapeKit.Companion.instance()
            .drawShape(canvas, control, viewIndex, shape, path, rect, zoom)
    }

    /**
     * 可选过程
     * @param canvas
     * @param shape
     * @param rect
     */
    private fun drawFlowChartAlternateProcess(
        canvas: Canvas,
        control: IControl?,
        viewIndex: Int,
        shape: AutoShape,
        rect: Rect,
        zoom: Float
    ) {
        val x = min(rect.width(), rect.height()) * 0.18f

        path.reset()
        rectF.set(
            rect.left.toFloat(),
            rect.top.toFloat(),
            rect.right.toFloat(),
            rect.bottom.toFloat()
        )
        path.addRoundRect(rectF, floatArrayOf(x, x, x, x, x, x, x, x), Path.Direction.CW)

        AutoShapeKit.Companion.instance()
            .drawShape(canvas, control, viewIndex, shape, path, rect, zoom)
    }

    /**
     * 决策
     * @param canvas
     * @param shape
     * @param rect
     */
    private fun drawFlowChartDecision(
        canvas: Canvas,
        control: IControl?,
        viewIndex: Int,
        shape: AutoShape,
        rect: Rect,
        zoom: Float
    ) {
        path.reset()
        path.moveTo(rect.exactCenterX(), rect.top.toFloat())
        path.lineTo(rect.right.toFloat(), rect.exactCenterY())
        path.lineTo(rect.exactCenterX(), rect.bottom.toFloat())
        path.lineTo(rect.left.toFloat(), rect.exactCenterY())
        path.close()

        AutoShapeKit.Companion.instance()
            .drawShape(canvas, control, viewIndex, shape, path, rect, zoom)
    }


    /**
     * 数据
     * @param canvas
     * @param shape
     * @param rect
     */
    private fun drawFlowChartInputOutput(
        canvas: Canvas,
        control: IControl?,
        viewIndex: Int,
        shape: AutoShape,
        rect: Rect,
        zoom: Float
    ) {
        val x = rect.width() * 0.2f

        path.reset()
        path.moveTo(rect.left + x, rect.top.toFloat())
        path.lineTo(rect.right.toFloat(), rect.top.toFloat())
        path.lineTo(rect.right - x, rect.bottom.toFloat())
        path.lineTo(rect.left.toFloat(), rect.bottom.toFloat())
        path.close()

        AutoShapeKit.Companion.instance()
            .drawShape(canvas, control, viewIndex, shape, path, rect, zoom)
    }

    /**
     * 预定义过程
     * @param canvas
     * @param shape
     * @param rect
     */
    private fun drawFlowChartPredefinedProcess(
        canvas: Canvas,
        control: IControl?,
        viewIndex: Int,
        shape: AutoShape,
        rect: Rect,
        zoom: Float
    ) {
        val x = rect.width() * 0.125f

        path.reset()
        path.addRect(
            rect.left.toFloat(),
            rect.top.toFloat(),
            rect.right.toFloat(),
            rect.bottom.toFloat(),
            Path.Direction.CW
        )

        path.moveTo(rect.left + x, rect.top.toFloat())
        path.lineTo(rect.left + x, rect.bottom.toFloat())
        path.moveTo(rect.right - x, rect.top.toFloat())
        path.lineTo(rect.right - x, rect.bottom.toFloat())

        AutoShapeKit.Companion.instance()
            .drawShape(canvas, control, viewIndex, shape, path, rect, zoom)
    }

    /**
     * 内部储存
     * @param canvas
     * @param shape
     * @param rect
     */
    private fun drawFlowChartInternalStorage(
        canvas: Canvas,
        control: IControl?,
        viewIndex: Int,
        shape: AutoShape,
        rect: Rect,
        zoom: Float
    ) {
        val x = rect.width() * 0.125f
        val y = rect.height() * 0.125f

        path.reset()
        path.addRect(
            rect.left.toFloat(),
            rect.top.toFloat(),
            rect.right.toFloat(),
            rect.bottom.toFloat(),
            Path.Direction.CW
        )

        path.moveTo(rect.left + x, rect.top.toFloat())
        path.lineTo(rect.left + x, rect.bottom.toFloat())
        path.moveTo(rect.left.toFloat(), rect.top + y)
        path.lineTo(rect.right.toFloat(), rect.top + y)

        AutoShapeKit.Companion.instance()
            .drawShape(canvas, control, viewIndex, shape, path, rect, zoom)
    }

    /**
     * 文档
     * @param canvas
     * @param shape
     * @param rect
     */
    private fun drawFlowChartDocument(
        canvas: Canvas,
        control: IControl?,
        viewIndex: Int,
        shape: AutoShape,
        rect: Rect,
        zoom: Float
    ) {
        val x = rect.height() * 0.2f
        val y = rect.height() * 0.07f

        path.reset()
        path.moveTo(rect.left.toFloat(), rect.top.toFloat())
        path.lineTo(rect.right.toFloat(), rect.top.toFloat())
        rectF.set(
            rect.exactCenterX(), rect.bottom - x,
            rect.right + rect.width().toFloat() / 2, rect.bottom + x - y * 2
        )
        path.arcTo(rectF, 270f, -90f)
        rectF.set(
            rect.left.toFloat(),
            rect.bottom - y * 2,
            rect.exactCenterX(),
            rect.bottom.toFloat()
        )
        path.arcTo(rectF, 0f, 180f)
        path.close()

        AutoShapeKit.Companion.instance()
            .drawShape(canvas, control, viewIndex, shape, path, rect, zoom)
    }

    /**
     * 多文档
     * @param canvas
     * @param shape
     * @param rect
     */
    private fun drawFlowChartMultidocument(
        canvas: Canvas,
        control: IControl?,
        viewIndex: Int,
        shape: AutoShape,
        rect: Rect,
        zoom: Float
    ) {
        val x = (rect.width() * 0.137).toInt()
        val y = (rect.height() * 0.167).toInt()

        flowRect.set(rect.left + x, rect.top, rect.right, rect.bottom - y)
        drawFlowChartDocument(canvas, control, viewIndex, shape, flowRect, zoom)

        flowRect.set(rect.left + x / 2, rect.top + y / 2, rect.right - x / 2, rect.bottom - y / 2)
        drawFlowChartDocument(canvas, control, viewIndex, shape, flowRect, zoom)

        flowRect.set(rect.left, rect.top + y, rect.right - x, rect.bottom)
        drawFlowChartDocument(canvas, control, viewIndex, shape, flowRect, zoom)
    }

    /**
     * 终止
     * @param canvas
     * @param shape
     * @param rect
     */
    private fun drawFlowChartTerminator(
        canvas: Canvas,
        control: IControl?,
        viewIndex: Int,
        shape: AutoShape,
        rect: Rect,
        zoom: Float
    ) {
        val x = rect.width() * 0.16f
        val y = rect.height() * 0.5f

        path.reset()
        rectF.set(
            rect.left.toFloat(),
            rect.top.toFloat(),
            rect.right.toFloat(),
            rect.bottom.toFloat()
        )
        path.addRoundRect(rectF, floatArrayOf(x, y, x, y, x, y, x, y), Path.Direction.CW)

        AutoShapeKit.Companion.instance()
            .drawShape(canvas, control, viewIndex, shape, path, rect, zoom)
    }

    /**
     * 准备
     * @param canvas
     * @param shape
     * @param rect
     */
    private fun drawFlowChartPreparation(
        canvas: Canvas,
        control: IControl?,
        viewIndex: Int,
        shape: AutoShape,
        rect: Rect,
        zoom: Float
    ) {
        val x = rect.width() * 0.2f

        path.reset()
        path.moveTo(rect.left + x, rect.top.toFloat())
        path.lineTo(rect.right - x, rect.top.toFloat())
        path.lineTo(rect.right.toFloat(), rect.exactCenterY())
        path.lineTo(rect.right - x, rect.bottom.toFloat())
        path.lineTo(rect.left + x, rect.bottom.toFloat())
        path.lineTo(rect.left.toFloat(), rect.exactCenterY())
        path.close()

        AutoShapeKit.Companion.instance()
            .drawShape(canvas, control, viewIndex, shape, path, rect, zoom)
    }

    /**
     * 手动输入
     * @param canvas
     * @param shape
     * @param rect
     */
    private fun drawFlowChartManualInput(
        canvas: Canvas,
        control: IControl?,
        viewIndex: Int,
        shape: AutoShape,
        rect: Rect,
        zoom: Float
    ) {
        val x = rect.height() * 0.2f

        path.reset()
        path.moveTo(rect.left.toFloat(), rect.top + x)
        path.lineTo(rect.right.toFloat(), rect.top.toFloat())
        path.lineTo(rect.right.toFloat(), rect.bottom.toFloat())
        path.lineTo(rect.left.toFloat(), rect.bottom.toFloat())
        path.close()

        AutoShapeKit.Companion.instance()
            .drawShape(canvas, control, viewIndex, shape, path, rect, zoom)
    }

    /**
     * 手动操作
     * @param canvas
     * @param shape
     * @param rect
     */
    private fun drawFlowChartManualOperation(
        canvas: Canvas,
        control: IControl?,
        viewIndex: Int,
        shape: AutoShape,
        rect: Rect,
        zoom: Float
    ) {
        val x = rect.width() * 0.2f

        path.reset()
        path.moveTo(rect.left.toFloat(), rect.top.toFloat())
        path.lineTo(rect.right.toFloat(), rect.top.toFloat())
        path.lineTo(rect.right - x, rect.bottom.toFloat())
        path.lineTo(rect.left + x, rect.bottom.toFloat())
        path.close()

        AutoShapeKit.Companion.instance()
            .drawShape(canvas, control, viewIndex, shape, path, rect, zoom)
    }

    /**
     * 联系
     * @param canvas
     * @param shape
     * @param rect
     */
    private fun drawFlowChartConnector(
        canvas: Canvas,
        control: IControl?,
        viewIndex: Int,
        shape: AutoShape,
        rect: Rect,
        zoom: Float
    ) {
        path.reset()
        rectF.set(
            rect.left.toFloat(),
            rect.top.toFloat(),
            rect.right.toFloat(),
            rect.bottom.toFloat()
        )
        path.addOval(rectF, Path.Direction.CW)

        AutoShapeKit.Companion.instance()
            .drawShape(canvas, control, viewIndex, shape, path, rect, zoom)
    }

    /**
     * 离页连接符
     * @param canvas
     * @param shape
     * @param rect
     */
    private fun drawFlowChartOffpageConnector(
        canvas: Canvas,
        control: IControl?,
        viewIndex: Int,
        shape: AutoShape,
        rect: Rect,
        zoom: Float
    ) {
        val x = rect.height() * 0.2f

        path.reset()
        path.moveTo(rect.left.toFloat(), rect.top.toFloat())
        path.lineTo(rect.right.toFloat(), rect.top.toFloat())
        path.lineTo(rect.right.toFloat(), rect.bottom - x)
        path.lineTo(rect.exactCenterX(), rect.bottom.toFloat())
        path.lineTo(rect.left.toFloat(), rect.bottom - x)
        path.close()

        AutoShapeKit.Companion.instance()
            .drawShape(canvas, control, viewIndex, shape, path, rect, zoom)
    }

    /**
     * 卡片
     * @param canvas
     * @param shape
     * @param rect
     */
    private fun drawFlowChartPunchedCard(
        canvas: Canvas,
        control: IControl?,
        viewIndex: Int,
        shape: AutoShape,
        rect: Rect,
        zoom: Float
    ) {
        val x = rect.width() * 0.2f
        val y = rect.height() * 0.2f

        path.reset()
        path.moveTo(rect.left + x, rect.top.toFloat())
        path.lineTo(rect.right.toFloat(), rect.top.toFloat())
        path.lineTo(rect.right.toFloat(), rect.bottom.toFloat())
        path.lineTo(rect.left.toFloat(), rect.bottom.toFloat())
        path.lineTo(rect.left.toFloat(), rect.top + y)
        path.close()

        AutoShapeKit.Companion.instance()
            .drawShape(canvas, control, viewIndex, shape, path, rect, zoom)
    }

    /**
     * 资料带
     * @param canvas
     * @param shape
     * @param rect
     */
    private fun drawFlowChartPunchedTape(
        canvas: Canvas,
        control: IControl?,
        viewIndex: Int,
        shape: AutoShape,
        rect: Rect,
        zoom: Float
    ) {
        val x = rect.height() * 0.1f

        path.reset()
        rectF.set(rect.left.toFloat(), rect.top.toFloat(), rect.exactCenterX(), rect.top + x * 2)
        path.arcTo(rectF, 180f, -180f)
        rectF.set(rect.exactCenterX(), rect.top.toFloat(), rect.right.toFloat(), rect.top + x * 2)
        path.arcTo(rectF, 180f, 180f)
        rectF.set(
            rect.exactCenterX(),
            rect.bottom - x * 2,
            rect.right.toFloat(),
            rect.bottom.toFloat()
        )
        path.arcTo(rectF, 0f, -180f)
        rectF.set(
            rect.left.toFloat(),
            rect.bottom - x * 2,
            rect.exactCenterX(),
            rect.bottom.toFloat()
        )
        path.arcTo(rectF, 0f, 180f)
        path.close()

        AutoShapeKit.Companion.instance()
            .drawShape(canvas, control, viewIndex, shape, path, rect, zoom)
    }

    /**
     * 汇总连接
     * @param canvas
     * @param shape
     * @param rect
     */
    private fun drawFlowChartSummingJunction(
        canvas: Canvas,
        control: IControl?,
        viewIndex: Int,
        shape: AutoShape,
        rect: Rect,
        zoom: Float
    ) {
        val x = sqrt(2.0).toFloat() * rect.width() / 4
        val y = sqrt(2.0).toFloat() * rect.height() / 4
        val x0 = rect.exactCenterX()
        val y0 = rect.exactCenterY()

        path.reset()
        rectF.set(
            rect.left.toFloat(),
            rect.top.toFloat(),
            rect.right.toFloat(),
            rect.bottom.toFloat()
        )
        path.addOval(rectF, Path.Direction.CW)

        path.moveTo(x0 - x, y0 - y)
        path.lineTo(x0 + x, y0 + y)

        path.moveTo(x0 + x, y0 - y)
        path.lineTo(x0 - x, y0 + y)

        AutoShapeKit.Companion.instance()
            .drawShape(canvas, control, viewIndex, shape, path, rect, zoom)
    }

    /**
     * 或者
     * @param canvas
     * @param shape
     * @param rect
     */
    private fun drawFlowChartOr(
        canvas: Canvas,
        control: IControl?,
        viewIndex: Int,
        shape: AutoShape,
        rect: Rect,
        zoom: Float
    ) {
        path.reset()
        rectF.set(
            rect.left.toFloat(),
            rect.top.toFloat(),
            rect.right.toFloat(),
            rect.bottom.toFloat()
        )
        path.addOval(rectF, Path.Direction.CW)

        path.moveTo(rect.exactCenterX(), rect.top.toFloat())
        path.lineTo(rect.exactCenterX(), rect.bottom.toFloat())

        path.moveTo(rect.left.toFloat(), rect.exactCenterY())
        path.lineTo(rect.right.toFloat(), rect.exactCenterY())

        AutoShapeKit.Companion.instance()
            .drawShape(canvas, control, viewIndex, shape, path, rect, zoom)
    }

    /**
     * 对照
     * @param canvas
     * @param shape
     * @param rect
     */
    private fun drawFlowChartCollate(
        canvas: Canvas,
        control: IControl?,
        viewIndex: Int,
        shape: AutoShape,
        rect: Rect,
        zoom: Float
    ) {
        path.reset()
        path.moveTo(rect.left.toFloat(), rect.top.toFloat())
        path.lineTo(rect.right.toFloat(), rect.top.toFloat())
        path.lineTo(rect.exactCenterX(), rect.exactCenterY())
        path.lineTo(rect.right.toFloat(), rect.bottom.toFloat())
        path.lineTo(rect.left.toFloat(), rect.bottom.toFloat())
        path.lineTo(rect.exactCenterX(), rect.exactCenterY())
        path.close()

        AutoShapeKit.Companion.instance()
            .drawShape(canvas, control, viewIndex, shape, path, rect, zoom)
    }

    /**
     * 排序
     * @param canvas
     * @param shape
     * @param rect
     */
    private fun drawFlowChartSort(
        canvas: Canvas,
        control: IControl?,
        viewIndex: Int,
        shape: AutoShape,
        rect: Rect,
        zoom: Float
    ) {
        path.reset()
        path.moveTo(rect.exactCenterX(), rect.top.toFloat())
        path.lineTo(rect.right.toFloat(), rect.exactCenterY())
        path.lineTo(rect.exactCenterX(), rect.bottom.toFloat())
        path.lineTo(rect.left.toFloat(), rect.exactCenterY())
        path.close()

        path.moveTo(rect.left.toFloat(), rect.exactCenterY())
        path.lineTo(rect.right.toFloat(), rect.exactCenterY())

        AutoShapeKit.Companion.instance()
            .drawShape(canvas, control, viewIndex, shape, path, rect, zoom)
    }

    /**
     * 摘录
     * @param canvas
     * @param shape
     * @param rect
     */
    private fun drawFlowChartExtract(
        canvas: Canvas,
        control: IControl?,
        viewIndex: Int,
        shape: AutoShape,
        rect: Rect,
        zoom: Float
    ) {
        path.reset()
        path.moveTo(rect.exactCenterX(), rect.top.toFloat())
        path.lineTo(rect.right.toFloat(), rect.bottom.toFloat())
        path.lineTo(rect.left.toFloat(), rect.bottom.toFloat())
        path.close()

        AutoShapeKit.Companion.instance()
            .drawShape(canvas, control, viewIndex, shape, path, rect, zoom)
    }

    /**
     * 合并
     * @param canvas
     * @param shape
     * @param rect
     */
    private fun drawFlowChartMerge(
        canvas: Canvas,
        control: IControl?,
        viewIndex: Int,
        shape: AutoShape,
        rect: Rect,
        zoom: Float
    ) {
        path.reset()
        path.moveTo(rect.left.toFloat(), rect.top.toFloat())
        path.lineTo(rect.right.toFloat(), rect.top.toFloat())
        path.lineTo(rect.exactCenterX(), rect.bottom.toFloat())
        path.close()

        AutoShapeKit.Companion.instance()
            .drawShape(canvas, control, viewIndex, shape, path, rect, zoom)
    }

    /**
     * 库存数据
     * @param canvas
     * @param shape
     * @param rect
     */
    private fun drawFlowChartOnlineStorage(
        canvas: Canvas,
        control: IControl?,
        viewIndex: Int,
        shape: AutoShape,
        rect: Rect,
        zoom: Float
    ) {
        val x = rect.width() * 0.16f

        path.reset()
        rectF.set(rect.right - x, rect.top.toFloat(), rect.right + x, rect.bottom.toFloat())
        path.arcTo(rectF, 90f, 180f)
        rectF.set(rect.left.toFloat(), rect.top.toFloat(), rect.left + x * 2, rect.bottom.toFloat())
        path.arcTo(rectF, 270f, -180f)
        path.close()

        AutoShapeKit.Companion.instance()
            .drawShape(canvas, control, viewIndex, shape, path, rect, zoom)
    }

    /**
     * 延期
     * @param canvas
     * @param shape
     * @param rect
     */
    private fun drawFlowChartDelay(
        canvas: Canvas,
        control: IControl?,
        viewIndex: Int,
        shape: AutoShape,
        rect: Rect,
        zoom: Float
    ) {
        path.reset()
        path.moveTo(rect.left.toFloat(), rect.top.toFloat())
        rectF.set(
            rect.left.toFloat(),
            rect.top.toFloat(),
            rect.right.toFloat(),
            rect.bottom.toFloat()
        )
        path.arcTo(rectF, 270f, 180f)
        path.lineTo(rect.left.toFloat(), rect.bottom.toFloat())
        path.close()

        AutoShapeKit.Companion.instance()
            .drawShape(canvas, control, viewIndex, shape, path, rect, zoom)
    }

    /**
     * 顺序访问存储器
     * @param canvas
     * @param shape
     * @param rect
     */
    private fun drawFlowChartMagneticTape(
        canvas: Canvas,
        control: IControl?,
        viewIndex: Int,
        shape: AutoShape,
        rect: Rect,
        zoom: Float
    ) {
        val x = rect.width() * 0.15f
        val y = rect.height() * 0.15f

        path.reset()
        rectF.set(
            rect.left.toFloat(),
            rect.top.toFloat(),
            rect.right.toFloat(),
            rect.bottom.toFloat()
        )
        path.addOval(rectF, Path.Direction.CW)

        AutoShapeKit.Companion.instance()
            .drawShape(canvas, control, viewIndex, shape, path, rect, zoom)

        val border = shape.hasLine()
        shape.setLine(false)

        path.reset()
        path.moveTo(rect.exactCenterX(), rect.bottom - y)
        path.lineTo(rect.right.toFloat(), rect.bottom - y)
        path.lineTo(rect.right.toFloat(), rect.bottom.toFloat())
        path.moveTo(rect.exactCenterX(), rect.bottom.toFloat())
        path.close()

        AutoShapeKit.Companion.instance()
            .drawShape(canvas, control, viewIndex, shape, path, rect, zoom)
        shape.setLine(border)

        path.reset()
        path.moveTo(rect.right - x, rect.bottom - y)
        path.lineTo(rect.right.toFloat(), rect.bottom - y)
        path.lineTo(rect.right.toFloat(), rect.bottom.toFloat())
        path.lineTo(rect.exactCenterX(), rect.bottom.toFloat())

        AutoShapeKit.Companion.instance()
            .drawShape(canvas, control, viewIndex, shape, path, rect, zoom)
    }

    /**
     * 磁盘
     * @param canvas
     * @param shape
     * @param rect
     */
    private fun drawFlowChartMagneticDisk(
        canvas: Canvas,
        control: IControl?,
        viewIndex: Int,
        shape: AutoShape,
        rect: Rect,
        zoom: Float
    ) {
        val x = rect.height() * 0.32f

        path.reset()
        rectF.set(rect.left.toFloat(), rect.top.toFloat(), rect.right.toFloat(), rect.top + x)
        path.addOval(rectF, Path.Direction.CW)
        rectF.set(rect.left.toFloat(), rect.bottom - x, rect.right.toFloat(), rect.bottom.toFloat())
        path.arcTo(rectF, 0f, 180f)
        rectF.set(rect.left.toFloat(), rect.top.toFloat(), rect.right.toFloat(), rect.top + x)
        path.arcTo(rectF, 180f, -180f)
        path.close()

        AutoShapeKit.Companion.instance()
            .drawShape(canvas, control, viewIndex, shape, path, rect, zoom)
    }

    /**
     * 直接访问存储器
     * @param canvas
     * @param shape
     * @param rect
     */
    private fun drawFlowChartMagneticDrum(
        canvas: Canvas,
        control: IControl?,
        viewIndex: Int,
        shape: AutoShape,
        rect: Rect,
        zoom: Float
    ) {
        val x = rect.width() * 0.34f

        path.reset()
        rectF.set(rect.right - x, rect.top.toFloat(), rect.right.toFloat(), rect.bottom.toFloat())
        path.addOval(rectF, Path.Direction.CW)
        path.moveTo(rect.right - x / 2, rect.bottom.toFloat())
        rectF.set(rect.right - x, rect.top.toFloat(), rect.right.toFloat(), rect.bottom.toFloat())
        path.arcTo(rectF, 90f, 180f)
        rectF.set(rect.left.toFloat(), rect.top.toFloat(), rect.left + x, rect.bottom.toFloat())
        path.arcTo(rectF, 270f, -180f)
        path.close()

        AutoShapeKit.Companion.instance()
            .drawShape(canvas, control, viewIndex, shape, path, rect, zoom)
    }

    /**
     * 显示
     * @param canvas
     * @param shape
     * @param rect
     */
    private fun drawFlowChartDisplay(
        canvas: Canvas,
        control: IControl?,
        viewIndex: Int,
        shape: AutoShape,
        rect: Rect,
        zoom: Float
    ) {
        val x = rect.width() * 0.16f

        path.reset()
        path.moveTo(rect.left.toFloat(), rect.exactCenterY())
        path.lineTo(rect.left + x, rect.top.toFloat())
        rectF.set(
            rect.right - x * 2,
            rect.top.toFloat(),
            rect.right.toFloat(),
            rect.bottom.toFloat()
        )
        path.arcTo(rectF, 270f, 180f)
        path.lineTo(rect.left + x, rect.bottom.toFloat())
        path.close()

        AutoShapeKit.Companion.instance()
            .drawShape(canvas, control, viewIndex, shape, path, rect, zoom)
    }

    companion object {
        private val flowRect = Rect()

        private val rectF = RectF()

        private val path = Path()

        //
        private val kit = FlowChartDrawing()

        /**
         * 
         * @return
         */
        fun instance(): FlowChartDrawing {
            return kit
        }
    }
}
