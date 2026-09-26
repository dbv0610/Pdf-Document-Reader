/*
 * 文件名称:          IRoot.java
 *
 * 编译器:            android2.2
 * 时间:              下午5:19:22
 */
package com.wxiwei.office.simpletext.view

/**
 * 定义视图接口方法
 *
 * Read版本:        Read V1.0
 *
 * 作者:            ljj8494
 *
 * 日期:            2011-11-14
 *
 * 负责人:          ljj8494
 */
interface IRoot {

    /**
     * 是否可以后台
     *
     * @return
     */
    fun canBackLayout(): Boolean

    /**
     * 后台布局
     */
    fun backLayout()

    /**
     *
     */
    fun getViewContainer(): ViewContainer?

    companion object {
        //the min layout width
        const val MINLAYOUTWIDTH = 5
    }
}
