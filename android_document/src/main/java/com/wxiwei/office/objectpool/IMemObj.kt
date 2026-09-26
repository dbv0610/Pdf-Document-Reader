/*
 * 文件名称:          IMemObj.java
 *
 * 编译器:            android2.2
 * 时间:              下午5:12:25
 */
package com.wxiwei.office.objectpool

/**
 * 共享对象接口
 */
interface IMemObj {

    /**
     * 放回对象池
     */
    fun free()

    /**
     * 复制对象
     */
    fun getCopy(): IMemObj?
}
