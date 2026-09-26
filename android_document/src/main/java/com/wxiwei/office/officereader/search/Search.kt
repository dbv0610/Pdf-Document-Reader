/*
 * 文件名称:          HighSearch.java
 *  
 * 编译器:            android2.2
 * 时间:              上午10:51:56
 */
package com.wxiwei.office.officereader.search

import com.wxiwei.office.constant.MainConstant
import com.wxiwei.office.fc.doc.DOCReader
import com.wxiwei.office.fc.doc.DOCXReader
import com.wxiwei.office.fc.doc.TXTReader
import com.wxiwei.office.fc.ppt.PPTReader
import com.wxiwei.office.fc.ppt.PPTXReader
import com.wxiwei.office.fc.xls.XLSReader
import com.wxiwei.office.fc.xls.XLSXReader
import com.wxiwei.office.system.AbortReaderError
import com.wxiwei.office.system.FileKit.Companion.instance
import com.wxiwei.office.system.IControl
import com.wxiwei.office.system.IReader
import com.wxiwei.office.system.OfficeCoroutineExecutor.launch
import kotlinx.coroutines.Job
import java.io.File
import java.util.Locale

/**
 * high level search
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
 * 日期:            2012-3-26
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
class Search
    (control: IControl?, searchResult: ISearchResult?) {
    /**
     * 
     */
    fun doSearch(directory: File, key: String, searchType: Byte) {
        stopSearch = false
        if (searching) {
            return
        }
        searching = true
        searchJob = SearchJob(directory, key, searchType).start()
    }

    /**
     * 
     */
    fun stopSearch() {
        if (reader != null) {
            reader!!.abortReader()
        }

        stopSearch = true
        if (searchJob != null) {
            searchJob!!.cancel(null)
            searchJob = null
        }
    }

    /**
     * search thread
     */
    internal inner class SearchJob
        (//
        private val directory: File, //
        private val key: String, //
        private val searchType: Byte
    ) {
        /**
         * 
         */
        fun start(): Job {
            return launch(object : Runnable {
                override fun run() {
                    searchFiles(directory, key)
                    searching = false
                    if (searchResult != null) {
                        searchResult!!.searchFinish()
                    }
                }
            })
        }

        /**
         * search file
         * @param query
         * @param directory
         * @param fileList
         */
        private fun searchFiles(directory: File, key: String) {
            var key = key
            key = key.lowercase(Locale.getDefault())

            val files = directory.listFiles()
            if (files == null) {
                return
            }
            for (file in files) {
                if (stopSearch) {
                    return
                }
                if (file.isDirectory()) {
                    searchFiles(file, key)
                } else {
                    val fileName = file.getName()
                    if (instance().isSupport(fileName)) {
                        if (searchType == SEARCH_BY_NAME) {
                            if (fileName.lowercase(Locale.getDefault()).indexOf(key) > -1) {
                                searchResult!!.onResult(file)
                            }
                        } else if (searchType == SEARCH_BY_CONTENT) {
                            try {
                                searchContent(file)
                            } catch (e: AbortReaderError) {
                                if (reader != null) {
                                    reader!!.dispose()
                                    reader = null
                                }

                                break
                            } catch (e: Exception) {
                                continue
                            }
                        }
                    }
                }
            }
        }

        /**
         * 
         */
        @Throws(Exception::class)
        private fun searchContent(file: File) {
            val fileName = file.getName().lowercase(Locale.getDefault())
            // doc
            if (fileName.endsWith(MainConstant.FILE_TYPE_DOC)
                || fileName.endsWith(MainConstant.FILE_TYPE_DOT)
            ) {
                reader = DOCReader(null, file.getAbsolutePath())
            } else if (fileName.endsWith(MainConstant.FILE_TYPE_DOCX)
                || fileName.endsWith(MainConstant.FILE_TYPE_DOTX)
                || fileName.endsWith(MainConstant.FILE_TYPE_DOTM)
            ) {
                reader = DOCXReader(null, file.getAbsolutePath())
            } else if (fileName.endsWith(MainConstant.FILE_TYPE_TXT)) {
                reader = TXTReader(null, file.getAbsolutePath(), "GBK")
                reader!!.dispose()
            } else if (fileName.endsWith(MainConstant.FILE_TYPE_XLS)
                || fileName.endsWith(MainConstant.FILE_TYPE_XLT)
            ) {
                reader = XLSReader(control!!, file.getAbsolutePath())
            } else if (fileName.endsWith(MainConstant.FILE_TYPE_XLSX)
                || fileName.endsWith(MainConstant.FILE_TYPE_XLTX)
                || fileName.endsWith(MainConstant.FILE_TYPE_XLTM)
                || fileName.endsWith(MainConstant.FILE_TYPE_XLSM)
            ) {
                reader = XLSXReader(control!!, file.getAbsolutePath())
            } else if (fileName.endsWith(MainConstant.FILE_TYPE_PPT)
                || fileName.endsWith(MainConstant.FILE_TYPE_POT)
            ) {
                reader = PPTReader(control, file.getAbsolutePath())
            } else if (fileName.endsWith(MainConstant.FILE_TYPE_PPTX)
                || fileName.endsWith(MainConstant.FILE_TYPE_PPTM)
                || fileName.endsWith(MainConstant.FILE_TYPE_POTX)
                || fileName.endsWith(MainConstant.FILE_TYPE_POTM)
            ) {
                reader = PPTXReader(control, file.getAbsolutePath())
            }
            reader!!.dispose()
            reader = null
        }

    }

    /**
     * 
     */
    fun dispose() {
        //searchResult = null;
        control = null
        searchResult = null
        reader = null
    }

    //
    private var stopSearch = false

    //
    private var searching = false
    private var searchJob: Job? = null

    //
    private var searchResult: ISearchResult?

    //
    private var control: IControl?

    //search content reader
    private var reader: IReader?

    /**
     * 
     */
    init {
        this.control = control
        this.searchResult = searchResult

        reader = null
    }


    companion object {
        // by name
        const val SEARCH_BY_NAME: Byte = 0

        // by content
        const val SEARCH_BY_CONTENT: Byte = 1

        // by author
        const val SEARCH_BY_AUTHOR: Byte = 2
    }
}
