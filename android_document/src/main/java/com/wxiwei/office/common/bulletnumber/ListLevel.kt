/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
/*
 * 文件名称:          ListLevel.java
 *  
 * 编译器:            android2.2
 * 时间:              上午10:49:59
 */
package com.wxiwei.office.common.bulletnumber

/**
 * bullet and number level object
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
 * 日期:            2012-6-18
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
class ListLevel {
    /**
     * 
     */
    fun dispose() {
        numberText = null
    }


    /**
     * @return Returns the startAt.
     */
    /**
     * @param startAt The startAt to set.
     */
    // start number
    var startAt: Int = 0
    /**
     * @return Returns the numberFormat.
     */
    /**
     * @param numberFormat The numberFormat to set.
     */
    /*
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
         * = 22   decimalZero                       01、02、03、...o
         */
    var numberFormat: Int = 0
    /**
     * @return Returns the numberText.
     */
    /**
     * @param numberText The numberText to set.
     */
    // number text
    var numberText: CharArray? = null
    /**
     * @return Returns the align.
     */
    /**
     * @param align The align to set.
     */
    // horizontal alignment
    var align: Byte = 0
    /**
     * @return Returns the followChar.
     */
    /**
     * @param followChar The followChar to set.
     */
    // The type of character following the number text for the paragraph: 0 == tab, 1 == space, 2 == nothing
    var followChar: Byte = 0
    /**
     * @return Returns the textIndent.
     */
    /**
     * @param textIndent The textIndent to set.
     */
    //
    var textIndent: Int = 0
    /**
     * @return Returns the specialIndent.
     */
    /**
     * @param specialIndent The specialIndent to set.
     */
    //
    var specialIndent: Int = 0
    /**
     * @return Returns the paraCount.
     */
    /**
     * @param paraCount The paraCount to set.
     */
    // previous paragraph count of same level
    var paraCount: Int = 0
    /**
     * @return Returns the normalParaCount.
     */
    /**
     * @param normalParaCount The normalParaCount to set.
     */
    //
    var normalParaCount: Int = 0
}
