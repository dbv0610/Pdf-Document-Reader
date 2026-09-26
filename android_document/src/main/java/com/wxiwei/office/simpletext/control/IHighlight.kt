/*
 * 文件名称:          IHighlight.java
 *
 * 编译器:            android2.2
 * 时间:              上午9:57:12
 */
package com.wxiwei.office.simpletext.control

import android.graphics.Canvas
import com.wxiwei.office.simpletext.view.IView

/**
 * highlight
 *
 * Read版本:        Read V1.0
 *
 * 作者:            ljj8494
 *
 * 日期:            2012-7-27
 *
 * 负责人:          ljj8494
 */
interface IHighlight {

    /**
     * draw highlight
     */
    fun draw(canvas: Canvas, line: IView, originX: Int, originY: Int, start: Long, end: Long, zoom: Float)

    /**
     *
     */
    fun getSelectText(): String?

    /**
     *
     */
    fun isSelectText(): Boolean

    /**
     * remove all selection
     */
    fun removeHighlight()

    /**
     *
     */
    fun addHighlight(start: Long, end: Long)

    /**
     * @return Returns the selectStart.
     */
    fun getSelectStart(): Long

    /**
     * @param selectStart The selectStart to set.
     */
    fun setSelectStart(selectStart: Long)

    /**
     * @return Returns the selectEnd.
     */
    fun getSelectEnd(): Long

    /**
     * @param selectEnd The selectEnd to set.
     */
    fun setSelectEnd(selectEnd: Long)

    /**
     * @param isPaintHighlight The isPaintHighlight to set.
     */
    fun setPaintHighlight(isPaintHighlight: Boolean)

    /**
     *
     */
    fun dispose()
}
