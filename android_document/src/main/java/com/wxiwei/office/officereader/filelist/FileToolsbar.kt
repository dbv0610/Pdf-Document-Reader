package com.wxiwei.office.officereader.filelist

import android.content.Context
import android.util.AttributeSet
import com.wxiwei.office.constant.EventConstant
import com.wxiwei.office.officereader.FileListActivity
import com.wxiwei.office.officereader.beans.AToolsbar
import com.wxiwei.office.system.IControl

class FileToolsbar : AToolsbar {
    /**
     * 构选器
     * @param control
     */
    constructor(content: Context?, control: IControl?) : super(content, control) {
        init()
    }

    /**
     * 
     * @param context
     * @param attrs
     */
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    /**
     * 菜单项
     */
    fun init() {
        // 新建文件夹
//        addButton(R.drawable.file_createfolder, R.drawable.file_createfolder_disable,
//            R.string.file_toolsbar_create_folder, EventConstant.FILE_CREATE_FOLDER_ID, true);
//
//        // 重命名
//        addButton(R.drawable.file_rename, R.drawable.file_rename_disable,
//            R.string.file_toolsbar_rename, EventConstant.FILE_RENAME_ID, true);
//
//        // 复制
//        addButton(R.drawable.file_copy, R.drawable.file_copy_disable,
//            R.string.file_toolsbar_copy, EventConstant.FILE_COPY_ID, true);
//
//        // 剪切
//        addButton(R.drawable.file_cut, R.drawable.file_cut_disable,
//            R.string.file_toolsbar_cut, EventConstant.FILE_CUT_ID, true);
//
//        // 粘贴
//        addButton(R.drawable.file_paste, R.drawable.file_paste_disable,
//            R.string.file_toolsbar_paste, EventConstant.FILE_PASTE_ID, true);
//
//        // 删除
//        addButton(R.drawable.file_delete, R.drawable.file_delete_disable,
//            R.string.file_toolsbar_delete, EventConstant.FILE_DELETE_ID, true);
//
//        // 搜索
//        addButton(R.drawable.file_search, R.drawable.file_search_disable,
//            R.string.sys_button_search, EventConstant.SYS_SEARCH_ID, true);
//
//        // 分享
//        addButton(R.drawable.file_share, R.drawable.file_share_disable,
//            R.string.file_toolsbar_share, EventConstant.FILE_SHARE_ID, true);
//
//        // 排序
//        addButton(R.drawable.file_sort, R.drawable.file_sort_disable,
//            R.string.file_toolsbar_sort, EventConstant.FILE_SORT_ID, true);
//
//        // 标星
//        addButton(R.drawable.file_star, R.drawable.file_star_disable,
//            R.string.file_toolsbar_mark_star, EventConstant.FILE_MARK_STAR_ID, true);
//
        // 打印        
        //addButton(R.drawable.file_print, R.drawable.file_print_disable, 
        //    R.string.file_toolsbar_print, EventConstant.FILE_PRINT_ID, true);

        // 帮助
        //addButton(R.drawable.file_help, -1, R.string.sys_menu_help, EventConstant.SYS_HELP_ID, false);
    }


    /**
     * 
     */
    public override fun updateStatus() {
        val activity = control!!.getActivity() as FileListActivity


        // if current directory is "/mnt"，do nothing
        if (activity.isCurrentDirectory4mnt()) {
            // create folder
            setEnabled(EventConstant.FILE_CREATE_FOLDER_ID, false)

            // rename
            setEnabled(EventConstant.FILE_RENAME_ID, false)

            // copy
            setEnabled(EventConstant.FILE_COPY_ID, false)

            // cut
            setEnabled(EventConstant.FILE_CUT_ID, false)

            // paste
            setEnabled(EventConstant.FILE_PASTE_ID, false)

            // delete
            setEnabled(EventConstant.FILE_DELETE_ID, false)

            // search
            setEnabled(EventConstant.SYS_SEARCH_ID, true)

            // short
            setEnabled(EventConstant.FILE_SHARE_ID, false)

            // sort       
            setEnabled(EventConstant.FILE_SORT_ID, false)

            // mark    
            setEnabled(EventConstant.FILE_MARK_STAR_ID, false)
            // 打印        
            //setEnabled(EventConstant.FILE_PRINT_ID, false);            
            // 帮助
            //setEnabled(EventConstant.SYS_HELP_ID, true);
            return
        }


        //
        val selectFileItem = activity.getSelectFileItem()
        val nCount = selectFileItem.size
        val listType = activity.getListType()
        if (listType == FileListActivity.LIST_TYPE_EXPLORE) {
            // update new folder button state
            setEnabled(EventConstant.FILE_CREATE_FOLDER_ID, true)
            // update file rename button state
            setEnabled(EventConstant.FILE_RENAME_ID, nCount == 1)

            // copy
            setEnabled(EventConstant.FILE_COPY_ID, nCount > 0)
            // cut
            setEnabled(EventConstant.FILE_CUT_ID, nCount > 0)
            // update file paste button state
            setEnabled(EventConstant.FILE_PASTE_ID, activity.isCopy() || activity.isCut())
            // delete
            setEnabled(EventConstant.FILE_DELETE_ID, nCount > 0)

            // search
            setEnabled(EventConstant.SYS_SEARCH_ID, activity.getCurrentDirectoryFileSize() > 0)
            // share
            var shareEnabled = nCount > 0
            if (shareEnabled) {
                for (j in 0..<nCount) {
                    if (selectFileItem.get(j)!!.getFile()!!.isDirectory()) {
                        shareEnabled = false
                        break
                    }
                }
            }
            setEnabled(EventConstant.FILE_SHARE_ID, shareEnabled)
            // sort     
            setEnabled(EventConstant.FILE_SORT_ID, activity.getCurrentDirectoryFileSize() > 1)
            // mark
            setEnabled(
                EventConstant.FILE_MARK_STAR_ID,
                nCount == 1 && selectFileItem.get(0)!!.getFile()!!.isFile()
            )
        } else {
            // update new folder button state
            setEnabled(EventConstant.FILE_CREATE_FOLDER_ID, false)
            // update file rename button state
            setEnabled(EventConstant.FILE_RENAME_ID, false)

            // copy
            setEnabled(EventConstant.FILE_COPY_ID, false)
            // cut
            setEnabled(EventConstant.FILE_CUT_ID, false)
            // paste
            setEnabled(EventConstant.FILE_PASTE_ID, false)
            // delete
            setEnabled(EventConstant.FILE_DELETE_ID, false)

            // search
            setEnabled(EventConstant.SYS_SEARCH_ID, activity.getCurrentDirectoryFileSize() > 0)
            // share
            setEnabled(EventConstant.FILE_SHARE_ID, nCount > 0)
            // sort     
            setEnabled(EventConstant.FILE_SORT_ID, activity.getCurrentDirectoryFileSize() > 1)
            // mark
            setEnabled(
                EventConstant.FILE_MARK_STAR_ID,
                nCount == 1 && selectFileItem.get(0)!!.getFile()!!.isFile()
            )
        }
    }

    /**
     * 
     * 
     */
    public override fun dispose() {
        super.dispose()
    }
}
