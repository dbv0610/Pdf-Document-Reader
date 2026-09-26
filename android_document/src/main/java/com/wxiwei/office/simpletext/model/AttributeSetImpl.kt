/*
 * 文件名称:          AttributeSetImpl.java
 *
 * 编译器:            android2.2
 * 时间:              上午10:57:35
 */
package com.wxiwei.office.simpletext.model

import com.wxiwei.office.constant.wp.AttrIDConstant

/**
 * 属性集集合
 *
 * Read版本:        Read V1.0
 *
 * 作者:            ljj8494
 *
 * 日期:            2011-12-28
 *
 * 负责人:          ljj8494
 */
class AttributeSetImpl : IAttributeSet {
    // 属性值个数
    private var size = 0

    //
    private var ID = 0

    // ID
    private var arrayID: ShortArray? = ShortArray(10)

    // value
    private var arrayValue: IntArray? = IntArray(10)

    /**
     * 得到属性集ID
     */
    override fun getID(): Int {
        return this.ID
    }

    /**
     * 添加属性
     * @param attrID
     * @param value
     */
    override fun setAttribute(attrID: Short, value: Int) {
        if (size >= arrayID!!.size) {
            ensureCapacity()
        }
        val index = getIDIndex(attrID.toInt())
        if (index >= 0) {
            arrayValue!![index] = value
        } else {
            arrayID!![size] = attrID
            arrayValue!![size] = value
            size++
        }
    }

    /**
     * 删除属性
     *
     * @param attrID
     */
    override fun removeAttribute(attrID: Short) {
        val index = getIDIndex(attrID.toInt())
        if (index >= 0) {
            for (i in index + 1 until size) {
                arrayID!![i - 1] = arrayID!![i]
                arrayValue!![i - 1] = arrayValue!![i]
            }
            size--
        }
    }

    /**
     * 得到属性
     * @param attrID
     */
    override fun getAttribute(attrID: Short): Int {
        return getAttribute(attrID, true)
    }

    /**
     * 得到属性
     * @param attrID
     * @param pStyle process style
     */
    private fun getAttribute(attrID: Short, pStyle: Boolean): Int {
        var index = getIDIndex(attrID.toInt())
        if (index >= 0) {
            return arrayValue!![index]
        }
        if (!pStyle) {
            return Int.MIN_VALUE
        }
        //
        var style: Style? = null
        var value = Int.MIN_VALUE
        // character attribute
        if (attrID < 0x0fff) {
            index = getIDIndex(AttrIDConstant.FONT_STYLE_ID.toInt())
            if (index >= 0) {
                style = StyleManage.instance().getStyle(arrayValue!![index])
                value = getAttributeForStyle(style, attrID)
            }
        }
        if (value != Int.MIN_VALUE) {
            return value
        }
        // paragraph attribute
        index = getIDIndex(AttrIDConstant.PARA_STYLE_ID.toInt())
        if (index >= 0) {
            style = StyleManage.instance().getStyle(arrayValue!![index])
            value = getAttributeForStyle(style, attrID)
        }
        // paragraph attribute
        return value
    }

    /**
     *
     */
    private fun getAttributeForStyle(style: Style?, attrID: Short): Int {
        var current = style
        val visited = HashSet<Style>()
        // Malformed documents can contain cycles or references to missing styles.
        // Walk iteratively so even a very deep valid chain cannot exhaust the stack.
        while (current != null && visited.add(current)) {
            val attr = current.getAttrbuteSet() as? AttributeSetImpl
            val value = attr?.getAttribute(attrID, false) ?: Int.MIN_VALUE
            if (value != Int.MIN_VALUE) {
                return value
            }
            val baseID = current.getBaseID()
            if (baseID < 0) break
            current = StyleManage.instance().getStyle(baseID)
        }
        return Int.MIN_VALUE
    }

    /**
     * 合并属性
     */
    override fun mergeAttribute(attr: IAttributeSet?) {
        if (attr !is AttributeSetImpl) {
            return
        }
        val attrSet = attr
        val len = attrSet.arrayID!!.size
        var index: Int
        for (i in 0 until len) {
            index = getIDIndex(attrSet.arrayID!![i].toInt())
            if (index > 0) {
                arrayValue!![index] = attrSet.arrayValue!![i]
                continue
            }
            if (size >= arrayID!!.size) {
                ensureCapacity()
            }
            arrayID!![size] = attrSet.arrayID!![i]
            arrayValue!![size] = attrSet.arrayValue!![i]
            size++
        }
    }

    /**
     * (non-Javadoc)
     * @see Object.clone()
     */
    override fun clone(): IAttributeSet {
        val attr = AttributeSetImpl()
        attr.size = size
        val aID = ShortArray(size)
        System.arraycopy(arrayID!!, 0, aID, 0, size)
        attr.arrayID = aID
        val aValue = IntArray(size)
        System.arraycopy(arrayValue!!, 0, aValue, 0, size)
        attr.arrayValue = aValue
        return attr
    }

    /**
     * 得到attrID的index
     */
    private fun getIDIndex(attrID: Int): Int {
        for (i in 0 until size) {
            if (arrayID!![i].toInt() == attrID) {
                return i
            }
        }
        return -1
    }

    /**
     *
     */
    private fun ensureCapacity() {
        val len = size + CAPACITY
        val aID = ShortArray(len)
        System.arraycopy(arrayID!!, 0, aID, 0, size)
        arrayID = aID
        val aValue = IntArray(len)
        System.arraycopy(arrayValue!!, 0, aValue, 0, size)
        arrayValue = aValue
    }

    /**
     *
     */
    override fun dispose() {
        arrayID = null
        arrayValue = null
    }

    companion object {
        // 数组扩容值
        const val CAPACITY = 5
    }
}
