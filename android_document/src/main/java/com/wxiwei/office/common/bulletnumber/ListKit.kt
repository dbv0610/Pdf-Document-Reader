/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
/*
 * 文件名称:          ListKit.java
 *  
 * 编译器:            android2.2
 * 时间:              下午2:00:00
 */
package com.wxiwei.office.common.bulletnumber

import com.wxiwei.office.constant.wp.WPViewConstant
import com.wxiwei.office.simpletext.view.DocAttr
import java.util.Locale

/**
 * bullet and number kit
 * 
 * 
 * 
 * 
 * Read版本:        Read V1.0
 * 
 * 
 * 作者:            ljj8494
 * 
 * 
 * 日期:            2012-6-20
 * 
 * 
 * 负责人:          ljj8494
 * 
 * 
 * 负责小组:
 * 
 * 
 * 
 * 
 */
class ListKit {
    /**
     * @param
     * = 0    decimal                           1、2、3、...
     * = 1    upperRoman                        I、II、III、...
     * = 2    lowerRoman                        i、ii、iii、...
     * = 3    upperLetter                       A、B、C、...
     * = 4    lowerLetter                       a、b、c、...
     * = 39   chineseCountingThousand           一、二、三、...
     * = 38   chineseLegalSimplified            壹、贰、叁、...
     * = 30   ideographTraditional              甲、乙、丙、...
     * = 31   ideographZodiac                   子、丑、寅、...
     * = 5    ordinal                           1st、2st、3st、...
     * = 6    cardinalText                      one、two、three、...
     * = 7    ordinalText                       First、Second、Third、...
     * = 22   decimalZero                       01、02、03、...
     */
    fun getNumberStr(num: Int, style: Int): String? {
        when (style) {
            1 -> return getRoman(num).uppercase(Locale.getDefault())

            2 -> return getRoman(num)

            3 -> return getLetters(num).uppercase(Locale.getDefault())

            4 -> return getLetters(num)

            5 -> return getOrdinal(num)

            6 -> return getCardinalText(num)

            22 -> return (if (num < 10) "0" else "") + num.toString()

            30 -> return if (num <= 10) TRADITIONAL[num - 1] else num.toString()

            31 -> return if (num <= 12) ZODIAC[num - 1] else num.toString()

            38 -> return getChineseLegalSimplified(num)

            39 -> return getChineseCountingThousand(num)

            0 -> return num.toString()
            else -> return num.toString()
        }
    }

    /**
     * 
     * @param number
     * @return
     */
    fun getLetters(number: Int): String {
        val base = 26
        if (number <= 0 || number > 780) {
            return ENGLISH_LETTERS[0].toString()
        }
        if (number <= base) {
            return ENGLISH_LETTERS[number - 1].toString()
        }

        val sb = StringBuilder()
        val t = number / base
        var mod = number % base
        mod = if (mod == 0) 26 else mod
        for (i in 0..<t) {
            sb.append(ENGLISH_LETTERS[mod - 1])
        }
        return sb.toString()
    }

    /**
     * 
     * @param number
     * @return
     */
    fun getRoman(number: Int): String {
        var number = number
        if (number <= 0) {
            return ROMAN_LETTERS[ROMAN_LETTERS.size - 1]
        }
        val sb = StringBuilder()
        for (i in ROMAN_LETTERS.indices) {
            val letter: String = ROMAN_LETTERS[i]
            val value: Int = ROMAN_VALUES[i]
            while (number >= value) {
                number -= value
                sb.append(letter)
            }
        }
        return sb.toString()
    }

    /**
     * 
     * @return
     */
    fun getChineseLegalSimplified(number: Int): String {
        if (number <= 0 || number > 99999) {
            return CN_SIMPLIFIED[0].toString()
        }
        if (number <= 9) {
            return CN_SIMPLIFIED[number].toString()
        }
        val sb = StringBuilder()
        val numStr = number.toString()
        val len = numStr.length
        var isAddZero = false
        for (i in 0..<len) {
            val t = numStr.get(i).code - 0x30
            if (t > 0) {
                sb.append(CN_SIMPLIFIED[t])
                if (len - i - 2 >= 0) {
                    sb.append(CN_SIMPLIFIED_SERIES[len - i - 2])
                }
                isAddZero = true
            } else if (isAddZero && i != len - 1) {
                sb.append(CN_SIMPLIFIED[0])
                isAddZero = false
            }
        }
        // delete last "零"
        if (sb.get(sb.length - 1) == CN_SIMPLIFIED[0]) {
            sb.deleteCharAt(sb.length - 1)
        }

        return sb.toString()
    }

