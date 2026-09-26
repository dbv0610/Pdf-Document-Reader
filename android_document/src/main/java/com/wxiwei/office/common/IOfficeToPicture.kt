/*
 * 文件名称:          OfficeToPicture.java
 *  
 * 编译器:            android2.2
 * 时间:              下午2:15:37
 */
package com.wxiwei.office.common

import android.graphics.Bitmap

/**
 * Office 文档转换成图片的接口
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
 * 日期:            2012-5-14
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
interface IOfficeToPicture {
    /**
     * 生成picture模式
     * 
     * @ return  = 0，视图发生变化时生成picture，例如滚动、fling进行中，
     * = 1，视图发生变化结束后，例如滚动、fling、横竖屏切换结束后。
     */
    /**
     * set mode type
     * @param modeType
     */
    var modeType: Byte

    /**
     * 获得converter to picture的Bitmap，如果返回空，则不生成picture
     * 
     * @param visibleWidth  engine组件的可视图宽度
     * @param visibleHeight engine组件的可视图高度
     * @return Bitmap    Bitmap实例
     */
    fun getBitmap(visibleWidth: Int, visibleHeight: Int): Bitmap?

    /**
     * picture 绘制完成，回调方法
     * 
     * @param bitmap  绘制好的图片
     */
    fun callBack(bitmap: Bitmap?)

    /**
     * 是否以zoom方式生成picture，此zoom是指office engine的size 和 bitmap size 之间的zoom。
     * 例如 engine的size 1280 * 768，而bitmap size要求是845 * 480，这样的情况是否需要zoom。
     * 
     * @return   true    do zoom
     * false   don’t zoom
     */
    val isZoom: Boolean

    /**
     * 
     */
    fun dispose()

    companion object {
        // 实现IOfficeToPicture接口的类路径
        const val INSTANCE_CLASS_PATH: String = "com.wxiwei.office.officereader.OfficeToPicture"

        // 视图发生变化时生成picture，例如滚动、fling进行中
        const val VIEW_CHANGING: Byte = 0
        // 视图发生变化结束后，例如滚动、fling、横竖屏切换结束后
        const val VIEW_CHANGE_END: Byte = 1
    }
}
