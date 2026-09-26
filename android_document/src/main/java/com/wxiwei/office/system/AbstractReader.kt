package com.wxiwei.office.system

import java.io.File

open class AbstractReader : IReader {
    @Throws(Exception::class)
    override fun getModel(): Any? = null

    @Throws(Exception::class)
    override fun searchContent(file: File?, key: String): Boolean = false

    override fun isReaderFinish(): Boolean = true

    @Throws(Exception::class)
    override fun backReader() {
    }

    override fun abortReader() {
        abortReader = true
    }

    override fun isAborted(): Boolean = abortReader

    override fun getControl(): IControl = control!!

    override fun dispose() {
        control = null
    }

    @JvmField
    protected var abortReader: Boolean = false

    @JvmField
    protected var control: IControl? = null
}
