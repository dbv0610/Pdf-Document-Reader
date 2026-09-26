/*
 * 文件名称:          TableStyleKit.java
 *
 * 编译器:            android2.2
 * 时间:              下午2:06:58
 */
package com.wxiwei.office.ss.model.table

import com.wxiwei.office.constant.SchemeClrConstant
import com.wxiwei.office.ss.util.ColorUtil

/**
 * TODO: 文件注释
 *
 * Read版本:        Read V1.0
 * 作者:            jqin
 * 日期:            2013-4-18
 * 负责人:           jqin
 */
class TableStyleKit {
    /**
     * light table style
     */
    private var tableStyleLight1_7: SSTableStyle? = null
    private var tableStyleLight8_14: SSTableStyle? = null
    private var tableStyleLight15_21: SSTableStyle? = null

    /**
     * medium table style
     */
    private var tableStyleMedium1_7: SSTableStyle? = null
    private var tableStyleMedium8_14: SSTableStyle? = null
    private var tableStyleMedium15_21: SSTableStyle? = null
    private var tableStyleMedium22_28: SSTableStyle? = null

    /**
     * dark table style
     */
    private var tableStyleDark1_7: SSTableStyle? = null
    private var tableStyleDark8: SSTableStyle? = null
    private var tableStyleDark9_11: SSTableStyle? = null

    fun getTableStyle(tableName: String?, schemeColor: Map<String, Int>?): SSTableStyle? {
        var tableName = tableName
        try {
//          String regEx="[^0-9]";
//          Pattern p = Pattern.compile(regEx);
//          Matcher m = p.matcher(tableName);
//          tableName = m.replaceAll("").trim();

            if (tableName == null || tableName.length == 0) {
                return null
            }

            if (tableName.contains("Light")) {
                //Light
                tableName = tableName.substring("TableStyleLight".length).split(" ")[0]
                val id = tableName.toInt()
                when ((id - 1) / 7) {
                    0 -> //id = 1,2,3,4,5,6,7
                        return getTableStyleLight1_7(getSchemeColor(schemeColor, id))

                    1 -> //id = 8,9,10,11,12,13,14
                        return getTableStyleLight8_14(getSchemeColor(schemeColor, id))

                    2 -> //id = 15,16,17,18,19,20,21
                        return getTableStyleLight15_21(getSchemeColor(schemeColor, id))
                }
            } else if (tableName.contains("Medium")) {
                //Medium
                tableName = tableName.substring("TableStyleMedium".length).split(" ")[0]
                val id = tableName.toInt()
                when ((id - 1) / 7) {
                    0 -> //id = 1,2,3,4,5,6,7
                        return getTableStyleMedium1_7(getSchemeColor(schemeColor, id))

                    1 -> //id = 8,9,10,11,12,13,14
                        return getTableStyleMedium8_14(getSchemeColor(schemeColor, id))

                    2 -> //id = 15,16,17,18,19,20,21
                        return getTableStyleMedium15_21(getSchemeColor(schemeColor, id))

                    3 -> //id = 22,23,24,25,26,27,28
                        return getTableStyleMedium22_28(getSchemeColor(schemeColor, id))
                }
            } else {
                //Dark
                tableName = tableName.substring("TableStyleDark".length).split(" ")[0]
                val id = tableName.toInt()
                when (id) {
                    1, 2, 3, 4, 5, 6, 7 -> return getTableStyleDark1_7(getSchemeColor(schemeColor, id))

                    8 -> return getTableStyleDark8()

                    9, 10, 11 -> return getTableStyleDark9_11(
                        getSchemeColor(schemeColor, (id - 8) * 2 + 1),
                        getSchemeColor(schemeColor, (id - 8) * 2)
                    )
                }
            }
        } catch (e: Exception) {
        }

        return null
    }

