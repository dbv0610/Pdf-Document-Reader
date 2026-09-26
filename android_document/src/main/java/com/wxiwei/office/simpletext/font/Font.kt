/*
 * 文件名称:          AFont.java
 *
 * 编译器:            android2.2
 * 时间:              上午9:48:59
 */
package com.wxiwei.office.simpletext.font

/**
 * Font
 *
 * Read版本:        Read V1.0
 *
 * 作者:            ljj8494
 *
 * 日期:            2012-2-22
 *
 * 负责人:          ljj8494
 */
open class Font {
    // 字体index
    private var index = 0

    // 字体名称
    private var name: String? = null

    //
    private var fontSize = 0.0

    // 斜体
    private var isItalic = false

    // 粗体
    private var isBold = false

    // 颜色 index
    private var colorIndex = 0

    // byte， = 1 Super, = 2 sub
    private var superSubScript: Byte = 0

    // 下划线
    private var underline = 0

    // 删除线
    private var strikeline = false

    // 风格
    @JvmField
    protected var style = 0

    constructor()

    constructor(name: String?, style: Int, size: Int) {
        this.name = name ?: "Default"
        this.style = if ((style and 0x03.inv()) == 0) style else 0
        this.fontSize = size.toDouble()
    }

    /**
     * Returns the style of this `Font`.  The style can be
     * PLAIN, BOLD, ITALIC, or BOLD+ITALIC.
     * @return the style of this `Font`
     * @see .isPlain
     * @see .isBold
     * @see .isItalic
     * @since JDK1.0
     */
    fun getStyle(): Int {
        return style
    }

    /**
     * @return Returns the index.
     */
    fun getIndex(): Int {
        return index
    }

    /**
     * @param index The index to set.
     */
    fun setIndex(index: Int) {
        this.index = index
    }

    /**
     * @return Returns the name.
     */
    fun getName(): String? {
        return name
    }

    /**
     * @param name The name to set.
     */
    fun setName(name: String?) {
        this.name = name
    }

    /**
     * @return Returns the fontSize.
     */
    fun getFontSize(): Double {
        return fontSize
    }

    /**
     * @param fontSize The fontSize to set.
     */
    fun setFontSize(fontSize: Double) {
        this.fontSize = fontSize
    }

    /**
     * @return Returns the isItalic.
     */
    fun isItalic(): Boolean {
        return isItalic
    }

    /**
     * @param isItalic The isItalic to set.
     */
    fun setItalic(isItalic: Boolean) {
        this.isItalic = isItalic
    }

    /**
     * @return Returns the isBold.
     */
    fun isBold(): Boolean {
        return isBold
    }

    /**
     * @param isBold The isBold to set.
     */
    fun setBold(isBold: Boolean) {
        this.isBold = isBold
    }

    /**
     * @return Returns the colorIndex.
     */
    fun getColorIndex(): Int {
        return colorIndex
    }

    /**
     * @param colorIndex The colorIndex to set.
     */
    fun setColorIndex(colorIndex: Int) {
        this.colorIndex = colorIndex
    }

    /**
     * @return Returns the superSubScript.
     */
    fun getSuperSubScript(): Byte {
        return superSubScript
    }

    /**
     * @param superSubScript The superSubScript to set.
     */
    fun setSuperSubScript(superSubScript: Byte) {
        this.superSubScript = superSubScript
    }

    /**
     * @return Returns the underline.
     */
    fun getUnderline(): Int {
        return underline
    }

    fun setUnderline(underline: String) {
        if (underline.equals("none", ignoreCase = true)) {
            setUnderline(U_NONE.toInt())
        } else if (underline.equals("single", ignoreCase = true)) {
            setUnderline(U_SINGLE.toInt())
        } else if (underline.equals("double", ignoreCase = true)) {
            setUnderline(U_DOUBLE.toInt())
        } else if (underline.equals("singleAccounting", ignoreCase = true)) {
            setUnderline(U_SINGLE_ACCOUNTING.toInt())
        } else if (underline.equals("doubleAccounting", ignoreCase = true)) {
            setUnderline(U_DOUBLE_ACCOUNTING.toInt())
        }
    }

    /**
     * @param underline The underline to set.
     */
    fun setUnderline(underline: Int) {
        this.underline = underline
    }

    /**
     * @return Returns the strikeline.
     */
    fun isStrikeline(): Boolean {
        return strikeline
    }

    /**
     * @param strikeline The strikeline to set.
     */
    fun setStrikeline(strikeline: Boolean) {
        this.strikeline = strikeline
    }

    fun dispose() {
        name = null
    }

    companion object {
        // Normal boldness (not bold)
        const val BOLDWEIGHT_NORMAL: Short = 0x190

        // Bold boldness (bold)
        const val BOLDWEIGHT_BOLD: Short = 0x2bc

        // normal type of black color.
        const val COLOR_NORMAL: Short = 0x7fff

        // Dark Red color
        const val COLOR_RED: Short = 0xa

        // no type offsetting (not super or subscript)
        const val SS_NONE: Short = 0

        // superscript
        const val SS_SUPER: Short = 1

        // subscript
        const val SS_SUB: Short = 2

        // not underlined
        const val U_NONE: Byte = 0

        // single (normal) underline
        const val U_SINGLE: Byte = 1

        // double underlined
        const val U_DOUBLE: Byte = 2

        // accounting style single underline
        const val U_SINGLE_ACCOUNTING: Byte = 0x21

        // accounting style double underline
        const val U_DOUBLE_ACCOUNTING: Byte = 0x22

        // ANSI character set
        const val ANSI_CHARSET: Byte = 0

        // Default character set.
        const val DEFAULT_CHARSET: Byte = 1

        // Symbol character set
        const val SYMBOL_CHARSET: Byte = 2

        /**
         * The plain style constant.
         */
        const val PLAIN = 0

        /**
         * The bold style constant.  This can be combined with the other style
         * constants (except PLAIN) for mixed styles.
         */
        const val BOLD = 1

        /**
         * The italicized style constant.  This can be combined with the other
         * style constants (except PLAIN) for mixed styles.
         */
        const val ITALIC = 2
    }
}
