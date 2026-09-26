/*
 * 文件名称:           MainControl.java
 *
 * 编译器:             android2.2
 * 时间:               下午1:34:44
 */
package com.wxiwei.office.officereader.filelist

import android.content.Context
import android.graphics.drawable.Drawable
import android.provider.Settings
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.wxiwei.office.constant.MainConstant
import com.wxiwei.office.system.IControl
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Hashtable
import java.util.Locale

/**
 * 文件注释
 * 
 * 
 * 
 * 
 * Read版本:        Read V1.0
 * 
 * 
 * 作者:            梁金晶
 * 
 * 
 * 日期:            2011-10-31
 * 
 * 
 * 负责人:          梁金晶
 * 
 * 
 * 负责小组:
 * 
 * 
 * 
 * 
 */
class FileItemAdapter(
    context: Context, //
    private var control: IControl?
) : BaseAdapter() {
    /**
     * @param it
     */
    fun addItem(it: FileItem?) {
        mItems!!.add(it!!)
    }

    fun setListItems(fileItem: MutableList<FileItem>?) {
        mItems = fileItem
    }

    /**
     * 
     */
    override fun getCount(): Int {
        return mItems!!.size
    }

    /**
     * 
     */
    override fun getItem(position: Int): Any? {
        return mItems!!.get(position)
    }

    /**
     * @return
     */
    fun areAllItemsSelectable(): Boolean {
        return false
    }

    /**
     * @param position
     * @return
     */
    fun isSelectable(position: Int): Boolean {
        return mItems!!.get(position).isCheck()
    }

    /**
     * 
     */
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    /**
     * (non-Javadoc)
     * 
     * @see BaseAdapter.isEmpty
     */
    override fun isEmpty(): Boolean {
        return mItems!!.size == 0
    }

    /**
     * 
     */
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        var convertView = convertView
        val control = control ?: return convertView
        val fileItem = mItems!!.get(position)
        if (fileItem == null) {
            return null
        }
        if (convertView == null || convertView.getWidth() != parent.getContext().getResources()
                .getDisplayMetrics().widthPixels
        ) {
            convertView = FileItemView(
                control.getActivity().getApplicationContext(),
                control,
                this,
                fileItem
            )
        } else {
            (convertView as FileItemView).updateFileItem(fileItem, this)
        }
        return convertView
    }

    /**
     * @param time
     * @return
     */
    fun formatDate(time: Long): String {
        calendar.setTimeInMillis(time)
        return if (is24Hour) sdf_24.format(calendar.getTime()) else sdf_12.format(calendar.getTime())
    }

    /**
     * 
     */
    fun formatSize(size: Long): String {
        var str = ""
        if (size == 0L) {
            return "0B"
        }

        if (size >= GB) {
            str += df.format((size.toFloat() / GB).toDouble()) + "GB"
        } else if (size >= MB) {
            str += df.format((size.toFloat() / MB).toDouble()) + "MB"
        } else if (size >= KB) {
            str += df.format((size.toFloat() / KB).toDouble()) + "KB"
        } else {
            str += size.toString() + " B"
        }
        return str
    }

    /**
     * @param icontType
     * @return
     */
    fun getIconDrawable(icontType: Int): Drawable? {
        return iconMap!!.get(icontType)
    }

    /**
     * get file icon type
     * 
     * @param fileName
     * @return
     */
    fun getFileIconType(fileName: String): Int {
        var fileName = fileName
        fileName = fileName.lowercase(Locale.getDefault())
        // doc
        if (fileName.endsWith(MainConstant.FILE_TYPE_DOC)
            || fileName.endsWith(MainConstant.FILE_TYPE_DOT)
        ) {
            return ICON_TYPE_DOC
        } else if (fileName.endsWith(MainConstant.FILE_TYPE_DOCX)
            || fileName.endsWith(MainConstant.FILE_TYPE_DOTX)
            || fileName.endsWith(MainConstant.FILE_TYPE_DOTM)
        ) {
            return ICON_TYPE_DOCX
        } else if (fileName.endsWith(MainConstant.FILE_TYPE_XLS)
            || fileName.endsWith(MainConstant.FILE_TYPE_XLT)
        ) {
            return ICON_TYPE_XSL
        } else if (fileName.endsWith(MainConstant.FILE_TYPE_XLSX)
            || fileName.endsWith(MainConstant.FILE_TYPE_XLTX)
            || fileName.endsWith(MainConstant.FILE_TYPE_XLTM)
            || fileName.endsWith(MainConstant.FILE_TYPE_XLSM)
        ) {
            return ICON_TYPE_XLSX
        } else if (fileName.endsWith(MainConstant.FILE_TYPE_PPT)
            || fileName.endsWith(MainConstant.FILE_TYPE_POT)
        ) {
            return ICON_TYPE_PPT
        } else if (fileName.endsWith(MainConstant.FILE_TYPE_PPTX)
            || fileName.endsWith(MainConstant.FILE_TYPE_PPTM)
            || fileName.endsWith(MainConstant.FILE_TYPE_POTX)
            || fileName.endsWith(MainConstant.FILE_TYPE_POTM)
        ) {
            return ICON_TYPE_PPTX
        } else if (fileName.endsWith(MainConstant.FILE_TYPE_PDF)) {
            return ICON_TYPE_PDF
        } else if (fileName.endsWith(MainConstant.FILE_TYPE_TXT)) {
            return ICON_TYPE_TXT
        }

        return -1
    }

    /**
     * 释放内存
     */
    fun dispose() {
        control = null
        if (mItems != null) {
            for (item in mItems) {
                item.dispose()
            }
            mItems!!.clear()
            mItems = null
        }
        if (iconMap != null) {
            iconMap!!.clear()
            iconMap = null
        }
    }

    // 是否24小时制
    private val is24Hour: Boolean

    //
    private var mItems: MutableList<FileItem>? = null

    // 文件列表选用的icon
    private var iconMap: MutableMap<Int?, Drawable?>?

    /**
     * @param context
     */
    init {
        this.is24Hour = "24" == Settings.System.getString(
            context.getContentResolver(),
            Settings.System.TIME_12_24
        )

        val res = context.getResources()
        iconMap = Hashtable<Int?, Drawable?>()
        // folder        
//        iconMap.put(ICON_TYPE_FOLDER, res.getDrawable(R.drawable.file_folder));
//        // doc
//        iconMap.put(ICON_TYPE_DOC, res.getDrawable(R.drawable.file_doc));
//        // docx
//        iconMap.put(ICON_TYPE_DOCX, res.getDrawable(R.drawable.file_docx));
//        // xls
//        iconMap.put(ICON_TYPE_XSL, res.getDrawable(R.drawable.file_xls));
//        // xlsx
//        iconMap.put(ICON_TYPE_XLSX, res.getDrawable(R.drawable.file_xlsx));
//        // ppt
//        iconMap.put(ICON_TYPE_PPT, res.getDrawable(R.drawable.file_ppt));
//        // pptx
//        iconMap.put(ICON_TYPE_PPTX, res.getDrawable(R.drawable.file_pptx));
//        // txt
//        iconMap.put(ICON_TYPE_TXT, res.getDrawable(R.drawable.file_txt));
//        // stat
//        iconMap.put(ICON_TYPE_STAR, res.getDrawable(R.drawable.file_icon_star));
//        // pdf
//        iconMap.put(ICON_TYPE_PDF, res.getDrawable(R.drawable.file_pdf));
    }

    companion object {
        private val calendar: Calendar = Calendar.getInstance()

        // 数值格式化
        private val df = DecimalFormat("#0.00")

        // 日期格式 分为24和12小时制
        private val sdf_24 = SimpleDateFormat("yyyy-MM-dd hh:mm")
        private val sdf_12 = SimpleDateFormat("yyyy-MM-dd a hh:mm")

        // 目录
        const val ICON_TYPE_FOLDER: Int = 0 // 0

        // doc
        val ICON_TYPE_DOC: Int = ICON_TYPE_FOLDER + 1 // 1

        // docx
        val ICON_TYPE_DOCX: Int = ICON_TYPE_DOC + 1 // 2

        // xls
        val ICON_TYPE_XSL: Int = ICON_TYPE_DOCX + 1 // 3

        // xlsx
        val ICON_TYPE_XLSX: Int = ICON_TYPE_XSL + 1 // 4

        // ppt
        val ICON_TYPE_PPT: Int = ICON_TYPE_XLSX + 1 // 5

        // pptx
        val ICON_TYPE_PPTX: Int = ICON_TYPE_PPT + 1 // 6

        // txt
        val ICON_TYPE_TXT: Int = ICON_TYPE_PPTX + 1 // 7

        // 
        val ICON_TYPE_STAR: Int = ICON_TYPE_TXT + 1 // 8

        //
        val ICON_TYPE_PDF: Int = ICON_TYPE_STAR + 1 // 9 

        // GB
        private val GB = 1024 * 1024 * 1024

        // MB
        private val MB = 1024 * 1024

        // KB
        private const val KB = 1024
    }
}
