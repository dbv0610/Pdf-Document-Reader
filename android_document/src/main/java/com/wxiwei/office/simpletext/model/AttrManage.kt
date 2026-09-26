/*
 * 文件名称:          AttrKit.java
 *
 * 编译器:            android2.2
 * 时间:              下午1:51:06
 */
package com.wxiwei.office.simpletext.model

import android.graphics.Color
import android.util.Log
import com.wxiwei.office.common.bulletnumber.ListData
import com.wxiwei.office.common.bulletnumber.ListLevel
import com.wxiwei.office.constant.MainConstant
import com.wxiwei.office.constant.wp.AttrIDConstant
import com.wxiwei.office.constant.wp.WPAttrConstant
import com.wxiwei.office.simpletext.view.CharAttr
import com.wxiwei.office.simpletext.view.PageAttr
import com.wxiwei.office.simpletext.view.ParaAttr
import com.wxiwei.office.simpletext.view.TableAttr
import com.wxiwei.office.system.IControl
import com.wxiwei.office.system.MainControl

/**
 * 属性管理器
 *
 * Read版本:        Read V1.0
 *
 * 作者:            ljj8494
 *
 * 日期:            2011-12-28
 *
 * 负责人:          ljj8494
 */
class AttrManage {

    /**
     * 指定的AttributeSet 是否有指定的attrID属性
     *
     */
    fun hasAttribute(attr: IAttributeSet?, attrID: Short): Boolean {
        return attr!!.getAttribute(attrID) != Int.MIN_VALUE
    }

    /* ============= 字符属性 ========== */
    /**
     * get character style id
     */
    fun getFontStyleID(attr: IAttributeSet?): Int {
        val a = attr!!.getAttribute(AttrIDConstant.FONT_STYLE_ID)
        if (a == Int.MIN_VALUE) {
            return -1
        }
        return a
    }

    /**
     * get style id
     */
    fun setFontStyleID(attr: IAttributeSet?, value: Int) {
        attr!!.setAttribute(AttrIDConstant.FONT_STYLE_ID, value)
    }

    /**
     * get fontSize
     */
    fun getFontSize(paraAttr: IAttributeSet?, leafAttr: IAttributeSet?): Int {
        var a = leafAttr!!.getAttribute(AttrIDConstant.FONT_SIZE_ID)
        if (a == Int.MIN_VALUE) {
            a = paraAttr!!.getAttribute(AttrIDConstant.FONT_SIZE_ID)
            if (a == Int.MIN_VALUE) {
                return 12
            }
        }
        return a
    }

    /**
     * set font size
     */
    fun setFontSize(attr: IAttributeSet?, value: Int) {
        attr!!.setAttribute(AttrIDConstant.FONT_SIZE_ID, value)
    }

    /**
     * get fontSize
     */
    fun getFontName(paraAttr: IAttributeSet?, leafAttr: IAttributeSet?): Int {
        var a = leafAttr!!.getAttribute(AttrIDConstant.FONT_NAME_ID)
        if (a == Int.MIN_VALUE) {
            a = paraAttr!!.getAttribute(AttrIDConstant.FONT_NAME_ID)
            if (a == Int.MIN_VALUE) {
                return -1
            }
        }
        return a
    }

    /**
     * set fontSize
     */
    fun setFontName(attr: IAttributeSet?, value: Int) {
        attr!!.setAttribute(AttrIDConstant.FONT_NAME_ID, value)
    }

    /**
     * get fontScale
     */
    fun getFontScale(paraAttr: IAttributeSet?, leafAttr: IAttributeSet?): Int {
        var a = leafAttr!!.getAttribute(AttrIDConstant.FONT_SCALE_ID)
        if (a == Int.MIN_VALUE) {
            a = paraAttr!!.getAttribute(AttrIDConstant.FONT_SCALE_ID)
            if (a == Int.MIN_VALUE) {
                return 100
            }
        }
        return a
    }

    /**
     * set fontScale
     */
    fun setFontScale(attr: IAttributeSet?, value: Int) {
        attr!!.setAttribute(AttrIDConstant.FONT_SCALE_ID, value)
    }

    /**
     * get FontColor
     */
    fun getFontColor(paraAttr: IAttributeSet?, leafAttr: IAttributeSet?): Int {
        var a = leafAttr!!.getAttribute(AttrIDConstant.FONT_COLOR_ID)
        if (a == Int.MIN_VALUE) {
            a = paraAttr!!.getAttribute(AttrIDConstant.FONT_COLOR_ID)
            if (a == Int.MIN_VALUE) {
                return Color.BLACK
            }
        }
        return a
    }

    /**
     * set FontColor
     */
    fun setFontColor(attr: IAttributeSet?, value: Int) {
        attr!!.setAttribute(AttrIDConstant.FONT_COLOR_ID, value)
    }

    /**
     * get Bold
     */
    fun getFontBold(paraAttr: IAttributeSet?, leafAttr: IAttributeSet?): Boolean {
        var a = leafAttr!!.getAttribute(AttrIDConstant.FONT_BOLD_ID)
        if (a == Int.MIN_VALUE) {
            a = paraAttr!!.getAttribute(AttrIDConstant.FONT_BOLD_ID)
            if (a == Int.MIN_VALUE) {
                return false
            }
        }
        return a == 1
    }

    /**
     * set Bold
     */
    fun setFontBold(attr: IAttributeSet?, b: Boolean) {
        attr!!.setAttribute(AttrIDConstant.FONT_BOLD_ID, if (b) 1 else 0)
    }

    /**
     * get Italic
     */
    fun getFontItalic(paraAttr: IAttributeSet?, leafAttr: IAttributeSet?): Boolean {
        var a = leafAttr!!.getAttribute(AttrIDConstant.FONT_ITALIC_ID)
        if (a == Int.MIN_VALUE) {
            a = paraAttr!!.getAttribute(AttrIDConstant.FONT_ITALIC_ID)
            if (a == Int.MIN_VALUE) {
                return false
            }
        }
        return a == 1
    }

    /**
     * set Italic
     */
    fun setFontItalic(attr: IAttributeSet?, b: Boolean) {
        attr!!.setAttribute(AttrIDConstant.FONT_ITALIC_ID, if (b) 1 else 0)
    }

    /**
     * get 删除线
     */
    fun getFontStrike(paraAttr: IAttributeSet?, leafAttr: IAttributeSet?): Boolean {
        var a = leafAttr!!.getAttribute(AttrIDConstant.FONT_STRIKE_ID)
        if (a == Int.MIN_VALUE) {
            a = paraAttr!!.getAttribute(AttrIDConstant.FONT_STRIKE_ID)
            if (a == Int.MIN_VALUE) {
                return false
            }
        }
        return a == 1
    }

