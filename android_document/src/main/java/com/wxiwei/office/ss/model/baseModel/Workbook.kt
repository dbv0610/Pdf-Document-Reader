/*
 * 文件名称:          WorkBooks.java
 *
 * 编译器:            android2.2
 * 时间:              下午3:07:27
 */
package com.wxiwei.office.ss.model.baseModel

import android.graphics.Color
import android.os.Message
import com.wxiwei.office.common.bg.BackgroundAndFill
import com.wxiwei.office.common.picture.Picture
import com.wxiwei.office.constant.MainConstant
import com.wxiwei.office.simpletext.font.Font
import com.wxiwei.office.simpletext.model.SectionElement
import com.wxiwei.office.ss.model.style.CellStyle
import com.wxiwei.office.ss.model.table.TableFormatManager
import com.wxiwei.office.system.ReaderHandler
import com.wxiwei.office.system.OpenTrace

/**
 * Excel model 数据
 *
 * Read版本:        Read V1.0
 * 作者:            ljj8494
 * 日期:            2012-2-16
 * 负责人:          ljj8494
 */
open class Workbook(before07: Boolean) {
    @JvmField
    protected var readerHandler: ReaderHandler? = null

    //
    @JvmField
    protected var isUsing1904DateWindowing: Boolean = false

    // this holds the Sheet objects attached to this workbook, sheet index is continuous
    @JvmField
    protected var sheets: MutableMap<Int, Sheet?>? = HashMap(5)

    // this holds the Font objects attached to this workbook, index is continuous
    @JvmField
    protected var fonts: MutableMap<Int, Font?>? = HashMap(20)

    // this holds the Font objects attached to this workbook
    @JvmField
    protected var colors: MutableMap<Int, Int>? = HashMap(20)

    //image data
    private var pictures: MutableMap<Int, Picture?>? = HashMap()

    //cell styles, index is continuous
    @JvmField
    protected var cellStyles: MutableMap<Int, CellStyle?>? = HashMap(80)

    //shared strings, index is continuous
    @JvmField
    protected var sharedString: MutableMap<Int, Any?>? = HashMap(20)

    //theme Color index
    private var themeColor: MutableMap<Int, Int>? = HashMap(20)

    //scheme Color index
    private var schemeColor: MutableMap<String?, Int>? = HashMap(20)

    //table format manager
    private var tableFormatManager: TableFormatManager? = null

    //version
    private val before07: Boolean = before07

    /**
     * add sheet on this workbook
     * @param index
     * @param sheet
     */
    fun addSheet(index: Int, sheet: Sheet?) {
        sheets!![index] = sheet
    }

    /**
     * set reader handler
     * @param readerHandler
     */
    fun setReaderHandler(readerHandler: ReaderHandler?) {
        this.readerHandler = readerHandler
    }

    /**
     *
     * @return
     */
    fun getReaderHandler(): ReaderHandler? = readerHandler

    /**
     * get sheet for sheet name
     */
    fun getSheet(sheetName: String?): Sheet? {
        val sheetCol: Collection<Sheet?> = sheets!!.values
        for (sheet in sheetCol) {
            if (sheet!!.getSheetName()!! == sheetName) {
                return sheet
            }
        }
        return null
    }

    /**
     * get sheet for sheet name
     */
    open fun getSheetIndex(sheet: Sheet?): Int {
        val iter = sheets!!.keys.iterator()
        while (iter.hasNext()) {
            val index = iter.next()
            if (sheets!![index]!! == sheet) {
                return index
            }
        }
        return -1
    }

    /**
     * get sheet for sheet index;
     */
    fun getSheet(index: Int): Sheet? {
        if (index < 0 || index >= sheets!!.size) {
            return null
        }
        return sheets!![index]
    }

    /**
     * get sheet count of this workbook
     */
    fun getSheetCount(): Int {
        return sheets!!.size
    }

