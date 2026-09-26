package com.wxiwei.office.officereader.filelist

import java.io.File

class FileItem(
    private var file: File?,
    private val iconType: Int,
    private var fileStar: Int
) : Comparable<FileItem> {
    private var showCheckView = true
    private var isCheck = false

    fun getFileName(): String = getFile()!!.name
    override fun compareTo(other: FileItem): Int = getFileName().compareTo(other.getFileName(), ignoreCase = true)
    fun getFile(): File? = file
    fun getIconType(): Int = iconType
    fun setCheck(isCheck: Boolean) { this.isCheck = isCheck }
    fun isCheck(): Boolean = isCheck
    fun getFileStar(): Int = fileStar
    fun setFileStar(fileStar: Int) { this.fileStar = fileStar }
    fun isShowCheckView(): Boolean = showCheckView
    fun setShowCheckView(showCheckView: Boolean) { this.showCheckView = showCheckView }
    fun dispose() { file = null }
}