    /**
     * 
     * @param number
     * @return
     */
    fun getChineseCountingThousand(number: Int): String {
        if (number <= 0 || number > 99999) {
            return CN_THOUSAND[0].toString()
        }
        if (number <= 9) {
            return CN_THOUSAND[number].toString()
        }
        val sb = StringBuilder()
        val numStr = number.toString()
        val len = numStr.length
        var isAddZero = false
        for (i in 0..<len) {
            val t = numStr.get(i).code - 0x30
            if (t > 0) {
                sb.append(CN_THOUSAND[t])
                if (len - i - 2 >= 0) {
                    sb.append(CN_THOUSAND_SERIES[len - i - 2])
                }
                isAddZero = true
            } else if (isAddZero && i != len - 1) {
                sb.append(CN_THOUSAND[0])
                isAddZero = false
            }
        }
        // delete last "〇"
        if (sb.get(sb.length - 1) == CN_THOUSAND[0]) {
            sb.deleteCharAt(sb.length - 1)
        }
        // delete first "一"
        if (number > 10 && number < 20) {
            if (sb.get(0) == CN_THOUSAND[1]) {
                sb.deleteCharAt(0)
            }
        }

        return sb.toString()
    }

    /**
     * 
     * @param number
     * @return
     */
    fun getOrdinal(number: Int): String {
        val t = number % 10
        var suff = ""
        if (t == 1) {
            suff = "st"
        } else if (t == 2) {
            suff = "nd"
        } else if (t == 3) {
            suff = "rd"
        } else {
            suff = "th"
        }
        return number.toString() + suff
    }


    /**
     * 
     * @param listLevel
     * @return
     */
    fun getBulletText(
        listData: ListData,
        listLevel: ListLevel,
        docAttr: DocAttr,
        currentLevel: Int
    ): String {
        if (listLevel.numberText == null) {
            return ""
        }
        val bulletBuffer = StringBuffer()
        val xst = listLevel.numberText ?: return ""
        for (ch in xst) {
            if (ch.code >= 0 && ch.code < 9) {
                val numLevel = listData.getLevel(ch.code) ?: continue
                var num = numLevel.startAt +
                        (if (docAttr.rootType.toShort() == WPViewConstant.NORMAL_ROOT) numLevel.normalParaCount else numLevel.paraCount)

                if (ch.code < currentLevel && num > numLevel.startAt) {
                    num--
                }
                bulletBuffer.append(getNumberStr(num, numLevel.numberFormat))
            } else {
                bulletBuffer.append(ch)
            }
        }
        val follow = listLevel.followChar
        when (follow.toInt()) {
            1 -> bulletBuffer.append(" ")
            else -> {}
        }
        return bulletBuffer.toString()
    }

    fun getCardinalText(num: Int): String {
        val numberStr = num.toString()
        val lStr = numberStr // 没有小数点的情况
        var lStrRev = reverseString(lStr) // 对左边的字串取反字串
        val a = Array(5) { "" } // 定义5个字串变量用来存放解析出的三位一组的字串
        when (lStrRev.length % 3) {
            1 -> lStrRev = lStrRev + "00"
            2 -> lStrRev = lStrRev + "0"
            else -> {}
        }
        var StrInt = ""
        for (i in 0..lStrRev.length / 3 - 1)  // 计算有多少个三位
        {
            a[i] = reverseString(lStrRev.substring(3 * i, 3 * i + 3)) // 截取第1个三位
            if (a[i] != "000")  // 用来避免这种情况“1000000=ONE MILLION THOUSAND ONLY”
            {
                if (i != 0) {
                    StrInt = (w3(a[i]!!) + " " + dw(i.toString()) + " "
                            + StrInt) // 用来加上“THOUSAND
                    // OR
                    // MILLION
                    // OR
                    // BILLION”
                } else {
                    StrInt = w3(a[i]!!) // 防止i=0时“lm=w3(a(i))+" "+dw(i)+" "+lm”多加两个尾空格
                }
            } else {
                StrInt = w3(a[i]!!) + StrInt
            }
        }
        return toUpperCaseFirstOne(StrInt)
    }

    // 将字符串反置
    private fun reverseString(str: String): String {
        var str = str
        val lenInt = str.length
        val z = arrayOfNulls<String>(str.length)
        for (i in 0..<lenInt) {
            z[i] = str.substring(i, i + 1)
        }
        str = ""
        for (i in lenInt - 1 downTo 0) {
            str = str + z[i]
        }
        return str
    }