    /**
     *
     * @param schemeColor
     * @param id
     * @return
     */
    private fun getSchemeColor(schemeColor: Map<String, Int>?, id: Int): Int {
        var id = id
        id %= 7
        if (id == 1) {
            //black
            return 0xFF000000.toInt()
        } else {
            return schemeColor!![schemeClrName[(id - 2 + 7) % 7]]!!
        }
    }

    /**
     * Table Style Light 1-7
     * @param schemeColor
     * @return
     */
    private fun getTableStyleLight1_7(schemeColor: Int): SSTableStyle? {
        if (tableStyleLight1_7 == null) {
            tableStyleLight1_7 = SSTableStyle()
            //first row, last row
            var cellstyle = SSTableCellStyle(0xFFFFFFFF.toInt())
            cellstyle.setFontColor(schemeColor)
            cellstyle.setBorderColor(schemeColor)

            tableStyleLight1_7!!.setFirstRow(cellstyle)
            tableStyleLight1_7!!.setLastRow(cellstyle)

            //band1H, band1V
            var color = ColorUtil.instance().getColorWithTint(schemeColor, 0.8f.toDouble())
            cellstyle = SSTableCellStyle(color)
            cellstyle.setFontColor(schemeColor)

            tableStyleLight1_7!!.setBand1H(cellstyle)
            tableStyleLight1_7!!.setBand1V(cellstyle)

            //band2H, band2V
            cellstyle = SSTableCellStyle(0xFFFFFFFF.toInt())
            cellstyle.setFontColor(schemeColor)

            tableStyleLight1_7!!.setBand2H(cellstyle)
            tableStyleLight1_7!!.setBand2V(cellstyle)
        } else {
            //first row, last row
            var cellstyle = tableStyleLight1_7!!.getFirstRow()!!
            cellstyle.setFontColor(schemeColor)
            cellstyle.setBorderColor(schemeColor)
            tableStyleLight1_7!!.setFirstRow(cellstyle)
            tableStyleLight1_7!!.setLastRow(cellstyle)

            //band1H, band1V
            var color = ColorUtil.instance().getColorWithTint(schemeColor, 0.8f.toDouble())
            cellstyle = tableStyleLight1_7!!.getBand1H()!!
            cellstyle.setFillColor(color)
            cellstyle.setFontColor(schemeColor)

            tableStyleLight1_7!!.setBand1H(cellstyle)
            tableStyleLight1_7!!.setBand1V(cellstyle)

            //band2H, band2V
            cellstyle = tableStyleLight1_7!!.getBand2H()!!
            cellstyle.setFontColor(schemeColor)

            tableStyleLight1_7!!.setBand2H(cellstyle)
            tableStyleLight1_7!!.setBand2V(cellstyle)
        }
        return tableStyleLight1_7
    }

    /**
     * Table Style Light 8-14
     * @param schemeColor
     * @return
     */
    private fun getTableStyleLight8_14(schemeColor: Int): SSTableStyle? {
        if (tableStyleLight8_14 == null) {
            tableStyleLight8_14 = SSTableStyle()
            //first row
            var cellstyle = SSTableCellStyle(schemeColor)
            cellstyle.setFontColor(0xFFFFFFFF.toInt())
            cellstyle.setBorderColor(schemeColor)

            tableStyleLight8_14!!.setFirstRow(cellstyle)

            //band1H, band1V, last row, band2H, band2V
            cellstyle = SSTableCellStyle(0xFFFFFFFF.toInt())
            cellstyle.setBorderColor(schemeColor)

            tableStyleLight8_14!!.setLastRow(cellstyle)
            tableStyleLight8_14!!.setBand1H(cellstyle)
            tableStyleLight8_14!!.setBand1V(cellstyle)
            tableStyleLight8_14!!.setBand2H(cellstyle)
            tableStyleLight8_14!!.setBand2V(cellstyle)
        } else {
            //first row
            var cellstyle = tableStyleLight8_14!!.getFirstRow()!!
            cellstyle.setFillColor(schemeColor)
            cellstyle.setBorderColor(schemeColor)

            tableStyleLight8_14!!.setFirstRow(cellstyle)

            //band1H, band1V, last row, band2H, band2V
            cellstyle = tableStyleLight8_14!!.getBand1H()!!
            cellstyle.setBorderColor(schemeColor)

            tableStyleLight8_14!!.setLastRow(cellstyle)
            tableStyleLight8_14!!.setBand1H(cellstyle)
            tableStyleLight8_14!!.setBand1V(cellstyle)
            tableStyleLight8_14!!.setBand2H(cellstyle)
            tableStyleLight8_14!!.setBand2V(cellstyle)
        }
        return tableStyleLight8_14
    }

