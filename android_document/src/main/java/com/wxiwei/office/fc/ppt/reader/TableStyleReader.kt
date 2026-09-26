/*
 * 文件名称:          TableStyleReader.java
 *  
 * 编译器:            android2.2
 * 时间:              下午1:33:41
 */
package com.wxiwei.office.fc.ppt.reader

import com.wxiwei.office.fc.dom4j.Element
import com.wxiwei.office.fc.dom4j.ElementHandler
import com.wxiwei.office.fc.dom4j.ElementPath
import com.wxiwei.office.fc.dom4j.io.SAXReader
import com.wxiwei.office.fc.openxml4j.opc.PackagePart
import com.wxiwei.office.pg.model.PGModel
import com.wxiwei.office.pg.model.tableStyle.TableCellBorders
import com.wxiwei.office.pg.model.tableStyle.TableCellStyle
import com.wxiwei.office.pg.model.tableStyle.TableStyle
import com.wxiwei.office.simpletext.model.AttrManage
import com.wxiwei.office.simpletext.model.AttributeSetImpl
import com.wxiwei.office.simpletext.model.IAttributeSet

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
 * 日期:            2013-3-22
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
class TableStyleReader {
    private var pgModel: PGModel? = null
    private var defaultFontSize = 12

    @Throws(Exception::class)
    fun read(pgModel: PGModel?, tableStyle: PackagePart, defaultFontSize: Int) {
        var pgModel = pgModel
        this.pgModel = pgModel
        this.defaultFontSize = defaultFontSize


        // get table style xml
        val saxreader = SAXReader()
        try {
            val `in` = tableStyle.getInputStream()

            val preSaxHandler = TableStyleSaxHandler()

            saxreader.addHandler("/tblStyleLst/tblStyle", preSaxHandler)

            saxreader.read(`in`)
            `in`.close()
            pgModel = null
        } catch (e: Exception) {
            throw e
        } finally {
            saxreader.resetHandlers()
        }
    }

    private fun processTableStyle(tablestyleElement: Element) {
        val tableStyle = TableStyle()

        val styleId = tablestyleElement.attributeValue("styleId")
        //whole table
        var element = tablestyleElement.element("wholeTbl")
        if (element != null) {
            tableStyle.setWholeTable(processTableCellStyle(element))
        }


        //band1 horizontal
        element = tablestyleElement.element("band1H")
        if (element != null) {
            tableStyle.setBand1H(processTableCellStyle(element))
        }


        //band2 horizontal
        element = tablestyleElement.element("band2H")
        if (element != null) {
            tableStyle.setBand2H(processTableCellStyle(element))
        }


        //band1 vertical
        element = tablestyleElement.element("band1V")
        if (element != null) {
            tableStyle.setBand1V(processTableCellStyle(element))
        }


        //band2 vertical
        element = tablestyleElement.element("band2V")
        if (element != null) {
            tableStyle.setBand2V(processTableCellStyle(element))
        }


        //last column
        element = tablestyleElement.element("lastCol")
        if (element != null) {
            tableStyle.setLastCol(processTableCellStyle(element))
        }


        //first column
        element = tablestyleElement.element("firstCol")
        if (element != null) {
            tableStyle.setFirstCol(processTableCellStyle(element))
        }


        //last row
        element = tablestyleElement.element("lastRow")
        if (element != null) {
            tableStyle.setLastRow(processTableCellStyle(element))
        }


        //first row
        element = tablestyleElement.element("firstRow")
        if (element != null) {
            tableStyle.setFirstRow(processTableCellStyle(element))
        }

        pgModel!!.putTableStyle(styleId, tableStyle)
    }

    private fun processTableCellStyle(tableStyleElement: Element): TableCellStyle {
        val tableCellStyle = TableCellStyle()
        //cell text style
        val cellTextStyleElement = tableStyleElement.element("tcTxStyle")
        if (cellTextStyleElement != null) {
            val attr: IAttributeSet = AttributeSetImpl()
            //bold
            var str = cellTextStyleElement.attributeValue("b")
            if ("on" == str) {
                AttrManage.instance().setFontBold(attr, true)
            }


            //Italic
            str = cellTextStyleElement.attributeValue("i")
            if ("on" == str) {
                AttrManage.instance().setFontItalic(attr, true)
            }


            //TTOD: font color

            //set default font size
            AttrManage.instance().setFontSize(attr, defaultFontSize)

            tableCellStyle.setFontAttributeSet(attr)
        }
        //cell style
        val cellStyleElement = tableStyleElement.element("tcStyle")
        //borders
        val ele = cellStyleElement.element("tcBdr")
        if (ele != null) {
            tableCellStyle.setTableCellBorders(getTableCellBorders(ele))
        }


        //fill
        tableCellStyle.setTableCellBgFill(cellStyleElement.element("fill"))


        return tableCellStyle
    }

    private fun getTableCellBorders(tcBrdElement: Element): TableCellBorders {
        val tableCellBorders = TableCellBorders()
        //left
        var ele = tcBrdElement.element("left")
        if (ele != null) {
            tableCellBorders.setLeftBorder(ele.element("ln"))
        }


        //right
        ele = tcBrdElement.element("right")
        if (ele != null) {
            tableCellBorders.setRightBorder(ele.element("ln"))
        }


        //top
        ele = tcBrdElement.element("top")
        if (ele != null) {
            tableCellBorders.setTopBorder(ele.element("ln"))
        }


        //bottom
        ele = tcBrdElement.element("bottom")
        if (ele != null) {
            tableCellBorders.setBottomBorder(ele.element("ln"))
        }

        return tableCellBorders
    }

    /**
     * fix very large XML documents
     * 
     */
    internal inner class TableStyleSaxHandler : ElementHandler {
        /**
         * 
         * 
         */
        override fun onStart(elementPath: ElementPath?) {
        }

        /**
         * @throws Exception
         */
        override fun onEnd(elementPath: ElementPath) {
            val elem = elementPath.getCurrent()
            val name = elem.getName()
            try {
                if (name == "tblStyle") {
                    processTableStyle(elem)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            elem.detach()
        }
    }

    companion object {
        private val tableStyleReader = TableStyleReader()

        /**
         * 
         */
        @JvmStatic
        fun instance(): TableStyleReader {
            return tableStyleReader
        }
    }
}
