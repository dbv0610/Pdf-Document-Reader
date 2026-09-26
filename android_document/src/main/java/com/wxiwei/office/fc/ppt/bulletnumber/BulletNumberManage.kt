/*
 * 文件名称:           BulletNumberManage.java
 *  
 * 编译器:             android2.2
 * 时间:               下午2:54:16
 */
package com.wxiwei.office.fc.ppt.bulletnumber

import com.wxiwei.office.common.bulletnumber.ListKit
import com.wxiwei.office.fc.dom4j.Element
import com.wxiwei.office.system.IControl

/**
 * TODO: 文件注释
 * 
 * 
 * 
 * 
 * Read版本:       Read V1.0
 * 
 * 
 * 作者:           jhy1790
 * 
 * 
 * 日期:           2012-7-4
 * 
 * 
 * 负责人:         jhy1790
 * 
 * 
 * 负责小组:
 * 
 * 
 * 
 * 
 */
class BulletNumberManage {
    /**
     * 
     * @return
     */
    fun getBulletID(styleID: Int): Int {
        val id = styleBulletIDs!!.get(styleID)
        if (id != null) {
            return id
        }
        return -1
    }

    /**
     * 
     * @param styleID
     * @param pPr
     * @return
     */
    fun addBulletNumber(control: IControl, styleID: Int, pPr: Element?): Int {
        var id = styleBulletIDs!!.get(styleID)
        if (id != null) {
            return id
        } else {
            val text = getBulletText(pPr)
            if (text != null) {
                id = bulletIDs!!.get(text)
                if (id != null) {
                    if (styleID > 0) {
                        styleBulletIDs!!.put(styleID, id)
                    }
                    return id
                } else {
                    id = control.getSysKit().getPGBulletText().addBulletText(text!!)
                    bulletIDs!!.put(text, id)
                    if (styleID > 0) {
                        styleBulletIDs!!.put(styleID, id)
                    }
                    return id
                }
            } else {
                if (pPr != null && pPr.element("buNone") != null) {
                    // use -2 representing having indicated no bullet number
                    styleBulletIDs!!.put(styleID, -2)
                }
            }
        }
        return -1
    }

    /**
     * 
     * @param pPr
     * @return
     */
    private fun getBulletText(pPr: Element?): String? {
        if (pPr != null && pPr.element("buNone") == null) {
            var lvl = 0
            var `val`: String? = null
            if (pPr != null && pPr.attribute("lvl") != null) {
                `val` = pPr.attributeValue("lvl")
                if (`val` != null && `val`.length > 0) {
                    lvl = `val`.toInt()
                }
            }

            var temp = pPr.element("buAutoNum")
            if (temp != null) {
                var startAt = 1
                if (temp.attribute("startAt") != null) {
                    `val` = temp.attributeValue("startAt")
                    if (`val` != null && `val`.length > 0) {
                        startAt = `val`.toInt()
                    }
                }
                return getText(lvl, convertedNumberFormat(temp.attributeValue("type")), startAt)
            } else if ((pPr.element("buBlip").also { temp = it }) != null) {
                //bullet is picture, replace it by dot(用实心圆点代替)
                if (temp!!.element("blip") != null && temp.element("blip")
                        .attributeValue("embed") != null
                ) {
                    var c = 'l'
                    c = converterNumberChar(c.code)

                    val beforFmt = lvlFmt!!.get(lvl)
                    if (beforFmt == null || beforFmt != c.code) {
                        if (beforFmt != null && lvl == 0) {
                            lvlFmt!!.clear()
                            lvlStartAt!!.clear()
                            lvlNum!!.clear()
                        }
                        lvlFmt!!.put(lvl, c.code)
                    }
                    return c.toString()
                }
            } else if ((pPr.element("buChar").also { temp = it }) != null) {
                if (temp!!.attribute("char") != null) {
                    `val` = temp.attributeValue("char")
                    if (`val` != null && `val`.length > 0) {
                        var c = `val`.get(0)
                        c = converterNumberChar(c.code)

                        val beforFmt = lvlFmt!!.get(lvl)
                        if (beforFmt == null || beforFmt != c.code) {
                            if (beforFmt != null && lvl == 0) {
                                lvlFmt!!.clear()
                                lvlStartAt!!.clear()
                                lvlNum!!.clear()
                            }
                            lvlFmt!!.put(lvl, c.code)
                        }
                        return c.toString()
                    }
                }
            }
        }
        return null
    }