    /**
     * Table Style Light 15-21
     * @param schemeColor
     * @return
     */
    private fun getTableStyleLight15_21(schemeColor: Int): SSTableStyle? {
        if (tableStyleLight15_21 == null) {
            tableStyleLight15_21 = SSTableStyle()
            //first row, last row
            var cellstyle = SSTableCellStyle(0xFFFFFFFF.toInt())
            cellstyle.setBorderColor(schemeColor)

            tableStyleLight15_21!!.setFirstRow(cellstyle)
            tableStyleLight15_21!!.setLastRow(cellstyle)

            //band1H, band1V
            var color = ColorUtil.instance().getColorWithTint(schemeColor, 0.8f.toDouble())
            cellstyle = SSTableCellStyle(color)
            cellstyle.setBorderColor(schemeColor)

            tableStyleLight15_21!!.setBand1H(cellstyle)
            tableStyleLight15_21!!.setBand1V(cellstyle)

            //band2H, band2V
            cellstyle = SSTableCellStyle(0xFFFFFFFF.toInt())
            cellstyle.setBorderColor(schemeColor)

            tableStyleLight15_21!!.setBand2H(cellstyle)
            tableStyleLight15_21!!.setBand2V(cellstyle)
        } else {
            //first row, last row
            var cellstyle = tableStyleLight15_21!!.getFirstRow()!!
            cellstyle.setBorderColor(schemeColor)
            tableStyleLight15_21!!.setFirstRow(cellstyle)
            tableStyleLight15_21!!.setLastRow(cellstyle)

            //band1H, band1V
            var color = ColorUtil.instance().getColorWithTint(schemeColor, 0.8f.toDouble())
            cellstyle = tableStyleLight15_21!!.getBand1H()!!
            cellstyle.setFillColor(color)
            cellstyle.setBorderColor(schemeColor)

            tableStyleLight15_21!!.setBand1H(cellstyle)
            tableStyleLight15_21!!.setBand1V(cellstyle)

            //band2H, band2V
            cellstyle = tableStyleLight15_21!!.getBand2H()!!
            cellstyle.setBorderColor(schemeColor)

            tableStyleLight15_21!!.setBand2H(cellstyle)
            tableStyleLight15_21!!.setBand2V(cellstyle)
        }
        return tableStyleLight15_21
    }