    private fun zr4(y: String): String? {
        val z = Array(10) { "" }
        z[0] = ""
        z[1] = "one"
        z[2] = "two"
        z[3] = "three"
        z[4] = "four"
        z[5] = "five"
        z[6] = "six"
        z[7] = "seven"
        z[8] = "eight"
        z[9] = "nine"
        return z[y.substring(0, 1).toInt()]
    }

    private fun zr3(y: String): String {
        val z: Array<String> = Array(10) { "" }
        z[0] = ""
        z[1] = "one"
        z[2] = "two"
        z[3] = "three"
        z[4] = "four"
        z[5] = "five"
        z[6] = "six"
        z[7] = "seven"
        z[8] = "eight"
        z[9] = "nine"
        return z[y.substring(2, 3).toInt()]
    }

    private fun zr2(y: String): String {
        val z: Array<String> = Array(20) { "" }
        z[10] = "ten"
        z[11] = "eleven"
        z[12] = "twelve"
        z[13] = "thirteen"
        z[14] = "fourteen"
        z[15] = "fifteen"
        z[16] = "sixteen"
        z[17] = "seventeen"
        z[18] = "eighteen"
        z[19] = "nineteen"
        return z[y.substring(1, 3).toInt()]
    }

    private fun zr1(y: String): String {
        val z: Array<String> = Array(10) { "" }
        z[1] = "ten"
        z[2] = "twenty"
        z[3] = "thirty"
        z[4] = "forty"
        z[5] = "fifty"
        z[6] = "sixty"
        z[7] = "seventy"
        z[8] = "eighty"
        z[9] = "ninety"
        return z[y.substring(1, 2).toInt()]
    }

    private fun dw(y: String): String? {
        val z = Array(5) { "" }
        z[0] = ""
        z[1] = "thousand"
        z[2] = "million"
        z[3] = "billion"
        return z[y.toInt()]
    }

    // 用来制作2位数字转英文
    private fun w2(y: String): String {
        val tempstr: String
        if (y.substring(1, 2) == "0")  // 判断是否小于十
        {
            tempstr = zr3(y)
        } else if (y.substring(1, 2) == "1")  // 判断是否在十到二十之间
        {
            tempstr = zr2(y)
        } else {
            if (y.substring(2, 3) == "0")  // 判断是否为大于二十小于一百的能被十整除的数（为了去掉尾空格）
            {
                tempstr = zr1(y)
            } else {
                tempstr = zr1(y) + "-" + zr3(y)
            }
        }
        return tempstr
    }

    private fun w3(y: String): String {
        val tempstr: String
        if (y.substring(0, 1) == "0")  // 判断是否小于一百
        {
            tempstr = w2(y)
        } else {
            if (y.substring(1, 3) == "00")  // 判断是否能被一百整除
            {
                tempstr = zr4(y) + " " + "hundred"
            } else {
                tempstr = zr4(y) + " " + "hundred" + " " + w2(y) // 不能整除的要后面加“AND”
            }
        }
        return tempstr
    }

    fun toUpperCaseFirstOne(s: String): String {
        if (s == "") {
            return 0.toString()
        } else {
            return (StringBuilder())
                .append(s.get(0).uppercaseChar())
                .append(s.substring(1)).toString()
        }
    }

    companion object {
        // letter
        private val ENGLISH_LETTERS = charArrayOf(
            'a',
            'b',
            'c',
            'd',
            'e',
            'f',
            'g',
            'h',
            'i',
            'j',
            'k',
            'l',
            'm',
            'n',
            'o',
            'p',
            'q',
            'r',
            's',
            't',
            'u',
            'v',
            'w',
            'x',
            'y',
            'z'
        )

        // ROMAN
        private val ROMAN_LETTERS = arrayOf<String>(
            "m", "cm", "d", "cd", "c", "xc", "l", "xl", "x",
            "ix", "v", "iv", "i"
        )
        private val ROMAN_VALUES = intArrayOf(1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1)

        // chineseLegalSimplified
        private val CN_SIMPLIFIED =
            charArrayOf('零', '壹', '贰', '叁', '肆', '伍', '陆', '柒', '捌', '玖')
        private val CN_SIMPLIFIED_SERIES = charArrayOf('拾', '佰', '仟', '萬')

        // chineseCountingThousand
        private val CN_THOUSAND =
            charArrayOf('〇', '一', '二', '三', '四', '五', '六', '七', '八', '九', '十')
        private val CN_THOUSAND_SERIES = charArrayOf('十', '百', '千', '万')


        // ideographTraditional
        private val TRADITIONAL =
            arrayOf<String?>("甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸")

        // ideographZodiac
        private val ZODIAC =
            arrayOf<String?>("子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥")


        private val kit = ListKit()

        /**
         * 
         */
        fun instance(): ListKit {
            return kit
        }
    }
}
