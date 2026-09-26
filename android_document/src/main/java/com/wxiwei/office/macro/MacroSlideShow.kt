/*
 * 文件名称:          MacroSlideShow.java
 *
 * 编译器:            android2.2
 * 时间:              上午9:51:54
 */
package com.wxiwei.office.macro

import com.wxiwei.office.common.ISlideShow

/**
 *
 */
class MacroSlideShow internal constructor(private var listener: SlideShowListener?) : ISlideShow {

    /**
     * exit slideshow
     */
    override fun exit() {
        listener?.exit()
    }

    fun dispose() {
        listener = null
    }
}