    /**
     * Table Style Medium 1-7
     * @param schemeColor
     * @return
     */
    private fun getTableStyleMedium1_7(schemeColor: Int): SSTableStyle? {
        if (tableStyleMedium1_7 == null) {
            tableStyleMedium1_7 = SSTableStyle()
            //header row
            var cellstyle = SSTableCellStyle(schemeColor)
            cellstyle.setFontColor(0xFFFFFFFF.toInt())
            cellstyle.setBorderColor(schemeColor)
            tableStyleMedium1_7!!.setFirstRow(cellstyle)

            //band1H, band1V
            var color = ColorUtil.instance().getColorWithTint(schemeColor, 0.8f.toDouble())
            cellstyle = SSTableCellStyle(color)
            cellstyle.setBorderColor(schemeColor)
            tableStyleMedium1_7!!.setBand1H(cellstyle)
            tableStyleMedium1_7!!.setBand1V(cellstyle)

            //band12H, band2V
            cellstyle = SSTableCellStyle(0xFFFFFFFF.toInt())
            cellstyle.setBorderColor(schemeColor)
            tableStyleMedium1_7!!.setBand2H(cellstyle)
            tableStyleMedium1_7!!.setBand2V(cellstyle)
        } else {
            var cellstyle = tableStyleMedium1_7!!.getFirstRow()!!
            cellstyle.setFillColor(schemeColor)
            cellstyle.setBorderColor(schemeColor)
            cellstyle.setFontColor(0xFFFFFFFF.toInt())
            tableStyleMedium1_7!!.setFirstRow(cellstyle)

            //band1H, band1V
            var color = ColorUtil.instance().getColorWithTint(schemeColor, 0.8f.toDouble())
            cellstyle = tableStyleMedium1_7!!.getBand1H()!!
            cellstyle.setFillColor(color)
            cellstyle.setBorderColor(schemeColor)

            tableStyleMedium1_7!!.setBand1H(cellstyle)
            tableStyleMedium1_7!!.setBand1V(cellstyle)

            //band12H, band2V
            cellstyle = tableStyleMedium1_7!!.getBand2H()!!
            cellstyle.setBorderColor(schemeColor)
            tableStyleMedium1_7!!.setBand2H(cellstyle)
            tableStyleMedium1_7!!.setBand2V(cellstyle)
        }

        return tableStyleMedium1_7
    }

    /**
     * Table Style Medium 8-14
     * @param schemeColor
     * @return
     */
    private fun getTableStyleMedium8_14(schemeColor: Int): SSTableStyle? {
        if (tableStyleMedium8_14 == null) {
            tableStyleMedium8_14 = SSTableStyle()
            //first row, last row, first col, last col
            var cellstyle = SSTableCellStyle(schemeColor)
            cellstyle.setBorderColor(0xFFFFFFFF.toInt())
            cellstyle.setFontColor(0xFFFFFFFF.toInt())
            tableStyleMedium8_14!!.setFirstRow(cellstyle)
            tableStyleMedium8_14!!.setFirstCol(cellstyle)
            tableStyleMedium8_14!!.setLastCol(cellstyle)
            tableStyleMedium8_14!!.setLastRow(cellstyle)

            //band1H, band1V
            var color = ColorUtil.instance().getColorWithTint(schemeColor, 0.6f.toDouble())
            cellstyle = SSTableCellStyle(color)
            cellstyle.setBorderColor(0xFFFFFFFF.toInt())
            tableStyleMedium8_14!!.setBand1H(cellstyle)
            tableStyleMedium8_14!!.setBand1V(cellstyle)

            //band12H, band2V
            color = ColorUtil.instance().getColorWithTint(schemeColor, 0.8f.toDouble())
            cellstyle = SSTableCellStyle(color)
            cellstyle.setBorderColor(0xFFFFFFFF.toInt())

            tableStyleMedium8_14!!.setBand2H(cellstyle)
            tableStyleMedium8_14!!.setBand2V(cellstyle)
        } else {
            var cellstyle = tableStyleMedium8_14!!.getFirstRow()!!
            cellstyle.setFillColor(schemeColor)
            cellstyle.setBorderColor(0xFFFFFFFF.toInt())
            cellstyle.setFontColor(0xFFFFFFFF.toInt())
            tableStyleMedium8_14!!.setFirstRow(cellstyle)
            tableStyleMedium8_14!!.setFirstCol(cellstyle)
            tableStyleMedium8_14!!.setLastCol(cellstyle)
            tableStyleMedium8_14!!.setLastRow(cellstyle)

            //band1H, band1V
            var color = ColorUtil.instance().getColorWithTint(schemeColor, 0.6f.toDouble())
            cellstyle = tableStyleMedium8_14!!.getBand1H()!!
            cellstyle.setFillColor(color)
            cellstyle.setBorderColor(0xFFFFFFFF.toInt())

            tableStyleMedium8_14!!.setBand1H(cellstyle)
            tableStyleMedium8_14!!.setBand1V(cellstyle)

            //band12H, band2V
            color = ColorUtil.instance().getColorWithTint(schemeColor, 0.8f.toDouble())
            cellstyle = tableStyleMedium8_14!!.getBand2H()!!
            cellstyle.setFillColor(color)
            cellstyle.setBorderColor(0xFFFFFFFF.toInt())

            tableStyleMedium8_14!!.setBand2H(cellstyle)
            tableStyleMedium8_14!!.setBand2V(cellstyle)
        }

        return tableStyleMedium8_14
    }