    /**
     * set 删除线
     */
    fun setFontStrike(attr: IAttributeSet?, b: Boolean) {
        attr!!.setAttribute(AttrIDConstant.FONT_STRIKE_ID, if (b) 1 else 0)
    }

    /**
     * get 双删除线
     */
    fun getFontDoubleStrike(paraAttr: IAttributeSet?, leafAttr: IAttributeSet?): Boolean {
        var a = leafAttr!!.getAttribute(AttrIDConstant.FONT_DOUBLESTRIKE_ID)
        if (a == Int.MIN_VALUE) {
            a = paraAttr!!.getAttribute(AttrIDConstant.FONT_DOUBLESTRIKE_ID)
            if (a == Int.MIN_VALUE) {
                return false
            }
        }
        return a == 1
    }

    /**
     * set 双删除线
     */
    fun setFontDoubleStrike(attr: IAttributeSet?, b: Boolean) {
        attr!!.setAttribute(AttrIDConstant.FONT_DOUBLESTRIKE_ID, if (b) 1 else 0)
    }

    /**
     * get 下划线
     */
    fun getFontUnderline(paraAttr: IAttributeSet?, leafAttr: IAttributeSet?): Int {
        var a = leafAttr!!.getAttribute(AttrIDConstant.FONT_UNDERLINE_ID)
        if (a == Int.MIN_VALUE) {
            a = paraAttr!!.getAttribute(AttrIDConstant.FONT_UNDERLINE_ID)
            if (a == Int.MIN_VALUE) {
                return 0
            }
        }
        return a
    }

    /**
     * set 下划线
     */
    fun setFontUnderline(attr: IAttributeSet?, value: Int) {
        attr!!.setAttribute(AttrIDConstant.FONT_UNDERLINE_ID, value)
    }

    /**
     * get 下划线Color
     */
    fun getFontUnderlineColor(paraAttr: IAttributeSet?, leafAttr: IAttributeSet?): Int {
        var a = leafAttr!!.getAttribute(AttrIDConstant.FONT_UNDERLINE_COLOR_ID)
        if (a == Int.MIN_VALUE) {
            a = paraAttr!!.getAttribute(AttrIDConstant.FONT_UNDERLINE_COLOR_ID)
            if (a == Int.MIN_VALUE) {
                return getFontColor(paraAttr, leafAttr)
            }
        }
        return a
    }

    /**
     * set 下划线Color
     */
    fun setFontUnderlineColr(attr: IAttributeSet?, value: Int) {
        attr!!.setAttribute(AttrIDConstant.FONT_UNDERLINE_COLOR_ID, value)
    }

    /**
     * get 上下标
     */
    fun getFontScript(paraAttr: IAttributeSet?, leafAttr: IAttributeSet?): Int {
        var a = leafAttr!!.getAttribute(AttrIDConstant.FONT_SCRIPT_ID)
        if (a == Int.MIN_VALUE) {
            a = paraAttr!!.getAttribute(AttrIDConstant.FONT_SCRIPT_ID)
            if (a == Int.MIN_VALUE) {
                return 0
            }
        }
        return a
    }

    /**
     * set 上下标
     */
    fun setFontScript(attr: IAttributeSet?, value: Int) {
        attr!!.setAttribute(AttrIDConstant.FONT_SCRIPT_ID, value)
    }

    /**
     * get 高亮
     */
    fun getFontHighLight(paraAttr: IAttributeSet?, leafAttr: IAttributeSet?): Int {
        var a = leafAttr!!.getAttribute(AttrIDConstant.FONT_HIGHLIGHT_ID)
        if (a == Int.MIN_VALUE) {
            a = paraAttr!!.getAttribute(AttrIDConstant.FONT_HIGHLIGHT_ID)
            if (a == Int.MIN_VALUE) {
                return -1
            }
        }
        return a
    }

    /**
     * set 高亮
     */
    fun setFontHighLight(attr: IAttributeSet?, value: Int) {
        attr!!.setAttribute(AttrIDConstant.FONT_HIGHLIGHT_ID, value)
    }

    /**
     * get hyperlink id
     */
    fun getHperlinkID(attr: IAttributeSet?): Int {
        val a = attr!!.getAttribute(AttrIDConstant.FONT_HYPERLINK_ID)
        if (a == Int.MIN_VALUE) {
            return -1
        }
        return a
    }

    /**
     * set hyperlink id
     */
    fun setHyperlinkID(attr: IAttributeSet?, value: Int) {
        attr!!.setAttribute(AttrIDConstant.FONT_HYPERLINK_ID, value)
    }

    /**
     * get shape id
     */
    fun getShapeID(attr: IAttributeSet?): Int {
        val a = attr!!.getAttribute(AttrIDConstant.FONT_SHAPE_ID)
        if (a == Int.MIN_VALUE) {
            return -1
        }
        return a
    }

    /**
     *
     */
    fun setShapeID(attr: IAttributeSet?, value: Int) {
        attr!!.setAttribute(AttrIDConstant.FONT_SHAPE_ID, value)
    }

    /**
     * get page number type
     */
    fun getFontPageNumberType(attr: IAttributeSet?): Int {
        val a = attr!!.getAttribute(AttrIDConstant.FONT_PAGE_NUMBER_TYPE_ID)
        if (a == Int.MIN_VALUE) {
            return -1
        }
        return a
    }

    /**
     * @param attr
     * @param value  = 1, page number
     *               = 2， total pages
     */
    fun setFontPageNumberType(attr: IAttributeSet?, value: Int) {
        attr!!.setAttribute(AttrIDConstant.FONT_PAGE_NUMBER_TYPE_ID, value)
    }

    /**
     *
     */
    /**
     * get page number type
     */
    fun getFontEncloseChanacterType(paraAttr: IAttributeSet?, leafAttr: IAttributeSet?): Int {
        val a = leafAttr!!.getAttribute(AttrIDConstant.FONT_ENCLOSE_CHARACTER_TYPE_ID)
        if (a == Int.MIN_VALUE) {
            return -1
        }
        return a
    }

    /**
     * @param attr
     * @param value  = 1, page number
     *               = 2， total pages
     */
    fun setEncloseChanacterType(attr: IAttributeSet?, value: Int) {
        attr!!.setAttribute(AttrIDConstant.FONT_ENCLOSE_CHARACTER_TYPE_ID, value)
    }

