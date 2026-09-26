package com.wxiwei.office.officereader.search

import java.io.File

interface ISearchResult {
    fun onResult(file: File)
    fun searchFinish()
}
