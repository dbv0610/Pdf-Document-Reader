/*
 * 文件名称:          ParaToken.java
 *
 * 编译器:            android2.2
 * 时间:              上午9:26:51
 */
package com.wxiwei.office.objectpool

/**
 * 段落视图的令牌
 */
class ParaToken(private var obj: IMemObj?) {

    fun free() {
        obj!!.free()
    }

    /**
     * Returns the using.
     */
    var isFree: Boolean = false

    fun dispose() {
        obj = null
    }
}