    /**
     * Bounded model dump used to separate parser problems from renderer
     * problems.  It intentionally reports coordinates and decoded values,
     * not just row/cell counts.
     */
    fun debugDump(label: String) {
        OpenTrace.d("excel.model[$label] sheets=${getSheetCount()} before07=${isBefore07Version()}")
        for (sheetIndex in 0 until getSheetCount()) {
            val sheet = getSheet(sheetIndex) ?: continue
            var rows = 0
            var cells = 0
            var valued = 0
            var sample = 0
            val sampleText = StringBuilder()
            for (rowIndex in sheet.getFirstRowNum()..sheet.getLastRowNum()) {
                val row = sheet.getRow(rowIndex) ?: continue
                rows++
                for (cell in row.cellCollection()) {
                    cells++
                    if (!cell.hasValidValue()) continue
                    valued++
                    if (sample < 12) {
                        val value = try {
                            when (cell.getCellType()) {
                                Cell.CELL_TYPE_STRING -> sheet.getWorkbook()?.getSharedString(cell.getStringCellValueIndex())
                                Cell.CELL_TYPE_NUMERIC -> cell.getNumberValue().toString()
                                Cell.CELL_TYPE_BOOLEAN -> cell.getBooleanValue().toString()
                                Cell.CELL_TYPE_ERROR -> cell.getErrorValue().toString()
                                else -> "type=${cell.getCellType()}"
                            }
                        } catch (error: Throwable) {
                            "decode-error=${error.javaClass.simpleName}"
                        }
                        sampleText.append(" [r=${cell.getRowNumber()},c=${cell.getColNumber()},type=${cell.getCellType()},value=$value]")
                        sample++
                    }
                }
            }
            OpenTrace.d(
                "excel.model[$label] sheet=$sheetIndex name=${sheet.getSheetName()} " +
                    "state=${sheet.getState()} firstRow=${sheet.getFirstRowNum()} lastRow=${sheet.getLastRowNum()} " +
                    "rows=$rows cells=$cells valued=$valued$sampleText"
            )
        }
    }

    /**
     * add font of this workbook
     */
    fun addFont(index: Int, font: Font?) {
        fonts!![index] = font
    }

    /**
     * get font for index
     * @param index
     * @return
     */
    fun getFont(index: Int): Font? {
        return fonts!![index]
    }

    /**
     *  put paletter color before add new color
     * @param argb
     * @return index of color list
     */
    @Synchronized
    fun addColor(argb: Int): Int {
        val colors = colors!!
        if (colors.containsValue(argb)) {
            val iter = colors.keys.iterator()
            var index = 0
            while (iter.hasNext()) {
                index = iter.next()
                if (colors[index]!! == argb) {
                    break
                }
            }
            return index
        } else {
            var index = colors.size - 1
            while (colors[index] != null) {
                index++
            }
            colors[index] = argb
            return index
        }
    }

    /**
     * add color of this workbook
     */
    @Synchronized
    fun addColor(index: Int, rgba: Int) {
        colors!![index] = rgba
    }

    /**
     * get color of index
     */
    fun getColor(index: Int): Int {
        return getColor(index, false)
    }

    /**
     * get color of index
     */
    @Synchronized
    fun getColor(index: Int, line: Boolean): Int {
        var t = colors!![index]
        if (t == null && (index >= 0 && index <= 7)) {
            t = colors!![8]
        }

        if (t == null) {
            if (line) {
                return Color.BLACK
            } else {
                return Color.WHITE
            }
        }
        return t
    }

    /**
     *
     * @param index
     * @param cellStyle
     */
    fun addCellStyle(index: Int, cellStyle: CellStyle?) {
        cellStyles!![index] = cellStyle
    }

    fun getNumStyles(): Int {
        return cellStyles!!.size
    }

    /**
     *
     * @param index
     * @return
     */
    fun getCellStyle(index: Int): CellStyle? {
        return cellStyles!![index]
    }

    /**
     *
     * @param item
     * @return
     */
    fun addSharedString(item: Any?): Int {
        if (item == null) {
            return -1
        }

//        if(sharedString.containsValue(item))
//        {
//            Iterator<Integer> iter = sharedString.keySet().iterator();
//            int index;
//            while(iter.hasNext())
//            {
//                index = iter.next();
//                if(sharedString.get(index).equals(item))
//                {
//                    return index;
//                }
//            }
//        }
//        else
        run {
            //add to end
            sharedString!![sharedString!!.size] = item
            return sharedString!!.size - 1
        }
    }

    /**
     *
     * @param index
     * @param item
     */
    fun addSharedString(index: Int, item: Any?) {
        sharedString!![index] = item
    }

    /**
     *
     * @param index
     * @return
     */
    fun getSharedString(index: Int): String? {
        val si = sharedString!![index]
        var value: String? = null
        if (si is SectionElement) {
            value = si.getText(null)
        } else if (si is String) {
            value = si
        }

        return value
    }

    /**
     *
     * @param index
     * @return string or SectionElement
     */
    fun getSharedItem(index: Int): Any? {
        return sharedString!![index]
    }

    /**
     *
     * @param index
     * @param colorIndex
     */
    @Synchronized
    fun addThemeColorIndex(index: Int, colorIndex: Int) {
        themeColor!![index] = colorIndex
    }

    /**
     *
     * @param index
     * @return color index
     */
    @Synchronized
    fun getThemeColorIndex(index: Int): Int {
        val t = themeColor!![index]
        return t ?: -1
    }

    /**
     *
     * @param index
     * @return color
     */
    @Synchronized
    fun getThemeColor(index: Int): Int {
        val t = themeColor!![index]?.let { colors!![it] }
        return t ?: Color.BLACK
    }

