package com.wxiwei.office.system.beans.CalloutView

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Region
import android.view.MotionEvent
import android.view.View
import com.wxiwei.office.constant.MainConstant
import com.wxiwei.office.system.IControl

class CalloutView(
    context: Context,
    private var control: IControl,
    private val mListener: IExportListener
) : View(context) {
    private var zoom = 1.0f
    private var mX = 0f
    private var mY = 0f
    private val touchTolerance = 4f
    private var mPathList: MutableList<PathInfo>? = null
    private var mPathInfo: PathInfo? = null
    private val offset = 5
    private var left = 0
    private var top = 0
    private val calloutMgr = control.getSysKit().getCalloutManager()
    private var runnable: Runnable? = null
    private var index = 0

    fun setZoom(zoom: Float) { this.zoom = zoom }
    fun setIndex(index: Int) { this.index = index }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val clipRect = canvas.clipBounds
        mPathList = calloutMgr.getPath(index, false)
        mPathList?.forEach { pathInfo ->
            val paint = MyPaint()
            paint.strokeWidth = pathInfo.width.toFloat()
            paint.color = pathInfo.color
            canvas.save()
            canvas.clipRect(left, top, clipRect.right, clipRect.bottom)
            canvas.scale(zoom, zoom)
            canvas.drawPath(pathInfo.path!!, paint)
            canvas.restore()
        }
    }

    fun setClip(left: Int, top: Int) {
        this.left = left
        this.top = top
    }

    private fun touchStart(xValue: Float, yValue: Float) {
        val x = xValue / zoom
        val y = yValue / zoom
        mX = x
        mY = y
        if (calloutMgr.getDrawingMode() == MainConstant.DRAWMODE_CALLOUTDRAW) {
            mPathInfo = PathInfo()
            mPathInfo!!.path = Path()
            mPathInfo!!.path!!.moveTo(x, y)
            mPathInfo!!.color = calloutMgr.getColor()
            mPathInfo!!.width = calloutMgr.getWidth()
            mPathList = calloutMgr.getPath(index, true)
            mPathList!!.add(mPathInfo!!)
        }
    }

    private fun touchMove(xValue: Float, yValue: Float) {
        if (calloutMgr.getDrawingMode() == MainConstant.DRAWMODE_CALLOUTDRAW) {
            val x = xValue / zoom
            val y = yValue / zoom
            if (kotlin.math.abs(x - mX) >= touchTolerance || kotlin.math.abs(y - mY) >= touchTolerance) {
                mPathInfo!!.path!!.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2)
                mX = x
                mY = y
            }
        }
    }

    private fun touchUp() {
        if (calloutMgr.getDrawingMode() == MainConstant.DRAWMODE_CALLOUTDRAW) {
            mPathInfo!!.path!!.lineTo(mX, mY)
            mPathInfo!!.x = mX + 1
            mPathInfo!!.y = mY + 1
        } else if (calloutMgr.getDrawingMode() == MainConstant.DRAWMODE_CALLOUTERASE) {
            mPathList?.let { paths ->
                var i = 0
                while (i < paths.size) {
                    val pathInfo = paths[i]
                    val path = Path(pathInfo.path!!)
                    path.lineTo(pathInfo.x, pathInfo.y)
                    val bounds = RectF()
                    path.computeBounds(bounds, false)
                    val region = Region()
                    region.setPath(path, Region(bounds.left.toInt(), bounds.top.toInt(), bounds.right.toInt(), bounds.bottom.toInt()))
                    if (region.op(Region(mX.toInt() - offset, mY.toInt() - offset, mX.toInt() + offset, mY.toInt() + offset), Region.Op.INTERSECT)) {
                        paths.removeAt(i)
                    } else {
                        i++
                    }
                }
            }
        }
    }

    private fun exportImage() {
        runnable?.let { removeCallbacks(it) }
        runnable = Runnable { mListener.exportImage() }
        postDelayed(runnable!!, 1000)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (calloutMgr.getDrawingMode() == MainConstant.DRAWMODE_NORMAL) return false
        when (event.action) {
            MotionEvent.ACTION_DOWN -> { touchStart(event.x, event.y); invalidate() }
            MotionEvent.ACTION_MOVE -> { touchMove(event.x, event.y); invalidate() }
            MotionEvent.ACTION_UP -> { touchUp(); invalidate(); exportImage() }
        }
        return true
    }
}
