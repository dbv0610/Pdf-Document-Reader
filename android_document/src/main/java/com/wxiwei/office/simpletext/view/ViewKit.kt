/*
 * 文件名称:          ViewKit.java
 *
 * 编译器:            android2.2
 * 时间:              上午10:00:00
 */
package com.wxiwei.office.simpletext.view

/**
 * 视图布局用到工具类
 *
 * Read版本:        Read V1.0
 *
 * 作者:            ljj8494
 *
 * 日期:            2011-11-17
 *
 * 负责人:          ljj8494
 */
open class ViewKit {

    /**
     * 位操作，把指定位 置为0或1
     *
     * @param flag
     * @param pos 指的位
     * @param b = true 置1，= false 置0
     */
    fun setBitValue(flag: Int, pos: Int, b: Boolean): Int {
        var temp = if (b) flag else flag.inv()
        temp = (temp ushr pos) or 1
        temp = (temp shl pos)
        temp = if (b) temp or flag else (temp.inv()) and flag
        return temp
    }

    /**
     * 位操作，把指定位 置为0或1
     *
     * @param flag
     * @param pos 指的位
     * @param b = true 置1，= false 置0
     */
    fun getBitValue(flag: Int, pos: Int): Boolean {
        return ((flag ushr pos) and 1) == 1
    }

    /**
     * 得到类型的上层View，没有则返回空
     */
    fun getParentView(view: IView?, viewType: Short): IView? {
        var p = view!!.getParentView()
        while (p != null && p.getType() != viewType) {
            p = p.getParentView()
        }
        return p
    }

    companion object {
        private val kit = ViewKit()

        //
        @JvmStatic
        fun instance(): ViewKit {
            return kit
        }
    }
}
