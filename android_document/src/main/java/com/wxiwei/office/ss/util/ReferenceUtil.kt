/*
 * 文件名称:          RefUtil.java
 *
 * 编译器:            android2.2
 * 时间:              上午10:54:34
 */
package com.wxiwei.office.ss.util

/**
 * TODO: 文件注释
 *
 * Read版本:        Read V1.0
 * 作者:            jqin
 * 日期:            2012-2-27
 * 负责人:           jqin
 */
class ReferenceUtil {
    fun getColumnIndex(ref: String): Int {
        var end = 0
        while (end < ref.length) {
            if (ref[end] >= '0' && ref[end] <= '9') {
                break
            }
            end++
        }
        val colString = ref.substring(0, end)
        return HeaderUtil.instance().getColumnHeaderIndexByText(colString)
    }

    fun getRowIndex(ref: String): Int {
        var ref = ref
        if (ref.indexOf(":") > 0) {
            ref = ref.substring(0, ref.indexOf(":"))
        }

        var end = 0
        while (end < ref.length) {
            if (ref[end] >= '0' && ref[end] <= '9') {
                break
            }
            end++
        }
        val rowString = ref.substring(end, ref.length)
        return rowString.toInt() - 1
    }

    companion object {
        //
        private val util = ReferenceUtil()

        //
        @JvmStatic
        fun instance(): ReferenceUtil {
            return util
        }
    }
}