    /* ============ 段落属性 =========== */
    /**
     * get character style id
     */
    fun getParaStyleID(attr: IAttributeSet?): Int {
        val a = attr!!.getAttribute(AttrIDConstant.PARA_STYLE_ID)
        if (a == Int.MIN_VALUE) {
            return -1
        }
        return a
    }

    /**
     * get style id
     */
    fun setParaStyleID(attr: IAttributeSet?, value: Int) {
        attr!!.setAttribute(AttrIDConstant.PARA_STYLE_ID, value)
    }

    /**
     * get 左缩进
     */
    fun getParaIndentLeft(attr: IAttributeSet?): Int {
        val a = attr!!.getAttribute(AttrIDConstant.PARA_INDENT_LEFT_ID)
        if (a == Int.MIN_VALUE) {
            return 0
        }
        return a
    }

    /**
     * set 左缩进
     */
    fun setParaIndentLeft(attr: IAttributeSet?, value: Int) {
        attr!!.setAttribute(AttrIDConstant.PARA_INDENT_LEFT_ID, value)
    }

    fun getParaIndentInitLeft(attr: IAttributeSet?): Int {
        val a = attr!!.getAttribute(AttrIDConstant.PARA_INDENT_INITLEFT_ID)
        if (a == Int.MIN_VALUE) {
            return 0
        }
        return a
    }

    /**
     * set 左缩进
     */
    fun setParaIndentInitLeft(attr: IAttributeSet?, value: Int) {
        attr!!.setAttribute(AttrIDConstant.PARA_INDENT_INITLEFT_ID, value)
    }

    /**
     * get 右缩进
     */
    fun getParaIndentRight(attr: IAttributeSet?): Int {
        val a = attr!!.getAttribute(AttrIDConstant.PARA_INDENT_RIGHT_ID)
        if (a == Int.MIN_VALUE) {
            return 0
        }
        return a
    }

    /**
     * set 右缩进
     */
    fun setParaIndentRight(attr: IAttributeSet?, value: Int) {
        attr!!.setAttribute(AttrIDConstant.PARA_INDENT_RIGHT_ID, value)
    }

    /**
     * get 段前间距
     */
    fun getParaBefore(attr: IAttributeSet?): Int {
        val a = attr!!.getAttribute(AttrIDConstant.PARA_BEFORE_ID)
        if (a == Int.MIN_VALUE) {
            return 0
        }
        return a
    }

    /**
     * set 段前间距
     */
    fun setParaBefore(attr: IAttributeSet?, value: Int) {
        attr!!.setAttribute(AttrIDConstant.PARA_BEFORE_ID, value)
    }

    /**
     * get 段后间距
     */
    fun getParaAfter(attr: IAttributeSet?): Int {
        val a = attr!!.getAttribute(AttrIDConstant.PARA_AFTER_ID)
        if (a == Int.MIN_VALUE) {
            return 0
        }
        return a
    }

    /**
     * set 段后间距
     */
    fun setParaAfter(attr: IAttributeSet?, value: Int) {
        attr!!.setAttribute(AttrIDConstant.PARA_AFTER_ID, value)
    }

    /**
     * get 特殊缩进
     */
    fun getParaSpecialIndent(attr: IAttributeSet?): Int {
        val a = attr!!.getAttribute(AttrIDConstant.PARA_SPECIALINDENT_ID)
        if (a == Int.MIN_VALUE) {
            return 0
        }
        return a
    }

    /**
     * set 特殊缩进
     */
    fun setParaSpecialIndent(attr: IAttributeSet?, value: Int) {
        attr!!.setAttribute(AttrIDConstant.PARA_SPECIALINDENT_ID, value)
    }

    /**
     * get 行距
     */
    fun getParaLineSpace(attr: IAttributeSet?): Float {
        val a = attr!!.getAttribute(AttrIDConstant.PARA_LINESPACE_ID)
        if (a == Int.MIN_VALUE) {
            return 1f
        }
        return a / 100f
    }

    /**
     * set 行距
     */
    fun setParaLineSpace(attr: IAttributeSet?, value: Float) {
        attr!!.setAttribute(AttrIDConstant.PARA_LINESPACE_ID, (value * 100).toInt())
    }

    /**
     * get 行距类型
     */
    fun getParaLineSpaceType(attr: IAttributeSet?): Int {
        val a = attr!!.getAttribute(AttrIDConstant.PARA_LINESPACE_TYPE_ID)
        if (a == Int.MIN_VALUE) {
            return 1
        }
        return a
    }

    /**
     * set 行距类型
     */
    fun setParaLineSpaceType(attr: IAttributeSet?, value: Int) {
        attr!!.setAttribute(AttrIDConstant.PARA_LINESPACE_TYPE_ID, value)
    }

    /**
     * get 水平对齐
     */
    fun getParaHorizontalAlign(attr: IAttributeSet?): Int {
        val a = attr!!.getAttribute(AttrIDConstant.PARA_HORIZONTAL_ID)
        if (a == Int.MIN_VALUE) {
            return 0
        }
        return a
    }

    /**
     * set 水平对齐
     */
    fun setParaHorizontalAlign(attr: IAttributeSet?, value: Int) {
        attr!!.setAttribute(AttrIDConstant.PARA_HORIZONTAL_ID, value)
    }

    /**
     * get 垂直对齐
     */
    fun getParaVerticalAlign(attr: IAttributeSet?): Int {
        val a = attr!!.getAttribute(AttrIDConstant.PARA_VERTICAL_ID)
        if (a == Int.MIN_VALUE) {
            return 0
        }
        return a
    }

    /**
     * set 垂直对齐
     */
    fun setParaVerticalAlign(attr: IAttributeSet?, value: Int) {
        attr!!.setAttribute(AttrIDConstant.PARA_VERTICAL_ID, value)
    }

    /**
     * get paragraph level
     */
    fun getParaLevel(attr: IAttributeSet?): Int {
        val a = attr!!.getAttribute(AttrIDConstant.PARA_LEVEL_ID)
        if (a == Int.MIN_VALUE) {
            return -1
        }
        return a
    }

    /**
     * set paragraph level
     */
    fun setParaLevel(attr: IAttributeSet?, value: Int) {
        attr!!.setAttribute(AttrIDConstant.PARA_LEVEL_ID, value)
    }