    /**
     * Table Style Medium 15-21
     * @param schemeColor
     * @return
     */
    private fun getTableStyleMedium15_21(schemeColor: Int): SSTableStyle? {
        if (tableStyleMedium15_21 == null) {
            tableStyleMedium15_21 = SSTableStyle()
            //first row, first col, last col
            var cellstyle = SSTableCellStyle(schemeColor)
            cellstyle.setFontColor(0xFFFFFFFF.toInt())
            tableStyleMedium15_21!!.setFirstRow(cellstyle)
            tableStyleMedium15_21!!.setFirstCol(cellstyle)
            tableStyleMedium15_21!!.setLastCol(cellstyle)

            //band1H, band1V
            cellstyle = SSTableCellStyle(0xFFD8D8D8.toInt())
            tableStyleMedium15_21!!.setBand1H(cellstyle)
            tableStyleMedium15_21!!.setBand1V(cellstyle)

            //band12H, band2V
            cellstyle = SSTableCellStyle(0xFFFFFFFF.toInt())
            tableStyleMedium15_21!!.setBand2H(cellstyle)
            tableStyleMedium15_21!!.setBand2V(cellstyle)
        } else {
            var cellStyle = tableStyleMedium15_21!!.getFirstRow()!!
            cellStyle.setFillColor(schemeColor)

            tableStyleMedium15_21!!.setFirstCol(cellStyle)
            tableStyleMedium15_21!!.setLastCol(cellStyle)
        }

        return tableStyleMedium15_21
    }

    private fun getTableStyleMedium22_28(schemeColor: Int): SSTableStyle? {
        if (tableStyleMedium22_28 == null) {
            tableStyleMedium22_28 = SSTableStyle()
            // first col, band1H, band1V
            var color = ColorUtil.instance().getColorWithTint(schemeColor, 0.6f.toDouble())
            var cellstyle = SSTableCellStyle(color)
            cellstyle.setBorderColor(schemeColor)
            tableStyleMedium22_28!!.setBand1H(cellstyle)
            tableStyleMedium22_28!!.setBand1V(cellstyle)

            //last row, first row, band12H, band2V
            color = ColorUtil.instance().getColorWithTint(schemeColor, 0.8f.toDouble())
            cellstyle = SSTableCellStyle(color)
            cellstyle.setBorderColor(schemeColor)
            tableStyleMedium22_28!!.setFirstRow(cellstyle)
            tableStyleMedium22_28!!.setLastRow(cellstyle)
            tableStyleMedium22_28!!.setBand2H(cellstyle)
            tableStyleMedium22_28!!.setBand2V(cellstyle)
        } else {

            var color = ColorUtil.instance().getColorWithTint(schemeColor, 0.6f.toDouble())
            var cellstyle = tableStyleMedium22_28!!.getBand1H()!!
            cellstyle.setFillColor(color)
            cellstyle.setBorderColor(schemeColor)

            tableStyleMedium22_28!!.setBand1H(cellstyle)
            tableStyleMedium22_28!!.setBand1V(cellstyle)

            //last row, first row, band12H, band2V
            color = ColorUtil.instance().getColorWithTint(schemeColor, 0.8f.toDouble())
            cellstyle = tableStyleMedium22_28!!.getFirstRow()!!
            cellstyle.setFillColor(color)
            cellstyle.setBorderColor(schemeColor)

            tableStyleMedium22_28!!.setFirstRow(cellstyle)
            tableStyleMedium22_28!!.setLastRow(cellstyle)
            tableStyleMedium22_28!!.setBand2H(cellstyle)
            tableStyleMedium22_28!!.setBand2V(cellstyle)
        }

        return tableStyleMedium22_28
    }

