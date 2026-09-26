/*
 * 文件名称:          ViewContainer.java
 *
 * 编译器:            android2.2
 * 时间:              下午2:03:24
 */
package com.wxiwei.office.simpletext.view

import com.wxiwei.office.constant.wp.WPModelConstant
import java.util.Collections

/**
 * paragraph view container
 *
 * Read版本:        Read V1.0
 *
 * 作者:            ljj8494
 *
 * 日期:            2012-11-20
 *
 * 负责人:          ljj8494
 */
class ViewContainer {
    //
    private var paras: MutableList<IView>? = ArrayList()

    /**
     *
     */
    @Synchronized
    fun add(para: IView?) {
        if (para != null && para.getEndOffset(null) < WPModelConstant.HEADER) {
            paras!!.add(para)
        }
    }

    /**
     *
     */
    @Synchronized
    fun sort() {
        try {
            Collections.sort(paras!!, object : Comparator<IView> {
                override fun compare(prePara: IView, nextPara: IView): Int {
                    return if (prePara.getEndOffset(null) <= nextPara.getStartOffset(null)) -1 else 1
                }
            })
        } catch (e: Exception) {
        }
    }

    /**
     *
     */
    @Synchronized
    fun getParagraph(offset: Long, isBack: Boolean): IView? {
        val size = paras!!.size
        if (size == 0 || offset < 0 || offset >= paras!![size - 1].getEndOffset(null)) {
            return null
        }
        var max = size
        var min = 0
        var view: IView
        var start: Long
        var end: Long
        var mid = -1
        while (true) {
            mid = (max + min) / 2
            view = paras!![mid]
            start = view.getStartOffset(null)
            end = view.getEndOffset(null)
            if (offset >= start && offset < end) {
                break
            } else if (start > offset) {
                max = mid - 1
            } else if (end <= offset) {
                min = mid + 1
            }
        }
        return view
    }

    /**
     *
     */
    @Synchronized
    fun clear() {
        if (paras != null) {
            paras!!.clear()
        }
    }

    /**
     *
     */
    @Synchronized
    fun dispose() {
        if (paras != null) {
            paras!!.clear()
            paras = null
        }
    }
}
