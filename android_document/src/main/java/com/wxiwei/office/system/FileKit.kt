package com.wxiwei.office.system

import com.wxiwei.office.constant.MainConstant
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class FileKit private constructor() {
    fun deleteFile(file: File) {
        if (file.isDirectory) {
            val files = file.listFiles()
            if (files != null) {
                for (tempFile in files) {
                    deleteFile(tempFile)
                }
            }
            file.delete()
        } else {
            file.delete()
        }
    }

    fun pasteFile(fromFile: File, toFile: File) {
        if (fromFile.isDirectory) {
            copyFolder(fromFile, toFile)
        } else {
            copyFile(fromFile, toFile)
        }
    }

    fun copyFile(fromFile: File, toFile: File) {
        if (toFile.exists()) {
            toFile.delete()
        }
        try {
            val fosfrom = FileInputStream(fromFile)
            val fosto = FileOutputStream(toFile)
            val bt = ByteArray(8192)
            var c: Int
            while (fosfrom.read(bt).also { c = it } > 0) {
                fosto.write(bt, 0, c)
            }
            fosfrom.close()
            fosto.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun copyFolder(fromFile: File, toFile: File) {
        if (!toFile.exists()) {
            toFile.mkdir()
        }
        val toPath = toFile.absolutePath
        val files = fromFile.listFiles()
        if (files != null) {
            for (tempFile in files) {
                if (toPath.endsWith(File.separator)) {
                    pasteFile(tempFile, File(toPath + tempFile.name))
                } else {
                    pasteFile(tempFile, File(toPath + File.separator + tempFile.name))
                }
            }
        }
    }

    fun isSupport(fileName: String?): Boolean {
        val lowerName = fileName!!.lowercase()
        return lowerName.indexOf(".") > 0 &&
            (lowerName.endsWith(MainConstant.FILE_TYPE_DOC) ||
                lowerName.endsWith(MainConstant.FILE_TYPE_DOCX) ||
                lowerName.endsWith(MainConstant.FILE_TYPE_XLS) ||
                lowerName.endsWith(MainConstant.FILE_TYPE_XLSX) ||
                lowerName.endsWith(MainConstant.FILE_TYPE_PPT) ||
                lowerName.endsWith(MainConstant.FILE_TYPE_PPTX) ||
                lowerName.endsWith(MainConstant.FILE_TYPE_TXT) ||
                lowerName.endsWith(MainConstant.FILE_TYPE_DOT) ||
                lowerName.endsWith(MainConstant.FILE_TYPE_DOTX) ||
                lowerName.endsWith(MainConstant.FILE_TYPE_DOTM) ||
                lowerName.endsWith(MainConstant.FILE_TYPE_XLT) ||
                lowerName.endsWith(MainConstant.FILE_TYPE_XLTX) ||
                lowerName.endsWith(MainConstant.FILE_TYPE_XLTM) ||
                lowerName.endsWith(MainConstant.FILE_TYPE_XLSM) ||
                lowerName.endsWith(MainConstant.FILE_TYPE_POT) ||
                lowerName.endsWith(MainConstant.FILE_TYPE_PPTM) ||
                lowerName.endsWith(MainConstant.FILE_TYPE_POTX) ||
                lowerName.endsWith(MainConstant.FILE_TYPE_POTM))
    }

    fun isFileMarked(filePath: String?, fileList: List<File>?): Boolean {
        if (filePath == null || fileList == null || fileList.isEmpty()) {
            return false
        }
        for (file in fileList) {
            if (filePath == file.absolutePath) {
                return true
            }
        }
        return false
    }

    companion object {
        private val mt = FileKit()

        @JvmStatic
        fun instance(): FileKit = mt
    }
}
