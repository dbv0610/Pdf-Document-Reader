/*
 * 文件名称:          ListManage.java
 *  
 * 编译器:            android2.2
 * 时间:              下午2:44:43
 */
package com.wxiwei.office.common.bulletnumber

/**
 * bullet and number manage
 * 
 * 
 * 
 * 
 * Read版本:        Read V1.0
 * 
 * 
 * 作者:            ljj8494
 * 
 * 
 * 日期:            2012-6-18
 * 
 * 
 * 负责人:          ljj8494
 * 
 * 
 * 负责小组:
 * 
 * 
 * 
 * 
 */
class ListManage {
    /**
     * 
     */
    fun putListData(key: Int?, value: ListData?): Int {
        lists!!.put(key, value)
        return lists.size - 1
    }

    /**
     * 
     */
    fun getListData(key: Int?): ListData? {
        return lists!!.get(key)
    }

    /**
     * 
     */
    fun resetForNormalView() {
        val sets = lists!!.keys
        for (key in sets) {
            val list = lists.get(key)
            if (list != null) {
                list.normalPreParaLevel = 0.toByte()
                list.resetForNormalView()
            }
        }
    }

    /**
     * 
     */
    fun dispose() {
        if (lists != null) {
            val sets = lists.keys
            for (key in sets) {
                lists.get(key)!!.dispose()
            }
            lists.clear()
        }
    }

    //
    private val lists: LinkedHashMap<Int?, ListData?>?

    init {
        lists = LinkedHashMap<Int?, ListData?>()
    }
}
