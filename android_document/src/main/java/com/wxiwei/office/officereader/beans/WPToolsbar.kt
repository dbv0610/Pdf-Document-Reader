package com.wxiwei.office.officereader.beans

import android.content.Context
import android.view.View
import com.wxiwei.office.constant.EventConstant
import com.wxiwei.office.constant.wp.WPViewConstant
import com.wxiwei.office.system.IControl
import com.wxiwei.office.wp.control.Word

class WPToolsbar(content: Context?, control: IControl?) : AToolsbar(content, control) {
    /**
     * @param control
     */
    init {
        init()
    }

    /**
     * 
     */
    private fun init() {
        //copy
//        addButton(R.drawable.file_copy, R.drawable.file_copy_disable,
//            R.string.file_toolsbar_copy, EventConstant.FILE_COPY_ID, true);
//
//        // 查找
//        addButton(R.drawable.app_find, R.drawable.app_find_disable,
//            R.string.app_toolsbar_find, EventConstant.APP_FIND_ID, true);
//
//        // 选择文本
//        //addButton(R.drawable.wp_select_text, R.drawable.wp_select_text_disable,
//        //    R.string.wp_toolsbar_select_text, EventConstant.WP_SELECT_TEXT_ID, true);
//
//        // 非文本文件才需要视图切换
//        //if (!((String)control.getActionValue(EventConstant.SYS_FILEPAHT_ID)).endsWith(MainConstant.FILE_TYPE_TXT))
//        {
//           // 视图切换
//            addButton(R.drawable.wp_switch_view, R.drawable.wp_switch_view_disable,
//                R.string.wp_toolsbar_switch_view, EventConstant.WP_SWITCH_VIEW, true);
//            // print mode
//            addButton(R.drawable.app_print_n, R.drawable.app_print_d,
//                R.string.wp_toolsbar_print_mode, EventConstant.WP_PRINT_MODE, true);
//        }
//
//        // 分享
//        addButton(R.drawable.file_share, R.drawable.file_share_disable,
//            R.string.file_toolsbar_share, EventConstant.APP_SHARE_ID, true);
//
//        // 联网搜索
//        addButton(R.drawable.app_internet_search, R.drawable.app_internet_search_disable,
//            R.string.app_toolsbar_internet_search, EventConstant.APP_INTERNET_SEARCH_ID, true);
//
//        // 标星
//        addCheckButton(R.drawable.file_star_check, R.drawable.file_star_uncheck,R.drawable.file_star_disable,
//            R.string.file_toolsbar_mark_star, R.string.file_toolsbar_unmark_star,
//            EventConstant.FILE_MARK_STAR_ID, true);
//
//        // 标签
//        addButton(R.drawable.app_drawing, R.drawable.app_drawing_disable,
//            R.string.app_toolsbar_draw, EventConstant.APP_DRAW_ID, true);
//
        // 朗读
        /*addButton(R.drawable.app_read, R.drawable.app_read_disable, 
            R.string.app_toolsbar_read, EventConstant.APP_READ_ID, true);*/

        // 签批
        /*addButton(R.drawable.app_approve, R.drawable.app_approve_disable, 
            R.string.app_toolsbar_approve, EventConstant.APP_APPROVE_ID, true);*/

        // 生成图片 
        //addButton(R.drawable.app_approve, R.drawable.app_approve_disable, 
        //    R.string.app_toolsbar_generated_picture, EventConstant.APP_GENERATED_PICTURE_ID, true);

        // 帮助
        //addButton(R.drawable.file_help, -1, R.string.sys_menu_help, EventConstant.SYS_HELP_ID, false);
    }

    /**
     * 
     */
    override fun updateStatus() {
        val word = control?.getView() as? Word ?: return

        // 查找
        //setEnabled(EventConstant.APP_FIND_ID, true);

        // 选择文本
        //setEnabled(EventConstant.WP_SELECT_TEXT_ID, true);

        // 非文本文件才需要视图切换
        /*if (!((String)control.getActionValue(EventConstant.SYS_FILEPAHT_ID)).endsWith(MainConstant.FILE_TYPE_TXT))
        {
           // 视图切换
            setEnabled(EventConstant.WP_SWITCH_VIEW, true);
        }*/

        // 分享
        //setEnabled(EventConstant.APP_SHARE_ID, true);

        // 联网搜索
        setEnabled(EventConstant.APP_INTERNET_SEARCH_ID, word.getHighlight().isSelectText())
        // copy
        setEnabled(EventConstant.FILE_COPY_ID, word.getHighlight().isSelectText())

        // 标星
        //setEnabled(EventConstant.FILE_MARK_STAR_ID, true);

        // 朗读
        //setEnabled(EventConstant.APP_READ_ID, true);

        // 签批
        //setEnabled( EventConstant.APP_APPROVE_ID, true);

        // 帮助
        //setEnabled(EventConstant.SYS_HELP_ID, true);
        // 标注        
        setEnabled(
            EventConstant.APP_DRAW_ID,
            word != null && word.getCurrentRootType() == WPViewConstant.PRINT_ROOT.toInt()
        )
    }

    /**
     * 单击事件
     */
    fun onClick(v: View?) {
        if (v is AImageButton) {
            val b = v
            val action = b.actionID
            when (action) {
                EventConstant.WP_SELECT_TEXT_ID -> control?.actionEvent(action, null)
                else -> control?.actionEvent(action, null)
            }
        }
    }

    /**
     * 
     */
    override fun dispose() {
        super.dispose()
    }
}