    /**
     * 
     * @param lvl
     * @param type
     * @param start
     * @return
     */
    private fun getText(lvl: Int, type: Int, start: Int): String {
        var start = start
        val bulletBuffer = StringBuffer()
        val beforType = lvlFmt!!.get(lvl)
        if (beforType == null || beforType != type) {
            if (beforType != null && lvl == 0) {
                lvlFmt!!.clear()
                lvlStartAt!!.clear()
                lvlNum!!.clear()
            }
            lvlFmt!!.put(lvl, type)
            lvlStartAt!!.put(lvl, start)
            lvlNum!!.put(lvl, start)
        } else {
            val beforStart = lvlStartAt!!.get(lvl)
            if (beforStart == null || beforStart != start) {
                lvlStartAt!!.put(lvl, start)
                lvlNum!!.put(lvl, start)
            } else {
                start = lvlNum!!.get(lvl)!! + 1
                lvlNum!!.put(lvl, start)
            }
        }
        var numID = type
        if (numID == 5) {
            numID = 0
        } else if (numID == 6 || numID == 11) {
            numID = 0
        } else if (numID == 7 || numID == 12) {
            numID = 1
        } else if (numID == 8 || numID == 13) {
            numID = 2
        } else if (numID == 9 || numID == 14) {
            numID = 3
        } else if (numID == 10 || numID == 15) {
            numID = 4
        }
        // text
        if (type >= 11 && type <= 15) {
            bulletBuffer.append("(")
        }
        bulletBuffer.append(ListKit.instance().getNumberStr(start, numID))
        if (type >= 6 && type <= 15) {
            bulletBuffer.append(")")
        } else if (type != 5) {
            bulletBuffer.append(".")
        }
        return bulletBuffer.toString()
    }

    /**
     * 
     * @param c
     * @return
     */
    fun addBulletNumber(control: IControl, lvl: Int, type: Int, start: Int, c: Char): Int {
        var text: String? = null
        //        if (type >= 0)
//        {
//            text = getText(lvl, convertedNumberFormat(type), start);
//        }
//        else
        run {
            text = converterNumberChar(c.code).toString()
        }
        var id = bulletIDs!!.get(text)
        if (id != null) {
            return id
        } else {
            id = control.getSysKit().getPGBulletText().addBulletText(text!!)
            bulletIDs!!.put(text, id)
            return id
        }
    }

    /**
     * = 0    arabicPeriod                      3            1.、2.、3.、...
     * = 1    romanUcPeriod                     7            I.、II.、III.、...
     * = 2    romanLcPeriod                     6            i.、ii.、iii.、...
     * = 3    alphaUcPeriod                     1            A.、B.、C.、...
     * = 4    alphaLcPeriod                     0            a.、b.、c.、...
     * = 39   ea1JpnChsDbPeriod                 38           一.、二.、三.、...
     * = 5    circleNumDbPlain/arabicPlain      18/13        1、2、3、...
     * = 6    arabicParenR                      2            1)、2)、3)、...
     * = 7    romanUcParenR                     15           I)、II)、III)、...
     * = 8    romanLcParenR                     5            i)、ii)、iii)、...
     * = 9    alphaUcParenR                     11           A)、B)、C)、...
     * = 10   alphaLcParenR                     9            a)、b)、c)、...
     * = 11   arabicParenBoth                   12           (1)、(2)、(3)、...
     * = 12   romanUcParentBoth                 14           (I)、(II)、(III)、...
     * = 13   romanLcParenBoth                  4            (i)、(ii)、(iii)、...
     * = 14   alphaUcParenBoth                  10           (A)、(B)、(C)、...
     * = 15   alphaLcParenBoth                  8            (a)、(b)、(c)、...
     */
    private fun convertedNumberFormat(numFormat: String?): Int {
        if ("arabicPeriod".equals(numFormat, ignoreCase = true)) {
            return 0
        } else if ("romanUcPeriod".equals(numFormat, ignoreCase = true)) {
            return 1
        } else if ("romanLcPeriod".equals(numFormat, ignoreCase = true)) {
            return 2
        } else if ("alphaUcPeriod".equals(numFormat, ignoreCase = true)) {
            return 3
        } else if ("alphaLcPeriod".equals(numFormat, ignoreCase = true)) {
            return 4
        } else if ("arabicPlain".equals(numFormat, ignoreCase = true)
            || "circleNumDbPlain".equals(numFormat, ignoreCase = true)
        ) {
            return 5
        } else if ("arabicParenR".equals(numFormat, ignoreCase = true)) {
            return 6
        } else if ("romanUcParenR".equals(numFormat, ignoreCase = true)) {
            return 7
        } else if ("romanLcParenR".equals(numFormat, ignoreCase = true)) {
            return 8
        } else if ("alphaUcParenR".equals(numFormat, ignoreCase = true)) {
            return 9
        } else if ("alphaLcParenR".equals(numFormat, ignoreCase = true)) {
            return 10
        } else if ("arabicParenBoth".equals(numFormat, ignoreCase = true)) {
            return 11
        } else if ("romanUcParentBoth".equals(numFormat, ignoreCase = true)) {
            return 12
        } else if ("romanLcParenBoth".equals(numFormat, ignoreCase = true)) {
            return 13
        } else if ("alphaUcParenBoth".equals(numFormat, ignoreCase = true)) {
            return 14
        } else if ("alphaLcParenBoth".equals(numFormat, ignoreCase = true)) {
            return 15
        } else if ("ea1JpnChsDbPeriod".equals(numFormat, ignoreCase = true)) {
            return 39
        }
        return 0
    }

