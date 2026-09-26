/*
 * 文件名称:          FontKit.java
 *
 * 编译器:            android2.2
 * 时间:              下午5:12:46
 */
package com.wxiwei.office.simpletext.font

import android.graphics.Paint
import android.graphics.Typeface
import com.wxiwei.office.common.PaintKit
import com.wxiwei.office.constant.MainConstant
import com.wxiwei.office.ss.model.baseModel.Cell
import com.wxiwei.office.ss.model.baseModel.Workbook
import com.wxiwei.office.ss.model.table.SSTableCellStyle
import java.text.BreakIterator
import java.util.regex.Pattern

/**
 * 布局绘制用到的与字体相关的方法
 *
 * Read版本:        Read V1.0
 *
 * 作者:            ljj8494
 *
 * 日期:            2011-11-8
 *
 * 负责人:          ljj8494
 */
class FontKit {
    // 断词、断行算法
    private val lineBreak: BreakIterator = BreakIterator.getLineInstance()
    private val typefaceCache = HashMap<Int, Typeface>()

    /**
     *
     * @param s
     * @param p
     * @return
     */
    fun getCellPaint(cell: Cell, wb: Workbook?, tableCellStyle: SSTableCellStyle?): Paint {
        //Paint paint = new Paint();
        val paint = PaintKit.instance().getPaint()
        paint.isAntiAlias = true
        val s = cell.getCellStyle()
        val font = wb?.getFont(s!!.getFontIndex().toInt())
        val isbold = font!!.isBold() //getBoldweight() > HSSFFont.BOLDWEIGHT_NORMAL;
        val isitalics = font.isItalic()
        // 精斜体
        if (isbold && isitalics) {
            paint.textSkewX = -0.2f
            paint.isFakeBoldText = true
        }
        // 粗体
        else if (isbold) {
            paint.isFakeBoldText = true
        }
        // 斜体
        else if (isitalics) {
            paint.textSkewX = -0.2f
        }

        //Strike
        if (font.isStrikeline()) {
            paint.isStrikeThruText = true
        }

        //underline
        if (font.getUnderline() != Font.U_NONE.toInt()) {
            paint.isUnderlineText = true
        }

        // 字符样式
        val fontIndex = s!!.getFontIndex().toInt()
        paint.typeface = typefaceCache.getOrPut(fontIndex) {
            Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        }
        // fontsize
        paint.textSize = (font.getFontSize() * MainConstant.POINT_TO_PIXEL + 0.5f).toFloat()
        // color
        var color = wb.getColor(font.getColorIndex())
        if ((color and 0xFFFFFF) == 0 && tableCellStyle != null) {
            color = tableCellStyle.getFontColor()
        }
        paint.setColor(color)

        return paint
    }

    /**
     * 查找换行时的断词点
     *
     * @param text
     * @param startPos
     * @return
     */
    @Synchronized
    fun findBreakOffset(text: String?, pos: Int): Int {
        lineBreak.setText(text)
        /*char ch = text.charAt(pos);
        while (ch == 0x20 || ch == 0x3000)
        {
            pos--;
            if (pos >= 0)
            {
                ch = text.charAt(pos);
            }
        }*/
        // 如果一行只有一个单，则返回布局点位置
        //int newPos = wordBreak.following(pos - 1);
        lineBreak.following(pos)
        val newPos = lineBreak.previous()
        return if (newPos == 0) pos else newPos
    }

    fun breakText(content: String, lineWidth: Int, paint: Paint): List<String> {
        // Java String.split 语义（去掉末尾空串）
        val words = Pattern.compile("\\n").split(content)
        val textList: MutableList<String> = ArrayList()

        var index = 0
        var item: List<String>
        while (index < words.size) {
            item = wrapText(words[index], lineWidth, paint)
            val iter = item.iterator()
            while (iter.hasNext()) {
                textList.add(iter.next())
            }
            index++
        }

        return textList
    }

    /**
     * ignore char differences between word
     * @param content
     * @param lineWidth
     * @param paint
     * @return
     */
    fun wrapText(content: String, lineWidth: Int, paint: Paint): List<String> {
        var item = ""
        val restContent = content.substring(0)
        // Java String.split 语义（去掉末尾空串）
        val words: Array<String?> = Pattern.compile(" ").split(restContent)
        val textList: MutableList<String> = ArrayList()

        var wordIndex = 0
        var charIndex = 0
        var chars: CharArray
        while (wordIndex < words.size) {
            if (words[wordIndex]!!.length == 0) {
                words[wordIndex] = " "
            }
            wordIndex++
        }

        wordIndex = 0
        while (wordIndex < words.size) {
            //one word width larger than linewidth
            while (paint.measureText(words[wordIndex]) > lineWidth) {
                chars = words[wordIndex]!!.toCharArray()
                charIndex = chars.size
                item = words[wordIndex]!!.substring(0, charIndex)
                while (charIndex > 0 && paint.measureText(item) > lineWidth) {
                    charIndex--
                    item = words[wordIndex]!!.substring(0, charIndex)
                }
                textList.add(item)
                words[wordIndex] = words[wordIndex]!!.substring(charIndex, words[wordIndex]!!.length)
            }

            //made one line
            item = ""
            while (wordIndex < words.size && paint.measureText(item + words[wordIndex]) <= lineWidth) {
                item += words[wordIndex] + " "
                wordIndex++
            }
            textList.add(item.substring(0, item.length - 1))
        }

        disposeString(words)
        return textList
    }

    /**
     *
     * @param stringArray
     */
    private fun disposeString(stringArray: Array<String?>) {
        var index = 0
        while (index < stringArray.size) {
            stringArray[index] = null
            index++
        }
    }

    companion object {
        //
        private val fontKit = FontKit()

        /**
         *
         * @return
         */
        @JvmStatic
        fun instance(): FontKit {
            return fontKit
        }
    }
}
