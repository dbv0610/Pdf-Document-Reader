/*
 * 文件名称:          CellProperty.java
 *
 * 编译器:            android2.2
 * 时间:              下午4:43:58
 */
package com.wxiwei.office.ss.model.baseModel

import com.wxiwei.office.common.hyperlink.Hyperlink
import com.wxiwei.office.simpletext.view.STRoot
import com.wxiwei.office.ss.model.table.SSTable
import java.util.TreeMap

/**
 * TODO: 文件注释
 *
 * Read版本:        Read V1.0
 * 作者:            jqin
 * 日期:            2012-8-31
 * 负责人:           jqin
 */
class CellProperty {
    //propertys of cell
    private val props: MutableMap<Short, Any?> = TreeMap()

    /**
     *
     * @param id
     * @param value
     */
    fun setCellProp(id: Short, value: Any?) {
        props[id] = value
    }

    /**
     *
     * @param id
     * @return
     */
    fun getCellProp(id: Short): Any? {
        return props[id]
    }

    /**
     *
     * @return
     */
    fun getCellNumericType(): Short {
        val obj = props[CELLPROPID_NUMERICTYPE]
        return if (obj != null) {
            obj as Short
        } else {
            -1
        }
    }

    /**
     *
     * @return
     */
    fun getCellMergeRangeAddressIndex(): Int {
        val obj = props[CELLPROPID_MERGEDRANGADDRESS]
        return if (obj != null) {
            obj as Int
        } else {
            -1
        }
    }

    /**
     *
     * @return
     */
    fun getExpandCellRangeAddressIndex(): Int {
        val obj = props[CELLPROPID_EXPANDRANGADDRESS]
        return if (obj != null) {
            obj as Int
        } else {
            -1
        }
    }

    /**
     *
     * @return
     */
    fun getCellHyperlink(): Hyperlink? {
        val obj = props[CELLPROPID_HYPERLINK]
        return if (obj != null) {
            obj as Hyperlink
        } else {
            null
        }
    }

    /**
     *
     * @return
     */
    fun getCellSTRoot(): Int {
        val obj = props[CELLPROPID_STROOT]
        return if (obj != null) {
            obj as Int
        } else {
            -1
        }
    }

    fun getTableInfo(): SSTable? {
        val obj = props[CELLPROPID_TABLEINFO]
        return if (obj != null) {
            obj as SSTable
        } else {
            null
        }
    }

    /**
     *
     */
    fun removeCellSTRoot() {
        props.remove(CELLPROPID_STROOT)
    }

    fun dispose() {
        val objs = props.values
        for (obj in objs) {
            if (obj is Hyperlink) {
                obj.dispose()
            } else if (obj is STRoot) {
                obj.dispose()
            }
        }
    }

    companion object {
        //numeric type
        const val CELLPROPID_NUMERICTYPE: Short = 0

        // rangeAddress index of merge cell
        const val CELLPROPID_MERGEDRANGADDRESS: Short = 1

        //expanded range address index of cell(contents width larger than cell width)
        const val CELLPROPID_EXPANDRANGADDRESS: Short = 2

        //hyperlink
        const val CELLPROPID_HYPERLINK: Short = 3

        //for wraptext cell(its width larger than cell width)
        const val CELLPROPID_STROOT: Short = 4

        //table style
        const val CELLPROPID_TABLEINFO: Short = 5
    }
}