    /**
     * get paragraph level
     */
    fun getParaListLevel(attr: IAttributeSet?): Int {
        val a = attr!!.getAttribute(AttrIDConstant.PARA_LIST_LEVEL_ID)
        if (a == Int.MIN_VALUE) {
            return -1
        }
        return a
    }

    /**
     * set paragraph level
     */
    fun setParaListLevel(attr: IAttributeSet?, value: Int) {
        attr!!.setAttribute(AttrIDConstant.PARA_LIST_LEVEL_ID, value)
    }

    /**
     * get paragraph level
     */
    fun getParaListID(attr: IAttributeSet?): Int {
        val a = attr!!.getAttribute(AttrIDConstant.PARA_LIST_ID)
        if (a == Int.MIN_VALUE) {
            return -1
        }
        return a
    }

    /**
     * set paragraph level
     */
    fun setParaListID(attr: IAttributeSet?, value: Int) {
        attr!!.setAttribute(AttrIDConstant.PARA_LIST_ID, value)
    }

    /**
     * get pg bullet text ID
     */
    fun getPGParaBulletID(attr: IAttributeSet?): Int {
        val a = attr!!.getAttribute(AttrIDConstant.PARA_PG_BULLET_ID)
        if (a == Int.MIN_VALUE) {
            return -1
        }
        return a
    }

    /**
     * set pg bullet text ID
     */
    fun setPGParaBulletID(attr: IAttributeSet?, value: Int) {
        attr!!.setAttribute(AttrIDConstant.PARA_PG_BULLET_ID, value)
    }

    /**
     * get paragraph tabs clear position
     */
    fun getParaTabsClearPostion(attr: IAttributeSet?): Int {
        val a = attr!!.getAttribute(AttrIDConstant.PARA_TABS_CLEAR_POSITION_ID)
        if (a == Int.MIN_VALUE) {
            return -1
        }
        return a
    }

    /**
     * set paragraph tabs clear position
     */
    fun setParaTabsClearPostion(attr: IAttributeSet?, value: Int) {
        attr!!.setAttribute(AttrIDConstant.PARA_TABS_CLEAR_POSITION_ID, value)
    }

    /* =========== 章节属性 =========== */
    /**
     * get页面宽度
     */
    fun getPageWidth(attr: IAttributeSet?): Int {
        val a = attr!!.getAttribute(AttrIDConstant.PAGE_WIDTH_ID)
        if (a == Int.MIN_VALUE) {
            return 1000
        }
        return a
    }

    /**
     * set 页面宽度
     */
    fun setPageWidth(attr: IAttributeSet?, value: Int) {
        attr!!.setAttribute(AttrIDConstant.PAGE_WIDTH_ID, value)
    }

    /**
     * get页面高度
     */
    fun getPageHeight(attr: IAttributeSet?): Int {
        val a = attr!!.getAttribute(AttrIDConstant.PAGE_HEIGHT_ID)
        if (a == Int.MIN_VALUE) {
            return 1200
        }
        return a
    }

    /**
     * set 页面高度
     */
    fun setPageHeight(attr: IAttributeSet?, value: Int) {
        attr!!.setAttribute(AttrIDConstant.PAGE_HEIGHT_ID, value)
    }

    /**
     *
     * @param attr
     * @param verAlign
     */
    fun setPageVerticalAlign(attr: IAttributeSet?, verAlign: Byte) {
        attr!!.setAttribute(AttrIDConstant.PAGE_VERTICAL_ID, verAlign.toInt())
    }

    /**
     *
     * @param attr
     * @return
     */
    fun getPageVerticalAlign(attr: IAttributeSet?): Byte {
        val a = attr!!.getAttribute(AttrIDConstant.PAGE_VERTICAL_ID)
        if (a == Int.MIN_VALUE) {
            return 0
        }
        return a.toByte()
    }

    /**
     *
     * @param attr
     * @param verAlign
     */
    fun setPageHorizontalAlign(attr: IAttributeSet?, verAlign: Byte) {
        attr!!.setAttribute(AttrIDConstant.PAGE_HORIZONTAL_ID, verAlign.toInt())
    }

    /**
     *
     * @param attr
     * @return
     */
    fun getPageHorizontalAlign(attr: IAttributeSet?): Byte {
        val a = attr!!.getAttribute(AttrIDConstant.PAGE_HORIZONTAL_ID)
        if (a == Int.MIN_VALUE) {
            return WPAttrConstant.PAGE_H_LEFT
        }
        return a.toByte()
    }

    /**
     * get 页面左边距
     */
    fun getPageMarginLeft(attr: IAttributeSet?): Int {
        val a = attr!!.getAttribute(AttrIDConstant.PAGE_LEFT_ID)
        if (a == Int.MIN_VALUE) {
            return 1800
        }
        return a
    }

    /**
     * set 页面左边距
     */
    fun setPageMarginLeft(attr: IAttributeSet?, value: Int) {
        attr!!.setAttribute(AttrIDConstant.PAGE_LEFT_ID, value)
    }

    /**
     * get 页面右边距
     */
    fun getPageMarginRight(attr: IAttributeSet?): Int {
        val a = attr!!.getAttribute(AttrIDConstant.PAGE_RIGHT_ID)
        if (a == Int.MIN_VALUE) {
            return 1800
        }
        return a
    }

    /**
     * set 页面右边距
     */
    fun setPageMarginRight(attr: IAttributeSet?, value: Int) {
        attr!!.setAttribute(AttrIDConstant.PAGE_RIGHT_ID, value)
    }

    /**
     * get 页面上边距
     */
    fun getPageMarginTop(attr: IAttributeSet?): Int {
        val a = attr!!.getAttribute(AttrIDConstant.PAGE_TOP_ID)
        if (a == Int.MIN_VALUE) {
            return 1440
        }
        return a
    }

    /**
     * set 页面上边距
     */
    fun setPageMarginTop(attr: IAttributeSet?, value: Int) {
        attr!!.setAttribute(AttrIDConstant.PAGE_TOP_ID, value)
    }

    /**
     * get 页面下边距
     */
    fun getPageMarginBottom(attr: IAttributeSet?): Int {
        val a = attr!!.getAttribute(AttrIDConstant.PAGE_BOTTOM_ID)
        if (a == Int.MIN_VALUE) {
            return 1440
        }
        return a
    }

    /**
     * set 页面下边距
     */
    fun setPageMarginBottom(attr: IAttributeSet?, value: Int) {
        attr!!.setAttribute(AttrIDConstant.PAGE_BOTTOM_ID, value)
    }

