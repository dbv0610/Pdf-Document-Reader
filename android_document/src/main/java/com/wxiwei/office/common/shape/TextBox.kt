/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
/*
 * 文件名称:          TextBox.java
 *  
 * 编译器:            android2.2
 * 时间:              下午7:55:23
 */
package com.wxiwei.office.common.shape

import com.wxiwei.office.simpletext.model.SectionElement
import com.wxiwei.office.simpletext.view.STRoot

/**
 * text box shape
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
 * 日期:            2012-2-14
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
class TextBox : AbstractShape() {
    override val type: Short
        get() = AbstractShape.Companion.SHAPE_TEXTBOX


    /**
     * set data of this shape
     */
    override var data: Any?
        get() = element
        set(value) {
            if (value is SectionElement) element = value
        }

    /**
     * dispose
     */
    override fun dispose() {
        super.dispose()
        if (this.element != null) {
            element!!.dispose()
            this.element = null
        }
    }

    /**
     * @return Returns the isWrapLine.
     */
    /**
     * @param isWrapLine The isWrapLine to set.
     */
    @JvmField
    var isWrapLine: Boolean = false
    /**
     * @return Returns the isEditor.
     */
    /**
     * @param isEditor The isEditor to set.
     */
    //
    var isEditor: Boolean = false
    /**
     * get element of this text box
     */
    /**
     * set element of this text box
     */
    //
    var element: SectionElement? = null
    /**
     * @return Returns the rootView.
     */
    /**
     * @param rootView The rootView to set.
     */
    //
    var rootView: STRoot? = null
    /**
     * 
     * @return
     */
    /**
     * 
     * @param mcType
     */
    var mcType: Byte = MC_None
    var isWordArt: Boolean = false

    init {
        placeHolderID = -1
    }

    companion object {
        //Meta Characters type
        const val MC_None: Byte = 0
        const val MC_SlideNumber: Byte = 1
        const val MC_DateTime: Byte = 2
        const val MC_GenericDate: Byte = 3
        const val MC_Footer: Byte = 4
        const val MC_RTFDateTime: Byte = 5
    }
}
