/*
 * 文件名称:          IWord.java
 *
 * 编译器:            android2.2
 * 时间:              上午9:56:16
 */
package com.wxiwei.office.simpletext.control

import com.wxiwei.office.common.shape.IShape
import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.pg.animate.IAnimation
import com.wxiwei.office.simpletext.model.IDocument
import com.wxiwei.office.system.IControl

/**
 * IWord 接口
 *
 * Read版本:        Read V1.0
 *
 * 作者:            ljj8494
 *
 * 日期:            2012-7-27
 *
 * 负责人:          ljj8494
 */
interface IWord {
    /**
     * @return Returns the highlight.
     */
    fun getHighlight(): IHighlight?

    /**
     *
     */
    fun modelToView(offset: Long, rect: Rectangle, isBack: Boolean): Rectangle?

    /**
     *
     */
    fun getDocument(): IDocument?

    /**
     *
     */
    fun getText(start: Long, end: Long): String?

    /**
     * @param x 为100%的值
     * @param y 为100%的值
     */
    fun viewToModel(x: Int, y: Int, isBack: Boolean): Long

    /**
     *
     */
    fun getEditType(): Byte

    /**
     *
     * @param para
     * @return
     */
    fun getParagraphAnimation(pargraphID: Int): IAnimation?

    /**
     *
     */
    fun getTextBox(): IShape?

    /**
     *
     */
    fun getControl(): IControl?

    /**
     *
     */
    fun dispose()
}