    /**
     * get header margin
     */
    fun getPageHeaderMargin(attr: IAttributeSet?): Int {
        val a = attr!!.getAttribute(AttrIDConstant.PAGE_HEADER_ID)
        if (a == Int.MIN_VALUE) {
            return 850
        }
        return a
    }

    /**
     * set header margin
     */
    fun setPageHeaderMargin(attr: IAttributeSet?, value: Int) {
        attr!!.setAttribute(AttrIDConstant.PAGE_HEADER_ID, value)
    }

    /**
     * get footer margin
     */
    fun getPageFooterMargin(attr: IAttributeSet?): Int {
        val a = attr!!.getAttribute(AttrIDConstant.PAGE_FOOTER_ID)
        if (a == Int.MIN_VALUE) {
            return 850
        }
        return a
    }

    /**
     * set footer margin
     */
    fun setPageFooterMargin(attr: IAttributeSet?, value: Int) {
        attr!!.setAttribute(AttrIDConstant.PAGE_FOOTER_ID, value)
    }

    /**
     * get page background color
     */
    fun getPageBackgroundColor(attr: IAttributeSet?): Int {
        val a = attr!!.getAttribute(AttrIDConstant.PAGE_BACKGROUND_COLOR_ID)
        if (a == Int.MIN_VALUE) {
            return Color.WHITE
        }
        return a
    }

    /**
     * get page background color
     */
    fun setPageBackgroundColor(attr: IAttributeSet?, value: Int) {
        attr!!.setAttribute(AttrIDConstant.PAGE_BACKGROUND_COLOR_ID, value)
    }

    /**
     * get page background color
     */
    fun getPageBorder(attr: IAttributeSet?): Int {
        val a = attr!!.getAttribute(AttrIDConstant.PAGE_BORDER_ID)
        if (a == Int.MIN_VALUE) {
            return -1
        }
        return a
    }

    /**
     * get page background color
     */
    fun setPageBorder(attr: IAttributeSet?, value: Int) {
        attr!!.setAttribute(AttrIDConstant.PAGE_BORDER_ID, value)
    }

    /**
     * line pitch
     * @param attr
     * @return
     */
    fun getPageLinePitch(attr: IAttributeSet?): Int {
        val a = attr!!.getAttribute(AttrIDConstant.PAGE_LINEPITCH_ID)
        if (a == Int.MIN_VALUE) {
            return -1
        }
        return a
    }

    /**
     * line pitch
     * @param attr
     * @param value
     */
    fun setPageLinePitch(attr: IAttributeSet?, value: Int) {
        attr!!.setAttribute(AttrIDConstant.PAGE_LINEPITCH_ID, value)
    }

    // =========== 表格属性 ================
    /**
     * get top border
     */
    fun getTableTopBorder(attr: IAttributeSet?): Int {
        val a = attr!!.getAttribute(AttrIDConstant.TABLE_TOP_BORDER_ID)
        if (a == Int.MIN_VALUE) {
            return 0
        }
        return a
    }

    /**
     * set top border
     */
    fun setTableTopBorder(attr: IAttributeSet?, value: Int) {
        attr!!.setAttribute(AttrIDConstant.TABLE_TOP_BORDER_ID, value)
    }

    /**
     * get top border color
     */
    fun getTableTopBorderColor(attr: IAttributeSet?): Int {
        val a = attr!!.getAttribute(AttrIDConstant.TABLE_TOP_BORDER_COLOR_ID)
        if (a == Int.MIN_VALUE) {
            return Color.BLACK
        }
        return a
    }

    /**
     * set top border color
     */
    fun setTableTopBorderColor(attr: IAttributeSet?, value: Int) {
        attr!!.setAttribute(AttrIDConstant.TABLE_TOP_BORDER_COLOR_ID, value)
    }

    /**
     * get left border
     */
    fun getTableLeftBorder(attr: IAttributeSet?): Int {
        val a = attr!!.getAttribute(AttrIDConstant.TABLE_LEFT_BORDER_ID)
        if (a == Int.MIN_VALUE) {
            return 0
        }
        return a
    }

    /**
     * set left border
     */
    fun setTableLeftBorder(attr: IAttributeSet?, value: Int) {
        attr!!.setAttribute(AttrIDConstant.TABLE_LEFT_BORDER_ID, value)
    }

    /**
     * get left border color
     */
    fun getTableLeftBorderColor(attr: IAttributeSet?): Int {
        val a = attr!!.getAttribute(AttrIDConstant.TABLE_LEFT_BORDER_COLOR_ID)
        if (a == Int.MIN_VALUE) {
            return Color.BLACK
        }
        return a
    }

    /**
     * set left border color
     */
    fun setTableLeftBorderColor(attr: IAttributeSet?, value: Int) {
        attr!!.setAttribute(AttrIDConstant.TABLE_LEFT_BORDER_COLOR_ID, value)
    }

    /**
     * get bottom border
     */
    fun getTableBottomBorder(attr: IAttributeSet?): Int {
        val a = attr!!.getAttribute(AttrIDConstant.TABLE_BOTTOM_BORDER_ID)
        if (a == Int.MIN_VALUE) {
            return 0
        }
        return a
    }

    /**
     * set bottom border
     */
    fun setTableBottomBorder(attr: IAttributeSet?, value: Int) {
        attr!!.setAttribute(AttrIDConstant.TABLE_BOTTOM_BORDER_ID, value)
    }

    /**
     * get bottom border color
     */
    fun getTableBottomBorderColor(attr: IAttributeSet?): Int {
        val a = attr!!.getAttribute(AttrIDConstant.TABLE_BOTTOM_BORDER_COLOR_ID)
        if (a == Int.MIN_VALUE) {
            return Color.BLACK
        }
        return a
    }

    /**
     * set bottom border color
     */
    fun setTableBottomBorderColor(attr: IAttributeSet?, value: Int) {
        attr!!.setAttribute(AttrIDConstant.TABLE_BOTTOM_BORDER_COLOR_ID, value)
    }

    /**
     * get right border
     */
    fun getTableRightBorder(attr: IAttributeSet?): Int {
        val a = attr!!.getAttribute(AttrIDConstant.TABLE_RIGHT_BORDER_ID)
        if (a == Int.MIN_VALUE) {
            return 0
        }
        return a
    }

    /**
     * set rith border
     */
    fun setTableRightBorder(attr: IAttributeSet?, value: Int) {
        attr!!.setAttribute(AttrIDConstant.TABLE_RIGHT_BORDER_ID, value)
    }