    private fun getTableStyleDark1_7(schemeColor: Int): SSTableStyle? {
        if (tableStyleDark1_7 == null) {
            tableStyleDark1_7 = SSTableStyle()
            //first row
            var cellstyle = SSTableCellStyle(0xFF000000.toInt())
            cellstyle.setFontColor(0xFFFFFFFF.toInt())
            cellstyle.setBorderColor(0xFFFFFFFF.toInt())
            tableStyleDark1_7!!.setFirstRow(cellstyle)

            //last row
            var color = 0
            if ((schemeColor and 0xFFFFFF) == 0) {
                color = ColorUtil.instance().getColorWithTint(schemeColor, 0.15f.toDouble())
            } else {
                color = ColorUtil.instance().getColorWithTint(schemeColor, -0.5f.toDouble())
            }
            cellstyle = SSTableCellStyle(color)
            cellstyle.setFontColor(0xFFFFFFFF.toInt())
            cellstyle.setBorderColor(0xFFFFFFFF.toInt())
            tableStyleDark1_7!!.setLastRow(cellstyle)

            //band1H, band1V
            if ((schemeColor and 0xFFFFFF) == 0) {
                color = ColorUtil.instance().getColorWithTint(schemeColor, 0.25f.toDouble())
            } else {
                color = ColorUtil.instance().getColorWithTint(schemeColor, -0.25f.toDouble())
            }
            cellstyle = SSTableCellStyle(color)
            cellstyle.setFontColor(0xFFFFFFFF.toInt())
            tableStyleDark1_7!!.setBand1H(cellstyle)
            tableStyleDark1_7!!.setBand1V(cellstyle)

            //band2H, band2V
            if ((schemeColor and 0xFFFFFF) == 0) {
                color = ColorUtil.instance().getColorWithTint(schemeColor, 0.5f.toDouble())
            } else {
                color = schemeColor
            }
            cellstyle = SSTableCellStyle(color)
            cellstyle.setFontColor(0xFFFFFFFF.toInt())
            tableStyleDark1_7!!.setBand2H(cellstyle)
            tableStyleDark1_7!!.setBand2V(cellstyle)
        } else {
            //last row
            var color = 0
            if ((schemeColor and 0xFFFFFF) == 0) {
                color = ColorUtil.instance().getColorWithTint(schemeColor, 0.15f.toDouble())
            } else {
                color = ColorUtil.instance().getColorWithTint(schemeColor, -0.5f.toDouble())
            }
            var cellstyle = tableStyleDark1_7!!.getLastRow()!!
            cellstyle.setFillColor(color)
            tableStyleDark1_7!!.setLastRow(cellstyle)

            //band1H, band1V
            if ((schemeColor and 0xFFFFFF) == 0) {
                color = ColorUtil.instance().getColorWithTint(schemeColor, 0.25f.toDouble())
            } else {
                color = ColorUtil.instance().getColorWithTint(schemeColor, -0.25f.toDouble())
            }
            cellstyle = tableStyleDark1_7!!.getBand1H()!!
            cellstyle.setFillColor(color)

            tableStyleDark1_7!!.setBand1H(cellstyle)
            tableStyleDark1_7!!.setBand1V(cellstyle)

            //band2H, band2V
            if ((schemeColor and 0xFFFFFF) == 0) {
                color = ColorUtil.instance().getColorWithTint(schemeColor, 0.5f.toDouble())
            } else {
                color = schemeColor
            }
            cellstyle = tableStyleDark1_7!!.getBand2H()!!
            cellstyle.setFillColor(color)

            tableStyleDark1_7!!.setBand2H(cellstyle)
            tableStyleDark1_7!!.setBand2V(cellstyle)
        }

        return tableStyleDark1_7
    }

