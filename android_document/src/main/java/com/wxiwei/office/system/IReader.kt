package com.wxiwei.office.system

import java.io.File

interface IReader {
    @Throws(Exception::class)
    fun getModel(): Any?

    @Throws(Exception::class)
    fun searchContent(file: File?, key: String): Boolean

    fun isReaderFinish(): Boolean

    @Throws(Exception::class)
    fun backReader()

    fun abortReader()

    fun isAborted(): Boolean

    fun getControl(): IControl

    fun dispose()
}
