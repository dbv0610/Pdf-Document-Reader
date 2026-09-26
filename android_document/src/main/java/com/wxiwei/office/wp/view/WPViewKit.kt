/*
 * 文件名称:          WPViewKit.java
 *
 * 编译器:            android2.2
 * 时间:              上午9:05:16
 */
package com.wxiwei.office.wp.view

import com.wxiwei.office.constant.wp.WPModelConstant
import com.wxiwei.office.constant.wp.WPViewConstant
import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.simpletext.view.IView
import com.wxiwei.office.simpletext.view.ViewKit
import com.wxiwei.office.wp.control.Word

/**
 * word 布局的工具类
 */
class WPViewKit : ViewKit() {

    companion object {
        //
        private val kit = WPViewKit()

        @JvmStatic
        fun instance(): WPViewKit {
            return kit
        }
    }

    /**
     * 给定坐标，得到页面视图
     * @param root
     * @param x  x值是100%的值
     * @param y  y值是100%的值
     */
    fun getPageView(root: IView?, x: Int, y: Int): PageView? {
        if (root == null) {
            return null
        }
        val space = (root.getContainer() as? Word)?.getPageSpacing() ?: WPViewConstant.PAGE_SPACE.toInt()
        var view = root.getChildView()
        while (view != null) {
            if (y > view.getY() && y < view.getY() + view.getHeight() + space) {
                break
            }
            view = view.getNextView()
        }
        // 没有就返回第一页
        if (view == null) {
            view = root.getChildView()
        }
        return if (view == null) null else view as PageView
    }

    /**
     * get the view of the specified Offset and viewType
     */
    fun getView(word: Word, offset: Long, type: Int, isBack: Boolean): IView? {
        return word.getRoot(word.getCurrentRootType())!!.getView(offset, type, isBack)
    }

    /**
     * get the view of the specified X, Y and viewType
     */
    fun getView(word: Word, x: Int, y: Int, type: Int, isBack: Boolean): IView? {
        return word.getRoot(word.getCurrentRootType())!!.getView(x, y, type, isBack)
    }

    /**
     * 得到指定视图到指定视图的类型的绝对坐标
     */
    fun getAbsoluteCoordinate(view: IView?, type: Int, rect: Rectangle): Rectangle {
        var view = view
        rect.setBounds(0, 0, 0, 0)
        while (view != null && view.getType().toInt() != type) {
            rect.x += view.getX()
            rect.y += view.getY()
            view = view.getParentView()
        }
        return rect
    }

    fun getArea(offset: Long): Long {
        return offset and WPModelConstant.AREA_MASK
    }
}