    /**
     * get right border color
     */
    fun getTableRightBorderColor(attr: IAttributeSet?): Int {
        val a = attr!!.getAttribute(AttrIDConstant.TABLE_RIGHT_BORDER_COLOR_ID)
        if (a == Int.MIN_VALUE) {
            return Color.BLACK
        }
        return a
    }

    /**
     * set right border color
     */
    fun setTableRightBorderColor(attr: IAttributeSet?, value: Int) {
        attr!!.setAttribute(AttrIDConstant.TABLE_RIGHT_BORDER_COLOR_ID, value)
    }

    /**
     * get table row height
     */
    fun getTableRowHeight(attr: IAttributeSet?): Int {
        val a = attr!!.getAttribute(AttrIDConstant.TABLE_ROW_HEIGHT_ID)
        if (a == Int.MIN_VALUE) {
            return 0
        }
        return a
    }

    /**
     * set table row height
     */
    fun setTableRowHeight(attr: IAttributeSet?, value: Int) {
        attr!!.setAttribute(AttrIDConstant.TABLE_ROW_HEIGHT_ID, value)
    }

    /**
     * get table cell width
     */
    fun getTableCellWidth(attr: IAttributeSet?): Int {
        val a = attr!!.getAttribute(AttrIDConstant.TABLE_CELL_WIDTH_ID)
        if (a == Int.MIN_VALUE) {
            return 0
        }
        return a
    }

    /**
     *
     */
    fun setTableCellWidth(attr: IAttributeSet?, value: Int) {
        attr!!.setAttribute(AttrIDConstant.TABLE_CELL_WIDTH_ID, value)
    }

    /**
     * get table row split
     */
    fun isTableRowSplit(attr: IAttributeSet?, value: Int): Boolean {
        val a = attr!!.getAttribute(AttrIDConstant.TABLE_ROW_SPLIT_ID)
        if (a == Int.MIN_VALUE) {
            return true
        }
        return a == 1
    }

    /**
     * set table row split
     */
    fun setTableRowSplit(attr: IAttributeSet?, b: Boolean) {
        attr!!.setAttribute(AttrIDConstant.TABLE_ROW_SPLIT_ID, if (b) 1 else 0)
    }

    /**
     * get table header row
     */
    fun isTableHeaderRow(attr: IAttributeSet?): Boolean {
        val a = attr!!.getAttribute(AttrIDConstant.TABLE_ROW_HEADER_ID)
        if (a == Int.MIN_VALUE) {
            return true
        }
        return a == 1
    }

    /**
     * set table header row
     */
    fun setTableHeaderRow(attr: IAttributeSet?, b: Boolean) {
        attr!!.setAttribute(AttrIDConstant.TABLE_ROW_HEADER_ID, if (b) 1 else 0)
    }

    /**
     * get the first horizontal merged cell
     */
    fun isTableHorFirstMerged(attr: IAttributeSet?): Boolean {
        val a = attr!!.getAttribute(AttrIDConstant.TABLE_CELL_HOR_FIRST_MERGED_ID)
        if (a == Int.MIN_VALUE) {
            return false
        }
        return a == 1
    }

    /**
     * set first horizontal merged cell
     */
    fun setTableHorFirstMerged(attr: IAttributeSet?, b: Boolean) {
        attr!!.setAttribute(AttrIDConstant.TABLE_CELL_HOR_FIRST_MERGED_ID, if (b) 1 else 0)
    }

    /**
     * get horizontal merged cell
     */
    fun isTableHorMerged(attr: IAttributeSet?): Boolean {
        val a = attr!!.getAttribute(AttrIDConstant.TABLE_CELL_HORIZONTAL_MERGED_ID)
        if (a == Int.MIN_VALUE) {
            return false
        }
        return a == 1
    }

    /**
     * set horizontal merged cell
     */
    fun setTableHorMerged(attr: IAttributeSet?, b: Boolean) {
        attr!!.setAttribute(AttrIDConstant.TABLE_CELL_HORIZONTAL_MERGED_ID, if (b) 1 else 0)
    }

    /**
     * get the first horizontal merged cell
     */
    fun isTableVerFirstMerged(attr: IAttributeSet?): Boolean {
        val a = attr!!.getAttribute(AttrIDConstant.TABLE_CELL_VER_FIRST_MERGED_ID)
        if (a == Int.MIN_VALUE) {
            return false
        }
        return a == 1
    }

    /**
     * set first horizontal merged cell
     */
    fun setTableVerFirstMerged(attr: IAttributeSet?, b: Boolean) {
        attr!!.setAttribute(AttrIDConstant.TABLE_CELL_VER_FIRST_MERGED_ID, if (b) 1 else 0)
    }

    /**
     * get horizontal merged cell
     */
    fun isTableVerMerged(attr: IAttributeSet?): Boolean {
        val a = attr!!.getAttribute(AttrIDConstant.TABLE_CELL_VERTICAL_MERGED_ID)
        if (a == Int.MIN_VALUE) {
            return false
        }
        return a == 1
    }

    /**
     * set horizontal merged cell
     */
    fun setTableVerMerged(attr: IAttributeSet?, b: Boolean) {
        attr!!.setAttribute(AttrIDConstant.TABLE_CELL_VERTICAL_MERGED_ID, if (b) 1 else 0)
    }

    /**
     * get table cell vertical alignment
     */
    fun getTableCellVerAlign(attr: IAttributeSet?): Int {
        val a = attr!!.getAttribute(AttrIDConstant.TABLE_CELL_VERTICAL_ALIGN_ID)
        if (a == Int.MIN_VALUE) {
            return 0
        }
        return a
    }

    /**
     * set table cell vertical alignment
     */
    fun setTableCellVerAlign(attr: IAttributeSet?, value: Int) {
        attr!!.setAttribute(AttrIDConstant.TABLE_CELL_VERTICAL_ALIGN_ID, value)
    }

    /**
     * get table top MARGIN
     */
    fun getTableTopMargin(attr: IAttributeSet?): Int {
        val a = attr!!.getAttribute(AttrIDConstant.TABLE_TOP_MARGIN_ID)
        if (a == Int.MIN_VALUE) {
            return 0
        }
        return a
    }

    /**
     * set table top MARGIN
     */
    fun setTableTopMargin(attr: IAttributeSet?, value: Int) {
        attr!!.setAttribute(AttrIDConstant.TABLE_TOP_MARGIN_ID, value)
    }

    /**
     * get table bottom MARGIN
     */
    fun getTableBottomMargin(attr: IAttributeSet?): Int {
        val a = attr!!.getAttribute(AttrIDConstant.TABLE_BOTTOM_MARGIN_ID)
        if (a == Int.MIN_VALUE) {
            return 0
        }
        return a
    }

