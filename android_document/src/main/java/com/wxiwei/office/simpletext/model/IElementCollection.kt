/*
 * 文件名称:          IElementCollection.java
 *
 * 编译器:            android2.2
 * 时间:              下午5:27:45
 */
package com.wxiwei.office.simpletext.model

/**
 * Element集合接口
 *
 * Read版本:        Read V1.0
 *
 * 作者:            ljj8494
 *
 * 日期:            2011-12-29
 *
 * 负责人:          ljj8494
 */
interface IElementCollection {
    /**
     * 得到指定Office的Element
     */
    fun getElement(offset: Long): IElement?

    /**
     * 得到指定index的Offset
     */
    fun getElementForIndex(index: Int): IElement?

    /**
     *
     */
    fun size(): Int

    /**
     *
     */
    fun dispose()
}
