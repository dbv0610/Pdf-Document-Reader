/*
 * 文件名称:          AllocMem.java
 *
 * 编译器:            android2.2
 * 时间:              下午5:16:04
 */
package com.wxiwei.office.objectpool

import java.util.Vector

/**
 * 共享对象管理
 */
class AllocPool(private val prototype: IMemObj, capacity: Int, capacityIncrement: Int) {

    private val free: Vector<IMemObj> = Vector(capacity, capacityIncrement)

    @Synchronized
    fun allocObject(): IMemObj? {
        if (free.size > 0) {
            return free.removeAt(0)
        }
        return prototype.getCopy()
    }

    @Synchronized
    fun free(obj: IMemObj) {
        free.add(obj)
    }

    @Synchronized
    fun dispose() {
        free.clear()
    }
}