    /**
     * set table bottom MARGIN
     */
    fun setTableBottomMargin(attr: IAttributeSet?, value: Int) {
        attr!!.setAttribute(AttrIDConstant.TABLE_BOTTOM_MARGIN_ID, value)
    }

    /**
     * get table left MARGIN
     */
    fun getTableLeftMargin(attr: IAttributeSet?): Int {
        val a = attr!!.getAttribute(AttrIDConstant.TABLE_LEFT_MARGIN_ID)
        if (a == Int.MIN_VALUE) {
            return 0
        }
        return a
    }

    /**
     * set table left MARGIN
     */
    fun setTableLeftMargin(attr: IAttributeSet?, value: Int) {
        attr!!.setAttribute(AttrIDConstant.TABLE_LEFT_MARGIN_ID, value)
    }

    /**
     * get table left MARGIN
     */
    fun getTableRightMargin(attr: IAttributeSet?): Int {
        val a = attr!!.getAttribute(AttrIDConstant.TABLE_RIGHT_MARGIN_ID)
        if (a == Int.MIN_VALUE) {
            return 0
        }
        return a
    }

    /**
     * set table left MARGIN
     */
    fun setTableRightMargin(attr: IAttributeSet?, value: Int) {
        attr!!.setAttribute(AttrIDConstant.TABLE_RIGHT_MARGIN_ID, value)
    }

    /**
     * get table left MARGIN
     */
    fun getTableCellTableBackground(attr: IAttributeSet?): Int {
        val a = attr!!.getAttribute(AttrIDConstant.PAGE_BACKGROUND_COLOR_ID)
        if (a == Int.MIN_VALUE) {
            return -1
        }
        return a
    }

    /**
     *
     */
    fun setTableCellBackground(attr: IAttributeSet?, value: Int) {
        attr!!.setAttribute(AttrIDConstant.PAGE_BACKGROUND_COLOR_ID, value)
    }

    //=========================  非属性操作 ==================
    /**
     * 填充页面属性
     * @param pageAttr
     * @param section
     */
    fun fillPageAttr(pageAttr: PageAttr?, attr: IAttributeSet?) {
        pageAttr!!.reset()

        pageAttr.verticalAlign = getPageVerticalAlign(attr)
        pageAttr.horizontalAlign = getPageHorizontalAlign(attr)

        pageAttr.pageWidth = (getPageWidth(attr) * MainConstant.TWIPS_TO_PIXEL).toInt()
        pageAttr.pageHeight = (getPageHeight(attr) * MainConstant.TWIPS_TO_PIXEL).toInt()
        pageAttr.topMargin = (getPageMarginTop(attr) * MainConstant.TWIPS_TO_PIXEL).toInt()
        pageAttr.bottomMargin = (getPageMarginBottom(attr) * MainConstant.TWIPS_TO_PIXEL).toInt()
        pageAttr.rightMargin = (getPageMarginRight(attr) * MainConstant.TWIPS_TO_PIXEL).toInt()
        pageAttr.leftMargin = (getPageMarginLeft(attr) * MainConstant.TWIPS_TO_PIXEL).toInt()
        pageAttr.headerMargin = (getPageHeaderMargin(attr) * MainConstant.TWIPS_TO_PIXEL).toInt()
        pageAttr.footerMargin = (getPageFooterMargin(attr) * MainConstant.TWIPS_TO_PIXEL).toInt()

        pageAttr.pageBRColor = getPageBackgroundColor(attr)
        pageAttr.pageBorder = getPageBorder(attr)
        pageAttr.pageLinePitch = (getPageLinePitch(attr) * MainConstant.TWIPS_TO_PIXEL)

        Log.e("verticalAlign", "" + pageAttr.verticalAlign)
        Log.e("horizontalAlign", "" + pageAttr.horizontalAlign)
        Log.e("pageWidth", "" + pageAttr.pageWidth)
        Log.e("pageHeight", "" + pageAttr.pageHeight)
        Log.e("topMargin", "" + pageAttr.topMargin)
        Log.e("bottomMargin", "" + pageAttr.bottomMargin)
        Log.e("rightMargin", "" + pageAttr.rightMargin)
        Log.e("leftMargin", "" + pageAttr.leftMargin)
        Log.e("headerMargin", "" + pageAttr.headerMargin)
        Log.e("footerMargin", "" + pageAttr.footerMargin)
    }

