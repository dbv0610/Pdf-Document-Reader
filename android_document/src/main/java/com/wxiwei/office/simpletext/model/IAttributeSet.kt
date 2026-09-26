/*
 * 文件名称:          I.java
 *
 * 编译器:            android2.2
 * 时间:              上午10:44:20
 */
package com.wxiwei.office.simpletext.model

/**
 * 属性集接口
 *
 * 属性以ID，Value方式记录，ID类型是short，value类型是int
 *
 * Read版本:        Read V1.0
 *
 * 作者:            ljj8494
 *
 * 日期:            2011-12-28
 *
 * 负责人:          ljj8494
 */
interface IAttributeSet {

    /**
     * 得到属性集ID
     */
    fun getID(): Int

    /**
     * 添加属性
     * @param attrID
     * @param value
     */
    fun setAttribute(attrID: Short, value: Int)

    /**
     * 删除属性
     *
     * @param attrID
     */
    fun removeAttribute(attrID: Short)

    /**
     * 得到属性
     * @param attrID
     */
    fun getAttribute(attrID: Short): Int

    /**
     * 合并属性
     */
    fun mergeAttribute(attr: IAttributeSet?)

    /**
     *
     */
    fun clone(): IAttributeSet

    /**
     *
     */
    fun dispose()
}
