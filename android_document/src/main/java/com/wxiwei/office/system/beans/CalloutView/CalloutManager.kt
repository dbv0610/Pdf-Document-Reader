package com.wxiwei.office.system.beans.CalloutView

import kotlin.jvm.JvmName

import android.graphics.Canvas
import android.graphics.Color
import com.wxiwei.office.common.PaintKit
import com.wxiwei.office.constant.MainConstant
import com.wxiwei.office.system.IControl

class CalloutManager(private var control: IControl?) {
    @JvmField var alpha = 0xFF
    @JvmField var color = Color.RED
    @JvmField var width = 10
    private var mode = MainConstant.DRAWMODE_NORMAL
    private var mPathMap: HashMap<Int, MutableList<PathInfo>?>? = HashMap()

    fun drawPath(canvas: Canvas, index: Int, zoom: Float) {
        canvas.scale(zoom, zoom)
        val pathList = mPathMap!![index]
        val paint = PaintKit.instance().paint
        if (pathList != null) {
            for (pathInfo in pathList) {
                paint.strokeWidth = pathInfo.width.toFloat()
                paint.color = pathInfo.color
                canvas.drawPath(pathInfo.path!!, paint)
            }
        }
    }

    fun isPathEmpty(): Boolean = mPathMap!!.isEmpty()

    fun isPathEmpty(index: Int): Boolean = mPathMap!![index] == null

    fun getPath(index: Int, assignPath: Boolean): MutableList<PathInfo>? {
        if (assignPath && mPathMap!![index] == null) {
            mPathMap!![index] = ArrayList()
        }
        return mPathMap!![index]
    }

    fun getAlpha(): Int = alpha
    fun setAlpha(alpha: Int) { this.alpha = alpha }
    fun getColor(): Int = color
    fun setColor(color: Int) { this.color = color }
    fun getWidth(): Int = width
    fun setWidth(width: Int) { this.width = width }

    fun setDrawingMode(mode: Int) {
        if (mode < MainConstant.DRAWMODE_NORMAL || mode > MainConstant.DRAWMODE_CALLOUTERASE) return
        this.mode = mode
    }

    fun getDrawingMode(): Int = mode

    fun dispose() {
        mPathMap!!.clear()
        mPathMap = null
        control = null
    }
}

@get:JvmName("getDrawingModeProperty")
@set:JvmName("setDrawingModeProperty")
var CalloutManager.drawingMode: Int
    get() = getDrawingMode()
    set(value) = setDrawingMode(value)
