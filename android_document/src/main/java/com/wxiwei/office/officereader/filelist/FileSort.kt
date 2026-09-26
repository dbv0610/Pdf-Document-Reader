/*
 * 文件名称:           FileSort.java
 *  
 * 编译器:             android2.2
 * 时间:               下午4:54:20
 */
package com.wxiwei.office.officereader.filelist

import com.wxiwei.office.constant.MainConstant
import java.util.Locale

/**
 * 文件排序
 * 
 * 
 * 
 * 
 * Read版本:       Read V1.0
 * 
 * 
 * 作者:           jhy1790
 * 
 * 
 * 日期:           2011-12-19
 * 
 * 
 * 负责人:         jhy1790
 * 
 * 
 * 负责小组:
 * 
 * 
 * 
 * 
 */
class FileSort
/**
 * 
 */
private constructor() : Comparator<FileItem> {
    /**
     * 
     * 
     */
    override fun compare(object1: FileItem, object2: FileItem): Int {
        var result = 0
        when (sortType) {
            FILESORT_TYPE_DIR -> if (ascending == FILESORT_ASCENDING) {
                result = compareByDirUp(object1, object2)
            } else {
                result = compareByDirDown(object1, object2)
            }

            FILESORT_TYPE_MODIFIED_DATE -> if (ascending == FILESORT_ASCENDING) {
                result = compareByModifiedDateUp(object1, object2)
            } else {
                result = compareByModifiedDateDown(object1, object2)
            }

            FILESORT_TYPE_SIZE -> if (ascending == FILESORT_ASCENDING) {
                result = compareBySizeUp(object1, object2)
            } else {
                result = compareBySizeDown(object1, object2)
            }

            FILESORT_TYPE_NAME -> if (ascending == FILESORT_ASCENDING) {
                result = comparaByNameUp(object1, object2)
            } else {
                result = comparaByNameDown(object1, object2)
            }

            else -> {}
        }
        return result
    }

    /**
     * 按文件名降序排序
     * @param object1
     * @param object2
     * @return
     */
    private fun comparaByNameDown(object1: FileItem, object2: FileItem): Int {
        val file1 = object1.getFile()
        val file2 = object2.getFile()
        if (file1!!.isDirectory() && file2!!.isFile()) {
            return 1
        }
        if (file1.isFile() && file2!!.isDirectory()) {
            return -1
        }
        val fileName1 = object1.getFileName()
        val fileName2 = object2.getFileName()
        if (fileName1.compareTo(fileName2, ignoreCase = true) < 0) {
            return 1
        } else if (fileName1.compareTo(fileName2, ignoreCase = true) > 0) {
            return -1
        } else {
            return 0
        }
    }

    /**
     * 按文件名升序排序
     * @param object1
     * @param object2
     * @return
     */
    private fun comparaByNameUp(object1: FileItem, object2: FileItem): Int {
        val file1 = object1.getFile()
        val file2 = object2.getFile()
        if (file1!!.isDirectory() && file2!!.isFile()) {
            return -1
        }
        if (file1.isFile() && file2!!.isDirectory()) {
            return 1
        }
        val fileName1 = object1.getFileName()
        val fileName2 = object2.getFileName()
        if (fileName1.compareTo(fileName2, ignoreCase = true) > 0) {
            return 1
        } else if (fileName1.compareTo(fileName2, ignoreCase = true) < 0) {
            return -1
        } else {
            return 0
        }
    }

    /**
     * 按文件修改时间，降序排序
     * @param object1
     * @param object2
     * @return
     */
    private fun compareByModifiedDateDown(object1: FileItem, object2: FileItem): Int {
        val file1 = object1.getFile()
        val file2 = object2.getFile()
        if (file1!!.isDirectory() && file2!!.isFile()) {
            return 1
        }
        if (file1.isFile() && file2!!.isDirectory()) {
            return -1
        }
        val d1 = object1.getFile()!!.lastModified()
        val d2 = object2.getFile()!!.lastModified()
        if (d1 == d2) {
            return comparaByNameDown(object1, object2)
        } else {
            return if (d1 < d2) 1 else -1
        }
    }

    /**
     * 按文件修改时间，升序排序
     * @param object1
     * @param object2
     * @return
     */
    private fun compareByModifiedDateUp(object1: FileItem, object2: FileItem): Int {
        val file1 = object1.getFile()
        val file2 = object2.getFile()
        if (file1!!.isDirectory() && file2!!.isFile()) {
            return -1
        }
        if (file1.isFile() && file2!!.isDirectory()) {
            return 1
        }
        val d1 = object1.getFile()!!.lastModified()
        val d2 = object2.getFile()!!.lastModified()
        if (d1 == d2) {
            return comparaByNameUp(object1, object2)
        } else {
            return if (d1 > d2) 1 else -1
        }
    }

    /**
     * 按文件大小，降序排序
     * @param object1
     * @param object2
     * @return
     */
    private fun compareBySizeDown(object1: FileItem, object2: FileItem): Int {
        val file1 = object1.getFile()
        val file2 = object2.getFile()
        if (file1!!.isDirectory() && file2!!.isFile()) {
            return 1
        }
        if (file1.isDirectory() && file2!!.isDirectory()) {
            return comparaByNameDown(object1, object2)
        }
        if (file1.isFile() && file2!!.isDirectory()) {
            return -1
        }
        val s1 = file1.length()
        val s2 = file2!!.length()
        if (s1 == s2) {
            return comparaByNameDown(object1, object2)
        } else {
            return if (s1 < s2) 1 else -1
        }
    }

    /**
     * 按文件大小，升序排序
     * @param object1
     * @param object2
     * @return
     */
    private fun compareBySizeUp(object1: FileItem, object2: FileItem): Int {
        val file1 = object1.getFile()
        val file2 = object2.getFile()
        if (file1!!.isDirectory() && file2!!.isFile()) {
            return -1
        }
        if (file1.isDirectory() && file2!!.isDirectory()) {
            return comparaByNameUp(object1, object2)
        }
        if (file1.isFile() && file2!!.isDirectory()) {
            return 1
        }
        val s1 = file1.length()
        val s2 = file2!!.length()
        if (s1 == s2) {
            return comparaByNameUp(object1, object2)
        } else {
            return if (s1 > s2) 1 else -1
        }
    }

    /**
     * 按目录，文件排序
     * @param object1
     * @param object2
     * @return
     */
    private fun compareByDirDown(object1: FileItem, object2: FileItem): Int {
        val file1 = object1.getFile()
        val file2 = object2.getFile()
        if (file1!!.isDirectory() && file2!!.isFile()) {
            return 1
        } else if (file1.isDirectory() && file2!!.isDirectory()) {
            return comparaByNameDown(object1, object2)
        } else if (file1.isFile() && file2!!.isDirectory()) {
            return -1
        } else {
            val type1 = getFileType(object1.getFileName())
            val type2 = getFileType(object2.getFileName())
            if (type1 == type2) {
                return comparaByNameDown(object1, object2)
            } else {
                return if (type1 < type2) 1 else -1
            }
        }
    }

    /**
     * 按目录，文件排序
     * @param object1
     * @param object2
     * @return
     */
    private fun compareByDirUp(object1: FileItem, object2: FileItem): Int {
        val file1 = object1.getFile()
        val file2 = object2.getFile()
        if (file1!!.isDirectory() && file2!!.isFile()) {
            return -1
        } else if (file1.isDirectory() && file2!!.isDirectory()) {
            return comparaByNameUp(object1, object2)
        } else if (file1.isFile() && file2!!.isDirectory()) {
            return 1
        } else {
            val type1 = getFileType(object1.getFileName())
            val type2 = getFileType(object2.getFileName())
            if (type1 == type2) {
                return comparaByNameUp(object1, object2)
            } else {
                return if (type1 > type2) 1 else -1
            }
        }
    }

    /**
     * get file type
     * @param fileName
     * @return
     */
    fun getFileType(fileName: String): Int {
        var fileName = fileName
        fileName = fileName.lowercase(Locale.getDefault())
        // doc
        if (fileName.endsWith(MainConstant.FILE_TYPE_DOC)
            || fileName.endsWith(MainConstant.FILE_TYPE_DOT)
        ) {
            return FILE_TYPE_DOC
        } else if (fileName.endsWith(MainConstant.FILE_TYPE_DOCX)
            || fileName.endsWith(MainConstant.FILE_TYPE_DOTX)
            || fileName.endsWith(MainConstant.FILE_TYPE_DOTM)
        ) {
            return FILE_TYPE_DOCX
        } else if (fileName.endsWith(MainConstant.FILE_TYPE_XLS)
            || fileName.endsWith(MainConstant.FILE_TYPE_XLT)
        ) {
            return FILE_TYPE_XLS
        } else if (fileName.endsWith(MainConstant.FILE_TYPE_XLSX)
            || fileName.endsWith(MainConstant.FILE_TYPE_XLTX)
            || fileName.endsWith(MainConstant.FILE_TYPE_XLTM)
            || fileName.endsWith(MainConstant.FILE_TYPE_XLSM)
        ) {
            return FILE_TYPE_XLSX
        } else if (fileName.endsWith(MainConstant.FILE_TYPE_PPT)
            || fileName.endsWith(MainConstant.FILE_TYPE_POT)
        ) {
            return FILE_TYPE_PPT
        } else if (fileName.endsWith(MainConstant.FILE_TYPE_PPTX)
            || fileName.endsWith(MainConstant.FILE_TYPE_PPTM)
            || fileName.endsWith(MainConstant.FILE_TYPE_POTX)
            || fileName.endsWith(MainConstant.FILE_TYPE_POTM)
        ) {
            return FILE_TYPE_PPTX
        } else if (fileName.endsWith(MainConstant.FILE_TYPE_PDF)) {
            return FILE_TYPE_PDF
        } else {
            return FILE_TYPE_TXT
        }
    }

    /**
     * 设置排序类型
     * @param typeName
     * @param childName
     */
    fun setType(type: Int, ascending: Int) {
        sortType = type
        this.ascending = ascending
    }

    /**
     * 
     */
    fun dispose() {
    }

    //排序类型
    private var sortType = 0

    //升降序
    private var ascending = 0

    companion object {
        // 不改变排列顺序
        @JvmField
        val FILESORT_TYPE_NULL: Int = -1

        // 按目录，文件排序
        const val FILESORT_TYPE_DIR: Int = 0

        // 按修改时间排序
        const val FILESORT_TYPE_MODIFIED_DATE: Int = 1

        // 按修文件大小排序
        const val FILESORT_TYPE_SIZE: Int = 2

        // 按文件名排序
        const val FILESORT_TYPE_NAME: Int = 3

        // 按升序排序
        const val FILESORT_ASCENDING: Int = 0

        // 按降序排序
        const val FILESORT_TYPE_DESCENDING: Int = 1

        // doc文档格式
        const val FILE_TYPE_DOC: Int = 0

        // docx文档格式
        const val FILE_TYPE_DOCX: Int = 1

        // xls文档格式
        const val FILE_TYPE_XLS: Int = 2

        // xlsx文档格式
        const val FILE_TYPE_XLSX: Int = 3

        // ppt文档格式
        const val FILE_TYPE_PPT: Int = 4

        // pptx文档格式
        const val FILE_TYPE_PPTX: Int = 5

        // txt文档格式
        const val FILE_TYPE_TXT: Int = 6

        //
        const val FILE_TYPE_PDF: Int = 7

        //
        private val mt = FileSort()

        //instance
        @JvmStatic
        fun instance(): FileSort {
            return mt
        }
    }
}
