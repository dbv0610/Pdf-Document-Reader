/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
/*
 * 文件名称:          ICustomDialog.java
 *  
 * 编译器:            android2.2
 * 时间:              下午12:47:14
 */
package com.wxiwei.office.common

/**
 * TODO: 文件注释
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
 * 日期:            2012-12-21
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
interface ICustomDialog {
    /**
     * 
     * @param type dialog type
     */
    fun showDialog(type: Byte)

    /**
     * 
     * @param type
     */
    fun dismissDialog(type: Byte)

    companion object {
        //password dialog
        const val DIALOGTYPE_PASSWORD: Byte = 0
        //txt encode dialog
        const val DIALOGTYPE_ENCODE: Byte = 1
        //loading dialog
        const val DIALOGTYPE_LOADING: Byte = 2
        //error dialog
        const val DIALOGTYPE_ERROR: Byte = 3
        //
        const val DIALOGTYPE_FIND: Byte = 4
    }
}
