/*
 * 文件名称:          FileItemView.java
 *  
 * 编译器:            android2.2
 * 时间:              下午12:39:26
 */
package com.wxiwei.office.officereader.filelist

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.text.TextUtils
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.wxiwei.office.constant.MainConstant
import com.wxiwei.office.system.IControl

/**
 * 文件列表视图
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
 * 日期:            2011-11-30
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
class FileItemView(context: Context?, control: IControl, fia: FileItemAdapter, fileItem: FileItem) :
    LinearLayout(context) {
    /**
     * 
     */
    private fun init(control: IControl, fileItem: FileItem, fia: FileItemAdapter) {
        val res = getResources()
        val context = getContext()
        val opts = BitmapFactory.Options()
        opts.inJustDecodeBounds = true


        // 图标
//        BitmapFactory.decodeResource(res, R.drawable.file_doc, opts);
        val iconWidth: Int = opts.outWidth + GAP * 2
        val iconHeight: Int = opts.outHeight + GAP * 4
        var params = LayoutParams(iconWidth, iconHeight)
        params.leftMargin = GAP
        params.rightMargin = GAP
        params.topMargin = GAP
        params.bottomMargin = GAP
        params.gravity = Gravity.CENTER
        icon = ImageView(context)
        icon!!.setImageDrawable(fia.getIconDrawable(fileItem.getIconType()))
        addView(icon, params)


        // 文件属性
        val propWidth: Int = mWidth - iconWidth * 2 - GAP * 6
        val propHeight = iconHeight
        val fileProp = LinearLayout(context)
        fileProp.setOrientation(VERTICAL)
        params.gravity = Gravity.CENTER_VERTICAL
        params = LayoutParams(propWidth, propHeight)
        addView(fileProp, params)


        //文件属性上半部
//        BitmapFactory.decodeResource(res, R.drawable.file_star, opts);
        val filePropTop = RelativeLayout(context)
        params = LayoutParams(propWidth, propHeight / 2)
        params.topMargin = GAP * 3
        params.gravity = Gravity.CENTER_VERTICAL
        fileProp.addView(filePropTop, params)


        // 文件名称
        fileName = TextView(context)
        fileName!!.setSingleLine(true)
        fileName!!.setEllipsize(TextUtils.TruncateAt.END)
        fileName!!.setText(fileItem.getFileName())
        params = LayoutParams(propWidth - opts.outWidth - GAP * 2, LayoutParams.WRAP_CONTENT)
        params.gravity = Gravity.LEFT
        filePropTop.addView(fileName, params)


        // 文件标星
        fileStar = ImageView(context)
        var reParams = RelativeLayout.LayoutParams(opts.outWidth, opts.outHeight)
        reParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE)
        if (fileItem.getFileStar() > 0) {
            fileStar!!.setImageDrawable(fia.getIconDrawable(FileItemAdapter.Companion.ICON_TYPE_STAR))
        }
        filePropTop.addView(fileStar, reParams)


        //文件属性下半部
        val filePropBotom = RelativeLayout(context)
        params = LayoutParams(propWidth, propHeight / 2)
        params.gravity = Gravity.CENTER_VERTICAL
        fileProp.addView(filePropBotom, params)


        // 文件建立日期
        fileCreateDate = TextView(context)
        fileCreateDate!!.setSingleLine(true)
        fileCreateDate!!.setEllipsize(TextUtils.TruncateAt.END)
        fileCreateDate!!.setText(fia.formatDate(fileItem.getFile()!!.lastModified()))
        params = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        params.gravity = Gravity.LEFT
        filePropBotom.addView(fileCreateDate, params)


        // 文件大小
        fileSize = TextView(context)
        reParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        reParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE)
        fileSize!!.setText(
            if (fileItem.getFile()!!.isDirectory()) "" else fia.formatSize(
                fileItem.getFile()!!.length()
            )
        )
        filePropBotom.addView(fileSize, reParams)


        // 文件选择框
        if (fileItem.isShowCheckView()) {
            checkBox = FileCheckBox(context, control, fileItem)
            params = LayoutParams(iconWidth + GAP * 6, iconHeight + GAP * 2)
            params.gravity = Gravity.CENTER_VERTICAL
            params.leftMargin = GAP
            params.rightMargin = GAP
            addView(checkBox, params)
        }
    }

    /**
     * 
     */
    fun updateFileItem(fileItem: FileItem, fia: FileItemAdapter) {
        // 图标
        icon!!.setImageDrawable(fia.getIconDrawable(fileItem.getIconType()))

        // 文件名称
        fileName!!.setText(fileItem.getFileName())

        // 文件标星
        if (fileItem.getFileStar() > 0) {
            fileStar!!.setImageDrawable(fia.getIconDrawable(FileItemAdapter.Companion.ICON_TYPE_STAR))
        } else {
            fileStar!!.setImageDrawable(null)
        }
        // 文件建立日期
        fileCreateDate!!.setText(fia.formatDate(fileItem.getFile()!!.lastModified()))
        // 文件大小
        fileSize!!.setText(
            if (fileItem.getFile()!!.isDirectory()) "" else fia.formatSize(
                fileItem.getFile()!!.length()
            )
        )
        // 文件选择框
        checkBox!!.setFileItem(fileItem)
    }

    /**
     * set file selected state
     */
    override fun setSelected(isSelected: Boolean) {
        checkBox!!.setChecked(isSelected)
    }

    /**
     * 
     */
    fun dispose() {
        icon = null
        fileName = null
        fileStar = null
        fileCreateDate = null
        fileSize = null
        if (checkBox != null) {
            checkBox!!.dispose()
            checkBox = null
        }
    }

    // 屏幕宽度
    private val mWidth: Int

    // 
    //private IControl control;
    //
    private var icon: ImageView? = null

    //
    private var fileName: TextView? = null

    //
    private var fileStar: ImageView? = null

    //
    private var fileCreateDate: TextView? = null

    //
    private var fileSize: TextView? = null

    //
    private var checkBox: FileCheckBox? = null

    /**
     * 
     * 
     */
    init {
        setOrientation(HORIZONTAL)
        //setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        //this.control = control;
        mWidth = getResources().getDisplayMetrics().widthPixels
        init(control, fileItem, fia)
    }

    companion object {
        private val GAP = MainConstant.GAP

        //
        val PUSH_COLOR: Int = Color.rgb(195, 255, 100)
    }
}
