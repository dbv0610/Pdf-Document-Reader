/*
 * 文件名称:          CriticalLock.java
 *  
 * 编译器:            android2.2
 * 时间:              上午10:22:11
 */
package com.wxiwei.office.common

import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

/**
 * TODO: 文件注释
 * 
 * 
 * 
 * 
 * Read版本:        Read V1.0
 * 
 * 
 * 作者:            jqin
 * 
 * 
 * 日期:            2011-12-12
 * 
 * 
 * 负责人:           jqin
 * 
 * 
 * 负责小组:
 * 
 * 
 * 
 * 
 */
object CriticalLock {
    fun lock() {
        reentrantLock.lock()
    }

    fun unlock() {
        reentrantLock.unlock()
    }

    private val reentrantLock: Lock = ReentrantLock()
}