    /**
     * = 0    arabicPeriod                      3            1.、2.、3.、...
     * = 1    romanUcPeriod                     7            I.、II.、III.、...
     * = 2    romanLcPeriod                     6            i.、ii.、iii.、...
     * = 3    alphaUcPeriod                     1            A.、B.、C.、...
     * = 4    alphaLcPeriod                     0            a.、b.、c.、...
     * = 39   ea1JpnChsDbPeriod                 38           一.、二.、三.、...
     * = 5    circleNumDbPlain/arabicPlain      18/13        1、2、3、...
     * = 6    arabicParenR                      2            1)、2)、3)、...
     * = 7    romanUcParenR                     15           I)、II)、III)、...
     * = 8    romanLcParenR                     5            i)、ii)、iii)、...
     * = 9    alphaUcParenR                     11           A)、B)、C)、...
     * = 10   alphaLcParenR                     9            a)、b)、c)、...
     * = 11   arabicParenBoth                   12           (1)、(2)、(3)、...
     * = 12   romanUcParentBoth                 14           (I)、(II)、(III)、...
     * = 13   romanLcParenBoth                  4            (i)、(ii)、(iii)、...
     * = 14   alphaUcParenBoth                  10           (A)、(B)、(C)、...
     * = 15   alphaLcParenBoth                  8            (a)、(b)、(c)、...
     */
    private fun convertedNumberFormat(type: Int): Int {
        var fmt = 0
        when (type) {
            0 -> fmt = 4
            1 -> fmt = 3
            2 -> fmt = 6
            3 -> fmt = 0
            4 -> fmt = 13
            5 -> fmt = 8
            6 -> fmt = 2
            7 -> fmt = 1
            8 -> fmt = 15
            9 -> fmt = 10
            10 -> fmt = 14
            11 -> fmt = 9
            12 -> fmt = 11
            13, 18 -> fmt = 5
            14 -> fmt = 12
            15 -> fmt = 7
            38 -> fmt = 39
            else -> fmt = 0
        }
        return fmt
    }

    /**
     * 
     * @param c
     * @return
     */
    private fun converterNumberChar(c: Int): Char {
        var c = c
        if (c == 0x2022 || c == 0x006C || c == 0x0070) {
            c = 0x25CF
        } else if (c == 0x006E || c == 0x00A7) {
            c = 0x25A0
        } else if (c == 0x0075) {
            c = 0x25C6
        } else if (c == 0x00FC) {
            c = 0x221A
        } else if (c == 0x00D8) {
            c = 0x2605
        } else if (c != 0x2013) {
            c = 0x25CF
        }
        return c.toChar()
    }

    /**
     * 
     */
    fun clearData() {
        if (lvlFmt != null) {
            lvlFmt!!.clear()
        }
        if (lvlStartAt != null) {
            lvlStartAt!!.clear()
        }
        if (lvlNum != null) {
            lvlNum!!.clear()
        }
    }

    /**
     * 
     */
    fun dispose() {
        if (lvlFmt != null) {
            lvlFmt!!.clear()
            lvlFmt = null
        }
        if (lvlStartAt != null) {
            lvlStartAt!!.clear()
            lvlStartAt = null
        }
        if (lvlNum != null) {
            lvlNum!!.clear()
            lvlNum = null
        }
        if (styleBulletIDs != null) {
            styleBulletIDs!!.clear()
            styleBulletIDs = null
        }
        if (bulletIDs != null) {
            bulletIDs!!.clear()
            bulletIDs = null
        }
        kit = null
    }

    // lvl, format
    private var lvlFmt: MutableMap<Int?, Int?>?

    // lvl, start
    private var lvlStartAt: MutableMap<Int?, Int?>?

    // lvl, number now
    private var lvlNum: MutableMap<Int?, Int?>?

    // styleID, bulletText ID
    private var styleBulletIDs: MutableMap<Int?, Int?>?

    // styleID, bulletText ID
    private var bulletIDs: MutableMap<String?, Int?>?

    //
    init {
        lvlFmt = HashMap<Int?, Int?>()
        lvlStartAt = HashMap<Int?, Int?>()
        lvlNum = HashMap<Int?, Int?>()
        styleBulletIDs = HashMap<Int?, Int?>()
        bulletIDs = HashMap<String?, Int?>()
    }

    companion object {
        private var kit: BulletNumberManage? = null

        /**
         * 
         */
        @JvmStatic
        fun instance(): BulletNumberManage {
            if (kit == null) {
                kit = BulletNumberManage()
            }
            return kit!!
        }
    }
}
