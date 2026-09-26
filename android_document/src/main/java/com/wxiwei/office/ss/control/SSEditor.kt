/*
 * 文件名称:          	SSEditor.java
 *
 * 编译器:            android2.2
 * 时间:             	上午4:13:44
 */
package com.wxiwei.office.ss.control

import com.wxiwei.office.common.shape.IShape
import com.wxiwei.office.constant.MainConstant
import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.pg.animate.IAnimation
import com.wxiwei.office.simpletext.control.IHighlight
import com.wxiwei.office.simpletext.control.IWord
import com.wxiwei.office.simpletext.model.IDocument
import com.wxiwei.office.system.IControl

/**
 * TODO: 文件注释
 *
 * Read版本:        	Office engine V1.0
 *
 * 作者:            	ljj8494
 *
 * 日期:            	2013-3-22
 *
 * 负责人:          	ljj8494
 *
 * 负责小组:        	TMC
 */
class SSEditor(ss: Spreadsheet?) : IWord {
    private var ss: Spreadsheet? = ss

    override fun getHighlight(): IHighlight? {
        // TODO Auto-generated method stub
        return null
    }

    override fun modelToView(offset: Long, rect: Rectangle, isBack: Boolean): Rectangle? {
        // TODO Auto-generated method stub
        return null
    }

    override fun getDocument(): IDocument? {
        // TODO Auto-generated method stub
        return null
    }

    override fun getText(start: Long, end: Long): String {
        // TODO Auto-generated method stub
        return ""
    }

    override fun viewToModel(x: Int, y: Int, isBack: Boolean): Long {
        // TODO Auto-generated method stub
        return 0
    }

    override fun getEditType(): Byte {
        return MainConstant.APPLICATION_TYPE_SS
    }

    override fun getParagraphAnimation(pargraphID: Int): IAnimation? {
        // TODO Auto-generated method stub
        return null
    }

    override fun getTextBox(): IShape? {
        // TODO Auto-generated method stub
        return null
    }

    override fun getControl(): IControl {
        // TODO Auto-generated method stub
        return ss!!.getControl()
    }

    override fun dispose() {
        ss = null
    }
}
