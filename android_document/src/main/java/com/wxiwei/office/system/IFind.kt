package com.wxiwei.office.system

interface IFind {
    fun find(value: String?): Boolean
    fun findBackward(): Boolean
    fun findForward(): Boolean
    fun getPageIndex(): Int
    fun resetSearchResult()
    fun dispose()
}
