/*
 * 文件名称:          ElementCollectionImpl.java
 *
 * 编译器:            android2.2
 * 时间:              下午4:43:39
 */
package com.wxiwei.office.simpletext.model

/**
 *
 * Read版本:        Read V1.0
 *
 * 作者:            ljj8494
 *
 * 日期:            2011-12-28
 *
 * 负责人:          ljj8494
 */
open class ElementCollectionImpl(capacity: Int) : IElementCollection {
    private var size = 0

    //
    @JvmField
    protected var elems: Array<IElement?>? = arrayOfNulls(capacity)

    /**
     *
     */
    fun addElement(element: IElement?) {
        if (size >= elems!!.size) {
            ensureCapacity()
        }
        elems!![size] = element
        size++
    }

    /**
     * 插入Element至指定的index
     * @param element
     * @param index
     */
    fun insertElementForIndex(element: IElement?, index: Int) {
        if (size + 1 >= elems!!.size) {
            ensureCapacity()
        }
        var i = size
        while (i >= index) {
            elems!![i] = elems!![i - 1]
            i--
        }
        elems!![index] = element
        size++
    }

    /**
     *
     */
    fun removeElement(offset: Long) {
        val index = getIndex(offset)
        if (index < 0) {
            return
        }
        // Java 中 removeElement(int) 会拓宽调用 removeElement(long)
        removeElement(index.toLong())
    }

    /**
     * 删除指定index的elemnet
     *
     * @param index
     */
    fun removeElementForIndex(index: Int) {
        if (index < 0) {
            return
        }
        val e = elems!![index]
        for (i in index + 1 until size) {
            elems!![i - 1] = elems!![i]
        }
        elems!![size] = null
        size--
        e!!.dispose()
    }

    /**
     *
     */
    override fun getElement(offset: Long): IElement? {
        return getElementForIndex(getIndex(offset))
    }

    /**
     * 得到指定index的Offset
     */
    override fun getElementForIndex(index: Int): IElement? {
        if (index >= 0 && index < size) {
            return elems!![index]
        }
        return null
    }

    /**
     *
     */
    override fun size(): Int {
        return size
    }

    /**
     *
     */
    protected fun getIndex(offset: Long): Int {
        if (size == 0 || offset < 0 || offset >= elems!![size - 1]!!.getEndOffset()) {
            return -1
        }
        var max = size
        var min = 0
        var element: IElement
        var start: Long
        var end: Long
        var mid = -1
        while (true) {
            mid = (max + min) / 2
            element = elems!![mid]!!
            start = element.getStartOffset()
            end = element.getEndOffset()
            if (offset >= start && offset < end) {
                break
            } else if (start > offset) {
                max = mid - 1
            } else if (end <= offset) {
                min = mid + 1
            }
        }
        return mid
    }

    /**
     *
     */
    private fun ensureCapacity() {
        val len = size + CAPACITY
        val e = arrayOfNulls<IElement>(len)
        System.arraycopy(elems!!, 0, e, 0, size)
        elems = e
    }

    /**
     * (non-Javadoc)
     * @see IElementCollection.dispose()
     */
    override fun dispose() {
        if (elems != null) {
            for (i in 0 until size) {
                elems!![i]!!.dispose()
                elems!![i] = null
            }
            elems = null
        }
        size = 0
    }

    companion object {
        // 数组扩容值
        const val CAPACITY = 5
    }
}
