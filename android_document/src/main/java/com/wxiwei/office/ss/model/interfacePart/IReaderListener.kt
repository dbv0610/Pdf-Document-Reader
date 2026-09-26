/*
 * 文件名称:          IWorkbookHandler.java
 *
 * 编译器:            android2.2
 * 时间:              上午11:19:31
 */
package com.wxiwei.office.ss.model.interfacePart

/**
 * TODO: 文件注释
 *
 * Read版本:        Read V1.0
 * 作者:            jqin
 * 日期:            2012-3-15
 * 负责人:           jqin
 */
interface IReaderListener {
    fun OnReadingFinished()

    fun OnReadingProgress() {}
}