    /**
     *
     * @param key
     * @param colorIndex
     */
    @Synchronized
    fun addSchemeColorIndex(key: String?, colorIndex: Int) {
        schemeColor!![key] = colorIndex
    }

    /**
     *
     * @param key
     * @return color index
     */
    @Synchronized
    fun getSchemeColorIndex(key: String?): Int {
        val t = schemeColor!![key]
        return t ?: -1
    }

    /**
     *
     * @param key
     * @return color
     */
    @Synchronized
    fun getSchemeColor(key: String?): Int {
        val t = schemeColor!![key]?.let { colors!![it] }
        return t ?: Color.BLACK
    }

    /**
     * @return Returns the isUsing1904DateWindowing.
     */
    fun isUsing1904DateWindowing(): Boolean = isUsing1904DateWindowing

    /**
     * @param isUsing1904DateWindowing The isUsing1904DateWindowing to set.
     */
    fun setUsing1904DateWindowing(isUsing1904DateWindowing: Boolean) {
        this.isUsing1904DateWindowing = isUsing1904DateWindowing
    }

    /**
     *
     * @param index
     * @param pic
     */
    fun addPicture(index: Int, pic: Picture?) {
        pictures!![index] = pic
    }

    /**
     *
     * @param pic
     * @return add postion
     */
    fun addPicture(pic: Picture): Int {
        //check exist
        val iter = pictures!!.keys.iterator()
        var index = 0
        while (iter.hasNext()) {
            index = iter.next()
            if (pictures!![index]!!.getTempFilePath()!! == pic.getTempFilePath()) {
                //has exist
                return index
            }
        }

        pictures!![index + 1] = pic
        return index + 1
    }

    /**
     *
     * @param index
     * @return
     */
    fun getPicture(index: Int): Picture? {
        return pictures!![index]
    }

    /**
     *
     * @return
     */
    fun isBefore07Version(): Boolean {
        return before07
    }

    fun getMaxRow(): Int {
        return if (before07) MAXROW_03 else MAXROW_07
    }

    fun getMaxColumn(): Int {
        return if (before07) MAXCOLUMN_03 else MAXCOLUMN_07
    }

    fun setTableFormatManager(tableFormatManager: TableFormatManager?) {
        this.tableFormatManager = tableFormatManager
    }

    fun getTableFormatManager(): TableFormatManager? {
        return tableFormatManager
    }

    fun destroy() {
        if (readerHandler != null) {
            val msg = Message()
            msg.what = MainConstant.HANDLER_MESSAGE_DISPOSE
            readerHandler!!.handleMessage(msg)
            readerHandler = null
        }

        if (sheets != null) {
            val sheetCollection: Collection<Sheet?> = sheets!!.values
            for (sheet in sheetCollection) {
                sheet!!.dispose()
            }
            sheets!!.clear()
            sheets = null
        }

        if (fonts != null) {
            val fontCollection: Collection<Font?> = fonts!!.values
            for (font in fontCollection) {
                font!!.dispose()
            }
            fonts!!.clear()
            fonts = null
        }

        if (colors != null) {
            colors!!.clear()
            colors = null
        }

        if (pictures != null) {
            pictures!!.clear()
            pictures = null
        }

        if (cellStyles != null) {
            val styleCollection: Collection<CellStyle?> = cellStyles!!.values
            for (cellStyle in styleCollection) {
                cellStyle!!.dispose()
            }
            cellStyles!!.clear()
            cellStyles = null
        }

        if (sharedString != null) {
            sharedString!!.clear()
            sharedString = null
        }

        if (themeColor != null) {
            themeColor!!.clear()
            themeColor = null
        }

        if (schemeColor != null) {
            schemeColor!!.clear()
            schemeColor = null
        }
    }

    /**
     *
     */
    open fun dispose() {
        synchronized(this) {
            destroy()
        }
    }

    companion object {
        const val MAXROW_03 = 65536
        const val MAXCOLUMN_03 = 256
        const val MAXROW_07 = 1048576
        const val MAXCOLUMN_07 = 16384

        @JvmStatic
        fun isValidateStyle(cellStyle: CellStyle?): Boolean {
            if (cellStyle == null) {
                return false
            }

            if (cellStyle.getBorderLeft() > 0
                || cellStyle.getBorderTop() > 0
                || cellStyle.getBorderRight() > 0
                || cellStyle.getBorderBottom() > 0
            ) {
                //has border
                return true
            }

            //not special AUTOMATIC case, not white
            if (cellStyle.getFillPatternType() != BackgroundAndFill.FILL_NO) {
                //foregrond color was  not white
                return true
            }

            return false
        }
    }
}