    /**
     * 填充段落属性
     *
     * @param paraAttr
     * @param para
     */
    fun fillParaAttr(control: IControl?, paraAttr: ParaAttr?, attr: IAttributeSet?) {
        paraAttr!!.reset()
        paraAttr.tabClearPosition = (getParaTabsClearPostion(attr) * MainConstant.TWIPS_TO_PIXEL).toInt()
        paraAttr.leftIndent = (getParaIndentLeft(attr) * MainConstant.TWIPS_TO_PIXEL).toInt()
        paraAttr.rightIndent = (getParaIndentRight(attr) * MainConstant.TWIPS_TO_PIXEL).toInt()
        paraAttr.beforeSpace = (getParaBefore(attr) * MainConstant.TWIPS_TO_PIXEL).toInt()
        paraAttr.afterSpace = (getParaAfter(attr) * MainConstant.TWIPS_TO_PIXEL).toInt()
        // 行距类型
        paraAttr.lineSpaceType = getParaLineSpaceType(attr).toByte()
        // 行距值
        paraAttr.lineSpaceValue = getParaLineSpace(attr)
        //
        if (paraAttr.lineSpaceType == WPAttrConstant.LINE_SAPCE_LEAST
            || paraAttr.lineSpaceType == WPAttrConstant.LINE_SPACE_EXACTLY
        ) {
            paraAttr.lineSpaceValue *= MainConstant.TWIPS_TO_PIXEL
        }
        // 水平对齐
        paraAttr.horizontalAlignment = getParaHorizontalAlign(attr).toByte()
        /*
         * 特殊缩进
         * 规则：firstLineIndent > 0，表示首行缩进
         *       firstLineIndent < 0，表示悬挂缩进
         */
        paraAttr.specialIndentValue = (getParaSpecialIndent(attr) * MainConstant.TWIPS_TO_PIXEL).toInt()
        // listID
        paraAttr.listID = getParaListID(attr)
        //
        paraAttr.listLevel = getParaListLevel(attr).toByte()
        //
        paraAttr.pgBulletID = getPGParaBulletID(attr)

        // 左缩进和特殊缩进需要考虑 bullet and number 因素
        if (paraAttr.listID >= 0 && paraAttr.listLevel >= 0 && control != null) {
            val listData: ListData? = control.getSysKit().getListManage().getListData(paraAttr.listID)
            if (listData != null) {
                val listLevel: ListLevel? = listData.getLevel(paraAttr.listLevel.toInt())
                if (listLevel != null) {
                    // 文本缩进
                    paraAttr.listTextIndent = (listLevel.getTextIndent() * MainConstant.TWIPS_TO_PIXEL).toInt()
                    // bn 对齐位置
                    paraAttr.listAlignIndent = paraAttr.listTextIndent + (listLevel.getSpecialIndent() * MainConstant.TWIPS_TO_PIXEL).toInt()
                    // 段落没有左缩进
                    if (paraAttr.leftIndent - paraAttr.listTextIndent == 0
                        || paraAttr.leftIndent == 0
                    ) {
                        // 如果段落的特殊 = 0，则需要把 bn 的文本缩进设置为段落的悬挂缩进
                        if (paraAttr.specialIndentValue == 0) {
                            paraAttr.specialIndentValue = -(paraAttr.listTextIndent - paraAttr.listAlignIndent)
                        }
                        // 如果是悬挂缩进，需要把对齐位置设置到段浇左缩时
                        if (paraAttr.specialIndentValue < 0) {
                            paraAttr.leftIndent = paraAttr.listAlignIndent
                            paraAttr.listAlignIndent = 0
                        }
                        //
                        else if (paraAttr.listAlignIndent > paraAttr.specialIndentValue) {
                            paraAttr.specialIndentValue += paraAttr.listAlignIndent
                            //paraAttr.listAlignIndent = 0;
                        }
                    }
                    // 段落有左缩进
                    else {
                        // 如果段浇缩进　+ BN 对齐位置　= BN 文本缩时，则段落缩进　=　BN 对齐位置
                        if (paraAttr.leftIndent + paraAttr.listAlignIndent == paraAttr.listTextIndent) {
                            paraAttr.leftIndent = paraAttr.listAlignIndent
                        }
                        // 如果是是首行缩进，则需要把首行缩进值设置给 bn 对齐位置
                        if (paraAttr.specialIndentValue >= 0) {
                            paraAttr.listAlignIndent = paraAttr.specialIndentValue
                        }
                        // 如果是悬挂缩进，则 bn 对齐位置 0
                        else {
                            paraAttr.listAlignIndent = 0
                        }
                        // 如果
                        if (paraAttr.specialIndentValue == 0 && paraAttr.listTextIndent - paraAttr.leftIndent > 0) {
                            paraAttr.specialIndentValue -= paraAttr.listTextIndent - paraAttr.leftIndent
                        }
                    }
                    // 如果制表位清除位置大于等于左缩进，则左缩进设置为0
                    /*if (tabClearPosition >= paraAttr.leftIndent && tabClearPosition > 0)
                    {
                        paraAttr.leftIndent = 0;
                    }
                    // 如果制表位清除位置大于等于左缩进，则左缩进设置为0
                    if (tabClearPosition >= paraAttr.listAlignIndent && tabClearPosition > 0)
                    {
                        paraAttr.listAlignIndent = 0;
                    }
                    // 如果制表位清除位置大于等于左缩进，则左缩进设置为0
                    if (tabClearPosition >= paraAttr.listTextIndent && tabClearPosition > 0)
                    {
                        paraAttr.listTextIndent = 0;
                    }*/
                }
            }
        }
    }

    /**
     * 填充段落属性
     *
     * @param paraAttr
     * @param para
     */
    fun fillCharAttr(charAttr: CharAttr?, paraAttr: IAttributeSet?, leafAttr: IAttributeSet?) {
        charAttr!!.reset()
        charAttr.fontIndex = getFontName(paraAttr, leafAttr)
        charAttr.fontSize = getFontSize(paraAttr, leafAttr)
        charAttr.fontScale = getFontScale(paraAttr, leafAttr)
        charAttr.fontColor = getFontColor(paraAttr, leafAttr)
        charAttr.isBold = getFontBold(paraAttr, leafAttr)
        charAttr.isItalic = getFontItalic(paraAttr, leafAttr)
        charAttr.isStrikeThrough = getFontStrike(paraAttr, leafAttr)
        charAttr.isDoubleStrikeThrough = getFontDoubleStrike(paraAttr, leafAttr)
        charAttr.underlineType = getFontUnderline(paraAttr, leafAttr)
        charAttr.underlineColor = getFontUnderlineColor(paraAttr, leafAttr)
        charAttr.subSuperScriptType = getFontScript(paraAttr, leafAttr).toShort()
        charAttr.highlightedColor = getFontHighLight(paraAttr, leafAttr)
        charAttr.encloseType = getFontEncloseChanacterType(paraAttr, leafAttr).toByte()
        charAttr.pageNumberType = getFontPageNumberType(leafAttr).toByte()
    }

    /**
     *
     * @param tabelAttr
     * @param attr
     */
    fun fillTableAttr(tableAttr: TableAttr?, attr: IAttributeSet?) {
        // 由于POI无法没有解析出表格上、下、左、右边距，故采用默认值
        tableAttr!!.topMargin = 0 //(int)(AttrManage.instance().getTableTopMargin(attr) * MainConstant.TWIPS_TO_PIXEL);
        tableAttr.leftMargin = 7 //(int)(AttrManage.instance().getTableLeftMargin(attr) * MainConstant.TWIPS_TO_PIXEL);
        tableAttr.rightMargin = 7 //(int)(AttrManage.instance().getTableRightMargin(attr) * MainConstant.TWIPS_TO_PIXEL);
        tableAttr.bottomMargin = 0 //(int)(AttrManage.instance().getTableBottomMargin(attr) * MainConstant.TWIPS_TO_PIXEL);
        tableAttr.cellWidth = (getTableCellWidth(attr) * MainConstant.TWIPS_TO_PIXEL).toInt()
        tableAttr.cellVerticalAlign = getTableCellVerAlign(attr).toByte()
        tableAttr.cellBackground = getTableCellTableBackground(attr)
    }

    /**
     *
     */
    fun dispose() {
    }

    companion object {
        @JvmField
        var am = AttrManage()

        /**
         *
         * @param doc
         */
        @JvmStatic
        fun instance(): AttrManage {
            return am
        }
    }
}