    /**
     *
     * @return
     */
    private fun getTableStyleDark8(): SSTableStyle? {
        if (tableStyleDark8 == null) {
            tableStyleDark8 = SSTableStyle()
            //first row
            var cellstyle = SSTableCellStyle(0xFF000000.toInt())
            cellstyle.setFontColor(0xFFFFFFFF.toInt())
            tableStyleDark8!!.setFirstRow(cellstyle)

            //band1H, band1V
            cellstyle = SSTableCellStyle(0xFFA5A5A5.toInt())
            tableStyleDark8!!.setBand1H(cellstyle)
            tableStyleDark8!!.setBand1V(cellstyle)

            //band2H, band2V
            cellstyle = SSTableCellStyle(0xFFD8D8D8.toInt())
            tableStyleDark8!!.setBand2H(cellstyle)
            tableStyleDark8!!.setBand2V(cellstyle)
        }

        return tableStyleDark8
    }

    /**
     * Table Style Dark 9-11
     * @param headerColor
     * @param bodyColor
     * @return
     */
    private fun getTableStyleDark9_11(headerColor: Int, bodyColor: Int): SSTableStyle? {
        if (tableStyleDark9_11 == null) {
            tableStyleDark9_11 = SSTableStyle()
            //first row
            var cellstyle = SSTableCellStyle(headerColor)
            cellstyle.setFontColor(0xFFFFFFFF.toInt())
            tableStyleDark9_11!!.setFirstRow(cellstyle)

            //band1H, band1V
            var color = ColorUtil.instance().getColorWithTint(bodyColor, 0.6f.toDouble())
            cellstyle = SSTableCellStyle(color)
            tableStyleDark9_11!!.setBand1H(cellstyle)
            tableStyleDark9_11!!.setBand1V(cellstyle)

            //band2H, band2V
            color = ColorUtil.instance().getColorWithTint(bodyColor, 0.8f.toDouble())
            cellstyle = SSTableCellStyle(color)
            tableStyleDark9_11!!.setBand2H(cellstyle)
            tableStyleDark9_11!!.setBand2V(cellstyle)
        } else {
            var cellstyle = tableStyleDark9_11!!.getFirstRow()!!
            cellstyle.setFillColor(headerColor)
            tableStyleDark9_11!!.setFirstRow(cellstyle)

            //band1H, band1V
            var color = ColorUtil.instance().getColorWithTint(bodyColor, 0.6f.toDouble())
            cellstyle = tableStyleDark9_11!!.getBand1H()!!
            cellstyle.setFillColor(color)
            tableStyleDark9_11!!.setBand1H(cellstyle)
            tableStyleDark9_11!!.setBand1V(cellstyle)

            //band2H, band2V
            color = ColorUtil.instance().getColorWithTint(bodyColor, 0.8f.toDouble())
            cellstyle = tableStyleDark9_11!!.getBand2H()!!
            cellstyle.setFillColor(color)

            tableStyleDark9_11!!.setBand2H(cellstyle)
            tableStyleDark9_11!!.setBand2V(cellstyle)
        }

        return tableStyleDark9_11
    }

    /**
     *
     */
    fun dispose() {
    }

    companion object {
        /**
         * scheme color name
         */
        private val schemeClrName = arrayOf(
            SchemeClrConstant.SCHEME_ACCENT1,
            SchemeClrConstant.SCHEME_ACCENT2,
            SchemeClrConstant.SCHEME_ACCENT3,
            SchemeClrConstant.SCHEME_ACCENT4,
            SchemeClrConstant.SCHEME_ACCENT5,
            SchemeClrConstant.SCHEME_ACCENT6
        )
    }
}
